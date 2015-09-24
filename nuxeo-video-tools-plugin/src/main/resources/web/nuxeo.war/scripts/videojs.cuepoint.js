(function() {
  var defaults = {
      '2.000000' : {
        text: 'randomtext'
      },
      '1551.000000' : {
        text: 'randomtext 2'
      },

    },
    extend = function() {
      var args, target, i, object, property;
      args = Array.prototype.slice.call(arguments);
      target = args.shift() || {};
      for (i in args) {
        object = args[i];
        for (property in object) {
          if (object.hasOwnProperty(property)) {
            if (typeof object[property] === 'object') {
              target[property] = extend(target[property], object[property]);
            } else {
              target[property] = object[property];
            }
          }
        }
      }
      return target;
    };

  /**
   * register the thubmnails plugin
   */
  videojs.plugin('cuepoints', function(options) {
    var div, settings, img, player, progressControl, duration;
    settings = extend(defaults, options);
    player = this;
    for (time in settings) {
      alert(time);

      // create the thumbnail
      div = document.createElement('div');
      div.className = 'vjs-cuepoint-holder';
      span = document.createElement('span');
      div.appendChild(span);
      span.text = time.src;
      span.className = 'vjs-cuepoint';
      alert(span);

      div.addEventListener('mouseclick', function(event) {
        player.currentTime(parseFloat(jQuery(this).attr('timecode')));
      }, false);

      // add the thumbnail to the player
      progressControl = player.controlBar.progressControl;
      progressControl.el().appendChild(div);

    }
  });
})();
