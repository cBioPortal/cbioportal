Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (null, "J.adapter.smarter.Resolver", ["java.lang.Character", "$.Float", "java.util.StringTokenizer", "javajs.api.GenericBinaryDocument", "JU.LimitedLineReader", "$.PT", "J.adapter.smarter.AtomSetCollectionReader", "$.SmarterJmolAdapter", "J.api.Interface", "JU.Logger"], function () {
c$ = Clazz.declareType (J.adapter.smarter, "Resolver");
c$.getReaderClassBase = Clazz.defineMethod (c$, "getReaderClassBase", 
function (type) {
var name = type + "Reader";
if (type.startsWith ("Xml")) return "J.adapter.readers." + "xml." + name;
var key = ";" + type + ";";
for (var i = 1; i < J.adapter.smarter.Resolver.readerSets.length; i += 2) if (J.adapter.smarter.Resolver.readerSets[i].indexOf (key) >= 0) return "J.adapter.readers." + J.adapter.smarter.Resolver.readerSets[i - 1] + name;

return "J.adapter.readers." + "???." + name;
}, "~S");
c$.getFileType = Clazz.defineMethod (c$, "getFileType", 
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
c$.getAtomCollectionReader = Clazz.defineMethod (c$, "getAtomCollectionReader", 
function (fullName, type, bufferedReader, htParams, ptFile) {
var rdr = null;
var readerName;
fullName = fullName.$replace ('\\', '/');
var errMsg = null;
if (type != null) {
readerName = J.adapter.smarter.Resolver.getReaderFromType (type);
if (readerName == null) readerName = J.adapter.smarter.Resolver.getReaderFromType ("Xml" + type);
if (readerName == null) errMsg = "unrecognized file format type " + type;
 else JU.Logger.info ("The Resolver assumes " + readerName);
} else {
readerName = J.adapter.smarter.Resolver.determineAtomSetCollectionReader (bufferedReader, true);
if (readerName.charAt (0) == '\n') {
type = htParams.get ("defaultType");
if (type != null) {
type = J.adapter.smarter.Resolver.getReaderFromType (type);
if (type != null) readerName = type;
}}if (readerName.charAt (0) == '\n') errMsg = "unrecognized file format for file\n" + fullName + "\n" + J.adapter.smarter.Resolver.split (readerName, 50);
 else if (readerName.equals ("spt")) errMsg = "NOTE: file recognized as a script file: " + fullName + "\n";
 else if (!fullName.equals ("ligand")) JU.Logger.info ("The Resolver thinks " + readerName);
}if (errMsg != null) {
J.adapter.smarter.SmarterJmolAdapter.close (bufferedReader);
return errMsg;
}htParams.put ("ptFile", Integer.$valueOf (ptFile));
if (ptFile <= 0) htParams.put ("readerName", readerName);
if (readerName.indexOf ("Xml") == 0) readerName = "Xml";
var className = null;
var err = null;
className = J.adapter.smarter.Resolver.getReaderClassBase (readerName);
if ((rdr = J.api.Interface.getInterface (className)) == null) {
err = "File reader was not found:" + className;
JU.Logger.error (err);
return err;
}return rdr;
}, "~S,~S,~O,java.util.Map,~N");
c$.getReaderFromType = Clazz.defineMethod (c$, "getReaderFromType", 
 function (type) {
type = ";" + type.toLowerCase () + ";";
var set;
var pt;
for (var i = J.adapter.smarter.Resolver.readerSets.length; --i >= 0; ) if ((pt = (set = J.adapter.smarter.Resolver.readerSets[i--]).toLowerCase ().indexOf (type)) >= 0) return set.substring (pt + 1, set.indexOf (";", pt + 2));

return null;
}, "~S");
c$.split = Clazz.defineMethod (c$, "split", 
 function (a, n) {
var s = "";
var l = a.length;
for (var i = 0, j = 0; i < l; i = j) s += a.substring (i, (j = Math.min (i + n, l))) + "\n";

return s;
}, "~S,~N");
c$.DOMResolve = Clazz.defineMethod (c$, "DOMResolve", 
function (DOMNode, htParams) {
var className = null;
var rdr;
var rdrName = J.adapter.smarter.Resolver.getXmlType (htParams.get ("nameSpaceInfo"));
if (JU.Logger.debugging) {
JU.Logger.debug ("The Resolver thinks " + rdrName);
}htParams.put ("readerName", rdrName);
className = "J.adapter.readers.xml.XmlReader";
if ((rdr = J.api.Interface.getInterface (className)) != null) return rdr;
var err = "File reader was not found:" + className;
JU.Logger.error (err);
return err;
}, "~O,java.util.Map");
c$.determineAtomSetCollectionReader = Clazz.defineMethod (c$, "determineAtomSetCollectionReader", 
 function (readerOrDocument, returnLines) {
if (Clazz.instanceOf (readerOrDocument, javajs.api.GenericBinaryDocument)) {
return "PyMOL";
}var llr =  new JU.LimitedLineReader (readerOrDocument, 16384);
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
if ((readerName = J.adapter.smarter.Resolver.checkLineStarts (lines)) != null) return readerName;
if ((readerName = J.adapter.smarter.Resolver.checkHeaderContains (llr.getHeader (0))) != null) return readerName;
if ((readerName = J.adapter.smarter.Resolver.checkSpecial (nLines, lines, true)) != null) return readerName;
return (returnLines ? "\n" + lines[0] + "\n" + lines[1] + "\n" + lines[2] + "\n" : null);
}, "~O,~B");
c$.checkHeaderContains = Clazz.defineMethod (c$, "checkHeaderContains", 
 function (header) {
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
}, "~S");
c$.checkLineStarts = Clazz.defineMethod (c$, "checkLineStarts", 
 function (lines) {
for (var i = 0; i < J.adapter.smarter.Resolver.lineStartsWithRecords.length; ++i) {
var recordTags = J.adapter.smarter.Resolver.lineStartsWithRecords[i];
for (var j = 1; j < recordTags.length; ++j) {
var recordTag = recordTags[j];
for (var k = 0; k < lines.length; k++) {
if (lines[k].startsWith (recordTag)) return recordTags[0];
}
}
}
return null;
}, "~A");
c$.getXmlType = Clazz.defineMethod (c$, "getXmlType", 
 function (header) {
if (header.indexOf ("http://www.molpro.net/") >= 0) {
return "XmlMolpro";
}if (header.indexOf ("odyssey") >= 0) {
return "XmlOdyssey";
}if (header.indexOf ("C3XML") >= 0) {
return "XmlChem3d";
}if (header.indexOf ("arguslab") >= 0) {
return "XmlArgus";
}if (header.indexOf ("jvxl") >= 0 || header.indexOf ("http://www.xml-cml.org/schema") >= 0 || header.indexOf ("cml:") >= 0) {
return "XmlCml";
}if (header.indexOf ("XSD") >= 0) {
return "XmlXsd";
}if (header.indexOf (">vasp") >= 0) {
return "XmlVasp";
}if (header.indexOf ("<GEOMETRY_INFO>") >= 0) {
return "XmlQE";
}return "XmlCml(unidentified)";
}, "~S");
c$.checkSpecial = Clazz.defineMethod (c$, "checkSpecial", 
 function (nLines, lines, isEnd) {
if (isEnd) {
if (J.adapter.smarter.Resolver.checkGromacs (lines)) return "Gromacs";
if (J.adapter.smarter.Resolver.checkCrystal (lines)) return "Crystal";
if (J.adapter.smarter.Resolver.checkCastep (lines)) return "Castep";
if (J.adapter.smarter.Resolver.checkVaspposcar (lines)) return "VaspPoscar";
} else {
if (nLines == 1 && lines[0].length > 0 && Character.isDigit (lines[0].charAt (0))) return "Jme";
if (J.adapter.smarter.Resolver.checkMopacGraphf (lines)) return "MopacGraphf";
if (J.adapter.smarter.Resolver.checkOdyssey (lines)) return "Odyssey";
switch (J.adapter.smarter.Resolver.checkMol (lines)) {
case 1:
case 3:
case 2000:
case 3000:
return "Mol";
}
switch (J.adapter.smarter.Resolver.checkXyz (lines)) {
case 1:
return "Xyz";
case 2:
return "Bilbao";
}
if (J.adapter.smarter.Resolver.checkAlchemy (lines[0])) return "Alchemy";
if (J.adapter.smarter.Resolver.checkFoldingXyz (lines)) return "FoldingXyz";
if (J.adapter.smarter.Resolver.checkCube (lines)) return "Cube";
if (J.adapter.smarter.Resolver.checkWien2k (lines)) return "Wien2k";
if (J.adapter.smarter.Resolver.checkAims (lines)) return "Aims";
if (J.adapter.smarter.Resolver.checkGenNBO (lines)) return "GenNBO";
}return null;
}, "~N,~A,~B");
c$.checkAims = Clazz.defineMethod (c$, "checkAims", 
 function (lines) {
for (var i = 0; i < lines.length; i++) {
if (lines[i].startsWith ("mol 1")) return false;
var tokens = JU.PT.getTokens (lines[i]);
if (tokens.length == 0) continue;
if (tokens[0].startsWith ("atom") && tokens.length >= 5 || tokens[0].startsWith ("multipole") && tokens.length >= 6 || tokens[0].startsWith ("lattice_vector") && tokens.length >= 4) return true;
}
return false;
}, "~A");
c$.checkAlchemy = Clazz.defineMethod (c$, "checkAlchemy", 
 function (line) {
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
}, "~S");
c$.checkCastep = Clazz.defineMethod (c$, "checkCastep", 
 function (lines) {
for (var i = 0; i < lines.length; i++) {
if (lines[i].indexOf ("Frequencies in         cm-1") == 1 || lines[i].contains ("CASTEP") || lines[i].toUpperCase ().startsWith ("%BLOCK LATTICE_ABC") || lines[i].toUpperCase ().startsWith ("%BLOCK LATTICE_CART") || lines[i].toUpperCase ().startsWith ("%BLOCK POSITIONS_FRAC") || lines[i].toUpperCase ().startsWith ("%BLOCK POSITIONS_ABS") || lines[i].contains ("<-- E")) return true;
}
return false;
}, "~A");
c$.checkVaspposcar = Clazz.defineMethod (c$, "checkVaspposcar", 
 function (lines) {
var select = lines[8].trim ().toLowerCase ();
if (select.contains ("direct") || select.contains ("cartesian") || select.contains ("selective")) return true;
var normal = lines[7].trim ().toLowerCase ();
if (normal.contains ("direct") || normal.contains ("cartesian")) return true;
return false;
}, "~A");
c$.checkCrystal = Clazz.defineMethod (c$, "checkCrystal", 
 function (lines) {
var s = lines[1].trim ();
if (s.equals ("SLAB") || s.equals ("MOLECULE") || s.equals ("CRYSTAL") || s.equals ("POLYMER") || (s = lines[3]).equals ("SLAB") || s.equals ("MOLECULE") || s.equals ("POLYMER")) return true;
for (var i = 0; i < lines.length; i++) {
if (lines[i].trim ().equals ("OPTGEOM") || lines[i].trim ().equals ("FREQCALC") || lines[i].contains ("DOVESI") || lines[i].contains ("TORINO") || lines[i].contains ("http://www.crystal.unito.it") || lines[i].contains ("Pcrystal") || lines[i].contains ("MPPcrystal") || lines[i].contains ("crystal executable")) return true;
}
return false;
}, "~A");
c$.checkCube = Clazz.defineMethod (c$, "checkCube", 
 function (lines) {
try {
for (var j = 2; j <= 5; j++) {
var tokens2 =  new java.util.StringTokenizer (lines[j]);
var n = tokens2.countTokens ();
if (!(n == 4 || j == 2 && n == 5)) return false;
Integer.parseInt (tokens2.nextToken ());
for (var i = 3; --i >= 0; ) JU.PT.fVal (tokens2.nextToken ());

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
}, "~A");
c$.checkFoldingXyz = Clazz.defineMethod (c$, "checkFoldingXyz", 
 function (lines) {
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
}, "~A");
c$.checkGenNBO = Clazz.defineMethod (c$, "checkGenNBO", 
 function (lines) {
return (lines[1].startsWith (" Basis set information needed for plotting orbitals") || lines[1].indexOf ("s in the AO basis:") >= 0 || lines[2].indexOf (" N A T U R A L   A T O M I C   O R B I T A L") >= 0);
}, "~A");
c$.checkGromacs = Clazz.defineMethod (c$, "checkGromacs", 
 function (lines) {
if (JU.PT.parseInt (lines[1]) == -2147483648) return false;
var len = -1;
for (var i = 2; i < 16 && len != 0; i++) if ((len = lines[i].length) != 69 && len != 45 && len != 0) return false;

return true;
}, "~A");
c$.checkMol = Clazz.defineMethod (c$, "checkMol", 
 function (lines) {
var line4trimmed = ("X" + lines[3]).trim ().toUpperCase ();
if (line4trimmed.length < 7 || line4trimmed.indexOf (".") >= 0) return 0;
if (line4trimmed.endsWith ("V2000")) return 2000;
if (line4trimmed.endsWith ("V3000")) return 3000;
try {
var n1 = Integer.parseInt (lines[3].substring (0, 3).trim ());
var n2 = Integer.parseInt (lines[3].substring (3, 6).trim ());
return (n1 > 0 && n2 >= 0 && lines[0].indexOf ("@<TRIPOS>") != 0 && lines[1].indexOf ("@<TRIPOS>") != 0 && lines[2].indexOf ("@<TRIPOS>") != 0 ? 3 : 0);
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
} else {
throw nfe;
}
}
return 0;
}, "~A");
c$.checkMopacGraphf = Clazz.defineMethod (c$, "checkMopacGraphf", 
 function (lines) {
return (lines[0].indexOf ("MOPAC-Graphical data") > 2);
}, "~A");
c$.checkOdyssey = Clazz.defineMethod (c$, "checkOdyssey", 
 function (lines) {
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
}, "~A");
c$.checkWien2k = Clazz.defineMethod (c$, "checkWien2k", 
 function (lines) {
return (lines[2].startsWith ("MODE OF CALC=") || lines[2].startsWith ("             RELA") || lines[2].startsWith ("             NREL"));
}, "~A");
c$.checkXyz = Clazz.defineMethod (c$, "checkXyz", 
 function (lines) {
try {
Integer.parseInt (lines[0].trim ());
try {
Integer.parseInt (lines[2].trim ());
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
return 1;
} else {
throw nfe;
}
}
return 2;
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
if (lines[0].indexOf ("Bilbao Crys") >= 0) return 2;
} else {
throw nfe;
}
}
return 0;
}, "~A");
Clazz.defineStatics (c$,
"classBase", "J.adapter.readers.");
c$.readerSets = c$.prototype.readerSets = ["cif.", ";Cif;MMCif;", "molxyz.", ";Mol3D;Mol;Xyz;", "more.", ";BinaryDcd;Gromacs;Jcampdx;MdCrd;MdTop;Mol2;TlsDataOnly;", "quantum.", ";Adf;Csf;Dgrid;GamessUK;GamessUS;Gaussian;GaussianFchk;GaussianWfn;Jaguar;Molden;MopacGraphf;GenNBO;NWChem;Odyssey;Psi;Qchem;Spartan;SpartanSmol;WebMO;", "pdb.", ";Pdb;Pqr;P2n;", "pymol.", ";PyMOL;", "simple.", ";Alchemy;Ampac;Cube;FoldingXyz;GhemicalMM;HyperChem;Jme;JSON;Mopac;MopacArchive;Tinker;ZMatrix;", "xtal.", ";Abinit;Aims;Bilbao;Castep;Crystal;Dmol;Espresso;Gulp;Jana;Magres;Shelx;Siesta;VaspOutcar;VaspPoscar;Wien2k;Xcrysden;", "xml.", ";XmlArgus;XmlCml;XmlChem3d;XmlMolpro;XmlOdyssey;XmlXsd;XmlVasp;XmlQE;"];
Clazz.defineStatics (c$,
"CML_NAMESPACE_URI", "http://www.xml-cml.org/schema",
"LEADER_CHAR_MAX", 64,
"sptContainsRecords", ["spt", "# Jmol state", "# Jmol script", "JmolManifest"],
"cubeFileStartRecords", ["Cube", "JVXL", "#JVXL"],
"mol2Records", ["Mol2", "mol2", "@<TRIPOS>"],
"webmoFileStartRecords", ["WebMO", "[HEADER]"],
"moldenFileStartRecords", ["Molden", "[Molden"],
"dcdFileStartRecords", ["BinaryDcd", "T\0\0\0CORD", "\0\0\0TCORD"],
"tlsDataOnlyFileStartRecords", ["TlsDataOnly", "REFMAC\n\nTL", "REFMAC\r\n\r\n", "REFMAC\r\rTL"],
"zMatrixFileStartRecords", ["ZMatrix", "#ZMATRIX"],
"magresFileStartRecords", ["Magres", "#$magres", "# magres"],
"pymolStartRecords", ["PyMOL", "}q"],
"janaStartRecords", ["Jana", "Version Jana"],
"jsonStartRecords", ["JSON", "{\"mol\":"],
"m3dStartRecords", ["Alchemy", "STRUCTURE  1.00     1"]);
c$.fileStartsWithRecords = c$.prototype.fileStartsWithRecords = [J.adapter.smarter.Resolver.sptContainsRecords, J.adapter.smarter.Resolver.m3dStartRecords, J.adapter.smarter.Resolver.cubeFileStartRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.webmoFileStartRecords, J.adapter.smarter.Resolver.moldenFileStartRecords, J.adapter.smarter.Resolver.dcdFileStartRecords, J.adapter.smarter.Resolver.tlsDataOnlyFileStartRecords, J.adapter.smarter.Resolver.zMatrixFileStartRecords, J.adapter.smarter.Resolver.magresFileStartRecords, J.adapter.smarter.Resolver.pymolStartRecords, J.adapter.smarter.Resolver.janaStartRecords, J.adapter.smarter.Resolver.jsonStartRecords];
Clazz.defineStatics (c$,
"pqrLineStartRecords", ["Pqr", "REMARK   1 PQR"],
"p2nLineStartRecords", ["P2n", "REMARK   1 P2N"],
"pdbLineStartRecords", ["Pdb", "HEADER", "OBSLTE", "TITLE ", "CAVEAT", "COMPND", "SOURCE", "KEYWDS", "EXPDTA", "AUTHOR", "REVDAT", "SPRSDE", "JRNL  ", "REMARK ", "DBREF ", "SEQADV", "SEQRES", "MODRES", "HELIX ", "SHEET ", "TURN  ", "CRYST1", "ORIGX1", "ORIGX2", "ORIGX3", "SCALE1", "SCALE2", "SCALE3", "ATOM  ", "HETATM", "MODEL ", "LINK  "],
"shelxLineStartRecords", ["Shelx", "TITL ", "ZERR ", "LATT ", "SYMM ", "CELL "],
"cifLineStartRecords", ["Cif", "data_", "_publ"],
"mmcifLineStartRecords", ["MMCif", "_database_PDB_"],
"ghemicalMMLineStartRecords", ["GhemicalMM", "!Header mm1gp", "!Header gpr"],
"jaguarLineStartRecords", ["Jaguar", "  |  Jaguar version"],
"mdlLineStartRecords", ["Mol", "$MDL "],
"spartanSmolLineStartRecords", ["SpartanSmol", "INPUT="],
"csfLineStartRecords", ["Csf", "local_transform"],
"mdTopLineStartRecords", ["MdTop", "%FLAG TITLE"],
"hyperChemLineStartRecords", ["HyperChem", "mol 1"],
"vaspOutcarLineStartRecords", ["VaspOutcar", " vasp.", " INCAR:"]);
c$.lineStartsWithRecords = c$.prototype.lineStartsWithRecords = [J.adapter.smarter.Resolver.mmcifLineStartRecords, J.adapter.smarter.Resolver.cifLineStartRecords, J.adapter.smarter.Resolver.pqrLineStartRecords, J.adapter.smarter.Resolver.p2nLineStartRecords, J.adapter.smarter.Resolver.pdbLineStartRecords, J.adapter.smarter.Resolver.shelxLineStartRecords, J.adapter.smarter.Resolver.ghemicalMMLineStartRecords, J.adapter.smarter.Resolver.jaguarLineStartRecords, J.adapter.smarter.Resolver.mdlLineStartRecords, J.adapter.smarter.Resolver.spartanSmolLineStartRecords, J.adapter.smarter.Resolver.csfLineStartRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.mdTopLineStartRecords, J.adapter.smarter.Resolver.hyperChemLineStartRecords, J.adapter.smarter.Resolver.vaspOutcarLineStartRecords];
Clazz.defineStatics (c$,
"xmlContainsRecords", ["Xml", "<?xml", "<atom", "<molecule", "<reaction", "<cml", "<bond", ".dtd\"", "<list>", "<entry", "<identifier", "http://www.xml-cml.org/schema/cml2/core"],
"gaussianContainsRecords", ["Gaussian", "Entering Gaussian System", "Entering Link 1", "1998 Gaussian, Inc."],
"bilbaoContainsRecords", ["Bilbao", ">Bilbao Crystallographic Server<"],
"gaussianFchkContainsRecords", ["GaussianFchk", "Number of point charges in /Mol/"],
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
"crystalContainsRecords", ["Crystal", "*                                CRYSTAL", "TORINO", "DOVESI"],
"espressoContainsRecords", ["Espresso", "Program PWSCF", "Program PHONON"],
"siestaContainsRecords", ["Siesta", "MD.TypeOfRun", "SolutionMethod", "MeshCutoff", "WELCOME TO SIESTA"],
"xcrysDenContainsRecords", ["Xcrysden", "PRIMVEC", "CONVVEC", "PRIMCOORD", "ANIMSTEP"],
"mopacArchiveContainsRecords", ["MopacArchive", "SUMMARY OF PM"],
"abinitContainsRecords", ["Abinit", "http://www.abinit.org", "Catholique", "Louvain"]);
c$.headerContainsRecords = c$.prototype.headerContainsRecords = [J.adapter.smarter.Resolver.sptContainsRecords, J.adapter.smarter.Resolver.bilbaoContainsRecords, J.adapter.smarter.Resolver.xmlContainsRecords, J.adapter.smarter.Resolver.gaussianContainsRecords, J.adapter.smarter.Resolver.ampacContainsRecords, J.adapter.smarter.Resolver.mopacContainsRecords, J.adapter.smarter.Resolver.qchemContainsRecords, J.adapter.smarter.Resolver.gamessUKContainsRecords, J.adapter.smarter.Resolver.gamessUSContainsRecords, J.adapter.smarter.Resolver.spartanBinaryContainsRecords, J.adapter.smarter.Resolver.spartanContainsRecords, J.adapter.smarter.Resolver.mol2Records, J.adapter.smarter.Resolver.adfContainsRecords, J.adapter.smarter.Resolver.psiContainsRecords, J.adapter.smarter.Resolver.nwchemContainsRecords, J.adapter.smarter.Resolver.uicrcifContainsRecords, J.adapter.smarter.Resolver.dgridContainsRecords, J.adapter.smarter.Resolver.crystalContainsRecords, J.adapter.smarter.Resolver.dmolContainsRecords, J.adapter.smarter.Resolver.gulpContainsRecords, J.adapter.smarter.Resolver.espressoContainsRecords, J.adapter.smarter.Resolver.siestaContainsRecords, J.adapter.smarter.Resolver.xcrysDenContainsRecords, J.adapter.smarter.Resolver.mopacArchiveContainsRecords, J.adapter.smarter.Resolver.abinitContainsRecords, J.adapter.smarter.Resolver.gaussianFchkContainsRecords];
});
