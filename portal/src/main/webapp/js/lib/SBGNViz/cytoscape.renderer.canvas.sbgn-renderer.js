/*
 * Copyright 2014 Memorial-Sloan Kettering Cancer Center.
 *
 * This file is part of PCViz.
 *
 * PCViz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCViz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PCViz. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * author : Mecit Sari
 */

/*
 * Some of the core functions of cytoscape.js must be overrided so that we can access the data
 * associated with the node's itself.
 */
;
(function ($$) {
  "use strict";

  /*
   * Those are the sbgn shapes, we need this map to override cytoscape.js core functions.
   * Compartment is also another sbgn shape but we did not include it since we just use
   * roundrectangle shape for it.
   */
  var sbgnShapes = {'unspecified entity': true, 'simple chemical': true, 'macromolecule': true,
    'nucleic acid feature': true, 'perturbing agent': true, 'source and sink': true,
    'complex': true, 'process': true, 'omitted process': true, 'uncertain process': true,
    'association': true, 'dissociation': true, 'phenotype': true,
    'tag': true, 'consumption': true, 'production': true, 'modulation': true,
    'stimulation': true, 'catalysis': true, 'inhibition': true, 'necessary stimulation': true,
    'logic arc': true, 'equivalence arc': true, 'and operator': true,
    'or operator': true, 'not operator': true, 'and': true, 'or': true, 'not': true,
    'nucleic acid feature multimer': true, 'macromolecule multimer': true,
    'simple chemical multimer': true, 'complex multimer': true};

  var CanvasRenderer = $$('renderer', 'canvas');
  var renderer = CanvasRenderer.prototype;
  var lineStyles = $$.style.types.lineStyle.enums;
  lineStyles.push("consumption", "production");
  
  //added padding-relative property to css features
  $$.style.properties.push({name: 'padding-relative', type: $$.style.types.percent});
  $$.style.properties['padding-relative'] = {name: 'padding-relative', type: $$.style.types.percent};

  function drawSelection(render, context, node) {
    //TODO: do it for all classes in sbgn, create a sbgn class array to check
    if (sbgnShapes[render.getNodeShape(node)]) {
      CanvasRenderer.nodeShapes[render.getNodeShape(node)].draw(
              context,
              node); //node._private.data.weight / 5.0
    }
    else {
      CanvasRenderer.nodeShapes[render.getNodeShape(node)].draw(
              context,
              node._private.position.x,
              node._private.position.y,
              render.getNodeWidth(node),
              render.getNodeHeight(node)); //node._private.data.weight / 5.0
    }
  }
  ;

  function strokeSelection(render, context, node) {
    if (!sbgnShapes[render.getNodeShape(node)]) {
      context.stroke();
    }
  }
  ;

  function drawPathSelection(render, context, node) {
    //TODO: do it for all classes in sbgn, create a sbgn class array to check
    if (sbgnShapes[render.getNodeShape(node)]) {
      CanvasRenderer.nodeShapes[render.getNodeShape(node)].drawPath(
              context,
              node); //node._private.data.weight / 5.0
    }
    else {
      CanvasRenderer.nodeShapes[render.getNodeShape(node)].drawPath(
              context,
              node._private.position.x,
              node._private.position.y,
              render.getNodeWidth(node),
              render.getNodeHeight(node)); //node._private.data.weight / 5.0
    }
  }
  ;

  function intersectLineSelection(render, node, x, y, portId) {
    //TODO: do it for all classes in sbgn, create a sbgn class array to check
    if (sbgnShapes[render.getNodeShape(node)]) {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].intersectLine(
              node, x, y, portId);
    }
    else {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].intersectLine(
              node._private.position.x,
              node._private.position.y,
              render.getNodeWidth(node),
              render.getNodeHeight(node),
              x, //halfPointX,
              y, //halfPointY
              node._private.style["border-width"].pxValue / 2
              );
    }
  }
  ;

  function checkPointSelection(render, node, x, y, nodeThreshold) {
    //TODO: do it for all classes in sbgn, create a sbgn class array to check
    if (sbgnShapes[render.getNodeShape(node)]) {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].checkPoint(x, y,
              node,
              nodeThreshold);
    }
    else {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].checkPoint(x, y,
              node._private.style["border-width"].pxValue / 2,
              render.getNodeWidth(node) + nodeThreshold,
              render.getNodeHeight(node) + nodeThreshold,
              node._private.position.x,
              node._private.position.y);
    }
  }
  ;

  function intersectBoxSelection(render, x1, y1, x2, y2, node) {
    //TODO: do it for all classes in sbgn, create a sbgn class array to check
    if (sbgnShapes[render.getNodeShape(node)]) {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].intersectBox(x1, y1, x2, y2, node);
    }
    else {
      return CanvasRenderer.nodeShapes[render.getNodeShape(node)].intersectBox(x1, y1, x2, y2,
              render.getNodeWidth(node),
              render.getNodeHeight(node),
              node._private.position.x,
              node._private.position.y,
              node._private.style["border-width"].pxValue / 2);
    }
  }
  ;

  CanvasRenderer.prototype.drawPie = function (context, node) {
    node = node[0]; // ensure ele ref

    if (!this.hasPie(node)) {
      return;
    } // exit early if not needed

    var nodeW = this.getNodeWidth(node);
    var nodeH = this.getNodeHeight(node);
    var x = node._private.position.x;
    var y = node._private.position.y;
    var radius = Math.min(nodeW, nodeH) / 2; // must fit in node
    var lastPercent = 0; // what % to continue drawing pie slices from on [0, 1]

    context.save();

    // clip to the node shape
    drawPathSelection(this, context, node);

    context.clip();

    for (var i = 1; i <= $$.style.pieBackgroundN; i++) { // 1..N
      var size = node._private.style['pie-' + i + '-background-size'].value;
      var color = node._private.style['pie-' + i + '-background-color'];
      var percent = size / 100; // map integer range [0, 100] to [0, 1]
      var angleStart = 1.5 * Math.PI + 2 * Math.PI * lastPercent; // start at 12 o'clock and go clockwise
      var angleDelta = 2 * Math.PI * percent;
      var angleEnd = angleStart + angleDelta;

      // slice start and end points
      var sx1 = x + radius * Math.cos(angleStart);
      var sy1 = y + radius * Math.sin(angleStart);

      // ignore if
      // - zero size
      // - we're already beyond the full circle
      // - adding the current slice would go beyond the full circle
      if (size === 0 || lastPercent >= 1 || lastPercent + percent > 1) {
        continue;
      }

      context.beginPath();
      context.moveTo(x, y);
      context.arc(x, y, radius, angleStart, angleEnd);
      context.closePath();

      context.fillStyle = 'rgb('
              + color.value[0] + ','
              + color.value[1] + ','
              + color.value[2] + ')'
              ;

      context.fill();

      lastPercent += percent;
    }

    context.restore();
  };


  CanvasRenderer.prototype.drawInscribedImage = function (context, img, node) {
    var r = this;
//		console.log(this.data);
    var zoom = this.data.cy._private.zoom;

    var nodeX = node._private.position.x;
    var nodeY = node._private.position.y;

    //var nodeWidth = node._private.style["width"].value;
    //var nodeHeight = node._private.style["height"].value;
    var nodeWidth = this.getNodeWidth(node);
    var nodeHeight = this.getNodeHeight(node);

    context.save();

    drawPathSelection(this, context, node);

    context.clip();

//		context.setTransform(1, 0, 0, 1, 0, 0);

    var imgDim = [img.width, img.height];
    context.drawImage(img,
            nodeX - imgDim[0] / 2,
            nodeY - imgDim[1] / 2,
            imgDim[0],
            imgDim[1]);

    context.restore();

    if (node._private.style["border-width"].value > 0) {
      context.stroke();
    }

  };

  CanvasRenderer.prototype.getAllInBox = function (x1, y1, x2, y2) {
    var data = this.data;
    var nodes = this.getCachedNodes();
    var edges = this.getCachedEdges();
    var box = [];

    var x1c = Math.min(x1, x2);
    var x2c = Math.max(x1, x2);
    var y1c = Math.min(y1, y2);
    var y2c = Math.max(y1, y2);
    x1 = x1c;
    x2 = x2c;
    y1 = y1c;
    y2 = y2c;
    var heur;

    for (var i = 0; i < nodes.length; i++) {
      if (intersectBoxSelection(this, x1, y1, x2, y2, nodes[i])) {
        box.push(nodes[i]);
      }
    }

    for (var i = 0; i < edges.length; i++) {
      if (edges[i]._private.rscratch.edgeType == "self") {
        if ((heur = $$.math.boxInBezierVicinity(x1, y1, x2, y2,
                edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                edges[i]._private.rscratch.cp2ax, edges[i]._private.rscratch.cp2ay,
                edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))
                &&
                (heur == 2 || (heur == 1 && $$.math.checkBezierInBox(x1, y1, x2, y2,
                        edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                        edges[i]._private.rscratch.cp2ax, edges[i]._private.rscratch.cp2ay,
                        edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value)))
                ||
                (heur = $$.math.boxInBezierVicinity(x1, y1, x2, y2,
                        edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                        edges[i]._private.rscratch.cp2cx, edges[i]._private.rscratch.cp2cy,
                        edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))
                &&
                (heur == 2 || (heur == 1 && $$.math.checkBezierInBox(x1, y1, x2, y2,
                        edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                        edges[i]._private.rscratch.cp2cx, edges[i]._private.rscratch.cp2cy,
                        edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value)))
                )
        {
          box.push(edges[i]);
        }
      }

      if (edges[i]._private.rscratch.edgeType == "bezier" &&
              (heur = $$.math.boxInBezierVicinity(x1, y1, x2, y2,
                      edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                      edges[i]._private.rscratch.cp2x, edges[i]._private.rscratch.cp2y,
                      edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))
              &&
              (heur == 2 || (heur == 1 && $$.math.checkBezierInBox(x1, y1, x2, y2,
                      edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                      edges[i]._private.rscratch.cp2x, edges[i]._private.rscratch.cp2y,
                      edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))))
      {
        box.push(edges[i]);
      }

      if (edges[i]._private.rscratch.edgeType == "straight" &&
              (heur = $$.math.boxInBezierVicinity(x1, y1, x2, y2,
                      edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                      edges[i]._private.rscratch.startX * 0.5 + edges[i]._private.rscratch.endX * 0.5,
                      edges[i]._private.rscratch.startY * 0.5 + edges[i]._private.rscratch.endY * 0.5,
                      edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))
              &&
              (heur == 2 || (heur == 1 && $$.math.checkStraightEdgeInBox(x1, y1, x2, y2,
                      edges[i]._private.rscratch.startX, edges[i]._private.rscratch.startY,
                      edges[i]._private.rscratch.endX, edges[i]._private.rscratch.endY, edges[i]._private.style["width"].value))))
      {
        box.push(edges[i]);
      }

    }

    return box;
  };

  // Find nearest element
  CanvasRenderer.prototype.findNearestElement = function (x, y, visibleElementsOnly) {
    var data = this.data;
    var nodes = this.getCachedNodes();
    var edges = this.getCachedEdges();
    var near = [];
    var isTouch = CanvasRenderer.isTouch;

    var zoom = this.data.cy.zoom();
    var edgeThreshold = (isTouch ? 256 : 32) / zoom;
    var nodeThreshold = (isTouch ? 16 : 0) / zoom;

    // Check nodes
    for (var i = 0; i < nodes.length; i++) {

      if (checkPointSelection(this, nodes[i], x, y, nodeThreshold)) {

        if (visibleElementsOnly) {
          if (nodes[i]._private.style["opacity"].value != 0
                  && nodes[i]._private.style["visibility"].value == "visible"
                  && nodes[i]._private.style["display"].value == "element") {

            near.push(nodes[i]);
          }
        } else {
          near.push(nodes[i]);
        }
      }
    }

    // Check edges
    var addCurrentEdge;
    for (var i = 0; i < edges.length; i++) {
      var edge = edges[i];
      var rs = edge._private.rscratch;

      addCurrentEdge = false;

      if (rs.edgeType == "self") {
        if (($$.math.inBezierVicinity(x, y,
                rs.startX,
                rs.startY,
                rs.cp2ax,
                rs.cp2ay,
                rs.selfEdgeMidX,
                rs.selfEdgeMidY,
                Math.pow(edge._private.style["width"].value / 2, 2))
                &&
                (Math.pow(edges[i]._private.style["width"].value / 2, 2) + edgeThreshold >
                        $$.math.sqDistanceToQuadraticBezier(x, y,
                                rs.startX,
                                rs.startY,
                                rs.cp2ax,
                                rs.cp2ay,
                                rs.selfEdgeMidX,
                                rs.selfEdgeMidY)))
                ||
                ($$.math.inBezierVicinity(x, y,
                        rs.selfEdgeMidX,
                        rs.selfEdgeMidY,
                        rs.cp2cx,
                        rs.cp2cy,
                        rs.endX,
                        rs.endY,
                        Math.pow(edges[i]._private.style["width"].value / 2, 2))
                        &&
                        (Math.pow(edges[i]._private.style["width"].value / 2, 2) + edgeThreshold >
                                $$.math.sqDistanceToQuadraticBezier(x, y,
                                        rs.selfEdgeMidX,
                                        rs.selfEdgeMidY,
                                        rs.cp2cx,
                                        rs.cp2cy,
                                        rs.endX,
                                        rs.endY))))
        {
          addCurrentEdge = true;
        }

      } else if (rs.edgeType == "straight") {
        if ($$.math.inLineVicinity(x, y, rs.startX, rs.startY, rs.endX, rs.endY, edges[i]._private.style["width"].value * 2)
                &&
                Math.pow(edges[i]._private.style["width"].value / 2, 2) + edgeThreshold >
                $$.math.sqDistanceToFiniteLine(x, y,
                        rs.startX,
                        rs.startY,
                        rs.endX,
                        rs.endY))
        {
          addCurrentEdge = true;
        }

      } else if (rs.edgeType == "bezier") {
        if ($$.math.inBezierVicinity(x, y,
                rs.startX,
                rs.startY,
                rs.cp2x,
                rs.cp2y,
                rs.endX,
                rs.endY,
                Math.pow(edges[i]._private.style["width"].value / 2, 2))
                &&
                (Math.pow(edges[i]._private.style["width"].value / 2, 2) + edgeThreshold >
                        $$.math.sqDistanceToQuadraticBezier(x, y,
                                rs.startX,
                                rs.startY,
                                rs.cp2x,
                                rs.cp2y,
                                rs.endX,
                                rs.endY)))
        {
          addCurrentEdge = true;
        }
      }

      if (!near.length || near[near.length - 1] != edges[i]) {
        if ((CanvasRenderer.arrowShapes[edges[i]._private.style["source-arrow-shape"].value].roughCollide(x, y,
                edges[i]._private.rscratch.arrowStartX, edges[i]._private.rscratch.arrowStartY,
                this.getArrowWidth(edges[i]._private.style["width"].value),
                this.getArrowHeight(edges[i]._private.style["width"].value),
                [edges[i]._private.rscratch.arrowStartX - edges[i].source()[0]._private.position.x,
                  edges[i]._private.rscratch.arrowStartY - edges[i].source()[0]._private.position.y], 0)
                &&
                CanvasRenderer.arrowShapes[edges[i]._private.style["source-arrow-shape"].value].collide(x, y,
                edges[i]._private.rscratch.arrowStartX, edges[i]._private.rscratch.arrowStartY,
                this.getArrowWidth(edges[i]._private.style["width"].value),
                this.getArrowHeight(edges[i]._private.style["width"].value),
                [edges[i]._private.rscratch.arrowStartX - edges[i].source()[0]._private.position.x,
                  edges[i]._private.rscratch.arrowStartY - edges[i].source()[0]._private.position.y], 0))
                ||
                (CanvasRenderer.arrowShapes[edges[i]._private.style["target-arrow-shape"].value].roughCollide(x, y,
                        edges[i]._private.rscratch.arrowEndX, edges[i]._private.rscratch.arrowEndY,
                        this.getArrowWidth(edges[i]._private.style["width"].value),
                        this.getArrowHeight(edges[i]._private.style["width"].value),
                        [edges[i]._private.rscratch.arrowEndX - edges[i].target()[0]._private.position.x,
                          edges[i]._private.rscratch.arrowEndY - edges[i].target()[0]._private.position.y], 0)
                        &&
                        CanvasRenderer.arrowShapes[edges[i]._private.style["target-arrow-shape"].value].collide(x, y,
                        edges[i]._private.rscratch.arrowEndX, edges[i]._private.rscratch.arrowEndY,
                        this.getArrowWidth(edges[i]._private.style["width"].value),
                        this.getArrowHeight(edges[i]._private.style["width"].value),
                        [edges[i]._private.rscratch.arrowEndX - edges[i].target()[0]._private.position.x,
                          edges[i]._private.rscratch.arrowEndY - edges[i].target()[0]._private.position.y], 0)))
        {
          addCurrentEdge = true;
        }
      }

      if (addCurrentEdge) {
        if (visibleElementsOnly) {
          // For edges, make sure the edge is visible/has nonzero opacity,
          // then also make sure both source and target nodes are visible/have
          // nonzero opacity
          var source = data.cy.getElementById(edges[i]._private.data.source)
          var target = data.cy.getElementById(edges[i]._private.data.target)

          if (edges[i]._private.style["opacity"].value != 0
                  && edges[i]._private.style["visibility"].value == "visible"
                  && edges[i]._private.style["display"].value == "element"
                  && source._private.style["opacity"].value != 0
                  && source._private.style["visibility"].value == "visible"
                  && source._private.style["display"].value == "element"
                  && target._private.style["opacity"].value != 0
                  && target._private.style["visibility"].value == "visible"
                  && target._private.style["display"].value == "element") {

            near.push(edges[i]);
          }
        } else {
          near.push(edges[i]);
        }
      }
    }

    near.sort(this.zOrderSort);

    if (near.length > 0) {
      return near[ near.length - 1 ];
    } else {
      return null;
    }
  };

  function addPortReplacementIfAny(node, edgePort) {
    var posX = node.position().x;
    var posY = node.position().y;
    if (typeof node._private.data.ports != 'undefined') {
      for (var i = 0; i < node._private.data.ports.length; i++) {
        var port = node._private.data.ports[i];
        if (port.id == edgePort) {
          posX = posX + port.x;
          posY = posY + port.y;
          break;
        }
      }
    }
    return {'x': posX, 'y': posY};
  }
  ;

  CanvasRenderer.prototype.drawArrowheads = function (context, edge, drawOverlayInstead) {
    if (drawOverlayInstead) {
      return;
    } // don't do anything for overlays 

    var rs = edge._private.rscratch;
    var self = this;
    var isHaystack = rs.edgeType === 'haystack';

    // Displacement gives direction for arrowhead orientation
    var dispX, dispY;
    var startX, startY, endX, endY;

    var srcPos = edge.source().position();
    var tgtPos = edge.target().position();

    if (isHaystack) {
      startX = rs.haystackPts[0];
      startY = rs.haystackPts[1];
      endX = rs.haystackPts[2];
      endY = rs.haystackPts[3];
    } else {
      startX = rs.arrowStartX;
      startY = rs.arrowStartY;
      endX = rs.arrowEndX;
      endY = rs.arrowEndY;
    }

    var style = edge._private.style;

    function drawArrowhead(prefix, x, y, dispX, dispY) {
      var arrowShape = style[prefix + '-arrow-shape'].value;

      if (arrowShape === 'none') {
        return;
      }

      var gco = context.globalCompositeOperation;

      var arrowClearFill = style[prefix + '-arrow-fill'].value === 'hollow' ? 'both' : 'filled';
      var arrowFill = style[prefix + '-arrow-fill'].value;

      if (arrowShape === 'half-triangle-overshot') {
        arrowFill = 'hollow';
        arrowClearFill = 'hollow';
      }

      if (style.opacity.value !== 1 || arrowFill === 'hollow') { // then extra clear is needed
        context.globalCompositeOperation = 'destination-out';

        self.fillStyle(context, 255, 255, 255, 1);
        self.strokeStyle(context, 255, 255, 255, 1);

        self.drawArrowShape(edge, prefix, context,
                arrowClearFill, style['width'].pxValue, style[prefix + '-arrow-shape'].value,
                x, y, dispX, dispY
                );

        context.globalCompositeOperation = gco;
      } // otherwise, the opaque arrow clears it for free :)

      var color = style[prefix + '-arrow-color'].value;
      self.fillStyle(context, color[0], color[1], color[2], style.opacity.value);
      self.strokeStyle(context, color[0], color[1], color[2], style.opacity.value);

      self.drawArrowShape(edge, prefix, context,
              arrowFill, style['width'].pxValue, style[prefix + '-arrow-shape'].value,
              x, y, dispX, dispY
              );
    }

    dispX = startX - srcPos.x;
    dispY = startY - srcPos.y;

    if (!isHaystack && !isNaN(startX) && !isNaN(startY) && !isNaN(dispX) && !isNaN(dispY)) {
      drawArrowhead('source', startX, startY, dispX, dispY);

    } else {
      // window.badArrow = true;
      // debugger;
    }

    var midX = rs.midX;
    var midY = rs.midY;

    if (isHaystack) {
      midX = (startX + endX) / 2;
      midY = (startY + endY) / 2;
    }

    dispX = startX - endX;
    dispY = startY - endY;

    if (rs.edgeType === 'self') {
      dispX = 1;
      dispY = -1;
    }

    if (!isNaN(midX) && !isNaN(midY)) {
      drawArrowhead('mid-target', midX, midY, dispX, dispY);
    }

    dispX *= -1;
    dispY *= -1;

    if (!isNaN(midX) && !isNaN(midY)) {
      drawArrowhead('mid-source', midX, midY, dispX, dispY);
    }

    dispX = endX - tgtPos.x;
    dispY = endY - tgtPos.y;

    if (!isHaystack && !isNaN(endX) && !isNaN(endY) && !isNaN(dispX) && !isNaN(dispY)) {
      drawArrowhead('target', endX, endY, dispX, dispY);
    }
  };


  CanvasRenderer.prototype.findEndpoints = function (edge) {
    var intersect;

    var source = edge.source()[0];
    var target = edge.target()[0];

    var srcPos = source._private.position;
    var tgtPos = target._private.position;

    var tgtArShape = edge._private.style["target-arrow-shape"].value;
    var srcArShape = edge._private.style["source-arrow-shape"].value;

    var tgtBorderW = target._private.style["border-width"].pxValue;
    var srcBorderW = source._private.style["border-width"].pxValue;

    var rs = edge._private.rscratch;

    if (edge._private.rscratch.edgeType == "self") {

      var cp = [rs.cp2cx, rs.cp2cy];

      intersect = intersectLineSelection(this, target, cp[0], cp[1],
              edge._private.data.porttarget);

      var arrowEnd = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[tgtArShape].spacing(edge));
      var edgeEnd = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[tgtArShape].gap(edge));

      rs.endX = edgeEnd[0];
      rs.endY = edgeEnd[1];

      rs.arrowEndX = arrowEnd[0];
      rs.arrowEndY = arrowEnd[1];

      var cp = [rs.cp2ax, rs.cp2ay];

      intersect = intersectLineSelection(this, source, cp[0], cp[1],
              edge._private.data.portsource);

      var arrowStart = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[srcArShape].spacing(edge));
      var edgeStart = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[srcArShape].gap(edge));

      rs.startX = edgeStart[0];
      rs.startY = edgeStart[1];


      rs.arrowStartX = arrowStart[0];
      rs.arrowStartY = arrowStart[1];

    } else if (rs.edgeType == "straight") {
      var sourcePos = addPortReplacementIfAny(source, edge._private.data.portsource);

      intersect = intersectLineSelection(this, target, sourcePos.x,
              sourcePos.y, edge._private.data.porttarget);

      if (intersect.length == 0) {
        rs.noArrowPlacement = true;
        //			return;
      } else {
        rs.noArrowPlacement = false;
      }

      var arrowEnd = $$.math.shortenIntersection(intersect,
              [sourcePos.x, sourcePos.y],
              CanvasRenderer.arrowShapes[tgtArShape].spacing(edge));
      var edgeEnd = $$.math.shortenIntersection(intersect,
              [sourcePos.x, sourcePos.y],
              CanvasRenderer.arrowShapes[tgtArShape].gap(edge));

      rs.endX = edgeEnd[0];
      rs.endY = edgeEnd[1];

      rs.arrowEndX = arrowEnd[0];
      rs.arrowEndY = arrowEnd[1];

      var targetPos = addPortReplacementIfAny(target, edge._private.data.porttarget);

      intersect = intersectLineSelection(this, source, targetPos.x,
              targetPos.y, edge._private.data.portsource);

      if (intersect.length == 0) {
        rs.noArrowPlacement = true;
        //			return;
      } else {
        rs.noArrowPlacement = false;
      }

      var arrowStart = $$.math.shortenIntersection(intersect,
              [targetPos.x, targetPos.y],
              CanvasRenderer.arrowShapes[srcArShape].spacing(edge));
      var edgeStart = $$.math.shortenIntersection(intersect,
              [targetPos.x, targetPos.y],
              CanvasRenderer.arrowShapes[srcArShape].gap(edge));

      rs.startX = edgeStart[0];
      rs.startY = edgeStart[1];

      rs.arrowStartX = arrowStart[0];
      rs.arrowStartY = arrowStart[1];

    } else if (rs.edgeType == "bezier") {
      // if( window.badArrow) debugger;

      var cp = [rs.cp2x, rs.cp2y];

      intersect = intersectLineSelection(this, target, cp[0], cp[1],
              edge._private.data.porttarget);

      var arrowEnd = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[tgtArShape].spacing(edge));
      var edgeEnd = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[tgtArShape].gap(edge));

      rs.endX = edgeEnd[0];
      rs.endY = edgeEnd[1];

      rs.arrowEndX = arrowEnd[0];
      rs.arrowEndY = arrowEnd[1];

      intersect = intersectLineSelection(this, source, cp[0], cp[1],
              edge._private.data.portsource);

      var arrowStart = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[srcArShape].spacing(edge)
              );
      var edgeStart = $$.math.shortenIntersection(intersect, cp,
              CanvasRenderer.arrowShapes[srcArShape].gap(edge)
              );

      rs.startX = edgeStart[0];
      rs.startY = edgeStart[1];

      rs.arrowStartX = arrowStart[0];
      rs.arrowStartY = arrowStart[1];

      // if( isNaN(rs.startX) || isNaN(rs.startY) ){
      // 	debugger;
      // }

    } else if (rs.isArcEdge) {
      return;
    }
  };

  // Draw node
  CanvasRenderer.prototype.drawNode = function (context, node, drawOverlayInstead) {

    var nodeWidth, nodeHeight;

    if (!node.visible()) {
      return;
    }

    var parentOpacity = node.effectiveOpacity();
    if (parentOpacity === 0) {
      return;
    }

    // context.fillStyle = "orange";
    // context.fillRect(node.position().x, node.position().y, 2, 2);

    nodeWidth = this.getNodeWidth(node);
    nodeHeight = this.getNodeHeight(node);

    context.lineWidth = node._private.style["border-width"].pxValue;

    if (drawOverlayInstead === undefined || !drawOverlayInstead) {

      // Node color & opacity
      context.fillStyle = "rgba("
              + node._private.style["background-color"].value[0] + ","
              + node._private.style["background-color"].value[1] + ","
              + node._private.style["background-color"].value[2] + ","
              + (node._private.style["background-opacity"].value
                      * node._private.style["opacity"].value * parentOpacity) + ")";

      // Node border color & opacity
      context.strokeStyle = "rgba("
              + node._private.style["border-color"].value[0] + ","
              + node._private.style["border-color"].value[1] + ","
              + node._private.style["border-color"].value[2] + ","
              + (node._private.style["border-opacity"].value * node._private.style["opacity"].value * parentOpacity) + ")";

      context.lineJoin = 'miter'; // so borders are square with the node shape

      //var image = this.getCachedImage("url");

      var url = node._private.style["background-image"].value[2] ||
              node._private.style["background-image"].value[1];

      if (url != undefined) {

        var r = this;
        var image = this.getCachedImage(url,
                function () {

//							console.log(e);
                  r.data.canvasNeedsRedraw[CanvasRenderer.NODE] = true;
                  r.data.canvasNeedsRedraw[CanvasRenderer.DRAG] = true;

                  // Replace Image object with Canvas to solve zooming too far
                  // into image graphical errors (Jan 10 2013)
                  r.swapCachedImage(url);

                  r.redraw();
                }
        );

        if (image.complete == false) {

          drawPathSelection(r, context, node);

          strokeSelection(r, context, node);
          context.fillStyle = "#555555";
          context.fill();

        } else {
          //context.clip
          this.drawInscribedImage(context, image, node);
        }

      } else {

        // Draw node
        drawSelection(this, context, node);
      }

      this.drawPie(context, node);

      // Border width, draw border
      if (node._private.style["border-width"].pxValue > 0) {
        drawPathSelection(this, context, node);

        strokeSelection(this, context, node);
      }

      // draw the overlay
    } else {

      var overlayPadding = node._private.style["overlay-padding"].pxValue;
      var overlayOpacity = node._private.style["overlay-opacity"].value;
      var overlayColor = node._private.style["overlay-color"].value;
      if (overlayOpacity > 0) {
        context.fillStyle = "rgba( " + overlayColor[0] + ", " + overlayColor[1] + ", " + overlayColor[2] + ", " + overlayOpacity + " )";

        CanvasRenderer.nodeShapes['roundrectangle'].draw(
                context,
                node._private.position.x,
                node._private.position.y,
                nodeWidth + overlayPadding * 2,
                nodeHeight + overlayPadding * 2
                );
      }
    }

  };

  CanvasRenderer.prototype.getNodeShape = function (node)
  {
    // TODO only allow rectangle for a compound node?
//		if (node._private.style["width"].value == "auto" ||
//		    node._private.style["height"].value == "auto")
//		{
//			return "rectangle";
//		}

    var shape = node._private.style["shape"].value;

    return shape;
  };

  // Find edge control points
  CanvasRenderer.prototype.findEdgeControlPoints = function (edges) {
    var hashTable = {};
    var cy = this.data.cy;
    var pairIds = [];
    var haystackEdges = [];

    // create a table of edge (src, tgt) => list of edges between them
    var pairId;
    for (var i = 0; i < edges.length; i++) {
      var edge = edges[i];
      var style = edge._private.style;

      // ignore edges who are not to be displayed
      // they shouldn't take up space
      if (style.display.value === 'none') {
        continue;
      }

      if (style['curve-style'].value === 'haystack') {
        haystackEdges.push(edge);
        continue;
      }

      var srcId = edge._private.data.source;
      var tgtId = edge._private.data.target;

      pairId = srcId > tgtId ?
              tgtId + '-' + srcId :
              srcId + '-' + tgtId;

      if (hashTable[pairId] == null) {
        hashTable[pairId] = [];
      }

      hashTable[pairId].push(edge);
      pairIds.push(pairId);
    }

    var src, tgt, srcPos, tgtPos, srcW, srcH, tgtW, tgtH, srcShape, tgtShape, srcBorder, tgtBorder;
    var vectorNormInverse;
    var badBezier;

    // for each pair (src, tgt), create the ctrl pts
    // Nested for loop is OK; total number of iterations for both loops = edgeCount  
    for (var p = 0; p < pairIds.length; p++) {
      pairId = pairIds[p];

      src = cy.getElementById(hashTable[pairId][0]._private.data.source);
      tgt = cy.getElementById(hashTable[pairId][0]._private.data.target);

      srcPos = src._private.position;
      tgtPos = tgt._private.position;

      srcW = this.getNodeWidth(src);
      srcH = this.getNodeHeight(src);

      tgtW = this.getNodeWidth(tgt);
      tgtH = this.getNodeHeight(tgt);

      srcShape = CanvasRenderer.nodeShapes[ this.getNodeShape(src) ];
      tgtShape = CanvasRenderer.nodeShapes[ this.getNodeShape(tgt) ];

      srcBorder = src._private.style['border-width'].pxValue;
      tgtBorder = tgt._private.style['border-width'].pxValue;

      badBezier = false;


      if (hashTable[pairId].length > 1 && src !== tgt) {

        // pt outside src shape to calc distance/displacement from src to tgt
        var srcOutside = intersectLineSelection(this, src, tgtPos.x, tgtPos.y);

        // pt outside tgt shape to calc distance/displacement from src to tgt
        var tgtOutside = intersectLineSelection(this, tgt, srcPos.x, srcPos.y);

        var midptSrcPts = {
          x1: srcOutside[0],
          x2: tgtOutside[0],
          y1: srcOutside[1],
          y2: tgtOutside[1]
        };

        var dy = (tgtOutside[1] - srcOutside[1]);
        var dx = (tgtOutside[0] - srcOutside[0]);
        var l = Math.sqrt(dx * dx + dy * dy);

        var vector = {
          x: dx,
          y: dy
        };

        var vectorNorm = {
          x: vector.x / l,
          y: vector.y / l
        };
        vectorNormInverse = {
          x: -vectorNorm.y,
          y: vectorNorm.x
        };

        // if src intersection is inside tgt or tgt intersection is inside src, then no ctrl pts to draw
        if (checkPointSelection(this, tgt, srcOutside[0], srcOutside[1], tgtBorder / 2) ||
                checkPointSelection(this, src, tgtOutside[0], tgtOutside[1], srcBorder / 2)
                ) {
          vectorNormInverse = {};
          badBezier = true;
        }

      }

      var edge;
      var rs;

      for (var i = 0; i < hashTable[pairId].length; i++) {
        edge = hashTable[pairId][i];
        rs = edge._private.rscratch;

        var tgtPos2 = addPortReplacementIfAny(edge.target()[0], edge._private.data.porttarget);
        var srcPos2 = addPortReplacementIfAny(edge.source()[0], edge._private.data.portsource);

        // pt outside src shape to calc distance/displacement from src to tgt
        var srcOutside = intersectLineSelection(this, src, tgtPos2.x, tgtPos2.y, edge._private.data.portsource);

        // pt outside tgt shape to calc distance/displacement from src to tgt
        var tgtOutside = intersectLineSelection(this, tgt, srcPos2.x, srcPos2.y, edge._private.data.porttarget);

        var edgeIndex1 = rs.lastEdgeIndex;
        var edgeIndex2 = i;

        var numEdges1 = rs.lastNumEdges;
        var numEdges2 = hashTable[pairId].length;

        var srcX1 = rs.lastSrcCtlPtX;
        var srcX2 = srcPos2.x;
        var srcY1 = rs.lastSrcCtlPtY;
        var srcY2 = srcPos2.y;
        var srcW1 = rs.lastSrcCtlPtW;
        var srcW2 = src.outerWidth();
        var srcH1 = rs.lastSrcCtlPtH;
        var srcH2 = src.outerHeight();

        var tgtX1 = rs.lastTgtCtlPtX;
        var tgtX2 = tgtPos2.x;
        var tgtY1 = rs.lastTgtCtlPtY;
        var tgtY2 = tgtPos2.y;
        var tgtW1 = rs.lastTgtCtlPtW;
        var tgtW2 = tgt.outerWidth();
        var tgtH1 = rs.lastTgtCtlPtH;
        var tgtH2 = tgt.outerHeight();

        if (badBezier) {
          rs.badBezier = true;
        } else {
          rs.badBezier = false;
        }

        if (srcX1 === srcX2 && srcY1 === srcY2 && srcW1 === srcW2 && srcH1 === srcH2
                && tgtX1 === tgtX2 && tgtY1 === tgtY2 && tgtW1 === tgtW2 && tgtH1 === tgtH2
                && edgeIndex1 === edgeIndex2 && numEdges1 === numEdges2) {
          // console.log('edge ctrl pt cache HIT')
          continue; // then the control points haven't changed and we can skip calculating them
        } else {
          rs.lastSrcCtlPtX = srcX2;
          rs.lastSrcCtlPtY = srcY2;
          rs.lastSrcCtlPtW = srcW2;
          rs.lastSrcCtlPtH = srcH2;
          rs.lastTgtCtlPtX = tgtX2;
          rs.lastTgtCtlPtY = tgtY2;
          rs.lastTgtCtlPtW = tgtW2;
          rs.lastTgtCtlPtH = tgtH2;
          rs.lastEdgeIndex = edgeIndex2;
          rs.lastNumEdges = numEdges2;
          // console.log('edge ctrl pt cache MISS')
        }

        var eStyle = edge._private.style;
        var stepSize = eStyle['control-point-step-size'].pxValue;
        var stepDist = eStyle['control-point-distance'] !== undefined ? eStyle['control-point-distance'].pxValue : undefined;
        var stepWeight = eStyle['control-point-weight'].value;

        // Self-edge
        if (src.id() == tgt.id()) {

          rs.edgeType = 'self';

          // New -- fix for large nodes
          rs.cp2ax = srcPos2.x;
          rs.cp2ay = srcPos2.y - (1 + Math.pow(srcH, 1.12) / 100) * stepSize * (i / 3 + 1);

          rs.cp2cx = srcPos2.x - (1 + Math.pow(srcW, 1.12) / 100) * stepSize * (i / 3 + 1);
          rs.cp2cy = srcPos2.y;

          rs.selfEdgeMidX = (rs.cp2ax + rs.cp2cx) / 2.0;
          rs.selfEdgeMidY = (rs.cp2ay + rs.cp2cy) / 2.0;

          // Straight edge
        } else if (hashTable[pairId].length % 2 == 1
                && i == Math.floor(hashTable[pairId].length / 2)) {

          rs.edgeType = 'straight';

          // Bezier edge
        } else {
          var normStepDist = (0.5 - hashTable[pairId].length / 2 + i) * stepSize;
          var manStepDist = stepDist !== undefined ? $$.math.signum(normStepDist) * stepDist : undefined;
          var distanceFromMidpoint = manStepDist !== undefined ? manStepDist : normStepDist;

          var adjustedMidpt = {
            x: midptSrcPts.x1 * (1 - stepWeight) + midptSrcPts.x2 * stepWeight,
            y: midptSrcPts.y1 * (1 - stepWeight) + midptSrcPts.y2 * stepWeight
          };

          rs.edgeType = 'bezier';

          rs.cp2x = adjustedMidpt.x + vectorNormInverse.x * distanceFromMidpoint;
          rs.cp2y = adjustedMidpt.y + vectorNormInverse.y * distanceFromMidpoint;

          // console.log(edge, midPointX, displacementX, distanceFromMidpoint);
        }

        // find endpts for edge
        this.findEndpoints(edge);

        var badStart = !$$.is.number(rs.startX) || !$$.is.number(rs.startY);
        var badAStart = !$$.is.number(rs.arrowStartX) || !$$.is.number(rs.arrowStartY);
        var badEnd = !$$.is.number(rs.endX) || !$$.is.number(rs.endY);
        var badAEnd = !$$.is.number(rs.arrowEndX) || !$$.is.number(rs.arrowEndY);

        var minCpADistFactor = 3;
        var arrowW = this.getArrowWidth(edge._private.style['width'].pxValue) * CanvasRenderer.arrowShapeHeight;
        var minCpADist = minCpADistFactor * arrowW;
        var startACpDist = $$.math.distance({x: rs.cp2x, y: rs.cp2y}, {x: rs.startX, y: rs.startY});
        var closeStartACp = startACpDist < minCpADist;
        var endACpDist = $$.math.distance({x: rs.cp2x, y: rs.cp2y}, {x: rs.endX, y: rs.endY});
        var closeEndACp = endACpDist < minCpADist;

        if (rs.edgeType === 'bezier') {
          var overlapping = false;

          if (badStart || badAStart || closeStartACp) {
            overlapping = true;

            // project control point along line from src centre to outside the src shape
            // (otherwise intersection will yield nothing)
            var cpD = {// delta
              x: rs.cp2x - srcPos2.x,
              y: rs.cp2y - srcPos2.y
            };
            var cpL = Math.sqrt(cpD.x * cpD.x + cpD.y * cpD.y); // length of line
            var cpM = {// normalised delta
              x: cpD.x / cpL,
              y: cpD.y / cpL
            };
            var radius = Math.max(srcW, srcH);
            var cpProj = {// *2 radius guarantees outside shape
              x: rs.cp2x + cpM.x * 2 * radius,
              y: rs.cp2y + cpM.y * 2 * radius
            };

            var srcCtrlPtIntn = intersectLineSelection(this, src, cpProj.x, cpProj.y, edge._private.data.portsource);


            if (closeStartACp) {
              rs.cp2x = rs.cp2x + cpM.x * (minCpADist - startACpDist);
              rs.cp2y = rs.cp2y + cpM.y * (minCpADist - startACpDist);
            } else {
              rs.cp2x = srcCtrlPtIntn[0] + cpM.x * minCpADist;
              rs.cp2y = srcCtrlPtIntn[1] + cpM.y * minCpADist;
            }
          }

          if (badEnd || badAEnd || closeEndACp) {
            overlapping = true;

            // project control point along line from tgt centre to outside the tgt shape
            // (otherwise intersection will yield nothing)
            var cpD = {// delta
              x: rs.cp2x - tgtPos2.x,
              y: rs.cp2y - tgtPos2.y
            };
            var cpL = Math.sqrt(cpD.x * cpD.x + cpD.y * cpD.y); // length of line
            var cpM = {// normalised delta
              x: cpD.x / cpL,
              y: cpD.y / cpL
            };
            var radius = Math.max(srcW, srcH);
            var cpProj = {// *2 radius guarantees outside shape
              x: rs.cp2x + cpM.x * 2 * radius,
              y: rs.cp2y + cpM.y * 2 * radius
            };

            var tgtCtrlPtIntn = intersectLineSelection(this, tgt, cpProj.x, cpProj.y, edge._private.data.porttarget);


            if (closeEndACp) {
              rs.cp2x = rs.cp2x + cpM.x * (minCpADist - endACpDist);
              rs.cp2y = rs.cp2y + cpM.y * (minCpADist - endACpDist);
            } else {
              rs.cp2x = tgtCtrlPtIntn[0] + cpM.x * minCpADist;
              rs.cp2y = tgtCtrlPtIntn[1] + cpM.y * minCpADist;
            }

          }

          if (overlapping) {
            // recalc endpts
            this.findEndpoints(edge);
          }
        }

        // project the edge into rstyle
        this.projectBezier(edge);

      }
    }

    for (var i = 0; i < haystackEdges.length; i++) {
      var edge = haystackEdges[i];
      var rscratch = edge._private.rscratch;
      var rFactor = 0.8;

      if (!rscratch.haystack) {
        var srcR = rFactor * 0.5;
        var angle = Math.random() * 2 * Math.PI;

        rscratch.source = {
          x: srcR * Math.cos(angle),
          y: srcR * Math.sin(angle)
        };

        var tgtR = rFactor * 0.5;
        var angle = Math.random() * 2 * Math.PI;

        rscratch.target = {
          x: tgtR * Math.cos(angle),
          y: tgtR * Math.sin(angle)
        };

        rscratch.edgeType = 'haystack';
        rscratch.haystack = true;
      }
    }

    return hashTable;
  };

  var _genPoints = function (pt, spacing, even) {

    var approxLen = Math.sqrt(Math.pow(pt[4] - pt[0], 2) + Math.pow(pt[5] - pt[1], 2));
    approxLen += Math.sqrt(Math.pow((pt[4] + pt[0]) / 2 - pt[2], 2) + Math.pow((pt[5] + pt[1]) / 2 - pt[3], 2));

    var pts = Math.ceil(approxLen / spacing);
    var pz;

    if (pts > 0) {
      pz = new Array(pts * 2);
    } else {
      return null;
    }

    for (var i = 0; i < pts; i++) {
      var cur = i / pts;
      pz[i * 2] = pt[0] * (1 - cur) * (1 - cur) + 2 * (pt[2]) * (1 - cur) * cur + pt[4] * (cur) * (cur);
      pz[i * 2 + 1] = pt[1] * (1 - cur) * (1 - cur) + 2 * (pt[3]) * (1 - cur) * cur + pt[5] * (cur) * (cur);
    }

    return pz;
  };

  var _genStraightLinePoints = function (pt, spacing, even) {

    var approxLen = Math.sqrt(Math.pow(pt[2] - pt[0], 2) + Math.pow(pt[3] - pt[1], 2));

    var pts = Math.ceil(approxLen / spacing);
    var pz;

    if (pts > 0) {
      pz = new Array(pts * 2);
    } else {
      return null;
    }

    var lineOffset = [pt[2] - pt[0], pt[3] - pt[1]];
    for (var i = 0; i < pts; i++) {
      var cur = i / pts;
      pz[i * 2] = lineOffset[0] * cur + pt[0];
      pz[i * 2 + 1] = lineOffset[1] * cur + pt[1];
    }

    return pz;
  };

  var qBezierLength = function (pts) {
    var x1, y1, x2, y2;

    x1 = pts[0] - 2 * pts[2] + pts[4];
    y1 = pts[1] - 2 * pts[3] + pts[5];
    x2 = 2 * pts[2] - 2 * pts[0];
    y2 = 2 * pts[3] - 2 * pts[1];

    var A = 4 * (x1 * x1 + y1 * y1);
    var B = 4 * (x1 * x2 + y1 * y2);
    var C = x2 * x2 + y2 * y2;

    var sabc = 2 * Math.sqrt(A + B + C);
    var A2 = Math.sqrt(A);
    var A32 = 2 * A * A2;
    var C2 = 2 * Math.sqrt(C);
    var BA = B / A2;

    return (A32 * sabc + A2 * B * (sabc - C2) +
            (4 * C * A - B * B) * Math.log((2 * A2 + BA + sabc) / (BA + C2))
            ) / (4 * A32);
  };

  var drawQuadraticLineCardinality = function (context, edge, pts, type) {
    context.moveTo(pts[0], pts[1]);
    context.quadraticCurveTo(pts[2], pts[3], pts[4], pts[5]);

    //if cardinality is zero, return here.
    var cardinality = edge._private.data.sbgncardinality;
    if (cardinality == 0)
      return;

    var carProp = $$.sbgn.cardinalityProperties();

    var totalLength = qBezierLength(pts);

    var startLength = totalLength - 25;

    var startPortion = startLength / totalLength;

    if (type === "consumption") {
      startPortion = carProp.distanceToSource / totalLength;
    }
    else {
      startPortion = (totalLength - carProp.distanceToTarget) / totalLength;
    }

    var t = startPortion;
    var x1 = (1 - t) * (1 - t) * pts[0] + 2 * (1 - t) * t * pts[2] + t * t * pts[4];
    var y1 = (1 - t) * (1 - t) * pts[1] + 2 * (1 - t) * t * pts[3] + t * t * pts[5];

    //get a short line to determine tanget line
    t = startPortion + 0.01;
    var x2 = (1 - t) * (1 - t) * pts[0] + 2 * (1 - t) * t * pts[2] + t * t * pts[4];
    var y2 = (1 - t) * (1 - t) * pts[1] + 2 * (1 - t) * t * pts[3] + t * t * pts[5];

    var dispX = x1 - x2;
    var dispY = y1 - y2;

    var angle = Math.asin(dispY / (Math.sqrt(dispX * dispX + dispY * dispY)));
    if (dispX < 0) {
      angle = angle + Math.PI / 2;
    } else {
      angle = -(Math.PI / 2 + angle);
    }

    context.translate(x1, y1);
    context.rotate(-angle);

    context.rect(0, -13 / 2, 13, 13);

    context.rotate(-Math.PI / 2);

    var textProp = {'centerX': 0, 'centerY': 13 / 2,
      'opacity': edge._private.style['text-opacity'].value,
      'width': 13, 'label': cardinality};
    $$.sbgn.drawCardinalityText(context, textProp);

    context.rotate(Math.PI / 2);

    context.rotate(angle);
    context.translate(-x1, -y1);

  };

  function drawStraightLineCardinality(context, edge, pts, type) {
    context.moveTo(pts[0], pts[1]);
    context.lineTo(pts[2], pts[3]);

    //if cardinality is zero, return here.
    var cardinality = edge._private.data.sbgncardinality;
    if (cardinality <= 0)
      return;

    var carProp = $$.sbgn.cardinalityProperties();

    var length = (Math.sqrt((pts[2] - pts[0]) * (pts[2] - pts[0]) +
            (pts[3] - pts[1]) * (pts[3] - pts[1])));

    var dispX, dispY, startX, startY;

    //TODO : you may need to change here
    if (type === "consumption") {
      startX = edge._private.rscratch.arrowStartX;
      startY = edge._private.rscratch.arrowStartY;
    }
    else {
      startX = edge._private.rscratch.arrowEndX;
      startY = edge._private.rscratch.arrowEndY;
    }
    var srcPos = (type === "consumption") ? edge.source().position() : edge.target().position();
    //var srcPos = edge.source().position();
    dispX = startX - srcPos.x;
    dispY = startY - srcPos.y;

    var angle = Math.asin(dispY / (Math.sqrt(dispX * dispX + dispY * dispY)));

    if (dispX < 0) {
      angle = angle + Math.PI / 2;
    } else {
      angle = -(Math.PI / 2 + angle);
    }

    context.translate(startX, startY);
    context.rotate(-angle);

    if (length > carProp.distanceToNode) {
      context.rect(0, -carProp.distanceToNode, carProp.boxLength, carProp.boxLength);

      context.rotate(Math.PI / 2);

      var textProp = {'centerX': -carProp.distanceToNode + carProp.boxLength / 2, 'centerY': -carProp.boxLength / 2,
        'opacity': edge._private.style['text-opacity'].value,
        'width': carProp.boxLength, 'label': cardinality};
      $$.sbgn.drawCardinalityText(context, textProp);

      context.rotate(-Math.PI / 2);
    }

    context.rotate(angle);
    context.translate(-startX, -startY);
  }
  ;

  var calls = 0;
  var time = 0;
  var avg = 0;

  CanvasRenderer.prototype.drawStyledEdge = function (
          edge, context, pts, type, width) {

    var start = +new Date();

    // 3 points given -> assume Bezier
    // 2 -> assume straight

    var cy = this.data.cy;
    var zoom = cy.zoom();
    var rs = edge._private.rscratch;
    var canvasCxt = context;
    var path;
    var pathCacheHit = false;
    var usePaths = CanvasRenderer.usePaths();

    // Adjusted edge width for dotted
    // width = Math.max(width * 1.6, 3.4) * zoom;
    // console.log('w', width);

    if (type === 'solid') {

      if (usePaths) {
        var pathCacheKey = pts;
        var keyLengthMatches = rs.pathCacheKey && pathCacheKey.length === rs.pathCacheKey.length;
        var keyMatches = keyLengthMatches;

        for (var i = 0; keyMatches && i < pathCacheKey.length; i++) {
          if (rs.pathCacheKey[i] !== pathCacheKey[i]) {
            keyMatches = false;
          }
        }

        if (keyMatches) {
          path = context = rs.pathCache;
          pathCacheHit = true;
        } else {
          path = context = new Path2D();
          rs.pathCacheKey = pathCacheKey;
          rs.pathCache = path;
        }
      }

      if (!pathCacheHit) {
        if (context.beginPath) {
          context.beginPath();
        }
        context.moveTo(pts[0], pts[1]);
        if (pts.length == 3 * 2) {
          context.quadraticCurveTo(pts[2], pts[3], pts[4], pts[5]);
        } else {
          context.lineTo(pts[2], pts[3]);
        }
      }

      context = canvasCxt;
      if (usePaths) {
        context.stroke(path);
      } else {
        context.stroke();
      }

    } else if (type === 'consumption' || type === 'production') {

      // if( usePaths ){
      // 	var pathCacheKey = pts;
      // 	var keyLengthMatches = rs.pathCacheKey && pathCacheKey.length === rs.pathCacheKey.length;
      // 	var keyMatches = keyLengthMatches;

      // 	for( var i = 0; keyMatches && i < pathCacheKey.length; i++ ){
      // 		if( rs.pathCacheKey[i] !== pathCacheKey[i] ){
      // 			keyMatches = false;
      // 		}
      // 	}

      // 	if( keyMatches ){
      // 		path = context = rs.pathCache;
      // 		pathCacheHit = true;
      // 	} else {
      // 		path = context = new Path2D();
      // 	  	rs.pathCacheKey = pathCacheKey;
      // 	  	rs.pathCache = path;
      // 	}
      // }

      if (!pathCacheHit) {
        if (context.beginPath) {
          context.beginPath();
        }
        if (pts.length == 3 * 2) {
          drawQuadraticLineCardinality(context, edge, pts, type);
        } else {
          drawStraightLineCardinality(context, edge, pts, type);
        }
      }

      context = canvasCxt;
      // if( usePaths ){
      // 	context.stroke( path );
      // } else {
      context.stroke();
      // }

    } else if (type === 'dotted') {

      var pt;
      if (pts.length == 3 * 2) {
        pt = _genPoints(pts, 16, true);
      } else {
        pt = _genStraightLinePoints(pts, 16, true);
      }

      if (!pt) {
        return;
      }

      var dotRadius = Math.max(width * 1.6, 3.4) * zoom;
      var bufW = dotRadius * 2, bufH = dotRadius * 2;
      bufW = Math.max(bufW, 1);
      bufH = Math.max(bufH, 1);

      var buffer = this.createBuffer(bufW, bufH);

      var context2 = buffer[1];
      //console.log(buffer);
      //console.log(bufW, bufH);

      // Draw on buffer
      context2.setTransform(1, 0, 0, 1, 0, 0);
      context2.clearRect(0, 0, bufW, bufH);

      context2.fillStyle = context.strokeStyle;
      context2.beginPath();
      context2.arc(bufW / 2, bufH / 2, dotRadius * 0.5, 0, Math.PI * 2, false);
      context2.fill();

      // Now use buffer
      context.beginPath();
      //context.save();

      for (var i = 0; i < pt.length / 2; i++) {

        //context.beginPath();
        //context.arc(pt[i*2], pt[i*2+1], width * 0.5, 0, Math.PI * 2, false);
        //context.fill();

        context.drawImage(
                buffer[0],
                pt[i * 2] - bufW / 2 / zoom,
                pt[i * 2 + 1] - bufH / 2 / zoom,
                bufW / zoom,
                bufH / zoom);
      }

      context.closePath();

      //context.restore();

    } else if (type === 'dashed') {
      var pt;
      if (pts.length == 3 * 2) {
        pt = _genPoints(pts, 14, true);
      } else {
        pt = _genStraightLinePoints(pts, 14, true);
      }
      if (!pt) {
        return;
      }

      //var dashSize = Math.max(width * 1.6, 3.4);
      //dashSize = Math.min(dashSize)

      //var bufW = width * 2 * zoom, bufH = width * 2.5 * zoom;
      var bufW = width * 2 * zoom;
      var bufH = 7.8 * zoom;
      bufW = Math.max(bufW, 1);
      bufH = Math.max(bufH, 1);

      var buffer = this.createBuffer(bufW, bufH);
      var context2 = buffer[1];

      // Draw on buffer
      context2.setTransform(1, 0, 0, 1, 0, 0);
      context2.clearRect(0, 0, bufW, bufH);

      if (context.strokeStyle) {
        context2.strokeStyle = context.strokeStyle;
      }

      context2.lineWidth = width * cy.zoom();

      //context2.fillStyle = context.strokeStyle;

      context2.beginPath();
      context2.moveTo(bufW / 2, bufH * 0.2);
      context2.lineTo(bufW / 2, bufH * 0.8);

      //context2.arc(bufH, dotRadius, dotRadius * 0.5, 0, Math.PI * 2, false);

      //context2.fill();
      context2.stroke();

      //context.save();

      //document.body.appendChild(buffer[0]);

      var quadraticBezierVaryingTangent = false;
      var rotateVector, angle;

      //Straight line; constant tangent angle
      if (pts.length == 2 * 2) {
        rotateVector = [pts[2] - pts[0], pts[3] - pt[1]];

        angle = Math.acos((rotateVector[0] * 0 + rotateVector[1] * -1) / Math.sqrt(rotateVector[0] * rotateVector[0]
                + rotateVector[1] * rotateVector[1]));

        if (rotateVector[0] < 0) {
          angle = -angle + 2 * Math.PI;
        }
      } else if (pts.length == 3 * 2) {
        quadraticBezierVaryingTangent = true;
      }

      for (var i = 0; i < pt.length / 2; i++) {

        var p = i / (Math.max(pt.length / 2 - 1, 1));

        // Quadratic bezier; varying tangent
        // So, use derivative of quadratic Bezier function to find tangents
        if (quadraticBezierVaryingTangent) {
          rotateVector = [2 * (1 - p) * (pts[2] - pts[0])
                    + 2 * p * (pts[4] - pts[2]),
            2 * (1 - p) * (pts[3] - pts[1])
                    + 2 * p * (pts[5] - pts[3])];

          angle = Math.acos((rotateVector[0] * 0 + rotateVector[1] * -1) / Math.sqrt(rotateVector[0] * rotateVector[0]
                  + rotateVector[1] * rotateVector[1]));

          if (rotateVector[0] < 0) {
            angle = -angle + 2 * Math.PI;
          }
        }

        context.translate(pt[i * 2], pt[i * 2 + 1]);

        context.rotate(angle);
        context.translate(-bufW / 2 / zoom, -bufH / 2 / zoom);

        context.drawImage(
                buffer[0],
                0,
                0,
                bufW / zoom,
                bufH / zoom);

        context.translate(bufW / 2 / zoom, bufH / 2 / zoom);
        context.rotate(-angle);

        context.translate(-pt[i * 2], -pt[i * 2 + 1]);

        context.closePath();

      }

      //context.restore();
    } else {
      this.drawStyledEdge(edge, context, pts, 'solid', width);
    }

    var end = +new Date();
    time += end - start;
    avg = time / (++calls);
    window.avg = avg;
  };
})(cytoscape);


