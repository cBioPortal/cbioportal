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
    this.vizWidth = ($("#d3_segment").width()-40)/this.num;
    this.margin = {top: 20, bottom: 20, left:20, right: 20};
    this.width = this.vizWidth - this.margin.left - this.margin.right;
    this.height = 3400 - this.margin.top - this.margin.bottom;      
    this.yScale = d3.scale.ordinal().rangeRoundBands([0, this.height], .8, 0);
    this.min=0;
       
    this.initViz();
}

D3SegmentCNViz.prototype.initViz = function(){    
    this.svg=[];
    this.rect=[];
    this.g =[];
    this.genebpStart=[];
    this.genebpEnd=[];
                        
    for(j=0; j<this.num; j++){

        this.genebpStart[j] = geneMapping[this.genes[j]].bpStart; 
        this.genebpEnd[j] = geneMapping[this.genes[j]].bpEnd; 
        var xScale = d3.scale.linear()
                           .domain([this.genebpStart[j], this.genebpEnd[j]])
                           .range([0,this.width]);
        var barwidth = 2;
        var samplePadding = 1;

        this.svg[j] = d3.select("#d3_segment").append("svg")
                    .attr("width", this.width+ this.margin.left + this.margin.right)
                    .attr("height", this.height + this.margin.top + this.margin.bottom)
                    .attr("class", "segmentSvg");
        this.g[j] = this.svg[j].append("g")
                  .attr("transform", "translate("+ this.margin.left+"," + this.margin.top+")");
        
        var vizData = this.data['"'+this.genes[j]+'"']; 
        this.rect[j] = this.g[j].selectAll("rect")
                    .data(vizData)
                    .enter()
                    .append("rect"); 

        this.rect[j]
                .attr("width", function(d){
                    return (xScale(d.end)-xScale(d.start));
                })
                .attr("height", barwidth)
                .attr("x", function(d){
                    return xScale(d.start);
                })
                .attr("y", function(d,i) {
                    return (barwidth + samplePadding)*i; 
                })
                .attr("fill", function(d) {
                    if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*100))+","+ (255-Math.round(d.value*100))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*100))+","+(255+Math.round(d.value*100))+"," + 255 + ")"; 
                    }                           
                }); 
    } 
}          


//function for updating barchart but also maintaining an original sorted/unsorted status
D3SegmentCNViz.prototype.update =function(data, button){ 

    var barwidth = 2;
    var samplePadding = 1;
    var index=this.genes.indexOf(button);
    var xScale = d3.scale.linear()
                           .domain([this.genebpStart[index], this.genebpEnd[index]])
                           .range([0,this.width]);
    //update rect
    this.rect[index] =this.g[index].selectAll("rect")
                .data(data);
    this.rect[index].enter()
                .append("rect");
    this.rect[index]
            .attr("width", function(d){
                    return (xScale(d.end)-xScale(d.start));
                }) 
              .attr("height", barwidth) 
              .attr("x", function(d){
                    return xScale(d.start);
                })
              .attr("y", function(d,i) { return (barwidth + samplePadding)*i;})
              .attr("fill", function(d) {
                  if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*100))+","+ (255-Math.round(d.value*100))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*100))+","+(255+Math.round(d.value*100))+"," + 255 + ")"; 
                    }                                 
                });   
            this.rect[index].exit().remove();  
}

