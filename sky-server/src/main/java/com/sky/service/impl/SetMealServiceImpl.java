package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;
    /**
     * 添加套餐和对应的套餐菜品关系表
     * @param dto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO dto) {
        //先把DTO的数据弄到对应的entity类中，比如SetMeal和SetMealDish 这是要插入到两个表里的entity
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto,setmeal);

        //此时拿到了两个可插入的数据 但是新的问题也随之而来,插入到SetMealDish关系表中的数据中
        //首先最重要的一点是，关系表中的套餐id在插入前都是未知的，而菜品是已知的，也就是说
        //前端不会传来套餐id，套餐id还需要自己获取
        //此时拿到了id值了 insert中写好了 封装到setmeal里了
        setmealMapper.insert(setmeal);
        //现在只需要把id值设在SetmealDish即可
        List<SetmealDish> setmealDishes =dto.getSetmealDishes();
        if (setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<SetmealVO> page= setmealMapper.pageQuery(dto);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 根据id删除套餐和对应关系表
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatchs(List<Long> ids) {
        //判断当前套餐是否可删除 即是否在售中
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据套餐id查询
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        SetmealVO setmealVO=new SetmealVO();
        //先根据id查找到对应的套餐数据
        Setmeal setmeal=setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        //最终是要封装到VO里面 所以要把对应的数据准备齐全
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        log.info("到这里了吗");
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //多的就是这一步
        if (setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
//        //判断如果是要起售 当前套餐下的菜品是否有停售 如果有则报错信息
//        if (status==StatusConstant.ENABLE){
//            //通过id获取setmealDish表中的dishId 然后调用dishIDmapper看看status是否为启用才行
//            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
//            //遍历拿到的每一个关联表项目
//            setmealDishes.forEach(setmealDish -> {
//                //这里用的是dishMapper根据dishid去查找每个数据库中的dish
//                Dish dish = dishMapper.getById(setmealDish.getDishId());
//                //然后判断这个dish是否status为disable 完事
//                if (dish.getStatus()==StatusConstant.DISABLE)
//                    //如果真是那就直接抛出异常了 甚至不用return
//                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
//            });
//        }
        //判断如果是要起售 当前套餐下的菜品是否有停售 如果有则报错信息
        if (status==StatusConstant.ENABLE){
            List<Dish> dishes=dishMapper.getBySetmealId(id);
            if (dishes!=null&&dishes.size()>0){
                dishes.forEach(dish -> {
                    //拿到每一个dish看看里面是不是有未起售的
                    if (dish.getStatus()==StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        //遍历完都没问题那就可以正常插入了
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        //其实也可以传过去status和id 只不过这样不规范 所以传过去的还是一个setmeal好 那就build一个
        setmealMapper.updateStatus(setmeal);
    }
}
