function GenomicOverviewConfig(nRows,width) {
    this.nRows = nRows;
    this.canvasWidth = width;
    this.wideLeftText = 25;
    this.wideRightText = 35;
    this.GenomeWidth = this.canvasWidth-this.wideLeftText-this.wideRightText;
    this.pixelsPerBinMut = 3;
    this.rowHeight = 20;
    this.rowMargin = 5;
    this.ticHeight = 10;
    this.cnTh = [0.2,1.5];
    this.cnLengthTh = 50000;
}
GenomicOverviewConfig.prototype = {
    getCnColor: function(cnValue) {
        if (cnValue>=this.cnTh[1])
            return "#f00";
        if (cnValue<=-this.cnTh[1])
            return "#00f";
        var c = Math.round(255*(this.cnTh[1]-Math.abs(cnValue))/(this.cnTh[1]-this.cnTh[0]));
        if (cnValue<0)
            return "rgb("+c+","+c+",255)";
        else
            return "rgb(255,"+c+","+c+")";
    },
    canvasHeight: function() {
        return 2*this.rowMargin+this.ticHeight+this.nRows*(this.rowHeight+this.rowMargin);
    },
    yRow: function(row) {
        return 2*this.rowMargin+this.ticHeight+row*(this.rowHeight+this.rowMargin);
    },
    xRightText: function() {
        return this.wideLeftText + this.GenomeWidth+5;
    }
};

function createRaphaelCanvas(elementId, config) {
    return Raphael(elementId, config.canvasWidth, config.canvasHeight());
}

function getChmEndsPerc(chms, total) {
    var ends = [0];
    for (var i=1; i<chms.length; i++) {
        ends.push(ends[i-1]+chms[i]/total);
    }
    return ends;
}

/**
 * storing chromesome length info
 */
function ChmInfo() {
    this.hg19 = [0,249250621,243199373,198022430,191154276,180915260,171115067,159138663,146364022,141213431,135534747,135006516,133851895,115169878,107349540,102531392,90354753,81195210,78077248,59128983,63025520,48129895,51304566,155270560];
    this.total = 3036303846;
    this.perc = getChmEndsPerc(this.hg19,this.total);
}
ChmInfo.prototype = {
    loc2perc : function(chm,loc) {
        return this.perc[chm-1] + loc/this.total;
    },
    loc2xpixil : function(chm,loc,goConfig) {
        return this.loc2perc(chm,loc)*goConfig.GenomeWidth+goConfig.wideLeftText;
    },
    perc2loc : function(xPerc,startChm) {
        var chm;
        if (!startChm) {//binary search
            var low = 1, high = this.hg19.length-1, i;
            while (low <= high) {
                i = Math.floor((low + high) / 2); 
                if (this.perc[i] >= xPerc)  {high = i - 1;}
                else  {low = i + 1;}
            }
            chm = low;
        } else {//linear search
            var i;
            for (i=startChm; i<this.hg19.length; i++) {
                if (xPerc<=this.perc[i]) break;
            }
            chm = i;
        }
        var loc = Math.round(this.total*(xPerc-this.perc[chm-1]));
        return [chm,loc];
    },
    xpixil2loc : function(goConfig,x,startChm) {
        var xPerc = (x-goConfig.wideLeftText)/goConfig.GenomeWidth;
        return this.perc2loc(xPerc,startChm);
    },
    middle : function(chm, goConfig) {
        var loc = this.hg19[chm]/2;
        return this.loc2xpixil(chm,loc,goConfig);
    },
    chmName : function(chm) {
        if (chm == 23) {
            return "X/Y";
        }
        return chm;
    }
};

function plotChromosomes(p,config,chmInfo) {
    var yRuler = config.rowMargin+config.ticHeight;
    drawLine(config.wideLeftText,yRuler,config.wideLeftText+config.GenomeWidth,yRuler,p,'#000',1);
    // ticks & texts
    for (var i=1; i<chmInfo.hg19.length; i++) {
        var xt = chmInfo.loc2xpixil(i,0,config);
        drawLine(xt,yRuler,xt,config.rowMargin,p,'#000',1);
        
        var m = chmInfo.middle(i,config);
        p.text(m,yRuler-config.rowMargin,chmInfo.chmName(i));
    }
    drawLine(config.wideLeftText+config.GenomeWidth,yRuler,config.wideLeftText+config.GenomeWidth,config.rowMargin,p,'#000',1);
}

