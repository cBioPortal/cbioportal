Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["java.io.ByteArrayOutputStream"], "J.awtjs2d.WebOutputChannel", ["J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fileName = null;
this.sb = null;
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "WebOutputChannel", java.io.ByteArrayOutputStream);
Clazz.makeConstructor (c$, 
function (fileName) {
Clazz.superConstructor (this, J.awtjs2d.WebOutputChannel, []);
this.fileName = fileName;
}, "~S");
$_M(c$, "write", 
function (data) {
if (this.sb == null) this.sb =  new J.util.SB ();
this.sb.append (data);
}, "~S");
Clazz.overrideMethod (c$, "close", 
function () {
if (this.fileName == null) return;
{
Jmol._doAjax(this.fileName, null, (this.sb == null ? this.toByteArray() : this.sb.toString()));
}});
});
