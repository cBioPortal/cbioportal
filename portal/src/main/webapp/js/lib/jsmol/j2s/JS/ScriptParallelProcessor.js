Clazz.declarePackage ("JS");
Clazz.load (["J.api.JmolParallelProcessor", "JS.ScriptFunction", "JU.Lst"], "JS.ScriptParallelProcessor", ["java.util.concurrent.Executors", "JS.ScriptProcess", "$.ScriptProcessRunnable", "JU.Logger", "JV.ShapeManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.counter = 0;
this.error = null;
this.lock = null;
this.processes = null;
Clazz.instantialize (this, arguments);
}, JS, "ScriptParallelProcessor", JS.ScriptFunction, J.api.JmolParallelProcessor);
Clazz.prepareFields (c$, function () {
this.lock =  new Clazz._O ();
this.processes =  new JU.Lst ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, JS.ScriptParallelProcessor, []);
});
Clazz.overrideMethod (c$, "getExecutor", 
function () {
return java.util.concurrent.Executors.newCachedThreadPool ();
});
Clazz.overrideMethod (c$, "runAllProcesses", 
function (vwr) {
if (this.processes.size () == 0) return;
this.vwr = vwr;
var inParallel = !vwr.isParallel () && vwr.setParallel (true);
var vShapeManagers =  new JU.Lst ();
this.error = null;
this.counter = 0;
if (JU.Logger.debugging) JU.Logger.debug ("running " + this.processes.size () + " processes on " + JV.Viewer.nProcessors + " processesors inParallel=" + inParallel);
this.counter = this.processes.size ();
for (var i = this.processes.size (); --i >= 0; ) {
var sm = null;
if (inParallel) {
sm =  new JV.ShapeManager (vwr);
sm.setParallel ();
vShapeManagers.addLast (sm);
}this.runProcess (this.processes.remove (0), sm);
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
vwr.setParallel (false);
}, "JV.Viewer");
Clazz.defineMethod (c$, "mergeResults", 
function (vShapeManagers) {
try {
for (var i = 0; i < vShapeManagers.size (); i++) this.vwr.shm.mergeShapes (vShapeManagers.get (i).getShapes ());

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
}, "JU.Lst");
Clazz.defineMethod (c$, "clearShapeManager", 
function (er) {
{
this.error = er;
this.notifyAll ();
}}, "Error");
Clazz.overrideMethod (c$, "addProcess", 
function (name, context) {
this.processes.addLast ( new JS.ScriptProcess (name, context));
}, "~S,JS.ScriptContext");
Clazz.defineMethod (c$, "runProcess", 
 function (process, shapeManager) {
var r =  new JS.ScriptProcessRunnable (this, process, this.lock, shapeManager);
var exec = (shapeManager == null ? null : this.vwr.getExecutor ());
if (exec != null) {
exec.execute (r);
} else {
r.run ();
}}, "JS.ScriptProcess,JV.ShapeManager");
Clazz.defineMethod (c$, "eval", 
function (context, shapeManager) {
this.vwr.evalParallel (context, shapeManager);
}, "JS.ScriptContext,JV.ShapeManager");
});