/* 
 * Those are the utilization functions to use for drawing sbgn shapes.
 */
;
(function ($$) {
  'use strict';
  $$.sbgn = {};

  var CanvasRenderer = $$('renderer', 'canvas');
  var renderer = CanvasRenderer.prototype;
  var nodeShapes = CanvasRenderer.nodeShapes;
  var sbgnNodeShapes = CanvasRenderer.sbgnShapes = {};

  $$.sbgn.cardinalityProperties = function () {
    return {
      boxLength: 13,
      distanceToNode: 25,
    };
  }

  $$.sbgn.colors = {
    clone: "#a9a9a9",
    association: "#6B6B6B",
    port: "#6B6B6B"
  };

  function truncateText(textProp, context) {
    var width;
    var text = (typeof textProp.label === 'undefined') ? "" : textProp.label;
    var len = text.length;
    var ellipsis = "...";

    //if(context.measureText(text).width < textProp.width)
    //	return text;
    var textWidth = (textProp.width > 30) ? textProp.width - 10 : textProp.width;

    while ((width = context.measureText(text).width) > textWidth) {
      --len;
      text = text.substring(0, len) + ellipsis;
    }
    return text;
  }
  ;

  $$.sbgn.drawText = function (context, textProp, truncate) {
    var oldFont = context.font;
    context.font = textProp.font;
    context.textAlign = "center";
    context.textBaseline = "middle";
    var oldStyle = context.fillStyle;
    context.fillStyle = textProp.color;
    var oldOpacity = context.globalAlpha;
    context.globalAlpha = textProp.opacity;
    var text;
    if (truncate == false) {
      text = textProp.label;
    }
    else {
      text = truncateText(textProp, context);
    }
    context.fillText(text, textProp.centerX, textProp.centerY);
    context.fillStyle = oldStyle;
    context.font = oldFont;
    context.globalAlpha = oldOpacity;
    //context.stroke();
  };

  $$.sbgn.drawLabelText = function (context, textProp) {
    textProp.color = "#0f0f0f";
    textProp.font = "9px Arial";
    $$.sbgn.drawText(context, textProp);
  };

  $$.sbgn.drawCardinalityText = function (context, textProp) {
    textProp.color = "#0f0f0f";
    textProp.font = "9px Arial";
    $$.sbgn.drawText(context, textProp, false);
  }

  $$.sbgn.drawDynamicLabelText = function (context, textProp) {
    var textHeight = parseInt(textProp.height / (2.45));
    textProp.color = "#0f0f0f";
    textProp.font = textHeight + "px Arial";
    $$.sbgn.drawText(context, textProp);
  };

  $$.sbgn.drawStateText = function (context, textProp) {
    var stateValue = textProp.state.value;
    var stateVariable = textProp.state.variable;

    var stateLabel = (stateVariable == null /*|| typeof stateVariable === undefined */) ? stateValue :
            stateValue + "@" + stateVariable;

    var fontSize = parseInt(textProp.height / 1.5);

    textProp.font = fontSize + "px Arial";
    textProp.label = stateLabel;
    textProp.color = "#0f0f0f";
    $$.sbgn.drawText(context, textProp);
  };

  $$.sbgn.drawInfoText = function (context, textProp) {
    var fontSize = parseInt(textProp.height / 1.5);
    textProp.font = fontSize + "px Arial";
    textProp.color = "#0f0f0f";
    $$.sbgn.drawText(context, textProp);
  };

  $$.sbgn.drawCloneMarkerText = function (context, textProp) {
    textProp.color = "#fff";
    textProp.font = "4px Arial";
    $$.sbgn.drawText(context, textProp);
  };

  $$.sbgn.drawEllipsePath = function (context, x, y, width, height) {
    // context.beginPath();
    // context.translate(x, y);
    // context.scale(width / 2, height / 2);
    // context.arc(0, 0, 1, 0, Math.PI * 2 * 0.999, false); // *0.999 b/c chrome rendering bug on full circle
    // context.closePath();
    // context.scale(2/width, 2/height);
    // context.translate(-x, -y);
    nodeShapes['ellipse'].drawPath(context, x, y, width, height);
  };

  $$.sbgn.drawEllipse = function (context, x, y, width, height) {
    //$$.sbgn.drawEllipsePath(context, x, y, width, height);
    //context.fill();
    nodeShapes['ellipse'].draw(context, x, y, width, height);
  };

  $$.sbgn.drawNucAcidFeature = function (context, width, height,
          centerX, centerY, cornerRadius) {
    var halfWidth = width / 2;
    var halfHeight = height / 2;

    context.translate(centerX, centerY);
    context.beginPath();

    context.moveTo(-halfWidth, -halfHeight);
    context.lineTo(halfWidth, -halfHeight);
    context.lineTo(halfWidth, 0);
    context.arcTo(halfWidth, halfHeight, 0, halfHeight, cornerRadius);
    context.arcTo(-halfWidth, halfHeight, -halfWidth, 0, cornerRadius);
    context.lineTo(-halfWidth, -halfHeight);

    context.closePath();
    context.translate(-centerX, -centerY);
    context.fill();
  };

  $$.sbgn.drawStateAndInfos = function (node, context, centerX, centerY) {
    var stateAndInfos = node._private.data.sbgnstatesandinfos;
    var stateCount = 0, infoCount = 0;

    for (var i = 0; i < stateAndInfos.length; i++) {
      var state = stateAndInfos[i];
      var stateWidth = state.bbox.w;
      var stateHeight = state.bbox.h;
      var stateCenterX = state.bbox.x + centerX;
      var stateCenterY = state.bbox.y + centerY;

      var textProp = {'centerX': stateCenterX, 'centerY': stateCenterY,
        'opacity': node._private.style['text-opacity'].value,
        'width': stateWidth, 'height': stateHeight};

      if (state.clazz == "state variable" && stateCount < 2) {//draw ellipse
        //var stateLabel = state.state.value;
        $$.sbgn.drawEllipse(context, stateCenterX, stateCenterY,
                stateWidth, stateHeight);

        textProp.state = state.state;
        $$.sbgn.drawStateText(context, textProp);

        stateCount++;
        context.stroke();

      }
      else if (state.clazz == "unit of information" && infoCount < 2) {//draw rectangle
        renderer.drawRoundRectangle(context,
                stateCenterX, stateCenterY,
                stateWidth, stateHeight,
                10);

        textProp.label = state.label.text;
        $$.sbgn.drawInfoText(context, textProp);

        infoCount++;
        context.stroke();
      }
    }
  };

  //use this function to sort states according to their x positions
  $$.sbgn.compareStates = function (st1, st2) {
    if (st1.bbox.x < st2.bbox.x)
      return -1;
    if (st1.bbox.x > st2.bbox.x)
      return 1;
    return 0;
  };

  $$.sbgn.drawComplexStateAndInfo = function (context, node, stateAndInfos,
          centerX, centerY, width, height) {
    var upWidth = 0, downWidth = 0;
    var boxPadding = 10, betweenBoxPadding = 5;
    var beginPosY = height / 2, beginPosX = width / 2;

    stateAndInfos.sort($$.sbgn.compareStates);

    for (var i = 0; i < stateAndInfos.length; i++) {
      var state = stateAndInfos[i];
      var stateWidth = state.bbox.w;
      var stateHeight = state.bbox.h;
      var stateLabel = state.state.value;
      var relativeYPos = state.bbox.y;
      var stateCenterX, stateCenterY;

      if (relativeYPos < 0) {
        if (upWidth + stateWidth < width) {
          stateCenterX = centerX - beginPosX + boxPadding + upWidth + stateWidth / 2;
          stateCenterY = centerY - beginPosY;

          var textProp = {'centerX': stateCenterX, 'centerY': stateCenterY,
            'opacity': node._private.style['text-opacity'].value,
            'width': stateWidth, 'height': stateHeight};

          if (state.clazz == "state variable") {//draw ellipse
            $$.sbgn.drawEllipse(context,
                    stateCenterX, stateCenterY,
                    stateWidth, stateHeight);

            textProp.state = state.state;
            $$.sbgn.drawStateText(context, textProp);
          }
          else if (state.clazz == "unit of information") {//draw rectangle
            renderer.drawRoundRectangle(context,
                    stateCenterX, stateCenterY,
                    stateWidth, stateHeight,
                    10);

            textProp.label = state.label.text;
            $$.sbgn.drawInfoText(context, textProp);
          }
        }
        upWidth = upWidth + width + boxPadding;
      }
      else if (relativeYPos > 0) {
        if (downWidth + stateWidth < width) {
          stateCenterX = centerX - beginPosX + boxPadding + downWidth + stateWidth / 2;
          stateCenterY = centerY + beginPosY;

          var textProp = {'centerX': stateCenterX, 'centerY': stateCenterY,
            'opacity': node._private.style['text-opacity'].value,
            'width': stateWidth, 'height': stateHeight};

          if (state.clazz == "state variable") {//draw ellipse
            $$.sbgn.drawEllipse(context,
                    stateCenterX, stateCenterY,
                    stateWidth, stateHeight);

            textProp.state = state.state;
            $$.sbgn.drawStateText(context, textProp);
          }
          else if (state.clazz == "unit of information") {//draw rectangle
            renderer.drawRoundRectangle(context,
                    stateCenterX, stateCenterY,
                    stateWidth, stateHeight,
                    10);
            textProp.label = state.label.text;
            $$.sbgn.drawInfoText(context, textProp);
          }
        }
        downWidth = downWidth + width + boxPadding;
      }
      context.stroke();

      //update new state and info position(relative to node center)
      state.bbox.x = stateCenterX - centerX;
      state.bbox.y = stateCenterY - centerY;
    }
  };

  //this function is created to have same corner length when
  //complex's width or height is changed
  $$.sbgn.generateComplexShapePoints = function (cornerLength, width, height) {
    //cp stands for corner proportion
    var cpX = cornerLength / width;
    var cpY = cornerLength / height;

    var complexPoints = [-1 + cpX, -1, -1, -1 + cpY, -1, 1 - cpY, -1 + cpX,
      1, 1 - cpX, 1, 1, 1 - cpY, 1, -1 + cpY, 1 - cpX, -1];

    return complexPoints;
  };

  $$.sbgn.drawSimpleChemicalPath = function (
          context, x, y, width, height) {

    var halfWidth = width / 2;
    var halfHeight = height / 2;
    //var cornerRadius = $$.math.getRoundRectangleRadius(width, height);
    var cornerRadius = Math.min(halfWidth, halfHeight);
    context.translate(x, y);

    context.beginPath();

    // Start at top middle
    context.moveTo(0, -halfHeight);
    // Arc from middle top to right side
    context.arcTo(halfWidth, -halfHeight, halfWidth, 0, cornerRadius);
    // Arc from right side to bottom
    context.arcTo(halfWidth, halfHeight, 0, halfHeight, cornerRadius);
    // Arc from bottom to left side
    context.arcTo(-halfWidth, halfHeight, -halfWidth, 0, cornerRadius);
    // Arc from left side to topBorder
    context.arcTo(-halfWidth, -halfHeight, 0, -halfHeight, cornerRadius);
    // Join line
    context.lineTo(0, -halfHeight);

    context.closePath();

    context.translate(-x, -y);
  };

  $$.sbgn.drawSimpleChemical = function (
          context, x, y, width, height) {
    $$.sbgn.drawSimpleChemicalPath(context, x, y, width, height);
    context.fill();
  };

  function simpleChemicalLeftClone(context, centerX, centerY,
          width, height, cloneMarker, opacity) {
    if (cloneMarker != null) {
      var oldGlobalAlpha = context.globalAlpha;
      context.globalAlpha = opacity;
      var oldStyle = context.fillStyle;
      context.fillStyle = $$.sbgn.colors.clone;

      context.beginPath();
      context.translate(centerX, centerY);
      context.scale(width / 2, height / 2);

      var markerBeginX = -1 * Math.sin(Math.PI / 3);
      var markerBeginY = Math.cos(Math.PI / 3);
      var markerEndX = 0;
      var markerEndY = markerBeginY;

      context.moveTo(markerBeginX, markerBeginY);
      context.lineTo(markerEndX, markerEndY);
      context.arc(0, 0, 1, 3 * Math.PI / 6, 5 * Math.PI / 6);

      context.scale(2 / width, 2 / height);
      context.translate(-centerX, -centerY);
      context.closePath();

      context.fill();
      context.fillStyle = oldStyle;
      context.globalAlpha = oldGlobalAlpha;
    }
  }
  ;

  function simpleChemicalRightClone(context, centerX, centerY,
          width, height, cloneMarker, opacity) {
    if (cloneMarker != null) {
      var oldGlobalAlpha = context.globalAlpha;
      context.globalAlpha = opacity;
      var oldStyle = context.fillStyle;
      context.fillStyle = $$.sbgn.colors.clone;

      context.beginPath();
      context.translate(centerX, centerY);
      context.scale(width / 2, height / 2);

      var markerBeginX = 0;
      var markerBeginY = Math.cos(Math.PI / 3);
      var markerEndX = 1 * Math.sin(Math.PI / 3);
      var markerEndY = markerBeginY;

      context.moveTo(markerBeginX, markerBeginY);
      context.lineTo(markerEndX, markerEndY);
      context.arc(0, 0, 1, Math.PI / 6, 3 * Math.PI / 6);

      context.scale(2 / width, 2 / height);
      context.translate(-centerX, -centerY);
      context.closePath();

      context.fill();
      context.fillStyle = oldStyle;
      context.globalAlpha = oldGlobalAlpha;
    }
  }
  ;

  $$.sbgn.cloneMarker = {
    unspecifiedEntity: function (context, centerX, centerY,
            width, height, cloneMarker, opacity) {
      if (cloneMarker != null) {
        var oldGlobalAlpha = context.globalAlpha;
        context.globalAlpha = opacity;
        var oldStyle = context.fillStyle;
        context.fillStyle = $$.sbgn.colors.clone;

        context.beginPath();
        context.translate(centerX, centerY);
        context.scale(width / 2, height / 2);

        var markerBeginX = -1 * Math.sin(Math.PI / 3);
        var markerBeginY = Math.cos(Math.PI / 3);
        var markerEndX = 1 * Math.sin(Math.PI / 3);
        var markerEndY = markerBeginY;

        context.moveTo(markerBeginX, markerBeginY);
        context.lineTo(markerEndX, markerEndY);
        context.arc(0, 0, 1, Math.PI / 6, 5 * Math.PI / 6);

        context.scale(2 / width, 2 / height);
        context.translate(-centerX, -centerY);
        context.closePath();

        context.fill();
        context.fillStyle = oldStyle;
        context.globalAlpha = oldGlobalAlpha;
      }
    },
    sourceAndSink: function (context, centerX, centerY,
            width, height, cloneMarker, opacity) {
      $$.sbgn.cloneMarker.unspecifiedEntity(context, centerX, centerY,
              width, height, cloneMarker, opacity);
    },
    simpleChemical: function (context, centerX, centerY,
            width, height, cloneMarker, isMultimer, opacity) {
      if (cloneMarker != null) {
        var cornerRadius = Math.min(width / 2, height / 2);

        var firstCircleCenterX = centerX - width / 2 + cornerRadius;
        var firstCircleCenterY = centerY;
        var secondCircleCenterX = centerX + width / 2 - cornerRadius;
        var secondCircleCenterY = centerY;

        simpleChemicalLeftClone(context, firstCircleCenterX, firstCircleCenterY,
                2 * cornerRadius, 2 * cornerRadius, cloneMarker, opacity);

        simpleChemicalRightClone(context, secondCircleCenterX, secondCircleCenterY,
                2 * cornerRadius, 2 * cornerRadius, cloneMarker, opacity);

        var oldStyle = context.fillStyle;
        context.fillStyle = $$.sbgn.colors.clone;
        var oldGlobalAlpha = context.globalAlpha;
        context.globalAlpha = opacity;

        var recPoints = $$.math.generateUnitNgonPointsFitToSquare(4, 0);
        var cloneX = centerX;
        var cloneY = centerY + 3 / 4 * cornerRadius;
        var cloneWidth = width - 2 * cornerRadius;
        var cloneHeight = cornerRadius / 2;

        renderer.drawPolygon(context, cloneX, cloneY, cloneWidth, cloneHeight, recPoints);
        context.fillStyle = oldStyle;
        context.globalAlpha = oldGlobalAlpha;
      }
    },
    perturbingAgent: function (context, centerX, centerY,
            width, height, cloneMarker, opacity) {
      if (cloneMarker != null) {
        var cloneWidth = width;
        var cloneHeight = height / 4;
        var cloneX = centerX;
        var cloneY = centerY + height / 2 - height / 8;

        var markerPoints = [-5 / 6, -1, 5 / 6, -1, 1, 1, -1, 1];

        var oldStyle = context.fillStyle;
        context.fillStyle = $$.sbgn.colors.clone;
        var oldGlobalAlpha = context.globalAlpha;
        context.globalAlpha = opacity;

        renderer.drawPolygon(context,
                cloneX, cloneY,
                cloneWidth, cloneHeight, markerPoints);

        context.fill();

        context.fillStyle = oldStyle;
        context.globalAlpha = oldGlobalAlpha;
        //context.stroke();
      }
    },
    nucleicAcidFeature: function (context, centerX, centerY,
            width, height, cloneMarker, isMultimer, opacity) {
      if (cloneMarker != null) {
        var cloneWidth = width;
        var cloneHeight = height / 4;
        var cloneX = centerX;
        var cloneY = centerY + 3 * height / 8;

        var oldStyle = context.fillStyle;
        context.fillStyle = $$.sbgn.colors.clone;
        var oldGlobalAlpha = context.globalAlpha;
        context.globalAlpha = opacity;

        var cornerRadius = $$.math.getRoundRectangleRadius(width, height);

        $$.sbgn.drawNucAcidFeature(context, cloneWidth, cloneHeight,
                cloneX, cloneY, cornerRadius, opacity);

        context.fillStyle = oldStyle;
        context.globalAlpha = oldGlobalAlpha;
        //context.stroke();
      }
    },
    macromolecule: function (context, centerX, centerY,
            width, height, cloneMarker, isMultimer, opacity) {
      $$.sbgn.cloneMarker.nucleicAcidFeature(context, centerX, centerY,
              width, height, cloneMarker, isMultimer, opacity);
    },
    complex: function (context, centerX, centerY,
            width, height, cornerLength, cloneMarker, isMultimer, opacity) {
      if (cloneMarker != null) {
        var cpX = cornerLength / width;
        var cpY = cornerLength / height;
        var cloneWidth = width;
        var cloneHeight = height * cpY / 2;
        var cloneX = centerX;
        var cloneY = centerY + height / 2 - cloneHeight / 2;

        var markerPoints = [-1, -1, 1, -1, 1 - cpX, 1, -1 + cpX, 1];

        var oldStyle = context.fillStyle;
        context.fillStyle = $$.sbgn.colors.clone;
        var oldGlobalAlpha = context.globalAlpha;
        context.globalAlpha = opacity;

        renderer.drawPolygon(context,
                cloneX, cloneY,
                cloneWidth, cloneHeight, markerPoints);

        context.fillStyle = oldStyle;
        context.globalAlpha = oldGlobalAlpha;
        //context.stroke();
      }
    }
  };

  $$.sbgn.isMultimer = function (node) {
    var sbgnClass = node._private.data.sbgnclass;
    if (sbgnClass.indexOf("multimer") != -1)
      return true;
    return false;
  };

  $$.sbgn.nucleicAcidIntersectionLine = function (node, x, y, nodeX, nodeY, cornerRadius) {
    var nodeX = node._private.position.x;
    var nodeY = node._private.position.y;
    var width = node.width();
    var height = node.height();
    var padding = node._private.style["border-width"].pxValue / 2;

    var halfWidth = width / 2;
    var halfHeight = height / 2;

    var straightLineIntersections;

    // Top segment, left to right
    {
      var topStartX = nodeX - halfWidth - padding;
      var topStartY = nodeY - halfHeight - padding;
      var topEndX = nodeX + halfWidth + padding;
      var topEndY = topStartY;

      straightLineIntersections = $$.math.finiteLinesIntersect(
              x, y, nodeX, nodeY, topStartX, topStartY, topEndX, topEndY, false);

      if (straightLineIntersections.length > 0) {
        return straightLineIntersections;
      }
    }

    // Right segment, top to bottom
    {
      var rightStartX = nodeX + halfWidth + padding;
      var rightStartY = nodeY - halfHeight - padding;
      var rightEndX = rightStartX;
      var rightEndY = nodeY + halfHeight - cornerRadius + padding;

      straightLineIntersections = $$.math.finiteLinesIntersect(
              x, y, nodeX, nodeY, rightStartX, rightStartY, rightEndX, rightEndY, false);

      if (straightLineIntersections.length > 0) {
        return straightLineIntersections;
      }
    }

    // Bottom segment, left to right
    {
      var bottomStartX = nodeX - halfWidth + cornerRadius - padding;
      var bottomStartY = nodeY + halfHeight + padding;
      var bottomEndX = nodeX + halfWidth - cornerRadius + padding;
      var bottomEndY = bottomStartY;

      straightLineIntersections = $$.math.finiteLinesIntersect(
              x, y, nodeX, nodeY, bottomStartX, bottomStartY, bottomEndX, bottomEndY, false);

      if (straightLineIntersections.length > 0) {
        return straightLineIntersections;
      }
    }

    // Left segment, top to bottom
    {
      var leftStartX = nodeX - halfWidth - padding;
      var leftStartY = nodeY - halfHeight - padding;
      var leftEndX = leftStartX;
      var leftEndY = nodeY + halfHeight - cornerRadius + padding;

      straightLineIntersections = $$.math.finiteLinesIntersect(
              x, y, nodeX, nodeY, leftStartX, leftStartY, leftEndX, leftEndY, false);

      if (straightLineIntersections.length > 0) {
        return straightLineIntersections;
      }
    }

    // Check intersections with arc segments, we have only two arcs for
    //nucleic acid features
    var arcIntersections;

    // Bottom Right
    {
      var bottomRightCenterX = nodeX + halfWidth - cornerRadius;
      var bottomRightCenterY = nodeY + halfHeight - cornerRadius
      arcIntersections = $$.math.intersectLineCircle(
              x, y, nodeX, nodeY,
              bottomRightCenterX, bottomRightCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] >= bottomRightCenterX
              && arcIntersections[1] >= bottomRightCenterY) {
        return [arcIntersections[0], arcIntersections[1]];
      }
    }

    // Bottom Left
    {
      var bottomLeftCenterX = nodeX - halfWidth + cornerRadius;
      var bottomLeftCenterY = nodeY + halfHeight - cornerRadius
      arcIntersections = $$.math.intersectLineCircle(
              x, y, nodeX, nodeY,
              bottomLeftCenterX, bottomLeftCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] <= bottomLeftCenterX
              && arcIntersections[1] >= bottomLeftCenterY) {
        return [arcIntersections[0], arcIntersections[1]];
      }
    }
    return []; // if nothing
  };

  $$.sbgn.nucleicAcidIntersectionBox = function (x1, y1, x2, y2, centerX, centerY, node, points, cornerRadius) {
    var width = node.width();
    var height = node.height();
    var padding = node._private.style["border-width"].pxValue / 2;

    //we have a rectangle at top and a roundrectangle at bottom
    var rectIntersectBoxResult = $$.math.boxIntersectPolygon(
            x1, y1, x2, y2,
            points, width, height - cornerRadius, centerX,
            centerY - cornerRadius / 2, [0, -1], padding);

    var roundRectIntersectBoxResult = $$.math.roundRectangleIntersectBox(
            x1, y1, x2, y2,
            width, 2 * cornerRadius, centerX,
            centerY + height / 2 - cornerRadius, padding);

    return rectIntersectBoxResult || roundRectIntersectBoxResult;
  };

  $$.sbgn.nucleicAcidCheckPoint = function (x, y, centerX, centerY, node, threshold, points, cornerRadius) {
    var width = node.width();
    var height = node.height();
    var padding = node._private.style["border-width"].pxValue / 2;

    //check rectangle at top
    if ($$.math.pointInsidePolygon(x, y, points,
            centerX, centerY - cornerRadius / 2, width, height - cornerRadius / 3, [0, -1],
            padding)) {
      return true;
    }

    //check rectangle at bottom
    if ($$.math.pointInsidePolygon(x, y, points,
            centerX, centerY + height / 2 - cornerRadius / 2, width - 2 * cornerRadius, cornerRadius, [0, -1],
            padding)) {
      return true;
    }

    //check ellipses
    var checkInEllipse = function (x, y, centerX, centerY, width, height, padding) {
      x -= centerX;
      y -= centerY;

      x /= (width / 2 + padding);
      y /= (height / 2 + padding);

      return (Math.pow(x, 2) + Math.pow(y, 2) <= 1);
    }

    // Check bottom right quarter circle
    if (checkInEllipse(x, y,
            centerX + width / 2 - cornerRadius,
            centerY + height / 2 - cornerRadius,
            cornerRadius * 2, cornerRadius * 2, padding)) {

      return true;
    }

    // Check bottom left quarter circle
    if (checkInEllipse(x, y,
            centerX - width / 2 + cornerRadius,
            centerY + height / 2 - cornerRadius,
            cornerRadius * 2, cornerRadius * 2, padding)) {

      return true;
    }

    return false;
  };

  $$.sbgn.checkPointStateAndInfoBoxes = function (x, y, node, threshold) {
    var centerX = node._private.position.x;
    var centerY = node._private.position.y;
    var padding = node._private.style["border-width"].pxValue / 2;
    var stateAndInfos = node._private.data.sbgnstatesandinfos;

    var stateCount = 0, infoCount = 0;


    for (var i = 0; i < stateAndInfos.length; i++) {
      var state = stateAndInfos[i];
      var stateWidth = state.bbox.w + threshold;
      var stateHeight = state.bbox.h + threshold;
      var stateCenterX = state.bbox.x + centerX;
      var stateCenterY = state.bbox.y + centerY;

      if (state.clazz == "state variable" && stateCount < 2) {//draw ellipse
        var stateCheckPoint = nodeShapes["ellipse"].checkPoint(
                x, y, padding, stateWidth, stateHeight, stateCenterX, stateCenterY);

        if (stateCheckPoint == true)
          return true;

        stateCount++;
      }
      else if (state.clazz == "unit of information" && infoCount < 2) {//draw rectangle
        var infoCheckPoint = nodeShapes["roundrectangle"].checkPoint(
                x, y, padding, stateWidth, stateHeight, stateCenterX, stateCenterY);

        if (infoCheckPoint == true)
          return true;

        infoCount++;
      }

    }
    return false;
  };

  $$.sbgn.intersectLineStateAndInfoBoxes = function (node, x, y) {
    var centerX = node._private.position.x;
    var centerY = node._private.position.y;
    var padding = node._private.style["border-width"].pxValue / 2;

    var stateAndInfos = node._private.data.sbgnstatesandinfos;

    var stateCount = 0, infoCount = 0;

    var intersections = [];

    for (var i = 0; i < stateAndInfos.length; i++) {
      var state = stateAndInfos[i];
      var stateWidth = state.bbox.w;
      var stateHeight = state.bbox.h;
      var stateCenterX = state.bbox.x + centerX;
      var stateCenterY = state.bbox.y + centerY;

      if (state.clazz == "state variable" && stateCount < 2) {//draw ellipse
        var stateIntersectLines = $$.sbgn.intersectLineEllipse(x, y, centerX, centerY,
                stateCenterX, stateCenterY, stateWidth, stateHeight, padding);

        if (stateIntersectLines.length > 0)
          intersections = intersections.concat(stateIntersectLines);

        stateCount++;
      }
      else if (state.clazz == "unit of information" && infoCount < 2) {//draw rectangle
        var infoIntersectLines = $$.sbgn.roundRectangleIntersectLine(x, y, centerX, centerY,
                stateCenterX, stateCenterY, stateWidth, stateHeight, 5, padding);

        if (infoIntersectLines.length > 0)
          intersections = intersections.concat(infoIntersectLines);

        infoCount++;
      }

    }
    if (intersections.length > 0)
      return intersections;
    return [];
  };

  $$.sbgn.intersectBoxStateAndInfoBoxes = function (x1, y1, x2, y2, node) {
    var centerX = node._private.position.x;
    var centerY = node._private.position.y;
    var width = node.width();
    var height = node.height();
    var padding = node._private.style["border-width"].pxValue / 2;

    var stateAndInfos = node._private.data.sbgnstatesandinfos;

    var stateCount = 0, infoCount = 0;
    var padding = node._private.style["border-width"].pxValue / 2;

    for (var i = 0; i < stateAndInfos.length; i++) {
      var state = stateAndInfos[i];
      var stateWidth = state.bbox.w;
      var stateHeight = state.bbox.h;
      var stateCenterX = state.bbox.x + centerX;
      var stateCenterY = state.bbox.y + centerY;

      if (state.clazz == "state variable" && stateCount < 2) {//draw ellipse
        var stateIntersectBox = nodeShapes["ellipse"].intersectBox(x1, y1, x2, y2,
                stateWidth, stateHeight, stateCenterX, stateCenterY, padding);

        if (stateIntersectBox == true)
          return true;

        stateCount++;
      }
      else if (state.clazz == "unit of information" && infoCount < 2) {//draw rectangle
        var infoIntersectBox = nodeShapes["roundrectangle"].intersectBox(x1, y1, x2, y2,
                stateWidth, stateHeight, stateCenterX, stateCenterY, padding);

        if (infoIntersectBox == true)
          return true;

        infoCount++;
      }

    }

    return false;
  };

  $$.math.calculateDistance = function (point1, point2) {
    var distance = Math.pow(point1[0] - point2[0], 2) + Math.pow(point1[1] - point2[1], 2);
    return Math.sqrt(distance);
  };

  $$.sbgn.closestPoint = function (point, cp1, cp2) {
    if (cp1.length < 1)
      return cp2;
    else if (cp2.length < 1)
      return cp1;

    var distance1 = $$.math.calculateDistance(point, cp1);
    var distance2 = $$.math.calculateDistance(point, cp2);

    if (distance1 < distance2)
      return cp1;
    return cp2;
  };

  //we need to force opacity to 1 since we might have state and info boxes.
  //having opaque nodes which have state and info boxes gives unpleasent results.
  $$.sbgn.forceOpacityToOne = function (node, context) {
    var parentOpacity = node.effectiveOpacity();
    if (parentOpacity === 0) {
      return;
    }

    context.fillStyle = "rgba("
            + node._private.style["background-color"].value[0] + ","
            + node._private.style["background-color"].value[1] + ","
            + node._private.style["background-color"].value[2] + ","
            + (1 * node._private.style["opacity"].value * parentOpacity) + ")";
  };

  $$.sbgn.closestIntersectionPoint = function (point, intersections) {
    if (intersections.length <= 0)
      return [];

    var closestIntersection = [];
    var minDistance = Number.MAX_VALUE;

    for (var i = 0; i < intersections.length; i = i + 2) {
      var checkPoint = [intersections[i], intersections[i + 1]];
      var distance = $$.math.calculateDistance(point, checkPoint);

      if (distance < minDistance) {
        minDistance = distance;
        closestIntersection = checkPoint;
      }
    }

    return closestIntersection;
  };

  $$.sbgn.intersectLineEllipse = function (
          x1, y1, x2, y2, centerX, centerY, width, height, padding) {

    var w = width / 2 + padding;
    var h = height / 2 + padding;
    var an = centerX;
    var bn = centerY;

    var d = [x2 - x1, y2 - y1];

    var m = d[1] / d[0];
    var n = -1 * m * x2 + y2;
    var a = h * h + w * w * m * m;
    var b = -2 * an * h * h + 2 * m * n * w * w - 2 * bn * m * w * w;
    var c = an * an * h * h + n * n * w * w - 2 * bn * w * w * n +
            bn * bn * w * w - h * h * w * w;

    var discriminant = b * b - 4 * a * c;

    if (discriminant < 0) {
      return [];
    }

    var t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
    var t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

    var xMin = Math.min(t1, t2);
    var xMax = Math.max(t1, t2);

    var yMin = m * xMin - m * x2 + y2;
    var yMax = m * xMax - m * x2 + y2;

    return [xMin, yMin, xMax, yMax];
  };

  //this function gives the intersections of any line with a round rectangle 
  $$.sbgn.roundRectangleIntersectLine = function (
          x1, y1, x2, y2, nodeX, nodeY, width, height, cornerRadius, padding) {

    var halfWidth = width / 2;
    var halfHeight = height / 2;

    // Check intersections with straight line segments
    var straightLineIntersections = [];

    // Top segment, left to right
    {
      var topStartX = nodeX - halfWidth + cornerRadius - padding;
      var topStartY = nodeY - halfHeight - padding;
      var topEndX = nodeX + halfWidth - cornerRadius + padding;
      var topEndY = topStartY;

      var intersection = renderer.finiteLinesIntersect(
              x1, y1, x2, y2, topStartX, topStartY, topEndX, topEndY, false);

      if (intersection.length > 0) {
        straightLineIntersections = straightLineIntersections.concat(intersection);
      }
    }

    // Right segment, top to bottom
    {
      var rightStartX = nodeX + halfWidth + padding;
      var rightStartY = nodeY - halfHeight + cornerRadius - padding;
      var rightEndX = rightStartX;
      var rightEndY = nodeY + halfHeight - cornerRadius + padding;

      var intersection = renderer.finiteLinesIntersect(
              x1, y1, x2, y2, rightStartX, rightStartY, rightEndX, rightEndY, false);

      if (intersection.length > 0) {
        straightLineIntersections = straightLineIntersections.concat(intersection);
      }
    }

    // Bottom segment, left to right
    {
      var bottomStartX = nodeX - halfWidth + cornerRadius - padding;
      var bottomStartY = nodeY + halfHeight + padding;
      var bottomEndX = nodeX + halfWidth - cornerRadius + padding;
      var bottomEndY = bottomStartY;

      var intersection = renderer.finiteLinesIntersect(
              x1, y1, x2, y2, bottomStartX, bottomStartY, bottomEndX, bottomEndY, false);

      if (intersection.length > 0) {
        straightLineIntersections = straightLineIntersections.concat(intersection);
      }
    }

    // Left segment, top to bottom
    {
      var leftStartX = nodeX - halfWidth - padding;
      var leftStartY = nodeY - halfHeight + cornerRadius - padding;
      var leftEndX = leftStartX;
      var leftEndY = nodeY + halfHeight - cornerRadius + padding;

      var intersection = renderer.finiteLinesIntersect(
              x1, y1, x2, y2, leftStartX, leftStartY, leftEndX, leftEndY, false);

      if (intersection.length > 0) {
        straightLineIntersections = straightLineIntersections.concat(intersection);
      }
    }

    // Check intersections with arc segments
    var arcIntersections;

    // Top Left
    {
      var topLeftCenterX = nodeX - halfWidth + cornerRadius;
      var topLeftCenterY = nodeY - halfHeight + cornerRadius
      arcIntersections = renderer.intersectLineCircle(
              x1, y1, x2, y2,
              topLeftCenterX, topLeftCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] <= topLeftCenterX
              && arcIntersections[1] <= topLeftCenterY) {
        straightLineIntersections = straightLineIntersections.concat(arcIntersections);
      }
    }

    // Top Right
    {
      var topRightCenterX = nodeX + halfWidth - cornerRadius;
      var topRightCenterY = nodeY - halfHeight + cornerRadius
      arcIntersections = renderer.intersectLineCircle(
              x1, y1, x2, y2,
              topRightCenterX, topRightCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] >= topRightCenterX
              && arcIntersections[1] <= topRightCenterY) {
        straightLineIntersections = straightLineIntersections.concat(arcIntersections);
      }
    }

    // Bottom Right
    {
      var bottomRightCenterX = nodeX + halfWidth - cornerRadius;
      var bottomRightCenterY = nodeY + halfHeight - cornerRadius
      arcIntersections = renderer.intersectLineCircle(
              x1, y1, x2, y2,
              bottomRightCenterX, bottomRightCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] >= bottomRightCenterX
              && arcIntersections[1] >= bottomRightCenterY) {
        straightLineIntersections = straightLineIntersections.concat(arcIntersections);
      }
    }

    // Bottom Left
    {
      var bottomLeftCenterX = nodeX - halfWidth + cornerRadius;
      var bottomLeftCenterY = nodeY + halfHeight - cornerRadius
      arcIntersections = renderer.intersectLineCircle(
              x1, y1, x2, y2,
              bottomLeftCenterX, bottomLeftCenterY, cornerRadius + padding);

      // Ensure the intersection is on the desired quarter of the circle
      if (arcIntersections.length > 0
              && arcIntersections[0] <= bottomLeftCenterX
              && arcIntersections[1] >= bottomLeftCenterY) {
        straightLineIntersections = straightLineIntersections.concat(arcIntersections);
      }
    }

    if (straightLineIntersections.length > 0)
      return straightLineIntersections;
    return []; // if nothing
  };

  $$.sbgn.drawPortsToEllipseShape = function (context, node) {
    var width = node.width();
    var height = node.height();
    var centerX = node._private.position.x;
    var centerY = node._private.position.y;
    var padding = node._private.style['border-width'].pxValue / 2;

    for (var i = 0; i < node._private.data.ports.length; i++) {
      var port = node._private.data.ports[i];
      var portX = port.x + centerX;
      var portY = port.y + centerY;
      var closestPoint = $$.math.intersectLineEllipse(
              portX, portY, centerX, centerY, width / 2, height / 2);
      context.moveTo(portX, portY);
      context.lineTo(closestPoint[0], closestPoint[1]);
      context.stroke();

      //add a little black circle to ports
      var oldStyle = context.fillStyle;
      context.fillStyle = $$.sbgn.colors.port;
      $$.sbgn.drawEllipse(context, portX, portY, 2, 2);
      context.fillStyle = oldStyle;
      context.stroke();
    }
  };

  $$.sbgn.drawPortsToPolygonShape = function (context, node, points) {
    var width = node.width();
    var height = node.height();
    var centerX = node._private.position.x;
    var centerY = node._private.position.y;
    var padding = node._private.style['border-width'].pxValue / 2;

    for (var i = 0; i < node._private.data.ports.length; i++) {
      var port = node._private.data.ports[i];
      var portX = port.x + centerX;
      var portY = port.y + centerY;
      var closestPoint = $$.math.polygonIntersectLine(portX, portY,
              points, centerX, centerY, width / 2, height / 2, padding);
      context.beginPath();
      context.moveTo(portX, portY);
      context.lineTo(closestPoint[0], closestPoint[1]);
      context.stroke();
      context.closePath();


      //add a little black circle to ports
      var oldStyle = context.fillStyle;
      context.fillStyle = $$.sbgn.colors.port;
      $$.sbgn.drawEllipse(context, portX, portY, 2, 2);
      context.fillStyle = oldStyle;
      context.stroke();
    }
  };

  $$.sbgn.intersectLinePorts = function (node, x, y, portId) {
    var ports = node._private.data.ports;
    if (ports.length < 0)
      return [];

    var nodeX = node._private.position.x;
    var nodeY = node._private.position.y;
    var width = node.width();
    var height = node.height();
    var padding = node._private.style['border-width'].pxValue / 2;

    for (var i = 0; i < node._private.data.ports.length; i++) {
      var port = node._private.data.ports[i];
      if (portId == port.id) {
        return $$.math.intersectLineEllipse(
                x, y, port.x + nodeX, port.y + nodeY, 1, 1);
      }
    }
    return [];
  };

})(cytoscape);


