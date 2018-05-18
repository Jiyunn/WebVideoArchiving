$(function() {

    $("video").on("play", function() {
//        App.ShowFloatingButton()
    });

    $("video").on("ended", function() {
        App.hideFloatingButton()
    });

});