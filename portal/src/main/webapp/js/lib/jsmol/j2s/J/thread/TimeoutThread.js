Clazz.declarePackage ("J.thread");
Clazz.load (["J.thread.JmolThread"], "J.thread.TimeoutThread", ["java.lang.Thread", "J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.script = null;
this.status = 0;
this.triggered = true;
this.timeouts = null;
Clazz.instantialize (this, arguments);
}, J.thread, "TimeoutThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (viewer, name, ms, script) {
Clazz.superConstructor (this, J.thread.TimeoutThread, []);
this.setViewer (viewer, name);
this.$name = name;
this.set (ms, script);
}, "J.viewer.Viewer,~S,~N,~S");
$_M(c$, "set", 
($fz = function (ms, script) {
this.sleepTime = ms;
if (script != null) this.script = script;
}, $fz.isPrivate = true, $fz), "~N,~S");
Clazz.overrideMethod (c$, "toString", 
function () {
return "timeout name=" + this.$name + " executions=" + this.status + " mSec=" + this.sleepTime + " secRemaining=" + (this.targetTime - System.currentTimeMillis ()) / 1000 + " script=" + this.script;
});
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) {
switch (mode) {
case -1:
if (!this.isJS) Thread.currentThread ().setPriority (1);
this.timeouts = this.viewer.getTimeouts ();
this.targetTime = System.currentTimeMillis () + Math.abs (this.sleepTime);
mode = 0;
break;
case 0:
if (this.checkInterrupted () || this.script == null || this.script.length == 0) return;
if (!this.runSleep (26, 1)) return;
mode = 1;
break;
case 1:
mode = (System.currentTimeMillis () < this.targetTime ? 0 : 2);
break;
case 2:
this.currentTime = System.currentTimeMillis ();
if (this.timeouts.get (this.$name) == null) return;
this.status++;
var continuing = (this.sleepTime < 0);
if (continuing) this.targetTime = System.currentTimeMillis () + Math.abs (this.sleepTime);
 else this.timeouts.remove (this.$name);
if (this.triggered) {
this.triggered = false;
if (this.$name.equals ("_SET_IN_MOTION_")) {
this.viewer.checkInMotion (2);
} else {
this.viewer.evalStringQuiet ((continuing ? this.script + ";\ntimeout ID \"" + this.$name + "\";" : this.script));
}}mode = (continuing ? 0 : -2);
break;
case -2:
this.timeouts.remove (this.$name);
return;
}
}
}, "~N");
c$.clear = $_M(c$, "clear", 
function (timeouts) {
for (var o, $o = timeouts.values ().iterator (); $o.hasNext () && ((o = $o.next ()) || true);) {
var t = o;
if (!t.script.equals ("exitJmol")) t.interrupt ();
}
timeouts.clear ();
}, "java.util.Map");
c$.setTimeout = $_M(c$, "setTimeout", 
function (viewer, timeouts, name, mSec, script) {
var t = timeouts.get (name);
if (mSec == 0) {
if (t != null) {
t.interrupt ();
timeouts.remove (name);
}return;
}if (t != null) {
t.set (mSec, script);
return;
}t =  new J.thread.TimeoutThread (viewer, name, mSec, script);
timeouts.put (name, t);
t.start ();
}, "J.viewer.Viewer,java.util.Map,~S,~N,~S");
c$.trigger = $_M(c$, "trigger", 
function (timeouts, name) {
var t = timeouts.get (name);
if (t != null) t.triggered = (t.sleepTime < 0);
}, "java.util.Map,~S");
c$.showTimeout = $_M(c$, "showTimeout", 
function (timeouts, name) {
var sb =  new J.util.SB ();
if (timeouts != null) {
for (var o, $o = timeouts.values ().iterator (); $o.hasNext () && ((o = $o.next ()) || true);) {
var t = o;
if (name == null || t.$name.equalsIgnoreCase (name)) sb.append (t.toString ()).append ("\n");
}
}return (sb.length () > 0 ? sb.toString () : "<no timeouts set>");
}, "java.util.Map,~S");
});
