package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayStateEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;


    public Long createOrder(OrderDto orderDto) {
        // 1.新增订单
        Order order = new Order();
        // 1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());
        // 1.2 用户信息
        UserInfo user = UserInterceptor.getLoginUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        // 1.3 收货人地址
        AddressDTO addr = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        // 1.4金额
        Map<Long, Integer> numMap = orderDto.getCarts().stream().collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));
        //获取所有sku的id
        Set<Long> ids = numMap.keySet();
        //根据id查询skus
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        //准备orderDetaj集合
        List<OrderDetail> details = new ArrayList<>();
        long totalPay = 0L;
        for (Sku sku : skus) {
            //  删除购物车
//            cartClient.deleteCartById(String.valueOf(sku.getId()));   //TODO 401异常
//            try {
//            cartClient.deleteCartById(sku.getId().toString());
//            }catch (Exception e){
//                log.error("[创建订单服务] 删除购物车商品异常，{}",e);
//            }
            //计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetai
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());

            details.add(orderDetail);
        }
        order.setTotalPay(totalPay);
        //实付金额 = 总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);
        // 1.5 把order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 2 新增订单详情
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单] 创建订单详情失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_DETAIL_ERROR);
        }
        // 3 新增订单状态
        OrderStatus status = new OrderStatus();
        status.setCreateTime(order.getCreateTime());
        status.setOrderId(orderId);
        status.setStatus(OrderStatusEnum.UN_PAY.getCode());
        count = statusMapper.insertSelective(status);
        if (count != 1) {
            log.error("[创建订单] 创建订单状态失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_STATUS_ERROR);
        }
        /*
            这里采用了乐观锁。
            因为要调用不同的微服务，所以这一行代码只能加在最后。
            通过其他微服务来检查库存异常，库存不足，抛出异常，整个方法回滚。
            如果加在前边，其他代码出错，这个方法回滚，但是这一行代码调用了其他微服务，不能让其他微服务也回滚。
         */
        // 4 库存减少
        List<CartDto> cartDtos =orderDto.getCarts();
        goodsClient.decreaseStock(cartDtos);

        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> orderDetails = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);

        return order;
    }

    public String creatPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //实际金额
//        Long actualPay = order.getActualPay();
        Long actualPay = 1L;        //TODO  这里设置为1分钱，仅为测试使用
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String title = detail.getTitle();
        return payHelper.createPayUrl(orderId,actualPay,title);
    }

    public void handleNotify(Map<String, String> result) {
        // 数据校验
        payHelper.isSuccess(result);
        // 校验签名
        payHelper.isValidSign(result);
        // 检验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)) {
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 获取微信支付结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);

        //获取订单金额
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        // TODO 这里应该是判断是否等于 order.getActualPay()才对，因为前面支付测试是1分钱，所以这里也是一分钱
        if (totalFee != /*order.getActualPay()*/ 1L) {
            //微信支付金额与订单金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //修改订单状态
        payHelper.queryOrderState(orderId);
        log.info("[订单回调] 订单支付成功，订单编号:{}",orderId);
    }

    public PayStateEnum queryOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (status != OrderStatusEnum.UN_PAY.getCode()) {
            //如果订单表里是已支付状态，就一定是已支付
            return PayStateEnum.SUCCESS;
        }
            //如果订单表里是未支付，不一定是未支付
        return payHelper.queryOrderState(orderId);
    }

    public List<Order> queryOrdersByUserId(Integer page, Integer rows) {
        //分页
        PageHelper.startPage(page, rows);
        //TODO 未完成
        return null;
    }
}
