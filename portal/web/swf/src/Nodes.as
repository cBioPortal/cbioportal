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
    import flare.vis.data.NodeSprite;
    
    import flash.filters.GlowFilter;
    
    import org.cytoscapeweb.ApplicationFacade;
    import org.cytoscapeweb.model.ConfigProxy;
    import org.cytoscapeweb.model.data.VisualStyleBypassVO;
    import org.cytoscapeweb.model.data.VisualStyleVO;
    import org.cytoscapeweb.view.render.CBioNodeRenderer;
    
    
    public class Nodes {
        
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
        
        private static function get bypass():VisualStyleBypassVO {
            return configProxy.visualStyleBypass;
        }
        
        // ========[ CONSTRUCTOR ]==================================================================
        
        /**
         * This constructor will throw an error, as this is an abstract class. 
         */
        public function Nodes() {
            throw new Error("This is an abstract class.");
        }
        
        // ========[ PUBLIC PROPERTIES ]============================================================
        
        public static function get properties():Object {
            if (_properties == null) {
                _properties = {
                    shape: shape,
                    size: size,
                    fillColor: fillColor,
                    lineColor: lineColor, 
                    lineWidth: lineWidth,
                    alpha: alpha,
                    "props.imageUrl": imageUrl,
                    visible: visible,
                    buttonMode: true,
                    filters: filters,
                    renderer: CBioNodeRenderer.instance
                };
            }  
            return _properties;
        }
        
        // ========[ PUBLIC METHODS ]===============================================================
        
        public static function shape(n:NodeSprite):String {
            var shape:String = style.getValue(VisualProperties.NODE_SHAPE, n.data);
            return NodeShapes.parse(shape);
        }
        
        public static function size(n:NodeSprite):Number {
            var size:Number = style.getValue(VisualProperties.NODE_SIZE, n.data);
            // Flare size is a relative value:
            return size / _properties.renderer.defaultSize;
        }
        
        public static function fillColor(n:NodeSprite):uint {
            var propName:String = VisualProperties.NODE_COLOR;
            
            if (n.props.$selected && style.hasVisualProperty(VisualProperties.NODE_SELECTION_COLOR))
                propName = VisualProperties.NODE_SELECTION_COLOR;
            
            return style.getValue(propName, n.data);
        }
        
        public static function lineColor(n:NodeSprite):uint {
            var propName:String = VisualProperties.NODE_LINE_COLOR;

            if (n.props.$hover && style.hasVisualProperty(VisualProperties.NODE_HOVER_LINE_COLOR))
                propName = VisualProperties.NODE_HOVER_LINE_COLOR;
            else if (n.props.$selected && style.hasVisualProperty(VisualProperties.NODE_SELECTION_LINE_COLOR))
                propName = VisualProperties.NODE_SELECTION_LINE_COLOR;
            
            return style.getValue(propName, n.data);
        }
        
        public static function lineWidth(n:NodeSprite):Number {
            var propName:String = VisualProperties.NODE_LINE_WIDTH;
            
            if (n.props.$hover && style.hasVisualProperty(VisualProperties.NODE_HOVER_LINE_WIDTH))
                propName = VisualProperties.NODE_HOVER_LINE_WIDTH;
            else if (n.props.$selected && style.hasVisualProperty(VisualProperties.NODE_SELECTION_LINE_WIDTH))
                propName = VisualProperties.NODE_SELECTION_LINE_WIDTH;
        
            return style.getValue(propName, n.data);
        }
        
        public static function selectionLineWidth(n:NodeSprite):Number {
            var propName:String = VisualProperties.NODE_LINE_WIDTH;
            
            if (style.hasVisualProperty(VisualProperties.NODE_SELECTION_LINE_WIDTH))
                propName = VisualProperties.NODE_SELECTION_LINE_WIDTH;
            else if (n.props.$hover && style.hasVisualProperty(VisualProperties.NODE_HOVER_LINE_WIDTH))
                propName = VisualProperties.NODE_HOVER_LINE_WIDTH;
        
            return style.getValue(propName, n.data);
        }
        
        public static function alpha(n:NodeSprite):Number {
            var propName:String = VisualProperties.NODE_ALPHA;
            
            if (n.props.$hover && style.hasVisualProperty(VisualProperties.NODE_HOVER_ALPHA))
                propName = VisualProperties.NODE_HOVER_ALPHA;
            else if (n.props.$selected && style.hasVisualProperty(VisualProperties.NODE_SELECTION_ALPHA))
                propName = VisualProperties.NODE_SELECTION_ALPHA;

            return style.getValue(propName, n.data);
        }

        public static function imageUrl(n:NodeSprite):String {
            var propName:String = VisualProperties.NODE_IMAGE;
            return style.getValue(propName, n.data);
        }
        
        public static function selectionAlpha(n:NodeSprite):Number {
            var propName:String = VisualProperties.NODE_ALPHA;
            
            if (style.hasVisualProperty(VisualProperties.NODE_SELECTION_ALPHA))
                propName = VisualProperties.NODE_SELECTION_ALPHA;
                
            return style.getValue(propName, n.data);
        }
        
        public static function visible(n:NodeSprite):Boolean {
            return !n.props.$filteredOut;
        }
        
        public static function filters(n:NodeSprite, selectNow:Boolean=false):Array {
            var filters:Array = [];
            var glow:GlowFilter = null;

            if (!selectNow && n.props.$hover)
                glow = hoverGlow(n);
            if (glow == null && n.props.$selected)
                glow = selectionGlow(n);
            
            if (glow != null)
                filters.push(glow);

            return filters;
        }
        
        public static function selectionGlow(n:NodeSprite):GlowFilter {
            var filter:GlowFilter = null;
            var data:Object = n.data;
            var alpha:Number = style.getValue(VisualProperties.NODE_SELECTION_GLOW_ALPHA, data);
            var blur:Number = style.getValue(VisualProperties.NODE_SELECTION_GLOW_BLUR, data);
            var strength:Number = style.getValue(VisualProperties.NODE_SELECTION_GLOW_STRENGTH, data);
            
            if (alpha > 0 && blur > 0 && strength > 0) {
                var color:uint = style.getValue(VisualProperties.NODE_SELECTION_GLOW_COLOR, data);           
                filter = new GlowFilter(color, alpha, blur, blur, strength);
            }
            
            return filter;
        }
        
        public static function hoverGlow(n:NodeSprite):GlowFilter {
            var filter:GlowFilter = null;
            var data:Object = n.data;
            var alpha:Number = style.getValue(VisualProperties.NODE_HOVER_GLOW_ALPHA, data);
            var blur:Number = style.getValue(VisualProperties.NODE_HOVER_GLOW_BLUR, data);
            var strength:Number = style.getValue(VisualProperties.NODE_HOVER_GLOW_STRENGTH, data);
            
            if (alpha > 0 && blur > 0 && strength > 0) {
                var color:uint = style.getValue(VisualProperties.NODE_HOVER_GLOW_COLOR, data);
                filter = new GlowFilter(color, alpha, blur, blur, strength);
            }
            
            return filter;
        }
        
        // ========[ PRIVATE METHODS ]==============================================================
        
    }
}