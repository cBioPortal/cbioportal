Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread", "J.util.AxisAngle4f", "$.Matrix3f", "$.V3"], "J.thread.MoveToThread", ["java.lang.Float", "J.util.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.transformManager = null;
this.aaStepCenter = null;
this.aaStepNavCenter = null;
this.aaStep = null;
this.aaTotal = null;
this.matrixStart = null;
this.matrixStartInv = null;
this.matrixStep = null;
this.matrixEnd = null;
this.center = null;
this.navCenter = null;
this.ptMoveToCenter = null;
this.zoom = null;
this.xTrans = null;
this.yTrans = null;
this.xNav = null;
this.yNav = null;
this.navDepth = null;
this.cameraDepth = null;
this.cameraX = null;
this.cameraY = null;
this.rotationRadius = null;
this.pixelScale = null;
this.totalSteps = 0;
this.fps = 0;
this.frameTimeMillis = 0;
this.iStep = 0;
this.doEndMove = false;
this.floatSecondsTotal = 0;
this.fStep = 0;
if (!Clazz.isClassDefined ("J.thread.MoveToThread.Slider")) {
J.thread.MoveToThread.$MoveToThread$Slider$ ();
}
Clazz.instantialize (this, arguments);
}, J.thread, "MoveToThread", J.thread.JmolThread);
Clazz.prepareFields (c$, function () {
this.aaStepCenter =  new J.util.V3 ();
this.aaStepNavCenter =  new J.util.V3 ();
this.aaStep =  new J.util.AxisAngle4f ();
this.aaTotal =  new J.util.AxisAngle4f ();
this.matrixStart =  new J.util.Matrix3f ();
this.matrixStartInv =  new J.util.Matrix3f ();
this.matrixStep =  new J.util.Matrix3f ();
this.matrixEnd =  new J.util.Matrix3f ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.thread.MoveToThread, []);
});
Clazz.overrideMethod (c$, "setManager", 
function (manager, viewer, params) {
var options = params;
this.setViewer (viewer, "MoveToThread");
this.transformManager = manager;
this.center = options[0];
this.matrixEnd.setM (options[1]);
var f = options[3];
this.ptMoveToCenter = (this.center == null ? this.transformManager.fixedRotationCenter : this.center);
this.floatSecondsTotal = f[0];
this.zoom = this.newSlider (this.transformManager.zoomPercent, f[1]);
this.xTrans = this.newSlider (this.transformManager.getTranslationXPercent (), f[2]);
this.yTrans = this.newSlider (this.transformManager.getTranslationYPercent (), f[3]);
this.rotationRadius = this.newSlider (this.transformManager.modelRadius, (this.center == null || Float.isNaN (f[4]) ? this.transformManager.modelRadius : f[4] <= 0 ? viewer.calcRotationRadius (this.center) : f[4]));
this.pixelScale = this.newSlider (this.transformManager.scaleDefaultPixelsPerAngstrom, f[5]);
if (f[6] != 0) {
this.navCenter = options[2];
this.navDepth = this.newSlider (this.transformManager.getNavigationDepthPercent (), f[6]);
this.xNav = this.newSlider (this.transformManager.getNavigationOffsetPercent ('X'), f[7]);
this.yNav = this.newSlider (this.transformManager.getNavigationOffsetPercent ('Y'), f[8]);
}this.cameraDepth = this.newSlider (this.transformManager.getCameraDepth (), f[9]);
this.cameraX = this.newSlider (this.transformManager.camera.x, f[10]);
this.cameraY = this.newSlider (this.transformManager.camera.y, f[11]);
this.transformManager.getRotation (this.matrixStart);
this.matrixStartInv.invertM (this.matrixStart);
this.matrixStep.mul2 (this.matrixEnd, this.matrixStartInv);
this.aaTotal.setM (this.matrixStep);
this.fps = 30;
this.totalSteps = Clazz.floatToInt (this.floatSecondsTotal * this.fps);
this.frameTimeMillis = Clazz.doubleToInt (1000 / this.fps);
this.targetTime = System.currentTimeMillis ();
this.aaStepCenter.setT (this.ptMoveToCenter);
this.aaStepCenter.sub (this.transformManager.fixedRotationCenter);
this.aaStepCenter.scale (1 / this.totalSteps);
if (this.navCenter != null && this.transformManager.mode == 1) {
this.aaStepNavCenter.setT (this.navCenter);
this.aaStepNavCenter.sub (this.transformManager.navigationCenter);
this.aaStepNavCenter.scale (1 / this.totalSteps);
}return this.totalSteps;
}, "~O,J.viewer.Viewer,~O");
$_M(c$, "newSlider", 
($fz = function (start, value) {
return (Float.isNaN (value) || value == 3.4028235E38 ? null : Clazz.innerTypeInstance (J.thread.MoveToThread.Slider, this, null, start, value));
}, $fz.isPrivate = true, $fz), "~N,~N");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
if (this.totalSteps > 0) this.viewer.setInMotion (true);
mode = 0;
break;
case 0:
if (this.stopped || ++this.iStep >= this.totalSteps) {
mode = -2;
break;
}this.doStepTransform ();
this.doEndMove = true;
this.targetTime += this.frameTimeMillis;
this.currentTime = System.currentTimeMillis ();
var doRender = (this.currentTime < this.targetTime);
if (!doRender && this.isJS) {
this.targetTime = this.currentTime;
doRender = true;
}if (doRender) this.viewer.requestRepaintAndWait ("movetoThread");
if (this.transformManager.motion == null || !this.isJS && this.eval != null && !this.viewer.isScriptExecuting ()) {
this.stopped = true;
break;
}this.currentTime = System.currentTimeMillis ();
var sleepTime = (this.targetTime - this.currentTime);
if (!this.runSleep (sleepTime, 0)) return;
mode = 0;
break;
case -2:
if (this.totalSteps <= 0 || this.doEndMove && !this.stopped) this.doFinalTransform ();
if (this.totalSteps > 0) this.viewer.setInMotion (false);
this.viewer.moveUpdate (this.floatSecondsTotal);
if (this.transformManager.motion != null && !this.stopped) {
this.transformManager.motion = null;
this.viewer.finalizeTransformParameters ();
}this.resumeEval ();
return;
}

}, "~N");
$_M(c$, "doStepTransform", 
($fz = function () {
if (!Float.isNaN (this.matrixEnd.m00)) {
this.transformManager.getRotation (this.matrixStart);
this.matrixStartInv.invertM (this.matrixStart);
this.matrixStep.mul2 (this.matrixEnd, this.matrixStartInv);
this.aaTotal.setM (this.matrixStep);
this.aaStep.setAA (this.aaTotal);
this.aaStep.angle /= (this.totalSteps - this.iStep);
if (this.aaStep.angle == 0) this.matrixStep.setIdentity ();
 else this.matrixStep.setAA (this.aaStep);
this.matrixStep.mul (this.matrixStart);
}this.fStep = this.iStep / (this.totalSteps - 1);
if (this.center != null) this.transformManager.fixedRotationCenter.add (this.aaStepCenter);
if (this.navCenter != null && this.transformManager.mode == 1) {
var pt = J.util.P3.newP (this.transformManager.navigationCenter);
pt.add (this.aaStepNavCenter);
this.transformManager.setNavigatePt (pt);
}this.setValues (this.matrixStep, null, null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "doFinalTransform", 
($fz = function () {
this.fStep = -1;
this.setValues (this.matrixEnd, this.center, this.navCenter);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setValues", 
($fz = function (m, center, navCenter) {
this.transformManager.setAll (center, m, navCenter, this.getVal (this.zoom), this.getVal (this.xTrans), this.getVal (this.yTrans), this.getVal (this.rotationRadius), this.getVal (this.pixelScale), this.getVal (this.navDepth), this.getVal (this.xNav), this.getVal (this.yNav), this.getVal (this.cameraDepth), this.getVal (this.cameraX), this.getVal (this.cameraY));
}, $fz.isPrivate = true, $fz), "J.util.Matrix3f,J.util.P3,J.util.P3");
$_M(c$, "getVal", 
($fz = function (s) {
return (s == null ? NaN : s.getVal (this.fStep));
}, $fz.isPrivate = true, $fz), "J.thread.MoveToThread.Slider");
$_M(c$, "interrupt", 
function () {
this.doEndMove = false;
Clazz.superCall (this, J.thread.MoveToThread, "interrupt", []);
});
c$.$MoveToThread$Slider$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.start = 0;
this.delta = 0;
this.value = 0;
Clazz.instantialize (this, arguments);
}, J.thread.MoveToThread, "Slider");
Clazz.makeConstructor (c$, 
function (a, b) {
this.start = a;
this.value = b;
this.delta = b - a;
}, "~N,~N");
$_M(c$, "getVal", 
function (a) {
return (a < 0 ? this.value : this.start + a * this.delta);
}, "~N");
c$ = Clazz.p0p ();
};
});
