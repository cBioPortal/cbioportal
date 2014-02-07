Clazz.declarePackage ("J.viewer");
Clazz.load (["J.util.ColorEncoder"], "J.viewer.ColorManager", ["java.lang.Float", "J.constant.EnumPalette", "J.util.ArrayUtil", "$.C", "$.ColorUtil", "$.Elements", "$.Logger", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.propertyColorEncoder = null;
this.viewer = null;
this.g3d = null;
this.argbsCpk = null;
this.altArgbsCpk = null;
this.colorData = null;
this.isDefaultColorRasmol = false;
this.colixRubberband = 22;
this.colixBackgroundContrast = 0;
Clazz.instantialize (this, arguments);
}, J.viewer, "ColorManager");
Clazz.prepareFields (c$, function () {
this.propertyColorEncoder =  new J.util.ColorEncoder (null);
});
Clazz.makeConstructor (c$, 
function (viewer, gdata) {
this.viewer = viewer;
this.g3d = gdata;
this.argbsCpk = J.constant.EnumPalette.argbsCpk;
this.altArgbsCpk = J.util.ArrayUtil.arrayCopyRangeI (J.viewer.JC.altArgbsCpk, 0, -1);
}, "J.viewer.Viewer,J.util.GData");
$_M(c$, "clear", 
function () {
});
$_M(c$, "getDefaultColorRasmol", 
function () {
return this.isDefaultColorRasmol;
});
$_M(c$, "resetElementColors", 
function () {
this.setDefaultColors (false);
});
$_M(c$, "setDefaultColors", 
function (isRasmol) {
if (isRasmol) {
this.isDefaultColorRasmol = true;
this.argbsCpk = J.util.ArrayUtil.arrayCopyI (J.util.ColorEncoder.getRasmolScale (), -1);
} else {
this.isDefaultColorRasmol = false;
this.argbsCpk = J.constant.EnumPalette.argbsCpk;
}this.altArgbsCpk = J.util.ArrayUtil.arrayCopyRangeI (J.viewer.JC.altArgbsCpk, 0, -1);
this.propertyColorEncoder.createColorScheme ((isRasmol ? "Rasmol=" : "Jmol="), true, true);
for (var i = J.constant.EnumPalette.argbsCpk.length; --i >= 0; ) this.g3d.changeColixArgb (i, this.argbsCpk[i]);

for (var i = J.viewer.JC.altArgbsCpk.length; --i >= 0; ) this.g3d.changeColixArgb (J.util.Elements.elementNumberMax + i, this.altArgbsCpk[i]);

}, "~B");
$_M(c$, "setRubberbandArgb", 
function (argb) {
this.colixRubberband = (argb == 0 ? 0 : J.util.C.getColix (argb));
}, "~N");
$_M(c$, "setColixBackgroundContrast", 
function (argb) {
this.colixBackgroundContrast = J.util.ColorUtil.getBgContrast (argb);
}, "~N");
$_M(c$, "getColixBondPalette", 
function (bond, pid) {
var argb = 0;
switch (pid) {
case 19:
return this.propertyColorEncoder.getColorIndexFromPalette (bond.getEnergy (), -2.5, -0.5, 7, false);
}
return (argb == 0 ? 10 : J.util.C.getColix (argb));
}, "J.modelset.Bond,~N");
$_M(c$, "getColixAtomPalette", 
function (atom, pid) {
var argb = 0;
var index;
var id;
var modelSet;
var modelIndex;
var lo;
var hi;
switch (pid) {
case 84:
return (this.colorData == null || atom.index >= this.colorData.length ? 12 : this.getColixForPropertyValue (this.colorData[atom.index]));
case 0:
case 1:
id = atom.getAtomicAndIsotopeNumber ();
if (id < J.util.Elements.elementNumberMax) return this.g3d.getChangeableColix (id, this.argbsCpk[id]);
var id0 = id;
id = J.util.Elements.altElementIndexFromNumber (id);
if (id == 0) {
id = J.util.Elements.getElementNumber (id0);
return this.g3d.getChangeableColix (id, this.argbsCpk[id]);
}return this.g3d.getChangeableColix (J.util.Elements.elementNumberMax + id, this.altArgbsCpk[id]);
case 2:
index = J.util.ColorEncoder.quantize (atom.getPartialCharge (), -1, 1, J.viewer.JC.PARTIAL_CHARGE_RANGE_SIZE);
return this.g3d.getChangeableColix (J.viewer.JC.PARTIAL_CHARGE_COLIX_RED + index, J.viewer.JC.argbsRwbScale[index]);
case 3:
index = atom.getFormalCharge () - -4;
return this.g3d.getChangeableColix (J.viewer.JC.FORMAL_CHARGE_COLIX_RED + index, J.viewer.JC.argbsFormalCharge[index]);
case 68:
case 5:
if (pid == 68) {
modelSet = this.viewer.getModelSet ();
lo = modelSet.getBfactor100Lo ();
hi = modelSet.getBfactor100Hi ();
} else {
lo = 0;
hi = 10000;
}return this.propertyColorEncoder.getColorIndexFromPalette (atom.getBfactor100 (), lo, hi, 7, false);
case 86:
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getGroupParameter (1112539150), -1, 1, 7, false);
case 70:
hi = this.viewer.getSurfaceDistanceMax ();
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getSurfaceDistance100 (), 0, hi, 7, false);
case 8:
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getGroupID (), 0, 0, 5, false);
case 9:
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getGroupID (), 0, 0, 4, false);
case 75:
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getSelectedGroupIndexWithinChain (), 0, atom.getSelectedGroupCountWithinChain () - 1, 1, false);
case 87:
var m = this.viewer.getModelSet ().models[atom.modelIndex];
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getPolymerIndexInModel (), 0, m.getBioPolymerCount () - 1, 1, false);
case 76:
return this.propertyColorEncoder.getColorIndexFromPalette (atom.getSelectedMonomerIndexWithinPolymer (), 0, atom.getSelectedMonomerCountWithinPolymer () - 1, 1, false);
case 77:
modelSet = this.viewer.getModelSet ();
return this.propertyColorEncoder.getColorIndexFromPalette (modelSet.getMoleculeIndex (atom.getIndex (), true), 0, modelSet.getMoleculeCountInModel (atom.getModelIndex ()) - 1, 0, false);
case 14:
modelSet = this.viewer.getModelSet ();
modelIndex = atom.getModelIndex ();
return this.propertyColorEncoder.getColorIndexFromPalette (modelSet.getAltLocIndexInModel (modelIndex, atom.getAlternateLocationID ()), 0, modelSet.getAltLocCountInModel (modelIndex), 0, false);
case 15:
modelSet = this.viewer.getModelSet ();
modelIndex = atom.getModelIndex ();
return this.propertyColorEncoder.getColorIndexFromPalette (modelSet.getInsertionCodeIndexInModel (modelIndex, atom.getInsertionCode ()), 0, modelSet.getInsertionCountInModel (modelIndex), 0, false);
case 16:
id = atom.getAtomicAndIsotopeNumber ();
argb = this.getJmolOrRasmolArgb (id, 1073741992);
break;
case 17:
id = atom.getAtomicAndIsotopeNumber ();
argb = this.getJmolOrRasmolArgb (id, 1073742116);
break;
case 7:
argb = atom.getProteinStructureSubType ().getColor ();
break;
case 10:
var chain = atom.getChainID ();
if (chain < 0) chain = 0;
 else if (chain >= 256) chain -= 256;
