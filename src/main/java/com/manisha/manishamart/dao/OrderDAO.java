package com.manisha.manishamart.dao;
import com.manisha.manishamart.model.Order;
import com.manisha.manishamart.model.OrderItem;
import java.sql.SQLException;
import java.util.List;
public interface OrderDAO {
    Order createOrderWithItems(Order order, List<OrderItem> items) throws SQLException;
    Order findById(long id) throws SQLException;
    List<Order> findByBuyerId(long buyerId) throws SQLException;
    List<Order> findBySellerId(long sellerId) throws SQLException;
    List<OrderItem> findItemsByOrderId(long orderId) throws SQLException;
    void updateStatus(long orderId, Order.Status status) throws SQLException;
}
