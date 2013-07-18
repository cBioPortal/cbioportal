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

    <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;">
        <style>
            .onco-customize {
                color:#2153AA; font-weight: bold; cursor: pointer;
            }
            .onco-customize:hover { text-decoration: underline; }
        </style>
        <p onclick="$('#oncoprint_controls #main').toggle(); $('#oncoprint_controls .triangle').toggle();"
           style="margin-bottom: 0px;">
            <span class="triangle ui-icon ui-icon-triangle-1-e" style="float: left; display: block;"></span>
            <span class="triangle ui-icon ui-icon-triangle-1-s" style="float: left; display: none;"></span>
            <span class='onco-customize'>Customize</span>
        </p>

        <div id="main" style="display:none;">
            <table style="padding-left:13px; padding-top:5px">
                <tr>
                    <td style="padding-right: 15px;"><span>Zoom</span><div id="zoom" style="display: inline-table;"></div></td>
                    <td><input id='toggle_unaltered_cases' type='checkbox' onclick='oncoprint.toggleUnalteredCases();'>Remove Unaltered Cases</td>
                    <td><input id='toggle_whitespace' type='checkbox' onclick='oncoprint.toggleWhiteSpace();'>Remove Whitespace</td>
                </tr>
                <tr>
                    <td>
                        <span>Sort by: </span>
                        <select id="sort_by" style="width: 200px;">
                            <option value="genes">gene data</option>
                            <option value="clinical" disabled>clinical data</option>
                            <option value="alphabetical">alphabetically by case id</option>
                            <option value="custom">user-defined case list / default</option>
                        </select>
                    </td>
                </tr>
            </table>
        </div>
    </div>

    <div id="oncoprint"></div>

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

<script type="text/javascript" src="js/lib/jquery.min.js"></script>
<script type="text/javascript" src="js/lib/jquery.qtip.min.js"></script>
<script type="text/javascript" src="js/lib/d3.v3.min.js"></script>
<script type="text/javascript" src="js/lib/underscore-min.js"></script>

<script type="text/javascript" src="js/src/EchoedDataUtils.js"></script>
<script type="text/javascript" src="js/src/MemoSort.js"></script>
<script type="text/javascript" src="js/src/oncoprint.js"></script>
<script type="text/javascript">

    // This is for the moustache-like templates
    // prevents collisions with JSP tags
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };

    // some hacks to get the oncoprint spiffy for this page.
    // Perhaps these should be properly incorporated as options.
    var hacks_on_callback = function() {
        // remove text: "Copy number alterations are putative."
        $('#oncoprint_legend p').remove();
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

                    hacks_on_callback();
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
