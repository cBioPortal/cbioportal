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
        <input type="button" value="Go!"><br/>
        <progress></progress>
    </form>

    <div id="oncoprint"></div>
    <div id="oncoprint_legend"></div>
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
//                    params.legend =  document.getElementById("oncoprint_legend");

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