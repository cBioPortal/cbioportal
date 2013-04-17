Clazz.declarePackage ("J.awtjs2d");
c$ = Clazz.declareType (J.awtjs2d, "Image");
c$.getWidth = $_M(c$, "getWidth", 
function (canvas) {
{
return (canvas.imageWidth ? canvas.imageWidth : canvas.width);
}}, "~O");
c$.getHeight = $_M(c$, "getHeight", 
function (canvas) {
{
return (canvas.imageHeight ? canvas.imageHeight : canvas.height);
}}, "~O");
c$.getJpgImage = $_M(c$, "getJpgImage", 
function (apiPlatform, viewer, quality, comment) {
return null;
}, "J.api.ApiPlatform,J.viewer.Viewer,~N,~S");
c$.grabPixels = $_M(c$, "grabPixels", 
function (context, width, height) {
{
if (context._buf32) return context._buf32; // non-canvas internal buffer for image writing
var data = context.getImageData(0, 0, width, height).data;
return this.toIntARGB(data);
}}, "~O,~N,~N");
c$.toIntARGB = $_M(c$, "toIntARGB", 
function (imgData) {
var n = Clazz.doubleToInt (imgData.length / 4);
var iData =  Clazz.newIntArray (n, 0);
for (var i = 0, j = 0; i < n; j++) {
iData[i++] = (imgData[j++] << 16) | (imgData[j++] << 8) | imgData[j++] | 0xFF000000;
}
return iData;
}, "~A");
c$.fromIntARGB = $_M(c$, "fromIntARGB", 
function (buf32, buf8) {
var n = buf8.length >> 2;
for (var i = 0, j = 0; i < n; i++) {
buf8[j++] = (buf32[i] >> 16) & 0xFF;
buf8[j++] = (buf32[i] >> 8) & 0xFF;
buf8[j++] = buf32[i] & 0xFF;
buf8[j++] = 0xFF;
}
}, "~A,~A");
c$.getTextPixels = $_M(c$, "getTextPixels", 
function (text, font3d, context, width, height, ascent) {
{
context.fillStyle = "#000000";
context.fillRect(0, 0, width, height);
context.fillStyle = "#FFFFFF";
context.font = font3d.font;
context.fillText(text, 0, ascent);
return this.grabPixels(context, width, height);
}}, "~S,J.util.JmolFont,~O,~N,~N,~N");
c$.allocateRgbImage = $_M(c$, "allocateRgbImage", 
function (windowWidth, windowHeight, pBuffer, windowSize, backgroundTransparent, canvas) {
{
if (canvas == null)
canvas = {width:windowWidth,height:windowHeight};
canvas.buf32 = pBuffer;
return canvas;
}}, "~N,~N,~A,~N,~B,~O");
c$.getStaticGraphics = $_M(c$, "getStaticGraphics", 
function (canvas, backgroundTransparent) {
{
return this.getGraphics(canvas);
}}, "~O,~B");
c$.getGraphics = $_M(c$, "getGraphics", 
function (canvas) {
{
return canvas.getContext("2d");
}}, "~O");
c$.drawImage = $_M(c$, "drawImage", 
function (context, canvas, x, y, width, height) {
{
this.fromIntARGB(canvas.buf32, canvas.buf8);
context.putImageData(canvas.imgdata,x,y);
}}, "~O,~O,~N,~N,~N,~N");
