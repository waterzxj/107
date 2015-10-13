var Card = {
    create : function(cardContainer) {
        cardContainer = $(cardContainer || ".cardContainer");
        var card = {};
        var slideDistance = 0;
        /**
         * Slide duration in millisecond.
         */
        card.slideDuration = 500;
        /**
         * Layout cards. Called after page loaded.
         */
        card.layout = function() {
            var cardWidth = $(".card").width();
            slideDistance = cardWidth + ($("body").width() - cardWidth) / 2;
            var card = cardContainer.children(".card:first");
            onCardShown(card);
            card.show();
            $("body").css("overflow-x", "hidden");
        };

        function onCardShown(card) {
            var f = card.attr('onCardShow');
            f && eval(f + '()');
        }

        /**
         * @param option  {'beforeSlide', 'afterSlide'}
         */
        function slide(toLeft, target, option) {
            option = option || {};
            if ( typeof target === "function") {
                option.afterSlide = target;
                target = null;
            } else if (target) {
                target = $(target);
            }
            // callback
            option.beforeSlide && option.beforeSlide();
            var visible = cardContainer.children(".card:visible");
            target = target || ( toLeft ? visible.nextAll('.card') : visible.prevAll('.card')).first();
            visible.animate({
                "left" : toLeft ? -slideDistance : slideDistance
            }, card.slideDuration, function() {
                $(this).hide();
                // callback
                option.afterSlide && option.afterSlide();
            });
            target.css('left', toLeft ? slideDistance : -slideDistance).show().animate({
                "left" : 0
            }, card.slideDuration);
            onCardShown(target);
        }


        card.show = function(target) {
            $('.card').hide();
            target = $(target);
            target.show();
            onCardShown(target);
        };

        card.slideLeft = function(target, option) {
            slide(true, target, option);
        };

        card.slideRight = function(target, option) {
            slide(false, target, option);
        };

        return card;
    }
};
