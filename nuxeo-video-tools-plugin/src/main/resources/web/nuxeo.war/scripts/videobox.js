function initVideoJs(videoElement,sliderOn) {

  var video = vjs(videoElement);
  video.hideControlTime();

  // set sliders
  if (sliderOn === 'true') {
    video.rangeslider({hidden:false});
  } else {
    video.rangeslider({hidden:true});
  }

  var current = jQuery('#videobox_current_timestamp :input').first();

  video.on('timeupdate',function() {
    if (current !== undefined) current.attr('value',''+videojs.round(video.currentTime(),3));
  });

  if (sliderOn === 'true') {
    var start = jQuery('#videobox_start_timestamp :input').first();
    var end = jQuery('#videobox_end_timestamp :input').first();

    video.on('sliderchange',function() {
      var values = video.getValueSlider();
      if (start !== undefined) start.attr('value',''+videojs.round(values.start,3));
      if (end !== undefined) end.attr('value',videojs.round(values.end,3));
    });
  }

  video.ready(function(){
    this.play();
    this.pause();
  });

}
