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

    public User checkUser(String username, String password)
            throws SQLException {
        User user = null;
        String query = "SELECT * FROM user WHERE username = ? and password = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setString(1, username);
            pstatement.setString(2, password);
            result = pstatement.executeQuery();
            while (result.next()) {
                user = new User();
                user.setName(result.getString("name") + result.getString("surname"));
                user.setCode(result.getInt("code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                throw new SQLException(e);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

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
