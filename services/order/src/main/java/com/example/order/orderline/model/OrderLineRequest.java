package com.example.order.orderline.model;

import lombok.Data;

@Data
public class OrderLineRequest {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private double quantity;
}
