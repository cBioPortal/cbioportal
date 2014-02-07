Clazz.declarePackage ("J.smiles");
Clazz.load (["J.util.JmolNode", "$.P3"], "J.smiles.SmilesAtom", ["J.util.ArrayUtil", "$.Elements", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomsOr = null;
this.nAtomsOr = 0;
this.primitives = null;
this.nPrimitives = 0;
this.index = 0;
this.atomName = null;
this.residueName = null;
this.residueChar = null;
this.isBioAtom = false;
this.bioType = '\0';
this.$isLeadAtom = false;
this.notBondedIndex = -1;
this.notCrossLinked = false;
this.aromaticAmbiguous = true;
this.atomType = null;
this.covalentHydrogenCount = -1;
this.not = false;
this.selected = false;
this.hasSymbol = false;
this.isFirst = true;
this.jmolIndex = -1;
this.elementNumber = -2;
this.missingHydrogenCount = -2147483648;
this.implicitHydrogenCount = -2147483648;
this.parent = null;
this.bonds = null;
this.bondCount = 0;
this.iNested = 0;
this.atomicMass = -2147483648;
this.charge = -2147483648;
this.matchingAtom = -1;
this.chiralClass = -2147483648;
this.chiralOrder = -2147483648;
this.$isAromatic = false;
this.component = 0;
this.atomSite = 0;
this.degree = -1;
this.nonhydrogenDegree = -1;
this.valence = 0;
this.connectivity = -1;
this.ringMembership = -2147483648;
this.ringSize = -2147483648;
this.ringConnectivity = -1;
Clazz.instantialize (this, arguments);
}, J.smiles, "SmilesAtom", J.util.P3, J.util.JmolNode);
Clazz.prepareFields (c$, function () {
this.bonds =  new Array (4);
});
c$.getChiralityClass = $_M(c$, "getChiralityClass", 
function (xx) {
return Clazz.doubleToInt (("0;11;AL;33;TH;TP;OH;77;SP;".indexOf (xx) + 1) / 3);
}, "~S");
c$.allowSmilesUnbracketed = $_M(c$, "allowSmilesUnbracketed", 
function (xx) {
return ("B, C, N, O, P, S, F, Cl, Br, I,".indexOf (xx + ",") >= 0);
}, "~S");
$_M(c$, "setBioAtom", 
function (bioType) {
this.isBioAtom = (bioType != '\0');
this.bioType = bioType;
if (this.parent != null) {
this.parent.bioType = bioType;
this.parent.isBioAtom = this.isBioAtom;
}}, "~S");
$_M(c$, "setAtomName", 
function (name) {
if (name == null) return;
if (name.length > 0) this.atomName = name;
if (name.equals ("0")) this.$isLeadAtom = true;
if (this.parent != null) {
this.parent.atomName = name;
}}, "~S");
$_M(c$, "setBonds", 
function (bonds) {
this.bonds = bonds;
}, "~A");
$_M(c$, "addAtomOr", 
function () {
if (this.atomsOr == null) this.atomsOr =  new Array (2);
if (this.nAtomsOr >= this.atomsOr.length) this.atomsOr = J.util.ArrayUtil.doubleLength (this.atomsOr);
var sAtom =  new J.smiles.SmilesAtom ().setIndex (this.index);
sAtom.parent = this;
this.atomsOr[this.nAtomsOr] = sAtom;
this.nAtomsOr++;
return sAtom;
});
$_M(c$, "addPrimitive", 
function () {
if (this.primitives == null) this.primitives =  new Array (2);
if (this.nPrimitives >= this.primitives.length) {
var tmp =  new Array (this.primitives.length * 2);
System.arraycopy (this.primitives, 0, tmp, 0, this.primitives.length);
this.primitives = tmp;
}var sAtom =  new J.smiles.SmilesAtom ().setIndex (this.index);
sAtom.parent = this;
this.primitives[this.nPrimitives] = sAtom;
this.setSymbol ("*");
this.hasSymbol = false;
this.nPrimitives++;
return sAtom;
});
Clazz.overrideMethod (c$, "toString", 
function () {
var s = (this.residueChar != null || this.residueName != null ? (this.residueChar == null ? this.residueName : this.residueChar) + "." + this.atomName : this.elementNumber == -1 ? "A" : this.elementNumber == -2 ? "*" : J.util.Elements.elementSymbolFromNumber (this.elementNumber));
if (this.$isAromatic) s = s.toLowerCase ();
return "[" + s + '.' + this.index + (this.matchingAtom >= 0 ? "(" + this.matchingAtom + ")" : "") + "]";
});
$_M(c$, "setIndex", 
function (index) {
this.index = index;
return this;
}, "~N");
$_M(c$, "setAll", 
function (iComponent, ptAtom, flags, atomicNumber, charge) {
this.component = iComponent;
this.index = ptAtom;
this.atomSite = flags;
this.elementNumber = atomicNumber;
this.charge = charge;
return this;
}, "~N,~N,~N,~N,~N");
$_M(c$, "setHydrogenCount", 
function (molecule) {
if (this.missingHydrogenCount != -2147483648) return true;
var count = J.smiles.SmilesAtom.getDefaultCount (this.elementNumber, this.$isAromatic);
if (count == -2) return false;
if (count == -1) return true;
if (this.elementNumber == 7 && this.$isAromatic && this.bondCount == 2) {
if (this.bonds[0].order == 1 && this.bonds[1].order == 1) count++;
}for (var i = 0; i < this.bondCount; i++) {
var bond = this.bonds[i];
switch (bond.order) {
case 81:
if (this.elementNumber == 7) {
J.util.Logger.info ("Ambiguous bonding to aromatic N found -- MF may be in error");
}count -= 1;
break;
case 1:
case 257:
case 513:
count -= 1;
break;
case 2:
count -= (this.$isAromatic && this.elementNumber == 6 ? 1 : 2);
break;
case 3:
count -= 3;
break;
}
}
if (count > 0) this.missingHydrogenCount = count;
return true;
}, "J.smiles.SmilesSearch");
c$.getDefaultCount = $_M(c$, "getDefaultCount", 
function (elementNumber, isAromatic) {
switch (elementNumber) {
case 0:
case -1:
return -1;
case 6:
return (isAromatic ? 3 : 4);
case 8:
case 16:
return 2;
case 7:
return (isAromatic ? 2 : 3);
case 5:
case 15:
return 3;
case 9:
case 17:
case 35:
case 53:
return 1;
}
return -2;
}, "~N,~B");
Clazz.overrideMethod (c$, "getIndex", 
function () {
return this.index;
});
$_M(c$, "isAromatic", 
function () {
return this.$isAromatic;
});
$_M(c$, "setSymbol", 
function (symbol) {
this.$isAromatic = symbol.equals (symbol.toLowerCase ());
this.hasSymbol = true;
if (symbol.equals ("*")) {
this.$isAromatic = false;
this.elementNumber = -2;
return true;
}if (symbol.equals ("Xx")) {
this.elementNumber = 0;
return true;
}this.aromaticAmbiguous = false;
if (symbol.equals ("a") || symbol.equals ("A")) {
if (this.elementNumber < 0) this.elementNumber = -1;
return true;
}if (this.$isAromatic) symbol = symbol.substring (0, 1).toUpperCase () + (symbol.length == 1 ? "" : symbol.substring (1));
this.elementNumber = J.util.Elements.elementNumberFromSymbol (symbol, true);
return (this.elementNumber != 0);
}, "~S");
Clazz.overrideMethod (c$, "getElementNumber", 
function () {
return this.elementNumber;
});
$_M(c$, "getAtomicMass", 
function () {
return this.atomicMass;
});
$_M(c$, "setAtomicMass", 
function (mass) {
this.atomicMass = mass;
}, "~N");
$_M(c$, "getCharge", 
function () {
return this.charge;
});
$_M(c$, "setCharge", 
function (charge) {
this.charge = charge;
}, "~N");
$_M(c$, "getMatchingAtom", 
function () {
return this.matchingAtom;
});
$_M(c$, "setMatchingAtom", 
function (atom) {
this.matchingAtom = atom;
}, "~N");
$_M(c$, "getChiralClass", 
function () {
return this.chiralClass;
});
$_M(c$, "setChiralClass", 
function (chiralClass) {
this.chiralClass = chiralClass;
}, "~N");
$_M(c$, "getChiralOrder", 
function () {
return this.chiralOrder;
});
$_M(c$, "setChiralOrder", 
function (chiralOrder) {
this.chiralOrder = chiralOrder;
}, "~N");
$_M(c$, "setExplicitHydrogenCount", 
function (count) {
this.missingHydrogenCount = count;
}, "~N");
$_M(c$, "setImplicitHydrogenCount", 
function (count) {
this.implicitHydrogenCount = count;
}, "~N");
$_M(c$, "setDegree", 
function (degree) {
this.degree = degree;
}, "~N");
$_M(c$, "setNonhydrogenDegree", 
function (degree) {
this.nonhydrogenDegree = degree;
}, "~N");
$_M(c$, "setValence", 
function (valence) {
this.valence = valence;
}, "~N");
$_M(c$, "setConnectivity", 
function (connectivity) {
this.connectivity = connectivity;
}, "~N");
$_M(c$, "setRingMembership", 
function (rm) {
this.ringMembership = rm;
}, "~N");
$_M(c$, "setRingSize", 
function (rs) {
this.ringSize = rs;
}, "~N");
$_M(c$, "setRingConnectivity", 
function (rc) {
this.ringConnectivity = rc;
}, "~N");
Clazz.overrideMethod (c$, "getModelIndex", 
function () {
return this.component;
});
Clazz.overrideMethod (c$, "getAtomSite", 
function () {
return this.atomSite;
});
Clazz.overrideMethod (c$, "getImplicitHydrogenCount", 
function () {
return 0;
});
$_M(c$, "getExplicitHydrogenCount", 
function () {
return this.missingHydrogenCount;
});
Clazz.overrideMethod (c$, "getFormalCharge", 
function () {
return this.charge;
});
Clazz.overrideMethod (c$, "getIsotopeNumber", 
function () {
return this.atomicMass;
});
Clazz.overrideMethod (c$, "getAtomicAndIsotopeNumber", 
function () {
return J.util.Elements.getAtomicAndIsotopeNumber (this.elementNumber, this.atomicMass);
});
Clazz.overrideMethod (c$, "getAtomName", 
function () {
return this.atomName == null ? "" : this.atomName;
});
Clazz.overrideMethod (c$, "getGroup3", 
function (allowNull) {
return this.residueName == null ? "" : this.residueName;
}, "~B");
Clazz.overrideMethod (c$, "getGroup1", 
function (c0) {
return this.residueChar == null ? "" : this.residueChar;
}, "~S");
$_M(c$, "addBond", 
function (bond) {
if (this.bondCount >= this.bonds.length) this.bonds = J.util.ArrayUtil.doubleLength (this.bonds);
this.bonds[this.bondCount] = bond;
this.bondCount++;
}, "J.smiles.SmilesBond");
$_M(c$, "setBondArray", 
function () {
if (this.bonds.length > this.bondCount) this.bonds = J.util.ArrayUtil.arrayCopyObject (this.bonds, this.bondCount);
if (this.atomsOr != null && this.atomsOr.length > this.nAtomsOr) this.atomsOr = J.util.ArrayUtil.arrayCopyObject (this.atomsOr, this.atomsOr.length);
if (this.primitives != null && this.primitives.length > this.nPrimitives) this.primitives = J.util.ArrayUtil.arrayCopyObject (this.primitives, this.primitives.length);
for (var i = 0; i < this.bonds.length; i++) {
if (this.isBioAtom && this.bonds[i].order == 17) this.bonds[i].order = 112;
if (this.bonds[i].getAtom1 ().index > this.bonds[i].getAtom2 ().index) {
this.bonds[i].switchAtoms ();
}}
});
Clazz.overrideMethod (c$, "getEdges", 
function () {
return (this.parent != null ? this.parent.getEdges () : this.bonds);
});
$_M(c$, "getBond", 
function (number) {
return (this.parent != null ? this.parent.getBond (number) : number >= 0 && number < this.bondCount ? this.bonds[number] : null);
}, "~N");
Clazz.overrideMethod (c$, "getCovalentBondCount", 
function () {
return this.getBondCount ();
});
$_M(c$, "getBondCount", 
function () {
return (this.parent != null ? this.parent.getCovalentBondCount () : this.bondCount);
});
$_M(c$, "getMatchingBondedAtom", 
function (i) {
if (this.parent != null) return this.parent.getMatchingBondedAtom (i);
if (i >= this.bondCount) return -1;
var b = this.bonds[i];
return (b.getAtom1 () === this ? b.getAtom2 () : b.getAtom1 ()).matchingAtom;
}, "~N");
Clazz.overrideMethod (c$, "getBondedAtomIndex", 
function (j) {
return (this.parent != null ? this.parent.getBondedAtomIndex (j) : this.bonds[j].getOtherAtom (this).index);
}, "~N");
Clazz.overrideMethod (c$, "getCovalentHydrogenCount", 
function () {
if (this.covalentHydrogenCount >= 0) return this.covalentHydrogenCount;
if (this.parent != null) return this.parent.getCovalentHydrogenCount ();
this.covalentHydrogenCount = 0;
for (var k = 0; k < this.bonds.length; k++) if (this.bonds[k].getOtherAtom (this).elementNumber == 1) this.covalentHydrogenCount++;

return this.covalentHydrogenCount;
});
Clazz.overrideMethod (c$, "getValence", 
function () {
if (this.parent != null) return this.parent.getValence ();
var n = this.valence;
if (n <= 0 && this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) n += this.bonds[i].getValence ();

this.valence = n;
return n;
});
$_M(c$, "getBondTo", 
function (atom) {
if (this.parent != null) return this.parent.getBondTo (atom);
var bond;
for (var k = 0; k < this.bonds.length; k++) {
if ((bond = this.bonds[k]) == null) continue;
if (atom == null ? bond.getAtom2 () === this : bond.getOtherAtom (this) === atom) return bond;
}
return null;
}, "J.smiles.SmilesAtom");
$_M(c$, "getBondNotTo", 
function (atom, allowH) {
var bond;
for (var k = 0; k < this.bonds.length; k++) {
if ((bond = this.bonds[k]) == null) continue;
var atom2 = bond.getOtherAtom (this);
if (atom !== atom2 && (allowH || atom2.elementNumber != 1)) return bond;
}
return null;
}, "J.smiles.SmilesAtom,~B");
Clazz.overrideMethod (c$, "isLeadAtom", 
function () {
return this.$isLeadAtom;
});
Clazz.overrideMethod (c$, "getOffsetResidueAtom", 
function (name, offset) {
if (this.isBioAtom) for (var k = 0; k < this.bonds.length; k++) if (this.bonds[k].getAtomIndex1 () == this.index && this.bonds[k].order == 96) return this.bonds[k].getOtherAtom (this).index;

return -1;
}, "~S,~N");
Clazz.overrideMethod (c$, "getGroupBits", 
function (bs) {
bs.set (this.index);
return;
}, "J.util.BS");
Clazz.overrideMethod (c$, "isCrossLinked", 
function (node) {
var bond = this.getBondTo (node);
return bond.isHydrogen ();
}, "J.util.JmolNode");
Clazz.overrideMethod (c$, "getCrossLinkLeadAtomIndexes", 
function (vLinks) {
for (var k = 0; k < this.bonds.length; k++) if (this.bonds[k].order == 112) vLinks.addLast (Integer.$valueOf (this.bonds[k].getOtherAtom (this).index));

return true;
}, "J.util.JmolList");
Clazz.overrideMethod (c$, "getBioStructureTypeName", 
function () {
return null;
});
Clazz.overrideMethod (c$, "getResno", 
function () {
return 0;
});
Clazz.overrideMethod (c$, "getChainID", 
function () {
return 0;
});
Clazz.overrideMethod (c$, "getChainIDStr", 
function () {
return "";
});
c$.getAtomLabel = $_M(c$, "getAtomLabel", 
function (atomicNumber, isotopeNumber, valence, charge, nH, isAromatic, stereo) {
var sym = J.util.Elements.elementSymbolFromNumber (atomicNumber);
if (isAromatic) {
sym = sym.toLowerCase ();
if (atomicNumber != 6) valence = 2147483647;
}var count = (stereo.length > 0 || isotopeNumber != 0 || charge != 0 ? -1 : J.smiles.SmilesAtom.getDefaultCount (atomicNumber, false));
return (count == valence ? sym : "[" + (isotopeNumber <= 0 ? "" : "" + isotopeNumber) + sym + (charge < 0 ? "" + charge : charge > 0 ? "+" + charge : "") + stereo + (nH > 1 ? "H" + nH : nH == 1 ? "H" : "") + "]");
}, "~N,~N,~N,~N,~N,~B,~S");
Clazz.overrideMethod (c$, "isDna", 
function () {
return this.bioType == 'd';
});
Clazz.overrideMethod (c$, "isRna", 
function () {
return this.bioType == 'r';
});
Clazz.overrideMethod (c$, "isNucleic", 
function () {
return this.bioType == 'n' || this.bioType == 'r' || this.bioType == 'd';
});
Clazz.overrideMethod (c$, "isProtein", 
function () {
return this.bioType == 'p';
});
Clazz.overrideMethod (c$, "isPurine", 
function () {
return this.residueChar != null && this.isNucleic () && "AG".indexOf (this.residueChar) >= 0;
});
Clazz.overrideMethod (c$, "isPyrimidine", 
function () {
return this.residueChar != null && this.isNucleic () && "CTUI".indexOf (this.residueChar) >= 0;
});
Clazz.overrideMethod (c$, "isDeleted", 
function () {
return false;
});
$_M(c$, "setAtomType", 
function (type) {
this.atomType = type;
}, "~S");
Clazz.overrideMethod (c$, "getAtomType", 
function () {
return (this.atomType == null ? this.atomName : this.atomType);
});
Clazz.overrideMethod (c$, "findAtomsLike", 
function (substring) {
return null;
}, "~S");
Clazz.defineStatics (c$,
"STEREOCHEMISTRY_DEFAULT", 0,
"STEREOCHEMISTRY_ALLENE", 2,
"STEREOCHEMISTRY_TETRAHEDRAL", 4,
"STEREOCHEMISTRY_TRIGONAL_BIPYRAMIDAL", 5,
"STEREOCHEMISTRY_OCTAHEDRAL", 6,
"STEREOCHEMISTRY_SQUARE_PLANAR", 8,
"UNBRACKETED_SET", "B, C, N, O, P, S, F, Cl, Br, I,");
});
