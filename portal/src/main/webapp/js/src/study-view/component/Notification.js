// http://bootstrap-notify.remabledesigns.com/
function Notification() {

    function getNotificationType(notificationType){
        var type;
        switch (notificationType.toUpperCase()){
            case "WARNING": type = "warning"; break;
            case "ERROR": type = "error"; break;
            case "INFO": type = "info"; break;
            default: type="success";
        }
        return type;
    }

    this.createNotification = function(notificationMessage, notificationType) {
        var type = getNotificationType(notificationType);

        $.notify({
            message: notificationMessage,
        }, {
            // settings
            element: 'body',
            type: type,
            allow_dismiss: false,
            newest_on_top: false,
            showProgressbar: false,
            placement: {
                from: "top",
                align: "center"
            },
            spacing: 10,
            z_index: 1031,
            delay: 5000,
            timer: 1000,
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            template: '<div data-notify="container" class="col-xs-11 col-sm-3 alert alert-{0}" style="width: 500px" role="alert">' +
            '<button type="button" aria-hidden="true" class="close" data-notify="dismiss">×</button>' +
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