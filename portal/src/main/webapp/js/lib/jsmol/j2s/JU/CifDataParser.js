Clazz.declarePackage ("JU");
Clazz.load (["javajs.api.GenericCifDataParser", "java.util.Hashtable", "JU.SB"], "JU.CifDataParser", ["java.lang.Character", "JU.Lst", "$.PT"], function () {
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
this.nullString = "\0";
this.fields = null;
Clazz.instantialize (this, arguments);
}, JU, "CifDataParser", null, javajs.api.GenericCifDataParser);
Clazz.prepareFields (c$, function () {
this.fileHeader =  new JU.SB ();
});
Clazz.defineMethod (c$, "setNullValue", 
function (nullString) {
this.nullString = nullString;
}, "~S");
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "getLoopData", 
function (i) {
return this.loopData[i];
}, "~N");
Clazz.overrideMethod (c$, "getFieldCount", 
function () {
return this.fieldCount;
});
Clazz.overrideMethod (c$, "getField", 
function (i) {
return this.fields[i];
}, "~N");
Clazz.overrideMethod (c$, "set", 
function (reader, br) {
this.reader = reader;
this.br = br;
return this;
}, "javajs.api.GenericLineReader,java.io.BufferedReader");
Clazz.overrideMethod (c$, "getFileHeader", 
function () {
return this.fileHeader.toString ();
});
Clazz.overrideMethod (c$, "getAllCifData", 
function () {
this.line = "";
var key;
var data = null;
var allData =  new java.util.Hashtable ();
var models =  new JU.Lst ();
allData.put ("models", models);
try {
while ((key = this.getNextToken ()) != null) {
if (key.startsWith ("global_") || key.startsWith ("data_")) {
models.addLast (data =  new java.util.Hashtable ());
data.put ("name", key);
continue;
}if (key.startsWith ("loop_")) {
this.getAllCifLoopData (data);
continue;
}if (key.charAt (0) != '_') {
System.out.println ("CIF ERROR ? should be an underscore: " + key);
} else {
var value = this.getNextToken ();
if (value == null) {
System.out.println ("CIF ERROR ? end of file; data missing: " + key);
} else {
data.put (this.fixKey (key), value);
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
return allData;
});
Clazz.defineMethod (c$, "getAllCifLoopData", 
 function (data) {
var key;
var keyWords =  new JU.Lst ();
while ((key = this.peekToken ()) != null && key.charAt (0) == '_') {
key = this.fixKey (this.getTokenPeeked ());
keyWords.addLast (key);
data.put (key,  new JU.Lst ());
}
this.fieldCount = keyWords.size ();
if (this.fieldCount == 0) return;
this.loopData =  new Array (this.fieldCount);
while (this.getData ()) for (var i = 0; i < this.fieldCount; i++) (data.get (keyWords.get (i))).addLast (this.loopData[i]);


}, "java.util.Map");
Clazz.overrideMethod (c$, "readLine", 
function () {
try {
this.line = (this.reader == null ? this.br.readLine () : this.reader.readNextLine ());
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
Clazz.overrideMethod (c$, "getData", 
function () {
for (var i = 0; i < this.fieldCount; ++i) if ((this.loopData[i] = this.getNextDataToken ()) == null) return false;

return true;
});
Clazz.overrideMethod (c$, "skipLoop", 
function () {
var str;
while ((str = this.peekToken ()) != null && str.charAt (0) == '_') this.getTokenPeeked ();

while (this.getNextDataToken () != null) {
}
});
Clazz.overrideMethod (c$, "getNextToken", 
function () {
while (!this.strHasMoreTokens ()) if (this.setStringNextLine () == null) return null;

return this.nextStrToken ();
});
Clazz.overrideMethod (c$, "getNextDataToken", 
function () {
var str = this.peekToken ();
if (str == null) return null;
if (this.wasUnQuoted) if (str.charAt (0) == '_' || str.startsWith ("loop_") || str.startsWith ("data_") || str.startsWith ("stop_") || str.startsWith ("global_")) return null;
return this.getTokenPeeked ();
});
Clazz.overrideMethod (c$, "peekToken", 
function () {
while (!this.strHasMoreTokens ()) if (this.setStringNextLine () == null) return null;

var ich = this.ich;
this.strPeeked = this.nextStrToken ();
this.ichPeeked = this.ich;
this.ich = ich;
return this.strPeeked;
});
Clazz.overrideMethod (c$, "getTokenPeeked", 
function () {
this.ich = this.ichPeeked;
return this.strPeeked;
});
Clazz.overrideMethod (c$, "fullTrim", 
function (str) {
var pt0 = -1;
var pt1 = str.length;
while (++pt0 < pt1 && Character.isWhitespace (str.charAt (pt0))) {
}
while (--pt1 > pt0 && Character.isWhitespace (str.charAt (pt1))) {
}
return str.substring (pt0, pt1 + 1);
}, "~S");
Clazz.overrideMethod (c$, "toUnicode", 
function (data) {
var pt;
try {
while ((pt = data.indexOf ('\\')) >= 0) {
var c = data.charCodeAt (pt + 1);
var ch = (c >= 65 && c <= 90 ? "ABX\u0394E\u03a6\u0393HI_K\u039bMNO\u03a0\u0398P\u03a3TY_\u03a9\u039e\u03a5Z".substring (c - 65, c - 64) : c >= 97 && c <= 122 ? "\u03b1\u03b2\u03c7\u03a4\u03a5\u03c6\u03b3\u03b7\u03b9_\u03ba\u03bb\u03bc\u03bd\u03bf\u03c0\u03b8\u03c1\u03c3\u03c4\u03c5_\u03c9\u03be\u03c5\u03b6".substring (c - 97, c - 96) : "_");
data = data.substring (0, pt) + ch + data.substring (pt + 2);
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return data;
}, "~S");
Clazz.overrideMethod (c$, "parseLoopParameters", 
function (fields, fieldOf, propertyOf) {
var propertyCount = 0;
if (fields == null) {
this.fields =  new Array (100);
} else {
if (!JU.CifDataParser.htFields.containsKey (fields[0])) for (var i = fields.length; --i >= 0; ) JU.CifDataParser.htFields.put (fields[i], Integer.$valueOf (i));

for (var i = fields.length; --i >= 0; ) fieldOf[i] = -1;

propertyCount = fields.length;
}this.fieldCount = 0;
while (true) {
var str = this.peekToken ();
if (str == null) {
this.fieldCount = 0;
break;
}if (str.charAt (0) != '_') break;
var pt = this.fieldCount++;
str = this.fixKey (this.getTokenPeeked ());
if (fields == null) {
this.fields[propertyOf[pt] = fieldOf[pt] = pt] = str;
continue;
}var iField = JU.CifDataParser.htFields.get (str);
var i = (iField == null ? -1 : iField.intValue ());
if ((propertyOf[pt] = i) != -1) fieldOf[i] = pt;
}
if (this.fieldCount > 0) this.loopData =  new Array (this.fieldCount);
return propertyCount;
}, "~A,~A,~A");
Clazz.defineMethod (c$, "fixKey", 
 function (key) {
if (key.startsWith ("_magnetic")) key = key.substring (9);
return (JU.PT.rep (key, ".", "_").toLowerCase ());
}, "~S");
Clazz.defineMethod (c$, "setString", 
 function (str) {
this.str = this.line = str;
this.cch = (str == null ? 0 : str.length);
this.ich = 0;
}, "~S");
Clazz.defineMethod (c$, "setStringNextLine", 
 function () {
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
});
Clazz.defineMethod (c$, "strHasMoreTokens", 
 function () {
if (this.str == null) return false;
var ch = '#';
while (this.ich < this.cch && ((ch = this.str.charAt (this.ich)) == ' ' || ch == '\t')) ++this.ich;

return (this.ich < this.cch && ch != '#');
});
Clazz.defineMethod (c$, "nextStrToken", 
 function () {
if (this.ich == this.cch) return null;
var ichStart = this.ich;
var ch = this.str.charAt (ichStart);
if (ch != '\'' && ch != '"' && ch != '\1') {
this.wasUnQuoted = true;
while (this.ich < this.cch && (ch = this.str.charAt (this.ich)) != ' ' && ch != '\t') ++this.ich;

if (this.ich == ichStart + 1) if (this.nullString != null && (this.str.charAt (ichStart) == '.' || this.str.charAt (ichStart) == '?')) return this.nullString;
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
});
c$.htFields = c$.prototype.htFields =  new java.util.Hashtable ();
Clazz.defineStatics (c$,
"grABC", "ABX\u0394E\u03a6\u0393HI_K\u039bMNO\u03a0\u0398P\u03a3TY_\u03a9\u039e\u03a5Z",
"grabc", "\u03b1\u03b2\u03c7\u03a4\u03a5\u03c6\u03b3\u03b7\u03b9_\u03ba\u03bb\u03bc\u03bd\u03bf\u03c0\u03b8\u03c1\u03c3\u03c4\u03c5_\u03c9\u03be\u03c5\u03b6");
});
