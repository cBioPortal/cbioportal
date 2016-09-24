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
<div class="section" id="igv_tab">
<script type="text/javascript">
    $(document).ready(function(){
        function generateHTML(cancerStudyId, hugoSymbol, sampleIds){
            var headerContent = '<head>    <link rel="stylesheet" type="text/css"  href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/themes/smoothness/jquery-ui.css"/>'
                + '<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css">'
                + '<link rel="stylesheet" type="text/css" href="css/igv.css">'
                + '<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"><\/script>'
                + '<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js"><\/script>'
                + '<script type="text/javascript" src="js/lib/igv.js"><\/script></head>';
            var bodyContent1 = '<body><div id="igvDiv" style="padding-top: 10px;padding-bottom: 10px; border:1px solid lightgray;width:100%"></div><script type="text/javascript">  $(document).ready(function () {    var div = $("#igvDiv")[0],   options = {'
                + 'showNavigation: true, showRuler: true, genome: "hg19", locus: "' + hugoSymbol + '", tracks: [  { url: "api/copynumbersegments", indexed: false, name: "Segmented CN", type:"seg", json: true, method: "POST", ';
            var bodyContent2 = 'cancerStudyId: "' + cancerStudyId + '", hugoSymbol: "' + hugoSymbol +'", sampleIds: "' + sampleIds + '"},{name: "Genes", url: "https://s3.amazonaws.com/igv.broadinstitute.org/annotations/hg19/genes/gencode.v18.collapsed.bed", order: Number.MAX_VALUE,  displayMode: "EXPANDED"}]};igv.createBrowser(div, options);});<\/script><\/body>';    
            var fullContent = headerContent + bodyContent1 + bodyContent2;
            return fullContent;
        }
        
        $("#igv-result-tab").click(function(){
            var genes = window.QuerySession.getQueryGenes();
            if(genes.length > 0){
                var sampleIds = window.QuerySession.getSampleIds().join(",");
                var height = 300 + 2*window.QuerySession.getSampleIds().length;
                height = Math.min(height, 800);
                $("#igvList").append('<li class="active"><a data-toggle="tab" href="#home">' + genes[0] + '</a></li>');
                $("#igvContent").append('<div id="home" class="tab-pane fade in active"></div>');
                if($("#home").html().length === 0){
                    var html = generateHTML('<%= cancerStudyId %>', genes[0], sampleIds);
                    var iframe = document.createElement('iframe');
                    $("#home").html(iframe);
                    iframe.style.cssText = 'width:100%;height:'+height+'px';
                    iframe.contentWindow.document.open();
                    iframe.contentWindow.document.write(html);
                    iframe.contentWindow.document.close();
                }
                for(var i = 1;i < genes.length;i++){
                    $("#igvList").append('<li class="geneList" value="' + i +  '"><a data-toggle="tab" href="#menu'+ i +'">' + genes[i] + '</a></li>');
                    $("#igvContent").append('<div id="menu' + i + '" class="tab-pane fade"></div>');
                }
                $(".geneList").click(function(event){
                    var index = $(this).val();
                    if($("#menu"+index).html().length === 0){
                        var html = generateHTML('<%= cancerStudyId %>', genes[index], sampleIds);
                        var iframe = document.createElement('iframe');
                        $("#menu"+index).html(iframe);
                        iframe.style.cssText = 'width:100%;height:'+height+'px';
                        iframe.contentWindow.document.open();
                        iframe.contentWindow.document.write(html);
                        iframe.contentWindow.document.close();
                    }
                });
            }
 
            
        });

    });
</script>

<div class="container">
    <ul class="nav nav-tabs" id="igvList"></ul>
    <div class="tab-content" id="igvContent"></div>
</div>


</div>
