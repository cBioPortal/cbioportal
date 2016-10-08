<%--
 - Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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
<div class="section" id="igv_tab">
<script type="text/javascript">
    $(document).ready(function(){
        function generateHTML(cancerStudyId, hugoSymbol, id){
            $.when($.ajax({
                    method : "POST",
                    url : "api-legacy/gene/fetch-by-hugo",
                    data : {
                        hugo_gene_symbols: hugoSymbol
                    }
                })).then(
                    function(response) {
                        var sampleIds = window.QuerySession.getSampleIds().join(",");
                        var height = 300 + 2*window.QuerySession.getSampleIds().length;
                        height = Math.min(height, 800);
                        var headerContent = '<head>    <link rel="stylesheet" type="text/css"  href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/themes/smoothness/jquery-ui.css"/>'
                            + '<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css">'
                            + '<link rel="stylesheet" type="text/css" href="css/igv.css">'
                            + '<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"><\/script>'
                            + '<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js"><\/script>'
                            + '<script type="text/javascript" src="js/lib/igv.min.js"><\/script></head>';
                        var bodyContent1 = '<body><div id="igvDiv" style="padding-top: 10px;padding-bottom: 10px; border:1px solid lightgray;width:100%"></div><script type="text/javascript">  $(document).ready(function () {    var div = $("#igvDiv"),   options = {'
                            + 'divId: "igvDiv", showNavigation: true, showRuler: true, genome: "hg19", divId: "igvDiv", locus: "' + hugoSymbol + '", tracks: [  { url: "api-legacy/copynumbersegments", indexed: false, name: "Alt click to sort", type:"seg", json: true, method: "POST", ';
                        var bodyContent2 = 'cancerStudyId: "' + cancerStudyId + '", chromosome: "' + response[0].chromosome +'", sampleIds: "' + sampleIds + '"},{name: "Genes", url: "https://s3.amazonaws.com/igv.broadinstitute.org/annotations/hg19/genes/gencode.v18.collapsed.bed", order: Number.MAX_VALUE,  displayMode: "EXPANDED"}]};igv.createBrowser(div, options);});<\/script><\/body>';    
                        var fullContent = headerContent + bodyContent1 + bodyContent2;
                        var iframe = document.createElement('iframe');
                        $("#"+id).html(iframe);
                        iframe.style.cssText = 'width:100%;height:'+height+'px';
                        iframe.contentWindow.document.open();
                        iframe.contentWindow.document.write(fullContent);
                        iframe.contentWindow.document.close();
                    });
        }

        $("#igv-result-tab").click(function(){
            var genes = window.QuerySession.getQueryGenes();
            if(genes.length > 0 && $("#igvList").html().length === 0){
                $("#igvList").append('<li><a style="font-size:11px"  href="#home">' + genes[0] + '</a></li>');
                $("#igvContent").append('<div id="home" class="tab-pane fade in active"></div>');
                generateHTML('<%= cancerStudyId %>', genes[0], "home");
                for(var i = 1;i < genes.length;i++){
                    $("#igvList").append('<li class="geneList" value="' + i +  '"><a style="font-size:11px" href="#menu'+ i +'">' + genes[i] + '</a></li>');
                    $("#igvContent").append('<div id="menu' + i + '"></div>');
                }
                $("#segment_tabs").tabs();
                $(".geneList").click(function(event){
                    var index = $(this).val();
                    if($("#menu"+index).html().length === 0){
                        generateHTML('<%= cancerStudyId %>', genes[index], "menu"+index);
                    }
                });
            }
        });
    });
</script>

<div id="segment_tabs">
  <ul id="igvList"></ul>
  <div id="igvContent"></div>
</div>

</div>
