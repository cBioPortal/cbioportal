Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer", "J.util.P3i"], "J.render.EchoRenderer", ["J.render.TextRenderer", "J.util.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.imageFontScaling = 0;
this.ptAtom = null;
this.pt = null;
Clazz.instantialize (this, arguments);
}, J.render, "EchoRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.pt =  new J.util.P3i ();
});
Clazz.overrideMethod (c$, "render", 
function () {
if (this.viewer.isPreviewOnly ()) return false;
var echo = this.shape;
var e = echo.objects.values ().iterator ();
var scalePixelsPerMicron = (this.viewer.getFontScaling () ? this.viewer.getScalePixelsPerAngstrom (true) * 10000 : 0);
this.imageFontScaling = this.viewer.getImageFontScaling ();
var haveTranslucent = false;
while (e.hasNext ()) {
var t = e.next ();
if (!t.visible || t.hidden) {
continue;
}if (t.valign == 4) {
this.viewer.transformPtScr (t.xyz, this.pt);
t.setXYZs (this.pt.x, this.pt.y, this.pt.z, this.pt.z);
} else if (t.movableZPercent != 2147483647) {
var z = this.viewer.zValueFromPercent (t.movableZPercent);
t.setZs (z, z);
}J.render.TextRenderer.render (t, this.g3d, scalePixelsPerMicron, this.imageFontScaling, false, null);
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
