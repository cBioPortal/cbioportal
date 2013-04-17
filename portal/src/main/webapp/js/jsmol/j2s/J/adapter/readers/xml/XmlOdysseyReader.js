Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.readers.xml.XmlReader"], "J.adapter.readers.xml.XmlOdysseyReader", ["java.lang.Float", "J.adapter.smarter.Atom", "J.util.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modelName = null;
this.formula = null;
this.phase = null;
this.myAttributes = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xml, "XmlOdysseyReader", J.adapter.readers.xml.XmlReader);
Clazz.prepareFields (c$, function () {
this.myAttributes = ["id", "label", "xyz", "element", "hybrid", "a", "b", "order", "box"];
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.xml.XmlOdysseyReader, []);
});
Clazz.overrideMethod (c$, "getDOMAttributes", 
function () {
return this.myAttributes;
});
Clazz.overrideMethod (c$, "processStartElement", 
function (localName) {
if ("structure".equals (localName)) {
this.atomSetCollection.newAtomSet ();
return;
}if ("atom".equals (localName)) {
this.atom =  new J.adapter.smarter.Atom ();
if (this.atts.containsKey ("label")) this.atom.atomName = this.atts.get ("label");
 else this.atom.atomName = this.atts.get ("id");
if (this.atts.containsKey ("xyz")) {
var xyz = this.atts.get ("xyz");
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (xyz);
this.atom.set (this.parseFloatStr (tokens[0]), this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]));
}if (this.atts.containsKey ("element")) {
this.atom.elementSymbol = this.atts.get ("element");
}return;
}if ("bond".equals (localName)) {
var atom1 = this.atts.get ("a");
var atom2 = this.atts.get ("b");
var order = 1;
if (this.atts.containsKey ("order")) order = this.parseBondToken (this.atts.get ("order"));
this.atomSetCollection.addNewBondFromNames (atom1, atom2, order);
return;
}if ("boundary".equals (localName)) {
var boxDim = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.atts.get ("box"));
var x = this.parseFloatStr (boxDim[0]);
var y = this.parseFloatStr (boxDim[1]);
var z = this.parseFloatStr (boxDim[2]);
this.parent.setUnitCellItem (0, x);
this.parent.setUnitCellItem (1, y);
this.parent.setUnitCellItem (2, z);
this.parent.setUnitCellItem (3, 90);
this.parent.setUnitCellItem (4, 90);
this.parent.setUnitCellItem (5, 90);
var pt = J.util.P3.new3 (-x / 2, -y / 2, -z / 2);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("periodicOriginXyz", pt);
var atoms = this.atomSetCollection.getAtoms ();
for (var i = this.atomSetCollection.getAtomCount (); --i >= 0; ) {
atoms[i].sub (pt);
this.parent.setAtomCoord (atoms[i]);
}
if (this.parent.latticeCells[0] == 0) this.parent.latticeCells[0] = this.parent.latticeCells[1] = this.parent.latticeCells[2] = 1;
this.parent.setSymmetryOperator ("x,y,z");
this.parent.setSpaceGroupName ("P1");
this.parent.applySymmetryAndSetTrajectory ();
return;
}if ("odyssey_simulation".equals (localName)) {
if (this.modelName != null && this.phase != null) this.modelName += " - " + this.phase;
if (this.modelName != null) this.atomSetCollection.setAtomSetName (this.modelName);
if (this.formula != null) this.atomSetCollection.setAtomSetAuxiliaryInfo ("formula", this.formula);
}if ("title".equals (localName) || "formula".equals (localName) || "phase".equals (localName)) this.keepChars = true;
}, "~S");
$_M(c$, "parseBondToken", 
($fz = function (str) {
if (str.length >= 1) {
switch (str.charAt (0)) {
case 's':
return 1;
case 'd':
return 2;
case 't':
return 3;
case 'a':
return 515;
}
return this.parseIntStr (str);
}return 1;
}, $fz.isPrivate = true, $fz), "~S");
Clazz.overrideMethod (c$, "processEndElement", 
function (localName) {
if ("atom".equals (localName)) {
if (this.atom.elementSymbol != null && !Float.isNaN (this.atom.z)) {
this.atomSetCollection.addAtomWithMappedName (this.atom);
}this.atom = null;
return;
}if ("title".equals (localName)) {
this.modelName = this.chars;
}if ("formula".equals (localName)) {
this.formula = this.chars;
}if ("phase".equals (localName)) {
this.phase = this.chars;
}this.keepChars = false;
this.chars = null;
}, "~S");
});
