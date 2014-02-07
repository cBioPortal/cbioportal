$_L(["java.util.Hashtable"],"java.util.Properties",["java.lang.NullPointerException","$.StringBuffer"],function(){
c$=$_C(function(){
this.builder=null;
this.defaults=null;
$_Z(this,arguments);
},java.util,"Properties",java.util.Hashtable);

$_K(c$,
function(properties){
$_R(this,java.util.Properties,[]);
this.defaults=properties;
},"java.util.Properties");
$_M(c$,"dumpString",
($fz=function(buffer,string,key){
var i=0;
if(!key&&i<string.length&&(string.charAt(i)).charCodeAt(0)==(' ').charCodeAt(0)){
buffer.append("\\ ");
i++;
}for(;i<string.length;i++){
var ch=string.charAt(i);
switch(ch){
case'\t':
buffer.append("\\t");
break;
case'\n':
buffer.append("\\n");
break;
case'\f':
buffer.append("\\f");
break;
case'\r':
buffer.append("\\r");
break;
default:
if("\\#!=:".indexOf(ch)>=0||(key&&(ch).charCodeAt(0)==(' ').charCodeAt(0))){
buffer.append('\\');
}if((ch).charCodeAt(0)>=(' ').charCodeAt (0) && (ch).charCodeAt (0) <= ('~').charCodeAt(0)){
buffer.append(ch);
}else{
var hex=Integer.toHexString(ch.charCodeAt(0));
buffer.append("\\u");
for(var j=0;j<4-hex.length;j++){
buffer.append("0");
}
buffer.append(hex);
}}
}
},$fz.isPrivate=true,$fz),"StringBuilder,~S,~B");
$_M(c$,"getProperty",
function(name){
var result=this.get(name);
var property=$_O(result,String)?result:null;
if(property==null&&this.defaults!=null){
property=this.defaults.getProperty(name);
}return property;
},"~S");
$_M(c$,"getProperty",
function(name,defaultValue){
var result=this.get(name);
var property=$_O(result,String)?result:null;
if(property==null&&this.defaults!=null){
property=this.defaults.getProperty(name);
}if(property==null){
return defaultValue;
}return property;
},"~S,~S");
$_M(c$,"list",
function(out){
if(out==null){
throw new NullPointerException();
}var buffer=new StringBuffer(80);
var keys=this.propertyNames();
while(keys.hasMoreElements()){
var key=keys.nextElement();
buffer.append(key);
buffer.append('=');
var property=this.get(key);
var def=this.defaults;
while(property==null){
property=def.get(key);
def=def.defaults;
}
if(property.length>40){
buffer.append(property.substring(0,37));
buffer.append("...");
}else{
buffer.append(property);
}out.println(buffer.toString());
buffer.setLength(0);
}
},"java.io.PrintStream");
$_M(c$,"list",
function(writer){
if(writer==null){
throw new NullPointerException();
}var buffer=new StringBuffer(80);
var keys=this.propertyNames();
while(keys.hasMoreElements()){
var key=keys.nextElement();
buffer.append(key);
buffer.append('=');
var property=this.get(key);
var def=this.defaults;
while(property==null){
property=def.get(key);
def=def.defaults;
}
if(property.length>40){
buffer.append(property.substring(0,37));
buffer.append("...");
}else{
buffer.append(property);
}writer.println(buffer.toString());
buffer.setLength(0);
}
},"java.io.PrintWriter");
$_M(c$,"load",
function($in){

},"java.io.InputStream");
$_M(c$,"propertyNames",
function(){
if(this.defaults==null){
return this.keys();
}var set=new java.util.Hashtable(this.defaults.size()+this.size());
var keys=this.defaults.propertyNames();
while(keys.hasMoreElements()){
set.put(keys.nextElement(),set);
}
keys=this.keys();
while(keys.hasMoreElements()){
set.put(keys.nextElement(),set);
}
return set.keys();
});
$_M(c$,"save",
function(out,comment){
try{
this.store(out,comment);
}catch(e){
if($_O(e,java.io.IOException)){
}else{
throw e;
}
}
},"java.io.OutputStream,~S");
$_M(c$,"setProperty",
function(name,value){
return this.put(name,value);
},"~S,~S");
$_M(c$,"store",
function(out,comment){

},"java.io.OutputStream,~S");
$_M(c$,"loadFromXML",
function($in){

},"java.io.InputStream");
$_M(c$,"storeToXML",
function(os,comment){

},"java.io.OutputStream,~S");
$_M(c$,"storeToXML",
function(os,comment,encoding){

},"java.io.OutputStream,~S,~S");
$_M(c$,"substitutePredefinedEntries",
($fz=function(s){
return s.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\u0027","&apos;").replaceAll("\"","&quot;");
},$fz.isPrivate=true,$fz),"~S");
$_S(c$,
"PROP_DTD_NAME","http://java.sun.com/dtd/properties.dtd",
"PROP_DTD","<?xml version=\"1.0\" encoding=\"UTF-8\"?>    <!ELEMENT properties (comment?, entry*) >    <!ATTLIST properties version CDATA #FIXED \"1.0\" >    <!ELEMENT comment (#PCDATA) >    <!ELEMENT entry (#PCDATA) >    <!ATTLIST entry key CDATA #REQUIRED >",
"NONE",0,
"SLASH",1,
"UNICODE",2,
"CONTINUE",3,
"KEY_DONE",4,
"IGNORE",5,
"lineSeparator",null);

});
