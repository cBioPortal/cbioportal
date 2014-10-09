Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.readers.xml.XmlReader"], "J.adapter.readers.xml.XmlCmlReader", ["java.lang.Float", "$.IndexOutOfBoundsException", "java.util.Properties", "$.StringTokenizer", "JU.PT", "J.adapter.smarter.Atom", "$.Bond", "J.api.JmolAdapter", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.scalarDictRef = null;
this.scalarDictValue = null;
this.scalarTitle = null;
this.cellParameterType = null;
this.checkedSerial = false;
this.isSerial = false;
this.moleculeNesting = 0;
this.latticeVectorPtr = 0;
this.embeddedCrystal = false;
this.atomIdNames = null;
this.tokens = null;
this.ac = 0;
this.atomArray = null;
this.bondCount = 0;
this.bondArray = null;
this.tokenCount = 0;
this.moduleNestingLevel = 0;
this.haveMolecule = false;
this.localSpaceGroupName = null;
this.processing = true;
this.state = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xml, "XmlCmlReader", J.adapter.readers.xml.XmlReader);
Clazz.prepareFields (c$, function () {
this.tokens =  new Array (16);
this.atomArray =  new Array (100);
this.bondArray =  new Array (100);
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.xml.XmlCmlReader, []);
});
Clazz.overrideMethod (c$, "getDOMAttributes", 
function () {
return ["id", "title", "label", "name", "x3", "y3", "z3", "x2", "y2", "isotope", "elementType", "formalCharge", "atomId", "atomRefs2", "order", "atomRef1", "atomRef2", "dictRef", "spaceGroup"];
});
Clazz.overrideMethod (c$, "processStartElement", 
function (name) {
if (!this.processing) return;
this.processStart2 (name);
}, "~S");
Clazz.defineMethod (c$, "processStart2", 
function (name) {
switch (this.state) {
case 0:
if (name.equals ("molecule")) {
this.state = 6;
this.haveMolecule = true;
if (this.moleculeNesting == 0) {
this.createNewAtomSet ();
}this.moleculeNesting++;
} else if (name.equals ("crystal")) {
this.state = 2;
} else if (name.equals ("symmetry")) {
this.state = 17;
if (this.atts.containsKey ("spaceGroup")) {
this.localSpaceGroupName = this.atts.get ("spaceGroup");
} else {
this.localSpaceGroupName = "P1";
this.parent.clearUnitCell ();
}} else if (name.equals ("module")) {
this.moduleNestingLevel++;
} else if (name.equalsIgnoreCase ("latticeVector")) {
this.state = 18;
this.setKeepChars (true);
}break;
case 2:
this.checkedSerial = true;
this.isSerial = false;
if (name.equals ("scalar")) {
this.state = 3;
this.setKeepChars (true);
this.scalarTitle = this.atts.get ("title");
this.getDictRefValue ();
} else if (name.equals ("symmetry")) {
this.state = 4;
if (this.atts.containsKey ("spaceGroup")) {
this.localSpaceGroupName = this.atts.get ("spaceGroup");
for (var i = 0; i < this.localSpaceGroupName.length; i++) if (this.localSpaceGroupName.charAt (i) == '_') this.localSpaceGroupName = this.localSpaceGroupName.substring (0, i) + this.localSpaceGroupName.substring ((i--) + 1);

}} else if (name.equalsIgnoreCase ("cellParameter")) {
if (this.atts.containsKey ("parameterType")) {
this.cellParameterType = this.atts.get ("parameterType");
this.setKeepChars (true);
}}break;
case 18:
this.setKeepChars (true);
break;
case 17:
case 3:
case 4:
if (name.equals ("transform3")) {
this.state = 5;
this.setKeepChars (true);
}break;
case 5:
case 6:
if (name.equals ("crystal")) {
this.state = 2;
this.embeddedCrystal = true;
}if (name.equals ("molecule")) {
this.state = 6;
this.moleculeNesting++;
}if (name.equalsIgnoreCase ("bondArray")) {
this.state = 10;
this.bondCount = 0;
if (this.atts.containsKey ("order")) {
this.breakOutBondTokens (this.atts.get ("order"));
for (var i = this.tokenCount; --i >= 0; ) this.bondArray[i].order = this.parseBondToken (this.tokens[i]);

}if (this.atts.containsKey ("atomRef1")) {
this.breakOutBondTokens (this.atts.get ("atomRef1"));
for (var i = this.tokenCount; --i >= 0; ) this.bondArray[i].atomIndex1 = this.asc.getAtomIndex (this.tokens[i]);

}if (this.atts.containsKey ("atomRef2")) {
this.breakOutBondTokens (this.atts.get ("atomRef2"));
for (var i = this.tokenCount; --i >= 0; ) this.bondArray[i].atomIndex2 = this.asc.getAtomIndex (this.tokens[i]);

}}if (name.equalsIgnoreCase ("atomArray")) {
this.state = 7;
this.ac = 0;
var coords3D = false;
if (this.atts.containsKey ("atomID")) {
this.breakOutAtomTokens (this.atts.get ("atomID"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].atomName = this.tokens[i];

}if (this.atts.containsKey ("x3")) {
coords3D = true;
this.breakOutAtomTokens (this.atts.get ("x3"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].x = this.parseFloatStr (this.tokens[i]);

}if (this.atts.containsKey ("y3")) {
this.breakOutAtomTokens (this.atts.get ("y3"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].y = this.parseFloatStr (this.tokens[i]);

}if (this.atts.containsKey ("z3")) {
this.breakOutAtomTokens (this.atts.get ("z3"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].z = this.parseFloatStr (this.tokens[i]);

}if (this.atts.containsKey ("x2")) {
this.breakOutAtomTokens (this.atts.get ("x2"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].x = this.parseFloatStr (this.tokens[i]);

}if (this.atts.containsKey ("y2")) {
this.breakOutAtomTokens (this.atts.get ("y2"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].y = this.parseFloatStr (this.tokens[i]);

}if (this.atts.containsKey ("elementType")) {
this.breakOutAtomTokens (this.atts.get ("elementType"));
for (var i = this.tokenCount; --i >= 0; ) this.atomArray[i].elementSymbol = this.tokens[i];

}for (var i = this.ac; --i >= 0; ) {
var atom = this.atomArray[i];
if (!coords3D) atom.z = 0;
this.addAtom (atom);
}
}if (name.equals ("formula")) {
this.state = 12;
}break;
case 10:
if (name.equals ("bond")) {
this.state = 11;
var order = -1;
this.tokenCount = 0;
if (this.atts.containsKey ("atomRefs2")) this.breakOutTokens (this.atts.get ("atomRefs2"));
if (this.atts.containsKey ("order")) order = this.parseBondToken (this.atts.get ("order"));
if (this.tokenCount == 2 && order > 0) {
this.addNewBond (this.tokens[0], this.tokens[1], order);
}}break;
case 7:
if (name.equals ("atom")) {
this.state = 8;
this.atom =  new J.adapter.smarter.Atom ();
this.parent.setFractionalCoordinates (false);
var id = this.atts.get ("id");
if (this.atts.containsKey ("name")) this.atom.atomName = this.atts.get ("name");
 else if (this.atts.containsKey ("title")) this.atom.atomName = this.atts.get ("title");
 else if (this.atts.containsKey ("label")) this.atom.atomName = this.atts.get ("label");
 else this.atom.atomName = id;
if (!this.checkedSerial) {
this.isSerial = (id != null && id.length > 1 && id.startsWith ("a") && JU.PT.parseInt (id.substring (1)) != -2147483648);
this.checkedSerial = true;
}if (this.isSerial) this.atom.atomSerial = JU.PT.parseInt (id.substring (1));
if (this.atts.containsKey ("xFract") && (this.parent.iHaveUnitCell || !this.atts.containsKey ("x3"))) {
this.parent.setFractionalCoordinates (true);
this.atom.set (this.parseFloatStr (this.atts.get ("xFract")), this.parseFloatStr (this.atts.get ("yFract")), this.parseFloatStr (this.atts.get ("zFract")));
} else if (this.atts.containsKey ("x3")) {
this.atom.set (this.parseFloatStr (this.atts.get ("x3")), this.parseFloatStr (this.atts.get ("y3")), this.parseFloatStr (this.atts.get ("z3")));
} else if (this.atts.containsKey ("x2")) {
this.atom.set (this.parseFloatStr (this.atts.get ("x2")), this.parseFloatStr (this.atts.get ("y2")), 0);
}if (this.atts.containsKey ("elementType")) {
var sym = this.atts.get ("elementType");
if (this.atts.containsKey ("isotope")) this.atom.elementNumber = ((this.parseIntStr (this.atts.get ("isotope")) << 7) + J.api.JmolAdapter.getElementNumber (sym));
this.atom.elementSymbol = sym;
}if (this.atts.containsKey ("formalCharge")) this.atom.formalCharge = this.parseIntStr (this.atts.get ("formalCharge"));
}break;
case 11:
if (this.atts.containsKey ("builtin")) {
this.setKeepChars (true);
this.state = 14;
this.scalarDictValue = this.atts.get ("builtin");
}break;
case 8:
if (name.equals ("scalar")) {
this.state = 9;
this.setKeepChars (true);
this.scalarTitle = this.atts.get ("title");
this.getDictRefValue ();
} else if (this.atts.containsKey ("builtin")) {
this.setKeepChars (true);
this.state = 13;
this.scalarDictValue = this.atts.get ("builtin");
}break;
case 9:
break;
case 12:
break;
case 13:
break;
case 14:
break;
}
}, "~S");
Clazz.overrideMethod (c$, "processEndElement", 
function (name) {
if (!this.processing) return;
this.processEnd2 (name);
}, "~S");
Clazz.defineMethod (c$, "processEnd2", 
function (name) {
switch (this.state) {
case 0:
if (name.equals ("module")) {
if (--this.moduleNestingLevel == 0) {
if (this.parent.iHaveUnitCell) this.applySymmetryAndSetTrajectory ();
this.atomIdNames = this.asc.setAtomNames (this.atomIdNames);
}}break;
case 2:
if (name.equals ("crystal")) {
if (this.embeddedCrystal) {
this.state = 6;
this.embeddedCrystal = false;
} else {
this.state = 0;
}} else if (name.equalsIgnoreCase ("cellParameter") && this.keepChars) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.chars);
this.setKeepChars (false);
if (tokens.length != 3 || this.cellParameterType == null) {
} else if (this.cellParameterType.equals ("length")) {
for (var i = 0; i < 3; i++) this.parent.setUnitCellItem (i, this.parseFloatStr (tokens[i]));

break;
} else if (this.cellParameterType.equals ("angle")) {
for (var i = 0; i < 3; i++) this.parent.setUnitCellItem (i + 3, this.parseFloatStr (tokens[i]));

break;
}JU.Logger.error ("bad cellParameter information: parameterType=" + this.cellParameterType + " data=" + this.chars);
this.parent.setFractionalCoordinates (false);
}break;
case 3:
if (name.equals ("scalar")) {
this.state = 2;
if (this.scalarTitle != null) this.checkUnitCellItem (J.adapter.readers.xml.XmlCmlReader.notionalUnitcellTags, this.scalarTitle);
 else if (this.scalarDictRef != null) this.checkUnitCellItem (J.api.JmolAdapter.cellParamNames, (this.scalarDictValue.startsWith ("_") ? this.scalarDictValue : "_" + this.scalarDictValue));
}this.setKeepChars (false);
this.scalarTitle = null;
this.scalarDictRef = null;
break;
case 5:
if (name.equals ("transform3")) {
this.setKeepChars (false);
this.state = 4;
}break;
case 18:
var values = J.adapter.smarter.AtomSetCollectionReader.getTokensFloat (this.chars, null, 3);
this.parent.addPrimitiveLatticeVector (this.latticeVectorPtr, values, 0);
this.latticeVectorPtr = (this.latticeVectorPtr + 1) % 3;
this.setKeepChars (false);
this.state = 0;
break;
case 4:
case 17:
if (name.equals ("symmetry")) this.state = (this.state == 4 ? 2 : 0);
if (this.moduleNestingLevel == 0 && this.parent.iHaveUnitCell && !this.embeddedCrystal) this.applySymmetryAndSetTrajectory ();
break;
case 6:
if (name.equals ("molecule")) {
if (--this.moleculeNesting == 0) {
this.applySymmetryAndSetTrajectory ();
this.atomIdNames = this.asc.setAtomNames (this.atomIdNames);
this.state = 0;
} else {
this.state = 6;
}}break;
case 10:
if (name.equalsIgnoreCase ("bondArray")) {
this.state = 6;
for (var i = 0; i < this.bondCount; ++i) this.asc.addBond (this.bondArray[i]);

this.parent.applySymmetryToBonds = true;
}break;
case 7:
if (name.equalsIgnoreCase ("atomArray")) {
this.state = 6;
for (var i = 0; i < this.ac; ++i) this.addAtom (this.atomArray[i]);

}break;
case 11:
if (name.equals ("bond")) {
this.state = 10;
}break;
case 8:
if (name.equals ("atom")) {
this.state = 7;
this.addAtom (this.atom);
this.atom = null;
}break;
case 9:
if (name.equals ("scalar")) {
this.state = 8;
if ("jmol:charge".equals (this.scalarDictRef)) {
this.atom.partialCharge = this.parseFloatStr (this.chars);
} else if (this.scalarDictRef != null && "_atom_site_label".equals (this.scalarDictValue)) {
if (this.atomIdNames == null) this.atomIdNames =  new java.util.Properties ();
this.atomIdNames.put (this.atom.atomName, this.chars);
}}this.setKeepChars (false);
this.scalarTitle = null;
this.scalarDictRef = null;
break;
case 13:
this.state = 8;
if (this.scalarDictValue.equals ("x3")) this.atom.x = this.parseFloatStr (this.chars);
 else if (this.scalarDictValue.equals ("y3")) this.atom.y = this.parseFloatStr (this.chars);
 else if (this.scalarDictValue.equals ("z3")) this.atom.z = this.parseFloatStr (this.chars);
 else if (this.scalarDictValue.equals ("elementType")) this.atom.elementSymbol = this.chars;
this.setKeepChars (false);
break;
case 14:
this.state = 11;
if (this.scalarDictValue.equals ("atomRef")) {
if (this.tokenCount == 0) this.tokens =  new Array (2);
if (this.tokenCount < 2) this.tokens[this.tokenCount++] = this.chars;
} else if (this.scalarDictValue.equals ("order")) {
var order = this.parseBondToken (this.chars);
if (order > 0 && this.tokenCount == 2) this.addNewBond (this.tokens[0], this.tokens[1], order);
}this.setKeepChars (false);
break;
case 12:
this.state = 6;
break;
}
}, "~S");
Clazz.defineMethod (c$, "addNewBond", 
 function (a1, a2, order) {
this.parent.applySymmetryToBonds = true;
if (this.isSerial) this.asc.addNewBondFromNames (a1.substring (1), a2.substring (1), order);
 else this.asc.addNewBondFromNames (a1, a2, order);
}, "~S,~S,~N");
Clazz.defineMethod (c$, "getDictRefValue", 
 function () {
this.scalarDictRef = this.atts.get ("dictRef");
if (this.scalarDictRef != null) {
var iColon = this.scalarDictRef.indexOf (":");
this.scalarDictValue = this.scalarDictRef.substring (iColon + 1);
}});
Clazz.defineMethod (c$, "checkUnitCellItem", 
 function (tags, value) {
for (var i = tags.length; --i >= 0; ) if (value.equals (tags[i])) {
this.parent.setUnitCellItem (i, this.parseFloatStr (this.chars));
return;
}
}, "~A,~S");
Clazz.defineMethod (c$, "addAtom", 
 function (atom) {
if ((atom.elementSymbol == null && atom.elementNumber < 0) || Float.isNaN (atom.z)) return;
this.parent.setAtomCoord (atom);
if (this.isSerial) this.asc.addAtomWithMappedSerialNumber (atom);
 else this.asc.addAtomWithMappedName (atom);
}, "J.adapter.smarter.Atom");
Clazz.defineMethod (c$, "parseBondToken", 
 function (str) {
var floatOrder = this.parseFloatStr (str);
if (Float.isNaN (floatOrder) && str.length >= 1) {
str = str.toUpperCase ();
switch (str.charAt (0)) {
case 'S':
return 1;
case 'D':
return 2;
case 'T':
return 3;
case 'A':
return 515;
case 'P':
return 66;
}
return this.parseIntStr (str);
}if (floatOrder == 1.5) return 515;
if (floatOrder == 2) return 2;
if (floatOrder == 3) return 3;
return 1;
}, "~S");
Clazz.defineMethod (c$, "breakOutTokens", 
 function (str) {
var st =  new java.util.StringTokenizer (str);
this.tokenCount = st.countTokens ();
if (this.tokenCount > this.tokens.length) this.tokens =  new Array (this.tokenCount);
for (var i = 0; i < this.tokenCount; ++i) {
try {
this.tokens[i] = st.nextToken ();
} catch (nsee) {
if (Clazz.exceptionOf (nsee, java.util.NoSuchElementException)) {
this.tokens[i] = null;
} else {
throw nsee;
}
}
}
}, "~S");
Clazz.defineMethod (c$, "breakOutAtomTokens", 
function (str) {
this.breakOutTokens (str);
this.checkAtomArrayLength (this.tokenCount);
}, "~S");
Clazz.defineMethod (c$, "checkAtomArrayLength", 
function (newAtomCount) {
if (this.ac == 0) {
if (newAtomCount > this.atomArray.length) this.atomArray =  new Array (newAtomCount);
for (var i = newAtomCount; --i >= 0; ) this.atomArray[i] =  new J.adapter.smarter.Atom ();

this.ac = newAtomCount;
} else if (newAtomCount != this.ac) {
throw  new IndexOutOfBoundsException ("bad atom attribute length");
}}, "~N");
Clazz.defineMethod (c$, "breakOutBondTokens", 
function (str) {
this.breakOutTokens (str);
this.checkBondArrayLength (this.tokenCount);
}, "~S");
Clazz.defineMethod (c$, "checkBondArrayLength", 
function (newBondCount) {
if (this.bondCount == 0) {
if (newBondCount > this.bondArray.length) this.bondArray =  new Array (newBondCount);
for (var i = newBondCount; --i >= 0; ) this.bondArray[i] =  new J.adapter.smarter.Bond (-1, -1, 1);

this.bondCount = newBondCount;
} else if (newBondCount != this.bondCount) {
throw  new IndexOutOfBoundsException ("bad bond attribute length");
}}, "~N");
Clazz.defineMethod (c$, "createNewAtomSet", 
 function () {
this.asc.newAtomSet ();
var collectionName = null;
if (this.atts.containsKey ("title")) collectionName = this.atts.get ("title");
 else if (this.atts.containsKey ("id")) collectionName = this.atts.get ("id");
if (collectionName != null) {
this.asc.setAtomSetName (collectionName);
}});
Clazz.defineMethod (c$, "applySymmetryAndSetTrajectory", 
function () {
if (this.moduleNestingLevel > 0 || !this.haveMolecule || this.localSpaceGroupName == null) return;
this.parent.setSpaceGroupName (this.localSpaceGroupName);
this.parent.iHaveSymmetryOperators = this.iHaveSymmetryOperators;
this.parent.applySymmetryAndSetTrajectory ();
});
Clazz.defineStatics (c$,
"START", 0,
"CML", 1,
"CRYSTAL", 2,
"CRYSTAL_SCALAR", 3,
"CRYSTAL_SYMMETRY", 4,
"CRYSTAL_SYMMETRY_TRANSFORM3", 5,
"MOLECULE", 6,
"MOLECULE_ATOM_ARRAY", 7,
"MOLECULE_ATOM", 8,
"MOLECULE_ATOM_SCALAR", 9,
"MOLECULE_BOND_ARRAY", 10,
"MOLECULE_BOND", 11,
"MOLECULE_FORMULA", 12,
"MOLECULE_ATOM_BUILTIN", 13,
"MOLECULE_BOND_BUILTIN", 14,
"MODULE", 15,
"SYMMETRY", 17,
"LATTICE_VECTOR", 18,
"notionalUnitcellTags", ["a", "b", "c", "alpha", "beta", "gamma"]);
});
