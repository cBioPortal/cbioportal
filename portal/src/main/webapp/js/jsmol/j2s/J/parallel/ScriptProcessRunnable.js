Clazz.declarePackage ("J.parallel");
Clazz.load (null, "J.parallel.ScriptProcessRunnable", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.parallelProcessor = null;
this.process = null;
this.processLock = null;
this.shapeManager = null;
Clazz.instantialize (this, arguments);
}, J.parallel, "ScriptProcessRunnable", null, Runnable);
Clazz.makeConstructor (c$, 
function (parallelProcessor, process, lock, shapeManager) {
this.parallelProcessor = parallelProcessor;
this.process = process;
this.processLock = lock;
this.shapeManager = shapeManager;
}, "J.parallel.ScriptParallelProcessor,J.parallel.ScriptProcess,~O,J.viewer.ShapeManager");
Clazz.overrideMethod (c$, "run", 
function () {
try {
if (this.parallelProcessor.error == null) {
if (J.util.Logger.debugging) J.util.Logger.debug ("Running process " + this.process.processName + " " + this.process.context.pc + " - " + (this.process.context.pcEnd - 1));
this.parallelProcessor.eval (this.process.context, this.shapeManager);
if (J.util.Logger.debugging) J.util.Logger.debug ("Process " + this.process.processName + " complete");
}} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var e = e$$;
{
if (this.parallelProcessor.getTok () != 364558) e.printStackTrace ();
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.parallelProcessor.clearShapeManager (er);
}
} else {
throw e$$;
}
} finally {
{
--this.parallelProcessor.counter;
this.processLock.notifyAll ();
}}
});
});
