Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread", "J.util.AxisAngle4f", "$.Matrix3f", "$.V3"], "J.thread.MoveToThread", ["java.lang.Float", "J.util.Logger", "$.P3"], function () {
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
this.zoom = 0;
this.xTrans = 0;
this.yTrans = 0;
this.navCenter = null;
this.xNav = 0;
this.yNav = 0;
this.navDepth = 0;
this.ptMoveToCenter = null;
this.startRotationRadius = 0;
this.targetPixelScale = 0;
this.totalSteps = 0;
this.startPixelScale = 0;
this.targetRotationRadius = 0;
this.fps = 0;
this.rotationRadiusDelta = 0;
this.pixelScaleDelta = 0;
this.zoomStart = 0;
this.zoomDelta = 0;
this.xTransStart = 0;
this.xTransDelta = 0;
this.yTransStart = 0;
this.yTransDelta = 0;
this.xNavTransStart = 0;
this.xNavTransDelta = 0;
this.yNavTransDelta = 0;
this.yNavTransStart = 0;
this.navDepthStart = 0;
this.navDepthDelta = 0;
this.frameTimeMillis = 0;
this.iStep = 0;
this.doEndMove = false;
this.floatSecondsTotal = 0;
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
function (transformManager, viewer) {
Clazz.superConstructor (this, J.thread.MoveToThread);
this.setViewer (viewer, "MoveToThread");
this.transformManager = transformManager;
}, "J.viewer.TransformManager,J.viewer.Viewer");
$_M(c$, "set", 
function (floatSecondsTotal, center, end, zoom, xTrans, yTrans, newRotationRadius, navCenter, xNav, yNav, navDepth) {
this.center = center;
this.matrixEnd.setM (end);
this.zoom = zoom;
this.xTrans = xTrans;
this.yTrans = yTrans;
this.navCenter = navCenter;
this.xNav = xNav;
this.yNav = yNav;
this.navDepth = navDepth;
this.ptMoveToCenter = (center == null ? this.transformManager.fixedRotationCenter : center);
this.startRotationRadius = this.transformManager.modelRadius;
this.targetRotationRadius = (center == null || Float.isNaN (newRotationRadius) ? this.transformManager.modelRadius : newRotationRadius <= 0 ? this.viewer.calcRotationRadius (center) : newRotationRadius);
this.startPixelScale = this.transformManager.scaleDefaultPixelsPerAngstrom;
this.targetPixelScale = (center == null ? this.startPixelScale : this.transformManager.defaultScaleToScreen (this.targetRotationRadius));
if (Float.isNaN (zoom)) zoom = this.transformManager.zoomPercent;
this.transformManager.getRotation (this.matrixStart);
this.matrixStartInv.invertM (this.matrixStart);
this.matrixStep.mul2 (this.matrixEnd, this.matrixStartInv);
this.aaTotal.setM (this.matrixStep);
this.fps = 30;
this.floatSecondsTotal = floatSecondsTotal;
this.totalSteps = Clazz.floatToInt (floatSecondsTotal * this.fps);
if (this.totalSteps == 0) return 0;
this.frameTimeMillis = Clazz.doubleToInt (1000 / this.fps);
this.targetTime = System.currentTimeMillis ();
this.zoomStart = this.transformManager.zoomPercent;
this.zoomDelta = zoom - this.zoomStart;
this.xTransStart = this.transformManager.getTranslationXPercent ();
this.xTransDelta = xTrans - this.xTransStart;
this.yTransStart = this.transformManager.getTranslationYPercent ();
this.yTransDelta = yTrans - this.yTransStart;
this.aaStepCenter.setT (this.ptMoveToCenter);
this.aaStepCenter.sub (this.transformManager.fixedRotationCenter);
this.aaStepCenter.scale (1 / this.totalSteps);
this.pixelScaleDelta = (this.targetPixelScale - this.startPixelScale);
this.rotationRadiusDelta = (this.targetRotationRadius - this.startRotationRadius);
if (navCenter != null && this.transformManager.mode == 1) {
this.aaStepNavCenter.setT (navCenter);
this.aaStepNavCenter.sub (this.transformManager.navigationCenter);
this.aaStepNavCenter.scale (1 / this.totalSteps);
}var xNavTransStart = this.transformManager.getNavigationOffsetPercent ('X');
this.xNavTransDelta = xNav - xNavTransStart;
this.yNavTransStart = this.transformManager.getNavigationOffsetPercent ('Y');
this.yNavTransDelta = yNav - this.yNavTransStart;
var navDepthStart = this.transformManager.getNavigationDepthPercent ();
this.navDepthDelta = navDepth - navDepthStart;
return this.totalSteps;
}, "~N,J.util.P3,J.util.Matrix3f,~N,~N,~N,~N,J.util.P3,~N,~N,~N");
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
}if (doRender) this.viewer.requestRepaintAndWait ();
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
}var fStep = this.iStep / (this.totalSteps - 1);
this.transformManager.modelRadius = this.startRotationRadius + this.rotationRadiusDelta * fStep;
this.transformManager.scaleDefaultPixelsPerAngstrom = this.startPixelScale + this.pixelScaleDelta * fStep;
if (!Float.isNaN (this.xTrans)) {
this.transformManager.zoomToPercent (this.zoomStart + this.zoomDelta * fStep);
this.transformManager.translateToPercent ('x', this.xTransStart + this.xTransDelta * fStep);
this.transformManager.translateToPercent ('y', this.yTransStart + this.yTransDelta * fStep);
}this.transformManager.setRotation (this.matrixStep);
if (this.center != null) this.transformManager.fixedRotationCenter.add (this.aaStepCenter);
if (this.navCenter != null && this.transformManager.mode == 1) {
var pt = J.util.P3.newP (this.transformManager.navigationCenter);
pt.add (this.aaStepNavCenter);
this.transformManager.setNavigatePt (pt);
if (!Float.isNaN (this.xNav) && !Float.isNaN (this.yNav)) this.transformManager.navTranslatePercentOrTo (0, this.xNavTransStart + this.xNavTransDelta * fStep, this.yNavTransStart + this.yNavTransDelta * fStep);
if (!Float.isNaN (this.navDepth)) this.transformManager.setNavigationDepthPercent (this.navDepthStart + this.navDepthDelta * fStep);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "doFinalTransform", 
($fz = function () {
this.transformManager.setRotationRadius (this.targetRotationRadius, true);
this.transformManager.scaleDefaultPixelsPerAngstrom = this.targetPixelScale;
if (this.center != null) this.transformManager.moveRotationCenter (this.center, !this.transformManager.windowCentered);
if (!Float.isNaN (this.xTrans)) {
this.transformManager.zoomToPercent (this.zoom);
this.transformManager.translateToPercent ('x', this.xTrans);
this.transformManager.translateToPercent ('y', this.yTrans);
}this.transformManager.setRotation (this.matrixEnd);
if (this.navCenter != null && this.transformManager.mode == 1) {
this.transformManager.navigationCenter.setT (this.navCenter);
if (!Float.isNaN (this.xNav) && !Float.isNaN (this.yNav)) this.transformManager.navTranslatePercentOrTo (0, this.xNav, this.yNav);
if (!Float.isNaN (this.navDepth)) this.transformManager.setNavigationDepthPercent (this.navDepth);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "interrupt", 
function () {
J.util.Logger.debug ("moveto thread interrupted!");
this.doEndMove = false;
Clazz.superCall (this, J.thread.MoveToThread, "interrupt", []);
});
});
