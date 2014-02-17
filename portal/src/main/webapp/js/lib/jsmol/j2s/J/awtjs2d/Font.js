Clazz.declarePackage ("J.awtjs2d");
c$ = Clazz.declareType (J.awtjs2d, "Font");
c$.newFont = $_M(c$, "newFont", 
function (fontFace, isBold, isItalic, fontSize, type) {
fontFace = (fontFace.equals ("Monospaced") ? "Courier" : fontFace.startsWith ("Sans") ? "Sans-Serif" : "Serif");
return (isBold ? "bold " : "") + (isItalic ? "italic " : "") + fontSize + type + " " + fontFace;
}, "~S,~B,~B,~N,~S");
c$.getFontMetrics = $_M(c$, "getFontMetrics", 
function (font, context) {
{
if (context.font != font.font) {
context.font = font.font;
font.font = context.font;
context._fontAscent = Math.ceil(font.fontSize); //pt, not px
// the descent is actually (px - pt)
// but I know of no way of getting access to the drawn height
context._fontDescent = Math.ceil(font.fontSize * 0.25);//approx
}
}return context;
}, "J.util.JmolFont,~O");
c$.getAscent = $_M(c$, "getAscent", 
function (context) {
{
return Math.ceil(context._fontAscent);
}}, "~O");
c$.getDescent = $_M(c$, "getDescent", 
function (context) {
{
return Math.ceil(context._fontDescent);
}}, "~O");
c$.stringWidth = $_M(c$, "stringWidth", 
function (font, context, text) {
{
context.font = font.font;
return Math.ceil(context.measureText(text).width);
}}, "J.util.JmolFont,~O,~S");
