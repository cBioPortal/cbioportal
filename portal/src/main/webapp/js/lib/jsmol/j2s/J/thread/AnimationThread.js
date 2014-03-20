Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.AnimationThread", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.animationManager = null;
this.framePointer1 = 0;
this.framePointer2 = 0;
this.intThread = 0;
this.isFirst = false;
Clazz.instantialize (this, arguments);
}, J.thread, "AnimationThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.thread.AnimationThread, []);
});
Clazz.overrideMethod (c$, "setManager", 
function (manager, viewer, params) {
var options = params;
this.framePointer1 = options[0];
this.framePointer2 = options[1];
this.intThread = options[2];
this.animationManager = manager;
this.setViewer (viewer, "AnimationThread");
viewer.startHoverWatcher (false);
return 0;
}, "~O,J.viewer.Viewer,~O");
$_M(c$, "interrupt", 
function () {
if (this.stopped) return;
this.stopped = true;
if (J.util.Logger.debugging) J.util.Logger.debug ("animation thread interrupted!");
try {
this.animationManager.setAnimationOn (false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
Clazz.superCall (this, J.thread.AnimationThread, "interrupt", []);
});
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) {
switch (mode) {
case -1:
if (J.util.Logger.debugging) J.util.Logger.debug ("animation thread " + this.intThread + " running");
this.viewer.requestRepaintAndWait ("animationThread");
this.viewer.startHoverWatcher (false);
this.isFirst = true;
mode = 0;
break;
case 0:
if (this.checkInterrupted () || !this.animationManager.animationOn) {
mode = -2;
break;
}if (this.animationManager.currentFrameIs (this.framePointer1)) {
this.targetTime += this.animationManager.firstFrameDelayMs;
this.sleepTime = (this.targetTime - (System.currentTimeMillis () - this.startTime));
if (!this.runSleep (this.sleepTime, 1)) return;
}mode = 1;
break;
case 1:
if (this.animationManager.currentFrameIs (this.framePointer2)) {
this.targetTime += this.animationManager.lastFrameDelayMs;
this.sleepTime = (this.targetTime - (System.currentTimeMillis () - this.startTime));
if (!this.runSleep (this.sleepTime, 2)) return;
}mode = 2;
break;
case 2:
if (!this.isFirst && this.animationManager.currentIsLast () && !this.animationManager.setAnimationNext ()) {
mode = -2;
break;
}this.isFirst = false;
this.targetTime += Clazz.floatToInt ((1000 / this.animationManager.animationFps) + this.viewer.getFrameDelayMs (this.animationManager.getCurrentModelIndex ()));
mode = 3;
break;
case 3:
while (this.animationManager.animationOn && !this.checkInterrupted () && !this.viewer.getRefreshing ()) {
if (!this.runSleep (10, 3)) return;
}
if (!this.viewer.getSpinOn ()) this.viewer.refresh (1, "animationThread");
this.sleepTime = (this.targetTime - (System.currentTimeMillis () - this.startTime));
if (!this.runSleep (this.sleepTime, 0)) return;
mode = 0;
break;
case -2:
if (J.util.Logger.debugging) J.util.Logger.debug ("animation thread " + this.intThread + " exiting");
this.animationManager.stopThread (false);
return;
}
}
}, "~N");
});
