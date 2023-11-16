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
import org.springframework.cache.annotation.CacheEvict;
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
    @CacheEvict(cacheNames = "setmealCache",key = "#dto.categoryId")
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

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam("ids") List<Long> ids){
        setMealService.deleteBatchs(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO=setMealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("要修改的信息为:{}",setmealDTO);
        setMealService.updateWithDishes(setmealDTO);
        return Result.success();
    }

    /**
     * 起售停售套餐
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售套餐接口")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        setMealService.startOrStop(status,id);
        return Result.success();
    }


}
