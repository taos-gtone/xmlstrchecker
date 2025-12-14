package com.taos.xmlstrchecker.core;

import java.io.File;
import org.w3c.dom.Document;

public class XmlCheckerService {

    private final XmlParser parser;
    private final DependencyChecker dependencyChecker;
    private final StrRuleValidatorService strRuleValidatorService;

    public XmlCheckerService(XmlParser parser,
                             DependencyChecker dependencyChecker,
                             StrRuleValidatorService strRuleValidatorService) {
        this.parser = parser;
        this.dependencyChecker = dependencyChecker;
        this.strRuleValidatorService = strRuleValidatorService;
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

            // 1) 기존 dependency 체크
            dependencyChecker.check(doc, result);

            // ✅ 2) STR Rule 체크 추가
            // 버전은 콤보 선택값을 넘기면 좋고, 지금은 기본 버전으로 예시
            strRuleValidatorService.check(doc, "v5.6 (250416)", result);

            if (result.getErrors().isEmpty()) {
                result.setStatus(CheckResult.StatusKind.SUCCESS,
                        "Success: XML format, dependency and STR rule checks passed.");
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
