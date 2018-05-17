$(function() {

    $("video").on("play", function() {
        App.hideFloatingButton()
    });

    $("video").on("ended", function() {
        App.hideFloatingButton()
    });

});