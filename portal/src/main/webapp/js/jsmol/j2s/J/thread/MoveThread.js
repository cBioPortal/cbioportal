Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.MoveThread", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.transformManager = null;
this.floatSecondsTotal = 0;
this.iStep = 0;
this.timePerStep = 0;
this.totalSteps = 0;
this.radiansXStep = 0;
this.radiansYStep = 0;
this.radiansZStep = 0;
this.dRot = null;
this.dTrans = null;
this.dZoom = 0;
this.dSlab = 0;
this.zoomPercent0 = 0;
this.slab = 0;
this.transX = 0;
this.transY = 0;
this.transZ = 0;
Clazz.instantialize (this, arguments);
}, J.thread, "MoveThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (transformManager, viewer) {
Clazz.superConstructor (this, J.thread.MoveThread);
this.setViewer (viewer, "MoveThread");
this.transformManager = transformManager;
}, "J.viewer.TransformManager,J.viewer.Viewer");
$_M(c$, "set", 
function (dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps) {
this.dRot = dRot;
this.dTrans = dTrans;
this.dZoom = dZoom;
this.dSlab = dSlab;
this.floatSecondsTotal = floatSecondsTotal;
this.slab = this.transformManager.getSlabPercentSetting ();
this.transX = this.transformManager.getTranslationXPercent ();
this.transY = this.transformManager.getTranslationYPercent ();
this.transZ = this.transformManager.getTranslationZPercent ();
this.timePerStep = Clazz.doubleToInt (1000 / fps);
this.totalSteps = Clazz.floatToInt (fps * floatSecondsTotal);
if (this.totalSteps <= 0) this.totalSteps = 1;
var radiansPerDegreePerStep = (1 / 57.29577951308232 / this.totalSteps);
this.radiansXStep = radiansPerDegreePerStep * dRot.x;
this.radiansYStep = radiansPerDegreePerStep * dRot.y;
this.radiansZStep = radiansPerDegreePerStep * dRot.z;
this.zoomPercent0 = this.transformManager.zoomPercent;
this.iStep = 0;
}, "J.util.V3,~N,J.util.V3,~N,~N,~N");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
if (this.floatSecondsTotal > 0) this.viewer.setInMotion (true);
mode = 0;
break;
case 0:
if (this.stopped || ++this.iStep >= this.totalSteps) {
mode = -2;
break;
}if (this.dRot.x != 0) this.transformManager.rotateXRadians (this.radiansXStep, null);
if (this.dRot.y != 0) this.transformManager.rotateYRadians (this.radiansYStep, null);
if (this.dRot.z != 0) this.transformManager.rotateZRadians (this.radiansZStep);
if (this.dZoom != 0) this.transformManager.zoomToPercent (this.zoomPercent0 + this.dZoom * this.iStep / this.totalSteps);
if (this.dTrans.x != 0) this.transformManager.translateToPercent ('x', this.transX + this.dTrans.x * this.iStep / this.totalSteps);
if (this.dTrans.y != 0) this.transformManager.translateToPercent ('y', this.transY + this.dTrans.y * this.iStep / this.totalSteps);
if (this.dTrans.z != 0) this.transformManager.translateToPercent ('z', this.transZ + this.dTrans.z * this.iStep / this.totalSteps);
if (this.dSlab != 0) this.transformManager.slabToPercent (Clazz.doubleToInt (Math.floor (this.slab + this.dSlab * this.iStep / this.totalSteps)));
var timeSpent = (System.currentTimeMillis () - this.startTime);
var timeAllowed = this.iStep * this.timePerStep;
if (timeSpent < timeAllowed) {
this.viewer.requestRepaintAndWait ();
if (!this.isJS && !this.viewer.isScriptExecuting ()) {
mode = -2;
break;
}timeSpent = (System.currentTimeMillis () - this.startTime);
this.sleepTime = timeAllowed - timeSpent;
if (!this.runSleep (this.sleepTime, 0)) return;
}break;
case -2:
if (this.floatSecondsTotal > 0) this.viewer.setInMotion (false);
this.resumeEval ();
return;
}

}, "~N");
});
