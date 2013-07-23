<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
    <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery.qtip.min.css"/>

    <style type="text/css">
        progress {
            background-color: #f3f3f3;
            border: 0;
            height: 18px;
            border-radius: 9px;
        }
    </style>
</head>
<body>

<div id="container" style="margin-left:50px; margin-top:50px;">
    <h1>Custom Oncoprint</h1>

    <form name="echofile" action="echofile" enctype="multipart/form-data" method="POST">
    <p>Choose data files to upload:</p>

    <div>
        <span>Copy Number File</span>
        <input name="cna" type="file" size="40">
    </div>
    <div>
        <span>Mutations File</span>
        <input name="mutation" type="file" size="40">
    </div>
        <input type="button" value="Go!"><progress></progress>
    </form>

    <%@ include file="WEB-INF/jsp/oncoprint/main.jsp"%>

</body>

<script type="text/javascript" src="js/lib/jquery.min.js"></script>
<script type="text/javascript" src="js/lib/jquery.qtip.min.js"></script>
<script type="text/javascript" src="js/lib/d3.v3.min.js"></script>
<script type="text/javascript" src="js/lib/underscore-min.js"></script>
<script data-main="js/src/oncoprint/custom-boilerplate.js" type="text/javascript" src="js/require.js"></script>
</html>
