var mutationTranslator = function(mutationDetail) {
    
     vocabulary = {  //Key and "typeName" are always identical
            frameshift : {
                type : "Frameshift",
                vals: [
                    "Frame_Shift_Del", 
                    "Frame_Shift_Ins", 
                    "frameshift insertion", 
                    "frameshift", 
                    "frameshift_insertion", 
                    "Frameshift deletion",
                    "FRAMESHIFT_CODING"
                ]
            },
            nonsense : {
                type : "Nonsense",
                vals: ["Nonsense_Mutation", "Nonsense"]
            },
            splice : {
                type : "Splice",
                vals : [
                    "Splice_Site", 
                    "Splice_Site_SNP", 
                    "splicing", 
                    "splice", 
                    "ESSENTIAL_SPLICE_SITE"
                ]
            },
            in_frame : {
                type : "In_frame",
                vals : [
                    "In_Frame_Del", 
                    "In_Frame_Ins"
                ]
            },
            nonstart : {
                type : "Nonstart",
                vals : ["Translation_Start_Site"]
            },
            nonstop : {
                type : "Nonstop",
                vals : ["NonStop_Mutation"]
            },
            missense : {
                type : "Missense",
                vals : [
                    "Missense_Mutation", 
                    "Missense"
                ]
            },
            other: {
                type : "Other",
                vals : [
                    "COMPLEX_INDEL", 
                    "5'Flank", 
                    "Fusion", 
                    "vIII deletion", 
                    "Exon skipping", 
                    "exon14skip"
                ]
            }
        };
        
    for(var key in vocabulary) {
        if ($.inArray(mutationDetail, vocabulary[key].vals) !== -1) {
            return vocabulary[key].type;
        }
    }
    return vocabulary.other.type; //categorize all other mutations as other 

};