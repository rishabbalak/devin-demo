package com.costcodemo.wms.terminal.screen;

import org.springframework.stereotype.Component;

/**
 * Renders a {@link ScreenBuffer} to HTML.
 *
 * <p>The markup is built here in Java rather than in a template on purpose. A 24x80 grid
 * cannot tolerate stray whitespace, and any template engine that pretty-prints its output
 * will inject newlines and indentation between elements — which shifts every column and
 * destroys the alignment. Emitting one flat string keeps the grid exact.
 *
 * <p>Cells are grouped into runs sharing a colour and reverse-image state, so a full row of
 * plain text costs one span rather than eighty.
 */
@Component
public class ScreenRenderer {

    public String toHtml(ScreenBuffer buffer) {
        StringBuilder html = new StringBuilder(8192);

        for (int row = 1; row <= ScreenBuffer.ROWS; row++) {
            html.append("<div class=\"r\">");

            int col = 1;
            while (col <= ScreenBuffer.COLS) {
                ScreenColor color = buffer.colorAt(row, col);
                boolean reverse = buffer.isReverseAt(row, col);

                int runEnd = col;
                while (runEnd + 1 <= ScreenBuffer.COLS
                        && buffer.colorAt(row, runEnd + 1) == color
                        && buffer.isReverseAt(row, runEnd + 1) == reverse) {
                    runEnd++;
                }

                StringBuilder text = new StringBuilder(runEnd - col + 1);
                for (int c = col; c <= runEnd; c++) {
                    text.append(buffer.characterAt(row, c));
                }

                html.append("<span class=\"c-").append(color.getCssSuffix());
                if (reverse) {
                    html.append(" rv");
                }
                html.append("\">").append(escape(text.toString())).append("</span>");

                col = runEnd + 1;
            }

            html.append("</div>");
        }

        return html.toString();
    }

    /**
     * Escapes for HTML text content. Spaces are deliberately left as literal spaces rather
     * than {@code &nbsp;} — the grid relies on {@code white-space: pre}, and non-breaking
     * spaces would render at a different width in some fonts and break the column alignment.
     */
    private String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                default:
                    escaped.append(ch);
            }
        }
        return escaped.toString();
    }
}
