Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["J.api.ApiPlatform"], "J.awtjs2d.Platform", ["java.net.URL", "J.api.Interface", "J.awtjs2d.AjaxURLStreamHandlerFactory", "$.Display", "$.Font", "$.Image", "$.JmolFile", "$.JmolFileAdapter", "$.Mouse"], function () {
c$ = Clazz.decorateAsClass (function () {
this.canvas = null;
this.viewer = null;
this.context = null;
this.fileAdapter = null;
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "Platform", null, J.api.ApiPlatform);
Clazz.overrideMethod (c$, "setViewer", 
function (viewer, canvas) {
{
this.viewer = viewer;
this.canvas = canvas;
if (canvas != null) {
this.context = canvas.getContext("2d");
canvas.imgdata = this.context.getImageData(0, 0, canvas.width, canvas.height);
canvas.buf8 = canvas.imgdata.data;
}
}try {
java.net.URL.setURLStreamHandlerFactory ( new J.awtjs2d.AjaxURLStreamHandlerFactory ());
} catch (e) {
}
}, "J.viewer.Viewer,~O");
Clazz.overrideMethod (c$, "isSingleThreaded", 
function () {
return true;
});
Clazz.overrideMethod (c$, "getJsObjectInfo", 
function (jsObject, method, args) {
{
if (method == "localName")return jsObject[0]["nodeName"];
return (args == null ? jsObject[0][method] : jsObject[0][method](args[0]));
}}, "~A,~S,~A");
Clazz.overrideMethod (c$, "isHeadless", 
function () {
return false;
});
Clazz.overrideMethod (c$, "getFileAdapter", 
function () {
return (this.fileAdapter == null ? this.fileAdapter =  new J.awtjs2d.JmolFileAdapter () : this.fileAdapter);
});
Clazz.overrideMethod (c$, "newFile", 
function (name) {
return  new J.awtjs2d.JmolFile (name);
}, "~S");
Clazz.overrideMethod (c$, "getMouseManager", 
function (viewer, actionManager) {
return  new J.awtjs2d.Mouse (viewer, actionManager);
}, "J.viewer.Viewer,J.viewer.ActionManager");
Clazz.overrideMethod (c$, "convertPointFromScreen", 
function (canvas, ptTemp) {
J.awtjs2d.Display.convertPointFromScreen (canvas, ptTemp);
}, "~O,J.util.P3");
Clazz.overrideMethod (c$, "getFullScreenDimensions", 
function (canvas, widthHeight) {
J.awtjs2d.Display.getFullScreenDimensions (canvas, widthHeight);
}, "~O,~A");
Clazz.overrideMethod (c$, "getMenuPopup", 
function (viewer, menuStructure, type) {
var c = (type == 'j' ? "awtjs2d.JSmolPopup" : "awtjs2d.JSModelKitPopup");
var jmolpopup = J.api.Interface.getOptionInterface (c);
try {
if (jmolpopup != null) jmolpopup.jpiInitialize (viewer, menuStructure);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
c = "Exception creating " + c + ":" + e;
System.out.println (c);
return null;
} else {
throw e;
}
}
return jmolpopup;
}, "J.viewer.Viewer,~S,~S");
Clazz.overrideMethod (c$, "hasFocus", 
function (canvas) {
return J.awtjs2d.Display.hasFocus (canvas);
}, "~O");
Clazz.overrideMethod (c$, "prompt", 
function (label, data, list, asButtons) {
return J.awtjs2d.Display.prompt (label, data, list, asButtons);
}, "~S,~S,~A,~B");
Clazz.overrideMethod (c$, "renderScreenImage", 
function (context, size) {
J.awtjs2d.Display.renderScreenImage (this.viewer, context, size);
}, "~O,~O");
Clazz.overrideMethod (c$, "drawImage", 
function (context, canvas, x, y, width, height) {
J.awtjs2d.Image.drawImage (context, canvas, x, y, width, height);
}, "~O,~O,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "requestFocusInWindow", 
function (canvas) {
J.awtjs2d.Display.requestFocusInWindow (canvas);
}, "~O");
Clazz.overrideMethod (c$, "repaint", 
function (canvas) {
J.awtjs2d.Display.repaint (canvas);
}, "~O");
Clazz.overrideMethod (c$, "setTransparentCursor", 
function (canvas) {
J.awtjs2d.Display.setTransparentCursor (canvas);
}, "~O");
Clazz.overrideMethod (c$, "setCursor", 
function (c, canvas) {
J.awtjs2d.Display.setCursor (c, canvas);
}, "~N,~O");
Clazz.overrideMethod (c$, "allocateRgbImage", 
function (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent, isImageWrite) {
return J.awtjs2d.Image.allocateRgbImage (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent, (isImageWrite ? null : this.canvas));
}, "~N,~N,~A,~N,~B,~B");
Clazz.overrideMethod (c$, "notifyEndOfRendering", 
function () {
});
Clazz.overrideMethod (c$, "createImage", 
function (data) {
return null;
}, "~O");
Clazz.overrideMethod (c$, "disposeGraphics", 
function (gOffscreen) {
}, "~O");
Clazz.overrideMethod (c$, "grabPixels", 
function (canvas, width, height, pixels, startRow, nRows) {
{
if (canvas.image && (width != canvas.width || height != canvas.height))
Jmol._setCanvasImage(canvas, width, height);
if (canvas.buf32) return canvas.buf32;
}var buf = J.awtjs2d.Image.grabPixels (J.awtjs2d.Image.getGraphics (canvas), width, height);
{
canvas.buf32 = buf;
}return buf;
}, "~O,~N,~N,~A,~N,~N");
Clazz.overrideMethod (c$, "drawImageToBuffer", 
function (gOffscreen, imageOffscreen, canvas, width, height, bgcolor) {
return this.grabPixels (canvas, width, height, null, 0, 0);
}, "~O,~O,~O,~N,~N,~N");
Clazz.overrideMethod (c$, "getTextPixels", 
function (text, font3d, context, image, width, height, ascent) {
return J.awtjs2d.Image.getTextPixels (text, font3d, context, width, height, ascent);
}, "~S,J.util.JmolFont,~O,~O,~N,~N,~N");
Clazz.overrideMethod (c$, "flushImage", 
function (imagePixelBuffer) {
}, "~O");
Clazz.overrideMethod (c$, "getGraphics", 
function (image) {
return J.awtjs2d.Image.getGraphics (image);
}, "~O");
Clazz.overrideMethod (c$, "getImageHeight", 
function (canvas) {
return J.awtjs2d.Image.getHeight (canvas);
}, "~O");
Clazz.overrideMethod (c$, "getImageWidth", 
function (canvas) {
return J.awtjs2d.Image.getWidth (canvas);
}, "~O");
Clazz.overrideMethod (c$, "getJpgImage", 
function (viewer, quality, comment) {
return J.awtjs2d.Image.getJpgImage (this, viewer, quality, comment);
}, "J.viewer.Viewer,~N,~S");
Clazz.overrideMethod (c$, "getStaticGraphics", 
function (image, backgroundTransparent) {
return J.awtjs2d.Image.getStaticGraphics (image, backgroundTransparent);
}, "~O,~B");
Clazz.overrideMethod (c$, "newBufferedImage", 
function (image, w, h) {
{
if (typeof Jmol != "undefined" && Jmol._getHiddenCanvas)
return Jmol._getHiddenCanvas(this.viewer.applet, "stereoImage", w, h);
}return null;
}, "~O,~N,~N");
Clazz.overrideMethod (c$, "newOffScreenImage", 
function (w, h) {
{
if (typeof Jmol != "undefined" && Jmol._getHiddenCanvas)
return Jmol._getHiddenCanvas(this.viewer.applet, "textImage", w, h);
}return null;
}, "~N,~N");
Clazz.overrideMethod (c$, "waitForDisplay", 
function (echoNameAndPath, zipBytes) {
{
if (typeof Jmol == "undefined" || !Jmol._getHiddenCanvas) return false;
var viewer = this.viewer;
var sc = viewer.getEvalContextAndHoldQueue(viewer.eval);
var echoName = echoNameAndPath[0];
return Jmol._loadImage(this, echoNameAndPath, zipBytes,
function(canvas, pathOrError) { viewer.loadImageData(canvas, pathOrError, echoName, sc) }
);
}}, "~O,~O");
Clazz.overrideMethod (c$, "fontStringWidth", 
function (font, context, text) {
return J.awtjs2d.Font.stringWidth (font, context, text);
}, "J.util.JmolFont,~O,~S");
Clazz.overrideMethod (c$, "getFontAscent", 
function (context) {
return J.awtjs2d.Font.getAscent (context);
}, "~O");
Clazz.overrideMethod (c$, "getFontDescent", 
function (context) {
return J.awtjs2d.Font.getDescent (context);
}, "~O");
Clazz.overrideMethod (c$, "getFontMetrics", 
function (font, context) {
return J.awtjs2d.Font.getFontMetrics (font, context);
}, "J.util.JmolFont,~O");
Clazz.overrideMethod (c$, "newFont", 
function (fontFace, isBold, isItalic, fontSize) {
return J.awtjs2d.Font.newFont (fontFace, isBold, isItalic, fontSize, "px");
}, "~S,~B,~B,~N");
Clazz.overrideMethod (c$, "getDateFormat", 
function () {
{
return null;
}});
});
