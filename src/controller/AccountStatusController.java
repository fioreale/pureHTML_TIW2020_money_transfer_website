package controller;

import beans.User;
import dao.AccountDAO;
import dao.HomeDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import utils.Utilities;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/AccountStatusController")
public class AccountStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public AccountStatusController() {
        super();
    }

    public void init() throws ServletException {
        try {
            ServletContext servletContext = getServletContext();
            ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
            templateResolver.setTemplateMode(TemplateMode.HTML);
            this.templateEngine = new TemplateEngine();
            this.templateEngine.setTemplateResolver(templateResolver);
            templateResolver.setSuffix(".html");
            String driver = servletContext.getInitParameter("dbDriver");
            String url = servletContext.getInitParameter("dbUrl");
            String user = servletContext.getInitParameter("dbUser");
            String password = servletContext.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnavailableException("Couldn't get db connection");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int chosen_account_code = Integer.parseInt(request.getParameter("account_code"));
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        AccountDAO accountDAO = new AccountDAO(connection, user);
        int chosen_account = Integer.parseInt(request.getParameter("account_code"));
        try {
            user.getAccount(chosen_account)
                    .setMovements(
                            accountDAO.fillMovements(user.getAccount(chosen_account))
                    );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.sendError(502, "Server was not able to fill the movements");
            return;
        }

        session.setAttribute("currentUser", user);
        response.setHeader("chosenAccount", String.valueOf(chosen_account_code));
        response.setStatus(200);
        String path = "/WEB-INF/Account.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!Utilities.checkValidityTransferReq(request)) {
            try {
                response.sendError(505, "Parameters incomplete");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        int versus_user;
        int versus_account;
        String subject;
        double amount;
        try {
            versus_user = Integer.parseInt(request.getParameter("user_code"));
            versus_account = Integer.parseInt(request.getParameter("account_code"));
            subject = request.getParameter("subject");
            amount = Double.parseDouble(request.getParameter("amount"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendError(response.SC_NOT_ACCEPTABLE, "The form contains input in a non valid format");
            return;
        }


        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        int chosen_account_code = Integer.parseInt(request.getParameter("chosenAccountField"));
        AccountDAO accountDAO = new AccountDAO(connection, user);
        HomeDAO homeDAO = new HomeDAO(connection, user);

        int outcome_transfer;
        String path;

        try {
            // setting the outcome for the confirmation page

            outcome_transfer = accountDAO.doTransfer(user.getCode(), chosen_account_code, versus_user,
                    versus_account, subject, amount);
            if (outcome_transfer == 0) {
                // update the user
                user = homeDAO.fillAccounts(user);
                user.getAccount(chosen_account_code)
                        .setMovements(accountDAO.fillMovements(user.getAccount(chosen_account_code)));
                // printing a good outcome page
                // taking the balance of the destination
                double dest_balance = accountDAO.getBalance(versus_user, versus_account);
                path = "/WEB-INF/goodOutcome.html";
                response.setStatus(200);
                response.setHeader("balance_dest", String.valueOf(dest_balance));
                response.setHeader("account_dest", String.valueOf(versus_account));
                response.setHeader("user_dest", String.valueOf(versus_user));
            } else {
                // printing a bad outcome page
                path = "/WEB-INF/badOutcome.html";
                if (outcome_transfer == 1) {
                    response.setStatus(400);
                    response.setHeader("outcome", "The amount specified cannot be transferred");
                } else if (outcome_transfer == 2) {
                    response.setStatus(502);
                    response.setHeader("outcome", "It's not possible to fulfill the transfer");
                } else if (outcome_transfer == 3) {
                    response.setStatus(502);
                    response.setHeader("outcome", "It's not possible to update your balance");
                } else if (outcome_transfer == 4) {
                    response.setStatus(502);
                    response.setHeader("outcome", "It's not possible to update the balance of the account you sent money");
                } else if (outcome_transfer == 5) {
                    response.setStatus(400);
                    response.setHeader("outcome", "The server does not contain any user or account associated to " +
                            "the credentials you specified");
                } else if (outcome_transfer == 6) {
                    response.setStatus(400);
                    response.setHeader("outcome", "You are trying to send money to yourself");
                }
            }

            response.setHeader("chosenAccount", String.valueOf(chosen_account_code));
            session.setAttribute("currentUser", user);
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            templateEngine.process(path, ctx, response.getWriter());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.sendError(502, "Server encountered some issue in the connection with database");
        }
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}