$_L(["java.util.IllegalFormatException"],"java.util.IllegalFormatFlagsException",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.flags=null;
$_Z(this,arguments);
},java.util,"IllegalFormatFlagsException",java.util.IllegalFormatException,java.io.Serializable);
$_K(c$,
function(f){
$_R(this,java.util.IllegalFormatFlagsException,[]);
if(null==f){
throw new NullPointerException();
}this.flags=f;
},"~S");
$_M(c$,"getFlags",
function(){
return this.flags;
});
$_V(c$,"getMessage",
function(){
return"Flags = '"+this.flags+"'";
});
});
