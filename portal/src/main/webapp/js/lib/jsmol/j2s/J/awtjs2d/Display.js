Clazz.declarePackage ("J.awtjs2d");
c$ = Clazz.declareType (J.awtjs2d, "Display");
c$.getFullScreenDimensions = $_M(c$, "getFullScreenDimensions", 
function (canvas, widthHeight) {
{
widthHeight[0] = canvas.width;
widthHeight[1] = canvas.height;
}}, "~O,~A");
c$.hasFocus = $_M(c$, "hasFocus", 
function (canvas) {
{
}return true;
}, "~O");
c$.requestFocusInWindow = $_M(c$, "requestFocusInWindow", 
function (canvas) {
{
}}, "~O");
c$.repaint = $_M(c$, "repaint", 
function (canvas) {
}, "~O");
c$.renderScreenImage = $_M(c$, "renderScreenImage", 
function (viewer, g, size) {
{
}}, "J.api.JmolViewer,~O,~O");
c$.setTransparentCursor = $_M(c$, "setTransparentCursor", 
function (canvas) {
{
}}, "~O");
c$.setCursor = $_M(c$, "setCursor", 
function (c, canvas) {
{
}}, "~N,~O");
c$.prompt = $_M(c$, "prompt", 
function (label, data, list, asButtons) {
{
var s = prompt(label, data);
if (s != null)return s;
}return "null";
}, "~S,~S,~A,~B");
c$.convertPointFromScreen = $_M(c$, "convertPointFromScreen", 
function (canvas, ptTemp) {
{
}}, "~O,J.util.P3");
