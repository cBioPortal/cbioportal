Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.BasisFunctionReader"], "J.adapter.readers.quantum.SpartanInputReader", ["J.adapter.smarter.Bond", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modelName = null;
this.modelAtomCount = 0;
this.atomCount = 0;
this.bondData = "";
this.constraints = "";
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "SpartanInputReader", J.adapter.readers.quantum.BasisFunctionReader);
$_M(c$, "readInputRecords", 
function () {
var atomCount0 = this.atomCount;
this.readInputHeader ();
while (this.readLine () != null) {
var tokens = this.getTokens ();
if (tokens.length == 2 && this.parseIntStr (tokens[0]) != -2147483648 && this.parseIntStr (tokens[1]) >= 0) break;
}
if (this.line == null) return;
this.readInputAtoms ();
this.discardLinesUntilContains ("ATOMLABELS");
if (this.line != null) this.readAtomNames ();
if (this.modelAtomCount > 1) {
this.discardLinesUntilContains ("HESSIAN");
if (this.line != null) this.readBonds (atomCount0);
if (this.line != null && this.line.indexOf ("BEGINCONSTRAINTS") >= 0) this.readConstraints ();
}while (this.line != null && this.line.indexOf ("END ") < 0 && this.line.indexOf ("MOLSTATE") < 0) this.readLine ();

if (this.line != null && this.line.indexOf ("MOLSTATE") >= 0) this.readTransform ();
if (this.atomSetCollection.getAtomCount () > 0) this.atomSetCollection.setAtomSetName (this.modelName);
});
$_M(c$, "readConstraints", 
($fz = function () {
this.constraints = "";
while (this.readLine () != null && this.line.indexOf ("END") < 0) this.constraints += (this.constraints === "" ? "" : "\n") + this.line;

this.readLine ();
if (this.constraints.length == 0) return;
this.atomSetCollection.setAtomSetAuxiliaryInfo ("constraints", this.constraints);
this.atomSetCollection.setAtomSetModelProperty (".PATH", "EnergyProfile");
this.atomSetCollection.setAtomSetModelProperty ("Constraint", this.constraints);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readTransform", 
($fz = function () {
this.readLine ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine () + " " + this.readLine ());
this.setTransform (this.parseFloatStr (tokens[0]), this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[4]), this.parseFloatStr (tokens[5]), this.parseFloatStr (tokens[6]), this.parseFloatStr (tokens[8]), this.parseFloatStr (tokens[9]), this.parseFloatStr (tokens[10]));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readInputHeader", 
($fz = function () {
while (this.readLine () != null && !this.line.startsWith (" ")) {
}
this.readLine ();
this.modelName = this.line + ";";
this.modelName = this.modelName.substring (0, this.modelName.indexOf (";")).trim ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "readInputAtoms", 
($fz = function () {
this.modelAtomCount = 0;
while (this.readLine () != null && !this.line.startsWith ("ENDCART")) {
var tokens = this.getTokens ();
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = J.adapter.smarter.AtomSetCollectionReader.getElementSymbol (this.parseIntStr (tokens[0]));
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]));
this.modelAtomCount++;
}
this.atomCount = this.atomSetCollection.getAtomCount ();
if (J.util.Logger.debugging) J.util.Logger.debug (this.atomCount + " atoms read");
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtomNames", 
($fz = function () {
var atom0 = this.atomCount - this.modelAtomCount;
for (var i = 0; i < this.modelAtomCount; i++) {
this.line = this.readLine ().trim ();
var name = this.line.substring (1, this.line.length - 1);
this.atomSetCollection.getAtom (atom0 + i).atomName = name;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readBonds", 
($fz = function (atomCount0) {
var nAtoms = this.modelAtomCount;
this.bondData = "";
while (this.readLine () != null && !this.line.startsWith ("ENDHESS")) {
var tokens = this.getTokens ();
this.bondData += this.line + " ";
if (nAtoms == 0) {
var sourceIndex = this.parseIntStr (tokens[0]) - 1 + atomCount0;
var targetIndex = this.parseIntStr (tokens[1]) - 1 + atomCount0;
var bondOrder = this.parseIntStr (tokens[2]);
if (bondOrder > 0) {
this.atomSetCollection.addBond ( new J.adapter.smarter.Bond (sourceIndex, targetIndex, bondOrder < 4 ? bondOrder : bondOrder == 5 ? 515 : 1));
}} else {
nAtoms -= tokens.length;
}}
this.readLine ();
if (J.util.Logger.debugging) J.util.Logger.debug (this.atomSetCollection.getBondCount () + " bonds read");
}, $fz.isPrivate = true, $fz), "~N");
});
