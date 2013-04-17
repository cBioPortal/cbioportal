Clazz.declarePackage ("java.net");
c$ = Clazz.decorateAsClass (function () {
this.path = null;
this.query = null;
this.ref = null;
Clazz.instantialize (this, arguments);
}, java.net, "Parts");
Clazz.makeConstructor (c$, 
function (file) {
var ind = file.indexOf ('#');
this.ref = ind < 0 ? null : file.substring (ind + 1);
file = ind < 0 ? file : file.substring (0, ind);
var q = file.lastIndexOf ('?');
if (q != -1) {
this.query = file.substring (q + 1);
this.path = file.substring (0, q);
} else {
this.path = file;
}}, "~S");
$_M(c$, "getPath", 
function () {
return this.path;
});
$_M(c$, "getQuery", 
function () {
return this.query;
});
$_M(c$, "getRef", 
function () {
return this.ref;
});
