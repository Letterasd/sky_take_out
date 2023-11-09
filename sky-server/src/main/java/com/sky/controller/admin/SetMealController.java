package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO dto){
        log.info("套餐分页查询:{}",dto);
        PageResult pageResult= setMealService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam("ids") List<Long> ids){
        setMealService.deleteBatchs(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO=setMealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }
}
