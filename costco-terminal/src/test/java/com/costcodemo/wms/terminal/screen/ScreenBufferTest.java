package com.costcodemo.wms.terminal.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScreenBufferTest {

    @Test
    void holdsExactlyTwentyFourRowsOfEighty() {
        assertEquals(24 * 80, new ScreenBuffer().snapshot().length());
    }

    @Test
    void writesTextAtTheRequestedColumn() {
        ScreenBuffer buffer = new ScreenBuffer();
        buffer.write(1, 10, "WMS210", ScreenColor.BLUE);
        assertEquals('W', buffer.characterAt(1, 10));
    }
}
