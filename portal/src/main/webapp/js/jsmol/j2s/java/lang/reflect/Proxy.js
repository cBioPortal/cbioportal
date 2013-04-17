$_L(null,"java.lang.reflect.Proxy",["java.lang.IllegalArgumentException","$.NullPointerException"],function(){
c$=$_C(function(){
this.h=null;
$_Z(this,arguments);
},java.lang.reflect,"Proxy",null,java.io.Serializable);
$_K(c$,
function(h){
this.h=h;
},"java.lang.reflect.InvocationHandler");
c$.getProxyClass=$_M(c$,"getProxyClass",
function(loader,interfaces){
if(interfaces==null){
throw new NullPointerException();
}return null;
},"ClassLoader,~A");
c$.newProxyInstance=$_M(c$,"newProxyInstance",
function(loader,interfaces,h){
if(h!=null){
}throw new NullPointerException();
},"ClassLoader,~A,java.lang.reflect.InvocationHandler");
c$.isProxyClass=$_M(c$,"isProxyClass",
function(cl){
if(cl!=null){
}throw new NullPointerException();
},"Class");
c$.getInvocationHandler=$_M(c$,"getInvocationHandler",
function(proxy){
if(java.lang.reflect.Proxy.isProxyClass(proxy.getClass())){
return(proxy).h;
}throw new IllegalArgumentException(("K00f1"));
},"~O");
});
