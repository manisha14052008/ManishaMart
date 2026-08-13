package com.manisha.manishamart.dao;

import com.manisha.manishamart.model.Product;
import java.sql.SQLException;
import java.util.List;

public interface ProductDAO {
    Product create(Product product) throws SQLException;
    Product findById(long id) throws SQLException;
    List<Product> findAll() throws SQLException;
    List<Product> findByCategory(String category) throws SQLException;
    List<Product> searchByKeyword(String keyword) throws SQLException;
    List<Product> findBySellerId(long sellerId) throws SQLException;
    void update(Product product) throws SQLException;
    void delete(long id) throws SQLException;
}
