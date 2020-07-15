package utils;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class Utilities {

    // generation of a code of n digits
    public static Integer generate_code(int digits) {
        Random rand = new Random();
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int n = max - min + 1;

        return rand.nextInt() % n;
    }

    public static boolean checkValidityTransferReq(HttpServletRequest request) {
        return request.getParameter("user_code") != null &&
                request.getParameter("account_code") != null &&
                request.getParameter("subject") != null &&
                request.getParameter("amount") != null &&
                request.getParameter("chosenAccountField") != null;
    }

    public static void closeDbAccess(ResultSet result, PreparedStatement statement) {
        try {
            if (result != null)
                result.close();
        } catch (Exception e) {
            System.out.println("SQL RESULT ERROR");
        }

        try {
            if (statement != null)
                statement.close();
        } catch (Exception e) {
            System.out.println("SQL STATEMENT ERROR");
        }
    }
}
