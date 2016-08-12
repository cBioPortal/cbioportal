<!---
 - Code written by: Linghong Chen, final revision on August 10, 2016
 - Authors: Linghong Chen
 - This file is written for igv.js
 - Code is written on August 11, 2016
 -->

<style type="text/css">

.igv-ui-tabs{
    display: block;
    border: 1px solid #aaaaaa;
    padding: 2px;
    background: none;
}

</style>

<!-- IGV CSS -->
<link rel="stylesheet" type="text/css" href="https://igv.org/web/release/1.0.1/igv-1.0.1.css">
   
<div class="section ui-corner-bottom" >
    <!--display igv-->
    <div class="igv-ui-tabs ui-corner-bottom" id="igvjs_tab" >
        <!--display buttons-->
        <ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all' role='group' aria-label='switch genes' id='switchGenes' value='<%= cancerTypeId %>'>    
        </ul>
    </div>
</div>

<!-- IGV JS-->
<script type="text/javascript" src="https://igv.org/web/release/1.0.1/igv-1.0.1.js"></script>

