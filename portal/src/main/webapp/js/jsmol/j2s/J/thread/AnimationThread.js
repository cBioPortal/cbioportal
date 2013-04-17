Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.AnimationThread", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.animationManager = null;
this.framePointer = 0;
this.framePointer2 = 0;
this.intThread = 0;
this.isFirst = false;
Clazz.instantialize (this, arguments);
}, J.thread, "AnimationThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (animationManager, viewer, framePointer, framePointer2, intAnimThread) {
Clazz.superConstructor (this, J.thread.AnimationThread);
this.setViewer (viewer, "AnimationThread");
this.animationManager = animationManager;
this.framePointer = framePointer;
this.framePointer2 = framePointer2;
this.intThread = intAnimThread;
viewer.startHoverWatcher (false);
}, "J.viewer.AnimationManager,J.viewer.Viewer,~N,~N,~N");
$_M(c$, "interrupt", 
function () {
if (this.stopped) return;
this.stopped = true;
J.util.Logger.debug ("animation thread interrupted!");
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
while (true) switch (mode) {
case -1:
if (J.util.Logger.debugging) J.util.Logger.debug ("animation thread " + this.intThread + " running");
this.viewer.requestRepaintAndWait ();
this.viewer.startHoverWatcher (false);
this.isFirst = true;
mode = 0;
break;
case 0:
if (this.checkInterrupted () || !this.animationManager.animationOn) {
mode = -2;
break;
}if (this.animationManager.currentFrameIs (this.framePointer)) {
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
this.targetTime += Clazz.floatToInt ((1000 / this.animationManager.animationFps) + this.viewer.getFrameDelayMs (this.animationManager.getCurrentFrame ()));
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
J.util.Logger.debug ("animation thread " + this.intThread + " exiting");
this.animationManager.stopThread (false);
return;
}

}, "~N");
});
