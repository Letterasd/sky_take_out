package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户下单方法
     * @param submitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO) {
        //处理业务异常
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        if (addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (list==null||list.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //插入订单表的数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(submitDTO,orders);

        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);
        //插入订单明细表 当然是根据购物车数据去插入订单明细啦，而且刚好之前就查询过了
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车数据
        shoppingCartMapper.deleByUserId(userId);
        //返回封装好的vo对象
        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return vo;
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("666");
        vo.setPaySign("hhh");
        vo.setPackageStr("prepay_id=wx");
        vo.setSignType("RSA");
        vo.setTimeStamp("1670380960");

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO pageQueryDTO=new OrdersPageQueryDTO();
        pageQueryDTO.setUserId(BaseContext.getCurrentId());
        pageQueryDTO.setStatus(status);
        //分页条件查询
        Page<Orders> page =orderMapper.pageQuery(pageQueryDTO);
        List<OrderVO> list=new ArrayList<>();
        if (page!=null&&!page.isEmpty()){
            page.forEach(orders -> {
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            });
        }

        return new PageResult(page.getTotal(),list);
    }

    /**
     * 根据id查询订单信息
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders order=orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }
    /**
     * 根据订单id取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        Orders order = orderMapper.getById(id);

        // 校验订单是否存在
        if (order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (order.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders newOrder=new Orders();
        newOrder.setId(order.getId());
        if (order.getStatus()==2){
            //这里的微信退款功能实现不了，直接设置完事！
            newOrder.setPayStatus(Orders.REFUND);
        }

        newOrder.setStatus(Orders.CANCELLED);
        newOrder.setCancelReason("用户取消");
        newOrder.setCancelTime(LocalDateTime.now());
        orderMapper.update(newOrder);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> cartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(cartList);
    }

    /**
     * 订单搜索
     * @param dto
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(dto);

        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(),orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList=new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes=getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        List<String> list = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        return String.join(" ",list);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer confirmed=orderMapper.countStatus(Orders.CONFIRMED);
        Integer toBeConfirmed=orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress=orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;

    }
    /**
     * 接单
     * @param dto
     */
    @Override
    public void confirm(OrdersConfirmDTO dto) {
        Orders orders = Orders.builder()
                .id(dto.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param dto
     */
    @Override
    public void rejection(OrdersRejectionDTO dto) {
        Orders orders = orderMapper.getById(dto.getId());
        if (orders==null||!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = new Orders();
        if (orders.getPayStatus().equals(Orders.PAID)){
            order.setPayStatus(Orders.REFUND);
        }
        //未支付的话就不用改支付状态了 因为就是未支付即可
        //不对，为什么我们不能用这个原来的对象，因为查询出的值都在里面了 而update 它是检测null就不update了
        //如果我们直接设置orders 那么更新的字段将是全部 所以我们需要一个新的对象，里面只有需要update的字段！

        order.setStatus(Orders.CANCELLED);
        order.setId(orders.getId());
        order.setRejectionReason(dto.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    /**
     * 取消订单
     * @param dto
     */
    @Override
    public void adminCancel(OrdersCancelDTO dto) {
        Orders orders = orderMapper.getById(dto.getId());
        Orders order = new Orders();
        if (orders.getPayStatus().equals(Orders.PAID)){
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        order.setId(dto.getId());
        orderMapper.update(order);

    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders==null||!orders.getStatus().equals(Orders.CONFIRMED))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Orders order = new Orders();
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        order.setId(orders.getId());

        orderMapper.update(order);

    }
    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);

        if (orders==null||!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = new Orders();
        order.setStatus(Orders.COMPLETED);
        order.setId(orders.getId());
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(order);

    }

}
