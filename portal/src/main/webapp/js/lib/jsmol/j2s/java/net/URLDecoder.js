$_J("java.net");
$_L(null,"java.net.URLDecoder",["java.lang.NullPointerException"],function(){
c$=$_T(java.net,"URLDecoder");
c$.decode=$_M(c$,"decode",
function(s){
return decodeURIComponent(arguments[0]);
},"~S");
c$.decode=$_M(c$,"decode",
function(s,enc){
if(enc==null){
throw new NullPointerException();
}{
return decodeURIComponent(arguments[0]);
}return null;
},"~S,~S");
});
