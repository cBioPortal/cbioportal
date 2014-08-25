Clazz.declarePackage ("JU");
Clazz.load (["java.net.URLConnection"], "JU.AjaxURLConnection", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.bytesOut = null;
this.postOut = "";
Clazz.instantialize (this, arguments);
}, JU, "AjaxURLConnection", java.net.URLConnection);
Clazz.defineMethod (c$, "doAjax", 
 function () {
{
return Jmol._doAjax(this.url, this.postOut, this.bytesOut);
}});
Clazz.overrideMethod (c$, "connect", 
function () {
});
Clazz.defineMethod (c$, "outputBytes", 
function (bytes) {
this.bytesOut = bytes;
}, "~A");
Clazz.defineMethod (c$, "outputString", 
function (post) {
this.postOut = post;
}, "~S");
Clazz.defineMethod (c$, "getContents", 
function () {
return this.doAjax ();
});
});
