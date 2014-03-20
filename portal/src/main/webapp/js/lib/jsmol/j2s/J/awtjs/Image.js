Clazz.declarePackage ("J.awtjs");
c$ = Clazz.declareType (J.awtjs, "Image");
c$.grabPixels = $_M(c$, "grabPixels", 
function (imageobj, width, height) {
return null;
}, "~O,~N,~N");
c$.drawImageToBuffer = $_M(c$, "drawImageToBuffer", 
function (gOffscreen, imageOffscreen, imageobj, width, height, bgcolor) {
return null;
}, "~O,~O,~O,~N,~N,~N");
c$.getTextPixels = $_M(c$, "getTextPixels", 
function (text, font3d, gObj, image, width, height, ascent) {
return null;
}, "~S,J.util.JmolFont,~O,~O,~N,~N,~N");
c$.newBufferedImage = $_M(c$, "newBufferedImage", 
function (image, w, h) {
return null;
}, "~O,~N,~N");
c$.newBufferedImage = $_M(c$, "newBufferedImage", 
function (w, h) {
return null;
}, "~N,~N");
c$.allocateRgbImage = $_M(c$, "allocateRgbImage", 
function (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent) {
return null;
}, "~N,~N,~A,~N,~B");
c$.getStaticGraphics = $_M(c$, "getStaticGraphics", 
function (image, backgroundTransparent) {
return null;
}, "~O,~B");
c$.getGraphics = $_M(c$, "getGraphics", 
function (image) {
return null;
}, "~O");
c$.drawImage = $_M(c$, "drawImage", 
function (g, img, x, y, width, height) {
}, "~O,~O,~N,~N,~N,~N");
c$.flush = $_M(c$, "flush", 
function (image) {
}, "~O");
c$.disposeGraphics = $_M(c$, "disposeGraphics", 
function (graphicForText) {
}, "~O");
