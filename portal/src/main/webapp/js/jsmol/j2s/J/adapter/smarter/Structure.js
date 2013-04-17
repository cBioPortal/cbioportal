Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (["J.adapter.smarter.AtomSetObject"], "J.adapter.smarter.Structure", ["J.constant.EnumStructure"], function () {
c$ = Clazz.decorateAsClass (function () {
this.structureType = null;
this.substructureType = null;
this.structureID = null;
this.serialID = 0;
this.strandCount = 0;
this.startChainID = '\0';
this.startInsertionCode = '\0';
this.endChainID = '\0';
this.endInsertionCode = '\0';
this.startSequenceNumber = 0;
this.endSequenceNumber = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.smarter, "Structure", J.adapter.smarter.AtomSetObject);
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
Clazz.superConstructor (this, J.adapter.smarter.Structure, []);
this.structureType = structureType;
this.substructureType = substructureType;
if (structureID == null) return;
this.atomSetIndex = modelIndex;
this.structureID = structureID;
this.strandCount = strandCount;
this.serialID = serialID;
}, "~N,J.constant.EnumStructure,J.constant.EnumStructure,~S,~N,~N");
$_M(c$, "set", 
function (startChainID, startSequenceNumber, startInsertionCode, endChainID, endSequenceNumber, endInsertionCode) {
this.startChainID = startChainID;
this.startSequenceNumber = startSequenceNumber;
this.startInsertionCode = startInsertionCode;
this.endChainID = endChainID;
this.endSequenceNumber = endSequenceNumber;
this.endInsertionCode = endInsertionCode;
}, "~S,~N,~S,~S,~N,~S");
});
