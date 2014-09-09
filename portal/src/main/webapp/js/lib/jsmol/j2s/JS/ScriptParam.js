Clazz.declarePackage ("JS");
Clazz.load (["JS.ScriptError"], "JS.ScriptParam", ["java.lang.Float", "JU.CU", "$.Lst", "$.Measure", "$.P3", "$.P4", "$.PT", "$.Quat", "$.SB", "$.V3", "JM.TickInfo", "JS.SV", "$.T", "JU.Edge", "$.Escape", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.contextVariables = null;
this.thisContext = null;
this.iToken = 0;
this.theTok = 0;
this.theToken = null;
this.st = null;
this.slen = 0;
this.fractionalPoint = null;
this.coordinatesAreFractional = false;
this.isBondSet = false;
this.expressionResult = null;
Clazz.instantialize (this, arguments);
}, JS, "ScriptParam", JS.ScriptError);
Clazz.defineMethod (c$, "getToken", 
function (i) {
if (!this.checkToken (i)) this.error (13);
this.theToken = this.st[i];
this.theTok = this.theToken.tok;
return this.theToken;
}, "~N");
Clazz.defineMethod (c$, "tokAt", 
function (i) {
return (i < this.slen && this.st[i] != null ? this.st[i].tok : 0);
}, "~N");
Clazz.defineMethod (c$, "checkToken", 
function (i) {
return (this.iToken = i) < this.slen;
}, "~N");
Clazz.defineMethod (c$, "getParameter", 
function (key, tokType, nullAsString) {
var v = this.getContextVariableAsVariable (key);
if (v == null) {
if (nullAsString) v = this.vwr.getP (key);
 else if ((v = this.vwr.getPOrNull (key)) == null) return null;
}switch (tokType) {
case 1073742190:
return JS.SV.getVariable (v);
case 4:
if (!(Clazz.instanceOf (v, JU.Lst))) break;
var sv = v;
var sb =  new JU.SB ();
for (var i = 0; i < sv.size (); i++) sb.append (sv.get (i).asString ()).appendC ('\n');

return sb.toString ();
}
return (Clazz.instanceOf (v, JS.SV) ? JS.SV.oValue (v) : v);
}, "~S,~N,~B");
Clazz.defineMethod (c$, "getStringParameter", 
function ($var, orReturnName) {
var v = this.getContextVariableAsVariable ($var);
if (v != null) return v.asString ();
var val = "" + this.vwr.getP ($var);
return (val.length == 0 && orReturnName ? $var : val);
}, "~S,~B");
Clazz.defineMethod (c$, "getContextVariableAsVariable", 
function ($var) {
if ($var.equals ("expressionBegin")) return null;
$var = $var.toLowerCase ();
return (this.contextVariables != null && this.contextVariables.containsKey ($var) ? this.contextVariables.get ($var) : this.thisContext == null ? null : this.thisContext.getVariable ($var));
}, "~S");
Clazz.defineMethod (c$, "paramAsStr", 
function (i) {
this.getToken (i);
if (this.theToken == null) this.error (13);
return JS.SV.sValue (this.theToken);
}, "~N");
Clazz.defineMethod (c$, "stringParameter", 
function (index) {
if (!this.checkToken (index) || this.getToken (index).tok != 4) this.error (41);
return this.theToken.value;
}, "~N");
Clazz.defineMethod (c$, "stringParameterSet", 
function (i) {
switch (this.tokAt (i)) {
case 4:
var s = this.stringParameter (i);
if (s.startsWith ("[\"")) {
var o = this.vwr.evaluateExpression (s);
if (Clazz.instanceOf (o, String)) return JU.PT.split (o, "\n");
}return [s];
case 1073742195:
i += 2;
break;
case 269484096:
++i;
break;
case 7:
return JS.SV.strListValue (this.getToken (i));
default:
this.invArg ();
}
var tok;
var v =  new JU.Lst ();
while ((tok = this.tokAt (i)) != 269484097) {
switch (tok) {
case 269484080:
break;
case 4:
v.addLast (this.stringParameter (i));
break;
default:
case 0:
this.invArg ();
}
i++;
}
this.iToken = i;
var n = v.size ();
var sParams =  new Array (n);
for (var j = 0; j < n; j++) {
sParams[j] = v.get (j);
}
return sParams;
}, "~N");
Clazz.defineMethod (c$, "objectNameParameter", 
function (index) {
if (!this.checkToken (index)) this.error (37);
return this.paramAsStr (index);
}, "~N");
Clazz.defineMethod (c$, "atomCenterOrCoordinateParameter", 
function (i) {
switch (this.getToken (i).tok) {
case 10:
case 1048577:
var bs = this.atomExpression (this.st, i, 0, true, false, false, true);
if (bs != null && bs.cardinality () == 1) return this.vwr.getAtomPoint3f (bs.nextSetBit (0));
if (bs != null) return this.vwr.ms.getAtomSetCenter (bs);
if (Clazz.instanceOf (this.expressionResult, JU.P3)) return this.expressionResult;
this.invArg ();
break;
case 1048586:
case 8:
return this.getPoint3f (i, true);
}
this.invArg ();
return null;
}, "~N");
Clazz.defineMethod (c$, "isCenterParameter", 
function (i) {
var tok = this.tokAt (i);
return (tok == 1048582 || tok == 1048586 || tok == 1048577 || tok == 8 || tok == 10);
}, "~N");
Clazz.defineMethod (c$, "centerParameter", 
function (i) {
return this.centerParameterForModel (i, -2147483648);
}, "~N");
Clazz.defineMethod (c$, "centerParameterForModel", 
function (i, modelIndex) {
var center = null;
this.expressionResult = null;
if (this.checkToken (i)) {
switch (this.getToken (i).tok) {
case 1048582:
var id = this.objectNameParameter (++i);
var index = -2147483648;
if (this.tokAt (i + 1) == 269484096) {
index = this.parameterExpressionList (-i - 1, -1, true).get (0).asInt ();
if (this.getToken (--this.iToken).tok != 269484097) this.invArg ();
}if (this.chk) return  new JU.P3 ();
if (this.tokAt (i + 1) == 1048583 && (this.tokAt (i + 2) == 1141899267 || this.tokAt (i + 2) == 1141899270)) {
index = 2147483647;
this.iToken = i + 2;
}if ((center = this.getObjectCenter (id, index, modelIndex)) == null) this.errorStr (12, id);
break;
case 10:
case 1048577:
case 1048586:
case 8:
center = this.atomCenterOrCoordinateParameter (i);
break;
}
}if (center == null) this.error (11);
return center;
}, "~N,~N");
Clazz.defineMethod (c$, "planeParameter", 
function (i) {
var vAB =  new JU.V3 ();
var vAC =  new JU.V3 ();
var plane = null;
if (this.tokAt (i) == 135266319) i++;
var isNegated = (this.tokAt (i) == 269484192);
if (isNegated) i++;
if (i < this.slen) switch (this.getToken (i).tok) {
case 9:
plane = JU.P4.newPt (this.theToken.value);
break;
case 1048582:
var id = this.objectNameParameter (++i);
if (this.chk) return  new JU.P4 ();
plane = this.getPlaneForObject (id, vAB, vAC);
break;
case 1112541205:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("x=?", null);
plane = JU.P4.new4 (1, 0, 0, -this.floatParameter (i));
break;
case 1112541206:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("y=?", null);
plane = JU.P4.new4 (0, 1, 0, -this.floatParameter (i));
break;
case 1112541207:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("z=?", null);
plane = JU.P4.new4 (0, 0, 1, -this.floatParameter (i));
break;
case 1073741824:
case 4:
var str = this.paramAsStr (i);
if (str.equalsIgnoreCase ("xy")) return JU.P4.new4 (0, 0, 1, 0);
if (str.equalsIgnoreCase ("xz")) return JU.P4.new4 (0, 1, 0, 0);
if (str.equalsIgnoreCase ("yz")) return JU.P4.new4 (1, 0, 0, 0);
this.iToken += 2;
break;
case 1048586:
case 8:
if (!this.isPoint3f (i)) {
plane = this.getPoint4f (i);
break;
}case 10:
case 1048577:
var pt1 = this.atomCenterOrCoordinateParameter (i);
if (this.getToken (++this.iToken).tok == 269484080) ++this.iToken;
var pt2 = this.atomCenterOrCoordinateParameter (this.iToken);
if (this.getToken (++this.iToken).tok == 269484080) ++this.iToken;
var pt3 = this.atomCenterOrCoordinateParameter (this.iToken);
i = this.iToken;
var norm =  new JU.V3 ();
var w = JU.Measure.getNormalThroughPoints (pt1, pt2, pt3, norm, vAB, vAC);
plane =  new JU.P4 ();
plane.set4 (norm.x, norm.y, norm.z, w);
if (!this.chk && JU.Logger.debugging) JU.Logger.debug ("points: " + pt1 + pt2 + pt3 + " defined plane: " + plane);
break;
}
if (plane == null) this.errorMore (38, "{a b c d}", "\"xy\" \"xz\" \"yz\" \"x=...\" \"y=...\" \"z=...\"", "$xxxxx");
if (isNegated) {
plane.scale4 (-1);
}return plane;
}, "~N");
Clazz.defineMethod (c$, "hklParameter", 
function (i) {
if (!this.chk && this.vwr.getCurrentUnitCell () == null) this.error (33);
var pt = this.getPointOrPlane (i, false, true, false, true, 3, 3);
var p = this.getHklPlane (pt);
if (p == null) this.error (3);
if (!this.chk && JU.Logger.debugging) JU.Logger.debug ("defined plane: " + p);
return p;
}, "~N");
Clazz.defineMethod (c$, "getHklPlane", 
function (pt) {
var vAB =  new JU.V3 ();
var vAC =  new JU.V3 ();
var pt1 = JU.P3.new3 (pt.x == 0 ? 1 : 1 / pt.x, 0, 0);
var pt2 = JU.P3.new3 (0, pt.y == 0 ? 1 : 1 / pt.y, 0);
var pt3 = JU.P3.new3 (0, 0, pt.z == 0 ? 1 : 1 / pt.z);
if (pt.x == 0 && pt.y == 0 && pt.z == 0) {
return null;
} else if (pt.x == 0 && pt.y == 0) {
pt1.set (1, 0, pt3.z);
pt2.set (0, 1, pt3.z);
} else if (pt.y == 0 && pt.z == 0) {
pt2.set (pt1.x, 0, 1);
pt3.set (pt1.x, 1, 0);
} else if (pt.z == 0 && pt.x == 0) {
pt3.set (0, pt2.y, 1);
pt1.set (1, pt2.y, 0);
} else if (pt.x == 0) {
pt1.set (1, pt2.y, 0);
} else if (pt.y == 0) {
pt2.set (0, 1, pt3.z);
} else if (pt.z == 0) {
pt3.set (pt1.x, 0, 1);
}this.vwr.toCartesian (pt1, false);
this.vwr.toCartesian (pt2, false);
this.vwr.toCartesian (pt3, false);
var plane =  new JU.V3 ();
var w = JU.Measure.getNormalThroughPoints (pt1, pt2, pt3, plane, vAB, vAC);
var pt4 =  new JU.P4 ();
pt4.set4 (plane.x, plane.y, plane.z, w);
return pt4;
}, "JU.P3");
Clazz.defineMethod (c$, "getPointOrPlane", 
function (index, integerOnly, allowFractional, doConvert, implicitFractional, minDim, maxDim) {
var coord =  Clazz.newFloatArray (6, 0);
var n = 0;
this.coordinatesAreFractional = implicitFractional;
if (this.tokAt (index) == 8) {
if (minDim <= 3 && maxDim >= 3) return this.getToken (index).value;
this.invArg ();
}if (this.tokAt (index) == 9) {
if (minDim <= 4 && maxDim >= 4) return this.getToken (index).value;
this.invArg ();
}var multiplier = 1;
out : for (var i = index; i < this.st.length; i++) {
switch (this.getToken (i).tok) {
case 1048586:
case 269484080:
case 269484128:
case 269484160:
break;
case 1048590:
break out;
case 269484192:
multiplier = -1;
break;
case 1048615:
if (n == 6) this.invArg ();
coord[n++] = this.theToken.intValue;
multiplier = -1;
break;
case 2:
case 1048614:
if (n == 6) this.invArg ();
coord[n++] = this.theToken.intValue * multiplier;
multiplier = 1;
break;
case 269484208:
case 1048610:
if (!allowFractional) this.invArg ();
if (this.theTok == 269484208) this.getToken (++i);
n--;
if (n < 0 || integerOnly) this.invArg ();
if (Clazz.instanceOf (this.theToken.value, Integer) || this.theTok == 2) {
coord[n++] /= (this.theToken.intValue == 2147483647 ? (this.theToken.value).intValue () : this.theToken.intValue);
} else if (Clazz.instanceOf (this.theToken.value, Float)) {
coord[n++] /= (this.theToken.value).floatValue ();
}this.coordinatesAreFractional = true;
break;
case 1048609:
case 1073741824:
coord[n++] = NaN;
break;
case 3:
case 1048611:
if (integerOnly) this.invArg ();
if (n == 6) this.invArg ();
coord[n++] = (this.theToken.value).floatValue ();
break;
default:
this.invArg ();
}
}
if (n < minDim || n > maxDim) this.invArg ();
if (n == 3) {
var pt = JU.P3.new3 (coord[0], coord[1], coord[2]);
if (this.coordinatesAreFractional && doConvert) {
this.fractionalPoint = JU.P3.newP (pt);
if (!this.chk) this.vwr.toCartesian (pt, false);
}return pt;
}if (n == 4) {
if (this.coordinatesAreFractional) this.invArg ();
var plane = JU.P4.new4 (coord[0], coord[1], coord[2], coord[3]);
return plane;
}return coord;
}, "~N,~B,~B,~B,~B,~N,~N");
Clazz.defineMethod (c$, "isPoint3f", 
function (i) {
var isOK;
if ((isOK = (this.tokAt (i) == 8)) || this.tokAt (i) == 9 || this.isFloatParameter (i + 1) && this.isFloatParameter (i + 2) && this.isFloatParameter (i + 3) && this.isFloatParameter (i + 4)) return isOK;
this.ignoreError = true;
var t = this.iToken;
isOK = true;
try {
this.getPoint3f (i, true);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
isOK = false;
} else {
throw e;
}
}
this.ignoreError = false;
this.iToken = t;
return isOK;
}, "~N");
Clazz.defineMethod (c$, "getPoint3f", 
function (i, allowFractional) {
return this.getPointOrPlane (i, false, allowFractional, true, false, 3, 3);
}, "~N,~B");
Clazz.defineMethod (c$, "getPoint4f", 
function (i) {
return this.getPointOrPlane (i, false, false, false, false, 4, 4);
}, "~N");
Clazz.defineMethod (c$, "xypParameter", 
function (index) {
var tok = this.tokAt (index);
if (tok == 1073742195) tok = this.tokAt (++index);
if (tok != 269484096 || !this.isFloatParameter (++index)) return null;
var pt =  new JU.P3 ();
pt.x = this.floatParameter (index);
if (this.tokAt (++index) == 269484080) index++;
if (!this.isFloatParameter (index)) return null;
pt.y = this.floatParameter (index);
var isPercent = (this.tokAt (++index) == 269484210);
if (isPercent) ++index;
if (this.tokAt (index) != 269484097) return null;
this.iToken = index;
pt.z = (isPercent ? -1 : 1) * 3.4028235E38;
return pt;
}, "~N");
Clazz.defineMethod (c$, "optParameterAsString", 
function (i) {
if (i >= this.slen) return "";
return this.paramAsStr (i);
}, "~N");
Clazz.defineMethod (c$, "intParameter", 
function (index) {
if (this.checkToken (index)) if (this.getToken (index).tok == 2) return this.theToken.intValue;
this.error (20);
return 0;
}, "~N");
Clazz.defineMethod (c$, "isFloatParameter", 
function (index) {
switch (this.tokAt (index)) {
case 2:
case 3:
return true;
}
return false;
}, "~N");
Clazz.defineMethod (c$, "floatParameter", 
function (index) {
if (this.checkToken (index)) {
this.getToken (index);
switch (this.theTok) {
case 1048615:
return -this.theToken.intValue;
case 1048614:
case 2:
return this.theToken.intValue;
case 1048611:
case 3:
return (this.theToken.value).floatValue ();
}
}this.error (34);
return 0;
}, "~N");
Clazz.defineMethod (c$, "getPointArray", 
function (i, nPoints) {
var points = (nPoints < 0 ? null :  new Array (nPoints));
var vp = (nPoints < 0 ?  new JU.Lst () : null);
var tok = (i < 0 ? 7 : this.getToken (i++).tok);
switch (tok) {
case 7:
var v = (this.theToken).getList ();
if (nPoints >= 0 && v.size () != nPoints) this.invArg ();
nPoints = v.size ();
if (points == null) points =  new Array (nPoints);
for (var j = 0; j < nPoints; j++) if ((points[j] = JS.SV.ptValue (v.get (j))) == null) this.invArg ();

return points;
case 1073742195:
tok = this.tokAt (i++);
break;
}
if (tok != 269484096) this.invArg ();
var n = 0;
while (tok != 269484097 && tok != 0) {
tok = this.getToken (i).tok;
switch (tok) {
case 0:
case 269484097:
break;
case 269484080:
i++;
break;
default:
if (nPoints >= 0 && n == nPoints) {
tok = 0;
break;
}var pt = this.centerParameter (i);
if (points == null) vp.addLast (pt);
 else points[n] = pt;
n++;
i = this.iToken + 1;
}
}
if (tok != 269484097) this.invArg ();
if (points == null) points = vp.toArray ( new Array (vp.size ()));
if (nPoints > 0 && points[nPoints - 1] == null) this.invArg ();
return points;
}, "~N,~N");
Clazz.defineMethod (c$, "listParameter", 
function (i, nMin, nMax) {
var v =  new JU.Lst ();
var pt;
var tok = this.tokAt (i);
if (tok == 1073742195) tok = this.tokAt (++i);
var haveBrace = (tok == 1048586);
var haveSquare = (tok == 269484096);
if (haveBrace || haveSquare) i++;
var n = 0;
while (n < nMax) {
tok = this.tokAt (i);
if (haveBrace && tok == 1048590 || haveSquare && tok == 269484097) break;
switch (tok) {
case 269484080:
case 1048586:
case 1048590:
break;
case 4:
break;
case 8:
pt = this.getPoint3f (i, false);
v.addLast (Float.$valueOf (pt.x));
v.addLast (Float.$valueOf (pt.y));
v.addLast (Float.$valueOf (pt.z));
n += 3;
break;
case 9:
var pt4 = this.getPoint4f (i);
v.addLast (Float.$valueOf (pt4.x));
v.addLast (Float.$valueOf (pt4.y));
v.addLast (Float.$valueOf (pt4.z));
v.addLast (Float.$valueOf (pt4.w));
n += 4;
break;
default:
v.addLast (Float.$valueOf (this.floatParameter (i)));
n++;
if (n == nMax && haveSquare && this.tokAt (i + 1) == 1048590) i++;
}
i++;
}
if (haveBrace && this.tokAt (i++) != 1048590 || haveSquare && this.tokAt (i++) != 269484097 || n < nMin || n > nMax) this.invArg ();
this.iToken = i - 1;
return v;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "floatParameterSet", 
function (i, nMin, nMax) {
var v = null;
var fparams = null;
var n = 0;
var s = null;
this.iToken = i;
switch (this.tokAt (i)) {
case 4:
s = JS.SV.sValue (this.st[i]);
s = JU.PT.replaceWithCharacter (s, "{},[]\"'", ' ');
fparams = JU.PT.parseFloatArray (s);
n = fparams.length;
break;
case 7:
fparams = JS.SV.flistValue (this.st[i], 0);
n = fparams.length;
break;
default:
v = this.listParameter (i, nMin, nMax);
n = v.size ();
}
if (n < nMin || n > nMax) this.invArg ();
if (fparams == null) {
fparams =  Clazz.newFloatArray (n, 0);
for (var j = 0; j < n; j++) fparams[j] = (v.get (j)).floatValue ();

}return fparams;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "isArrayParameter", 
function (i) {
switch (this.tokAt (i)) {
case 7:
case 11:
case 12:
case 1073742195:
case 269484096:
return true;
}
return false;
}, "~N");
Clazz.defineMethod (c$, "getQuaternionParameter", 
function (i) {
switch (this.tokAt (i)) {
case 7:
var sv = (this.getToken (i)).getList ();
var p4 = null;
if (sv.size () == 0 || (p4 = JS.SV.pt4Value (sv.get (0))) == null) this.invArg ();
return JU.Quat.newP4 (p4);
case 1073741863:
return (this.chk ? null : JU.Quat.newP4 (JU.Escape.uP (this.vwr.getOrientationText (1073741863, null))));
default:
return JU.Quat.newP4 (this.getPoint4f (i));
}
}, "~N");
Clazz.defineMethod (c$, "checkLast", 
function (i) {
return this.checkLength (i + 1) - 1;
}, "~N");
Clazz.defineMethod (c$, "checkLength", 
function (length) {
if (length >= 0) return this.checkLengthErrorPt (length, 0);
if (this.slen > -length) {
this.iToken = -length;
this.bad ();
}return this.slen;
}, "~N");
Clazz.defineMethod (c$, "checkLengthErrorPt", 
function (length, errorPt) {
if (this.slen != length) {
this.iToken = errorPt > 0 ? errorPt : this.slen;
if (errorPt > 0) this.invArg ();
 else this.bad ();
}return this.slen;
}, "~N,~N");
Clazz.defineMethod (c$, "checkLength23", 
function () {
this.iToken = this.slen;
if (this.slen != 2 && this.slen != 3) this.bad ();
return this.slen;
});
Clazz.defineMethod (c$, "checkLength34", 
function () {
this.iToken = this.slen;
if (this.slen != 3 && this.slen != 4) this.bad ();
return this.slen;
});
Clazz.defineMethod (c$, "modelNumberParameter", 
function (index) {
var iFrame = 0;
var useModelNumber = false;
switch (this.tokAt (index)) {
case 2:
useModelNumber = true;
case 3:
iFrame = this.getToken (index).intValue;
break;
case 4:
iFrame = JS.ScriptParam.getFloatEncodedInt (this.stringParameter (index));
break;
default:
this.invArg ();
}
return this.vwr.ms.getModelNumberIndex (iFrame, useModelNumber, true);
}, "~N");
Clazz.defineMethod (c$, "getMadParameter", 
function () {
var mad = 1;
switch (this.getToken (1).tok) {
case 1073742072:
this.restrictSelected (false, false);
break;
case 1048589:
break;
case 1048588:
mad = 0;
break;
case 2:
var radiusRasMol = this.intParameterRange (1, 0, 750);
mad = radiusRasMol * 4 * 2;
break;
case 3:
var f = this.floatParameterRange (1, -3, 3);
mad = (Float.isNaN (f) ? 2147483647 : Clazz.doubleToInt (Math.floor (f * 1000 * 2)));
if (mad < 0) {
this.restrictSelected (false, false);
mad = -mad;
}break;
default:
this.error (6);
}
return mad;
});
Clazz.defineMethod (c$, "intParameterRange", 
function (i, min, max) {
var val = this.intParameter (i);
if (val < min || val > max) {
this.integerOutOfRange (min, max);
return 2147483647;
}return val;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "floatParameterRange", 
function (i, min, max) {
var val = this.floatParameter (i);
if (val < min || val > max) {
this.numberOutOfRange (min, max);
return NaN;
}return val;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "getPointVector", 
function (t, i) {
switch (t.tok) {
case 10:
return this.vwr.ms.getAtomPointVector (t.value);
case 7:
var data =  new JU.Lst ();
var pt;
var pts = (t).getList ();
for (var j = 0; j < pts.size (); j++) if ((pt = JS.SV.ptValue (pts.get (j))) != null) data.addLast (pt);
 else return null;

return data;
}
if (i > 0) return this.vwr.ms.getAtomPointVector (this.atomExpressionAt (i));
return null;
}, "JS.T,~N");
c$.getFloatEncodedInt = Clazz.defineMethod (c$, "getFloatEncodedInt", 
function (strDecimal) {
var pt = strDecimal.indexOf (".");
if (pt < 1 || strDecimal.charAt (0) == '-' || strDecimal.endsWith (".") || strDecimal.contains (".0")) return 2147483647;
var i = 0;
var j = 0;
if (pt > 0) {
try {
i = Integer.parseInt (strDecimal.substring (0, pt));
if (i < 0) i = -i;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
i = -1;
} else {
throw e;
}
}
}if (pt < strDecimal.length - 1) try {
j = Integer.parseInt (strDecimal.substring (pt + 1));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
} else {
throw e;
}
}
i = i * 1000000 + j;
return (i < 0 ? 2147483647 : i);
}, "~S");
c$.getPartialBondOrderFromFloatEncodedInt = Clazz.defineMethod (c$, "getPartialBondOrderFromFloatEncodedInt", 
function (bondOrderInteger) {
return (((Clazz.doubleToInt (bondOrderInteger / 1000000)) % 6) << 5) + ((bondOrderInteger % 1000000) & 0x1F);
}, "~N");
c$.getBondOrderFromString = Clazz.defineMethod (c$, "getBondOrderFromString", 
function (s) {
return (s.indexOf (' ') < 0 ? JU.Edge.getBondOrderFromString (s) : s.toLowerCase ().indexOf ("partial ") == 0 ? JS.ScriptParam.getPartialBondOrderFromString (s.substring (8).trim ()) : 131071);
}, "~S");
c$.getPartialBondOrderFromString = Clazz.defineMethod (c$, "getPartialBondOrderFromString", 
 function (s) {
return JS.ScriptParam.getPartialBondOrderFromFloatEncodedInt (JS.ScriptParam.getFloatEncodedInt (s));
}, "~S");
Clazz.defineMethod (c$, "isColorParam", 
function (i) {
var tok = this.tokAt (i);
return (tok == 570425378 || tok == 1073742195 || tok == 269484096 || tok == 7 || tok == 8 || this.isPoint3f (i) || (tok == 4 || JS.T.tokAttr (tok, 1073741824)) && JU.CU.getArgbFromString (this.st[i].value) != 0);
}, "~N");
Clazz.defineMethod (c$, "getArgbParam", 
function (index) {
return this.getArgbParamOrNone (index, false);
}, "~N");
Clazz.defineMethod (c$, "getArgbParamLast", 
function (index, allowNone) {
var icolor = this.getArgbParamOrNone (index, allowNone);
this.checkLast (this.iToken);
return icolor;
}, "~N,~B");
Clazz.defineMethod (c$, "getArgbParamOrNone", 
function (index, allowNone) {
var pt = null;
if (this.checkToken (index)) {
switch (this.getToken (index).tok) {
default:
if (!JS.T.tokAttr (this.theTok, 1073741824)) break;
case 570425378:
case 4:
return JU.CU.getArgbFromString (this.paramAsStr (index));
case 1073742195:
return this.getColorTriad (index + 2);
case 269484096:
return this.getColorTriad (++index);
case 7:
var rgb = JS.SV.flistValue (this.theToken, 3);
if (rgb != null && rgb.length != 3) pt = JU.P3.new3 (rgb[0], rgb[1], rgb[2]);
break;
case 8:
pt = this.theToken.value;
break;
case 1048586:
pt = this.getPoint3f (index, false);
break;
case 1048587:
if (allowNone) return 0;
}
}if (pt == null) this.error (8);
return JU.CU.colorPtToFFRGB (pt);
}, "~N,~B");
Clazz.defineMethod (c$, "getColorTriad", 
 function (i) {
var colors =  Clazz.newFloatArray (3, 0);
var n = 0;
var hex = "";
this.getToken (i);
var pt = null;
var val = 0;
out : switch (this.theTok) {
case 2:
case 1048614:
case 3:
for (; i < this.slen; i++) {
switch (this.getToken (i).tok) {
case 269484080:
continue;
case 1073741824:
if (n != 1 || colors[0] != 0) this.error (4);
hex = "0" + this.paramAsStr (i);
break out;
case 3:
if (n > 2) this.error (4);
val = this.floatParameter (i);
break;
case 2:
if (n > 2) this.error (4);
val = this.theToken.intValue;
break;
case 1048614:
if (n > 2) this.error (4);
val = (this.theToken.value).intValue () % 256;
break;
case 269484097:
if (n != 3) this.error (4);
--i;
pt = JU.P3.new3 (colors[0], colors[1], colors[2]);
break out;
default:
this.error (4);
}
colors[n++] = val;
}
this.error (4);
break;
case 8:
pt = this.theToken.value;
break;
case 1073741824:
hex = this.paramAsStr (i);
break;
default:
this.error (4);
}
if (this.getToken (++i).tok != 269484097) this.error (4);
if (pt != null) return JU.CU.colorPtToFFRGB (pt);
if ((n = JU.CU.getArgbFromString ("[" + hex + "]")) == 0) this.error (4);
return n;
}, "~N");
Clazz.defineMethod (c$, "tickParamAsStr", 
function (index, allowUnitCell, allowScale, allowFirst) {
this.iToken = index - 1;
if (this.tokAt (index) != 1073742164) return null;
var tickInfo;
var str = " ";
switch (this.tokAt (index + 1)) {
case 1112541205:
case 1112541206:
case 1112541207:
str = this.paramAsStr (++index).toLowerCase ();
break;
case 1073741824:
this.invArg ();
}
if (this.tokAt (++index) == 1048587) {
tickInfo =  new JM.TickInfo (null);
tickInfo.type = str;
this.iToken = index;
return tickInfo;
}tickInfo =  new JM.TickInfo (this.getPointOrPlane (index, false, true, false, false, 3, 3));
if (this.coordinatesAreFractional || this.tokAt (this.iToken + 1) == 1614417948) {
tickInfo.scale = JU.P3.new3 (NaN, NaN, NaN);
allowScale = false;
}if (this.tokAt (this.iToken + 1) == 1614417948) this.iToken++;
tickInfo.type = str;
if (this.tokAt (this.iToken + 1) == 1288701959) tickInfo.tickLabelFormats = this.stringParameterSet (this.iToken + 2);
if (!allowScale) return tickInfo;
if (this.tokAt (this.iToken + 1) == 1073742138) {
if (this.isFloatParameter (this.iToken + 2)) {
var f = this.floatParameter (this.iToken + 2);
tickInfo.scale = JU.P3.new3 (f, f, f);
} else {
tickInfo.scale = this.getPoint3f (this.iToken + 2, true);
}}if (allowFirst) if (this.tokAt (this.iToken + 1) == 1073741942) tickInfo.first = this.floatParameter (this.iToken + 2);
return tickInfo;
}, "~N,~B,~B,~B");
Clazz.defineMethod (c$, "setBooleanProperty", 
function (key, value) {
if (!this.chk) this.vwr.setBooleanProperty (key, value);
}, "~S,~B");
Clazz.defineMethod (c$, "setIntProperty", 
function (key, value) {
if (!this.chk) this.vwr.setIntProperty (key, value);
return true;
}, "~S,~N");
Clazz.defineMethod (c$, "setFloatProperty", 
function (key, value) {
if (!this.chk) this.vwr.setFloatProperty (key, value);
return true;
}, "~S,~N");
Clazz.defineMethod (c$, "setStringProperty", 
function (key, value) {
if (!this.chk) this.vwr.setStringProperty (key, value);
}, "~S,~S");
});
