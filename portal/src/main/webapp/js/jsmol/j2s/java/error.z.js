c$=$_C(function(){
this.detailMessage=null;
this.cause=null;
this.stackTrace=null;
$_Z(this,arguments);
},java.lang,"Throwable",null,java.io.Serializable);
$_Y(c$,function(){
this.cause=this;
});
$_K(c$,
function(){
this.fillInStackTrace();
});
$_K(c$,
function(message){
this.fillInStackTrace();
this.detailMessage=message;
},"~S");
$_K(c$,
function(message,cause){
this.fillInStackTrace();
this.detailMessage=message;
this.cause=cause;
},"~S,Throwable");
$_K(c$,
function(cause){
this.fillInStackTrace();
this.detailMessage=(cause==null?null:cause.toString());
this.cause=cause;
},"Throwable");
$_M(c$,"getMessage",
function(){
{
if(typeof this.message!="undefined"){
return this.message;
}
}return this.detailMessage;
});
$_M(c$,"getLocalizedMessage",
function(){
return this.getMessage();
});
$_M(c$,"getCause",
function(){
return(this.cause===this?null:this.cause);
});
$_M(c$,"initCause",
function(cause){
if(this.cause!==this)throw new IllegalStateException("Can't overwrite cause");
if(cause===this)throw new IllegalArgumentException("Self-causation not permitted");
this.cause=cause;
return this;
},"Throwable");
$_V(c$,"toString",
function(){
var s=this.getClass().getName();
var message=this.getLocalizedMessage();
return(message!=null)?(s+": "+message):s;
});
$_M(c$,"printStackTrace",
function(){
System.err.println(this);
for(var i=0;i<this.stackTrace.length;i++){
var t=this.stackTrace[i];
var x=t.methodName.indexOf("(");
var n=t.methodName.substring(0,x).replace(/\s+/g,"");
if(n!="construct"||t.nativeClazz==null
||Clazz.getInheritedLevel(t.nativeClazz,Throwable)<0){
System.err.println(t);
}
}
});
$_M(c$,"printStackTrace",
function(s){
this.printStackTrace();
},"java.io.PrintStream");
$_M(c$,"printStackTrace",
function(s){
this.printStackTrace();
},"java.io.PrintWriter");
$_M(c$,"fillInStackTrace",
function(){
this.stackTrace=new Array();
var caller=arguments.callee.caller;
var superCaller=null;
var callerList=new Array();
var index=Clazz.callingStackTraces.length-1;
var noLooping=true;
while(index>-1||caller!=null){
var clazzName=null;
var nativeClazz=null;
if(!noLooping||caller==Clazz.tryToSearchAndExecute||caller==$_U||caller==null){
if(index<0){
break;
}
noLooping=true;
superCaller=Clazz.callingStackTraces[index].caller;
nativeClazz=Clazz.callingStackTraces[index].owner;
index--;
}else{
superCaller=caller;
if(superCaller.claxxOwner!=null){
nativeClazz=superCaller.claxxOwner;
}else if(superCaller.exClazz!=null){
nativeClazz=superCaller.exClazz;
}
}
var st=new StackTraceElement(
((nativeClazz!=null&&nativeClazz.__CLASS_NAME__.length!=0)?
nativeClazz.__CLASS_NAME__:"anonymous"),
((superCaller.exName==null)?"anonymous":superCaller.exName)
+" ("+Clazz.getParamsType(superCaller.arguments)+")",
null,-1);
st.nativeClazz=nativeClazz;
this.stackTrace[this.stackTrace.length]=st;
for(var i=0;i<callerList.length;i++){
if(callerList[i]==superCaller){

var st=new StackTraceElement("lost","missing",null,-3);
st.nativeClazz=null;
this.stackTrace[this.stackTrace.length]=st;
noLooping=false;

}
}
if(superCaller!=null){
callerList[callerList.length]=superCaller;
}
caller=superCaller.arguments.callee.caller;
}
Clazz.initializingException=false;
return this;
});
$_M(c$,"setStackTrace",
function(stackTrace){
var defensiveCopy=stackTrace.clone();
for(var i=0;i<defensiveCopy.length;i++)if(defensiveCopy[i]==null)throw new NullPointerException("stackTrace["+i+"]");

this.stackTrace=defensiveCopy;
},"~A");

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

