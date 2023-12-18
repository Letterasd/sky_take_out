package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dto);

    /**
     * 根据主键查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id =#{id}")
    Dish getById(Long id);

    /**
     * 根据主键删除菜品数据
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据id集合批量删除菜品数据
     * @param list
     */
    void deleteByIds(List<Long> list);

    /**
     * 根据id动态修改菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 动态sql根据分类查询菜品
     * @param dish
     * @return
     */
    List<Dish> selectByCategoryId(Dish dish);

    /**
     * 根据SetmealId获取Dishes
     * @param id
     * @return
     */
    @Select("select d.* from dish d left join setmeal_dish sd on d.id = sd.dish_id where sd.setmeal_id=#{id}")
    List<Dish> getBySetmealId(Long id);

    @AutoFill(value = OperationType.UPDATE)
    @Update("update dish set status=#{status} where dish.id=#{id}")
    void updateStatus(Dish dish);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
