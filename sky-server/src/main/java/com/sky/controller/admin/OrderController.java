package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api("订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 订单搜索
     * @param dto
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO dto){
        PageResult pageResult=orderService.conditionSearch(dto);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO ov=orderService.statistics();
        return Result.success(ov);
    }

    /**
     * 查询订单信息
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单信息")
    public Result<OrderVO> details(@PathVariable Long id){
        OrderVO orderVO=orderService.details(id);
        return Result.success(orderVO);
    }
    /**
     * 接单
     *
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO dto){
        orderService.confirm(dto);
        return Result.success();
    }

    /**
     * 拒单
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result Rejection(@RequestBody OrdersRejectionDTO dto){
        orderService.rejection(dto);
        return Result.success();
    }

    /**
     * 取消订单
     * @param dto
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result Cancel(@RequestBody OrdersCancelDTO dto){
        orderService.adminCancel(dto);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }

}
