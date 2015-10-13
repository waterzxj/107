/**
Requirement: jQuery, Baidu Map, common.js, paired css style(file), paired imgs
Author: oyyd(oyydoibh@gmail.com)
**/
(function($) {
  var options = {
    map: { //read-only
      width: 360,
      height: 360
    },
    pluginName: '107room.beijing-district',
    className: 'beijing-district',
    MAX_SHOWN_SUBSCRIBER: 20,
    MIN_RADIUS_ARRAY: {
      10: 0.08,
      11: 0.04,
      12: 0.025,
      13: 0.015,
      14: 0.007,
      15: 0.003,
      16: 0.0015,
      17: 0.00075,
      18: 0.000375
    }
  };

  // Customer user head overlay
  var HeadOverlay = function(point, picUrl, deleteCount, nickname, click) {
    this._point = point;
    this._picUrl = picUrl;
    this._imgWidth = this._imgHeight = 36;
    this._deleteCount = deleteCount;
    this._countR = 19;
    if (nickname) {
      this._nickname = nickname;
    }
    this._click = click;
  };
  HeadOverlay.prototype = new BMap.Overlay();
  HeadOverlay.prototype.initialize = function(map) {
    var that = this;
    this._map = map;
    var div = this._div = document.createElement('div');
    var img = this._img = document.createElement('img');
    div.style.position = 'absolute';
    div.style.height = this._imgHeight + 'px';
    div.style.width = this._imgWidth + 'px';
    div.style.border = '2px solid #00ac97';
    div.style.borderRadius = '50%';
    div.style.display = 'none';
    if (this._nickname) {
      var title = this._nickname;
      if (this._deleteCount) {
        title += '等' + (this._deleteCount + 1) + '人';
      }
      div.title = title;
    }
    img.style.borderRadius = '50%';
    img.style.height = this._imgHeight + 'px';
    img.style.width = this._imgWidth + 'px';
    img.src = this._picUrl;
    // show pictures after loading
    img.onload = function(){
      that._div.style.display = 'block';
    };    
    img.style.cursor = 'pointer';
    img.className = 'user-img';
    img.onclick = function(){
      that._click && that._click(that._point);
    };
    div.appendChild(img);

    if (this._deleteCount > 0) {
      // img containing label should be upper
      div.style.zIndex = 100 + this._deleteCount;
      var countP = this._countP = document.createElement('p');
      countP.style.display = 'inline-block';
      countP.style.position = 'absolute';
      countP.style.color = '#FFF';
      countP.style.width = this._countR + 'px';
      countP.style.height = this._countR + 'px';
      countP.style.top = '-2px';
      countP.style.right = '-5px';
      countP.style.textAlign = 'center';
      countP.style.fontSize = '14px';
      countP.style.backgroundColor = '#00ac97';
      countP.style.borderRadius = '50%';
      var num = this._num = document.createElement('span');
      num.innerHTML = this._deleteCount + 1;      
      countP.appendChild(num);
      div.appendChild(countP);
    }

    map.getPanes().labelPane.appendChild(div);
    return div;
  };

  HeadOverlay.prototype.draw = function() {
    var pixel = this._map.pointToOverlayPixel(this._point);
    this._div.style.left = pixel.x - this._imgWidth / 2 + 'px';
    this._div.style.top = pixel.y - this._imgHeight / 2 + 'px';
    // make center marker higher than other makers
    // this is a hacky way to set BMap_Marker higher than other overlays,
    // as official apis do not work.
    this._div.parentNode.style.zIndex = 400;
  };

  // init beijing-district  
  // parameter `path` is the static file path
  $.fn.district = function(path) {
    var callback = null;
    var mouseEnterTrigger = null;
    var mouseClickTrigger = null;
    var city = '北京';
    var addr = '北京';
    var curValue = '';
    var map = null;
    var mapLv = 13;
    var currentCenterOverlay = null;
    // Use this to fix the bug when BMap centering at first time.
    var isFirstCentering = true;
    var _subscribers = [];
    var headOverlays = [];
    // check params
    if (!!!path) {
      throw Error(options.pluginName + ': invalid path');
    }

    // handle params
    if (path[path.length - 1] !== '/') {
      path = path + '/';
    }

    options.path = path;

    //init style
    var $e = $(this);
    var $svg = $e;
    $e.addClass(options.className);

    // init districts
    var districts = $e.find('[data-district]');
    districts.each(function() {
      var $district = $(this),
        $districtItem = $e.find('[data-district-name="' + $district.attr('id') + '"]'),
        $districtText = $districtItem.find('text');

      var clickTrigger = function() {
        var $e = $(this);
        $svg.find('[data-district-name]').not($e).removeAttr('active');
        $e.attr('active', '');
        curValue = $e.attr('data-district-name');
        callback && callback(curValue);
      };

      $districtItem.click(clickTrigger);

      $districtItem.mouseenter(function() {
        var district = $(this).attr('data-district-name');
        // showDistrict(district);
        showEle($district);
        activateText($districtText);

        mouseEnterTrigger && mouseEnterTrigger(district);
      });

      $districtItem.click(function() {
        var district = $(this).attr('data-district-name');
        mouseClickTrigger && mouseClickTrigger(district);
      });

      $districtItem.mouseleave(function() {
        hideEle($district);
        inactivateText($districtText);
      });

      function showDistrict(districtName) {
        $e.find('[data-district]').each(function() {
          var $e = $(this);
          if ($e.attr('id') !== districtName) {
            hideEle($e);
          } else {
            showEle($e);
          }
        });
      }

      function hideEle($e) {
        $e.stop();
        $e.css({
          opacity: 0.3
        });
      }

      function showEle($e) {
        $e.animate({
          opacity: 1
        });
      }

      function activateText($e) {
        $e.css({
          fill: '#fff',
          opacity: 1
        });
      }

      function inactivateText($e) {
        $e.css({
          fill: '#000',
          opacity: 0.5
        });
      }
    });

    // init animation circle
    $circle = $('<div class="circle"></div>');
    $e.append($circle);

    // init bmap
    var $map = $('<div id="map" class="map hide"></div>');
    $e.append($map);

    map = new BMap.Map('map',{
      enableMapClick: false
    });

    map.centerAndZoom(new BMap.Point(116.404, 39.915), 11);
    map.setMapStyle(room107GenMapStyle({
      styleSubwayWeight: 1
    }));
    map.setMinZoom(10);
    map.setCurrentCity(city);
    map.enableScrollWheelZoom();

    // init zoom buttons
    var $panel = $('<div class="panel"></div>');
    var $zoomInBtn = $('<img class="zoomBtn in" role="zoom" src="' + path + 'image/icon/map/zoomin.png"/>');
    var $zoomOutBtn = $('<img class="zoomBtn out" role="zoom" src="' + path + 'image/icon/map/zoomout.png"/>');
    $panel.append($zoomInBtn);
    $panel.append($zoomOutBtn);
    $map.append($panel);

    $zoomInBtn.click(function() {
      map.zoomIn();
    });

    $zoomOutBtn.click(function() {
      map.zoomOut();
    });

    // funcions
    var addMark = function(data) {
      var marker = new HeadOverlay(
        new BMap.Point(data.x, data.y), 
        data.url, 
        data.deleteCount, 
        data.title,
        function(point){
          var currentPosition = map.getCenter();
          map.panTo(point);
          setTimeout(function(){
            map.zoomIn();
          }, 600);
        }
      );
      map.addOverlay(marker);
      headOverlays.push(marker);
    };

    var removeHeadOverlays = function() {
      for (var index in headOverlays) {
        map.removeOverlay(headOverlays[index]);
      }
      headOverlays = [];
    };

    var addCenterMark = function(x, y) {
      // argument is point
      var point = null;
      var firstTime = (currentCenterOverlay)?false:true;
      if (arguments.length === 1) {
        point = x;
      }
      if (!point) {
        point = new BMap.Point(x, y);
      }
      var markerIcon = new BMap.Icon(options.path + 'image/icon/map/add-normal.png', new BMap.Size(26, 31));
      currentCenterOverlay = new BMap.Marker(point, {
        icon: markerIcon
      });      
      map.addOverlay(currentCenterOverlay);
      currentCenterOverlay.setAnimation(BMAP_ANIMATION_BOUNCE);      
    };

    var animate = function(cb) {
      $svg.find('svg').animate({
        opacity: 0
      }, 700);

      $circle.animate({
        width: options.map.width,
        height: options.map.height,
        top: 0,
        left: 0
      }, 700, cb).animate({
        opacity: 0
      }, 700, function() {
        $circle.addClass('hide');
      });
    };

    var addMarkers = function(markers) {
      for (var index = 0; index < markers.length; index++) {
        var marker = markers[index];
        addMark(marker);
      }
    };

    var getDistence = function(pos1, pos2) {
      var x1 = pos1.x,
        x2 = pos2.x,
        y1 = pos1.y,
        y2 = pos2.y;
      // if pos1 and pos2 are BMap.Point
      if(pos1.lng && pos2.lng){
        x1 = pos1.lng;
        x2 = pos2.lng;
        y1 = pos1.lat;
        y2 = pos2.lat;
      }
      return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    };

    var renderSubscribers = function(subscribers) {
      log('subscribers');
      log(subscribers);
      _subscribers = subscribers;
      _rerender();
    };

    map.addEventListener('zoomend', function() {
      _rerender();
    });

    var _rerender = function() {
      log('map lvl');
      log(map.getZoom());
      removeHeadOverlays();
      var subscriber = null;
      var count = options.MAX_SHOWN_SUBSCRIBER;
      var minRadius = 100;
      var deleteCount = 0;
      var points = [];

      if (options.MIN_RADIUS_ARRAY[map.getZoom()]) {
        minRadius = options.MIN_RADIUS_ARRAY[map.getZoom()];
      }
      for (var index = 0; index < _subscribers.length; index++) {
        subscriber = _subscribers[index];
        var isExist = true;
        // valid user imgs
        if (subscriber.faviconUrl && subscriber.x && subscriber.y) {
          for (var i = 0; i < points.length; i++) {
            var point = points[i];
            if (getDistence(point, subscriber) < minRadius) {
              // delete and push it              
              isExist = false;
              point.deleteCount++;
              break;
            }
          }

          if (isExist) {
            subscriber.deleteCount = 0;
            points.push({
              x: subscriber.x,
              y: subscriber.y,
              url: subscriber.faviconUrl,
              deleteCount: 0,
              title: subscriber.nickname
            });
          }
        }
      }

      addMarkers(points);
    };

    // return
    var res = {};
    res.setCallback = function(cb) {
      if (typeof cb !== 'function') {
        throw Error('invalid callback');
      }

      callback = cb;
    };

    res.showMap = function(cb) {
      animate(function() {
        $e.find('#map').removeClass('hide');
        $e.find('svg').attr('display', 'none');
        cb && typeof cb === 'function' && cb();
      });
    };

    res.hideMap = function() {
      $e.find('#map').attr('display', 'block');
      $e.find('svg').removeClass('hide');
    };

    res.setMapPosition = function(address, cb) {
      var addr = address;
      var addrGeo = new BMap.Geocoder();
      addrGeo.getPoint(addr, function(point) {
        if (point) {
          res.addCenterMark(point);
          res.setMapCenter(point);
          cb && cb(true);
        } else {
          console.err('Failed to get address point!');
          cb && cb(false);
        }
      }, city);
    };

    res.setMapCenter = function(point) {
      map.centerAndZoom(point, mapLv);
      if (isFirstCentering) {
        map.panBy(options.map.width / 2, options.map.height / 2);
        isFirstCentering = false;
      }
    };

    res.addMarkers = addMarkers;

    res.animate = function(cb) {
      animate(cb);
    };

    res.click = function(cb) {
      if (typeof cb !== 'function') {
        throw Error('invalid mouseenter callback');
      }
      mouseClickTrigger = cb;
    };

    res.mouseEnter = function(cb) {
      if (typeof cb !== 'function') {
        throw Error('invalid mouseenter callback');
      }
      mouseEnterTrigger = cb;
    };

    res.addCenterMark = addCenterMark;

    res.clearOverlays = function() {
      map.clearOverlays();
    };

    res.getMapZoom = function() {
      return map.getZoom();
    };

    res.renderSubscribers = renderSubscribers;

    return res;
  };
})(jQuery);