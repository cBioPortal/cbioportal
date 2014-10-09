Clazz.declarePackage ("J.adapter.readers.cif");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader", "JU.Lst", "$.P3"], "J.adapter.readers.cif.CifReader", ["java.lang.Boolean", "$.Character", "$.Float", "java.util.Hashtable", "JU.BS", "$.CifDataParser", "$.PT", "$.Rdr", "$.V3", "J.adapter.smarter.Atom", "J.api.Interface", "$.JmolAdapter", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.mr = null;
this.parser = null;
this.isMolecular = false;
this.filterAssembly = false;
this.allowRotations = true;
this.checkSpecial = true;
this.readIdeal = true;
this.configurationPtr = -2147483648;
this.thisDataSetName = "";
this.chemicalName = "";
this.thisStructuralFormula = "";
this.thisFormula = "";
this.iHaveDesiredModel = false;
this.isMMCIF = false;
this.isMagCIF = false;
this.molecularType = "GEOM_BOND default";
this.lastAltLoc = '\0';
this.haveAromatic = false;
this.conformationIndex = 0;
this.nMolecular = 0;
this.appendedData = null;
this.skipping = false;
this.nAtoms = 0;
this.ac = 0;
this.auditBlockCode = null;
this.lastSpaceGroupName = null;
this.modulated = false;
this.isCourseGrained = false;
this.haveCellWaveVector = false;
this.latticeType = null;
this.modDim = 0;
this.htGroup1 = null;
this.nAtoms0 = 0;
this.htCellTypes = null;
this.modelMap = null;
this.htAudit = null;
this.symops = null;
this.key = null;
this.data = null;
this.propertyOf = null;
this.fieldOf = null;
this.field = null;
this.firstChar = '\0';
this.propertyCount = 0;
this.atomTypes = null;
this.bondTypes = null;
this.disorderAssembly = ".";
this.lastDisorderAssembly = null;
this.lattvecs = null;
this.centerings = null;
this.maxSerial = 0;
this.atomRadius = null;
this.bsConnected = null;
this.bsSets = null;
this.ptOffset = null;
this.bsMolecule = null;
this.bsExclude = null;
this.firstAtom = 0;
this.atoms = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.cif, "CifReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.propertyOf =  Clazz.newIntArray (100, 0);
this.fieldOf =  Clazz.newIntArray (100, 0);
this.bondTypes =  new JU.Lst ();
this.ptOffset =  new JU.P3 ();
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.initSubclass ();
this.parser =  new JU.CifDataParser ().set (this, null);
this.allowPDBFilter = true;
this.appendedData = this.htParams.get ("appendedData");
var conf = this.getFilter ("CONF ");
if (conf != null) this.configurationPtr = this.parseIntStr (conf);
this.isMolecular = this.checkFilterKey ("MOLECUL") && !this.checkFilterKey ("BIOMOLECULE");
this.readIdeal = !this.checkFilterKey ("NOIDEAL");
this.filterAssembly = this.checkFilterKey ("$");
if (this.isMolecular) {
if (!this.doApplySymmetry) {
this.doApplySymmetry = true;
this.latticeCells[0] = 1;
this.latticeCells[1] = 1;
this.latticeCells[2] = 1;
}this.molecularType = "filter \"MOLECULAR\"";
}this.checkSpecial = !this.checkFilterKey ("NOSPECIAL");
this.asc.setCheckSpecial (this.checkSpecial);
this.allowRotations = !this.checkFilterKey ("NOSYM");
if (this.altCell != null && this.altCell.indexOf (",") >= 0) this.addCellType ("conventional", this.altCell, true);
this.readCifData ();
this.continuing = false;
});
Clazz.defineMethod (c$, "initSubclass", 
function () {
});
Clazz.defineMethod (c$, "readCifData", 
 function () {
this.line = "";
while ((this.key = this.parser.peekToken ()) != null) if (!this.readAllData ()) break;

if (this.appendedData != null) {
this.parser = JU.Rdr.getCifParser ().set (null, JU.Rdr.getBR (this.appendedData));
while ((this.key = this.parser.peekToken ()) != null) if (!this.readAllData ()) break;

}});
Clazz.defineMethod (c$, "readAllData", 
 function () {
if (this.key.startsWith ("data_")) {
this.newModel (++this.modelNumber);
if (this.iHaveDesiredModel) return false;
this.haveCellWaveVector = false;
if (this.auditBlockCode == null) this.modulated = false;
if (!this.skipping) {
this.nAtoms0 = this.asc.ac;
this.processDataParameter ();
this.nAtoms = this.asc.ac;
}return true;
}if (this.skipping && this.key.equals ("_audit_block_code")) {
this.iHaveDesiredModel = false;
this.skipping = false;
}if (this.key.startsWith ("loop_")) {
if (this.skipping) {
this.parser.getTokenPeeked ();
this.parser.skipLoop ();
} else {
this.processLoopBlock ();
}return true;
}if (this.key.indexOf ("_") != 0) {
if (this.key.equals ("DSSR:")) this.processDSSR (this, this.htGroup1);
 else JU.Logger.warn ("CIF ERROR ? should be an underscore: " + this.key);
this.parser.getTokenPeeked ();
} else if (!this.getData ()) {
return true;
}if (!this.skipping) {
this.key = this.fixKey (this.key);
if (this.key.startsWith ("_chemical_name") || this.key.equals ("_chem_comp_name")) {
this.processChemicalInfo ("name");
} else if (this.key.startsWith ("_chemical_formula_structural")) {
this.processChemicalInfo ("structuralFormula");
} else if (this.key.startsWith ("_chemical_formula_sum") || this.key.equals ("_chem_comp_formula")) {
this.processChemicalInfo ("formula");
} else if (this.key.equals ("_cell_modulation_dimension")) {
this.modDim = this.parseIntStr (this.data);
} else if (this.key.startsWith ("_cell_")) {
this.processCellParameter ();
} else if (this.key.startsWith ("_atom_sites_fract_tran")) {
this.processUnitCellTransformMatrix ();
} else if (this.key.equals ("_audit_block_code")) {
this.auditBlockCode = this.parser.fullTrim (this.data).toUpperCase ();
this.appendLoadNote (this.auditBlockCode);
if (this.htAudit != null && this.auditBlockCode.contains ("_MOD_")) {
var key = JU.PT.rep (this.auditBlockCode, "_MOD_", "_REFRNCE_");
if (this.asc.setSymmetry (this.htAudit.get (key)) != null) {
this.notionalUnitCell = this.asc.getSymmetry ().getNotionalUnitCell ();
this.iHaveUnitCell = true;
}} else if (this.htAudit != null) {
if (this.symops != null) for (var i = 0; i < this.symops.size (); i++) this.setSymmetryOperator (this.symops.get (i));

}if (this.lastSpaceGroupName != null) this.setSpaceGroupName (this.lastSpaceGroupName);
} else if (this.key.equals (J.adapter.readers.cif.CifReader.singleAtomID)) {
this.readSingleAtom ();
} else if (this.key.startsWith ("_symmetry_space_group_name_h-m") || this.key.startsWith ("_symmetry_space_group_name_hall") || this.key.startsWith ("_space_group_name") || this.key.contains ("_ssg_name") || this.key.contains ("_magn_name") || this.key.contains ("_bns_name")) {
this.processSymmetrySpaceGroupName ();
} else if (this.key.startsWith ("_space_group_transform")) {
this.processUnitCellTransform ();
} else if ("__citation_title__publ_section_title__active_magnetic_irreps_details__".contains ("_" + this.key + "__")) {
this.appendLoadNote ("TITLE: " + this.parser.fullTrim (this.data) + "\n");
} else {
this.processSubclassEntry ();
}}return true;
});
Clazz.defineMethod (c$, "processSubclassEntry", 
function () {
if (this.modDim > 0) this.getModulationReader ().processEntry ();
});
Clazz.defineMethod (c$, "processUnitCellTransform", 
 function () {
this.data = JU.PT.replaceAllCharacters (this.data, " ", "");
if (this.key.contains ("_from_parent")) this.addCellType ("parent", this.data, true);
 else if (this.key.contains ("_to_standard")) this.addCellType ("standard", this.data, false);
this.appendLoadNote (this.key + ": " + this.data);
});
Clazz.defineMethod (c$, "addCellType", 
 function (type, data, isFrom) {
if (this.htCellTypes == null) this.htCellTypes =  new java.util.Hashtable ();
if (data.startsWith ("!")) {
data = data.substring (1);
isFrom = !isFrom;
}var cell = (isFrom ? "!" : "") + data;
this.htCellTypes.put (type, cell);
if (type.equalsIgnoreCase (this.altCell)) {
this.altCell = cell;
this.htCellTypes.put ("conventional", (isFrom ? "" : "!") + data);
}}, "~S,~S,~B");
Clazz.defineMethod (c$, "readSingleAtom", 
 function () {
var atom =  new J.adapter.smarter.Atom ();
atom.set (0, 0, 0);
var s = atom.atomName = this.parser.fullTrim (this.data);
atom.elementSymbol = s.length == 1 ? s : s.substring (0, 1) + s.substring (1, 2).toLowerCase ();
this.asc.addAtom (atom);
});
Clazz.defineMethod (c$, "getModulationReader", 
 function () {
return (this.mr == null ? this.initializeMSCIF () : this.mr);
});
Clazz.defineMethod (c$, "initializeMSCIF", 
 function () {
if (this.mr == null) this.ms = this.mr = J.api.Interface.getOption ("adapter.readers.cif.MSCifRdr");
this.modulated = (this.mr.initialize (this, this.modDim) > 0);
return this.mr;
});
Clazz.defineMethod (c$, "fixKey", 
 function (key) {
key = JU.PT.rep (key, ".", "_").toLowerCase ();
if (key.startsWith ("_magnetic")) {
key = key.substring (9);
}return key;
}, "~S");
Clazz.defineMethod (c$, "newModel", 
function (modelNo) {
this.skipping = !this.doGetModel (this.modelNumber = modelNo, null);
if (this.skipping) {
this.parser.getTokenPeeked ();
return;
}this.chemicalName = "";
this.thisStructuralFormula = "";
this.thisFormula = "";
this.iHaveDesiredModel = this.isLastModel (this.modelNumber);
if (this.isCourseGrained) this.asc.setAtomSetAuxiliaryInfo ("courseGrained", Boolean.TRUE);
if (this.nAtoms0 == this.asc.ac) {
this.modelNumber--;
this.haveModel = false;
this.asc.removeCurrentAtomSet ();
} else {
this.applySymmetryAndSetTrajectory ();
}}, "~N");
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
if (this.isMMCIF) this.finalizeSubclass ();
 else this.applySymmetryAndSetTrajectory ();
