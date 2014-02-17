$_L(["java.io.Writer"],"java.io.CharArrayWriter",["java.lang.IllegalArgumentException","$.IndexOutOfBoundsException","$.NullPointerException","$.StringIndexOutOfBoundsException"],function(){
c$=$_C(function(){
this.buf=null;
this.count=0;
$_Z(this,arguments);
},java.io,"CharArrayWriter",java.io.Writer);
$_K(c$,
function(){
$_R(this,java.io.CharArrayWriter);
this.buf=$_A(32,'\0');
this.lock=this.buf;
});
$_K(c$,
function(initialSize){
$_R(this,java.io.CharArrayWriter);
if(initialSize>=0){
this.buf=$_A(initialSize,'\0');
this.lock=this.buf;
}else{
throw new IllegalArgumentException(("K005e"));
}},"~N");
$_V(c$,"close",
function(){
});
$_M(c$,"expand",
($fz=function(i){
if(this.count+i<=this.buf.length){
return;
}var newbuf=$_A(this.buf.length+(2*i),'\0');
System.arraycopy(this.buf,0,newbuf,0,this.count);
this.buf=newbuf;
},$fz.isPrivate=true,$fz),"~N");
$_V(c$,"flush",
function(){
});
$_M(c$,"reset",
function(){
{
this.count=0;
}});
$_M(c$,"size",
function(){
{
return this.count;
}});
$_M(c$,"toCharArray",
function(){
{
var result=$_A(this.count,'\0');
System.arraycopy(this.buf,0,result,0,this.count);
return result;
}});
$_V(c$,"toString",
function(){
{
return String.instantialize(this.buf,0,this.count);
}});
$_M(c$,"write",
function(c,offset,len){
if(0<=offset&&offset<=c.length&&0<=len&&len<=c.length-offset){
{
this.expand(len);
System.arraycopy(c,offset,this.buf,this.count,len);
this.count+=len;
}}else{
throw new IndexOutOfBoundsException();
}},"~A,~N,~N");
$_M(c$,"write",
function(oneChar){
{
this.expand(1);
this.buf[this.count++]=String.fromCharCode(oneChar);
}},"~N");
$_M(c$,"write",
function(str,offset,len){
if(str==null){
throw new NullPointerException(("K0047"));
}if(0<=offset&&offset<=str.length&&0<=len&&len<=str.length-offset){
{
this.expand(len);
str.getChars(offset,offset+len,this.buf,this.count);
this.count+=len;
}}else{
throw new StringIndexOutOfBoundsException();
}},"~S,~N,~N");
$_M(c$,"writeTo",
function(out){
{
out.write(this.buf,0,this.count);
}},"java.io.Writer");
$_M(c$,"append",
function(c){
this.write(c.charCodeAt(0));
return this;
},"~N");
$_M(c$,"append",
function(csq){
if(null==csq){
this.append("null",0,"null".length);
}else{
this.append(csq,0,csq.length());
}return this;
},"CharSequence");
$_M(c$,"append",
function(csq,start,end){
if(null==csq){
csq="null";
}var output=csq.subSequence(start,end).toString();
this.write(output,0,output.length);
return this;
},"CharSequence,~N,~N");
});
