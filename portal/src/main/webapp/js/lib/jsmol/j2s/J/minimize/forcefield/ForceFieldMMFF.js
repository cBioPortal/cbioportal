Clazz.declarePackage ("J.minimize.forcefield");
Clazz.load (["J.minimize.forcefield.ForceField"], "J.minimize.forcefield.ForceFieldMMFF", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.minimize.MinAtom", "$.MinObject", "J.minimize.forcefield.AtomType", "$.CalculationsMMFF", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Elements", "$.Escape", "$.JmolList", "$.Logger", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.useEmpiricalRules = true;
this.rawAtomTypes = null;
this.rawBondTypes = null;
this.rawMMFF94Charges = null;
this.vRings = null;
this.typeData = null;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "ForceFieldMMFF", J.minimize.forcefield.ForceField);
Clazz.prepareFields (c$, function () {
this.typeData =  Clazz.newIntArray (4, 0);
});
$_M(c$, "getAtomTypeDescriptions", 
function () {
return J.minimize.forcefield.ForceFieldMMFF.getAtomTypeDescs (this.rawAtomTypes);
});
$_M(c$, "getPartialCharges", 
function () {
return this.rawMMFF94Charges;
});
Clazz.makeConstructor (c$, 
function (m) {
Clazz.superConstructor (this, J.minimize.forcefield.ForceFieldMMFF, []);
this.minimizer = m;
this.name = "MMFF";
this.getChargeParameters ();
}, "J.minimize.Minimizer");
Clazz.overrideMethod (c$, "clear", 
function () {
});
Clazz.overrideMethod (c$, "setModel", 
function (bsElements, elemnoMax) {
this.getMinimizationParameters ();
var m = this.minimizer;
if (!this.setArrays (m.atoms, m.bsAtoms, m.bonds, m.rawBondCount, false, false)) return false;
this.setModelFields ();
this.fixTypes ();
this.calc =  new J.minimize.forcefield.CalculationsMMFF (this, J.minimize.forcefield.ForceFieldMMFF.ffParams, this.minAtoms, this.minBonds, this.minAngles, this.minTorsions, this.minPositions, this.minimizer.constraints);
this.calc.setLoggingEnabled (true);
return this.calc.setupCalculations ();
}, "J.util.BS,~N");
$_M(c$, "setArrays", 
function (atoms, bsAtoms, bonds, rawBondCount, doRound, allowUnknowns) {
var m = this.minimizer;
this.vRings = J.util.ArrayUtil.createArrayOfArrayList (4);
this.rawAtomTypes = J.minimize.forcefield.ForceFieldMMFF.setAtomTypes (atoms, bsAtoms, m.viewer.getSmilesMatcher (), this.vRings, allowUnknowns);
if (this.rawAtomTypes == null) return false;
this.rawBondTypes = this.setBondTypes (bonds, rawBondCount, bsAtoms);
this.rawMMFF94Charges = J.minimize.forcefield.ForceFieldMMFF.calculatePartialCharges (bonds, this.rawBondTypes, atoms, this.rawAtomTypes, bsAtoms, doRound);
return true;
}, "~A,J.util.BS,~A,~N,~B,~B");
$_M(c$, "getChargeParameters", 
($fz = function () {
if (J.minimize.forcefield.ForceFieldMMFF.ffParams != null) return;
this.getAtomTypes ("MMFF94-smarts.txt");
var data =  new java.util.Hashtable ();
this.getMmffParameters ("mmffpbci.par.txt", data, 1);
this.getMmffParameters ("mmffchg.par.txt", data, 34);
($t$ = J.minimize.forcefield.ForceFieldMMFF.ffParams = data, J.minimize.forcefield.ForceFieldMMFF.prototype.ffParams = J.minimize.forcefield.ForceFieldMMFF.ffParams, $t$);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getMinimizationParameters", 
($fz = function () {
if (J.minimize.forcefield.ForceFieldMMFF.ffParams.containsKey (Integer.$valueOf (-1))) return;
this.getMmffParameters ("mmffang.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 5);
this.getMmffParameters ("mmffbndk.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 546);
this.getMmffParameters ("mmffbond.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 3);
this.getMmffParameters ("mmffoop.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 13);
this.getMmffParameters ("mmffstbn.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 21);
this.getMmffParameters ("mmffdfsb.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 37);
this.getMmffParameters ("mmfftor.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 9);
this.getMmffParameters ("mmffvdw.par.txt", J.minimize.forcefield.ForceFieldMMFF.ffParams, 17);
J.minimize.forcefield.ForceFieldMMFF.ffParams.put (Integer.$valueOf (-1), Boolean.TRUE);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getMmffParameters", 
($fz = function (fileName, data, dataType) {
var line = null;
var value = null;
if (J.util.Logger.debugging) J.util.Logger.debug ("reading data from " + fileName);
try {
var br = this.getBufferedReader (fileName);
while ((line = br.readLine ()) != null && line.length < 5 || !line.startsWith ("*")) continue;

var a1 = 0;
var a2 = 127;
var a3 = 127;
var a4 = 127;
var type = 0;
switch (dataType) {
case 3:
case 5:
case 9:
break;
case 34:
a4 = 124;
break;
case 21:
a4 = 125;
break;
case 546:
a4 = 123;
type = 0;
break;
case 13:
type = 6;
break;
case 1:
type = 0;
break;
case 37:
a4 = 126;
type = 0;
break;
case 17:
a4 = 122;
type = 0;
break;
}
while ((line = br.readLine ()) != null) {
if (line.length < 5 || line.startsWith ("*")) continue;
switch (dataType) {
case 546:
case 13:
case 1:
case 37:
break;
case 17:
if (line.charAt (5) != ' ') continue;
break;
case 34:
if (line.charAt (0) == '4') continue;
case 5:
case 3:
case 21:
case 9:
type = line.charCodeAt (0) - 48;
break;
}
switch (dataType) {
case 13:
case 9:
a4 = J.util.Parser.parseInt (line.substring (18, 20).trim ());
case 5:
case 21:
case 37:
a3 = J.util.Parser.parseInt (line.substring (13, 15).trim ());
case 546:
case 3:
case 34:
a2 = J.util.Parser.parseInt (line.substring (8, 10).trim ());
case 1:
case 17:
a1 = J.util.Parser.parseInt (line.substring (3, 5).trim ());
break;
}
switch (dataType) {
case 546:
value = [J.util.Parser.dVal (line.substring (19, 25).trim ()), J.util.Parser.dVal (line.substring (13, 18).trim ())];
break;
case 3:
value = [J.util.Parser.dVal (line.substring (14, 20).trim ()), J.util.Parser.dVal (line.substring (25, 31).trim ())];
break;
case 5:
case 21:
value = [J.util.Parser.dVal (line.substring (19, 25).trim ()), J.util.Parser.dVal (line.substring (28, 35).trim ())];
break;
case 34:
value = Float.$valueOf (J.util.Parser.fVal (line.substring (10, 20).trim ()));
break;
case 13:
value = [J.util.Parser.dVal (line.substring (24, 30).trim ())];
break;
case 1:
value = Float.$valueOf (J.util.Parser.fVal (line.substring (5, 15).trim ()));
break;
case 37:
var v1 = J.util.Parser.dVal (line.substring (19, 25).trim ());
var v2 = J.util.Parser.dVal (line.substring (28, 35).trim ());
value = [v1, v2];
var key = J.minimize.MinObject.getKey (type, a1, a2, a3, a4);
data.put (key, value);
value = [v2, v1];
var a = a1;
a1 = a3;
a3 = a;
break;
case 9:
value = [J.util.Parser.dVal (line.substring (22, 28).trim ()), J.util.Parser.dVal (line.substring (30, 36).trim ()), J.util.Parser.dVal (line.substring (38, 44).trim ())];
break;
case 17:
value = [J.util.Parser.dVal (line.substring (10, 15).trim ()), J.util.Parser.dVal (line.substring (20, 25).trim ()), J.util.Parser.dVal (line.substring (30, 35).trim ()), J.util.Parser.dVal (line.substring (40, 45).trim ()), line.charAt (46)];
break;
}
var key = J.minimize.MinObject.getKey (type, a1, a2, a3, a4);
data.put (key, value);
if (J.util.Logger.debugging) J.util.Logger.debug (J.minimize.MinObject.decodeKey (key) + " " + (Clazz.instanceOf (value, Float) ? value : J.util.Escape.eAD (value)));
}
br.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.err.println ("Exception " + e.toString () + " in getResource " + fileName + " line=" + line);
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz), "~S,java.util.Map,~N");
$_M(c$, "getAtomTypes", 
($fz = function (fileName) {
var types =  new J.util.JmolList ();
var line = null;
try {
var br = this.getBufferedReader (fileName);
var at;
types.addLast ( new J.minimize.forcefield.AtomType (0, 0, 0, 0, 1, "H or NOT FOUND", ""));
while ((line = br.readLine ()) != null) {
if (line.length == 0 || line.startsWith ("#")) continue;
var elemNo = J.util.Parser.parseInt (line.substring (3, 5).trim ());
var mmType = J.util.Parser.parseInt (line.substring (6, 8).trim ());
var hType = J.util.Parser.parseInt (line.substring (9, 11).trim ());
var formalCharge = J.util.Parser.fVal (line.substring (12, 15).trim ()) / 12;
var val = J.util.Parser.parseInt (line.substring (16, 18).trim ());
var desc = line.substring (19, 44).trim ();
var smarts = line.substring (45).trim ();
types.addLast (at =  new J.minimize.forcefield.AtomType (elemNo, mmType, hType, formalCharge, val, desc, smarts));
J.minimize.forcefield.ForceFieldMMFF.setFlags (at);
}
br.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.err.println ("Exception " + e.toString () + " in getResource " + fileName + " line=" + line);
} else {
throw e;
}
}
J.util.Logger.info ((types.size () - 1) + " SMARTS-based atom types read");
($t$ = J.minimize.forcefield.ForceFieldMMFF.atomTypes = types, J.minimize.forcefield.ForceFieldMMFF.prototype.atomTypes = J.minimize.forcefield.ForceFieldMMFF.atomTypes, $t$);
}, $fz.isPrivate = true, $fz), "~S");
c$.setFlags = $_M(c$, "setFlags", 
($fz = function (at) {
switch (at.mmType) {
case 32:
case 35:
case 72:
at.fcadj = 0.5;
break;
case 62:
case 76:
at.fcadj = 0.25;
break;
}
switch (at.mmType) {
case 37:
case 38:
case 39:
case 44:
case 58:
case 59:
case 63:
case 64:
case 65:
case 66:
case 69:
case 78:
case 79:
case 81:
case 82:
at.arom = true;
}
switch (at.mmType) {
case 2:
case 3:
case 4:
case 9:
case 30:
case 37:
case 39:
case 54:
case 57:
case 58:
case 63:
case 64:
case 67:
case 75:
case 78:
case 80:
case 81:
at.sbmb = true;
}
switch (at.mmType) {
case 6:
case 8:
case 10:
case 11:
case 12:
case 13:
case 14:
case 15:
case 26:
case 32:
case 35:
case 39:
case 40:
case 43:
case 44:
case 59:
case 62:
case 70:
case 72:
case 76:
at.pilp = true;
}
switch (at.mmType) {
case 10:
case 32:
case 35:
case 39:
case 41:
case 44:
case 55:
case 56:
case 58:
case 59:
case 69:
case 72:
case 81:
case 82:
at.mltb = 1;
break;
case 2:
case 3:
case 7:
case 9:
case 16:
case 17:
case 30:
case 37:
case 38:
case 45:
case 46:
case 47:
case 51:
case 53:
case 54:
case 57:
case 63:
case 64:
case 65:
case 66:
case 67:
case 74:
case 75:
case 78:
case 79:
case 80:
at.mltb = 2;
break;
case 4:
case 42:
case 60:
case 61:
at.mltb = 3;
break;
}
}, $fz.isPrivate = true, $fz), "J.minimize.forcefield.AtomType");
c$.calculatePartialCharges = $_M(c$, "calculatePartialCharges", 
function (bonds, bTypes, atoms, aTypes, bsAtoms, doRound) {
var partialCharges =  Clazz.newFloatArray (atoms.length, 0);
for (var i = bsAtoms.nextSetBit (0); i >= 0; i = bsAtoms.nextSetBit (i + 1)) partialCharges[i] = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, aTypes[i])).formalCharge;

var a1 = null;
for (var i = bTypes.length; --i >= 0; ) {
a1 = bonds[i].getAtom1 ();
var a2 = bonds[i].getAtom2 ();
var ok1 = bsAtoms.get (a1.index);
var ok2 = bsAtoms.get (a2.index);
if (!ok1 && !ok2) continue;
var it = aTypes[a1.index];
var at1 = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, it));
var type1 = (it < 0 ? -it : at1.mmType);
it = aTypes[a2.index];
var at2 = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, it));
var type2 = (it < 0 ? -it : at2.mmType);
var dq;
try {
var bondType = bTypes[i];
var bFactor = (type1 < type2 ? -1 : 1);
var key = J.minimize.MinObject.getKey (bondType, bFactor == 1 ? type2 : type1, bFactor == 1 ? type1 : type2, 127, 124);
var bciValue = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (key);
var bci;
var msg = (J.util.Logger.debugging ? a1 + "/" + a2 + " mmTypes=" + type1 + "/" + type2 + " formalCharges=" + at1.formalCharge + "/" + at2.formalCharge + " bci = " : null);
if (bciValue == null) {
var pa = (J.minimize.forcefield.ForceFieldMMFF.ffParams.get (J.minimize.MinObject.getKey (0, type1, 127, 127, 127))).floatValue ();
var pb = (J.minimize.forcefield.ForceFieldMMFF.ffParams.get (J.minimize.MinObject.getKey (0, type2, 127, 127, 127))).floatValue ();
bci = pa - pb;
if (J.util.Logger.debugging) msg += pa + " - " + pb + " = ";
} else {
bci = bFactor * bciValue.floatValue ();
}if (J.util.Logger.debugging) {
msg += bci;
J.util.Logger.debug (msg);
}dq = at2.fcadj * at2.formalCharge - at1.fcadj * at1.formalCharge + bci;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
dq = NaN;
} else {
throw e;
}
}
if (ok1) partialCharges[a1.index] += dq;
if (ok2) partialCharges[a2.index] -= dq;
}
if (doRound) {
var abscharge = 0;
for (var i = partialCharges.length; --i >= 0; ) {
partialCharges[i] = (Math.round (partialCharges[i] * 1000)) / 1000;
abscharge += Math.abs (partialCharges[i]);
}
if (abscharge == 0 && a1 != null) {
partialCharges[a1.index] = -0.0;
}}return partialCharges;
}, "~A,~A,~A,~A,J.util.BS,~B");
c$.isBondType1 = $_M(c$, "isBondType1", 
($fz = function (at1, at2) {
return at1.sbmb && at2.sbmb || at1.arom && at2.arom;
}, $fz.isPrivate = true, $fz), "J.minimize.forcefield.AtomType,J.minimize.forcefield.AtomType");
$_M(c$, "getBondType", 
($fz = function (bond, at1, at2, index1, index2) {
return (J.minimize.forcefield.ForceFieldMMFF.isBondType1 (at1, at2) && bond.getCovalentOrder () == 1 && !this.isAromaticBond (index1, index2) ? 1 : 0);
}, $fz.isPrivate = true, $fz), "J.modelset.Bond,J.minimize.forcefield.AtomType,J.minimize.forcefield.AtomType,~N,~N");
$_M(c$, "isAromaticBond", 
($fz = function (a1, a2) {
if (this.vRings[3] != null) for (var i = this.vRings[3].size (); --i >= 0; ) {
var bsRing = this.vRings[3].get (i);
if (bsRing.get (a1) && bsRing.get (a2)) return true;
}
return false;
}, $fz.isPrivate = true, $fz), "~N,~N");
c$.getAtomTypeDescs = $_M(c$, "getAtomTypeDescs", 
function (types) {
var stypes =  new Array (types.length);
for (var i = types.length; --i >= 0; ) {
stypes[i] = String.valueOf (types[i] < 0 ? -types[i] : J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (types[i]).mmType);
}
return stypes;
}, "~A");
c$.setAtomTypes = $_M(c$, "setAtomTypes", 
($fz = function (atoms, bsAtoms, smartsMatcher, vRings, allowUnknowns) {
var bitSets =  new J.util.JmolList ();
var smarts =  new Array (J.minimize.forcefield.ForceFieldMMFF.atomTypes.size ());
var types =  Clazz.newIntArray (atoms.length, 0);
var bsElements =  new J.util.BS ();
var bsHydrogen =  new J.util.BS ();
var bsConnected = J.util.BSUtil.copy (bsAtoms);
for (var i = bsAtoms.nextSetBit (0); i >= 0; i = bsAtoms.nextSetBit (i + 1)) {
var a = atoms[i];
var bonds = a.getBonds ();
if (bonds != null) for (var j = bonds.length; --j >= 0; ) if (bonds[j].isCovalent ()) bsConnected.set (bonds[j].getOtherAtom (a).index);

}
for (var i = bsConnected.nextSetBit (0); i >= 0; i = bsConnected.nextSetBit (i + 1)) {
var n = atoms[i].getElementNumber ();
switch (n) {
case 1:
bsHydrogen.set (i);
break;
default:
bsElements.set (n);
}
}
var nUsed = 0;
for (var i = 1; i < J.minimize.forcefield.ForceFieldMMFF.atomTypes.size (); i++) {
var at = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (i);
if (!bsElements.get (at.elemNo)) continue;
smarts[i] = at.smartsCode;
nUsed++;
}
J.util.Logger.info (nUsed + " SMARTS matches used");
smartsMatcher.getSubstructureSets (smarts, atoms, atoms.length, 20, bsConnected, bitSets, vRings);
var bsDone =  new J.util.BS ();
for (var j = 0; j < bitSets.size (); j++) {
var bs = bitSets.get (j);
if (bs == null) continue;
bs.andNot (bsDone);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) types[i] = j;

bsDone.or (bs);
}
for (var i = bsHydrogen.nextSetBit (0); i >= 0; i = bsHydrogen.nextSetBit (i + 1)) {
var bonds = atoms[i].getBonds ();
if (bonds != null) {
var j = types[bonds[0].getOtherAtom (atoms[i]).index];
if (j != 0) bsDone.set (i);
types[i] = -J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (j).hType;
}}
if (J.util.Logger.debugging) for (var i = bsConnected.nextSetBit (0); i >= 0; i = bsConnected.nextSetBit (i + 1)) J.util.Logger.debug ("atom " + atoms[i] + "\ttype " + (types[i] < 0 ? "" + -types[i] : (J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (types[i]).mmType + "\t" + J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (types[i]).smartsCode + "\t" + J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (types[i]).descr)));

if (!allowUnknowns && bsDone.cardinality () != bsConnected.cardinality ()) return null;
return types;
}, $fz.isPrivate = true, $fz), "~A,J.util.BS,J.api.SmilesMatcherInterface,~A,~B");
$_M(c$, "setBondTypes", 
($fz = function (bonds, bondCount, bsAtoms) {
var bTypes =  Clazz.newIntArray (bondCount, 0);
for (var i = bondCount; --i >= 0; ) {
var a1 = bonds[i].getAtom1 ();
var a2 = bonds[i].getAtom2 ();
var ok1 = bsAtoms.get (a1.index);
var ok2 = bsAtoms.get (a2.index);
if (!ok1 && !ok2) continue;
var it = this.rawAtomTypes[a1.index];
var at1 = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, it));
it = this.rawAtomTypes[a2.index];
var at2 = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, it));
bTypes[i] = this.getBondType (bonds[i], at1, at2, a1.index, a2.index);
}
return bTypes;
}, $fz.isPrivate = true, $fz), "~A,~N,J.util.BS");
$_M(c$, "fixTypes", 
($fz = function () {
for (var i = this.minAtomCount; --i >= 0; ) {
var a = this.minAtoms[i];
var rawIndex = a.atom.index;
var it = this.rawAtomTypes[rawIndex];
a.ffAtomType = J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (Math.max (0, it));
var type = (it < 0 ? -it : J.minimize.forcefield.ForceFieldMMFF.atomTypes.get (it).mmType);
a.ffType = type;
a.vdwKey = J.minimize.MinObject.getKey (0, type, 127, 127, 122);
a.partialCharge = this.rawMMFF94Charges[rawIndex];
}
for (var i = this.minBonds.length; --i >= 0; ) {
var bond = this.minBonds[i];
bond.type = this.rawBondTypes[bond.rawIndex];
bond.key = this.getKey (bond, bond.type, 3);
}
for (var i = this.minAngles.length; --i >= 0; ) {
var angle = this.minAngles[i];
angle.key = this.getKey (angle, angle.type, 5);
angle.sbKey = this.getKey (angle, angle.sbType, 21);
}
for (var i = this.minTorsions.length; --i >= 0; ) {
var torsion = this.minTorsions[i];
torsion.key = this.getKey (torsion, torsion.type, 9);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAngleType", 
($fz = function (angle) {
angle.type = this.minBonds[angle.data[3]].type + this.minBonds[angle.data[4]].type;
if (this.checkRings (this.vRings[0], angle.data, 3)) {
angle.type += (angle.type == 0 ? 3 : 4);
} else if (this.checkRings (this.vRings[1], angle.data, 3)) {
angle.type += (angle.type == 0 ? 4 : 6);
}angle.sbType = J.minimize.forcefield.ForceFieldMMFF.sbMap[angle.type];
switch (angle.type) {
case 1:
case 5:
case 7:
angle.sbType += this.minBonds[angle.data[4]].type;
break;
}
return angle.type;
}, $fz.isPrivate = true, $fz), "J.minimize.MinAngle");
$_M(c$, "setTorsionType", 
($fz = function (t) {
if (this.checkRings (this.vRings[1], t.data, 4)) return (t.type = 4);
t.type = (this.minBonds[t.data[5]].type == 1 ? 1 : this.minBonds[t.data[4]].type == 0 && this.minBonds[t.data[6]].type == 0 ? 0 : 2);
if (t.type == 0 && this.checkRings (this.vRings[2], t.data, 4)) {
t.type = 5;
}return t.type;
}, $fz.isPrivate = true, $fz), "J.minimize.MinTorsion");
$_M(c$, "typeOf", 
($fz = function (iAtom) {
return this.minAtoms[iAtom].ffType;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "checkRings", 
($fz = function (v, minlist, n) {
if (v != null) for (var i = v.size (); --i >= 0; ) {
var bs = v.get (i);
if (bs.get (this.minAtoms[minlist[0]].atom.index) && bs.get (this.minAtoms[minlist[1]].atom.index) && (n < 3 || bs.get (this.minAtoms[minlist[2]].atom.index)) && (n < 4 || bs.get (this.minAtoms[minlist[3]].atom.index))) return true;
}
return false;
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~A,~N");
$_M(c$, "getKey", 
($fz = function (obj, type, ktype) {
var o = (Clazz.instanceOf (obj, J.minimize.MinObject) ? obj : null);
var data = (o == null ? obj : o.data);
var n = 4;
switch (ktype) {
case 3:
this.fixOrder (data, 0, 1);
n = 2;
break;
case 5:
if (this.fixOrder (data, 0, 2) == -1) J.minimize.forcefield.ForceFieldMMFF.swap (data, 3, 4);
type = this.setAngleType (o);
n = 3;
break;
case 21:
n = 3;
break;
case 9:
switch (this.fixOrder (data, 1, 2)) {
case 1:
break;
case -1:
J.minimize.forcefield.ForceFieldMMFF.swap (data, 0, 3);
J.minimize.forcefield.ForceFieldMMFF.swap (data, 4, 6);
break;
case 0:
if (this.fixOrder (data, 0, 3) == -1) J.minimize.forcefield.ForceFieldMMFF.swap (data, 4, 6);
break;
}
type = this.setTorsionType (o);
}
var key = null;
for (var i = 0; i < 4; i++) this.typeData[i] = (i < n ? this.typeOf (data[i]) : 127);

switch (ktype) {
case 21:
this.typeData[3] = 125;
break;
case 13:
J.minimize.forcefield.ForceFieldMMFF.sortOop (this.typeData);
break;
}
key = J.minimize.MinObject.getKey (type, this.typeData[0], this.typeData[1], this.typeData[2], this.typeData[3]);
var ddata = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (key);
switch (ktype) {
case 3:
if (!this.useEmpiricalRules) return key;
return (ddata != null && ddata[0] > 0 ? key : this.applyEmpiricalRules (o, ddata, 3));
case 5:
if (ddata != null && ddata[0] != 0) return key;
break;
case 9:
if (ddata == null) {
if (!J.minimize.forcefield.ForceFieldMMFF.ffParams.containsKey (key = this.getTorsionKey (type, 0, 2)) && !J.minimize.forcefield.ForceFieldMMFF.ffParams.containsKey (key = this.getTorsionKey (type, 2, 0)) && !J.minimize.forcefield.ForceFieldMMFF.ffParams.containsKey (key = this.getTorsionKey (type, 2, 2))) key = this.getTorsionKey (0, 2, 2);
ddata = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (key);
}if (!this.useEmpiricalRules) return key;
return (ddata != null ? key : this.applyEmpiricalRules (o, ddata, 9));
case 21:
if (ddata != null) return key;
var r1 = this.getRowFor (data[0]);
var r2 = this.getRowFor (data[1]);
var r3 = this.getRowFor (data[2]);
return J.minimize.MinObject.getKey (0, r1, r2, r3, 126);
case 13:
if (ddata != null) return key;
}
if (!this.useEmpiricalRules && ddata != null) return key;
var isSwapped = false;
var haveKey = false;
for (var i = 0; i < 3 && !haveKey; i++) {
for (var j = 0, bit = 1; j < n; j++, bit <<= 1) if ((ktype & bit) == bit) this.typeData[j] = J.minimize.forcefield.ForceFieldMMFF.getEquivalentType (this.typeOf (data[j]), i);

switch (ktype) {
case 3:
isSwapped = (J.minimize.forcefield.ForceFieldMMFF.fixTypeOrder (this.typeData, 0, 1));
break;
case 5:
isSwapped = (J.minimize.forcefield.ForceFieldMMFF.fixTypeOrder (this.typeData, 0, 2));
break;
case 13:
J.minimize.forcefield.ForceFieldMMFF.sortOop (this.typeData);
break;
}
key = J.minimize.MinObject.getKey (type, this.typeData[0], this.typeData[1], this.typeData[2], this.typeData[3]);
haveKey = J.minimize.forcefield.ForceFieldMMFF.ffParams.containsKey (key);
}
if (haveKey) {
if (isSwapped) switch (ktype) {
case 5:
J.minimize.forcefield.ForceFieldMMFF.swap (data, 0, 2);
J.minimize.forcefield.ForceFieldMMFF.swap (data, 3, 4);
this.setAngleType (o);
break;
}
} else if (type != 0 && ktype == 5) {
key = Integer.$valueOf (key.intValue () ^ 0xFF);
}if (!this.useEmpiricalRules) return key;
ddata = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (key);
switch (ktype) {
case 5:
return (ddata != null && ddata[0] != 0 ? key : this.applyEmpiricalRules (o, ddata, 5));
}
return key;
}, $fz.isPrivate = true, $fz), "~O,~N,~N");
$_M(c$, "getTorsionKey", 
($fz = function (type, i, j) {
return J.minimize.MinObject.getKey (type, J.minimize.forcefield.ForceFieldMMFF.getEquivalentType (this.typeData[0], i), this.typeData[1], this.typeData[2], J.minimize.forcefield.ForceFieldMMFF.getEquivalentType (this.typeData[3], j));
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "applyEmpiricalRules", 
($fz = function (o, ddata, ktype) {
var rr;
var rr2;
var beta = 0;
var a;
var b;
var c;
switch (ktype) {
case 3:
a = this.minAtoms[o.data[0]];
b = this.minAtoms[o.data[1]];
var elemno1 = a.atom.getElementNumber ();
var elemno2 = b.atom.getElementNumber ();
var key = J.minimize.MinObject.getKey (0, Math.min (elemno1, elemno2), Math.max (elemno1, elemno2), 127, 123);
ddata = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (key);
if (ddata == null) return null;
var kbref = ddata[0];
var r0ref = ddata[1];
var r0 = J.minimize.forcefield.ForceFieldMMFF.getRuleBondLength (a, b, (o).order, this.isAromaticBond (o.data[0], o.data[1]));
if (r0 == 0) return null;
rr = r0ref / r0;
rr2 = rr * rr;
var rr4 = rr2 * rr2;
var rr6 = rr4 * rr2;
var kb = kbref * rr6;
o.ddata = [kb, r0];
return Integer.$valueOf (-1);
case 5:
var theta0;
if (ddata == null || (theta0 = ddata[1]) == 0) {
b = this.minAtoms[o.data[1]];
var atom = b.atom;
var elemno = atom.getElementNumber ();
switch (o.type) {
case 3:
case 5:
case 6:
theta0 = 60;
beta *= 0.05;
break;
case 4:
case 7:
case 8:
theta0 = 90;
break;
default:
theta0 = 120;
var crd = atom.getCovalentBondCount ();
switch (crd) {
case 2:
if (J.minimize.MinAtom.isLinear (b)) theta0 = 180;
 else if (elemno == 8) theta0 = 105;
 else if (elemno > 10) theta0 = 95.0;
break;
case 3:
if (b.ffAtomType.mltb == 0 && b.ffAtomType.val == 3) theta0 = (elemno == 7 ? 107 : 92);
break;
case 4:
theta0 = 109.45;
break;
}
}
}beta = 1.75;
switch (o.type) {
case 3:
case 5:
case 6:
beta *= 0.05;
break;
case 4:
case 7:
case 8:
beta *= 0.85;
break;
}
var za = J.minimize.forcefield.ForceFieldMMFF.getZParam (this.minAtoms[o.data[0]].atom.getElementNumber ());
var cb = J.minimize.forcefield.ForceFieldMMFF.getCParam (this.minAtoms[o.data[1]].atom.getElementNumber ());
var zc = J.minimize.forcefield.ForceFieldMMFF.getZParam (this.minAtoms[o.data[2]].atom.getElementNumber ());
var r0ab = J.minimize.forcefield.ForceFieldMMFF.getR0 (this.minBonds[o.data[3]]);
var r0bc = J.minimize.forcefield.ForceFieldMMFF.getR0 (this.minBonds[o.data[4]]);
rr = r0ab + r0bc;
rr2 = rr * rr;
var D = (r0ab - r0bc) / rr2;
var theta2 = theta0 * 0.017453292519943295;
theta2 *= theta2;
var ka = (beta * za * cb * zc * Math.exp (-2 * D)) / (rr * theta2);
o.ddata = [ka, theta0];
return Integer.$valueOf (-1);
case 9:
var ib = o.data[1];
var ic = o.data[2];
b = this.minAtoms[ib];
c = this.minAtoms[ic];
if (J.minimize.MinAtom.isLinear (b) || J.minimize.MinAtom.isLinear (c)) return null;
var bondBC = this.minBonds[o.data[5]];
var elemnoB = b.atom.getElementNumber ();
var elemnoC = c.atom.getElementNumber ();
var ub = J.minimize.forcefield.ForceFieldMMFF.getUParam (elemnoB);
var uc = J.minimize.forcefield.ForceFieldMMFF.getUParam (elemnoC);
var vb = J.minimize.forcefield.ForceFieldMMFF.getVParam (elemnoB);
var vc = J.minimize.forcefield.ForceFieldMMFF.getVParam (elemnoC);
var v1 = 0;
var v2 = 0;
var v3 = 0;
var pi_bc = -1;
var n_bc = -1;
var wb = -1;
var wc = 0;
var valB = b.ffAtomType.val;
var valC = c.ffAtomType.val;
var pilpB = b.ffAtomType.pilp;
var pilpC = c.ffAtomType.pilp;
var mltbB = b.ffAtomType.mltb;
var mltbC = c.ffAtomType.mltb;
out : while (true) {
if (this.isAromaticBond (ib, ic)) {
pi_bc = (pilpB || pilpC ? 0.3 : 0.5);
beta = (valB + valC == 7 ? 3 : 6);
break out;
}if (bondBC.order == 2) {
beta = 6;
pi_bc = (mltbB == 2 && mltbC == 2 ? 1.0 : 0.4);
break out;
}var crdB = b.atom.getCovalentBondCount ();
var crdC = c.atom.getCovalentBondCount ();
if (crdB == 4 && crdC == 4) {
vb = J.minimize.forcefield.ForceFieldMMFF.getVParam (elemnoB);
vc = J.minimize.forcefield.ForceFieldMMFF.getVParam (elemnoC);
n_bc = 9;
break out;
}if (crdB != 4 && (valB > crdB || mltbB > 0) || crdC != 4 && (valC > crdC || mltbC > 0)) return null;
var case2 = (pilpB && mltbC > 0);
var case3 = (pilpC && mltbB > 0);
if (bondBC.order == 1 && (mltbB > 0 && mltbC > 0 || case2 || case3)) {
if (pilpB && pilpC) return null;
beta = 6;
if (case2) {
pi_bc = (mltbC == 1 ? 0.5 : elemnoB <= 10 && elemnoC <= 10 ? 0.3 : 0.15);
break out;
}if (case3) {
pi_bc = (mltbB == 1 ? 0.5 : elemnoB <= 10 && elemnoC <= 10 ? 0.3 : 0.15);
break out;
}if ((mltbB == 1 || mltbC == 1) && (elemnoB == 6 || elemnoC == 6)) {
pi_bc = 0.4;
break out;
}pi_bc = 0.15;
break out;
}switch (elemnoB << 8 + elemnoC) {
case 0x808:
wb = wc = 2;
break out;
case 0x810:
wb = 2;
wc = 8;
break out;
case 0x1008:
wb = 8;
wc = 2;
break out;
case 0x1010:
wb = wc = 8;
break out;
}
n_bc = crdB * crdC;
break out;
}
if (pi_bc > 0) v2 = beta * pi_bc * Math.sqrt (ub * uc);
 else if (n_bc > 0) v3 = Math.sqrt (vb * vc) / n_bc;
 else if (wb != 0) v2 = -Math.sqrt (wb * wc);
o.ddata = [v1, v2, v3];
return Integer.$valueOf (-1);
default:
return null;
}
}, $fz.isPrivate = true, $fz), "J.minimize.MinObject,~A,~N");
c$.getR0 = $_M(c$, "getR0", 
($fz = function (b) {
return (b.ddata == null ? (J.minimize.forcefield.ForceFieldMMFF.ffParams.get (b.key)) : b.ddata)[1];
}, $fz.isPrivate = true, $fz), "J.minimize.MinBond");
$_M(c$, "getRowFor", 
($fz = function (i) {
var elemno = this.minAtoms[i].atom.getElementNumber ();
return (elemno < 3 ? 0 : elemno < 11 ? 1 : elemno < 19 ? 2 : elemno < 37 ? 3 : 4);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getOutOfPlaneParameter", 
function (data) {
var ddata = J.minimize.forcefield.ForceFieldMMFF.ffParams.get (this.getKey (data, 6, 13));
return (ddata == null ? 0 : ddata[0]);
}, "~A");
c$.sortOop = $_M(c$, "sortOop", 
($fz = function (typeData) {
J.minimize.forcefield.ForceFieldMMFF.fixTypeOrder (typeData, 0, 2);
J.minimize.forcefield.ForceFieldMMFF.fixTypeOrder (typeData, 0, 3);
J.minimize.forcefield.ForceFieldMMFF.fixTypeOrder (typeData, 2, 3);
}, $fz.isPrivate = true, $fz), "~A");
c$.fixTypeOrder = $_M(c$, "fixTypeOrder", 
($fz = function (a, i, j) {
if (a[i] > a[j]) {
J.minimize.forcefield.ForceFieldMMFF.swap (a, i, j);
return true;
}return false;
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
$_M(c$, "fixOrder", 
($fz = function (a, i, j) {
var test = this.typeOf (a[j]) - this.typeOf (a[i]);
if (test < 0) J.minimize.forcefield.ForceFieldMMFF.swap (a, i, j);
return (test < 0 ? -1 : test > 0 ? 1 : 0);
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
c$.swap = $_M(c$, "swap", 
($fz = function (a, i, j) {
var t = a[i];
a[i] = a[j];
a[j] = t;
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
c$.getEquivalentType = $_M(c$, "getEquivalentType", 
($fz = function (type, level) {
return (type == 0 ? 0 : type == 70 || type > 82 ? type : level == 2 ? 0 : J.minimize.forcefield.ForceFieldMMFF.equivalentTypes[((type - 1) << 1) + level]);
}, $fz.isPrivate = true, $fz), "~N,~N");
c$.getZParam = $_M(c$, "getZParam", 
($fz = function (elemno) {
switch (elemno) {
case 1:
return 1.395;
case 6:
return 2.494;
case 7:
return 2.711;
case 8:
return 3.045;
case 9:
return 2.847;
case 14:
return 2.350;
case 15:
return 2.350;
case 16:
return 2.980;
case 17:
return 2.909;
case 35:
return 3.017;
case 53:
return 3.086;
}
return 0.0;
}, $fz.isPrivate = true, $fz), "~N");
c$.getCParam = $_M(c$, "getCParam", 
($fz = function (elemno) {
switch (elemno) {
case 5:
return 0.704;
case 6:
return 1.016;
case 7:
return 1.113;
case 8:
return 1.337;
case 14:
return 0.811;
case 15:
return 1.068;
case 16:
return 1.249;
case 17:
return 1.078;
case 33:
return 0.825;
}
return 0.0;
}, $fz.isPrivate = true, $fz), "~N");
c$.getUParam = $_M(c$, "getUParam", 
($fz = function (elemno) {
switch (elemno) {
case 6:
case 7:
case 8:
return 2.0;
case 14:
case 15:
case 16:
return 1.25;
}
return 0.0;
}, $fz.isPrivate = true, $fz), "~N");
c$.getVParam = $_M(c$, "getVParam", 
($fz = function (elemno) {
switch (elemno) {
case 6:
return 2.12;
case 7:
return 1.5;
case 8:
return 0.2;
case 14:
return 1.22;
case 15:
return 2.4;
case 16:
return 0.49;
}
return 0.0;
}, $fz.isPrivate = true, $fz), "~N");
c$.getCovalentRadius = $_M(c$, "getCovalentRadius", 
($fz = function (elemno) {
switch (elemno) {
case 1:
return 0.33;
case 5:
return 0.81;
case 6:
return 0.77;
case 7:
return 0.73;
case 8:
return 0.72;
case 9:
return 0.74;
case 13:
return 1.22;
case 14:
return 1.15;
case 15:
return 1.09;
case 16:
return 1.03;
case 17:
return 1.01;
case 31:
return 1.19;
case 32:
return 1.20;
case 33:
return 1.20;
case 34:
return 1.16;
case 35:
return 1.15;
case 44:
return 1.46;
case 50:
return 1.40;
case 51:
return 1.41;
case 52:
return 1.35;
case 53:
return 1.33;
case 81:
return 1.51;
case 82:
return 1.53;
case 83:
return 1.55;
default:
return J.util.Elements.getBondingRadiusFloat (elemno, 0);
}
}, $fz.isPrivate = true, $fz), "~N");
c$.getRuleBondLength = $_M(c$, "getRuleBondLength", 
($fz = function (a, b, boAB, isAromatic) {
switch (boAB) {
case 1:
case 2:
case 3:
break;
case 5:
break;
default:
return 0;
}
var elemnoA = a.atom.getElementNumber ();
var elemnoB = b.atom.getElementNumber ();
var r0a = J.minimize.forcefield.ForceFieldMMFF.getCovalentRadius (elemnoA);
var r0b = J.minimize.forcefield.ForceFieldMMFF.getCovalentRadius (elemnoB);
var Xa = J.util.Elements.getAllredRochowElectroNeg (elemnoA);
var Xb = J.util.Elements.getAllredRochowElectroNeg (elemnoB);
var c = (elemnoA == 1 || elemnoB == 1 ? 0.05 : 0.085);
var n = 1.4;
var r = r0a + r0b;
if (isAromatic) boAB = (a.ffAtomType.pilp || b.ffAtomType.pilp ? 5 : 4);
 else switch (a.ffAtomType.mltb << 4 + b.ffAtomType.mltb) {
case 0x11:
boAB = 4;
break;
case 0x12:
case 0x21:
boAB = 5;
break;
}
var red = 0;
switch (boAB) {
case 1:
switch (a.ffAtomType.mltb) {
case 0:
break;
case 1:
case 2:
red += J.minimize.forcefield.ForceFieldMMFF.r0reductions[1];
break;
case 3:
red += J.minimize.forcefield.ForceFieldMMFF.r0reductions[0];
break;
}
switch (b.ffAtomType.mltb) {
case 0:
break;
case 1:
case 2:
red += J.minimize.forcefield.ForceFieldMMFF.r0reductions[1];
break;
case 3:
red += J.minimize.forcefield.ForceFieldMMFF.r0reductions[0];
break;
}
break;
default:
red += 2 * J.minimize.forcefield.ForceFieldMMFF.r0reductions[boAB];
break;
}
r -= c * Math.pow (Math.abs (Xa - Xb), n);
return r;
}, $fz.isPrivate = true, $fz), "J.minimize.MinAtom,J.minimize.MinAtom,~N,~B");
Clazz.defineStatics (c$,
"A4_VDW", 122,
"A4_BNDK", 123,
"A4_CHRG", 124,
"A4_SB", 125,
"A4_SBDEF", 126,
"KEY_SBDEF", 0,
"KEY_PBCI", 0,
"KEY_VDW", 0,
"KEY_BNDK", 0,
"KEY_OOP", 6,
"TYPE_PBCI", 0x1,
"TYPE_VDW", 0x11,
"TYPE_BNDK", 0x222,
"TYPE_CHRG", 0x22,
"TYPE_BOND", 0x3,
"TYPE_ANGLE", 0x5,
"TYPE_SB", 0x15,
"TYPE_SBDEF", 0x25,
"TYPE_TORSION", 0x9,
"TYPE_OOP", 0xD,
"atomTypes", null,
"ffParams", null,
"sbMap", [0, 1, 3, 5, 4, 6, 8, 9, 11],
"equivalentTypes", [1, 1, 2, 1, 3, 1, 4, 1, 5, 5, 6, 6, 7, 6, 8, 8, 9, 8, 10, 8, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 16, 15, 17, 15, 18, 15, 19, 19, 1, 1, 21, 5, 22, 1, 23, 5, 24, 5, 25, 25, 26, 25, 28, 5, 28, 5, 29, 5, 2, 1, 31, 31, 7, 6, 21, 5, 8, 8, 6, 6, 36, 5, 2, 1, 9, 8, 10, 8, 10, 8, 3, 1, 42, 8, 10, 8, 16, 15, 10, 8, 9, 8, 42, 8, 9, 8, 6, 6, 21, 5, 7, 6, 21, 5, 42, 8, 9, 8, 10, 8, 10, 8, 2, 1, 10, 8, 6, 6, 4, 1, 42, 8, 10, 8, 2, 1, 2, 1, 9, 8, 9, 8, 9, 8, 8, 8, 9, 8, 70, 70, 5, 5, 16, 15, 18, 15, 17, 15, 26, 25, 9, 8, 12, 12, 2, 1, 9, 8, 2, 1, 10, 8, 9, 8],
"r0reductions", [0.08, 0.03, 0.10, 0.17, 0.075, 0.04]);
});
