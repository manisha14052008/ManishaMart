package com.manisha.manishamart.controller;
import com.google.gson.Gson;
import com.manisha.manishamart.dao.ProductDAO;
import com.manisha.manishamart.dao.impl.ProductDAOImpl;
import com.manisha.manishamart.dto.ApiResponse;
import com.manisha.manishamart.listener.DataSourceListener;
import com.manisha.manishamart.model.Product;
import com.manisha.manishamart.service.ProductService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/v1/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        ProductDAO productDAO = new ProductDAOImpl(DataSourceListener.getDataSource());
        productService = new ProductService(productDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String category = req.getParameter("category");
                String keyword = req.getParameter("q");

                List<Product> results;
                if (keyword != null && !keyword.isBlank()) {
                    results = productService.search(keyword);
                } else if (category != null && !category.isBlank()) {
                    results = productService.getByCategory(category);
                } else {
                    results = productService.getAllProducts();
                }
                writeJson(resp, 200, ApiResponse.ok(results));

            } else {
                long id = Long.parseLong(pathInfo.substring(1));
                Product product = productService.getProduct(id);
                writeJson(resp, 200, ApiResponse.ok(product));
            }
        } catch (NumberFormatException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Invalid product id"));
        } catch (IllegalArgumentException e) {
            writeJson(resp, 404, ApiResponse.fail("NOT_FOUND", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Long sellerId = requireSeller(req);
            if (sellerId == null) {
                writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", "Seller login required"));
                return;
            }

            String body = readBody(req);
            Product product = gson.fromJson(body, Product.class);
            product.setSellerId(sellerId);

            Product created = productService.createProduct(product);
            writeJson(resp, 201, ApiResponse.ok(created));

        } catch (IllegalArgumentException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Long sellerId = requireSeller(req);
            if (sellerId == null) {
                writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", "Seller login required"));
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Product id required"));
                return;
            }
            long id = Long.parseLong(pathInfo.substring(1));

            String body = readBody(req);
            Product product = gson.fromJson(body, Product.class);
            product.setId(id);

            Product existing = productService.getProduct(id);
            if (existing.getSellerId() != sellerId) {
                writeJson(resp, 403, ApiResponse.fail("FORBIDDEN", "Not your product"));
                return;
            }

            productService.updateProduct(product);
            writeJson(resp, 200, ApiResponse.ok(null));

        } catch (NumberFormatException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Invalid product id"));
        } catch (IllegalArgumentException e) {
            writeJson(resp, 404, ApiResponse.fail("NOT_FOUND", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Long sellerId = requireSeller(req);
            if (sellerId == null) {
                writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", "Seller login required"));
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Product id required"));
                return;
            }
            long id = Long.parseLong(pathInfo.substring(1));

            productService.deleteProduct(id, sellerId);
            writeJson(resp, 200, ApiResponse.ok(null));

        } catch (NumberFormatException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", "Invalid product id"));
        } catch (SecurityException e) {
            writeJson(resp, 403, ApiResponse.fail("FORBIDDEN", e.getMessage()));
        } catch (IllegalArgumentException e) {
            writeJson(resp, 404, ApiResponse.fail("NOT_FOUND", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
    }

    private Long requireSeller(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void writeJson(HttpServletResponse resp, int status, ApiResponse<?> body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(body));
    }
            }
