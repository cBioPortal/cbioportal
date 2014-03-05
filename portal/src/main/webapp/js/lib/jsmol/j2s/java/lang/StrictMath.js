$_L(null,"java.lang.StrictMath",["java.lang.Double","$.Float"],function(){
c$=$_T(java.lang,"StrictMath");
c$.abs=$_M(c$,"abs",
function(d){
return Math.abs(d);
},"~N");
c$.acos=$_M(c$,"acos",
function(d){
return Math.acos(d);
},"~N");
c$.asin=$_M(c$,"asin",
function(d){
return Math.asin(d);
},"~N");
c$.atan=$_M(c$,"atan",
function(d){
return Math.atan(d);
},"~N");
c$.atan2=$_M(c$,"atan2",
function(d1,d2){
return Math.atan2(d1,d2);
},"~N,~N");
c$.ceil=$_M(c$,"ceil",
function(d){
return Math.ceil(d);
},"~N");
c$.cosh=$_M(c$,"cosh",
function(d){
return Math.cosh(d);
},"~N");
c$.cos=$_M(c$,"cos",
function(d){
return Math.cos(d);
},"~N");
c$.exp=$_M(c$,"exp",
function(d){
return Math.exp(d);
},"~N");
c$.floor=$_M(c$,"floor",
function(d){
return Math.floor(d);
},"~N");
c$.log=$_M(c$,"log",
function(d){
return Math.log(d);
},"~N");
c$.log10=$_M(c$,"log10",
function(d){
return Math.log10(d);
},"~N");
c$.max=$_M(c$,"max",
function(d1,d2){
return Math.max(d1,d2);
},"~N,~N");
c$.min=$_M(c$,"min",
function(d1,d2){
return Math.min(d1,d2);
},"~N,~N");
c$.pow=$_M(c$,"pow",
function(d1,d2){
return Math.pow(d1,d2);
},"~N,~N");
c$.random=$_M(c$,"random",
function(){
return Math.random();
});
c$.rint=$_M(c$,"rint",
function(d){
return Math.round(d);
},"~N");
c$.round=$_M(c$,"round",
function(d){
return Math.round(d);
},"~N");
c$.signum=$_M(c$,"signum",
function(d){
if(Double.isNaN(d)){
return NaN;
}var sig=d;
if(d>0){
sig=1.0;
}else if(d<0){
sig=-1.0;
}return sig;
},"~N");
c$.signum=$_M(c$,"signum",
function(f){
if(Float.isNaN(f)){
return NaN;
}var sig=f;
if(f>0){
sig=1.0;
}else if(f<0){
sig=-1.0;
}return sig;
},"~N");
c$.sinh=$_M(c$,"sinh",
function(d){
return Math.sinh(d);
},"~N");
c$.sin=$_M(c$,"sin",
function(d){
return Math.sin(d);
},"~N");
c$.sqrt=$_M(c$,"sqrt",
function(d){
return Math.sqrt(d);
},"~N");
c$.tan=$_M(c$,"tan",
function(d){
return Math.tan(d);
},"~N");
c$.tanh=$_M(c$,"tanh",
function(d){
return Math.tanh(d);
},"~N");
c$.toDegrees=$_M(c$,"toDegrees",
function(angrad){
return angrad*180/3.141592653589793;
},"~N");
c$.toRadians=$_M(c$,"toRadians",
function(angdeg){
return angdeg/180*3.141592653589793;
},"~N");
$_S(c$,
"E",2.718281828459045,
"PI",3.141592653589793,
"$random",null);
});
