$(function() {

    $("video").on("ended", function() {
        App.hideFloatingButton()
    });

});