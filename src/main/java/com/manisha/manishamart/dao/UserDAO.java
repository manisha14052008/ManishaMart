package com.manisha.manishamart.dao;
import com.manisha.manishamart.model.User;
import java.sql.SQLException;
import java.util.List;
public interface UserDAO {
    User create(User user) throws SQLException;
    User findById(long id) throws SQLException;
    User findByEmail(String email) throws SQLException;
    List<User> findAll() throws SQLException;
    void update(User user) throws SQLException;
    void delete(long id) throws SQLException;
}
