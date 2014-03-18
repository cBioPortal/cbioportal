$_L(["java.util.IllegalFormatException"],"java.util.MissingFormatArgumentException",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.s=null;
$_Z(this,arguments);
},java.util,"MissingFormatArgumentException",java.util.IllegalFormatException);
$_K(c$,
function(s){
$_R(this,java.util.MissingFormatArgumentException,[]);
if(null==s){
throw new NullPointerException();
}this.s=s;
},"~S");
$_M(c$,"getFormatSpecifier",
function(){
return this.s;
});
$_V(c$,"getMessage",
function(){
return"Format specifier '"+this.s+"'";
});
});
