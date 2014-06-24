Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer"], "J.render.FrankRenderer", ["J.util.C"], function () {
c$ = Clazz.declareType (J.render, "FrankRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
var frank = this.shape;
var allowKeys = this.viewer.getBooleanProperty ("allowKeyStrokes");
var modelKitMode = this.viewer.getBoolean (603979883);
this.colix = (modelKitMode ? 20 : this.viewer.isSignedApplet () ? (allowKeys || this.viewer.isJS && !this.viewer.isWebGL ? 5 : 10) : allowKeys ? 7 : 12);
if (this.isExport || !this.viewer.getShowFrank ()) return false;
if (!this.g3d.setColix (J.util.C.getColixTranslucent3 (this.colix, this.g3d.haveTranslucentObjects (), 0.5))) return true;
var imageFontScaling = this.viewer.getImageFontScaling ();
frank.getFont (imageFontScaling);
var dx = Clazz.floatToInt (frank.frankWidth + 4 * imageFontScaling);
var dy = frank.frankDescent;
this.g3d.drawStringNoSlab (frank.frankString, frank.font3d, this.g3d.getRenderWidth () - dx, this.g3d.getRenderHeight () - dy, 0, 0);
if (modelKitMode) {
this.g3d.fillRect (0, 0, 0, 0, dy * 2, Clazz.doubleToInt (dx * 3 / 2));
}return false;
});
});
