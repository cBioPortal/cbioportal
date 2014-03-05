Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["java.net.URLStreamHandler"], "J.awtjs2d.AjaxURLStreamHandler", ["J.awtjs2d.JmolURLConnection", "J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.protocol = null;
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "AjaxURLStreamHandler", java.net.URLStreamHandler);
Clazz.makeConstructor (c$, 
function (protocol) {
Clazz.superConstructor (this, J.awtjs2d.AjaxURLStreamHandler, []);
this.protocol = protocol;
}, "~S");
Clazz.overrideMethod (c$, "openConnection", 
function (url) {
return  new J.awtjs2d.JmolURLConnection (url);
}, "java.net.URL");
Clazz.overrideMethod (c$, "toExternalForm", 
function (u) {
var result =  new J.util.SB ();
result.append (u.getProtocol ());
result.append (":");
if (u.getAuthority () != null && u.getAuthority ().length > 0) {
result.append ("//");
result.append (u.getAuthority ());
}if (u.getPath () != null) {
result.append (u.getPath ());
}if (u.getQuery () != null) {
result.append ("?");
result.append (u.getQuery ());
}if (u.getRef () != null) {
result.append ("#");
result.append (u.getRef ());
}return result.toString ();
}, "java.net.URL");
});
