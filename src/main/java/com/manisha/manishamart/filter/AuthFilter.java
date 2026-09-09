package com.manisha.manishamart.controller;

import com.manisha.manishamart.dto.ApiResponse;
import com.manisha.manishamart.model.User;
import com.manisha.manishamart.service.UserService;
import com.manisha.manishamart.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

// IMPORTANT: the /* wildcard is required so /api/v1/auth/login
// and /api/v1/auth/register both route into this one servlet.
@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // pathInfo is everything AFTER "/api/v1/auth" — e.g. "/login" or "/register"
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "No auth action specified");
            return;
        }

        String body = readBody(request);
        JsonObject json;
        try {
            json = gson.fromJson(body, JsonObject.class);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Malformed JSON body");
            return;
        }

        switch (pathInfo) {
            case "/login":
                handleLogin(request, response, json);
                break;
            case "/register":
                handleRegister(request, response, json);
                break;
            default:
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown auth action: " + pathInfo);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, JsonObject json)
            throws IOException {

        if (json == null || !json.has("email") || !json.has("password")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "email and password are required");
            return;
        }

        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();

        User user = userService.authenticate(email, password); // returns null if invalid

        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_FAILED", "Invalid email or password");
            return;
        }

        // Regenerate session ID on login (security requirement)
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole());
        session.setMaxInactiveInterval(30 * 60); // 30 min timeout

        JsonObject data = new JsonObject();
        data.addProperty("id", user.getId());
        data.addProperty("name", user.getName());
        data.addProperty("email", user.getEmail());
        data.addProperty("role", user.getRole().toString());

        sendSuccess(response, HttpServletResponse.SC_OK, data);
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, JsonObject json)
            throws IOException {

        if (json == null || !json.has("name") || !json.has("email")
                || !json.has("password") || !json.has("role")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "name, email, password, role are required");
            return;
        }

        String name = json.get("name").getAsString();
        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();
        String role = json.get("role").getAsString(); // "BUYER" or "SELLER" only

        try {
            User created = userService.register(name, email, password, role);

            JsonObject data = new JsonObject();
            data.addProperty("id", created.getId());
            data.addProperty("email", created.getEmail());

            sendSuccess(response, HttpServletResponse.SC_CREATED, data);
        } catch (IllegalArgumentException e) {
            // e.g. email already exists, invalid role, weak password
            sendError(response, HttpServletResponse.SC_CONFLICT, "VALIDATION_ERROR", e.getMessage());
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    private void sendSuccess(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        JsonObject envelope = new JsonObject();
        envelope.addProperty("success", true);
        envelope.add("data", gson.toJsonTree(data));
        envelope.add("error", null);
        response.getWriter().write(gson.toJson(envelope));
    }

    private void sendError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        JsonObject envelope = new JsonObject();
        envelope.addProperty("success", false);
        envelope.add("data", null);
        envelope.add("error", error);

        response.getWriter().write(gson.toJson(envelope));
    }
            }
