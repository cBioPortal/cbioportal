
function createRaphaelCanvas(elementId, width, height) {
    return Raphael(elementId, width, height);
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
    this.hg19 = [0,249250621,243199373,198022430,191154276,180915260,171115067,159138663,146364022,141213431,135534747,135006516,133851895,115169878,107349540,102531392,90354753,81195210,78077248,59128983,63025520,48129895,51304566];
    this.total = 2881033286;
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
    }
};
var chmInfo = new ChmInfo();

function plotChromosomes(p, width) {
    var mg = 10;
    drawLine(mg,20,width-mg,20,p);
    var chmLen = width-mg;
    // ticks & texts
    for (var i=1; i<chmInfo.hg19.length; i++) {
        var x = mg+chmInfo.loc2scale(i,0,chmLen);
        drawLine(x,0,x,20,p);
        
        var m = mg+chmInfo.middle(i,chmLen);
        p.text(m,15,i);
    }
    drawLine(width-mg,0,width-mg,20,p);

}

function drawLine(x1, y1, x2, y2, p) {
    var path = "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
    var line = p.path(path);
    line.attr("stroke", "#000000");
    line.attr("stroke-width", "1");
    line.attr("opacity", 0.5);
    line.translate(0.5, 0.5);
}