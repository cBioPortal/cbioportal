$_L(["java.io.Reader"],"java.io.CharArrayReader",["java.io.IOException","java.lang.ArrayIndexOutOfBoundsException","$.IllegalArgumentException"],function(){
c$=$_C(function(){
this.buf=null;
this.pos=0;
this.markedPos=-1;
this.count=0;
$_Z(this,arguments);
},java.io,"CharArrayReader",java.io.Reader);
$_K(c$,
function(buf){
$_R(this,java.io.CharArrayReader,[buf]);
this.buf=buf;
this.count=buf.length;
},"~A");
$_K(c$,
function(buf,offset,length){
$_R(this,java.io.CharArrayReader,[buf]);
if(0<=offset&&offset<=buf.length&&length>=0){
this.buf=buf;
this.pos=offset;
this.count=this.pos+length<buf.length?length:buf.length;
}else{
throw new IllegalArgumentException();
}},"~A,~N,~N");
$_V(c$,"close",
function(){
{
if(this.isOpen()){
this.buf=null;
}}});
$_M(c$,"isOpen",
($fz=function(){
return this.buf!=null;
},$fz.isPrivate=true,$fz));
$_V(c$,"mark",
function(readLimit){
{
if(this.isOpen()){
this.markedPos=this.pos;
}else{
throw new java.io.IOException(("K0060"));
}}},"~N");
$_V(c$,"markSupported",
function(){
return true;
});
$_M(c$,"read",
function(){
{
if(this.isOpen()){
if(this.pos!=this.count){
return this.buf[this.pos++];
}return-1;
}throw new java.io.IOException(("K0060"));
}});
$_M(c$,"read",
function(buffer,offset,len){
if(0<=offset&&offset<=buffer.length&&0<=len&&len<=buffer.length-offset){
{
if(this.isOpen()){
if(this.pos<this.count){
var bytesRead=this.pos+len>this.count?this.count-this.pos:len;
System.arraycopy(this.buf,this.pos,buffer,offset,bytesRead);
this.pos+=bytesRead;
return bytesRead;
}return-1;
}throw new java.io.IOException(("K0060"));
}}throw new ArrayIndexOutOfBoundsException();
},"~A,~N,~N");
$_V(c$,"ready",
function(){
{
if(this.isOpen()){
return this.pos!=this.count;
}throw new java.io.IOException(("K0060"));
}});
$_V(c$,"reset",
function(){
{
if(this.isOpen()){
this.pos=this.markedPos!=-1?this.markedPos:0;
}else{
throw new java.io.IOException(("K0060"));
}}});
$_V(c$,"skip",
function(n){
{
if(this.isOpen()){
if(n<=0){
return 0;
}var skipped=0;
if(n<this.count-this.pos){
this.pos=this.pos+n;
skipped=n;
}else{
skipped=this.count-this.pos;
this.pos=this.count;
}return skipped;
}throw new java.io.IOException(("K0060"));
}},"~N");
});
