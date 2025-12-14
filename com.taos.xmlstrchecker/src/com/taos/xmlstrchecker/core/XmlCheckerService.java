package com.taos.xmlstrchecker.core;

import java.io.File;

import org.w3c.dom.Document;

public class XmlCheckerService {

    private final XmlParser parser;
    private final DependencyChecker dependencyChecker;

    public XmlCheckerService(XmlParser parser, DependencyChecker dependencyChecker) {
        this.parser = parser;
        this.dependencyChecker = dependencyChecker;
    }

    public CheckResult check(String path) {
        CheckResult result = new CheckResult();

        if (path == null || path.trim().isEmpty()) {
            result.setStatus(CheckResult.StatusKind.WARNING, "Warning: Please select a file.");
            result.addError("ERROR", "No file selected.", "-");
            return result;
        }

        File file = new File(path);
        if (!file.exists()) {
            result.setStatus(CheckResult.StatusKind.ERROR, "Error: File not found.");
            result.addError("ERROR", "File does not exist.", path);
            return result;
        }

        try {
            Document doc = parser.parse(file);
            dependencyChecker.check(doc, result);

            if (result.getErrors().isEmpty()) {
                result.setStatus(CheckResult.StatusKind.SUCCESS,
                        "Success: XML format and dependency checks passed.");
            } else {
                result.setStatus(CheckResult.StatusKind.ERROR,
                        "Error: Validation failed.");
            }

        } catch (Exception e) {
            result.setStatus(CheckResult.StatusKind.ERROR, "Error: " + e.getMessage());
            result.addError("ERROR", e.getMessage(), path);
        }

        return result;
    }
}
