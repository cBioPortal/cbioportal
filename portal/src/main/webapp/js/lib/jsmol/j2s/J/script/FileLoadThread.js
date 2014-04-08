Clazz.declarePackage ("J.script");
Clazz.load (["J.thread.JmolThread"], "J.script.FileLoadThread", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.fileName = null;
this.cacheName = null;
this.key = null;
Clazz.instantialize (this, arguments);
}, J.script, "FileLoadThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (eval, viewer, fileName, key, cacheName) {
Clazz.superConstructor (this, J.script.FileLoadThread, []);
this.setViewer (viewer, "FileLoadThread");
this.fileName = fileName;
this.key = key;
this.cacheName = cacheName;
this.setEval (eval);
this.sc.pc--;
}, "J.api.JmolScriptEvaluator,J.viewer.Viewer,~S,~S,~S");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
mode = 0;
break;
case 0:
if (this.stopped || this.eval.isStopped ()) {
mode = -2;
break;
}{
return Jmol._loadFileAsynchronously(this, this.viewer.applet, this.fileName);
}break;
case -2:
this.resumeEval ();
return;
}

}, "~N");
$_M(c$, "setData", 
function (fileName, data) {
if (fileName != null) this.sc.parentContext.htFileCache.put (this.key, this.cacheName = this.cacheName.substring (0, this.cacheName.lastIndexOf ("_") + 1) + fileName);
this.viewer.cachePut (this.cacheName, data);
this.run1 (-2);
}, "~S,~O");
});
