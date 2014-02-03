Clazz.declarePackage ("J.script");
Clazz.load (["J.thread.JmolThread"], "J.script.ScriptQueueThread", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.scriptManager = null;
this.startedByCommandThread = false;
this.pt = 0;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptQueueThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (scriptManager, viewer, startedByCommandThread, pt) {
Clazz.superConstructor (this, J.script.ScriptQueueThread);
this.setViewer (viewer, "QueueThread" + pt);
this.scriptManager = scriptManager;
this.viewer = viewer;
this.startedByCommandThread = startedByCommandThread;
this.pt = pt;
}, "J.api.JmolScriptManager,J.viewer.Viewer,~B,~N");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
mode = 0;
break;
case 0:
if (this.stopped || this.scriptManager.getScriptQueue ().size () == 0) {
mode = -2;
break;
}if (!this.runNextScript () && !this.runSleep (100, 0)) return;
break;
case -2:
this.scriptManager.queueThreadFinished (this.pt);
return;
}

}, "~N");
$_M(c$, "runNextScript", 
($fz = function () {
var queue = this.scriptManager.getScriptQueue ();
if (queue.size () == 0) return false;
var scriptItem = this.scriptManager.getScriptItem (false, this.startedByCommandThread);
if (scriptItem == null) return false;
var script = scriptItem.get (0);
var statusList = scriptItem.get (1);
var returnType = scriptItem.get (2);
var isScriptFile = (scriptItem.get (3)).booleanValue ();
var isQuiet = (scriptItem.get (4)).booleanValue ();
if (J.util.Logger.debugging) {
J.util.Logger.debug ("Queue[" + this.pt + "][" + queue.size () + "] scripts; running: " + script);
}queue.remove (0);
this.viewer.evalStringWaitStatusQueued (returnType, script, statusList, isScriptFile, isQuiet, true);
if (queue.size () == 0) {
return false;
}return true;
}, $fz.isPrivate = true, $fz));
});
