/**
 * Created by Mehmet Furkan Sahin on 01/07/2015.
 */

var showDetailsForAll = false;
var getDegrees = function(node){
    var map = {};
    map['amplification-start'] = 3.75;

    map['amplification-end'] = (node._private.data['PERCENT_CNA_AMPLIFIED'] === undefined) ?
                                map['amplification-start'] :
                                3.75 + 1.1 * node._private.data['PERCENT_CNA_AMPLIFIED'] * 100 * 2 * Math.PI / 360;

    map['homozygous-deletion-start'] = map['amplification-end'];

    map['homozygous-deletion-end'] = (node._private.data['PERCENT_CNA_HOMOZYGOUSLY_DELETED'] === undefined) ?
                                map['homozygous-deletion-start'] :
                                map['homozygous-deletion-start'] + 1.1 * node._private.data['PERCENT_CNA_HOMOZYGOUSLY_DELETED'] * 100 * 2 * Math.PI / 360;

    map['gain-start'] = map['homozygous-deletion-end'];

    map['gain-end'] = (node._private.data['PERCENT_CNA_GAINED'] === undefined) ?
                                map['gain-start'] :
                                map['gain-start'] + 1.1 * node._private.data['PERCENT_CNA_GAINED'] * 100 * 2 * Math.PI / 360;

    map['hemizygous-deletion-start'] = map['gain-end'];

    map['hemizygous-deletion-end'] = (node._private.data['PERCENT_CNA_HEMIZYGOUSLY_DELETED'] === undefined) ?
                                map['hemizygous-deletion-start']:
                                map['hemizygous-deletion-start'] + 1.1 * node._private.data['PERCENT_CNA_HEMIZYGOUSLY_DELETED'] * 100 * 2 * Math.PI / 360;

    map['upper-start'] = map['hemizygous-deletion-end'];

    map['upper-end'] = 5.68;

    map['mutated-start'] = 5.85;

    map['mutated-end'] = (node._private.data['PERCENT_MUTATED'] === undefined) ?
                                map['mutated-start'] :
                                map['mutated-start'] + node._private.data['PERCENT_MUTATED'] * 100 * 2 * Math.PI / 360;

    map['right-start'] = map['mutated-end'];

    map['right-end'] = 1.48;

    map['up-regulated-start'] = 3.58;

    map['up-regulated-end'] = (node._private.data['PERCENT_MRNA_WAY_UP'] === undefined) ?
                                map['up-regulated-start'] :
                                map['up-regulated-start'] - 1.1 * node._private.data['PERCENT_MRNA_WAY_UP'] * 100 * 2 * Math.PI / 360;

    map['down-regulated-start'] = map['up-regulated-end'];

    map['down-regulated-end'] = (node._private.data['PERCENT_MRNA_WAY_DOWN'] === undefined) ?
                                map['down-regulated-start'] :
                                map['down-regulated-start'] - 1.1 * node._private.data['PERCENT_MRNA_WAY_DOWN'] * 100 * 2 * Math.PI / 360;

    map['left-start'] = map['down-regulated-end'];

    map['left-end'] = 1.65;

    return map;
};

var mutNames = ['amplification', 'homozygous-deletion', 'gain', 'hemizygous-deletion','upper', 'mutated','right', 'up-regulated', 'down-regulated','left'];
var rgbs     = ['rgba(254,80,51,0.8)', 'rgba(53,91,255,0.8)', 'rgba(255,208,214,0.8)', 'rgba(158,223,224,0.8)','rgba(255,255,255,0.3)', 'rgba(50,161,50,0.8)','rgba(255,255,255,0.3)', 'rgba(250,185,182,0.8)', 'rgba(147,187,221,0.8)','rgba(255,255,255,0.3)'];

