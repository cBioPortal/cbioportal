Clazz.declarePackage ("J.render");
Clazz.load (["J.render.LabelsRenderer"], "J.render.EchoRenderer", ["JM.Atom", "J.render.TextRenderer", "JU.C"], function () {
c$ = Clazz.declareType (J.render, "EchoRenderer", J.render.LabelsRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
if (this.vwr.isPreviewOnly ()) return false;
var echo = this.shape;
var scalePixelsPerMicron = (this.vwr.getBoolean (603979845) ? this.vwr.getScalePixelsPerAngstrom (true) * 10000 : 0);
this.imageFontScaling = this.vwr.getImageFontScaling ();
var haveTranslucent = false;
for (var t, $t = echo.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) {
if (!t.visible || t.hidden) {
continue;
}if (Clazz.instanceOf (t.pointerPt, JM.Atom)) {
if (!(t.pointerPt).checkVisible ()) continue;
}if (t.valign == 4) {
this.tm.transformPtScr (t.xyz, this.pt0i);
t.setXYZs (this.pt0i.x, this.pt0i.y, this.pt0i.z, this.pt0i.z);
}if (t.movableZPercent != 2147483647) {
var z = this.vwr.tm.zValueFromPercent (t.movableZPercent % 1000);
if (t.valign == 4 && Math.abs (t.movableZPercent) >= 1000) z = this.pt0i.z - this.vwr.tm.zValueFromPercent (0) + z;
t.setZs (z, z);
}if (t.pointerPt == null) {
t.pointer = 0;
} else {
t.pointer = 1;
this.tm.transformPtScr (t.pointerPt, this.pt0i);
t.atomX = this.pt0i.x;
t.atomY = this.pt0i.y;
t.atomZ = this.pt0i.z;
if (t.zSlab == -2147483648) t.zSlab = 1;
}J.render.TextRenderer.render (t, this.vwr, this.g3d, scalePixelsPerMicron, this.imageFontScaling, false, null, this.xy);
if (JU.C.isColixTranslucent (t.bgcolix) || JU.C.isColixTranslucent (t.colix)) haveTranslucent = true;
}
if (!this.isExport) {
var frameTitle = this.vwr.getFrameTitle ();
if (frameTitle != null && frameTitle.length > 0) {
if (this.g3d.setC (this.vwr.getColixBackgroundContrast ())) {
if (frameTitle.indexOf ("%{") >= 0 || frameTitle.indexOf ("@{") >= 0) frameTitle = this.vwr.formatText (frameTitle);
this.renderFrameTitle (frameTitle);
}}}return haveTranslucent;
});
Clazz.defineMethod (c$, "renderFrameTitle", 
 function (frameTitle) {
var fid = this.g3d.getFontFidFS ("Serif", 14 * this.imageFontScaling);
this.g3d.setFontFid (fid);
var y = Clazz.doubleToInt (Math.floor (this.vwr.getScreenHeight () * (this.g3d.isAntialiased () ? 2 : 1) - 10 * this.imageFontScaling));
var x = Clazz.doubleToInt (Math.floor (5 * this.imageFontScaling));
this.g3d.drawStringNoSlab (frameTitle, null, x, y, 0, 0);
}, "~S");
});
