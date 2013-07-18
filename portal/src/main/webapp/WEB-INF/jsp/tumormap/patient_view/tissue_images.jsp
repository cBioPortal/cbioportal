
<script type="text/javascript">
    var tissueImageUrl = '<%=tissueImageUrl%>';
    
      var tissueImageLoaded = false;
    $("#link-tissue-images").click(function(){
        if (!tissueImageLoaded) {
            $("#tissue-images-div").html('<iframe id="frame" src="'+tissueImageUrl+'" width="100%" height="600px"></iframe>');
            tissueImageLoaded = true;
        }
    });
</script>

<div id="tissue-images-div"><img src="images/ajax-loader.gif"/></div>