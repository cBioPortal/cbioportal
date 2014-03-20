Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.Shape"], "J.shape.AtomShape", ["J.atomdata.RadiusData", "J.constant.EnumPalette", "$.EnumVdw", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.mad = -1;
this.mads = null;
this.colixes = null;
this.paletteIDs = null;
this.atomCount = 0;
this.atoms = null;
this.isActive = false;
this.monomerCount = 0;
this.bsSizeDefault = null;
this.rd = null;
Clazz.instantialize (this, arguments);
}, J.shape, "AtomShape", J.shape.Shape);
$_M(c$, "getMonomers", 
function () {
return null;
});
Clazz.overrideMethod (c$, "initModelSet", 
function () {
this.atoms = this.modelSet.atoms;
this.atomCount = this.modelSet.getAtomCount ();
if (this.mads != null) this.mads = J.util.ArrayUtil.arrayCopyShort (this.mads, this.atomCount);
if (this.colixes != null) this.colixes = J.util.ArrayUtil.arrayCopyShort (this.colixes, this.atomCount);
if (this.paletteIDs != null) this.paletteIDs = J.util.ArrayUtil.arrayCopyByte (this.paletteIDs, this.atomCount);
});
Clazz.overrideMethod (c$, "getSize", 
function (atomIndex) {
return (this.mads == null ? 0 : this.mads[atomIndex]);
}, "~N");
Clazz.overrideMethod (c$, "setSize", 
function (size, bsSelected) {
this.setSize2 (size, bsSelected);
}, "~N,J.util.BS");
$_M(c$, "setSize2", 
function (size, bsSelected) {
if (size == 0) {
this.setSizeRD (null, bsSelected);
return;
}if (this.rd == null) this.rd =  new J.atomdata.RadiusData (null, size, J.atomdata.RadiusData.EnumType.SCREEN, null);
 else this.rd.value = size;
this.setSizeRD (this.rd, bsSelected);
}, "~N,J.util.BS");
Clazz.overrideMethod (c$, "setSizeRD", 
function (rd, bsSelected) {
if (this.atoms == null) return;
this.isActive = true;
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
var isVisible = (rd != null && rd.value != 0);
var isAll = (bsSelected == null);
var i0 = (isAll ? this.atomCount - 1 : bsSelected.nextSetBit (0));
if (this.mads == null && i0 >= 0) this.mads =  Clazz.newShortArray (this.atomCount, 0);
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsSelected.nextSetBit (i + 1))) this.setSizeRD2 (i, rd, isVisible);

}, "J.atomdata.RadiusData,J.util.BS");
$_M(c$, "setSizeRD2", 
function (i, rd, isVisible) {
var atom = this.atoms[i];
this.mads[i] = atom.calculateMad (this.viewer, rd);
this.bsSizeSet.setBitTo (i, isVisible);
atom.setShapeVisibility (this.myVisibilityFlag, isVisible);
}, "~N,J.atomdata.RadiusData,~B");
$_M(c$, "setPropAS", 
function (propertyName, value, bs) {
if ("color" === propertyName) {
this.isActive = true;
var colix = J.util.C.getColixO (value);
var pid = J.constant.EnumPalette.pidOf (value);
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.setColixAndPalette (colix, pid, i);

return;
}if ("params" === propertyName) {
this.isActive = true;
var data = value;
var colixes = data[0];
var atrans = data[1];
var sizes = data[2];
var rd =  new J.atomdata.RadiusData (null, 0, J.atomdata.RadiusData.EnumType.FACTOR, J.constant.EnumVdw.AUTO);
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
var i0 = bs.nextSetBit (0);
if (this.mads == null && i0 >= 0) this.mads =  Clazz.newShortArray (this.atomCount, 0);
for (var i = i0, pt = 0; i >= 0; i = bs.nextSetBit (i + 1), pt++) {
var colix = (colixes == null ? 0 : colixes[pt]);
if (colix == 0) colix = 0;
var f = (atrans == null ? 0 : atrans[pt]);
if (f > 0.01) colix = J.util.C.getColixTranslucent3 (colix, true, f);
this.setColixAndPalette (colix, J.constant.EnumPalette.UNKNOWN.id, i);
if (sizes == null) continue;
var isVisible = ((rd.value = sizes[pt]) > 0);
this.setSizeRD2 (i, rd, isVisible);
}
return;
}if ("translucency" === propertyName) {
this.isActive = true;
var isTranslucent = (value.equals ("translucent"));
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (this.colixes == null) {
this.colixes =  Clazz.newShortArray (this.atomCount, 0);
this.paletteIDs =  Clazz.newByteArray (this.atomCount, 0);
}this.colixes[i] = J.util.C.getColixTranslucent3 (this.colixes[i], isTranslucent, this.translucentLevel);
if (isTranslucent) this.bsColixSet.set (i);
}
return;
}if (propertyName === "deleteModelAtoms") {
this.atoms = (value)[1];
var info = (value)[2];
this.atomCount = this.modelSet.getAtomCount ();
var firstAtomDeleted = info[1];
var nAtomsDeleted = info[2];
this.mads = J.util.ArrayUtil.deleteElements (this.mads, firstAtomDeleted, nAtomsDeleted);
this.colixes = J.util.ArrayUtil.deleteElements (this.colixes, firstAtomDeleted, nAtomsDeleted);
this.paletteIDs = J.util.ArrayUtil.deleteElements (this.paletteIDs, firstAtomDeleted, nAtomsDeleted);
J.util.BSUtil.deleteBits (this.bsSizeSet, bs);
J.util.BSUtil.deleteBits (this.bsColixSet, bs);
return;
}this.setPropS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "setColixAndPalette", 
function (colix, paletteID, atomIndex) {
if (this.colixes == null || atomIndex >= this.colixes.length) {
if (colix == 0) return;
this.colixes = J.util.ArrayUtil.ensureLengthShort (this.colixes, atomIndex + 1);
this.paletteIDs = J.util.ArrayUtil.ensureLengthByte (this.paletteIDs, atomIndex + 1);
}if (this.bsColixSet == null) this.bsColixSet = J.util.BS.newN (this.atomCount);
this.colixes[atomIndex] = colix = this.getColixI (colix, paletteID, atomIndex);
this.bsColixSet.setBitTo (atomIndex, colix != 0);
this.paletteIDs[atomIndex] = paletteID;
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (!this.isActive) return;
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
if ((atom.getShapeVisibilityFlags () & this.myVisibilityFlag) == 0 || this.modelSet.isAtomHidden (i)) continue;
atom.setClickable (this.myVisibilityFlag);
}
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return (this.isActive ? this.viewer.getAtomShapeState (this) : "");
});
$_M(c$, "getInfoAsString", 
function (i) {
return null;
}, "~N");
});