c$=$_T(java.lang,"Error",Throwable);

c$=$_T(java.lang,"LinkageError",Error);

c$=$_T(java.lang,"IncompatibleClassChangeError",LinkageError);

c$=$_T(java.lang,"AbstractMethodError",IncompatibleClassChangeError);

c$=$_T(java.lang,"AssertionError",Error);
$_K(c$,
function(detailMessage){
$_R(this,AssertionError,[String.valueOf(detailMessage),($_O(detailMessage,Throwable)?detailMessage:null)]);
},"~O");
$_K(c$,
function(detailMessage){
this.construct(String.valueOf(detailMessage));
},"~B");
$_K(c$,
function(detailMessage){
this.construct(String.valueOf(detailMessage));
},"~N");
$_K(c$,
function(detailMessage){
this.construct(Integer.toString(detailMessage));
},"~N");
$_K(c$,
function(detailMessage){
this.construct(Long.toString(detailMessage));
},"~N");
$_K(c$,
function(detailMessage){
this.construct(Float.toString(detailMessage));
},"~N");
$_K(c$,
function(detailMessage){
this.construct(Double.toString(detailMessage));
},"~N");

c$=$_T(java.lang,"ClassCircularityError",LinkageError);

c$=$_T(java.lang,"ClassFormatError",LinkageError);

c$=$_C(function(){
this.exception=null;
$_Z(this,arguments);
},java.lang,"ExceptionInInitializerError",LinkageError);
$_K(c$,
function(){
$_R(this,ExceptionInInitializerError);
this.initCause(null);
});
$_K(c$,
function(detailMessage){
$_R(this,ExceptionInInitializerError,[detailMessage]);
this.initCause(null);
},"~S");
$_K(c$,
function(exception){
$_R(this,ExceptionInInitializerError);
this.exception=exception;
this.initCause(exception);
},"Throwable");
$_M(c$,"getException",
function(){
return this.exception;
});
$_V(c$,"getCause",
function(){
return this.exception;
});

c$=$_T(java.lang,"IllegalAccessError",IncompatibleClassChangeError);

c$=$_T(java.lang,"InstantiationError",IncompatibleClassChangeError);

c$=$_T(java.lang,"VirtualMachineError",Error);

c$=$_T(java.lang,"InternalError",VirtualMachineError);

c$=$_T(java.lang,"NoClassDefFoundError",LinkageError);

c$=$_T(java.lang,"NoSuchFieldError",IncompatibleClassChangeError);

c$=$_T(java.lang,"NoSuchMethodError",IncompatibleClassChangeError);

c$=$_T(java.lang,"OutOfMemoryError",VirtualMachineError);

c$=$_T(java.lang,"StackOverflowError",VirtualMachineError);

c$=$_T(java.lang,"UnknownError",VirtualMachineError);

c$=$_T(java.lang,"UnsatisfiedLinkError",LinkageError);

c$=$_T(java.lang,"UnsupportedClassVersionError",ClassFormatError);

c$=$_T(java.lang,"VerifyError",LinkageError);

c$=$_T(java.lang,"ThreadDeath",Error);
$_K(c$,
function(){
$_R(this,ThreadDeath,[]);
});

c$=$_T(java.lang,"Exception",Throwable);

c$=$_T(java.lang,"RuntimeException",Exception);

c$=$_T(java.lang,"ArithmeticException",RuntimeException);

c$=$_T(java.lang,"IndexOutOfBoundsException",RuntimeException);

