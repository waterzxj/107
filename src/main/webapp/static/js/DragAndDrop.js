var DragAndDrop = {
    defaultOption : {
        'minOffsetX' : 0,
        'maxOffsetX' : 999999,
        'onDrag' : function() {
        },
        'dragEnd' : function() {
        }
    },
    create : function(dragTargetSelector, moveTargetSelector, option) {
        dragTargetSelector = $(dragTargetSelector);
        moveTargetSelector = moveTargetSelector ? $(moveTargetSelector) : dragTargetSelector;
        option = merge(DragAndDrop.defaultOption, option);
        var dragging = false;
        var x;
        dragTargetSelector.bind("mousedown touchstart", function(e) {
            dragging = true;
            e.preventDefault();
            if (room107.isTouch) {
                var touch = e.originalEvent.touches.item(0);
                x = touch.pageX;
            } else {
                x = e.pageX;
            }
            var left = moveTargetSelector.position().left;
            // on move
            $(document).bind("mousemove touchmove", function(e) {
                if (dragging) {
                    e.preventDefault();
                    var newX = x;
                    if (room107.isTouch) {
                        var touch = e.originalEvent.touches.item(0);
                        newX = touch.pageX;
                    } else {
                        newX = e.pageX;
                    }
                    var delta = newX - x, newLeft = left + delta;
                    if (newLeft < option.minOffsetX) {
                        newLeft = option.minOffsetX;
                    } else if (newLeft > option.maxOffsetX) {
                        newLeft = option.maxOffsetX;
                    }
                    moveTargetSelector.css("left", newLeft);
                    option.onDrag();
                }
            });
            dragTargetSelector.removeClass("dragEnd").addClass("dragging");
        });
        // move end
        $(document).bind("mouseup touchend", function(e) {
            if (dragging) {
                e.preventDefault();
                dragTargetSelector.unbind("mousemove touchmove");
                dragTargetSelector.removeClass("dragging").addClass("dragEnd");
                dragging = false;
                option.onDrag();
                option.dragEnd();
            }
        });
        return {
            isDragging : function() {
                return dragging;
            }
        };
    }
};
