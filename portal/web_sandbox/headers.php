<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"><!-- Include Global Style Sheets -->
		<link rel="icon" href="http://cbio.mskcc.org/favicon.ico">
		<link href="css/popeye/jquery.popeye.css" type="text/css" rel="stylesheet">
		<link href="css/popeye/jquery.popeye.style.css" type="text/css" rel="stylesheet">
		<link href="css/tipTip.css" type="text/css" rel="stylesheet">
		<link href="css/global_portal.css" type="text/css" rel="stylesheet">
		<link href="css/redmond/jquery-ui-1.8.14.custom.css" type="text/css" rel="stylesheet"><!-- Include Global List of Javascript Files to Load -->

		<script type="text/javascript" src="js/jquery.min.js">
</script>
		<script type="text/javascript" src="js/jquery.tipTip.minified.js">
</script>
		<script type="text/javascript" src="js/jquery.address-1.4.min.js">
</script>
		<script type="text/javascript" src="js/jquery-ui-1.8.14.custom.min.js">
</script>
		<script type="text/javascript" src="js/jquery.cookie.js">
</script>
		<script type="text/javascript" src="js/cgx_jquery.js">
</script>
		<script type="text/javascript" src="js/global-tabs.js">
</script>
		<script type="text/javascript" src="js/jquery.popeye-2.0.4.min.js">
</script>
		<script type="text/javascript" src="js/mailme.js">
</script>
		<title>
			cBio Cancer Genomics Portal::What's New
		</title>
	</head>
	<body>
		<center>
			<div id="page_wrapper">
				<table width="860px" cellpadding="0px" cellspacing="5px" border="0px">
					<tr valign="top">
						<td colspan="3">
							<div id="header_wrapper">
								<div id="header">
									
									<? createHeader("HEADER"); ?>
									<? createHeader("HEADER"); ?>
									<? createHeader("HEADER"); ?>
									
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
														<a href="cgds_r.jsp">R/MATLAB</a>
													</li>
													<li class="internal">
														<a href="networks.jsp">Networks</a>
													</li>
													<li>
														<a href="http://www.twitter.com/cbioportal"><img style="margin-top:5px; margin-bottom:4px" src="images/twitter-b.png" title="Follow us on Twitter" alt="Follow us on Twitter"></a>
													</li>
													<li>
														<a href="http://cbio.mskcc.org"><img style="margin-top:6px; margin-bottom:4px; margin-right:-3px" src="images/cbioLogo.png" title="cBio@MSKCC" alt="cBio@MSKCC"></a>
													</li>
												</ul>
											</td>
										</tr>
									</table><!-- End DIV id="header" -->
								</div><!-- End DIV id="header_wrapper" -->
							</div>
						</td>
					</tr>
					<tr valign="top">
						<td>
							<div id="content"></div>
						</td>
						<td width="172">
							<div id="right_side"></div>
						</td>
					</tr>
				</table>
			</div>
		</center>
	</body>
</html>

<?
	function createHeader($siteName) { 
?>
<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
	<tr>
		<td class="logo" width="250px">
			<a href="http://www.mskcc.org"><img src="images/msk_logo.png" alt="MSKCC Logo"></a>
		</td>
		<td class="logo" width="330px">
			<a href="index.do"><$ siteName $></a>
		</td>
		<td class="logo" width="200px">
			<img src="images/tag_line.png" alt="Tag Line">
		</td>
	</tr>
</table>
<?	}
?>