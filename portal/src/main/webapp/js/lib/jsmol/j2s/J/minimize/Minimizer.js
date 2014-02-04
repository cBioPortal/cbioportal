Clazz.declarePackage ("J.minimize");
Clazz.load (["J.api.MinimizerInterface"], "J.minimize.Minimizer", ["java.util.Hashtable", "J.i18n.GT", "J.minimize.MinAngle", "$.MinAtom", "$.MinBond", "$.MinTorsion", "$.MinimizationThread", "J.minimize.forcefield.ForceFieldMMFF", "$.ForceFieldUFF", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Escape", "$.JmolList", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.atoms = null;
this.bonds = null;
this.rawBondCount = 0;
this.minAtoms = null;
this.minBonds = null;
this.minAngles = null;
this.minTorsions = null;
this.minPositions = null;
this.bsMinFixed = null;
this.atomCount = 0;
this.bondCount = 0;
this.atomMap = null;
this.partialCharges = null;
this.steps = 50;
this.crit = 1e-3;
this.units = "kJ/mol";
this.pFF = null;
this.ff = "UFF";
this.bsTaint = null;
this.bsSelected = null;
this.bsAtoms = null;
this.bsFixedDefault = null;
this.bsFixed = null;
this.constraints = null;
this.isSilent = false;
this.constraintMap = null;
this.elemnoMax = 0;
this.$minimizationOn = false;
this.minimizationThread = null;
this.coordSaved = null;
Clazz.instantialize (this, arguments);
}, J.minimize, "Minimizer", null, J.api.MinimizerInterface);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value) {
if (propertyName.equals ("ff")) {
if (!this.ff.equals (value)) {
this.setProperty ("clear", null);
this.ff = value;
}return;
}if (propertyName.equals ("cancel")) {
this.stopMinimization (false);
return;
}if (propertyName.equals ("clear")) {
if (this.minAtoms != null) {
this.stopMinimization (false);
this.clear ();
}return;
}if (propertyName.equals ("constraint")) {
this.addConstraint (value);
return;
}if (propertyName.equals ("fixed")) {
this.bsFixedDefault = value;
return;
}if (propertyName.equals ("stop")) {
this.stopMinimization (true);
return;
}if (propertyName.equals ("viewer")) {
this.viewer = value;
return;
}}, "~S,~O");
Clazz.overrideMethod (c$, "getProperty", 
function (propertyName, param) {
if (propertyName.equals ("log")) {
return (this.pFF == null ? "" : this.pFF.getLogData ());
}return null;
}, "~S,~N");
$_M(c$, "addConstraint", 
($fz = function (c) {
if (c == null) return;
var atoms = c[0];
var nAtoms = atoms[0];
if (nAtoms == 0) {
this.constraints = null;
return;
}if (this.constraints == null) {
this.constraints =  new J.util.JmolList ();
this.constraintMap =  new java.util.Hashtable ();
}if (atoms[1] > atoms[nAtoms]) {
J.util.ArrayUtil.swapInt (atoms, 1, nAtoms);
if (nAtoms == 4) J.util.ArrayUtil.swapInt (atoms, 2, 3);
}var id = J.util.Escape.eAI (atoms);
var c1 = this.constraintMap.get (id);
if (c1 != null) {
c1[2] = c[2];
return;
}this.constraintMap.put (id, c);
this.constraints.addLast (c);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "clear", 
($fz = function () {
this.setMinimizationOn (false);
this.atomCount = 0;
this.bondCount = 0;
this.atoms = null;
this.bonds = null;
this.rawBondCount = 0;
this.minAtoms = null;
this.minBonds = null;
this.minAngles = null;
this.minTorsions = null;
this.partialCharges = null;
this.coordSaved = null;
this.atomMap = null;
this.bsTaint = null;
this.bsAtoms = null;
this.bsFixed = null;
this.bsFixedDefault = null;
this.bsMinFixed = null;
this.bsSelected = null;
this.constraints = null;
this.constraintMap = null;
this.pFF = null;
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "minimize", 
function (steps, crit, bsSelected, bsFixed, haveFixed, forceSilent, ff) {
this.isSilent = (forceSilent || this.viewer.getBooleanProperty ("minimizationSilent"));
var val;
this.setEnergyUnits ();
if (steps == 2147483647) {
val = this.viewer.getParameter ("minimizationSteps");
if (val != null && Clazz.instanceOf (val, Integer)) steps = (val).intValue ();
}this.steps = steps;
if (!haveFixed && this.bsFixedDefault != null) bsFixed.and (this.bsFixedDefault);
if (crit <= 0) {
val = this.viewer.getParameter ("minimizationCriterion");
if (val != null && Clazz.instanceOf (val, Float)) crit = (val).floatValue ();
}this.crit = Math.max (crit, 0.0001);
if (this.$minimizationOn) return false;
var pFF0 = this.pFF;
this.getForceField (ff);
if (this.pFF == null) {
J.util.Logger.error (J.i18n.GT._ ("Could not get class for force field {0}", ff));
return false;
}J.util.Logger.info ("minimize: initializing " + this.pFF.name + " (steps = " + steps + " criterion = " + crit + ") ...");
if (bsSelected.cardinality () == 0) {
J.util.Logger.error (J.i18n.GT._ ("No atoms selected -- nothing to do!"));
return false;
}this.atoms = this.viewer.getModelSet ().atoms;
this.bsAtoms = J.util.BSUtil.copy (bsSelected);
if (bsFixed != null) this.bsAtoms.or (bsFixed);
this.atomCount = this.bsAtoms.cardinality ();
var sameAtoms = J.util.BSUtil.areEqual (bsSelected, this.bsSelected);
this.bsSelected = bsSelected;
if (pFF0 != null && this.pFF !== pFF0) sameAtoms = false;
if (!sameAtoms) this.pFF.clear ();
if ((!sameAtoms || !J.util.BSUtil.areEqual (bsFixed, this.bsFixed)) && !this.setupMinimization ()) {
this.clear ();
return false;
}if (steps > 0) {
this.bsTaint = J.util.BSUtil.copy (this.bsAtoms);
J.util.BSUtil.andNot (this.bsTaint, bsFixed);
this.viewer.setTaintedAtoms (this.bsTaint, 2);
}if (bsFixed != null) this.bsFixed = bsFixed;
this.setAtomPositions ();
if (this.constraints != null) {
for (var i = this.constraints.size (); --i >= 0; ) {
var constraint = this.constraints.get (i);
var aList = constraint[0];
var minList = constraint[1];
var nAtoms = aList[0] = Math.abs (aList[0]);
for (var j = 1; j <= nAtoms; j++) {
if (steps <= 0 || !this.bsAtoms.get (aList[j])) {
aList[0] = -nAtoms;
break;
}minList[j - 1] = this.atomMap[aList[j]];
}
}
}this.pFF.setConstraints (this);
if (steps <= 0) this.getEnergyOnly ();
 else if (this.isSilent || !this.viewer.useMinimizationThread ()) this.minimizeWithoutThread ();
 else this.setMinimizationOn (true);
return true;
}, "~N,~N,J.util.BS,J.util.BS,~B,~B,~S");
$_M(c$, "setEnergyUnits", 
($fz = function () {
var s = this.viewer.getEnergyUnits ();
this.units = (s.equalsIgnoreCase ("kcal") ? "kcal" : "kJ");
}, $fz.isPrivate = true, $fz));
$_M(c$, "setupMinimization", 
($fz = function () {
this.coordSaved = null;
this.atomMap =  Clazz.newIntArray (this.atoms.length, 0);
this.minAtoms =  new Array (this.atomCount);
this.elemnoMax = 0;
var bsElements =  new J.util.BS ();
for (var i = this.bsAtoms.nextSetBit (0), pt = 0; i >= 0; i = this.bsAtoms.nextSetBit (i + 1), pt++) {
var atom = this.atoms[i];
this.atomMap[i] = pt;
var atomicNo = this.atoms[i].getElementNumber ();
this.elemnoMax = Math.max (this.elemnoMax, atomicNo);
bsElements.set (atomicNo);
this.minAtoms[pt] =  new J.minimize.MinAtom (pt, atom, [atom.x, atom.y, atom.z], this.atomCount);
this.minAtoms[pt].sType = atom.getAtomName ();
}
J.util.Logger.info (J.i18n.GT._ ("{0} atoms will be minimized.", "" + this.atomCount));
J.util.Logger.info ("minimize: getting bonds...");
this.bonds = this.viewer.modelSet.bonds;
this.rawBondCount = this.viewer.modelSet.bondCount;
this.getBonds ();
J.util.Logger.info ("minimize: getting angles...");
this.getAngles ();
J.util.Logger.info ("minimize: getting torsions...");
this.getTorsions ();
return this.setModel (bsElements);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setModel", 
($fz = function (bsElements) {
if (!this.pFF.setModel (bsElements, this.elemnoMax)) {
J.util.Logger.error (J.i18n.GT._ ("could not setup force field {0}", this.ff));
if (this.ff.equals ("MMFF")) {
this.getForceField ("UFF");
return this.setModel (bsElements);
}return false;
}return true;
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "setAtomPositions", 
($fz = function () {
for (var i = 0; i < this.atomCount; i++) this.minAtoms[i].set ();

this.bsMinFixed = null;
if (this.bsFixed != null) {
this.bsMinFixed =  new J.util.BS ();
for (var i = this.bsAtoms.nextSetBit (0), pt = 0; i >= 0; i = this.bsAtoms.nextSetBit (i + 1), pt++) if (this.bsFixed.get (i)) this.bsMinFixed.set (pt);

}}, $fz.isPrivate = true, $fz));
$_M(c$, "getBonds", 
($fz = function () {
var bondInfo =  new J.util.JmolList ();
this.bondCount = 0;
var i1;
var i2;
for (var i = 0; i < this.rawBondCount; i++) {
var bond = this.bonds[i];
if (!this.bsAtoms.get (i1 = bond.getAtomIndex1 ()) || !this.bsAtoms.get (i2 = bond.getAtomIndex2 ())) continue;
if (i2 < i1) {
var ii = i1;
i1 = i2;
i2 = ii;
}var bondOrder = bond.getCovalentOrder ();
switch (bondOrder) {
case 1:
case 2:
case 3:
break;
case 515:
bondOrder = 5;
break;
default:
bondOrder = 1;
}
bondInfo.addLast ( new J.minimize.MinBond (i, this.bondCount++, this.atomMap[i1], this.atomMap[i2], bondOrder, 0, null));
}
this.minBonds =  new Array (this.bondCount);
for (var i = 0; i < this.bondCount; i++) {
var bond = this.minBonds[i] = bondInfo.get (i);
var atom1 = bond.data[0];
var atom2 = bond.data[1];
this.minAtoms[atom1].addBond (bond, atom2);
this.minAtoms[atom2].addBond (bond, atom1);
}
for (var i = 0; i < this.atomCount; i++) this.minAtoms[i].getBondedAtomIndexes ();

}, $fz.isPrivate = true, $fz));
$_M(c$, "getAngles", 
function () {
var vAngles =  new J.util.JmolList ();
var atomList;
var ic;
for (var i = 0; i < this.bondCount; i++) {
var bond = this.minBonds[i];
var ia = bond.data[0];
var ib = bond.data[1];
if (this.minAtoms[ib].nBonds > 1) {
atomList = this.minAtoms[ib].getBondedAtomIndexes ();
for (var j = atomList.length; --j >= 0; ) if ((ic = atomList[j]) > ia) {
vAngles.addLast ( new J.minimize.MinAngle ([ia, ib, ic, i, this.minAtoms[ib].getBondIndex (j)]));
this.minAtoms[ia].bsVdw.clear (ic);
}
}if (this.minAtoms[ia].nBonds > 1) {
atomList = this.minAtoms[ia].getBondedAtomIndexes ();
for (var j = atomList.length; --j >= 0; ) if ((ic = atomList[j]) < ib && ic > ia) {
vAngles.addLast ( new J.minimize.MinAngle ([ic, ia, ib, this.minAtoms[ia].getBondIndex (j), i]));
this.minAtoms[ic].bsVdw.clear (ib);
}
}}
this.minAngles = vAngles.toArray ( new Array (vAngles.size ()));
J.util.Logger.info (this.minAngles.length + " angles");
});
$_M(c$, "getTorsions", 
function () {
var vTorsions =  new J.util.JmolList ();
var id;
for (var i = this.minAngles.length; --i >= 0; ) {
var angle = this.minAngles[i].data;
var ia = angle[0];
var ib = angle[1];
var ic = angle[2];
var atomList;
if (ic > ib && this.minAtoms[ic].nBonds > 1) {
atomList = this.minAtoms[ic].getBondedAtomIndexes ();
for (var j = 0; j < atomList.length; j++) {
id = atomList[j];
if (id != ia && id != ib) {
vTorsions.addLast ( new J.minimize.MinTorsion ([ia, ib, ic, id, angle[3], angle[4], this.minAtoms[ic].getBondIndex (j)]));
this.minAtoms[Math.min (ia, id)].bs14.set (Math.max (ia, id));
}}
}if (ia > ib && this.minAtoms[ia].nBonds != 1) {
atomList = this.minAtoms[ia].getBondedAtomIndexes ();
for (var j = 0; j < atomList.length; j++) {
id = atomList[j];
if (id != ic && id != ib) {
vTorsions.addLast ( new J.minimize.MinTorsion ([ic, ib, ia, id, angle[4], angle[3], this.minAtoms[ia].getBondIndex (j)]));
this.minAtoms[Math.min (ic, id)].bs14.set (Math.max (ic, id));
}}
}}
this.minTorsions = vTorsions.toArray ( new Array (vTorsions.size ()));
J.util.Logger.info (this.minTorsions.length + " torsions");
});
$_M(c$, "getForceField", 
function (ff) {
if (ff.startsWith ("MMFF")) ff = "MMFF";
if (this.pFF == null || !ff.equals (this.ff)) {
if (ff.equals ("UFF")) {
this.pFF =  new J.minimize.forcefield.ForceFieldUFF (this);
} else if (ff.equals ("MMFF")) {
this.pFF =  new J.minimize.forcefield.ForceFieldMMFF (this);
} else {
this.pFF =  new J.minimize.forcefield.ForceFieldUFF (this);
ff = "UFF";
}this.ff = ff;
this.viewer.setStringProperty ("_minimizationForceField", ff);
}return this.pFF;
}, "~S");
Clazz.overrideMethod (c$, "minimizationOn", 
function () {
return this.$minimizationOn;
});
$_M(c$, "setMinimizationOn", 
($fz = function (minimizationOn) {
this.$minimizationOn = minimizationOn;
if (!minimizationOn) {
if (this.minimizationThread != null) {
this.minimizationThread = null;
}return;
}if (this.minimizationThread == null) {
this.minimizationThread =  new J.minimize.MinimizationThread ();
this.minimizationThread.setManager (this, this.viewer, null);
this.minimizationThread.start ();
}}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getEnergyOnly", 
($fz = function () {
if (this.pFF == null || this.viewer == null) return;
this.pFF.steepestDescentInitialize (this.steps, this.crit);
this.viewer.setFloatProperty ("_minimizationEnergyDiff", 0);
this.reportEnergy ();
this.viewer.setStringProperty ("_minimizationStatus", "calculate");
this.viewer.notifyMinimizationStatus ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "reportEnergy", 
($fz = function () {
this.viewer.setFloatProperty ("_minimizationEnergy", this.pFF.toUserUnits (this.pFF.getEnergy ()));
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "startMinimization", 
function () {
try {
J.util.Logger.info ("minimizer: startMinimization");
this.viewer.setIntProperty ("_minimizationStep", 0);
this.viewer.setStringProperty ("_minimizationStatus", "starting");
this.viewer.setFloatProperty ("_minimizationEnergy", 0);
this.viewer.setFloatProperty ("_minimizationEnergyDiff", 0);
this.viewer.notifyMinimizationStatus ();
this.viewer.saveCoordinates ("minimize", this.bsTaint);
this.pFF.steepestDescentInitialize (this.steps, this.crit);
this.reportEnergy ();
this.saveCoordinates ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("minimization error viewer=" + this.viewer + " pFF = " + this.pFF);
return false;
} else {
throw e;
}
}
this.$minimizationOn = true;
return true;
});
Clazz.overrideMethod (c$, "stepMinimization", 
function () {
if (!this.$minimizationOn) return false;
var doRefresh = (!this.isSilent && this.viewer.getBooleanProperty ("minimizationRefresh"));
this.viewer.setStringProperty ("_minimizationStatus", "running");
var going = this.pFF.steepestDescentTakeNSteps (1);
var currentStep = this.pFF.getCurrentStep ();
this.viewer.setIntProperty ("_minimizationStep", currentStep);
this.reportEnergy ();
this.viewer.setFloatProperty ("_minimizationEnergyDiff", this.pFF.toUserUnits (this.pFF.getEnergyDiff ()));
this.viewer.notifyMinimizationStatus ();
if (doRefresh) {
this.updateAtomXYZ ();
this.viewer.refresh (3, "minimization step " + currentStep);
}return going;
});
Clazz.overrideMethod (c$, "endMinimization", 
function () {
this.updateAtomXYZ ();
this.setMinimizationOn (false);
var failed = this.pFF.detectExplosion ();
if (failed) this.restoreCoordinates ();
this.viewer.setIntProperty ("_minimizationStep", this.pFF.getCurrentStep ());
this.reportEnergy ();
this.viewer.setStringProperty ("_minimizationStatus", (failed ? "failed" : "done"));
this.viewer.notifyMinimizationStatus ();
this.viewer.refresh (3, "Minimizer:done" + (failed ? " EXPLODED" : "OK"));
J.util.Logger.info ("minimizer: endMinimization");
});
$_M(c$, "saveCoordinates", 
($fz = function () {
if (this.coordSaved == null) this.coordSaved =  Clazz.newDoubleArray (this.atomCount, 3, 0);
for (var i = 0; i < this.atomCount; i++) for (var j = 0; j < 3; j++) this.coordSaved[i][j] = this.minAtoms[i].coord[j];


}, $fz.isPrivate = true, $fz));
$_M(c$, "restoreCoordinates", 
($fz = function () {
if (this.coordSaved == null) return;
for (var i = 0; i < this.atomCount; i++) for (var j = 0; j < 3; j++) this.minAtoms[i].coord[j] = this.coordSaved[i][j];


this.updateAtomXYZ ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "stopMinimization", 
function (coordAreOK) {
if (!this.$minimizationOn) return;
this.setMinimizationOn (false);
if (coordAreOK) this.endMinimization ();
 else this.restoreCoordinates ();
}, "~B");
$_M(c$, "updateAtomXYZ", 
function () {
if (this.steps <= 0) return;
for (var i = 0; i < this.atomCount; i++) {
var minAtom = this.minAtoms[i];
var atom = minAtom.atom;
atom.x = minAtom.coord[0];
atom.y = minAtom.coord[1];
atom.z = minAtom.coord[2];
}
this.viewer.refreshMeasures (false);
});
$_M(c$, "minimizeWithoutThread", 
($fz = function () {
if (!this.startMinimization ()) return;
while (this.stepMinimization ()) {
}
this.endMinimization ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "report", 
function (msg, isEcho) {
if (this.isSilent) J.util.Logger.info (msg);
 else if (isEcho) this.viewer.showString (msg, false);
 else this.viewer.scriptEcho (msg);
}, "~S,~B");
Clazz.overrideMethod (c$, "calculatePartialCharges", 
function (bonds, bondCount, atoms, bsAtoms) {
var ff =  new J.minimize.forcefield.ForceFieldMMFF (this);
ff.setArrays (atoms, bsAtoms, bonds, bondCount, true, true);
this.viewer.setAtomProperty (bsAtoms, 1087375361, 0, 0, null, null, ff.getAtomTypeDescriptions ());
this.viewer.setAtomProperty (bsAtoms, 1112541196, 0, 0, null, ff.getPartialCharges (), null);
}, "~A,~N,~A,J.util.BS");
});
