Clazz.declarePackage ("J.render");
Clazz.load (["J.render.LabelsRenderer"], "J.render.EchoRenderer", ["J.render.TextRenderer", "J.util.C"], function () {
c$ = Clazz.declareType (J.render, "EchoRenderer", J.render.LabelsRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
if (this.viewer.isPreviewOnly ()) return false;
var echo = this.shape;
var scalePixelsPerMicron = (this.viewer.getBoolean (603979845) ? this.viewer.getScalePixelsPerAngstrom (true) * 10000 : 0);
this.imageFontScaling = this.viewer.getImageFontScaling ();
var haveTranslucent = false;
for (var t, $t = echo.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) {
if (!t.visible || t.hidden) {
continue;
}if (Clazz.instanceOf (t.pointerPt, J.modelset.Atom)) {
if (!(t.pointerPt).isVisible (-1)) continue;
}if (t.valign == 4) {
this.viewer.transformPtScr (t.xyz, this.pt0i);
t.setXYZs (this.pt0i.x, this.pt0i.y, this.pt0i.z, this.pt0i.z);
} else if (t.movableZPercent != 2147483647) {
var z = this.viewer.zValueFromPercent (t.movableZPercent);
t.setZs (z, z);
}if (t.pointerPt == null) {
t.pointer = 0;
} else {
t.pointer = 1;
this.viewer.transformPtScr (t.pointerPt, this.pt0i);
t.atomX = this.pt0i.x;
t.atomY = this.pt0i.y;
t.atomZ = this.pt0i.z;
if (t.zSlab == -2147483648) t.zSlab = 1;
}J.render.TextRenderer.render (t, this.viewer, this.g3d, scalePixelsPerMicron, this.imageFontScaling, false, null, this.xy);
if (J.util.C.isColixTranslucent (t.bgcolix) || J.util.C.isColixTranslucent (t.colix)) haveTranslucent = true;
}
if (!this.isExport) {
var frameTitle = this.viewer.getFrameTitle ();
if (frameTitle != null && frameTitle.length > 0) {
if (this.g3d.setColix (this.viewer.getColixBackgroundContrast ())) {
if (frameTitle.indexOf ("%{") >= 0 || frameTitle.indexOf ("@{") >= 0) frameTitle = this.viewer.formatText (frameTitle);
this.renderFrameTitle (frameTitle);
}}}return haveTranslucent;
});
$_M(c$, "renderFrameTitle", 
($fz = function (frameTitle) {
var fid = this.g3d.getFontFidFS ("Serif", 14 * this.imageFontScaling);
this.g3d.setFontFid (fid);
var y = Clazz.doubleToInt (Math.floor (this.viewer.getScreenHeight () * (this.g3d.isAntialiased () ? 2 : 1) - 10 * this.imageFontScaling));
var x = Clazz.doubleToInt (Math.floor (5 * this.imageFontScaling));
this.g3d.drawStringNoSlab (frameTitle, null, x, y, 0, 0);
}, $fz.isPrivate = true, $fz), "~S");
});
