package com.manisha.manishamart.service;

import com.manisha.manishamart.dao.UserDAO;
import com.manisha.manishamart.model.User;
import com.manisha.manishamart.util.PasswordUtil;
import com.manisha.manishamart.util.ValidationUtil;

import java.sql.SQLException;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String name, String email, String plainPassword, User.Role role) throws SQLException {
        if (!ValidationUtil.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name is required");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (!ValidationUtil.isNonEmpty(plainPassword) || plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (role == User.Role.ADMIN) {
            throw new SecurityException("Admin accounts cannot be created via signup");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hash(plainPassword));
        user.setRole(role);
        return userDAO.create(user);
    }

    public User login(String email, String plainPassword) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user == null || !PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new SecurityException("Invalid email or password");
        }
        return user;
    }

    public User getById(long id) throws SQLException {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        return user;
    }
          }
