Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["java.net.URLConnection"], "J.awtjs2d.JmolURLConnection", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.bytesOut = null;
this.postOut = "";
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "JmolURLConnection", java.net.URLConnection);
$_M(c$, "doAjax", 
($fz = function () {
{
return Jmol._doAjax(this.url, this.postOut, this.bytesOut);
}}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "connect", 
function () {
});
$_M(c$, "outputBytes", 
function (bytes) {
this.bytesOut = bytes;
}, "~A");
$_M(c$, "outputString", 
function (post) {
this.postOut = post;
}, "~S");
$_M(c$, "getStringXBuilder", 
function () {
return this.doAjax ();
});
});
