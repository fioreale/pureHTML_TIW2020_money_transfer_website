package beans;

import java.util.ArrayList;
import java.util.Random;

public class Account {
    private int code;
    private double balance;
    private ArrayList<Transfer> movements;

    private Integer generate_code() {
        Random rand = new Random();
        int min = 0;
        int max = 999999999;
        int n = max - min + 1;

        return rand.nextInt() % n;
    }

    public Account() {
        this.code = generate_code();
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

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public ArrayList<Transfer> getMovements() {
        return movements;
    }

    public void setMovements(ArrayList<Transfer> movements) {
        this.movements = movements;
    }
}