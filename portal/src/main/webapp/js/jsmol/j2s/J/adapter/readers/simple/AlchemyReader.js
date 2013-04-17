Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.AlchemyReader", ["java.lang.Character", "J.adapter.smarter.Atom"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomCount = 0;
this.bondCount = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.simple, "AlchemyReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.atomSetCollection.newAtomSet ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.atomCount = this.parseIntStr (tokens[0]);
this.bondCount = this.parseIntStr (tokens[2]);
this.readAtoms ();
this.readBonds ();
this.continuing = false;
});
$_M(c$, "readAtoms", 
($fz = function () {
for (var i = this.atomCount; --i >= 0; ) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var atom =  new J.adapter.smarter.Atom ();
atom.atomSerial = this.parseIntStr (tokens[0]);
var name = atom.atomName = tokens[1];
atom.elementSymbol = name.substring (0, 1);
var c1 = name.charAt (0);
var c2 = ' ';
var nChar = (name.length == 2 && (J.adapter.smarter.Atom.isValidElementSymbol2 (c1, c2 = Character.toLowerCase (name.charAt (1))) || name.equals ("Du")) ? 2 : 1);
atom.elementSymbol = (nChar == 1 ? "" + c1 : "" + c1 + c2);
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]), this.parseFloatStr (tokens[4]));
atom.partialCharge = (tokens.length >= 6 ? this.parseFloatStr (tokens[5]) : 0);
this.atomSetCollection.addAtomWithMappedSerialNumber (atom);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readBonds", 
($fz = function () {
for (var i = this.bondCount; --i >= 0; ) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var atomSerial1 = this.parseIntStr (tokens[1]);
var atomSerial2 = this.parseIntStr (tokens[2]);
var sOrder = (tokens.length < 4 ? "1" : tokens[3].toUpperCase ());
var order = 0;
switch (sOrder.charAt (0)) {
default:
case '1':
case 'S':
order = 1;
break;
case '2':
case 'D':
order = 2;
break;
case '3':
case 'T':
order = 3;
break;
case 'A':
order = 515;
break;
case 'H':
order = 2048;
break;
}
this.atomSetCollection.addNewBondWithMappedSerialNumbers (atomSerial1, atomSerial2, order);
}
}, $fz.isPrivate = true, $fz));
});
