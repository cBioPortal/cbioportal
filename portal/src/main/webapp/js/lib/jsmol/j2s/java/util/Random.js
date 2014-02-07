$_L(null,"java.util.Random",["java.lang.IllegalArgumentException"],function(){
c$=$_C(function(){
this.haveNextNextGaussian=false;
this.seed=0;
this.nextNextGaussian=0;
$_Z(this,arguments);
},java.util,"Random",null,java.io.Serializable);
$_K(c$,
function(){
this.setSeed(System.currentTimeMillis());
});
$_K(c$,
function(seed){
this.setSeed(seed);
},"~N");
$_M(c$,"next",
function(bits){
this.seed=(this.seed*25214903917+0xb)&(281474976710655);
return(this.seed>>>(48-bits));
},"~N");
$_M(c$,"nextBoolean",
function(){
return Math.random()>0.5;
});
$_M(c$,"nextBytes",
function(buf){
for(var i=0;i<bytes.length;i++){
bytes[i]=Math.round(0x100*Math.random());
}
},"~A");
$_M(c$,"nextDouble",
function(){
return Math.random();
});
$_M(c$,"nextFloat",
function(){
return Math.random();
});
$_M(c$,"nextGaussian",
function(){
if(this.haveNextNextGaussian){
this.haveNextNextGaussian=false;
return this.nextNextGaussian;
}var v1;
var v2;
var s;
do{
v1=2*this.nextDouble()-1;
v2=2*this.nextDouble()-1;
s=v1*v1+v2*v2;
}while(s>=1);
var norm=Math.sqrt(-2*Math.log(s)/s);
this.nextNextGaussian=v2*norm;
this.haveNextNextGaussian=true;
return v1*norm;
});
$_M(c$,"nextInt",
function(){
return Math.ceil(0xffff*Math.random())-0x8000;
});
$_M(c$,"nextInt",
function(n){
if(n>0){
if((n&-n)==n){
return((n*this.next(31))>>31);
}var bits;
var val;
do{
bits=this.next(31);
val=bits%n;
}while(bits-val+(n-1)<0);
return val;
}throw new IllegalArgumentException();
},"~N");
$_M(c$,"nextLong",
function(){
return Math.ceil(0xffffffff*Math.random())-0x80000000;
});
$_M(c$,"setSeed",
function(seed){
this.seed=(seed^25214903917)&(281474976710655);
this.haveNextNextGaussian=false;
},"~N");
$_S(c$,
"multiplier",0x5deece66d);
});
