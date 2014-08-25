Clazz.declarePackage ("JS");
Clazz.load (["java.lang.Exception"], "JS.InvalidSmilesException", null, function () {
c$ = Clazz.declareType (JS, "InvalidSmilesException", Exception);
c$.getLastError = Clazz.defineMethod (c$, "getLastError", 
function () {
return JS.InvalidSmilesException.lastError;
});
c$.clear = Clazz.defineMethod (c$, "clear", 
function () {
JS.InvalidSmilesException.lastError = null;
});
Clazz.makeConstructor (c$, 
function (message) {
Clazz.superConstructor (this, JS.InvalidSmilesException, [message]);
JS.InvalidSmilesException.lastError = message;
}, "~S");
Clazz.defineStatics (c$,
"lastError", null);
});
