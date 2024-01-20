package de.zonlykroks.p2p.client.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IpFieldWidget extends TextFieldWidget {
    {
        this.setTextPredicate(s -> s.isBlank() || s.matches("[0-9.]+"));
        setRenderTextProvider((string, firstCharacterIndex) -> OrderedText.styledForwardsVisitedString(
                string, isIp() ? Style.EMPTY : Style.EMPTY.withColor(Formatting.RED)
        ));
    }
    public IpFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public boolean isIp() {
        return this.getText().matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d).?\\b){4}$");
    }
}
