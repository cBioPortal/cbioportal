
<script type="text/javascript">
    var tissueImageUrl = '<%=tissueImageUrl%>';
    $("#link-tissue-images").click(function(){
        $("#tissue-images-div")
                .html('<iframe id="frame" src="'+tissueImageUrl+'" width="100%" height="600px"></iframe>');
        
    });
</script>

<div id="tissue-images-div"><img src="images/ajax-loader.gif"/></div>