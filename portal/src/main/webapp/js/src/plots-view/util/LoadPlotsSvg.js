// Takes the content in the plots svg element
// and returns XML serialized *string*
function loadPlotsSVG() {
    //Remove the help icons
    var elemXHelpTxt = $(".x-title-help").qtip('api').options.content.text;
    var elemYHelpTxt = $(".y-title-help").qtip('api').options.content.text;
    var elemXHelp = $(".x-title-help").remove();
    var elemYHelp = $(".y-title-help").remove();
    //Extract SVG
    var result = $("#plots_box").html();
    //Add the help icons back on
    $(".axis").append(elemXHelp);
    $(".axis").append(elemYHelp);
    $(".x-title-help").qtip({
        content: {text: elemXHelpTxt },
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'left bottom',at:'top right'}
    });
    $(".y-title-help").qtip({
        content: {text: elemYHelpTxt },
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'right bottom',at:'top left', viewport: $(window)}
    });

    return result;
}