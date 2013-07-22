/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

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
	    out.println(c.eval("getCancerStudies(conn)"));
	} catch (RserveException rse) {
	    out.println(rse);
	} catch (REXPMismatchException mme) {
	    out.println(mme);
	}

	out.println("done!");
	out.close();

    }
}
