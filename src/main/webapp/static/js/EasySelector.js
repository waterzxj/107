var EasySelector = {

    defaultOption : {
        'singleSelection' : true,
        'selections' : [],
        'onShow': null
    },

    create : function(inputContainer, option, input) {
        merge(EasySelector.defaultOption, option);
        var popup = $('<div class="easySelector"></div>');
        var r = Popup.create(popup, inputContainer, option, input);
        if (!r)
            return false;
        inputContainer = r.inputContainer;
        input = r.input;
        // text
        if (option.text) {
            popup.append('<p>' + option.text + '</p>');
        }
        // button
        for (var i = 0; i < option.selections.length; i++) {
            var a = '<a i="' + i + '" class="button buttonNormal buttonUnselected" href="#?">' + option.selections[i] + "</a>";
            popup.append(a);
        };
        var b = popup.find('.button');
        function onClick() {
            var s = '';
            popup.find('.buttonSelected').each(function() {
                if (s)
                    s += '、';
                s += $(this).text();
            });
            input.val(s);
            if (option.singleSelection) {
                r.close();
            }
        }

        if (option.singleSelection) {
            Input.radioGroup(b, onClick);
        } else {
            Input.checkboxGroup(b, onClick);
        }
        option.onShow && option.onShow();
        // display
        var v = input.val(), end = false;
        if (v && ( v = v.split('、'))) {
            for (var i = 0; i < v.length; i++) {
                if (end)
                    break;
                b.each(function() {
                    if ($(this).text() === v[i]) {
                        Input.select($(this));
                        if (option.singleSelection) {
                            end = true;
                            return false;
                        }
                    }
                });
            };
        }
        return r;
    }
};
