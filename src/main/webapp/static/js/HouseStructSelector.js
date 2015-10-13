var HouseStructSelector = {

    create : function(inputContainer, option, input) {
        merge(EasySelector.defaultOption, option);
        var popup = $('<div class="houseStructSelector"></div>');
        var r = Popup.create(popup, inputContainer, option, input);
        if (!r)
            return false;
        inputContainer = r.inputContainer;
        input = r.input;

        function store() {
            var s = popup.find('.roomSelector input').val() + '室 ' + popup.find('.hallSelector input').val() + '厅 ' + popup.find('.kitchenSelector input').val() + '厨 ' + popup.find('.toiletSelector input').val() + '卫';
            input.val(s);
        }

        function createSpin(clazz, label, value) {
            var s = SpinButton.create(label, store);
            var i = s.addClass(clazz).find('input').attr('numberInput', '0-').attr('validate', 'int0-');
            if (value) {
                i.val(value);
            }
            s.appendTo(popup);
            return s;
        }

        var spinInputs = [createSpin('roomSelector', '卧室'), createSpin('hallSelector', '客厅'), createSpin('kitchenSelector', '厨房'), createSpin('toiletSelector', '卫生间')];
        for (var i = 0; i < spinInputs.length; i++) {
            spinInputs[i] = spinInputs[i].find('input').attr('tabindex', i+1);
        }
        spinInputs[0].attr('numberInput', '1-');
        // room > 0
        popup.find('.spinButton').css('left', -10);
        Input.bindValidate(popup);
        // load
        var v = input.val().split(' ');
        if (v && v.length > 1) {// set already
            for (var i = 0; i < spinInputs.length; i++) {
                spinInputs[i].val(parseInt(v[i]));
            }
        } else {
            store();
        }
        return r;
    }
};
