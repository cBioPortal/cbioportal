Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.XmlReader", ["java.lang.Character", "J.io.XmlUtil", "J.util.Escape", "$.P3", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.br = null;
this.line = null;
Clazz.instantialize (this, arguments);
}, J.io, "XmlReader");
$_M(c$, "getLine", 
function () {
return this.line;
});
Clazz.makeConstructor (c$, 
function (br) {
this.br = br;
}, "java.io.BufferedReader");
$_M(c$, "toTag", 
function (name) {
this.skipTo ("<" + name);
if (this.line == null) return "";
var i = this.line.indexOf ("<" + name) + name.length + 1;
if (i == this.line.length) return this.line;
if (this.line.charAt (i) == ' ' || this.line.charAt (i) == '>') return this.line;
this.line = null;
return this.toTag (name);
}, "~S");
$_M(c$, "skipTag", 
function (name) {
this.skipTo ("</" + name + ">");
}, "~S");
$_M(c$, "getXmlData", 
function (name, data, withTag, allowSelfCloseOption) {
var closer = "</" + name + ">";
var tag = "<" + name;
if (data == null) {
var sb =  new J.util.SB ();
try {
if (this.line == null) this.line = this.br.readLine ();
while (this.line.indexOf (tag) < 0) {
this.line = this.br.readLine ();
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
sb.append (this.line);
var selfClosed = false;
var pt = this.line.indexOf ("/>");
var pt1 = this.line.indexOf (">");
if (pt1 < 0 || pt == pt1 - 1) selfClosed = allowSelfCloseOption;
while (this.line.indexOf (closer) < 0 && (!selfClosed || this.line.indexOf ("/>") < 0)) sb.append (this.line = this.br.readLine ());

data = sb.toString ();
}return J.io.XmlReader.extractTag (data, tag, closer, withTag);
}, "~S,~S,~B,~B");
c$.extractTagOnly = $_M(c$, "extractTagOnly", 
function (data, tag) {
return J.io.XmlReader.extractTag (data, "<" + tag + ">", "</" + tag + ">", false);
}, "~S,~S");
c$.extractTag = $_M(c$, "extractTag", 
($fz = function (data, tag, closer, withTag) {
var pt1 = data.indexOf (tag);
if (pt1 < 0) return "";
var pt2 = data.indexOf (closer, pt1);
if (pt2 < 0) {
pt2 = data.indexOf ("/>", pt1);
closer = "/>";
}if (pt2 < 0) return "";
if (withTag) {
pt2 += closer.length;
return data.substring (pt1, pt2);
}var quoted = false;
for (; pt1 < pt2; pt1++) {
var ch;
if ((ch = data.charAt (pt1)) == '"') quoted = !quoted;
 else if (quoted && ch == '\\') pt1++;
 else if (!quoted && (ch == '>' || ch == '/')) break;
}
if (pt1 >= pt2) return "";
while (Character.isWhitespace (data.charAt (++pt1))) {
}
return J.io.XmlUtil.unwrapCdata (data.substring (pt1, pt2));
}, $fz.isPrivate = true, $fz), "~S,~S,~S,~B");
c$.getXmlAttrib = $_M(c$, "getXmlAttrib", 
function (data, what) {
var nexta =  Clazz.newIntArray (1, 0);
var pt = J.io.XmlReader.setNext (data, what, nexta, 1);
if (pt < 2 || (pt = J.io.XmlReader.setNext (data, "\"", nexta, 0)) < 2) return "";
var pt1 = J.io.XmlReader.setNext (data, "\"", nexta, -1);
return (pt1 <= 0 ? "" : data.substring (pt, pt1));
}, "~S,~S");
$_M(c$, "getXmlPoint", 
function (data, key) {
var spt = J.io.XmlReader.getXmlAttrib (data, key).$replace ('(', '{').$replace (')', '}');
var value = J.util.Escape.uP (spt);
if (Clazz.instanceOf (value, J.util.P3)) return value;
return  new J.util.P3 ();
}, "~S,~S");
c$.setNext = $_M(c$, "setNext", 
($fz = function (data, what, next, offset) {
var ipt = next[0];
if (ipt < 0 || (ipt = data.indexOf (what, next[0])) < 0) return -1;
ipt += what.length;
next[0] = ipt + offset;
if (offset > 0 && ipt < data.length && data.charAt (ipt) != '=') return J.io.XmlReader.setNext (data, what, next, offset);
return next[0];
}, $fz.isPrivate = true, $fz), "~S,~S,~A,~N");
$_M(c$, "skipTo", 
($fz = function (key) {
if (this.line == null) this.line = this.br.readLine ();
while (this.line != null && this.line.indexOf (key) < 0) this.line = this.br.readLine ();

}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "isNext", 
function (name) {
if (this.line == null || this.line.indexOf ("</") >= 0 && this.line.indexOf ("</") == this.line.indexOf ("<")) this.line = this.br.readLine ();
return (this.line.indexOf ("<" + name) >= 0);
}, "~S");
});
