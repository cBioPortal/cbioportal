Clazz.declarePackage ("J.script");
Clazz.load (["J.thread.JmolThread"], "J.script.ScriptDelayThread", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.millis = 0;
this.seconds = 0;
this.doPopPush = false;
this.isPauseDelay = false;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptDelayThread", J.thread.JmolThread);
Clazz.makeConstructor (c$, 
function (eval, viewer, millis) {
Clazz.superConstructor (this, J.script.ScriptDelayThread, []);
this.setViewer (viewer, "ScriptDelayThread");
this.millis = millis;
this.setEval (eval);
}, "J.api.JmolScriptEvaluator,J.viewer.Viewer,~N");
Clazz.overrideMethod (c$, "run1", 
function (mode) {
while (true) switch (mode) {
case -1:
var delayMax;
this.doPopPush = (this.millis > 0);
this.isPauseDelay = (this.millis == -100);
if (!this.doPopPush) this.millis = -this.millis;
 else if ((delayMax = this.viewer.getDelayMaximumMs ()) > 0 && this.millis > delayMax) this.millis = delayMax;
this.millis -= System.currentTimeMillis () - this.startTime;
if (this.isJS) {
this.seconds = 0;
} else {
this.seconds = Clazz.doubleToInt (this.millis / 1000);
this.millis -= this.seconds * 1000;
if (this.millis <= 0) this.millis = 1;
}if (this.doPopPush) this.viewer.popHoldRepaint ("scriptDelayThread INIT");
mode = 0;
break;
case 0:
if (this.stopped || this.eval.isStopped ()) {
mode = -2;
break;
}if (!this.runSleep (this.seconds-- > 0 ? 1000 : this.millis, -2)) return;
if (this.seconds < 0) this.millis = 0;
mode = (this.seconds > 0 || this.millis > 0 ? 0 : -2);
break;
case -2:
if (this.doPopPush) this.viewer.pushHoldRepaint ("delay FINISH");
if (this.isPauseDelay) this.eval.notifyResumeStatus ();
this.resumeEval ();
return;
}

}, "~N");
Clazz.defineStatics (c$,
"PAUSE_DELAY", -100);
});