var n = this.asc.atomSetCount;
if (n > 1) this.asc.setCollectionName ("<collection of " + n + " models>");
this.finalizeReaderASCR ();
var header = this.parser.getFileHeader ();
if (header.length > 0) {
var s = this.setLoadNote ();
this.appendLoadNote (null);
this.appendLoadNote (header);
this.appendLoadNote (s);
this.setLoadNote ();
this.asc.setInfo ("fileHeader", header);
}if (this.haveAromatic) this.addJmolScript ("calculate aromatic");
});
Clazz.defineMethod (c$, "finalizeSubclass", 
function () {
});
Clazz.overrideMethod (c$, "doPreSymmetry", 
function () {
if (this.centerings != null) this.addLatticeVectors ();
if (this.modDim > 0) this.getModulationReader ().setModulation (false);
if (this.isMagCIF) {
this.vibsFractional = true;
var params = this.asc.xtalSymmetry.symmetry.getNotionalUnitCell ();
var ptScale = JU.P3.new3 (1 / params[0], 1 / params[1], 1 / params[2]);
for (var i = this.asc.ac; --i >= this.nAtoms0; ) if (this.asc.atoms[i].vib != null) {
this.asc.atoms[i].vib.scaleT (ptScale);
}
}});
Clazz.overrideMethod (c$, "applySymmetryAndSetTrajectory", 
function () {
if (this.isMMCIF) this.asc.setCheckSpecial (false);
var doCheckBonding = this.doCheckUnitCell && !this.isMMCIF;
if (this.isMMCIF) {
var modelIndex = this.asc.iSet;
this.asc.setAtomSetAuxiliaryInfo ("PDB_CONECT_firstAtom_count_max", [this.asc.getAtomSetAtomIndex (modelIndex), this.asc.getAtomSetAtomCount (modelIndex), this.maxSerial]);
}if (this.htCellTypes != null) {
for (var e, $e = this.htCellTypes.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) this.asc.setAtomSetAuxiliaryInfo ("unitcell_" + e.getKey (), e.getValue ());

this.htCellTypes = null;
}if (!this.haveCellWaveVector) this.modDim = 0;
var sym = this.applySymTrajASCR ();
if (this.modDim > 0) {
this.addLatticeVectors ();
this.asc.setTensors ();
this.getModulationReader ().setModulation (true);
this.mr.finalizeModulation ();
}if (this.isMagCIF && sym != null) {
this.addJmolScript ("connect none;vectors on;vectors 0.15;");
var n = this.asc.getXSymmetry ().setVibVectors ();
this.appendLoadNote (n + " magnetic moments - use VECTORS ON/OFF or VECTOR SCALE x.x or SELECT VXYZ>0");
}if (this.auditBlockCode != null && this.auditBlockCode.contains ("REFRNCE") && sym != null) {
if (this.htAudit == null) this.htAudit =  new java.util.Hashtable ();
this.htAudit.put (this.auditBlockCode, sym);
}if (doCheckBonding && (this.bondTypes.size () > 0 || this.isMolecular)) this.setBondingAndMolecules ();
this.asc.setAtomSetAuxiliaryInfo ("fileHasUnitCell", Boolean.TRUE);
this.asc.xtalSymmetry = null;
});
Clazz.defineMethod (c$, "processDataParameter", 
 function () {
this.bondTypes.clear ();
this.parser.getTokenPeeked ();
this.thisDataSetName = (this.key.length < 6 ? "" : this.key.substring (5));
if (this.thisDataSetName.length > 0) this.nextAtomSet ();
if (JU.Logger.debugging) JU.Logger.debug (this.key);
});
Clazz.defineMethod (c$, "nextAtomSet", 
 function () {
this.asc.setAtomSetAuxiliaryInfo ("isCIF", Boolean.TRUE);
if (this.asc.iSet >= 0) {
this.asc.newAtomSet ();
if (this.isMMCIF) this.setModelPDB (true);
} else {
this.asc.setCollectionName (this.thisDataSetName);
}});
Clazz.defineMethod (c$, "processChemicalInfo", 
 function (type) {
if (type.equals ("name")) {
this.chemicalName = this.data = this.parser.fullTrim (this.data);
if (!this.data.equals ("?")) this.asc.setInfo ("modelLoadNote", this.data);
} else if (type.equals ("structuralFormula")) {
this.thisStructuralFormula = this.data = this.parser.fullTrim (this.data);
} else if (type.equals ("formula")) {
this.thisFormula = this.data = this.parser.fullTrim (this.data);
}if (JU.Logger.debugging) {
JU.Logger.debug (type + " = " + this.data);
}return this.data;
}, "~S");
Clazz.defineMethod (c$, "processSymmetrySpaceGroupName", 
 function () {
if (this.key.indexOf ("_ssg_name") >= 0) {
this.modulated = true;
this.latticeType = this.data.substring (0, 1);
} else if (this.modulated) {
return;
}this.data = this.parser.toUnicode (this.data);
this.setSpaceGroupName (this.lastSpaceGroupName = (this.key.indexOf ("h-m") > 0 ? "HM:" : this.key.indexOf ("bns") >= 0 ? "BNS:" : this.modulated ? "SSG:" : this.key.indexOf ("hall") >= 0 ? "Hall:" : "") + this.data);
});
Clazz.defineMethod (c$, "addLatticeVectors", 
 function () {
this.lattvecs = null;
if (this.centerings != null) {
this.latticeType = "Magnetic";
this.lattvecs =  new JU.Lst ();
for (var i = 0; i < this.centerings.size (); i++) {
var s = this.centerings.get (i);
var f =  Clazz.newFloatArray (5, 0);
f[4] = NaN;
var tokens = JU.PT.split (JU.PT.replaceAllCharacters (s, "xyz+", ""), ",");
var n = 0;
for (var j = 0; j < tokens.length; j++) {
s = tokens[j].trim ();
if (s.length == 0) continue;
var pt = s.indexOf ("/");
if (pt < 0) {
pt = s.length;
s += "/1";
}if ((f[j] = JU.PT.parseFloat (s.substring (0, pt)) / JU.PT.parseFloat (s.substring (pt + 1))) != 0) n++;
}
if (n >= 2) this.lattvecs.addLast (f);
}
this.centerings = null;
} else if (this.latticeType != null && "ABCFI".indexOf (this.latticeType) >= 0) {
this.lattvecs =  new JU.Lst ();
try {
this.ms.addLatticeVector (this.lattvecs, this.latticeType);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}if (this.lattvecs != null && this.lattvecs.size () > 0 && this.asc.getSymmetry ().addLatticeVectors (this.lattvecs)) this.appendLoadNote ("Note! Symmetry operators added for lattice centering " + this.latticeType);
this.latticeType = null;
});
Clazz.defineMethod (c$, "processCellParameter", 
 function () {
for (var i = J.api.JmolAdapter.cellParamNames.length; --i >= 0; ) if (this.key.equals (J.api.JmolAdapter.cellParamNames[i])) {
this.setUnitCellItem (i, this.parseFloatStr (this.data));
return;
}
});
Clazz.defineMethod (c$, "processUnitCellTransformMatrix", 
 function () {
var v = this.parseFloatStr (this.data);
if (Float.isNaN (v)) return;
for (var i = 0; i < J.adapter.readers.cif.CifReader.TransformFields.length; i++) {
if (this.key.indexOf (J.adapter.readers.cif.CifReader.TransformFields[i]) >= 0) {
this.setUnitCellItem (6 + i, v);
return;
}}
});
Clazz.defineMethod (c$, "getData", 
 function () {
this.key = this.parser.getTokenPeeked ();
this.data = this.parser.getNextToken ();
if (this.data == null) {
JU.Logger.warn ("CIF ERROR ? end of file; data missing: " + this.key);
return false;
}return (this.data.length == 0 || this.data.charAt (0) != '\0');
});
Clazz.defineMethod (c$, "processLoopBlock", 
 function () {
this.parser.getTokenPeeked ();
this.key = this.parser.peekToken ();
if (this.key == null) return;
var isLigand = false;
this.key = this.fixKey (this.key);
if (this.modDim > 0) switch (this.getModulationReader ().processLoopBlock ()) {
case 0:
break;
case -1:
this.parser.skipLoop ();
case 1:
return;
}
if (this.key.startsWith ("_atom_site_") || (isLigand = this.key.equals ("_chem_comp_atom_comp_id"))) {
if (!this.processAtomSiteLoopBlock (isLigand)) return;
this.asc.setAtomSetName (this.thisDataSetName);
this.asc.setAtomSetAuxiliaryInfo ("chemicalName", this.chemicalName);
this.asc.setAtomSetAuxiliaryInfo ("structuralFormula", this.thisStructuralFormula);
this.asc.setAtomSetAuxiliaryInfo ("formula", this.thisFormula);
return;
}if (this.key.startsWith ("_symmetry_equiv_pos") || this.key.startsWith ("_space_group_symop") || this.key.startsWith ("_symmetry_ssg_equiv")) {
if (this.ignoreFileSymmetryOperators) {
JU.Logger.warn ("ignoring file-based symmetry operators");
this.parser.skipLoop ();
} else {
this.processSymmetryOperationsLoopBlock ();
}return;
}if (this.key.startsWith ("_citation")) {
this.processCitationListBlock ();
return;
}if (this.key.startsWith ("_atom_type")) {
this.processAtomTypeLoopBlock ();
return;
}if (this.key.startsWith ("_geom_bond") && (this.isMolecular || !this.doApplySymmetry)) {
this.processGeomBondLoopBlock ();
return;
}if (this.processSubclassLoopBlock ()) return;
if (this.key.equals ("_propagation_vector_seq_id")) {
this.addMore ();
return;
}this.parser.skipLoop ();
});
Clazz.defineMethod (c$, "processSubclassLoopBlock", 
function () {
return false;
});
Clazz.defineMethod (c$, "addMore", 
 function () {
var str;
var n = 0;
try {
while ((str = this.parser.peekToken ()) != null && str.charAt (0) == '_') {
this.parser.getTokenPeeked ();
n++;
}
var m = 0;
var s = "";
while ((str = this.parser.getNextDataToken ()) != null) {
s += str + (m % n == 0 ? "=" : " ");
if (++m % n == 0) {
this.appendUunitCellInfo (s.trim ());
s = "";
}}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
});
Clazz.defineMethod (c$, "fieldProperty", 
function (i) {
return (i >= 0 && (this.field = this.parser.getLoopData (i)).length > 0 && (this.firstChar = this.field.charAt (0)) != '\0' ? this.propertyOf[i] : -1);
}, "~N");
Clazz.defineMethod (c$, "parseLoopParameters", 
function (fields) {
this.propertyCount = this.parser.parseLoopParameters (fields, this.fieldOf, this.propertyOf);
}, "~A");
Clazz.defineMethod (c$, "disableField", 
 function (fieldIndex) {
var i = this.fieldOf[fieldIndex];
if (i != -1) this.propertyOf[i] = -1;
}, "~N");
Clazz.defineMethod (c$, "processAtomTypeLoopBlock", 
 function () {
this.parseLoopParameters (J.adapter.readers.cif.CifReader.atomTypeFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] == -1) {
this.parser.skipLoop ();
return;
}
while (this.parser.getData ()) {
var atomTypeSymbol = null;
var oxidationNumber = NaN;
var n = this.parser.getFieldCount ();
for (var i = 0; i < n; ++i) {
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
});
Clazz.defineMethod (c$, "processAtomSiteLoopBlock", 
function (isLigand) {
var currentModelNo = -1;
var haveCoord = true;
this.parseLoopParameters (J.adapter.readers.cif.CifReader.atomFields);
if (this.fieldOf[55] != -1) {
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
} else if (this.fieldOf[20] != -1 || this.fieldOf[21] != -1 || this.fieldOf[63] != -1) {
haveCoord = false;
} else {
this.parser.skipLoop ();
return false;
}var modelField = this.fieldOf[17];
var siteMult = 0;
while (this.parser.getData ()) {
if (this.isMMCIF) {
if (modelField >= 0) {
this.fieldProperty (modelField);
var modelNo = this.parseIntStr (this.field);
if (modelNo != currentModelNo) {
if (this.iHaveDesiredModel) {
this.parser.skipLoop ();
this.skipping = false;
this.continuing = true;
break;
}currentModelNo = modelNo;
this.newModel (modelNo);
if (!this.skipping) {
this.nextAtomSet ();
if (this.modelMap == null || this.asc.ac == 0) this.modelMap =  new java.util.Hashtable ();
this.modelMap.put ("" + modelNo, Integer.$valueOf (Math.max (0, this.asc.iSet)));
this.modelMap.put ("_" + Math.max (0, this.asc.iSet), Integer.$valueOf (modelNo));
}}if (this.skipping) continue;
}}var atom = null;
if (haveCoord) {
atom =  new J.adapter.smarter.Atom ();
} else {
if (this.fieldProperty (this.fieldOf[20]) != -1 || this.fieldProperty (this.fieldOf[63]) != -1 || this.fieldProperty (this.fieldOf[21]) != -1) {
if ((atom = this.asc.getAtomFromName (this.field)) == null) continue;
} else {
continue;
}}var assemblyId = null;
var strChain = null;
var id = null;
var seqID = 0;
var n = this.parser.getFieldCount ();
for (var i = 0; i < n; ++i) {
var tok = this.fieldProperty (i);
switch (tok) {
case -1:
break;
case 71:
seqID = this.parseIntStr (this.field);
break;
case 70:
id = this.field;
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
if (Math.abs (atom.formalCharge - charge) > 0.1) if (JU.Logger.debugging) {
JU.Logger.debug ("CIF charge on " + this.field + " was " + charge + "; rounded to " + atom.formalCharge);
}}break;
case 49:
case 1:
case 2:
atom.atomName = this.field;
break;
case 55:
var x = this.parseFloatStr (this.field);
if (this.readIdeal && !Float.isNaN (x)) atom.x = x;
break;
case 56:
var y = this.parseFloatStr (this.field);
if (this.readIdeal && !Float.isNaN (y)) atom.y = y;
break;
case 57:
var z = this.parseFloatStr (this.field);
if (this.readIdeal && !Float.isNaN (z)) atom.z = z;
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
if (!Float.isNaN (floatOccupancy)) atom.foccupancy = floatOccupancy;
break;
case 10:
atom.bfactor = this.parseFloatStr (this.field) * (this.isMMCIF ? 1 : 100);
break;
case 48:
case 11:
atom.group3 = this.field;
break;
case 59:
assemblyId = this.field;
break;
case 12:
atom.chainID = this.vwr.getChainID (strChain = this.field);
break;
case 13:
this.maxSerial = Math.max (this.maxSerial, atom.sequenceNumber = this.parseIntStr (this.field));
break;
case 14:
atom.insertionCode = this.firstChar;
break;
case 15:
case 60:
atom.altLoc = this.firstChar;
break;
case 58:
this.disorderAssembly = this.field;
break;
case 19:
if (this.firstChar == '-' && this.field.length > 1) {
atom.altLoc = this.field.charAt (1);
atom.ignoreSymmetry = true;
} else {
atom.altLoc = this.firstChar;
}break;
case 16:
if ("HETATM".equals (this.field)) atom.isHetero = true;
break;
case 18:
if ("dum".equals (this.field)) {
atom.x = NaN;
continue;
}break;
case 61:
if (this.modulated) siteMult = this.parseIntStr (this.field);
break;
case 62:
case 47:
if (this.field.equalsIgnoreCase ("Uiso")) {
var j = this.fieldOf[34];
if (j != -1) this.asc.setU (atom, 7, this.parseFloatStr (this.parser.getLoopData (j)));
}break;
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
this.asc.setU (atom, (this.propertyOf[i] - 22) % 6, this.parseFloatStr (this.field));
break;
case 35:
case 36:
case 37:
case 38:
case 39:
case 40:
this.asc.setU (atom, 6, 4);
this.asc.setU (atom, (this.propertyOf[i] - 35) % 6, this.parseFloatStr (this.field));
break;
case 41:
case 42:
case 43:
case 44:
case 45:
case 46:
this.asc.setU (atom, 6, 0);
this.asc.setU (atom, (this.propertyOf[i] - 41) % 6, this.parseFloatStr (this.field));
break;
case 64:
case 65:
case 66:
case 67:
case 68:
case 69:
this.isMagCIF = true;
var pt = atom.vib;
if (pt == null) pt = this.asc.addVibrationVector (atom.index, 0, 0, 0);
var v = this.parseFloatStr (this.field);
switch (tok) {
case 64:
case 67:
pt.x = v;
this.appendLoadNote ("magnetic moment: " + this.line);
break;
case 65:
case 68:
pt.y = v;
break;
case 66:
case 69:
pt.z = v;
break;
}
break;
}
}
if (!haveCoord) continue;
if (Float.isNaN (atom.x) || Float.isNaN (atom.y) || Float.isNaN (atom.z)) {
JU.Logger.warn ("atom " + atom.atomName + " has invalid/unknown coordinates");
continue;
}if (atom.elementSymbol == null && atom.atomName != null) {
var sym = atom.atomName;
var pt = 0;
while (pt < sym.length && Character.isLetter (sym.charAt (pt))) pt++;

atom.elementSymbol = (pt == 0 || pt > 2 ? "Xx" : sym.substring (0, pt));
}if (!this.filterCIFAtom (atom, assemblyId)) continue;
this.setAtomCoord (atom);
if (this.isMMCIF && !this.processSubclassAtom (atom, assemblyId, strChain)) continue;
this.asc.addAtomWithMappedName (atom);
if (id != null) {
this.asc.atomSymbolicMap.put (id, atom);
if (seqID > 0) {
var pt = atom.vib;
if (pt == null) pt = this.asc.addVibrationVector (atom.index, 0, NaN, 1095761940);
pt.x = seqID;
}}this.ac++;
if (this.modDim > 0 && siteMult != 0) atom.vib = JU.V3.new3 (siteMult, 0, NaN);
}
this.asc.setAtomSetAuxiliaryInfo ("isCIF", Boolean.TRUE);
if (this.isMMCIF) this.setModelPDB (true);
if (this.isMMCIF && this.skipping) this.skipping = false;
return true;
}, "~B");
Clazz.defineMethod (c$, "processSubclassAtom", 
function (atom, assemblyId, strChain) {
return false;
}, "J.adapter.smarter.Atom,~S,~S");
Clazz.defineMethod (c$, "filterCIFAtom", 
function (atom, assemblyId) {
if (!this.filterAtom (atom, -1)) return false;
if (this.filterAssembly && this.filterReject (this.filter, "$", assemblyId)) return false;
if (this.configurationPtr > 0) {
if (!this.disorderAssembly.equals (this.lastDisorderAssembly)) {
this.lastDisorderAssembly = this.disorderAssembly;
this.lastAltLoc = '\0';
this.conformationIndex = this.configurationPtr;
}if (atom.altLoc != '\0') {
if (this.conformationIndex >= 0 && atom.altLoc != this.lastAltLoc) {
this.lastAltLoc = atom.altLoc;
this.conformationIndex--;
}if (this.conformationIndex != 0) {
JU.Logger.info ("ignoring " + atom.atomName);
return false;
}}}return true;
}, "J.adapter.smarter.Atom,~S");
Clazz.defineMethod (c$, "processCitationListBlock", 
 function () {
this.parseLoopParameters (J.adapter.readers.cif.CifReader.citationFields);
var m =  Clazz.newFloatArray (16, 0);
m[15] = 1;
while (this.parser.getData ()) {
var n = this.parser.getFieldCount ();
for (var i = 0; i < n; ++i) {
switch (this.fieldProperty (i)) {
case 0:
break;
case 1:
this.appendLoadNote ("TITLE: " + this.parser.toUnicode (this.field));
break;
}
}
}
});
Clazz.defineMethod (c$, "processSymmetryOperationsLoopBlock", 
 function () {
if (this.key.endsWith ("magn_centering_id")) {
this.processMagCenteringLoopBlock ();
return;
}this.parseLoopParameters (J.adapter.readers.cif.CifReader.symmetryOperationsFields);
var nRefs = 0;
this.symops =  new JU.Lst ();
for (var i = this.propertyCount; --i >= 0; ) if (this.fieldOf[i] != -1) nRefs++;

if (nRefs == 0) {
JU.Logger.warn ("?que? _symmetry_equiv or _space_group_symop property not found");
this.parser.skipLoop ();
return;
}var n = 0;
while (this.parser.getData ()) {
var ssgop = false;
var nn = this.parser.getFieldCount ();
var timeRev = (this.fieldProperty (this.fieldOf[5]) == -1 ? 0 : this.field.equals ("-1") ? -1 : 1);
for (var i = 0, tok; i < nn; ++i) {
switch (tok = this.fieldProperty (i)) {
case 2:
if (this.field.indexOf ('~') >= 0) this.field = JU.PT.rep (this.field, "~", "");
case 3:
this.modulated = true;
ssgop = true;
case 0:
case 1:
case 4:
if (this.allowRotations || timeRev != 0 || ++n == 1) if (!this.modulated || ssgop) {
if (tok == 4) {
timeRev = (this.field.endsWith (",+1") ? 1 : this.field.endsWith (",-1") ? -1 : 0);
if (timeRev != 0) this.field = this.field.substring (0, this.field.length - 3);
}if (timeRev != 0) this.field += ",m" + (timeRev == 1 ? "+1" : "-1");
this.field = this.field.$replace (';', ' ');
this.symops.addLast (this.field);
this.setSymmetryOperator (this.field);
}break;
}
}
}
if (this.ms != null) this.addLatticeVectors ();
});
Clazz.defineMethod (c$, "processMagCenteringLoopBlock", 
 function () {
this.parseLoopParameters (J.adapter.readers.cif.CifReader.magnCenteringFields);
this.centerings =  new JU.Lst ();
while (this.parser.getData ()) this.centerings.addLast (this.fieldProperty (this.fieldOf[0]) == -1 ? null : this.field);

});
Clazz.defineMethod (c$, "getBondOrder", 
function (field) {
switch (field.charAt (0)) {
default:
JU.Logger.warn ("unknown CIF bond order: " + field);
case 'S':
return 1;
case 'D':
return 2;
case 'T':
return 3;
case 'A':
this.haveAromatic = true;
return 515;
}
}, "~S");
Clazz.defineMethod (c$, "processGeomBondLoopBlock", 
 function () {
this.parseLoopParameters (J.adapter.readers.cif.CifReader.geomBondFields);
for (var i = this.propertyCount; --i >= 0; ) if (this.propertyOf[i] != 3 && this.fieldOf[i] == -1) {
JU.Logger.warn ("?que? missing property: " + J.adapter.readers.cif.CifReader.geomBondFields[i]);
this.parser.skipLoop ();
return;
}
var name1 = null;
var name2 = null;
var order = Integer.$valueOf (1);
var bondCount = 0;
while (this.parser.getData ()) {
var atomIndex1 = -1;
var atomIndex2 = -1;
var distance = 0;
var dx = 0;
var nn = this.parser.getFieldCount ();
for (var i = 0; i < nn; ++i) {
switch (this.fieldProperty (i)) {
case -1:
break;
case 0:
atomIndex1 = this.asc.getAtomIndex (name1 = this.field);
break;
case 1:
atomIndex2 = this.asc.getAtomIndex (name2 = this.field);
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
JU.Logger.info ("error reading uncertainty for " + this.line);
dx = 0.015;
}} else {
dx = 0.015;
}break;
case 3:
order = Integer.$valueOf (this.getBondOrder (this.field));
break;
}
}
if (atomIndex1 < 0 || atomIndex2 < 0 || distance == 0) continue;
bondCount++;
this.bondTypes.addLast ([name1, name2, Float.$valueOf (distance), Float.$valueOf (dx), order]);
}
if (bondCount > 0) {
JU.Logger.info (bondCount + " bonds read");
if (!this.doApplySymmetry) {
this.isMolecular = true;
this.doApplySymmetry = true;
this.latticeCells[0] = this.latticeCells[1] = this.latticeCells[2] = 1;
}}});
Clazz.defineMethod (c$, "setBondingAndMolecules", 
 function () {
JU.Logger.info ("CIF creating molecule " + (this.bondTypes.size () > 0 ? " using GEOM_BOND records" : ""));
this.atoms = this.asc.atoms;
this.firstAtom = this.asc.getLastAtomSetAtomIndex ();
var nat = this.asc.getLastAtomSetAtomCount ();
this.ac = this.firstAtom + nat;
this.bsSets =  new Array (nat);
this.symmetry = this.asc.getSymmetry ();
for (var i = this.firstAtom; i < this.ac; i++) {
var ipt = this.asc.getAtomFromName (this.atoms[i].atomName).index - this.firstAtom;
if (this.bsSets[ipt] == null) this.bsSets[ipt] =  new JU.BS ();
this.bsSets[ipt].set (i - this.firstAtom);
}
if (this.isMolecular) {
this.atomRadius =  Clazz.newFloatArray (this.ac, 0);
for (var i = this.firstAtom; i < this.ac; i++) {
var elemnoWithIsotope = J.api.JmolAdapter.getElementNumber (this.atoms[i].getElementSymbol ());
this.atoms[i].elementNumber = elemnoWithIsotope;
var charge = (this.atoms[i].formalCharge == -2147483648 ? 0 : this.atoms[i].formalCharge);
if (elemnoWithIsotope > 0) this.atomRadius[i] = J.api.JmolAdapter.getBondingRadius (elemnoWithIsotope, charge);
}
this.bsConnected =  new Array (this.ac);
for (var i = this.firstAtom; i < this.ac; i++) this.bsConnected[i] =  new JU.BS ();

this.bsMolecule =  new JU.BS ();
this.bsExclude =  new JU.BS ();
}var isFirst = true;
while (this.createBonds (isFirst)) {
isFirst = false;
}
if (this.isMolecular) {
if (this.asc.bsAtoms == null) this.asc.bsAtoms =  new JU.BS ();
this.asc.bsAtoms.clearBits (this.firstAtom, this.ac);
this.asc.bsAtoms.or (this.bsMolecule);
this.asc.bsAtoms.andNot (this.bsExclude);
for (var i = this.firstAtom; i < this.ac; i++) {
if (this.asc.bsAtoms.get (i)) this.symmetry.toCartesian (this.atoms[i], true);
 else if (JU.Logger.debugging) JU.Logger.debug (this.molecularType + " removing " + i + " " + this.atoms[i].atomName + " " + this.atoms[i]);
}
this.asc.setAtomSetAuxiliaryInfo ("notionalUnitcell", null);
if (this.nMolecular++ == this.asc.iSet) {
this.asc.clearGlobalBoolean (0);
this.asc.clearGlobalBoolean (1);
this.asc.clearGlobalBoolean (2);
}}if (this.bondTypes.size () > 0) this.asc.setAtomSetAuxiliaryInfo ("hasBonds", Boolean.TRUE);
this.bondTypes.clear ();
this.atomRadius = null;
this.bsSets = null;
this.bsConnected = null;
this.bsMolecule = null;
this.bsExclude = null;
});
Clazz.defineMethod (c$, "createBonds", 
 function (doInit) {
for (var i = this.bondTypes.size (); --i >= 0; ) {
var o = this.bondTypes.get (i);
var distance = (o[2]).floatValue ();
var dx = (o[3]).floatValue ();
var order = (o[4]).intValue ();
var iatom1 = this.asc.getAtomIndex (o[0]);
var iatom2 = this.asc.getAtomIndex (o[1]);
var bs1 = this.bsSets[iatom1 - this.firstAtom];
var bs2 = this.bsSets[iatom2 - this.firstAtom];
if (bs1 == null || bs2 == null) continue;
for (var j = bs1.nextSetBit (0); j >= 0; j = bs1.nextSetBit (j + 1)) for (var k = bs2.nextSetBit (0); k >= 0; k = bs2.nextSetBit (k + 1)) {
if ((!this.isMolecular || !this.bsConnected[j + this.firstAtom].get (k)) && this.symmetry.checkDistance (this.atoms[j + this.firstAtom], this.atoms[k + this.firstAtom], distance, dx, 0, 0, 0, this.ptOffset)) this.addNewBond (j + this.firstAtom, k + this.firstAtom, order);
}

}
if (this.bondTypes.size () > 0) for (var i = this.firstAtom; i < this.ac; i++) if (this.atoms[i].elementNumber == 1) {
var checkAltLoc = (this.atoms[i].altLoc != '\0');
for (var k = this.firstAtom; k < this.ac; k++) if (k != i && this.atoms[k].elementNumber != 1 && (!checkAltLoc || this.atoms[k].altLoc == '\0' || this.atoms[k].altLoc == this.atoms[i].altLoc)) {
if (!this.bsConnected[i].get (k) && this.symmetry.checkDistance (this.atoms[i], this.atoms[k], 1.1, 0, 0, 0, 0, this.ptOffset)) this.addNewBond (i, k, 1);
}
}
if (!this.isMolecular) return false;
if (doInit) for (var i = this.firstAtom; i < this.ac; i++) if (this.atoms[i].atomSite + this.firstAtom == i && !this.bsMolecule.get (i)) this.setBs (this.atoms, i, this.bsConnected, this.bsMolecule);

var bondTolerance = this.vwr.getFloat (570425348);
var bsBranch =  new JU.BS ();
var cart1 =  new JU.P3 ();
var cart2 =  new JU.P3 ();
var nFactor = 2;
for (var i = this.firstAtom; i < this.ac; i++) if (!this.bsMolecule.get (i) && !this.bsExclude.get (i)) for (var j = this.bsMolecule.nextSetBit (0); j >= 0; j = this.bsMolecule.nextSetBit (j + 1)) if (this.symmetry.checkDistance (this.atoms[j], this.atoms[i], this.atomRadius[i] + this.atomRadius[j] + bondTolerance, 0, nFactor, nFactor, nFactor, this.ptOffset)) {
this.setBs (this.atoms, i, this.bsConnected, bsBranch);
for (var k = bsBranch.nextSetBit (0); k >= 0; k = bsBranch.nextSetBit (k + 1)) {
this.atoms[k].add (this.ptOffset);
cart1.setT (this.atoms[k]);
this.symmetry.toCartesian (cart1, true);
var bs = this.bsSets[this.asc.getAtomIndex (this.atoms[k].atomName) - this.firstAtom];
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
}, "~B");
Clazz.defineMethod (c$, "addNewBond", 
 function (i, j, order) {
this.asc.addNewBondWithOrder (i, j, order);
if (!this.isMolecular) return;
this.bsConnected[i].set (j);
this.bsConnected[j].set (i);
}, "~N,~N,~N");
Clazz.defineMethod (c$, "setBs", 
 function (atoms, iatom, bsBonds, bs) {
var bsBond = bsBonds[iatom];
bs.set (iatom);
for (var i = bsBond.nextSetBit (0); i >= 0; i = bsBond.nextSetBit (i + 1)) {
if (!bs.get (i)) this.setBs (atoms, i, bsBonds, bs);
}
}, "~A,~N,~A,JU.BS");
Clazz.defineStatics (c$,
"titleRecords", "__citation_title__publ_section_title__active_magnetic_irreps_details__",
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
"AUTH_SEQ_ID", 13,
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
"ANISO_BETA_11", 41,
"ANISO_BETA_22", 42,
"ANISO_BETA_33", 43,
"ANISO_BETA_12", 44,
"ANISO_BETA_13", 45,
"ANISO_BETA_23", 46,
"ADP_TYPE", 47,
"CC_COMP_ID", 48,
"CC_ATOM_ID", 49,
"CC_ATOM_SYM", 50,
"CC_ATOM_CHARGE", 51,
"CC_ATOM_X", 52,
"CC_ATOM_Y", 53,
"CC_ATOM_Z", 54,
"CC_ATOM_X_IDEAL", 55,
"CC_ATOM_Y_IDEAL", 56,
"CC_ATOM_Z_IDEAL", 57,
"DISORDER_ASSEMBLY", 58,
"ASYM_ID", 59,
"SUBSYS_ID", 60,
"SITE_MULT", 61,
"THERMAL_TYPE", 62,
"MOMENT_LABEL", 63,
"MOMENT_PRELIM_X", 64,
"MOMENT_PRELIM_Y", 65,
"MOMENT_PRELIM_Z", 66,
"MOMENT_X", 67,
"MOMENT_Y", 68,
"MOMENT_Z", 69,
"ATOM_ID", 70,
"SEQ_ID", 71,
"atomFields", ["_atom_site_type_symbol", "_atom_site_label", "_atom_site_auth_atom_id", "_atom_site_fract_x", "_atom_site_fract_y", "_atom_site_fract_z", "_atom_site_cartn_x", "_atom_site_cartn_y", "_atom_site_cartn_z", "_atom_site_occupancy", "_atom_site_b_iso_or_equiv", "_atom_site_auth_comp_id", "_atom_site_auth_asym_id", "_atom_site_auth_seq_id", "_atom_site_pdbx_pdb_ins_code", "_atom_site_label_alt_id", "_atom_site_group_pdb", "_atom_site_pdbx_pdb_model_num", "_atom_site_calc_flag", "_atom_site_disorder_group", "_atom_site_aniso_label", "_atom_site_anisotrop_id", "_atom_site_aniso_u_11", "_atom_site_aniso_u_22", "_atom_site_aniso_u_33", "_atom_site_aniso_u_12", "_atom_site_aniso_u_13", "_atom_site_aniso_u_23", "_atom_site_anisotrop_u[1][1]", "_atom_site_anisotrop_u[2][2]", "_atom_site_anisotrop_u[3][3]", "_atom_site_anisotrop_u[1][2]", "_atom_site_anisotrop_u[1][3]", "_atom_site_anisotrop_u[2][3]", "_atom_site_u_iso_or_equiv", "_atom_site_aniso_b_11", "_atom_site_aniso_b_22", "_atom_site_aniso_b_33", "_atom_site_aniso_b_12", "_atom_site_aniso_b_13", "_atom_site_aniso_b_23", "_atom_site_aniso_beta_11", "_atom_site_aniso_beta_22", "_atom_site_aniso_beta_33", "_atom_site_aniso_beta_12", "_atom_site_aniso_beta_13", "_atom_site_aniso_beta_23", "_atom_site_adp_type", "_chem_comp_atom_comp_id", "_chem_comp_atom_atom_id", "_chem_comp_atom_type_symbol", "_chem_comp_atom_charge", "_chem_comp_atom_model_cartn_x", "_chem_comp_atom_model_cartn_y", "_chem_comp_atom_model_cartn_z", "_chem_comp_atom_pdbx_model_cartn_x_ideal", "_chem_comp_atom_pdbx_model_cartn_y_ideal", "_chem_comp_atom_pdbx_model_cartn_z_ideal", "_atom_site_disorder_assembly", "_atom_site_label_asym_id", "_atom_site_subsystem_code", "_atom_site_symmetry_multiplicity", "_atom_site_thermal_displace_type", "_atom_site_moment_label", "_atom_site_moment_crystalaxis_mx", "_atom_site_moment_crystalaxis_my", "_atom_site_moment_crystalaxis_mz", "_atom_site_moment_crystalaxis_x", "_atom_site_moment_crystalaxis_y", "_atom_site_moment_crystalaxis_z", "_atom_site_id", "_atom_site_label_seq_id"]);
c$.singleAtomID = c$.prototype.singleAtomID = J.adapter.readers.cif.CifReader.atomFields[48];
Clazz.defineStatics (c$,
"CITATION_ID", 0,
"CITATION_TITLE", 1,
"citationFields", ["_citation_id", "_citation_title"],
"SYMOP_XYZ", 0,
"SYM_EQUIV_XYZ", 1,
"SYM_SSG_XYZ", 2,
"SYM_SSG_OP", 3,
"SYM_MAGN_OP", 4,
"SYM_PRELIM_REV", 5,
"symmetryOperationsFields", ["_space_group_symop_operation_xyz", "_symmetry_equiv_pos_as_xyz", "_symmetry_ssg_equiv_pos_as_xyz", "_space_group_symop_ssg_operation_algebraic", "_space_group_symop_magn_operation_xyz", "_space_group_symop_operation_timereversal"],
"MAGN_CENTERING", 0,
"magnCenteringFields", ["_space_group_symop_magn_centering_xyz"],
"GEOM_BOND_ATOM_SITE_LABEL_1", 0,
"GEOM_BOND_ATOM_SITE_LABEL_2", 1,
"GEOM_BOND_DISTANCE", 2,
"CCDC_GEOM_BOND_TYPE", 3,
"geomBondFields", ["_geom_bond_atom_site_label_1", "_geom_bond_atom_site_label_2", "_geom_bond_distance", "_ccdc_geom_bond_type"]);
});
