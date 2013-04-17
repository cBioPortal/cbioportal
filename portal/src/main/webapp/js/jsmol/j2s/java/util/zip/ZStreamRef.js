Clazz.declarePackage ("java.util.zip");
c$ = Clazz.decorateAsClass (function () {
this.$address = 0;
Clazz.instantialize (this, arguments);
}, java.util.zip, "ZStreamRef");
Clazz.makeConstructor (c$, 
function (address) {
this.$address = address;
}, "~N");
$_M(c$, "address", 
function () {
return this.$address;
});
$_M(c$, "clear", 
function () {
this.$address = 0;
});
