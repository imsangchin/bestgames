package com.anzhuoshoudiantong;

public enum LedStatus {
    ON, OFF;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
