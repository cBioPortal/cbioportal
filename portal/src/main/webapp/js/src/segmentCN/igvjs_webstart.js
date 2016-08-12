
//------------------------------------------------------------------------------
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
 **/
// global variables
var igvjs_data_fetched = false;
var segGene = [];
var segCNUrl;

//function for clicking event that is used to lauch IGVJS tab and fetch data
var prepJSIGVForSegView = function (_studyId) {
    
    if (!igvjs_data_fetched) {
        $.when($.ajax({
            method : "GET",
            url : 'igvlinking.json',
            data : {
                cmd : 'get_igv_args',
                cancer_study_id : _studyId,
                gene_list : window.QuerySession.getQueryGenes().join(" ")
            }
        })).then(
                function(response) {                    
                    segGene =response['geneList'].split('+');
                    segCNUrl=response['segfileUrl'];
                    igvjs_data_fetched = true; 

                    addIGVButtons(segGene, segCNUrl);
                    startIGV(segGene[0].toLowerCase(), segCNUrl);                
                });        
    } 
}
/**
* function for adding buttons
* @gene      an array for buttons that represents genes
*/
var addIGVButtons = function (genes,url){

    var buttonNumber = genes.length;    
    for (i=0; i<buttonNumber; i++){
        $('#switchGenes').append(
            '<li class="ui-state-default ui-corner-top" role="tab" tabindex="0" >'+
            '<a href="#" class="ui-tabs-anchor" onclick="switchGenes('+"'"+genes[i]+"', '"+url+"')"+'"><span>' +genes[i]+'</span></a>'+
            '</li>');
    }  
}

//function for switching butons
var switchGenes = function(buttonVal, url){

    startIGV(buttonVal.toLowerCase(), url);  
}

//function used for displaying a gene's segmentCN  for a group of samples  
var startIGV = function(targetGene, segUrl) {

    options = {
                showNavigation: true,
                showRuler: true,
                genome: "hg19",
                locus: targetGene,
                tracks: [
                    {
                        url: segUrl,
                        indexed: false,
                        name: 'Segmented CN'
                    },
                    {
                        name: "Genes",
                        url: "https://s3.amazonaws.com/igv.broadinstitute.org/annotations/hg19/genes/gencode.v18.collapsed.bed",
                        order: Number.MAX_VALUE,
                        displayMode: "EXPANDED"
                    }
                ]
            };
     
    igv.createBrowser("#igvjs_tab", options);
}
