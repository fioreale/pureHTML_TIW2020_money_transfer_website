package dao;

import beans.Account;
import beans.Transfer;
import beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountDAO {
    private Connection con;
    User user;

    public AccountDAO(Connection connection, User user){
        this.con = connection;
        this.user = user;
    }

    public ArrayList<Transfer> fillMovements(Account account) throws SQLException {
        String query = "SELECT * FROM transfers T WHERE T.origin_account = ? OR T.destination_account = ? ORDER BY T.date desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        ArrayList<Transfer> list = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, account.getCode());
            pstatement.setInt(2, account.getCode());
            result = pstatement.executeQuery();
            list = new ArrayList<Transfer>(0);
            while (result.next()) {
                Transfer transfer = new Transfer();
                transfer.setAmount(result.getInt("amount"));
                transfer.setDate(result.getDate("date"));
                transfer.setDest_account(result.getInt("destination_account"));
                transfer.setOrigin_account(result.getInt("origin_account"));
                transfer.setSubject(result.getString("subject"));
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
        return list;
    }


}