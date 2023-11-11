package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetMealService {


    /**
     * 添加套餐和对应的套餐菜品关系表
     * @param dto
     */
    void saveWithDish(SetmealDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO dto);

    /**
     * 根据id删除套餐和对应关系表
     * @param ids
     */
    void deleteBatchs(List<Long> ids);

    /**
     * 根据套餐id查询
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void updateWithDishes(SetmealDTO setmealDTO);

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
