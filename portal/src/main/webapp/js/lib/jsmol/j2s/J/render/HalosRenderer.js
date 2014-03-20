Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer"], "J.render.HalosRenderer", ["J.util.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isAntialiased = false;
Clazz.instantialize (this, arguments);
}, J.render, "HalosRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
var halos = this.shape;
var selectDisplayTrue = this.viewer.getSelectionHaloEnabled (true);
var showHiddenSelections = (selectDisplayTrue && this.viewer.getBoolean (603979920));
if (halos.mads == null && halos.bsHighlight == null && !selectDisplayTrue) return false;
this.isAntialiased = this.g3d.isAntialiased ();
var atoms = this.modelSet.atoms;
var bsSelected = (selectDisplayTrue ? this.viewer.getSelectionSet (false) : null);
var needTranslucent = false;
for (var i = this.modelSet.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if ((atom.getShapeVisibilityFlags () & 1) == 0) continue;
var isHidden = this.modelSet.isAtomHidden (i);
this.mad = (halos.mads == null ? 0 : halos.mads[i]);
this.colix = (halos.colixes == null || i >= halos.colixes.length ? 0 : halos.colixes[i]);
if (selectDisplayTrue && bsSelected.get (i)) {
if (isHidden && !showHiddenSelections) continue;
if (this.mad == 0) this.mad = -1;
if (this.colix == 0) this.colix = halos.colixSelection;
if (this.colix == 2) this.colix = 23;
 else if (this.colix == 0) this.colix = J.util.C.getColixInherited (this.colix, atom.getColix ());
} else if (isHidden) {
continue;
} else {
this.colix = J.util.C.getColixInherited (this.colix, atom.getColix ());
}if (this.mad != 0) {
if (this.render1 (atom)) needTranslucent = true;
}if (!isHidden && halos.bsHighlight != null && halos.bsHighlight.get (i)) {
this.mad = -2;
this.colix = halos.colixHighlight;
if (this.render1 (atom)) needTranslucent = true;
}}
return needTranslucent;
});
$_M(c$, "render1", 
function (atom) {
var colixFill = (this.mad == -2 ? 0 : J.util.C.getColixTranslucent3 (this.colix, true, 0.5));
var needTranslucent = (this.mad != -2);
if (!this.g3d.setColix (this.colix)) {
needTranslucent = true;
this.colix = 0;
if (colixFill == 0 || !this.g3d.setColix (colixFill)) return needTranslucent;
}var z = atom.screenZ;
var d = this.mad;
if (d < 0) {
d = atom.screenDiameter;
if (d == 0) {
var ellipsemax = (atom.isShapeVisible (20) ? atom.getADPMinMax (true) : 0);
if (ellipsemax > 0) d = this.viewer.scaleToScreen (z, Clazz.doubleToInt (Math.floor (ellipsemax * 2000)));
if (d == 0) {
d = Clazz.floatToInt (this.viewer.scaleToScreen (z, this.mad == -2 ? 250 : 500));
}}} else {
d = this.viewer.scaleToScreen (z, this.mad);
}if (this.isAntialiased) d /= 2;
var more = (d / 2);
if (this.mad == -2) more /= 2;
if (more < 8) more = 8;
if (more > 20) more = 20;
d += more;
if (this.isAntialiased) d *= 2;
if (d < 1) return false;
this.g3d.drawFilledCircle (this.colix, colixFill, Clazz.doubleToInt (Math.floor (d)), atom.screenX, atom.screenY, atom.screenZ);
return needTranslucent;
}, "J.modelset.Atom");
});
