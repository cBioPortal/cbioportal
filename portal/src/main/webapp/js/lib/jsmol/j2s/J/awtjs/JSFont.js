Clazz.declarePackage ("J.awtjs");
c$ = Clazz.declareType (J.awtjs, "JSFont");
c$.newFont = Clazz.defineMethod (c$, "newFont", 
function (fontFace, isBold, isItalic, fontSize) {
return null;
}, "~S,~B,~B,~N");
c$.getFontMetrics = Clazz.defineMethod (c$, "getFontMetrics", 
function (graphics, font) {
return null;
}, "~O,~O");
c$.getAscent = Clazz.defineMethod (c$, "getAscent", 
function (fontMetrics) {
return 0;
}, "~O");
c$.getDescent = Clazz.defineMethod (c$, "getDescent", 
function (fontMetrics) {
return 0;
}, "~O");
c$.stringWidth = Clazz.defineMethod (c$, "stringWidth", 
function (font, text) {
return 0;
}, "javajs.awt.Font,~S");
