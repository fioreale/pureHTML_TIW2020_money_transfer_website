package controller;

import beans.User;
import dao.HomeDAO;
import dao.LoginDAO;
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

@WebServlet("/LoginController")
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public LoginController() {
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
//            String driver = servletContext.getInitParameter("dbDriver");
//            String url = servletContext.getInitParameter("dbUrl");
//            String user = servletContext.getInitParameter("dbUser");
//            String password = servletContext.getInitParameter("dbPassword");
//            Class.forName(driver);
//            connection = DriverManager.getConnection(url, user, password);
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
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log("entered in post login");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            try {
                response.sendError(505, "Parameters incomplete");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        LoginDAO userDAO = new LoginDAO(connection);
        User user = null;
        try {
            user = userDAO.checkUser(username, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        String path = getServletContext().getContextPath();
        if (user != null) {
            try {
                HomeDAO homeDAO = new HomeDAO(connection,user);
                user = homeDAO.fillAccounts(user);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            //redirect to Home Page
            HttpSession session = request.getSession();
            session.setAttribute("currentUser", user);
            path = getServletContext().getContextPath() + "/HomePageController";
            ServletContext servletContext = getServletContext();
            // thymeleaf context
            final WebContext ctx = new WebContext(request,response,servletContext,request.getLocale());
            ctx.setVariable("actualUser",user);
            templateEngine.process(path,ctx,response.getWriter());
        } else {
            response.sendError(505, "Invalid user");
        }
        response.sendRedirect(path);
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