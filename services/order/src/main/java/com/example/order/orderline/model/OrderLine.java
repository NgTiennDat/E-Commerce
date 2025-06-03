package com.example.order.orderline.model;

import com.example.order.order.model.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_line")
public class OrderLine {
    @Id
    @GeneratedValue
    public Integer id;
    public Integer productId;
    public double quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

}
