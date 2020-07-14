package beans;

import utils.Utilities;

import java.sql.Timestamp;
import java.util.Date;

public class Transfer {
    private Timestamp date;
    private double amount;
    private String causal;
    private int origin_account;
    private int dest_account;
    private int transfer_code;

    public Transfer() {
        this.date = null;
        this.amount = 0;
        this.causal = "";
        this.origin_account = 0;
        this.dest_account = 0;
        this.transfer_code = Utilities.generate_code(7);
    }

    public int getTransfer_code() {
        return transfer_code;
    }

    public void setTransfer_code(int transfer_code) {
        this.transfer_code = transfer_code;
    }

    public String getCausal() {
        return causal;
    }

    public void setCausal(String causal) {
        this.causal = causal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getOrigin_account() {
        return origin_account;
    }

    public void setOrigin_account(int origin_account) {
        this.origin_account = origin_account;
    }

    public int getDest_account() {
        return dest_account;
    }

    public void setDest_account(int dest_account) {
        this.dest_account = dest_account;
    }
}