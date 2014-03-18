Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.FoldingXyzReader", ["java.lang.Character", "java.util.StringTokenizer", "J.adapter.smarter.Atom", "J.util.ArrayUtil", "$.Parser"], function () {
c$ = Clazz.declareType (J.adapter.readers.simple, "FoldingXyzReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
var tokens =  new java.util.StringTokenizer (this.line, " \t");
if (tokens.hasMoreTokens ()) {
this.atomSetCollection.newAtomSet ();
var modelAtomCount = J.util.Parser.parseIntRadix (tokens.nextToken (), 10);
if (tokens.hasMoreTokens ()) this.atomSetCollection.setAtomSetName ("Protein " + tokens.nextToken ());
this.readAtoms (modelAtomCount);
}return true;
});
$_M(c$, "readAtoms", 
function (modelAtomCount) {
var bonds = J.util.ArrayUtil.newInt2 (modelAtomCount + 1);
for (var i = 0; i <= modelAtomCount; ++i) {
bonds[i] = null;
}
for (var i = 0; i <= modelAtomCount; ++i) {
this.readLine ();
if (this.line != null && this.line.length == 0) {
this.readLine ();
}if (this.line != null && this.line.length > 0) {
var atom = this.atomSetCollection.addNewAtom ();
this.parseIntStr (this.line);
atom.atomName = this.parseToken ();
if (atom.atomName != null) {
var carCount = 1;
if (atom.atomName.length >= 2) {
var c1 = atom.atomName.charAt (0);
var c2 = atom.atomName.charAt (1);
if (Character.isUpperCase (c1) && Character.isLowerCase (c2) && J.adapter.smarter.Atom.isValidElementSymbol2 (c1, c2)) {
carCount = 2;
}if ((c1 == 'C') && (c2 == 'L')) {
carCount = 2;
}}atom.elementSymbol = atom.atomName.substring (0, carCount);
}this.setAtomCoordXYZ (atom, this.parseFloat (), this.parseFloat (), this.parseFloat ());
var bondCount = 0;
bonds[i] =  Clazz.newIntArray (5, 0);
var bondNum = -2147483648;
while ((bondNum = this.parseInt ()) > 0) {
if (bondCount == bonds[i].length) {
bonds[i] = J.util.ArrayUtil.arrayCopyI (bonds[i], bondCount + 1);
}bonds[i][bondCount++] = bondNum - 1;
}
if (bondCount < bonds[i].length) {
bonds[i] = J.util.ArrayUtil.arrayCopyI (bonds[i], bondCount);
}}}
if (true) {
var incorrectBonds = 0;
for (var origin = 0; origin < bonds.length; origin++) {
if ((bonds[origin] != null) && (bonds[origin].length > 0)) {
var correct = false;
var destination = bonds[origin][0];
if ((destination >= 0) && (destination < bonds.length) && (bonds[destination] != null)) {
for (var j = 0; j < bonds[destination].length; j++) {
if (bonds[destination][j] == origin) {
correct = true;
}}
}if (!correct) {
incorrectBonds++;
}}}
var start = (incorrectBonds * 5) > bonds.length ? 1 : 0;
for (var origin = start; origin < bonds.length; origin++) {
if (bonds[origin] != null) {
for (var i = 0; i < bonds[origin].length; i++) {
var correct = false;
var destination = bonds[origin][i];
if ((destination >= 0) && (destination < bonds.length) && (bonds[destination] != null)) {
for (var j = start; j < bonds[destination].length; j++) {
if (bonds[destination][j] == origin) {
correct = true;
}}
}if (correct && (destination > origin)) {
this.atomSetCollection.addNewBondWithOrder (origin, destination, 1);
}}
}}
}}, "~N");
Clazz.defineStatics (c$,
"useAutoBond", false);
});