/*
 * Those are the sbgn node objects
 */
;
(function ($$) {
  'use strict';
  var CanvasRenderer = $$('renderer', 'canvas');
  var renderer = CanvasRenderer.prototype;

  //default node shapes are in nodeShape array,
  //all different types must be added
  var nodeShape = $$.style.types.nodeShape.enums;
  //add each sbgn node type to cytoscape.js
  nodeShape.push('unspecified entity', 'simple chemical', 'macromolecule', 'nucleic acid feature',
          'perturbing agent', 'source and sink', 'complex', 'process', 'omitted process',
          'uncertain process', 'association', 'dissociation', 'phenotype', 'compartment',
          'tag', 'and operator', 'or operator', 'not operator', 'and', 'or', 'not',
          'nucleic acid feature multimer', 'macromolecule multimer', 'simple chemical multimer',
          'complex multimer', 'submap', 'terminal');

  var nodeShapes = CanvasRenderer.nodeShapes;

  nodeShapes["complex"] = {
    points: [],
    multimerPadding: 5,
    cornerLength: 12,
    draw: function (context, node) {
      var width = node.width();
      var height = node.height();
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var stateAndInfos = node._private.data.sbgnstatesandinfos;
      var label = node._private.data.sbgnlabel;
      var cornerLength = nodeShapes["complex"].cornerLength;
      var multimerPadding = nodeShapes["complex"].multimerPadding;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.forceOpacityToOne(node, context);

      nodeShapes["complex"].points = $$.sbgn.generateComplexShapePoints(cornerLength,
              width, height);

      //check whether sbgn class includes multimer substring or not
      if ($$.sbgn.isMultimer(node)) {
        //add multimer shape
        renderer.drawPolygon(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height, nodeShapes["complex"].points);

        context.stroke();

        $$.sbgn.cloneMarker.complex(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height, cornerLength, cloneMarker, true,
                node._private.style['background-opacity'].value);

        //context.stroke();
      }

      renderer.drawPolygon(context,
              centerX, centerY,
              width, height, nodeShapes["complex"].points);

      context.stroke();

      $$.sbgn.cloneMarker.complex(context, centerX, centerY,
              width, height, cornerLength, cloneMarker, false,
              node._private.style['background-opacity'].value);

      $$.sbgn.drawComplexStateAndInfo(context, node, stateAndInfos, centerX, centerY, width, height);

    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["complex"].multimerPadding;
      var cornerLength = nodeShapes["complex"].cornerLength;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      nodeShapes["complex"].points = $$.sbgn.generateComplexShapePoints(cornerLength,
              width, height);

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = $$.math.polygonIntersectLine(
              x, y,
              nodeShapes["complex"].points,
              centerX,
              centerY,
              width / 2, height / 2,
              padding);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectionLines = [];
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectionLines = $$.math.polygonIntersectLine(
                x, y,
                nodeShapes["complex"].points,
                centerX + multimerPadding,
                centerY + multimerPadding,
                width / 2, height / 2,
                padding);
      }

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines, multimerIntersectionLines);

      return $$.sbgn.closestIntersectionPoint([x, y], intersections);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var cornerLength = nodeShapes["complex"].cornerLength;
      var multimerPadding = nodeShapes["complex"].multimerPadding;

      nodeShapes["complex"].points = $$.sbgn.generateComplexShapePoints(cornerLength,
              width, height);

      var points = nodeShapes["complex"].points;

      var nodeIntersectBox = $$.math.boxIntersectPolygon(
              x1, y1, x2, y2,
              points, width, height, centerX, centerY, [0, -1], padding);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectBox = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectBox = $$.math.boxIntersectPolygon(
                x1, y1, x2, y2, points, width, height,
                centerX + multimerPadding, centerY + multimerPadding,
                [0, -1], padding);
      }

      return nodeIntersectBox || stateAndInfoIntersectBox || multimerIntersectBox;
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width() + threshold;
      var height = node.height() + threshold;
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["complex"].multimerPadding;
      var cornerLength = nodeShapes["complex"].cornerLength;

      nodeShapes["complex"].points = $$.sbgn.generateComplexShapePoints(cornerLength,
              width, height);

      var nodeCheckPoint = $$.math.pointInsidePolygon(x, y, nodeShapes["complex"].points,
              centerX, centerY, width, height, [0, -1], padding);

      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      //check whether sbgn class includes multimer substring or not
      var multimerCheckPoint = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerCheckPoint = $$.math.pointInsidePolygon(x, y,
                nodeShapes["complex"].points,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height, [0, -1], padding);

      }

      return nodeCheckPoint || stateAndInfoCheckPoint || multimerCheckPoint;
    }
  };

  nodeShapes["macromolecule"] = {
    points: $$.math.generateUnitNgonPoints(4, 0),
    multimerPadding: 5,
    draw: function (context, node) {
      var width = node.width();
      var height = node.height();
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var label = node._private.data.sbgnlabel;
      var multimerPadding = nodeShapes["macromolecule"].multimerPadding;
      var cloneMarker = node._private.data.sbgnclonemarker;
      var padding = node._private.style["border-width"].pxValue;

      $$.sbgn.forceOpacityToOne(node, context);

      //check whether sbgn class includes multimer substring or not
      if ($$.sbgn.isMultimer(node)) {
        //add multimer shape
        renderer.drawRoundRectangle(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height);

        context.stroke();

        $$.sbgn.cloneMarker.macromolecule(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height, cloneMarker, true,
                node._private.style['background-opacity'].value);

        //context.stroke();
      }

      renderer.drawRoundRectangle(context,
              centerX, centerY,
              width, height);

      context.stroke();

      $$.sbgn.cloneMarker.macromolecule(context, centerX, centerY,
              width, height, cloneMarker, false,
              node._private.style['background-opacity'].value);

      $$.sbgn.drawStateAndInfos(node, context, centerX, centerY);

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["macromolecule"].multimerPadding;
      var cornerRadius = $$.math.getRoundRectangleRadius(width, height);

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = $$.sbgn.roundRectangleIntersectLine(
              x, y,
              centerX, centerY,
              centerX, centerY,
              width, height,
              cornerRadius, padding);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectionLines = [];
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectionLines = $$.sbgn.roundRectangleIntersectLine(
                x, y,
                centerX, centerY,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height,
                cornerRadius, padding);
      }

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines, multimerIntersectionLines);

      return $$.sbgn.closestIntersectionPoint([x, y], intersections);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["macromolecule"].multimerPadding;

      var nodeIntersectBox = $$.math.roundRectangleIntersectBox(
              x1, y1, x2, y2,
              width, height, centerX, centerY, padding);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectBox = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectBox = $$.math.roundRectangleIntersectBox(
                x1, y1, x2, y2, width, height, centerX + multimerPadding,
                centerY + multimerPadding, padding);
      }

      return nodeIntersectBox || stateAndInfoIntersectBox || multimerIntersectBox;
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width() + threshold;
      var height = node.height() + threshold;
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["macromolecule"].multimerPadding;

      var nodeCheckPoint = nodeShapes["roundrectangle"].checkPoint(x, y, padding,
              width, height, centerX, centerY);
      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      //check whether sbgn class includes multimer substring or not
      var multimerCheckPoint = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerCheckPoint = nodeShapes["roundrectangle"].checkPoint(x, y, padding,
                width, height, centerX + multimerPadding, centerY + multimerPadding);
      }

      return nodeCheckPoint || stateAndInfoCheckPoint || multimerCheckPoint;
    }
  };

  nodeShapes["nucleic acid feature"] = {
    points: $$.math.generateUnitNgonPointsFitToSquare(4, 0),
    multimerPadding: 5,
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var label = node._private.data.sbgnlabel;
      var cornerRadius = $$.math.getRoundRectangleRadius(width, height);
      var multimerPadding = nodeShapes["nucleic acid feature"].multimerPadding;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.forceOpacityToOne(node, context);

      //check whether sbgn class includes multimer substring or not
      if ($$.sbgn.isMultimer(node)) {
        //add multimer shape
        $$.sbgn.drawNucAcidFeature(context, width, height,
                centerX + multimerPadding,
                centerY + multimerPadding, cornerRadius);

        context.stroke();

        $$.sbgn.cloneMarker.nucleicAcidFeature(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width, height, cloneMarker, true,
                node._private.style['background-opacity'].value);

        //context.stroke();
      }

      $$.sbgn.drawNucAcidFeature(context, width, height, centerX,
              centerY, cornerRadius);

      context.stroke();

      $$.sbgn.cloneMarker.nucleicAcidFeature(context, centerX, centerY,
              width, height, cloneMarker, false,
              node._private.style['background-opacity'].value);

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);

      $$.sbgn.drawStateAndInfos(node, context, centerX, centerY);
    },
    drawPath: function (context, node) {

    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var multimerPadding = nodeShapes["nucleic acid feature"].multimerPadding;
      var width = node.width();
      var height = node.height();
      var cornerRadius = $$.math.getRoundRectangleRadius(width, height);

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = $$.sbgn.nucleicAcidIntersectionLine(node,
              x, y, centerX, centerY, cornerRadius);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectionLines = [];
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectionLines = $$.sbgn.nucleicAcidIntersectionLine(node,
                x, y, centerX + multimerPadding, centerY + multimerPadding,
                cornerRadius);
      }

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines,
              multimerIntersectionLines);

      return $$.sbgn.closestIntersectionPoint([x, y], intersections);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var multimerPadding = nodeShapes["nucleic acid feature"].multimerPadding;
      var cornerRadius = $$.math.getRoundRectangleRadius(width, height);

      var nodeIntersectBox = $$.sbgn.nucleicAcidIntersectionBox(
              x1, y1, x2, y2, centerX, centerY, node, this.points, cornerRadius);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectBox = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectBox = $$.sbgn.nucleicAcidIntersectionBox(
                x1, y1, x2, y2,
                centerX + multimerPadding, centerY + multimerPadding,
                node, this.points, this.cornerRadius);
      }

      return nodeIntersectBox || stateAndInfoIntersectBox || multimerIntersectBox;
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var multimerPadding = nodeShapes["nucleic acid feature"].multimerPadding;
      var width = node.width();
      var height = node.height();
      var cornerRadius = $$.math.getRoundRectangleRadius(width, height);

      var nodeCheckPoint = $$.sbgn.nucleicAcidCheckPoint(x, y, centerX, centerY,
              node, threshold, this.points, cornerRadius);
      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      //check whether sbgn class includes multimer substring or not
      var multimerCheckPoint = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerCheckPoint = $$.sbgn.nucleicAcidCheckPoint(x, y,
                centerX + multimerPadding, centerY + multimerPadding,
                node, threshold, this.points, cornerRadius);
      }

      return nodeCheckPoint || stateAndInfoCheckPoint || multimerCheckPoint;
    }
  };

  nodeShapes["perturbing agent"] = {
    points: [-2 / 3, 0, -1, 1, 1, 1, 2 / 3, 0,
      1, -1, -1, -1],
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var label = node._private.data.sbgnlabel;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.forceOpacityToOne(node, context);

      renderer.drawPolygon(context,
              centerX, centerY,
              width, height,
              nodeShapes["perturbing agent"].points);

      context.stroke();

      $$.sbgn.cloneMarker.perturbingAgent(context, centerX, centerY,
              width, height, cloneMarker,
              node._private.style['background-opacity'].value);

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);

      $$.sbgn.drawStateAndInfos(node, context, centerX, centerY);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = renderer.polygonIntersectLine(
              x, y,
              nodeShapes["perturbing agent"].points,
              centerX,
              centerY,
              width / 2, height / 2,
              padding);

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines);

      return $$.sbgn.closestIntersectionPoint([x, y], intersections);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var points = nodeShapes["perturbing agent"].points;
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var nodeIntersectBox = renderer.boxIntersectPolygon(x1, y1, x2, y2,
              points, width, height, centerX, centerY, [0, -1], padding);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      return nodeIntersectBox || stateAndInfoIntersectBox;

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var nodeCheckPoint = $$.math.pointInsidePolygon(x, y,
              nodeShapes["perturbing agent"].points,
              centerX, centerY, width, height, [0, -1], padding);

      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      return nodeCheckPoint || stateAndInfoCheckPoint;

    }
  };

  nodeShapes["simple chemical"] = {
    multimerPadding: 5,
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var multimerPadding = nodeShapes["simple chemical"].multimerPadding;
      var label = node._private.data.sbgnlabel;
      var padding = node._private.style["border-width"].pxValue;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.forceOpacityToOne(node, context);

      if ($$.sbgn.isMultimer(node)) {
        //add multimer shape
        $$.sbgn.drawSimpleChemical(context, centerX + multimerPadding,
                centerY + multimerPadding, width, height);

        context.stroke();

        $$.sbgn.cloneMarker.simpleChemical(context,
                centerX + multimerPadding, centerY + multimerPadding,
                width - padding, height - padding, cloneMarker, true,
                node._private.style['background-opacity'].value);

        //context.stroke();
      }

      $$.sbgn.drawSimpleChemical(context,
              centerX, centerY,
              width, height);

      context.stroke();

      $$.sbgn.cloneMarker.simpleChemical(context, centerX, centerY,
              width - padding, height - padding, cloneMarker, false,
              node._private.style['background-opacity'].value);

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);

      $$.sbgn.drawStateAndInfos(node, context, centerX, centerY);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["simple chemical"].multimerPadding;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = nodeShapes["ellipse"].intersectLine(
              centerX, centerY, width, height, x, y, padding);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectionLines = [];
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectionLines = nodeShapes["ellipse"].intersectLine(
                centerX + multimerPadding, centerY + multimerPadding, width,
                height, x, y, padding);
      }

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines, multimerIntersectionLines);

      return $$.sbgn.closestIntersectionPoint([x, y], intersections);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["simple chemical"].multimerPadding;

      var nodeIntersectBox = nodeShapes["roundrectangle"].intersectBox(
              x1, y1, x2, y2, width,
              height, centerX, centerY, padding);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      //check whether sbgn class includes multimer substring or not
      var multimerIntersectBox = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerIntersectBox = nodeShapes["ellipse"].intersectBox(
                x1, y1, x2, y2, width, height,
                centerX + multimerPadding, centerY + multimerPadding,
                padding);
      }

      return nodeIntersectBox || stateAndInfoIntersectBox || multimerIntersectBox;

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;
      var multimerPadding = nodeShapes["simple chemical"].multimerPadding;

      var nodeCheckPoint = nodeShapes["roundrectangle"].checkPoint(x, y,
              padding, width, height,
              centerX, centerY);

      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      //check whether sbgn class includes multimer substring or not
      var multimerCheckPoint = false;
      if ($$.sbgn.isMultimer(node)) {
        multimerCheckPoint = nodeShapes["ellipse"].checkPoint(x, y,
                padding, width, height,
                centerX + multimerPadding, centerY + multimerPadding);
      }

      return nodeCheckPoint || stateAndInfoCheckPoint || multimerCheckPoint;
    }
  };

  nodeShapes["source and sink"] = {
    points: $$.math.generateUnitNgonPoints(4, 0),
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var label = node._private.data.sbgnlabel;
      var pts = nodeShapes["source and sink"].points;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.drawEllipse(context, centerX, centerY,
              width, height);

      context.stroke();

      context.beginPath();
      context.translate(centerX, centerY);
      context.scale(width * Math.sqrt(2) / 2, height * Math.sqrt(2) / 2);

      context.moveTo(pts[2], pts[3]);
      context.lineTo(pts[6], pts[7]);
      context.closePath();

      context.scale(2 / (width * Math.sqrt(2)), 2 / (height * Math.sqrt(2)));
      context.translate(-centerX, -centerY);

      context.stroke();

      $$.sbgn.cloneMarker.sourceAndSink(context, centerX, centerY,
              width, height, cloneMarker,
              node._private.style['background-opacity'].value);

    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return nodeShapes["ellipse"].intersectLine(centerX, centerY, width,
              height, x, y, padding);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return nodeShapes["ellipse"].intersectBox(x1, y1, x2, y2, width, height,
              centerX, centerY, padding);

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return nodeShapes["ellipse"].checkPoint(x, y, padding, width,
              height, centerX, centerY)

    }
  };

  nodeShapes["tag"] = {
    points: [-1, -1, 1 / 3, -1, 1, 0, 1 / 3, 1, -1, 1],
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var label = node._private.data.sbgnlabel;

      renderer.drawPolygon(context,
              centerX, centerY,
              width, height,
              nodeShapes["tag"].points);

      context.stroke();

      var nodeProp = {'label': label, 'centerX': centerX - width / 6, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return renderer.polygonIntersectLine(
              x, y,
              nodeShapes["tag"].points,
              centerX,
              centerY,
              width / 2, height / 2,
              padding);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var points = nodeShapes["tag"].points;
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return renderer.boxIntersectPolygon(x1, y1, x2, y2,
              points, width, height, centerX, centerY, [0, -1], padding);

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return $$.math.pointInsidePolygon(x, y, nodeShapes["tag"].points,
              centerX, centerY, width, height, [0, -1], padding);

    }
  };

  nodeShapes["unspecified entity"] = {
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var sbgnClass = node._private.data.sbgnclass;
      var label = node._private.data.sbgnlabel;
      var cloneMarker = node._private.data.sbgnclonemarker;

      $$.sbgn.forceOpacityToOne(node, context);

      $$.sbgn.drawEllipse(context, centerX, centerY, width, height);

      context.stroke();

      $$.sbgn.cloneMarker.unspecifiedEntity(context, centerX, centerY,
              width, height, cloneMarker,
              node._private.style['background-opacity'].value);

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);

      $$.sbgn.drawStateAndInfos(node, context, centerX, centerY);

    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var stateAndInfoIntersectLines = $$.sbgn.intersectLineStateAndInfoBoxes(
              node, x, y);

      var nodeIntersectLines = nodeShapes["ellipse"].intersectLine(centerX, centerY, width,
              height, x, y, padding);

      var intersections = stateAndInfoIntersectLines.concat(nodeIntersectLines);
      return $$.sbgn.closestIntersectionPoint([x, y], intersections);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var nodeIntersectBox = nodeShapes["ellipse"].intersectBox(
              x1, y1, x2, y2, width,
              height, centerX, centerY, padding);

      var stateAndInfoIntersectBox = $$.sbgn.intersectBoxStateAndInfoBoxes(
              x1, y1, x2, y2, node);

      return nodeIntersectBox || stateAndInfoIntersectBox;

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var nodeCheckPoint = nodeShapes["ellipse"].checkPoint(x, y,
              padding, width, height,
              centerX, centerY);

      var stateAndInfoCheckPoint = $$.sbgn.checkPointStateAndInfoBoxes(x, y, node,
              threshold);

      return nodeCheckPoint || stateAndInfoCheckPoint;
    }
  };

  nodeShapes['and operator'] = {
    label: 'AND',
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();

      $$.sbgn.drawEllipse(context, centerX, centerY, width, height);

      context.stroke();

      var textProp = {'label': this.label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, textProp);

      $$.sbgn.drawPortsToEllipseShape(context, node);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return nodeShapes['ellipse'].intersectLine(centerX, centerY, width,
              height, x, y, padding);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;

      return nodeShapes['ellipse'].intersectLine(x1, y1, x2, y2, width,
              height, centerX, centerY, padding);
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;
      return nodeShapes['ellipse'].checkPoint(x, y, padding, width,
              height, centerX, centerY);
    }
  };

  nodeShapes["phenotype"] = {
    points: [-1, 0, -0.7, -1, 0.7, -1,
      1, 0, 0.7, 1, -0.7, 1],
    draw: function (context, node) {
      var width = node.width();
      var height = node.height();
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var label = node._private.data.sbgnlabel;

      renderer.drawPolygon(context,
              centerX, centerY,
              width, height,
              nodeShapes["phenotype"].points);

      context.stroke();

      var nodeProp = {'label': label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawDynamicLabelText(context, nodeProp);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return renderer.polygonIntersectLine(
              x, y,
              nodeShapes["phenotype"].points,
              centerX,
              centerY,
              width / 2, height / 2,
              padding);

    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var points = nodeShapes["phenotype"].points;
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return renderer.boxIntersectPolygon(x1, y1, x2, y2,
              points, width, height, centerX, centerY, [0, -1], padding);

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return $$.math.pointInsidePolygon(x, y, nodeShapes["phenotype"].points,
              centerX, centerY, width, height, [0, -1], padding);

    }
  };

  nodeShapes["dissociation"] = {
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      ;
      var width = node.width();
      var height = node.height();

      context.beginPath();
      context.translate(centerX, centerY);
      context.scale(width / 4, height / 4);

      // At origin, radius 1, 0 to 2pi
      context.arc(0, 0, 1, 0, Math.PI * 2 * 0.999, false); // *0.999 b/c chrome rendering bug on full circle

      context.closePath();
      context.scale(4 / width, 4 / height);
      context.translate(-centerX, -centerY);

      $$.sbgn.drawEllipsePath(context, centerX, centerY, width / 2, height / 2);

      context.stroke();

      $$.sbgn.drawEllipsePath(context, centerX, centerY, width, height);

      context.stroke();

      context.fill();

      $$.sbgn.drawPortsToEllipseShape(context, node);

    },
    drawPath: function (context, node) {

    },
    intersectLine: function (node, x, y, portId) {
      var nodeX = node._private.position.x;
      var nodeY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return $$.math.intersectLineEllipse(
              x, y,
              nodeX,
              nodeY,
              width / 2 + padding,
              height / 2 + padding);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return $$.math.boxIntersectEllipse(
              x1, y1, x2, y2, padding, width, height, centerX, centerY);

    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      x -= centerX;
      y -= centerY;

      x /= (width / 2 + padding);
      y /= (height / 2 + padding);

      return (Math.pow(x, 2) + Math.pow(y, 2) <= 1);
    }
  };

  nodeShapes['association'] = {
    draw: function (context, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue;

      var oldStyle = context.fillStyle;
      var oldAlpha = context.globalAlpha;

      context.fillStyle = $$.sbgn.colors.association;
      context.globalAlpha = node._private.style['background-opacity'].value;

      nodeShapes['ellipse'].draw(context, centerX, centerY, width, height);

      context.fillStyle = oldStyle;
      context.globalAlpha = oldAlpha;

      nodeShapes['ellipse'].drawPath(context, centerX, centerY, width, height);
      context.stroke();

      $$.sbgn.drawPortsToEllipseShape(context, node);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      var intersect = $$.math.intersectLineEllipse(
              x, y,
              centerX,
              centerY,
              width / 2 + padding,
              height / 2 + padding);

      return intersect;
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      return $$.math.boxIntersectEllipse(
              x1, y1, x2, y2, padding, width, height, centerX, centerY);
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style["border-width"].pxValue / 2;

      x -= centerX;
      y -= centerY;

      x /= (width / 2 + padding);
      y /= (height / 2 + padding);

      return (Math.pow(x, 2) + Math.pow(y, 2) <= 1);
    }
  };

  nodeShapes['process'] = {
    points: $$.math.generateUnitNgonPointsFitToSquare(4, 0),
    label: '',
    draw: function (context, node) {
      var width = node.width();
      var height = node.height();
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var padding = node._private.style['border-width'].pxValue / 2;

      renderer.drawPolygon(context,
              centerX, centerY,
              width, height,
              nodeShapes['process'].points);

      context.stroke();

      var textProp = {'label': this.label, 'centerX': centerX, 'centerY': centerY,
        'opacity': node._private.style['text-opacity'].value, 'width': node.width(), 'height': node.height()};
      $$.sbgn.drawLabelText(context, textProp);

      $$.sbgn.drawPortsToPolygonShape(context, node, this.points);
    },
    drawPath: function (context, node) {
    },
    intersectLine: function (node, x, y, portId) {
      var nodeX = node._private.position.x;
      var nodeY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;

      var portIntersection = $$.sbgn.intersectLinePorts(node, x, y, portId);
      if (portIntersection.length > 0) {
        return portIntersection;
      }

      return $$.math.polygonIntersectLine(
              x, y,
              nodeShapes['process'].points,
              nodeX,
              nodeY,
              width / 2, height / 2,
              padding);
    },
    intersectBox: function (x1, y1, x2, y2, node) {
      var points = nodeShapes['process'].points;
      var nodeX = node._private.position.x;
      var nodeY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;

      return $$.math.boxIntersectPolygon(x1, y1, x2, y2,
              points, width, height, nodeX, nodeY, [0, -1], padding);
    },
    checkPoint: function (x, y, node, threshold) {
      var centerX = node._private.position.x;
      var centerY = node._private.position.y;
      var width = node.width();
      var height = node.height();
      var padding = node._private.style['border-width'].pxValue / 2;

      return $$.math.pointInsidePolygon(x, y, nodeShapes['process'].points,
              centerX, centerY, width, height, [0, -1], padding);
    }
  };

  nodeShapes['omitted process'] = jQuery.extend(true, {}, nodeShapes['process']);
  nodeShapes['omitted process'].label = '\\\\';

  nodeShapes['uncertain process'] = jQuery.extend(true, {}, nodeShapes['process']);
  nodeShapes['uncertain process'].label = '?';

  nodeShapes['or operator'] = jQuery.extend(true, {}, nodeShapes['and operator']);
  nodeShapes['or operator'].label = 'OR';

  nodeShapes['not operator'] = jQuery.extend(true, {}, nodeShapes['and operator']);
  nodeShapes['not operator'].label = 'NOT';

  nodeShapes['and'] = nodeShapes['and operator'];
  nodeShapes['or'] = nodeShapes['or operator'];
  nodeShapes['not'] = nodeShapes['not operator'];

  nodeShapes['compartment'] = nodeShapes['roundrectangle'];
  nodeShapes['submap'] = nodeShapes['roundrectangle'];
  nodeShapes['terminal'] = nodeShapes['ellipse'];

  //adding multimer shapes
  nodeShapes['macromolecule multimer'] = nodeShapes['macromolecule'];
  nodeShapes['nucleic acid feature multimer'] = nodeShapes['nucleic acid feature'];
  nodeShapes['simple chemical multimer'] = nodeShapes['simple chemical'];
  nodeShapes['complex multimer'] = nodeShapes['complex'];


})(cytoscape);

