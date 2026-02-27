package com.dailycodework.buynowdotcom.service.order;

import com.dailycodework.buynowdotcom.dtos.OrderDto;
import com.dailycodework.buynowdotcom.model.Order;
import com.dailycodework.buynowdotcom.request.PaymentRequest;
import com.stripe.exception.StripeException;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    List<OrderDto> getUserOrders(Long userId);


    String createPaymentIntent(PaymentRequest request) throws StripeException;

    OrderDto convertToDto(Order order);
}
