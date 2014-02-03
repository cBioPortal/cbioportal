Clazz.declarePackage ("J.util");
Clazz.load (["J.util.BS"], "J.util.Elements", ["java.lang.Character", "java.util.Hashtable", "J.util.Logger", "$.Parser"], function () {
c$ = Clazz.declareType (J.util, "Elements");
c$.getAtomicMass = $_M(c$, "getAtomicMass", 
function (i) {
return (i < 1 || i >= J.util.Elements.atomicMass.length ? 0 : J.util.Elements.atomicMass[i]);
}, "~N");
c$.elementNumberFromSymbol = $_M(c$, "elementNumberFromSymbol", 
function (elementSymbol, isSilent) {
if (J.util.Elements.htElementMap == null) {
var map =  new java.util.Hashtable ();
for (var elementNumber = J.util.Elements.elementNumberMax; --elementNumber >= 0; ) {
var symbol = J.util.Elements.elementSymbols[elementNumber];
var boxed = Integer.$valueOf (elementNumber);
map.put (symbol, boxed);
if (symbol.length == 2) map.put (symbol.toUpperCase (), boxed);
}
for (var i = J.util.Elements.altElementMax; --i >= 4; ) {
var symbol = J.util.Elements.altElementSymbols[i];
var boxed = Integer.$valueOf (J.util.Elements.altElementNumbers[i]);
map.put (symbol, boxed);
if (symbol.length == 2) map.put (symbol.toUpperCase (), boxed);
}
($t$ = J.util.Elements.htElementMap = map, J.util.Elements.prototype.htElementMap = J.util.Elements.htElementMap, $t$);
}if (elementSymbol == null) return 0;
var boxedAtomicNumber = J.util.Elements.htElementMap.get (elementSymbol);
if (boxedAtomicNumber != null) return boxedAtomicNumber.intValue ();
if (Character.isDigit (elementSymbol.charAt (0))) {
var pt = elementSymbol.length - 2;
if (pt >= 0 && Character.isDigit (elementSymbol.charAt (pt))) pt++;
var isotope = (pt > 0 ? J.util.Parser.parseInt (elementSymbol.substring (0, pt)) : 0);
if (isotope > 0) {
var n = J.util.Elements.elementNumberFromSymbol (elementSymbol.substring (pt), true);
if (n > 0) {
isotope = J.util.Elements.getAtomicAndIsotopeNumber (n, isotope);
J.util.Elements.htElementMap.put (elementSymbol.toUpperCase (), Integer.$valueOf (isotope));
return isotope;
}}}if (!isSilent) J.util.Logger.error ("'" + elementSymbol + "' is not a recognized symbol");
return 0;
}, "~S,~B");
c$.elementSymbolFromNumber = $_M(c$, "elementSymbolFromNumber", 
function (elemNo) {
var isoNumber = 0;
if (elemNo >= J.util.Elements.elementNumberMax) {
for (var j = J.util.Elements.altElementMax; --j >= 0; ) if (elemNo == J.util.Elements.altElementNumbers[j]) return J.util.Elements.altElementSymbols[j];

isoNumber = J.util.Elements.getIsotopeNumber (elemNo);
elemNo %= 128;
return "" + isoNumber + J.util.Elements.getElementSymbol (elemNo);
}return J.util.Elements.getElementSymbol (elemNo);
}, "~N");
c$.getElementSymbol = $_M(c$, "getElementSymbol", 
($fz = function (elemNo) {
if (elemNo < 0 || elemNo >= J.util.Elements.elementNumberMax) elemNo = 0;
return J.util.Elements.elementSymbols[elemNo];
}, $fz.isPrivate = true, $fz), "~N");
c$.elementNameFromNumber = $_M(c$, "elementNameFromNumber", 
function (elementNumber) {
if (elementNumber >= J.util.Elements.elementNumberMax) {
for (var j = J.util.Elements.altElementMax; --j >= 0; ) if (elementNumber == J.util.Elements.altElementNumbers[j]) return J.util.Elements.altElementNames[j];

elementNumber %= 128;
}if (elementNumber < 0 || elementNumber >= J.util.Elements.elementNumberMax) elementNumber = 0;
return J.util.Elements.elementNames[elementNumber];
}, "~N");
c$.altElementNameFromIndex = $_M(c$, "altElementNameFromIndex", 
function (i) {
return J.util.Elements.altElementNames[i];
}, "~N");
c$.altElementNumberFromIndex = $_M(c$, "altElementNumberFromIndex", 
function (i) {
return J.util.Elements.altElementNumbers[i];
}, "~N");
c$.altElementSymbolFromIndex = $_M(c$, "altElementSymbolFromIndex", 
function (i) {
return J.util.Elements.altElementSymbols[i];
}, "~N");
c$.altIsotopeSymbolFromIndex = $_M(c$, "altIsotopeSymbolFromIndex", 
function (i) {
var code = J.util.Elements.altElementNumbers[i];
return (code >> 7) + J.util.Elements.elementSymbolFromNumber (code & 127);
}, "~N");
c$.altIsotopeSymbolFromIndex2 = $_M(c$, "altIsotopeSymbolFromIndex2", 
function (i) {
var code = J.util.Elements.altElementNumbers[i];
return J.util.Elements.elementSymbolFromNumber (code & 127) + (code >> 7);
}, "~N");
c$.getElementNumber = $_M(c$, "getElementNumber", 
function (atomicAndIsotopeNumber) {
return atomicAndIsotopeNumber & 127;
}, "~N");
c$.getIsotopeNumber = $_M(c$, "getIsotopeNumber", 
function (atomicAndIsotopeNumber) {
return atomicAndIsotopeNumber >> 7;
}, "~N");
c$.getAtomicAndIsotopeNumber = $_M(c$, "getAtomicAndIsotopeNumber", 
function (n, mass) {
return ((n < 0 ? 0 : n) + (mass <= 0 ? 0 : mass << 7));
}, "~N,~N");
c$.altElementIndexFromNumber = $_M(c$, "altElementIndexFromNumber", 
function (atomicAndIsotopeNumber) {
for (var i = 0; i < J.util.Elements.altElementMax; i++) if (J.util.Elements.altElementNumbers[i] == atomicAndIsotopeNumber) return i;

return 0;
}, "~N");
c$.getNaturalIsotope = $_M(c$, "getNaturalIsotope", 
function (elementNumber) {
for (var i = 0; i < J.util.Elements.naturalIsotopeMasses.length; i += 2) if (J.util.Elements.naturalIsotopeMasses[i] == elementNumber) return J.util.Elements.naturalIsotopeMasses[++i];

return 0;
}, "~N");
c$.isNaturalIsotope = $_M(c$, "isNaturalIsotope", 
function (isotopeSymbol) {
return ("1H,12C,14N,".indexOf (isotopeSymbol + ",") >= 0);
}, "~S");
c$.getBondingRadiusFloat = $_M(c$, "getBondingRadiusFloat", 
function (atomicNumberAndIsotope, charge) {
var atomicNumber = atomicNumberAndIsotope & 127;
if (charge > 0 && J.util.Elements.bsCations.get (atomicNumber)) return J.util.Elements.getBondingRadFromTable (atomicNumber, charge, J.util.Elements.cationLookupTable);
if (charge < 0 && J.util.Elements.bsAnions.get (atomicNumber)) return J.util.Elements.getBondingRadFromTable (atomicNumber, charge, J.util.Elements.anionLookupTable);
return J.util.Elements.covalentMars[atomicNumber] / 1000;
}, "~N,~N");
c$.getBondingRadFromTable = $_M(c$, "getBondingRadFromTable", 
function (atomicNumber, charge, table) {
var ionic = (atomicNumber << 4) + (charge + 4);
var iVal = 0;
var iMid = 0;
var iMin = 0;
var iMax = Clazz.doubleToInt (table.length / 2);
while (iMin != iMax) {
iMid = Clazz.doubleToInt ((iMin + iMax) / 2);
iVal = table[iMid << 1];
if (iVal > ionic) iMax = iMid;
 else if (iVal < ionic) iMin = iMid + 1;
 else return table[(iMid << 1) + 1] / 1000;
}
if (iVal > ionic) iMid--;
iVal = table[iMid << 1];
if (atomicNumber != (iVal >> 4)) iMid++;
return table[(iMid << 1) + 1] / 1000;
}, "~N,~N,~A");
c$.getVanderwaalsMar = $_M(c$, "getVanderwaalsMar", 
function (atomicAndIsotopeNumber, type) {
return J.util.Elements.vanderwaalsMars[((atomicAndIsotopeNumber & 127) << 2) + (type.pt % 4)];
}, "~N,J.constant.EnumVdw");
c$.getHydrophobicity = $_M(c$, "getHydrophobicity", 
function (i) {
return (i < 1 || i >= J.util.Elements.hydrophobicities.length ? 0 : J.util.Elements.hydrophobicities[i]);
}, "~N");
c$.getAllredRochowElectroNeg = $_M(c$, "getAllredRochowElectroNeg", 
function (elemno) {
return (elemno > 0 && elemno < J.util.Elements.electroNegativities.length ? J.util.Elements.electroNegativities[elemno] : 0);
}, "~N");
c$.isElement = $_M(c$, "isElement", 
function (atomicAndIsotopeNumber, elemNo) {
return ((atomicAndIsotopeNumber & 127) == elemNo);
}, "~N,~N");
Clazz.defineStatics (c$,
"elementSymbols", ["Xx", "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt"],
"atomicMass", [0, 1.008, 4.003, 6.941, 9.012, 10.81, 12.011, 14.007, 15.999, 18.998, 20.18, 22.99, 24.305, 26.981, 28.086, 30.974, 32.07, 35.453, 39.948, 39.1, 40.08, 44.956, 47.88, 50.941, 52, 54.938, 55.847, 58.93, 58.69, 63.55, 65.39, 69.72, 72.61, 74.92, 78.96, 79.9, 83.8, 85.47, 87.62, 88.91, 91.22, 92.91, 95.94, 98.91, 101.07, 102.91, 106.42, 107.87, 112.41, 114.82, 118.71, 121.75, 127.6, 126.91, 131.29, 132.91, 137.33, 138.91, 140.12, 140.91, 144.24, 144.9, 150.36, 151.96, 157.25, 158.93, 162.5, 164.93, 167.26, 168.93, 173.04, 174.97, 178.49, 180.95, 183.85, 186.21, 190.2, 192.22, 195.08, 196.97, 200.59, 204.38, 207.2, 208.98, 210, 210, 222, 223, 226.03, 227.03, 232.04, 231.04, 238.03, 237.05, 239.1, 243.1, 247.1, 247.1, 252.1, 252.1, 257.1, 256.1, 259.1, 260.1, 261, 262, 263, 262, 265, 268]);
c$.elementNumberMax = c$.prototype.elementNumberMax = J.util.Elements.elementSymbols.length;
Clazz.defineStatics (c$,
"htElementMap", null,
"elementNames", ["unknown", "hydrogen", "helium", "lithium", "beryllium", "boron", "carbon", "nitrogen", "oxygen", "fluorine", "neon", "sodium", "magnesium", "aluminum", "silicon", "phosphorus", "sulfur", "chlorine", "argon", "potassium", "calcium", "scandium", "titanium", "vanadium", "chromium", "manganese", "iron", "cobalt", "nickel", "copper", "zinc", "gallium", "germanium", "arsenic", "selenium", "bromine", "krypton", "rubidium", "strontium", "yttrium", "zirconium", "niobium", "molybdenum", "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium", "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "cesium", "barium", "lanthanum", "cerium", "praseodymium", "neodymium", "promethium", "samarium", "europium", "gadolinium", "terbium", "dysprosium", "holmium", "erbium", "thulium", "ytterbium", "lutetium", "hafnium", "tantalum", "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury", "thallium", "lead", "bismuth", "polonium", "astatine", "radon", "francium", "radium", "actinium", "thorium", "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium", "berkelium", "californium", "einsteinium", "fermium", "mendelevium", "nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium"],
"naturalIsotopeMasses", [1, 1, 6, 12, 7, 14, 8, 16],
"naturalIsotopes", "1H,12C,14N,",
"firstIsotope", 4,
"altElementNumbers", [0, 13, 16, 55, 257, 385, 1414, 1670, 1798, 1927]);
c$.altElementMax = c$.prototype.altElementMax = J.util.Elements.altElementNumbers.length;
Clazz.defineStatics (c$,
"altElementSymbols", ["Xx", "Al", "S", "Cs", "D", "T", "11C", "13C", "14C", "15N"],
"altElementNames", ["dummy", "aluminium", "sulphur", "caesium", "deuterium", "tritium", "", "", "", ""],
"VdwPROBE", "#VDW radii for PROBE;{_H}.vdw = 1.0;{_H and connected(_C) and not connected(within(smiles,\'[a]\'))}.vdw = 1.17;{_C}.vdw = 1.75;{_C and connected(3) and connected(_O)}.vdw = 1.65;{_N}.vdw = 1.55;{_O}.vdw = 1.4;{_P}.vdw = 1.8;{_S}.vdw = 1.8;message VDW radii for H, C, N, O, P, and S set according to Word, et al., J. Mol. Biol. (1999) 285, 1711-1733",
"vanderwaalsMars", [1000, 1000, 1000, 1000, 1200, 1100, 1100, 1200, 1400, 1400, 2200, 1400, 1820, 1810, 1220, 2200, 1700, 1530, 628, 1900, 2080, 1920, 1548, 1800, 1950, 1700, 1548, 1700, 1850, 1550, 1400, 1600, 1700, 1520, 1348, 1550, 1730, 1470, 1300, 1500, 1540, 1540, 2020, 1540, 2270, 2270, 2200, 2400, 1730, 1730, 1500, 2200, 2050, 1840, 1500, 2100, 2100, 2100, 2200, 2100, 2080, 1800, 1880, 1950, 2000, 1800, 1808, 1800, 1970, 1750, 1748, 1800, 1880, 1880, 2768, 1880, 2750, 2750, 2388, 2800, 1973, 2310, 1948, 2400, 1700, 2300, 1320, 2300, 1700, 2150, 1948, 2150, 1700, 2050, 1060, 2050, 1700, 2050, 1128, 2050, 1700, 2050, 1188, 2050, 1700, 2050, 1948, 2050, 1700, 2000, 1128, 2000, 1630, 2000, 1240, 2000, 1400, 2000, 1148, 2000, 1390, 2100, 1148, 2100, 1870, 1870, 1548, 2100, 1700, 2110, 3996, 2100, 1850, 1850, 828, 2050, 1900, 1900, 900, 1900, 2100, 1830, 1748, 1900, 2020, 2020, 1900, 2020, 1700, 3030, 2648, 2900, 1700, 2490, 2020, 2550, 1700, 2400, 1608, 2400, 1700, 2300, 1420, 2300, 1700, 2150, 1328, 2150, 1700, 2100, 1748, 2100, 1700, 2050, 1800, 2050, 1700, 2050, 1200, 2050, 1700, 2000, 1220, 2000, 1630, 2050, 1440, 2050, 1720, 2100, 1548, 2100, 1580, 2200, 1748, 2200, 1930, 2200, 1448, 2200, 2170, 1930, 1668, 2250, 2200, 2170, 1120, 2200, 2060, 2060, 1260, 2100, 2150, 1980, 1748, 2100, 2160, 2160, 2100, 2160, 1700, 3430, 3008, 3000, 1700, 2680, 2408, 2700, 1700, 2500, 1828, 2500, 1700, 2480, 1860, 2480, 1700, 2470, 1620, 2470, 1700, 2450, 1788, 2450, 1700, 2430, 1760, 2430, 1700, 2420, 1740, 2420, 1700, 2400, 1960, 2400, 1700, 2380, 1688, 2380, 1700, 2370, 1660, 2370, 1700, 2350, 1628, 2350, 1700, 2330, 1608, 2330, 1700, 2320, 1588, 2320, 1700, 2300, 1568, 2300, 1700, 2280, 1540, 2280, 1700, 2270, 1528, 2270, 1700, 2250, 1400, 2250, 1700, 2200, 1220, 2200, 1700, 2100, 1260, 2100, 1700, 2050, 1300, 2050, 1700, 2000, 1580, 2000, 1700, 2000, 1220, 2000, 1720, 2050, 1548, 2050, 1660, 2100, 1448, 2100, 1550, 2050, 1980, 2050, 1960, 1960, 1708, 2200, 2020, 2020, 2160, 2300, 1700, 2070, 1728, 2300, 1700, 1970, 1208, 2000, 1700, 2020, 1120, 2000, 1700, 2200, 2300, 2000, 1700, 3480, 3240, 2000, 1700, 2830, 2568, 2000, 1700, 2000, 2120, 2000, 1700, 2400, 1840, 2400, 1700, 2000, 1600, 2000, 1860, 2300, 1748, 2300, 1700, 2000, 1708, 2000, 1700, 2000, 1668, 2000, 1700, 2000, 1660, 2000, 1700, 2000, 1648, 2000, 1700, 2000, 1640, 2000, 1700, 2000, 1628, 2000, 1700, 2000, 1620, 2000, 1700, 2000, 1608, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1588, 2000, 1700, 2000, 1580, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000],
"covalentMars", [0, 230, 930, 680, 350, 830, 680, 680, 680, 640, 1120, 970, 1100, 1350, 1200, 750, 1020, 990, 1570, 1330, 990, 1440, 1470, 1330, 1350, 1350, 1340, 1330, 1500, 1520, 1450, 1220, 1170, 1210, 1220, 1210, 1910, 1470, 1120, 1780, 1560, 1480, 1470, 1350, 1400, 1450, 1500, 1590, 1690, 1630, 1460, 1460, 1470, 1400, 1980, 1670, 1340, 1870, 1830, 1820, 1810, 1800, 1800, 1990, 1790, 1760, 1750, 1740, 1730, 1720, 1940, 1720, 1570, 1430, 1370, 1350, 1370, 1320, 1500, 1500, 1700, 1550, 1540, 1540, 1680, 1700, 2400, 2000, 1900, 1880, 1790, 1610, 1580, 1550, 1530, 1510, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1600, 1600, 1600, 1600, 1600, 1600],
"FORMAL_CHARGE_MIN", -4,
"FORMAL_CHARGE_MAX", 7,
"cationLookupTable", [53, 680, 69, 440, 70, 350, 85, 350, 87, 230, 104, 160, 117, 680, 119, 160, 121, 130, 133, 220, 138, 90, 155, 80, 165, 1120, 181, 970, 197, 820, 198, 660, 215, 510, 229, 650, 232, 420, 247, 440, 249, 350, 262, 2190, 264, 370, 266, 300, 281, 340, 283, 270, 293, 1540, 309, 1330, 325, 1180, 326, 990, 343, 732, 357, 960, 358, 940, 359, 760, 360, 680, 374, 880, 375, 740, 376, 630, 377, 590, 389, 810, 390, 890, 391, 630, 394, 520, 406, 800, 407, 660, 408, 600, 411, 460, 422, 740, 423, 640, 438, 720, 439, 630, 454, 690, 469, 960, 470, 720, 485, 880, 486, 740, 501, 810, 503, 620, 518, 730, 520, 530, 535, 580, 537, 460, 549, 660, 552, 500, 554, 420, 569, 470, 571, 390, 597, 1470, 614, 1120, 631, 893, 645, 1090, 648, 790, 661, 1000, 664, 740, 665, 690, 677, 930, 680, 700, 682, 620, 699, 979, 712, 670, 727, 680, 742, 800, 744, 650, 757, 1260, 758, 890, 773, 1140, 774, 970, 791, 810, 806, 930, 808, 710, 823, 760, 825, 620, 837, 820, 840, 700, 842, 560, 857, 620, 859, 500, 885, 1670, 901, 1530, 902, 1340, 917, 1390, 919, 1016, 933, 1270, 935, 1034, 936, 920, 951, 1013, 952, 900, 967, 995, 983, 979, 999, 964, 1014, 1090, 1015, 950, 1031, 938, 1047, 923, 1048, 840, 1063, 908, 1079, 894, 1095, 881, 1111, 870, 1126, 930, 1127, 858, 1143, 850, 1160, 780, 1177, 680, 1192, 700, 1194, 620, 1208, 720, 1211, 560, 1224, 880, 1226, 690, 1240, 680, 1254, 800, 1256, 650, 1269, 1370, 1271, 850, 1285, 1270, 1286, 1100, 1301, 1470, 1303, 950, 1318, 1200, 1320, 840, 1333, 980, 1335, 960, 1337, 740, 1354, 670, 1371, 620, 1397, 1800, 1414, 1430, 1431, 1180, 1448, 1020, 1463, 1130, 1464, 980, 1465, 890, 1480, 970, 1482, 800, 1495, 1100, 1496, 950, 1499, 710, 1511, 1080, 1512, 930, 1527, 1070, 1528, 920],
"anionLookupTable", [19, 1540, 96, 2600, 113, 1710, 130, 1360, 131, 680, 147, 1330, 241, 2120, 258, 1840, 275, 1810, 512, 2720, 529, 2220, 546, 1980, 563, 1960, 800, 2940, 803, 3700, 817, 2450, 834, 2110, 835, 2500, 851, 2200]);
c$.bsCations = c$.prototype.bsCations =  new J.util.BS ();
c$.bsAnions = c$.prototype.bsAnions =  new J.util.BS ();
{
for (var i = 0; i < J.util.Elements.anionLookupTable.length; i += 2) J.util.Elements.bsAnions.set (J.util.Elements.anionLookupTable[i] >> 4);

for (var i = 0; i < J.util.Elements.cationLookupTable.length; i += 2) J.util.Elements.bsCations.set (J.util.Elements.cationLookupTable[i] >> 4);

}Clazz.defineStatics (c$,
"hydrophobicities", [0, 0.62, -2.53, -0.78, -0.9, 0.29, -0.85, -0.74, 0.48, -0.4, 1.38, 1.06, -1.5, 0.64, 1.19, 0.12, -0.18, -0.05, 0.81, 0.26, 1.08]);
{
if ((J.util.Elements.elementNames.length != J.util.Elements.elementNumberMax) || (Clazz.doubleToInt (J.util.Elements.vanderwaalsMars.length / 4) != J.util.Elements.elementNumberMax) || (J.util.Elements.covalentMars.length != J.util.Elements.elementNumberMax)) {
J.util.Logger.error ("ERROR!!! Element table length mismatch:\n elementSymbols.length=" + J.util.Elements.elementSymbols.length + "\n elementNames.length=" + J.util.Elements.elementNames.length + "\n vanderwaalsMars.length=" + J.util.Elements.vanderwaalsMars.length + "\n covalentMars.length=" + J.util.Elements.covalentMars.length);
}}Clazz.defineStatics (c$,
"electroNegativities", [0, 2.2, 0, 0.97, 1.47, 2.01, 2.5, 3.07, 3.5, 4.1, 0, 1.01, 1.23, 1.47, 1.74, 2.06, 2.44, 2.83, 0, 0.91, 1.04, 1.2, 1.32, 1.45, 1.56, 1.6, 1.64, 1.7, 1.75, 1.75, 1.66, 1.82, 2.02, 2.2, 2.48, 2.74, 0, 0.89, 0.99, 1.11, 1.22, 1.23, 1.3, 1.36, 1.42, 1.45, 1.35, 1.42, 1.46, 1.49, 1.72, 1.82, 2.01, 2.21]);
});
