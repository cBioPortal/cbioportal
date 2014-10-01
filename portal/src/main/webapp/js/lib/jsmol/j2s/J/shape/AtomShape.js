Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.Shape"], "J.shape.AtomShape", ["JU.AU", "$.BS", "J.atomdata.RadiusData", "J.c.PAL", "$.VDW", "JU.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.mad = -1;
this.mads = null;
this.colixes = null;
this.paletteIDs = null;
this.ac = 0;
this.atoms = null;
this.isActive = false;
this.monomerCount = 0;
this.bsSizeDefault = null;
this.rd = null;
Clazz.instantialize (this, arguments);
}, J.shape, "AtomShape", J.shape.Shape);
Clazz.defineMethod (c$, "getMonomers", 
function () {
return null;
});
Clazz.overrideMethod (c$, "initModelSet", 
function () {
this.atoms = this.ms.at;
this.ac = this.ms.getAtomCount ();
if (this.mads != null) this.mads = JU.AU.arrayCopyShort (this.mads, this.ac);
if (this.colixes != null) this.colixes = JU.AU.arrayCopyShort (this.colixes, this.ac);
if (this.paletteIDs != null) this.paletteIDs = JU.AU.arrayCopyByte (this.paletteIDs, this.ac);
});
Clazz.overrideMethod (c$, "getSize", 
function (atomIndex) {
return (this.mads == null ? 0 : this.mads[atomIndex]);
}, "~N");
Clazz.overrideMethod (c$, "setSize", 
function (size, bsSelected) {
this.setSize2 (size, bsSelected);
}, "~N,JU.BS");
Clazz.defineMethod (c$, "setSize2", 
function (size, bsSelected) {
if (size == 0) {
this.setSizeRD (null, bsSelected);
return;
}if (this.rd == null) this.rd =  new J.atomdata.RadiusData (null, size, J.atomdata.RadiusData.EnumType.SCREEN, null);
 else this.rd.value = size;
this.setSizeRD (this.rd, bsSelected);
}, "~N,JU.BS");
Clazz.overrideMethod (c$, "setSizeRD", 
function (rd, bsSelected) {
if (this.atoms == null) return;
this.isActive = true;
if (this.bsSizeSet == null) this.bsSizeSet =  new JU.BS ();
var isVisible = (rd != null && rd.value != 0);
var isAll = (bsSelected == null);
var i0 = (isAll ? this.ac - 1 : bsSelected.nextSetBit (0));
if (this.mads == null && i0 >= 0) this.mads =  Clazz.newShortArray (this.ac, 0);
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsSelected.nextSetBit (i + 1))) this.setSizeRD2 (i, rd, isVisible);

}, "J.atomdata.RadiusData,JU.BS");
Clazz.defineMethod (c$, "setSizeRD2", 
function (i, rd, isVisible) {
var atom = this.atoms[i];
this.mads[i] = atom.calculateMad (this.vwr, rd);
this.bsSizeSet.setBitTo (i, isVisible);
this.setShapeVisibility (atom, isVisible);
}, "~N,J.atomdata.RadiusData,~B");
Clazz.defineMethod (c$, "setPropAS", 
function (propertyName, value, bs) {
if ("color" === propertyName) {
this.isActive = true;
var colix = JU.C.getColixO (value);
var pid = J.c.PAL.pidOf (value);
if (this.bsColixSet == null) this.bsColixSet =  new JU.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.setColixAndPalette (colix, pid, i);

return;
}if ("params" === propertyName) {
this.isActive = true;
var data = value;
var colixes = data[0];
var atrans = data[1];
var sizes = data[2];
var rd =  new J.atomdata.RadiusData (null, 0, J.atomdata.RadiusData.EnumType.FACTOR, J.c.VDW.AUTO);
if (this.bsColixSet == null) this.bsColixSet =  new JU.BS ();
if (this.bsSizeSet == null) this.bsSizeSet =  new JU.BS ();
var i0 = bs.nextSetBit (0);
if (this.mads == null && i0 >= 0) this.mads =  Clazz.newShortArray (this.ac, 0);
for (var i = i0, pt = 0; i >= 0; i = bs.nextSetBit (i + 1), pt++) {
var colix = (colixes == null ? 0 : colixes[pt]);
if (colix == 0) colix = 0;
var f = (atrans == null ? 0 : atrans[pt]);
if (f > 0.01) colix = JU.C.getColixTranslucent3 (colix, true, f);
this.setColixAndPalette (colix, J.c.PAL.UNKNOWN.id, i);
if (sizes == null) continue;
var isVisible = ((rd.value = sizes[pt]) > 0);
this.setSizeRD2 (i, rd, isVisible);
}
return;
}if ("translucency" === propertyName) {
this.isActive = true;
var isTranslucent = (value.equals ("translucent"));
if (this.bsColixSet == null) this.bsColixSet =  new JU.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (this.colixes == null) {
this.colixes =  Clazz.newShortArray (this.ac, 0);
this.paletteIDs =  Clazz.newByteArray (this.ac, 0);
}this.colixes[i] = JU.C.getColixTranslucent3 (this.colixes[i], isTranslucent, this.translucentLevel);
if (isTranslucent) this.bsColixSet.set (i);
}
return;
}if (propertyName === "deleteModelAtoms") {
this.atoms = (value)[1];
var info = (value)[2];
this.ac = this.ms.getAtomCount ();
var firstAtomDeleted = info[1];
var nAtomsDeleted = info[2];
this.mads = JU.AU.deleteElements (this.mads, firstAtomDeleted, nAtomsDeleted);
this.colixes = JU.AU.deleteElements (this.colixes, firstAtomDeleted, nAtomsDeleted);
this.paletteIDs = JU.AU.deleteElements (this.paletteIDs, firstAtomDeleted, nAtomsDeleted);
JU.BSUtil.deleteBits (this.bsSizeSet, bs);
JU.BSUtil.deleteBits (this.bsColixSet, bs);
return;
}this.setPropS (propertyName, value, bs);
}, "~S,~O,JU.BS");
Clazz.defineMethod (c$, "setColixAndPalette", 
function (colix, paletteID, atomIndex) {
if (this.colixes == null || atomIndex >= this.colixes.length) {
if (colix == 0) return;
this.colixes = JU.AU.ensureLengthShort (this.colixes, atomIndex + 1);
this.paletteIDs = JU.AU.ensureLengthByte (this.paletteIDs, atomIndex + 1);
}if (this.bsColixSet == null) this.bsColixSet = JU.BS.newN (this.ac);
this.colixes[atomIndex] = colix = this.getColixI (colix, paletteID, atomIndex);
this.bsColixSet.setBitTo (atomIndex, colix != 0);
this.paletteIDs[atomIndex] = paletteID;
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (!this.isActive) return;
for (var i = this.ac; --i >= 0; ) {
var atom = this.atoms[i];
if ((atom.shapeVisibilityFlags & this.vf) == 0 || this.ms.isAtomHidden (i)) continue;
atom.setClickable (this.vf);
}
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return (this.isActive ? this.vwr.getAtomShapeState (this) : "");
});
Clazz.defineMethod (c$, "getInfoAsString", 
function (i) {
return null;
}, "~N");
});
