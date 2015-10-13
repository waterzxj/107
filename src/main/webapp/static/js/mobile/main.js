/*
 * Submit button.
 * @param b button
 */
var SubmitButton = {
    /**
     * For a submit button's attributes:
     * 'validate': function(element, hideErrorWhenInvalid): boolean
     * 'callback': function(element)
     */
    init : function() {
        // reactive effect
        UI.reactive($('.submit a'), function() {
            if (!$(this).hasClass('disabled') && !$(this).hasClass('loading')) {
                $(this).addClass('highlight');
            }
        }, function(e) {
            $(this).removeClass('highlight');
        });
        $('.submit a').each(function() {
            var validate = eval($(this).attr('validate'));
            // click
            var callback = eval($(this).attr('callback'));
            if (callback) {
                $(this).click(function() {
                    var a = $(this);
                    if (a.hasClass('disabled') || a.hasClass('loading'))
                        return;
                    var valid = true;
                    if (validate) {
                        valid = validate(a);
                    }
                    if (valid) {
                        SubmitButton.loading(a, true);
                        setTimeout(function() {
                            callback(a);
                        }, 1000);
                    }
                });
            }
            /*
             // bind inputs
             var c = $(this).closest('.card'), a = $(this);
             c.find('.inputBox input').bind('input propertychange', function() {
             if (validate($(this), true)) {
             a.removeClass('disabled');
             } else {
             a.addClass('disabled');
             }
             });
             */
        });
    },
    loading : function(b, on) {
        UI.loading(b, on);
    }
};
/**
 * @param e element
 */
var UI = {
    reactive : function(e, onfocus, onclur) {
        e.bind("mousedown touchstart", onfocus).bind("mouseup touchend", onclur);
    },
    /**
     * @param on boolean to turn on/off loading effect
     */
    loading : function(e, on, speed) {
        e = $(e);
        var tag = 'loading';
        if (on) {
            var i = 0;
            function loading() {
                i = (i % 3) + 1;
                var v = '';
                for (var j = 0; j < i; j++) {
                    v += '.';
                }
                e.text(v);
            }

            loading();
            var tid = setInterval(loading, speed ? speed : 300);
            e.addClass(tag).attr(tag, tid);
        } else {
            clearInterval(e.attr(tag));
            e.text('').removeClass(tag).removeAttr(tag);
        }
    }
};
/*
 * Validate.
 */
var Validate = {
    usernameMin : 1,
    usernameMax : 30,
    passwordMin : 5,
    passwordMax : 30,
    username : function(data) {
    	return new RegExp('^[\\w-+]+(\\.[\\w-+]+)*@([\\w-]+\\.)+[a-zA-Z]+$').test(data);
    },
    password : function password(data) {
        return data.length >= this.passwordMin && data.length <= this.passwordMax;
    },
    email : function email(data) {
        return new RegExp('^[\\w-+]+(\\.[\\w-+]+)*@([\\w-]+\\.)+[a-zA-Z]+$').test(data);
    },
    errorType : {
        'gender' : '请选择性别',
        'username' : '请输入正确格式的邮箱',
        'username.exist' : '用户名已被占用',
        'password' : '密码需5-30个字符',
        'password.error' : '用户名与密码不匹配',
        'email' : '请输入正确格式的邮箱'
    }
};
/*
 * Card.
 */
