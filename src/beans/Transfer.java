package beans;

import java.util.Date;

public class Transfer {
    private Date date;
    private float amount;
    private String subject;
    private int origin_account;
    private int dest_account;

    public Transfer() {
        this.date = null;
        this.amount = 0;
        this.subject = "";
        this.origin_account = 0;
        this.dest_account = 0;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
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