Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.TextFormat", ["java.lang.Boolean", "$.Character", "$.Double", "$.Float", "J.util.Parser", "$.SB"], function () {
c$ = Clazz.declareType (J.util, "TextFormat");
Clazz.prepareFields (c$, function () {
{
J.util.TextFormat.useNumberLocalization[0] = Boolean.TRUE;
}});
c$.setUseNumberLocalization = $_M(c$, "setUseNumberLocalization", 
function (TF) {
J.util.TextFormat.useNumberLocalization[0] = (TF ? Boolean.TRUE : Boolean.FALSE);
}, "~B");
c$.formatDecimal = $_M(c$, "formatDecimal", 
function (value, decimalDigits) {
if (decimalDigits == 2147483647 || value == -Infinity || value == Infinity || Float.isNaN (value)) return "" + value;
var n;
if (decimalDigits < 0) {
decimalDigits = -decimalDigits;
if (decimalDigits > J.util.TextFormat.formattingStrings.length) decimalDigits = J.util.TextFormat.formattingStrings.length;
if (value == 0) return J.util.TextFormat.formattingStrings[decimalDigits] + "E+0";
n = 0;
var d;
if (Math.abs (value) < 1) {
n = 10;
d = value * 1e-10;
} else {
n = -10;
d = value * 1e10;
}var s = ("" + d).toUpperCase ();
var i = s.indexOf ("E");
n = J.util.Parser.parseInt (s.substring (i + 1)) + n;
return (i < 0 ? "" + value : J.util.TextFormat.formatDecimal (J.util.Parser.parseFloatStr (s.substring (0, i)), decimalDigits - 1) + "E" + (n >= 0 ? "+" : "") + n);
}if (decimalDigits >= J.util.TextFormat.formattingStrings.length) decimalDigits = J.util.TextFormat.formattingStrings.length - 1;
var s1 = ("" + value).toUpperCase ();
var isNeg = s1.startsWith ("-");
if (isNeg) s1 = s1.substring (1);
var pt = s1.indexOf (".");
if (pt < 0) return s1 + J.util.TextFormat.formattingStrings[decimalDigits].substring (1);
var pt1 = s1.indexOf ("E-");
if (pt1 > 0) {
n = J.util.Parser.parseInt (s1.substring (pt1 + 1));
s1 = "0." + "0000000000000000000000000000000000000000".substring (0, -n - 1) + s1.substring (0, 1) + s1.substring (2, pt1);
pt = 1;
}pt1 = s1.indexOf ("E");
if (pt1 > 0) {
n = J.util.Parser.parseInt (s1.substring (pt1 + 1));
s1 = s1.substring (0, 1) + s1.substring (2, pt1) + "0000000000000000000000000000000000000000";
s1 = s1.substring (0, n + 1) + "." + s1.substring (n + 1);
pt = s1.indexOf (".");
}var len = s1.length;
var pt2 = decimalDigits + pt + 1;
if (pt2 < len && s1.charAt (pt2) >= '5') {
return J.util.TextFormat.formatDecimal (value + (isNeg ? -1 : 1) * J.util.TextFormat.formatAdds[decimalDigits], decimalDigits);
}var sb = J.util.SB.newS (s1.substring (0, (decimalDigits == 0 ? pt : ++pt)));
for (var i = 0; i < decimalDigits; i++, pt++) {
if (pt < len) sb.appendC (s1.charAt (pt));
 else sb.appendC ('0');
}
s1 = (isNeg ? "-" : "") + sb;
return (Boolean.TRUE.equals (J.util.TextFormat.useNumberLocalization[0]) ? s1 : s1.$replace (',', '.'));
}, "~N,~N");
c$.formatF = $_M(c$, "formatF", 
function (value, width, precision, alignLeft, zeroPad) {
return J.util.TextFormat.formatS (J.util.TextFormat.formatDecimal (value, precision), width, 0, alignLeft, zeroPad);
}, "~N,~N,~N,~B,~B");
c$.formatD = $_M(c$, "formatD", 
function (value, width, precision, alignLeft, zeroPad, allowOverflow) {
return J.util.TextFormat.formatS (J.util.TextFormat.formatDecimal (value, -1 - precision), width, 0, alignLeft, zeroPad);
}, "~N,~N,~N,~B,~B,~B");
c$.formatS = $_M(c$, "formatS", 
function (value, width, precision, alignLeft, zeroPad) {
if (value == null) return "";
var len = value.length;
if (precision != 2147483647 && precision > 0 && precision < len) value = value.substring (0, precision);
 else if (precision < 0 && len + precision >= 0) value = value.substring (len + precision + 1);
var padLength = width - value.length;
if (padLength <= 0) return value;
var isNeg = (zeroPad && !alignLeft && value.charAt (0) == '-');
var padChar = (zeroPad ? '0' : ' ');
var padChar0 = (isNeg ? '-' : padChar);
var sb =  new J.util.SB ();
if (alignLeft) sb.append (value);
sb.appendC (padChar0);
for (var i = padLength; --i > 0; ) sb.appendC (padChar);

if (!alignLeft) sb.append (isNeg ? padChar + value.substring (1) : value);
return sb.toString ();
}, "~S,~N,~N,~B,~B");
c$.formatStringS = $_M(c$, "formatStringS", 
function (strFormat, key, strT) {
return J.util.TextFormat.formatString (strFormat, key, strT, NaN, NaN, false);
}, "~S,~S,~S");
c$.formatStringF = $_M(c$, "formatStringF", 
function (strFormat, key, floatT) {
return J.util.TextFormat.formatString (strFormat, key, null, floatT, NaN, false);
}, "~S,~S,~N");
c$.formatStringI = $_M(c$, "formatStringI", 
function (strFormat, key, intT) {
return J.util.TextFormat.formatString (strFormat, key, "" + intT, NaN, NaN, false);
}, "~S,~S,~N");
c$.sprintf = $_M(c$, "sprintf", 
function (strFormat, list, values) {
if (values == null) return strFormat;
var n = list.length;
if (n == values.length) try {
for (var o = 0; o < n; o++) {
if (values[o] == null) continue;
switch (list.charAt (o)) {
case 's':
strFormat = J.util.TextFormat.formatString (strFormat, "s", values[o], NaN, NaN, true);
break;
case 'f':
strFormat = J.util.TextFormat.formatString (strFormat, "f", null, (values[o]).floatValue (), NaN, true);
break;
case 'i':
strFormat = J.util.TextFormat.formatString (strFormat, "d", "" + values[o], NaN, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "i", "" + values[o], NaN, NaN, true);
break;
case 'd':
strFormat = J.util.TextFormat.formatString (strFormat, "e", null, NaN, (values[o]).doubleValue (), true);
break;
case 'p':
var pVal = values[o];
strFormat = J.util.TextFormat.formatString (strFormat, "p", null, pVal.x, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "p", null, pVal.y, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "p", null, pVal.z, NaN, true);
break;
case 'q':
var qVal = values[o];
strFormat = J.util.TextFormat.formatString (strFormat, "q", null, qVal.x, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "q", null, qVal.y, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "q", null, qVal.z, NaN, true);
strFormat = J.util.TextFormat.formatString (strFormat, "q", null, qVal.w, NaN, true);
break;
case 'S':
var sVal = values[o];
for (var i = 0; i < sVal.length; i++) strFormat = J.util.TextFormat.formatString (strFormat, "s", sVal[i], NaN, NaN, true);

break;
case 'F':
var fVal = values[o];
for (var i = 0; i < fVal.length; i++) strFormat = J.util.TextFormat.formatString (strFormat, "f", null, fVal[i], NaN, true);

break;
case 'I':
var iVal = values[o];
for (var i = 0; i < iVal.length; i++) strFormat = J.util.TextFormat.formatString (strFormat, "d", "" + iVal[i], NaN, NaN, true);

for (var i = 0; i < iVal.length; i++) strFormat = J.util.TextFormat.formatString (strFormat, "i", "" + iVal[i], NaN, NaN, true);

break;
case 'D':
var dVal = values[o];
for (var i = 0; i < dVal.length; i++) strFormat = J.util.TextFormat.formatString (strFormat, "e", null, NaN, dVal[i], true);

}
}
return J.util.TextFormat.simpleReplace (strFormat, "%%", "%");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
System.out.println ("TextFormat.sprintf error " + list + " " + strFormat);
return J.util.TextFormat.simpleReplace (strFormat, "%", "?");
}, "~S,~S,~A");
c$.formatString = $_M(c$, "formatString", 
($fz = function (strFormat, key, strT, floatT, doubleT, doOne) {
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
if (!Float.isNaN (floatT)) strLabel += J.util.TextFormat.formatF (floatT, width, precision, alignLeft, zeroPad);
 else if (strT != null) strLabel += J.util.TextFormat.formatS (strT, width, precision, alignLeft, zeroPad);
 else if (!Double.isNaN (doubleT)) strLabel += J.util.TextFormat.formatD (doubleT, width, precision, alignLeft, zeroPad, true);
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
}, $fz.isPrivate = true, $fz), "~S,~S,~S,~N,~N,~B");
c$.formatCheck = $_M(c$, "formatCheck", 
function (strFormat) {
if (strFormat == null || strFormat.indexOf ('p') < 0 && strFormat.indexOf ('q') < 0) return strFormat;
strFormat = J.util.TextFormat.simpleReplace (strFormat, "%%", "\1");
strFormat = J.util.TextFormat.simpleReplace (strFormat, "%p", "%6.2p");
strFormat = J.util.TextFormat.simpleReplace (strFormat, "%q", "%6.2q");
var format = J.util.TextFormat.split (strFormat, '%');
var sb =  new J.util.SB ();
sb.append (format[0]);
for (var i = 1; i < format.length; i++) {
var f = "%" + format[i];
var pt;
if (f.length >= 3) {
if ((pt = f.indexOf ('p')) >= 0) f = J.util.TextFormat.fdup (f, pt, 3);
if ((pt = f.indexOf ('q')) >= 0) f = J.util.TextFormat.fdup (f, pt, 4);
}sb.append (f);
}
return sb.toString ().$replace ('\1', '%');
}, "~S");
c$.fdup = $_M(c$, "fdup", 
($fz = function (f, pt, n) {
var ch;
var count = 0;
for (var i = pt; --i >= 1; ) {
if (Character.isDigit (ch = f.charAt (i))) continue;
switch (ch) {
case '.':
if (count++ != 0) return f;
continue;
case '-':
if (i != 1) return f;
continue;
default:
return f;
}
}
var s = f.substring (0, pt + 1);
var sb =  new J.util.SB ();
for (var i = 0; i < n; i++) sb.append (s);

sb.append (f.substring (pt + 1));
return sb.toString ();
}, $fz.isPrivate = true, $fz), "~S,~N,~N");
c$.splitChars = $_M(c$, "splitChars", 
function (text, run) {
if (text.length == 0) return  new Array (0);
var n = 1;
var i = text.indexOf (run);
var lines;
var runLen = run.length;
if (i < 0 || runLen == 0) {
lines =  new Array (1);
lines[0] = text;
return lines;
}var len = text.length - runLen;
for (; i >= 0 && i < len; n++) i = text.indexOf (run, i + runLen);

lines =  new Array (n);
i = 0;
var ipt = 0;
var pt = 0;
for (; (ipt = text.indexOf (run, i)) >= 0 && pt + 1 < n; ) {
lines[pt++] = text.substring (i, ipt);
i = ipt + runLen;
}
if (text.indexOf (run, len) != len) len += runLen;
lines[pt] = text.substring (i, len);
return lines;
}, "~S,~S");
c$.replaceAllCharacters = $_M(c$, "replaceAllCharacters", 
function (str, strFrom, strTo) {
for (var i = strFrom.length; --i >= 0; ) {
var chFrom = strFrom.substring (i, i + 1);
str = J.util.TextFormat.simpleReplace (str, chFrom, strTo);
}
return str;
}, "~S,~S,~S");
c$.replaceAllCharacter = $_M(c$, "replaceAllCharacter", 
function (str, strFrom, chTo) {
if (str == null) return null;
for (var i = strFrom.length; --i >= 0; ) str = str.$replace (strFrom.charAt (i), chTo);

return str;
}, "~S,~S,~S");
c$.simpleReplace = $_M(c$, "simpleReplace", 
function (str, strFrom, strTo) {
if (str == null || strFrom.length == 0 || str.indexOf (strFrom) < 0) return str;
var isOnce = (strTo.indexOf (strFrom) >= 0);
do {
str = str.$replace (strFrom, strTo);
} while (!isOnce && str.indexOf (strFrom) >= 0);
return str;
}, "~S,~S,~S");
c$.trim = $_M(c$, "trim", 
function (str, chars) {
if (chars.length == 0) return str.trim ();
var len = str.length;
var k = 0;
while (k < len && chars.indexOf (str.charAt (k)) >= 0) k++;

var m = str.length - 1;
while (m > k && chars.indexOf (str.charAt (m)) >= 0) m--;

return str.substring (k, m + 1);
}, "~S,~S");
c$.split = $_M(c$, "split", 
function (text, ch) {
return J.util.TextFormat.splitChars (text, "" + ch);
}, "~S,~S");
c$.lFill = $_M(c$, "lFill", 
function (s, s1, s2) {
s.append (s2);
var n = s1.length - s2.length;
if (n > 0) s.append (s1.substring (0, n));
}, "J.util.SB,~S,~S");
c$.rFill = $_M(c$, "rFill", 
function (s, s1, s2) {
var n = s1.length - s2.length;
if (n > 0) s.append (s1.substring (0, n));
s.append (s2);
}, "J.util.SB,~S,~S");
c$.safeTruncate = $_M(c$, "safeTruncate", 
function (f, n) {
if (f > -0.001 && f < 0.001) f = 0;
return (f + "         ").substring (0, n);
}, "~N,~N");
c$.isWild = $_M(c$, "isWild", 
function (s) {
return s != null && (s.indexOf ("*") >= 0 || s.indexOf ("?") >= 0);
}, "~S");
c$.isMatch = $_M(c$, "isMatch", 
function (s, strWildcard, checkStar, allowInitialStar) {
var ich = 0;
var cchWildcard = strWildcard.length;
var cchs = s.length;
if (cchs == 0 || cchWildcard == 0) return (cchs == cchWildcard || cchWildcard == 1 && strWildcard.charAt (0) == '*');
var isStar0 = (checkStar && allowInitialStar ? strWildcard.charAt (0) == '*' : false);
if (isStar0 && strWildcard.charAt (cchWildcard - 1) == '*') return (cchWildcard < 3 || s.indexOf (strWildcard.substring (1, cchWildcard - 1)) >= 0);
var qqq = "????";
while (qqq.length < s.length) qqq += qqq;

if (checkStar) {
if (allowInitialStar && isStar0) strWildcard = qqq + strWildcard.substring (1);
if (strWildcard.charAt (ich = strWildcard.length - 1) == '*') strWildcard = strWildcard.substring (0, ich) + qqq;
cchWildcard = strWildcard.length;
}if (cchWildcard < cchs) return false;
ich = 0;
while (cchWildcard > cchs) {
if (allowInitialStar && strWildcard.charAt (ich) == '?') {
++ich;
} else if (strWildcard.charAt (ich + cchWildcard - 1) != '?') {
return false;
}--cchWildcard;
}
for (var i = cchs; --i >= 0; ) {
var charWild = strWildcard.charAt (ich + i);
if (charWild == '?') continue;
if (charWild != s.charAt (i) && (charWild != '\1' || s.charAt (i) != '?')) return false;
}
return true;
}, "~S,~S,~B,~B");
c$.join = $_M(c$, "join", 
function (s, c, i0) {
if (s.length < i0) return null;
var sb =  new J.util.SB ();
sb.append (s[i0++]);
for (var i = i0; i < s.length; i++) sb.appendC (c).append (s[i]);

return sb.toString ();
}, "~A,~S,~N");
c$.replaceQuotedStrings = $_M(c$, "replaceQuotedStrings", 
function (s, list, newList) {
var n = list.size ();
for (var i = 0; i < n; i++) {
var name = list.get (i);
var newName = newList.get (i);
if (!newName.equals (name)) s = J.util.TextFormat.simpleReplace (s, "\"" + name + "\"", "\"" + newName + "\"");
}
return s;
}, "~S,java.util.List,java.util.List");
c$.replaceStrings = $_M(c$, "replaceStrings", 
function (s, list, newList) {
var n = list.size ();
for (var i = 0; i < n; i++) {
var name = list.get (i);
var newName = newList.get (i);
if (!newName.equals (name)) s = J.util.TextFormat.simpleReplace (s, name, newName);
}
return s;
}, "~S,java.util.List,java.util.List");
c$.ichMathTerminator = $_M(c$, "ichMathTerminator", 
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
Clazz.defineStatics (c$,
"formattingStrings", ["0", "0.0", "0.00", "0.000", "0.0000", "0.00000", "0.000000", "0.0000000", "0.00000000", "0.000000000"],
"zeros", "0000000000000000000000000000000000000000",
"formatAdds", [0.5, 0.05, 0.005, 0.0005, 0.00005, 0.000005, 0.0000005, 0.00000005, 0.000000005, 0.0000000005]);
c$.useNumberLocalization = c$.prototype.useNumberLocalization =  new Array (1);
});
