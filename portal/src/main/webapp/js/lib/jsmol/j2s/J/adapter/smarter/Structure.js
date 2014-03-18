Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (null, "J.adapter.smarter.Structure", ["J.constant.EnumStructure"], function () {
c$ = Clazz.decorateAsClass (function () {
this.structureType = null;
this.substructureType = null;
this.structureID = null;
this.serialID = 0;
this.strandCount = 0;
this.startChainID = 0;
this.startChainStr = null;
this.startInsertionCode = '\0';
this.endChainID = 0;
this.endChainStr = null;
this.endInsertionCode = '\0';
this.startSequenceNumber = 0;
this.endSequenceNumber = 0;
this.atomStartEnd = null;
this.modelStartEnd = null;
Clazz.instantialize (this, arguments);
}, J.adapter.smarter, "Structure");
Clazz.prepareFields (c$, function () {
this.atomStartEnd =  Clazz.newIntArray (2, 0);
this.modelStartEnd = [-1, -1];
});
c$.getHelixType = $_M(c$, "getHelixType", 
function (type) {
switch (type) {
case 1:
return J.constant.EnumStructure.HELIXALPHA;
case 3:
return J.constant.EnumStructure.HELIXPI;
case 5:
return J.constant.EnumStructure.HELIX310;
}
return J.constant.EnumStructure.HELIX;
}, "~N");
Clazz.makeConstructor (c$, 
function (modelIndex, structureType, substructureType, structureID, serialID, strandCount) {
this.structureType = structureType;
this.substructureType = substructureType;
if (structureID == null) return;
this.setModels (modelIndex, 0);
this.structureID = structureID;
this.strandCount = strandCount;
this.serialID = serialID;
}, "~N,J.constant.EnumStructure,J.constant.EnumStructure,~S,~N,~N");
$_M(c$, "set", 
function (startChainID, startSequenceNumber, startInsertionCode, endChainID, endSequenceNumber, endInsertionCode, istart, iend) {
this.startChainID = startChainID;
this.startSequenceNumber = startSequenceNumber;
this.startInsertionCode = startInsertionCode;
this.endChainID = endChainID;
this.endSequenceNumber = endSequenceNumber;
this.endInsertionCode = endInsertionCode;
this.atomStartEnd[0] = istart;
this.atomStartEnd[1] = iend;
}, "~N,~N,~S,~N,~N,~S,~N,~N");
$_M(c$, "setModels", 
function (model1, model2) {
this.modelStartEnd[0] = model1;
this.modelStartEnd[1] = (model2 == 0 ? model1 : model2);
}, "~N,~N");
});
