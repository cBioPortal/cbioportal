 <!--
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 ** 
 ** Goal: generating segment CN visualization using igv.js API
 ** Authors: Linghong Chen
 ** Written on August 12, 2016
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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
<script type="text/javascript" src="js/src/segmentCN/igvjs_webstart.js"></script>
