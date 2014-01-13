/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var dataObjectM = new Array();
var usefulData;
var dataObject = new Array();   


$.getJSON("ClinicalFreeForm.json?studyId=ucec_tcga_pub",function(data) {
        $.each(data, function(i, field){
            if(i == "freeFormData")
                usefulData = field;
        });
        
        console.log(usefulData[0]["caseId"]);
        
        for(var i=0; i < usefulData.length;i++){
            if(usefulData[i]["caseId"] in dataObject){
                dataObject[usefulData[i]["caseId"]][usefulData[i]["paramName"]] = usefulData[i]["paramValue"];
                //console.log(usefulData[i]["caseId"]);                
            }
            else{
                dataObject[usefulData[i]["caseId"]] = new Array();
                dataObject[usefulData[i]["caseId"]][usefulData[i]["paramName"]] = usefulData[i]["paramValue"];
            }
        }
        
        var keys = Object.keys(dataObject);
        
        //console.log(keys);
        
        for(var j = 0; j< keys.length ; j++){
            dataObjectM[j] = new Array();
            dataObjectM[j]["CASE_ID"] = keys[j];
            for (var key in dataObject[keys[j]])
                dataObjectM[j][key] = dataObject[keys[j]][key];
        }
        
        
        /*
        $.each(usefulData,function(i,field){
            if(dataObject.hasOwnProperty(field["caseId"])){
                dataObject[field["caseId"]][field["paramName"]] = field["paramValue"];
            
            }
            else{
                dataObject[field["caseId"]] = new Array();
                dataObject[field["caseId"]][field["paramName"]] = field["paramValue"];
            }
            //alert(dataObject.length);
        });
        */
    });