/*
 * Those are the sbgn edge objects
 */
;
(function ($$) {
  'use strict';
  var CanvasRenderer = $$('renderer', 'canvas');
  var renderer = CanvasRenderer.prototype;

  //default edge shapes are in arrowShape array,
  //all different types must be added
  var arrowShape = $$.style.types.arrowShape.enums;
  //add each sbgn node type to cytoscape.js
  arrowShape.push('consumption', 'production', 'modulation', 'stimulation',
          'catalysis', 'inhibition', 'necessary stimulation', 'logic arc',
          'equivalence arc');

  var arrowShapes = CanvasRenderer.arrowShapes;

  var bbCollide = function (x, y, centerX, centerY, width, height, direction, padding) {
    var x1 = centerX - width / 2;
    var x2 = centerX + width / 2;
    var y1 = centerY - height / 2;
    var y2 = centerY + height / 2;

    return (x1 <= x && x <= x2) && (y1 <= y && y <= y2);
  };

  var transform = function (x, y, size, angle, translation) {
    angle = -angle; // b/c of notation used in arrow draw fn

    var xRotated = x * Math.cos(angle) - y * Math.sin(angle);
    var yRotated = x * Math.sin(angle) + y * Math.cos(angle);

    var xScaled = xRotated * size;
    var yScaled = yRotated * size;

    var xTranslated = xScaled + translation.x;
    var yTranslated = yScaled + translation.y;

    return {
      x: xTranslated,
      y: yTranslated
    };
  };

  arrowShapes['necessary stimulation'] = {
    trianglePoints: [
      -0.15, -0.3,
      0, 0,
      0.15, -0.3
    ],
    linePoints: [
      -0.15, -0.37,
      0.15, -0.37
    ],
    collide: function (x, y, centerX, centerY, width, height, direction, padding) {
      var points = arrowShapes['necessary stimulation'].trianglePoints;

      return $$.math.pointInsidePolygon(
              x, y, points, centerX, centerY, width, height, direction, padding);
    },
    roughCollide: bbCollide,
    draw: function (context, size, angle, translation) {
      var points = arrowShapes['necessary stimulation'].trianglePoints;
      var linePoints = arrowShapes['necessary stimulation'].linePoints;

      for (var i = 0; i < points.length / 2; i++) {
        var pt = transform(points[i * 2], points[i * 2 + 1], size, angle, translation);

        context.lineTo(pt.x, pt.y);
      }
      context.closePath();

      var pt1 = transform(linePoints[0], linePoints[1], size, angle, translation);
      var pt2 = transform(linePoints[2], linePoints[3], size, angle, translation);

      context.moveTo(pt1.x, pt1.y);
      context.lineTo(pt2.x, pt2.y);

    },
    spacing: function (edge) {
      return 0;
    },
    gap: function (edge) {
      return edge._private.style['width'].pxValue * 2;
    }
  };

  arrowShapes['consumption'] = arrowShapes['none'];

  arrowShapes['production'] = arrowShapes['triangle'];

  arrowShapes['modulation'] = arrowShapes['diamond'];

  arrowShapes['stimulation'] = arrowShapes['triangle'];

  arrowShapes['catalysis'] = arrowShapes['circle'];

  arrowShapes['inhibition'] = arrowShapes['tee'];

  arrowShapes['logic arc'] = arrowShapes['none'];

  arrowShapes['equivalence arc'] = arrowShapes['none'];

})(cytoscape);

//converter functions
