Clazz.declarePackage ("J.adapter.readers.more");
Clazz.load (["J.adapter.readers.cifpdb.PdbReader", "J.util.JmolList"], "J.adapter.readers.more.P2nReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.altNames = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.more, "P2nReader", J.adapter.readers.cifpdb.PdbReader);
Clazz.prepareFields (c$, function () {
this.altNames =  new J.util.JmolList ();
});
Clazz.overrideMethod (c$, "setAdditionalAtomParameters", 
function (atom) {
var altName = this.line.substring (69, 72).trim ();
if (altName.length == 0) altName = atom.atomName;
if (this.useAltNames) atom.atomName = altName;
 else this.altNames.addLast (altName);
}, "J.adapter.smarter.Atom");
$_M(c$, "finalizeReader", 
function () {
Clazz.superCall (this, J.adapter.readers.more.P2nReader, "finalizeReader", []);
if (!this.useAltNames) this.atomSetCollection.setAtomSetAuxiliaryInfo ("altName", this.altNames.toArray ( new Array (this.altNames.size ())));
});
});
