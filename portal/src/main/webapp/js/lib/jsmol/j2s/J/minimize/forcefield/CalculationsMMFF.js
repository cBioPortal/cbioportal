Clazz.declarePackage ("J.minimize.forcefield");
Clazz.load (["J.minimize.forcefield.Calculation", "$.Calculations"], "J.minimize.forcefield.CalculationsMMFF", ["J.minimize.MinAtom", "$.MinObject", "J.util.JmolList", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ffParams = null;
this.bondCalc = null;
this.angleCalc = null;
this.torsionCalc = null;
this.oopCalc = null;
this.vdwCalc = null;
this.esCalc = null;
this.sbCalc = null;
this.mmff = null;
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.DistanceCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$DistanceCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.AngleCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$AngleCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.SBCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$SBCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.TorsionCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$TorsionCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.OOPCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$OOPCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.VDWCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$VDWCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsMMFF.ESCalc")) {
J.minimize.forcefield.CalculationsMMFF.$CalculationsMMFF$ESCalc$ ();
}
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "CalculationsMMFF", J.minimize.forcefield.Calculations);
Clazz.makeConstructor (c$, 
function (ff, ffParams, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints) {
Clazz.superConstructor (this, J.minimize.forcefield.CalculationsMMFF, [ff, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints]);
this.mmff = ff;
this.ffParams = ffParams;
this.bondCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.DistanceCalc, this, null);
this.angleCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.AngleCalc, this, null);
this.sbCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.SBCalc, this, null);
this.torsionCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.TorsionCalc, this, null);
this.oopCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.OOPCalc, this, null);
this.vdwCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.VDWCalc, this, null);
this.esCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.ESCalc, this, null);
}, "J.minimize.forcefield.ForceField,java.util.Map,~A,~A,~A,~A,~A,J.util.JmolList");
Clazz.overrideMethod (c$, "getUnits", 
function () {
return "kcal";
});
Clazz.overrideMethod (c$, "setupCalculations", 
function () {
var calc;
var distanceCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.DistanceCalc, this, null);
calc = this.calculations[0] =  new J.util.JmolList ();
for (var i = 0; i < this.bondCount; i++) distanceCalc.setData (calc, this.minBonds[i]);

calc = this.calculations[1] =  new J.util.JmolList ();
var angleCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.AngleCalc, this, null);
for (var i = 0; i < this.angleCount; i++) angleCalc.setData (calc, this.minAngles[i]);

calc = this.calculations[2] =  new J.util.JmolList ();
var sbCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.SBCalc, this, null);
for (var i = 0; i < this.angleCount; i++) sbCalc.setData (calc, this.minAngles[i]);

calc = this.calculations[3] =  new J.util.JmolList ();
var torsionCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.TorsionCalc, this, null);
for (var i = 0; i < this.torsionCount; i++) torsionCalc.setData (calc, this.minTorsions[i]);

calc = this.calculations[4] =  new J.util.JmolList ();
var oopCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.OOPCalc, this, null);
for (var i = 0; i < this.atomCount; i++) if (J.minimize.forcefield.CalculationsMMFF.isInvertible (this.minAtoms[i])) oopCalc.setData (calc, i);