c$=$_T(java.lang,"ArrayIndexOutOfBoundsException",IndexOutOfBoundsException);
$_K(c$,
function(index){
$_R(this,ArrayIndexOutOfBoundsException,["Array index out of range: "+index]);
},"~N");

c$=$_T(java.lang,"ArrayStoreException",RuntimeException);

c$=$_T(java.lang,"ClassCastException",RuntimeException);

c$=$_C(function(){
this.ex=null;
$_Z(this,arguments);
},java.lang,"ClassNotFoundException",Exception);
$_K(c$,
function(){
$_R(this,ClassNotFoundException,[Clazz.castNullAs("Throwable")]);
});
$_K(c$,
function(detailMessage){
$_R(this,ClassNotFoundException,[detailMessage,null]);
},"~S");
$_K(c$,
function(detailMessage,exception){
$_R(this,ClassNotFoundException,[detailMessage]);
this.ex=exception;
},"~S,Throwable");
$_M(c$,"getException",
function(){
return this.ex;
});
$_V(c$,"getCause",
function(){
return this.ex;
});

c$=$_T(java.lang,"CloneNotSupportedException",Exception);

c$=$_T(java.lang,"IllegalAccessException",Exception);

c$=$_T(java.lang,"IllegalArgumentException",RuntimeException);
$_K(c$,
function(cause){
$_R(this,IllegalArgumentException,[(cause==null?null:cause.toString()),cause]);
},"Throwable");

c$=$_T(java.lang,"IllegalMonitorStateException",RuntimeException);

c$=$_T(java.lang,"IllegalStateException",RuntimeException);
$_K(c$,
function(cause){
$_R(this,IllegalStateException,[(cause==null?null:cause.toString()),cause]);
},"Throwable");

c$=$_T(java.lang,"IllegalThreadStateException",IllegalArgumentException);

c$=$_T(java.lang,"InstantiationException",Exception);

c$=$_T(java.lang,"InterruptedException",Exception);

c$=$_T(java.lang,"NegativeArraySizeException",RuntimeException);

c$=$_T(java.lang,"NoSuchFieldException",Exception);

c$=$_T(java.lang,"NoSuchMethodException",Exception);

c$=$_T(java.lang,"NullPointerException",RuntimeException);

c$=$_T(java.lang,"NumberFormatException",IllegalArgumentException);

c$=$_T(java.lang,"SecurityException",RuntimeException);
$_K(c$,
function(cause){
$_R(this,SecurityException,[(cause==null?null:cause.toString()),cause]);
},"Throwable");

c$=$_T(java.lang,"StringIndexOutOfBoundsException",IndexOutOfBoundsException);
$_K(c$,
function(index){
$_R(this,StringIndexOutOfBoundsException,["String index out of range: "+index]);
},"~N");

c$=$_T(java.lang,"UnsupportedOperationException",RuntimeException);
$_K(c$,
function(){
$_R(this,UnsupportedOperationException,[]);
});
$_K(c$,
function(cause){
$_R(this,UnsupportedOperationException,[(cause==null?null:cause.toString()),cause]);
},"Throwable");

c$=$_C(function(){
this.target=null;
$_Z(this,arguments);
},java.lang.reflect,"InvocationTargetException",Exception);
$_K(c$,
function(){
$_R(this,java.lang.reflect.InvocationTargetException,[Clazz.castNullAs("Throwable")]);
});
$_K(c$,
function(exception){
$_R(this,java.lang.reflect.InvocationTargetException,[null,exception]);
this.target=exception;
},"Throwable");
$_K(c$,
function(exception,detailMessage){
$_R(this,java.lang.reflect.InvocationTargetException,[detailMessage,exception]);
this.target=exception;
},"Throwable,~S");
$_M(c$,"getTargetException",
function(){
return this.target;
});
$_V(c$,"getCause",
function(){
return this.target;
});

