
function clearDevState(e){
    localStorage.removeItem('localdev');
    localStorage.removeItem('heroku');
    window.location.reload();
}

window.loadReactApp = function(config) {
    
    // Set frontend route to /patient
    window.defaultRoute = '/' + config.defaultRoute;
    
    if (localStorage.getItem('localdev') === "true") {
        // Use cbioportal-frontend localhost:3000 for dev
        document.write('<link rel="stylesheet" type="text/css" href="//localhost:3000/reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<script src="//localhost:3000/reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="//localhost:3000/reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
        // Show alert
        
        document.addEventListener("DOMContentLoaded", function(event) {
            var newDiv = document.createElement("div");
            newDiv.setAttribute('style', 'position: fixed; z-index:100; top: 0; left: 0; width: 100%');
            document.body.appendChild(newDiv);
            newDiv.innerHTML = '<div style="">' +
                '<div class="alert alert-warning">' +
                '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
                'cbioportal-frontend dev mode, using localhost:3000' +
                '&nbsp;<a href="#" onclick="clearDevState()">clear</a>' +
                '</div>' +
                '</div>';
            newDiv.onclick=function(){
                document.body.removeChild(this);
            }
        });
        
    } else if (localStorage.getItem('heroku')) {
        var herokuInstance = '//' + localStorage.getItem('heroku') + '.herokuapp.com';
        document.write('<link rel="stylesheet" type="text/css" href="' + herokuInstance + '/reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + herokuInstance + '/reactapp/styles.css?'+ window.appVersion +'" />');
        document.write('<script src="' + herokuInstance + '/reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="' + herokuInstance + '/reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
    
        document.addEventListener("DOMContentLoaded", function(event) {
            var newDiv = document.createElement("div");
            newDiv.setAttribute('style', 'position: fixed; z-index:100; top: 0; left: 0; width: 100%');
            document.body.appendChild(newDiv);
            newDiv.innerHTML = '<div style="">' +
                '<div class="alert alert-warning">' +
                '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
                'cbioportal-frontend dev mode, using ' + herokuInstance +
                '</div>' +
                '</div>';
            newDiv.onclick=function(){
                document.body.removeChild(this);
            }
        });

    } else {
        if (window.frontendUrl === undefined) {
            // this should never happen
            if (console.error) {
                console.error('ERROR: No frontend URL defined, should at least be empty string');
            } else {
                console.log('ERROR: No frontend URL defined, should at least be empty string');
            }
        }
        // Use deployed sources//
        document.write('<link rel="stylesheet" type="text/css" href="' + window.frontendUrl + 'reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + window.frontendUrl + 'reactapp/styles.css?'+ window.appVersion +'" />');
        document.write('<script src="' + window.frontendUrl + 'reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="' + window.frontendUrl + 'reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
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
