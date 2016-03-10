var system = require('system');
var args = system.args;

if (args.length !== 4) {
    console.log("usage: phantomjs make_screenshot.js url screenshot delay");
    console.log(args);
    phantom.exit(1);
}

takeScreenshot = function(url, screenshot, delay) {
    var page = require('webpage').create();
    page.viewportSize = { width: 1000, height: 1000 };

    page.onResourceError = function(resourceError) {
        page.reason = resourceError.errorString;
        page.reason_url = resourceError.url;
    };

    page.open(url, function(status) {
        if (status !== 'success') {
                console.log('Error opening url ' + page.reason_url + ":" + page.reason);
                phantom.exit(1);
            } else {
                window.setTimeout(function () {
                    page.render(screenshot);
                    phantom.exit(0);
                }, delay);
            }
    });
}

takeScreenshot(args[1], args[2], parseInt(args[3]));
