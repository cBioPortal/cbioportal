Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (null, "J.adapter.smarter.Resolver", ["java.lang.Character", "$.Float", "java.util.StringTokenizer", "J.adapter.smarter.AtomSetCollectionReader", "$.SmarterJmolAdapter", "J.io.LimitedLineReader", "J.util.Logger", "$.Parser"], function () {
c$ = Clazz.declareType (J.adapter.smarter, "Resolver");
c$.getReaderClassBase = $_M(c$, "getReaderClassBase", 
function (type) {
var name = type + "Reader";
if (type.startsWith ("Xml")) return "J.adapter.readers." + "xml." + name;
var key = ";" + type + ";";
for (var i = 1; i < J.adapter.smarter.Resolver.readerSets.length; i += 2) if (J.adapter.smarter.Resolver.readerSets[i].indexOf (key) >= 0) return "J.adapter.readers." + J.adapter.smarter.Resolver.readerSets[i - 1] + name;

return "J.adapter.readers." + "???." + name;
}, "~S");
c$.getFileType = $_M(c$, "getFileType", 
function (br) {
try {
return J.adapter.smarter.Resolver.determineAtomSetCollectionReader (br, false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
}, "java.io.BufferedReader");
c$.getAtomCollectionReader = $_M(c$, "getAtomCollectionReader", 
function (fullName, type, bufferedReader, htParams, ptFile) {
var atomSetCollectionReader = null;
var readerName;
fullName = fullName.$replace ('\\', '/');
var errMsg = null;
if (type != null) {
readerName = J.adapter.smarter.Resolver.getReaderFromType (type);
if (readerName == null) errMsg = "unrecognized file format type " + type;
 else J.util.Logger.info ("The Resolver assumes " + readerName);
} else {
readerName = J.adapter.smarter.Resolver.determineAtomSetCollectionReader (bufferedReader, true);
if (readerName.charAt (0) == '\n') {
type = htParams.get ("defaultType");
if (type != null) {
type = J.adapter.smarter.Resolver.getReaderFromType (type);
if (type != null) readerName = type;
}}if (readerName.charAt (0) == '\n') errMsg = "unrecognized file format for file " + fullName + "\n" + readerName;
 else if (readerName.equals ("spt")) errMsg = "NOTE: file recognized as a script file: " + fullName + "\n";
 else if (!fullName.equals ("ligand")) J.util.Logger.info ("The Resolver thinks " + readerName);
}if (errMsg != null) {
J.adapter.smarter.SmarterJmolAdapter.close (bufferedReader);
return errMsg;
}htParams.put ("ptFile", Integer.$valueOf (ptFile));
if (ptFile <= 0) htParams.put ("readerName", readerName);
if (readerName.indexOf ("Xml") == 0) readerName = "Xml";
var className = null;
var atomSetCollectionReaderClass;
var err = null;
try {
try {
className = J.adapter.smarter.Resolver.getReaderClassBase (readerName);
atomSetCollectionReaderClass = Class.forName (className);
atomSetCollectionReader = atomSetCollectionReaderClass.newInstance ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
err = "File reader was not found:" + className;
J.util.Logger.error (err);
return err;
} else {
throw e;
}
}
return atomSetCollectionReader;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
err = "uncaught error in file loading for " + className;
J.util.Logger.error (err);
System.out.println (e.getMessage ());
return err;
} else {
throw e;
}
}
}, "~S,~S,~O,java.util.Map,~N");
c$.DOMResolve = $_M(c$, "DOMResolve", 
function (DOMNode, htParams) {
var className = null;
var atomSetCollectionReaderClass;
var atomSetCollectionReader;
var atomSetCollectionReaderName = J.adapter.smarter.Resolver.getXmlType (htParams.get ("nameSpaceInfo"));
if (J.util.Logger.debugging) {
J.util.Logger.debug ("The Resolver thinks " + atomSetCollectionReaderName);
}htParams.put ("readerName", atomSetCollectionReaderName);
try {
className = "J.adapter.readers.xml.XmlReader";
atomSetCollectionReaderClass = Class.forName (className);
atomSetCollectionReader = atomSetCollectionReaderClass.newInstance ();
return atomSetCollectionReader;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
var err = "File reader was not found:" + className;
J.util.Logger.errorEx (err, e);
return err;
} else {
throw e;
}
}
}, "~O,java.util.Map");
c$.determineAtomSetCollectionReader = $_M(c$, "determineAtomSetCollectionReader", 
($fz = function (readerOrDocument, returnLines) {
if (Clazz.instanceOf (readerOrDocument, J.api.JmolDocument)) {
return "PyMOL";
}var llr =  new J.io.LimitedLineReader (readerOrDocument, 16384);
var leader = llr.getHeader (64).trim ();
for (var i = 0; i < J.adapter.smarter.Resolver.fileStartsWithRecords.length; ++i) {
var recordTags = J.adapter.smarter.Resolver.fileStartsWithRecords[i];
for (var j = 1; j < recordTags.length; ++j) {
var recordTag = recordTags[j];
if (leader.startsWith (recordTag)) return recordTags[0];
}
}
if (leader.indexOf ("PNG") == 1 && leader.indexOf ("PNGJ") >= 0) return "pngj";
if (leader.indexOf ("PNG") == 1 || leader.indexOf ("JPG") == 1 || leader.indexOf ("JFIF") == 6) return "spt";
if (leader.startsWith ("##TITLE")) return "Jcampdx";
var lines =  new Array (16);
var nLines = 0;
for (var i = 0; i < lines.length; ++i) {
lines[i] = llr.readLineWithNewline ();
if (lines[i].length > 0) nLines++;
}
var readerName;
if ((readerName = J.adapter.smarter.Resolver.checkSpecial (nLines, lines, false)) != null) return readerName;
if ((readerName = J.adapter.smarter.Resolver.checkLineStarts (lines)) != null) {
if (readerName.equals ("Cif") && llr.getHeader (0).contains ("mmcif_pdbx.dic")) readerName = "MMCIF_PDBX";
return readerName;
}if ((readerName = J.adapter.smarter.Resolver.checkHeaderContains (llr.getHeader (0))) != null) return readerName;
if ((readerName = J.adapter.smarter.Resolver.checkSpecial (nLines, lines, true)) != null) return readerName;
return (returnLines ? "\n" + lines[0] + "\n" + lines[1] + "\n" + lines[2] + "\n" : null);
}, $fz.isPrivate = true, $fz), "~O,~B");
c$.checkHeaderContains = $_M(c$, "checkHeaderContains", 
($fz = function (header) {
for (var i = 0; i < J.adapter.smarter.Resolver.headerContainsRecords.length; ++i) {
var recordTags = J.adapter.smarter.Resolver.headerContainsRecords[i];
for (var j = 1; j < recordTags.length; ++j) {
var recordTag = recordTags[j];
if (header.indexOf (recordTag) < 0) continue;
var type = recordTags[0];
return (!type.equals ("Xml") ? type : header.indexOf ("<!DOCTYPE HTML PUBLIC") < 0 && header.indexOf ("XHTML") < 0 && (header.indexOf ("xhtml") < 0 || header.indexOf ("<cml") >= 0) ? J.adapter.smarter.Resolver.getXmlType (header) : null);
}
}
return null;
}, $fz.isPrivate = true, $fz), "~S");
c$.checkLineStarts = $_M(c$, "checkLineStarts", 
($fz = function (lines) {
for (var i = 0; i < J.adapter.smarter.Resolver.lineStartsWithRecords.length; ++i) {
var recordTags = J.adapter.smarter.Resolver.lineStartsWithRecords[i];
for (var j = 1; j < recordTags.length; ++j) {
var recordTag = recordTags[j];
for (var k = 0; k < lines.length; ++k) {
if (lines[k].startsWith (recordTag)) return recordTags[0];
}
}
}
return null;
}, $fz.isPrivate = true, $fz), "~A");
c$.getXmlType = $_M(c$, "getXmlType", 
($fz = function (header) {
if (header.indexOf ("http://www.molpro.net/") >= 0) {
return J.adapter.smarter.Resolver.specialTags[19][0];
}if (header.indexOf ("odyssey") >= 0) {
return J.adapter.smarter.Resolver.specialTags[20][0];
}if (header.indexOf ("C3XML") >= 0) {
return J.adapter.smarter.Resolver.specialTags[18][0];
}if (header.indexOf ("arguslab") >= 0) {
return J.adapter.smarter.Resolver.specialTags[16][0];
}if (header.indexOf ("jvxl") >= 0) {
return J.adapter.smarter.Resolver.specialTags[17][0];
}if (header.indexOf ("http://www.xml-cml.org/schema") >= 0 || header.indexOf ("cml:") >= 0) {
return J.adapter.smarter.Resolver.specialTags[17][0];
}if (header.indexOf ("XSD") >= 0) {
return J.adapter.smarter.Resolver.specialTags[21][0];
}if (header.indexOf (">vasp") >= 0) {
return J.adapter.smarter.Resolver.specialTags[22][0];
}if (header.indexOf ("<GEOMETRY_INFO>") >= 0) {
return J.adapter.smarter.Resolver.specialTags[23][0];
}return J.adapter.smarter.Resolver.specialTags[17][0] + "(unidentified)";
}, $fz.isPrivate = true, $fz), "~S");
c$.getReaderFromType = $_M(c$, "getReaderFromType", 
($fz = function (type) {
type = type.toLowerCase ();
var base = null;
if ((base = J.adapter.smarter.Resolver.checkType (J.adapter.smarter.Resolver.specialTags, type)) != null) return base;
if ((base = J.adapter.smarter.Resolver.checkType (J.adapter.smarter.Resolver.fileStartsWithRecords, type)) != null) return base;
if ((base = J.adapter.smarter.Resolver.checkType (J.adapter.smarter.Resolver.lineStartsWithRecords, type)) != null) return base;
return J.adapter.smarter.Resolver.checkType (J.adapter.smarter.Resolver.headerContainsRecords, type);
}, $fz.isPrivate = true, $fz), "~S");
c$.checkType = $_M(c$, "checkType", 
($fz = function (typeTags, type) {
for (var i = 0; i < typeTags.length; ++i) if (typeTags[i][0].toLowerCase ().equals (type)) return typeTags[i][0];

return null;
}, $fz.isPrivate = true, $fz), "~A,~S");
c$.checkSpecial = $_M(c$, "checkSpecial", 
($fz = function (nLines, lines, isEnd) {
if (isEnd) {
if (J.adapter.smarter.Resolver.checkGromacs (lines)) return J.adapter.smarter.Resolver.specialTags[14][0];
if (J.adapter.smarter.Resolver.checkCrystal (lines)) return J.adapter.smarter.Resolver.specialTags[12][0];
if (J.adapter.smarter.Resolver.checkCastep (lines)) return J.adapter.smarter.Resolver.specialTags[10][0];
if (J.adapter.smarter.Resolver.checkVaspposcar (lines)) return J.adapter.smarter.Resolver.specialTags[13][0];
} else {
if (nLines == 1 && lines[0].length > 0 && Character.isDigit (lines[0].charAt (0))) return J.adapter.smarter.Resolver.specialTags[0][0];
if (J.adapter.smarter.Resolver.checkMopacGraphf (lines)) return J.adapter.smarter.Resolver.specialTags[1][0];
if (J.adapter.smarter.Resolver.checkOdyssey (lines)) return J.adapter.smarter.Resolver.specialTags[3][0];
if (J.adapter.smarter.Resolver.checkMol (lines)) return J.adapter.smarter.Resolver.specialTags[4][0];
if (J.adapter.smarter.Resolver.checkXyz (lines)) return J.adapter.smarter.Resolver.specialTags[5][0];
if (J.adapter.smarter.Resolver.checkAlchemy (lines[0])) return J.adapter.smarter.Resolver.specialTags[8][0];
if (J.adapter.smarter.Resolver.checkFoldingXyz (lines)) return J.adapter.smarter.Resolver.specialTags[6][0];
if (J.adapter.smarter.Resolver.checkCube (lines)) return J.adapter.smarter.Resolver.specialTags[7][0];
if (J.adapter.smarter.Resolver.checkWien2k (lines)) return J.adapter.smarter.Resolver.specialTags[9][0];
if (J.adapter.smarter.Resolver.checkAims (lines)) return J.adapter.smarter.Resolver.specialTags[11][0];
if (J.adapter.smarter.Resolver.checkGenNBO (lines)) return J.adapter.smarter.Resolver.specialTags[15][0];
}return null;
}, $fz.isPrivate = true, $fz), "~N,~A,~B");
c$.checkAims = $_M(c$, "checkAims", 
($fz = function (lines) {
for (var i = 0; i < lines.length; i++) {
if (lines[i].startsWith ("mol 1")) return false;
var tokens = J.util.Parser.getTokens (lines[i]);
if (tokens.length == 0) continue;
if (tokens[0].startsWith ("atom") && tokens.length >= 5 || tokens[0].startsWith ("multipole") && tokens.length >= 6 || tokens[0].startsWith ("lattice_vector") && tokens.length >= 4) return true;
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkAlchemy = $_M(c$, "checkAlchemy", 
($fz = function (line) {
var pt;
if ((pt = line.indexOf ("ATOMS")) >= 0 && line.indexOf ("BONDS") > pt) try {
var n = Integer.parseInt (line.substring (0, pt).trim ());
return (n > 0);
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
} else {
throw nfe;
}
}
return false;
}, $fz.isPrivate = true, $fz), "~S");
c$.checkCastep = $_M(c$, "checkCastep", 
($fz = function (lines) {
for (var i = 0; i < lines.length; i++) {
if (lines[i].indexOf ("Frequencies in         cm-1") == 1 || lines[i].contains ("CASTEP") || lines[i].toUpperCase ().startsWith ("%BLOCK LATTICE_ABC") || lines[i].toUpperCase ().startsWith ("%BLOCK LATTICE_CART") || lines[i].toUpperCase ().startsWith ("%BLOCK POSITIONS_FRAC") || lines[i].toUpperCase ().startsWith ("%BLOCK POSITIONS_ABS") || lines[i].contains ("<-- E")) return true;
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkVaspposcar = $_M(c$, "checkVaspposcar", 
($fz = function (lines) {
var select = lines[8].trim ().toLowerCase ();
if (select.contains ("direct") || select.contains ("cartesian") || select.contains ("selective")) return true;
var normal = lines[7].trim ().toLowerCase ();
if (normal.contains ("direct") || normal.contains ("cartesian")) return true;
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkCrystal = $_M(c$, "checkCrystal", 
($fz = function (lines) {
var s = lines[1].trim ();
if (s.equals ("SLAB") || s.equals ("MOLECULE") || s.equals ("CRYSTAL") || s.equals ("POLYMER") || (s = lines[3]).equals ("SLAB") || s.equals ("MOLECULE") || s.equals ("POLYMER")) return true;
for (var i = 0; i < lines.length; i++) {
if (lines[i].trim ().equals ("OPTGEOM")) return true;
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkCube = $_M(c$, "checkCube", 
($fz = function (lines) {
try {
for (var j = 2; j <= 5; j++) {
var tokens2 =  new java.util.StringTokenizer (lines[j]);
var n = tokens2.countTokens ();
if (!(n == 4 || j == 2 && n == 5)) return false;
Integer.parseInt (tokens2.nextToken ());
for (var i = 3; --i >= 0; ) J.util.Parser.fVal (tokens2.nextToken ());

if (n == 5) Integer.parseInt (tokens2.nextToken ());
}
return true;
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
} else {
throw nfe;
}
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkFoldingXyz = $_M(c$, "checkFoldingXyz", 
($fz = function (lines) {
var tokens =  new java.util.StringTokenizer (lines[0].trim (), " \t");
if (tokens.countTokens () < 2) return false;
try {
Integer.parseInt (tokens.nextToken ().trim ());
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
return false;
} else {
throw nfe;
}
}
var secondLine = lines[1].trim ();
if (secondLine.length == 0) secondLine = lines[2].trim ();
tokens =  new java.util.StringTokenizer (secondLine, " \t");
if (tokens.countTokens () == 0) return false;
try {
Integer.parseInt (tokens.nextToken ().trim ());
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
return false;
} else {
throw nfe;
}
}
return true;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkGenNBO = $_M(c$, "checkGenNBO", 
($fz = function (lines) {
return (lines[1].startsWith (" Basis set information needed for plotting orbitals") || lines[1].indexOf ("s in the AO basis:") >= 0 || lines[2].indexOf (" N A T U R A L   A T O M I C   O R B I T A L") >= 0);
}, $fz.isPrivate = true, $fz), "~A");
c$.checkGromacs = $_M(c$, "checkGromacs", 
($fz = function (lines) {
if (J.util.Parser.parseInt (lines[1]) == -2147483648) return false;
var len = -1;
for (var i = 2; i < 16 && len != 0; i++) if ((len = lines[i].length) != 69 && len != 45 && len != 0) return false;

return true;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkMol = $_M(c$, "checkMol", 
($fz = function (lines) {
var line4trimmed = ("X" + lines[3]).trim ().toUpperCase ();
if (line4trimmed.length < 7 || line4trimmed.indexOf (".") >= 0) return false;
if (line4trimmed.endsWith ("V2000") || line4trimmed.endsWith ("V3000")) return true;
try {
var n1 = Integer.parseInt (lines[3].substring (0, 3).trim ());
var n2 = Integer.parseInt (lines[3].substring (3, 6).trim ());
return (n1 > 0 && n2 >= 0 && lines[0].indexOf ("@<TRIPOS>") != 0 && lines[1].indexOf ("@<TRIPOS>") != 0 && lines[2].indexOf ("@<TRIPOS>") != 0);
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
} else {
throw nfe;
}
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkMopacGraphf = $_M(c$, "checkMopacGraphf", 
($fz = function (lines) {
return (lines[0].indexOf ("MOPAC-Graphical data") > 2);
}, $fz.isPrivate = true, $fz), "~A");
c$.checkOdyssey = $_M(c$, "checkOdyssey", 
($fz = function (lines) {
var i;
for (i = 0; i < lines.length; i++) if (!lines[i].startsWith ("C ") && lines[i].length != 0) break;

if (i >= lines.length || lines[i].charAt (0) != ' ' || (i = i + 2) + 1 >= lines.length) return false;
try {
var spin = Integer.parseInt (lines[i].substring (2).trim ());
var charge = Integer.parseInt (lines[i].substring (0, 2).trim ());
var atom1 = Integer.parseInt (lines[++i].substring (0, 2).trim ());
if (spin < 0 || spin > 5 || atom1 <= 0 || charge > 5) return false;
var atomline = J.adapter.smarter.AtomSetCollectionReader.getTokensFloat (lines[i], null, 5);
return !Float.isNaN (atomline[1]) && !Float.isNaN (atomline[2]) && !Float.isNaN (atomline[3]) && Float.isNaN (atomline[4]);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
c$.checkWien2k = $_M(c$, "checkWien2k", 
($fz = function (lines) {
return (lines[2].startsWith ("MODE OF CALC=") || lines[2].startsWith ("             RELA") || lines[2].startsWith ("             NREL"));
}, $fz.isPrivate = true, $fz), "~A");
c$.checkXyz = $_M(c$, "checkXyz", 
($fz = function (lines) {
try {
Integer.parseInt (lines[0].trim ());
return true;
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
} else {
throw nfe;
}
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
Clazz.defineStatics (c$,
"classBase", "J.adapter.readers.");
c$.readerSets = c$.prototype.readerSets = ["cif.", ";Cif;MMCIF_PDBX;", "molxyz.", ";Mol3D;Mol;Xyz;", "more.", ";BinaryDcd;Gromacs;Jcampdx;MdCrd;MdTop;Mol2;Pqr;P2n;TlsDataOnly;", "quantum.", ";Adf;Csf;Dgrid;GamessUK;GamessUS;Gaussian;GausianWfn;Jaguar;Molden;MopacGraphf;GenNBO;NWChem;Odyssey;Psi;Qchem;Spartan;SpartanSmol;WebMO;", "pdb.", ";Pdb;", "pymol.", ";PyMOL;", "simple.", ";Alchemy;Ampac;Cube;FoldingXyz;GhemicalMM;HyperChem;Jme;Mopac;MopacArchive;ZMatrix;", "xtal.", ";Abinit;Aims;Castep;Crystal;Dmol;Espresso;Gulp;Jana;Magres;Shelx;Siesta;VaspOutcar;VaspPoscar;Wien2k;Xcrysden;"];
Clazz.defineStatics (c$,
"CML_NAMESPACE_URI", "http://www.xml-cml.org/schema",
"SPECIAL_JME", 0,
"SPECIAL_MOPACGRAPHF", 1,
"SPECIAL_ODYSSEY", 3,
"SPECIAL_MOL", 4,
"SPECIAL_XYZ", 5,
"SPECIAL_FOLDINGXYZ", 6,
"SPECIAL_CUBE", 7,
"SPECIAL_ALCHEMY", 8,
"SPECIAL_WIEN", 9,
"SPECIAL_CASTEP", 10,
"SPECIAL_AIMS", 11,
"SPECIAL_CRYSTAL", 12,
"SPECIAL_VASPPOSCAR", 13,
"SPECIAL_GROMACS", 14,
"SPECIAL_GENNBO", 15,
"SPECIAL_ARGUS_XML", 16,
"SPECIAL_CML_XML", 17,
"SPECIAL_CHEM3D_XML", 18,
"SPECIAL_MOLPRO_XML", 19,
"SPECIAL_ODYSSEY_XML", 20,
"SPECIAL_XSD_XML", 21,
"SPECIAL_VASP_XML", 22,
"SPECIAL_QE_XML", 23,
"SPECIAL_ARGUS_DOM", 24,
"SPECIAL_CML_DOM", 25,
"SPECIAL_CHEM3D_DOM", 26,
"SPECIAL_MOLPRO_DOM", 27,
"SPECIAL_ODYSSEY_DOM", 28,
"SPECIAL_XSD_DOM", 29,
"SPECIAL_VASP_DOM", 30,
"specialTags", [["Jme"], ["MopacGraphf"], ["Mol3D"], ["Odyssey"], ["Mol"], ["Xyz"], ["FoldingXyz"], ["Cube"], ["Alchemy"], ["Wien2k"], ["Castep"], ["Aims"], ["Crystal"], ["VaspPoscar"], ["Gromacs"], ["GenNBO"], ["XmlArgus"], ["XmlCml"], ["XmlChem3d"], ["XmlMolpro"], ["XmlOdyssey"], ["XmlXsd"], ["XmlVasp"], ["XmlQE"], ["XmlArgus(DOM)"], ["XmlCml(DOM)"], ["XmlChem3d(DOM)"], ["XmlMolpro(DOM)"], ["XmlOdyssey(DOM)"], ["XmlXsd(DOM)"], ["XmlVasp(DOM)"], ["MdCrd"]],
"LEADER_CHAR_MAX", 64,
"sptContainsRecords", ["spt", "# Jmol state", "# Jmol script"],
"cubeFileStartRecords", ["Cube", "JVXL", "#JVXL"],
"mol2Records", ["Mol2", "mol2", "@<TRIPOS>"],
"webmoFileStartRecords", ["WebMO", "[HEADER]"],
"moldenFileStartRecords", ["Molden", "[Molden"],
"dcdFileStartRecords", ["BinaryDcd", "T\0\0\0CORD", "\0\0\0TCORD"],
"tlsDataOnlyFileStartRecords", ["TlsDataOnly", "REFMAC\n\nTL", "REFMAC\r\n\r\n", "REFMAC\r\rTL"],
"zMatrixFileStartRecords", ["ZMatrix", "#ZMATRIX"],
"magresFileStartRecords", ["Magres", "#$magres", "# magres"],
"pymolStartRecords", ["PyMOL", "}q"],
"janaStartRecords", ["Jana", "Version Jana"]);
c$.fileStartsWithRecords = c$.prototype.fileStartsWithRecords = [J.adapter.smarter.Resolver.sptContainsRecords, J.adapter.smarter.Resolver.cubeFileStartRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.webmoFileStartRecords, J.adapter.smarter.Resolver.moldenFileStartRecords, J.adapter.smarter.Resolver.dcdFileStartRecords, J.adapter.smarter.Resolver.tlsDataOnlyFileStartRecords, J.adapter.smarter.Resolver.zMatrixFileStartRecords, J.adapter.smarter.Resolver.magresFileStartRecords, J.adapter.smarter.Resolver.pymolStartRecords, J.adapter.smarter.Resolver.janaStartRecords];
Clazz.defineStatics (c$,
"pqrLineStartRecords", ["Pqr", "REMARK   1 PQR"],
"p2nLineStartRecords", ["P2n", "REMARK   1 P2N"],
"pdbLineStartRecords", ["Pdb", "HEADER", "OBSLTE", "TITLE ", "CAVEAT", "COMPND", "SOURCE", "KEYWDS", "EXPDTA", "AUTHOR", "REVDAT", "SPRSDE", "JRNL  ", "REMARK ", "DBREF ", "SEQADV", "SEQRES", "MODRES", "HELIX ", "SHEET ", "TURN  ", "CRYST1", "ORIGX1", "ORIGX2", "ORIGX3", "SCALE1", "SCALE2", "SCALE3", "ATOM  ", "HETATM", "MODEL ", "LINK  "],
"shelxLineStartRecords", ["Shelx", "TITL ", "ZERR ", "LATT ", "SYMM ", "CELL "],
"cifLineStartRecords", ["Cif", "data_", "_publ"],
"ghemicalMMLineStartRecords", ["GhemicalMM", "!Header mm1gp", "!Header gpr"],
"jaguarLineStartRecords", ["Jaguar", "  |  Jaguar version"],
"mdlLineStartRecords", ["Mol", "$MDL "],
"spartanSmolLineStartRecords", ["SpartanSmol", "INPUT="],
"csfLineStartRecords", ["Csf", "local_transform"],
"mdTopLineStartRecords", ["MdTop", "%FLAG TITLE"],
"hyperChemLineStartRecords", ["HyperChem", "mol 1"],
"vaspOutcarLineStartRecords", ["VaspOutcar", " vasp.", " INCAR:"]);
c$.lineStartsWithRecords = c$.prototype.lineStartsWithRecords = [J.adapter.smarter.Resolver.cifLineStartRecords, J.adapter.smarter.Resolver.pqrLineStartRecords, J.adapter.smarter.Resolver.p2nLineStartRecords, J.adapter.smarter.Resolver.pdbLineStartRecords, J.adapter.smarter.Resolver.shelxLineStartRecords, J.adapter.smarter.Resolver.ghemicalMMLineStartRecords, J.adapter.smarter.Resolver.jaguarLineStartRecords, J.adapter.smarter.Resolver.mdlLineStartRecords, J.adapter.smarter.Resolver.spartanSmolLineStartRecords, J.adapter.smarter.Resolver.csfLineStartRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.mdTopLineStartRecords, J.adapter.smarter.Resolver.hyperChemLineStartRecords, J.adapter.smarter.Resolver.vaspOutcarLineStartRecords];
Clazz.defineStatics (c$,
"xmlContainsRecords", ["Xml", "<?xml", "<atom", "<molecule", "<reaction", "<cml", "<bond", ".dtd\"", "<list>", "<entry", "<identifier", "http://www.xml-cml.org/schema/cml2/core"],
"gaussianContainsRecords", ["Gaussian", "Entering Gaussian System", "Entering Link 1", "1998 Gaussian, Inc."],
"ampacContainsRecords", ["Ampac", "AMPAC Version"],
"mopacContainsRecords", ["Mopac", "MOPAC 93 (c) Fujitsu", "MOPAC FOR LINUX (PUBLIC DOMAIN VERSION)", "MOPAC:  VERSION  6", "MOPAC   7", "MOPAC2", "MOPAC (PUBLIC"],
"qchemContainsRecords", ["Qchem", "Welcome to Q-Chem", "A Quantum Leap Into The Future Of Chemistry"],
"gamessUKContainsRecords", ["GamessUK", "GAMESS-UK", "G A M E S S - U K"],
"gamessUSContainsRecords", ["GamessUS", "GAMESS"],
"spartanBinaryContainsRecords", ["SpartanSmol", "|PropertyArchive", "_spartan", "spardir", "BEGIN Directory Entry Molecule"],
"spartanContainsRecords", ["Spartan", "Spartan"],
"adfContainsRecords", ["Adf", "Amsterdam Density Functional"],
"dgridContainsRecords", ["Dgrid", "BASISFILE   created by DGrid"],
"dmolContainsRecords", ["Dmol", "DMol^3"],
"gulpContainsRecords", ["Gulp", "GENERAL UTILITY LATTICE PROGRAM"],
"psiContainsRecords", ["Psi", "    PSI  3", "PSI3:"],
"nwchemContainsRecords", ["NWChem", " argument  1 = "],
"uicrcifContainsRecords", ["Cif", "Crystallographic Information File"],
"crystalContainsRecords", ["Crystal", "*                                CRYSTAL"],
"espressoContainsRecords", ["Espresso", "Program PWSCF", "Program PHONON"],
"siestaContainsRecords", ["Siesta", "MD.TypeOfRun", "SolutionMethod", "MeshCutoff", "WELCOME TO SIESTA"],
"xcrysDenContainsRecords", ["Xcrysden", "PRIMVEC", "CONVVEC", "PRIMCOORD"],
"mopacArchiveContainsRecords", ["MopacArchive", "SUMMARY OF PM"],
"abinitContainsRecords", ["Abinit", "http://www.abinit.org", "Catholique", "Louvain"]);
c$.headerContainsRecords = c$.prototype.headerContainsRecords = [J.adapter.smarter.Resolver.sptContainsRecords, J.adapter.smarter.Resolver.xmlContainsRecords, J.adapter.smarter.Resolver.gaussianContainsRecords, J.adapter.smarter.Resolver.ampacContainsRecords, J.adapter.smarter.Resolver.mopacContainsRecords, J.adapter.smarter.Resolver.qchemContainsRecords, J.adapter.smarter.Resolver.gamessUKContainsRecords, J.adapter.smarter.Resolver.gamessUSContainsRecords, J.adapter.smarter.Resolver.spartanBinaryContainsRecords, J.adapter.smarter.Resolver.spartanContainsRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.adfContainsRecords, J.adapter.smarter.Resolver.psiContainsRecords, J.adapter.smarter.Resolver.nwchemContainsRecords, J.adapter.smarter.Resolver.uicrcifContainsRecords, J.adapter.smarter.Resolver.dgridContainsRecords, J.adapter.smarter.Resolver.crystalContainsRecords, J.adapter.smarter.Resolver.dmolContainsRecords, J.adapter.smarter.Resolver.gulpContainsRecords, J.adapter.smarter.Resolver.espressoContainsRecords, J.adapter.smarter.Resolver.siestaContainsRecords, J.adapter.smarter.Resolver.xcrysDenContainsRecords, J.adapter.smarter.Resolver.mopacArchiveContainsRecords, J.adapter.smarter.Resolver.abinitContainsRecords];
});