this.pairSearch (this.calculations[5] =  new J.util.JmolList (), Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.VDWCalc, this, null), this.calculations[6] =  new J.util.JmolList (), Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsMMFF.ESCalc, this, null));
return true;
});
$_M(c$, "isLinear", 
function (i) {
return J.minimize.MinAtom.isLinear (this.minAtoms[i]);
}, "~N");
c$.isInvertible = $_M(c$, "isInvertible", 
($fz = function (a) {
switch (a.ffType) {
default:
return false;
case 2:
case 3:
case 10:
case 30:
case 37:
case 39:
case 40:
case 41:
case 45:
case 49:
case 54:
case 55:
case 56:
case 57:
case 58:
case 63:
case 64:
case 67:
case 69:
case 78:
case 80:
case 81:
return true;
}
}, $fz.isPrivate = true, $fz), "J.minimize.MinAtom");
Clazz.overrideMethod (c$, "compute", 
function (iType, dataIn) {
switch (iType) {
case 0:
return this.bondCalc.compute (dataIn);
case 1:
return this.angleCalc.compute (dataIn);
case 2:
return this.sbCalc.compute (dataIn);
case 3:
return this.torsionCalc.compute (dataIn);
case 4:
return this.oopCalc.compute (dataIn);
case 5:
return this.vdwCalc.compute (dataIn);
case 6:
return this.esCalc.compute (dataIn);
}
return 0.0;
}, "~N,~A");
$_M(c$, "getParameterObj", 
function (a) {
return (a.key == null || a.ddata != null ? a.ddata : this.ffParams.get (a.key));
}, "J.minimize.MinObject");
$_M(c$, "getParameter", 
function (key) {
return this.ffParams.get (key);
}, "Integer");
Clazz.overrideMethod (c$, "getDebugHeader", 
function (iType) {
switch (iType) {
case -1:
return "MMFF94 Force Field -- T. A. Halgren, J. Comp. Chem. 5 & 6 490-519ff (1996).\n";
case 3:
return "\nT O R S I O N A L (" + this.minTorsions.length + " torsions)\n\n" + "      ATOMS           ATOM TYPES          TORSION\n" + "  I   J   K   L   I     J     K     L      ANGLE       V1       V2       V3     ENERGY\n" + "--------------------------------------------------------------------------------------\n";
default:
return this.getDebugHeader2 (iType);
}
}, "~N");
Clazz.overrideMethod (c$, "getDebugLine", 
function (iType, c) {
var energy = this.ff.toUserUnits (c.energy);
switch (iType) {
case 1:
case 2:
return J.util.TextFormat.sprintf ("%15s  %-5s %-5s %-5s  %8.3f  %8.3f     %8.3f   %8.3f", "ssssFI", [J.minimize.MinObject.decodeKey (c.key), this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, [(c.theta * 57.29577951308232), c.dData[1], c.dData[0], energy], [this.minAtoms[c.ia].atom.getAtomNumber (), this.minAtoms[c.ib].atom.getAtomNumber (), this.minAtoms[c.ic].atom.getAtomNumber ()]]);
case 3:
return J.util.TextFormat.sprintf ("%15s  %-5s %-5s %-5s %-5s  %8.3f %8.3f %8.3f %8.3f %8.3f", "sssssF", [J.minimize.MinObject.decodeKey (c.key), this.minAtoms[c.ia].sType, this.minAtoms[c.ib].sType, this.minAtoms[c.ic].sType, this.minAtoms[c.id].sType, [(c.theta * 57.29577951308232), c.dData[0], c.dData[1], c.dData[2], energy]]);
default:
return this.getDebugLineC (iType, c);
}
}, "~N,J.minimize.forcefield.Calculation");
c$.$CalculationsMMFF$DistanceCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.r0 = 0;
this.kb = 0;
this.delta2 = 0;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "DistanceCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
this.ia = b.data[0];
this.ib = b.data[1];
var c = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (b);
if (c == null) return;
a.addLast ([[this.ia, this.ib], c]);
}, "J.util.JmolList,J.minimize.MinBond");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
this.kb = this.dData[0];
this.r0 = this.dData[1];
this.b$["J.minimize.forcefield.CalculationsMMFF"].setPairVariables (this);
this.delta = this.rab - this.r0;
this.delta2 = this.delta * this.delta;
this.energy = 71.96625 * this.kb * this.delta2 * (1 + -2.0 * this.delta + 2.3333333333333335 * (this.delta2));
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
this.dE = 71.96625 * this.kb * this.delta * (2 + 3 * -2.0 * this.delta + 4 * 2.3333333333333335 * this.delta2);
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 2);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (0, this));
return this.energy;
}, "~A");
Clazz.defineStatics (c$,
"FSTRETCH", 71.96625,
"CS", -2.0,
"CS2", (2.3333333333333335));
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$AngleCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "AngleCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
var c = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (b);
if (c == null) return;
a.addLast ([b.data, c, b.key]);
}, "J.util.JmolList,J.minimize.MinAngle");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.key = a[2];
this.getPointers (a);
var b = this.dData[0];
var c = this.dData[1];
this.b$["J.minimize.forcefield.CalculationsMMFF"].setAngleVariables (this);
var d = (this.theta * 57.29577951308232 - c);
if (c == 180) {
this.energy = 143.9325 * b * (1 + Math.cos (this.theta));
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) this.dE = -143.9325 * b * Math.sin (this.theta);
} else {
this.energy = 0.021922 * b * Math.pow (d, 2) * (1 + -0.006981317007977318 * d);
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) this.dE = 0.021922 * b * d * (2 + 3 * -0.006981317007977318 * d);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 3);
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (1, this));
return this.energy;
}, "~A");
Clazz.defineStatics (c$,
"CB", -0.006981317007977318);
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$SBCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "SBCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].isLinear (b.data[1])) return;
var c = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameter (b.sbKey);
var d = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (b);
var e = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (this.b$["J.minimize.forcefield.CalculationsMMFF"].minBonds[b.data[3]]);
var f = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (this.b$["J.minimize.forcefield.CalculationsMMFF"].minBonds[b.data[4]]);
if (c == null || d == null || e == null || f == null) return;
var g = d[1];
var h = e[1];
var i = f[1];
a.addLast ([b.data, [c[0], g, h]]);
a.addLast ([[b.data[2], b.data[1], b.data[0]], [c[1], g, i]]);
}, "J.util.JmolList,J.minimize.MinAngle");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = 2.51210 * this.dData[0];
var c = this.dData[1];
var d = this.dData[2];
this.b$["J.minimize.forcefield.CalculationsMMFF"].setPairVariables (this);
this.b$["J.minimize.forcefield.CalculationsMMFF"].setAngleVariables (this);
var e = this.rab - d;
this.delta = this.theta * 57.29577951308232 - c;
this.energy = b * e * this.delta;
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (2, this));
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
this.dE = b * e;
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 3);
this.b$["J.minimize.forcefield.CalculationsMMFF"].setPairVariables (this);
this.dE = b * this.delta;
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 2);
}return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$TorsionCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "TorsionCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].isLinear (b.data[1]) || this.b$["J.minimize.forcefield.CalculationsMMFF"].isLinear (b.data[2])) return;
var c = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameterObj (b);
if (c == null) return;
a.addLast ([b.data, c, b.key]);
}, "J.util.JmolList,J.minimize.MinTorsion");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.key = a[2];
this.getPointers (a);
var b = this.dData[0];
var c = this.dData[1];
var d = this.dData[2];
this.b$["J.minimize.forcefield.CalculationsMMFF"].setTorsionVariables (this);
var e = Math.cos (this.theta);
var f = e * e;
this.energy = 0.5 * (b * (1 + e) + c * (2 - 2 * f) + d * (1 + e * (4 * f - 3)));
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
var g = Math.sin (this.theta);
this.dE = 0.5 * (-b * g + 4 * c * g * e + 3 * d * g * (1 - 4 * f));
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 4);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (3, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$OOPCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.list = null;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "OOPCalc", J.minimize.forcefield.Calculation);
Clazz.prepareFields (c$, function () {
this.list =  Clazz.newIntArray (4, 0);
});
$_M(c$, "setData", 
function (a, b) {
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b].nBonds != 3) return;
var c = this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b].getBondedAtomIndexes ();
this.list[0] = c[2];
this.list[1] = b;
this.list[2] = c[1];
this.list[3] = c[0];
var d = this.b$["J.minimize.forcefield.CalculationsMMFF"].mmff.getOutOfPlaneParameter (this.list);
if (d == 0) return;
var e = [d];
a.addLast ([[c[0], b, c[1], c[2]], e]);
a.addLast ([[c[1], b, c[2], c[0]], e]);
a.addLast ([[c[2], b, c[0], c[1]], e]);
}, "J.util.JmolList,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
this.b$["J.minimize.forcefield.CalculationsMMFF"].setOopVariables (this, false);
var b = this.dData[0];
this.energy = 71.96568080495746 * b * this.theta * this.theta;
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
this.dE = 2.5120761569715815 * b * this.theta;
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 4);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (4, this));
return this.energy;
}, "~A");
Clazz.defineStatics (c$,
"FOOPD", 2.5120761569715815,
"FOOP", 71.96568080495746);
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$VDWCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "VDWCalc", J.minimize.forcefield.Calculations.PairCalc, null, Clazz.innerTypeInstance (J.minimize.forcefield.Calculations.PairCalc, this, null, Clazz.inheritArgs));
Clazz.overrideMethod (c$, "setData", 
function (a, b, c) {
this.a = this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b];
this.b = this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[c];
var d = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameter (this.a.vdwKey);
var e = this.b$["J.minimize.forcefield.CalculationsMMFF"].getParameter (this.b.vdwKey);
if (d == null || e == null) return;
var f = d[0];
var g = d[1];
var h = d[2];
var i = d[3];
var j = Clazz.doubleToInt (d[4]);
var k = e[0];
var l = e[1];
var m = e[2];
var n = e[3];
var o = Clazz.doubleToInt (e[4]);
var p = h * Math.pow (f, 0.25);
var q = m * Math.pow (k, 0.25);
var r = (p - q) / (p + q);
var s = 0.5 * (p + q);
if (j != 68 && o != 68) s *= (1.0 + 0.2 * (1.0 - Math.exp (-12.0 * r * r)));
var t = ((181.16 * i * n * f * k) / (Math.sqrt (f / g) + Math.sqrt (k / l))) * Math.pow (s, -6.0);
if (j + o == 133) {
s *= 0.8;
t *= 0.5;
}a.addLast ([[b, c], [s, t]]);
}, "J.util.JmolList,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
this.b$["J.minimize.forcefield.CalculationsMMFF"].setPairVariables (this);
var b = this.dData[0];
var c = this.dData[1];
var d = this.rab / b;
var e = 1.07 / (d + 0.07);
var f = 1.12 / (Math.pow (d, 7) + 0.12);
this.energy = c * Math.pow (e, 7) * (f - 2);
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
this.dE = -7 * c * Math.pow (e, 7) / b * (e / 1.07 * (f - 2) + f * f * Math.pow (d, 6));
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 2);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging && Math.abs (this.energy) > 0.1) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (5, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsMMFF$ESCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsMMFF, "ESCalc", J.minimize.forcefield.Calculations.PairCalc, null, Clazz.innerTypeInstance (J.minimize.forcefield.Calculations.PairCalc, this, null, Clazz.inheritArgs));
Clazz.overrideMethod (c$, "setData", 
function (a, b, c) {
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b].partialCharge == 0 || this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[c].partialCharge == 0) return;
a.addLast ([[b, c], [this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b].partialCharge, this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[c].partialCharge, (this.b$["J.minimize.forcefield.CalculationsMMFF"].minAtoms[b].bs14.get (c) ? 249.0537 : 332.0716)]]);
}, "J.util.JmolList,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = this.dData[0] * this.dData[1] * this.dData[2];
this.b$["J.minimize.forcefield.CalculationsMMFF"].setPairVariables (this);
var c = this.rab + 0.05;
this.energy = b / c;
if (this.b$["J.minimize.forcefield.CalculationsMMFF"].gradients) {
this.dE = -this.energy / c;
this.b$["J.minimize.forcefield.CalculationsMMFF"].addForces (this, 2);
}if (this.b$["J.minimize.forcefield.CalculationsMMFF"].logging && Math.abs (this.energy) > 20) this.b$["J.minimize.forcefield.CalculationsMMFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsMMFF"].getDebugLine (6, this));
return this.energy;
}, "~A");
Clazz.defineStatics (c$,
"BUFF", 0.05);
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"FPAR", 143.9325,
"DA_D", 'D',
"DA_DA", 133);
});
