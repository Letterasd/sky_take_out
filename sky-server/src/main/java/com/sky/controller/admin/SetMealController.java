package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    /**
     * 添加套餐
     * @param dto
     * @return
     */
    @PostMapping
    public Result save(@RequestBody SetmealDTO dto){
        log.info("添加的套餐数据为:{}",dto);
        setMealService.saveWithDish(dto);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param dto
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO dto){
        log.info("套餐分页查询:{}",dto);
        PageResult pageResult= setMealService.pageQuery(dto);
        return Result.success(pageResult);
    }
}