function drawLine(x1, y1, x2, y2, p, cl, width) {
    var path = "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
    var line = p.path(path);
    line.attr("stroke", cl);
    line.attr("stroke-width", width);
    line.attr("opacity", 0.5);
    line.translate(0.5, 0.5);
    return line;
}

function plotMuts(p,config,chmInfo,row,muts,chrCol,startCol,endCol,idCol,hasCna) {
    var pixelMap = [];
    for (var i=0; i<muts.length; i++) {
        var loc = extractLoc(muts[i],chrCol,startCol,endCol);
        if (loc==null||loc[0]>=chmInfo.hg19.length) continue;
        var x = Math.round(chmInfo.loc2xpixil(loc[0],(loc[1]+loc[2])/2,config));
        var xBin = x-x%config.pixelsPerBinMut;
        if (pixelMap[xBin]==null)
            pixelMap[xBin] = [];
        pixelMap[xBin].push(muts[i][idCol]);
    }
    
    var maxCount = 0;
    for (var i in pixelMap) {
        var arr = pixelMap[i];
        if (arr && arr.length>maxCount)
            maxCount=arr.length;
    }
    
    var yRow = config.yRow(row)+config.rowHeight;
    for (var i in pixelMap) {
        var arr = pixelMap[i];
        var pixil = parseInt(i);
        if (arr) {
            var h = config.rowHeight*arr.length/maxCount;
            var r = p.rect(pixil,yRow-h,config.pixelsPerBinMut,h);
            r.attr("fill","#0f0");
            r.attr("stroke", "#0f0");
            r.attr("stroke-width", 1);
            r.attr("opacity", 0.5);
            r.translate(0.5, 0.5);
            var locStart = chmInfo.xpixil2loc(config,pixil);
            var locEnd = chmInfo.xpixil2loc(config,pixil+config.pixelsPerBinMut,locStart[0]);
            
            var mutGeneAA = getMutGeneAA(arr.slice(0,10));
            var tip = mutGeneAA.join('<br/>')
                +'<br/><a href="#" onclick="goTip.tipDiv.mouseleave();filterMutationsTableByIds(\''
                +idRegEx(arr)+'\');switchToTab(\'mutations\');return false;">'
                +(arr.length<=10?'show details':('show all '+arr.length+' mutations'))+'</a>';
                        //+"<br/>from "+loc2string(locStart,chmInfo)+"<br/>to "+loc2string(locEnd,chmInfo);
            goTip.addTip(r.node, tip);
        }
    }
        
    p.text(0,yRow-config.rowHeight/2,'MUT').attr({'text-anchor': 'start'});
    var t = p.text(config.xRightText(),yRow-config.rowHeight/2,muts.length).attr({'text-anchor': 'start','font-weight': 'bold'});
    underlineText(t,p);
    goTip.addTip(t.node, "Number of mutation events."
        +(!hasCna?"":" <a href='#' onclick='openMutCnaScatterDialog();return false;'>Context Plot</a>"));
}

function loc2string(loc,chmInfo) {
    return "chr"+chmInfo.chmName(loc[0])+":"+addCommas(loc[1]);
}

function addCommas(x)
{
    var strX = x.toString();
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(strX)) {
            strX = strX.replace(rgx, '$1' + ',' + '$2');
    }
    return strX;
}

