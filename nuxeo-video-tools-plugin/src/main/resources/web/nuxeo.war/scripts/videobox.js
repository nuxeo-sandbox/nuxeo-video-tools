function initVideoJs(videoElement) {

  var video = vjs(videoElement);
  video.rangeslider({hidden:false});
  video.hideControlTime();

  var start = jQuery('#videobox_start_timestamp :input').first();
  var end = jQuery('#videobox_end_timestamp :input').first();

  video.on("sliderchange",function() {
    var values = video.getValueSlider();
    start.attr('value',''+videojs.round(values.start,3));
    end.attr('value',videojs.round(values.end,3));
  });
}
