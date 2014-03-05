Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.Shape", "J.util.P3"], "J.shapespecial.Dipoles", ["java.lang.Float", "java.util.Hashtable", "J.script.T", "J.shapespecial.Dipole", "J.util.ArrayUtil", "$.BSUtil", "$.C", "$.JmolList", "$.Logger", "$.SB", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.dipoleCount = 0;
this.dipoles = null;
this.currentDipole = null;
this.tempDipole = null;
this.startCoord = null;
this.endCoord = null;
this.dipoleValue = 0;
this.isUserValue = false;
this.isBond = false;
this.iHaveTwoEnds = false;
this.atomIndex1 = 0;
this.atomIndex2 = 0;
this.colix = 0;
this.calculatedDipole = null;
this.wildID = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Dipoles", J.shape.Shape);
Clazz.prepareFields (c$, function () {
this.dipoles =  new Array (4);
this.startCoord =  new J.util.P3 ();
this.endCoord =  new J.util.P3 ();
});
$_M(c$, "setProperty", 
function (propertyName, value, bs) {
if ("init" === propertyName) {
this.tempDipole =  new J.shapespecial.Dipole ();
this.tempDipole.dipoleValue = 1;
this.tempDipole.mad = 5;
this.atomIndex1 = -1;
this.tempDipole.modelIndex = -1;
this.dipoleValue = 0;
this.calculatedDipole = null;
this.isUserValue = this.isBond = this.iHaveTwoEnds = false;
return;
}if ("calculate" === propertyName) {
this.calculatedDipole = this.viewer.calculateMolecularDipole ();
J.util.Logger.info ("calculated molecular dipole = " + this.calculatedDipole + " " + (this.calculatedDipole == null ? "" : "" + this.calculatedDipole.length ()));
return;
}if ("thisID" === propertyName) {
this.wildID = null;
var thisID = value;
if (thisID == null || J.util.TextFormat.isWild (thisID)) {
this.currentDipole = null;
if (thisID != null) this.wildID = thisID.toUpperCase ();
return;
}this.currentDipole = this.findDipole (thisID);
if (this.currentDipole == null) this.currentDipole = this.allocDipole (thisID, "");
this.tempDipole = this.currentDipole;
if (thisID.equals ("molecular")) {
var v = this.calculatedDipole;
if (v == null) {
v = this.viewer.getModelDipole ();
J.util.Logger.info ("file molecular dipole = " + v + " " + (v != null ? "" + v.length () : ""));
}if (v == null) this.calculatedDipole = v = this.viewer.calculateMolecularDipole ();
if (v == null) {
J.util.Logger.warn ("No molecular dipole found for this model; setting to {0 0 0}");
v =  new J.util.V3 ();
}this.tempDipole.set (J.util.P3.new3 (0, 0, 0), J.util.V3.new3 (-v.x, -v.y, -v.z));
this.tempDipole.type = 4;
this.tempDipole.thisID = "molecular";
this.setDipole ();
}return;
}if ("bonds" === propertyName) {
this.isBond = true;
this.currentDipole = null;
for (var i = this.dipoleCount; --i >= 0; ) if (this.isBondDipole (i)) return;

this.getBondDipoles ();
return;
}if ("on" === propertyName) {
this.setProperty (1048589, this.isBond, 0, 0);
return;
}if ("off" === propertyName) {
this.setProperty (1048588, this.isBond, 0, 0);
return;
}if ("delete" === propertyName) {
if (this.wildID == null && this.currentDipole == null) {
this.clear (false);
return;
}this.setProperty (12291, this.isBond, 0, 0);
return;
}if ("width" === propertyName) {
var mad = this.tempDipole.mad = Clazz.floatToShort ((value).floatValue () * 1000);
if (this.currentDipole == null) this.setProperty (659488, this.isBond, mad, 0);
return;
}if ("offset" === propertyName) {
var offset = this.tempDipole.offsetAngstroms = (value).floatValue ();
if (this.currentDipole == null) this.setProperty (1611272194, this.isBond, 0, offset);
return;
}if ("offsetPercent" === propertyName) {
var offsetPercent = this.tempDipole.offsetPercent = (value).intValue ();
if (this.tempDipole.dipoleValue != 0) this.tempDipole.offsetAngstroms = offsetPercent / 100 * this.tempDipole.dipoleValue;
if (this.currentDipole == null) this.setProperty (269484210, this.isBond, 0, offsetPercent / 100);
return;
}if ("offsetSide" === propertyName) {
var offsetSide = (value).floatValue ();
this.setProperty (3145754, this.isBond, 0, offsetSide);
return;
}if ("cross" === propertyName) {
this.setProperty (135267329, this.isBond, ((value).booleanValue () ? 1 : 0), 0);
return;
}if ("color" === propertyName) {
this.colix = J.util.C.getColixO (value);
if (this.isBond) {
this.setColixDipole (this.colix, 1023, bs);
} else if (value != null) {
this.setProperty (1766856708, false, 0, 0);
}return;
}if ("translucency" === propertyName) {
this.setProperty (603979967, this.isBond, (value.equals ("translucent") ? 1 : 0), 0);
return;
}if ("clear" === propertyName) {
this.currentDipole = null;
this.clear (false);
}if ("clearBonds" === propertyName) {
this.clear (true);
}if ("startSet" === propertyName) {
var atomset = value;
this.startCoord = this.viewer.getAtomSetCenter (atomset);
this.tempDipole.set (this.startCoord, J.util.P3.new3 (0, 0, 0), this.dipoleValue);
if (J.util.BSUtil.cardinalityOf (atomset) == 1) this.atomIndex1 = atomset.nextSetBit (0);
return;
}if ("atomBitset" === propertyName) {
var atomset = value;
this.atomIndex1 = atomset.nextSetBit (0);
this.startCoord = this.modelSet.atoms[this.atomIndex1];
atomset.clear (this.atomIndex1);
propertyName = "endSet";
}if ("endSet" === propertyName) {
this.iHaveTwoEnds = true;
var atomset = value;
if (this.atomIndex1 >= 0 && J.util.BSUtil.cardinalityOf (atomset) == 1) {
this.atomIndex2 = atomset.nextSetBit (0);
this.tempDipole.set (this.modelSet.atoms[this.atomIndex1], this.modelSet.atoms[this.atomIndex2], 1);
this.currentDipole = this.findDipole (this.tempDipole.thisID, this.tempDipole.dipoleInfo);
this.tempDipole.thisID = this.currentDipole.thisID;
if (this.isSameAtoms (this.currentDipole, this.tempDipole.dipoleInfo)) {
this.tempDipole = this.currentDipole;
if (this.dipoleValue > 0) this.tempDipole.dipoleValue = this.dipoleValue;
}} else {
this.tempDipole.set (this.startCoord, this.viewer.getAtomSetCenter (atomset), this.dipoleValue);
}return;
}if ("startCoord" === propertyName) {
this.startCoord.setT (value);
this.tempDipole.set (this.startCoord, J.util.P3.new3 (0, 0, 0), this.dipoleValue);
return;
}if ("endCoord" === propertyName) {
this.iHaveTwoEnds = true;
this.endCoord.setT (value);
this.tempDipole.set (this.startCoord, this.endCoord, this.dipoleValue);
this.dumpDipoles ("endCoord");
return;
}if ("value" === propertyName) {
this.dipoleValue = (value).floatValue ();
this.isUserValue = true;
this.tempDipole.set (this.dipoleValue);
if (this.tempDipole.offsetPercent != 0) this.tempDipole.offsetAngstroms = this.tempDipole.offsetPercent / 100 * this.tempDipole.dipoleValue;
return;
}if ("set" === propertyName) {
if (this.isBond || !this.iHaveTwoEnds) return;
this.setDipole ();
this.setModelIndex ();
return;
}if (propertyName === "deleteModelAtoms") {
var modelIndex = ((value)[2])[0];
for (var i = this.dipoleCount; --i >= 0; ) if (this.dipoles[i].modelIndex > modelIndex) {
this.dipoles[i].modelIndex--;
} else if (this.dipoles[i].modelIndex == modelIndex) {
if (this.dipoles[i] === this.currentDipole) this.currentDipole = null;
this.dipoles = J.util.ArrayUtil.deleteElements (this.dipoles, i, 1);
this.dipoleCount--;
}
this.currentDipole = null;
return;
}}, "~S,~O,J.util.BS");
$_M(c$, "setProperty", 
($fz = function (tok, bondOnly, iValue, fValue) {
if (this.currentDipole != null) this.setProperty (tok, this.currentDipole, iValue, fValue);
 else {
for (var i = this.dipoleCount; --i >= 0; ) if (!bondOnly || this.isBondDipole (i)) if (this.wildID == null || J.util.TextFormat.isMatch (this.dipoles[i].thisID.toUpperCase (), this.wildID, true, true)) this.setProperty (tok, this.dipoles[i], iValue, fValue);

}}, $fz.isPrivate = true, $fz), "~N,~B,~N,~N");
$_M(c$, "setProperty", 
($fz = function (tok, dipole, iValue, fValue) {
switch (tok) {
case 1048589:
dipole.visible = true;
return;
case 1048588:
dipole.visible = false;
return;
case 12291:
this.deleteDipole (dipole);
return;
case 659488:
dipole.mad = this.tempDipole.mad = iValue;
return;
case 1611272194:
dipole.offsetAngstroms = fValue;
return;
case 269484210:
dipole.offsetAngstroms = fValue * dipole.dipoleValue;
return;
case 3145754:
dipole.offsetSide = fValue;
return;
case 135267329:
dipole.noCross = (iValue == 0);
return;
case 1766856708:
dipole.colix = this.colix;
return;
case 603979967:
dipole.setTranslucent (iValue == 1, this.translucentLevel);
return;
}
J.util.Logger.error ("Unkown dipole property! " + J.script.T.nameOf (tok));
}, $fz.isPrivate = true, $fz), "~N,J.shapespecial.Dipole,~N,~N");
Clazz.overrideMethod (c$, "getPropertyData", 
function (property, data) {
if (property === "getNames") {
}if (property === "checkID") {
var key = (data[0]).toUpperCase ();
var isWild = J.util.TextFormat.isWild (key);
for (var i = this.dipoleCount; --i >= 0; ) {
var id = this.dipoles[i].thisID;
if (id.equalsIgnoreCase (key) || isWild && J.util.TextFormat.isMatch (id.toUpperCase (), key, true, true)) {
data[1] = id;
return true;
}}
return false;
}return false;
}, "~S,~A");
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if (property.equals ("list")) {
return this.getShapeState ();
}return null;
}, "~S,~N");
$_M(c$, "getBondDipoles", 
($fz = function () {
var partialCharges = this.modelSet.getPartialCharges ();
if (partialCharges == null) return;
this.clear (true);
var bonds = this.modelSet.bonds;
for (var i = this.modelSet.bondCount; --i >= 0; ) {
var bond = bonds[i];
if (!bond.isCovalent ()) continue;
var c1 = partialCharges[bond.getAtomIndex1 ()];
var c2 = partialCharges[bond.getAtomIndex2 ()];
if (c1 != c2) this.setDipole (bond.getAtom1 (), bond.getAtom2 (), c1, c2);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "isBondDipole", 
($fz = function (i) {
if (i >= this.dipoles.length || this.dipoles[i] == null) return false;
return (this.dipoles[i].isBondType ());
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setColixDipole", 
($fz = function (colix, bondTypeMask, bs) {
if (colix == 2) return;
var iter = this.modelSet.getBondIteratorForType (bondTypeMask, bs);
while (iter.hasNext ()) {
var d = this.findBondDipole (iter.next ());
if (d != null) d.colix = colix;
}
}, $fz.isPrivate = true, $fz), "~N,~N,J.util.BS");
$_M(c$, "setDipole", 
($fz = function () {
if (this.currentDipole == null) this.currentDipole = this.allocDipole ("", "");
this.currentDipole.set (this.tempDipole.thisID, this.tempDipole.dipoleInfo, this.tempDipole.atoms, this.tempDipole.dipoleValue, this.tempDipole.mad, this.tempDipole.offsetAngstroms, this.tempDipole.offsetPercent, this.tempDipole.offsetSide, this.tempDipole.origin, this.tempDipole.vector);
this.currentDipole.isUserValue = this.isUserValue;
this.currentDipole.modelIndex = this.viewer.getCurrentModelIndex ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setDipole", 
($fz = function (atom1, atom2, c1, c2) {
var dipole = this.findDipole (atom1, atom2, true);
var value = (c1 - c2) / 2 * atom1.distance (atom2) / 0.208194;
if (value < 0) {
dipole.set (atom2, atom1, -value);
} else {
dipole.set (atom1, atom2, value);
}dipole.type = 3;
dipole.modelIndex = atom1.getModelIndex ();
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.modelset.Atom,~N,~N");
$_M(c$, "getDipoleIndex", 
($fz = function (dipoleInfo, thisID) {
if (dipoleInfo != null && dipoleInfo.length > 0) for (var i = this.dipoleCount; --i >= 0; ) if (this.isSameAtoms (this.dipoles[i], dipoleInfo)) return i;

return this.getIndexFromName (thisID);
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "isSameAtoms", 
($fz = function (dipole, dipoleInfo) {
return (dipole != null && dipole.isBondType () && (dipole.dipoleInfo + dipole.dipoleInfo).indexOf (dipoleInfo) >= 0);
}, $fz.isPrivate = true, $fz), "J.shapespecial.Dipole,~S");
$_M(c$, "getDipoleIndex", 
($fz = function (atomIndex1, atomIndex2) {
for (var i = this.dipoleCount; --i >= 0; ) {
if (this.dipoles[i] != null && this.dipoles[i].atoms[0] != null && this.dipoles[i].atoms[1] != null && (this.dipoles[i].atoms[0].getIndex () == atomIndex1 && this.dipoles[i].atoms[1].getIndex () == atomIndex2 || this.dipoles[i].atoms[1].getIndex () == atomIndex1 && this.dipoles[i].atoms[0].getIndex () == atomIndex2)) return i;
}
return -1;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "deleteDipole", 
($fz = function (dipole) {
if (dipole == null) return;
if (this.currentDipole === dipole) this.currentDipole = null;
var i;
for (i = this.dipoleCount; this.dipoles[--i] !== dipole; ) {
}
if (i < 0) return;
for (var j = i + 1; j < this.dipoleCount; ++j) this.dipoles[j - 1] = this.dipoles[j];

this.dipoles[--this.dipoleCount] = null;
}, $fz.isPrivate = true, $fz), "J.shapespecial.Dipole");
$_M(c$, "findDipole", 
($fz = function (thisID) {
var dipoleIndex = this.getIndexFromName (thisID);
if (dipoleIndex >= 0) {
return this.dipoles[dipoleIndex];
}return null;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "findDipole", 
($fz = function (atom1, atom2, doAllocate) {
var dipoleIndex = this.getDipoleIndex (atom1.getIndex (), atom2.getIndex ());
if (dipoleIndex >= 0) {
return this.dipoles[dipoleIndex];
}return (doAllocate ? this.allocDipole ("", "") : null);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.modelset.Atom,~B");
$_M(c$, "findBondDipole", 
($fz = function (bond) {
var d = this.findDipole (bond.getAtom1 (), bond.getAtom2 (), false);
return (d == null || d.atoms[0] == null ? null : d);
}, $fz.isPrivate = true, $fz), "J.modelset.Bond");
$_M(c$, "findDipole", 
($fz = function (thisID, dipoleInfo) {
var dipoleIndex = this.getDipoleIndex (dipoleInfo, thisID);
if (dipoleIndex >= 0) {
if (thisID.length > 0) this.dipoles[dipoleIndex].thisID = thisID;
return this.dipoles[dipoleIndex];
}return this.allocDipole (thisID, dipoleInfo);
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "allocDipole", 
($fz = function (thisID, dipoleInfo) {
this.dipoles = J.util.ArrayUtil.ensureLength (this.dipoles, this.dipoleCount + 1);
if (thisID == null || thisID.length == 0) thisID = "dipole" + (this.dipoleCount + 1);
var d = this.dipoles[this.dipoleCount++] =  new J.shapespecial.Dipole (this.viewer.getCurrentModelIndex (), thisID, dipoleInfo, this.colix, 5, true);
return d;
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "dumpDipoles", 
($fz = function (msg) {
for (var i = this.dipoleCount; --i >= 0; ) {
var dipole = this.dipoles[i];
J.util.Logger.info ("\n\n" + msg + " dump dipole " + i + " " + dipole + " " + dipole.thisID + " " + dipole.dipoleInfo + " " + dipole.visibilityFlags + " mad=" + dipole.mad + " vis=" + dipole.visible + "\n orig" + dipole.origin + " " + " vect" + dipole.vector + " val=" + dipole.dipoleValue);
}
if (this.currentDipole != null) J.util.Logger.info (" current = " + this.currentDipole + this.currentDipole.origin);
if (this.tempDipole != null) J.util.Logger.info (" temp = " + this.tempDipole + " " + this.tempDipole.origin);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "clear", 
($fz = function (clearBondDipolesOnly) {
if (clearBondDipolesOnly) {
for (var i = this.dipoleCount; --i >= 0; ) if (this.isBondDipole (i)) this.deleteDipole (this.dipoles[i]);

return;
}for (var i = this.dipoleCount; --i >= 0; ) if (!this.isBond || this.isBondDipole (i)) this.deleteDipole (this.dipoles[i]);

}, $fz.isPrivate = true, $fz), "~B");
Clazz.overrideMethod (c$, "getIndexFromName", 
function (thisID) {
if (thisID == null) return -1;
for (var i = this.dipoleCount; --i >= 0; ) {
if (this.dipoles[i] != null && thisID.equals (this.dipoles[i].thisID)) return i;
}
return -1;
}, "~S");
Clazz.overrideMethod (c$, "getShapeDetail", 
function () {
var V =  new J.util.JmolList ();
var atomInfo;
for (var i = 0; i < this.dipoleCount; i++) {
var info =  new java.util.Hashtable ();
var dipole = this.dipoles[i];
info.put ("ID", dipole.thisID);
info.put ("vector", dipole.vector);
info.put ("origin", dipole.origin);
if (dipole.atoms[0] != null) {
atomInfo =  new java.util.Hashtable ();
this.viewer.getAtomIdentityInfo (dipole.atoms[0].getIndex (), atomInfo);
var atoms =  new J.util.JmolList ();
atoms.addLast (atomInfo);
atomInfo =  new java.util.Hashtable ();
this.viewer.getAtomIdentityInfo (dipole.atoms[1].getIndex (), atomInfo);
atoms.addLast (atomInfo);
info.put ("atoms", atoms);
info.put ("magnitude", Float.$valueOf (dipole.vector.length ()));
}V.addLast (info);
}
return V;
});
$_M(c$, "setModelIndex", 
($fz = function () {
if (this.currentDipole == null) return;
this.currentDipole.visible = true;
this.currentDipole.modelIndex = this.viewer.getCurrentModelIndex ();
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
for (var i = this.dipoleCount; --i >= 0; ) {
var dipole = this.dipoles[i];
dipole.visibilityFlags = ((dipole.modelIndex < 0 || bs.get (dipole.modelIndex)) && dipole.mad != 0 && dipole.visible && dipole.origin != null && dipole.vector != null && dipole.vector.length () != 0 && dipole.dipoleValue != 0 ? this.myVisibilityFlag : 0);
}
}, "J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
if (this.dipoleCount == 0) return "";
var s =  new J.util.SB ();
var thisModel = -1;
var modelCount = this.viewer.getModelCount ();
for (var i = 0; i < this.dipoleCount; i++) {
var dipole = this.dipoles[i];
if (dipole.isValid) {
if (modelCount > 1 && dipole.modelIndex != thisModel) J.shape.Shape.appendCmd (s, "frame " + this.viewer.getModelNumberDotted (thisModel = dipole.modelIndex));
s.append (dipole.getShapeState ());
J.shape.Shape.appendCmd (s, J.shape.Shape.getColorCommandUnk ("dipole", dipole.colix, this.translucentAllowed));
}}
return s.toString ();
});
Clazz.defineStatics (c$,
"DEFAULT_MAD", 5,
"DEFAULT_OFFSETSIDE", 0.40,
"E_ANG_PER_DEBYE", 0.208194);
});
