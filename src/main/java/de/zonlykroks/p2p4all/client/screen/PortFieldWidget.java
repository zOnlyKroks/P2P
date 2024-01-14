package de.zonlykroks.p2p4all.client.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class PortFieldWidget extends TextFieldWidget {
    {
        this.setTextPredicate(s -> {
            //Regex for port number
            return s.isBlank() || s.matches("[0-9]+");
        });
    }

    public PortFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
    }

    public PortFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public PortFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Override
    public Text getMessage() {
        return isFullPort() ? super.getMessage() : super.getMessage().copy().setStyle(Style.EMPTY.withColor(Formatting.RED));
    }

    public boolean isFullPort() {
        return this.getText().matches("^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$");
    }
}
