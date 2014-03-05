Clazz.declarePackage ("J.smiles");
Clazz.load (["java.lang.Exception"], "J.smiles.InvalidSmilesException", null, function () {
c$ = Clazz.declareType (J.smiles, "InvalidSmilesException", Exception);
c$.getLastError = $_M(c$, "getLastError", 
function () {
return J.smiles.InvalidSmilesException.lastError;
});
c$.setLastError = $_M(c$, "setLastError", 
function (message) {
($t$ = J.smiles.InvalidSmilesException.lastError = message, J.smiles.InvalidSmilesException.prototype.lastError = J.smiles.InvalidSmilesException.lastError, $t$);
}, "~S");
Clazz.makeConstructor (c$, 
function (message) {
Clazz.superConstructor (this, J.smiles.InvalidSmilesException, [message]);
($t$ = J.smiles.InvalidSmilesException.lastError = message, J.smiles.InvalidSmilesException.prototype.lastError = J.smiles.InvalidSmilesException.lastError, $t$);
}, "~S");
Clazz.makeConstructor (c$, 
function (cause) {
Clazz.superConstructor (this, J.smiles.InvalidSmilesException, [cause]);
($t$ = J.smiles.InvalidSmilesException.lastError = cause.toString (), J.smiles.InvalidSmilesException.prototype.lastError = J.smiles.InvalidSmilesException.lastError, $t$);
}, "Throwable");
Clazz.makeConstructor (c$, 
function (message, cause) {
Clazz.superConstructor (this, J.smiles.InvalidSmilesException, [message, cause]);
($t$ = J.smiles.InvalidSmilesException.lastError = message + "\n" + cause.getCause (), J.smiles.InvalidSmilesException.prototype.lastError = J.smiles.InvalidSmilesException.lastError, $t$);
}, "~S,Throwable");
Clazz.defineStatics (c$,
"lastError", null);
});
