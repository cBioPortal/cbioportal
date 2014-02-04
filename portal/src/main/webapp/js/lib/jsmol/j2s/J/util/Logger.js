Clazz.declarePackage ("J.util");
Clazz.load (["java.util.Hashtable", "J.util.DefaultLogger"], "J.util.Logger", ["java.lang.Long"], function () {
c$ = Clazz.declareType (J.util, "Logger");
c$.getProperty = $_M(c$, "getProperty", 
($fz = function (level, defaultValue) {
try {
var property = System.getProperty ("jmol.logger." + level, null);
if (property != null) {
return (property.equalsIgnoreCase ("true"));
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return defaultValue;
}, $fz.isPrivate = true, $fz), "~S,~B");
c$.setLogger = $_M(c$, "setLogger", 
function (logger) {
($t$ = J.util.Logger._logger = logger, J.util.Logger.prototype._logger = J.util.Logger._logger, $t$);
($t$ = J.util.Logger.debugging = J.util.Logger.isActiveLevel (5) || J.util.Logger.isActiveLevel (6), J.util.Logger.prototype.debugging = J.util.Logger.debugging, $t$);
($t$ = J.util.Logger.debuggingHigh = (J.util.Logger.debugging && J.util.Logger._activeLevels[6]), J.util.Logger.prototype.debuggingHigh = J.util.Logger.debuggingHigh, $t$);
}, "J.util.LoggerInterface");
c$.isActiveLevel = $_M(c$, "isActiveLevel", 
function (level) {
return J.util.Logger._logger != null && level >= 0 && level < 7 && J.util.Logger._activeLevels[level];
}, "~N");
c$.setActiveLevel = $_M(c$, "setActiveLevel", 
function (level, active) {
if (level < 0) level = 0;
if (level >= 7) level = 6;
J.util.Logger._activeLevels[level] = active;
($t$ = J.util.Logger.debugging = J.util.Logger.isActiveLevel (5) || J.util.Logger.isActiveLevel (6), J.util.Logger.prototype.debugging = J.util.Logger.debugging, $t$);
($t$ = J.util.Logger.debuggingHigh = (J.util.Logger.debugging && J.util.Logger._activeLevels[6]), J.util.Logger.prototype.debuggingHigh = J.util.Logger.debuggingHigh, $t$);
}, "~N,~B");
c$.setLogLevel = $_M(c$, "setLogLevel", 
function (level) {
for (var i = 7; --i >= 0; ) J.util.Logger.setActiveLevel (i, i <= level);

}, "~N");
c$.getLevel = $_M(c$, "getLevel", 
function (level) {
switch (level) {
case 6:
return "DEBUGHIGH";
case 5:
return "DEBUG";
case 4:
return "INFO";
case 3:
return "WARN";
case 2:
return "ERROR";
case 1:
return "FATAL";
}
return "????";
}, "~N");
c$.logLevel = $_M(c$, "logLevel", 
function () {
return J.util.Logger._logLevel;
});
c$.doLogLevel = $_M(c$, "doLogLevel", 
function (log) {
($t$ = J.util.Logger._logLevel = log, J.util.Logger.prototype._logLevel = J.util.Logger._logLevel, $t$);
}, "~B");
c$.debug = $_M(c$, "debug", 
function (txt) {
if (!J.util.Logger.debugging) return;
try {
J.util.Logger._logger.debug (txt);
} catch (t) {
}
}, "~S");
c$.info = $_M(c$, "info", 
function (txt) {
try {
if (J.util.Logger.isActiveLevel (4)) {
J.util.Logger._logger.info (txt);
}} catch (t) {
}
}, "~S");
c$.warn = $_M(c$, "warn", 
function (txt) {
try {
if (J.util.Logger.isActiveLevel (3)) {
J.util.Logger._logger.warn (txt);
}} catch (t) {
}
}, "~S");
c$.warnEx = $_M(c$, "warnEx", 
function (txt, e) {
try {
if (J.util.Logger.isActiveLevel (3)) {
J.util.Logger._logger.warnEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.error = $_M(c$, "error", 
function (txt) {
try {
if (J.util.Logger.isActiveLevel (2)) {
J.util.Logger._logger.error (txt);
}} catch (t) {
}
}, "~S");
c$.errorEx = $_M(c$, "errorEx", 
function (txt, e) {
try {
if (J.util.Logger.isActiveLevel (2)) {
J.util.Logger._logger.errorEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.getLogLevel = $_M(c$, "getLogLevel", 
function () {
for (var i = 7; --i >= 0; ) if (J.util.Logger.isActiveLevel (i)) return i;

return 0;
});
c$.fatal = $_M(c$, "fatal", 
function (txt) {
try {
if (J.util.Logger.isActiveLevel (1)) {
J.util.Logger._logger.fatal (txt);
}} catch (t) {
}
}, "~S");
c$.fatalEx = $_M(c$, "fatalEx", 
function (txt, e) {
try {
if (J.util.Logger.isActiveLevel (1)) {
J.util.Logger._logger.fatalEx (txt, e);
}} catch (t) {
}
}, "~S,Throwable");
c$.startTimer = $_M(c$, "startTimer", 
function (msg) {
if (msg != null) J.util.Logger.htTiming.put (msg, Long.$valueOf (System.currentTimeMillis ()));
}, "~S");
c$.getTimerMsg = $_M(c$, "getTimerMsg", 
function (msg, time) {
if (time == 0) time = J.util.Logger.getTimeFrom (msg);
return "Time for " + msg + ": " + (time) + " ms";
}, "~S,~N");
c$.getTimeFrom = $_M(c$, "getTimeFrom", 
($fz = function (msg) {
var t;
return (msg == null || (t = J.util.Logger.htTiming.get (msg)) == null ? -1 : (System.currentTimeMillis () - t.longValue ()));
}, $fz.isPrivate = true, $fz), "~S");
c$.checkTimer = $_M(c$, "checkTimer", 
function (msg, andReset) {
var time = J.util.Logger.getTimeFrom (msg);
if (time >= 0 && !msg.startsWith ("(")) J.util.Logger.info (J.util.Logger.getTimerMsg (msg, time));
if (andReset) J.util.Logger.startTimer (msg);
return time;
}, "~S,~B");
c$.checkMemory = $_M(c$, "checkMemory", 
function () {
var bTotal = 0;
var bFree = 0;
var bMax = 0;
{
}J.util.Logger.info ("Memory: Total-Free=" + (bTotal - bFree) + "; Total=" + bTotal + "; Free=" + bFree + "; Max=" + bMax);
});
c$._logger = c$.prototype._logger =  new J.util.DefaultLogger ();
Clazz.defineStatics (c$,
"LEVEL_FATAL", 1,
"LEVEL_ERROR", 2,
"LEVEL_WARN", 3,
"LEVEL_INFO", 4,
"LEVEL_DEBUG", 5,
"LEVEL_DEBUGHIGH", 6,
"LEVEL_MAX", 7,
"_activeLevels",  Clazz.newBooleanArray (7, false),
"_logLevel", false,
"debugging", false,
"debuggingHigh", false);
{
J.util.Logger._activeLevels[6] = J.util.Logger.getProperty ("debugHigh", false);
J.util.Logger._activeLevels[5] = J.util.Logger.getProperty ("debug", false);
J.util.Logger._activeLevels[4] = J.util.Logger.getProperty ("info", true);
J.util.Logger._activeLevels[3] = J.util.Logger.getProperty ("warn", true);
J.util.Logger._activeLevels[2] = J.util.Logger.getProperty ("error", true);
J.util.Logger._activeLevels[1] = J.util.Logger.getProperty ("fatal", true);
($t$ = J.util.Logger._logLevel = J.util.Logger.getProperty ("logLevel", false), J.util.Logger.prototype._logLevel = J.util.Logger._logLevel, $t$);
($t$ = J.util.Logger.debugging = (J.util.Logger._logger != null && (J.util.Logger._activeLevels[5] || J.util.Logger._activeLevels[6])), J.util.Logger.prototype.debugging = J.util.Logger.debugging, $t$);
($t$ = J.util.Logger.debuggingHigh = (J.util.Logger.debugging && J.util.Logger._activeLevels[6]), J.util.Logger.prototype.debuggingHigh = J.util.Logger.debuggingHigh, $t$);
}c$.htTiming = c$.prototype.htTiming =  new java.util.Hashtable ();
});
