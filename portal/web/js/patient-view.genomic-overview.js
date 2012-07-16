function GenomicOverviewConfig(nRows) {
    this.nRows = nRows;
    this.GenomeWidth = 1140;
    this.xGenome = 60;
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
    canvasWidth: function() {
        return this.xGenome+this.GenomeWidth+5;
    },
    canvasHeight: function() {
        return 2*this.rowMargin+this.ticHeight+this.nRows*(this.rowHeight+this.rowMargin);
    },
    yRow: function(row) {
        return 2*this.rowMargin+this.ticHeight+row*(this.rowHeight+this.rowMargin);
    }
};

function createRaphaelCanvas(elementId, config) {
    return Raphael(elementId, config.canvasWidth(), config.canvasHeight());
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
    loc2scale : function(chm,loc,goConfig) {
        return this.loc2perc(chm,loc)*goConfig.GenomeWidth+goConfig.xGenome;
    },
    middle : function(chm, goConfig) {
        var loc = this.hg19[chm]/2;
        return this.loc2scale(chm,loc,goConfig);
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
    drawLine(config.xGenome,yRuler,config.xGenome+config.GenomeWidth,yRuler,p,'#000',1);
    // ticks & texts
    for (var i=1; i<chmInfo.hg19.length; i++) {
        var xt = chmInfo.loc2scale(i,0,config);
        drawLine(xt,yRuler,xt,config.rowMargin,p,'#000',1);
        
        var m = chmInfo.middle(i,config);
        p.text(m,yRuler-config.rowMargin,chmInfo.chmName(i));
    }
    drawLine(config.xGenome+config.GenomeWidth,yRuler,config.xGenome+config.GenomeWidth,config.rowMargin,p,'#000',1);
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

function plotMuts(p,config,chmInfo,row,muts,chrCol,startCol,endCol) {
    var pixelMap = [];
    for (var i=0; i<muts.length; i++) {
        var loc = extractLoc(muts[i],chrCol,[startCol,endCol]);
        if (loc==null||loc[0]>chmInfo.hg19.length) continue;
        var x = Math.round(chmInfo.loc2scale(loc[0],(loc[1]+loc[2])/2,config));
        if (pixelMap[x]==null)
            pixelMap[x] = [];
        pixelMap[x].push(i);
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
        if (arr) {
            var l = drawLine(i,yRow,i,yRow-config.rowHeight*arr.length/maxCount,p,'#0f0',3);
            goTip.addTip(l.node, arr.length+" mutations");
        }
    }
    
    p.text(0,yRow-config.rowHeight/2,'MUT').attr({'text-anchor': 'start'});
    p.text(config.xGenome-5,yRow-config.rowHeight/2,muts.length).attr({'text-anchor': 'end'});
}

function plotCnSegs(p,config,chmInfo,row,segs,chrCol,startCol,endCol,segCol) {
    var yRow = config.yRow(row);
    var genomeMeasured = 0;
    var genomeAltered = 0;
    for (var i=0; i<segs.length; i++) {
        var loc = extractLoc(segs[i],chrCol,[startCol,endCol,segCol]);
        if (loc==null||loc[0]>chmInfo.hg19.length) continue;
        var chm = loc[0];
        var start = loc[1];
        var end = loc[2];
        var segMean = loc[3];
        genomeMeasured += end-start;
        if (Math.abs(segMean)<config.cnTh[0]) continue;
        if (end-start<config.cnLengthTh) continue; //filter cnv
        genomeAltered += end-start;
        var x1 = chmInfo.loc2scale(chm,start,config);
        var x2 = chmInfo.loc2scale(chm,end,config);
        var r = p.rect(x1,yRow,x2-x1,config.rowHeight);
        var cl = config.getCnColor(segMean);
        r.attr("fill",cl);
        r.attr("stroke", cl);
        r.attr("stroke-width", 1);
        r.attr("opacity", 0.5);
        r.translate(0.5, 0.5);
    }
    p.text(0,yRow+config.rowHeight/2,'CNA').attr({'text-anchor': 'start'});
    p.text(config.xGenome-5,yRow+config.rowHeight/2,(100*genomeAltered/genomeMeasured).toFixed(1)+'%').attr({'text-anchor': 'end'});
}

function extractLoc(data,chrCol,cols) {
    var chm = (data[chrCol]=='X'||data[chrCol]=='Y'||data[chrCol]=='x'||data[chrCol]=='y') ? 23 : parseInt(data[1]);
    if (isNaN(chm) || chm < 1 || chm > 23) {
        return null;
    }
    var ret = [chm];
    for (var i=0; i<cols.length; i++) {
        ret.push(data[cols[i]]);
    }
    return ret;
}

// support for tooltips
// see: http://jsfiddle.net/QK7hw/403/
function genomicOverviewTip() {
    this.tipDiv = null;
    this.node = null;
}
genomicOverviewTip.prototype = {
    setTipDiv: function(tipDiv) {
        this.tipDiv = tipDiv;
    },
    setTipDivLoc: function(x,y) {
        this.tipDiv.offset({left:x,top:y});
    },
    addTip: function (node, txt) {
        var tipObj = this;
        $(node).mouseenter(function(e){
            tipObj.node = node;
            setTimeout(function(){
                if (node==tipObj.node) {
                    tipObj.tipDiv.fadeIn();
                    tipObj.setTipDivLoc(e.clientX+pageXOffset+10,e.clientY+pageYOffset+10);
                    tipObj.tipDiv.text(txt);
                }
            },500);
            
        }).mouseleave(function(){
            tipObj.tipDiv.fadeOut(200);
            tipObj.node = null;
        });
    }
};

var goTip = new genomicOverviewTip();

$(document).ready(function(){
    goTip.setTipDiv($("#genomic-overview-tip").hide());

});