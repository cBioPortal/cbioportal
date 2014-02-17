$_L(["java.util.IllegalFormatException"],"java.util.IllegalFormatConversionException",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.c=0;
this.arg=null;
$_Z(this,arguments);
},java.util,"IllegalFormatConversionException",java.util.IllegalFormatException,java.io.Serializable);
$_K(c$,
function(c,arg){
$_R(this,java.util.IllegalFormatConversionException,[]);
this.c=c;
if(arg==null){
throw new NullPointerException();
}this.arg=arg;
},"~N,Class");
$_M(c$,"getArgumentClass",
function(){
return this.arg;
});
$_M(c$,"getConversion",
function(){
return this.c;
});
$_V(c$,"getMessage",
function(){
return""+this.c+" is incompatible with "+this.arg.getName();
});
});
