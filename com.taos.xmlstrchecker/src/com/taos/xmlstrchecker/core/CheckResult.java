package com.taos.xmlstrchecker.core;

import java.util.ArrayList;
import java.util.List;

public class CheckResult {
    public enum StatusKind { READY, SUCCESS, WARNING, ERROR }

    private StatusKind statusKind = StatusKind.READY;
    private String statusMessage = "";
    private final List<CheckError> errors = new ArrayList<>();

    public StatusKind getStatusKind() { return statusKind; }
    public String getStatusMessage() { return statusMessage; }
    public List<CheckError> getErrors() { return errors; }

    public void setStatus(StatusKind kind, String message) {
        this.statusKind = kind;
        this.statusMessage = message != null ? message : "";
    }

    public void addError(String type, String message, String location) {
        errors.add(new CheckError(type, message, location));
    }
}
