$_J("java.net");
c$=$_T(java.net,"URLEncoder");
c$.encode=$_M(c$,"encode",
function(s){
return encodeURIComponent(arguments[0]);
},"~S");
c$.encode=$_M(c$,"encode",
function(s,enc){
return encodeURIComponent(arguments[0]);
},"~S,~S");
$_S(c$,
"digits","0123456789ABCDEF");
