package com.manisha.manishamart.service;
import com.manisha.manishamart.dao.CartDAO;
import com.manisha.manishamart.dao.OrderDAO;
import com.manisha.manishamart.dao.ProductDAO;
import com.manisha.manishamart.model.CartItem;
import com.manisha.manishamart.model.Order;
import com.manisha.manishamart.model.OrderItem;
import com.manisha.manishamart.model.Product;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public Order placeOrder(long buyerId) throws SQLException {
        List<CartItem> cartItems = cartDAO.findByUserId(buyerId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            Product product = productDAO.findById(ci.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Product no longer available: " + ci.getProductId());
            }
            if (product.getStockQty() < ci.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(product.getPrice());
            orderItems.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(Order.Status.PENDING);
        order.setTotalAmount(total);

        // Mock payment confirmation step (Section 1 scope constraint: no real gateway)
        boolean paymentConfirmed = true;
        if (!paymentConfirmed) {
            throw new IllegalStateException("Mock payment confirmation failed");
        }

        Order created = orderDAO.createOrderWithItems(order, orderItems);
        cartDAO.deleteAllByUserId(buyerId);
        return created;
    }

    public Order getOrder(long orderId) throws SQLException {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    public List<OrderItem> getOrderItems(long orderId) throws SQLException {
        return orderDAO.findItemsByOrderId(orderId);
    }

    public List<Order> getBuyerOrders(long buyerId) throws SQLException {
        return orderDAO.findByBuyerId(buyerId);
    }

    public List<Order> getSellerOrders(long sellerId) throws SQLException {
        return orderDAO.findBySellerId(sellerId);
    }

    public void updateStatus(long orderId, Order.Status status) throws SQLException {
        orderDAO.updateStatus(orderId, status);
    }
                           }
