package dao;

import beans.Account;
import beans.Transfer;
import beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class AccountDAO {
    private Connection con;
    User user;

    public AccountDAO(Connection connection, User user) {
        this.con = connection;
        this.user = user;
    }

    public ArrayList<Transfer> fillMovements(Account account)
            throws SQLException {
        String query = "SELECT * FROM transfers T WHERE T.origin_account = ? OR T.destination_account = ? ORDER BY T.date desc";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        ArrayList<Transfer> list = new ArrayList<Transfer>(0);

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, account.getCode());
            pstatement.setInt(2, account.getCode());
            result = pstatement.executeQuery();
            while (result.next()) {
                Transfer transfer = new Transfer();
                transfer.setAmount(result.getInt("amount"));
                transfer.setDate(result.getDate("date"));
                transfer.setDest_account(result.getInt("destination_account"));
                transfer.setOrigin_account(result.getInt("origin_account"));
                transfer.setSubject(result.getString("subject"));
                list.add(transfer);
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

    public int doTransfer(int origin_user, int origin_account,
                          int dest_user, int dest_account, String subject, double amount)
            throws SQLException {
        int done;

        if (!check_amount(amount, origin_account)) {
            return 1;
        }

        // update of destination account.

        done = updateBalance(con, dest_user, dest_account, amount, 2);

        // update of origin account.
        if (done == 0)
            done = updateBalance(con, origin_user, origin_account, amount, 1);

        // adding the transfer
        if (done == 0) {
            String query1 = "INSERT into transfers (transfer_code,origin_account,destination_account,amount,date,subject) " +
                    "VALUES(?,?,?,?,?,?)";
            PreparedStatement pstatement1 = null;

            try {
                pstatement1 = con.prepareStatement(query1);
                pstatement1.setInt(1, generator());
                pstatement1.setInt(2, origin_account);
                pstatement1.setInt(3, dest_account);
                pstatement1.setDouble(4, amount);
                pstatement1.setDate(5, java.sql.Date.valueOf(java.time.LocalDate.now()));
                pstatement1.setString(6, subject);
                pstatement1.executeUpdate();
            } catch (SQLException e) {
                return 2;
            } finally {
                try {
                    if (pstatement1 != null) {
                        pstatement1.close();
                    }
                } catch (Exception e2) {
                    try {
                        throw new SQLException(e2);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }

        return done;
    }

    private boolean check_amount(double amount, int account) {
        return user.getAccount(account).getBalance() >= amount;
    }

    private int generator() {
        Random random = new Random();
        return random.nextInt(8999999) + 1000000;
    }

    private int updateBalance(Connection con, int user_code, int account, double amount, int type)
            throws SQLException, NullPointerException {

        String query = "UPDATE accounts SET balance = ? WHERE code_user = ? AND code_account = ?";
        PreparedStatement pstatement = null;

        try {
            double balance = getBalance(user_code, account);
            if (type == 1) {
                balance = balance - amount;
            } else {
                balance = balance + amount;
            }

            pstatement = con.prepareStatement(query);
            pstatement.setDouble(1, balance);
            pstatement.setInt(2, user_code);
            pstatement.setInt(3, account);
            pstatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            if (type == 1)
                return 3;
            else return 4;
        } finally {
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
        return 0;
    }

    private double getBalance(int user_code, int account)
            throws SQLException {

        String query = "SELECT * FROM accounts WHERE code_user = ? AND code_account = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, user_code);
            pstatement.setInt(2, account);
            result = pstatement.executeQuery();
            if (result.next()) {
                return result.getDouble("balance");
            } else throw new SQLException();
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
    }
}