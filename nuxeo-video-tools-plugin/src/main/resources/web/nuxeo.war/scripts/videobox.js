function initVideoJs(videoElement,sliderOn) {
  var video = vjs(videoElement);
  // set sliders
  if (sliderOn === 'true') {
    video.rangeslider({hidden:false,controlTime:false});
  } else {
    video.rangeslider({hidden:true,controlTime:false});
  }

  var current = jQuery('#videobox_current_timestamp :input').first();

  var autostop = true;

  video.on('timeupdate',function() {
    if (current !== undefined) current.attr('value',''+videojs.round(video.currentTime(),3));
    if (autostop) {
      autostop = false;
      this.pause();
      this.currentTime(0);
    }
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
}
