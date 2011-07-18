<%
    Config globalConfig = Config.getInstance();
    String tagLine = globalConfig.getProperty("tag_line_image");
    String siteName = globalConfig.getProperty("header_image");
    String global_style = globalConfig.getProperty("global_css");
    String special_style = globalConfig.getProperty("special_css");

    if (tagLine == null) {
        tagLine = "images/tag_line.png";
    } else {
        tagLine = "images/" + tagLine;
    }
    if (siteName == null) {
        siteName = "images/site_name.png";
    } else {
        siteName = "images/" + siteName;
    }
    if (global_style == null) {
        global_style = "css/global_portal.css";
    } else {
        global_style = "css/" + global_style;
    }
    if (special_style == null) {
        special_style = "";
    } else {
        special_style = "css/" + special_style;
    }

%> 

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

<link rel="icon" href="http://cbio.mskcc.org/favicon.ico"/>    
<link href="css/jquery-ui-1.8.6.custom.css" type="text/css" rel="stylesheet" />
<link href="css/popeye/jquery.popeye.css" type="text/css" rel="stylesheet" />
<link href="css/popeye/jquery.popeye.style.css" type="text/css" rel="stylesheet" />
<link href="css/tipTip.css" type="text/css" rel="stylesheet" />
<link href="<%= global_style %>" type="text/css" rel="stylesheet" />
<link href="<%= special_style %>" type="text/css" rel="stylesheet" />

<title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>

<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/dom/dom-min.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/event/event-min.js" ></script>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.tipTip.minified.js"></script>

<script type="text/javascript" src="js/jquery-ui-1.8.6.custom.min.js"></script>
<script type="text/javascript" src="js/cgx_jquery.js"></script>
<script type="text/javascript" src="js/global-tabs.js"></script>
<script type="text/javascript" src="js/mootools-core-1.3-full-compat-yc.js"></script>
<script type="text/javascript" src="js/mootools-more.js"></script>
<script type="text/javascript" src="js/jquery.popeye-2.0.4.min.js"></script>
           
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-17134933-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
</head>

<%
    Boolean isIndexPage = (Boolean) request.getAttribute("index.jsp");
    if (isIndexPage != null) { %>
        <body onload="updateCaseList()">                
    <% } else { %>
        <body>
    <% } %>
<center>
<div id="page_wrapper">
<table width="860px" cellpadding="0px" cellspacing="5px" border="0px">
  <tr valign="top">
    <td colspan="3">
    <div id="header_wrapper">
    <div id="header">

        <table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
    	<tr>
	        <td class="logo" width="250px"><a href="http://www.mskcc.org"><img src="images/msk_logo.png" alt="MSKCC Logo"/></a></td>
            <td class="logo" width="330px"><a href="index.do"><img src="<%= siteName %>" alt="Main Logo"/></a></td>
            <td class="logo" width="200px"><img src="<%= tagLine %>" alt="Tag Line"/></td>
        </tr>
        </table>

        <table width="100%">
        <tr>
            <td class="navigation">
               <ul>
                    <li class="selected">
					    <a href="index.do">Home</a>
					</li>
					<li class="internal" id="results">
					    <a href="#">Results</a>
					</li>
					<li class="internal">
					   	<a href="video.jsp">Tutorial</a>
					</li>
					<li class="internal">
					   	<a href="news.jsp">News</a>
					</li>
					<li class="internal">
					  	<a href="faq.jsp">FAQ</a>
					</li>
                    <li class="internal">
					   	<a href="data_sets.jsp">Data Sets</a>
					</li>
                    <li class="internal">
					   	<a href="about_us.jsp">About</a>
					</li>
                    <li class="internal">
					   	<a href="web_api.jsp">Web API</a>
					</li>
					<li class="internal">
					   	<a href="cgds_r.jsp">R Package</a>
					</li>
                    <li class="internal">
                        <a href="networks.jsp">Networks</a>
                    </li>
                    <li class="internal">
                        <a href="login.jsp">Login</a>
                    </li>
                    <li>
                        <a href="http://www.twitter.com/cbioportal"><img style="margin-top:5px; margin-bottom:4px"
                            src="images/twitter-b.png" title="Follow us on Twitter" alt="Follow us on Twitter"/></a>
                    </li>
                   <li>
                       <a href="http://cbio.mskcc.org"><img style="margin-top:6px; margin-bottom:4px; margin-right:-3px"
                            src="images/cbioLogo.png" title="cBio@MSKCC" alt="cBio@MSKCC"/></a>
                   </li>
				 </ul> 
            </td>
        </tr>
    </table>
    <!-- End DIV id="header" -->
    </div>

    <!-- End DIV id="header_wrapper" -->    
    </div>
	</td>
  </tr>

  <tr valign="top">
    <td>
        <div id="content">