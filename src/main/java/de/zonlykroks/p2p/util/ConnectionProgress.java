package de.zonlykroks.p2p.util;

import net.minecraft.util.Identifier;

import java.util.List;

public enum ConnectionProgress {

    PENDING(getFirstPing(), 10, 8),
    PUNCHING(getPings(), 10, 8),
    FAILED(getPingUnknown(), 10, 8),
    SUCCESS(getCheckmark(), 9, 8);

    private final int width;
    private final int height;
    private final List<Identifier> ids;
    private int index = 0;
    private long lastUpdate;

    ConnectionProgress(Identifier id, int width, int height) {
        this(List.of(id), width, height);
    };

    ConnectionProgress(List<Identifier> ids, int width, int height) {
        this.ids = ids;
        this.width = width;
        this.height = height;
    }

    public Identifier getId() {
        if(index >= this.ids.size()) index = 0;
        return this.ids.get(index);
    }

    public void tryIncrementIndex() {
        if(System.currentTimeMillis() - lastUpdate >= 1000) {
            index++;
            lastUpdate = System.currentTimeMillis();
        }
    }

    public static Identifier getCheckmark() {
        return new Identifier("p2p","textures/icon/checkmark.png");
    }


    public static Identifier getPingUnknown() {
        return new Identifier("p2p","textures/icon/ping_unknown.png");
    }

    public static Identifier getFirstPing() {
        return new Identifier("p2p","textures/icon/ping_1.png");
    }

    public static List<Identifier> getPings() {
        final Identifier PING_1_ICON_TEXTURE = new Identifier("p2p","textures/icon/ping_1.png");
        final Identifier PING_2_ICON_TEXTURE = new Identifier("p2p","textures/icon/ping_2.png");
        final Identifier PING_3_ICON_TEXTURE = new Identifier("p2p","textures/icon/ping_3.png");
        final Identifier PING_4_ICON_TEXTURE = new Identifier("p2p","textures/icon/ping_4.png");
        final Identifier PING_5_ICON_TEXTURE = new Identifier("p2p","textures/icon/ping_5.png");
        return List.of(PING_1_ICON_TEXTURE, PING_2_ICON_TEXTURE, PING_3_ICON_TEXTURE, PING_4_ICON_TEXTURE, PING_5_ICON_TEXTURE);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
