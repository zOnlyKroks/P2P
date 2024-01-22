package de.zonlykroks.p2p.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.List;

public enum ConnectionProgress {

    PENDING(List.of(
            new Pair<>(0,49)
    ), 10, 8),
    PUNCHING(List.of(
            new Pair<>(0,48),
            new Pair<>(0,40),
            new Pair<>(0,32),
            new Pair<>(0,24),
            new Pair<>(0,16)
    ), 10, 8),
    FAILED(List.of(
            new Pair<>(0,56)
    ), 10, 8),
    SUCCESS(List.of(
            new Pair<>(0,16)
    ), 10, 8);

    private final int width;
    private final int height;
    private final List<Pair<Integer, Integer>> uvCoordinates;
    private int index = 0;
    private long lastUpdate;

    ConnectionProgress(List<Pair<Integer, Integer>> uvCoordinates, int width, int height) {
        this.uvCoordinates = uvCoordinates;
        this.width = width;
        this.height = height;
    }

    public Pair<Integer, Integer> getId() {
        if(index >= this.uvCoordinates.size()) index = 0;
        System.out.println(this.name());
        System.out.println(this.uvCoordinates.get(0).getRight());
        return this.uvCoordinates.get(index);
    }

    public void tryIncrementIndex() {
        if(System.currentTimeMillis() - lastUpdate >= 1000) {
            index++;
            lastUpdate = System.currentTimeMillis();
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
