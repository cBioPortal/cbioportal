<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 - Code written by: Linghong Chen, final revision on August 10, 2016
 - Authors: Linghong Chen
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
    <div class="igv-ui-tabs ui-corner-bottom" id="igv_tab" >
        <ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all' role='group' aria-label='switch genes' id='switchGenes' value='<%= cancerTypeId %>'>    
        </ul>
    </div>
</div>

<!-- IGV JS-->
<!--<script type="text/javascript" src="https://igv.org/web/release/1.0.1/igv-1.0.1.js"></script>-->
    <!-- Vendor JS -->
    <script type="text/javascript" src="js/lib/igv/vendor/inflate.js"></script>
    <script type="text/javascript" src="js/lib/igv/vendor/jquery.mousewheel.js"></script>
    <script type="text/javascript" src="js/lib/igv/vendor/jquery.kinetic.min.js"></script>
    <script type="text/javascript" src="js/lib/igv/vendor/opentip-native.min.js"></script>
    <script type="text/javascript" src="js/lib/igv/vendor/promise-7.0.4.js"></script>
    <script type="text/javascript" src="js/lib/igv/vendor/zlib_and_gzip.min.js"></script>

    <!-- IGV JS -->
    <script type="text/javascript" src="js/lib/igv/js/binary.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/browser.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/fasta.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/genome.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ideogram.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/igv-create.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/igv-color.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/igv-utils.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/igv-canvas.js"></script>   
    <script type="text/javascript" src="js/lib/igv/js/igv-exts.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/igvxhr.js"></script>   
    <script type="text/javascript" src="js/lib/igv/js/intervalTree.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/module.js"></script> 
    <script type="text/javascript" src="js/lib/igv/js/oauthTest.js"></script>     
    <script type="text/javascript" src="js/lib/igv/js/parseUtils.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/rulerTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/referenceFrame.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/sequenceTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/set.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/svg.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/trackCore.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/trackView.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/windowSizePanel.js"></script> 

    <script type="text/javascript" src="js/lib/igv/js/bam/alignmentContainer.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/coverageMap.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bgzf.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamSource.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamAlignment.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamAlignmentRow.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/bamIndex.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bam/pairedAlignment.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/bigQuery/bigQuery.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigQuery/ccp.js"></script>   

    <script type="text/javascript" src="js/lib/igv/js/bigwig/bufferedReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigwig/bwBPTree.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigwig/bwReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigwig/bwRPTree.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigwig/bwSource.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/bigwig/bwTotalSummary.js"></script>

   <script type="text/javascript" src="js/lib/igv/js/encode/encode.js"></script>
   <script type="text/javascript" src="js/lib/igv/js/encode/encodeSearch.js"></script>

   <script type="text/javascript" src="js/lib/igv/js/feature/aneuSource.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/aneuTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/featureCache.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/featureFileReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/featureParsers.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/featureSource.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/featureTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/gffHelper.js"></script>       
    <script type="text/javascript" src="js/lib/igv/js/feature/segParser.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/segTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/tribble.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/feature/wigTrack.js"></script>
 
    <script type="text/javascript" src="js/lib/igv/js/ga4gh/ga4gAlignment.js"></script> 
    <script type="text/javascript" src="js/lib/igv/js/ga4gh/ga4ghAlignmentReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ga4gh/ga4ghVariantReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ga4gh/ga4ghHelper.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ga4gh/googleUtils.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/gtex/eqtlTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/gtex/gtexFileReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/gtex/gtexReader.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/gtex/gtex.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/gtex/immvarReader.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/gwas/t2dVariantSource.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/gwas/gwasTrack.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/karyo/karyo.js"></script>

    <script src="/js/lib/igv/js/oauth/google.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/ui/trackMenuPopupDialog.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ui/colorpicker.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ui/dataRangeDialog.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ui/dialog.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ui/userFeedback.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/ui/popover.js"></script>

    <script type="text/javascript" src="js/lib/igv/js/variant/variantTrack.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/variant/vcfParser.js"></script>
    <script type="text/javascript" src="js/lib/igv/js/variant/variant.js"></script>
 
