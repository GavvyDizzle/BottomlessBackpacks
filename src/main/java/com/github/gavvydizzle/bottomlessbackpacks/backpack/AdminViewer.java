package com.github.gavvydizzle.bottomlessbackpacks.backpack;

public class AdminViewer {

    private final Backpack backpack;
    private int page;

    public AdminViewer(Backpack backpack, int page) {
        this.backpack = backpack;
        this.page = page;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
