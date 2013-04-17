Clazz.declarePackage ("J.util");
c$ = Clazz.decorateAsClass (function () {
this.sb = null;
this.s = null;
Clazz.instantialize (this, arguments);
}, J.util, "SB");
Clazz.makeConstructor (c$, 
function () {
{
this.s = "";
}});
c$.newN = $_M(c$, "newN", 
function (n) {
{
return new J.util.SB();
}}, "~N");
c$.newS = $_M(c$, "newS", 
function (s) {
{
var sb = new J.util.SB();
sb.s = s;
return sb;
}}, "~S");
$_M(c$, "append", 
function (s) {
{
this.s += s
}return this;
}, "~S");
$_M(c$, "appendC", 
function (c) {
{
this.s += c;
}return this;
}, "~S");
$_M(c$, "appendI", 
function (i) {
{
this.s += i
}return this;
}, "~N");
$_M(c$, "appendB", 
function (b) {
{
this.s += b
}return this;
}, "~B");
$_M(c$, "appendF", 
function (f) {
{
var sf = "" + f;
if (sf.indexOf(".") < 0 && sf.indexOf("e") < 0)
sf += ".0" ;
this.s += sf;
}return this;
}, "~N");
$_M(c$, "appendD", 
function (d) {
{
var sf = "" + d;
if (sf.indexOf(".") < 0 && sf.indexOf("e") < 0)
sf += ".0" ;
this.s += sf;
}return this;
}, "~N");
$_M(c$, "appendSB", 
function (buf) {
{
this.s += buf.s;
}return this;
}, "J.util.SB");
$_M(c$, "appendO", 
function (data) {
{
this.s += data.toString();
}return this;
}, "~O");
$_M(c$, "appendCB", 
function (cb, off, len) {
{
for (var i = len,j=off; --i >= 0;)
this.s += cb[j++];
}}, "~A,~N,~N");
Clazz.overrideMethod (c$, "toString", 
function () {
{
return this.s;
}});
$_M(c$, "length", 
function () {
{
return this.s.length;
}});
$_M(c$, "indexOf", 
function (s) {
{
return this.s.indexOf(s);
}}, "~S");
$_M(c$, "charAt", 
function (i) {
{
return this.s.charAt(i);
}}, "~N");
$_M(c$, "setLength", 
function (n) {
{
this.s = this.s.substring(0, n);
}}, "~N");
$_M(c$, "lastIndexOf", 
function (s) {
{
return this.s.lastIndexOf(s);
}}, "~S");
$_M(c$, "indexOf2", 
function (s, i) {
{
return this.s.indexOf(s, i);
}}, "~S,~N");
$_M(c$, "substring", 
function (i) {
{
return this.s.substring(i);
}}, "~N");
$_M(c$, "substring2", 
function (i, j) {
{
return this.s.substring(i, j);
}}, "~N,~N");
$_M(c$, "toBytes", 
function (off, len) {
if (len < 0) len = this.length () - off;
var b =  Clazz.newByteArray (len, 0);
for (var i = off + len, j = i - off; --i >= off; ) b[--j] = (this.charAt (i)).charCodeAt (0);

return b;
}, "~N,~N");
