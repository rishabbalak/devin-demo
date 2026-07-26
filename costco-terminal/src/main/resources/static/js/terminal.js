/* -----------------------------------------------------------------------------------------
 * 5250 workstation behaviour.
 *
 * The guiding rule: the host only ever hears about whole screens. Typing, tabbing and
 * arrowing are local to the terminal. Pressing an AID key -- Enter or a function key --
 * locks the keyboard, ships the entire buffer, and waits for a new screen to be painted.
 * ----------------------------------------------------------------------------------------- */
(function () {
    'use strict';

    var form = document.getElementById('screenForm');
    var aidInput = document.getElementById('aidKey');
    var screenEl = document.getElementById('screen');
    var cursorEl = document.getElementById('blockCursor');
    var oiaLock = document.getElementById('oiaLock');
    var oiaPos = document.getElementById('oiaPos');

    if (!form || !screenEl) {
        return;
    }

    var CELL_HEIGHT = 16;
    var locked = false;

    /* Function keys that must not reach the browser: F1 opens help, F3 and F12 do nothing
     * useful, F5 reloads the page. All of them mean something else here. */
    var AID_KEYS = {
        F1: 'F1', F3: 'F3', F4: 'F4', F5: 'F5', F6: 'F6',
        F7: 'F7', F8: 'F8', F11: 'F11', F12: 'F12', F24: 'F24'
    };

    var fields = Array.prototype.slice.call(screenEl.querySelectorAll('.fld'));

    /* --------------------------------------------------------------------------------------
     * Alarm. DDS sounds the terminal beeper via the ALARM keyword, on validation errors only
     * -- never on ordinary navigation. A square wave with no envelope is the right texture;
     * anything smoother sounds like a notification chime.
     * ------------------------------------------------------------------------------------ */
    function beep() {
        try {
            var Ctx = window.AudioContext || window.webkitAudioContext;
            if (!Ctx) {
                return;
            }
            var ctx = new Ctx();
            var osc = ctx.createOscillator();
            var gain = ctx.createGain();
            osc.type = 'square';
            osc.frequency.value = 1000;
            gain.gain.value = 0.04;
            osc.connect(gain).connect(ctx.destination);
            osc.start();
            osc.stop(ctx.currentTime + 0.12);
            osc.onended = function () {
                ctx.close();
            };
        } catch (ignored) {
            /* Autoplay policy blocks audio until the operator has interacted. Silence is an
             * acceptable degradation; a thrown error inside a keydown handler is not. */
        }
    }

    /* --------------------------------------------------------------------------------------
     * Block cursor, tracked to the caret position inside the focused field.
     * ------------------------------------------------------------------------------------ */
    function cellWidth() {
        return screenEl.getBoundingClientRect().width / 80;
    }

    function updateCursor() {
        var active = document.activeElement;
        if (!active || !active.classList || !active.classList.contains('fld')) {
            cursorEl.classList.remove('on');
            return;
        }

        var width = cellWidth();
        var left = parseFloat(active.style.left) || 0;
        var leftCells = left / width;
        if (active.style.left.indexOf('ch') > -1) {
            leftCells = parseFloat(active.style.left);
        }

        var caret = active.selectionStart;
        if (caret === null || caret === undefined) {
            caret = active.value.length;
        }
        caret = Math.min(caret, active.maxLength > 0 ? active.maxLength - 1 : caret);

        cursorEl.style.left = ((leftCells + caret) * width) + 'px';
        cursorEl.style.top = active.style.top;
        cursorEl.style.width = width + 'px';
        cursorEl.classList.add('on');

        var row = Math.round((parseFloat(active.style.top) || 0) / CELL_HEIGHT) + 1;
        var col = Math.round(leftCells + caret) + 1;
        oiaPos.textContent = pad(row, 2) + '/' + pad(col, 3);
    }

    function pad(value, width) {
        var text = String(value);
        while (text.length < width) {
            text = '0' + text;
        }
        return text;
    }

    /* --------------------------------------------------------------------------------------
     * Submit. Locks the keyboard, shows X SYSTEM, and holds briefly before posting so the
     * lock is actually perceptible -- on real hardware the wait was the defining experience.
     * ------------------------------------------------------------------------------------ */
    function submit(aid) {
        if (locked) {
            return;
        }
        locked = true;
        aidInput.value = aid;
        document.body.classList.add('locked');
        oiaLock.textContent = 'X SYSTEM';

        window.setTimeout(function () {
            form.submit();
        }, 180);
    }

    /* --------------------------------------------------------------------------------------
     * Field navigation. Tab visits input fields only; arrows move within a field and spill
     * over to the next or previous one at the boundaries.
     * ------------------------------------------------------------------------------------ */
    function focusField(index) {
        if (fields.length === 0) {
            return;
        }
        var wrapped = (index + fields.length) % fields.length;
        var field = fields[wrapped];
        field.focus();
        field.setSelectionRange(field.value.length, field.value.length);
        updateCursor();
    }

    function indexOfActive() {
        return fields.indexOf(document.activeElement);
    }

    document.addEventListener('keydown', function (event) {
        if (locked) {
            /* Keystroke buffering is represented by simply discarding input while the host
             * has the keyboard -- the operator sees X SYSTEM and nothing echoes. */
            event.preventDefault();
            return;
        }

        var aid = AID_KEYS[event.key];
        if (aid) {
            event.preventDefault();
            submit(aid);
            return;
        }

        if (event.key === 'Enter') {
            event.preventDefault();
            submit('ENTER');
            return;
        }

        if (event.key === 'PageDown') {
            event.preventDefault();
            submit('F8');
            return;
        }

        if (event.key === 'PageUp') {
            event.preventDefault();
            submit('F7');
            return;
        }

        if (event.key === 'Tab') {
            event.preventDefault();
            focusField(indexOfActive() + (event.shiftKey ? -1 : 1));
            return;
        }

        /* Ctrl+F is browser find, which has no analogue here and breaks the illusion. */
        if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'f') {
            event.preventDefault();
            return;
        }

        window.setTimeout(updateCursor, 0);
    });

    document.addEventListener('keyup', updateCursor);

    fields.forEach(function (field) {
        field.addEventListener('focus', updateCursor);
        field.addEventListener('click', updateCursor);
        field.addEventListener('input', function () {
            /* Auto-advance: filling a field to its last position jumps to the next one. This
             * is a large part of why experienced operators could key without looking up. */
            if (field.maxLength > 0 && field.value.length >= field.maxLength) {
                var next = fields.indexOf(field) + 1;
                if (next < fields.length) {
                    focusField(next);
                }
            }
            updateCursor();
        });
    });

    /* --------------------------------------------------------------------------------------
     * Initial paint.
     * ------------------------------------------------------------------------------------ */
    var wanted = (window.WMS && window.WMS.cursorField) || '';
    var target = wanted ? screenEl.querySelector('[name="' + wanted + '"]') : null;
    if (target) {
        target.focus();
        target.setSelectionRange(target.value.length, target.value.length);
    } else if (fields.length > 0) {
        focusField(0);
    }
    updateCursor();

    if (window.WMS && window.WMS.alarm) {
        beep();
    }

    window.addEventListener('resize', updateCursor);
    window.addEventListener('pageshow', function () {
        /* Returning via the back/forward cache must not leave the keyboard locked. */
        locked = false;
        document.body.classList.remove('locked');
        oiaLock.textContent = '';
        updateCursor();
    });
})();
