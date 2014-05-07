$_L(["java.lang.reflect.AnnotatedElement"],"java.lang.reflect.AccessibleObject",null,function(){
c$=$_T(java.lang.reflect,"AccessibleObject",null,java.lang.reflect.AnnotatedElement);
$_K(c$,
function(){
});
$_M(c$,"isAccessible",
function(){
return false;
});
c$.setAccessible=$_M(c$,"setAccessible",
function(objects,flag){
return;
},"~A,~B");
$_M(c$,"setAccessible",
function(flag){
return;
},"~B");
$_V(c$,"isAnnotationPresent",
function(annotationType){
return false;
},"Class");
$_V(c$,"getDeclaredAnnotations",
function(){
return new Array(0);
});
$_V(c$,"getAnnotations",
function(){
return new Array(0);
});
$_V(c$,"getAnnotation",
function(annotationType){
return null;
},"Class");
c$.marshallArguments=$_M(c$,"marshallArguments",
function(parameterTypes,args){
return null;
},"~A,~A");
$_M(c$,"invokeV",
function(receiver,args){
return;
},"~O,~A");
$_M(c$,"invokeL",
function(receiver,args){
return null;
},"~O,~A");
$_M(c$,"invokeI",
function(receiver,args){
return 0;
},"~O,~A");
$_M(c$,"invokeJ",
function(receiver,args){
return 0;
},"~O,~A");
$_M(c$,"invokeF",
function(receiver,args){
return 0.0;
},"~O,~A");
$_M(c$,"invokeD",
function(receiver,args){
return 0.0;
},"~O,~A");
c$.emptyArgs=c$.prototype.emptyArgs=new Array(0);
});
