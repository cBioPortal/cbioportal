Clazz.declarePackage ("J.adapter.readers.cifpdb");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader", "J.api.JmolLineReader", "J.io.CifDataReader", "J.util.JmolList", "$.P3"], "J.adapter.readers.cifpdb.CifReader", ["java.io.BufferedReader", "$.StringReader", "java.lang.Boolean", "$.Character", "$.Float", "java.util.Hashtable", "J.adapter.smarter.Atom", "$.Structure", "J.api.JmolAdapter", "J.constant.EnumStructure", "J.util.BS", "$.Logger", "$.Matrix4f", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.tokenizer = null;
this.thisDataSetName = "";
this.chemicalName = "";
this.thisStructuralFormula = "";
this.thisFormula = "";
this.iHaveDesiredModel = false;
this.isPDB = false;
this.htHetero = null;
this.isMolecular = false;
this.molecularType = "GEOM_BOND default";
this.lastAltLoc = '\0';
this.configurationPtr = -2147483648;
this.conformationIndex = 0;
this.filterAssembly = false;
this.vBiomts = null;
this.vBiomolecules = null;
this.htBiomts = null;
this.assemblyIdAtoms = null;
this.appendedData = null;
this.skipping = false;
this.nAtoms = 0;
this.nMolecular = 0;
this.key = null;
this.data = null;
this.atomTypes = null;
this.bondTypes = null;
this.disorderAssembly = ".";
this.lastDisorderAssembly = null;
this.assem = null;
this.hetatmData = null;
this.htSites = null;
this.field = null;
this.firstChar = '\0';
this.propertyOf = null;
this.fieldOf = null;
this.propertyCount = 0;
this.atomRadius = null;
this.bsConnected = null;
this.bsSets = null;
this.ptOffset = null;
this.bsMolecule = null;
this.bsExclude = null;
this.firstAtom = 0;
this.atomCount = 0;
this.atoms = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.cifpdb, "CifReader", J.adapter.smarter.AtomSetCollectionReader, J.api.JmolLineReader);
Clazz.prepareFields (c$, function () {
this.tokenizer =  new J.io.CifDataReader (this);
this.bondTypes =  new J.util.JmolList ();
this.propertyOf =  Clazz.newIntArray (100, 0);
this.fieldOf =  Clazz.newByteArray (J.adapter.readers.cifpdb.CifReader.atomFields.length, 0);
this.ptOffset =  new J.util.P3 ();
});
Clazz.overrideMethod (c$, "readNextLine", 
function () {
if (this.readLine () != null && this.line.indexOf ("#jmolscript:") >= 0) this.checkCurrentLineForScript ();
return this.line;
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
if (this.checkFilterKey ("CONF ")) this.configurationPtr = this.parseIntAt (this.filter, this.filter.indexOf ("CONF ") + 5);
this.appendedData = this.htParams.get ("appendedData");
this.isMolecular = (this.filter != null && this.filter.indexOf ("MOLECUL") >= 0);
this.filterAssembly = (this.filter != null && this.filter.indexOf ("$") >= 0);
if (this.isMolecular) {
if (!this.doApplySymmetry) {
this.doApplySymmetry = true;
this.latticeCells[0] = 1;
this.latticeCells[1] = 1;
this.latticeCells[2] = 1;
}this.molecularType = "filter \"MOLECULAR\"";
}this.nAtoms = 0;
this.line = "";
this.skipping = false;
while ((this.key = this.tokenizer.peekToken ()) != null) if (!this.readAllData ()) break;

if (this.appendedData != null) {
this.tokenizer =  new J.io.CifDataReader ( new java.io.BufferedReader ( new java.io.StringReader (this.appendedData)));
while ((this.key = this.tokenizer.peekToken ()) != null) if (!this.readAllData ()) break;

}if (this.atomSetCollection.getAtomCount () == this.nAtoms) this.atomSetCollection.removeCurrentAtomSet ();
 else this.applySymmetryAndSetTrajectory ();
if (this.htSites != null) this.addSites (this.htSites);
this.atomSetCollection.setCollectionName ("<collection of " + this.atomSetCollection.getAtomSetCount () + " models>");
this.continuing = false;
});
$_M(c$, "readAllData", 
($fz = function () {
if (this.key.startsWith ("data_")) {
if (this.iHaveDesiredModel) return false;
this.skipping = !this.doGetModel (++this.modelNumber, null);
if (this.skipping) {
this.tokenizer.getTokenPeeked ();
} else {
this.chemicalName = "";
this.thisStructuralFormula = "";
this.thisFormula = "";
if (this.nAtoms == this.atomSetCollection.getAtomCount ()) this.atomSetCollection.removeCurrentAtomSet ();
 else this.applySymmetryAndSetTrajectory ();
this.processDataParameter ();
this.iHaveDesiredModel = (this.isLastModel (this.modelNumber));
this.nAtoms = this.atomSetCollection.getAtomCount ();
}return true;
}if (this.key.startsWith ("loop_")) {
if (this.skipping) {
this.tokenizer.getTokenPeeked ();
this.skipLoop ();
} else {
this.processLoopBlock ();
}return true;
}if (this.key.indexOf ("_") != 0) {
J.util.Logger.warn ("CIF ERROR ? should be an underscore: " + this.key);
this.tokenizer.getTokenPeeked ();
} else if (!this.getData ()) {
return true;
}if (!this.skipping) {
this.key = this.key.$replace ('.', '_');
if (this.key.startsWith ("_chemical_name") || this.key.equals ("_chem_comp_name")) {
this.processChemicalInfo ("name");
} else if (this.key.startsWith ("_chemical_formula_structural")) {
this.processChemicalInfo ("structuralFormula");
} else if (this.key.startsWith ("_chemical_formula_sum") || this.key.equals ("_chem_comp_formula")) {
this.processChemicalInfo ("formula");
} else if (this.key.startsWith ("_cell_")) {
this.processCellParameter ();
} else if (this.key.startsWith ("_symmetry_space_group_name_H-M") || this.key.startsWith ("_symmetry_space_group_name_Hall")) {
this.processSymmetrySpaceGroupName ();
} else if (this.key.startsWith ("_atom_sites_fract_tran")) {
this.processUnitCellTransformMatrix ();
} else if (this.key.startsWith ("_pdbx_entity_nonpoly")) {
this.processNonpolyData ();
} else if (this.key.startsWith ("_pdbx_struct_assembly_gen")) {
this.processAssemblyGen ();
}}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "finalizeReader", 
function () {
if (this.atomSetCollection.getStructureCount () > 0) this.atomSetCollection.bsStructuredModels.setBits (0, this.atomSetCollection.getAtomSetCount ());
if (this.vBiomolecules != null && this.vBiomolecules.size () == 1 && this.atomSetCollection.getAtomCount () > 0) {
this.atomSetCollection.setAtomSetAuxiliaryInfo ("biomolecules", this.vBiomolecules);
this.setBiomolecules ();
if (this.vBiomts != null && this.vBiomts.size () > 1) {
this.atomSetCollection.applySymmetryBio (this.vBiomts, this.notionalUnitCell, this.applySymmetryToBonds, this.filter);
}}Clazz.superCall (this, J.adapter.readers.cifpdb.CifReader, "finalizeReader", []);
var header = this.tokenizer.getFileHeader ();
if (header.length > 0) this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("fileHeader", header);
});
$_M(c$, "setBiomolecules", 
($fz = function () {
var mident =  new J.util.Matrix4f ();
mident.setIdentity ();
if (this.assemblyIdAtoms == null) return;
for (var i = this.vBiomolecules.size (); --i >= 0; ) {
var biomolecule = this.vBiomolecules.get (i);
var ops = J.util.TextFormat.split (biomolecule.get ("operators"), ',');
var assemblies = biomolecule.get ("assemblies");
this.vBiomts =  new J.util.JmolList ();
biomolecule.put ("biomts", this.vBiomts);
this.vBiomts.addLast (mident);
for (var j = 0; j < ops.length; j++) {
var m = this.htBiomts.get (ops[j]);
if (m != null && !m.equals (mident)) this.vBiomts.addLast (m);
}
if (this.vBiomts.size () < 2) return;
var bsAll =  new J.util.BS ();
for (var j = assemblies.length - 1; --j >= 0; ) if (assemblies.charAt (j) == '$') {
var bs = this.assemblyIdAtoms.get ("" + assemblies.charAt (j + 1));
if (bs != null) bsAll.or (bs);
}
var nAtoms = bsAll.cardinality ();
if (nAtoms < this.atomSetCollection.getAtomCount ()) this.atomSetCollection.bsAtoms = bsAll;
biomolecule.put ("atomCount", Integer.$valueOf (nAtoms * ops.length));
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "applySymmetryAndSetTrajectory", 
function () {
this.atomSetCollection.setCheckSpecial (!this.isPDB);
var doCheck = this.doCheckUnitCell && !this.isPDB;
Clazz.superCall (this, J.adapter.readers.cifpdb.CifReader, "applySymmetryAndSetTrajectory", []);
if (doCheck && (this.bondTypes.size () > 0 || this.isMolecular)) this.setBondingAndMolecules ();
});
$_M(c$, "processDataParameter", 
($fz = function () {
this.bondTypes.clear ();
this.tokenizer.getTokenPeeked ();
this.thisDataSetName = (this.key.length < 6 ? "" : this.key.substring (5));
if (this.thisDataSetName.length > 0) {
if (this.atomSetCollection.getCurrentAtomSetIndex () >= 0) {
this.atomSetCollection.newAtomSet ();
} else {
this.atomSetCollection.setCollectionName (this.thisDataSetName);
}}J.util.Logger.debug (this.key);
}, $fz.isPrivate = true, $fz));
$_M(c$, "processChemicalInfo", 
($fz = function (type) {
if (type.equals ("name")) {
this.chemicalName = this.data = this.tokenizer.fullTrim (this.data);
if (!this.data.equals ("?")) this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("modelLoadNote", this.data);
} else if (type.equals ("structuralFormula")) {
this.thisStructuralFormula = this.data = this.tokenizer.fullTrim (this.data);
} else if (type.equals ("formula")) {
this.thisFormula = this.data = this.tokenizer.fullTrim (this.data);
}if (J.util.Logger.debugging) {
J.util.Logger.debug (type + " = " + this.data);
}return this.data;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "processSymmetrySpaceGroupName", 
($fz = function () {
this.setSpaceGroupName ((this.key.equals ("_symmetry_space_group_name_H-M") ? "HM:" : "Hall:") + this.data);
}, $fz.isPrivate = true, $fz));
$_M(c$, "processCellParameter", 
($fz = function () {
for (var i = J.api.JmolAdapter.cellParamNames.length; --i >= 0; ) if (J.adapter.readers.cifpdb.CifReader.isMatch (this.key, J.api.JmolAdapter.cellParamNames[i])) {
this.setUnitCellItem (i, this.parseFloatStr (this.data));
return;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processUnitCellTransformMatrix", 
($fz = function () {
var v = this.parseFloatStr (this.data);
if (Float.isNaN (v)) return;
for (var i = 0; i < J.adapter.readers.cifpdb.CifReader.TransformFields.length; i++) {
if (this.key.indexOf (J.adapter.readers.cifpdb.CifReader.TransformFields[i]) >= 0) {
this.setUnitCellItem (6 + i, v);
return;
}}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getData", 
($fz = function () {
this.key = this.tokenizer.getTokenPeeked ();
this.data = this.tokenizer.getNextToken ();
if (J.util.Logger.debugging) J.util.Logger.debug (this.key + " " + this.data);
if (this.data == null) {
J.util.Logger.warn ("CIF ERROR ? end of file; data missing: " + this.key);
return false;
}return (this.data.length == 0 || this.data.charAt (0) != '\0');
}, $fz.isPrivate = true, $fz));
$_M(c$, "processLoopBlock", 
($fz = function () {
this.tokenizer.getTokenPeeked ();
var str = this.tokenizer.peekToken ();
if (str == null) return;
var isLigand = false;
if (str.startsWith ("_atom_site_") || str.startsWith ("_atom_site.") || (isLigand = str.equals ("_chem_comp_atom.comp_id"))) {
if (!this.processAtomSiteLoopBlock (isLigand)) return;
this.atomSetCollection.setAtomSetName (this.thisDataSetName);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("chemicalName", this.chemicalName);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("structuralFormula", this.thisStructuralFormula);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("formula", this.thisFormula);
return;
}if (str.startsWith ("_atom_type")) {
this.processAtomTypeLoopBlock ();
return;
}if (str.startsWith ("_chem_comp_bond")) {
this.processLigandBondLoopBlock ();
return;
}if (str.startsWith ("_geom_bond")) {
if (!this.doApplySymmetry) {
this.isMolecular = true;
this.doApplySymmetry = true;
this.latticeCells[0] = 1;
this.latticeCells[1] = 1;
this.latticeCells[2] = 1;
}if (this.isMolecular) this.processGeomBondLoopBlock ();
 else this.skipLoop ();
return;
}if (str.startsWith ("_pdbx_entity_nonpoly")) {
this.processNonpolyLoopBlock ();
return;
}if (str.startsWith ("_chem_comp")) {
this.processChemCompLoopBlock ();
return;
}if (str.startsWith ("_struct_conf") && !str.startsWith ("_struct_conf_type")) {
this.processStructConfLoopBlock ();
return;
}if (str.startsWith ("_struct_sheet_range")) {
this.processStructSheetRangeLoopBlock ();
return;
}if (str.startsWith ("_symmetry_equiv_pos") || str.startsWith ("_space_group_symop") || str.startsWith ("_space_group_symop")) {
if (this.ignoreFileSymmetryOperators) {
J.util.Logger.warn ("ignoring file-based symmetry operators");
this.skipLoop ();
} else {
this.processSymmetryOperationsLoopBlock ();
}return;
}if (str.startsWith ("_struct_site")) {
this.processStructSiteBlock ();
return;
}if (str.startsWith ("_pdbx_struct_oper_list")) {
this.processStructOperListBlock ();
return;
}if (str.startsWith ("_pdbx_struct_assembly_gen")) {
this.processAssemblyGenBlock ();
return;
}this.skipLoop ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "processAtomTypeLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.atomTypeFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
this.skipLoop ();
return;
}
while (this.tokenizer.getData ()) {
var atomTypeSymbol = null;
var oxidationNumber = NaN;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 0:
atomTypeSymbol = this.field;
break;
case 1:
oxidationNumber = this.parseFloatStr (this.field);
break;
}
}
if (atomTypeSymbol == null || Float.isNaN (oxidationNumber)) continue;
if (this.atomTypes == null) this.atomTypes =  new java.util.Hashtable ();
this.atomTypes.put (atomTypeSymbol, Float.$valueOf (oxidationNumber));
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processAtomSiteLoopBlock", 
function (isLigand) {
var currentModelNO = -1;
var isAnisoData = false;
var assemblyId = '\u0000';
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.atomFields);
if (this.fieldOf[55] != -1) {
this.isPDB = false;
this.setFractionalCoordinates (false);
} else if (this.fieldOf[6] != -1 || this.fieldOf[52] != -1) {
this.setFractionalCoordinates (false);
this.disableField (3);
this.disableField (4);
this.disableField (5);
} else if (this.fieldOf[3] != -1) {
this.setFractionalCoordinates (true);
this.disableField (6);
this.disableField (7);
this.disableField (8);
} else if (this.fieldOf[20] != -1) {
isAnisoData = true;
} else if (this.fieldOf[21] != -1) {
isAnisoData = true;
} else {
this.skipLoop ();
return false;
}var iAtom = -1;
var data;
while (this.tokenizer.getData ()) {
var atom =  new J.adapter.smarter.Atom ();
assemblyId = '\0';
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 50:
case 0:
var elementSymbol;
if (this.field.length < 2) {
elementSymbol = this.field;
} else {
var ch1 = Character.toLowerCase (this.field.charAt (1));
if (J.adapter.smarter.Atom.isValidElementSymbol2 (this.firstChar, ch1)) elementSymbol = "" + this.firstChar + ch1;
 else elementSymbol = "" + this.firstChar;
}atom.elementSymbol = elementSymbol;
if (this.atomTypes != null && this.atomTypes.containsKey (this.field)) {
var charge = this.atomTypes.get (this.field).floatValue ();
atom.formalCharge = Math.round (charge);
if (Math.abs (atom.formalCharge - charge) > 0.1) if (J.util.Logger.debugging) {
J.util.Logger.debug ("CIF charge on " + this.field + " was " + charge + "; rounded to " + atom.formalCharge);
}}break;
case 49:
case 1:
case 2:
atom.atomName = this.field;
break;
case 55:
var x = this.parseFloatStr (this.field);
if (!Float.isNaN (x)) atom.x = x;
break;
case 56:
var y = this.parseFloatStr (this.field);
if (!Float.isNaN (y)) atom.y = y;
break;
case 57:
var z = this.parseFloatStr (this.field);
if (!Float.isNaN (z)) atom.z = z;
break;
case 52:
case 6:
case 3:
atom.x = this.parseFloatStr (this.field);
break;
case 53:
case 7:
case 4:
atom.y = this.parseFloatStr (this.field);
break;
case 54:
case 8:
case 5:
atom.z = this.parseFloatStr (this.field);
break;
case 51:
atom.formalCharge = this.parseIntStr (this.field);
break;
case 9:
var floatOccupancy = this.parseFloatStr (this.field);
if (!Float.isNaN (floatOccupancy)) atom.occupancy = Clazz.floatToInt (floatOccupancy * 100);
break;
case 10:
atom.bfactor = this.parseFloatStr (this.field) * (this.isPDB ? 1 : 100);
break;
case 48:
case 11:
atom.group3 = this.field;
break;
case 59:
assemblyId = this.firstChar;
break;
case 12:
if (this.field.length > 1) J.util.Logger.warn ("Don't know how to deal with chains more than 1 char: " + this.field);
atom.chainID = this.firstChar;
break;
case 13:
atom.sequenceNumber = this.parseIntStr (this.field);
break;
case 14:
atom.insertionCode = this.firstChar;
break;
case 15:
atom.alternateLocationID = this.firstChar;
break;
case 58:
this.disorderAssembly = this.field;
break;
case 19:
if (this.firstChar == '-' && this.field.length > 1) {
atom.alternateLocationID = this.field.charAt (1);
atom.ignoreSymmetry = true;
} else {
atom.alternateLocationID = this.firstChar;
}break;
case 16:
this.isPDB = true;
if ("HETATM".equals (this.field)) atom.isHetero = true;
break;
case 17:
var modelNO = this.parseIntStr (this.field);
if (modelNO != currentModelNO) {
this.atomSetCollection.newAtomSet ();
currentModelNO = modelNO;
}break;
case 18:
if ("dum".equals (this.field)) {
atom.x = NaN;
continue;
}break;
case 47:
if (this.field.equalsIgnoreCase ("Uiso")) {
var j = this.fieldOf[34];
if (j != -1) {
data = this.atomSetCollection.getAnisoBorU (atom);
if (data == null) this.atomSetCollection.setAnisoBorU (atom, data =  Clazz.newFloatArray (8, 0), 8);
data[7] = this.parseFloatStr (this.tokenizer.loopData[j]);
}}break;
case 20:
iAtom = this.atomSetCollection.getAtomIndexFromName (this.field);
if (iAtom < 0) continue;
atom = this.atomSetCollection.getAtom (iAtom);
break;
case 21:
atom = this.atomSetCollection.getAtom (++iAtom);
break;
case 22:
case 23:
case 24:
case 25:
case 26:
case 27:
case 28:
case 29:
case 30:
case 31:
case 32:
case 33:
data = this.atomSetCollection.getAnisoBorU (atom);
if (data == null) {
this.atomSetCollection.setAnisoBorU (atom, data =  Clazz.newFloatArray (8, 0), 8);
}var iType = (this.propertyOf[i] - 22) % 6;
data[iType] = this.parseFloatStr (this.field);
break;
case 35:
case 36:
case 37:
case 38:
case 39:
case 40:
data = this.atomSetCollection.getAnisoBorU (atom);
if (data == null) {
this.atomSetCollection.setAnisoBorU (atom, data =  Clazz.newFloatArray (8, 0), 4);
}var iTypeB = (this.propertyOf[i] - 35) % 6;
data[iTypeB] = this.parseFloatStr (this.field);
break;
case 41:
case 42:
case 43:
case 44:
case 45:
case 46:
data = this.atomSetCollection.getAnisoBorU (atom);
if (data == null) {
this.atomSetCollection.setAnisoBorU (atom, data =  Clazz.newFloatArray (8, 0), 0);
}var iTypeBeta = (this.propertyOf[i] - 41) % 6;
data[iTypeBeta] = this.parseFloatStr (this.field);
break;
}
}
if (Float.isNaN (atom.x) || Float.isNaN (atom.y) || Float.isNaN (atom.z)) {
J.util.Logger.warn ("atom " + atom.atomName + " has invalid/unknown coordinates");
} else {
if (isAnisoData || !this.filterCIFAtom (atom, iAtom, assemblyId)) continue;
this.setAtomCoord (atom);
this.atomSetCollection.addAtomWithMappedName (atom);
if (assemblyId != '\0') {
if (this.assemblyIdAtoms == null) this.assemblyIdAtoms =  new java.util.Hashtable ();
var bs = this.assemblyIdAtoms.get ("" + assemblyId);
if (bs == null) this.assemblyIdAtoms.put ("" + assemblyId, bs =  new J.util.BS ());
bs.set (atom.atomIndex);
}if (atom.isHetero && this.htHetero != null) {
this.atomSetCollection.setAtomSetAuxiliaryInfo ("hetNames", this.htHetero);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("hetNames", this.htHetero);
this.htHetero = null;
}}}
if (this.isPDB) {
this.setIsPDB ();
}this.atomSetCollection.setAtomSetAuxiliaryInfo ("isCIF", Boolean.TRUE);
return true;
}, "~B");
$_M(c$, "filterCIFAtom", 
function (atom, iAtom, assemblyId) {
if (!this.filterAtom (atom, iAtom)) return false;
if (this.filterAssembly && this.filterReject (this.filter, "$", "" + assemblyId)) return false;
if (this.configurationPtr > 0) {
if (!this.disorderAssembly.equals (this.lastDisorderAssembly)) {
this.lastDisorderAssembly = this.disorderAssembly;
this.lastAltLoc = '\0';
this.conformationIndex = this.configurationPtr;
}if (atom.alternateLocationID != '\0') {
if (this.conformationIndex >= 0 && atom.alternateLocationID != this.lastAltLoc) {
this.lastAltLoc = atom.alternateLocationID;
this.conformationIndex--;
}if (this.conformationIndex != 0) {
J.util.Logger.info ("ignoring " + atom.atomName);
return false;
}}}return true;
}, "J.adapter.smarter.Atom,~N,~S");
$_M(c$, "processAssemblyGen", 
($fz = function () {
if (this.assem == null) this.assem =  new Array (3);
if (this.key.indexOf ("assembly_id") >= 0) this.assem[0] = this.data = this.tokenizer.fullTrim (this.data);
 else if (this.key.indexOf ("oper_expression") >= 0) this.assem[1] = this.data = this.tokenizer.fullTrim (this.data);
 else if (this.key.indexOf ("asym_id_list") >= 0) this.assem[2] = this.data = this.tokenizer.fullTrim (this.data);
if (this.assem[0] != null && this.assem[1] != null && this.assem[2] != null) this.addAssembly ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "processAssemblyGenBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.assemblyFields);
this.assem =  new Array (3);
while (this.tokenizer.getData ()) {
var count = 0;
var p;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (p = this.fieldProperty (i)) {
case 0:
case 1:
case 2:
count++;
this.assem[p] = this.field;
break;
}
}
if (count == 3) this.addAssembly ();
}
this.assem = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "addAssembly", 
($fz = function () {
var iMolecule = this.parseIntStr (this.assem[0]);
if (!this.checkFilterKey ("ASSEMBLY " + iMolecule + ";")) return;
if (this.vBiomolecules == null) {
this.vBiomolecules =  new J.util.JmolList ();
}var info =  new java.util.Hashtable ();
info.put ("molecule", Integer.$valueOf (iMolecule));
info.put ("assemblies", "$" + this.assem[2].$replace (',', '$'));
info.put ("operators", this.assem[1]);
info.put ("biomts",  new J.util.JmolList ());
J.util.Logger.info ("assembly " + iMolecule + " operators " + this.assem[1] + " ASYM_IDs " + this.assem[2]);
this.vBiomolecules.addLast (info);
this.assem = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "processStructOperListBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.operFields);
var m =  Clazz.newFloatArray (16, 0);
m[15] = 1;
while (this.tokenizer.getData ()) {
var count = 0;
var p;
var id = null;
var xyz = null;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (p = this.fieldProperty (i)) {
case -1:
break;
case 12:
id = this.field;
break;
case 13:
xyz = this.field;
break;
default:
m[p] = this.parseFloatStr (this.field);
++count;
}
}
if (id != null && (count == 12 || xyz != null && this.symmetry != null)) {
J.util.Logger.info ("assembly operator " + id + " " + xyz);
var m4 =  new J.util.Matrix4f ();
if (count != 12) {
this.symmetry.getMatrixFromString (xyz, m, false);
m[3] *= this.symmetry.getUnitCellInfoType (0) / 12;
m[7] *= this.symmetry.getUnitCellInfoType (1) / 12;
m[11] *= this.symmetry.getUnitCellInfoType (2) / 12;
}m4.setA (m);
if (this.htBiomts == null) this.htBiomts =  new java.util.Hashtable ();
this.htBiomts.put (id, m4);
}}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processLigandBondLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.chemCompBondFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
J.util.Logger.warn ("?que? missing _chem_comp_bond property:" + i);
this.skipLoop ();
return;
}
var order = 0;
var isAromatic = false;
while (this.tokenizer.getData ()) {
var atomIndex1 = -1;
var atomIndex2 = -1;
order = 0;
isAromatic = false;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case 0:
atomIndex1 = this.atomSetCollection.getAtomIndexFromName (this.field);
break;
case 1:
atomIndex2 = this.atomSetCollection.getAtomIndexFromName (this.field);
break;
case 3:
isAromatic = (this.field.charAt (0) == 'Y');
break;
case 2:
order = 1;
if (this.field.equals ("SING")) order = 1;
 else if (this.field.equals ("DOUB")) order = 2;
 else if (this.field.equals ("TRIP")) order = 3;
 else J.util.Logger.warn ("unknown CIF bond order: " + this.field);
break;
}
}
if (atomIndex1 < 0 || atomIndex2 < 0) continue;
if (isAromatic) switch (order) {
case 1:
order = 513;
break;
case 2:
order = 514;
break;
}
this.atomSetCollection.addNewBondWithOrder (atomIndex1, atomIndex2, order);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processGeomBondLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.geomBondFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
J.util.Logger.warn ("?que? missing _geom_bond property:" + i);
this.skipLoop ();
return;
}
var name1 = null;
var name2 = null;
while (this.tokenizer.getData ()) {
var atomIndex1 = -1;
var atomIndex2 = -1;
var distance = 0;
var dx = 0;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 0:
atomIndex1 = this.atomSetCollection.getAtomIndexFromName (name1 = this.field);
break;
case 1:
atomIndex2 = this.atomSetCollection.getAtomIndexFromName (name2 = this.field);
break;
case 2:
distance = this.parseFloatStr (this.field);
var pt = this.field.indexOf ('(');
if (pt >= 0) {
var data = this.field.toCharArray ();
var sdx = this.field.substring (pt + 1, this.field.length - 1);
var n = sdx.length;
for (var j = pt; --j >= 0; ) {
if (data[j] == '.') --j;
data[j] = (--n < 0 ? '0' : sdx.charAt (n));
}
dx = this.parseFloatStr (String.valueOf (data));
if (Float.isNaN (dx)) {
J.util.Logger.info ("error reading uncertainty for " + this.line);
dx = 0.015;
}} else {
dx = 0.015;
}break;
}
}
if (atomIndex1 < 0 || atomIndex2 < 0) continue;
if (distance > 0) this.bondTypes.addLast ([name1, name2, Float.$valueOf (distance), Float.$valueOf (dx)]);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processNonpolyData", 
($fz = function () {
if (this.hetatmData == null) this.hetatmData =  new Array (3);
for (var i = J.adapter.readers.cifpdb.CifReader.nonpolyFields.length; --i >= 0; ) if (J.adapter.readers.cifpdb.CifReader.isMatch (this.key, J.adapter.readers.cifpdb.CifReader.nonpolyFields[i])) {
this.hetatmData[i] = this.data;
break;
}
if (this.hetatmData[1] == null || this.hetatmData[2] == null) return;
this.addHetero (this.hetatmData[2], this.hetatmData[1]);
this.hetatmData = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "processChemCompLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.chemCompFields);
while (this.tokenizer.getData ()) {
var groupName = null;
var hetName = null;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 0:
groupName = this.field;
break;
case 1:
hetName = this.field;
break;
}
}
if (groupName != null && hetName != null) this.addHetero (groupName, hetName);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processNonpolyLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.nonpolyFields);
while (this.tokenizer.getData ()) {
var groupName = null;
var hetName = null;
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
case 0:
break;
case 2:
groupName = this.field;
break;
case 1:
hetName = this.field;
break;
}
}
if (groupName == null || hetName == null) return;
this.addHetero (groupName, hetName);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "addHetero", 
($fz = function (groupName, hetName) {
if (!J.api.JmolAdapter.isHetero (groupName)) return;
if (this.htHetero == null) this.htHetero =  new java.util.Hashtable ();
this.htHetero.put (groupName, hetName);
if (J.util.Logger.debugging) {
J.util.Logger.debug ("hetero: " + groupName + " = " + hetName);
}}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "processStructConfLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.structConfFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
J.util.Logger.warn ("?que? missing _struct_conf property:" + i);
this.skipLoop ();
return;
}
while (this.tokenizer.getData ()) {
var structure =  new J.adapter.smarter.Structure (-1, J.constant.EnumStructure.HELIX, J.constant.EnumStructure.HELIX, null, 0, 0);
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 0:
if (this.field.startsWith ("TURN")) structure.structureType = structure.substructureType = J.constant.EnumStructure.TURN;
 else if (!this.field.startsWith ("HELX")) structure.structureType = structure.substructureType = J.constant.EnumStructure.NONE;
break;
case 1:
structure.startChainID = this.firstChar;
break;
case 2:
structure.startSequenceNumber = this.parseIntStr (this.field);
break;
case 3:
structure.startInsertionCode = this.firstChar;
break;
case 4:
structure.endChainID = this.firstChar;
break;
case 5:
structure.endSequenceNumber = this.parseIntStr (this.field);
break;
case 9:
structure.substructureType = J.adapter.smarter.Structure.getHelixType (this.parseIntStr (this.field));
break;
case 6:
structure.endInsertionCode = this.firstChar;
break;
case 7:
structure.structureID = this.field;
break;
case 8:
structure.serialID = this.parseIntStr (this.field);
break;
}
}
this.atomSetCollection.addStructure (structure);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processStructSheetRangeLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.structSheetRangeFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
J.util.Logger.warn ("?que? missing _struct_conf property:" + i);
this.skipLoop ();
return;
}
while (this.tokenizer.getData ()) {
var structure =  new J.adapter.smarter.Structure (-1, J.constant.EnumStructure.SHEET, J.constant.EnumStructure.SHEET, null, 0, 0);
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case 1:
structure.startChainID = this.firstChar;
break;
case 2:
structure.startSequenceNumber = this.parseIntStr (this.field);
break;
case 3:
structure.startInsertionCode = this.firstChar;
break;
case 4:
structure.endChainID = this.firstChar;
break;
case 5:
structure.endSequenceNumber = this.parseIntStr (this.field);
break;
case 6:
structure.endInsertionCode = this.firstChar;
break;
case 0:
structure.strandCount = 1;
structure.structureID = this.field;
break;
case 7:
structure.serialID = this.parseIntStr (this.field);
break;
}
}
this.atomSetCollection.addStructure (structure);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processStructSiteBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.structSiteRangeFields);
for (var i = 3; --i >= 0; ) if (this.fieldOf[i] == -1) {
J.util.Logger.warn ("?que? missing _struct_site property:" + i);
this.skipLoop ();
return;
}
var siteID = "";
var seqNum = "";
var insCode = "";
var chainID = "";
var resID = "";
var group = "";
var htSite = null;
this.htSites =  new java.util.Hashtable ();
while (this.tokenizer.getData ()) {
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case 0:
if (group !== "") {
var groups = htSite.get ("groups");
groups += (groups.length == 0 ? "" : ",") + group;
group = "";
htSite.put ("groups", groups);
}siteID = this.field;
htSite = this.htSites.get (siteID);
if (htSite == null) {
htSite =  new java.util.Hashtable ();
htSite.put ("groups", "");
this.htSites.put (siteID, htSite);
}seqNum = "";
insCode = "";
chainID = "";
resID = "";
break;
case 1:
resID = this.field;
break;
case 2:
chainID = this.field;
break;
case 3:
seqNum = this.field;
break;
case 4:
insCode = this.field;
break;
}
if (seqNum !== "" && resID !== "") group = "[" + resID + "]" + seqNum + (insCode.length > 0 ? "^" + insCode : "") + (chainID.length > 0 ? ":" + chainID : "");
}
}
if (group !== "") {
var groups = htSite.get ("groups");
groups += (groups.length == 0 ? "" : ",") + group;
group = "";
htSite.put ("groups", groups);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "processSymmetryOperationsLoopBlock", 
($fz = function () {
this.parseLoopParameters (J.adapter.readers.cifpdb.CifReader.symmetryOperationsFields);
var nRefs = 0;
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] != -1) nRefs++;

if (nRefs != 1) {
J.util.Logger.warn ("?que? _symmetry_equiv or _space_group_symop property not found");
this.skipLoop ();
return;
}while (this.tokenizer.getData ()) {
for (var i = 0; i < this.tokenizer.fieldCount; ++i) {
switch (this.fieldProperty (i)) {
case 0:
case 1:
case 2:
this.setSymmetryOperator (this.field);
break;
}
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "fieldProperty", 
($fz = function (i) {
return ((this.field = this.tokenizer.loopData[i]).length > 0 && (this.firstChar = this.field.charAt (0)) != '\0' ? this.propertyOf[i] : -1);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "parseLoopParameters", 
($fz = function (fields) {
this.tokenizer.fieldCount = 0;
for (var i = fields.length; --i >= 0; ) this.fieldOf[i] = -1;

this.propertyCount = fields.length;
while (true) {
var str = this.tokenizer.peekToken ();
if (str == null) {
this.tokenizer.fieldCount = 0;
break;
}if (str.charAt (0) != '_') break;
this.tokenizer.getTokenPeeked ();
this.propertyOf[this.tokenizer.fieldCount] = -1;
for (var i = fields.length; --i >= 0; ) if (J.adapter.readers.cifpdb.CifReader.isMatch (str, fields[i])) {
this.propertyOf[this.tokenizer.fieldCount] = i;
this.fieldOf[i] = this.tokenizer.fieldCount;
break;
}
this.tokenizer.fieldCount++;
}
if (this.tokenizer.fieldCount > 0) this.tokenizer.loopData =  new Array (this.tokenizer.fieldCount);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "disableField", 
($fz = function (fieldIndex) {
var i = this.fieldOf[fieldIndex];
if (i != -1) this.propertyOf[i] = -1;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "skipLoop", 
($fz = function () {
var str;
while ((str = this.tokenizer.peekToken ()) != null && str.charAt (0) == '_') str = this.tokenizer.getTokenPeeked ();

while (this.tokenizer.getNextDataToken () != null) {
}
}, $fz.isPrivate = true, $fz));
c$.isMatch = $_M(c$, "isMatch", 
($fz = function (str1, str2) {
var cch = str1.length;
if (str2.length != cch) return false;
for (var i = cch; --i >= 0; ) {
var ch1 = str1.charAt (i);
var ch2 = str2.charAt (i);
if (ch1 == ch2) continue;
if ((ch1 == '_' || ch1 == '.') && (ch2 == '_' || ch2 == '.')) continue;
if (ch1 <= 'Z' && ch1 >= 'A') ch1 = String.fromCharCode (ch1.charCodeAt (0) + 32);
 else if (ch2 <= 'Z' && ch2 >= 'A') ch2 = String.fromCharCode (ch2.charCodeAt (0) + 32);
if (ch1 != ch2) return false;
}
return true;
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "setBondingAndMolecules", 
($fz = function () {
J.util.Logger.info ("CIF creating molecule " + (this.bondTypes.size () > 0 ? " using GEOM_BOND records" : ""));
this.atoms = this.atomSetCollection.getAtoms ();
this.firstAtom = this.atomSetCollection.getLastAtomSetAtomIndex ();
var nAtoms = this.atomSetCollection.getLastAtomSetAtomCount ();
this.atomCount = this.firstAtom + nAtoms;
this.bsSets =  new Array (nAtoms);
this.symmetry = this.atomSetCollection.getSymmetry ();
for (var i = this.firstAtom; i < this.atomCount; i++) {
var ipt = this.atomSetCollection.getAtomIndexFromName (this.atoms[i].atomName) - this.firstAtom;
if (this.bsSets[ipt] == null) this.bsSets[ipt] =  new J.util.BS ();
this.bsSets[ipt].set (i - this.firstAtom);
}
if (this.isMolecular) {
this.atomRadius =  Clazz.newFloatArray (this.atomCount, 0);
for (var i = this.firstAtom; i < this.atomCount; i++) {
var elemnoWithIsotope = this.atoms[i].elementNumber = J.api.JmolAdapter.getElementNumber (this.atoms[i].getElementSymbol ());
var charge = (this.atoms[i].formalCharge == -2147483648 ? 0 : this.atoms[i].formalCharge);
if (elemnoWithIsotope > 0) this.atomRadius[i] = J.api.JmolAdapter.getBondingRadiusFloat (elemnoWithIsotope, charge);
}
this.bsConnected =  new Array (this.atomCount);
for (var i = this.firstAtom; i < this.atomCount; i++) this.bsConnected[i] =  new J.util.BS ();

this.bsMolecule =  new J.util.BS ();
this.bsExclude =  new J.util.BS ();
}var isFirst = true;
while (this.createBonds (isFirst)) {
isFirst = false;
}
if (this.isMolecular) {
if (this.atomSetCollection.bsAtoms == null) this.atomSetCollection.bsAtoms =  new J.util.BS ();
this.atomSetCollection.bsAtoms.clearBits (this.firstAtom, this.atomCount);
this.atomSetCollection.bsAtoms.or (this.bsMolecule);
this.atomSetCollection.bsAtoms.andNot (this.bsExclude);
for (var i = this.firstAtom; i < this.atomCount; i++) {
if (this.atomSetCollection.bsAtoms.get (i)) this.symmetry.toCartesian (this.atoms[i], true);
 else if (J.util.Logger.debugging) J.util.Logger.info (this.molecularType + " removing " + i + " " + this.atoms[i].atomName + " " + this.atoms[i]);
}
this.atomSetCollection.setAtomSetAuxiliaryInfo ("notionalUnitcell", null);
if (this.nMolecular++ == this.atomSetCollection.getCurrentAtomSetIndex ()) {
this.atomSetCollection.clearGlobalBoolean (0);
this.atomSetCollection.clearGlobalBoolean (1);
this.atomSetCollection.clearGlobalBoolean (2);
}}if (this.bondTypes.size () > 0) this.atomSetCollection.setAtomSetAuxiliaryInfo ("hasBonds", Boolean.TRUE);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("fileHasUnitCell", Boolean.TRUE);
this.bondTypes.clear ();
this.atomRadius = null;
this.bsSets = null;
this.bsConnected = null;
this.bsMolecule = null;
this.bsExclude = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "createBonds", 
($fz = function (doInit) {
for (var i = this.bondTypes.size (); --i >= 0; ) {
var o = this.bondTypes.get (i);
var distance = (o[2]).floatValue ();
var dx = (o[3]).floatValue ();
var iatom1 = this.atomSetCollection.getAtomIndexFromName (o[0]);
var iatom2 = this.atomSetCollection.getAtomIndexFromName (o[1]);
var bs1 = this.bsSets[iatom1 - this.firstAtom];
var bs2 = this.bsSets[iatom2 - this.firstAtom];
if (bs1 == null || bs2 == null) continue;
for (var j = bs1.nextSetBit (0); j >= 0; j = bs1.nextSetBit (j + 1)) for (var k = bs2.nextSetBit (0); k >= 0; k = bs2.nextSetBit (k + 1)) {
if ((!this.isMolecular || !this.bsConnected[j + this.firstAtom].get (k)) && this.symmetry.checkDistance (this.atoms[j + this.firstAtom], this.atoms[k + this.firstAtom], distance, dx, 0, 0, 0, this.ptOffset)) this.addNewBond (j + this.firstAtom, k + this.firstAtom);
}

}
if (this.bondTypes.size () > 0) for (var i = this.firstAtom; i < this.atomCount; i++) if (this.atoms[i].elementNumber == 1) {
var checkAltLoc = (this.atoms[i].alternateLocationID != '\0');
for (var k = this.firstAtom; k < this.atomCount; k++) if (k != i && this.atoms[k].elementNumber != 1 && (!checkAltLoc || this.atoms[k].alternateLocationID == '\0' || this.atoms[k].alternateLocationID == this.atoms[i].alternateLocationID)) {
if (!this.bsConnected[i].get (k) && this.symmetry.checkDistance (this.atoms[i], this.atoms[k], 1.1, 0, 0, 0, 0, this.ptOffset)) this.addNewBond (i, k);
}
}
if (!this.isMolecular) return false;
if (doInit) for (var i = this.firstAtom; i < this.atomCount; i++) if (this.atoms[i].atomSite + this.firstAtom == i && !this.bsMolecule.get (i)) this.setBs (this.atoms, i, this.bsConnected, this.bsMolecule);

var bondTolerance = this.viewer.getBondTolerance ();
var bsBranch =  new J.util.BS ();
var cart1 =  new J.util.P3 ();
var cart2 =  new J.util.P3 ();
var nFactor = 2;
for (var i = this.firstAtom; i < this.atomCount; i++) if (!this.bsMolecule.get (i) && !this.bsExclude.get (i)) for (var j = this.bsMolecule.nextSetBit (0); j >= 0; j = this.bsMolecule.nextSetBit (j + 1)) if (this.symmetry.checkDistance (this.atoms[j], this.atoms[i], this.atomRadius[i] + this.atomRadius[j] + bondTolerance, 0, nFactor, nFactor, nFactor, this.ptOffset)) {
this.setBs (this.atoms, i, this.bsConnected, bsBranch);
for (var k = bsBranch.nextSetBit (0); k >= 0; k = bsBranch.nextSetBit (k + 1)) {
this.atoms[k].add (this.ptOffset);
cart1.setT (this.atoms[k]);
this.symmetry.toCartesian (cart1, true);
var bs = this.bsSets[this.atomSetCollection.getAtomIndexFromName (this.atoms[k].atomName) - this.firstAtom];
if (bs != null) for (var ii = bs.nextSetBit (0); ii >= 0; ii = bs.nextSetBit (ii + 1)) {
if (ii + this.firstAtom == k) continue;
cart2.setT (this.atoms[ii + this.firstAtom]);
this.symmetry.toCartesian (cart2, true);
if (cart2.distance (cart1) < 0.1) {
this.bsExclude.set (k);
break;
}}
this.bsMolecule.set (k);
}
return true;
}

return false;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "addNewBond", 
($fz = function (i, j) {
this.atomSetCollection.addNewBond (i, j);
if (!this.isMolecular) return;
this.bsConnected[i].set (j);
this.bsConnected[j].set (i);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setBs", 
($fz = function (atoms, iatom, bsBonds, bs) {
var bsBond = bsBonds[iatom];
bs.set (iatom);
for (var i = bsBond.nextSetBit (0); i >= 0; i = bsBond.nextSetBit (i + 1)) {
if (!bs.get (i)) this.setBs (atoms, i, bsBonds, bs);
}
}, $fz.isPrivate = true, $fz), "~A,~N,~A,J.util.BS");
Clazz.defineStatics (c$,
"TransformFields", ["x[1][1]", "x[1][2]", "x[1][3]", "r[1]", "x[2][1]", "x[2][2]", "x[2][3]", "r[2]", "x[3][1]", "x[3][2]", "x[3][3]", "r[3]"],
"ATOM_TYPE_SYMBOL", 0,
"ATOM_TYPE_OXIDATION_NUMBER", 1,
"atomTypeFields", ["_atom_type_symbol", "_atom_type_oxidation_number"],
"NONE", -1,
"TYPE_SYMBOL", 0,
"LABEL", 1,
"AUTH_ATOM", 2,
"FRACT_X", 3,
"FRACT_Y", 4,
"FRACT_Z", 5,
"CARTN_X", 6,
"CARTN_Y", 7,
"CARTN_Z", 8,
"OCCUPANCY", 9,
"B_ISO", 10,
"COMP_ID", 11,
"AUTH_ASYM_ID", 12,
"SEQ_ID", 13,
"INS_CODE", 14,
"ALT_ID", 15,
"GROUP_PDB", 16,
"MODEL_NO", 17,
"DUMMY_ATOM", 18,
"DISORDER_GROUP", 19,
"ANISO_LABEL", 20,
"ANISO_MMCIF_ID", 21,
"ANISO_U11", 22,
"ANISO_U22", 23,
"ANISO_U33", 24,
"ANISO_U12", 25,
"ANISO_U13", 26,
"ANISO_U23", 27,
"ANISO_MMCIF_U11", 28,
"ANISO_MMCIF_U22", 29,
"ANISO_MMCIF_U33", 30,
"ANISO_MMCIF_U12", 31,
"ANISO_MMCIF_U13", 32,
"ANISO_MMCIF_U23", 33,
"U_ISO_OR_EQUIV", 34,
"ANISO_B11", 35,
"ANISO_B22", 36,
"ANISO_B33", 37,
"ANISO_B12", 38,
"ANISO_B13", 39,
"ANISO_B23", 40,
"ANISO_Beta_11", 41,
"ANISO_Beta_22", 42,
"ANISO_Beta_33", 43,
"ANISO_Beta_12", 44,
"ANISO_Beta_13", 45,
"ANISO_Beta_23", 46,
"ADP_TYPE", 47,
"CHEM_COMP_AC_ID", 48,
"CHEM_COMP_AC_NAME", 49,
"CHEM_COMP_AC_SYM", 50,
"CHEM_COMP_AC_CHARGE", 51,
"CHEM_COMP_AC_X", 52,
"CHEM_COMP_AC_Y", 53,
"CHEM_COMP_AC_Z", 54,
"CHEM_COMP_AC_X_IDEAL", 55,
"CHEM_COMP_AC_Y_IDEAL", 56,
"CHEM_COMP_AC_Z_IDEAL", 57,
"DISORDER_ASSEMBLY", 58,
"ASYM_ID", 59,
"atomFields", ["_atom_site_type_symbol", "_atom_site_label", "_atom_site_auth_atom_id", "_atom_site_fract_x", "_atom_site_fract_y", "_atom_site_fract_z", "_atom_site_Cartn_x", "_atom_site_Cartn_y", "_atom_site_Cartn_z", "_atom_site_occupancy", "_atom_site_b_iso_or_equiv", "_atom_site_auth_comp_id", "_atom_site_auth_asym_id", "_atom_site_auth_seq_id", "_atom_site_pdbx_PDB_ins_code", "_atom_site_label_alt_id", "_atom_site_group_PDB", "_atom_site_pdbx_PDB_model_num", "_atom_site_calc_flag", "_atom_site_disorder_group", "_atom_site_aniso_label", "_atom_site_anisotrop_id", "_atom_site_aniso_U_11", "_atom_site_aniso_U_22", "_atom_site_aniso_U_33", "_atom_site_aniso_U_12", "_atom_site_aniso_U_13", "_atom_site_aniso_U_23", "_atom_site_anisotrop_U[1][1]", "_atom_site_anisotrop_U[2][2]", "_atom_site_anisotrop_U[3][3]", "_atom_site_anisotrop_U[1][2]", "_atom_site_anisotrop_U[1][3]", "_atom_site_anisotrop_U[2][3]", "_atom_site_U_iso_or_equiv", "_atom_site_aniso_B_11", "_atom_site_aniso_B_22", "_atom_site_aniso_B_33", "_atom_site_aniso_B_12", "_atom_site_aniso_B_13", "_atom_site_aniso_B_23", "_atom_site_aniso_Beta_11", "_atom_site_aniso_Beta_22", "_atom_site_aniso_Beta_33", "_atom_site_aniso_Beta_12", "_atom_site_aniso_Beta_13", "_atom_site_aniso_Beta_23", "_atom_site_adp_type", "_chem_comp_atom_comp_id", "_chem_comp_atom_atom_id", "_chem_comp_atom_type_symbol", "_chem_comp_atom_charge", "_chem_comp_atom_model_Cartn_x", "_chem_comp_atom_model_Cartn_y", "_chem_comp_atom_model_Cartn_z", "_chem_comp_atom_pdbx_model_Cartn_x_ideal", "_chem_comp_atom_pdbx_model_Cartn_y_ideal", "_chem_comp_atom_pdbx_model_Cartn_z_ideal", "_atom_site_disorder_assembly", "_atom_site_label_asym_id"],
"OPER_ID", 12,
"OPER_XYZ", 13,
"operFields", ["_pdbx_struct_oper_list_matrix[1][1]", "_pdbx_struct_oper_list_matrix[1][2]", "_pdbx_struct_oper_list_matrix[1][3]", "_pdbx_struct_oper_list_vector[1]", "_pdbx_struct_oper_list_matrix[2][1]", "_pdbx_struct_oper_list_matrix[2][2]", "_pdbx_struct_oper_list_matrix[2][3]", "_pdbx_struct_oper_list_vector[2]", "_pdbx_struct_oper_list_matrix[3][1]", "_pdbx_struct_oper_list_matrix[3][2]", "_pdbx_struct_oper_list_matrix[3][3]", "_pdbx_struct_oper_list_vector[3]", "_pdbx_struct_oper_list_id", "_pdbx_struct_oper_list_symmetry_operation"],
"ASSEM_ID", 0,
"ASSEM_OPERS", 1,
"ASSEM_LIST", 2,
"assemblyFields", ["_pdbx_struct_assembly_gen_assembly_id", "_pdbx_struct_assembly_gen_oper_expression", "_pdbx_struct_assembly_gen_asym_id_list"],
"CHEM_COMP_BOND_ATOM_ID_1", 0,
"CHEM_COMP_BOND_ATOM_ID_2", 1,
"CHEM_COMP_BOND_VALUE_ORDER", 2,
"CHEM_COMP_BOND_AROMATIC_FLAG", 3,
"chemCompBondFields", ["_chem_comp_bond_atom_id_1", "_chem_comp_bond_atom_id_2", "_chem_comp_bond_value_order", "_chem_comp_bond_pdbx_aromatic_flag"],
"GEOM_BOND_ATOM_SITE_LABEL_1", 0,
"GEOM_BOND_ATOM_SITE_LABEL_2", 1,
"GEOM_BOND_DISTANCE", 2,
"geomBondFields", ["_geom_bond_atom_site_label_1", "_geom_bond_atom_site_label_2", "_geom_bond_distance"],
"NONPOLY_ENTITY_ID", 0,
"NONPOLY_NAME", 1,
"NONPOLY_COMP_ID", 2,
"nonpolyFields", ["_pdbx_entity_nonpoly_entity_id", "_pdbx_entity_nonpoly_name", "_pdbx_entity_nonpoly_comp_id"],
"CHEM_COMP_ID", 0,
"CHEM_COMP_NAME", 1,
"chemCompFields", ["_chem_comp_id", "_chem_comp_name"],
"CONF_TYPE_ID", 0,
"BEG_ASYM_ID", 1,
"BEG_SEQ_ID", 2,
"BEG_INS_CODE", 3,
"END_ASYM_ID", 4,
"END_SEQ_ID", 5,
"END_INS_CODE", 6,
"STRUCT_ID", 7,
"SERIAL_NO", 8,
"HELIX_CLASS", 9,
"structConfFields", ["_struct_conf_conf_type_id", "_struct_conf_beg_auth_asym_id", "_struct_conf_beg_auth_seq_id", "_struct_conf_pdbx_beg_PDB_ins_code", "_struct_conf_end_auth_asym_id", "_struct_conf_end_auth_seq_id", "_struct_conf_pdbx_end_PDB_ins_code", "_struct_conf_id", "_struct_conf_pdbx_PDB_helix_id", "_struct_conf_pdbx_PDB_helix_class"],
"SHEET_ID", 0,
"STRAND_ID", 7,
"structSheetRangeFields", ["_struct_sheet_range_sheet_id", "_struct_sheet_range_beg_auth_asym_id", "_struct_sheet_range_beg_auth_seq_id", "_struct_sheet_range_pdbx_beg_PDB_ins_code", "_struct_sheet_range_end_auth_asym_id", "_struct_sheet_range_end_auth_seq_id", "_struct_sheet_range_pdbx_end_PDB_ins_code", "_struct_sheet_range_id"],
"SITE_ID", 0,
"SITE_COMP_ID", 1,
"SITE_ASYM_ID", 2,
"SITE_SEQ_ID", 3,
"SITE_INS_CODE", 4,
"structSiteRangeFields", ["_struct_site_gen_site_id", "_struct_site_gen_auth_comp_id", "_struct_site_gen_auth_asym_id", "_struct_site_gen_auth_seq_id", "_struct_site_gen_label_alt_id"],
"SYMOP_XYZ", 0,
"SYM_EQUIV_XYZ", 1,
"SYM_SSG_OP", 2,
"symmetryOperationsFields", ["_space_group_symop_operation_xyz", "_symmetry_equiv_pos_as_xyz", "_space_group_symop_ssg_operation_algebraic"]);
});
