Clazz.declarePackage ("J.minimize.forcefield");
Clazz.load (["J.minimize.forcefield.Calculation", "J.util.ArrayUtil", "$.SB", "$.Vector3d"], "J.minimize.forcefield.Calculations", ["java.lang.Float", "J.minimize.Util", "J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ff = null;
this.calculations = null;
this.atomCount = 0;
this.bondCount = 0;
this.angleCount = 0;
this.torsionCount = 0;
this.minAtoms = null;
this.minBonds = null;
this.minAngles = null;
this.minTorsions = null;
this.minPositions = null;
this.constraints = null;
this.isPreliminary = false;
this.gradients = false;
this.silent = false;
this.logData = null;
this.logging = false;
this.loggingEnabled = false;
if (!Clazz.isClassDefined ("J.minimize.forcefield.Calculations.PairCalc")) {
J.minimize.forcefield.Calculations.$Calculations$PairCalc$ ();
}
this.da = null;
this.db = null;
this.dc = null;
this.dd = null;
this.ia = 0;
this.ib = 0;
this.ic = 0;
this.id = 0;
this.v1 = null;
this.v2 = null;
this.v3 = null;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "Calculations");
Clazz.prepareFields (c$, function () {
this.calculations = J.util.ArrayUtil.createArrayOfArrayList (7);
this.logData =  new J.util.SB ();
this.da =  new J.util.Vector3d ();
this.db =  new J.util.Vector3d ();
this.dc =  new J.util.Vector3d ();
this.dd =  new J.util.Vector3d ();
this.v1 =  new J.util.Vector3d ();
this.v2 =  new J.util.Vector3d ();
this.v3 =  new J.util.Vector3d ();
});
$_M(c$, "setConstraints", 
function (constraints) {
this.constraints = constraints;
}, "J.util.JmolList");
Clazz.makeConstructor (c$, 
function (ff, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints) {
this.ff = ff;
this.minAtoms = minAtoms;
this.minBonds = minBonds;
this.minAngles = minAngles;
this.minTorsions = minTorsions;
this.minPositions = minPositions;
this.atomCount = minAtoms.length;
this.bondCount = minBonds.length;
this.angleCount = minAngles.length;
this.torsionCount = minTorsions.length;
this.constraints = constraints;
}, "J.minimize.forcefield.ForceField,~A,~A,~A,~A,~A,J.util.JmolList");
$_M(c$, "addForce", 
function (v, i, dE) {
this.minAtoms[i].force[0] += v.x * dE;
this.minAtoms[i].force[1] += v.y * dE;
this.minAtoms[i].force[2] += v.z * dE;
}, "J.util.Vector3d,~N,~N");
$_M(c$, "setSilent", 
function (TF) {
this.silent = TF;
}, "~B");
$_M(c$, "getLogData", 
function () {
return this.logData.toString ();
});
$_M(c$, "appendLogData", 
function (s) {
this.logData.append (s).append ("\n");
}, "~S");
$_M(c$, "setLoggingEnabled", 
function (TF) {
this.loggingEnabled = TF;
if (this.loggingEnabled) this.logData =  new J.util.SB ();
}, "~B");
$_M(c$, "setPreliminary", 
function (TF) {
this.isPreliminary = TF;
}, "~B");
$_M(c$, "pairSearch", 
function (calc1, pc1, calc2, pc2) {
for (var i = 0; i < this.atomCount - 1; i++) {
var bsVdw = this.minAtoms[i].bsVdw;
for (var j = bsVdw.nextSetBit (0); j >= 0; j = bsVdw.nextSetBit (j + 1)) {
pc1.setData (calc1, i, j);
if (pc2 != null) pc2.setData (calc2, i, j);
}
}
}, "J.util.JmolList,J.minimize.forcefield.Calculations.PairCalc,J.util.JmolList,J.minimize.forcefield.Calculations.PairCalc");
$_M(c$, "calc", 
($fz = function (iType, gradients) {
this.logging = this.loggingEnabled && !this.silent;
this.gradients = gradients;
var calcs = this.calculations[iType];
var nCalc;
var energy = 0;
if (calcs == null || (nCalc = calcs.size ()) == 0) return 0;
if (this.logging) this.appendLogData (this.getDebugHeader (iType));
for (var ii = 0; ii < nCalc; ii++) energy += this.compute (iType, this.calculations[iType].get (ii));

if (this.logging) this.appendLogData (this.getDebugFooter (iType, energy));
if (this.constraints != null && iType <= 3) energy += this.constraintEnergy (iType);
return energy;
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "energyStrBnd", 
function (gradients) {
return 0.0;
}, "~B");
$_M(c$, "energyBond", 
function (gradients) {
return this.calc (0, gradients);
}, "~B");
$_M(c$, "energyAngle", 
function (gradients) {
return this.calc (1, gradients);
}, "~B");
$_M(c$, "energyTorsion", 
function (gradients) {
return this.calc (3, gradients);
}, "~B");
$_M(c$, "energyStretchBend", 
function (gradients) {
return this.calc (2, gradients);
}, "~B");
$_M(c$, "energyOOP", 
function (gradients) {
return this.calc (4, gradients);
}, "~B");
$_M(c$, "energyVDW", 
function (gradients) {
return this.calc (5, gradients);
}, "~B");
$_M(c$, "energyES", 
function (gradients) {
return this.calc (6, gradients);
}, "~B");
$_M(c$, "constraintEnergy", 
($fz = function (iType) {
var value = 0;
var k = 0;
var energy = 0;
for (var i = this.constraints.size (); --i >= 0; ) {
var c = this.constraints.get (i);
var nAtoms = (c[0])[0];
if (nAtoms != iType + 2) continue;
var minList = c[1];
var targetValue = (c[2]).doubleValue ();
switch (iType) {
case 3:
this.id = minList[3];
if (this.gradients) this.dd.setA (this.minAtoms[this.id].coord);
case 1:
this.ic = minList[2];
if (this.gradients) this.dc.setA (this.minAtoms[this.ic].coord);
case 0:
this.ib = minList[1];
this.ia = minList[0];
if (this.gradients) {
this.db.setA (this.minAtoms[this.ib].coord);
this.da.setA (this.minAtoms[this.ia].coord);
}}
k = 10000.0;
switch (iType) {
case 3:
targetValue *= 0.017453292519943295;
value = (this.gradients ? J.minimize.Util.restorativeForceAndTorsionAngleRadians (this.da, this.db, this.dc, this.dd) : J.minimize.Util.getTorsionAngleRadians (this.minAtoms[this.ia].coord, this.minAtoms[this.ib].coord, this.minAtoms[this.ic].coord, this.minAtoms[this.id].coord, this.v1, this.v2, this.v3));
if (value < 0 && targetValue >= 1.5707963267948966) value += 6.283185307179586;
 else if (value > 0 && targetValue <= -1.5707963267948966) targetValue += 6.283185307179586;
break;
case 1:
targetValue *= 0.017453292519943295;
value = (this.gradients ? J.minimize.Util.restorativeForceAndAngleRadians (this.da, this.db, this.dc) : J.minimize.Util.getAngleRadiansABC (this.minAtoms[this.ia].coord, this.minAtoms[this.ib].coord, this.minAtoms[this.ic].coord));
break;
case 0:
value = (this.gradients ? J.minimize.Util.restorativeForceAndDistance (this.da, this.db, this.dc) : Math.sqrt (J.minimize.Util.distance2 (this.minAtoms[this.ia].coord, this.minAtoms[this.ib].coord)));
break;
}
energy += this.constrainQuadratic (value, targetValue, k, iType);
}
return energy;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "constrainQuadratic", 
($fz = function (value, targetValue, k, iType) {
if (!J.minimize.Util.isFinite (value)) return 0;
var delta = value - targetValue;
if (this.gradients) {
var dE = 2.0 * k * delta;
switch (iType) {
case 3:
this.addForce (this.dd, this.id, dE);
case 1:
this.addForce (this.dc, this.ic, dE);
case 0:
this.addForce (this.db, this.ib, dE);
this.addForce (this.da, this.ia, dE);
}
}return k * delta * delta;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "getConstraintList", 
function () {
if (this.constraints == null || this.constraints.size () == 0) return;
this.appendLogData ("C O N S T R A I N T S\n---------------------");
for (var i = this.constraints.size (); --i >= 0; ) {
var c = this.constraints.get (i);
var indexes = c[0];
var minList = c[1];
var targetValue = (c[2]).doubleValue ();
var iType = indexes[0] - 2;
switch (iType) {
case 3:
this.id = minList[3];
case 1:
this.ic = minList[2];
case 0:
this.ib = minList[1];
this.ia = minList[0];
}
switch (iType) {
case 0:
this.appendLogData (J.util.TextFormat.sprintf ("%3d %3d  %-5s %-5s  %12.6f", "ssFI", [this.minAtoms[this.ia].atom.getAtomName (), this.minAtoms[this.ib].atom.getAtomName (), [targetValue], [this.minAtoms[this.ia].atom.getAtomNumber (), this.minAtoms[this.ib].atom.getAtomNumber ()]]));
break;
case 1:
this.appendLogData (J.util.TextFormat.sprintf ("%3d %3d %3d  %-5s %-5s %-5s  %12.6f", "sssFI", [this.minAtoms[this.ia].atom.getAtomName (), this.minAtoms[this.ib].atom.getAtomName (), this.minAtoms[this.ic].atom.getAtomName (), [targetValue], [this.minAtoms[this.ia].atom.getAtomNumber (), this.minAtoms[this.ib].atom.getAtomNumber (), this.minAtoms[this.ic].atom.getAtomNumber ()]]));
break;
case 3:
this.appendLogData (J.util.TextFormat.sprintf ("%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %3d %8.3f     %8.3f     %8.3f     %8.3f", "ssssFI", [this.minAtoms[this.ia].atom.getAtomName (), this.minAtoms[this.ib].atom.getAtomName (), this.minAtoms[this.ic].atom.getAtomName (), this.minAtoms[this.id].atom.getAtomName (), [targetValue], [this.minAtoms[this.ia].atom.getAtomNumber (), this.minAtoms[this.ib].atom.getAtomNumber (), this.minAtoms[this.ic].atom.getAtomNumber (), this.minAtoms[this.id].atom.getAtomNumber ()]]));
break;
}
}
this.appendLogData ("---------------------\n");
});
$_M(c$, "getAtomList", 
function (title) {
var trailer = "-----------------------------------------------------------------------------------------------\n";
var sb =  new J.util.SB ();
sb.append ("\n" + title + "\n\n" + " ATOM    X        Y        Z    TYPE     GRADX    GRADY    GRADZ  " + "---------BONDED ATOMS--------\n" + trailer);
for (var i = 0; i < this.atomCount; i++) {
var atom = this.minAtoms[i];
var others = atom.getBondedAtomIndexes ();
var iVal =  Clazz.newIntArray (others.length + 1, 0);
iVal[0] = atom.atom.getAtomNumber ();
var s = "   ";
for (var j = 0; j < others.length; j++) {
s += " %3d";
iVal[j + 1] = this.minAtoms[others[j]].atom.getAtomNumber ();
}
sb.append (J.util.TextFormat.sprintf ("%3d %8.3f %8.3f %8.3f  %-5s %8.3f %8.3f %8.3f" + s + "\n", "sFI", [atom.sType, [atom.coord[0], atom.coord[1], atom.coord[2], atom.force[0], atom.force[1], atom.force[2]], iVal]));
}
sb.append (trailer + "\n\n");
return sb.toString ();
}, "~S");
$_M(c$, "getDebugHeader2", 
function (iType) {
switch (iType) {
case -1:
break;
case 0:
return "\nB O N D   S T R E T C H I N G (" + this.bondCount + " bonds)\n\n" + "  ATOMS  ATOM TYPES   BOND    BOND       IDEAL      FORCE\n" + "  I   J   I     J     TYPE   LENGTH     LENGTH    CONSTANT      DELTA     ENERGY\n" + "--------------------------------------------------------------------------------";
case 1:
return "\nA N G L E   B E N D I N G (" + this.minAngles.length + " angles)\n\n" + "    ATOMS      ATOM TYPES        VALENCE    IDEAL        FORCE\n" + "  I   J   K   I     J     K       ANGLE     ANGLE      CONSTANT     ENERGY\n" + "--------------------------------------------------------------------------";
case 2:
return "\nS T R E T C H   B E N D I N G (" + (this.minAngles.length * 2) + " angles)\n\n" + "    ATOMS      ATOM TYPES        VALENCE    IDEAL        FORCE\n" + "  I   J   K   I     J     K       ANGLE     ANGLE      CONSTANT     ENERGY\n" + "--------------------------------------------------------------------------";
case 3:
return "\nT O R S I O N A L (" + this.minTorsions.length + " torsions)\n\n" + "      ATOMS           ATOM TYPES            n    COS          FORCE      TORSION\n" + "  I   J   K   L   I     J     K     L          (n phi0)      CONSTANT     ANGLE        ENERGY\n" + "---------------------------------------------------------------------------------------------";
case 4:
return "\nO U T - O F - P L A N E   B E N D I N G\n\n      ATOMS           ATOM TYPES             OOP        FORCE \n  I   J   K   L   I     J     K     L       ANGLE     CONSTANT      ENERGY\n--------------------------------------------------------------------------";
case 5:
return "\nV A N   D E R   W A A L S  (partial list)\n\n  ATOMS  ATOM TYPES\n  I   J   I     J      Rij       kij     ENERGY\n-----------------------------------------------";
case 6:
return "\nE L E C T R O S T A T I C   I N T E R A C T I O N S  (partial list)\n\n  ATOMS  ATOM TYPES \n  I   J   I     J      Rij      f          Qi          Qj    ENERGY\n-------------------------------------------------------------------";
}
return "";
}, "~N");
$_M(c$, "getDebugLine", 
function (iType, c) {
return this.getDebugLineC (iType, c);
}, "~N,J.minimize.forcefield.Calculation");
$_M(c$, "getDebugLineC", 
function (iType, c) {
var energy = this.ff.toUserUnits (c.energy);
switch (iType) {
case 0:
return J.util.TextFormat.sprintf ("%3d %3d  %-5s %-5s  %4.2f%8.3f   %8.3f     %8.3f   %8.3f   %8.3f", "ssFI", [this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, [0, c.rab, c.dData[1], c.dData[0], c.delta, energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber ()]]);
case 1:
case 2:
return J.util.TextFormat.sprintf ("%3d %3d %3d  %-5s %-5s %-5s  %8.3f  %8.3f     %8.3f   %8.3f", "sssFI", [this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, [(c.theta * 57.29577951308232), c.dData[1], c.dData[0], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber (), this.minAtoms[c.ic].atom.getAtomNumber ()]]);
case 3:
return J.util.TextFormat.sprintf ("%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %3d %8.3f     %8.3f     %8.3f     %8.3f", "ssssFI", [this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, this.minAtoms[c.id].sType, [c.dData[1], c.dData[0], (c.theta * 57.29577951308232), energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber (), this.minAtoms[c.ic].atom.getAtomNumber (), this.minAtoms[c.id].atom.getAtomNumber (), c.iData[4]]]);
case 4:
return J.util.TextFormat.sprintf ("%3d %3d %3d %3d  %-5s %-5s %-5s %-5s  %8.3f   %8.3f     %8.3f", "ssssFI", [this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, this.minAtoms[c.id].sType, [(c.theta * 57.29577951308232), c.dData[0], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber (), this.minAtoms[c.ic].atom.getAtomNumber (), this.minAtoms[c.id].atom.getAtomNumber ()]]);
case 5:
return J.util.TextFormat.sprintf ("%3d %3d  %-5s %-5s %6.3f  %8.3f  %8.3f", "ssFI", [this.minAtoms[c.iData[0]].sType, this.minAtoms[c.iData[1]].sType, [c.rab, c.dData[0], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber ()]]);
case 6:
return J.util.TextFormat.sprintf ("%3d %3d  %-5s %-5s %6.3f  %8.3f  %8.3f  %8.3f  %8.3f", "ssFI", [this.minAtoms[c.iData[0]].sType, this.minAtoms[c.iData[1]].sType, [c.rab, c.dData[0], c.dData[1], c.dData[2], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber ()]]);
}
return "";
}, "~N,J.minimize.forcefield.Calculation");
$_M(c$, "getDebugFooter", 
function (iType, energy) {
var s = "";
switch (iType) {
case 0:
s = "BOND STRETCHING";
break;
case 1:
s = "ANGLE BENDING";
break;
case 3:
s = "TORSIONAL";
break;
case 4:
s = "OUT-OF-PLANE BENDING";
break;
case 2:
s = "STRETCH BENDING";
break;
case 5:
s = "VAN DER WAALS";
break;
case 6:
s = "ELECTROSTATIC ENERGY";
break;
}
return J.util.TextFormat.sprintf ("\n     TOTAL %s ENERGY = %8.3f %s/mol\n", "sfs", [s, Float.$valueOf (this.ff.toUserUnits (energy)), this.ff.minimizer.units]);
}, "~N,~N");
$_M(c$, "setPairVariables", 
function (c) {
if (this.gradients) {
this.setCoords (c, 2);
c.rab = J.minimize.Util.restorativeForceAndDistance (this.da, this.db, this.dc);
} else {
c.rab = Math.sqrt (J.minimize.Util.distance2 (this.minAtoms[c.ia].coord, this.minAtoms[c.ib].coord));
}if (J.minimize.Util.isNearZero2 (c.rab, 1.0e-3)) c.rab = 1.0e-3;
}, "J.minimize.forcefield.Calculation");
$_M(c$, "setAngleVariables", 
function (c) {
if (this.gradients) {
this.setCoords (c, 3);
c.theta = J.minimize.Util.restorativeForceAndAngleRadians (this.da, this.db, this.dc);
} else {
c.theta = J.minimize.Util.getAngleRadiansABC (this.minAtoms[c.ia].coord, this.minAtoms[c.ib].coord, this.minAtoms[c.ic].coord);
}if (!J.minimize.Util.isFinite (c.theta)) c.theta = 0.0;
}, "J.minimize.forcefield.Calculation");
$_M(c$, "setOopVariables", 
function (c, fixTheta) {
this.setCoords (c, 4);
if (this.gradients) {
c.theta = J.minimize.Util.restorativeForceAndOutOfPlaneAngleRadians (this.da, this.db, this.dc, this.dd, this.v1, this.v2, this.v3);
} else {
c.theta = J.minimize.Util.pointPlaneAngleRadians (this.da, this.db, this.dc, this.dd, this.v1, this.v2, this.v3, fixTheta);
}if (!J.minimize.Util.isFinite (c.theta)) c.theta = 0.0;
}, "J.minimize.forcefield.Calculation,~B");
$_M(c$, "setTorsionVariables", 
function (c) {
if (this.gradients) {
this.setCoords (c, 4);
c.theta = J.minimize.Util.restorativeForceAndTorsionAngleRadians (this.da, this.db, this.dc, this.dd);
if (!J.minimize.Util.isFinite (c.theta)) c.theta = 1.7453292519943296E-5;
} else {
c.theta = J.minimize.Util.getTorsionAngleRadians (this.minAtoms[c.ia].coord, this.minAtoms[c.ib].coord, this.minAtoms[c.ic].coord, this.minAtoms[c.id].coord, this.v1, this.v2, this.v3);
}}, "J.minimize.forcefield.Calculation");
$_M(c$, "setCoords", 
function (c, n) {
switch (n) {
case 4:
this.da.setA (this.minAtoms[c.ia].coord);
case 3:
this.db.setA (this.minAtoms[c.ib].coord);
case 2:
this.dc.setA (this.minAtoms[c.ic].coord);
case 1:
this.dd.setA (this.minAtoms[c.id].coord);
}
}, "J.minimize.forcefield.Calculation,~N");
$_M(c$, "addForces", 
function (c, n) {
switch (n) {
case 4:
this.addForce (this.dd, c.id, c.dE);
case 3:
this.addForce (this.dc, c.ic, c.dE);
case 2:
this.addForce (this.db, c.ib, c.dE);
case 1:
this.addForce (this.da, c.ia, c.dE);
}
}, "J.minimize.forcefield.Calculation,~N");
c$.$Calculations$PairCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.Calculations, "PairCalc", J.minimize.forcefield.Calculation);
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"RAD_TO_DEG", (57.29577951308232),
"DEG_TO_RAD", (0.017453292519943295),
"KCAL_TO_KJ", 4.1868,
"CALC_DISTANCE", 0,
"CALC_ANGLE", 1,
"CALC_STRETCH_BEND", 2,
"CALC_TORSION", 3,
"CALC_OOP", 4,
"CALC_VDW", 5,
"CALC_ES", 6,
"CALC_MAX", 7,
"PI_OVER_2", 1.5707963267948966,
"TWO_PI", 6.283185307179586);
});
