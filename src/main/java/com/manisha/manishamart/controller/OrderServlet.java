package com.manisha.manishamart.controller;
import com.google.gson.Gson;
import com.manisha.manishamart.dao.CartDAO;
import com.manisha.manishamart.dao.OrderDAO;
import com.manisha.manishamart.dao.ProductDAO;
import com.manisha.manishamart.dao.impl.CartDAOImpl;
import com.manisha.manishamart.dao.impl.OrderDAOImpl;
import com.manisha.manishamart.dao.impl.ProductDAOImpl;
import com.manisha.manishamart.dto.ApiResponse;
import com.manisha.manishamart.listener.DataSourceListener;
import com.manisha.manishamart.model.Order;
import com.manisha.manishamart.model.OrderItem;
import com.manisha.manishamart.service.OrderService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/orders/*")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        OrderDAO orderDAO = new OrderDAOImpl(DataSourceListener.getDataSource());
        CartDAO cartDAO = new CartDAOImpl(DataSourceListener.getDataSource());
        ProductDAO productDAO = new ProductDAOImpl(DataSourceListener.getDataSource());
        orderService = new OrderService(orderDAO, cartDAO, productDAO);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Long buyerId = requireUser(req);
            if (buyerId == null) {
                writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", "Login required"));
                return;
            }

            Order order = orderService.placeOrder(buyerId);
            writeJson(resp, 201, ApiResponse.ok(order));

        } catch (IllegalArgumentException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", e.getMessage()));
        } catch (IllegalStateException e) {
            writeJson(resp, 500, ApiResponse.fail("PAYMENT_FAILED", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Long userId = requireUser(req);
            if (userId == null) {
                writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", "Login required"));
                return;
            }

            String pathInfo = req.getPathInfo();
            String role = (String) req.getSession(false).getAttribute("role");

            if (pathInfo == null || pathInfo.equals("/")) {
                List<Order> orders = "SELLER".equals(role)
                        ? orderService.getSellerOrders(userId)
                        : orderService.getBuyerOrders(userId);
                writeJson(resp, 200, ApiResponse.ok(orders));

            } else {
                long orderId = Long.parseLong(pathInfo.substring(1));
                Order order = orderService.getOrder(orderId);
                List<OrderItem> items = orderService.getOrderItems(orderId);
                Map<String, Object> data = Map.of("order", order, "items", items);
                writeJson(resp, 200, ApiResponse.ok(data));
            }

        } catch (NumberFormatException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Invalid order id"));
        } catch (IllegalArgumentException e) {
            writeJson(resp, 404, ApiResponse.fail("NOT_FOUND", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    private Long requireUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    private void writeJson(HttpServletResponse resp, int status, ApiResponse<?> body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(body));
    }
                                                    }
