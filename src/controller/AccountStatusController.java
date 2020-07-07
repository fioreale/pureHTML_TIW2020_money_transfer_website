package controller;

import beans.User;
import dao.AccountDAO;
import dao.HomeDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
            session.setAttribute("currentUser", user);
            response.setHeader("chosenAccount", String.valueOf(chosen_account_code));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        String path = "/WEB-INF/Account.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!check(request)) {
            try {
                response.sendError(505, "Parameters incomplete");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        int versus_user = Integer.parseInt(request.getParameter("user_code"));
        int versus_account = Integer.parseInt(request.getParameter("account_code"));
        String subject = request.getParameter("subject");
        double amount = Double.parseDouble(request.getParameter("amount"));

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        int chosen_account_code = Integer.parseInt(request.getParameter("chosenAccountField"));
        AccountDAO accountDAO = new AccountDAO(connection, user);
        HomeDAO homeDAO = new HomeDAO(connection, user);

        int outcome_transfer;
        String path = getServletContext().getContextPath();

        try {
            outcome_transfer = accountDAO.doTransfer(user.getCode(), chosen_account_code, versus_user,
                    versus_account, subject, amount);
            // setting the outcome for the confirmation page
            response.setHeader("outcome", String.valueOf(outcome_transfer));

            if (outcome_transfer == 0) {
                // update the user
                user = homeDAO.fillAccounts(user);
                user.getAccount(chosen_account_code)
                        .setMovements(accountDAO.fillMovements(user.getAccount(chosen_account_code)));
                session.setAttribute("currentUser", user);
                // printing a good outcome page
                path += "/goodOutcome.html";
                response.sendRedirect(path);
            } else {
                // printing a bad outcome page
                path = "/WEB-INF/badOutcome.html";
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                templateEngine.process(path, ctx, response.getWriter());
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private boolean check(HttpServletRequest request) {
        return request.getParameter("user_code") != null &&
                request.getParameter("account_code") != null &&
                request.getParameter("subject") != null &&
                request.getParameter("amount") != null &&
                request.getParameter("chosenAccountField") != null;
    }
}