
<script type="text/javascript">
    var tissueImageUrl = '<%=tissueImageUrl%>';
    
    var tissueImageLoaded = false;
    function loadImages(){
        if (!tissueImageLoaded) {
            $("#tissue-images-div").html('<iframe id="frame" src="'+tissueImageUrl+'" width="100%" height="700px"></iframe>');
            tissueImageLoaded = true;
        }
    }
    $("#link-tissue-images").click(loadImages);
</script>

<div id="tissue-images-div"><img src="images/ajax-loader.gif"/></div>