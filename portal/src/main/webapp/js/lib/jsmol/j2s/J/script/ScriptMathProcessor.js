Clazz.declarePackage ("J.script");
Clazz.load (null, "J.script.ScriptMathProcessor", ["java.lang.Float", "java.util.Arrays", "$.Date", "$.Hashtable", "java.util.regex.Pattern", "J.atomdata.RadiusData", "J.constant.EnumVdw", "J.modelset.Bond", "$.MeasurementData", "J.script.SV", "$.ScriptEvaluator", "$.T", "J.util.ArrayUtil", "$.AxisAngle4f", "$.BS", "$.BSUtil", "$.ColorEncoder", "$.ColorUtil", "$.Escape", "$.JmolList", "$.JmolMolecule", "$.Logger", "$.Matrix3f", "$.Matrix4f", "$.Measure", "$.P3", "$.P4", "$.Parser", "$.Point3fi", "$.Quaternion", "$.SB", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.chk = false;
this.wasSyntaxCheck = false;
this.logMessages = false;
this.eval = null;
this.viewer = null;
this.oStack = null;
this.xStack = null;
this.ifStack = null;
this.ifPt = -1;
this.oPt = -1;
this.xPt = -1;
this.parenCount = 0;
this.squareCount = 0;
this.braceCount = 0;
this.wasX = false;
this.incrementX = 0;
this.isArrayItem = false;
this.asVector = false;
this.asBitSet = false;
this.ptid = 0;
this.ptx = 2147483647;
this.skipping = false;
this.haveSpaceBeforeSquare = false;
this.equalCount = 0;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptMathProcessor");
Clazz.prepareFields (c$, function () {
this.oStack =  new Array (8);
this.xStack =  new Array (8);
this.ifStack =  Clazz.newCharArray (8, '\0');
});
Clazz.makeConstructor (c$, 
function (eval, isArrayItem, asVector, asBitSet) {
this.eval = eval;
this.viewer = eval.viewer;
this.logMessages = eval.logMessages;
this.chk = this.wasSyntaxCheck = eval.chk;
this.isArrayItem = isArrayItem;
this.asVector = asVector || isArrayItem;
this.asBitSet = asBitSet;
this.wasX = isArrayItem;
if (this.logMessages) J.util.Logger.debug ("initialize RPN");
}, "J.script.ScriptEvaluator,~B,~B,~B");
$_M(c$, "getResult", 
function (allowUnderflow) {
var isOK = true;
while (isOK && this.oPt >= 0) isOK = this.operate ();

if (isOK) {
if (this.asVector) {
var result =  new J.util.JmolList ();
for (var i = 0; i <= this.xPt; i++) result.addLast (J.script.SV.selectItemVar (this.xStack[i]));

return J.script.SV.newVariable (135198, result);
}if (this.xPt == 0) {
var x = this.xStack[0];
if (x.tok == 10 || x.tok == 7 || x.tok == 4 || x.tok == 11 || x.tok == 12) x = J.script.SV.selectItemVar (x);
if (this.asBitSet && x.tok == 7) x = J.script.SV.newVariable (10, J.script.SV.unEscapeBitSetArray (x.value, false));
return x;
}}if (!allowUnderflow && (this.xPt >= 0 || this.oPt >= 0)) {
this.eval.error (22);
}return null;
}, "~B");
$_M(c$, "putX", 
($fz = function (x) {
if (this.skipping) return;
if (++this.xPt == this.xStack.length) this.xStack = J.util.ArrayUtil.doubleLength (this.xStack);
if (this.logMessages) {
J.util.Logger.debug ("\nputX: " + x);
}this.xStack[this.xPt] = x;
this.ptx = ++this.ptid;
}, $fz.isPrivate = true, $fz), "J.script.SV");
$_M(c$, "putOp", 
($fz = function (op) {
if (++this.oPt >= this.oStack.length) this.oStack = J.util.ArrayUtil.doubleLength (this.oStack);
this.oStack[this.oPt] = op;
this.ptid++;
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "putIf", 
($fz = function (c) {
if (++this.ifPt >= this.ifStack.length) this.ifStack = J.util.ArrayUtil.doubleLength (this.ifStack);
this.ifStack[this.ifPt] = c;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "addXVar", 
function (x) {
this.putX (x);
return this.wasX = true;
}, "J.script.SV");
$_M(c$, "addXObj", 
function (x) {
var v = J.script.SV.getVariable (x);
if (v == null) return false;
this.putX (v);
return this.wasX = true;
}, "~O");
$_M(c$, "addXStr", 
function (x) {
this.putX (J.script.SV.newVariable (4, x));
return this.wasX = true;
}, "~S");
$_M(c$, "addXBool", 
($fz = function (x) {
this.putX (J.script.SV.getBoolean (x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "addXInt", 
($fz = function (x) {
this.putX (J.script.SV.newScriptVariableInt (x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "addXList", 
($fz = function (x) {
this.putX (J.script.SV.getVariableList (x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "addXMap", 
($fz = function (x) {
this.putX (J.script.SV.getVariableMap (x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "addXM3", 
($fz = function (x) {
this.putX (J.script.SV.newVariable (11, x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "J.util.Matrix3f");
$_M(c$, "addXM4", 
($fz = function (x) {
this.putX (J.script.SV.newVariable (12, x));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "J.util.Matrix4f");
$_M(c$, "addXFloat", 
($fz = function (x) {
if (Float.isNaN (x)) return this.addXStr ("NaN");
this.putX (J.script.SV.newVariable (3, Float.$valueOf (x)));
return this.wasX = true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "addXBs", 
function (bs) {
this.putX (J.script.SV.newVariable (10, bs));
return this.wasX = true;
}, "J.util.BS");
$_M(c$, "addXPt", 
function (pt) {
this.putX (J.script.SV.newVariable (8, pt));
return this.wasX = true;
}, "J.util.P3");
$_M(c$, "addXPt4", 
function (pt) {
this.putX (J.script.SV.newVariable (9, pt));
return this.wasX = true;
}, "J.util.P4");
$_M(c$, "addXNum", 
function (x) {
if (this.wasX) switch (x.tok) {
case 2:
if (x.intValue < 0) {
this.addOp (J.script.T.tokenMinus);
x = J.script.SV.newScriptVariableInt (-x.intValue);
}break;
case 3:
var f = (x.value).floatValue ();
if (f < 0 || f == 0 && 1 / f == -Infinity) {
this.addOp (J.script.T.tokenMinus);
x = J.script.SV.newVariable (3, Float.$valueOf (-f));
}break;
}
this.putX (x);
return this.wasX = true;
}, "J.script.SV");
$_M(c$, "addXAV", 
function (x) {
this.putX (J.script.SV.getVariableAV (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAD", 
function (x) {
this.putX (J.script.SV.getVariableAD (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAS", 
function (x) {
this.putX (J.script.SV.getVariableAS (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAI", 
function (x) {
this.putX (J.script.SV.getVariableAI (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAII", 
function (x) {
this.putX (J.script.SV.getVariableAII (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAF", 
function (x) {
this.putX (J.script.SV.getVariableAF (x));
return this.wasX = true;
}, "~A");
$_M(c$, "addXAFF", 
function (x) {
this.putX (J.script.SV.getVariableAFF (x));
return this.wasX = true;
}, "~A");
c$.isOpFunc = $_M(c$, "isOpFunc", 
($fz = function (op) {
return (J.script.T.tokAttr (op.tok, 135266304) && op !== J.script.T.tokenArraySquare || op.tok == 269484241 && J.script.T.tokAttr (op.intValue, 135266304));
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "addOp", 
function (op) {
return this.addOpAllowMath (op, true);
}, "J.script.T");
$_M(c$, "addOpAllowMath", 
function (op, allowMathFunc) {
if (this.logMessages) {
J.util.Logger.debug ("addOp entry\naddOp: " + op);
}var tok0 = (this.oPt >= 0 ? this.oStack[this.oPt].tok : 0);
this.skipping = (this.ifPt >= 0 && (this.ifStack[this.ifPt] == 'F' || this.ifStack[this.ifPt] == 'X'));
if (this.skipping) {
switch (op.tok) {
case 269484048:
this.putOp (op);
return true;
case 269484066:
if (tok0 != 269484066 || this.ifStack[this.ifPt] == 'X') return true;
this.ifStack[this.ifPt] = 'T';
this.wasX = false;
this.skipping = false;
return true;
case 269484049:
if (tok0 == 269484048) {
this.oPt--;
return true;
}if (tok0 != 269484066) {
this.putOp (op);
return true;
}this.wasX = true;
this.ifPt--;
this.oPt -= 2;
this.skipping = false;
return true;
default:
return true;
}
}var newOp = null;
var tok;
var isLeftOp = false;
var isDotSelector = (op.tok == 269484241);
if (isDotSelector && !this.wasX) return false;
var isMathFunc = (allowMathFunc && J.script.ScriptMathProcessor.isOpFunc (op));
if (this.oPt >= 1 && op.tok != 269484048 && tok0 == 135266319) tok0 = this.oStack[--this.oPt].tok;
var isArgument = (this.oPt >= 1 && tok0 == 269484048);
switch (op.tok) {
case 1073742195:
this.haveSpaceBeforeSquare = true;
return true;
case 269484080:
if (!this.wasX) return false;
break;
case 32:
case 64:
case 96:
case 128:
case 160:
case 192:
case 480:
tok = (this.oPt < 0 ? 0 : tok0);
if (!this.wasX || !(tok == 269484241 || tok == 1678770178 || tok == 1141899265)) return false;
this.oStack[this.oPt].intValue |= op.tok;
return true;
case 269484096:
isLeftOp = true;
if (!this.wasX || this.haveSpaceBeforeSquare) {
this.squareCount++;
op = newOp = J.script.T.tokenArraySquare;
this.haveSpaceBeforeSquare = false;
}break;
case 269484097:
break;
case 269484225:
case 269484226:
this.incrementX = (op.tok == 269484226 ? 1 : -1);
if (this.ptid == this.ptx) {
if (this.chk) return true;
var x = this.xStack[this.xPt];
this.xStack[this.xPt] = J.script.SV.newVariable (4, "").setv (x, false);
return x.increment (this.incrementX);
}break;
case 269484192:
if (this.wasX) break;
this.addXInt (0);
op = J.script.SV.newVariable (269484224, "-");
break;
case 269484049:
if (!this.wasX && this.oPt >= 1 && tok0 == 269484048 && !J.script.ScriptMathProcessor.isOpFunc (this.oStack[this.oPt - 1])) return false;
break;
case 269484144:
case 269484048:
isLeftOp = true;
default:
if (isMathFunc) {
if (!isDotSelector && this.wasX && !isArgument) return false;
newOp = op;
isLeftOp = true;
break;
}if (this.wasX == isLeftOp && tok0 != 269484241) return false;
break;
}
while (this.oPt >= 0 && tok0 != 269484066 && (!isLeftOp || tok0 == 269484241 && (op.tok == 269484241 || op.tok == 269484096)) && J.script.T.getPrecedence (tok0) >= J.script.T.getPrecedence (op.tok)) {
if (this.logMessages) {
J.util.Logger.debug ("\noperating, oPt=" + this.oPt + " isLeftOp=" + isLeftOp + " oStack[oPt]=" + J.script.T.nameOf (tok0) + "        prec=" + J.script.T.getPrecedence (tok0) + " pending op=\"" + J.script.T.nameOf (op.tok) + "\" prec=" + J.script.T.getPrecedence (op.tok));
this.dumpStacks ("operating");
}if (op.tok == 269484049 && tok0 == 269484048) {
if (this.xPt >= 0) this.xStack[this.xPt] = J.script.SV.selectItemVar (this.xStack[this.xPt]);
break;
}if (op.tok == 269484097 && tok0 == 135266306) {
break;
}if (op.tok == 269484097 && tok0 == 269484096) {
if (this.isArrayItem && this.squareCount == 1 && this.equalCount == 0) {
this.addXVar (J.script.SV.newScriptVariableToken (J.script.T.tokenArraySelector));
break;
}if (!this.doBitsetSelect ()) return false;
break;
}if (!this.operate ()) return false;
tok0 = (this.oPt >= 0 ? this.oStack[this.oPt].tok : 0);
}
if (newOp != null) this.addXVar (J.script.SV.newVariable (269484436, newOp));
switch (op.tok) {
case 269484048:
this.parenCount++;
this.wasX = false;
break;
case 806354977:
var isFirst = this.getX ().asBoolean ();
if (tok0 == 269484066) this.ifPt--;
 else this.putOp (J.script.T.tokenColon);
this.putIf (isFirst ? 'T' : 'F');
this.skipping = !isFirst;
this.wasX = false;
return true;
case 269484066:
if (tok0 != 269484066) return false;
if (this.ifPt < 0) return false;
this.ifStack[this.ifPt] = 'X';
this.wasX = false;
this.skipping = true;
return true;
case 269484049:
this.wasX = true;
if (this.parenCount-- <= 0) return false;
if (tok0 == 269484066) {
this.ifPt--;
this.oPt--;
}this.oPt--;
if (this.oPt < 0) return true;
if (J.script.ScriptMathProcessor.isOpFunc (this.oStack[this.oPt]) && !this.evaluateFunction (0)) return false;
this.skipping = (this.ifPt >= 0 && this.ifStack[this.ifPt] == 'X');
return true;
case 269484080:
this.wasX = false;
return true;
case 269484096:
this.squareCount++;
this.wasX = false;
break;
case 269484097:
this.wasX = true;
if (this.squareCount-- <= 0 || this.oPt < 0) return false;
if (this.oStack[this.oPt].tok == 135266306) return this.evaluateFunction (269484096);
this.oPt--;
return true;
case 269484241:
this.wasX = (!allowMathFunc || !J.script.T.tokAttr (op.intValue, 135266304));
break;
case 1048586:
this.braceCount++;
this.wasX = false;
break;
case 1048590:
if (this.braceCount-- <= 0) return false;
this.wasX = false;
break;
case 269484128:
case 269484112:
if (!this.wasSyntaxCheck && this.xPt < 0) return false;
if (!this.wasSyntaxCheck && this.xStack[this.xPt].tok != 10 && this.xStack[this.xPt].tok != 7) {
var tf = this.getX ().asBoolean ();
this.addXVar (J.script.SV.getBoolean (tf));
if (tf == (op.tok == 269484112)) {
this.chk = true;
op = (op.tok == 269484112 ? J.script.T.tokenOrTRUE : J.script.T.tokenAndFALSE);
}}this.wasX = false;
break;
case 269484436:
if (this.squareCount == 0) this.equalCount++;
this.wasX = false;
break;
default:
this.wasX = false;
}
this.putOp (op);
if (op.tok == 269484241 && (op.intValue & -481) == 135368713 && op.intValue != 135368713) {
return this.evaluateFunction (0);
}return true;
}, "J.script.T,~B");
$_M(c$, "doBitsetSelect", 
($fz = function () {
if (this.xPt < 0 || this.xPt == 0 && !this.isArrayItem) {
return false;
}var var1 = this.xStack[this.xPt--];
var $var = this.xStack[this.xPt];
if ($var.tok == 7 && var1.tok == 4 && $var.intValue != 2147483647) {
$var = J.script.SV.selectItemVar2 ($var, -2147483648);
}if ($var.tok == 6) {
var v = $var.mapValue (J.script.SV.sValue (var1));
this.xStack[this.xPt] = (v == null ? J.script.SV.newVariable (4, "") : v);
return true;
}var i = var1.asInt ();
switch ($var.tok) {
default:
$var = J.script.SV.newVariable (4, J.script.SV.sValue ($var));
case 10:
case 7:
case 4:
case 11:
case 12:
this.xStack[this.xPt] = J.script.SV.selectItemVar2 ($var, i);
break;
}
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "dumpStacks", 
function (message) {
J.util.Logger.debug ("\n\n------------------\nRPN stacks: " + message + "\n");
for (var i = 0; i <= this.xPt; i++) J.util.Logger.debug ("x[" + i + "]: " + this.xStack[i]);

J.util.Logger.debug ("\n");
for (var i = 0; i <= this.oPt; i++) J.util.Logger.debug ("o[" + i + "]: " + this.oStack[i] + " prec=" + J.script.T.getPrecedence (this.oStack[i].tok));

J.util.Logger.debug (" ifStack = " + ( String.instantialize (this.ifStack)).substring (0, this.ifPt + 1));
}, "~S");
$_M(c$, "getX", 
($fz = function () {
if (this.xPt < 0) this.eval.error (13);
var v = J.script.SV.selectItemVar (this.xStack[this.xPt]);
this.xStack[this.xPt--] = null;
return v;
}, $fz.isPrivate = true, $fz));
$_M(c$, "evaluateFunction", 
($fz = function (tok) {
var op = this.oStack[this.oPt--];
if (tok == 0) tok = (op.tok == 269484241 ? op.intValue & -481 : op.tok);
var nParamMax = J.script.T.getMaxMathParams (tok);
var nParam = 0;
var pt = this.xPt;
while (pt >= 0 && this.xStack[pt--].value !== op) nParam++;

if (nParamMax > 0 && nParam > nParamMax) return false;
var args =  new Array (nParam);
for (var i = nParam; --i >= 0; ) args[i] = this.getX ();

this.xPt--;
if (this.chk) return (op.tok == 269484241 ? true : this.addXBool (true));
switch (tok) {
case 135266826:
case 135266819:
case 135266821:
case 135266318:
case 135266820:
case 135266822:
return this.evaluateMath (args, tok);
case 1276118017:
case 1276117504:
case 1276117507:
case 1276117509:
return this.evaluateList (op.intValue, args);
case 135266306:
case 269484096:
return this.evaluateArray (args, tok == 269484096);
case 135266307:
case 135270417:
return this.evaluateQuaternion (args, tok);
case 1276118529:
return this.evaluateBin (args);
case 1276117512:
case 1276117513:
return this.evaluateRowCol (args, tok);
case 1766856708:
return this.evaluateColor (args);
case 135270405:
return this.evaluateCompare (args);
case 135266310:
return this.evaluateConnected (args);
case 135267329:
return this.evaluateCross (args);
case 135270407:
return this.evaluateData (args);
case 1276118018:
case 1276117505:
if (op.tok == 269484241) return this.evaluateDot (args, tok, op.intValue);
case 135266305:
case 1746538509:
return this.evaluateMeasure (args, op.tok);
case 1229984263:
case 135271426:
return this.evaluateLoad (args, tok);
case 1276118531:
return this.evaluateFind (args);
case 135368713:
return this.evaluateUserFunction (op.value, args, op.intValue, op.tok == 269484241);
case 1288701960:
case 1826248715:
return this.evaluateLabel (op.intValue, args);
case 135270410:
return this.evaluateGetProperty (args);
case 137363468:
return this.evaluateHelix (args);
case 135267841:
case 135266319:
case 135267842:
return this.evaluatePlane (args, tok);
case 135287308:
case 135271429:
return this.evaluateScript (args, tok);
case 1276117506:
case 1276117508:
case 1276117510:
return this.evaluateString (op.intValue, args);
case 135266320:
return this.evaluatePoint (args);
case 135304707:
return this.evaluatePrompt (args);
case 135267332:
return this.evaluateRandom (args);
case 1276118019:
return this.evaluateReplace (args);
case 135267335:
case 135267336:
case 1238369286:
return this.evaluateSubstructure (args, tok);
case 135270422:
return this.evaluateCache (args);
case 1276117010:
case 1276117011:
return this.evaluateSort (args, tok);
case 1297090050:
return this.evaluateSymop (args, op.tok == 269484241);
case 1276117015:
return this.evaluateTensor (args);
case 135266324:
return this.evaluateWithin (args);
case 135402505:
return this.evaluateContact (args);
case 135270421:
return this.evaluateWrite (args);
}
return false;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "evaluateTensor", 
($fz = function (args) {
if (args.length > 2) return false;
var bs = J.script.SV.getBitSet (this.getX (), false);
var tensorType = (args.length == 0 ? null : J.script.SV.sValue (args[0]).toLowerCase ());
var calc = this.viewer.getNMRCalculation ();
if ("unique".equals (tensorType)) return this.addXBs (calc.getUniqueTensorSet (bs));
var infoType = (args.length < 2 ? null : J.script.SV.sValue (args[1]).toLowerCase ());
return this.addXList (calc.getTensorInfo (tensorType, infoType, bs));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateCache", 
($fz = function (args) {
if (args.length > 0) return false;
return this.addXMap (this.viewer.cacheList ());
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateCompare", 
($fz = function (args) {
if (args.length < 2 || args.length > 5) return false;
var stddev;
var sOpt = J.script.SV.sValue (args[args.length - 1]);
var isStdDev = sOpt.equalsIgnoreCase ("stddev");
var isIsomer = sOpt.equalsIgnoreCase ("ISOMER");
var isBonds = sOpt.equalsIgnoreCase ("BONDS");
var isSmiles = (isBonds || !isIsomer && args.length > (isStdDev ? 3 : 2));
var bs1 = (args[0].tok == 10 ? args[0].value : null);
var bs2 = (args[1].tok == 10 ? args[1].value : null);
var smiles1 = (bs1 == null ? J.script.SV.sValue (args[0]) : "");
var smiles2 = (bs2 == null ? J.script.SV.sValue (args[1]) : "");
var m =  new J.util.Matrix4f ();
stddev = NaN;
var ptsA;
var ptsB;
if (isSmiles) {
if (bs1 == null || bs2 == null) return false;
}if (isBonds) {
if (args.length != 4) return false;
smiles1 = J.script.SV.sValue (args[2]);
var mapSet = J.util.ArrayUtil.newInt2 (2);
this.eval.getSmilesCorrelation (bs1, bs2, smiles1, null, null, null, null, true, false, mapSet, null);
var bondMap1 = this.viewer.getDihedralMap (mapSet[0]);
var bondMap2 = (bondMap1 == null ? null : this.viewer.getDihedralMap (mapSet[1]));
if (bondMap2 == null || bondMap2.length != bondMap1.length) return this.addXStr ("");
var angles =  Clazz.newFloatArray (bondMap1.length, 3, 0);
var atoms = this.viewer.modelSet.atoms;
J.script.ScriptMathProcessor.getTorsions (atoms, bondMap2, angles, 0);
J.script.ScriptMathProcessor.getTorsions (atoms, bondMap1, angles, 1);
var data =  Clazz.newFloatArray (bondMap1.length * 6, 0);
for (var i = 0, pt = 0; i < bondMap1.length; i++) {
var map = bondMap1[i];
data[pt++] = map[0];
data[pt++] = map[1];
data[pt++] = map[2];
data[pt++] = map[3];
data[pt++] = angles[i][0];
data[pt++] = angles[i][1];
}
return this.addXAF (data);
}if (isIsomer) {
if (args.length != 3) return false;
if (bs1 == null && bs2 == null) return this.addXStr (this.viewer.getSmilesMatcher ().getRelationship (smiles1, smiles2).toUpperCase ());
var mf1 = (bs1 == null ? this.viewer.getSmilesMatcher ().getMolecularFormula (smiles1, false) : J.util.JmolMolecule.getMolecularFormula (this.viewer.getModelSet ().atoms, bs1, false));
var mf2 = (bs2 == null ? this.viewer.getSmilesMatcher ().getMolecularFormula (smiles2, false) : J.util.JmolMolecule.getMolecularFormula (this.viewer.getModelSet ().atoms, bs2, false));
if (!mf1.equals (mf2)) return this.addXStr ("NONE");
if (bs1 != null) smiles1 = this.eval.getSmilesMatches ("", null, bs1, null, false, true);
var check;
if (bs2 == null) {
check = (this.viewer.getSmilesMatcher ().areEqual (smiles2, smiles1) > 0);
} else {
check = ((this.eval.getSmilesMatches (smiles1, null, bs2, null, false, true)).nextSetBit (0) >= 0);
}if (!check) {
var s = smiles1 + smiles2;
if (s.indexOf ("/") >= 0 || s.indexOf ("\\") >= 0 || s.indexOf ("@") >= 0) {
if (smiles1.indexOf ("@") >= 0 && (bs2 != null || smiles2.indexOf ("@") >= 0)) {
smiles1 = this.viewer.getSmilesMatcher ().reverseChirality (smiles1);
if (bs2 == null) {
check = (this.viewer.getSmilesMatcher ().areEqual (smiles1, smiles2) > 0);
} else {
check = ((this.eval.getSmilesMatches (smiles1, null, bs2, null, false, true)).nextSetBit (0) >= 0);
}if (check) return this.addXStr ("ENANTIOMERS");
}if (bs2 == null) {
check = (this.viewer.getSmilesMatcher ().areEqual ("/nostereo/" + smiles2, smiles1) > 0);
} else {
var ret = this.eval.getSmilesMatches ("/nostereo/" + smiles1, null, bs2, null, false, true);
check = ((ret).nextSetBit (0) >= 0);
}if (check) return this.addXStr ("DIASTERIOMERS");
}return this.addXStr ("CONSTITUTIONAL ISOMERS");
}if (bs1 == null || bs2 == null) return this.addXStr ("IDENTICAL");
stddev = this.eval.getSmilesCorrelation (bs1, bs2, smiles1, null, null, null, null, false, false, null, null);
return this.addXStr (stddev < 0.2 ? "IDENTICAL" : "IDENTICAL or CONFORMATIONAL ISOMERS (RMSD=" + stddev + ")");
} else if (isSmiles) {
ptsA =  new J.util.JmolList ();
ptsB =  new J.util.JmolList ();
sOpt = J.script.SV.sValue (args[2]);
var isMap = sOpt.equalsIgnoreCase ("MAP");
isSmiles = (sOpt.equalsIgnoreCase ("SMILES"));
var isSearch = (isMap || sOpt.equalsIgnoreCase ("SMARTS"));
if (isSmiles || isSearch) sOpt = (args.length > 3 ? J.script.SV.sValue (args[3]) : null);
if (sOpt == null) return false;
stddev = this.eval.getSmilesCorrelation (bs1, bs2, sOpt, ptsA, ptsB, m, null, !isSmiles, isMap, null, null);
if (isMap) {
var nAtoms = ptsA.size ();
if (nAtoms == 0) return this.addXStr ("");
var nMatch = Clazz.doubleToInt (ptsB.size () / nAtoms);
var ret =  new J.util.JmolList ();
for (var i = 0, pt = 0; i < nMatch; i++) {
var a = J.util.ArrayUtil.newInt2 (nAtoms);
ret.addLast (a);
for (var j = 0; j < nAtoms; j++, pt++) a[j] = [(ptsA.get (j)).index, (ptsB.get (pt)).index];

}
return this.addXList (ret);
}} else {
ptsA = this.eval.getPointVector (args[0], 0);
ptsB = this.eval.getPointVector (args[1], 0);
if (ptsA != null && ptsB != null) stddev = J.util.Measure.getTransformMatrix4 (ptsA, ptsB, m, null);
}return (isStdDev || Float.isNaN (stddev) ? this.addXFloat (stddev) : this.addXM4 (m));
}, $fz.isPrivate = true, $fz), "~A");
c$.getTorsions = $_M(c$, "getTorsions", 
($fz = function (atoms, bondMap, diff, pt) {
for (var i = bondMap.length; --i >= 0; ) {
var map = bondMap[i];
var v = J.util.Measure.computeTorsion (atoms[map[0]], atoms[map[1]], atoms[map[2]], atoms[map[3]], true);
if (pt == 1) {
if (v - diff[i][0] > 180) v -= 360;
 else if (v - diff[i][0] <= -180) v += 360;
}diff[i][pt] = v;
}
}, $fz.isPrivate = true, $fz), "~A,~A,~A,~N");
$_M(c$, "evaluateSort", 
($fz = function (args, tok) {
if (args.length > 1) return false;
if (tok == 1276117010) {
var n = (args.length == 0 ? 0 : args[0].asInt ());
return this.addXVar (this.getX ().sortOrReverse (n));
}var x = this.getX ();
var match = (args.length == 0 ? null : args[0]);
if (x.tok == 4) {
var n = 0;
var s = J.script.SV.sValue (x);
if (match == null) return this.addXInt (0);
var m = J.script.SV.sValue (match);
for (var i = 0; i < s.length; i++) {
var pt = s.indexOf (m, i);
if (pt < 0) break;
n++;
i = pt;
}
return this.addXInt (n);
}var counts =  new J.util.JmolList ();
var last = null;
var count = null;
var xList = J.script.SV.getVariable (x.value).sortOrReverse (0).getList ();
if (xList == null) return (match == null ? this.addXStr ("") : this.addXInt (0));
for (var i = 0, nLast = xList.size (); i <= nLast; i++) {
var a = (i == nLast ? null : xList.get (i));
if (match != null && a != null && !J.script.SV.areEqual (a, match)) continue;
if (J.script.SV.areEqual (a, last)) {
count.intValue++;
continue;
} else if (last != null) {
var y =  new J.util.JmolList ();
y.addLast (last);
y.addLast (count);
counts.addLast (J.script.SV.getVariableList (y));
}count = J.script.SV.newScriptVariableInt (1);
last = a;
}
if (match == null) return this.addXVar (J.script.SV.getVariableList (counts));
if (counts.isEmpty ()) return this.addXInt (0);
return this.addXVar (counts.get (0).getList ().get (1));
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateSymop", 
($fz = function (args, haveBitSet) {
if (args.length == 0) return false;
var x1 = (haveBitSet ? this.getX () : null);
if (x1 != null && x1.tok != 10) return false;
var bs = (x1 != null ? x1.value : args.length > 2 && args[1].tok == 10 ? args[1].value : this.viewer.getModelUndeletedAtomsBitSet (-1));
var xyz;
switch (args[0].tok) {
case 4:
xyz = J.script.SV.sValue (args[0]);
break;
case 12:
xyz = args[0].escape ();
break;
default:
xyz = null;
}
var iOp = (xyz == null ? args[0].asInt () : 0);
var pt = (args.length > 1 ? this.ptValue (args[1], true) : null);
if (args.length == 2 && !Float.isNaN (pt.x)) return this.addXObj (this.viewer.getSymmetryInfo (bs, xyz, iOp, pt, null, null, 135266320));
var desc = (args.length == 1 ? "" : J.script.SV.sValue (args[args.length - 1])).toLowerCase ();
var tok = 135176;
if (args.length == 1 || desc.equalsIgnoreCase ("matrix")) {
tok = 12;
} else if (desc.equalsIgnoreCase ("array") || desc.equalsIgnoreCase ("list")) {
tok = 1073742001;
} else if (desc.equalsIgnoreCase ("description")) {
tok = 1826248715;
} else if (desc.equalsIgnoreCase ("xyz")) {
tok = 1073741982;
} else if (desc.equalsIgnoreCase ("translation")) {
tok = 1073742178;
} else if (desc.equalsIgnoreCase ("axis")) {
tok = 1073741854;
} else if (desc.equalsIgnoreCase ("plane")) {
tok = 135266319;
} else if (desc.equalsIgnoreCase ("angle")) {
tok = 135266305;
} else if (desc.equalsIgnoreCase ("axispoint")) {
tok = 135266320;
} else if (desc.equalsIgnoreCase ("center")) {
tok = 12289;
}return this.addXObj (this.viewer.getSymmetryInfo (bs, xyz, iOp, pt, null, desc, tok));
}, $fz.isPrivate = true, $fz), "~A,~B");
$_M(c$, "evaluateBin", 
($fz = function (args) {
if (args.length != 3) return false;
var x1 = this.getX ();
var isListf = (x1.tok == 13);
if (!isListf && x1.tok != 7) return this.addXVar (x1);
var f0 = J.script.SV.fValue (args[0]);
var f1 = J.script.SV.fValue (args[1]);
var df = J.script.SV.fValue (args[2]);
var data;
if (isListf) {
data = x1.value;
} else {
var list = x1.getList ();
data =  Clazz.newFloatArray (list.size (), 0);
for (var i = list.size (); --i >= 0; ) data[i] = J.script.SV.fValue (list.get (i));

}var nbins = Clazz.doubleToInt (Math.floor ((f1 - f0) / df + 0.01));
var array =  Clazz.newIntArray (nbins, 0);
var nPoints = data.length;
for (var i = 0; i < nPoints; i++) {
var v = data[i];
var bin = Clazz.doubleToInt (Math.floor ((v - f0) / df));
if (bin < 0) bin = 0;
 else if (bin >= nbins) bin = nbins - 1;
array[bin]++;
}
return this.addXAI (array);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateHelix", 
($fz = function (args) {
if (args.length < 1 || args.length > 5) return false;
var pt = (args.length > 2 ? 3 : 1);
var type = (pt >= args.length ? "array" : J.script.SV.sValue (args[pt]));
var tok = J.script.T.getTokFromName (type);
if (args.length > 2) {
var pta = this.ptValue (args[0], true);
var ptb = this.ptValue (args[1], true);
if (args[2].tok != 9) return false;
var dq = J.util.Quaternion.newP4 (args[2].value);
switch (tok) {
case 0:
break;
case 135266320:
case 1073741854:
case 1666189314:
case 135266305:
case 1746538509:
return this.addXObj (J.util.Measure.computeHelicalAxis (null, tok, pta, ptb, dq));
case 135266306:
var data = J.util.Measure.computeHelicalAxis (null, 1073742001, pta, ptb, dq);
if (data == null) return false;
return this.addXAS (data);
default:
return this.addXObj (J.util.Measure.computeHelicalAxis (type, 135176, pta, ptb, dq));
}
} else {
var bs = (Clazz.instanceOf (args[0].value, J.util.BS) ? args[0].value : this.eval.compareInt (1095761937, 269484436, args[0].asInt ()));
switch (tok) {
case 135266320:
return this.addXObj (this.viewer.getHelixData (bs, 135266320));
case 1073741854:
return this.addXObj (this.viewer.getHelixData (bs, 1073741854));
case 1666189314:
return this.addXObj (this.viewer.getHelixData (bs, 1666189314));
case 135266305:
return this.addXFloat ((this.viewer.getHelixData (bs, 135266305)).floatValue ());
case 135176:
case 1746538509:
return this.addXObj (this.viewer.getHelixData (bs, tok));
case 135266306:
var data = this.viewer.getHelixData (bs, 1073742001);
if (data == null) return false;
return this.addXAS (data);
}
}return false;
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateDot", 
($fz = function (args, tok, intValue) {
if (args.length != 1) return false;
var x1 = this.getX ();
var x2 = args[0];
var pt2 = (x2.tok == 7 ? null : this.ptValue (x2, false));
var plane2 = this.planeValue (x2);
if (tok == 1276118018) {
var minMax = intValue & 480;
switch (x1.tok) {
case 10:
switch (x2.tok) {
case 10:
var bs = J.script.SV.bsSelectVar (x1);
if (minMax == 32 || minMax == 64) {
var bs2 = J.script.SV.bsSelectVar (x2);
var data =  Clazz.newFloatArray (bs.cardinality (), 0);
var atoms = this.viewer.modelSet.atoms;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
pt2 = atoms[i];
data[i] = (this.eval.getBitsetProperty (bs2, intValue, pt2, plane2, x1.value, null, false, x1.index, false)).floatValue ();
}
return this.addXAF (data);
}return this.addXObj (this.eval.getBitsetProperty (bs, intValue, pt2, plane2, x1.value, null, false, x1.index, false));
}
}
}return this.addXFloat (this.getDistance (x1, x2, tok));
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
$_M(c$, "getDistance", 
($fz = function (x1, x2, tok) {
var pt1 = this.ptValue (x1, true);
var plane1 = this.planeValue (x1);
var pt2 = this.ptValue (x2, true);
var plane2 = this.planeValue (x2);
if (tok == 1276117505) {
if (plane1 != null && plane2 != null) return plane1.x * plane2.x + plane1.y * plane2.y + plane1.z * plane2.z + plane1.w * plane2.w;
if (plane1 != null) pt1 = J.util.P3.new3 (plane1.x, plane1.y, plane1.z);
if (plane2 != null) pt2 = J.util.P3.new3 (plane2.x, plane2.y, plane2.z);
return pt1.x * pt2.x + pt1.y * pt2.y + pt1.z * pt2.z;
}if (plane1 == null) return (plane2 == null ? pt2.distance (pt1) : J.util.Measure.distanceToPlane (plane2, pt1));
return J.util.Measure.distanceToPlane (plane1, pt2);
}, $fz.isPrivate = true, $fz), "J.script.SV,J.script.SV,~N");
$_M(c$, "ptValue", 
function (x, allowFloat) {
var pt;
if (this.chk) return  new J.util.P3 ();
switch (x.tok) {
case 8:
return x.value;
case 10:
return this.eval.getBitsetProperty (J.script.SV.bsSelectVar (x), 1146095626, null, null, x.value, null, false, 2147483647, false);
case 4:
pt = J.util.Escape.uP (J.script.SV.sValue (x));
if (Clazz.instanceOf (pt, J.util.P3)) return pt;
break;
case 7:
pt = J.util.Escape.uP ("{" + J.script.SV.sValue (x) + "}");
if (Clazz.instanceOf (pt, J.util.P3)) return pt;
break;
}
if (!allowFloat) return null;
var f = J.script.SV.fValue (x);
return J.util.P3.new3 (f, f, f);
}, "J.script.SV,~B");
$_M(c$, "planeValue", 
($fz = function (x) {
if (this.chk) return  new J.util.P4 ();
switch (x.tok) {
case 9:
return x.value;
case 7:
case 4:
var pt = J.util.Escape.uP (J.script.SV.sValue (x));
return (Clazz.instanceOf (pt, J.util.P4) ? pt : null);
case 10:
break;
}
return null;
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "evaluateMeasure", 
($fz = function (args, tok) {
var nPoints = 0;
switch (tok) {
case 1746538509:
var points =  new J.util.JmolList ();
var rangeMinMax = [3.4028235E38, 3.4028235E38];
var strFormat = null;
var units = null;
var isAllConnected = false;
var isNotConnected = false;
var rPt = 0;
var isNull = false;
var rd = null;
var nBitSets = 0;
var vdw = 3.4028235E38;
var asMinArray = false;
var asArray = false;
for (var i = 0; i < args.length; i++) {
switch (args[i].tok) {
case 10:
var bs = args[i].value;
if (bs.length () == 0) isNull = true;
points.addLast (bs);
nPoints++;
nBitSets++;
break;
case 8:
var v =  new J.util.Point3fi ();
v.setT (args[i].value);
points.addLast (v);
nPoints++;
break;
case 2:
case 3:
rangeMinMax[rPt++ % 2] = J.script.SV.fValue (args[i]);
break;
case 4:
var s = J.script.SV.sValue (args[i]);
if (s.equalsIgnoreCase ("vdw") || s.equalsIgnoreCase ("vanderwaals")) vdw = (i + 1 < args.length && args[i + 1].tok == 2 ? args[++i].asInt () : 100) / 100;
 else if (s.equalsIgnoreCase ("notConnected")) isNotConnected = true;
 else if (s.equalsIgnoreCase ("connected")) isAllConnected = true;
 else if (s.equalsIgnoreCase ("minArray")) asMinArray = (nBitSets >= 1);
 else if (s.equalsIgnoreCase ("asArray")) asArray = (nBitSets >= 1);
 else if (J.util.Parser.isOneOf (s.toLowerCase (), ";nm;nanometers;pm;picometers;angstroms;ang;au;") || s.endsWith ("hz")) units = s.toLowerCase ();
 else strFormat = nPoints + ":" + s;
break;
default:
return false;
}
}
if (nPoints < 2 || nPoints > 4 || rPt > 2 || isNotConnected && isAllConnected) return false;
if (isNull) return this.addXStr ("");
if (vdw != 3.4028235E38 && (nBitSets != 2 || nPoints != 2)) return this.addXStr ("");
rd = (vdw == 3.4028235E38 ?  new J.atomdata.RadiusData (rangeMinMax, 0, null, null) :  new J.atomdata.RadiusData (null, vdw, J.atomdata.RadiusData.EnumType.FACTOR, J.constant.EnumVdw.AUTO));
return this.addXObj (( new J.modelset.MeasurementData (null, this.viewer, points)).set (0, null, rd, strFormat, units, null, isAllConnected, isNotConnected, null, true, 0, 0, null).getMeasurements (asArray, asMinArray));
case 135266305:
if ((nPoints = args.length) != 3 && nPoints != 4) return false;
break;
default:
if ((nPoints = args.length) != 2) return false;
}
var pts =  new Array (nPoints);
for (var i = 0; i < nPoints; i++) pts[i] = this.ptValue (args[i], true);

switch (nPoints) {
case 2:
return this.addXFloat (pts[0].distance (pts[1]));
case 3:
return this.addXFloat (J.util.Measure.computeAngleABC (pts[0], pts[1], pts[2], true));
case 4:
return this.addXFloat (J.util.Measure.computeTorsion (pts[0], pts[1], pts[2], pts[3], true));
}
return false;
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateUserFunction", 
($fz = function (name, args, tok, isSelector) {
var x1 = null;
if (isSelector) {
x1 = this.getX ();
if (x1.tok != 10) return false;
}this.wasX = false;
var params =  new J.util.JmolList ();
for (var i = 0; i < args.length; i++) {
params.addLast (args[i]);
}
if (isSelector) {
return this.addXObj (this.eval.getBitsetProperty (J.script.SV.bsSelectVar (x1), tok, null, null, x1.value, [name, params], false, x1.index, false));
}var $var = this.eval.runFunctionRet (null, name, params, null, true, true, false);
return ($var == null ? false : this.addXVar ($var));
}, $fz.isPrivate = true, $fz), "~S,~A,~N,~B");
$_M(c$, "evaluateFind", 
($fz = function (args) {
if (args.length == 0) return false;
var x1 = this.getX ();
var sFind = J.script.SV.sValue (args[0]);
var flags = (args.length > 1 && args[1].tok != 1048589 && args[1].tok != 1048588 ? J.script.SV.sValue (args[1]) : "");
var isSequence = sFind.equalsIgnoreCase ("SEQUENCE");
var isSmiles = sFind.equalsIgnoreCase ("SMILES");
var isSearch = sFind.equalsIgnoreCase ("SMARTS");
var isMF = sFind.equalsIgnoreCase ("MF");
if (isSmiles || isSearch || x1.tok == 10) {
var iPt = (isSmiles || isSearch ? 2 : 1);
var bs2 = (iPt < args.length && args[iPt].tok == 10 ? args[iPt++].value : null);
var asBonds = ("bonds".equalsIgnoreCase (J.script.SV.sValue (args[args.length - 1])));
var isAll = (asBonds || args[args.length - 1].tok == 1048589);
var ret = null;
switch (x1.tok) {
case 4:
var smiles = J.script.SV.sValue (x1);
if (bs2 != null) return false;
if (flags.equalsIgnoreCase ("mf")) {
ret = this.viewer.getSmilesMatcher ().getMolecularFormula (smiles, isSearch);
if (ret == null) this.eval.evalError (this.viewer.getSmilesMatcher ().getLastException (), null);
} else {
ret = this.eval.getSmilesMatches (flags, smiles, null, null, isSearch, !isAll);
}break;
case 10:
if (isMF) return this.addXStr (J.util.JmolMolecule.getMolecularFormula (this.viewer.getModelSet ().atoms, x1.value, false));
if (isSequence) return this.addXStr (this.viewer.getSmiles (-1, -1, x1.value, true, isAll, isAll, false));
if (isSmiles || isSearch) sFind = flags;
var bsMatch3D = bs2;
if (asBonds) {
var map = this.viewer.getSmilesMatcher ().getCorrelationMaps (sFind, this.viewer.modelSet.atoms, this.viewer.getAtomCount (), x1.value, !isSmiles, true);
ret = (map.length > 0 ? this.viewer.getDihedralMap (map[0]) :  Clazz.newIntArray (0, 0));
} else {
ret = this.eval.getSmilesMatches (sFind, null, x1.value, bsMatch3D, !isSmiles, !isAll);
}break;
}
if (ret == null) this.eval.error (22);
return this.addXObj (ret);
}var isReverse = (flags.indexOf ("v") >= 0);
var isCaseInsensitive = (flags.indexOf ("i") >= 0);
var asMatch = (flags.indexOf ("m") >= 0);
var isList = (x1.tok == 7);
var isPattern = (args.length == 2);
if (isList || isPattern) {
var pattern = null;
try {
pattern = java.util.regex.Pattern.compile (sFind, isCaseInsensitive ? 2 : 0);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.eval.evalError (e.toString (), null);
} else {
throw e;
}
}
var list = J.script.SV.listValue (x1);
if (J.util.Logger.debugging) J.util.Logger.debug ("finding " + sFind);
var bs =  new J.util.BS ();
var ipt = 0;
var n = 0;
var matcher = null;
var v = (asMatch ?  new J.util.JmolList () : null);
for (var i = 0; i < list.length; i++) {
var what = list[i];
matcher = pattern.matcher (what);
var isMatch = matcher.find ();
if (asMatch && isMatch || !asMatch && isMatch == !isReverse) {
n++;
ipt = i;
bs.set (i);
if (asMatch) v.addLast (isReverse ? what.substring (0, matcher.start ()) + what.substring (matcher.end ()) : matcher.group ());
}}
if (!isList) {
return (asMatch ? this.addXStr (v.size () == 1 ? v.get (0) : "") : isReverse ? this.addXBool (n == 1) : asMatch ? this.addXStr (n == 0 ? "" : matcher.group ()) : this.addXInt (n == 0 ? 0 : matcher.start () + 1));
}if (n == 1) return this.addXStr (asMatch ? v.get (0) : list[ipt]);
var listNew =  new Array (n);
if (n > 0) for (var i = list.length; --i >= 0; ) if (bs.get (i)) {
--n;
listNew[n] = (asMatch ? v.get (n) : list[i]);
}
return this.addXAS (listNew);
}return this.addXInt (J.script.SV.sValue (x1).indexOf (sFind) + 1);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateGetProperty", 
($fz = function (args) {
var pt = 0;
var propertyName = (args.length > pt ? J.script.SV.sValue (args[pt++]).toLowerCase () : "");
if (propertyName.startsWith ("$")) {
}var propertyValue = "";
if (propertyName.equalsIgnoreCase ("fileContents") && args.length > 2) {
var s = J.script.SV.sValue (args[1]);
for (var i = 2; i < args.length; i++) s += "|" + J.script.SV.sValue (args[i]);

propertyValue = s;
pt = args.length;
} else if (args.length > pt) {
switch (args[pt].tok) {
case 10:
propertyValue = J.script.SV.bsSelectVar (args[pt++]);
if (propertyName.equalsIgnoreCase ("bondInfo") && args.length > pt && args[pt].tok == 10) propertyValue = [propertyValue, J.script.SV.bsSelectVar (args[pt])];
break;
case 4:
if (this.viewer.checkPropertyParameter (propertyName)) propertyValue = args[pt++].value;
break;
}
}var property = this.viewer.getProperty (null, propertyName, propertyValue);
if (pt < args.length) property = this.viewer.extractProperty (property, args, pt);
return this.addXObj (J.script.SV.isVariableType (property) ? property : J.util.Escape.toReadable (propertyName, property));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluatePlane", 
($fz = function (args, tok) {
if (tok == 135267841 && args.length != 3 || tok == 135267842 && args.length != 2 && args.length != 3 || args.length == 0 || args.length > 4) return false;
var pt1;
var pt2;
var pt3;
var plane;
var norm;
var vTemp;
switch (args.length) {
case 1:
if (args[0].tok == 10) {
var bs = J.script.SV.getBitSet (args[0], false);
if (bs.cardinality () == 3) {
var pts = this.viewer.getAtomPointVector (bs);
var vNorm =  new J.util.V3 ();
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
plane =  new J.util.P4 ();
J.util.Measure.getPlaneThroughPoints (pts.get (0), pts.get (1), pts.get (2), vNorm, vAB, vAC, plane);
return this.addXPt4 (plane);
}}var pt = J.util.Escape.uP (J.script.SV.sValue (args[0]));
if (Clazz.instanceOf (pt, J.util.P4)) return this.addXPt4 (pt);
return this.addXStr ("" + pt);
case 2:
if (tok == 135267842) {
if (args[1].tok != 9) return false;
pt3 =  new J.util.P3 ();
norm =  new J.util.V3 ();
vTemp =  new J.util.V3 ();
plane = args[1].value;
if (args[0].tok == 9) {
var list = J.util.Measure.getIntersectionPP (args[0].value, plane);
if (list == null) return this.addXStr ("");
return this.addXList (list);
}pt2 = this.ptValue (args[0], false);
if (pt2 == null) return this.addXStr ("");
return this.addXPt (J.util.Measure.getIntersection (pt2, null, plane, pt3, norm, vTemp));
}case 3:
case 4:
switch (tok) {
case 135267841:
return this.addXPt4 (this.eval.getHklPlane (J.util.P3.new3 (J.script.SV.fValue (args[0]), J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2]))));
case 135267842:
pt1 = this.ptValue (args[0], false);
pt2 = this.ptValue (args[1], false);
if (pt1 == null || pt2 == null) return this.addXStr ("");
var vLine = J.util.V3.newV (pt2);
vLine.normalize ();
if (args[2].tok == 9) {
pt3 =  new J.util.P3 ();
norm =  new J.util.V3 ();
vTemp =  new J.util.V3 ();
pt1 = J.util.Measure.getIntersection (pt1, vLine, args[2].value, pt3, norm, vTemp);
if (pt1 == null) return this.addXStr ("");
return this.addXPt (pt1);
}pt3 = this.ptValue (args[2], false);
if (pt3 == null) return this.addXStr ("");
var v =  new J.util.V3 ();
J.util.Measure.projectOntoAxis (pt3, pt1, vLine, v);
return this.addXPt (pt3);
}
switch (args[0].tok) {
case 2:
case 3:
if (args.length == 3) {
var r = J.script.SV.fValue (args[0]);
var theta = J.script.SV.fValue (args[1]);
var phi = J.script.SV.fValue (args[2]);
norm = J.util.V3.new3 (0, 0, 1);
pt2 = J.util.P3.new3 (0, 1, 0);
var q = J.util.Quaternion.newVA (pt2, phi);
q.getMatrix ().transform (norm);
pt2.set (0, 0, 1);
q = J.util.Quaternion.newVA (pt2, theta);
q.getMatrix ().transform (norm);
pt2.setT (norm);
pt2.scale (r);
plane =  new J.util.P4 ();
J.util.Measure.getPlaneThroughPoint (pt2, norm, plane);
return this.addXPt4 (plane);
}break;
case 10:
case 8:
pt1 = this.ptValue (args[0], false);
pt2 = this.ptValue (args[1], false);
if (pt2 == null) return false;
pt3 = (args.length > 2 && (args[2].tok == 10 || args[2].tok == 8) ? this.ptValue (args[2], false) : null);
norm = J.util.V3.newV (pt2);
if (pt3 == null) {
plane =  new J.util.P4 ();
if (args.length == 2 || !args[2].asBoolean ()) {
pt3 = J.util.P3.newP (pt1);
pt3.add (pt2);
pt3.scale (0.5);
norm.sub (pt1);
norm.normalize ();
} else {
pt3 = pt1;
}J.util.Measure.getPlaneThroughPoint (pt3, norm, plane);
return this.addXPt4 (plane);
}var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var nd = J.util.Measure.getDirectedNormalThroughPoints (pt1, pt2, pt3, (args.length == 4 ? this.ptValue (args[3], true) : null), norm, vAB, vAC);
return this.addXPt4 (J.util.P4.new4 (norm.x, norm.y, norm.z, nd));
}
}
if (args.length != 4) return false;
var x = J.script.SV.fValue (args[0]);
var y = J.script.SV.fValue (args[1]);
var z = J.script.SV.fValue (args[2]);
var w = J.script.SV.fValue (args[3]);
return this.addXPt4 (J.util.P4.new4 (x, y, z, w));
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluatePoint", 
($fz = function (args) {
if (args.length != 1 && args.length != 3 && args.length != 4) return false;
switch (args.length) {
case 1:
if (args[0].tok == 3 || args[0].tok == 2) return this.addXInt (args[0].asInt ());
var s = J.script.SV.sValue (args[0]);
if (args[0].tok == 7) s = "{" + s + "}";
var pt = J.util.Escape.uP (s);
if (Clazz.instanceOf (pt, J.util.P3)) return this.addXPt (pt);
return this.addXStr ("" + pt);
case 3:
return this.addXPt (J.util.P3.new3 (args[0].asFloat (), args[1].asFloat (), args[2].asFloat ()));
case 4:
return this.addXPt4 (J.util.P4.new4 (args[0].asFloat (), args[1].asFloat (), args[2].asFloat (), args[3].asFloat ()));
}
return false;
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluatePrompt", 
($fz = function (args) {
if (args.length != 1 && args.length != 2 && args.length != 3) return false;
var label = J.script.SV.sValue (args[0]);
var buttonArray = (args.length > 1 && args[1].tok == 7 ? J.script.SV.listValue (args[1]) : null);
var asButtons = (buttonArray != null || args.length == 1 || args.length == 3 && args[2].asBoolean ());
var input = (buttonArray != null ? null : args.length >= 2 ? J.script.SV.sValue (args[1]) : "OK");
var s = "" + this.viewer.prompt (label, input, buttonArray, asButtons);
return (asButtons && buttonArray != null ? this.addXInt (Integer.parseInt (s) + 1) : this.addXStr (s));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateReplace", 
($fz = function (args) {
if (args.length != 2) return false;
var x = this.getX ();
var sFind = J.script.SV.sValue (args[0]);
var sReplace = J.script.SV.sValue (args[1]);
var s = (x.tok == 7 ? null : J.script.SV.sValue (x));
if (s != null) return this.addXStr (J.util.TextFormat.simpleReplace (s, sFind, sReplace));
var list = J.script.SV.listValue (x);
for (var i = list.length; --i >= 0; ) list[i] = J.util.TextFormat.simpleReplace (list[i], sFind, sReplace);

return this.addXAS (list);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateString", 
($fz = function (tok, args) {
if (args.length > 1) return false;
var x = this.getX ();
var s = (tok == 1276117508 && x.tok == 10 || tok == 1276117510 && x.tok == 7 ? null : J.script.SV.sValue (x));
var sArg = (args.length == 1 ? J.script.SV.sValue (args[0]) : tok == 1276117510 ? "" : "\n");
switch (tok) {
case 1276117508:
if (x.tok == 10) {
var bsSelected = J.script.SV.bsSelectVar (x);
sArg = "\n";
var modelCount = this.viewer.getModelCount ();
s = "";
for (var i = 0; i < modelCount; i++) {
s += (i == 0 ? "" : "\n");
var bs = this.viewer.getModelUndeletedAtomsBitSet (i);
bs.and (bsSelected);
s += J.util.Escape.eBS (bs);
}
}return this.addXAS (J.util.TextFormat.splitChars (s, sArg));
case 1276117506:
if (s.length > 0 && s.charAt (s.length - 1) == '\n') s = s.substring (0, s.length - 1);
return this.addXStr (J.util.TextFormat.simpleReplace (s, "\n", sArg));
case 1276117510:
if (s != null) return this.addXStr (J.util.TextFormat.trim (s, sArg));
var list = J.script.SV.listValue (x);
for (var i = list.length; --i >= 0; ) list[i] = J.util.TextFormat.trim (list[i], sArg);

return this.addXAS (list);
}
return this.addXStr ("");
}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "evaluateList", 
($fz = function (tok, args) {
if (args.length != 1 && !(tok == 1276118017 && (args.length == 0 || args.length == 2))) return false;
var x1 = this.getX ();
var x2;
var len;
var sList1 = null;
var sList2 = null;
var sList3 = null;
if (args.length == 2) {
var itab = (args[0].tok == 4 ? 0 : 1);
var tab = J.script.SV.sValue (args[itab]);
sList1 = (x1.tok == 7 ? J.script.SV.listValue (x1) : J.util.TextFormat.split (J.script.SV.sValue (x1), '\n'));
x2 = args[1 - itab];
sList2 = (x2.tok == 7 ? J.script.SV.listValue (x2) : J.util.TextFormat.split (J.script.SV.sValue (x2), '\n'));
sList3 =  new Array (len = Math.max (sList1.length, sList2.length));
for (var i = 0; i < len; i++) sList3[i] = (i >= sList1.length ? "" : sList1[i]) + tab + (i >= sList2.length ? "" : sList2[i]);

return this.addXAS (sList3);
}x2 = (args.length == 0 ? J.script.SV.newVariable (1048579, "all") : args[0]);
var isAll = (x2.tok == 1048579);
if (x1.tok != 7 && x1.tok != 4) {
this.wasX = false;
this.addOp (J.script.T.tokenLeftParen);
this.addXVar (x1);
switch (tok) {
case 1276118017:
this.addOp (J.script.T.tokenPlus);
break;
case 1276117509:
this.addOp (J.script.T.tokenMinus);
break;
case 1276117507:
this.addOp (J.script.T.tokenTimes);
break;
case 1276117504:
this.addOp (J.script.T.tokenDivide);
break;
}
this.addXVar (x2);
return this.addOp (J.script.T.tokenRightParen);
}var isScalar = (x2.tok != 7 && J.script.SV.sValue (x2).indexOf ("\n") < 0);
var list1 = null;
var list2 = null;
var alist1 = x1.getList ();
var alist2 = x2.getList ();
if (x1.tok == 7) {
len = alist1.size ();
} else {
sList1 = (J.util.TextFormat.splitChars (x1.value, "\n"));
list1 =  Clazz.newFloatArray (len = sList1.length, 0);
J.util.Parser.parseFloatArrayData (sList1, list1);
}if (isAll) {
var sum = 0;
if (x1.tok == 7) {
for (var i = len; --i >= 0; ) sum += J.script.SV.fValue (alist1.get (i));

} else {
for (var i = len; --i >= 0; ) sum += list1[i];

}return this.addXFloat (sum);
}var scalar = null;
if (isScalar) {
scalar = x2;
} else if (x2.tok == 7) {
len = Math.min (len, alist2.size ());
} else {
sList2 = J.util.TextFormat.splitChars (x2.value, "\n");
list2 =  Clazz.newFloatArray (sList2.length, 0);
J.util.Parser.parseFloatArrayData (sList2, list2);
len = Math.min (list1.length, list2.length);
}var token = null;
switch (tok) {
case 1276118017:
token = J.script.T.tokenPlus;
break;
case 1276117509:
token = J.script.T.tokenMinus;
break;
case 1276117507:
token = J.script.T.tokenTimes;
break;
case 1276117504:
token = J.script.T.tokenDivide;
break;
}
var olist =  new Array (len);
for (var i = 0; i < len; i++) {
if (x1.tok == 7) this.addXVar (alist1.get (i));
 else if (Float.isNaN (list1[i])) this.addXObj (J.script.SV.unescapePointOrBitsetAsVariable (sList1[i]));
 else this.addXFloat (list1[i]);
if (isScalar) this.addXVar (scalar);
 else if (x2.tok == 7) this.addXVar (alist2.get (i));
 else if (Float.isNaN (list2[i])) this.addXObj (J.script.SV.unescapePointOrBitsetAsVariable (sList2[i]));
 else this.addXFloat (list2[i]);
if (!this.addOp (token) || !this.operate ()) return false;
olist[i] = this.xStack[this.xPt--];
}
return this.addXAV (olist);
}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "evaluateRowCol", 
($fz = function (args, tok) {
if (args.length != 1) return false;
var n = args[0].asInt () - 1;
var x1 = this.getX ();
var f;
switch (x1.tok) {
case 11:
if (n < 0 || n > 2) return false;
var m = x1.value;
switch (tok) {
case 1276117513:
f =  Clazz.newFloatArray (3, 0);
m.getRow (n, f);
return this.addXAF (f);
case 1276117512:
default:
f =  Clazz.newFloatArray (3, 0);
m.getColumn (n, f);
return this.addXAF (f);
}
case 12:
if (n < 0 || n > 2) return false;
var m4 = x1.value;
switch (tok) {
case 1276117513:
f =  Clazz.newFloatArray (4, 0);
m4.getRow (n, f);
return this.addXAF (f);
case 1276117512:
default:
f =  Clazz.newFloatArray (4, 0);
m4.getColumn (n, f);
return this.addXAF (f);
}
}
return false;
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateArray", 
($fz = function (args, allowMatrix) {
var len = args.length;
if (allowMatrix && (len == 4 || len == 3)) {
var isMatrix = true;
for (var i = 0; i < len && isMatrix; i++) isMatrix = (args[i].tok == 7 && args[i].getList ().size () == len);

if (isMatrix) {
var m =  Clazz.newFloatArray (len * len, 0);
var pt = 0;
for (var i = 0; i < len && isMatrix; i++) {
var list = args[i].getList ();
for (var j = 0; j < len; j++) {
var x = J.script.SV.fValue (list.get (j));
if (Float.isNaN (x)) {
isMatrix = false;
break;
}m[pt++] = x;
}
}
if (isMatrix) {
if (len == 3) return this.addXM3 (J.util.Matrix3f.newA (m));
return this.addXM4 (J.util.Matrix4f.newA (m));
}}}var a =  new Array (args.length);
for (var i = a.length; --i >= 0; ) a[i] = J.script.SV.newScriptVariableToken (args[i]);

return this.addXAV (a);
}, $fz.isPrivate = true, $fz), "~A,~B");
$_M(c$, "evaluateMath", 
($fz = function (args, tok) {
if (tok == 135266318) {
if (args.length == 1 && args[0].tok == 4) return this.addXStr (( new java.util.Date ()) + "\t" + J.script.SV.sValue (args[0]));
return this.addXInt ((System.currentTimeMillis () & 0x7FFFFFFF) - (args.length == 0 ? 0 : args[0].asInt ()));
}if (args.length != 1) return false;
if (tok == 135266826) {
if (args[0].tok == 2) return this.addXInt (Math.abs (args[0].asInt ()));
return this.addXFloat (Math.abs (args[0].asFloat ()));
}var x = J.script.SV.fValue (args[0]);
switch (tok) {
case 135266819:
return this.addXFloat ((Math.acos (x) * 180 / 3.141592653589793));
case 135266821:
return this.addXFloat (Math.cos (x * 3.141592653589793 / 180));
case 135266820:
return this.addXFloat (Math.sin (x * 3.141592653589793 / 180));
case 135266822:
return this.addXFloat (Math.sqrt (x));
}
return false;
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateQuaternion", 
($fz = function (args, tok) {
var pt0 = null;
var nArgs = args.length;
var nMax = 2147483647;
var isRelative = false;
if (tok == 135270417) {
if (nArgs > 1 && args[nArgs - 1].tok == 4 && (args[nArgs - 1].value).equalsIgnoreCase ("relative")) {
nArgs--;
isRelative = true;
}if (nArgs > 1 && args[nArgs - 1].tok == 2 && args[0].tok == 10) {
nMax = args[nArgs - 1].asInt ();
if (nMax <= 0) nMax = 2147483646;
nArgs--;
}}switch (nArgs) {
case 0:
case 1:
case 4:
break;
case 2:
if (tok == 135270417) {
if (args[0].tok == 7 && args[1].tok == 7) break;
if (args[0].tok == 10 && (args[1].tok == 2 || args[1].tok == 10)) break;
}if ((pt0 = this.ptValue (args[0], false)) == null || tok != 135270417 && args[1].tok == 8) return false;
break;
case 3:
if (tok != 135270417) return false;
if (args[0].tok == 9) {
if (args[2].tok != 8 && args[2].tok != 10) return false;
break;
}for (var i = 0; i < 3; i++) if (args[i].tok != 8 && args[i].tok != 10) return false;

break;
default:
return false;
}
var q = null;
var qs = null;
var p4 = null;
switch (nArgs) {
case 0:
return this.addXPt4 (J.util.Quaternion.newQ (this.viewer.getRotationQuaternion ()).toPoint4f ());
case 1:
default:
if (tok == 135270417 && args[0].tok == 7) {
var data1 = J.script.ScriptMathProcessor.getQuaternionArray (args[0].getList (), 1073742001);
var mean = J.util.Quaternion.sphereMean (data1, null, 0.0001);
q = (Clazz.instanceOf (mean, J.util.Quaternion) ? mean : null);
break;
} else if (tok == 135270417 && args[0].tok == 10) {
qs = this.viewer.getAtomGroupQuaternions (args[0].value, nMax);
} else if (args[0].tok == 11) {
q = J.util.Quaternion.newM (args[0].value);
} else if (args[0].tok == 9) {
p4 = args[0].value;
} else {
var s = J.script.SV.sValue (args[0]);
var v = J.util.Escape.uP (s.equalsIgnoreCase ("best") ? this.viewer.getOrientationText (1073741863, null) : s);
if (!(Clazz.instanceOf (v, J.util.P4))) return false;
p4 = v;
}if (tok == 135266307) q = J.util.Quaternion.newVA (J.util.P3.new3 (p4.x, p4.y, p4.z), p4.w);
break;
case 2:
if (tok == 135270417) {
if (args[0].tok == 7 && args[1].tok == 7) {
var data1 = J.script.ScriptMathProcessor.getQuaternionArray (args[0].getList (), 1073742001);
var data2 = J.script.ScriptMathProcessor.getQuaternionArray (args[1].getList (), 1073742001);
qs = J.util.Quaternion.div (data2, data1, nMax, isRelative);
break;
}if (args[0].tok == 10 && args[1].tok == 10) {
var data1 = this.viewer.getAtomGroupQuaternions (args[0].value, 2147483647);
var data2 = this.viewer.getAtomGroupQuaternions (args[1].value, 2147483647);
qs = J.util.Quaternion.div (data2, data1, nMax, isRelative);
break;
}}var pt1 = this.ptValue (args[1], false);
p4 = this.planeValue (args[0]);
if (pt1 != null) q = J.util.Quaternion.getQuaternionFrame (J.util.P3.new3 (0, 0, 0), pt0, pt1);
 else q = J.util.Quaternion.newVA (pt0, J.script.SV.fValue (args[1]));
break;
case 3:
if (args[0].tok == 9) {
var pt = (args[2].tok == 8 ? args[2].value : this.viewer.getAtomSetCenter (args[2].value));
return this.addXStr ((J.util.Quaternion.newP4 (args[0].value)).draw ("q", J.script.SV.sValue (args[1]), pt, 1));
}var pts =  new Array (3);
for (var i = 0; i < 3; i++) pts[i] = (args[i].tok == 8 ? args[i].value : this.viewer.getAtomSetCenter (args[i].value));

q = J.util.Quaternion.getQuaternionFrame (pts[0], pts[1], pts[2]);
break;
case 4:
if (tok == 135270417) p4 = J.util.P4.new4 (J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2]), J.script.SV.fValue (args[3]), J.script.SV.fValue (args[0]));
 else q = J.util.Quaternion.newVA (J.util.P3.new3 (J.script.SV.fValue (args[0]), J.script.SV.fValue (args[1]), J.script.SV.fValue (args[2])), J.script.SV.fValue (args[3]));
break;
}
if (qs != null) {
if (nMax != 2147483647) {
var list =  new J.util.JmolList ();
for (var i = 0; i < qs.length; i++) list.addLast (qs[i].toPoint4f ());

return this.addXList (list);
}q = (qs.length > 0 ? qs[0] : null);
}return this.addXPt4 ((q == null ? J.util.Quaternion.newP4 (p4) : q).toPoint4f ());
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateRandom", 
($fz = function (args) {
if (args.length > 2) return false;
var lower = (args.length < 2 ? 0 : J.script.SV.fValue (args[0]));
var range = (args.length == 0 ? 1 : J.script.SV.fValue (args[args.length - 1]));
range -= lower;
return this.addXFloat ((Math.random () * range) + lower);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateCross", 
($fz = function (args) {
if (args.length != 2) return false;
var x1 = args[0];
var x2 = args[1];
if (x1.tok != 8 || x2.tok != 8) return false;
var a = J.util.V3.newV (x1.value);
var b = J.util.V3.newV (x2.value);
a.cross (a, b);
return this.addXPt (J.util.P3.newP (a));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateLoad", 
($fz = function (args, tok) {
if (args.length > 2 || args.length < 1) return false;
var file = J.script.SV.sValue (args[0]);
var nBytesMax = (args.length == 2 ? args[1].asInt () : -1);
if (this.viewer.isJS && file.startsWith ("?")) {
if (tok == 1229984263) return this.addXStr ("");
file = this.eval.loadFileAsync ("load()_", file, this.oPt, true);
}return this.addXStr (tok == 135271426 ? this.viewer.getFileAsString4 (file, nBytesMax, false, false) : this.viewer.getFilePath (file, false));
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateWrite", 
($fz = function (args) {
if (args.length == 0) return false;
return this.addXStr (this.eval.write (args));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateScript", 
($fz = function (args, tok) {
if (tok == 135287308 && args.length != 1 || args.length == 0 || args.length > 2) return false;
var s = J.script.SV.sValue (args[0]);
var sb =  new J.util.SB ();
switch (tok) {
case 135271429:
var appID = (args.length == 2 ? J.script.SV.sValue (args[1]) : ".");
if (!appID.equals (".")) sb.append (this.viewer.jsEval (appID + "\1" + s));
if (appID.equals (".") || appID.equals ("*")) this.eval.runScriptBuffer (s, sb);
break;
case 135287308:
sb.append (this.viewer.jsEval (s));
break;
}
s = sb.toString ();
var f;
return (Float.isNaN (f = J.util.Parser.parseFloatStrict (s)) ? this.addXStr (s) : s.indexOf (".") >= 0 ? this.addXFloat (f) : this.addXInt (J.util.Parser.parseInt (s)));
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "evaluateData", 
($fz = function (args) {
if (args.length != 1 && args.length != 2 && args.length != 4) return false;
var selected = J.script.SV.sValue (args[0]);
var type = (args.length == 2 ? J.script.SV.sValue (args[1]) : "");
if (args.length == 4) {
var iField = args[1].asInt ();
var nBytes = args[2].asInt ();
var firstLine = args[3].asInt ();
var f = J.util.Parser.extractData (selected, iField, nBytes, firstLine);
return this.addXStr (J.util.Escape.escapeFloatA (f, false));
}if (selected.indexOf ("data2d_") == 0) {
var f1 = this.viewer.getDataFloat2D (selected);
if (f1 == null) return this.addXStr ("");
if (args.length == 2 && args[1].tok == 2) {
var pt = args[1].intValue;
if (pt < 0) pt += f1.length;
if (pt >= 0 && pt < f1.length) return this.addXStr (J.util.Escape.escapeFloatA (f1[pt], false));
return this.addXStr ("");
}return this.addXStr (J.util.Escape.escapeFloatAA (f1, false));
}if (selected.indexOf ("property_") == 0) {
var f1 = this.viewer.getDataFloat (selected);
if (f1 == null) return this.addXStr ("");
var f2 = (type.indexOf ("property_") == 0 ? this.viewer.getDataFloat (type) : null);
if (f2 != null) {
f1 = J.util.ArrayUtil.arrayCopyF (f1, -1);
for (var i = Math.min (f1.length, f2.length); --i >= 0; ) f1[i] += f2[i];

}return this.addXStr (J.util.Escape.escapeFloatA (f1, false));
}if (args.length == 1) {
var data = this.viewer.getData (selected);
return this.addXStr (data == null ? "" : "" + data[1]);
}return this.addXStr (this.viewer.getData (selected, type));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateLabel", 
($fz = function (intValue, args) {
var x1 = (args.length < 2 ? this.getX () : null);
var format = (args.length == 0 ? "%U" : J.script.SV.sValue (args[0]));
var asArray = J.script.T.tokAttr (intValue, 480);
if (x1 == null) return this.addXStr (J.script.SV.sprintfArray (args));
var bs = J.script.SV.getBitSet (x1, true);
if (bs == null) return this.addXObj (J.script.SV.sprintf (J.util.TextFormat.formatCheck (format), x1));
return this.addXObj (this.eval.getBitsetIdent (bs, format, x1.value, true, x1.index, asArray));
}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "evaluateWithin", 
($fz = function (args) {
if (args.length < 1 || args.length > 5) return false;
var i = args.length;
var distance = 0;
var withinSpec = args[0].value;
var withinStr = "" + withinSpec;
var tok = args[0].tok;
if (tok == 4) tok = J.script.T.getTokFromName (withinStr);
var isVdw = (tok == 1649412120);
if (isVdw) {
distance = 100;
withinSpec = null;
}var bs;
var isWithinModelSet = false;
var isWithinGroup = false;
var isDistance = (isVdw || tok == 3 || tok == 2);
var rd = null;
switch (tok) {
case 1048580:
if (i != 3 || !(Clazz.instanceOf (args[1].value, J.util.BS)) || !(Clazz.instanceOf (args[2].value, J.util.BS))) return false;
return this.addXBs (this.viewer.getBranchBitSet ((args[2].value).nextSetBit (0), (args[1].value).nextSetBit (0), true));
case 135267336:
case 1238369286:
case 135267335:
var bsSelected = null;
var isOK = true;
switch (i) {
case 2:
break;
case 3:
isOK = (args[2].tok == 10);
if (isOK) bsSelected = args[2].value;
break;
default:
isOK = false;
}
if (!isOK) this.eval.error (22);
return this.addXObj (this.eval.getSmilesMatches (J.script.SV.sValue (args[1]), null, bsSelected, null, tok == 135267335, this.asBitSet));
}
if (Clazz.instanceOf (withinSpec, String)) {
if (tok == 0) {
tok = 1048614;
if (i > 2) return false;
i = 2;
}} else if (isDistance) {
if (!isVdw) distance = J.script.SV.fValue (args[0]);
if (i < 2) return false;
switch (tok = args[1].tok) {
case 1048589:
case 1048588:
isWithinModelSet = args[1].asBoolean ();
i = 0;
break;
case 4:
var s = J.script.SV.sValue (args[1]);
if (s.startsWith ("$")) return this.addXBs (this.eval.getAtomsNearSurface (distance, s.substring (1)));
isWithinGroup = (s.equalsIgnoreCase ("group"));
isVdw = (s.equalsIgnoreCase ("vanderwaals"));
if (isVdw) {
withinSpec = null;
tok = 1649412120;
} else {
tok = 1087373318;
}break;
}
} else {
return false;
}var pt = null;
var plane = null;
switch (i) {
case 1:
switch (tok) {
case 137363468:
case 3145760:
case 1679429641:
return this.addXBs (this.viewer.getAtomBits (tok, null));
case 1073741864:
return this.addXBs (this.viewer.getAtomBits (tok, ""));
case 1048614:
return this.addXBs (this.viewer.getAtomBits (1087373320, withinStr));
}
return false;
case 2:
switch (tok) {
case 1048614:
tok = 1087373320;
break;
case 1087375362:
case 1087375361:
case 1073741864:
case 1087373320:
return this.addXBs (this.viewer.getAtomBits (tok, J.script.SV.sValue (args[args.length - 1])));
}
break;
case 3:
switch (tok) {
case 1048589:
case 1048588:
case 1087373318:
case 1649412120:
case 135266319:
case 135267841:
case 1048582:
break;
case 1087373320:
withinStr = J.script.SV.sValue (args[2]);
break;
default:
return false;
}
break;
}
i = args.length - 1;
if (Clazz.instanceOf (args[i].value, J.util.P4)) {
plane = args[i].value;
} else if (Clazz.instanceOf (args[i].value, J.util.P3)) {
pt = args[i].value;
if (J.script.SV.sValue (args[1]).equalsIgnoreCase ("hkl")) plane = this.eval.getHklPlane (pt);
}if (i > 0 && plane == null && pt == null && !(Clazz.instanceOf (args[i].value, J.util.BS))) return false;
if (plane != null) return this.addXBs (this.viewer.getAtomsNearPlane (distance, plane));
if (pt != null) return this.addXBs (this.viewer.getAtomsNearPt (distance, pt));
bs = (args[i].tok == 10 ? J.script.SV.bsSelectVar (args[i]) : null);
if (tok == 1087373320) return this.addXBs (this.viewer.getSequenceBits (withinStr, bs));
if (bs == null) bs =  new J.util.BS ();
if (!isDistance) return this.addXBs (this.viewer.getAtomBits (tok, bs));
if (isWithinGroup) return this.addXBs (this.viewer.getGroupsWithin (Clazz.floatToInt (distance), bs));
if (isVdw) rd =  new J.atomdata.RadiusData (null, (distance > 10 ? distance / 100 : distance), (distance > 10 ? J.atomdata.RadiusData.EnumType.FACTOR : J.atomdata.RadiusData.EnumType.OFFSET), J.constant.EnumVdw.AUTO);
return this.addXBs (this.viewer.getAtomsWithinRadius (distance, bs, isWithinModelSet, rd));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateContact", 
($fz = function (args) {
if (args.length < 1 || args.length > 3) return false;
var i = 0;
var distance = 100;
var tok = args[0].tok;
switch (tok) {
case 3:
case 2:
distance = J.script.SV.fValue (args[i++]);
break;
case 10:
break;
default:
return false;
}
if (i == args.length || !(Clazz.instanceOf (args[i].value, J.util.BS))) return false;
var bsA = J.util.BSUtil.copy (J.script.SV.bsSelectVar (args[i++]));
if (this.chk) return this.addXBs ( new J.util.BS ());
var bsB = (i < args.length ? J.util.BSUtil.copy (J.script.SV.bsSelectVar (args[i])) : null);
var rd =  new J.atomdata.RadiusData (null, (distance > 10 ? distance / 100 : distance), (distance > 10 ? J.atomdata.RadiusData.EnumType.FACTOR : J.atomdata.RadiusData.EnumType.OFFSET), J.constant.EnumVdw.AUTO);
bsB = this.eval.setContactBitSets (bsA, bsB, true, NaN, rd, false);
bsB.or (bsA);
return this.addXBs (bsB);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateColor", 
($fz = function (args) {
var colorScheme = (args.length > 0 ? J.script.SV.sValue (args[0]) : "");
if (colorScheme.equalsIgnoreCase ("hsl") && args.length == 2) {
var pt = J.util.P3.newP (J.script.SV.ptValue (args[1]));
var hsl =  Clazz.newFloatArray (3, 0);
J.util.ColorEncoder.RGBtoHSL (pt.x, pt.y, pt.z, hsl);
pt.set (hsl[0] * 360, hsl[1] * 100, hsl[2] * 100);
return this.addXPt (pt);
}var isIsosurface = colorScheme.startsWith ("$");
var ce = (isIsosurface ? null : this.viewer.getColorEncoder (colorScheme));
if (!isIsosurface && ce == null) return this.addXStr ("");
var lo = (args.length > 1 ? J.script.SV.fValue (args[1]) : 3.4028235E38);
var hi = (args.length > 2 ? J.script.SV.fValue (args[2]) : 3.4028235E38);
var value = (args.length > 3 ? J.script.SV.fValue (args[3]) : 3.4028235E38);
var getValue = (value != 3.4028235E38 || lo != 3.4028235E38 && hi == 3.4028235E38);
var haveRange = (hi != 3.4028235E38);
if (!haveRange && colorScheme.length == 0) {
value = lo;
var range = this.viewer.getCurrentColorRange ();
lo = range[0];
hi = range[1];
}if (isIsosurface) {
var id = colorScheme.substring (1);
var data = [id, null];
if (!this.viewer.getShapePropertyData (24, "colorEncoder", data)) return this.addXStr ("");
ce = data[1];
} else {
ce.setRange (lo, hi, lo > hi);
}var key = ce.getColorKey ();
if (getValue) return this.addXPt (J.util.ColorUtil.colorPointFromInt2 (ce.getArgb (hi == 3.4028235E38 ? lo : value)));
return this.addXVar (J.script.SV.getVariableMap (key));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateConnected", 
($fz = function (args) {
if (args.length > 5) return false;
var min = -2147483648;
var max = 2147483647;
var fmin = 0;
var fmax = 3.4028235E38;
var order = 65535;
var atoms1 = null;
var atoms2 = null;
var haveDecimal = false;
var isBonds = false;
for (var i = 0; i < args.length; i++) {
var $var = args[i];
switch ($var.tok) {
case 10:
isBonds = (Clazz.instanceOf ($var.value, J.modelset.Bond.BondSet));
if (isBonds && atoms1 != null) return false;
if (atoms1 == null) atoms1 = J.script.SV.bsSelectVar ($var);
 else if (atoms2 == null) atoms2 = J.script.SV.bsSelectVar ($var);
 else return false;
break;
case 4:
var type = J.script.SV.sValue ($var);
if (type.equalsIgnoreCase ("hbond")) order = 30720;
 else order = J.script.ScriptEvaluator.getBondOrderFromString (type);
if (order == 131071) return false;
break;
case 3:
haveDecimal = true;
default:
var n = $var.asInt ();
var f = $var.asFloat ();
if (max != 2147483647) return false;
if (min == -2147483648) {
min = Math.max (n, 0);
fmin = f;
} else {
max = n;
fmax = f;
}}
}
if (min == -2147483648) {
min = 1;
max = 100;
fmin = 0.1;
fmax = 1.0E8;
} else if (max == 2147483647) {
max = min;
fmax = fmin;
fmin = 0.1;
}if (atoms1 == null) atoms1 = this.viewer.getModelUndeletedAtomsBitSet (-1);
if (haveDecimal && atoms2 == null) atoms2 = atoms1;
if (atoms2 != null) {
var bsBonds =  new J.util.BS ();
this.viewer.makeConnections (fmin, fmax, order, 1087373321, atoms1, atoms2, bsBonds, isBonds, false, 0);
return this.addXVar (J.script.SV.newVariable (10,  new J.modelset.Bond.BondSet (bsBonds, this.viewer.getAtomIndices (this.viewer.getAtomBits (1678770178, bsBonds)))));
}return this.addXBs (this.viewer.getAtomsConnected (min, max, order, atoms1));
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "evaluateSubstructure", 
($fz = function (args, tok) {
if (args.length == 0) return false;
var bs =  new J.util.BS ();
var pattern = J.script.SV.sValue (args[0]);
if (pattern.length > 0) try {
var bsSelected = (args.length == 2 && args[1].tok == 10 ? J.script.SV.bsSelectVar (args[1]) : null);
bs = this.viewer.getSmilesMatcher ().getSubstructureSet (pattern, this.viewer.getModelSet ().atoms, this.viewer.getAtomCount (), bsSelected, tok != 135267336 && tok != 1238369286, false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.eval.evalError (e.toString (), null);
} else {
throw e;
}
}
return this.addXBs (bs);
}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "operate", 
($fz = function () {
var op = this.oStack[this.oPt--];
var pt;
var pt4;
var m;
var s;
var f;
if (this.logMessages) {
this.dumpStacks ("operate: " + op);
}if (this.isArrayItem && this.squareCount == 0 && this.equalCount == 1 && this.oPt < 0 && (op.tok == 269484436)) {
return true;
}var x2 = this.getX ();
if (x2 === J.script.T.tokenArraySelector) return false;
if (x2.tok == 7 || x2.tok == 11 || x2.tok == 12) x2 = J.script.SV.selectItemVar (x2);
if (op.tok == 269484225 || op.tok == 269484226) {
if (!this.chk && !x2.increment (this.incrementX)) return false;
this.wasX = true;
this.putX (x2);
return true;
}if (op.tok == 269484144) {
if (this.chk) return this.addXBool (true);
switch (x2.tok) {
case 9:
return this.addXPt4 ((J.util.Quaternion.newP4 (x2.value)).inv ().toPoint4f ());
case 11:
m = J.util.Matrix3f.newM (x2.value);
m.invert ();
return this.addXM3 (m);
case 12:
var m4 = J.util.Matrix4f.newM (x2.value);
m4.invert ();
return this.addXM4 (m4);
case 10:
return this.addXBs (J.util.BSUtil.copyInvert (J.script.SV.bsSelectVar (x2), (Clazz.instanceOf (x2.value, J.modelset.Bond.BondSet) ? this.viewer.getBondCount () : this.viewer.getAtomCount ())));
default:
return this.addXBool (!x2.asBoolean ());
}
}var iv = op.intValue & -481;
if (op.tok == 269484241) {
switch (iv) {
case 1073741824:
return this.getAllProperties (x2, op.value);
case 1141899267:
case 1276117011:
case 1141899270:
if (iv == 1141899267 && Clazz.instanceOf (x2.value, J.modelset.Bond.BondSet)) break;
return this.addXInt (J.script.SV.sizeOf (x2));
case 1141899272:
return this.addXStr (J.script.ScriptMathProcessor.typeOf (x2));
case 1141899281:
if (x2.tok != 6) return this.addXStr ("");
var keyset = (x2.value).keySet ();
var keys = keyset.toArray ( new Array (keyset.size ()));
java.util.Arrays.sort (keys);
return this.addXAS (keys);
case 1141899268:
switch (x2.tok) {
case 11:
case 12:
s = J.script.SV.sValue (x2);
s = J.util.TextFormat.simpleReplace (s.substring (1, s.length - 1), "],[", "]\n[");
break;
case 4:
s = x2.value;
break;
default:
s = J.script.SV.sValue (x2);
}
s = J.util.TextFormat.simpleReplace (s, "\n\r", "\n").$replace ('\r', '\n');
return this.addXAS (J.util.TextFormat.split (s, '\n'));
case 1766856708:
switch (x2.tok) {
case 4:
case 7:
s = J.script.SV.sValue (x2);
pt =  new J.util.P3 ();
return this.addXPt (J.util.ColorUtil.colorPointFromString (s, pt));
case 2:
case 3:
return this.addXPt (this.viewer.getColorPointForPropertyValue (J.script.SV.fValue (x2)));
case 8:
return this.addXStr (J.util.Escape.escapeColor (J.util.ColorUtil.colorPtToInt (x2.value)));
default:
}
break;
case 1679429641:
return (this.chk ? this.addXStr ("x") : this.getBoundBox (x2));
}
if (this.chk) return this.addXStr (J.script.SV.sValue (x2));
if (x2.tok == 4) {
var v = J.script.SV.unescapePointOrBitsetAsVariable (J.script.SV.sValue (x2));
if (!(Clazz.instanceOf (v, J.script.SV))) return false;
x2 = v;
}if (op.tok == x2.tok) x2 = this.getX ();
return this.getPointOrBitsetOperation (op, x2);
}var x1 = this.getX ();
if (this.chk) {
if (op === J.script.T.tokenAndFALSE || op === J.script.T.tokenOrTRUE) this.chk = false;
return this.addXVar (J.script.SV.newScriptVariableToken (x1));
}switch (op.tok) {
case 269484160:
case 269484128:
switch (x1.tok) {
case 10:
var bs = J.script.SV.bsSelectVar (x1);
switch (x2.tok) {
case 10:
bs = J.util.BSUtil.copy (bs);
bs.and (J.script.SV.bsSelectVar (x2));
return this.addXBs (bs);
case 2:
var x = x2.asInt ();
return (this.addXBool (x < 0 ? false : bs.get (x)));
}
break;
}
return this.addXBool (x1.asBoolean () && x2.asBoolean ());
case 269484112:
switch (x1.tok) {
case 10:
var bs = J.util.BSUtil.copy (J.script.SV.bsSelectVar (x1));
switch (x2.tok) {
case 10:
bs.or (J.script.SV.bsSelectVar (x2));
return this.addXBs (bs);
case 2:
var x = x2.asInt ();
if (x < 0) break;
bs.set (x);
return this.addXBs (bs);
case 7:
var sv = x2.value;
for (var i = sv.size (); --i >= 0; ) {
var b = sv.get (i).asInt ();
if (b >= 0) bs.set (b);
}
return this.addXBs (bs);
}
break;
case 7:
return this.addXVar (J.script.SV.concatList (x1, x2, false));
}
return this.addXBool (x1.asBoolean () || x2.asBoolean ());
case 269484113:
if (x1.tok == 10 && x2.tok == 10) {
var bs = J.util.BSUtil.copy (J.script.SV.bsSelectVar (x1));
bs.xor (J.script.SV.bsSelectVar (x2));
return this.addXBs (bs);
}var a = x1.asBoolean ();
var b = x2.asBoolean ();
return this.addXBool (a && !b || b && !a);
case 269484114:
if (x1.tok != 10 || x2.tok != 10) return false;
return this.addXBs (J.util.BSUtil.toggleInPlace (J.util.BSUtil.copy (J.script.SV.bsSelectVar (x1)), J.script.SV.bsSelectVar (x2)));
case 269484434:
return this.addXBool (x1.asFloat () <= x2.asFloat ());
case 269484433:
return this.addXBool (x1.asFloat () >= x2.asFloat ());
case 269484432:
return this.addXBool (x1.asFloat () > x2.asFloat ());
case 269484435:
return this.addXBool (x1.asFloat () < x2.asFloat ());
case 269484436:
return this.addXBool (J.script.SV.areEqual (x1, x2));
case 269484438:
return this.addXBool (!J.script.SV.areEqual (x1, x2));
case 269484193:
switch (x1.tok) {
default:
return this.addXFloat (x1.asFloat () + x2.asFloat ());
case 7:
return this.addXVar (J.script.SV.concatList (x1, x2, true));
case 2:
switch (x2.tok) {
case 4:
if ((s = (J.script.SV.sValue (x2)).trim ()).indexOf (".") < 0 && s.indexOf ("+") <= 0 && s.lastIndexOf ("-") <= 0) return this.addXInt (x1.intValue + x2.asInt ());
break;
case 3:
return this.addXFloat (x1.intValue + x2.asFloat ());
}
return this.addXInt (x1.intValue + x2.asInt ());
case 4:
return this.addXVar (J.script.SV.newVariable (4, J.script.SV.sValue (x1) + J.script.SV.sValue (x2)));
case 9:
var q1 = J.util.Quaternion.newP4 (x1.value);
switch (x2.tok) {
default:
return this.addXPt4 (q1.add (x2.asFloat ()).toPoint4f ());
case 9:
return this.addXPt4 (q1.mulQ (J.util.Quaternion.newP4 (x2.value)).toPoint4f ());
}
case 8:
pt = J.util.P3.newP (x1.value);
switch (x2.tok) {
case 8:
pt.add (x2.value);
return this.addXPt (pt);
case 9:
pt4 = x2.value;
pt.add (J.util.P3.new3 (pt4.x, pt4.y, pt4.z));
return this.addXPt (pt);
default:
f = x2.asFloat ();
return this.addXPt (J.util.P3.new3 (pt.x + f, pt.y + f, pt.z + f));
}
case 11:
switch (x2.tok) {
default:
return this.addXFloat (x1.asFloat () + x2.asFloat ());
case 11:
m = J.util.Matrix3f.newM (x1.value);
m.add (x2.value);
return this.addXM3 (m);
case 8:
return this.addXM4 (J.script.ScriptMathProcessor.getMatrix4f (x1.value, x2.value));
}
}
case 269484192:
if (x1.tok == 2) {
if (x2.tok == 4) {
if ((s = (J.script.SV.sValue (x2)).trim ()).indexOf (".") < 0 && s.indexOf ("+") <= 0 && s.lastIndexOf ("-") <= 0) return this.addXInt (x1.intValue - x2.asInt ());
} else if (x2.tok != 3) return this.addXInt (x1.intValue - x2.asInt ());
}if (x1.tok == 4 && x2.tok == 2) {
if ((s = (J.script.SV.sValue (x1)).trim ()).indexOf (".") < 0 && s.indexOf ("+") <= 0 && s.lastIndexOf ("-") <= 0) return this.addXInt (x1.asInt () - x2.intValue);
}switch (x1.tok) {
default:
return this.addXFloat (x1.asFloat () - x2.asFloat ());
case 6:
var ht =  new java.util.Hashtable (x1.value);
ht.remove (J.script.SV.sValue (x2));
return this.addXVar (J.script.SV.getVariableMap (ht));
case 11:
switch (x2.tok) {
default:
return this.addXFloat (x1.asFloat () - x2.asFloat ());
case 11:
m = J.util.Matrix3f.newM (x1.value);
m.sub (x2.value);
return this.addXM3 (m);
}
case 12:
switch (x2.tok) {
default:
return this.addXFloat (x1.asFloat () - x2.asFloat ());
case 12:
var m4 = J.util.Matrix4f.newM (x1.value);
m4.sub (x2.value);
return this.addXM4 (m4);
}
case 8:
pt = J.util.P3.newP (x1.value);
switch (x2.tok) {
default:
f = x2.asFloat ();
return this.addXPt (J.util.P3.new3 (pt.x - f, pt.y - f, pt.z - f));
case 8:
pt.sub (x2.value);
return this.addXPt (pt);
case 9:
pt4 = x2.value;
pt.sub (J.util.P3.new3 (pt4.x, pt4.y, pt4.z));
return this.addXPt (pt);
}
case 9:
var q1 = J.util.Quaternion.newP4 (x1.value);
switch (x2.tok) {
default:
return this.addXPt4 (q1.add (-x2.asFloat ()).toPoint4f ());
case 9:
var q2 = J.util.Quaternion.newP4 (x2.value);
return this.addXPt4 (q2.mulQ (q1.inv ()).toPoint4f ());
}
}
case 269484224:
switch (x2.tok) {
default:
return this.addXFloat (-x2.asFloat ());
case 2:
return this.addXInt (-x2.asInt ());
case 8:
pt = J.util.P3.newP (x2.value);
pt.scale (-1.0);
return this.addXPt (pt);
case 9:
pt4 = J.util.P4.newPt (x2.value);
pt4.scale (-1.0);
return this.addXPt4 (pt4);
case 11:
m = J.util.Matrix3f.newM (x2.value);
m.transpose ();
return this.addXM3 (m);
case 12:
var m4 = J.util.Matrix4f.newM (x2.value);
m4.transpose ();
return this.addXM4 (m4);
case 10:
return this.addXBs (J.util.BSUtil.copyInvert (J.script.SV.bsSelectVar (x2), (Clazz.instanceOf (x2.value, J.modelset.Bond.BondSet) ? this.viewer.getBondCount () : this.viewer.getAtomCount ())));
}
case 269484209:
if (x1.tok == 2 && x2.tok != 3) return this.addXInt (x1.intValue * x2.asInt ());
pt = (x1.tok == 11 ? this.ptValue (x2, false) : x2.tok == 11 ? this.ptValue (x1, false) : null);
pt4 = (x1.tok == 12 ? this.planeValue (x2) : x2.tok == 12 ? this.planeValue (x1) : null);
switch (x2.tok) {
case 11:
if (pt != null) {
var m3b = J.util.Matrix3f.newM (x2.value);
m3b.transpose ();
m3b.transform (pt);
if (x1.tok == 7) return this.addXVar (J.script.SV.getVariableAF ([pt.x, pt.y, pt.z]));
return this.addXPt (pt);
}if (pt4 != null) {
return this.addXPt4 ((J.util.Quaternion.newP4 (pt4).mulQ (J.util.Quaternion.newM (x2.value))).toPoint4f ());
}break;
case 12:
if (pt4 != null) {
var m4b = J.util.Matrix4f.newM (x2.value);
m4b.transpose ();
m4b.transform4 (pt4);
if (x1.tok == 7) return this.addXVar (J.script.SV.getVariableAF ([pt4.x, pt4.y, pt4.z, pt4.w]));
return this.addXPt4 (pt4);
}break;
}
switch (x1.tok) {
default:
return this.addXFloat (x1.asFloat () * x2.asFloat ());
case 11:
var m3 = x1.value;
if (pt != null) {
m3.transform (pt);
if (x2.tok == 7) return this.addXVar (J.script.SV.getVariableAF ([pt.x, pt.y, pt.z]));
return this.addXPt (pt);
}switch (x2.tok) {
case 11:
m = J.util.Matrix3f.newM (x2.value);
m.mul2 (m3, m);
return this.addXM3 (m);
case 9:
return this.addXM3 (J.util.Quaternion.newM (m3).mulQ (J.util.Quaternion.newP4 (x2.value)).getMatrix ());
default:
f = x2.asFloat ();
var aa =  new J.util.AxisAngle4f ();
aa.setM (m3);
aa.angle *= f;
var m2 =  new J.util.Matrix3f ();
m2.setAA (aa);
return this.addXM3 (m2);
}
case 12:
var m4 = x1.value;
if (pt != null) {
m4.transform (pt);
if (x2.tok == 7) return this.addXVar (J.script.SV.getVariableAF ([pt.x, pt.y, pt.z]));
return this.addXPt (pt);
}if (pt4 != null) {
m4.transform4 (pt4);
if (x2.tok == 7) return this.addXVar (J.script.SV.getVariableAF ([pt4.x, pt4.y, pt4.z, pt4.w]));
return this.addXPt4 (pt4);
}switch (x2.tok) {
case 12:
var m4b = J.util.Matrix4f.newM (x2.value);
m4b.mul2 (m4, m4b);
return this.addXM4 (m4b);
default:
return this.addXStr ("NaN");
}
case 8:
pt = J.util.P3.newP (x1.value);
switch (x2.tok) {
case 8:
var pt2 = (x2.value);
return this.addXFloat (pt.x * pt2.x + pt.y * pt2.y + pt.z * pt2.z);
default:
f = x2.asFloat ();
return this.addXPt (J.util.P3.new3 (pt.x * f, pt.y * f, pt.z * f));
}
case 9:
switch (x2.tok) {
case 9:
return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).mulQ (J.util.Quaternion.newP4 (x2.value)).toPoint4f ());
}
return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).mul (x2.asFloat ()).toPoint4f ());
}
case 269484210:
s = null;
var n = x2.asInt ();
switch (x1.tok) {
case 1048589:
case 1048588:
case 2:
default:
if (n == 0) return this.addXInt (0);
return this.addXInt (x1.asInt () % n);
case 3:
f = x1.asFloat ();
if (n == 0) return this.addXInt (Math.round (f));
s = J.util.TextFormat.formatDecimal (f, n);
return this.addXStr (s);
case 4:
s = x1.value;
if (n == 0) return this.addXStr (J.util.TextFormat.trim (s, "\n\t "));
if (n == 9999) return this.addXStr (s.toUpperCase ());
if (n == -9999) return this.addXStr (s.toLowerCase ());
if (n > 0) return this.addXStr (J.util.TextFormat.formatS (s, n, n, false, false));
return this.addXStr (J.util.TextFormat.formatS (s, n, n - 1, true, false));
case 7:
var list = J.script.SV.listValue (x1);
for (var i = 0; i < list.length; i++) {
if (n == 0) list[i] = list[i].trim ();
 else if (n > 0) list[i] = J.util.TextFormat.formatS (list[i], n, n, true, false);
 else list[i] = J.util.TextFormat.formatS (s, -n, n, false, false);
}
return this.addXAS (list);
case 8:
pt = J.util.P3.newP (x1.value);
this.viewer.toUnitCell (pt, J.util.P3.new3 (n, n, n));
return this.addXPt (pt);
case 9:
pt4 = x1.value;
if (x2.tok == 8) return this.addXPt ((J.util.Quaternion.newP4 (pt4)).transformPt (x2.value));
if (x2.tok == 9) {
var v4 = J.util.P4.newPt (x2.value);
(J.util.Quaternion.newP4 (pt4)).getThetaDirected (v4);
return this.addXPt4 (v4);
}switch (n) {
case 0:
return this.addXFloat (pt4.w);
case 1:
return this.addXFloat (pt4.x);
case 2:
return this.addXFloat (pt4.y);
case 3:
return this.addXFloat (pt4.z);
}
var q = J.util.Quaternion.newP4 (pt4);
switch (n) {
case 4:
return this.addXPt (J.util.P3.newP (q.getNormal ()));
case 5:
return this.addXAF (q.getEulerZXZ ());
case 6:
return this.addXAF (q.getEulerZYZ ());
case -1:
return this.addXPt (J.util.P3.newP (q.getVector (-1)));
case -2:
return this.addXFloat (q.getTheta ());
case -3:
return this.addXPt (J.util.P3.newP (q.getVector (0)));
case -4:
return this.addXPt (J.util.P3.newP (q.getVector (1)));
case -5:
return this.addXPt (J.util.P3.newP (q.getVector (2)));
case -6:
var ax = q.toAxisAngle4f ();
return this.addXPt4 (J.util.P4.new4 (ax.x, ax.y, ax.z, (ax.angle * 180 / 3.141592653589793)));
case -9:
return this.addXM3 (q.getMatrix ());
default:
return this.addXPt4 (pt4);
}
case 12:
var m4 = x1.value;
switch (n) {
case 1:
var m3 =  new J.util.Matrix3f ();
m4.getRotationScale (m3);
return this.addXM3 (m3);
case 2:
var v3 =  new J.util.V3 ();
m4.get (v3);
return this.addXPt (J.util.P3.newP (v3));
default:
return false;
}
case 10:
return this.addXBs (J.script.SV.bsSelectRange (x1, n));
}
case 269484208:
if (x1.tok == 2 && x2.tok == 2 && x2.intValue != 0) return this.addXInt (Clazz.doubleToInt (x1.intValue / x2.intValue));
var f2 = x2.asFloat ();
switch (x1.tok) {
default:
var f1 = x1.asFloat ();
return this.addXFloat (f1 / f2);
case 8:
pt = J.util.P3.newP (x1.value);
if (f2 == 0) return this.addXPt (J.util.P3.new3 (NaN, NaN, NaN));
return this.addXPt (J.util.P3.new3 (pt.x / f2, pt.y / f2, pt.z / f2));
case 9:
if (x2.tok == 9) return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).div (J.util.Quaternion.newP4 (x2.value)).toPoint4f ());
if (f2 == 0) return this.addXPt4 (J.util.P4.new4 (NaN, NaN, NaN, NaN));
return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).mul (1 / f2).toPoint4f ());
}
case 269484211:
f = x2.asFloat ();
switch (x1.tok) {
default:
return this.addXInt (f == 0 ? 0 : Clazz.doubleToInt (Math.floor (x1.asFloat () / x2.asFloat ())));
case 9:
if (f == 0) return this.addXPt4 (J.util.P4.new4 (NaN, NaN, NaN, NaN));
if (x2.tok == 9) return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).divLeft (J.util.Quaternion.newP4 (x2.value)).toPoint4f ());
return this.addXPt4 (J.util.Quaternion.newP4 (x1.value).mul (1 / f).toPoint4f ());
}
case 269484227:
f = Math.pow (x1.asFloat (), x2.asFloat ());
return (x1.tok == 2 && x2.tok == 2 ? this.addXInt (Clazz.floatToInt (f)) : this.addXFloat (f));
}
return true;
}, $fz.isPrivate = true, $fz));
c$.typeOf = $_M(c$, "typeOf", 
($fz = function (x) {
var tok = (x == null ? 0 : x.tok);
switch (tok) {
case 1048589:
case 1048588:
return "boolean";
case 10:
return (Clazz.instanceOf (x.value, J.modelset.Bond.BondSet) ? "bondset" : "bitset");
case 2:
case 3:
case 8:
case 9:
case 4:
case 7:
case 6:
case 11:
case 12:
return J.script.T.astrType[tok];
}
return "?";
}, $fz.isPrivate = true, $fz), "J.script.SV");
$_M(c$, "getAllProperties", 
($fz = function (x2, abbr) {
if (x2.tok != 10) return false;
if (this.chk) return this.addXStr ("");
var bs = J.script.SV.bsSelectVar (x2);
var tokens;
var n = bs.cardinality ();
if (n == 0 || (tokens = J.script.T.getAtomPropertiesLike (abbr.substring (0, abbr.length - 1))) == null) return this.addXStr ("");
var ht =  new java.util.Hashtable ();
var index = (n == 1 ? bs.nextSetBit (0) : 2147483647);
for (var i = tokens.size (); --i >= 0; ) {
var t = tokens.get (i);
var tok = t.tok;
switch (tok) {
case 1095766022:
case 1095761925:
continue;
default:
if (index == 2147483647) tok |= 480;
ht.put (t.value, J.script.SV.getVariable (this.eval.getBitsetProperty (bs, tok, null, null, null, null, false, index, true)));
}
}
return this.addXMap (ht);
}, $fz.isPrivate = true, $fz), "J.script.SV,~S");
c$.getMatrix4f = $_M(c$, "getMatrix4f", 
function (matRotate, vTranslate) {
return J.util.Matrix4f.newMV (matRotate, vTranslate == null ?  new J.util.V3 () : J.util.V3.newV (vTranslate));
}, "J.util.Matrix3f,J.util.Tuple3f");
$_M(c$, "getBoundBox", 
($fz = function (x2) {
if (x2.tok != 10) return false;
if (this.chk) return this.addXStr ("");
var b = this.viewer.getBoxInfo (J.script.SV.bsSelectVar (x2), 1);
var pts = b.getBoundBoxPoints (true);
var list =  new J.util.JmolList ();
for (var i = 0; i < 4; i++) list.addLast (pts[i]);

return this.addXList (list);
}, $fz.isPrivate = true, $fz), "J.script.SV");
$_M(c$, "getPointOrBitsetOperation", 
($fz = function (op, x2) {
switch (x2.tok) {
case 7:
switch (op.intValue) {
case 32:
case 64:
case 96:
case 192:
case 128:
case 160:
return this.addXObj (J.script.ScriptMathProcessor.getMinMax (x2.getList (), op.intValue));
case 1276117010:
case 1141899269:
return this.addXVar (x2.sortOrReverse (op.intValue == 1141899269 ? -2147483648 : 1));
}
var list2 =  new Array (x2.getList ().size ());
for (var i = 0; i < list2.length; i++) {
var v = J.script.SV.unescapePointOrBitsetAsVariable (x2.getList ().get (i));
if (!(Clazz.instanceOf (v, J.script.SV)) || !this.getPointOrBitsetOperation (op, v)) return false;
list2[i] = this.xStack[this.xPt--];
}
return this.addXAV (list2);
case 8:
switch (op.intValue) {
case 1112541185:
case 1112541205:
return this.addXFloat ((x2.value).x);
case 1112541186:
case 1112541206:
return this.addXFloat ((x2.value).y);
case 1112541187:
case 1112541207:
return this.addXFloat ((x2.value).z);
case 1146095626:
var pt = J.util.P3.newP (x2.value);
this.viewer.toCartesian (pt, true);
return this.addXPt (pt);
case 1112541188:
case 1112541189:
case 1112541190:
case 1146095627:
var ptf = J.util.P3.newP (x2.value);
this.viewer.toFractional (ptf, true);
return (op.intValue == 1146095627 ? this.addXPt (ptf) : this.addXFloat (op.intValue == 1112541188 ? ptf.x : op.intValue == 1112541189 ? ptf.y : ptf.z));
case 1112541191:
case 1112541192:
case 1112541193:
case 1146095629:
var ptfu = J.util.P3.newP (x2.value);
this.viewer.toFractional (ptfu, false);
return (op.intValue == 1146095627 ? this.addXPt (ptfu) : this.addXFloat (op.intValue == 1112541191 ? ptfu.x : op.intValue == 1112541192 ? ptfu.y : ptfu.z));
case 1112539153:
case 1112539154:
case 1112539155:
case 1146093582:
var ptu = J.util.P3.newP (x2.value);
this.viewer.toUnitCell (ptu, null);
this.viewer.toFractional (ptu, false);
return (op.intValue == 1146093582 ? this.addXPt (ptu) : this.addXFloat (op.intValue == 1112539153 ? ptu.x : op.intValue == 1112539154 ? ptu.y : ptu.z));
}
break;
case 9:
switch (op.intValue) {
case 1112541185:
case 1112541205:
return this.addXFloat ((x2.value).x);
case 1112541186:
case 1112541206:
return this.addXFloat ((x2.value).y);
case 1112541187:
case 1112541207:
return this.addXFloat ((x2.value).z);
case 1141899280:
return this.addXFloat ((x2.value).w);
}
break;
case 10:
if (op.intValue == 1678770178 && Clazz.instanceOf (x2.value, J.modelset.Bond.BondSet)) return this.addXVar (x2);
var bs = J.script.SV.bsSelectVar (x2);
if (bs.cardinality () == 1 && (op.intValue & 480) == 0) op.intValue |= 32;
var val = this.eval.getBitsetProperty (bs, op.intValue, null, null, x2.value, op.value, false, x2.index, true);
if (op.intValue != 1678770178) return this.addXObj (val);
return this.addXVar (J.script.SV.newVariable (10,  new J.modelset.Bond.BondSet (val, this.viewer.getAtomIndices (bs))));
}
return false;
}, $fz.isPrivate = true, $fz), "J.script.T,J.script.SV");
c$.getMinMax = $_M(c$, "getMinMax", 
($fz = function (floatOrSVArray, tok) {
var data = null;
var sv = null;
var ndata = 0;
while (true) {
if (J.util.Escape.isAF (floatOrSVArray)) {
data = floatOrSVArray;
ndata = data.length;
if (ndata == 0) break;
} else if (Clazz.instanceOf (floatOrSVArray, J.util.JmolList)) {
sv = floatOrSVArray;
ndata = sv.size ();
if (ndata == 0) break;
var sv0 = sv.get (0);
if (sv0.tok == 4 && (sv0.value).startsWith ("{")) {
var pt = J.script.SV.ptValue (sv0);
if (Clazz.instanceOf (pt, J.util.P3)) return J.script.ScriptMathProcessor.getMinMaxPoint (sv, tok);
if (Clazz.instanceOf (pt, J.util.P4)) return J.script.ScriptMathProcessor.getMinMaxQuaternion (sv, tok);
break;
}} else {
break;
}var sum;
switch (tok) {
case 32:
sum = 3.4028235E38;
break;
case 64:
sum = -3.4028235E38;
break;
default:
sum = 0;
}
var sum2 = 0;
var n = 0;
for (var i = ndata; --i >= 0; ) {
var v = (data == null ? J.script.SV.fValue (sv.get (i)) : data[i]);
if (Float.isNaN (v)) continue;
n++;
switch (tok) {
case 160:
case 192:
sum2 += (v) * v;
case 128:
case 96:
sum += v;
break;
case 32:
if (v < sum) sum = v;
break;
case 64:
if (v > sum) sum = v;
break;
}
}
if (n == 0) break;
switch (tok) {
case 96:
sum /= n;
break;
case 192:
if (n == 1) break;
sum = Math.sqrt ((sum2 - sum * sum / n) / (n - 1));
break;
case 32:
case 64:
case 128:
break;
case 160:
sum = sum2;
break;
}
return Float.$valueOf (sum);
}
return "NaN";
}, $fz.isPrivate = true, $fz), "~O,~N");
c$.getMinMaxPoint = $_M(c$, "getMinMaxPoint", 
($fz = function (pointOrSVArray, tok) {
var data = null;
var sv = null;
var ndata = 0;
if (Clazz.instanceOf (pointOrSVArray, Array)) {
data = pointOrSVArray;
ndata = data.length;
} else if (Clazz.instanceOf (pointOrSVArray, J.util.JmolList)) {
sv = pointOrSVArray;
ndata = sv.size ();
}if (sv != null || data != null) {
var result =  new J.util.P3 ();
var fdata =  Clazz.newFloatArray (ndata, 0);
var ok = true;
for (var xyz = 0; xyz < 3 && ok; xyz++) {
for (var i = 0; i < ndata; i++) {
var pt = (data == null ? J.script.SV.ptValue (sv.get (i)) : data[i]);
if (pt == null) {
ok = false;
break;
}switch (xyz) {
case 0:
fdata[i] = pt.x;
break;
case 1:
fdata[i] = pt.y;
break;
case 2:
fdata[i] = pt.z;
break;
}
}
if (!ok) break;
var f = J.script.ScriptMathProcessor.getMinMax (fdata, tok);
if (Clazz.instanceOf (f, Float)) {
var value = (f).floatValue ();
switch (xyz) {
case 0:
result.x = value;
break;
case 1:
result.y = value;
break;
case 2:
result.z = value;
break;
}
} else {
break;
}}
return result;
}return "NaN";
}, $fz.isPrivate = true, $fz), "~O,~N");
c$.getMinMaxQuaternion = $_M(c$, "getMinMaxQuaternion", 
($fz = function (svData, tok) {
var data;
switch (tok) {
case 32:
case 64:
case 128:
case 160:
return "NaN";
}
while (true) {
data = J.script.ScriptMathProcessor.getQuaternionArray (svData, 1073742001);
if (data == null) break;
var retStddev =  Clazz.newFloatArray (1, 0);
var result = J.util.Quaternion.sphereMean (data, retStddev, 0.0001);
switch (tok) {
case 96:
return result;
case 192:
return Float.$valueOf (retStddev[0]);
}
break;
}
return "NaN";
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
c$.getQuaternionArray = $_M(c$, "getQuaternionArray", 
function (quaternionOrSVData, itype) {
var data;
switch (itype) {
case 135270417:
data = quaternionOrSVData;
break;
case 9:
var pts = quaternionOrSVData;
data =  new Array (pts.length);
for (var i = 0; i < pts.length; i++) data[i] = J.util.Quaternion.newP4 (pts[i]);

break;
case 1073742001:
var sv = quaternionOrSVData;
data =  new Array (sv.size ());
for (var i = 0; i < sv.size (); i++) {
var pt = J.script.SV.pt4Value (sv.get (i));
if (pt == null) return null;
data[i] = J.util.Quaternion.newP4 (pt);
}
break;
default:
return null;
}
return data;
}, "~O,~N");
});
