package com.taos.xmlstrchecker.core;

public class CheckError {
    public final String type;
    public final String message;
    public final String location;

    public CheckError(String type, String message, String location) {
        this.type = type;
        this.message = message;
        this.location = location;
    }
}
