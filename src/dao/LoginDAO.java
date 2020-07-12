package dao;

import beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {
    private Connection con;

    public LoginDAO(Connection connection) {
        this.con = connection;
    }

    public User checkUser(String username, String password) {
        User user = null;
        String query = "SELECT * FROM user WHERE username = ? and password = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, password);
            result = pstatement.executeQuery();
            if (result.next()) {
                user = new User();
                user.setName(result.getString("username"));
                user.setCode(result.getInt("code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (Exception e1) {
                try {
                    throw new SQLException(e1);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                if (pstatement != null) {
                    pstatement.close();
                }
            } catch (Exception e2) {
                try {
                    throw new SQLException(e2);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return user;
    }
}
