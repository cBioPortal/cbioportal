Clazz.declarePackage ("J.util");
Clazz.load (["J.util.LoggerInterface"], "J.util.DefaultLogger", ["J.util.Logger"], function () {
c$ = Clazz.declareType (J.util, "DefaultLogger", null, J.util.LoggerInterface);
$_M(c$, "log", 
function (out, level, txt, e) {
if (out === System.err) System.out.flush ();
if ((out != null) && ((txt != null) || (e != null))) {
txt = (txt != null ? txt : "");
out.println ((J.util.Logger.logLevel () ? "[" + J.util.Logger.getLevel (level) + "] " : "") + txt + (e != null ? ": " + e.toString () : ""));
if (e != null) {
var elements = e.getStackTrace ();
if (elements != null) {
for (var i = 0; i < elements.length; i++) {
out.println (elements[i].getClassName () + " - " + elements[i].getLineNumber () + " - " + elements[i].getMethodName ());
}
}}}if (out === System.err) System.err.flush ();
}, "java.io.PrintStream,~N,~S,Throwable");
Clazz.overrideMethod (c$, "debug", 
function (txt) {
this.log (System.out, 5, txt, null);
}, "~S");
Clazz.overrideMethod (c$, "info", 
function (txt) {
this.log (System.out, 4, txt, null);
}, "~S");
Clazz.overrideMethod (c$, "warn", 
function (txt) {
this.log (System.out, 3, txt, null);
}, "~S");
Clazz.overrideMethod (c$, "warnEx", 
function (txt, e) {
this.log (System.out, 3, txt, e);
}, "~S,Throwable");
Clazz.overrideMethod (c$, "error", 
function (txt) {
this.log (System.err, 2, txt, null);
}, "~S");
Clazz.overrideMethod (c$, "errorEx", 
function (txt, e) {
this.log (System.err, 2, txt, e);
}, "~S,Throwable");
Clazz.overrideMethod (c$, "fatal", 
function (txt) {
this.log (System.err, 1, txt, null);
}, "~S");
Clazz.overrideMethod (c$, "fatalEx", 
function (txt, e) {
this.log (System.err, 1, txt, e);
}, "~S,Throwable");
});
