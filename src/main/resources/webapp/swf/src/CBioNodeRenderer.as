/*
 * This class is used as a special renderer for CBio nodes where details about a node
 * can be displayed using 3 different disc segments around the node.
 **/
package org.cytoscapeweb.view.render
{
	import flare.util.Shapes;
	import flare.vis.data.DataSprite;
	import flare.vis.data.NodeSprite;
	
	import flash.display.*;
	import flash.display.BitmapData;
	import flash.display.Graphics;
	import flash.display.Sprite;
	import flash.geom.Matrix;
	import flash.geom.Rectangle;
	import flash.ui.Mouse;
	import flash.utils.setTimeout;
	
	import mx.utils.StringUtil;
	
	import org.alivepdf.display.Display;
	import org.cytoscapeweb.util.GraphUtils;
	import org.cytoscapeweb.util.NodeShapes;
	
	public class CBioNodeRenderer extends NodeRenderer
	{				
		private static var _instance:CBioNodeRenderer =
			new CBioNodeRenderer();
		
		private var _imgCache:ImageCache = ImageCache.instance;
		
		private var _detailFlag:Boolean = false;
		
		public static function get instance() : CBioNodeRenderer
		{
			return _instance;
		}

		public function get detailFlag() : Boolean
		{
			return this._detailFlag;
		}

		public function set detailFlag(value:Boolean) : void
		{
			this._detailFlag = value;
		}

		public function CBioNodeRenderer(defaultSize:Number=6)
		{
			super(defaultSize);
		}
		
		/** @inheritDoc */
		public override function render(d:DataSprite):void {trace("RENDER NODE: " + d.data.id);
			var lineAlpha:Number = d.lineAlpha;
			var fillAlpha:Number = d.fillAlpha;
			var size:Number = d.size * defaultSize;
			
			var g:Graphics = d.graphics;
			g.clear();
			
			if (lineAlpha > 0 && d.lineWidth > 0) 
			{
				var pixelHinting:Boolean = d.shape === NodeShapes.ROUND_RECTANGLE;
				g.lineStyle(d.lineWidth, d.lineColor, lineAlpha, pixelHinting);
			}
			
			if (fillAlpha > 0) 
			{
				// 1. Draw the background color:
				// Using a bit mask to avoid transparent mdes when fillcolor=0xffffffff.
				// See https://sourceforge.net/forum/message.php?msg_id=7393265
				g.beginFill(0xffffff & d.fillColor, fillAlpha);
				drawShape(d, d.shape, null);
				g.endFill();
				
				// 2. Draw an image on top:
				drawImage(d,0,0);
			}
		}
		
		// Draws the corresponding shape on given sprite
		protected override function drawShape(st:Sprite, shape:String, bounds:Rectangle):void 
		{
			var d:DataSprite = st as DataSprite;
			var g:Graphics = d.graphics;
			var size:Number = d.size*defaultSize;
			
			switch (shape) {
				case null:
					break;
				case NodeShapes.RECTANGLE:
					g.drawRect(-size/2, -size/2, size, size);
					break;
				case NodeShapes.TRIANGLE:
				case NodeShapes.DIAMOND:
				case NodeShapes.HEXAGON:
				case NodeShapes.OCTAGON:
				case NodeShapes.PARALLELOGRAM:
				case NodeShapes.V:
					var r:Rectangle = new Rectangle(-size/2, -size/2, size, size);
					var points:Array = NodeShapes.getDrawPoints(r, shape);
					Shapes.drawPolygon(g, points);
					break;
				case NodeShapes.ROUND_RECTANGLE:
					g.drawRoundRect(-size/2, -size/2, size, size, size/2, size/2);
					break;
				case NodeShapes.ELLIPSE:
				default:
					if (this._detailFlag || Boolean(d.props.detailFlag))
					{
						// Draws the disc containing details
						drawDetails(d, size/2);
					}
					
					// Genes are colored according to total alteration percentage
					var total:Number = Number(d.data.PERCENT_ALTERED) * 100;

					g.beginFill(getNodeColorRW(total), 50);
					
					// Seeded genes (IN_QUERY = true) are drawn with thicker borders
					var inQuery:String = d.data.IN_QUERY;
					
					if (inQuery == "false")
					{
						g.lineStyle(1, 0x000000, 1);
					}
					else
					{
						g.lineStyle(5, 0x000000, 1);
					}

					g.drawCircle(0, 0, size/2);
			}
		}
		
		protected override function drawImage(d:DataSprite, w:Number, h:Number):void 
		{
			var url:String = d.props.imageUrl;
			var size:Number = d.size*defaultSize;
			
			if (size > 0 && url != null && StringUtil.trim(url).length > 0) {
				// Load the image into the cache first?
				if (!_imgCache.contains(url)) {trace("Will load IMAGE...");
					_imgCache.loadImage(url);
				}
				if (_imgCache.isLoaded(url)) {trace(" .LOADED :-)");
					draw();
				} else {trace(" .NOT loaded :-(");
					drawWhenLoaded();
				}
				
				function drawWhenLoaded():void {
					setTimeout(function():void {trace(" .TIMEOUT: Checking again...");
						if (_imgCache.isLoaded(url)) draw();
						else if (!_imgCache.isBroken(url)) drawWhenLoaded();
					}, 50);
				}
				
				function draw():void {trace("Will draw: " + d.data.id);
					// Get the image from cache:
					var bd:BitmapData = _imgCache.getImage(url);
					
					if (bd != null) {
						var bmpSize:Number = Math.min(bd.height, bd.width);
						var scale:Number = size/bmpSize;
						
						var m:Matrix = new Matrix();
						m.scale(scale, scale);
						m.translate(-(bd.width*scale)/2, -(bd.height*scale)/2);
						
						d.graphics.beginBitmapFill(bd, m, false, true);
						drawShape(d, d.shape, null);
						d.graphics.endFill();
					}
				}
			}
		}
		
		// Draws the details of the node around the node circle
		private function drawDetails(d:DataSprite, RADIUS:int):void
		{
			var g:Graphics = d.graphics;
			
			// Flags represent whether a disc part will be drawn or not 
			var topFlag:Boolean = false;
			var rightFlag:Boolean = false;
			var leftFlag:Boolean = false;
			
			var thickness:int = 16; // Thickness of the detail disc
			var smoothness:int = 90; // Determines how smooth the ars are drawn
			var circleMargin:int = 0; // Margin between the discs and the node
			var insideMargin:Number = 1.0; // Inner margins for the percentage slices (should be half the border thickness!)
			
			// These variables are used for the gradient color for unused disc parts
			var fillType:String = "linear"
			var colors:Array = [0xDCDCDC, 0xFFFFFF];
			var alphas:Array = [100, 100];
			var ratios:Array = [0x00, 0xFF];
			var matrix:Matrix = new Matrix();
			matrix.createGradientBox(3, 3, 0, 0, 0);
			matrix.rotate(90/360);
			var spreadMethod:String = "repeat";
			
			g.lineStyle(0,0x000000,1);
			
			// Following part looks at available data and sets percentages if data is available
			
			// Top disc
			if (d.data.PERCENT_CNA_HEMIZYGOUSLY_DELETED != null ||
				d.data.PERCENT_CNA_AMPLIFIED != null ||
				d.data.PERCENT_CNA_HOMOZYGOUSLY_DELETED != null ||
				d.data.PERCENT_CNA_GAINED != null)
			{
				var hemizygousDeletion:Number = Number(d.data.PERCENT_CNA_HEMIZYGOUSLY_DELETED) * 100;
				var amplification:Number = Number(d.data.PERCENT_CNA_AMPLIFIED) * 100;
				var homozygousDeletion:Number = Number(d.data.PERCENT_CNA_HOMOZYGOUSLY_DELETED) * 100;
				var gain:Number = Number(d.data.PERCENT_CNA_GAINED) * 100;
				topFlag = true;
			}
			
			// Right disc
			if (d.data.PERCENT_MUTATED != null)
			{
				var mutation:Number = Number(d.data.PERCENT_MUTATED) * 100;
				rightFlag = true;
			}
			
			// Left disc
			if (d.data.PERCENT_MRNA_WAY_UP != null ||
				d.data.PERCENT_MRNA_WAY_DOWN != null)
			{
				var upRegulation:Number = Number(d.data.PERCENT_MRNA_WAY_UP) * 100;
				var downRegulation:Number = Number(d.data.PERCENT_MRNA_WAY_DOWN) * 100;
				leftFlag = true;
			}
			
			/* Following part draws the disc slice if associated flag is on, 3 disc parts are drawn in such a
			 * way that there are 20 degrees empty space between each part and the parts themselves have a
			 * 100 degrees space each, adding up to 360 degrees. The gray backgrounds are drawn first and the
			 * corresponing colored percentages are filled using the data about the node.
			 */
			
			// Don't draw anything if none is available
			if (!(topFlag || rightFlag || leftFlag))
			{
				return;
			}
			
			var innerRadius:Number;
			var outerRadius:Number;

			// Draws the top arc if its flag is on
			if (topFlag == true)
			{	
				innerRadius = RADIUS + circleMargin;
				outerRadius = RADIUS + thickness;

				// The gray back part is drawn
				g.lineStyle(2, 0xDCDCDC, 1);
				g.beginFill(0xFFFFFF, 50);
				drawSolidArc(0, 0, innerRadius, outerRadius, 218/360, 104/360, smoothness, g);
				
				innerRadius += insideMargin;
				outerRadius -= insideMargin;

				g.lineStyle(0, 0xFFFFFF, 0);

				var amplificationArc:int = amplification;
				g.beginFill(0xFF2500, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 220/360, (amplificationArc/360), smoothness, g);
				
				var homozygousDeletionArc:int = homozygousDeletion;
				g.beginFill(0x0332FF, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 220/360 + (amplificationArc/360), (homozygousDeletionArc/360), smoothness,g);			
				
				var gainArc:int = gain;
				g.beginFill(0xFFC5CC, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 220/360 + ((amplificationArc+homozygousDeletionArc)/360), (gainArc/360), smoothness,g);
				
				var hemizygousDeletionArc:int = hemizygousDeletion;
				g.beginFill(0x9EDFE0, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 220/360 + ((amplificationArc+homozygousDeletionArc+gainArc)/360), (hemizygousDeletionArc/360), smoothness,g);

				g.lineStyle(1, 0x000000, 1);
			}	
			else
			{
				g.lineStyle(1, 0xDCDCDC, 0.5);
				g.beginGradientFill(fillType, colors, alphas, ratios, matrix, spreadMethod);
				drawSolidArc(0, 0, RADIUS+circleMargin, RADIUS+thickness, 219/360, 102/360, smoothness, g);
				g.endFill();
			}
			
			if (rightFlag == true)
			{
				innerRadius = RADIUS + circleMargin;
				outerRadius = RADIUS + thickness;

				g.lineStyle(2, 0xDCDCDC, 1);
				g.beginFill(0xFFFFFF, 50);
				drawSolidArc(0, 0, innerRadius, outerRadius, -22/360, 104/360, smoothness, g);
				
				innerRadius += insideMargin;
				outerRadius -= insideMargin;

				g.lineStyle(0,0xDCDCDC,0);
				
				var mutationArc:int = mutation;
				g.beginFill(0x008F00, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, -20/360, (mutationArc/360), smoothness,g);
				
				g.lineStyle(1, 0x000000, 1);
			}
			else
			{
				g.lineStyle(1, 0xDCDCDC, 0.5);
				g.beginGradientFill(fillType, colors, alphas, ratios, matrix, spreadMethod);
				drawSolidArc(0, 0, RADIUS+circleMargin, RADIUS+thickness, -21/360, 102/360, smoothness, g);
				g.endFill();
			}

			if (leftFlag == true)
			{	
				innerRadius = RADIUS + circleMargin;
				outerRadius = RADIUS + thickness;

				g.lineStyle(2, 0xDCDCDC, 1);
				g.beginFill(0xFFFFFF, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 202/360, -(104/360), smoothness,g);
				
				innerRadius += insideMargin;
				outerRadius -= insideMargin;

				g.lineStyle(0, 0xDCDCDC, 0);

				var upRegulationArc:int = upRegulation;
				g.beginFill(0xFFACA9, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 200/360, -(upRegulationArc/360), smoothness,g);
				
				var downRegulationArc:int = downRegulation;
				g.beginFill(0x78AAD6, 50);
				drawSolidArc (0, 0, innerRadius, outerRadius, 200/360 - (upRegulationArc/360), -(downRegulationArc/360), smoothness,g);

				// When only borders are to be drawn (like in the OncoPrint)
//				g.endFill();
//				var upRegulationArc:int = upRegulation;
//				g.lineStyle(1.5,0xFFACA9,1);
//				drawSolidArc (0, 0, RADIUS+circleMargin+1.5, RADIUS+thickness-1.5, 201/360-4/360, -(upRegulationArc/360), smoothness,g);
//				
//				var downRegulationArc:int = downRegulation;
//				g.lineStyle(1.5,0x78AAD6,1);
//				drawSolidArc (0, 0, RADIUS+circleMargin+1.5, RADIUS+thickness-1.5, 201/360 - (upRegulationArc/360)-8/360, -(downRegulationArc/360), smoothness,g);

				g.lineStyle(1, 0x000000, 1);
			}
			else
			{
				g.lineStyle(1, 0xDCDCDC, 0.5);
				g.beginGradientFill(fillType, colors, alphas, ratios, matrix, spreadMethod);
				drawSolidArc (0, 0, RADIUS+circleMargin, RADIUS+thickness, 201/360, -(102/360), smoothness,g);
				g.endFill();
			}
			
			g.lineStyle(1, 0x000000, 1);
		}
		
		/**
		 * Draws a solid arc part using the given parameters
		 * 
		 * @param centerX	X-axis of the center of the shape
		 * @param centerY	Y-axis of the center of the shape
		 * @param innerRadius	radius of the inner circle
		 * @param outerRadius	radius of the outer circle
		 * @param startAngle	starting angle of the arc
		 * @param arcAngle	the angle that arc fills
		 * @param steps		determines how smooth the curve of the arc is
		 * @param g			graphics object to draw on it
		 * Reference: http://www.pixelwit.com/blog/2008/12/drawing-closed-arc-shape/ 
		 * */
		private function drawSolidArc (centerX:Number, centerY:Number, innerRadius:Number, outerRadius:Number, startAngle:Number, arcAngle:Number, steps:Number, g:Graphics):void{
			var twoPI:Number = 2 * Math.PI;
			var angleStep:Number = arcAngle/steps;
			var angle:Number, i:Number, endAngle:Number;

			var xx:Number = centerX + Math.cos(startAngle * twoPI) * innerRadius;
			var yy:Number = centerY + Math.sin(startAngle * twoPI) * innerRadius;

			var startPoint:Object = {x:xx, y:yy};
			g.moveTo(xx, yy);

			for(i=1; i<=steps; i++){
				angle = (startAngle + i * angleStep) * twoPI;
				xx = centerX + Math.cos(angle) * innerRadius;
				yy = centerY + Math.sin(angle) * innerRadius;
				g.lineTo(xx, yy);
			}

			endAngle = startAngle + arcAngle;
			
			for(i=0; i<=steps; i++){
				angle = (endAngle - i * angleStep) * twoPI;
				xx = centerX + Math.cos(angle) * outerRadius;
				yy = centerY + Math.sin(angle) * outerRadius;
				g.lineTo(xx, yy);
			}

			g.lineTo(startPoint.x, startPoint.y);
		}
		
		// Returns the color between Red and White using the given value as a ratio
		private function getNodeColorRW(value:int):Number
		{
			var high:int = 100;
			var low:int = 0;
			
			var highCRed:int = 255;
			var highCGreen:int = 0;
			var highCBlue:int = 0;
			
			var lowCRed:int = 255;
			var lowCGreen:int = 255;
			var lowCBlue:int = 255;
			
			// transform percentage value by using the formula:
			// y = 0.000166377 x^3  +  -0.0380704 x^2  +  3.14277x
			// instead of linear scaling, we use polynomial scaling to better
			// emphasize lower alteration frequencies
			value = (0.000166377 * value * value * value) -
				(0.0380704 * value * value) +
				(3.14277 * value);
			
			// check boundary values
			if (value > 100)
			{
				value = 100;
			}
			else if (value < 0)
			{
				value = 0;
			}
			
			if (value >= high)
			{
				return rgb2hex(highCRed, highCGreen, highCBlue);
			}
			else if (value > low)
			{
				return rgb2hex( getValueByRatio(value, low, high, lowCRed, highCRed),
					getValueByRatio(value, low, high, lowCGreen, highCGreen),
					getValueByRatio(value, low, high, lowCBlue, highCBlue)
				);
			}
			else
			{
				return rgb2hex(lowCRed, lowCGreen, lowCBlue);
			}
		}
		
		// used in getNodeColorRW to calculate a color number according to the ratio given
		private function getValueByRatio(num:Number,
				numLow:Number, numHigh:Number, colorNum1:int, colorNum2:int):int
		{
			return ((((num - numLow) / (numHigh - numLow)) * (colorNum2 - colorNum1)) + colorNum1)
		}
		
		// bitwise conversion of rgb color to a hex value
		private function rgb2hex(r:int, g:int, b:int):Number {
			return(r<<16 | g<<8 | b);
		}
	}
}