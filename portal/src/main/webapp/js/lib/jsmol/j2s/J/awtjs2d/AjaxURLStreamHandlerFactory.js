Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["java.net.URLStreamHandlerFactory", "java.util.Hashtable"], "J.awtjs2d.AjaxURLStreamHandlerFactory", ["J.awtjs2d.AjaxURLStreamHandler"], function () {
c$ = Clazz.decorateAsClass (function () {
this.htFactories = null;
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "AjaxURLStreamHandlerFactory", null, java.net.URLStreamHandlerFactory);
Clazz.prepareFields (c$, function () {
this.htFactories =  new java.util.Hashtable ();
});
Clazz.overrideMethod (c$, "createURLStreamHandler", 
function (protocol) {
var fac = this.htFactories.get (protocol);
if (fac == null) this.htFactories.put (protocol, fac =  new J.awtjs2d.AjaxURLStreamHandler (protocol));
return (fac.protocol == null ? null : fac);
}, "~S");
});
