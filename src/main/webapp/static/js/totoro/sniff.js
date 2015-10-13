var maxRenderTime = 20;
var _requestAnimationFrame = ( typeof requestAnimationFrame != undefined) ? requestAnimationFrame : mozRequestAnimationFrame;
var sniffI = sniffSingleUrlCount, browser = "$!browser";
/**
 * @param putResult function to put result
 * @param sniffOver function called when all of the given URLs haven been sniffed
 */
function sniffUrl(urls, i, sniffInterval, putResult, sniffOver) {
    if (i >= urls.length) {
        sniffI = sniffSingleUrlCount;
        sniffOver();
        return;
    }
    var t = new Date().getTime(), url = urls[i];
    log("sniff url: urlId=" + url["urlId"] + ", url=" + url["url"]);
    var times = [], lastTime = null, timeCount = 5;
    function timeRender(time) {
        if (time) {
            if (lastTime != null) {
                times.push(parseInt(time - lastTime));
            }
            lastTime = time;
        }
        if (--timeCount >= -1) {
            window.requestAnimationFrame(timeRender);
        } else {// time end
            log(times);
            var browserBusy = false;
            // calculate
            if (times.length >= 2) {// for confidence
                var max = times[0], normalSum = 0;
                for (var j = 1; j < times.length; j++) {
                    if (times[j] > max) {
                        normalSum += max;
                        max = times[j];
                    } else {
                        normalSum += times[j];
                    }
                }
                var normalAvg = normalSum / (times.length - 1);
                if (normalAvg > maxRenderTime) {// browser busy
                    browserBusy = true;
                    log("avg normal time: " + normalAvg + " > " + maxRenderTime);
                } else {
                    var visited = (max * (times.length - 1) / normalSum) >= 3;
                    log("sniff result: visited=" + visited + ", time=" + (new Date().getTime() - t));
                    // send result
                    putResult({
                        "urlId" : url["urlId"],
                        "type" : url["type"],
                        "visited" : visited
                    });
                    log("--");
                }
            }
            if (browser != "ie") {// reset
                $("#sniffList a").each(function() {
                    $(this).attr("href", $(this).attr("href0")).css("color", "#fff").css("color", "");
                });
            }
            if (!browserBusy && (singleDebug || --sniffI == 0)) {
                sniffI = sniffSingleUrlCount;
                ++i;
            }
            setTimeout(function() {// sniff next
                sniffUrl(urls, i, sniffInterval, putResult, sniffOver);
            }, browserBusy ? sniffIntervalWhenBusy : sniffInterval);
        }
    }

    // time render
    if (browser == "ie") {// create URL
        var list = $("#sniffList").hide().empty();
        timeRender();
        var li = "<li><a href='" + url["url"] + "'>This is a health test for 107 ROOM. If you see this text, don't wory and please tell us for better user experience. 3X very much!</a></li>";
        list.append(li).show();
    } else {// reset URL
        timeRender();
        $("#sniffList a").each(function() {
            $(this).attr("href", url["url"]).css("color", "#fff").css("color", "");
        });
    }
}