function plotCnSegs(p,config,chmInfo,row,segs,chrCol,startCol,endCol,segCol,hasMut) {
    var yRow = config.yRow(row);
    var genomeMeasured = 0;
    var genomeAltered = 0;
    for (var i=0; i<segs.length; i++) {
        var loc = extractLoc(segs[i],chrCol,startCol,endCol);
        if (loc==null||loc[0]>=chmInfo.hg19.length) continue;
        var chm = loc[0];
        var start = loc[1];
        var end = loc[2];
        var segMean = segs[i][segCol];
        genomeMeasured += end-start;
        if (Math.abs(segMean)<config.cnTh[0]) continue;
        if (end-start<config.cnLengthTh) continue; //filter cnv
        genomeAltered += end-start;
        var x1 = chmInfo.loc2xpixil(chm,start,config);
        var x2 = chmInfo.loc2xpixil(chm,end,config);
        var r = p.rect(x1,yRow,x2-x1,config.rowHeight);
        var cl = config.getCnColor(segMean);
        r.attr("fill",cl);
        r.attr("stroke", cl);
        r.attr("stroke-width", 1);
        r.attr("opacity", 0.5);
        r.translate(0.5, 0.5);
        var tip = "Mean copy number log2 value: "+segMean+"<br/>from "+loc2string([chm,start],chmInfo)+"<br/>to "+loc2string([chm,end],chmInfo);
        goTip.addTip(r.node, tip);
    }
    p.text(0,yRow+config.rowHeight/2,'CNA').attr({'text-anchor': 'start'});
    
    var label = genomeMeasured==0 ? 'N/A' : (100*genomeAltered/genomeMeasured).toFixed(1)+'%';
    var tip = genomeMeasured==0 ? 'Copy number segment data not available' : 
                ("Percentage of copy-number altered chromosome regions (mean copy number log vaule >0.2 or <-0.2) out of measured regions."
                    +(!hasMut?"":" <a href='#' onclick='openMutCnaScatterDialog();return false;'>Context Plot</a>"));
    
    var t = p.text(config.xRightText(),yRow+config.rowHeight/2,label).attr({'text-anchor': 'start','font-weight': 'bold'});
    underlineText(t,p);
    goTip.addTip(t.node, tip);
}

function underlineText(textElement,p) {
    var textBBox = textElement.getBBox();
    return p.path("M"+textBBox.x+" "+(textBBox.y+textBBox.height)+"L"+(textBBox.x+textBBox.width)+" "+(textBBox.y+textBBox.height));  
}

function extractLoc(data,chrCol,startCol,endCol) {
    var chm = (data[chrCol]=='X'||data[chrCol]=='Y'||data[chrCol]=='x'||data[chrCol]=='y') ? 23 : parseInt(data[1]);
    if (isNaN(chm) || chm < 1 || chm > 23) {
        return null;
    }
    return [chm,data[startCol],data[endCol]];
}

// support for tooltips
// see: http://jsfiddle.net/QK7hw/403/
function genomicOverviewTip() {
    this.tipDiv = null;
    this.node = null;
    this.overTip = false;
}
genomicOverviewTip.prototype = {
    setTipDiv: function(tipDiv) {
        this.tipDiv = tipDiv;
        var tipObj = this;
        tipDiv.mouseenter(function(e){
            tipObj.overTip = true;            
        }).mouseleave(function(){
            tipObj.overTip = false;
            tipObj.tipDiv.fadeOut(200);
        });
    },
    setTipDivLoc: function(x,y) {
        var l = x<800 ? (x+10) : (x-this.tipDiv.outerWidth()-10);
        this.tipDiv.offset({left:l,top:(y+10)});
    },
    addTip: function (node, txt) {
        var tipObj = this;
        $(node).mouseenter(function(e){
            tipObj.node = node;
            setTimeout(function(){
                if (node==tipObj.node) {
                    tipObj.tipDiv.fadeIn();
                    tipObj.tipDiv.html(txt);
                    tipObj.setTipDivLoc(e.clientX+pageXOffset,e.clientY+pageYOffset);
                }
            },100);
            
        }).mouseleave(function(){
            tipObj.node = null;
            setTimeout(function(){
                if (!tipObj.overTip) {
                    tipObj.tipDiv.fadeOut(200);
                }
            },400);
            
        });
    }
};

var goTip = new genomicOverviewTip();

$(document).ready(function(){
    goTip.setTipDiv($("#genomic-overview-tip").hide());
});