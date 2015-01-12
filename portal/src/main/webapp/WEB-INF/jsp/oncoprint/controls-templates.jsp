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

        <button type="button" class="btn btn-default" style="background-color:#efefef"><img id="oncoprinter_diagram_showmutationcolor_icon" class="oncoprinter_diagram_showmutationcolor_icon" checked="0" src="images/colormutations.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" style="background-color:#efefef;display:none;"><img id="oncoprinter-diagram-showlegend-icon" class="oncoprinter-diagram-showlegend-icon" checked="0" src="images/showlegend.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" style="background-color:#efefef"><img id="oncoprinter-diagram-removeUCases-icon" class="oncoprinter-diagram-removeUCases-icon" checked="0" src="images/removeUCases.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" style="background-color:#efefef"><img id="oncoprinter-diagram-removeWhitespace-icon" class="oncoprinter-diagram-removeWhitespace-icon" checked="0" src="images/removeWhitespace.svg" alt="icon" width="16" height="16" /></button>
        <button type="button" class="btn btn-default" style="background-color:#efefef"><img id="oncoprinter-diagram-downloads-icon" class="oncoprinter-diagram-downloads-icon" src="images/in.svg" alt="icon" width="16" height="16" /></button>      
        <div class="btn-group btn-group-sm">
            <button type="button" id="oncoprinter_zoomout" class="btn btn-default" style="background-color:#efefef"><img src="images/zoom-out.svg" alt="icon" width="16" height="16" /></button>
            <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 80px; height: 20px"></span>
            <button type="button" id="oncoprinter_zoomin" class="btn btn-default" style="background-color:#efefef"><img src="images/zoom-in.svg" alt="icon" width="16" height="16" /></button>
        </div>
    </div>
</script>

<script type="text/javascript">
  
</script>