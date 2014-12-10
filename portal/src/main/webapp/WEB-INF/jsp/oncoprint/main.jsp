<script src="js/lib/bootstrap.min.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript"></script>
<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
        
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="outer_loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4 style="display:inline;">OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints" target="_blank">What are OncoPrints?</a>)</small>
        </h4>

        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;"></div>
        <%@ include file="controls-templates.jsp" %>

        <div id="oncoprint-statment" style="margin-left:3;">
            <p>Case Set: <span id='oncoprint_sample_set_name'></span>: <span id='oncoprint_sample_set_description'></span></span></p>
        </div>
        
        <div id="oncoprint_whole_body">
            <%--
            <p> Altered in <%=dataSummary.getNumCasesAffected()%> (<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>) of cases
            
            <span class='oncoprint-diagram-toolbar-buttons' style="float:right;margin-right:20px;display: none;">
            
            <img id="oncoprint_diagram_showmutationcolor_icon" checked="0" style="width: 16px; height: 16px" class="oncoprint_diagram_showmutationcolor_icon" src="images/colormutations.svg">
            <img id="oncoprint-diagram-showlegend-icon" checked="0" style="width: 16px; height: 16px;display:none;" class="oncoprint-diagram-showlegend-icon" src="images/showlegend.svg">
            <img id="oncoprint-diagram-removeUCases-icon" checked="0" style="width: 16px; height: 16px" class="oncoprint-diagram-removeUCases-icon" src="images/removeUCases.svg">
            <img id="oncoprint-diagram-removeWhitespace-icon" checked="0" style="width: 20px; height: 16px" class="oncoprint-diagram-removeWhitespace-icon" src="images/removeWhitespace.svg">
            <img id="oncoprint-diagram-downloads-icon" style="width: 16px; height: 16px" class="oncoprint-diagram-downloads-icon" src="images/in.svg">
            <span class='oncoprint_diagram_slider_icon' style="width: 80px; height: 16px"></span>
            </span>
            </p>
            --%>

        Altered in <%=dataSummary.getNumCasesAffected()%> (<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>) of cases
        <div class="btn-group btn-group-sm" id="oncoprint-diagram-toolbar-buttons" style="float:right;margin-right:15px;display: none;height:33px">
            <%--   
            <select data-placeholder="Add a clinical attribute track" id="select_clinical_attributes" class="select_clinical_attributes_from" style="float:left;width: 360px;height:30px;background-color:#efefef;margin:0px">
            <option value=""></option>
            </select>
       
            <select id="sort_by" class="form-control" style="float:left;width:200px;height:30px;background-color:#efefef;margin:0px">
                <option value="genes">gene data first</option>
                <option value="clinical" disabled="true">clinical data first</option>
                <option value="alphabetical">alphabetically by case id</option>
                <option value="custom">user-defined case list / default</option>
            </select>
            --%>
            
            <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_showmorefeatures_icon" data-toggle="dropdown" style="background-color:#efefef;margin:0px;height:30px;">
                    <span data-bind="label" style="color:#7f7f7f;font-weight: 1000;">+</span>
                </button>
                <ul class="dropdown-menu" style="height: 0px;background-color:rgba(255,255,255,.0)">
                <li style="list-style-type:none;cursor:pointer;font-weight: bold;">
                    <select data-placeholder="Add a clinical attribute track" id="select_clinical_attributes" class="select_clinical_attributes_from" style="float:left;width: 360px;height:30px;background-color:rgba(255,255,255,.8);margin:0px">
                    <option value=""></option>
                    </select>
                </li>
                </ul>
            </div>
            
            <%-- 
            <div class="nav-collapse" style="float: left;" >
            <ul class="nav">
            <li>                  
                <select data-placeholder="Add a clinical attribute track" id="select_clinical_attributes" class="select_clinical_attributes_from" style="float:left;width: 360px;height:30px;background-color:#efefef;margin:0px">
                <option value=""></option>
                </select>
            </li>
            </ul>
            </div>
            --%>
            
            <div class="btn-group btn-group-sm">
               <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_sortbyfirst_dropdonw" data-toggle="dropdown" style="background-color:#efefef;height:30px;margin:0px">
                 <span data-bind="label">Sort by</span>&nbsp;<span class="caret"></span>
               </button>
               <ul class="dropdown-menu">
                   <li style="list-style-type:none;cursor:pointer" value="genes"><a id="genes_first_a">gene data first</a></li>
                 <li style="list-style-type:none;cursor:pointer;display: none" value="clinical" id="clinical_first"><a id="clinical_first_a">clinical data first</a></li>
                 <li style="list-style-type:none;cursor:pointer" value="alphabetical"><a id="alphabetically_first_a">alphabetically by case id</a></li>
                 <li style="list-style-type:none;cursor:pointer" value="custom"><a id="user_defined_first_a">user-defined case list / default</a></li>
               </ul>
            </div>
            <button type="button" class="btn btn-default" style="background-color:#efefef;margin:0px"><img id="oncoprint_diagram_showmutationcolor_icon" class="oncoprint_diagram_showmutationcolor_icon" checked="0" src="images/colormutations.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-showlegend-icon" style="background-color:#efefef;margin:0px;display:none;"><img class="oncoprint-diagram-showlegend-icon" checked="0" src="images/showlegend.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" style="background-color:#efefef;margin:0px"><img id="oncoprint-diagram-removeUCases-icon" class="oncoprint-diagram-removeUCases-icon" checked="0" src="images/removeUCases.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" style="background-color:#efefef;margin:0px"><img id="oncoprint-diagram-removeWhitespace-icon" class="oncoprint-diagram-removeWhitespace-icon" checked="0" src="images/removeWhitespace.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" style="background-color:#efefef;margin:0px"><img id="oncoprint-diagram-downloads-icon" class="oncoprint-diagram-downloads-icon" src="images/in.svg" alt="icon" width="16" height="16" /></button>      
            <div class="btn-group btn-group-sm">
                <button type="button" id="oncoprinter_zoomout" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-out.svg" alt="icon" width="16" height="16" /></button>
                <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 80px; height: 16px"></span> 
                <button type="button" id="oncoprint_zoomin" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-in.svg" alt="icon" width="16" height="16" /></button>
            </div>
        </div>
   
        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
        <div id="oncoprint_body"></div>
        </div>
            <div id="oncoprint_legend" style="display: inline;"></div>
        <%@ include file="legend-template.jsp" %>
        <script type="text/javascript">
            $('#oncoprint_diagram_showmorefeatures_icon').click(function(){
                $('#select_clinical_attributes').click();
                return false;
            });
        </script>    
        <div id="oncoprint_legend"></div>
        <%@ include file="legend-template.jsp" %>
        <script type="text/javascript">
            $('.dropdown-menu #select_clinical_attributes').click(function(){return false;});
        </script>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>