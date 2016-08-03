/**
* D3SegmentCNViz
* author: Linhong Chen
* date: Augst 2, 2016
* @constructor
*
*/

D3SegmentCNViz = function(_data, _num){
 this.data=_data;
 this.num =_num
 this.vizWidth = $("#d3_segment").width()/this.num;
 this.initViz();
}

D3SegmentCNViz.prototype.initViz = function(){

    var margin = {top: 50, bottom: 50, left:2, right: 2};
    var width = this.vizWidth - margin.left - margin.right;
    var height = 2500 - margin.top - margin.bottom;
    var samplePadding = 1;
    var genePadding = 4;
    var xScale = d3.scale.linear().range([0, width]);
    var yScale = d3.scale.ordinal().rangeRoundBands([0, height], .8, 0);
 
    var svg=[];
    for(i=0; i<this.num; i++){

        svg[i] = d3.select("#d3_segment").append("svg")
                    .attr("width", width+margin.left+margin.right)
                    .attr("height", height+margin.top+margin.bottom)
                    .attr("class", "col-lg-4 col-sm-4 col-md-4");
   
        var g = svg[i].append("g")
                  .attr("transform", "translate("+margin.left+","+margin.top+")");
        var min=0;
        var barwidth =5;
    
        var rect = g.selectAll("rect")
                    .data(this.data)
                    .enter()
                    .append("rect"); 
        rect.attr("width", width/this.num-genePadding)
                .attr("height", barwidth)
                .attr("x", min)
                .attr("y", function(d,i) { return (barwidth+samplePadding)*i; })
                .attr("fill", function(d) {

                    if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*50))+","+ (255-Math.round(d.value*50))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*50))+","+(255+Math.round(d.value*50))+"," + 255 + ")"; 
                    }                        
                }); 
    } 
}          
    //sorting bar chart                
    d3.selectAll('input[name="sort"]').on("click", function(){ 
        //maintain an original aggregated/unaggregated and sorted/unsorted status
        update(refined_data);
    });

//function for updating barchart but also maintaining an original sorted/unsorted status
D3SegmentCNViz.prototype.update =function(date){ 
        //check whether need to sort data
        sortBars(data);

        //update rect
        var rect = g.selectAll("rect")
                .data(data);
            rect.enter()
                .append("rect");
            rect.attr("width", width/geneNumber-genePadding) 
              .attr("height", barwidth) 
              .attr("x", min)
              .attr("y", function(d,i) { return (barwidth+samplePadding)*i;})
              .attr("fill", function(d) {
                    if (d.value>0) {
                        return "rgb("+ 255 + ","+ (255-Math.round(d.value*50))+","+ (255-Math.round(d.value*50))+")";
                    } else{
                        return "rgb("+(255+Math.round(d.value*50))+","+(255+Math.round(d.value*50))+"," + 255 + ")"; 
                    }                        
                });   
            rect.exit().remove();  
}

    



