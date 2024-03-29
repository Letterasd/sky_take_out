package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态接口")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为:{}",status==1?"营业中":"已打烊");
        redisTemplate.opsForValue().set("SHOP_STATUS",status.toString());
        return Result.success();
    }

    /**
     * 查询店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺营业状态接口")
    public Result<Integer> getStatus(){
        log.info("查询店铺营业状态");
        String status = (String) redisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success(Integer.valueOf(status));
    }
}
