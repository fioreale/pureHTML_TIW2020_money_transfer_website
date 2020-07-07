package dao;

import beans.Account;
import beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class HomeDAO {
    private Connection con;
    User user;

    public HomeDAO(Connection connection, User usr) {
        this.con = connection;
        this.user = usr;
    }

    public User fillAccounts(User user)
            throws SQLException {
        int usr_code = user.getCode();
        String query = "SELECT * FROM accounts WHERE code_user = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, usr_code);
            result = pstatement.executeQuery();
            ArrayList<Account> list = new ArrayList<Account>(0);
            while (result.next()) {
                Account account = new Account();
                account.setCode(result.getInt("code_account"));
                account.setBalance(result.getInt("balance"));
                list.add(account);
            }
            user.setAccounts(list);
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