(function ($$) {"use strict";
    var CanvasRenderer = $$('renderer', 'canvas');
    var CRp = CanvasRenderer.prototype;


    //add showDetails property to css features
    $$.style.types.trueOrFalse = {enums: ['true', 'false']};
    $$.style.properties.push({name: 'show-details', type: $$.style.types.trueOrFalse});
    $$.style.properties['show-details'] = {name: 'show-details', type: $$.style.types.trueOrFalse};

    $$.style.types.trueOrFalse = {enums: ['true', 'false']};
    $$.style.properties.push({name: 'show-total-alteration', type: $$.style.types.trueOrFalse});
    $$.style.properties['show-total-alteration'] = {name: 'show-total-alteration', type: $$.style.types.trueOrFalse};

    $$.style.types.trueOrFalse = {enums: ['true', 'false']};
    $$.style.properties.push({name: 'show-details-selected', type: $$.style.types.trueOrFalse});
    $$.style.properties['show-details-selected'] = {name: 'show-details-selected', type: $$.style.types.trueOrFalse};

    $$.style.types.trueOrFalse = {enums: ['true', 'false']};
    $$.style.properties.push({name: 'merged', type: $$.style.types.trueOrFalse});
    $$.style.properties['merged'] = {name: 'merged', type: $$.style.types.trueOrFalse};

    $$.style.properties.push({name: 'mouse-position-x', type: $$.style.types.percent});
    $$.style.properties['mouse-position-x'] = {name: 'mouse-position-x', type: $$.style.types.percent};

    $$.style.properties.push({name: 'mouse-position-y', type: $$.style.types.percent});
    $$.style.properties['mouse-position-y'] = {name: 'mouse-position-y', type: $$.style.types.percent};

    $$.style.properties.push({name: 'total-alteration-color', type: $$.style.types.color});
    $$.style.properties['total-alteration-color'] = {name: 'total-alteration-color', type: $$.style.types.color};

    $$.style.properties.push({name: 'total-alteration-font', type: $$.style.types.fontFamily});
    $$.style.properties['total-alteration-font'] = {name: 'total-alteration-font', type: $$.style.types.fontFamily};

    $$.style.properties.push({name: 'total-alteration-font-size', type: $$.style.types.size});
    $$.style.properties['total-alteration-font-size'] = {name: 'total-alteration-font-size', type: $$.style.types.size};

    $$.style.properties.push({name: 'main-opacity', type: $$.style.types.size});
    $$.style.properties['main-opacity'] = {name: 'main-opacity', type: $$.style.types.size};

    CRp.drawNode = function(context, node, drawOverlayInstead) {

        var font = context.font;
        context.shadowOffsetX = 0;
        context.shadowOffsetY = 0;

        if (node._private.data['type'] !== 'Drug' && (node._private.style['show-details'] === true || node.selected() === true || showDetailsForAll === true))
        {
            node.css("opacity", 1);
            var degrees = getDegrees(node);
            if (node._private.data['PERCENT_CNA_AMPLIFIED'] + node._private.data['PERCENT_CNA_HOMOZYGOUSLY_DELETED']  + node._private.data['PERCENT_CNA_GAINED']
                + node._private.data['PERCENT_CNA_HEMIZYGOUSLY_DELETED']  > 1 ||
                node._private.data['PERCENT_MRNA_WAY_UP']  + node._private.data['PERCENT_MRNA_WAY_DOWN'] > 1 ||
                node._private.data['PERCENT_MUTATED'] > 1)
            {

                console.log("Inappropriate inputs!");
                return;
            }
            context.fillStyle = "#FF0000";
            context.shadowColor = "grey";
            context.shadowBlur = 10;
            if (showDetailsForAll === true && node.selected() === false && (node._private.style['show-total-alteration'].value === 'false' ||
                                                                            node._private.style['show-total-alteration'] === false)){
              context.shadowBlur = 1;
            }


            for (var i = 0; i < 10; i++) {
                context.beginPath();
                if (i < 7)
                    context.arc(node._private.position['x'], node._private.position['y'], node._private.style['width'].value + 10,
                        degrees[mutNames[i] + '-start'], degrees[mutNames[i] + '-end'], 0);
                else{
                    context.arc(node._private.position['x'], node._private.position['y'], node._private.style['width'].value + 10,
                        degrees[mutNames[i] + '-start'], degrees[mutNames[i] + '-end'], 1);
                }
                context.strokeStyle = "rgba(255,255,255,0)";
                context.lineTo(node._private.position['x'],
                    node._private.position['y']);
                context.fillStyle = rgbs[i];
                context.fill();
                context.closePath();
                context.stroke();
            }

            context.beginPath();

            if (node._private.data['PERCENT_CNA_AMPLIFIED'] === undefined && node._private.data['PERCENT_CNA_HOMOZYGOUSLY_DELETED'] === undefined &&
                node._private.data['PERCENT_CNA_GAINED'] === undefined && node._private.data['PERCENT_CNA_HEMIZYGOUSLY_DELETED'] === undefined ){
                var grd = context.createLinearGradient(node._private.position['x']-60,
                    node._private.position['y']-60, node._private.position['x']+30,
                    node._private.position['y']+30);
                var tempRgbs = ['rgba(255,255,255,100)', 'rgba(210,210,210,100)'];
                var b = 0;
                for (var i= 0; i < 1.001; i+=0.02 ){
                    grd.addColorStop(i, tempRgbs[b]);
                    if (b == 0) b = 1;
                    else b = 0;
                }
                context.fillStyle = grd;
                context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                    3.75,5.67, 0);
                context.lineTo(node._private.position['x'],
                    node._private.position['y']);
                context.fill();
                context.closePath();
                context.stroke();
                context.beginPath();
            }
            if (node._private.data['PERCENT_MUTATED'] === undefined ){
                var grd = context.createLinearGradient(node._private.position['x']-30,
                    node._private.position['y']-30, node._private.position['x']+60,
                    node._private.position['y']+60);
                var tempRgbs = ['rgba(255,255,255,100)', 'rgba(210,210,210,100)'];
                var b = 0;
                for (var i= 0; i < 1.001; i+=0.02 ){
                    grd.addColorStop(i, tempRgbs[b]);
                    if (b == 0) b = 1;
                    else b = 0;
                }
                context.fillStyle = grd;
                context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                    5.85,7.76, 0);
                context.lineTo(node._private.position['x'],
                    node._private.position['y']);
                context.fill();
                context.closePath();
                context.stroke();
                context.beginPath();
            }
            if (node._private.data['PERCENT_MRNA_WAY_UP']=== undefined && node._private.data['PERCENT_MRNA_WAY_DOWN'] === undefined ){
                var grd = context.createLinearGradient(node._private.position['x']-60,
                    node._private.position['y']-60, node._private.position['x']+30,
                    node._private.position['y']+30);
                var tempRgbs = ['rgba(255,255,255,100)', 'rgba(210,210,210,100)'];
                var b = 0;
                for (var i= 0; i < 1.001; i+=0.02 ){
                    grd.addColorStop(i, tempRgbs[b]);
                    if (b == 0) b = 1;
                    else b = 0;
                }
                context.fillStyle = grd;
                context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                    1.66,3.58, 0);
                context.lineTo(node._private.position['x'],
                    node._private.position['y']);
                context.fill();
                context.closePath();
                context.stroke();
                context.beginPath();
            }


            if (node.selected()){
                context.shadowColor = "rgb(255,255,0)";
                context.strokeStyle = "rgba(249,251,166," + node._private.style['main-opacity'].value + ")";
                context.shadowBlur = 1;

            }
            else if (node._private.style['show-total-alteration'] === true){
                context.shadowColor = "rgb(227,227,227)";
                context.strokeStyle = "rgba(0,0,0," + node._private.style['main-opacity'].value + ")";
                context.shadowBlur = 10;
            }
            else{
                node.css('opacity', node._private.style['main-opacity'].value);
                context.shadowBlur = 1;
            }

            context.lineWidth = 1;
      //      context.fillStyle = "rgba(200,200,200,1)"
            context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                3.75,5.67, 0);
            context.lineTo(node._private.position['x'],
                node._private.position['y']);

            context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                5.85,7.76, 0);
            context.lineTo(node._private.position['x'],
                node._private.position['y']);
            context.arc(node._private.position['x'],node._private.position['y'],node._private.style['width'].value + 10,
                1.66, 3.58, 0);
            context.lineTo(node._private.position['x'],
                node._private.position['y']);

            context.closePath();
            context.stroke();
        }

        var r = this;
        var nodeWidth, nodeHeight;
        var style = node._private.style;
        var rs = node._private.rscratch;
        var _p = node._private;

        var usePaths = CanvasRenderer.usePaths();
        var canvasContext = context;
        var path;
        var pathCacheHit = false;

        var overlayPadding = style['overlay-padding'].pxValue;
        var overlayOpacity = style['overlay-opacity'].value;
        var overlayColor = style['overlay-color'].value;

        if( drawOverlayInstead && overlayOpacity === 0 ){ // exit early if drawing overlay but none to draw
            return;
        }

        var parentOpacity = node.effectiveOpacity();
        if( parentOpacity === 0 ){ return; }

        nodeWidth = this.getNodeWidth(node);
        nodeHeight = this.getNodeHeight(node);

        context.lineWidth = style['border-width'].pxValue;

        if( drawOverlayInstead === undefined || !drawOverlayInstead ){

            var url = style['background-image'].value[2] ||
                style['background-image'].value[1];
            var image;

            if (url !== undefined) {

                // get image, and if not loaded then ask to redraw when later loaded
                image = this.getCachedImage(url, function(){
                    r.data.canvasNeedsRedraw[CanvasRenderer.NODE] = true;
                    r.data.canvasNeedsRedraw[CanvasRenderer.DRAG] = true;

                    r.drawingImage = true;

                    r.redraw();
                });

                var prevBging = _p.backgrounding;
                _p.backgrounding = !image.complete;

                if( prevBging !== _p.backgrounding ){ // update style b/c :backgrounding state changed
                    node.updateStyle( false );
                }
            }

            // Node color & opacity

            var bgColor = style['background-color'].value;
            var borderColor = style['border-color'].value;
            var borderStyle = style['border-style'].value;

            this.fillStyle(context, bgColor[0], bgColor[1], bgColor[2], style['background-opacity'].value * parentOpacity);

            this.strokeStyle(context, borderColor[0], borderColor[1], borderColor[2], style['border-opacity'].value * parentOpacity);

            var shadowBlur = style['shadow-blur'].pxValue;
            var shadowOpacity = style['shadow-opacity'].value;
            var shadowColor = style['shadow-color'].value;
            var shadowOffsetX = style['shadow-offset-x'].pxValue;
            var shadowOffsetY = style['shadow-offset-y'].pxValue;

            this.shadowStyle(context, shadowColor, shadowOpacity, shadowBlur, shadowOffsetX, shadowOffsetY);

            context.lineJoin = 'miter'; // so borders are square with the node shape

            if( context.setLineDash ){ // for very outofdate browsers
                switch( borderStyle ){
                    case 'dotted':
                        context.setLineDash([ 1, 1 ]);
                        break;

                    case 'dashed':
                        context.setLineDash([ 4, 2 ]);
                        break;

                    case 'solid':
                    case 'double':
                        context.setLineDash([ ]);
                        break;
                }
            }


            var styleShape = style['shape'].strValue;

            var pos = node._private.position;

            if( usePaths ){
                var pathCacheKey = styleShape + '$' + nodeWidth +'$' + nodeHeight;

                context.translate( pos.x, pos.y );

                if( rs.pathCacheKey === pathCacheKey ){
                    path = context = rs.pathCache;
                    pathCacheHit = true;
                } else {
                    path = context = new Path2D();
                    rs.pathCacheKey = pathCacheKey;
                    rs.pathCache = path;
                }
            }

            if( !pathCacheHit ){

                var npos = pos;

                if( usePaths ){
                    npos = {
                        x: 0,
                        y: 0
                    };
                }

                CanvasRenderer.nodeShapes[this.getNodeShape(node)].drawPath(
                    context,
                    npos.x,
                    npos.y,
                    nodeWidth,
                    nodeHeight);
            }

            context = canvasContext;

            if( usePaths ){
                context.fill( path );
            } else {
                context.fill();
            }

            this.shadowStyle(context, 'transparent', 0); // reset for next guy

            if (url !== undefined) {
                if( image.complete ){
                    this.drawInscribedImage(context, image, node);
                }
            }

            var darkness = style['background-blacken'].value;
            var borderWidth = style['border-width'].pxValue;

            if( this.hasPie(node) ){
                this.drawPie( context, node, parentOpacity );

                // redraw path for blacken and border
                if( darkness !== 0 || borderWidth !== 0 ){

                    if( !usePaths ){
                        CanvasRenderer.nodeShapes[this.getNodeShape(node)].drawPath(
                            context,
                            pos.x,
                            pos.y,
                            nodeWidth,
                            nodeHeight);
                    }
                }
            }

            if( darkness > 0 ){
                this.fillStyle(context, 0, 0, 0, darkness);

                if( usePaths ){
                    context.fill( path );
                } else {
                    context.fill();
                }

            } else if( darkness < 0 ){
                this.fillStyle(context, 255, 255, 255, -darkness);

                if( usePaths ){
                    context.fill( path );
                } else {
                    context.fill();
                }
            }

            // Border width, draw border
            if (borderWidth > 0) {

                if( usePaths ){
                    context.stroke( path );
                } else {
                    context.stroke();
                }

                if( borderStyle === 'double' ){
                    context.lineWidth = style['border-width'].pxValue/3;

                    var gco = context.globalCompositeOperation;
                    context.globalCompositeOperation = 'destination-out';

                    if( usePaths ){
                        context.stroke( path );
                    } else {
                        context.stroke();
                    }

                    context.globalCompositeOperation = gco;
                }

            }

            if( usePaths ){
                context.translate( -pos.x, -pos.y );
            }

            // reset in case we changed the border style
            if( context.setLineDash ){ // for very outofdate browsers
                context.setLineDash([ ]);
            }

            // draw the overlay
        } else {

            if( overlayOpacity > 0 ){
                this.fillStyle(context, overlayColor[0], overlayColor[1], overlayColor[2], overlayOpacity);

                CanvasRenderer.nodeShapes['roundrectangle'].drawPath(
                    context,
                    node._private.position.x,
                    node._private.position.y,
                    nodeWidth + overlayPadding * 2,
                    nodeHeight + overlayPadding * 2
                );

                context.fill();
            }
        }
        if (node._private.style['show-total-alteration'] === true) {

            if (node._private.data['type'] === 'Drug')
              return;

            context.fillStyle = "#000000";
            context.shadowColor = "rgb(30,30,30)";
            context.shadowOffsetX = 3;
            context.shadowOffsetY = 3;
            context.shadowBlur = 9;
            context.lineWidth = 2;
            context.beginPath();
            context.font = "bold " + node._private.style['total-alteration-font-size'].strValue + " " + node._private.style['total-alteration-font'].strValue;
            var a = node._private.position['x'];
            var b = node._private.position['y'];
            context.rect(node._private.style['mouse-position-x']+node._private.style['total-alteration-font-size'].pxValue * 2
                , node._private.style['mouse-position-y']+node._private.style['total-alteration-font-size'].pxValue * 2,
                node._private.style['total-alteration-font-size'].pxValue * 4,
                node._private.style['total-alteration-font-size'].pxValue * 3 / 2);


            context.fill();
            context.fillStyle = node._private.style['total-alteration-color'].strValue;
            context.fillText((node._private.data['PERCENT_ALTERED']*100).toString().substring(0,4) + "%",
                node._private.style['mouse-position-x']+node._private.style['total-alteration-font-size'].pxValue * 4,
                node._private.style['mouse-position-y']+node._private.style['total-alteration-font-size'].pxValue * 3.3);

            context.closePath();
            context.stroke();
        }
        context.font = font;

    }
})(cytoscape);
