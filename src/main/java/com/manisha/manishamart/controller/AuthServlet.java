package com.manisha.manishamart.controller;

import com.manisha.manishamart.dao.impl.UserDAOImpl;
import com.manisha.manishamart.listener.DataSourceListener;
import com.manisha.manishamart.model.User;
import com.manisha.manishamart.service.UserService;
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
import java.sql.SQLException;
import java.util.stream.Collectors;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {

    private UserService userService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        userService = new UserService(new UserDAOImpl(DataSourceListener.getDataSource()));
    }

    // ...the rest (doPost, handleLogin, handleRegister, readBody, sendSuccess, sendError)
    // stays exactly as in my previous message — no other changes needed
