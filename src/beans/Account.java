package beans;

import utils.Utilities;

import java.util.ArrayList;

public class Account {
    private int code;
    private double balance;
    private ArrayList<Transfer> movements;

    public Account() {
        this.code = Utilities.generate_code(8);
        this.balance = 0;
        this.movements = new ArrayList<Transfer>(0);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public ArrayList<Transfer> getMovements() {
        return movements;
    }

    public void setMovements(ArrayList<Transfer> movements) {
        this.movements = movements;
    }
}