chain &= 0x1F;
chain = chain % J.viewer.JC.argbsChainAtom.length;
argb = (atom.isHetero () ? J.viewer.JC.argbsChainHetero : J.viewer.JC.argbsChainAtom)[chain];
break;
}
return (argb == 0 ? 22 : J.util.C.getColix (argb));
}, "J.modelset.Atom,~N");
$_M(c$, "getJmolOrRasmolArgb", 
($fz = function (id, argb) {
switch (argb) {
case 1073741992:
if (id >= J.util.Elements.elementNumberMax) break;
return this.propertyColorEncoder.getArgbFromPalette (id, 0, 0, 2);
case 1073742116:
if (id >= J.util.Elements.elementNumberMax) break;
return this.propertyColorEncoder.getArgbFromPalette (id, 0, 0, 3);
default:
return argb;
}
return J.viewer.JC.altArgbsCpk[J.util.Elements.altElementIndexFromNumber (id)];
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setElementArgb", 
function (id, argb) {
if (argb == 1073741992 && this.argbsCpk === J.constant.EnumPalette.argbsCpk) return;
argb = this.getJmolOrRasmolArgb (id, argb);
if (this.argbsCpk === J.constant.EnumPalette.argbsCpk) {
this.argbsCpk = J.util.ArrayUtil.arrayCopyRangeI (J.constant.EnumPalette.argbsCpk, 0, -1);
this.altArgbsCpk = J.util.ArrayUtil.arrayCopyRangeI (J.viewer.JC.altArgbsCpk, 0, -1);
}if (id < J.util.Elements.elementNumberMax) {
this.argbsCpk[id] = argb;
this.g3d.changeColixArgb (id, argb);
return;
}id = J.util.Elements.altElementIndexFromNumber (id);
this.altArgbsCpk[id] = argb;
this.g3d.changeColixArgb (J.util.Elements.elementNumberMax + id, argb);
}, "~N,~N");
$_M(c$, "getPropertyColorRange", 
function () {
if (this.propertyColorEncoder.isReversed) return [this.propertyColorEncoder.hi, this.propertyColorEncoder.lo];
return [this.propertyColorEncoder.lo, this.propertyColorEncoder.hi];
});
$_M(c$, "setPropertyColorRangeData", 
function (data, bs, colorScheme) {
this.colorData = data;
this.propertyColorEncoder.currentPalette = this.propertyColorEncoder.createColorScheme (colorScheme, true, false);
this.propertyColorEncoder.hi = 1.4E-45;
this.propertyColorEncoder.lo = 3.4028235E38;
if (data == null) return;
var isAll = (bs == null);
var d;
var i0 = (isAll ? data.length - 1 : bs.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bs.nextSetBit (i + 1))) {
if (Float.isNaN (d = data[i])) continue;
this.propertyColorEncoder.hi = Math.max (this.propertyColorEncoder.hi, d);
this.propertyColorEncoder.lo = Math.min (this.propertyColorEncoder.lo, d);
}
this.setPropertyColorRange (this.propertyColorEncoder.lo, this.propertyColorEncoder.hi);
}, "~A,J.util.BS,~S");
$_M(c$, "setPropertyColorRange", 
function (min, max) {
this.propertyColorEncoder.setRange (min, max, min > max);
J.util.Logger.info ("ColorManager: color \"" + this.propertyColorEncoder.getCurrentColorSchemeName () + "\" range " + min + " " + max);
}, "~N,~N");
$_M(c$, "setPropertyColorScheme", 
function (colorScheme, isTranslucent, isOverloaded) {
var isReset = (colorScheme.length == 0);
if (isReset) colorScheme = "=";
var range = this.getPropertyColorRange ();
this.propertyColorEncoder.currentPalette = this.propertyColorEncoder.createColorScheme (colorScheme, true, isOverloaded);
if (!isReset) this.setPropertyColorRange (range[0], range[1]);
this.propertyColorEncoder.isTranslucent = isTranslucent;
}, "~S,~B,~B");
$_M(c$, "setUserScale", 
function (scale) {
this.propertyColorEncoder.setUserScale (scale);
}, "~A");
$_M(c$, "getColorSchemeList", 
function (colorScheme) {
var iPt = (colorScheme == null || colorScheme.length == 0) ? this.propertyColorEncoder.currentPalette : this.propertyColorEncoder.createColorScheme (colorScheme, true, false);
return J.util.ColorEncoder.getColorSchemeList (this.propertyColorEncoder.getColorSchemeArray (iPt));
}, "~S");
$_M(c$, "getColixForPropertyValue", 
function (val) {
return this.propertyColorEncoder.getColorIndex (val);
}, "~N");
$_M(c$, "getColorEncoder", 
function (colorScheme) {
if (colorScheme == null || colorScheme.length == 0) return this.propertyColorEncoder;
var ce =  new J.util.ColorEncoder (this.propertyColorEncoder);
ce.currentPalette = ce.createColorScheme (colorScheme, false, true);
return (ce.currentPalette == 2147483647 ? null : ce);
}, "~S");
});
