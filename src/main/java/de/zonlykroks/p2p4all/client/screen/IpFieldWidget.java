package de.zonlykroks.p2p4all.client.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class IpFieldWidget extends TextFieldWidget {
    {
        this.setTextPredicate(s -> {
            return s.isBlank() || s.matches("[0-9.]+");
        });
    }

    public IpFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        super(textRenderer, width, height, text);
    }

    public IpFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public IpFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    public boolean isIp() {
        return this.getText().matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d).?\\b){4}$");
    }
}
