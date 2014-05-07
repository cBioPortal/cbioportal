Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer"], "J.render.BallsRenderer", ["J.shape.Shape"], function () {
c$ = Clazz.declareType (J.render, "BallsRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
var needTranslucent = false;
if (this.isExport || this.viewer.checkMotionRendering (1141899265)) {
var atoms = this.modelSet.atoms;
var colixes = (this.shape).colixes;
var bsOK = this.viewer.getRenderableBitSet ();
for (var i = bsOK.nextSetBit (0); i >= 0; i = bsOK.nextSetBit (i + 1)) {
var atom = atoms[i];
if (atom.screenDiameter > 0 && (atom.getShapeVisibilityFlags () & this.myVisibilityFlag) != 0) {
if (this.g3d.setColix (colixes == null ? atom.getColix () : J.shape.Shape.getColix (colixes, i, atom))) {
this.g3d.drawAtom (atom);
} else {
needTranslucent = true;
}}}
}return needTranslucent;
});
});
