function GenomicOverviewConfig() {
    this.width = 1200;
    this.rows = 1;
    this.rowHeight = 20;
    this.rowMargin = 5;
    this.yRuler = this.rows*(this.rowHeight+this.rowMargin);
    this.ticHeight = 10;
    this.canvasWidth = this.width + 5;
    this.canvasHeight = this.yRuler+this.ticHeight+this.rowMargin;
    this.cnTh = [0.1,1.5];
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
    }
};

function createRaphaelCanvas(elementId, config) {
    return Raphael(elementId, config.canvasWidth, config.canvasHeight);
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
    loc2scale : function(chm,loc,width) {
        return this.loc2perc(chm,loc) * width;
    },
    middle : function(chm, width) {
        var loc = this.hg19[chm]/2;
        return this.loc2scale(chm,loc,width);
    },
    chmName : function(chm) {
        if (chm == 23) {
            return "X/Y";
        }
        if (chm == 24) {
            return "M";
        }
        return chm;
    }
};

function plotChromosomes(p,config,chmInfo) {
    drawLine(0,config.yRuler,config.width,config.yRuler,p);
    // ticks & texts
    for (var i=1; i<chmInfo.hg19.length; i++) {
        var xt = chmInfo.loc2scale(i,0,config.width);
        drawLine(xt,config.yRuler,xt,config.yRuler+config.ticHeight,p);
        
        var m = chmInfo.middle(i,config.width);
        p.text(m,config.yRuler+config.ticHeight,chmInfo.chmName(i));
    }
    drawLine(config.width,config.yRuler,config.width,config.yRuler+config.ticHeight,p);

}

function drawLine(x1, y1, x2, y2, p) {
    var path = "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
    var line = p.path(path);
    line.attr("stroke", "#000");
    line.attr("stroke-width", 1);
    line.attr("opacity", 0.5);
    line.translate(0.5, 0.5);
}

function plotCnSeg(p,config,seg,chmInfo) {
    var chm = seg[1];
    var segMean = seg[5];
    if (Math.abs(segMean)>config.cnTh[0]&&chm<=chmInfo.hg19.length) {
        var start = seg[2];
        var end = seg[3];
        var x = chmInfo.loc2scale(chm,start,config.width);
        var w = chmInfo.loc2scale(1,end-start,config.width);
        var r = p.rect(x,0,w,config.rowHeight);
        r.attr("stroke-width",0);
        r.attr("fill",config.getCnColor(segMean));
    }
}