c$=$_C(function(){
this.undeclaredThrowable=null;
$_Z(this,arguments);
},java.lang.reflect,"UndeclaredThrowableException",RuntimeException);
$_K(c$,
function(exception){
$_R(this,java.lang.reflect.UndeclaredThrowableException);
this.undeclaredThrowable=exception;
this.initCause(exception);
},"Throwable");
$_K(c$,
function(exception,detailMessage){
$_R(this,java.lang.reflect.UndeclaredThrowableException,[detailMessage]);
this.undeclaredThrowable=exception;
this.initCause(exception);
},"Throwable,~S");
$_M(c$,"getUndeclaredThrowable",
function(){
return this.undeclaredThrowable;
});
$_V(c$,"getCause",
function(){
return this.undeclaredThrowable;
});

c$=$_T(java.io,"IOException",Exception);

c$=$_T(java.io,"CharConversionException",java.io.IOException);

c$=$_T(java.io,"EOFException",java.io.IOException);

c$=$_T(java.io,"FileNotFoundException",java.io.IOException);

c$=$_C(function(){
this.bytesTransferred=0;
$_Z(this,arguments);
},java.io,"InterruptedIOException",java.io.IOException);

c$=$_T(java.io,"ObjectStreamException",java.io.IOException);

c$=$_C(function(){
this.classname=null;
$_Z(this,arguments);
},java.io,"InvalidClassException",java.io.ObjectStreamException);
$_K(c$,
function(className,detailMessage){
$_R(this,java.io.InvalidClassException,[detailMessage]);
this.classname=className;
},"~S,~S");
$_M(c$,"getMessage",
function(){
var msg=$_U(this,java.io.InvalidClassException,"getMessage",[]);
if(this.classname!=null){
msg=this.classname+';' + ' '+msg;
}return msg;
});

c$=$_T(java.io,"InvalidObjectException",java.io.ObjectStreamException);

c$=$_T(java.io,"NotActiveException",java.io.ObjectStreamException);

c$=$_T(java.io,"NotSerializableException",java.io.ObjectStreamException);

c$=$_C(function(){
this.eof=false;
this.length=0;
$_Z(this,arguments);
},java.io,"OptionalDataException",java.io.ObjectStreamException);

c$=$_T(java.io,"StreamCorruptedException",java.io.ObjectStreamException);

c$=$_T(java.io,"SyncFailedException",java.io.IOException);

c$=$_T(java.io,"UnsupportedEncodingException",java.io.IOException);

c$=$_T(java.io,"UTFDataFormatException",java.io.IOException);

c$=$_C(function(){
this.detail=null;
$_Z(this,arguments);
},java.io,"WriteAbortedException",java.io.ObjectStreamException);
$_K(c$,
function(detailMessage,rootCause){
$_R(this,java.io.WriteAbortedException,[detailMessage]);
this.detail=rootCause;
this.initCause(rootCause);
},"~S,Exception");
$_M(c$,"getMessage",
function(){
var msg=$_U(this,java.io.WriteAbortedException,"getMessage",[]);
if(this.detail!=null){
msg=msg+"; "+this.detail.toString();
}return msg;
});
$_V(c$,"getCause",
function(){
return this.detail;
});

c$=$_T(java.util,"ConcurrentModificationException",RuntimeException);
$_K(c$,
function(){
$_R(this,java.util.ConcurrentModificationException,[]);
});

c$=$_T(java.util,"EmptyStackException",RuntimeException);

c$=$_C(function(){
this.className=null;
this.key=null;
$_Z(this,arguments);
},java.util,"MissingResourceException",RuntimeException);
$_K(c$,
function(detailMessage,className,resourceName){
$_R(this,java.util.MissingResourceException,[detailMessage]);
this.className=className;
this.key=resourceName;
},"~S,~S,~S");
$_M(c$,"getClassName",
function(){
return this.className;
});
$_M(c$,"getKey",
function(){
return this.key;
});

c$=$_T(java.util,"NoSuchElementException",RuntimeException);

c$=$_T(java.util,"TooManyListenersException",Exception);
