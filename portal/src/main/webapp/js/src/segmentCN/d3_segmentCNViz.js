/**
* D3SegmentCNViz
* author: Linhong Chen
* date: Augst 2, 2016
* @constructor
*
*/

D3SegmentCNViz = function(_data, _genes){
    this.data=_data; 
    this.genes = _genes;
    this.num =_genes.length;
    this.vizWidth = $("#d3_segment").width()/this.num;
    this.margin = {top: 50, bottom: 50, left:10, right: 10};
    this.width = this.vizWidth - this.margin.left - this.margin.right;
    this.height = 5700 - this.margin.top - this.margin.bottom;    
    this.xScale = d3.scale.linear().range([0, this.width]);
    this.yScale = d3.scale.ordinal().rangeRoundBands([0, this.height], .8, 0);
    this.min=0;
       
    this.initViz();
}

D3SegmentCNViz.prototype.initViz = function(){    
    this.svg=[];
    this.rect=[];
    this.g =[];

    var geneMapping={
                    "KRAS": {
                             "chr": 12,
                            "bpStart": 25204789,
                            "bpEnd": 25252093
                            },
                    "NRAS": {
                            "chr": 1,
                            "bpStart": 115247084,
                            "bpEnd": 115259515 
                            },
                    "BRAF": {"chr": 7,
                            "bpStart": 140433812,
                            "bpEnd": 140624564
                            }
                    };
    
                        
    for(j=0; j<this.num; j++){
        var genebpStart = geneMapping[this.genes[j]].bpStart; 
        console.log(genebpStart);
        
        this.svg[j] = d3.select("#d3_segment").append("svg")
                    .attr("width", this.width+ this.margin.left + this.margin.right)
                    .attr("height", this.height + this.margin.top + this.margin.bottom);
   
        this.g[j] = this.svg[j].append("g")
                  .attr("transform", "translate("+ this.margin.left+"," + this.margin.top+")");
        console.log("this.data[this.genes[j]]"); 

        console.log(this.data['"'+this.genes[j]+'"']);    
        this.rect[j] = this.g[j].selectAll("rect")
                    .data(this.data['"'+this.genes[j]+'"'])
                    .enter()
                    .append("rect"); 
        var barwidth = 4;
        var samplePadding = 1;
        
        this.rect[j].attr("width", function(d){
                    return (d.end-d.start);
                })
                .attr("height", barwidth)
                .attr("x", function(d){
                    return d.start-genebpStart;
                })
                .attr("y", function(d,i) {
                    return (barwidth + samplePadding)*i; 
                })
                .attr("fill", function(d) {
                    if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*50))+","+ (255-Math.round(d.value*50))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*50))+","+(255+Math.round(d.value*50))+"," + 255 + ")"; 
                    }                           
                }); 
    } 
}          


//function for updating barchart but also maintaining an original sorted/unsorted status
D3SegmentCNViz.prototype.update =function(data){ 

    var barwidth = 4;
    var samplePadding = 1;
    
    //update rect
    this.rect[0] =this.g[0].selectAll("rect")
                .data(data['KRAS']);
    this.rect[0].enter()
                .append("rect");
    this.rect[0].attr("width", this.width) 
              .attr("height", barwidth) 
              .attr("x", this.min)
              .attr("y", function(d,i) { return (barwidth + samplePadding)*i;})
              .attr("fill", function(d) {
                  if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*50))+","+ (255-Math.round(d.value*50))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*50))+","+(255+Math.round(d.value*50))+"," + 255 + ")"; 
                    }                                 
                });   
            this.rect[0].exit().remove();  
}

    



