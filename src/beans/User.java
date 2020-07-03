package beans;

import java.util.ArrayList;

public class User {
    private String name;
    private int code;
    private ArrayList<Account> accounts;

    public User() {
        this.name = "";
        this.code = 0;
        this.accounts = new ArrayList<Account>(0);
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(int code) {
        for (Account acc : accounts
        ) {
            if (acc.getCode() == code)
                return acc;
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }
}
