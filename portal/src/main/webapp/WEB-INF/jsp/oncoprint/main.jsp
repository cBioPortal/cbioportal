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
    <!--<div id="oncoprint_progress_indicator">
        <p id="oncoprint_progress_indicator_text"></p>
        <svg width="200px" height="20px" style="outline: 1px solid #888888">
            <rect id="oncoprint_progress_indicator_rect" fill="#1974b8" height="20px">
        </svg>
    </div>-->
    <p id="oncoprint_progress_indicator"></p>
    <div style="display:none;" id="everything">
        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;"></div>
        <%@ include file="controls-templates.jsp" %>

        <div id="oncoprint-statment" style="margin-left:3; margin-top: 10px">
            <p><span id='oncoprint_sample_set_description'></span></span>
                <span><button id="switchPatientSample" type="button" valuetype="patients" class="btn btn-primary btn-xs jstree-node" style="display: none; cursor:pointer;  padding: 0px 5px; font-weight: normal;font-style: normal;color:white; background-color:#2986e2; font:small-caption;">Show samples in OncoPrint</button></span></p>
        </div>
        
        <div id="oncoprint_whole_body">

        <span id="altered_value" style="float:left; margin-top:12px"></span>
        <div class="btn-group btn-group-sm" id="oncoprint-diagram-toolbar-buttons" style="float:right;margin-right:15px;display: none;height:33px">           
            <div class="btn-group btn-group-sm" id="oncoprint_addclinical_attributes">
                <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_showmorefeatures_icon" data-toggle="dropdown" style="background-color:#efefef;margin:0px">
                 <span>Clinical Tracks</span>&nbsp;<span class="caret"></span>
               </button>
                <ul class="dropdown-menu" id="clinical_dropdown" style="height: 0px;background-color:rgba(255,255,255,.0)">
                <li style="list-style-type:none;cursor:pointer;font-weight: bold;">
                    <select data-placeholder="Add a clinical attribute track" id="select_clinical_attributes" class="select_clinical_attributes_from chosen-select" title="Add a clinical attribute track" style="float:left;width: 360px;height:30px;background-color:rgba(255,255,255,.8);margin:0px">
                    <option value=""></option>
                    </select>
                </li>
                </ul>
            </div>
            
            <div class="btn-group btn-group-sm"   id="oncoprint_diagram_sortby_group">
               <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_sortbyfirst_dropdown" data-toggle="dropdown" style="background-color:#efefef;margin:0px">
                 <span id="oncoprint_diagram_sortby_label" data-bind="label">Sort</span>&nbsp;<span class="caret"></span>
               </button>
                <div class="dropdown-menu" style="padding: 10px; width: 250px;">
                   <form action="" style="margin-bottom: 0;">
                       <div class="radio"><label><input type="radio" name="sortby" value="data" /> Sort by data</label></div>
                       <div style="margin-left: 10px;">
                          <div class="checkbox"><label><input type="checkbox" name="type" value="type" /> Mutation Type</label></div>
                          <div class="checkbox"><label><input type="checkbox" name="recurrence" value="recurrence" /> Mutation Recurrence</label></div>
                       </div>
                       <div class="radio"><label><input type="radio" name="sortby" value="id" /> Sort by case id (alphabetical)</label></div>
                       <div class="radio"><label><input type="radio" name="sortby" value="custom" /> Sort by user-defined order / default</label></div>
                   </form>
               </div>
            </div>
            <div class="btn-group btn-group-sm"   id="oncoprint_diagram_mutation_color">
               <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_mutation_color_dropdown" data-toggle="dropdown" style="background-color:#efefef;margin:0px">
                 <span>Mutation Color</span>&nbsp;<span class="caret"></span>
               </button>
               <div class="dropdown-menu" style="padding: 10px 5px; width: 120px;min-width: 120px;">
                   <form action="" style="margin-bottom: 0;">
                       <div class="checkbox"><label><input type="checkbox" name="type" value="type" /> Type</label></div>
                       <div class="checkbox"><label><input type="checkbox" name="recurrence" value="recurrence" /> Recurrence</label></div>
                   </form>
               </div>
            </div>
            <div class="btn-group btn-group-sm"   id="oncoprint_diagram_view_menu">
               <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_mutation_color_dropdown" data-toggle="dropdown" style="background-color:#efefef;margin:0px">
                 <span>View</span>&nbsp;<span class="caret"></span>
               </button>
               <div class="dropdown-menu" style="padding: 10px 5px; width: 270px;min-width: 270px;">
                   <form action="" style="margin-bottom: 0;">
                       <fieldset style="border: 0; padding-bottom: 0;">
                           <legend style="margin-bottom: 0; font-size: 18px;">Data type:</legend>
                           <div class="radio"><label><input type="radio" name="datatype"  value="sample"> Events per sample</label></div>
                           <div class="radio"><label><input type="radio" name="datatype"  value="patient"> Events per patient</label></div>
                       </fieldset>
                       <div class="checkbox"><label><input type="checkbox" name="show_unaltered" value="show_unaltered"> Show unaltered columns</label></div>
                       <div class="checkbox"><label><input type="checkbox" name="show_whitespace" value="show_whitespace"> Show whitespace between columns</label></div>
                       <div class="checkbox"><label><input type="checkbox" name="show_clinical_legends" value="show_clinical_legends"> Show legends for clinical tracks</label></div>
                   </form>
               </div>
            </div>
            
            <div class="btn-group btn-group-sm"   id="oncoprint_diagram_download_menu">
               <button type="button" class="btn btn-default dropdown-toggle" id="oncoprint_diagram_mutation_color_dropdown" data-toggle="dropdown" style="background-color:#efefef;margin:0px">
                 <span>Download</span>&nbsp;<span class="caret"></span>
               </button>
               <div class="dropdown-menu" style="padding: 10px 5px; width: 70px;min-width: 70px;">
                    <button class='oncoprint-diagram-download' type='pdf' style='font-size:13px; cursor:pointer;width:50px;'>PDF</button> <br/>
                    <button class='oncoprint-diagram-download' type='png' style='font-size:13px; cursor:pointer;width:50px;'>PNG</button> <br/>
                    <button class='oncoprint-diagram-download' type='svg' style='font-size:13px; cursor:pointer;width:50px;'>SVG</button> <br/>
                    <button class='oncoprint-sample-download'  type='txt' style='font-size:13px; cursor:pointer;width:50px;'>Sample order</button>
               </div>
            </div>
            
            <div class="btn-group btn-group-sm">
                <button type="button" id="oncoprint_zoomout" class="btn btn-default" style="background-color:#efefef;margin:0px;border-right: 0;"><img src="images/zoom-out.svg" alt="icon" width="18" height="18" /></button>
                <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 100px;display:inline;border-left: 0;border-right: 0;padding-left: 0;padding-right: 0;"></span>
                <input type="text" id="oncoprint_zoom_scale_input" class="form-control" style="border-radius: 0;float: left;width: 35px;height: 18px;padding: 5px 10px;padding-left:0px; padding-right:0px;border-left: 0;border-right: 0;" title="Zoom scale">
                <span type="button" id="oncoprint_zoom_scale_input_tail" class="btn btn-default" style="margin: 0px;background-color: rgb(239, 239, 239);border-left: 0;border-right: 0;padding-left:2px;">%</span>
                <button type="button" id="oncoprint_zoomin" class="btn btn-default" style="background-color:#efefef;margin:0px;border-left: 0;"><img src="images/zoom-in.svg" alt="icon" width="18" height="18" /></button>
                <button type="button" id="oncoprint_zoomtofit" class="btn btn-default" style="background-color:#efefef;margin:0px;border-left: 0;"><img src="images/fitalteredcases.svg" alt="icon" width="18" height="18" preserveAspectRatio="none"/></button>
            </div>
        </div>
        <br><br>
        <div id="oncoprint_body"></div>
        </div>
        <div id="oncoprint_legend" style="display: inline;"></div>
        <%@ include file="legend-template.jsp" %>   
        <script type="text/javascript">
