package org.mskcc.portal.servlet;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;

import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

public class PlotTestServlet extends HttpServlet
{
    public void doGet (HttpServletRequest req,
		       HttpServletResponse res)
	throws ServletException, IOException
    {
	PrintWriter out = res.getWriter();

	out.println("Lets try correlation of mRNA and CNA for NF1 in GBM ...");

	try {
	    RConnection c = new RConnection();
	    out.println(">>"+c.eval("R.version$version.string").asString()+"<<");
	    out.println("lib");
	    c.eval("library(cgdsr)");
	    c.eval("library(Cairo)");
	    out.println("conn");
	    c.eval("conn = CGDS('http://cbio.mskcc.org/cgds-public/')");
	    out.println("test conn");
	    out.println(c.eval("test(conn)"));
	    out.println("types");
	    out.println(c.eval("getCancerTypes(conn)"));
	} catch (RserveException rse) {
	    out.println(rse);
	} catch (REXPMismatchException mme) {
	    out.println(mme);
	}

	out.println("done!");
	out.close();

    }
}
