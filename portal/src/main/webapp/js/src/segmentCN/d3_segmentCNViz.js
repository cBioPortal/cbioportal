/**
* D3SegmentCNViz
* author: Linhong Chen
* date: Augst 2, 2016
* @constructor
*
*/

D3SegmentCNViz = function(_data, _num){
    this.data=_data;
    this.num =_num;
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
    for(j=0; j<this.num; j++){

        this.svg[j] = d3.select("#d3_segment").append("svg")
                    .attr("width", this.width+ this.margin.left + this.margin.right)
                    .attr("height", this.height + this.margin.top + this.margin.bottom);
   
        this.g[j] = this.svg[j].append("g")
                  .attr("transform", "translate("+ this.margin.left+"," + this.margin.top+")");
            
        this.rect[j] = this.g[j].selectAll("rect")
                    .data(this.data)
                    .enter()
                    .append("rect"); 
        var barwidth = 4;
        var samplePadding = 1;
        
        this.rect[j].attr("width", this.width)
                .attr("height", barwidth)
                .attr("x", this.min)
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
D3SegmentCNViz.prototype.update =function(date){ 

    var barwidth = 4;
    var samplePadding = 1;
    
    //update rect
    this.rect[0] =this.g[0].selectAll("rect")
                .data(data);
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

    