//            $( document.body ).on( 'click', '.dropdown-menu li', function( event ) {
            $('#oncoprint_diagram_sortby_group' ).on( 'click', '.dropdown-menu li', function( event ) {
                $('#oncoprint_diagram_sortby_label').text($(event.currentTarget).text());
                $('#oncoprint_diagram_sortby_group').children('.dropdown-toggle').dropdown('toggle');
                return false;
              /*var $target = $( event.currentTarget );

              $target.closest( '.btn-group' )
                 .find( '[data-bind="label"]' ).text( $target.text() )
                    .end()
                 .children( '.dropdown-toggle' ).dropdown( 'toggle' );

              return false;*/

            });
//           $('.dropdown-menu #select_clinical_attributes').click(function(){$('#clinical_dropdown').dropdown('toggle');});
        </script>

        <!--<script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/events.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/utils.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/defaults.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/RuleSet.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/OncoprintRenderer.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/OncoprintSVGRenderer.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/oncoprint.js?<%=GlobalProperties.getAppVersion()%>"></script>-->
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/OncoprintUtils.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/webgl/oncoprint-bundle.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <!--<script type="text/javascript" charset="utf-8" src="js/src/oncoprint/setup-oncoprint-improved.js?<%=GlobalProperties.getAppVersion()%>"></script>-->
        <!--<script type="text/javascript" charset="utf-8" src="js/src/oncoprint/new/ruleset2.js"?<%=GlobalProperties.getAppVersion()%>"></script>-->
        <!--<script type="text/javascript" charset="utf-8" src="js/src/oncoprint/oncoprint-analysis-setup.js?<%=GlobalProperties.getAppVersion()%>"></script>-->
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/webgl/geneticrules.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/canvas-toBlob.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/zlib.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/png.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.plugin.addimage.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.plugin.png_support.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/webgl/setup.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/webgl/setup-main.js?<%=GlobalProperties.getAppVersion()%>"></script>
        <!--<script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>-->
    </div>
</div>