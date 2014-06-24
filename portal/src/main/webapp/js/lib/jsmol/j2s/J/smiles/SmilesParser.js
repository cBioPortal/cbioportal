Clazz.declarePackage ("J.smiles");
Clazz.load (["java.util.Hashtable"], "J.smiles.SmilesParser", ["java.lang.Character", "J.smiles.InvalidSmilesException", "$.SmilesAtom", "$.SmilesBond", "$.SmilesMeasure", "$.SmilesSearch", "J.util.Elements", "$.JmolList", "$.Logger", "$.Parser", "$.SB", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isSmarts = false;
this.isBioSequence = false;
this.bioType = '\0';
this.ringBonds = null;
this.braceCount = 0;
this.branchLevel = 0;
this.flags = 0;
this.htMeasures = null;
Clazz.instantialize (this, arguments);
}, J.smiles, "SmilesParser");
Clazz.prepareFields (c$, function () {
this.ringBonds =  new java.util.Hashtable ();
this.htMeasures =  new java.util.Hashtable ();
});
c$.getMolecule = $_M(c$, "getMolecule", 
function (pattern, isSmarts) {
return ( new J.smiles.SmilesParser (isSmarts)).parse (pattern);
}, "~S,~B");
Clazz.makeConstructor (c$, 
function (isSmarts) {
this.isSmarts = isSmarts;
}, "~B");
$_M(c$, "reset", 
function () {
this.braceCount = 0;
this.branchLevel = 0;
});
$_M(c$, "parse", 
function (pattern) {
if (pattern == null) throw  new J.smiles.InvalidSmilesException ("SMILES expressions must not be null");
var search =  new J.smiles.SmilesSearch ();
if (pattern.indexOf ("$(select") >= 0) pattern = this.parseNested (search, pattern, "select");
pattern = J.smiles.SmilesParser.cleanPattern (pattern);
while (pattern.startsWith ("/")) {
var strFlags = J.smiles.SmilesParser.getSubPattern (pattern, 0, '/').toUpperCase ();
pattern = pattern.substring (strFlags.length);
this.flags = 0;
if (strFlags.indexOf ("NOAROMATIC") >= 0) this.flags |= 1;
if (strFlags.indexOf ("AROMATICSTRICT") >= 0) this.flags |= 4;
if (strFlags.indexOf ("AROMATICDEFINED") >= 0) this.flags |= 8;
if (strFlags.indexOf ("AROMATICDOUBLE") >= 0) this.flags |= 16;
if (strFlags.indexOf ("NOSTEREO") >= 0) this.flags |= 2;
}
if (pattern.indexOf ("$") >= 0) pattern = this.parseVariables (pattern);
if (this.isSmarts && pattern.indexOf ("[$") >= 0) pattern = this.parseVariableLength (pattern);
if (pattern.indexOf ("||") >= 0) {
var patterns = J.util.TextFormat.splitChars (pattern, "||");
var toDo = "";
search.subSearches =  new Array (patterns.length);
for (var i = 0; i < patterns.length; i++) {
var key = "|" + patterns[i] + "|";
if (toDo.indexOf (key) < 0) {
search.subSearches[i] = this.getSearch (search, patterns[i], this.flags);
toDo += key;
}}
J.util.Logger.info (toDo);
return search;
}return this.getSearch (search, pattern, this.flags);
}, "~S");
$_M(c$, "parseVariableLength", 
($fz = function (pattern) {
var sout =  new J.util.SB ();
var len = pattern.length - 1;
var nParen = 0;
var haveInternalOr = false;
for (var i = 0; i < len; i++) {
switch (pattern.charAt (i)) {
case '(':
nParen++;
break;
case ')':
nParen--;
break;
case '|':
if (nParen > 0) {
haveInternalOr = true;
if (pattern.charAt (i + 1) == '|') {
pattern = pattern.substring (0, i) + pattern.substring (i + 1);
len--;
}}break;
}
}
if (pattern.indexOf ("||") >= 0) {
var patterns = J.util.TextFormat.splitChars (pattern, "||");
for (var i = 0; i < patterns.length; i++) sout.append ("||").append (this.parseVariableLength (patterns[i]));

} else {
var pt = -1;
var ret =  Clazz.newIntArray (1, 0);
var isOK = true;
var bracketed = null;
while ((pt = pattern.indexOf ("[$", pt + 1)) >= 0) {
var pt0 = pt;
var min = -2147483648;
var max = -2147483648;
pt = J.smiles.SmilesParser.getDigits (pattern, pt + 2, ret);
min = ret[0];
if (min != -2147483648) {
if (J.smiles.SmilesParser.getChar (pattern, pt) == '-') {
pt = J.smiles.SmilesParser.getDigits (pattern, pt + 1, ret);
max = ret[0];
}}if (J.smiles.SmilesParser.getChar (pattern, pt) != '(') continue;
bracketed = J.smiles.SmilesParser.getSubPattern (pattern, pt0, '[');
if (!bracketed.endsWith (")")) continue;
var pt1 = pt0 + bracketed.length + 2;
var repeat = J.smiles.SmilesParser.getSubPattern (pattern, pt, '(');
var pt2 = pt;
bracketed = J.smiles.SmilesParser.getSubPattern (pattern, pt, '[');
pt += 1 + repeat.length;
if (repeat.indexOf (':') >= 0 && repeat.indexOf ('|') < 0) {
var parenCount = 0;
var n = repeat.length;
var ptColon = -1;
for (var i = 0; i < n; i++) {
switch (repeat.charAt (i)) {
case '[':
case '(':
parenCount++;
break;
case ')':
case ']':
parenCount--;
break;
case '.':
if (ptColon >= 0 && parenCount == 0) n = i;
break;
case ':':
if (ptColon < 0 && parenCount == 0) ptColon = i;
break;
}
}
if (ptColon > 0) repeat = repeat.substring (0, ptColon) + "(" + repeat.substring (ptColon, n) + ")" + repeat.substring (n);
}if (min == -2147483648) {
var ptOr = repeat.indexOf ("|");
if (ptOr >= 0) return this.parseVariableLength (pattern.substring (0, pt0) + "[$1" + pattern.substring (pt2, pt2 + ptOr + 1) + ")]" + pattern.substring (pt1) + "||" + pattern.substring (0, pt0) + "[$1(" + pattern.substring (pt2 + ptOr + 2) + pattern.substring (pt1));
continue;
}if (max == -2147483648) max = min;
if (repeat.indexOf ("|") >= 0) repeat = "[$(" + repeat + ")]";
for (var i = min; i <= max; i++) {
var sb =  new J.util.SB ();
sb.append ("||").append (pattern.substring (0, pt0));
for (var j = 0; j < i; j++) sb.append (repeat);

sb.append (pattern.substring (pt1));
sout.appendSB (sb);
}
}
if (!isOK) throw  new J.smiles.InvalidSmilesException ("bad variable expression: " + bracketed);
}return (haveInternalOr ? this.parseVariableLength (sout.substring (2)) : sout.length () < 2 ? pattern : sout.substring (2));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getSearch", 
function (parent, pattern, flags) {
this.htMeasures =  new java.util.Hashtable ();
var molecule =  new J.smiles.SmilesSearch ();
molecule.setTop (parent);
molecule.isSmarts = this.isSmarts;
molecule.pattern = pattern;
molecule.flags = flags;
if (pattern.indexOf ("$(") >= 0) pattern = this.parseNested (molecule, pattern, "");
this.parseSmiles (molecule, pattern, null, false);
if (this.braceCount != 0) throw  new J.smiles.InvalidSmilesException ("unmatched '{'");
if (!this.ringBonds.isEmpty ()) throw  new J.smiles.InvalidSmilesException ("Open ring");
molecule.setAtomArray ();
for (var i = molecule.atomCount; --i >= 0; ) {
var atom = molecule.patternAtoms[i];
atom.setBondArray ();
if (!this.isSmarts && atom.bioType == '\0' && !atom.setHydrogenCount (molecule)) throw  new J.smiles.InvalidSmilesException ("unbracketed atoms must be one of: B, C, N, O, P, S, F, Cl, Br, I,");
}
if (this.isSmarts) for (var i = molecule.atomCount; --i >= 0; ) {
var atom = molecule.patternAtoms[i];
this.checkNested (molecule, atom, flags);
for (var k = 0; k < atom.nAtomsOr; k++) this.checkNested (molecule, atom.atomsOr[k], flags);

for (var k = 0; k < atom.nPrimitives; k++) this.checkNested (molecule, atom.primitives[k], flags);

}
if (!this.isSmarts && !this.isBioSequence) molecule.elementCounts[1] = molecule.getMissingHydrogenCount ();
this.fixChirality (molecule);
return molecule;
}, "J.smiles.SmilesSearch,~S,~N");
$_M(c$, "checkNested", 
($fz = function (molecule, atom, flags) {
if (atom.iNested > 0) {
var o = molecule.getNested (atom.iNested);
if (Clazz.instanceOf (o, String)) {
var s = o;
if (s.startsWith ("select")) return;
if (s.charAt (0) != '~' && atom.bioType != '\0') s = "~" + atom.bioType + "~" + s;
var search = this.getSearch (molecule, s, flags);
if (search.atomCount > 0 && search.patternAtoms[0].selected) atom.selected = true;
molecule.setNested (atom.iNested, search);
}}}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,J.smiles.SmilesAtom,~N");
$_M(c$, "fixChirality", 
($fz = function (molecule) {
for (var i = molecule.atomCount; --i >= 0; ) {
var sAtom = molecule.patternAtoms[i];
var stereoClass = sAtom.getChiralClass ();
if (stereoClass == -2147483648) continue;
var nBonds = sAtom.missingHydrogenCount;
if (nBonds < 0) nBonds = 0;
nBonds += sAtom.getBondCount ();
switch (stereoClass) {
case 0:
switch (nBonds) {
case 2:
stereoClass = (sAtom.getValence () == 3 ? 3 : 2);
break;
case 3:
case 4:
case 5:
case 6:
stereoClass = nBonds;
break;
}
break;
case 8:
if (nBonds != 4) stereoClass = 0;
break;
case 2:
case 6:
case 4:
case 5:
if (nBonds != stereoClass) stereoClass = 0;
break;
}
if (stereoClass == 0) throw  new J.smiles.InvalidSmilesException ("Incorrect number of bonds for stereochemistry descriptor");
sAtom.setChiralClass (stereoClass);
}
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch");
$_M(c$, "parseSmiles", 
($fz = function (molecule, pattern, currentAtom, isBranchAtom) {
var ret =  Clazz.newIntArray (1, 0);
var pt = 0;
var ch;
var bond = null;
while (pattern != null && pattern.length != 0) {
var index = 0;
if (currentAtom == null || bond != null && bond.order == 0) {
if (this.isBioSequence) molecule.top.needAromatic = false;
index = this.checkBioType (pattern, 0);
}ch = J.smiles.SmilesParser.getChar (pattern, index);
var haveOpen = this.checkBrace (molecule, ch, '{');
if (haveOpen) ch = J.smiles.SmilesParser.getChar (pattern, ++index);
if (ch == '(') {
var isMeasure = (J.smiles.SmilesParser.getChar (pattern, index + 1) == '.');
if (currentAtom == null) throw  new J.smiles.InvalidSmilesException ("No previous atom for " + (isMeasure ? "measure" : "branch"));
var subString = J.smiles.SmilesParser.getSubPattern (pattern, index, '(');
if (subString.startsWith (".")) {
this.parseMeasure (molecule, subString.substring (1), currentAtom);
} else if (subString.length == 0 && this.isBioSequence) {
currentAtom.notCrossLinked = true;
} else {
this.branchLevel++;
this.parseSmiles (molecule, subString, currentAtom, true);
this.branchLevel--;
}index = subString.length + 2;
ch = J.smiles.SmilesParser.getChar (pattern, index);
if (ch == '}' && this.checkBrace (molecule, ch, '}')) index++;
} else {
pt = index;
while (J.smiles.SmilesBond.isBondType (ch, this.isSmarts, this.isBioSequence)) ch = J.smiles.SmilesParser.getChar (pattern, ++index);

bond = this.parseBond (molecule, null, pattern.substring (pt, index), null, currentAtom, false, isBranchAtom);
if (haveOpen && bond.order != -1) index = pt;
ch = J.smiles.SmilesParser.getChar (pattern, index);
if (this.checkBrace (molecule, ch, '{')) ch = J.smiles.SmilesParser.getChar (pattern, ++index);
if (ch == '~' && bond.order == 0) {
index = this.checkBioType (pattern, index);
ch = J.smiles.SmilesParser.getChar (pattern, index);
}if (ch == '\0' && bond.order == 0) return;
var isRing = (Character.isDigit (ch) || ch == '%');
var isAtom = (!isRing && (ch == '_' || ch == '[' || ch == '*' || Character.isLetter (ch)));
if (isRing) {
var ringNumber;
switch (ch) {
case '%':
if (J.smiles.SmilesParser.getChar (pattern, index + 1) == '(') {
var subPattern = J.smiles.SmilesParser.getSubPattern (pattern, index + 1, '(');
J.smiles.SmilesParser.getDigits (subPattern, 0, ret);
index += subPattern.length + 3;
if (ret[0] < 0) throw  new J.smiles.InvalidSmilesException ("Invalid ring designation: " + subPattern);
} else {
if (index + 3 <= pattern.length) index = J.smiles.SmilesParser.getDigits (pattern.substring (0, index + 3), index + 1, ret);
if (ret[0] < 10) throw  new J.smiles.InvalidSmilesException ("Two digits must follow the % sign");
}ringNumber = ret[0];
break;
default:
ringNumber = ch.charCodeAt (0) - 48;
index++;
}
this.parseRing (molecule, ringNumber, currentAtom, bond);
} else if (isAtom) {
switch (ch) {
case '[':
case '_':
var subPattern = J.smiles.SmilesParser.getSubPattern (pattern, index, ch);
index += subPattern.length + (ch == '[' ? 2 : 0);
if (this.isBioSequence && ch == '[' && subPattern.indexOf (".") < 0 && subPattern.indexOf ("_") < 0) subPattern += ".0";
currentAtom = this.parseAtom (molecule, null, subPattern, currentAtom, bond, ch == '[', false, isBranchAtom);
if (bond.order != -1 && bond.order != 0) bond.set2a (null, currentAtom);
break;
default:
var ch2 = (!this.isBioSequence && Character.isUpperCase (ch) ? J.smiles.SmilesParser.getChar (pattern, index + 1) : '\0');
if (ch != 'X' || ch2 != 'x') if (!Character.isLowerCase (ch2) || J.util.Elements.elementNumberFromSymbol (pattern.substring (index, index + 2), true) == 0) ch2 = '\0';
if (ch2 != '\0' && "NA CA BA PA SC AC".indexOf (pattern.substring (index, index + 2)) >= 0) {
ch2 = '\0';
}var size = (Character.isUpperCase (ch) && Character.isLowerCase (ch2) ? 2 : 1);
currentAtom = this.parseAtom (molecule, null, pattern.substring (index, index + size), currentAtom, bond, false, false, isBranchAtom);
index += size;
}
} else {
throw  new J.smiles.InvalidSmilesException ("Unexpected character: " + J.smiles.SmilesParser.getChar (pattern, index));
}ch = J.smiles.SmilesParser.getChar (pattern, index);
if (ch == '}' && this.checkBrace (molecule, ch, '}')) index++;
}pattern = pattern.substring (index);
isBranchAtom = false;
}
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~S,J.smiles.SmilesAtom,~B");
$_M(c$, "checkBioType", 
($fz = function (pattern, index) {
this.isBioSequence = (pattern.charAt (index) == '~');
if (this.isBioSequence) {
index++;
this.bioType = '*';
var ch = J.smiles.SmilesParser.getChar (pattern, 2);
if (ch == '~' && ((ch = pattern.charAt (1)) == '*' || Character.isLowerCase (ch))) {
this.bioType = ch;
index = 3;
}}return index;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "parseMeasure", 
($fz = function (molecule, strMeasure, currentAtom) {
var pt = strMeasure.indexOf (":");
var isNot = false;
var id = (pt < 0 ? strMeasure : strMeasure.substring (0, pt));
while (pt != 0) {
var len = id.length;
if (len == 1) id += "0";
var m = this.htMeasures.get (id);
if ((m == null) == (pt < 0)) break;
try {
if (pt > 0) {
var type = ("__dat".indexOf (id.charAt (0)));
if (type < 2) break;
var ret =  Clazz.newIntArray (1, 0);
var index = J.smiles.SmilesParser.getDigits (id, 1, ret);
var pt2 = strMeasure.indexOf (",", pt);
if (pt2 < 0) pt2 = strMeasure.indexOf ("-", pt + 1);
if (pt2 < 0) break;
var s = strMeasure.substring (pt + 1, pt2);
if (s.startsWith ("!")) {
isNot = true;
s = s.substring (1);
}var min = (pt + 1 == pt2 ? 0 : J.util.Parser.fVal (s));
s = strMeasure.substring (pt2 + 1);
var max = (s.length == 0 ? 3.4028235E38 : J.util.Parser.fVal (s));
m =  new J.smiles.SmilesMeasure (molecule, index, type, min, max, isNot);
molecule.measures.addLast (m);
if (index > 0) this.htMeasures.put (id, m);
 else if (index == 0 && J.util.Logger.debugging) J.util.Logger.debug ("measure created: " + m);
} else {
if (!m.addPoint (currentAtom.index)) break;
if (m.nPoints == m.type) {
this.htMeasures.remove (id);
if (J.util.Logger.debugging) J.util.Logger.debug ("measure created: " + m);
}return;
}if (!m.addPoint (currentAtom.index)) break;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
break;
} else {
throw e;
}
}
return;
}
throw  new J.smiles.InvalidSmilesException ("invalid measure: " + strMeasure);
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~S,J.smiles.SmilesAtom");
$_M(c$, "checkBrace", 
($fz = function (molecule, ch, type) {
switch (ch) {
case '{':
if (ch != type) break;
this.braceCount++;
molecule.top.haveSelected = true;
return true;
case '}':
if (ch != type) break;
if (this.braceCount > 0) {
this.braceCount--;
return true;
}break;
default:
return false;
}
throw  new J.smiles.InvalidSmilesException ("Unmatched '}'");
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~S,~S");
$_M(c$, "parseNested", 
($fz = function (molecule, pattern, prefix) {
var index;
prefix = "$(" + prefix;
while ((index = pattern.lastIndexOf (prefix)) >= 0) {
var s = J.smiles.SmilesParser.getSubPattern (pattern, index + 1, '(');
var pt = index + s.length + 3;
pattern = pattern.substring (0, index) + "_" + molecule.addNested (s) + "_" + pattern.substring (pt);
}
return pattern;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~S,~S");
$_M(c$, "parseVariables", 
($fz = function (pattern) {
var keys =  new J.util.JmolList ();
var values =  new J.util.JmolList ();
var index;
var ipt = 0;
var iptLast = -1;
while ((index = pattern.indexOf ("$", ipt)) >= 0) {
if (J.smiles.SmilesParser.getChar (pattern, ipt + 1) == '(') break;
ipt = J.smiles.SmilesParser.skipTo (pattern, index, '=');
if (ipt <= index + 1 || J.smiles.SmilesParser.getChar (pattern, ipt + 1) != '\"') break;
var key = pattern.substring (index, ipt);
if (key.lastIndexOf ('$') > 0 || key.indexOf (']') > 0) throw  new J.smiles.InvalidSmilesException ("Invalid variable name: " + key);
var s = J.smiles.SmilesParser.getSubPattern (pattern, ipt + 1, '\"');
keys.addLast ("[" + key + "]");
values.addLast (s);
ipt += s.length + 2;
ipt = J.smiles.SmilesParser.skipTo (pattern, ipt, ';');
iptLast = ++ipt;
}
if (iptLast < 0) return pattern;
return J.util.TextFormat.replaceStrings (pattern.substring (iptLast), keys, values);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "parseAtom", 
($fz = function (molecule, atomSet, pattern, currentAtom, bond, isBracketed, isPrimitive, isBranchAtom) {
if (pattern == null || pattern.length == 0) throw  new J.smiles.InvalidSmilesException ("Empty atom definition");
var newAtom = (atomSet == null ? molecule.addAtom () : isPrimitive ? atomSet.addPrimitive () : atomSet.addAtomOr ());
if (this.braceCount > 0) newAtom.selected = true;
if (!this.checkLogic (molecule, pattern, newAtom, null, currentAtom, isPrimitive, isBranchAtom)) {
var ret =  Clazz.newIntArray (1, 0);
if (this.isBioSequence && pattern.length == 1) pattern += ".0";
var ch = pattern.charAt (0);
var index = 0;
var isNot = false;
if (this.isSmarts && ch == '!') {
ch = J.smiles.SmilesParser.getChar (pattern, ++index);
if (ch == '\0') throw  new J.smiles.InvalidSmilesException ("invalid '!'");
newAtom.not = isNot = true;
}var hydrogenCount = -2147483648;
var biopt = pattern.indexOf ('.');
if (biopt >= 0) {
var name = pattern.substring (index, biopt);
if (name.length == 0) name = "*";
if (name.length > 1) newAtom.residueName = name.toUpperCase ();
 else if (!name.equals ("*")) newAtom.residueChar = name;
name = pattern.substring (biopt + 1).toUpperCase ();
if ((biopt = name.indexOf ("#")) >= 0) {
J.smiles.SmilesParser.getDigits (name, biopt + 1, ret);
newAtom.elementNumber = ret[0];
name = name.substring (0, biopt);
}if (name.length == 0) name = "*";
if (!name.equals ("*")) newAtom.setAtomName (name);
ch = '\0';
}newAtom.setBioAtom (this.bioType);
while (ch != '\0') {
newAtom.setAtomName (this.isBioSequence ? "0" : "");
if (Character.isDigit (ch)) {
index = J.smiles.SmilesParser.getDigits (pattern, index, ret);
var mass = ret[0];
if (mass == -2147483648) throw  new J.smiles.InvalidSmilesException ("Non numeric atomic mass");
if (J.smiles.SmilesParser.getChar (pattern, index) == '?') {
index++;
mass = -mass;
}newAtom.setAtomicMass (mass);
} else {
switch (ch) {
case '"':
var type = J.util.Parser.getQuotedStringAt (pattern, index);
index += type.length + 2;
newAtom.setAtomType (type);
break;
case '_':
index = J.smiles.SmilesParser.getDigits (pattern, index + 1, ret) + 1;
if (ret[0] == -2147483648) throw  new J.smiles.InvalidSmilesException ("Invalid SEARCH primitive: " + pattern.substring (index));
newAtom.iNested = ret[0];
if (this.isBioSequence && isBracketed) {
if (index != pattern.length) throw  new J.smiles.InvalidSmilesException ("invalid characters: " + pattern.substring (index));
}break;
case '=':
index = J.smiles.SmilesParser.getDigits (pattern, index + 1, ret);
newAtom.jmolIndex = ret[0];
break;
case '#':
index = J.smiles.SmilesParser.getDigits (pattern, index + 1, ret);
newAtom.elementNumber = ret[0];
break;
case '-':
case '+':
index = this.checkCharge (pattern, index, newAtom);
break;
case '@':
molecule.haveAtomStereochemistry = true;
index = this.checkChirality (pattern, index, molecule.patternAtoms[newAtom.index]);
break;
default:
var nextChar = J.smiles.SmilesParser.getChar (pattern, index + 1);
var sym2 = pattern.substring (index + 1, index + (Character.isLowerCase (nextChar) && (!isBracketed || !Character.isDigit (J.smiles.SmilesParser.getChar (pattern, index + 2))) ? 2 : 1));
var symbol = Character.toUpperCase (ch) + sym2;
var mustBeSymbol = true;
var checkForPrimitive = (isBracketed && Character.isLetter (ch));
if (checkForPrimitive) {
if (!isNot && (isPrimitive ? atomSet : newAtom).hasSymbol) {
mustBeSymbol = false;
} else if (ch == 'H') {
mustBeSymbol = !Character.isDigit (nextChar) || J.smiles.SmilesParser.getChar (pattern, index + 2) == '?';
} else if ("DdhRrvXx".indexOf (ch) >= 0 && Character.isDigit (nextChar)) {
mustBeSymbol = false;
} else if (!symbol.equals ("A") && !symbol.equals ("Xx")) {
mustBeSymbol = (J.util.Elements.elementNumberFromSymbol (symbol, true) > 0);
if (!mustBeSymbol && sym2 !== "") {
sym2 = "";
symbol = symbol.substring (0, 1);
mustBeSymbol = (J.util.Elements.elementNumberFromSymbol (symbol, true) > 0);
}}}if (mustBeSymbol) {
if (!isBracketed && !this.isSmarts && !this.isBioSequence && !J.smiles.SmilesAtom.allowSmilesUnbracketed (symbol) || !newAtom.setSymbol (symbol = ch + sym2)) throw  new J.smiles.InvalidSmilesException ("Invalid atom symbol: " + symbol);
if (isPrimitive) atomSet.hasSymbol = true;
index += symbol.length;
} else {
index = J.smiles.SmilesParser.getDigits (pattern, index + 1, ret);
var val = ret[0];
switch (ch) {
default:
throw  new J.smiles.InvalidSmilesException ("Invalid SEARCH primitive: " + pattern.substring (index));
case 'D':
newAtom.setDegree (val == -2147483648 ? 1 : val);
break;
case 'd':
newAtom.setNonhydrogenDegree (val == -2147483648 ? 1 : val);
break;
case 'H':
hydrogenCount = (val == -2147483648 ? 1 : val);
break;
case 'h':
newAtom.setImplicitHydrogenCount (val == -2147483648 ? -1 : val);
break;
case 'R':
if (val == -2147483648) val = -1;
newAtom.setRingMembership (val);
molecule.top.needRingData = true;
break;
case 'r':
if (val == -2147483648) {
val = -1;
newAtom.setRingMembership (val);
} else {
newAtom.setRingSize (val);
switch (val) {
case 500:
val = 5;
break;
case 600:
val = 6;
break;
}
if (val > molecule.ringDataMax) molecule.ringDataMax = val;
}molecule.top.needRingData = true;
break;
case 'v':
newAtom.setValence (val == -2147483648 ? 1 : val);
break;
case 'X':
newAtom.setConnectivity (val == -2147483648 ? 1 : val);
break;
case 'x':
newAtom.setRingConnectivity (val == -2147483648 ? -1 : val);
molecule.top.needRingData = true;
break;
}
}}
}ch = J.smiles.SmilesParser.getChar (pattern, index);
if (isNot && ch != '\0') throw  new J.smiles.InvalidSmilesException ("'!' may only involve one primitive.");
}
if (hydrogenCount == -2147483648 && isBracketed) hydrogenCount = -2147483647;
newAtom.setExplicitHydrogenCount (hydrogenCount);
molecule.patternAtoms[newAtom.index].setExplicitHydrogenCount (hydrogenCount);
}if (currentAtom != null && bond.order == 0) {
newAtom.notBondedIndex = currentAtom.index;
}if (currentAtom != null && bond.order != 0) {
if (bond.order == -1) bond.order = (this.isBioSequence && isBranchAtom ? 112 : this.isSmarts || currentAtom.isAromatic () && newAtom.isAromatic () ? 81 : 1);
if (!isBracketed) bond.set2a (null, newAtom);
if (this.branchLevel == 0 && (bond.order == 17 || bond.order == 112)) this.branchLevel++;
}if (this.branchLevel == 0) molecule.lastChainAtom = newAtom;
return newAtom;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,J.smiles.SmilesAtom,~S,J.smiles.SmilesAtom,J.smiles.SmilesBond,~B,~B,~B");
$_M(c$, "parseRing", 
($fz = function (molecule, ringNum, currentAtom, bond) {
var r = Integer.$valueOf (ringNum);
var bond0 = this.ringBonds.get (r);
if (bond0 == null) {
this.ringBonds.put (r, bond);
return;
}this.ringBonds.remove (r);
switch (bond.order) {
case -1:
bond.order = (bond0.order != -1 ? bond0.order : this.isSmarts || currentAtom.isAromatic () && bond0.getAtom1 ().isAromatic () ? 81 : 1);
break;
case 257:
bond.order = 513;
break;
case 513:
bond.order = 257;
break;
}
if (bond0.order != -1 && bond0.order != bond.order) throw  new J.smiles.InvalidSmilesException ("Incoherent bond type for ring");
bond0.set (bond);
currentAtom.bondCount--;
bond0.setAtom2 (currentAtom);
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~N,J.smiles.SmilesAtom,J.smiles.SmilesBond");
$_M(c$, "checkCharge", 
($fz = function (pattern, index, newAtom) {
var len = pattern.length;
var ch = pattern.charAt (index);
var count = 1;
++index;
if (index < len) {
var nextChar = pattern.charAt (index);
if (Character.isDigit (nextChar)) {
var ret =  Clazz.newIntArray (1, 0);
index = J.smiles.SmilesParser.getDigits (pattern, index, ret);
count = ret[0];
if (count == -2147483648) throw  new J.smiles.InvalidSmilesException ("Non numeric charge");
} else {
while (index < len && pattern.charAt (index) == ch) {
index++;
count++;
}
}}newAtom.setCharge (ch == '+' ? count : -count);
return index;
}, $fz.isPrivate = true, $fz), "~S,~N,J.smiles.SmilesAtom");
$_M(c$, "checkChirality", 
($fz = function (pattern, index, newAtom) {
var stereoClass = 0;
var order = -2147483648;
var len = pattern.length;
var ch;
stereoClass = 0;
order = 1;
if (++index < len) {
switch (ch = pattern.charAt (index)) {
case '@':
order = 2;
index++;
break;
case 'H':
break;
case 'A':
case 'D':
case 'E':
case 'O':
case 'S':
case 'T':
stereoClass = (index + 1 < len ? J.smiles.SmilesAtom.getChiralityClass (pattern.substring (index, index + 2)) : -1);
index += 2;
break;
default:
order = (Character.isDigit (ch) ? 1 : -1);
}
var pt = index;
if (order == 1) {
while (pt < len && Character.isDigit (pattern.charAt (pt))) pt++;

if (pt > index) {
try {
order = Integer.parseInt (pattern.substring (index, pt));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
order = -1;
} else {
throw e;
}
}
index = pt;
}}if (order < 1 || stereoClass < 0) throw  new J.smiles.InvalidSmilesException ("Invalid stereochemistry descriptor");
}newAtom.setChiralClass (stereoClass);
newAtom.setChiralOrder (order);
if (J.smiles.SmilesParser.getChar (pattern, index) == '?') {
J.util.Logger.info ("Ignoring '?' in stereochemistry");
index++;
}return index;
}, $fz.isPrivate = true, $fz), "~S,~N,J.smiles.SmilesAtom");
$_M(c$, "parseBond", 
($fz = function (molecule, bondSet, pattern, bond, currentAtom, isPrimitive, isBranchAtom) {
var ch = J.smiles.SmilesParser.getChar (pattern, 0);
if (ch == '.') {
if (bond != null || bondSet != null) throw  new J.smiles.InvalidSmilesException ("invalid '.'");
this.isBioSequence = (J.smiles.SmilesParser.getChar (pattern, 1) == '~');
return  new J.smiles.SmilesBond (null, null, 0, false);
}if (ch == '+' && bondSet != null) throw  new J.smiles.InvalidSmilesException ("invalid '+'");
var newBond = (bondSet == null ? (bond == null ?  new J.smiles.SmilesBond (currentAtom, null, (this.isBioSequence && currentAtom != null ? (isBranchAtom ? 112 : 96) : -1), false) : bond) : isPrimitive ? bondSet.addPrimitive () : bondSet.addBondOr ());
if (ch != '\0' && !this.checkLogic (molecule, pattern, null, newBond, currentAtom, isPrimitive, false)) {
var isBondNot = (ch == '!');
if (isBondNot) {
ch = J.smiles.SmilesParser.getChar (pattern, 1);
if (ch == '\0' || ch == '!') throw  new J.smiles.InvalidSmilesException ("invalid '!'");
}var bondType = J.smiles.SmilesBond.getBondTypeFromCode (ch);
if (bondType == 65) molecule.top.needRingMemberships = true;
if (currentAtom == null && bondType != 0) throw  new J.smiles.InvalidSmilesException ("Bond without a previous atom");
switch (bondType) {
case 769:
case 1025:
if (isBondNot) {
isBondNot = false;
bondType = (bondType == 769 ? 1025 : 769);
}molecule.haveBondStereochemistry = true;
break;
case 257:
case 513:
molecule.haveBondStereochemistry = true;
break;
case 17:
break;
case 2:
case 1:
if (currentAtom.isAromatic ()) molecule.top.needRingData = true;
break;
}
newBond.set2 (bondType, isBondNot);
if (this.isBioSequence && bondSet != null) bondSet.set2 (bondType, isBondNot);
}return newBond;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,J.smiles.SmilesBond,~S,J.smiles.SmilesBond,J.smiles.SmilesAtom,~B,~B");
$_M(c$, "checkLogic", 
($fz = function (molecule, pattern, atom, bond, currentAtom, isPrimitive, isBranchAtom) {
var pt = pattern.indexOf (',');
var len = pattern.length;
while (true) {
var haveOr = (pt > 0);
if (haveOr && !this.isSmarts || pt == 0) break;
var props = "";
pt = pattern.indexOf (';');
if (pt >= 0) {
if (!this.isSmarts || pt == 0) break;
props = "&" + pattern.substring (pt + 1);
pattern = pattern.substring (0, pt);
if (!haveOr) {
pattern += props;
props = "";
}}var index = 0;
if (haveOr) {
pattern += ",";
while ((pt = pattern.indexOf (',', index)) > 0 && pt <= len) {
var s = pattern.substring (index, pt) + props;
if (s.length == 0) throw  new J.smiles.InvalidSmilesException ("missing " + (bond == null ? "atom" : "bond") + " token");
if (bond == null) this.parseAtom (molecule, atom, s, null, null, true, false, isBranchAtom);
 else this.parseBond (molecule, bond, s, null, currentAtom, false, false);
index = pt + 1;
}
} else if ((pt = pattern.indexOf ('&')) >= 0 || bond != null && len > 1 && !isPrimitive) {
if (!this.isSmarts || pt == 0) break;
if (bond != null && pt < 0) {
if (len > 1) {
var sNew =  new J.util.SB ();
for (var i = 0; i < len; ) {
var ch = pattern.charAt (i++);
sNew.appendC (ch);
if (ch != '!' && i < len) sNew.appendC ('&');
}
pattern = sNew.toString ();
len = pattern.length;
}}pattern += "&";
while ((pt = pattern.indexOf ('&', index)) > 0 && pt <= len) {
var s = pattern.substring (index, pt) + props;
if (bond == null) this.parseAtom (molecule, atom, s, null, null, true, true, isBranchAtom);
 else this.parseBond (molecule, bond, s, null, currentAtom, true, false);
index = pt + 1;
}
} else {
return false;
}return true;
}
var ch = pattern.charAt (pt);
throw  new J.smiles.InvalidSmilesException ((this.isSmarts ? "invalid placement for '" + ch + "'" : "[" + ch + "] notation only valid with SMARTS, not SMILES,") + " in " + pattern);
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesSearch,~S,J.smiles.SmilesAtom,J.smiles.SmilesBond,J.smiles.SmilesAtom,~B,~B");
c$.getSubPattern = $_M(c$, "getSubPattern", 
($fz = function (pattern, index, ch) {
var ch2;
var margin = 1;
switch (ch) {
case '[':
ch2 = ']';
break;
case '"':
case '%':
ch2 = ch;
break;
case '(':
ch2 = ')';
break;
default:
ch2 = ch;
margin = 0;
}
var len = pattern.length;
var pCount = 1;
for (var pt = index + 1; pt < len; pt++) {
var ch1 = pattern.charAt (pt);
if (ch1 == ch2) {
pCount--;
if (pCount == 0) return pattern.substring (index + margin, pt + 1 - margin);
} else if (ch1 == ch) {
pCount++;
}}
throw  new J.smiles.InvalidSmilesException ("Unmatched " + ch);
}, $fz.isPrivate = true, $fz), "~S,~N,~S");
c$.getChar = $_M(c$, "getChar", 
($fz = function (pattern, i) {
return (i < pattern.length ? pattern.charAt (i) : '\0');
}, $fz.isPrivate = true, $fz), "~S,~N");
c$.getDigits = $_M(c$, "getDigits", 
($fz = function (pattern, index, ret) {
var pt = index;
var len = pattern.length;
while (pt < len && Character.isDigit (pattern.charAt (pt))) pt++;

try {
ret[0] = Integer.parseInt (pattern.substring (index, pt));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
ret[0] = -2147483648;
} else {
throw e;
}
}
return pt;
}, $fz.isPrivate = true, $fz), "~S,~N,~A");
c$.skipTo = $_M(c$, "skipTo", 
($fz = function (pattern, index, ch0) {
var pt = index;
var ch;
while ((ch = J.smiles.SmilesParser.getChar (pattern, ++pt)) != ch0 && ch != '\0') {
}
return (ch == '\0' ? -1 : pt);
}, $fz.isPrivate = true, $fz), "~S,~N,~S");
c$.getRingPointer = $_M(c$, "getRingPointer", 
function (i) {
return (i < 10 ? "" + i : i < 100 ? "%" + i : "%(" + i + ")");
}, "~N");
c$.cleanPattern = $_M(c$, "cleanPattern", 
function (pattern) {
pattern = J.util.TextFormat.replaceAllCharacters (pattern, " \t\n\r", "");
pattern = J.util.TextFormat.simpleReplace (pattern, "^^", "'");
var i = 0;
var i2 = 0;
while ((i = pattern.indexOf ("//*")) >= 0 && (i2 = pattern.indexOf ("*//")) >= i) pattern = pattern.substring (0, i) + pattern.substring (i2 + 3);

pattern = J.util.TextFormat.simpleReplace (pattern, "//", "");
return pattern;
}, "~S");
});
