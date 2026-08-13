package com.manisha.manishamart.dao;
import com.manisha.manishamart.model.CartItem;
import java.sql.SQLException;
import java.util.List;
public interface CartDAO {
    CartItem create(CartItem item) throws SQLException;
    CartItem findById(long id) throws SQLException;
    CartItem findByUserAndProduct(long userId, long productId) throws SQLException;
    List<CartItem> findByUserId(long userId) throws SQLException;
    void updateQuantity(long id, int quantity) throws SQLException;
    void delete(long id) throws SQLException;
    void deleteAllByUserId(long userId) throws SQLException;
}
