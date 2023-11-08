package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

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
}
