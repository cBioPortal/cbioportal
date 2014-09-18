var DataDownloadTab = (function() {

    var _rawDataObj = {},
        _rawStatObj = {},
        _isRendered = false;

    function processData() {
        console.log(_rawDataObj);
        console.log(_rawStatObj);
    }

    function renderDownloadLinks() {
        $("#data_downlonad_links_li").append();
    }

    function renderTextareas() {
        
        var _str_freq = "GENE_SYMBOL" + "\t" + "NUM_CASES_ALTERED" + "\t" + "PERCENT_CASES_ALTERED" + "\n";
        $.each(_rawStatObj, function(key, value) {
            _str_freq += key + "\t" + value.total_alter_num + "\t" + value.percent + "%" + "\n";
        });
        $("#text_area_frenquency").append(_str_freq);


        $("#text_area_type_of_alteration").append();
        $("#text_area_case_affected").append();
        $("#text_area_case_matrix").append();
    }

    return {
        setInput: function(_inputData) {
            _rawDataObj = _inputData;
        },
        setStat: function(_inputData) {
            _rawStatObj = _inputData;
        },
        init: function() {
            processData();
            renderDownloadLinks();
            renderTextareas();
            _isRendered = true;
        },
        isRendered: function() {
            return _isRendered;
        }
    };

}());

$(document).ready( function() {
    
    //Sign up getting oncoprint data
    PortalDataCollManager.subscribeOncoprint(function() {
        DataDownloadTab.setInput(PortalDataColl.getOncoprintData());
    });
    PortalDataCollManager.subscribeOncoprintStat(function() {
        DataDownloadTab.setStat(PortalDataColl.getOncoprintStat()); 
    });

});

//Bind tab clicking event listener
$("#tabs").bind("tabsactivate", function(event, ui) {
    if (ui.newTab.text().trim().toLowerCase() === "download") {
        if (!DataDownloadTab.isRendered()) {
            DataDownloadTab.init();
        } 
    }
});
if ($("#data_download").is(":visible")) {
    if (!DataDownloadTab.isRendered()) {
        DataDownloadTab.init();
    }
}
