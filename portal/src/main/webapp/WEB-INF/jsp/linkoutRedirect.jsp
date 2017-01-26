<%--
  Created by IntelliJ IDEA.
  User: suny1
  Date: 6/15/16
  Time: 5:50 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
    <%
        String redirectURL = (String)request.getAttribute("redirect_url");
    %>
</head>
<body>
    <script>
        window.location.replace('<%=redirectURL%>');
    </script>
</body>
</html>
