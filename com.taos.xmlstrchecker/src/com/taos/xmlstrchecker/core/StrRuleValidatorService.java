package com.taos.xmlstrchecker.core;

import org.w3c.dom.Document;

public class StrRuleValidatorService {

    private final RuleSetLoader ruleSetLoader = new RuleSetLoader();

    public void check(Document doc, String versionName, CheckResult result) {
        try {
            RuleSet rules = ruleSetLoader.loadByVersion(versionName);
            StrRuleValidator validator = new StrRuleValidator(rules);

            CheckResult r = validator.validate(doc);

            for (CheckError err : r.getErrors()) {
                if (err == null) continue;
                result.addError(err.type, err.message, err.location);
            }

        } catch (Exception e) {
            result.addError("ERROR", "STR rules validation failed: " + e.getMessage(), "RULES");
        }
    }
}
