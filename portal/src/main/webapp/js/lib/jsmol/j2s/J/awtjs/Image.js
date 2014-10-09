Clazz.declarePackage ("J.awtjs");
c$ = Clazz.declareType (J.awtjs, "Image");
c$.grabPixels = Clazz.defineMethod (c$, "grabPixels", 
function (imageobj, width, height) {
return null;
}, "~O,~N,~N");
c$.drawImageToBuffer = Clazz.defineMethod (c$, "drawImageToBuffer", 
function (gOffscreen, imageOffscreen, imageobj, width, height, bgcolor) {
return null;
}, "~O,~O,~O,~N,~N,~N");
c$.getTextPixels = Clazz.defineMethod (c$, "getTextPixels", 
function (text, font3d, gObj, image, width, height, ascent) {
return null;
}, "~S,javajs.awt.Font,~O,~O,~N,~N,~N");
c$.newBufferedImage = Clazz.defineMethod (c$, "newBufferedImage", 
function (image, w, h) {
return null;
}, "~O,~N,~N");
c$.newBufferedImage = Clazz.defineMethod (c$, "newBufferedImage", 
function (w, h) {
return null;
}, "~N,~N");
c$.allocateRgbImage = Clazz.defineMethod (c$, "allocateRgbImage", 
function (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent) {
return null;
}, "~N,~N,~A,~N,~B");
c$.getStaticGraphics = Clazz.defineMethod (c$, "getStaticGraphics", 
function (image, backgroundTransparent) {
return null;
}, "~O,~B");
c$.getGraphics = Clazz.defineMethod (c$, "getGraphics", 
function (image) {
return null;
}, "~O");
c$.flush = Clazz.defineMethod (c$, "flush", 
function (image) {
}, "~O");
c$.disposeGraphics = Clazz.defineMethod (c$, "disposeGraphics", 
function (graphicForText) {
}, "~O");
