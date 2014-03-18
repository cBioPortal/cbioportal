Clazz.declarePackage ("J.viewer");
Clazz.load (["java.io.BufferedInputStream", "java.util.Properties", "J.util.Elements", "$.SB", "$.V3"], "J.viewer.JC", ["java.lang.NullPointerException", "java.util.Hashtable", "J.util.Logger", "$.Parser"], function () {
c$ = Clazz.declareType (J.viewer, "JC");
c$.embedScript = $_M(c$, "embedScript", 
function (s) {
return "\n/**" + "**** Jmol Embedded Script ****" + " \n" + s + "\n**/";
}, "~S");
c$.getSpecialAtomName = $_M(c$, "getSpecialAtomName", 
function (atomID) {
return J.viewer.JC.specialAtomNames[atomID];
}, "~N");
c$.getSpecialAtomNames = $_M(c$, "getSpecialAtomNames", 
($fz = function () {
($t$ = J.viewer.JC.htSpecialAtoms =  new java.util.Hashtable (), J.viewer.JC.prototype.htSpecialAtoms = J.viewer.JC.htSpecialAtoms, $t$);
for (var i = J.viewer.JC.specialAtomNames.length; --i >= 0; ) {
var specialAtomName = J.viewer.JC.specialAtomNames[i];
if (specialAtomName != null) J.viewer.JC.htSpecialAtoms.put (specialAtomName, Integer.$valueOf (i));
}
}, $fz.isPrivate = true, $fz));
c$.lookupSpecialAtomID = $_M(c$, "lookupSpecialAtomID", 
function (atomName) {
if (J.viewer.JC.htSpecialAtoms == null) J.viewer.JC.getSpecialAtomNames ();
var boxedAtomID = J.viewer.JC.htSpecialAtoms.get (atomName);
if (boxedAtomID != null) return (boxedAtomID.intValue ());
return 0;
}, "~S");
c$.getAminoAcidValenceAndCharge = $_M(c$, "getAminoAcidValenceAndCharge", 
function (res, name, ret) {
if (res == null || res.length == 0 || res.length > 3 || name.equals ("CA") || name.equals ("CB")) return false;
var ch0 = name.charAt (0);
var ch1 = (name.length == 1 ? '\0' : name.charAt (1));
var isSp2 = false;
var bondCount = ret[3];
switch (res.length) {
case 3:
if (name.length == 1) {
switch (ch0) {
case 'N':
if (bondCount > 1) return false;
ret[1] = 1;
break;
case 'O':
isSp2 = ("HOH;DOD;WAT".indexOf (res) < 0);
break;
default:
isSp2 = true;
}
} else {
var id = res + ch0;
isSp2 = ("ARGN;ASNN;ASNO;ASPO;GLNN;GLNO;GLUO;HISN;HISC;PHECTRPC;TRPN;TYRC".indexOf (id) >= 0);
if ("LYSN".indexOf (id) >= 0) {
ret[1] = 1;
} else if (ch0 == 'O' && ch1 == 'X') {
ret[1] = -1;
}}break;
case 1:
case 2:
if (name.length > 2 && name.charAt (2) == '\'') return false;
switch (ch0) {
case 'C':
if (ch1 == '7') return false;
break;
case 'N':
switch (ch1) {
case '1':
case '3':
if ("A3;A1;C3;G3;I3".indexOf ("" + res.charAt (res.length - 1) + ch1) >= 0) ret[0]--;
break;
case '7':
ret[0]--;
break;
}
break;
}
isSp2 = true;
}
if (isSp2) {
switch (ch0) {
case 'N':
ret[2] = 2;
break;
case 'C':
ret[2] = 2;
ret[0]--;
break;
case 'O':
ret[0]--;
break;
}
}return true;
}, "~S,~S,~A");
c$.getStandardPdbHydrogenCount = $_M(c$, "getStandardPdbHydrogenCount", 
function (pt) {
return (pt < 0 || pt >= J.viewer.JC.pdbHydrogenCount.length ? -1 : J.viewer.JC.pdbHydrogenCount[pt]);
}, "~N");
c$.getPdbBondInfo = $_M(c$, "getPdbBondInfo", 
function (pt, isLegacy) {
if (pt < 0 || pt > J.viewer.JC.pdbBondInfo.length) return null;
var s = J.viewer.JC.pdbBondInfo[pt];
if (isLegacy && (pt = s.indexOf ("O3'")) >= 0) s = s.substring (0, pt);
var temp = J.util.Parser.getTokens (s);
var info =  new Array (Clazz.doubleToInt (temp.length / 2));
for (var i = 0, p = 0; i < info.length; i++) {
var source = temp[p++];
var target = temp[p++];
if (target.length == 1) switch (target.charAt (0)) {
case 'N':
target = "H@H2";
break;
case 'B':
target = "HB3@HB2";
break;
case 'D':
target = "HD2@HD3";
break;
case 'G':
target = "HG3@HG2";
break;
case '2':
target = "H2''@H2'";
break;
case '5':
target = "H5''@H5'";
break;
}
if (target.charAt (0) != 'H' && source.compareTo (target) > 0) {
s = target;
target = source;
source = s;
}info[i] = [source, target, (target.startsWith ("H") ? "1" : "2")];
}
return info;
}, "~N,~B");
c$.checkCarbohydrate = $_M(c$, "checkCarbohydrate", 
function (group3) {
return (group3 != null && ",[AHR],[ALL],[AMU],[ARA],[ARB],[BDF],[BDR],[BGC],[BMA],[FCA],[FCB],[FRU],[FUC],[FUL],[GAL],[GLA],[GLC],[GXL],[GUP],[LXC],[MAN],[RAM],[RIB],[RIP],[XYP],[XYS],[CBI],[CT3],[CTR],[CTT],[LAT],[MAB],[MAL],[MLR],[MTT],[SUC],[TRE],[GCU],[MTL],[NAG],[NDG],[RHA],[SOR],[SOL],[SOE],[XYL],[A2G],[LBT],[NGA],[SIA],[SLB],[AFL],[AGC],[GLB],[NAN],[RAA]".indexOf ("[" + group3.toUpperCase () + "]") >= 0);
}, "~S");
c$.getGroup3List = $_M(c$, "getGroup3List", 
function () {
if (J.viewer.JC.group3List != null) return J.viewer.JC.group3List;
var s =  new J.util.SB ();
for (var i = 1; i < 42; i++) s.append (",[").append ((J.viewer.JC.predefinedGroup3Names[i] + "   ").substring (0, 3) + "]");

s.append (",[AHR],[ALL],[AMU],[ARA],[ARB],[BDF],[BDR],[BGC],[BMA],[FCA],[FCB],[FRU],[FUC],[FUL],[GAL],[GLA],[GLC],[GXL],[GUP],[LXC],[MAN],[RAM],[RIB],[RIP],[XYP],[XYS],[CBI],[CT3],[CTR],[CTT],[LAT],[MAB],[MAL],[MLR],[MTT],[SUC],[TRE],[GCU],[MTL],[NAG],[NDG],[RHA],[SOR],[SOL],[SOE],[XYL],[A2G],[LBT],[NGA],[SIA],[SLB],[AFL],[AGC],[GLB],[NAN],[RAA]");
($t$ = J.viewer.JC.group3Count = Clazz.doubleToInt (s.length () / 6), J.viewer.JC.prototype.group3Count = J.viewer.JC.group3Count, $t$);
return ($t$ = J.viewer.JC.group3List = s.toString (), J.viewer.JC.prototype.group3List = J.viewer.JC.group3List, $t$);
});
c$.isHetero = $_M(c$, "isHetero", 
function (group3) {
return J.viewer.JC.getGroup3Pt (group3) >= 42;
}, "~S");
c$.getGroup3Pt = $_M(c$, "getGroup3Pt", 
($fz = function (group3) {
J.viewer.JC.getGroup3List ();
var sb =  new J.util.SB ().append ("[");
sb.append (group3);
switch (group3.length) {
case 1:
sb.append ("  ");
break;
case 2:
sb.append (" ");
break;
}
var pt = J.viewer.JC.group3List.indexOf (sb.toString ());
return (pt < 0 ? 2147483647 : Clazz.doubleToInt (pt / 6) + 1);
}, $fz.isPrivate = true, $fz), "~S");
c$.getGroup3Count = $_M(c$, "getGroup3Count", 
function () {
if (J.viewer.JC.group3Count > 0) return J.viewer.JC.group3Count;
J.viewer.JC.getGroup3List ();
return ($t$ = J.viewer.JC.group3Count = Clazz.doubleToInt (J.viewer.JC.group3List.length / 6), J.viewer.JC.prototype.group3Count = J.viewer.JC.group3Count, $t$);
});
c$.isShapeSecondary = $_M(c$, "isShapeSecondary", 
function (i) {
return i >= 9 && i < 16;
}, "~N");
c$.shapeTokenIndex = $_M(c$, "shapeTokenIndex", 
function (tok) {
switch (tok) {
case 1141899265:
case 1073741860:
return 0;
case 1678770178:
case 659488:
return 1;
case 1612189718:
return 2;
case 1611141176:
return 3;
case 1708058:
return 4;
case 1826248715:
return 5;
case 1746538509:
case 537006096:
return 6;
case 1113200652:
return 7;
case 1113200646:
return 8;
case 1115297793:
return 9;
case 1113200654:
return 10;
case 1113200642:
return 11;
case 1650071565:
return 12;
case 1113200647:
return 13;
case 1113200649:
return 14;
case 1113200650:
return 15;
case 1113198595:
return 16;
case 135175:
return 17;
case 135198:
return 18;
case 1113198597:
return 19;
case 1113198596:
return 20;
case 135192:
return 21;
case 135174:
return 23;
case 135176:
return 22;
case 135180:
return 24;
case 135402505:
return 25;
case 135182:
return 26;
case 1183762:
return 27;
case 135188:
return 28;
case 135190:
return 29;
case 537022465:
return 30;
case 1611272194:
return 31;
case 1679429641:
return 32;
case 1614417948:
return 33;
case 544771:
return 34;
case 1611272202:
return 35;
}
return -1;
}, "~N");
c$.getShapeClassName = $_M(c$, "getShapeClassName", 
function (shapeID, isRenderer) {
if (shapeID < 0) return J.viewer.JC.shapeClassBases[~shapeID];
return "J." + (isRenderer ? "render" : "shape") + (shapeID >= 9 && shapeID < 16 ? "bio." : shapeID >= 16 && shapeID < 23 ? "special." : shapeID >= 24 && shapeID < 29 ? "surface." : shapeID == 23 ? "cgo." : ".") + J.viewer.JC.shapeClassBases[shapeID];
}, "~N,~B");
c$.getShapeVisibilityFlag = $_M(c$, "getShapeVisibilityFlag", 
function (shapeID) {
return (4 << shapeID);
}, "~N");
Clazz.defineStatics (c$,
"copyright", "(C) 2012 Jmol Development",
"version", null,
"date", null);
{
var tmpVersion = null;
var tmpDate = null;
var bis = null;
var is = null;
try {
{
tmpVersion = ___JmolVersion; tmpDate = ___JmolDate;
}if (tmpDate != null) {
tmpDate = tmpDate.substring (8, 24);
tmpVersion = tmpVersion.substring (1, tmpVersion.length - 1);
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
} finally {
tmpVersion = ___JmolVersion; tmpDate = ___JmolDate;
}
($t$ = J.viewer.JC.version = (tmpVersion != null ? tmpVersion : "(Unknown version)"), J.viewer.JC.prototype.version = J.viewer.JC.version, $t$);
($t$ = J.viewer.JC.date = (tmpDate != null ? tmpDate : "(Unknown date)"), J.viewer.JC.prototype.date = J.viewer.JC.date, $t$);
}Clazz.defineStatics (c$,
"officialRelease", false,
"CLASSBASE_OPTIONS", "J.",
"DEFAULT_HELP_PATH", "http://chemapps.stolaf.edu/jmol/docs/index.htm",
"STATE_VERSION_STAMP", "# Jmol state version ",
"EMBEDDED_SCRIPT_TAG", "**** Jmol Embedded Script ****",
"NOTE_SCRIPT_FILE", "NOTE: file recognized as a script file: ",
"SCRIPT_EDITOR_IGNORE", "\1## EDITOR_IGNORE ##",
"REPAINT_IGNORE", "\1## REPAINT_IGNORE ##",
"LOAD_ATOM_DATA_TYPES", ";xyz;vxyz;vibration;temperature;occupancy;partialcharge;",
"radiansPerDegree", (0.017453292519943295),
"allowedQuaternionFrames", "RC;RP;a;b;c;n;p;q;x;",
"EXPORT_DRIVER_LIST", "Idtf;Maya;Povray;Vrml;X3d;Tachyon;Obj");
c$.center = c$.prototype.center = J.util.V3.new3 (0, 0, 0);
c$.axisX = c$.prototype.axisX = J.util.V3.new3 (1, 0, 0);
c$.axisY = c$.prototype.axisY = J.util.V3.new3 (0, 1, 0);
c$.axisZ = c$.prototype.axisZ = J.util.V3.new3 (0, 0, 1);
c$.axisNX = c$.prototype.axisNX = J.util.V3.new3 (-1, 0, 0);
c$.axisNY = c$.prototype.axisNY = J.util.V3.new3 (0, -1, 0);
c$.axisNZ = c$.prototype.axisNZ = J.util.V3.new3 (0, 0, -1);
c$.unitAxisVectors = c$.prototype.unitAxisVectors = [J.viewer.JC.axisX, J.viewer.JC.axisY, J.viewer.JC.axisZ, J.viewer.JC.axisNX, J.viewer.JC.axisNY, J.viewer.JC.axisNZ];
Clazz.defineStatics (c$,
"XY_ZTOP", 100,
"DEFAULT_PERCENT_VDW_ATOM", 23,
"DEFAULT_BOND_RADIUS", 0.15,
"DEFAULT_BOND_MILLIANGSTROM_RADIUS", Clazz.floatToShort (150.0),
"DEFAULT_STRUT_RADIUS", 0.3,
"DEFAULT_BOND_TOLERANCE", 0.45,
"DEFAULT_MIN_BOND_DISTANCE", 0.4,
"DEFAULT_MAX_CONNECT_DISTANCE", 100000000,
"DEFAULT_MIN_CONNECT_DISTANCE", 0.1,
"MINIMIZATION_ATOM_MAX", 200,
"MINIMIZE_FIXED_RANGE", 5.0,
"MOUSE_NONE", -1,
"MULTIBOND_NEVER", 0,
"MULTIBOND_WIREFRAME", 1,
"MULTIBOND_NOTSMALL", 2,
"MULTIBOND_ALWAYS", 3,
"MAXIMUM_AUTO_BOND_COUNT", 20,
"madMultipleBondSmallMaximum", 500,
"ANGSTROMS_PER_BOHR", 0.5291772,
"altArgbsCpk", [0xFFFF1493, 0xFFBFA6A6, 0xFFFFFF30, 0xFF57178F, 0xFFFFFFC0, 0xFFFFFFA0, 0xFFD8D8D8, 0xFF505050, 0xFF404040, 0xFF105050],
"argbsAmino", [0xFFBEA06E, 0xFFC8C8C8, 0xFF145AFF, 0xFF00DCDC, 0xFFE60A0A, 0xFFE6E600, 0xFF00DCDC, 0xFFE60A0A, 0xFFEBEBEB, 0xFF8282D2, 0xFF0F820F, 0xFF0F820F, 0xFF145AFF, 0xFFE6E600, 0xFF3232AA, 0xFFDC9682, 0xFFFA9600, 0xFFFA9600, 0xFFB45AB4, 0xFF3232AA, 0xFF0F820F, 0xFFFF69B4, 0xFFFF69B4, 0xFFBEA06E],
"argbShapelyBackbone", 0xFFB8B8B8,
"argbShapelySpecial", 0xFF5E005E,
"argbShapelyDefault", 0xFFFF00FF,
"argbsChainAtom", [0xFFffffff, 0xFFC0D0FF, 0xFFB0FFB0, 0xFFFFC0C8, 0xFFFFFF80, 0xFFFFC0FF, 0xFFB0F0F0, 0xFFFFD070, 0xFFF08080, 0xFFF5DEB3, 0xFF00BFFF, 0xFFCD5C5C, 0xFF66CDAA, 0xFF9ACD32, 0xFFEE82EE, 0xFF00CED1, 0xFF00FF7F, 0xFF3CB371, 0xFF00008B, 0xFFBDB76B, 0xFF006400, 0xFF800000, 0xFF808000, 0xFF800080, 0xFF008080, 0xFFB8860B, 0xFFB22222],
"argbsChainHetero", [0xFFffffff, -7298865, -8335464, -3174224, -3158160, -3174193, -8339264, -3170208, -4173712, -3821949, -16734257, -4895668, -11094638, -7686870, -4296002, -16730463, -16724113, -13329567, -16777029, -5922981, -16739328, -5242880, -5197824, -5242704, -16731984, -1526253, -4050382],
"argbsFormalCharge", [0xFFFF0000, 0xFFFF4040, 0xFFFF8080, 0xFFFFC0C0, 0xFFFFFFFF, 0xFFD8D8FF, 0xFFB4B4FF, 0xFF9090FF, 0xFF6C6CFF, 0xFF4848FF, 0xFF2424FF, 0xFF0000FF],
"argbsRwbScale", [0xFFFF0000, 0xFFFF1010, 0xFFFF2020, 0xFFFF3030, 0xFFFF4040, 0xFFFF5050, 0xFFFF6060, 0xFFFF7070, 0xFFFF8080, 0xFFFF9090, 0xFFFFA0A0, 0xFFFFB0B0, 0xFFFFC0C0, 0xFFFFD0D0, 0xFFFFE0E0, 0xFFFFFFFF, 0xFFE0E0FF, 0xFFD0D0FF, 0xFFC0C0FF, 0xFFB0B0FF, 0xFFA0A0FF, 0xFF9090FF, 0xFF8080FF, 0xFF7070FF, 0xFF6060FF, 0xFF5050FF, 0xFF4040FF, 0xFF3030FF, 0xFF2020FF, 0xFF1010FF, 0xFF0000FF]);
c$.FORMAL_CHARGE_COLIX_RED = c$.prototype.FORMAL_CHARGE_COLIX_RED = J.util.Elements.elementSymbols.length;
c$.PARTIAL_CHARGE_COLIX_RED = c$.prototype.PARTIAL_CHARGE_COLIX_RED = J.util.Elements.elementSymbols.length + J.viewer.JC.argbsFormalCharge.length;
c$.PARTIAL_CHARGE_RANGE_SIZE = c$.prototype.PARTIAL_CHARGE_RANGE_SIZE = J.viewer.JC.argbsRwbScale.length;
Clazz.defineStatics (c$,
"argbsRoygbScale", [0xFFFF0000, 0xFFFF2000, 0xFFFF4000, 0xFFFF6000, 0xFFFF8000, 0xFFFFA000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFFF00, 0xFFF0F000, 0xFFE0FF00, 0xFFC0FF00, 0xFFA0FF00, 0xFF80FF00, 0xFF60FF00, 0xFF40FF00, 0xFF20FF00, 0xFF00FF00, 0xFF00FF20, 0xFF00FF40, 0xFF00FF60, 0xFF00FF80, 0xFF00FFA0, 0xFF00FFC0, 0xFF00FFE0, 0xFF00FFFF, 0xFF00E0FF, 0xFF00C0FF, 0xFF00A0FF, 0xFF0080FF, 0xFF0060FF, 0xFF0040FF, 0xFF0020FF, 0xFF0000FF],
"argbsIsosurfacePositive", 0xFF5020A0,
"argbsIsosurfaceNegative", 0xFFA02050,
"specialAtomNames", [null, "N", "CA", "C", "O", "O1", "O5'", "C5'", "C4'", "C3'", "O3'", "C2'", "C1'", "P", "OD1", "OD2", "OE1", "OE2", "SG", null, null, null, null, null, null, null, null, null, null, null, null, null, "N1", "C2", "N3", "C4", "C5", "C6", "O2", "N7", "C8", "N9", "N4", "N2", "N6", "C5M", "O6", "O4", "S4", "C7", "H1", "H2", "H3", null, null, null, null, null, null, null, null, null, null, null, "OXT", "H", "1H", "2H", "3H", "HA", "1HA", "2HA", "H5T", "O5T", "O1P", "OP1", "O2P", "OP2", "O4'", "O2'", "1H5'", "2H5'", "H4'", "H3'", "1H2'", "2H2'", "2HO'", "H1'", "H3T", "HO3'", "HO5'", "HA2", "HA3", "HA2", "H5'", "H5''", "H2'", "H2''", "HO2'", "O3P", "OP3"]);
c$.ATOMID_MAX = c$.prototype.ATOMID_MAX = J.viewer.JC.specialAtomNames.length;
Clazz.defineStatics (c$,
"ATOMID_AMINO_NITROGEN", 1,
"ATOMID_ALPHA_CARBON", 2,
"ATOMID_CARBONYL_CARBON", 3,
"ATOMID_CARBONYL_OXYGEN", 4,
"ATOMID_O1", 5,
"ATOMID_ALPHA_ONLY_MASK", 4,
"ATOMID_PROTEIN_MASK", 14,
"ATOMID_O5_PRIME", 6,
"ATOMID_C4_PRIME", 8,
"ATOMID_C3_PRIME", 9,
"ATOMID_O3_PRIME", 10,
"ATOMID_C1_PRIME", 12,
"ATOMID_NUCLEIC_MASK", 8128,
"ATOMID_NUCLEIC_PHOSPHORUS", 13,
"ATOMID_PHOSPHORUS_ONLY_MASK", 8192,
"ATOMID_DISTINGUISHING_ATOM_MAX", 14,
"ATOMID_CARBONYL_OD1", 14,
"ATOMID_CARBONYL_OD2", 15,
"ATOMID_CARBONYL_OE1", 16,
"ATOMID_CARBONYL_OE2", 17,
"ATOMID_SG", 18,
"ATOMID_N1", 32,
"ATOMID_C2", 33,
"ATOMID_N3", 34,
"ATOMID_C4", 35,
"ATOMID_C5", 36,
"ATOMID_C6", 37,
"ATOMID_O2", 38,
"ATOMID_N7", 39,
"ATOMID_C8", 40,
"ATOMID_N9", 41,
"ATOMID_N4", 42,
"ATOMID_N2", 43,
"ATOMID_N6", 44,
"ATOMID_C5M", 45,
"ATOMID_O6", 46,
"ATOMID_O4", 47,
"ATOMID_S4", 48,
"ATOMID_C7", 49,
"ATOMID_TERMINATING_OXT", 64,
"ATOMID_H5T_TERMINUS", 72,
"ATOMID_O5T_TERMINUS", 73,
"ATOMID_O1P", 74,
"ATOMID_OP1", 75,
"ATOMID_O2P", 76,
"ATOMID_OP2", 77,
"ATOMID_O2_PRIME", 79,
"ATOMID_H3T_TERMINUS", 88,
"ATOMID_HO3_PRIME", 89,
"ATOMID_HO5_PRIME", 90,
"htSpecialAtoms", null,
"GROUPID_ARGININE", 2,
"GROUPID_ASPARAGINE", 3,
"GROUPID_ASPARTATE", 4,
"GROUPID_CYSTEINE", 5,
"GROUPID_GLUTAMINE", 6,
"GROUPID_GLUTAMATE", 7,
"GROUPID_HISTIDINE", 9,
"GROUPID_LYSINE", 12,
"GROUPID_PROLINE", 15,
"GROUPID_TRYPTOPHAN", 19,
"GROUPID_AMINO_MAX", 24,
"GROUPID_NUCLEIC_MAX", 42,
"GROUPID_WATER", 42,
"GROUPID_SOLVENT_MIN", 45,
"GROUPID_ION_MIN", 46,
"GROUPID_ION_MAX", 48,
"predefinedGroup3Names", ["", "ALA", "ARG", "ASN", "ASP", "CYS", "GLN", "GLU", "GLY", "HIS", "ILE", "LEU", "LYS", "MET", "PHE", "PRO", "SER", "THR", "TRP", "TYR", "VAL", "ASX", "GLX", "UNK", "G", "C", "A", "T", "U", "I", "DG", "DC", "DA", "DT", "DU", "DI", "+G", "+C", "+A", "+T", "+U", "+I", "HOH", "DOD", "WAT", "UREA", "PO4", "SO4", "UNL"],
"naNoH", "A3;A1;C3;G3;I3",
"aaSp2", "ARGN;ASNN;ASNO;ASPO;GLNN;GLNO;GLUO;HISN;HISC;PHECTRPC;TRPN;TYRC",
"aaPlus", "LYSN",
"pdbBondInfo", ["", "N N CA HA C O CB HB?", "N N CA HA C O CB HB2@HB3 CG HG2@HG3 CD D NE HE CZ NH1 NH1 HH11@HH12 NH2 HH21@HH22", "N N CA HA C O CB B CG OD1 ND2 HD21@HD22", "N N CA HA C O CB B CG OD1", "N N CA HA C O CB B SG HG", "N N CA HA C O CB B CG G CD OE1 NE2 HE21@HE22", "N N CA HA C O CB B CG G CD OE1", "N N CA HA2@HA3 C O", "N N CA HA C O CB B CG CD2 ND1 CE1 ND1 HD1 CD2 HD2 CE1 HE1 NE2 HE2", "N N CA HA C O CB HB CG1 HG12@HG13 CG2 HG2? CD1 HD1?", "N N CA HA C O CB HB2@HB3 CG HG CD1 HD1? CD2 HD2?", "N N CA HA C O CB B CG G CD HD2@HD3 CE HE3@HE2 NZ HZ?", "N N CA HA C O CB HB2@HB3 CG HG2@HG3 CE HE?", "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 CD2 HD2 CE1 CZ CE1 HE1 CE2 HE2 CZ HZ", "N H CA HA C O CB B CG G CD HD2@HD3", "N N CA HA C O CB B OG HG", "N N CA HA C O CB HB OG1 HG1 CG2 HG2?", "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 NE1 HE1 CE3 CZ3 CE3 HE3 CZ2 CH2 CZ2 HZ2 CZ3 HZ3 CH2 HH2", "N N CA HA C O CB B CG CD1 CD1 HD1 CD2 CE2 CD2 HD2 CE1 CZ CE1 HE1 CE2 HE2 OH HH", "N N CA HA C O CB HB CG1 HG1? CG2 HG2?", "CA HA C O CB HB2@HB1 C H", "CA HA C O CB HB1 CB HB2 CG HG1 CG HG2", "", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 N2 H21@H22 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C2 O2 N3 C4 N4 H41@H42 C5 C6 C5 H5 C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 N1 N6 H61@H62 C2 N3 C2 H2 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C7 H7? C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C5 H5 C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2' O2' HO2' C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 C2 H2 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 N2 H21@H22 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C2 O2 N3 C4 N4 H41@H42 C5 C6 C5 H5 C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 N1 N6 H61@H62 C2 N3 C2 H2 O3' HO3' O5' HO5'", "P OP1 C5' H5'@H5'' C4' H4' C3' H3' C2' H2'@H2'' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C7 H7? C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' H2'@H2'' C1' H1' C2 O2 N3 H3 C4 O4 C5 C6 C5 H5 C6 H6 O3' HO3' O5' HO5'", "P OP1 C5' 5 C4' H4' C3' H3' C2' 2 C1' H1' C8 N7 C8 H8 C5 C4 C6 O6 N1 H1 C2 N3 C2 H2 O3' HO3' O5' HO5'"],
"pdbHydrogenCount", [0, 6, 16, 7, 6, 6, 9, 8, 4, 9, 12, 12, 14, 10, 10, 8, 6, 8, 11, 10, 10, 3, 5, 0, 13, 13, 13, -1, 12, 12, 13, 13, 13, 14, 12, 12],
"argbsShapely", [0xFFFF00FF, 0xFF00007C, 0xFFFF7C70, 0xFF8CFF8C, 0xFFA00042, 0xFFFFFF70, 0xFFFF4C4C, 0xFF660000, 0xFFFFFFFF, 0xFF7070FF, 0xFF004C00, 0xFF455E45, 0xFF4747B8, 0xFF534C52, 0xFFB8A042, 0xFF525252, 0xFFFF7042, 0xFFB84C00, 0xFF4F4600, 0xFF8C704C, 0xFFFF8CFF, 0xFFFF00FF, 0xFFFF00FF, 0xFFFF00FF, 0xFFFF7070, 0xFFFF8C4B, 0xFFA0A0FF, 0xFFA0FFA0, 0xFFFF8080, 0xFF80FFFF, 0xFFFF7070, 0xFFFF8C4B, 0xFFA0A0FF, 0xFFA0FFA0, 0xFFFF8080, 0xFF80FFFF, 0xFFFF7070, 0xFFFF8C4B, 0xFFA0A0FF, 0xFFA0FFA0, 0xFFFF8080, 0xFF80FFFF],
"allCarbohydrates", ",[AHR],[ALL],[AMU],[ARA],[ARB],[BDF],[BDR],[BGC],[BMA],[FCA],[FCB],[FRU],[FUC],[FUL],[GAL],[GLA],[GLC],[GXL],[GUP],[LXC],[MAN],[RAM],[RIB],[RIP],[XYP],[XYS],[CBI],[CT3],[CTR],[CTT],[LAT],[MAB],[MAL],[MLR],[MTT],[SUC],[TRE],[GCU],[MTL],[NAG],[NDG],[RHA],[SOR],[SOL],[SOE],[XYL],[A2G],[LBT],[NGA],[SIA],[SLB],[AFL],[AGC],[GLB],[NAN],[RAA]",
"group3List", null,
"group3Count", 0,
"predefinedGroup1Names", ['\0', 'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', 'A', 'G', '?', 'G', 'C', 'A', 'T', 'U', 'I', 'G', 'C', 'A', 'T', 'U', 'I', 'G', 'C', 'A', 'T', 'U', 'I', 'I'],
"predefinedVariable", ["@_1H _H & !(_2H,_3H)", "@_12C _C & !(_13C,_14C)", "@_14N _N & !(_15N)", "@water _g>=42 & _g<45, oxygen & connected(2) & connected(2, hydrogen), (hydrogen) & connected(oxygen & connected(2) & connected(2, hydrogen))", "@solvent water, (_g>=45 & _g<48)", "@ligand _g=0|!(_g<46,protein,nucleic,water)", "@turn structure=1", "@sheet structure=2", "@helix structure=3", "@helix310 substructure=7", "@helixalpha substructure=8", "@helixpi substructure=9", "@bonded bondcount>0"],
"predefinedStatic", ["@amino _g>0 & _g<=23", "@acidic asp,glu", "@basic arg,his,lys", "@charged acidic,basic", "@negative acidic", "@positive basic", "@neutral amino&!(acidic,basic)", "@polar amino&!hydrophobic", "@cyclic his,phe,pro,trp,tyr", "@acyclic amino&!cyclic", "@aliphatic ala,gly,ile,leu,val", "@aromatic his,phe,trp,tyr", "@cystine within(group, (cys.sg or cyx.sg) and connected(cys.sg or cyx.sg))", "@buried ala,cys,ile,leu,met,phe,trp,val", "@surface amino&!buried", "@hydrophobic ala,gly,ile,leu,met,phe,pro,trp,tyr,val", "@mainchain backbone", "@small ala,gly,ser", "@medium asn,asp,cys,pro,thr,val", "@large arg,glu,gln,his,ile,leu,lys,met,phe,trp,tyr", "@c nucleic & ([C] or [DC] or within(group,_a=42))", "@g nucleic & ([G] or [DG] or within(group,_a=43))", "@cg c,g", "@a nucleic & ([A] or [DA] or within(group,_a=44))", "@t nucleic & ([T] or [DT] or within(group,_a=45 | _a=49))", "@at a,t", "@i nucleic & ([I] or [DI] or within(group,_a=46) & !g)", "@u nucleic & ([U] or [DU] or within(group,_a=47) & !t)", "@tu nucleic & within(group,_a=48)", "@ions _g>=46&_g<48", "@alpha _a=2", "@backbone protein&(_a>=1&_a<6|_a>=64&_a<72)|nucleic&(_a>=6&_a<14|_a>=72)", "@spine protein&_a>=1&_a<4|nucleic&_a>=6&_a<14&_a!=12", "@sidechain (protein,nucleic) & !backbone", "@base nucleic & !backbone", "@dynamic_flatring search('[a]')"],
"databases", ["ligand", "http://www.rcsb.org/pdb/files/ligand/%FILE.cif", "mp", "http://www.materialsproject.org/materials/%FILE/cif", "nci", "http://cactus.nci.nih.gov/chemical/structure/%FILE", "nmr", "http://www.nmrdb.org/predictor?POST?molfile=", "nmrdb", "http://www.nmrdb.org/service/predictor?POST?molfile=", "pdb", "http://www.rcsb.org/pdb/files/%FILE.pdb.gz", "pubchem", "http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/%FILE/SDF?record_type=3d"],
"MODELKIT_ZAP_STRING", "5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63",
"MODELKIT_ZAP_TITLE", "Jmol Model Kit",
"ADD_HYDROGEN_TITLE", "Viewer.AddHydrogens",
"DEFAULT_FONTFACE", "SansSerif",
"DEFAULT_FONTSTYLE", "Plain",
"LABEL_MINIMUM_FONTSIZE", 6,
"LABEL_MAXIMUM_FONTSIZE", 63,
"LABEL_DEFAULT_FONTSIZE", 13,
"LABEL_DEFAULT_X_OFFSET", 4,
"LABEL_DEFAULT_Y_OFFSET", 4,
"MEASURE_DEFAULT_FONTSIZE", 15,
"AXES_DEFAULT_FONTSIZE", 14,
"SHAPE_BALLS", 0,
"SHAPE_STICKS", 1,
"SHAPE_HSTICKS", 2,
"SHAPE_SSSTICKS", 3,
"SHAPE_STRUTS", 4,
"SHAPE_LABELS", 5,
"SHAPE_MEASURES", 6,
"SHAPE_STARS", 7,
"SHAPE_HALOS", 8,
"SHAPE_MIN_SECONDARY", 9,
"SHAPE_BACKBONE", 9,
"SHAPE_TRACE", 10,
"SHAPE_CARTOON", 11,
"SHAPE_STRANDS", 12,
"SHAPE_MESHRIBBON", 13,
"SHAPE_RIBBONS", 14,
"SHAPE_ROCKETS", 15,
"SHAPE_MAX_SECONDARY", 16,
"SHAPE_MIN_SPECIAL", 16,
"SHAPE_DOTS", 16,
"SHAPE_DIPOLES", 17,
"SHAPE_VECTORS", 18,
"SHAPE_GEOSURFACE", 19,
"SHAPE_ELLIPSOIDS", 20,
"SHAPE_MAX_SIZE_ZERO_ON_RESTRICT", 21,
"SHAPE_POLYHEDRA", 21,
"SHAPE_MIN_HAS_ID", 22,
"SHAPE_MIN_MESH_COLLECTION", 22,
"SHAPE_DRAW", 22,
"SHAPE_MAX_SPECIAL", 23,
"SHAPE_CGO", 23,
"SHAPE_MIN_SURFACE", 24,
"SHAPE_ISOSURFACE", 24,
"SHAPE_CONTACT", 25,
"SHAPE_LCAOCARTOON", 26,
"SHAPE_MO", 27,
"SHAPE_PMESH", 28,
"SHAPE_PLOT3D", 29,
"SHAPE_MAX_SURFACE", 29,
"SHAPE_MAX_MESH_COLLECTION", 29,
"SHAPE_ECHO", 30,
"SHAPE_MAX_HAS_ID", 31,
"SHAPE_AXES", 31,
"SHAPE_BBCAGE", 32,
"SHAPE_UCCAGE", 33,
"SHAPE_HOVER", 34,
"SHAPE_FRANK", 35,
"SHAPE_MAX", 36,
"shapeClassBases", ["Balls", "Sticks", "Hsticks", "Sssticks", "Struts", "Labels", "Measures", "Stars", "Halos", "Backbone", "Trace", "Cartoon", "Strands", "MeshRibbon", "Ribbons", "Rockets", "Dots", "Dipoles", "Vectors", "GeoSurface", "Ellipsoids", "Polyhedra", "Draw", "CGO", "Isosurface", "Contact", "LcaoCartoon", "MolecularOrbital", "Pmesh", "Plot3D", "Echo", "Axes", "Bbcage", "Uccage", "Hover", "Frank"],
"ATOM_IN_FRAME", 1,
"ATOM_SLABBED", 2,
"CURSOR_DEFAULT", 0,
"CURSOR_HAND", 1,
"CURSOR_CROSSHAIR", 2,
"CURSOR_MOVE", 3,
"CURSOR_WAIT", 4,
"CURSOR_ZOOM", 5,
"binaryExtensions", ";pse=PyMOL;",
"SCRIPT_COMPLETED", "Script completed",
"JPEG_EXTENSIONS", ";jpg;jpeg;jpg64;jpeg64;");
c$.IMAGE_TYPES = c$.prototype.IMAGE_TYPES = ";jpg;jpeg;jpg64;jpeg64;gif;ppm;png;pngj;pngt;";
c$.IMAGE_OR_SCENE = c$.prototype.IMAGE_OR_SCENE = ";jpg;jpeg;jpg64;jpeg64;gif;ppm;png;pngj;pngt;scene;";
{
if (J.viewer.JC.argbsFormalCharge.length != 12) {
J.util.Logger.error ("formal charge color table length");
throw  new NullPointerException ();
}if (J.viewer.JC.shapeClassBases.length != 36) {
J.util.Logger.error ("shapeClassBases wrong length");
throw  new NullPointerException ();
}if (J.viewer.JC.argbsAmino.length != 24) {
J.util.Logger.error ("argbsAmino wrong length");
throw  new NullPointerException ();
}if (J.viewer.JC.argbsShapely.length != 42) {
J.util.Logger.error ("argbsShapely wrong length");
throw  new NullPointerException ();
}if (J.viewer.JC.argbsChainHetero.length != J.viewer.JC.argbsChainAtom.length) {
J.util.Logger.error ("argbsChainHetero wrong length");
throw  new NullPointerException ();
}if (J.viewer.JC.shapeClassBases.length != 36) {
J.util.Logger.error ("the shapeClassBases array has the wrong length");
throw  new NullPointerException ();
}}});
