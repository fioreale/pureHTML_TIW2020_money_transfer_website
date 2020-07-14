package dao;

import beans.User;
import utils.Utilities;

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
            Utilities.closeDbAccess(result, pstatement);
        }
        return user;
    }
}
