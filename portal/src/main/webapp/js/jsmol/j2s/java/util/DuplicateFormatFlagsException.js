$_L(["java.util.IllegalFormatException"],"java.util.DuplicateFormatFlagsException",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.flags=null;
$_Z(this,arguments);
},java.util,"DuplicateFormatFlagsException",java.util.IllegalFormatException);
$_K(c$,
function(f){
$_R(this,java.util.DuplicateFormatFlagsException,[]);
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
return"Flags of the DuplicateFormatFlagsException is '"+this.flags+"'";
});
});
