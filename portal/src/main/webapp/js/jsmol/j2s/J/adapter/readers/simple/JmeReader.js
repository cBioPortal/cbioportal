Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.JmeReader", ["J.adapter.smarter.Bond", "J.util.TextFormat"], function () {
c$ = Clazz.declareType (J.adapter.readers.simple, "JmeReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.atomSetCollection.setCollectionName ("JME");
this.atomSetCollection.newAtomSet ();
this.line = this.readLine ().$replace ('\t', ' ');
this.checkCurrentLineForScript ();
this.addJmolScript ("jmeString='" + this.line + "'");
var atomCount = this.parseInt ();
var bondCount = this.parseInt ();
this.readAtoms (atomCount);
this.readBonds (bondCount);
this.set2D ();
this.continuing = false;
});
$_M(c$, "readAtoms", 
($fz = function (atomCount) {
for (var i = 0; i < atomCount; ++i) {
var strAtom = this.parseToken ();
var atom = this.atomSetCollection.addNewAtom ();
this.setAtomCoordXYZ (atom, this.parseFloat (), this.parseFloat (), 0);
var indexColon = strAtom.indexOf (':');
var elementSymbol = (indexColon > 0 ? strAtom.substring (0, indexColon) : strAtom);
if (elementSymbol.indexOf ("+") >= 0) {
elementSymbol = J.util.TextFormat.trim (elementSymbol, "+");
atom.formalCharge = 1;
} else if (elementSymbol.indexOf ("-") >= 0) {
elementSymbol = J.util.TextFormat.trim (elementSymbol, "-");
atom.formalCharge = -1;
}atom.elementSymbol = elementSymbol;
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "readBonds", 
($fz = function (bondCount) {
for (var i = 0; i < bondCount; ++i) {
var atomIndex1 = this.parseInt () - 1;
var atomIndex2 = this.parseInt () - 1;
var order = this.parseInt ();
switch (order) {
default:
continue;
case 1:
case 2:
case 3:
break;
case -1:
order = 1025;
break;
case -2:
order = 1041;
break;
}
this.atomSetCollection.addBond ( new J.adapter.smarter.Bond (atomIndex1, atomIndex2, order));
}
}, $fz.isPrivate = true, $fz), "~N");
});
