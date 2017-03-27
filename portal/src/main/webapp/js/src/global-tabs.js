/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

$(document).ready(function() {
    var pathname = window.location.pathname;
    var start = pathname.lastIndexOf("/")+1;
    var filename = pathname.substring(start);

    $('#main-nav ul li').each(function(index) {
        var currentPage = $(this).find('a').attr('href');
        if (currentPage == filename) {
            $('#main-nav ul li').removeClass('selected');
            $(this).addClass('selected');
            return false;
        }
    });

    return false;
});

window.loadReactApp = function(config) {

    // Set frontend route to /patient
    window.defaultRoute = `/${config.defaultRoute}`;


    if (localStorage.getItem('localdev') === "true") {
        // Use cbioportal-frontend localhost:3000 for dev
        document.write('<link rel="stylesheet" type="text/css" href="http://localhost:3000/reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<script src="http://localhost:3000/reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="http://localhost:3000/reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
        // Show alert
        document.write('<div style="position: fixed; top: 0; left: 0; width: 100%;">' +
            '<div class="alert alert-warning">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
            'cbioportal-frontend dev mode, using localhost:3000' +
            '</div>' +
            '</div>');
    } else if (localStorage.getItem('heroku')) {
        var herokuInstance = 'https://' + localStorage.getItem('heroku') + '.herokuapp.com';
        document.write('<link rel="stylesheet" type="text/css" href="' + herokuInstance + '/reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<link rel="stylesheet" type="text/css" href="' + herokuInstance + '/reactapp/styles.css?'+ window.appVersion +'" />');
        document.write('<script src="' + herokuInstance + '/reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="' + herokuInstance + '/reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
        // Show alert
        document.write('<div style="position: fixed; top: 0; left: 0; width: 100%;">' +
            '<div class="alert alert-warning">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
            'cbioportal-frontend dev mode, using ' + herokuInstance +
            '</div>' +
            '</div>');
    } else {
        // Use deployed sources//
        document.write('<link rel="stylesheet" type="text/css" href="reactapp/prefixed-bootstrap.min.css?'+ window.appVersion +'" />');
        document.write('<link rel="stylesheet" type="text/css" href="reactapp/styles.css?'+ window.appVersion +'" />');
        document.write('<script src="reactapp/common.bundle.js?'+ window.appVersion +'"></scr' + 'ipt>');
        document.write('<script src="reactapp/main.app.js?'+ window.appVersion +'"></scr' + 'ipt>');
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



   