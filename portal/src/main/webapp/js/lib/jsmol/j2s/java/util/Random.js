Clazz.load(null,"java.util.Random",["java.lang.IllegalArgumentException"],function(){
c$=Clazz.decorateAsClass(function(){
this.haveNextNextGaussian=false;
this.seed=0;
this.nextNextGaussian=0;
Clazz.instantialize(this,arguments);
},java.util,"Random",null,java.io.Serializable);
Clazz.makeConstructor(c$,
function(){
this.setSeed(System.currentTimeMillis());
});
Clazz.makeConstructor(c$,
function(seed){
this.setSeed(seed);
},"~N");
Clazz.defineMethod(c$,"next",
function(bits){
this.seed=(this.seed*25214903917+0xb)&(281474976710655);
return(this.seed>>>(48-bits));
},"~N");
Clazz.defineMethod(c$,"nextBoolean",
function(){
return Math.random()>0.5;
});
Clazz.defineMethod(c$,"nextBytes",
function(buf){
for(var i=0;i<bytes.length;i++){
bytes[i]=Math.round(0x100*Math.random());
}
},"~A");
Clazz.defineMethod(c$,"nextDouble",
function(){
return Math.random();
});
Clazz.defineMethod(c$,"nextFloat",
function(){
return Math.random();
});
Clazz.defineMethod(c$,"nextGaussian",
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
Clazz.defineMethod(c$,"nextInt",
function(){
return Math.ceil(0xffff*Math.random())-0x8000;
});
Clazz.defineMethod(c$,"nextInt",
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
Clazz.defineMethod(c$,"nextLong",
function(){
return Math.ceil(0xffffffff*Math.random())-0x80000000;
});
Clazz.defineMethod(c$,"setSeed",
function(seed){
this.seed=(seed^25214903917)&(281474976710655);
this.haveNextNextGaussian=false;
},"~N");
Clazz.defineStatics(c$,
"multiplier",0x5deece66d);
});
