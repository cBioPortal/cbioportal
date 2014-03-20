Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.ModulationThread", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modT = 0;
this.modT2 = 0;
this.animationManager = null;
Clazz.instantialize (this, arguments);
}, J.thread, "ModulationThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.thread.ModulationThread, []);
});
Clazz.overrideMethod (c$, "setManager", 
function (manager, viewer, params) {
var options = params;
this.modT = options[0];
this.modT2 = options[1];
this.animationManager = manager;
this.setViewer (viewer, "ModulationThread");
viewer.startHoverWatcher (false);
return 0;
}, "~O,J.viewer.Viewer,~O");
$_M(c$, "interrupt", 
function () {
if (this.stopped) return;
this.stopped = true;
if (J.util.Logger.debugging) J.util.Logger.debug ("animation thread interrupted!");
this.animationManager.modulationPlay = false;
try {
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
Clazz.superCall (this, J.thread.ModulationThread, "interrupt", []);
});
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) {
switch (mode) {
case -1:
if (J.util.Logger.debugging) J.util.Logger.debug ("modulation thread running");
this.viewer.requestRepaintAndWait ("modulationThread");
this.viewer.startHoverWatcher (false);
mode = 0;
break;
case 0:
if (this.checkInterrupted () || !this.animationManager.modulationPlay || this.modT > this.modT2) {
mode = -2;
break;
}mode = 1;
break;
case 1:
this.viewer.setModulation (true, [this.modT++], 2147483647, true);
mode = 2;
break;
case 2:
this.targetTime += Clazz.floatToInt (1000 / this.animationManager.modulationFps);
mode = 3;
break;
case 3:
while (this.animationManager.modulationPlay && !this.checkInterrupted () && !this.viewer.getRefreshing ()) {
if (!this.runSleep (10, 3)) return;
}
this.viewer.refresh (1, "modulationThread");
this.sleepTime = (this.targetTime - (System.currentTimeMillis () - this.startTime));
if (!this.runSleep (this.sleepTime, 0)) return;
mode = 0;
break;
case -2:
if (J.util.Logger.debugging) J.util.Logger.debug ("modulation thread exiting");
this.animationManager.stopModulationThread ();
return;
}
}
}, "~N");
});
