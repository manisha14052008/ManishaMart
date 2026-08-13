package com.manisha.manishamart.dao.impl;
import com.manisha.manishamart.dao.CartDAO;
import com.manisha.manishamart.model.CartItem;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CartDAOImpl implements CartDAO {

    private final DataSource dataSource;

    public CartDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CartItem create(CartItem item) throws SQLException {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getUserId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
            return item;
        }
    }

    @Override
    public CartItem findById(long id) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public CartItem findByUserAndProduct(long userId, long productId) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<CartItem> findByUserId(long userId) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE user_id = ?";
        List<CartItem> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public void updateQuantity(long id, int quantity) throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteAllByUserId(long userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    private CartItem mapRow(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        return item;
    }
                       }
