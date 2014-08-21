Clazz.declarePackage ("J.awtjs");
Clazz.load (["J.awtjs2d.Platform"], "J.awtjs.Platform", ["J.awtjs.Image", "$.JSFont"], function () {
c$ = Clazz.declareType (J.awtjs, "Platform", J.awtjs2d.Platform);
Clazz.overrideMethod (c$, "allocateRgbImage", 
function (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent, isImageWrite) {
return J.awtjs.Image.allocateRgbImage (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent);
}, "~N,~N,~A,~N,~B,~B");
Clazz.overrideMethod (c$, "disposeGraphics", 
function (gOffscreen) {
J.awtjs.Image.disposeGraphics (gOffscreen);
}, "~O");
Clazz.overrideMethod (c$, "drawImage", 
function (g, img, x, y, width, height) {
}, "~O,~O,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "grabPixels", 
function (imageobj, width, height, pixels, startRow, nRows) {
return null;
}, "~O,~N,~N,~A,~N,~N");
Clazz.overrideMethod (c$, "drawImageToBuffer", 
function (gOffscreen, imageOffscreen, imageobj, width, height, bgcolor) {
return J.awtjs.Image.drawImageToBuffer (gOffscreen, imageOffscreen, imageobj, width, height, bgcolor);
}, "~O,~O,~O,~N,~N,~N");
Clazz.overrideMethod (c$, "getTextPixels", 
function (text, font3d, gObj, image, width, height, ascent) {
return J.awtjs.Image.getTextPixels (text, font3d, gObj, image, width, height, ascent);
}, "~S,javajs.awt.Font,~O,~O,~N,~N,~N");
Clazz.overrideMethod (c$, "flushImage", 
function (imagePixelBuffer) {
J.awtjs.Image.flush (imagePixelBuffer);
}, "~O");
Clazz.overrideMethod (c$, "getGraphics", 
function (image) {
return J.awtjs.Image.getGraphics (image);
}, "~O");
Clazz.overrideMethod (c$, "getStaticGraphics", 
function (image, backgroundTransparent) {
return J.awtjs.Image.getStaticGraphics (image, backgroundTransparent);
}, "~O,~B");
Clazz.overrideMethod (c$, "newBufferedImage", 
function (image, w, h) {
return J.awtjs.Image.newBufferedImage (image, w, h);
}, "~O,~N,~N");
Clazz.overrideMethod (c$, "newOffScreenImage", 
function (w, h) {
return J.awtjs.Image.newBufferedImage (w, h);
}, "~N,~N");
Clazz.overrideMethod (c$, "fontStringWidth", 
function (font, text) {
return J.awtjs.JSFont.stringWidth (font, text);
}, "javajs.awt.Font,~S");
Clazz.overrideMethod (c$, "getFontAscent", 
function (fontMetrics) {
return J.awtjs.JSFont.getAscent (fontMetrics);
}, "~O");
Clazz.overrideMethod (c$, "getFontDescent", 
function (fontMetrics) {
return J.awtjs.JSFont.getDescent (fontMetrics);
}, "~O");
Clazz.overrideMethod (c$, "getFontMetrics", 
function (font, graphics) {
return J.awtjs.JSFont.getFontMetrics (graphics, font);
}, "javajs.awt.Font,~O");
Clazz.overrideMethod (c$, "newFont", 
function (fontFace, isBold, isItalic, fontSize) {
return J.awtjs.JSFont.newFont (fontFace, isBold, isItalic, fontSize);
}, "~S,~B,~B,~N");
});
