Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.VibrationThread", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.transformManager = null;
Clazz.instantialize (this, arguments);
}, J.thread, "VibrationThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.thread.VibrationThread, []);
});
Clazz.overrideMethod (c$, "setManager", 
function (manager, viewer, options) {
this.transformManager = manager;
this.setViewer (viewer, "VibrationThread");
return 0;
}, "~O,J.viewer.Viewer,~O");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
var elapsed;
while (true) switch (mode) {
case -1:
this.lastRepaintTime = this.startTime = System.currentTimeMillis ();
this.viewer.startHoverWatcher (false);
mode = 0;
break;
case 0:
elapsed = (System.currentTimeMillis () - this.lastRepaintTime);
this.sleepTime = 33 - elapsed;
if (!this.runSleep (this.sleepTime, 1)) return;
mode = 1;
break;
case 1:
this.lastRepaintTime = System.currentTimeMillis ();
elapsed = (this.lastRepaintTime - this.startTime);
var t = (elapsed % this.transformManager.vibrationPeriodMs) / this.transformManager.vibrationPeriodMs;
this.transformManager.setVibrationT (t);
this.viewer.refresh (3, "VibrationThread:run()");
mode = (this.checkInterrupted () ? -2 : 0);
break;
case -2:
this.viewer.startHoverWatcher (true);
return;
}

}, "~N");
});
