<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true">
    <jsp:param name="html_title" value="boo ya"></jsp:param>
</jsp:include>

    <style type="text/css">
        progress {
            background-color: #f3f3f3;
            border: 0;
            height: 18px;
            border-radius: 9px;
        }
    </style>

<script type="text/javascript" src="js/lib/jquery-ui-1.8.14.custom.min.js"></script>

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
        <input id="submit" type="button" value="Go!"><progress></progress>
    </form>

    <div id="oncoprint_controls"></div>
    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>

    <div id="oncoprint"></div>
</div>

</div>
</td>
</tr>
</table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
<script data-main="js/src/oncoprint/custom-boilerplate.js" type="text/javascript" src="js/require.js"></script>
</html>
