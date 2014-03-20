Clazz.declarePackage ("J.parallel");
Clazz.load (["J.api.JmolParallelProcessor", "J.script.ScriptFunction", "J.util.JmolList"], "J.parallel.ScriptParallelProcessor", ["java.util.concurrent.Executors", "J.parallel.ScriptProcess", "$.ScriptProcessRunnable", "J.util.Logger", "J.viewer.ShapeManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.counter = 0;
this.error = null;
this.lock = null;
this.processes = null;
Clazz.instantialize (this, arguments);
}, J.parallel, "ScriptParallelProcessor", J.script.ScriptFunction, J.api.JmolParallelProcessor);
Clazz.prepareFields (c$, function () {
this.lock =  new JavaObject ();
this.processes =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.parallel.ScriptParallelProcessor, []);
});
Clazz.overrideMethod (c$, "getExecutor", 
function () {
return java.util.concurrent.Executors.newCachedThreadPool ();
});
Clazz.overrideMethod (c$, "runAllProcesses", 
function (viewer) {
if (this.processes.size () == 0) return;
this.viewer = viewer;
var inParallel = !viewer.isParallel () && viewer.setParallel (true);
var vShapeManagers =  new J.util.JmolList ();
this.error = null;
this.counter = 0;
if (J.util.Logger.debugging) J.util.Logger.debug ("running " + this.processes.size () + " processes on " + J.viewer.Viewer.nProcessors + " processesors inParallel=" + inParallel);
this.counter = this.processes.size ();
for (var i = this.processes.size (); --i >= 0; ) {
var shapeManager = null;
if (inParallel) {
shapeManager =  new J.viewer.ShapeManager (viewer, viewer.getModelSet ());
vShapeManagers.addLast (shapeManager);
}this.runProcess (this.processes.remove (0), shapeManager);
}
{
while (this.counter > 0) {
try {
this.lock.wait ();
} catch (e) {
if (Clazz.exceptionOf (e, InterruptedException)) {
} else {
throw e;
}
}
if (this.error != null) throw this.error;
}
}this.mergeResults (vShapeManagers);
viewer.setParallel (false);
}, "J.viewer.Viewer");
$_M(c$, "mergeResults", 
function (vShapeManagers) {
try {
for (var i = 0; i < vShapeManagers.size (); i++) this.viewer.mergeShapes (vShapeManagers.get (i).getShapes ());

} catch (e) {
if (Clazz.exceptionOf (e, Error)) {
throw e;
} else {
throw e;
}
} finally {
this.counter = -1;
vShapeManagers = null;
}
}, "J.util.JmolList");
$_M(c$, "clearShapeManager", 
function (er) {
{
this.error = er;
this.notifyAll ();
}}, "Error");
Clazz.overrideMethod (c$, "addProcess", 
function (name, context) {
this.processes.addLast ( new J.parallel.ScriptProcess (name, context));
}, "~S,J.script.ScriptContext");
$_M(c$, "runProcess", 
($fz = function (process, shapeManager) {
var r =  new J.parallel.ScriptProcessRunnable (this, process, this.lock, shapeManager);
var exec = (shapeManager == null ? null : this.viewer.getExecutor ());
if (exec != null) {
exec.execute (r);
} else {
r.run ();
}}, $fz.isPrivate = true, $fz), "J.parallel.ScriptProcess,J.viewer.ShapeManager");
$_M(c$, "eval", 
function (context, shapeManager) {
this.viewer.evalParallel (context, shapeManager);
}, "J.script.ScriptContext,J.viewer.ShapeManager");
});
