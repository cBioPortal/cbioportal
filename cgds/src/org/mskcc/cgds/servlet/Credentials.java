// package
package org.mskcc.cgds.servlet;

// imports
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.WebApiUtil;
import org.mskcc.cgds.util.DatabaseProperties;
import org.mskcc.cgds.web_api.UserCredentials;
import org.mskcc.cgds.web_api.ProtocolException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Credentials servlet.  Request for user credentials
 * from Portal application comes here.
 *
 * @author Benjamin Gross
 */
public class Credentials extends HttpServlet {

	private static Log log = LogFactory.getLog(Credentials.class);

	// ref to get user auth token
	private UserCredentials userCredentials;

   /**
     * Shutdown the Servlet.
     */
    public void destroy() {
        super.destroy();
    }

    /**
     * Initializes Servlet with parameters in web.xml file.
     *
     * @throws javax.servlet.ServletException Servlet Initialization Error.
     */
    public void init() throws ServletException {
        super.init();
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        ServletConfig config = this.getServletConfig();
        String dbHost = config.getInitParameter("db_host");
        String dbUser = config.getInitParameter("db_user");
        String dbPassword = config.getInitParameter("db_password");
        String dbName = config.getInitParameter("db_name");
        dbProperties.setDbName(dbName);
        dbProperties.setDbHost(dbHost);
        dbProperties.setDbUser(dbUser);
        dbProperties.setDbPassword(dbPassword);

		// setup our context and init some beans
		ApplicationContext context =
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		userCredentials = (UserCredentials)context.getBean("userCredentials");
    }

    /**
     * Handles GET Requests.
     *
     * @param httpServletRequest    HttpServlet Request.
     * @param httpServletResponse   HttpServlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        processClient(httpServletRequest, httpServletResponse);
    }

    /**
     * Processes all Client Requests.
     *
     * @param httpServletRequest    HttpServlet Request.
     * @param httpServletResponse   HttpServlet Response.
     * @throws IOException IO Error.
     */
    public void processClient(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {
		PrintWriter writer = httpServletResponse.getWriter();

		try {
			httpServletResponse.setContentType("text/plain");
			writer.print(WebApiUtil.WEP_API_HEADER);
			String email = httpServletRequest.getParameter("email_address");
			if (email == null) {
				throw new ProtocolException ("Missing Parameter: email_address");
			}
			String out = userCredentials.getUserCredentials(email);
			writer.print(out);
		} catch (DaoException e) {
			e.printStackTrace();
			outputError(writer, "internal error:  " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			outputError(writer, e.toString());
		} finally {
			writer.flush();
			writer.close();
		}
    }

    private void outputError(PrintWriter writer, String msg) {
        writer.print("Error: " + msg + "\n");
    }
}