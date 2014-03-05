Clazz.declarePackage ("J.minimize");
Clazz.load (["J.thread.JmolThread"], "J.minimize.MinimizationThread", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.minimizer = null;
Clazz.instantialize (this, arguments);
}, J.minimize, "MinimizationThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.minimize.MinimizationThread, []);
});
Clazz.overrideMethod (c$, "setManager", 
function (manager, viewer, options) {
this.minimizer = manager;
this.setViewer (viewer, "MinimizationThread");
return 0;
}, "~O,J.viewer.Viewer,~O");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
this.lastRepaintTime = this.startTime;
if (!this.minimizer.startMinimization ()) return;
this.viewer.startHoverWatcher (false);
mode = 0;
break;
case 0:
if (!this.minimizer.minimizationOn () || this.checkInterrupted ()) {
mode = -2;
break;
}this.currentTime = System.currentTimeMillis ();
var elapsed = (this.currentTime - this.lastRepaintTime);
var sleepTime = 33 - elapsed;
if (!this.runSleep (sleepTime, 1)) return;
mode = 1;
break;
case 1:
this.lastRepaintTime = this.currentTime = System.currentTimeMillis ();
mode = (this.minimizer.stepMinimization () ? 0 : -2);
break;
case -2:
this.minimizer.endMinimization ();
this.viewer.startHoverWatcher (true);
return;
}

}, "~N");
Clazz.overrideMethod (c$, "oops", 
function (e) {
if (this.minimizer.minimizationOn ()) J.util.Logger.error (e.toString ());
}, "Exception");
});
