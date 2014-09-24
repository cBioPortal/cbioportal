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
        //constract alteration frequency string
        var _str_freq = "";
        $.each(_rawStatObj, function(key, value) {
            _str_freq += key + "\t" + value;
        });
        console.log("_str_freq:" + _str_freq);
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
            console.log("in rendering text areas");
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
