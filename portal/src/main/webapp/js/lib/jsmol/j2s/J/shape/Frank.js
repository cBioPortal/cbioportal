Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.FontShape"], "J.shape.Frank", ["J.i18n.GT"], function () {
c$ = Clazz.decorateAsClass (function () {
this.frankString = "Jmol";
this.currentMetricsFont3d = null;
this.baseFont3d = null;
this.frankWidth = 0;
this.frankAscent = 0;
this.frankDescent = 0;
this.x = 0;
this.y = 0;
this.dx = 0;
this.dy = 0;
Clazz.instantialize (this, arguments);
}, J.shape, "Frank", J.shape.FontShape);
Clazz.defineMethod (c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Frank, "initShape", []);
this.myType = "frank";
this.baseFont3d = this.font3d = this.gdata.getFont3DFSS ("SansSerif", "Plain", 16);
this.calcMetrics ();
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
this.setPropFS (propertyName, value);
}, "~S,~O,JU.BS");
Clazz.overrideMethod (c$, "wasClicked", 
function (x, y) {
var width = this.vwr.getScreenWidth ();
var height = this.vwr.getScreenHeight ();
return (width > 0 && height > 0 && x > width - this.frankWidth - 4 && y > height - this.frankAscent - 4);
}, "~N,~N");
Clazz.overrideMethod (c$, "checkObjectHovered", 
function (x, y, bsVisible) {
if (!this.vwr.getShowFrank () || !this.wasClicked (x, y) || !this.vwr.menuEnabled ()) return false;
if (this.gdata.isDisplayAntialiased () && !this.vwr.isSingleThreaded) {
x <<= 1;
y <<= 1;
}this.vwr.hoverOnPt (x, y, J.i18n.GT._ ("Click for menu..."), null, null);
return true;
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "calcMetrics", 
function () {
if (this.vwr.isJS) this.frankString = "JSmol";
 else if (this.vwr.isSignedApplet ()) this.frankString = "Jmol_S";
if (this.font3d === this.currentMetricsFont3d) return;
this.currentMetricsFont3d = this.font3d;
this.frankWidth = this.font3d.stringWidth (this.frankString);
this.frankDescent = this.font3d.getDescent ();
this.frankAscent = this.font3d.getAscent ();
});
Clazz.defineMethod (c$, "getFont", 
function (imageFontScaling) {
this.font3d = this.gdata.getFont3DScaled (this.baseFont3d, imageFontScaling);
this.calcMetrics ();
}, "~N");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.vwr.getFontState (this.myType, this.font3d);
});
Clazz.defineStatics (c$,
"defaultFontName", "SansSerif",
"defaultFontStyle", "Plain",
"defaultFontSize", 16,
"frankMargin", 4);
});
