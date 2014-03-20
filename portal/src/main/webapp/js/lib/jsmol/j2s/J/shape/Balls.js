Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.AtomShape"], "J.shape.Balls", ["J.constant.EnumPalette", "J.util.BS", "$.C"], function () {
c$ = Clazz.declareType (J.shape, "Balls", J.shape.AtomShape);
Clazz.overrideMethod (c$, "setSizeRD", 
function (rd, bsSelected) {
this.isActive = true;
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
var bsLength = Math.min (this.atoms.length, bsSelected.length ());
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < bsLength; i = bsSelected.nextSetBit (i + 1)) {
var atom = this.atoms[i];
atom.setMadAtom (this.viewer, rd);
this.bsSizeSet.set (i);
}
}, "J.atomdata.RadiusData,J.util.BS");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("color" === propertyName) {
var colix = J.util.C.getColixO (value);
if (colix == 0) colix = 2;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
var pid = J.constant.EnumPalette.pidOf (value);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var atom = this.atoms[i];
atom.setColixAtom (this.getColixA (colix, pid, atom));
this.bsColixSet.setBitTo (i, colix != 2 || pid != J.constant.EnumPalette.NONE.id);
atom.setPaletteID (pid);
}
return;
}if ("colorValues" === propertyName) {
var values = value;
if (values.length == 0) return;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
var n = 0;
var color = null;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (n >= values.length) return;
color = Integer.$valueOf (values[n++]);
var colix = J.util.C.getColixO (color);
if (colix == 0) colix = 2;
var pid = J.constant.EnumPalette.pidOf (color);
var atom = this.atoms[i];
atom.setColixAtom (this.getColixA (colix, pid, atom));
this.bsColixSet.setBitTo (i, colix != 2 || pid != J.constant.EnumPalette.NONE.id);
atom.setPaletteID (pid);
}
return;
}if ("colors" === propertyName) {
var data = value;
var colixes = data[0];
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (i >= colixes.length) continue;
this.atoms[i].setColixAtom (colixes[i]);
this.atoms[i].setPaletteID (J.constant.EnumPalette.UNKNOWN.id);
this.bsColixSet.set (i);
}
return;
}if ("translucency" === propertyName) {
var isTranslucent = ((value).equals ("translucent"));
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
this.atoms[i].setTranslucent (isTranslucent, this.translucentLevel);
if (isTranslucent) this.bsColixSet.set (i);
}
return;
}if (propertyName.startsWith ("ball")) {
propertyName = propertyName.substring (4).intern ();
}this.setPropAS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
var bsDeleted = this.viewer.getDeletedAtoms ();
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
atom.setClickable (0);
if (bsDeleted != null && bsDeleted.get (i) || (atom.getShapeVisibilityFlags () & this.myVisibilityFlag) == 0 || this.modelSet.isAtomHidden (i)) continue;
atom.setClickable (this.myVisibilityFlag);
}
});
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
var showHydrogens = this.viewer.getBoolean (603979922);
var bsDeleted = this.viewer.getDeletedAtoms ();
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
var flag = atom.getShapeVisibilityFlags ();
flag &= (-2 & ~this.myVisibilityFlag);
atom.setShapeVisibilityFlags (flag);
if (bsDeleted != null && bsDeleted.get (i) || !showHydrogens && atom.getElementNumber () == 1) continue;
var modelIndex = atom.getModelIndex ();
if (bs.get (modelIndex)) {
atom.setShapeVisibility (1, true);
if (atom.madAtom != 0 && !this.modelSet.isAtomHidden (i)) atom.setShapeVisibility (this.myVisibilityFlag, true);
}}
}, "J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getShapeState (this);
});
});
