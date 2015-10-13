var SpinButton = {
    create : function(label, onChange) {
        var r = $('<div class="spinButton"><span class="spinLabel">' + label + '</span><input class="spinInput numberInput" type="text" value="0"></input><div class="spinBox"><a class="spinUp" href="#?"></a><a class="spinDown" href="#?"></a></div></div>');
        // input
        var input = r.find('input');
        Input.bindNumberInput(input);
        // update input
        function update() {
            onChange && onChange();
        }

        // spin
        r.find('a').click(function() {
            var d = 0;
            if ($(this).hasClass('spinUp')) {
                d = 1;
            } else {
                d = -1;
            }
            Input.increaseNumberInput(input, d);
            update();
        });
        input.keyup(update);
        return r;
    }
};
