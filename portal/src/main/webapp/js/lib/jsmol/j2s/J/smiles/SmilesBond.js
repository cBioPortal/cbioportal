Clazz.declarePackage ("J.smiles");
Clazz.load (["J.util.JmolEdge"], "J.smiles.SmilesBond", ["J.smiles.InvalidSmilesException"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atom1 = null;
this.atom2 = null;
this.isNot = false;
this.matchingBond = null;
this.primitives = null;
this.nPrimitives = 0;
this.bondsOr = null;
this.nBondsOr = 0;
Clazz.instantialize (this, arguments);
}, J.smiles, "SmilesBond", J.util.JmolEdge);
c$.getBondOrderString = $_M(c$, "getBondOrderString", 
function (order) {
switch (order) {
case 1:
return "";
case 2:
return "=";
case 3:
return "#";
default:
return "";
}
}, "~N");
$_M(c$, "set", 
function (bond) {
this.order = bond.order;
this.isNot = bond.isNot;
this.primitives = bond.primitives;
this.nPrimitives = bond.nPrimitives;
this.bondsOr = bond.bondsOr;
this.nBondsOr = bond.nBondsOr;
}, "J.smiles.SmilesBond");
$_M(c$, "addBondOr", 
function () {
if (this.bondsOr == null) this.bondsOr =  new Array (2);
if (this.nBondsOr >= this.bondsOr.length) {
var tmp =  new Array (this.bondsOr.length * 2);
System.arraycopy (this.bondsOr, 0, tmp, 0, this.bondsOr.length);
this.bondsOr = tmp;
}var sBond =  new J.smiles.SmilesBond (null, null, -1, false);
this.bondsOr[this.nBondsOr] = sBond;
this.nBondsOr++;
return sBond;
});
$_M(c$, "addPrimitive", 
function () {
if (this.primitives == null) this.primitives =  new Array (2);
if (this.nPrimitives >= this.primitives.length) {
var tmp =  new Array (this.primitives.length * 2);
System.arraycopy (this.primitives, 0, tmp, 0, this.primitives.length);
this.primitives = tmp;
}var sBond =  new J.smiles.SmilesBond (null, null, -1, false);
this.primitives[this.nPrimitives] = sBond;
this.nPrimitives++;
return sBond;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return this.atom1 + " -" + (this.isNot ? "!" : "") + this.order + "- " + this.atom2;
});
Clazz.makeConstructor (c$, 
function (atom1, atom2, bondType, isNot) {
Clazz.superConstructor (this, J.smiles.SmilesBond, []);
this.set2 (bondType, isNot);
this.set2a (atom1, atom2);
}, "J.smiles.SmilesAtom,J.smiles.SmilesAtom,~N,~B");
$_M(c$, "set2", 
function (bondType, isNot) {
this.order = bondType;
this.isNot = isNot;
}, "~N,~B");
$_M(c$, "set2a", 
function (atom1, atom2) {
if (atom1 != null) {
this.atom1 = atom1;
atom1.addBond (this);
}if (atom2 != null) {
this.atom2 = atom2;
atom2.isFirst = false;
atom2.addBond (this);
}}, "J.smiles.SmilesAtom,J.smiles.SmilesAtom");
c$.isBondType = $_M(c$, "isBondType", 
function (ch, isSearch, isBioSequence) {
if ("-=#:/\\.+!,&;@~^'".indexOf (ch) < 0) return false;
if (!isSearch && "-=#:/\\.~^'".indexOf (ch) < 0) throw  new J.smiles.InvalidSmilesException ("SMARTS bond type " + ch + " not allowed in SMILES");
if (isBioSequence && ch == '~') return false;
return true;
}, "~S,~B,~B");
c$.getBondTypeFromCode = $_M(c$, "getBondTypeFromCode", 
function (code) {
switch (code) {
case '.':
return 0;
case '-':
return 1;
case '=':
return 2;
case '#':
return 3;
case ':':
return 17;
case '/':
return 257;
case '\\':
return 513;
case '^':
return 769;
case '\'':
return 1025;
case '@':
return 65;
case '~':
return 81;
case '+':
return 96;
}
return -1;
}, "~S");
$_M(c$, "getAtom1", 
function () {
return this.atom1;
});
$_M(c$, "getAtom2", 
function () {
return this.atom2;
});
$_M(c$, "setAtom2", 
function (atom) {
this.atom2 = atom;
if (this.atom2 != null) {
atom.addBond (this);
}}, "J.smiles.SmilesAtom");
$_M(c$, "getBondType", 
function () {
return this.order;
});
$_M(c$, "getOtherAtom", 
function (a) {
return (this.atom1 === a ? this.atom2 : this.atom1);
}, "J.smiles.SmilesAtom");
Clazz.overrideMethod (c$, "getAtomIndex1", 
function () {
return this.atom1.index;
});
Clazz.overrideMethod (c$, "getAtomIndex2", 
function () {
return this.atom2.index;
});
Clazz.overrideMethod (c$, "getCovalentOrder", 
function () {
return this.order;
});
Clazz.overrideMethod (c$, "getOtherAtomNode", 
function (atom) {
return (atom === this.atom1 ? this.atom2 : atom === this.atom2 ? this.atom1 : null);
}, "J.util.JmolNode");
Clazz.overrideMethod (c$, "isCovalent", 
function () {
return this.order != 112;
});
$_M(c$, "getValence", 
function () {
return (this.order & 7);
});
Clazz.overrideMethod (c$, "isHydrogen", 
function () {
return this.order == 112;
});
$_M(c$, "switchAtoms", 
function () {
var a = this.atom1;
this.atom1 = this.atom2;
this.atom2 = a;
switch (this.order) {
case 769:
this.order = 1025;
break;
case 1025:
this.order = 769;
break;
case 257:
this.order = 513;
break;
case 513:
this.order = 257;
break;
}
});
Clazz.defineStatics (c$,
"TYPE_UNKNOWN", -1,
"TYPE_NONE", 0,
"TYPE_SINGLE", 1,
"TYPE_DOUBLE", 2,
"TYPE_TRIPLE", 3,
"TYPE_AROMATIC", 0x11,
"TYPE_DIRECTIONAL_1", 0x101,
"TYPE_DIRECTIONAL_2", 0x201,
"TYPE_ATROPISOMER_1", 0x301,
"TYPE_ATROPISOMER_2", 0x401,
"TYPE_RING", 0x41,
"TYPE_ANY", 0x51,
"TYPE_BIO_SEQUENCE", 0x60,
"TYPE_BIO_PAIR", 0x70,
"TYPE_MULTIPLE", 999);
});
