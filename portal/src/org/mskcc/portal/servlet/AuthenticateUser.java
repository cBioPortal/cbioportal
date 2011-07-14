package org.mskcc.portal.servlet;

import org.owasp.validator.html.PolicyException;
import org.mskcc.portal.util.Config;
import org.mskcc.portal.util.SkinUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Shows Content Already Retrieved from the CGDS Server.
 */
public class AuthenticateUser extends HttpServlet {
    public static final String INDEX = "index";
    public static final String USER_IS_AUTHENTICATED = "USER_IS_AUTHENTICATED";
    private ServletXssUtil servletXssUtil;

    /**
     *
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Handles HTTP POST Request.
     *
     * @param request  Http Servlet Request Object.
     * @param response Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userName = servletXssUtil.getCleanInput(request, "user_name");
        String password = servletXssUtil.getCleanInput(request, "password");

        if (userName == null || password == null || userName.trim().length() == 0
                || password.trim().length() == 0) {
            tryAgain("Please enter a user name and password, and try again.", request, response);
        }
        if (userName.equals(SkinUtil.getAuthenticationUserName())
                && password.equals(SkinUtil.getAuthenticationPassword())) {
            goodToGo (request, response);
        } else {
            tryAgain("Incorrect user name / password.", request, response);
        }
    }

    private void tryAgain(String errorMsg, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, errorMsg);
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/sign_in.jsp");
        dispatcher.forward(request, response);
    }

    private void goodToGo (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "You are now logged in.");

        HttpSession session = request.getSession(true);
        session.setAttribute(USER_IS_AUTHENTICATED, Boolean.TRUE);

        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/index.do");
        dispatcher.forward(request, response);
    }


    public static boolean userIsAuthenticated (HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Boolean userIsAuthenticated = (Boolean) session.getAttribute(USER_IS_AUTHENTICATED);
        if (userIsAuthenticated != null && userIsAuthenticated == true) {
            return true;
        } else {
            return false;
        }
    }
}