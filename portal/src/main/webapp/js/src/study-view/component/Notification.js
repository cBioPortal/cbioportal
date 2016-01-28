// http://bootstrap-notify.remabledesigns.com/
function Notification() {

    // default settings
    var settings = {
        message_type: "success", //success, warning, danger, info
        allow_dismiss: false,
        newest_on_top: false,
        placement_from: "top",
        placement_align: "right",
        spacing: 10,
        delay: 5000,
        timer: 1000,
        custom_class:"geneAddedNotification"
    };

    // create a notification
    this.createNotification = function(notificationMessage, options) {
        //if the options isn’t null extend defaults with user options.
        if (options) $.extend(settings, options);

        // create the notification
        $.notify({
            message: notificationMessage,
        }, {
            // settings
            element: 'body',
            type: settings.message_type,
            allow_dismiss: settings.allow_dismiss,
            newest_on_top: settings.newest_on_top,
            showProgressbar: false,
            placement: {
                from: settings.placement_from,
                align: settings.placement_align
            },
            spacing: settings.spacing,
            z_index: 1031,
            delay: settings.delay,
            timer: settings.timer,
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            template: '<div data-notify="container" class="col-xs-11 col-sm-3 alert alert-{0} '+settings.custom_class+'" role="alert">' +
            '<button type="button" style="display: none" aria-hidden="true" class="close" data-notify="dismiss" >×</button>' +
            '<span data-notify="icon"></span> ' +
            '<span data-notify="title">{1}</span> ' +
            '<span data-notify="message">{2}</span>' +
            '<div class="progress" data-notify="progressbar">' +
            '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
            '</div>' +
            '<a href="{3}" target="{4}" data-notify="url"></a>' +
            '</div>'
        });


    }
}