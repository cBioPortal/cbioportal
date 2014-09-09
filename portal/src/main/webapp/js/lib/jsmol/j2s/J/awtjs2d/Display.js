Clazz.declarePackage ("J.awtjs2d");
c$ = Clazz.declareType (J.awtjs2d, "Display");
c$.getFullScreenDimensions = Clazz.defineMethod (c$, "getFullScreenDimensions", 
function (canvas, widthHeight) {
{
widthHeight[0] = canvas.width;
widthHeight[1] = canvas.height;
}}, "~O,~A");
c$.hasFocus = Clazz.defineMethod (c$, "hasFocus", 
function (canvas) {
{
}return true;
}, "~O");
c$.requestFocusInWindow = Clazz.defineMethod (c$, "requestFocusInWindow", 
function (canvas) {
{
}}, "~O");
c$.renderScreenImage = Clazz.defineMethod (c$, "renderScreenImage", 
function (vwr, g, size) {
{
}}, "javajs.api.PlatformViewer,~O,~O");
c$.setTransparentCursor = Clazz.defineMethod (c$, "setTransparentCursor", 
function (canvas) {
{
}}, "~O");
c$.setCursor = Clazz.defineMethod (c$, "setCursor", 
function (c, canvas) {
{
}}, "~N,~O");
c$.prompt = Clazz.defineMethod (c$, "prompt", 
function (label, data, list, asButtons) {
{
var s = prompt(label, data);
if (s != null)return s;
}return "null";
}, "~S,~S,~A,~B");
c$.convertPointFromScreen = Clazz.defineMethod (c$, "convertPointFromScreen", 
function (canvas, ptTemp) {
{
}}, "~O,JU.P3");
c$.drawImage = Clazz.defineMethod (c$, "drawImage", 
function (context, canvas, x, y, width, height) {
{
var buf8 = canvas.buf8;
var buf32 = canvas.buf32;
var n = width * height;
var dw = (canvas.width - width) * 4;
for (var i = 0, j = x * 4; i < n;) {
buf8[j++] = (buf32[i] >> 16) & 0xFF;
buf8[j++] = (buf32[i] >> 8) & 0xFF;
buf8[j++] = buf32[i] & 0xFF;
buf8[j++] = 0xFF;
if (((++i)%width)==0) j += dw;
}
context.putImageData(canvas.imgdata,x,y);
}}, "~O,~O,~N,~N,~N,~N");
