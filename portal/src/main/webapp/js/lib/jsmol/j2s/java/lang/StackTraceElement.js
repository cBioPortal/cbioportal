$_L(null,"java.lang.StackTraceElement",["java.lang.NullPointerException","$.StringBuilder"],function(){
c$=$_C(function(){
this.declaringClass=null;
this.methodName=null;
this.fileName=null;
this.lineNumber=0;
$_Z(this,arguments);
},java.lang,"StackTraceElement",null,java.io.Serializable);
$_K(c$,
function(cls,method,file,line){
if(cls==null||method==null){
throw new NullPointerException();
}this.declaringClass=cls;
this.methodName=method;
this.fileName=file;
this.lineNumber=line;
},"~S,~S,~S,~N");
$_V(c$,"equals",
function(obj){
if(!($_O(obj,StackTraceElement))){
return false;
}var castObj=obj;
if((this.methodName==null)||(castObj.methodName==null)){
return false;
}if(!this.getMethodName().equals(castObj.getMethodName())){
return false;
}if(!this.getClassName().equals(castObj.getClassName())){
return false;
}var localFileName=this.getFileName();
if(localFileName==null){
if(castObj.getFileName()!=null){
return false;
}}else{
if(!localFileName.equals(castObj.getFileName())){
return false;
}}if(this.getLineNumber()!=castObj.getLineNumber()){
return false;
}return true;
},"~O");
$_M(c$,"getClassName",
function(){
return(this.declaringClass==null)?"<unknown class>":this.declaringClass;
});
$_M(c$,"getFileName",
function(){
return this.fileName;
});
$_M(c$,"getLineNumber",
function(){
return this.lineNumber;
});
$_M(c$,"getMethodName",
function(){
return(this.methodName==null)?"<unknown method>":this.methodName;
});
$_V(c$,"hashCode",
function(){
if(this.methodName==null){
return 0;
}return this.methodName.hashCode()^this.declaringClass.hashCode();
});
$_M(c$,"isNativeMethod",
function(){
return this.lineNumber==-2;
});
$_V(c$,"toString",
function(){
var buf=new StringBuilder(80);
buf.append(this.getClassName());
buf.append('.');
buf.append(this.getMethodName());
if(this.isNativeMethod()){
buf.append("(Native Method)");
}else{
var fName=this.getFileName();
if(fName==null){
buf.append("(Unknown Source)");
}else{
var lineNum=this.getLineNumber();
buf.append('(');
buf.append(fName);
if(lineNum>=0){
buf.append(':');
buf.append(lineNum);
}buf.append(')');
}}return buf.toString();
});
});
