window.initModifyQueryComponent = function(modifyQueryButtonId, querySelectorId) {
    var selectorInitialized = false;
    var selectorVisible = false;

    setTimeout(function() {
        $("#" + modifyQueryButtonId).show();
    }, 2000);

    $("#" + modifyQueryButtonId).click(function() {
        if (!selectorInitialized) {
            window.renderQuerySelector(document.getElementById(querySelectorId), {showDownloadTab:false});
            selectorInitialized = true;
        }

        // toggle visibility
        selectorVisible = !selectorVisible;

        if (selectorVisible) {
            $("#" + querySelectorId).slideDown();
        }
        else {
            $("#" + querySelectorId).slideUp();
        }
    });
};
