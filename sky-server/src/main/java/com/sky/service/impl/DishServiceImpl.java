package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId=dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dto);
        return new PageResult(page.getTotal(),page.getResult());

    }

    /**
     * 菜品批量删除功能
     * @param list
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> list) {
        //判断当前菜品是否可删除--状态
        for (Long id : list) {
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()==StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否可删除--关联套餐
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(list);
        if (setmealIdsByDishIds!=null&&setmealIdsByDishIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        //删除dish
//        for (Long id : list) {
//            dishMapper.deleteById(id);
//            //删除dish关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //根据菜品id集合批量删除菜品数据
        dishMapper.deleteByIds(list);
        //根据菜品id集合批量删除口味数据
        dishFlavorMapper.deleteByDishIds(list);

    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //先查询菜品的数据
        Dish dish = dishMapper.getById(id);
        //再通过id查询到对应的口味数据
        List<DishFlavor> dishFlavorList= dishFlavorMapper.getByDishId(id);
        //最后都包装到DishVO即可
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表信息
        dishMapper.update(dish);
        //删除原有口味数据再插入口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {

        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> list=dishMapper.selectByCategoryId(dish);
        return list;
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.selectByCategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 起售或者停售菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //如果要停售 先判断关联的套餐是否起售
        if (status==StatusConstant.DISABLE){
           List<Setmeal> setmeals=setmealMapper.getByDishId(id);
           if (setmeals!=null&&setmeals.size()>0){
               setmeals.forEach(setmeal -> {
                   if (setmeal.getStatus()==StatusConstant.ENABLE){
                       throw new SetmealEnableFailedException(MessageConstant.DISH_DISABLE_FAILED);
                   }
               });
           }
        }
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateStatus(dish);
    }
}
