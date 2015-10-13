var NumberSlider = {

    defaultOption : {
        'sliderNumber' : {
            '0-30' : 5,
            '30-60' : 10
        },
        'delta' : 1,
        'tipArrowClass' : 'tipArrowLeft'
    },

    create : function(inputContainer, option, input) {
        option = merge(NumberSlider.defaultOption, option);
        var popup = $('<div class="numberSlider popup"><div class="numberSliderContent" style=""><div class="numberSliderTop"></div><div class="numberSliderMiddle"><div class="numberSliderDrag"></div></div><div class="numberSliderBottom"></div></div></div>');
        var r = Popup.create(popup, inputContainer, option, input);
        if (!r)
            return false;
        inputContainer = r.inputContainer;
        input = r.input;
        $(function() {
            // init number section
            var numberSection = (function(option) {
                var r = {
                    'count' : 0,
                    'intervalWidth' : 0,
                    'scales' : [],
                    'sections' : []
                };
                for (var i in option.sliderNumber) {
                    var t = i.split('-');
                    var s = {
                        'min' : parseInt(t[0]),
                        'max' : parseInt(t[1]),
                        'interval' : option.sliderNumber[i]
                    };
                    s.count = (s.max - s.min) / s.interval;
                    r.count += s.count;
                    r.scales.push(s);
                }
                r.intervalWidth = option.width / r.count;
                var number = 0, left = 0;
                var last = r.scales[r.scales.length - 1];
                for (var i = 0; i < r.scales.length; i++) {
                    var s = r.scales[i];
                    for (var j = s.min; j < s.max; j += s.interval) {
                        r.sections.push({
                            'number' : number,
                            'left' : left,
                            'interval' : s.interval
                        });
                        number += s.interval;
                        left += r.intervalWidth;
                    }
                }
                r.sections.push({
                    'number' : number,
                    'left' : left - 1,
                    'interval' : last.interval
                });
                return r;
            })(option);
            // draw numbers
            (function(numberSection) {
                var s = numberSection.sections;
                var up = false, topBar = popup.find('.numberSliderTop'), bottomBar = popup.find('.numberSliderBottom');
                for (var i = 0; i < s.length; i++) {
                    var bar = up ? topBar : bottomBar;
                    var mark = $('<div class="numberMark"></div>'), number = $('<div class="numberValue">' + s[i].number + '</div>');
                    if (up) {
                        mark.addClass('top');
                        number.addClass('top');
                    } else {
                        mark.addClass('bottom');
                        number.addClass('bottom');
                    }
                    var section = $('<div class="numberSection"></div>').append(mark).append(number).css({
                        'left' : s[i].left
                    });
                    bar.append(section);
                    up = !up;
                }
            })(numberSection);
            // init drag and click
            (function(numberSection) {
                var drag = popup.find('.numberSliderDrag'), bar = popup.find('.numberSliderMiddle'), initX = -drag.width() / 2;
                drag.css({
                    'left' : initX,
                    'top' : (popup.find('.numberSliderMiddle').height() - drag.height()) / 2
                });
                var dd = DragAndDrop.create(drag, drag, {
                    'minOffsetX' : initX,
                    'maxOffsetX' : option.width + initX,
                    'onDrag' : function() {
                        setDragLeft(drag.position().left);
                    }
                });

                // set drag position by left
                function setDragLeft(left) {
                    var p = left - initX, n = null;
                    for (var i = 0; i < numberSection.sections.length; i++) {
                        var s = numberSection.sections[i];
                        if (p == s.left) {
                            n = s.number;
                            break;
                        } else if (p < s.left) {
                            if (i > 0) {
                                s = numberSection.sections[i - 1];
                                n = s.number + Math.round(s.interval * (p - s.left) / numberSection.intervalWidth);
                                if (option.delta && option.delta > 1) {
                                    var m = parseInt(n / option.delta) * option.delta;
                                    var d = n - m;
                                    if (d != 0) {
                                        n = d >= (option.delta / 2) ? (m + option.delta) : m;
                                    }
                                }
                            }
                            break;
                        }
                    };
                    n != null && input.val(n);
                }

                // set drag position by number
                function setDragPosition(e) {
                    if (dd.isDragging()) {
                        return;
                    }
                    var n = parseInt(input.val()), p = null, ss = numberSection.sections;
                    if (!isNaN(n)) {
                        input.val(n);
                    } else {
                        return;
                    }
                    for (var i = 0; i < ss.length; i++) {
                        var s = ss[i];
                        if (n == s.number) {
                            p = s.left;
                            break;
                        } else if (n < s.number) {
                            if (i > 0) {
                                s = ss[i - 1];
                                p = s.left + (n - s.number) / s.interval * numberSection.intervalWidth;
                            }
                            break;
                        }
                    }
                    if (n < ss[0].number) {
                        p = ss[0].left;
                    } else if (n > ss[ss.length - 1].number) {
                        p = ss[ss.length - 1].left;
                    }
                    smoothAnimate(drag, {
                        'left' : p + initX
                    }, {
                        'duration' : 300
                    });
                }


                popup.find('.numberMark,.numberValue').click(numberClick);
                bar.click(numberClick);
                function numberClick(e) {
                    var left = e.pageX - $('.numberSliderMiddle').offset().left, s = numberSection.sections;
                    if (left >= s[0].left && left <= s[s.length - 1].left) {
                        left += initX;
                        drag.stop().animate({
                            'left' : left
                        }, 300);
                        setDragLeft(left);
                    }
                }


                input.keyup(setDragPosition);
                setDragPosition();
            })(numberSection);
        });
        return r;
    }
};
