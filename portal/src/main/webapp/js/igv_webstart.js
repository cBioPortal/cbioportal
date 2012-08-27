// $Id: webstart.js,v 1.13 2008-01-14 15:05:28 grossben Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
 ** Modified by Jim Robinson for use with IGV
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/



/*
 * Javascript library to communication with java webstart application
 */

// globals
var SCRIPT_ELEMENT_ID = "igv";
var timeoutVar; // used to set/unset timeout handlers
var sessionURL; // the session (or data) url
var genome; // the genome parameter
var locus;  // the locus parameter
var name;   // the name parameter
var merge;

/*
 * Function to determine webstart version - taken from sun site
 */
function webstartVersionCheck(versionString) {
    // Mozilla may not recognize new plugins without this refresh
    navigator.plugins.refresh(true);
    // First, determine if Web Start is available
    if (navigator.mimeTypes['application/x-java-jnlp-file']) {
        // Next, check for appropriate version family
        for (var i = 0; i < navigator.mimeTypes.length; ++i) {
            var pluginType = navigator.mimeTypes[i].type;
            if (pluginType == "application/x-java-applet;version=" + versionString) {
                return true;
            }
        }
    }
    return true;
}

/*
 * Handler function to launch IGV via java web start.  This handler is scheduled in the appRequest() function, and
 * is canceled by the callBack() function called in the response to the "localhost" request.  If callBack() is not
 * invoked we conclude IGV is not running and launch it via Java WebStart.
 */
function timeoutHandler() {

    // construct webstart url
//    var hostname = window.location.hostname;
//    var port = window.location.port;
//    if (port) {
//        hostname += (":" + port);
//    }
    // note: context_path is set in stylesAndScripts.jsp
    var webstart_url = "http://www.broadinstitute.org/igv/projects/current/igv.php";

    if (sessionURL) {
        webstart_url += "?sessionURL=" + sessionURL;
        if (genome) {
            webstart_url += "&genome=" + genome;
        }
        if (locus) {
            webstart_url += "&locus=" + locus;
        }
        if (name) {
            webstart_url += "&name=" + name;
        }
        if (merge) {
            webstart_url += "&merge=" + merge;
        }
    }

    // determine if webstart is available - code taken from sun site
    var userAgent = navigator.userAgent.toLowerCase();
    // user is running windows
    if (userAgent.indexOf("msie") != -1 && userAgent.indexOf("win") != -1) {
        document.write("<OBJECT " +
            "codeBase=http://java.sun.com/update/1.5.0/jinstall-1_5_0_05-windows-i586.cab " +
            "classid=clsid:5852F5ED-8BF4-11D4-A245-0080C6F74284 height=0 width=0>");
        document.write("<PARAM name=app VALUE=" + webstart_url + ">");
        document.write("<PARAM NAME=back VALUE=true>");
        // alternate html for browsers which cannot instantiate the object
        document.write("<A href=\"http://java.sun.com/j2se/1.5.0/download.html\">Download Java WebStart</A>");
        document.write("</OBJECT>");
    }
    // user is not running windows
    else if (webstartVersionCheck("1.6")) {
        window.location = webstart_url;
    }
    // user does not have jre installed or lacks appropriate version - direct them to sun download site
    else {
        window.open("http://jdl.sun.com/webapps/getjava/BrowserRedirect?locale=en&host=java.com",
            "needdownload");
    }
}

/*
 * This function is called by IGV in the response to the GET request to load the data.  It cancels the JNLP load.
 */
function callBack() {
    clearTimeout(timeoutVar);
}

/*
 * Called to disable a link to the webstart.
 */
function disableLink(linkID) {

    var link = document.getElementById(linkID);
    if (link) {
        link.onclick = function() {
            return false;
        };
        link.style.cursor = "default";
        link.style.color = "#000000";
    }
}

/**
 * This function is called from a link or button to load data into IGV.  First,  an attempt is made to load the
 * supplied data into a running IGV.  If this is not successful, as detected by a failure to cancel the timeoutHandler,
 * IGV is launched by JNLP.
 *
 * The first 2 arguments are required.  Remaining arguments are optional but must appear in the prescribed order.
 *
 * @param port -- the IGV port, typically 60151
 * @param dataUrl -- an http or ftp url to the data.
 * @param genomeID -- the genomeID,  e.g. hg18
 * @param mergeFlag -- flag to indicate if data should be merged with current IGV session, or should start a new session
 * @param locusString -- an IGV locus string, e.g. chr1:100,000-200,000  or EGFR.  See IGV doc for full details
 * @param trackName -- name for the track resulting from dataURL.  This only works for "single-track" formats, e.g. wig.
 */
function appRequest(port, dataUrl, genomeID, mergeFlag, locusString, trackName) {

    // be good and remove the previous cytoscape script element
    // although, based on debugging, i'm not sure this really does anything
    var oldScript = document.getElementById(SCRIPT_ELEMENT_ID);
    if (oldScript) {
        oldScript.parentNode.removeChild(oldScript);
    }

    var localURL = "http://127.0.0.1:" + port + "/load?file=" + dataUrl + "&callback=callBack();";

    sessionURL = dataUrl;
    genome = genomeID;
    locus = locusString;
    merge = mergeFlag;
    name = trackName;


    if (genomeID) {
        localURL += "&genome=" + genomeID;
    }
    if (locusString) {
        localURL += "&locus=" + locusString;
    }
    if (mergeFlag) {
        localURL += "&merge=" + mergeFlag;
    }
    if (trackName) {
        localURL += "&name=" + trackName;
    }


    // create new script
    var newScript = document.createElement("script");
    newScript.id = SCRIPT_ELEMENT_ID;
    newScript.setAttribute("type", "text/javascript");
    newScript.setAttribute("src", localURL);

    // add new script to document (head section)
    var head = document.getElementsByTagName("head")[0];
    head.appendChild(newScript);

    // disable link
    // we do this because some browsers
    // will not fetch data if the url has been fetched in the past
    //disableLink("1");

    // set timeout - handler for when IGV is not running
    timeoutVar = setTimeout("timeoutHandler()", 2000);

}

