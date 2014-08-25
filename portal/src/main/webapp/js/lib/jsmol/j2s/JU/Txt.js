Clazz.declarePackage ("JU");
Clazz.load (null, "JU.Txt", ["java.lang.Character", "$.Double", "$.Float", "JU.PT", "$.SB"], function () {
c$ = Clazz.declareType (JU, "Txt");
c$.formatStringS = Clazz.defineMethod (c$, "formatStringS", 
function (strFormat, key, strT) {
return JU.Txt.formatString (strFormat, key, strT, NaN, NaN, false);
}, "~S,~S,~S");
c$.formatStringF = Clazz.defineMethod (c$, "formatStringF", 
function (strFormat, key, floatT) {
return JU.Txt.formatString (strFormat, key, null, floatT, NaN, false);
}, "~S,~S,~N");
c$.formatStringI = Clazz.defineMethod (c$, "formatStringI", 
function (strFormat, key, intT) {
return JU.Txt.formatString (strFormat, key, "" + intT, NaN, NaN, false);
}, "~S,~S,~N");
c$.sprintf = Clazz.defineMethod (c$, "sprintf", 
function (strFormat, list, values) {
if (values == null) return strFormat;
var n = list.length;
if (n == values.length) try {
for (var o = 0; o < n; o++) {
if (values[o] == null) continue;
switch (list.charAt (o)) {
case 's':
strFormat = JU.Txt.formatString (strFormat, "s", values[o], NaN, NaN, true);
break;
case 'f':
strFormat = JU.Txt.formatString (strFormat, "f", null, (values[o]).floatValue (), NaN, true);
break;
case 'i':
strFormat = JU.Txt.formatString (strFormat, "d", "" + values[o], NaN, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "i", "" + values[o], NaN, NaN, true);
break;
case 'd':
strFormat = JU.Txt.formatString (strFormat, "e", null, NaN, (values[o]).doubleValue (), true);
break;
case 'p':
var pVal = values[o];
strFormat = JU.Txt.formatString (strFormat, "p", null, pVal.x, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "p", null, pVal.y, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "p", null, pVal.z, NaN, true);
break;
case 'q':
var qVal = values[o];
strFormat = JU.Txt.formatString (strFormat, "q", null, qVal.x, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "q", null, qVal.y, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "q", null, qVal.z, NaN, true);
strFormat = JU.Txt.formatString (strFormat, "q", null, qVal.w, NaN, true);
break;
case 'S':
var sVal = values[o];
for (var i = 0; i < sVal.length; i++) strFormat = JU.Txt.formatString (strFormat, "s", sVal[i], NaN, NaN, true);

break;
case 'F':
var fVal = values[o];
for (var i = 0; i < fVal.length; i++) strFormat = JU.Txt.formatString (strFormat, "f", null, fVal[i], NaN, true);

break;
case 'I':
var iVal = values[o];
for (var i = 0; i < iVal.length; i++) strFormat = JU.Txt.formatString (strFormat, "d", "" + iVal[i], NaN, NaN, true);

for (var i = 0; i < iVal.length; i++) strFormat = JU.Txt.formatString (strFormat, "i", "" + iVal[i], NaN, NaN, true);

break;
case 'D':
var dVal = values[o];
for (var i = 0; i < dVal.length; i++) strFormat = JU.Txt.formatString (strFormat, "e", null, NaN, dVal[i], true);

}
}
return JU.PT.rep (strFormat, "%%", "%");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
System.out.println ("TextFormat.sprintf error " + list + " " + strFormat);
return JU.PT.rep (strFormat, "%", "?");
}, "~S,~S,~A");
c$.formatString = Clazz.defineMethod (c$, "formatString", 
 function (strFormat, key, strT, floatT, doubleT, doOne) {
if (strFormat == null) return null;
if ("".equals (strFormat)) return "";
var len = key.length;
if (strFormat.indexOf ("%") < 0 || len == 0 || strFormat.indexOf (key) < 0) return strFormat;
var strLabel = "";
var ich;
var ichPercent;
var ichKey;
for (ich = 0; (ichPercent = strFormat.indexOf ('%', ich)) >= 0 && (ichKey = strFormat.indexOf (key, ichPercent + 1)) >= 0; ) {
if (ich != ichPercent) strLabel += strFormat.substring (ich, ichPercent);
ich = ichPercent + 1;
if (ichKey > ichPercent + 6) {
strLabel += '%';
continue;
}try {
var alignLeft = false;
if (strFormat.charAt (ich) == '-') {
alignLeft = true;
++ich;
}var zeroPad = false;
if (strFormat.charAt (ich) == '0') {
zeroPad = true;
++ich;
}var ch;
var width = 0;
while ((ch = strFormat.charAt (ich)) >= '0' && (ch <= '9')) {
width = (10 * width) + (ch.charCodeAt (0) - 48);
++ich;
}
var precision = 2147483647;
var isExponential = false;
if (strFormat.charAt (ich) == '.') {
++ich;
if ((ch = strFormat.charAt (ich)) == '-') {
isExponential = true;
++ich;
}if ((ch = strFormat.charAt (ich)) >= '0' && ch <= '9') {
precision = ch.charCodeAt (0) - 48;
++ich;
}if (isExponential) precision = -precision - (strT == null ? 1 : 0);
}var st = strFormat.substring (ich, ich + len);
if (!st.equals (key)) {
ich = ichPercent + 1;
strLabel += '%';
continue;
}ich += len;
if (!Float.isNaN (floatT)) strLabel += JU.PT.formatF (floatT, width, precision, alignLeft, zeroPad);
 else if (strT != null) strLabel += JU.PT.formatS (strT, width, precision, alignLeft, zeroPad);
 else if (!Double.isNaN (doubleT)) strLabel += JU.PT.formatD (doubleT, width, precision, alignLeft, zeroPad, true);
if (doOne) break;
} catch (ioobe) {
if (Clazz.exceptionOf (ioobe, IndexOutOfBoundsException)) {
ich = ichPercent;
break;
} else {
throw ioobe;
}
}
}
strLabel += strFormat.substring (ich);
return strLabel;
}, "~S,~S,~S,~N,~N,~B");
c$.formatCheck = Clazz.defineMethod (c$, "formatCheck", 
function (strFormat) {
if (strFormat == null || strFormat.indexOf ('p') < 0 && strFormat.indexOf ('q') < 0) return strFormat;
strFormat = JU.PT.rep (strFormat, "%%", "\1");
strFormat = JU.PT.rep (strFormat, "%p", "%6.2p");
strFormat = JU.PT.rep (strFormat, "%q", "%6.2q");
var format = JU.PT.split (strFormat, "%");
var sb =  new JU.SB ();
sb.append (format[0]);
for (var i = 1; i < format.length; i++) {
var f = "%" + format[i];
var pt;
if (f.length >= 3) {
if ((pt = f.indexOf ('p')) >= 0) f = JU.Txt.fdup (f, pt, 3);
if ((pt = f.indexOf ('q')) >= 0) f = JU.Txt.fdup (f, pt, 4);
}sb.append (f);
}
return sb.toString ().$replace ('\1', '%');
}, "~S");
c$.fdup = Clazz.defineMethod (c$, "fdup", 
 function (f, pt, n) {
var ch;
var count = 0;
for (var i = pt; --i >= 1; ) {
if (Character.isDigit (ch = f.charAt (i))) continue;
switch (ch) {
case '.':
if (count++ != 0) return f;
continue;
case '-':
if (i != 1 && f.charAt (i - 1) != '.') return f;
continue;
default:
return f;
}
}
var s = f.substring (0, pt + 1);
var sb =  new JU.SB ();
for (var i = 0; i < n; i++) sb.append (s);

sb.append (f.substring (pt + 1));
return sb.toString ();
}, "~S,~N,~N");
c$.leftJustify = Clazz.defineMethod (c$, "leftJustify", 
function (s, s1, s2) {
s.append (s2);
var n = s1.length - s2.length;
if (n > 0) s.append (s1.substring (0, n));
}, "JU.SB,~S,~S");
c$.rightJustify = Clazz.defineMethod (c$, "rightJustify", 
function (s, s1, s2) {
var n = s1.length - s2.length;
if (n > 0) s.append (s1.substring (0, n));
s.append (s2);
}, "JU.SB,~S,~S");
c$.safeTruncate = Clazz.defineMethod (c$, "safeTruncate", 
function (f, n) {
if (f > -0.001 && f < 0.001) f = 0;
return (f + "         ").substring (0, n);
}, "~N,~N");
c$.isWild = Clazz.defineMethod (c$, "isWild", 
function (s) {
return s != null && (s.indexOf ("*") >= 0 || s.indexOf ("?") >= 0);
}, "~S");
c$.isMatch = Clazz.defineMethod (c$, "isMatch", 
function (search, match, checkStar, allowInitialStar) {
if (search.equals (match)) return true;
var mLen = match.length;
if (mLen == 0) return false;
var isStar0 = (checkStar && allowInitialStar ? match.charAt (0) == '*' : false);
if (mLen == 1 && isStar0) return true;
var isStar1 = (checkStar && match.endsWith ("*"));
var haveQ = (match.indexOf ('?') >= 0);
if (!haveQ) {
if (isStar0) return (isStar1 ? (mLen < 3 || search.indexOf (match.substring (1, mLen - 1)) >= 0) : search.endsWith (match.substring (1)));
 else if (isStar1) return search.startsWith (match.substring (0, mLen - 1));
}var sLen = search.length;
var qqqq = "????";
var nq = 4;
while (nq < sLen) {
qqqq += qqqq;
nq += 4;
}
if (checkStar) {
if (isStar0) {
match = qqqq + match.substring (1);
mLen += nq - 1;
}if (isStar1) {
match = match.substring (0, mLen - 1) + qqqq;
mLen += nq - 1;
}}if (mLen < sLen) return false;
var ich = 0;
while (mLen > sLen) {
if (allowInitialStar && match.charAt (ich) == '?') {
++ich;
} else if (match.charAt (ich + mLen - 1) != '?') {
return false;
}--mLen;
}
for (var i = sLen; --i >= 0; ) {
var chm = match.charAt (ich + i);
if (chm == '?') continue;
var chs = search.charAt (i);
if (chm != chs && (chm != '\1' || chs != '?')) return false;
}
return true;
}, "~S,~S,~B,~B");
c$.replaceQuotedStrings = Clazz.defineMethod (c$, "replaceQuotedStrings", 
function (s, list, newList) {
var n = list.size ();
for (var i = 0; i < n; i++) {
var name = list.get (i);
var newName = newList.get (i);
if (!newName.equals (name)) s = JU.PT.rep (s, "\"" + name + "\"", "\"" + newName + "\"");
}
return s;
}, "~S,java.util.List,java.util.List");
c$.replaceStrings = Clazz.defineMethod (c$, "replaceStrings", 
function (s, list, newList) {
var n = list.size ();
for (var i = 0; i < n; i++) {
var name = list.get (i);
var newName = newList.get (i);
if (!newName.equals (name)) s = JU.PT.rep (s, name, newName);
}
return s;
}, "~S,java.util.List,java.util.List");
c$.ichMathTerminator = Clazz.defineMethod (c$, "ichMathTerminator", 
function (script, ichT, len) {
var nP = 1;
var chFirst = '\u0000';
var chLast = '\u0000';
while (nP > 0 && ++ichT < len) {
var ch = script.charAt (ichT);
if (chFirst != '\0') {
if (chLast == '\\') {
ch = '\0';
} else if (ch == chFirst) {
chFirst = '\0';
}chLast = ch;
continue;
}switch (ch) {
case '\'':
case '"':
chFirst = ch;
break;
case '{':
nP++;
break;
case '}':
nP--;
break;
}
}
return ichT;
}, "~S,~N,~N");
});
