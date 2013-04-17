Clazz.declarePackage ("java.net");
Clazz.load (null, "java.net.URLConnection", ["java.lang.IllegalStateException", "$.NullPointerException", "java.net.UnknownServiceException", "J.util.JmolList"], function () {
c$ = Clazz.decorateAsClass (function () {
this.url = null;
this.doInput = true;
this.doOutput = false;
this.connected = false;
this.requests = null;
Clazz.instantialize (this, arguments);
}, java.net, "URLConnection");
$_M(c$, "setDoInput", 
function (doinput) {
if (this.connected) throw  new IllegalStateException ("Already connected");
this.doInput = doinput;
}, "~B");
$_M(c$, "getDoInput", 
function () {
return this.doInput;
});
$_M(c$, "setDoOutput", 
function (dooutput) {
if (this.connected) throw  new IllegalStateException ("Already connected");
this.doOutput = dooutput;
}, "~B");
$_M(c$, "getDoOutput", 
function () {
return this.doOutput;
});
Clazz.makeConstructor (c$, 
function (url) {
this.url = url;
}, "java.net.URL");
$_M(c$, "getURL", 
function () {
return this.url;
});
$_M(c$, "getInputStream", 
function () {
throw  new java.net.UnknownServiceException ("protocol doesn't support input");
});
$_M(c$, "getOutputStream", 
function () {
throw  new java.net.UnknownServiceException ("protocol doesn't support output");
});
$_M(c$, "setRequestProperty", 
function (key, value) {
if (this.connected) throw  new IllegalStateException ("Already connected");
if (key == null) throw  new NullPointerException ("key is null");
if (this.requests == null) this.requests =  new J.util.JmolList ();
for (var i = this.requests.size (); --i >= 0; ) if (this.requests.get (i)[0].equals (key)) {
this.requests.get (i)[1] = value;
return;
}
this.requests.addLast ([key, value]);
}, "~S,~S");
});
