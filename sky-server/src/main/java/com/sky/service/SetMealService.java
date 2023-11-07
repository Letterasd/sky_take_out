package com.sky.service;

import com.sky.dto.SetmealDTO;

public interface SetMealService {


    /**
     * 添加套餐和对应的套餐菜品关系表
     * @param dto
     */
    void saveWithDish(SetmealDTO dto);
}
