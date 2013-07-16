<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>

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

    <div id="oncoprint" style="width: 1091px; display: inline-block; overflow-x: auto; overflow-y: hidden;"></div>

    <div id="oncoprint_legend"></div>
    <script type="text/template" id="glyph_template">
        <svg height="23" width="6">
            <rect fill="{{bg_color}}" width="5.5" height="23"></rect>

            <rect display="{{display_mutation}}" fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>

            <path display="{{display_down_rppa}}" d="M0,2.182461848650375L2.5200898716287647,-2.182461848650375 -2.5200898716287647,-2.182461848650375Z" transform="translate(2.75,2.3000000000000003)"></path>
            <path display="{{display_up_rppa}}" d="M0,-2.182461848650375L2.5200898716287647,2.182461848650375 -2.5200898716287647,2.182461848650375Z" transform="translate(2.75,20.909090909090907)" aria-describedby="ui-tooltip-838"></path>

            <rect display="{{display_down_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#6699CC" fill="none" aria-describedby="ui-tooltip-732"></rect>
            <rect display="{{display_up_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#FF9999" fill="none" aria-describedby="ui-tooltip-576"></rect>
        </svg>
        <span style="position: relative; bottom: 6px;">{{text}}</span>
    </script>
</div>

</body>

<script type="text/javascript" src="js/d3.v3.min.js"></script>
<script type="text/javascript" src="js/underscore-min.js"></script>
<script type="text/javascript" src="js/EchoedDataUtils.js"></script>
<script type="text/javascript" src="js/MemoSort.js"></script>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.qtip.min.js"></script>
<script type="text/javascript" src="js/oncoprint.js"></script>
<script type="text/javascript">

    // This is for the moustache-like templates
    // prevents collisions with JSP tags
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };

    var data;
    $(':button').click(function(){
        var formData = new FormData($('form')[0]);
        $.ajax({
            url: 'echofile',
            type: 'POST',
            xhr: function() {  // custom xhr
                var myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // check if upload property exists
                    myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // for handling the progress of the upload
                }
                return myXhr;
            },
            //Ajax events
//                beforeSend: beforeSendHandler,
                success: function(res) {
                    data = res; console.log(res);

                    data = EchoedDataUtils.oncoprint_wash(data);
                    var genes = _.chain(data).map(function(d){ return d.gene; }).uniq().value();
                    var params = { geneData: data, genes:genes };
                    params.legend =  document.getElementById("oncoprint_legend");

                    Oncoprint(document.getElementById("oncoprint"), params);
                },
//                error: errorHandler,
            // Form data
            data: formData,
            //Options to tell JQuery not to process data or worry about content-type
            cache: false,
            contentType: false,
            processData: false
        });
    });

    function progressHandlingFunction(e){
        if(e.lengthComputable){
            $('progress').attr({value:e.loaded,max:e.total});
        }
    }
</script>
</html>