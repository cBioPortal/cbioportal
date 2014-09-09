Clazz.declarePackage ("JS");
Clazz.load (["J.thread.JmolThread"], "JS.FileLoadThread", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.fileName = null;
this.cacheName = null;
this.key = null;
Clazz.instantialize (this, arguments);
}, JS, "FileLoadThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (eval, vwr, fileName, key, cacheName) {
Clazz.superConstructor (this, JS.FileLoadThread, []);
this.setViewer (vwr, "FileLoadThread");
this.fileName = fileName;
this.key = key;
this.cacheName = cacheName;
this.setEval (eval);
this.sc.pc--;
}, "J.api.JmolScriptEvaluator,JV.Viewer,~S,~S,~S");
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
return Jmol._loadFileAsynchronously(this, this.vwr.html5Applet, this.fileName, null);
}break;
case -2:
this.resumeEval ();
return;
}

}, "~N");
Clazz.defineMethod (c$, "setData", 
function (fileName, data, myData) {
if (fileName != null) this.sc.parentContext.htFileCache.put (this.key, this.cacheName = this.cacheName.substring (0, this.cacheName.lastIndexOf ("_") + 1) + fileName);
this.vwr.cachePut (this.cacheName, data);
this.run1 (-2);
}, "~S,~O,~O");
});
