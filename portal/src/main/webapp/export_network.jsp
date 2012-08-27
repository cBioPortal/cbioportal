<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.OutputStream"%>

<%
	/*
	 * This page is to export CytoscapeWeb network to a file. Direct access
	 * to this page should be avoided.
	 * 
	 * TODO avoid direct access!
	 */
	 
	InputStream requestStream = request.getInputStream();
	OutputStream outStream = response.getOutputStream();
	
	// identify file type to download
	
	String type = request.getParameter("type");
	
	if (type.equals("png"))
	{
		response.setContentType("image/png");
	}
	else if (type.equals("svg"))
	{
		response.setContentType("image/svg+xml");
	}
	else if (type.equals("pdf"))
	{
		response.setContentType("application/pdf");
	}
	else if (type.equals("xml"))
	{
		response.setContentType("text/xml");
	}
	
	// generate filename
	
	String filename = "graph." + type;
	
	response.setHeader("Content-disposition",
		"attachment; filename=" + filename);
	
	// read the raw data from input & write to output
	
	int numBytes = 0;
	byte[] buffer = new byte[4096];
	
	while (numBytes != -1)
	{
		numBytes = requestStream.read(buffer);
		
		if (numBytes > 0)
		{
			outStream.write(buffer, 0, numBytes);
		}
	}
	
	requestStream.close();
	outStream.flush();
	outStream.close();
	
/*
<?php
    # ##### The server-side code in PHP ####

    # Type sent as part of the URL:
    $type = $_GET['type'];
    # Get the raw POST data:
    $data = file_get_contents('php://input');

    # Set the content type accordingly:
    if ($type == 'png') {
        header('Content-type: image/png');
    } elseif ($type == 'pdf') {
        header('Content-type: application/pdf');
    } elseif ($type == 'svg') {
       header('Content-type: image/svg+xml');
    } elseif ($type == 'xml') {
        header('Content-type: text/xml');
    }

    # To force the browser to download the file:
    header('Content-disposition: attachment; filename="network.' . $type . '"');
    # Send the data to the browser:
    print $data;
?>
*/

%>