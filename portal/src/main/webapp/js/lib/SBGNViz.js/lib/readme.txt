This file indicates the altered parts in used libraries and explains why they are changed.

-Libary name: cytoscape-edgehandles.js
The code between line numbers 415 and 479 are commented out.
(starting from the line "for( var i = 0; i < targets.length; i++ ) {")

The reason of this change was disabling user with creating edges with multiple targets at a time.

Line number 581 is commented out.
(line is containing the following content "drawHandle( hx, hy, hr );")

The reason of this change was preventing that the red handle drawn on every hover on a node.

-Library name: cytoscape-noderesize.js
Changes are done inside moveHandler function defined as "function moveHandler(e)"

The reason of this changes was disabling user from changing the sizes of nodes with some special types arbitrarily.

clearDraws function globally named as clearDrawsOfNodeResize.

The reason of this change was to call "clearDraws" function when it is needed outside of the library.
In this case, it is needed to clear the handle on hide.

-Library name: cytoscape.js
In checkNode function inside findNearestElement
"if(
    pos.x - hw <= x && x <= pos.x + hw // bb check x
      &&
    pos.y - hh <= y && y <= pos.y + hh // bb check y
  ){"

is commented out.

In checkNode function inside findNearestElement, and BRp.findEdgeControlPoints shape.checkPoint and similar function calls are
replaced. An example replaced statement is "cytoscape.sbgn.totallyOverridenNodeShapes(self, node)?shape.checkPoint( x, y, node, 0 ):shape.checkPoint(x, y, 0, width, height, pos.x, pos.y)"

In BRp.getNodeShape function
"if( node.isParent() ){
    if( shape === 'rectangle' || shape === 'roundrectangle' ){
      return shape;
    } else {
      return 'rectangle';
    }
  }" is commented out.

This is done not to restrict the shapes of the parent nodes.

IntersectLine function calls are conditinally replaced(If the shape of that node is totally
overriden).

An example call is here.
"if(cytoscape.sbgn.totallyOverridenNodeShapes(this, src))
        srcOutside = srcShape.intersectLine(src, tgtPos.x, tgtPos.y, edge._private.data.porttarget);
//        srcOutside = cytoscape.sbgn.intersetLineSelection(this, src, tgtPos.x, tgtPos.y, edge._private.data.portsource );
      else
        srcOutside = srcShape.intersectLine(
          srcPos.x,
          srcPos.y,
          srcW,
          srcH,
          tgtPos.x,
          tgtPos.y,
          0
        );"

In BRp.registerNodeShapes function 
"var nodeShapes = this.nodeShapes = {}" => "var nodeShapes = this.nodeShapes = window.cyNodeShapes"

Added "cytoscape.sbgn.registerSbgnNodeShapes();" statement to BRp.registerNodeShapes function.

Change in call of draw function of nodeshapes.
An example call is 
"if(cytoscape.sbgnShapes[this.getNodeShape(node)]){
            r.nodeShapes[this.getNodeShape(node)].draw(
                context,
                node);
        }
        else{
            r.nodeShapes[r.getNodeShape(node)].draw(
              context,
              nodeX, nodeY,
              nodeW, nodeH);
        }"

 Added "cytoscape.sbgn.drawExpandCollapseBoxes(node, context);" statement to the end of drawNode function.

Before the definition of CRp.drawPolygonPath function,
"var CRp = {};" => "var CRp = window.cyRenderer;"

CRp.usePaths function returns false.

"var math = {};" => "var math = window.cyMath;"

"var styfn = {};" => "var styfn = window.cyStyfn;"