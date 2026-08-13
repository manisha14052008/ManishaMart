package com.manisha.manishamart.service;
import com.manisha.manishamart.dao.ProductDAO;
import com.manisha.manishamart.model.Product;
import com.manisha.manishamart.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;
public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product createProduct(Product product) throws SQLException {
        if (!ValidationUtil.isNonEmpty(product.getName())) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (!ValidationUtil.isPositive(product.getPrice())) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (!ValidationUtil.isNonNegativeInt(product.getStockQty())) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        return productDAO.create(product);
    }

    public Product getProduct(long id) throws SQLException {
        Product p = productDAO.findById(id);
        if (p == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        return p;
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> getByCategory(String category) throws SQLException {
        return productDAO.findByCategory(category);
    }

    public List<Product> search(String keyword) throws SQLException {
        return productDAO.searchByKeyword(keyword);
    }

    public List<Product> getBySeller(long sellerId) throws SQLException {
        return productDAO.findBySellerId(sellerId);
    }

    public void updateProduct(Product product) throws SQLException {
        if (!ValidationUtil.isNonEmpty(product.getName())) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (!ValidationUtil.isPositive(product.getPrice())) {
            throw new IllegalArgumentException("Price must be positive");
        }
        productDAO.update(product);
    }

    public void deleteProduct(long id, long requestingSellerId) throws SQLException {
        Product p = getProduct(id);
        if (p.getSellerId() != requestingSellerId) {
            throw new SecurityException("Only the owning seller can delete this product");
        }
        productDAO.delete(id);
    }
  }
