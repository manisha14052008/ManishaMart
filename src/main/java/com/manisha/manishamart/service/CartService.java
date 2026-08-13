package com.manisha.manishamart.service;

import com.manisha.manishamart.dao.CartDAO;
import com.manisha.manishamart.dao.ProductDAO;
import com.manisha.manishamart.model.CartItem;
import com.manisha.manishamart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public CartItem addItem(long userId, long productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (product.getStockQty() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + productId);
        }

        CartItem existing = cartDAO.findByUserAndProduct(userId, productId);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            cartDAO.updateQuantity(existing.getId(), newQty);
            existing.setQuantity(newQty);
            return existing;
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        return cartDAO.create(item);
    }

    public void updateQuantity(long cartItemId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        cartDAO.updateQuantity(cartItemId, quantity);
    }

    public void removeItem(long cartItemId) throws SQLException {
        cartDAO.delete(cartItemId);
    }

    public List<CartItem> getCart(long userId) throws SQLException {
        return cartDAO.findByUserId(userId);
    }

    public BigDecimal getCartTotal(long userId) throws SQLException {
        List<CartItem> items = cartDAO.findByUserId(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product p = productDAO.findById(item.getProductId());
            if (p != null) {
                total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return total;
    }

    public void clearCart(long userId) throws SQLException {
        cartDAO.deleteAllByUserId(userId);
    }
                          }
