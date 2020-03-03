
function clearDevState(e){
    localStorage.removeItem('localdev');
    localStorage.removeItem('localdist');
    localStorage.removeItem('netlify');
    window.location.reload();
}

function showFrontendPopup(url) {
    document.addEventListener("DOMContentLoaded", function(event) {
        var newDiv = document.createElement("div");
        newDiv.setAttribute('style', 'position: fixed; z-index:100; top: 0; left: 0; width: 100%');
        document.body.appendChild(newDiv);
        newDiv.innerHTML = '<div style="">' +
            '<div class="alert alert-warning">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
            'cbioportal-frontend dev mode, using ' + url + '&nbsp;<a href="" onclick="javascript:clearDevState();window.location.reload()">clear</a>' +
            '</div>' +
            '</div>';
        newDiv.onclick=function(){
            document.body.removeChild(this);
        }
    });
}

window.loadReactApp = function(config) {
    // Set frontend route to /patient
    window.defaultRoute = '/' + config.defaultRoute;

    if (window.frontendConfig.frontendUrl === undefined) {
        // this should never happen
        if (console.error) {
            console.error('ERROR: No frontend URL defined, should at least be empty string');
        } else {
            console.log('ERROR: No frontend URL defined, should at least be empty string');
        }
    }
    if (window.localdev || window.localdist || localStorage.netlify) {
        showFrontendPopup(window.frontendConfig.frontendUrl);
    }
    document.write('<script type="text/javascript" charset="UTF-8" src="' + window.frontendConfig.frontendUrl + 'reactapp/common.bundle.js?'+ window.frontendConfig.appVersion +'"></scr' + 'ipt>');
    document.write('<script type="text/javascript" charset="UTF-8" src="' + window.frontendConfig.frontendUrl + 'reactapp/main.app.js?'+ window.frontendConfig.appVersion +'"></scr' + 'ipt>');

};

window.loadAppStyles = function() {
    document.write('<link rel="stylesheet" type="text/css" href="' + window.frontendConfig.frontendUrl + 'reactapp/prefixed-bootstrap.min.css?'+ window.frontendConfig.appVersion +'" />');
    // localdev gets styling in another manner
    if (!window.localdev) {
        document.write('<link rel="stylesheet" type="text/css" href="' + window.frontendConfig.frontendUrl + 'reactapp/styles.css?'+ window.frontendConfig.appVersion +'" />');
    }
};

(function(){

    var appReady = false;
    var queue = [];
    window.onReactAppReady = function(arg){

        if (arguments.length === 0) {
            appReady = true;
        }

        queue.push(arg || function(){});
        if (appReady) {
            queue.forEach(function(item){
                if (typeof item === 'function') item();
            });
            queue = [];
        }
    }

}());
