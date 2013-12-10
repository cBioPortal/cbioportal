$_L(["java.util.IllegalFormatException"],"java.util.FormatFlagsConversionMismatchException",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.f=null;
this.c=0;
$_Z(this,arguments);
},java.util,"FormatFlagsConversionMismatchException",java.util.IllegalFormatException,java.io.Serializable);
$_K(c$,
function(f,c){
$_R(this,java.util.FormatFlagsConversionMismatchException,[]);
if(null==f){
throw new NullPointerException();
}this.f=f;
this.c=c;
},"~S,~N");
$_M(c$,"getFlags",
function(){
return this.f;
});
$_M(c$,"getConversion",
function(){
return this.c;
});
$_V(c$,"getMessage",
function(){
return"Mismatched Convertor ="+this.c+", Flags= "+this.f;
});
});
