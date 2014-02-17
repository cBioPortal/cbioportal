Clazz.declarePackage ("J.io");
Clazz.load (["J.util.SB"], "J.io.CifDataReader", ["java.util.Hashtable", "J.util.JmolList", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.reader = null;
this.br = null;
this.line = null;
this.str = null;
this.ich = 0;
this.cch = 0;
this.wasUnQuoted = false;
this.strPeeked = null;
this.ichPeeked = 0;
this.fieldCount = 0;
this.loopData = null;
this.fileHeader = null;
this.isHeader = true;
this.data = null;
this.allData = null;
Clazz.instantialize (this, arguments);
}, J.io, "CifDataReader");
Clazz.prepareFields (c$, function () {
this.fileHeader =  new J.util.SB ();
});
Clazz.makeConstructor (c$, 
function (reader) {
this.reader = reader;
}, "J.api.JmolLineReader");
Clazz.makeConstructor (c$, 
function (br) {
this.br = br;
}, "java.io.BufferedReader");
$_M(c$, "getFileHeader", 
function () {
return this.fileHeader.toString ();
});
c$.readCifData = $_M(c$, "readCifData", 
function (br) {
var cdr =  new J.io.CifDataReader (br);
return cdr.getAllCifData ();
}, "java.io.BufferedReader");
$_M(c$, "getAllCifData", 
($fz = function () {
this.line = "";
var key;
this.allData =  new java.util.Hashtable ();
var models =  new J.util.JmolList ();
this.allData.put ("models", models);
try {
while ((key = this.getNextToken ()) != null) {
if (key.startsWith ("global_") || key.startsWith ("data_")) {
models.addLast (this.data =  new java.util.Hashtable ());
this.data.put ("name", key);
continue;
}if (key.startsWith ("loop_")) {
this.getCifLoopData ();
continue;
}if (key.indexOf ("_") != 0) {
J.util.Logger.warn ("CIF ERROR ? should be an underscore: " + key);
} else {
var value = this.getNextToken ();
if (value == null) {
J.util.Logger.warn ("CIF ERROR ? end of file; data missing: " + key);
} else {
this.data.put (key, value);
}}}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
try {
if (this.br != null) this.br.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return this.allData;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readLine", 
function () {
try {
this.line = (this.reader != null ? this.reader.readNextLine () : this.br.readLine ());
if (this.line == null) return null;
if (this.isHeader) {
if (this.line.startsWith ("#")) this.fileHeader.append (this.line).appendC ('\n');
 else this.isHeader = false;
}return this.line;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
});
$_M(c$, "getData", 
function () {
for (var i = 0; i < this.fieldCount; ++i) if ((this.loopData[i] = this.getNextDataToken ()) == null) return false;

return true;
});
$_M(c$, "getNextToken", 
function () {
while (!this.hasMoreTokens ()) if (this.setStringNextLine () == null) return null;

return this.nextToken ();
});
$_M(c$, "setString", 
($fz = function (str) {
this.str = this.line = str;
this.cch = (str == null ? 0 : str.length);
this.ich = 0;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setStringNextLine", 
($fz = function () {
this.setString (this.readLine ());
if (this.line == null || this.line.length == 0) return this.line;
if (this.line.charAt (0) != ';') {
if (this.str.startsWith ("###non-st#")) this.ich = 10;
return this.line;
}this.ich = 1;
var str = '\1' + this.line.substring (1) + '\n';
while (this.readLine () != null) {
if (this.line.startsWith (";")) {
str = str.substring (0, str.length - 1) + '\1' + this.line.substring (1);
break;
}str += this.line + '\n';
}
this.setString (str);
return str;
}, $fz.isPrivate = true, $fz));
$_M(c$, "hasMoreTokens", 
($fz = function () {
if (this.str == null) return false;
var ch = '#';
while (this.ich < this.cch && ((ch = this.str.charAt (this.ich)) == ' ' || ch == '\t')) ++this.ich;

return (this.ich < this.cch && ch != '#');
}, $fz.isPrivate = true, $fz));
$_M(c$, "nextToken", 
($fz = function () {
if (this.ich == this.cch) return null;
var ichStart = this.ich;
var ch = this.str.charAt (ichStart);
if (ch != '\'' && ch != '"' && ch != '\1') {
this.wasUnQuoted = true;
while (this.ich < this.cch && (ch = this.str.charAt (this.ich)) != ' ' && ch != '\t') ++this.ich;

if (this.ich == ichStart + 1) if (this.str.charAt (ichStart) == '.' || this.str.charAt (ichStart) == '?') return "\0";
var s = this.str.substring (ichStart, this.ich);
return s;
}this.wasUnQuoted = false;
var chOpeningQuote = ch;
var previousCharacterWasQuote = false;
while (++this.ich < this.cch) {
ch = this.str.charAt (this.ich);
if (previousCharacterWasQuote && (ch == ' ' || ch == '\t')) break;
previousCharacterWasQuote = (ch == chOpeningQuote);
}
if (this.ich == this.cch) {
if (previousCharacterWasQuote) return this.str.substring (ichStart + 1, this.ich - 1);
return this.str.substring (ichStart, this.ich);
}++this.ich;
return this.str.substring (ichStart + 1, this.ich - 2);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getNextDataToken", 
function () {
var str = this.peekToken ();
if (str == null) return null;
if (this.wasUnQuoted) if (str.charAt (0) == '_' || str.startsWith ("loop_") || str.startsWith ("data_") || str.startsWith ("stop_") || str.startsWith ("global_")) return null;
return this.getTokenPeeked ();
});
$_M(c$, "peekToken", 
function () {
while (!this.hasMoreTokens ()) if (this.setStringNextLine () == null) return null;

var ich = this.ich;
this.strPeeked = this.nextToken ();
this.ichPeeked = this.ich;
this.ich = ich;
return this.strPeeked;
});
$_M(c$, "getTokenPeeked", 
function () {
this.ich = this.ichPeeked;
return this.strPeeked;
});
$_M(c$, "fullTrim", 
function (str) {
var pt0 = 0;
var pt1 = str.length;
for (; pt0 < pt1; pt0++) if ("\n\t ".indexOf (str.charAt (pt0)) < 0) break;

for (; pt0 < pt1; pt1--) if ("\n\t ".indexOf (str.charAt (pt1 - 1)) < 0) break;

return str.substring (pt0, pt1);
}, "~S");
$_M(c$, "getCifLoopData", 
($fz = function () {
var str;
var keyWords =  new J.util.JmolList ();
while ((str = this.peekToken ()) != null && str.charAt (0) == '_') {
str = this.getTokenPeeked ();
keyWords.addLast (str);
this.data.put (str,  new J.util.JmolList ());
}
this.fieldCount = keyWords.size ();
if (this.fieldCount == 0) return;
this.loopData =  new Array (this.fieldCount);
while (this.getData ()) {
for (var i = 0; i < this.fieldCount; i++) {
(this.data.get (keyWords.get (i))).addLast (this.loopData[i]);
}
}
}, $fz.isPrivate = true, $fz));
});
