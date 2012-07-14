function GenomicOverviewConfig(nMut, nCna) {
    this.nMut = nMut;
    this.nCna = nCna;
    this.width = 1200;
    this.mutHeight = 6;
    this.cnaHeight = 20;
    this.rowMargin = 5;
    this.ticHeight = 10;
    this.yCurr = this.rowMargin;
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
        return this.width + 5;
    },
    canvasHeight: function() {
        return this.yCurr+this.ticHeight+this.nMut*this.mutHeight+this.nCna*this.cnaHeight+(this.nMut+this.nCna)*this.rowMargin;
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
        return chm;
    }
};

function plotChromosomes(p,config,chmInfo) {
    var yRuler = config.yCurr+config.ticHeight;
    drawLine(0,yRuler,config.width,yRuler,p);
    // ticks & texts
    for (var i=1; i<chmInfo.hg19.length; i++) {
        var xt = chmInfo.loc2scale(i,0,config.width);
        drawLine(xt,yRuler,xt,config.yCurr,p);
        
        var m = chmInfo.middle(i,config.width);
        p.text(m,yRuler-config.rowMargin,chmInfo.chmName(i));
    }
    drawLine(config.width,yRuler,config.width,config.yCurr,p);
    config.yCurr = yRuler+config.rowMargin;
}

function drawLine(x1, y1, x2, y2, p) {
    var path = "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
    var line = p.path(path);
    line.attr("stroke", "#000");
    line.attr("stroke-width", 1);
    line.attr("opacity", 0.5);
    line.translate(0.5, 0.5);
}

function plotMuts(p,config,chmInfo,muts,chrCol,startCol,endCol) {
    for (var i=0; i<muts.length; i++) {
        var loc = extractLoc(muts[i],chrCol,[startCol,endCol]);
        if (loc==null) continue;
        var x = chmInfo.loc2scale(loc[0],(loc[1]+loc[2])/2,config.width);
        var y = config.yCurr + config.mutHeight/2;
        var c = p.circle(x,y,config.mutHeight/2);
        c.attr("fill","green");
        c.attr("stroke-width", 0)
    }
    config.yCurr += config.mutHeight + config.rowMargin;
}

function plotCnSegs(p,config,chmInfo,segs,chrCol,startCol,endCol,segCol) {
    for (var i=0; i<segs.length; i++) {
        var loc = extractLoc(segs[i],chrCol,[startCol,endCol,segCol]);
        if (loc==null) continue;
        var chm = loc[0];
        if (1 <= chm && chm <= 23) {
            var start = loc[1];
            var end = loc[2];
            var segMean = loc[3];
            if (Math.abs(segMean)<config.cnTh[0]||chm>chmInfo.hg19.length)
                continue;
            if (end-start<config.cnLengthTh) //filter cnv
                continue;
            var x = chmInfo.loc2scale(chm,start,config.width);
            var w = chmInfo.loc2scale(1,end-start,config.width);
            var r = p.rect(x,config.yCurr,w,config.cnaHeight);
            var cl = config.getCnColor(segMean);
            r.attr("fill",cl);
            r.attr("stroke", cl);
            r.attr("stroke-width", 1);
            r.attr("opacity", 0.5);
            r.translate(0.5, 0.5);
        }
    }
    config.yCurr += config.cnaHeight + config.rowMargin;
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