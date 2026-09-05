package com.manisha.manishamart.controller;
import com.google.gson.Gson;
import com.manisha.manishamart.dto.ApiResponse;
import com.manisha.manishamart.dto.UserResponseDTO;
import com.manisha.manishamart.model.User;
import com.manisha.manishamart.service.UserService;
import com.manisha.manishamart.dao.UserDAO;
import com.manisha.manishamart.dao.impl.UserDAOImpl;
import com.manisha.manishamart.listener.DataSourceListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {

    private UserService userService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        UserDAO userDAO = new UserDAOImpl(DataSourceListener.getDataSource());
        userService = new UserService(userDAO);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        System.err.println("DEBUG PATH: [" + path + "]");
        resp.setContentType("application/json");

        try {
            String body = readBody(req);
            Map<String, String> data = gson.fromJson(body, Map.class);

            if ("/register".equals(path)) {
                String name = data.get("name");
                String email = data.get("email");
                String password = data.get("password");
                String roleStr = data.getOrDefault("role", "BUYER");
                User.Role role = User.Role.valueOf(roleStr.toUpperCase());

                User user = userService.register(name, email, password, role);

                HttpSession session = req.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("role", user.getRole().name());

                writeJson(resp, 201, ApiResponse.ok(UserResponseDTO.fromUser(user)));

            } else if ("/login".equals(path)) {
                String email = data.get("email");
                String password = data.get("password");

                User user = userService.login(email, password);

                // Regenerate session ID on login (Section 2, engineering rule 3)
                req.changeSessionId();
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("role", user.getRole().name());
                session.setMaxInactiveInterval(1800);

                writeJson(resp, 200, ApiResponse.ok(UserResponseDTO.fromUser(user)));

            } else if ("/logout".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                writeJson(resp, 200, ApiResponse.ok(null));

            } else {
                writeJson(resp, 404, ApiResponse.fail("NOT_FOUND", "Unknown auth endpoint"));
            }

        } catch (IllegalArgumentException e) {
            writeJson(resp, 400, ApiResponse.fail("VALIDATION_ERROR", e.getMessage()));
        } catch (SecurityException e) {
            writeJson(resp, 401, ApiResponse.fail("UNAUTHENTICATED", e.getMessage()));
        } catch (SQLException e) {
            writeJson(resp, 500, ApiResponse.fail("SERVER_ERROR", "Database error"));
        }
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
