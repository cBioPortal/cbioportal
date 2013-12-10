Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.GhemicalMMReader", ["java.lang.Exception"], function () {
c$ = Clazz.declareType (J.adapter.readers.simple, "GhemicalMMReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.startsWith ("!Header")) {
this.processHeader ();
return true;
}if (this.line.startsWith ("!Info")) {
this.processInfo ();
return true;
}if (this.line.startsWith ("!Atoms")) {
this.processAtoms ();
return true;
}if (this.line.startsWith ("!Bonds")) {
this.processBonds ();
return true;
}if (this.line.startsWith ("!Coord")) {
this.processCoord ();
return true;
}if (this.line.startsWith ("!Charges")) {
this.processCharges ();
return true;
}return true;
});
$_M(c$, "processHeader", 
function () {
});
$_M(c$, "processInfo", 
function () {
});
$_M(c$, "processAtoms", 
function () {
var atomCount = this.parseIntAt (this.line, 6);
for (var i = 0; i < atomCount; ++i) {
if (this.atomSetCollection.getAtomCount () != i) throw  new Exception ("GhemicalMMReader error #1");
this.readLine ();
var atomIndex = this.parseIntStr (this.line);
if (atomIndex != i) throw  new Exception ("bad atom index in !Atomsexpected: " + i + " saw:" + atomIndex);
var elementNumber = this.parseInt ();
var atom = this.atomSetCollection.addNewAtom ();
atom.elementNumber = elementNumber;
}
});
$_M(c$, "processBonds", 
function () {
var bondCount = this.parseIntAt (this.line, 6);
for (var i = 0; i < bondCount; ++i) {
this.readLine ();
var atomIndex1 = this.parseIntStr (this.line);
var atomIndex2 = this.parseInt ();
var orderCode = this.parseToken ();
var order = 0;
switch (orderCode.charAt (0)) {
case 'C':
order = 515;
break;
case 'T':
order = 3;
break;
case 'D':
order = 2;
break;
case 'S':
default:
order = 1;
}
this.atomSetCollection.addNewBondWithOrder (atomIndex1, atomIndex2, order);
}
});
$_M(c$, "processCoord", 
function () {
var atoms = this.atomSetCollection.getAtoms ();
var atomCount = this.atomSetCollection.getAtomCount ();
for (var i = 0; i < atomCount; ++i) {
this.readLine ();
var atomIndex = this.parseIntStr (this.line);
if (atomIndex != i) throw  new Exception ("bad atom index in !Coordexpected: " + i + " saw:" + atomIndex);
this.setAtomCoordXYZ (atoms[i], this.parseFloat () * 10, this.parseFloat () * 10, this.parseFloat () * 10);
}
});
$_M(c$, "processCharges", 
function () {
var atoms = this.atomSetCollection.getAtoms ();
var atomCount = this.atomSetCollection.getAtomCount ();
for (var i = 0; i < atomCount; ++i) {
this.readLine ();
var atomIndex = this.parseIntStr (this.line);
if (atomIndex != i) throw  new Exception ("bad atom index in !Chargesexpected: " + i + " saw:" + atomIndex);
atoms[i].partialCharge = this.parseFloat ();
}
});
});
