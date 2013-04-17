Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer"], "J.render.StarsRenderer", ["J.shape.Shape"], function () {
c$ = Clazz.declareType (J.render, "StarsRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
var stars = this.shape;
if (stars.mads == null) return false;
var needTranslucent = false;
var atoms = this.modelSet.atoms;
for (var i = this.modelSet.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if (!atom.isVisible (this.myVisibilityFlag)) continue;
this.colix = J.shape.Shape.getColix (stars.colixes, i, atom);
if (this.g3d.setColix (this.colix)) this.render1 (atom, stars.mads[i]);
 else needTranslucent = true;
}
return needTranslucent;
});
$_M(c$, "render1", 
($fz = function (atom, mad) {
var x = atom.screenX;
var y = atom.screenY;
var z = atom.screenZ;
var d = this.viewer.scaleToScreen (z, mad);
d -= (d & 1) ^ 1;
var r = Clazz.doubleToInt (d / 2);
this.g3d.drawLineXYZ (x - r, y, z, x - r + d, y, z);
this.g3d.drawLineXYZ (x, y - r, z, x, y - r + d, z);
this.g3d.drawLineXYZ (x, y, z - r, x, y, z - r + d);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N");
});
