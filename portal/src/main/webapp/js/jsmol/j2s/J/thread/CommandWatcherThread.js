Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.CommandWatcherThread", ["java.lang.Thread", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.scriptManager = null;
Clazz.instantialize (this, arguments);
}, J.thread, "CommandWatcherThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (viewer, scriptManager) {
Clazz.superConstructor (this, J.thread.CommandWatcherThread);
this.setViewer (viewer, "CommmandWatcherThread");
this.scriptManager = scriptManager;
}, "J.viewer.Viewer,J.viewer.ScriptManager");
Clazz.overrideMethod (c$, "run", 
function () {
Thread.currentThread ().setPriority (1);
while (!this.stopped) {
try {
Thread.sleep (50);
if (!this.stopped) {
this.scriptManager.runScriptNow ();
}} catch (e$$) {
if (Clazz.exceptionOf (e$$, InterruptedException)) {
var ie = e$$;
{
J.util.Logger.warn ("CommandWatcher InterruptedException! " + this);
break;
}
} else if (Clazz.exceptionOf (e$$, Exception)) {
var ie = e$$;
{
var s = "script processing ERROR:\n\n" + ie.toString ();
for (var i = 0; i < ie.getStackTrace ().length; i++) {
s += "\n" + ie.getStackTrace ()[i].toString ();
}
J.util.Logger.warn ("CommandWatcher Exception! " + s);
break;
}
} else {
throw e$$;
}
}
}
});
Clazz.overrideMethod (c$, "run1", 
function (mode) {
}, "~N");
Clazz.defineStatics (c$,
"commandDelay", 50);
});
