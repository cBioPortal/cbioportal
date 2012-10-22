

<%
String tissueImageUrl = SkinUtil.getTumorTissueImageUrl(cancerStudy.getCancerStudyStableId());
String jsonTissueImageNames = JSONValue.toJSONString(tissueImages);
%>

<script type="text/javascript">
    var tissueImageUrl = '<%=tissueImageUrl%>';
    var tissueImageNames = <%=jsonTissueImageNames%>;
    $(document).ready(function(){
        $("#tissue-images-div").html('<p>Click on the image(s) below to open high-resolution one(s).</p>');
        for (var i=0, len=tissueImageNames.length; i<len; i++) {
            var image = tissueImageUrl+tissueImageNames[i];
            var htm = "<a href='"+image+".jpg' alt='click to open the high resolution image'><img class='tissue-image' src='"+image+".png'></a><br/>"
            $("#tissue-images-div").append(htm);
        }
    });
</script>

<div id="tissue-images-div"><img src="images/ajax-loader.gif"/></div>