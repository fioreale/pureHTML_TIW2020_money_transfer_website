package dao;

import beans.Account;
import beans.Transfer;
import beans.User;

import java.sql.*;
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
                transfer.setTransfer_code(result.getInt("transfer_code"));
                transfer.setAmount(result.getDouble("amount"));
                transfer.setDate(result.getTimestamp("date"));
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

        int out = -1;
        // check on the validity of the destination
        if (!checkValidity(dest_user, dest_account)) {
            return 5;
        }
        // check on the amount consistency
        if (!check_amount(amount, origin_account)) {
            return 1;
        }

        Savepoint savepoint1 = null;
        try {
            double balance_origin = getBalance(origin_user, origin_account);
            double balance_dest = getBalance(dest_user, dest_account);

            con.setAutoCommit(false);

            // saving a breakpoint
            savepoint1 = con.setSavepoint("Savepoint1");

            // update destination
            out = updateBalance(con, dest_user, dest_account, amount, balance_dest, 2);
            // update origin
            if (out == 0)
                out = updateBalance(con, origin_user, origin_account, amount, balance_origin, 1);
            // update transfers
            if (out == 0)
                out = updateTransfers(con, origin_user, origin_account,
                        dest_user, dest_account, subject, amount);
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            con.rollback(savepoint1);
            con.setAutoCommit(true);
        } finally {
            if (out != 0) {
                con.rollback(savepoint1);
            }
            con.setAutoCommit(true);
        }

        return out;
    }

    private boolean checkValidity(int dest_user, int dest_account) {
        String query = "SELECT * FROM accounts WHERE code_user = ? AND code_account = ?";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, dest_user);
            pstatement.setInt(2, dest_account);
            result = pstatement.executeQuery();
            if (!result.next()) {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
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
        return true;
    }

    private boolean check_amount(double amount, int account) {
        return user.getAccount(account).getBalance() >= amount;
    }

    private int updateBalance(Connection con, int user_code, int account, double amount, double balance, int type)
            throws SQLException, NullPointerException {

        String query = "UPDATE accounts SET balance = ? WHERE code_user = ? AND code_account = ?";
        PreparedStatement pstatement = null;

        try {
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

    public int updateTransfers(Connection con, int origin_user, int origin_account,
                               int dest_user, int dest_account, String subject, double amount)
            throws SQLException {

        String query = "INSERT into transfers (transfer_code,origin_account,destination_account,amount,date,subject) " +
                "VALUES(?,?,?,?,?,?)";
        PreparedStatement pstatement = null;

        try {
            pstatement = con.prepareStatement(query);
            pstatement.setInt(1, generate_code());
            pstatement.setInt(2, origin_account);
            pstatement.setInt(3, dest_account);
            pstatement.setDouble(4, amount);
            pstatement.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstatement.setString(6, subject);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 2;
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

    public Integer generate_code() {
        int digits = 7;
        Random rand = new Random();
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int n = max - min + 1;

        int generated_num = 0;

        String query = "SELECT transfer_code FROM transfers";
        ResultSet result = null;
        PreparedStatement pstatement = null;
        ArrayList<Integer> list = new ArrayList<>(0);
        try {
            pstatement = con.prepareStatement(query);
            result = pstatement.executeQuery();
            while (result.next()) {
                list.add(result.getInt("transfer_code"));
            }

            boolean ok = false;
            while (!ok) {
                generated_num = rand.nextInt() % n;
                for (Integer num : list) {
                    if (num == generated_num) {
                        break;
                    }
                }
                ok = true;
            }
            return generated_num;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -1;
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