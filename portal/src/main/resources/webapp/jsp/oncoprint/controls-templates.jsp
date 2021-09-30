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

<script type="text/template" id="main-controls-template">
    <style>
        .onco-customize {
        color:#2153AA; font-weight: bold; cursor: pointer;
        }
        .onco-customize:hover { text-decoration: underline; }

    <div id="main" style="display:inline;">
    </div>
</script>

<script type="text/template" id="custom-controls-template">                
    <style>
        .onco-customize {
        color:#2153AA; font-weight: bold; cursor: pointer;
    }
    .onco-customize:hover { text-decoration: underline; }
    </style>  
    <div class="btn-group btn-group-sm" id="oncoprinter-diagram-toolbar-buttons" style="float:right;margin-right:45px;display: inline;height:33px">
        <div class="btn-group btn-group-sm" id="oncoprinter_diagram_sortby_group">
           <button type="button" class="btn btn-default dropdown-toggle" id="oncoprinter_sortbyfirst_dropdonw" data-toggle="dropdown" style="background-color:#efefef">
             <span data-bind="label">Sort by</span>&nbsp;<span class="caret"></span>
           </button>
           <ul class="dropdown-menu">
             <li style="list-style-type:none;cursor:pointer"><a id="genesfirst">Genes first</a></li>
             <li style="list-style-type:none;cursor:pointer;display:none"><a id="clinicalfirst">Clinical first</a></li>
             <li style="list-style-type:none;cursor:pointer"><a id="alphabetical">alphabetically by case id</a></li>
             <li style="list-style-type:none;cursor:pointer"><a id="custom">user-defined case list / default</a></li>
           </ul>
        </div>
        <button type="button" class="btn btn-default" id="oncoprinter_diagram_showmutationcolor_icon" style="background-color:#efefef"><img  class="oncoprinter_diagram_showmutationcolor_icon" checked="0" src="images/uncolormutations.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" id="oncoprinter-diagram-showlegend-icon" style="background-color:#efefef;display:none;"><img class="oncoprinter-diagram-showlegend-icon" checked="0" src="images/showlegend.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" id="oncoprinter-diagram-removeUCases-icon" style="background-color:#efefef"><img class="oncoprinter-diagram-removeUCases-icon" checked="0" src="images/removeUCases.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" id="oncoprinter-diagram-removeWhitespace-icon" style="background-color:#efefef"><img class="oncoprinter-diagram-removeWhitespace-icon" checked="0" src="images/removeWhitespace.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" style="background-color:#efefef"><img id="oncoprinter-diagram-downloads-icon" class="oncoprinter-diagram-downloads-icon" src="images/in.svg" alt="icon" width="16" height="16" /></button>      
        
        <div class="btn-group btn-group-sm">
            <button type="button" id="oncoprinter_zoomout" class="btn btn-default" style="background-color:#efefef"><img src="images/zoom-out.svg" alt="icon" width="16" height="16" /></button>
            <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 100px;display:inline"></span>
            <button type="button" id="oncoprinter_zoomin" class="btn btn-default" style="background-color:#efefef"><img src="images/zoom-in.svg" alt="icon" width="16" height="16" /></button>
        </div>
    </div>
    <div style="height:20px;"></div>
</script>

<script type="text/javascript">
  
</script>