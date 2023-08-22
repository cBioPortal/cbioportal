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
package org.cytoscapeweb.util {
    import flare.util.Shapes;
    import flare.vis.data.EdgeSprite;
    
    import flash.display.CapsStyle;
    import flash.display.JointStyle;
    import flash.filters.GlowFilter;
    
    import org.cytoscapeweb.ApplicationFacade;
    import org.cytoscapeweb.model.ConfigProxy;
    import org.cytoscapeweb.model.data.VisualStyleVO;
    import org.cytoscapeweb.view.render.CBioEdgeRenderer;
    import org.cytoscapeweb.view.render.EdgeRenderer;
    
    
    public class Edges {
        
        // ========[ CONSTANTS ]====================================================================
        
        // ========[ PRIVATE PROPERTIES ]===========================================================

        private static var _properties:Object;
        private static var _configProxy:ConfigProxy;

        private static function get configProxy():ConfigProxy {
            if (_configProxy == null)
                _configProxy = ApplicationFacade.getInstance().retrieveProxy(ConfigProxy.NAME) as ConfigProxy;
            return _configProxy;
        }
        
        private static function get style():VisualStyleVO {
            return configProxy.visualStyle;
        }
        
                
        // ========[ CONSTRUCTOR ]==================================================================
        
        /**
         * This constructor will throw an error, as this is an abstract class. 
         */
        public function Edges() {
            throw new Error("This is an abstract class.");
        }
        
        // ========[ PUBLIC PROPERTIES ]============================================================
        
        public static function get properties():Object {
            if (_properties == null) {
                var renderer:EdgeRenderer = CBioEdgeRenderer.instance;
                renderer.caps = CapsStyle.NONE;
                renderer.joints = JointStyle.ROUND;
                renderer.miterLimit = 0;
                renderer.pixelHinting = false;
                
                _properties = {
                    lineWidth: lineWidth,
                    shape: shape,
                    lineColor: lineColor,
                    "props.lineStyle": lineStyle,
                    alpha: alpha,
                    visible: visible,
                    arrowHeight: 10, // default value, when edge width = 1
                    arrowWidth: 5,  // default value, when edge width = 1
                    "props.sourceArrowShape": sourceArrowShape,
                    "props.targetArrowShape": targetArrowShape,
                    "props.sourceArrowColor": sourceArrowColor,
                    "props.targetArrowColor": targetArrowColor,
                    "props.curvature": curvature,
                    filters: filters,
                    renderer: renderer,
                    buttonMode: true,
                    mouseChildren: false
                };
            }
            
            return _properties;
        }
        
        // ========[ PUBLIC METHODS ]===============================================================
        
        public static function lineWidth(e:EdgeSprite):Number {
            var propName:String = VisualProperties.EDGE_WIDTH;
            
            if (e.props.$merged) {
                propName = VisualProperties.EDGE_WIDTH_MERGE;
            }
                
            return style.getValue(propName, e.data) as Number;
        }
        
        public static function shape(e:EdgeSprite):String {
            var shape:String = Shapes.LINE;
            
            if ((!e.props.$merged && e.props.$curvatureFactor !== 0) || e.target === e.source)
                shape = Shapes.BEZIER;

            return shape;
        }
        
        public static function lineColor(e:EdgeSprite):uint {
            var propName:String = VisualProperties.EDGE_COLOR;

            if (e.props.$selected && style.hasVisualProperty(VisualProperties.EDGE_SELECTION_COLOR))
                propName = VisualProperties.EDGE_SELECTION_COLOR;
            else if (e.props.$merged) 
                propName = VisualProperties.EDGE_COLOR_MERGE;
            
            return style.getValue(propName, e.data);
        }
        
        public static function lineStyle(e:EdgeSprite):String {
            var propName:String = VisualProperties.EDGE_STYLE;
            if (e.props.$merged) propName = VisualProperties.EDGE_STYLE_MERGE;
            
            return style.getValue(propName, e.data);
        }
        
        public static function alpha(e:EdgeSprite):Number {
            var propName:String = VisualProperties.EDGE_ALPHA;

            if (e.props.$hover && style.hasVisualProperty(VisualProperties.EDGE_HOVER_ALPHA))
                propName = VisualProperties.EDGE_HOVER_ALPHA;
            else if (e.props.$selected && style.hasVisualProperty(VisualProperties.EDGE_SELECTION_ALPHA))
                propName = VisualProperties.EDGE_SELECTION_ALPHA;
            else if (e.props.$merged)
                propName = VisualProperties.EDGE_ALPHA_MERGE;

            return style.getValue(propName, e.data) as Number;
        }
        
        public static function visible(e:EdgeSprite):Boolean {
            var vis:Boolean = !GraphUtils.isFilteredOut(e);

            var merged:Boolean = configProxy.edgesMerged;
            vis = vis && ( (merged && e.props.$merged) || (!merged && !e.props.$merged) );
            
            return vis;
        }
        
        public static function filters(e:EdgeSprite):Array {
            var filters:Array = null;
            if (e.props.$selected) {
                var glow:GlowFilter = selectionGlow(e);
                if (glow != null) filters = [glow];
            }

            return filters;
        }
        
        public static function curvature(e:EdgeSprite):Number {
            var curvature:Number = style.getValue(VisualProperties.EDGE_CURVATURE, e.data) as Number;
            return e.props.$curvatureFactor * curvature;
        }
        
        public static function selectionGlow(e:EdgeSprite):GlowFilter {
            var filter:GlowFilter = null;
            var alpha:Number = style.getValue(VisualProperties.EDGE_SELECTION_GLOW_ALPHA, e.data) as Number;
            var blur:Number = style.getValue(VisualProperties.EDGE_SELECTION_GLOW_BLUR, e.data) as Number;
            var strength:Number = style.getValue(VisualProperties.EDGE_SELECTION_GLOW_STRENGTH, e.data) as Number;
            
            if (alpha > 0 && blur > 0 && strength > 0) {
                var color:uint = style.getValue(VisualProperties.EDGE_SELECTION_GLOW_COLOR, e.data) as uint;
                filter = new GlowFilter(color, alpha, blur, blur, strength);
            }
            
            return filter;
        }
        
        public static function sourceArrowShape(e:EdgeSprite):String {
            return arrowShape(e, VisualProperties.EDGE_SOURCE_ARROW_SHAPE);
        }
        
        public static function targetArrowShape(e:EdgeSprite):String {
            return arrowShape(e, VisualProperties.EDGE_TARGET_ARROW_SHAPE);
        }
        
        public static function sourceArrowColor(e:EdgeSprite):uint {
            return arrowColor(e, VisualProperties.EDGE_SOURCE_ARROW_COLOR);
        }
        
        public static function targetArrowColor(e:EdgeSprite):uint {
            return arrowColor(e, VisualProperties.EDGE_TARGET_ARROW_COLOR);
        }
        
        // ========[ PRIVATE METHODS ]==============================================================
        
        private static function arrowShape(e:EdgeSprite, propName:String):String {
            var shape:String = ArrowShapes.NONE;
            
            if (!e.props.$merged) {
                shape = style.getValue(propName, e.data) as String;
                shape = ArrowShapes.parse(shape);
            }
            
            return shape;
        }
        
        private static function arrowColor(e:EdgeSprite, propName:String):uint {
            if (e.props.$selected && style.hasVisualProperty(VisualProperties.EDGE_SELECTION_COLOR))
                propName = VisualProperties.EDGE_SELECTION_COLOR;
            else if (!style.hasVisualProperty(propName))
                propName = VisualProperties.EDGE_COLOR;
            
            return style.getValue(propName, e.data);
        }
        
    }
}