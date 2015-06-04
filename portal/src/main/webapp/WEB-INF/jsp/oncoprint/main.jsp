<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

        
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="outer_loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;"></div>
        <%@ include file="controls-templates.jsp" %>

        <div id="oncoprint-statment" style="margin-left:3;">
            <p>Case Set: <span id='oncoprint_sample_set_name'></span>: <span id='oncoprint_sample_set_description'></span></span><span><button id="switchPatientSample" type="button" valuetype="patients" class="btn btn-primary btn-xs jstree-node" style="display: none; cursor:pointer;  padding: 0px 5px; font-weight: normal;font-style: normal;margin-left: 10px; color:white; background-color:#2986e2; font:small-caption;">Show samples in OncoPrint</button></span></p>
        </div>
        
        <div id="oncoprint_whole_body">

        <span id="altered_value"></span>
        <div class="btn-group btn-group-sm" id="oncoprint-diagram-toolbar-buttons" style="float:right;margin-right:15px;display: none;height:33px">           
            <div class="btn-group btn-group-sm" id="oncoprint_addclinical_attributes">
                <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_showmorefeatures_icon" data-toggle="dropdown" style="background-color:#efefef;margin:0px;height:30px;">
                    <span data-bind="label" style="color:#7f7f7f;">+</span>
                </button>
                <ul class="dropdown-menu" id="clinical_dropdown" style="height: 0px;background-color:rgba(255,255,255,.0)">
                <li style="list-style-type:none;cursor:pointer;font-weight: bold;">
                    <select data-placeholder="Add a clinical attribute track" id="select_clinical_attributes" class="select_clinical_attributes_from chosen-select" style="float:left;width: 360px;height:30px;background-color:rgba(255,255,255,.8);margin:0px">
                    <option value=""></option>
                    </select>
                </li>
                </ul>
            </div>
            
            <div class="btn-group btn-group-sm"   id="oncoprint_diagram_sortby_group">
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
            <button type="button" class="btn btn-default" id="oncoprint_diagram_topatientid_icon" style="background-color:#efefef;margin:0px"><img checked="0" src="images/cool2.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint_diagram_showmutationcolor_icon" style="background-color:#efefef;margin:0px"><img checked="0" src="images/colormutations.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-showlegend-icon" style="background-color:#efefef;margin:0px;display:none;"><img class="oncoprint-diagram-showlegend-icon" checked="0" src="images/showlegend.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-removeUCases-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-removeUCases-icon" checked="0" src="images/removeUCases.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-removeWhitespace-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-removeWhitespace-icon" checked="0" src="images/removeWhitespace.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-downloads-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-downloads-icon" src="images/in.svg" alt="icon" width="16" height="16" /></button>      
            <div class="btn-group btn-group-sm">
                <button type="button" id="oncoprint_zoomout" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-out.svg" alt="icon" width="16" height="16" /></button>
                <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 120px;display:inline"></span> 
                <button type="button" id="oncoprint_zoomin" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-in.svg" alt="icon" width="16" height="16" /></button>
            </div>
        </div>
        
        <div style="height:20px;"></div>  
        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
        <div id="oncoprint_body"></div>
        </div>
        <div id="oncoprint_legend" style="display: inline;"></div>
        <%@ include file="legend-template.jsp" %>   
        <script type="text/javascript">
//            $( document.body ).on( 'click', '.dropdown-menu li', function( event ) {
            $('#oncoprint_diagram_sortby_group' ).on( 'click', '.dropdown-menu li', function( event ) {
              var $target = $( event.currentTarget );

              $target.closest( '.btn-group' )
                 .find( '[data-bind="label"]' ).text( $target.text() )
                    .end()
                 .children( '.dropdown-toggle' ).dropdown( 'toggle' );

              return false;

            });
//           $('.dropdown-menu #select_clinical_attributes').click(function(){$('#clinical_dropdown').dropdown('toggle');});
        </script>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>