var sbgnBendPointUtilities = {
  currentCtxEdge: undefined,
  currentCtxPos: undefined,
  currentBendIndex: undefined,
  //Get the direction of the line from source point to the target point
  getLineDirection: function(srcPoint, tgtPoint){
    if(srcPoint.y == tgtPoint.y && srcPoint.x < tgtPoint.x){
      return 1;
    }
    if(srcPoint.y < tgtPoint.y && srcPoint.x < tgtPoint.x){
      return 2;
    }
    if(srcPoint.y < tgtPoint.y && srcPoint.x == tgtPoint.x){
      return 3;
    }
    if(srcPoint.y < tgtPoint.y && srcPoint.x > tgtPoint.x){
      return 4;
    }
    if(srcPoint.y == tgtPoint.y && srcPoint.x > tgtPoint.x){
      return 5;
    }
    if(srcPoint.y > tgtPoint.y && srcPoint.x > tgtPoint.x){
      return 6;
    }
    if(srcPoint.y > tgtPoint.y && srcPoint.x == tgtPoint.x){
      return 7;
    }
    return 8;//if srcPoint.y > tgtPoint.y and srcPoint.x < tgtPoint.x
  },
  //Get the clipping point of the node if it has an edge between another node centered on (x, y) point
  getClippingPoint: function (node, x, y, portid) {
    var intersect;//The return value of intersectLine function
    var shape = node.css('shape');//get the node shape
    
    if(shape === 'polygon'){
        var points = node._private.style['shape-polygon-points'].value;
        shape = window.cyNodeShapes.makePolygon( points ).name;
    }
   
    //Determine the parameters according to the possible number of parameters
    if (window.cyNodeShapes[shape].intersectLine.length == 4) {
      intersect = window.cyNodeShapes[shape].intersectLine(node, x, y, portid);
    }
    else {
      intersect = window.cyNodeShapes[shape].intersectLine(
          node._private.position.x,
          node._private.position.y,
          node.width(),
          node.height(),
          x, //halfPointX,
          y, //halfPointY
          node._private.style["border-width"].pfValue / 2);
    }
    
    return {
      x: intersect[0],
      y: intersect[1]
    };
  },
  getClippingPointsAndTangents: function (edge) {
    var sourceNode = edge.source();
    var targetNode = edge.target();
    
    var tgtPosition = cytoscape.sbgn.addPortReplacementIfAny(targetNode, edge._private.data.porttarget);
    var srcPosition = cytoscape.sbgn.addPortReplacementIfAny(sourceNode, edge._private.data.portsource);
    
    var srcClippingPoint = this.getClippingPoint(sourceNode, tgtPosition.x, tgtPosition.y, edge._private.data.portsource);
    var tgtClippingPoint = this.getClippingPoint(targetNode, srcPosition.x, srcPosition.y, edge._private.data.porttarget);
    
//    var srcClippingPoint = {
//      x: edge._private.rscratch.startX,
//      y: edge._private.rscratch.startY
//    };
//    
//    var tgtClippingPoint = {
//      x: edge._private.rscratch.endX,
//      y: edge._private.rscratch.endY
//    };

    var m1 = (tgtClippingPoint.y - srcClippingPoint.y) / (tgtClippingPoint.x - srcClippingPoint.x);
    var m2 = -1 / m1;

    return {
      m1: m1,
      m2: m2,
      srcClippingPoint: srcClippingPoint,
      tgtClippingPoint: tgtClippingPoint
    };
  },
  getIntersection: function(edge, point, clippingPointsAndTangents){
    if (clippingPointsAndTangents === undefined) {
      clippingPointsAndTangents = this.getClippingPointsAndTangents(edge);
    }

    var srcClippingPoint = clippingPointsAndTangents.srcClippingPoint;
    var tgtClippingPoint = clippingPointsAndTangents.tgtClippingPoint;
    var m1 = clippingPointsAndTangents.m1;
    var m2 = clippingPointsAndTangents.m2;

    var intersectX;
    var intersectY;

    if(m1 == Infinity || m1 == -Infinity){
      intersectX = srcClippingPoint.x;
      intersectY = point.y;
    }
    else if(m1 == 0){
      intersectX = point.x;
      intersectY = srcClippingPoint.y;
    }
    else {
      var a1 = srcClippingPoint.y - m1 * srcClippingPoint.x;
      var a2 = point.y - m2 * point.x;

      intersectX = (a2 - a1) / (m1 - m2);
      intersectY = m1 * intersectX + a1;
    }

    //Intersection point is the intersection of the lines passing through the nodes and
    //passing through the bend point and perpendicular to the other line
    var intersectionPoint = {
      x: intersectX,
      y: intersectY
    };
    
    return intersectionPoint;
  },
  convertToRelativeBendPosition: function (edge, bendPoint, clippingPointsAndTangents) {
    if (clippingPointsAndTangents === undefined) {
      clippingPointsAndTangents = this.getClippingPointsAndTangents(edge);
    }
    
    var intersectionPoint = this.getIntersection(edge, bendPoint, clippingPointsAndTangents);
    var intersectX = intersectionPoint.x;
    var intersectY = intersectionPoint.y;
    
    var srcClippingPoint = clippingPointsAndTangents.srcClippingPoint;
    var tgtClippingPoint = clippingPointsAndTangents.tgtClippingPoint;
    
    var weight = intersectX == srcClippingPoint.x?0:(intersectX - srcClippingPoint.x) / (tgtClippingPoint.x - srcClippingPoint.x);
    var distance = Math.sqrt(Math.pow((intersectY - bendPoint.y), 2)
        + Math.pow((intersectX - bendPoint.x), 2));
    
    //Get the direction of the line form source clipping point to target clipping point
    var direction1 = this.getLineDirection(srcClippingPoint, tgtClippingPoint);
    //Get the direction of the line from intesection point to bend point
    var direction2 = this.getLineDirection(intersectionPoint, bendPoint);
    
    //If the difference is not -2 and not 6 then the direction of the distance is negative
    if(direction1 - direction2 != -2 && direction1 - direction2 != 6){
      distance = -distance;
    }
    
    return {
      weight: weight,
      distance: distance
    };
  },
  convertToRelativeBendPositions: function (edge) {
    var clippingPointsAndTangents = this.getClippingPointsAndTangents(edge);
    var bendPoints = edge.data('bendPointPositions');
    //output variables
    var weights = [];
    var distances = [];

    for (var i = 0; i < bendPoints.length; i++) {
      var bendPoint = bendPoints[i];
      var relativeBendPosition = this.convertToRelativeBendPosition(edge, bendPoint, clippingPointsAndTangents);

      weights.push(relativeBendPosition.weight);
      distances.push(relativeBendPosition.distance);
    }
//    weights = [0.2, 0.9];
//    distances = [0, 0];
    return {
      weights: weights,
      distances: distances
    };
  },
  getSegmentDistancesString: function (edge) {
    var str = "";

    var distances = edge.data('distances');
    for (var i = 0; i < distances.length; i++) {
      str = str + " " + distances[i];
    }
    
    return str;
  },
  getSegmentWeightsString: function (edge) {
    var str = "";

    var weights = edge.data('weights');
    for (var i = 0; i < weights.length; i++) {
      str = str + " " + weights[i];
    }
    
    return str;
  },
  addBendPoint: function(edge, newBendPoint) {
    if(edge === undefined || newBendPoint === undefined){
      edge = this.currentCtxEdge;
      newBendPoint = this.currentCtxPos;
    }
    
    var relativeBendPosition = this.convertToRelativeBendPosition(edge, newBendPoint);
    var originalPointWeight = relativeBendPosition.weight;
    
    var edgeStartX = edge._private.rscratch.startX;
    var edgeStartY = edge._private.rscratch.startY;
    var edgeEndX = edge._private.rscratch.endX;
    var edgeEndY = edge._private.rscratch.endY;
    
    var startWeight = this.convertToRelativeBendPosition(edge, {x: edgeStartX, y: edgeStartY}).weight;
    var endWeight = this.convertToRelativeBendPosition(edge, {x: edgeEndX, y: edgeEndY}).weight;
    var weightsWithTgtSrc = [startWeight].concat(edge.data('weights')?edge.data('weights'):[]).concat([endWeight]);
    
    var minDist = Infinity;
    var intersection;
    var segptsWithTgtSrc = [edgeStartX, edgeStartY]
            .concat(edge._private.rscratch.segpts?edge._private.rscratch.segpts:[])
            .concat([edgeEndX, edgeEndY]);
    var newBendIndex = -1;
    
    for(var i = 0; i < weightsWithTgtSrc.length - 1; i++){
      var w1 = weightsWithTgtSrc[i];
      var w2 = weightsWithTgtSrc[i + 1];
      
      //check if the weight is between w1 and w2
      if((originalPointWeight <= w1 && originalPointWeight >= w2) || (originalPointWeight <= w2 && originalPointWeight >= w1)){
        var startX = segptsWithTgtSrc[2 * i];
        var startY = segptsWithTgtSrc[2 * i + 1];
        var endX = segptsWithTgtSrc[2 * i + 2];
        var endY = segptsWithTgtSrc[2 * i + 3];
        
        var start = {
          x: startX,
          y: startY
        };
        
        var end = {
          x: endX,
          y: endY
        };
        
        var m1 = ( startY - endY ) / ( startX - endX );
        var m2 = -1 / m1;
        
        var clippingPointAndTangents = {
          srcClippingPoint: start,
          tgtClippingPoint: end,
          m1: m1,
          m2: m2
        };
        
        //get the intersection of the current segment with the new bend point
        var currentIntersection = this.getIntersection(edge, newBendPoint, clippingPointAndTangents);
        var dist = Math.sqrt( Math.pow( (newBendPoint.x - currentIntersection.x), 2 ) 
                + Math.pow( (newBendPoint.y - currentIntersection.y), 2 ));
        
        //Update the minimum distance
        if(dist < minDist){
          minDist = dist;
          intersection = currentIntersection;
          newBendIndex = i;
        }
      }
    }
    
    if(intersection !== undefined){
      newBendPoint = intersection;
    }
    
    relativeBendPosition = this.convertToRelativeBendPosition(edge, newBendPoint);
    
    if(intersection === undefined){
      relativeBendPosition.distance = 0;
    }

    var weights = edge.data('weights');
    var distances = edge.data('distances');
    
    weights = weights?weights:[];
    distances = distances?distances:[];
    
//    weights.push(relativeBendPosition.weight);
//    distances.push(relativeBendPosition.distance);
    if(newBendIndex != -1){
      console.log(weights);
      console.log(newBendIndex);
      weights.splice(newBendIndex, 0, relativeBendPosition.weight);
      distances.splice(newBendIndex, 0, relativeBendPosition.distance);
      console.log(weights);
    }
    
    edge.data('weights', weights);
    edge.data('distances', distances);
    edge.css('curve-style', 'segments');
    
    return relativeBendPosition;
  },
  removeBendPoint: function(edge, bendPointIndex){
    if(edge === undefined || bendPointIndex === undefined){
      edge = this.currentCtxEdge;
      bendPointIndex = this.currentBendIndex;
    }
    
    var distances = edge.data('distances');
    var weights = edge.data('weights');
    
    distances.splice(bendPointIndex, 1);
    weights.splice(bendPointIndex, 1);
    
    if(distances.length == 0 || weights.lenght == 0){
      edge.removeData('distances');
      edge.removeData('weights');
      edge.css('curve-style', 'bezier');
    }
    else {
      edge.data('distances', distances);
      edge.data('weights', weights);
    }
  }
};