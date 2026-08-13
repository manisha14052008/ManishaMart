package com.manisha.manishamart.dao.impl;

import com.manisha.manishamart.dao.OrderDAO;
import com.manisha.manishamart.model.Order;
import com.manisha.manishamart.model.OrderItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {

    private final DataSource dataSource;

    public OrderDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order createOrderWithItems(Order order, List<OrderItem> items) throws SQLException {
        String insertOrder = "INSERT INTO orders (buyer_id, status, total_amount, created_at) " +
                              "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        String insertItem = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) " +
                             "VALUES (?, ?, ?, ?)";
        String decrementStock = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, order.getBuyerId());
                ps.setString(2, order.getStatus().name());
                ps.setBigDecimal(3, order.getTotalAmount());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setId(rs.getLong(1));
                    }
                }
            }

            try (PreparedStatement itemPs = conn.prepareStatement(insertItem);
                 PreparedStatement stockPs = conn.prepareStatement(decrementStock)) {
                for (OrderItem item : items) {
                    itemPs.setLong(1, order.getId());
                    itemPs.setLong(2, item.getProductId());
                    itemPs.setInt(3, item.getQuantity());
                    itemPs.setBigDecimal(4, item.getUnitPrice());
                    itemPs.addBatch();

                    stockPs.setInt(1, item.getQuantity());
                    stockPs.setLong(2, item.getProductId());
                    stockPs.setInt(3, item.getQuantity());
                    int updated = stockPs.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Insufficient stock for product id " + item.getProductId());
                    }
                }
                itemPs.executeBatch();
            }

            conn.commit();
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    @Override
    public Order findById(long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Order> findByBuyerId(long buyerId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE buyer_id = ? ORDER BY created_at DESC";
        List<Order> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public List<Order> findBySellerId(long sellerId) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM orders o " +
                     "JOIN order_items oi ON o.id = oi.order_id " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE p.seller_id = ? ORDER BY o.created_at DESC";
        List<Order> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public List<OrderItem> findItemsByOrderId(long orderId) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    results.add(item);
                }
            }
        }
        return results;
    }

    @Override
    public void updateStatus(long orderId, Order.Status status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            ps.executeUpdate();
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setStatus(Order.Status.valueOf(rs.getString("status")));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return o;
    }
                  }