var CardLayout = {
    /**
     * @param cardView selector of class 'cardView'
     * @param layout array of selector or selector arrays like ['#0', ['#1', '#1', '#2'],'#3'] (1st element in an array indicates the MAIN card in this array), non-null
     */
    create : function(cardView, layout) {
        if (!cardView || !layout || !layout.length)
            return null;
        cardView = $(cardView);
        var currentId = null, top = null, bottom = null, left = null, right = null;
        var currentPosition = 0;
        var layoutLength = layout.length;
        // card nav
        cardView.append('<div class="cardNav left"></div>').append('<div class="cardNav right"></div>').append('<div class="cardNav top"></div>').append('<div class="cardNav bottom"></div>');
        // id->bottomId for horizontal movement
        var bottomMap = {};
        function updateBottomMap() {
            for (var i = 0; i < layout.length; i++) {
                if ( typeof layout[i] != 'string') {
                    var a = layout[i];
                    for (var j = 1; j < a.length - 1; j++) {
                        if (a[j] == a[0]) {// hit
                            for (var k = j + 1; k < a.length; k++) {
                                if (CardLayout._isValid(a[k])) {
                                    bottomMap[a[0]] = a[k];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
         * result
         */
        var r = {
            hLoop : false,
            vLoop : false,
            navFadeTime : 200,
            slideSpeed : 200,
            slideEasing : 'linear',
            rollbackSpeed : 200,
            rollbackEasing : 'linear',
            // function(id)
            onMoveStart : null,
            onMoveEnd : null,
            // function(id, offsetX, offsetY)
            onMove : null,
            // function(id)
            beforeShow : null,
            // function(id)
            onShow : null,
            cardView : function() {
                return cardView;
            },
            currentId : function() {
                return currentId;
            },
            /**
             * @param id ID of card to show
             * @param effect null: show immediately, 'toLeft', 'toRight', 'toTop', 'toBottom'
             * @param reset true when need to reset target position before sliding
             * @return whether shown successfully
             */
            show : function(id, effect, reset) {
                if (!id)
                    return false;
                var d = $(id);
                if (!d.length)
                    return false;
                // old model
                var oldModel = {
                    currentId : currentId,
                    currentPosition : currentPosition,
                    top : top,
                    right : right,
                    bottom : bottom,
                    left : left
                };
                // update model
                var l = layout.length, isMainCard = false;
                for (var i = 0; i < l; i++) {
                    var hit = false;
                    var v = layout[i];
                    if ( typeof v == 'string') {
                        if (v == id) {// hit
                            isMainCard = hit = true;
                            currentPosition = i;
                            top = bottom = null;
                        } else {
                            continue;
                        }
                    } else {
                        for (var j = 1; j < v.length; j++) {
                            if (v[j] == id) {// hit
                                if (!CardLayout._isValid(id))
                                    continue;
                                if (v[0] == id)
                                    isMainCard = true;
                                hit = true;
                                currentPosition = i;
                                function find(a, start, delta, loop) {
                                    var i = start + delta;
                                    if (loop)
                                        i = i % a.length;
                                    while (i >= 0 && i < a.length && i != start) {
                                        if (i != 0 && CardLayout._isValid(a[i])) {
                                            return a[i];
                                        }
                                        i += delta;
                                        if (loop)
                                            i = i % a.length;
                                    }
                                    return null;
                                }

                                top = find(v, j, -1, this.vLoop);
                                bottom = find(v, j, 1, this.vLoop);
                                break;
                            }
                        }
                    }
                    if (hit) {
                        // set left and right
                        if (l > 1 && isMainCard) {
                            left = CardLayout._getId(layout, i - 1, this.hLoop);
                            right = CardLayout._getId(layout, i + 1, this.hLoop);
                        } else {
                            left = right = null;
                        }
                        // show
                        log('top: ' + top + ', bottom: ' + bottom + ', left: ' + left + ', right: ' + right);
                        this._show(id, oldModel, effect, reset);
                        currentId = id;
                        log('currentId: ' + currentId + ', currentPosition: ' + currentPosition);
                        break;
                    }
                }
                return true;
            },
            _show : function(id, oldModel, effect, reset) {
                this.beforeShow && this.beforeShow(id);
                if (id != currentId) {
                    this._updateNav();
                }
                var d = $(id);
                if (!effect) {
                    cardView.children('.card').hide();
                    var p = {
                        left : 0
                    };
                    if (id != currentId) {
                        p.top = 0;
                    }
                    d.css(p).show();
                    if (bottom && d.height() < $(window).height()) {
                        $(bottom).css({
                            'top' : d.height(),
                            'left' : 0
                        }).show();
                    }
                    this.onShow && this.onShow(id);
                } else {
                    var old = $(oldModel.currentId), isHorizontal = false;
                    var shownNewLeft = old.position().left, shownNewTop = old.position().top;
                    switch(effect) {
                        case 'toLeft':
                            reset && d.css('left', old.width());
                            shownNewLeft = -cardView.width();
                            isHorizontal = true;
                            break;
                        case 'toRight':
                            reset && d.css('left', -d.width());
                            shownNewLeft = cardView.width();
                            isHorizontal = true;
                            break;
                        case 'toTop':
                            reset && d.css('top', old.height());
                            shownNewTop = -old.height();
                            break;
                        case 'toBottom':
                            reset && d.css('top', -d.height());
                            shownNewTop = d.height();
                            break;
                        default:
                            this._show(d, oldModel);
                            return;
                    }
                    var r = this;
                    function showSlide(target, top, left, onShow) {
                        if (!target)
                            return;
                        target = $(target);
                        var id = '#' + target.attr('id');
                        target.show().stop().animate({
                            top : top,
                            left : left
                        }, {
                            duration : r.slideSpeed,
                            easing : r.slideEasing,
                            always : function() {
                                onShow && onShow(id);
                            },
                            step : function() {
                                if (r.onMove && id == oldModel.currentId) {
                                    // omit Y
                                    r.onMove(id, old.position().left, 0);
                                }
                            }
                        });

                    }

                    // old
                    showSlide(old, shownNewTop, shownNewLeft);
                    // new
                    showSlide(d, 0, 0, this.onShow);
                    if (bottomMap[id] || isHorizontal) {
                        showSlide(oldModel.bottom, old.height() + shownNewTop, shownNewLeft);
                        showSlide(bottomMap[id], d.height(), 0);
                    }
                }
            },
            _updateNav : function() {
                function showNav(nav, css) {
                    if (nav && !nav.hasClass('disabled')) {
                        nav.css(css).fadeIn(this.navFadeTime);
                    }
                }


                cardView.children('.cardNav').hide();
                setTimeout(function() {
                    var navTop = cardView.height() / 2 - 11, navLeft = cardView.width() / 2 - 11;
                    left && showNav(cardView.children('.cardNav.left'), {
                        top : navTop
                    });
                    right && showNav(cardView.children('.cardNav.right'), {
                        top : navTop
                    });
                    top && showNav(cardView.children('.cardNav.top'), {
                        left : navLeft
                    });
                    bottom && showNav(cardView.children('.cardNav.bottom'), {
                        left : navLeft
                    });
                }, 0);
            }
        };
        /*
         * drag and drop
         */
        (function() {
            var dragging = false, p = {
                x : 0,
                y : 0
            };
            var offsetX = 0, offsetY = 0, c = null, moved = false, time = 0, cards = cardView.children(".card");
            /*
             * start
             */
            cards.bind("mousedown touchstart", function(e) {
                var t = $(e.target);
                if (dragging || t.closest('.undraggable').length || t.is('a')) {
                    return;
                }
                // e.preventDefault();
                // e.stopPropagation();
                dragging = true;
                moved = false;
                p = getPosition(e);
                c = $(currentId).stop();
                offsetX = c.position().left, offsetY = c.position().top;
                CardLayout._lockX = false, CardLayout._lockY = false;
                time = new Date().getTime();
                updateBottomMap();
                // callback
                r.onMoveStart && r.onMoveStart(currentId);
            });
            /*
             * move
             */
            cards.bind("mousemove touchmove", function(e) {
                if (!dragging)
                    return;
                var p2 = getPosition(e);
                var v = layout.get(currentPosition);
                if ( typeof v == 'string' || currentId == v[0]) {// main card
                    if (!CardLayout._lockX && !CardLayout._lockY) {
                        var winX = Math.abs(p2.x - p.x) >= Math.abs(p2.y - p.y);
                        if (winX) {// x win
                            CardLayout._lockY = true;
                            p2.y = p.y;
                        } else {
                            CardLayout._lockX = true;
                            p2.x = p.x;
                        }
                    }
                } else {// second card
                    p2.x = p.x;
                }
                if (CardLayout._lockX) {
                    p2.x = p.x;
                }
                if (CardLayout._lockY) {
                    p2.y = p.y;
                }
                // callback
                var deltaX = p2.x - p.x, deltaY = p2.y - p.y;
                var deltaH = $(window).height() - c.height();
                e.preventDefault();
                // check bound
                var left2 = offsetX + deltaX, top2 = offsetY + deltaY;
                if (left2 > 0 && !left)
                    return;
                if (left2 < 0 && !right)
                    return;
                if (top2 > 0 && !top)
                    return;
                if (deltaH >= 0) {
                    if (top2 < 0 && !bottom)
                        return;
                } else if (top2 < deltaH && !bottom)
                    return;
                // hide nav
                if (CardLayout._lockX) {
                    cardView.children('.cardNav.left').hide();
                    cardView.children('.cardNav.right').hide();
                }
                if (CardLayout._lockY) {
                    cardView.children('.cardNav.top').hide();
                    cardView.children('.cardNav.bottom').hide();
                }
                // show
                moved = true;
                c.css({
                    left : left2,
                    top : top2
                });
                // move bottom when horizon
                if (c.height() < $(window).height() && bottom && deltaY == 0) {
                    $(bottom).css({
                        left : left2
                    });
                }
                var offset = c.position();
                if (p.x != p2.x) {
                    // special handle when only 2 cards in horizon
                    // which means `left` equals `right`
                    if(layoutLength === 2){
                        if(p.x<p2.x){
                            moveCard(left, null, offset.left - c.width());
                        }else{
                            moveCard(right, null, offset.left + c.width());
                        }
                    }else{
                        moveCard(left, null, offset.left - c.width());
                        moveCard(right, null, offset.left + c.width());
                    }
                } else if (p.y != p2.y) {
                    top && $(top).show().css({
                        left : offset.left,
                        top : offset.top - $(top).height()
                    });
                    if (bottom) {
                        $(bottom).show().css({
                            left : offset.left,
                            top : offset.top + c.height()
                        });
                        var bb = bottomMap[bottom];
                        if (bb) {
                            $(bb).show().css({
                                left : offset.left,
                                top : offset.top + c.height() + $(bottom).height()
                            });
                        }
                    }
                }
                // callback
                r.onMove && r.onMove(currentId, left2, top2);
            });
            /*
             * end
             */
            cards.bind("mouseup touchend", function(e) {
                if (!dragging)
                    return;
                if (!moved) {
                    dragging = false;
                    return;
                }
                var c = $(currentId);
                var left = c.position().left, top = c.position().top;
                var rollback = true;
                r._updateNav();
                if (left != offsetX) {
                    var deltaX = left - offsetX;
                    if (Math.abs(deltaX) > c.width() / 2 || shouldSlide(deltaX)) {
                        rollback = !slide(deltaX, 0);
                    }
                } else if (top != offsetY) {
                    var deltaH = c.height() - $(window).height(), deltaY = top - offsetY;
                    if (deltaH > 0) {// higher than window
                        rollback = top > 0 || top < -deltaH;
                    } else {
                        if (Math.abs(deltaY) > c.height() / 2 || shouldSlide(deltaY)) {
                            rollback = !slide(0, deltaY);
                        }
                    }
                    // inertia
                    if (deltaH > 0) {
                        var deltaTime = new Date().getTime() - time;
                        if (!deltaTime) {
                            deltaTime = 1;
                        }
                        var velocity = 600 * deltaY / deltaTime;
                        if (deltaTime < 200) {
                            var top2 = top + velocity;
                            // correct out of bound
                            if (top2 > 0) {
                                top2 = Math.min(20, top2);
                            } else if (top2 < -deltaH) {
                                top2 = Math.max(-deltaH - 20, top2);
                            }
                            c.stop().animate({
                                top : top2
                            }, {
                                duration : 600,
                                easing : 'easeOutExpo',
                                always : function() {
                                    var backTop = 1;
                                    if (top2 > 0) {
                                        backTop = 0;
                                    } else if (top2 < -deltaH) {
                                        backTop = -deltaH;
                                    }
                                    backTop <= 0 && c.animate({
                                        top : backTop
                                    }, {
                                        duration : 150,
                                        easing : 'linear',
                                    });
                                }
                            });
                        }
                    }
                }
                rollback && rollbackCard();
                time = new Date().getTime();
                dragging = false;
                // callback
                r.onMoveEnd && onMoveEnd(currentId);
            });
            function shouldSlide(delta) {
                delta = Math.abs(delta);
                if (delta < 20) {
                    return false;
                } else {
                    var base = 0.1;
                    if (delta < 50) {
                        base = 0.05;
                    }
                    return 1.0 * delta / (new Date().getTime() - time) > base;
                }
            }

            function slide(deltaX, deltaY) {
                if (deltaX > 0) {
                    return r.show(left, 'toRight');
                } else if (deltaX < 0) {
                    return r.show(right, 'toLeft');
                } else if (deltaY > 0) {
                    return r.show(top, 'toBottom');
                } else if (deltaY < 0) {
                    return r.show(bottom, 'toTop');
                }
                return false;
            }

            /**
             * @param {Object} option null when no animation
             */
            function moveCard(id, option, left) {
                if (!id)
                    return;
                var t = $(id).show();
                var p = {
                    left : left,
                    top : 0
                };
                if (option) {// animate
                    t.animate(p, option);
                } else {
                    t.css(p);
                }
                // move bottom
                if (t && t.height() < $(window).height()) {
                    var b = bottomMap[id];
                    if (b) {
                        p = {
                            left : left,
                            top : t.height()
                        };
                        if (option) {// animate
                            $(b).animate(p, option);
                        } else {
                            $(b).show().css(p);
                        }
                    }
                }
            }

            function rollbackCard() {
                var option = {
                    duration : r.rollbackSpeed,
                    easing : r.rollbackEasing
                };
                var x = c.position().left, y = c.position().top;
                var deltaH = $(window).height() - c.height();
                c.animate({
                    left : 0,
                    top : deltaH >= 0 ? 0 : (Math.abs(y - deltaH) < 30 ? deltaH : y)
                }, {
                    duration : r.rollbackSpeed,
                    easing : r.rollbackEasing,
                    step : function() {
                        // omit Y
                        r.onMove && r.onMove(currentId, c.position().left, 0);
                    },
                    always : function() {
                        r.show(currentId);
                    }
                });
                // move bottom when horizon
                if (c.height() < $(window).height() && bottom) {
                    $(bottom).animate({
                        left : 0
                    }, option);
                }

                if (x > 0) {
                    moveCard(left, option, -c.width());
                } else if (x < 0) {
                    moveCard(right, option, c.width());
                } else if (y > 0) {
                    top && $(top).animate({
                        top : -c.height()
                    }, option);
                } else if (y < 0) {
                    bottom && $(bottom).animate({
                        top : c.height()
                    }, option);
                }
            }

        })();
        CardLayout._all.push(r);
        return r;
    },
    _getId : function(layout, i, loop) {
        var v = layout.get(i, loop);
        return v == null ? null : ( typeof v == 'string' ? v : v[0]);
    },
    _isValid : function(id) {
        var o = $(id);
        if (o.length == 0 || o.hasClass('disabled')) {
            log('Not found or disabled: ' + id);
            return false;
        }
        return true;
    },
    _lockX : false,
    _lockY : false,
    // all layouts
    _all : []
};
$(function() {
    SubmitButton.init();
});
