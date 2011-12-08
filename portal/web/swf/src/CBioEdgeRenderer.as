/*
  This file is part of Cytoscape Web.
  Copyright (c) 2009, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
    - Agilent Technologies
    - Institut Pasteur
    - Institute for Systems Biology
    - Memorial Sloan-Kettering Cancer Center
    - National Center for Integrative Biomedical Informatics
    - Unilever
    - University of California San Diego
    - University of California San Francisco
    - University of Toronto

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package org.cytoscapeweb.view.render {
	import com.senocular.drawing.DashedLine;
	
	import flare.util.Geometry;
	import flare.util.Shapes;
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import org.cytoscapeweb.util.ArrowShapes;
	import org.cytoscapeweb.util.GraphUtils;
	import org.cytoscapeweb.util.LineStyles;
	import org.cytoscapeweb.util.NodeShapes;
	import org.cytoscapeweb.util.Utils;
	
	public class CBioEdgeRenderer extends EdgeRenderer {

        // ========[ CONSTANTS ]====================================================================

        // ========[ PRIVATE PROPERTIES ]===========================================================

		private static var _instance:CBioEdgeRenderer = new CBioEdgeRenderer();
        
		/** Static AppEdgeRenderer instance. */
        public static function get instance():CBioEdgeRenderer
		{
			return _instance;
		}
        
        // ========[ PUBLIC METHODS ]===============================================================

        /** @inheritDoc */
        public override function render(d:DataSprite):void {
            var e:EdgeSprite = d as EdgeSprite;
            if (e == null || e.source == null || e.target == null) { return; }
            var g:Graphics = e.graphics;
            
            // No need to continue if the edge is totally transparent:
            if (e.lineWidth === 0 || e.lineAlpha === 0 || e.alpha === 0) {
                g.clear();
                return;
            }

            var s:NodeSprite = e.source;
            var t:NodeSprite = e.target;
            var loop:Boolean = t === s;

            // This renderer ignores the control points!
            // var ctrls:Array = e.points as Array;
            
            var x1:Number = e.x1, y1:Number = e.y1;
            var x2:Number = e.x2, y2:Number = e.y2;
            var np1:Point = new Point(x1, y1);
            var np2:Point = new Point(x2, y2);
            var nd:Number = Point.distance(np1, np2);
            var w:Number = e.lineWidth;
            var curve:Boolean = e.shape === Shapes.BEZIER || e.shape === Shapes.BSPLINE || e.shape === Shapes.CARDINAL;
            
            // Edge intersection points (with target and souce nodes):
            var _intT:Point = new Point(), _intS:Point = new Point();
            _intS.x = x1; _intS.y = y1;
            _intT.x = x2; _intT.y = y2;
            
            // Get arrow styles:
            var sourceShape:String = e.props.sourceArrowShape;
            var targetShape:String = e.props.targetArrowShape;
            var targetArrowColor:uint = e.props.targetArrowColor;
            var sourceArrowColor:uint = e.props.sourceArrowColor;
            
            var sourceArrowStyle:Object, targetArrowStyle:Object;
            
            // Check special conditions for merged edges
            
            var edgeType:String = null;
			var sameType:Boolean = true;
			var direction:Object = new Object();
			var lastEdge:EdgeSprite = null;
            
            if (e.props.$merged)
			{
				// visit all edges of the source node of the merged edge and
				// find all edges between source and target nodes 
				e.source.visitEdges(
					function(edge:EdgeSprite):Boolean
					{
						var endVisit:Boolean = false;
						
						if (edge == e ||
							GraphUtils.isFilteredOut(edge))
						{
							// skip the merged edge & filtered edges
							endVisit = false;
						}
						// if the target or source of the current edge is
						// the target edge of the merged edge, then the
						// current edge is between e.source and e.target
						else if (edge.target == e.target ||
							edge.source == e.target)
						{
							// update last edge
							lastEdge = edge;
							
							// first edge, so set the type & direction directly
							if (edgeType == null)
							{
								edgeType = edge.data.type;
								
								direction.same = true;
								
								if (edge.target == e.target)
								{
									direction.source = "source";
								}
								else
								{
									direction.source = "target";
								}
							}
							// different type found
							else if (edgeType != edge.data.type)
							{
								sameType = false;
								
								// end visitation with an early exit
								endVisit = true;
							}
							
							// check for same edge direction
							if (direction.same)
							{
								if (direction.source == "source")
								{
									if (edge.target != e.target)
									{
										direction.same = false;
									}	
								}
								else
								{
									if (edge.target != e.source)
									{
										direction.same = false;
									}
								}
							}
						}
						
						return endVisit;
					},
					NodeSprite.OUT_LINKS);
				
				// if all edges of same type, and their direction are all same,
				// then draw the arrow shape for the merged edge
				if (sameType &&
					(lastEdge != null)
					&& direction.same)
				{
					// set arrow shape & color
					sourceShape = lastEdge.props.sourceArrowShape;
					targetShape = lastEdge.props.targetArrowShape;
					targetArrowColor = lastEdge.props.targetArrowColor;
					sourceArrowColor = lastEdge.props.sourceArrowColor;
				}
			}
            
            if (targetShape != ArrowShapes.NONE)
                targetArrowStyle = ArrowShapes.getArrowStyle(e, targetShape, targetArrowColor);
            if (sourceShape != ArrowShapes.NONE)
                sourceArrowStyle = ArrowShapes.getArrowStyle(e, sourceShape, sourceArrowColor);
            
            // Curvature
            var op1:Point, op2:Point;
            
			if (curve) {
			    var h:Number = e.props.curvature;
			    var saH:Number = sourceArrowStyle != null ? sourceArrowStyle.height : 0;
                var taH:Number = targetArrowStyle != null ? targetArrowStyle.height : 0;
			    
			    if (loop) {
			        h += Math.max(s.width, saH*2, taH*2, w*4);
			        op1 = new Point(s.x, s.y - s.height/2 - h);
			        op2 = new Point(s.x - s.width/2 - h, s.y);
			    } else {
    			    // Fix curvature height
    			    if (sourceShape != ArrowShapes.NONE || targetShape != ArrowShapes.NONE) {
        			    var maxH:Number = Math.max(saH, taH, s.width/2, t.width/2);
        
                        if (maxH >= h) {
                            var nbd:Number = nd - s.width/2 - t.width/2; // distance between nodes borders
            			    h += 2 * Math.sqrt(Math.max(0, maxH*maxH - Math.pow(nd/4, 2))) * (h/Math.abs(h));
                        }
                    }
                    
    			    // Find bezier control point
    			    op2 = op1 = Utils.orthogonalPoint(h, np1, np2);
    			}
			} else {
			    op1 = np1.clone();
			    op2 = np2.clone();
			}
			
            // ----------------------------------------------------

            // get arrow tip point as intersection of edge with bounding box
            intersectNode(s, op2, np1, _intS);
            intersectNode(t, op1, np2, _intT);

            var start:Point = _intS, end:Point = _intT;
            //var c:Point = (curve ? op1 : null);
        
            // Using a bit mask to avoid transparent edges when fillcolor=0xffffffff.
            // See https://sourceforge.net/forum/message.php?msg_id=7393265
            var color:uint =  0xffffff & e.lineColor;
        
        	// if all edges are of the same type, so use a specific color
        	// of that type instead of color of the merged edge
			if (sameType &&
				(lastEdge != null))
			{
				color =  0xffffff & lastEdge.lineColor;
			}
					
            // Start/end points of the line (without arrows):
            var sShaft:Point = start.clone(), eShaft:Point = end.clone();

            // Total length of the edge:
            var vector:Number = Point.distance(start, end);
            // Vector from curve (control point) to start/end:
            var slopeVector:Number;
            // Arrow height:
            var ah:Number;

            if (sourceArrowStyle != null) {
                ah = sourceArrowStyle.height + sourceArrowStyle.gap;
                // The arrow should follow the curve slope:
                if (e.shape != Shapes.LINE) {
                    slopeVector = Point.distance(op2, sShaft);
                    sShaft = Point.interpolate(op2, start, ah/slopeVector);
                } else {
                    sShaft = Point.interpolate(end, start, ah/vector);
                }
            }
            if (targetArrowStyle != null) {
                ah = targetArrowStyle.height + targetArrowStyle.gap;
                // The arrow should follow the curve slope:
                if (e.shape != Shapes.LINE) {
                    slopeVector = Point.distance(op1, end);
                    eShaft = Point.interpolate(op1, end, ah/slopeVector);
                } else {
                    eShaft = Point.interpolate(start, end, ah/vector);
                }
            }

            // Draw the line of the edge:
            // ---------------------------
            var lineStyle:String = e.props.lineStyle;
            var solid:Boolean = lineStyle === LineStyles.SOLID;
            var dashedLine:DashedLine;
            
            g.clear();
            
            if (solid) {
                g.lineStyle(w, color, 1, pixelHinting, scaleMode, caps, joints, miterLimit);
                g.moveTo(sShaft.x, sShaft.y);
            } else {
                var onLength:Number = LineStyles.getOnLength(e, lineStyle);
                var offLength:Number = LineStyles.getOffLength(e, lineStyle);
                
                dashedLine = new DashedLine(e, onLength, offLength);
                dashedLine.lineStyle(w, color, 1);
                dashedLine.moveTo(sShaft.x, sShaft.y);
                
                var newCaps:String = LineStyles.getCaps(lineStyle);
                g.lineStyle(w, color, 1, pixelHinting, scaleMode, newCaps, joints, miterLimit);
            }
            
            if (loop) {
                if (solid) {
                    Shapes.drawCubic(g, sShaft.x, sShaft.y, op2.x, op2.y, op1.x, op1.y, eShaft.x, eShaft.y, false);
                } else {
                    // We cannot draw a cubic bezier here, so let's just split it in 2 quadratic curves...
                    // First, find the middle point of the original cubic curve:
                    var m:Point = Utils.cubicBezierPoint(sShaft, op2, op1, eShaft, 0.5);
                    var cp1:Point = Utils.cubicBezierPoint(sShaft, op2, op1, eShaft, 0.25);
                    var cp2:Point = Utils.cubicBezierPoint(sShaft, op2, op1, eShaft, 0.75);
                    
                    // Find the middle point of a segment that goes from the edge's start point the edge's middle point:
                    var ms:Point = Point.interpolate(m, sShaft, 0.5);
                    // Find the middle point of a segment that goes from the edge's end point the edge's middle point:
                    var me:Point = Point.interpolate(m, eShaft, 0.5);

                    // Move the original contol point--just an aproximation:
                    cp1 = Utils.lerp(ms, cp1, 1.9);
                    cp2 = Utils.lerp(me, cp2, 1.9);
                    
                    // Draw 2 quadratic bezier curves:
                    dashedLine.curveTo(cp1.x, cp1.y, m.x, m.y);
                    dashedLine.curveTo(cp2.x, cp2.y, eShaft.x, eShaft.y);
                }
            } else {
                if (e.shape != Shapes.LINE) {
                    if (solid) {
                        if (nd > 5*w) {
                            // Nodes are not too close...
                            g.curveTo(op1.x, op1.y, eShaft.x, eShaft.y);
                        } else {
                            // Flash has a knowm problem with cubic beziers, which can create artifacts.
                            // Let's try to avoid it by using a quadratic bezier, instead:
                            var c1:Point = new Point(), c2:Point = new Point();
                            Utils.quadraticToCubic(sShaft, op1, eShaft, c1, c2);
                            Shapes.drawCubic(g, sShaft.x, sShaft.y, c1.x, c1.y, c2.x, c2.y, eShaft.x, eShaft.y, false);
                        }
                    } else {
                        dashedLine.curveTo(op1.x, op1.y, eShaft.x, eShaft.y);
                    }
                } else {
                    if (solid) g.lineTo(eShaft.x, eShaft.y);
                    else       dashedLine.lineTo(eShaft.x, eShaft.y);
                }
            }

            g.endFill();

            // ARROWS:
            // ---------------------------
            var ds:Point = op1 == null ? end.subtract(start) : op1.subtract(start);
            var de:Point = op1 == null ? start.subtract(end) : op1.subtract(end);
            var ns:Point = new Point(ds.y, -ds.x);
            ns.normalize(w/2);
            var ne:Point = new Point(-de.y, de.x);
            ne.normalize(w/2);
            
            var sShaft1:Point = sShaft.add(ns);
            var sShaft2:Point = sShaft.subtract(ns);
            var eShaft1:Point = eShaft.add(ne);
            var eShaft2:Point = eShaft.subtract(ne);

            // Draw the source arrow:
            var saPoints:Object;
            if (sourceArrowStyle != null)
                saPoints = drawArrow(g, sShaft, start, sShaft2, sShaft1,
                                     { lineWidth: e.lineWidth, color: color, alpha: 1 }, // TODO: remove this object
                                     sourceArrowStyle);
            
            // Draw the target arrow:
            var taPoints:Object;
            if (targetArrowStyle != null)
                taPoints = drawArrow(g, eShaft, end, eShaft1, eShaft2,
                                     { lineWidth: e.lineWidth, color: color, alpha: 1 }, // TODO: remove this object
                                     targetArrowStyle);
            
            // Store the draw points for future use:
            // ------------------------------------------
            var points:Object = new Object();
            points.start = sShaft.clone();
            points.end = eShaft.clone();
            points.c1 = e.shape != Shapes.LINE && op1 != null ? op1.clone() : null; // First control point of cubic bezier OR control point of quadratic bezier
            points.c2 = e.shape != Shapes.LINE && op2 != null && op2 != op1 ? op2.clone() : null; // Second control point of cubic bezier
            points.sourceArrow = saPoints != null ? saPoints.arrow : null;
            points.targetArrow = taPoints != null ? taPoints.arrow : null;
            points.sourceArrowJoint = saPoints != null ? saPoints.joint : null;
            points.targetArrowJoint = taPoints != null ? taPoints.joint : null;
                
            // Store the draw points for future use (e.g. PDF generation):
            e.props.$points = points;
        }
        
        // ========[ PRIVATE METHODS ]==============================================================
        
        private function drawArrow(g:Graphics, start:Point, end:Point, eb1:Point, eb2:Point,
                                   edgeStyle:Object, arrowStyle:Object):Object {
            // Returned reference points:
            var points:Object = new Object();

            var aw:Number = arrowStyle.width/2;
            var ah:Number = arrowStyle.height;
            var shape:String = arrowStyle.shape;
            
            end = Point.interpolate(start, end, arrowStyle.gap / Point.distance(start, end));
            
            // Find perpendicular vector points for the arrow base:
            var n1:Point = new Point(start.x-end.x, start.y-end.y);
            var n2:Point = n1.clone();
            n1.normalize(1);
            n2.normalize(-1);
            var b1:Point = new Point(n1.y, -n1.x);
            var b2:Point = new Point(n2.y, -n2.x);
            b1 = new Point(start.x + b1.x * aw, start.y + b1.y * aw);
            b2 = new Point(start.x + b2.x * aw, start.y + b2.y * aw);

            g.lineStyle(0, 0x000000, 0);
            g.beginFill(arrowStyle.color, arrowStyle.alpha);
            
            switch (shape) {
                case ArrowShapes.T:
                    // Find the other points of the rectangle:
                    var d:Point = b2.subtract(b1);
                    var n:Point = new Point(-d.y, d.x);
                    n.normalize(ah);
                    var b3:Point = b2.add(n);
                    var b4:Point = b1.add(n);
                    g.moveTo(b1.x, b1.y);
                    g.lineTo(b2.x, b2.y);
                    g.lineTo(b3.x, b3.y);
                    g.lineTo(b4.x, b4.y);
                    g.lineTo(b1.x, b1.y);
                    g.endFill();
                    // Future reference points:
                    points.arrow = [b1.clone(), b2.clone(), b3.clone(), b4.clone()];
                    break;
                case ArrowShapes.CIRCLE:
                    var center:Point = Point.interpolate(start, end, 0.5);
                    g.drawCircle(center.x, center.y, ah/2);
                    g.endFill();
                    // Draw the junction between the arrow and the edge line:
                    points.joint = drawCircleArrowJoint(g, start, end, eb1, eb2, center, edgeStyle, arrowStyle);
                    // Future reference points:
                    points.arrow = [center.clone()];
                    break;
                case ArrowShapes.DIAMOND:
                    b1 = Point.interpolate(b1, end, 0.5);
                    b2 = Point.interpolate(b2, end, 0.5);
                    g.moveTo(start.x, start.y);
                    g.lineTo(b1.x, b1.y);
                    g.lineTo(end.x, end.y);
                    g.lineTo(b2.x, b2.y);
                    g.lineTo(start.x, start.y);
                    g.endFill();
                    // Future reference points:
                    points.arrow = [start.clone(), b1.clone(), end.clone(), b2.clone()];
                    // Draw the junction between the arrow and the edge line:
                    points.joint = drawDiamondArrowJoint(g, start, end, b1, b2, eb1, eb2, edgeStyle);
                    break;
                case ArrowShapes.ARROW:
                    // Control points for curved sides:
                    var h:Number = Math.max(1, arrowStyle.height/10);
                    var c1:Point = Utils.orthogonalPoint(h, end, b1);
                    var c2:Point = Utils.orthogonalPoint(h, b2, end);
                    g.moveTo(end.x, end.y);
                    g.curveTo(c1.x, c1.y, b1.x, b1.y);
                    g.lineTo(b2.x, b2.y);
                    g.curveTo(c2.x, c2.y, end.x, end.y);
                    g.endFill();
                    // Future reference points:
                    points.arrow = [end.clone(), c1.clone(), b1.clone(), b2.clone(), c2.clone()];
                    break;
                case ArrowShapes.DELTA:
                default:
                    g.moveTo(end.x, end.y);
                    g.lineTo(b1.x, b1.y);
                    g.lineTo(b2.x, b2.y);
                    g.lineTo(end.x, end.y);
                    g.endFill();
                    // Future reference points:
                    points.arrow = [end.clone(), b1.clone(), b2.clone()];
                    break;

            }

            return points;
        }

        private function drawCircleArrowJoint(g:Graphics, start:Point, end:Point,
                                              b1:Point, b2:Point, center:Point,
                                              edgeStyle:Object, arrowStyle:Object):Array {
            // Shaft width:
            var w:Number = edgeStyle.lineWidth;
            var ww:Number = w/2;
            // Circle radius:
            var r:Number = arrowStyle.height/2;
            // Find distance between the center of the circle and the imaginary line done by
            // the intersection points between the arrow joint and the circle (Pitagoras):
            var h:Number = Math.sqrt(r*r - ww*ww);
            // Another point distanced 2*h from the center:
            var p:Point = Point.interpolate(start, center, 2*h/r);

            // Get the points where the shaft should hit the base of the diamond shape:
            var int1:Point = Utils.orthogonalPoint(ww, center, p);
            var int2:Point = Utils.orthogonalPoint(-ww, center, p);
            // Bezier control point:
            var ctrl:Point = Utils.orthogonalPoint((r-h)*2, int1, int2);

            g.lineStyle(0, 0x000000, 0);
            g.beginFill(edgeStyle.color, edgeStyle.alpha);
            g.moveTo(int1.x, int1.y);
            g.lineTo(b1.x, b1.y);
            g.lineTo(b2.x, b2.y);
            g.lineTo(int2.x, int2.y);
            g.curveTo(ctrl.x, ctrl.y, int1.x, int1.y);
            g.endFill();
            
            return [int1.clone(), b1.clone(), b2.clone(), int2.clone(), ctrl.clone()];
        }
        
        private function drawDiamondArrowJoint(g:Graphics, start:Point, end:Point,
                                               b1:Point, b2:Point, e1:Point, e2:Point,
                                               edgeStyle:Object):Array {
            // Shaft width:
            var w:Number = edgeStyle.lineWidth/2;
            // Get the points where the shaft should hit the base of the diamond shape:
            var ee1:Point = Utils.orthogonalPoint(w, start, end);
            var ee2:Point = Utils.orthogonalPoint(-w, start, end);

            g.lineStyle(0, 0x000000, 0);
            g.beginFill(edgeStyle.color, edgeStyle.alpha);
            g.moveTo(start.x, start.y);

            var int1:Point = new Point(), int2:Point = new Point();
            
            if (Geometry.intersectLines(e1.x, e1.y, ee1.x, ee1.y, start.x, start.y, b1.x, b1.y, int1) > 0)
                g.lineTo(int1.x, int1.y);
            else if (Geometry.intersectLines(e1.x, e1.y, ee2.x, ee2.y, start.x, start.y, b2.x, b2.y, int1) > 0)
                g.lineTo(int1.x, int1.y);
            g.lineTo(e1.x, e1.y);
            g.lineTo(e2.x, e2.y);
  
            if (Geometry.intersectLines(e2.x, e2.y, ee2.x, ee2.y, start.x, start.y, b2.x, b2.y, int2) > 0)
                g.lineTo(int2.x, int2.y);
            else if (Geometry.intersectLines(e2.x, e2.y, ee1.x, ee1.y, start.x, start.y, b1.x, b1.y, int2) > 0)
                g.lineTo(int2.x, int2.y);

            g.lineTo(start.x, start.y);
            g.endFill();
            
            return [start.clone(), int1.clone(), e1.clone(), e2.clone(), int2.clone()];
        }

        private function intersectNode(n:NodeSprite, start:Point, end:Point, int:Point):void {
        	var r:Rectangle = n.getBounds(n.parent);
        	
        	switch (n.shape) {
                case NodeShapes.ELLIPSE:
                    intersectCircle(n.height/2, start, end, int);
                    break;
                case NodeShapes.ROUND_RECTANGLE:
                    intersectRoundRectangle(r, start, end, int);
                    break;
                default:
                    var points:Array = NodeShapes.getDrawPoints(r, n.shape);
                    intersectLines(points, start, end, int);
        	}
        }
        
        private function intersectCircle(radius:Number, start:Point, end:Point, ip:Point):void {
            var obj:Object = Utils.lineIntersectCircle(start, end, end, radius);
            if (obj.enter != null) {
                ip.x = obj.enter.x;
                ip.y = obj.enter.y;
            }
        }
        
        private function intersectRoundRectangle(r:Rectangle, start:Point, end:Point, ip:Point):void {
            var points:Array = NodeShapes.getDrawPoints(r, NodeShapes.ROUND_RECTANGLE);
            var res:int = Geometry.NO_INTERSECTION;
            var length:int = points.length;
            
            for (var i:int = 0; i < length; i += 4) {
                if (i+3 >= length) break;
                var x1:Number = points[i], y1:Number = points[i+1];
                var x2:Number = points[i+2], y2:Number = points[i+3];

                res = Geometry.intersectLines(x1, y1, x2, y2, start.x, start.y, end.x, end.y, ip);
                if (res > 0) break;
            }
            if (res <= 0) {
                // Verify if the edge intersects one of the rounded courners,
                // which are described by four circles.
                var radius:Number = r.width/4;
                // Calculate the center of the circles:
                var xR:Number = r.right, yB:Number = r.bottom;
                var xL:Number = r.left,  yT:Number = r.top;
                points = [ new Point(xL+radius, yT+radius),
                           new Point(xR-radius, yT+radius),
                           new Point(xR-radius, yB-radius),
                           new Point(xL+radius, yB-radius) ];
                
                for each (var c:Point in points) {
                    var obj:Object = Utils.lineIntersectCircle(start, end, c, radius);
                    if (obj.enter != null) {
                        ip.x = obj.enter.x;
                        ip.y = obj.enter.y;
                        break;
                    }
                }
            }
        }
        
        private function intersectLines(points:Array, start:Point, end:Point, ip:Point):int {
            var res:int = Geometry.NO_INTERSECTION;
            var length:int = points.length;
            
            for (var i:int = 0; i < length; i += 2) {
                if (i >= length) break;
                var x1:Number = points[i], y1:Number = points[i+1];
                var x2:Number, y2:Number;
                
                if (i+3 < length) {
                    x2 = points[i+2]; y2 = points[i+3];
                } else {
                    x2 = points[0]; y2 = points[1];
                }

                res = Geometry.intersectLines(x1, y1, x2, y2, start.x, start.y, end.x, end.y, ip);
                if (res > 0) break;
            }
            
            return res;
        }
    }
}