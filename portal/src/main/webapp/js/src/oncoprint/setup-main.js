$(document).ready(function () {

    //whether this tab has already been initialized or not:
    var tab_init = false;
    //function that will listen to tab changes and init this one when applicable:
    function tabsUpdate() {
        if ($("#summary").is(":visible")) {
            if (tab_init === false) {
                tab_init = true;
                CreateCBioPortalOncoprintWithToolbar('#oncoprint #everything', '#oncoprint #oncoprint-diagram-toolbar-buttons');
            }
            $(window).trigger("resize");
        }
    }
    //this is for the scenario where the tab is open by default (as part of URL >> #tab_name at the end of URL):
    tabsUpdate();
    //this is for the scenario where the user navigates to this tab:
    $("#tabs").bind("tabsactivate", function(event, ui) {
        tabsUpdate();
    });


});