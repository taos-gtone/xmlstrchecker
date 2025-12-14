package com.taos.xmlstrchecker.core;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class RuleSetJsonMapper {

    public static RuleSet fromJson(JSONObject root) {
        RuleSet rs = new RuleSet();

        rs.code_sets = parseCodeSets(root.optJSONObject("code_sets"));
        rs.elements  = parseElements(root.optJSONArray("elements"));

        return rs;
    }

    private static Map<String, List<RuleSet.CodeItem>> parseCodeSets(JSONObject obj) {
        Map<String, List<RuleSet.CodeItem>> out = new HashMap<>();
        if (obj == null) return out;

        for (String key : obj.keySet()) {
            JSONArray arr = obj.optJSONArray(key);
            List<RuleSet.CodeItem> list = new ArrayList<>();

            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;

                    RuleSet.CodeItem ci = new RuleSet.CodeItem();
                    ci.id      = o.optString("id", null);
                    ci.general = o.optString("general", null);
                    ci.casino  = o.optString("casino", null);
                    ci.remark  = o.optString("remark", null);

                    list.add(ci);
                }
            }
            out.put(key, list);
        }
        return out;
    }

    private static List<RuleSet.ElementRule> parseElements(JSONArray arr) {
        List<RuleSet.ElementRule> out = new ArrayList<>();
        if (arr == null) return out;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;

            RuleSet.ElementRule er = new RuleSet.ElementRule();
            er.path = o.optString("path", null);
            er.tag  = o.optString("tag", null);

            JSONObject card = o.optJSONObject("cardinality");
            if (card != null) {
                RuleSet.Cardinality c = new RuleSet.Cardinality();
                Integer max = null;
                
                c.min = card.has("min") ? card.getInt("min") : null;
                if (card.has("max") && !card.isNull("max")) {
                    max = card.getInt("max");
                }
                c.max = max;
                
                er.cardinality = c;
            }

            er.attributes        = toStringList(o.optJSONArray("attributes"));
            er.attr_constraints  = o.optString("attr_constraints", null);
            er.data_length       = o.optString("data_length", null);
            er.constraint1       = o.optString("constraint1", null);
            er.constraint2       = o.optString("constraint2", null);
            er.optional_required = o.optString("optional_required", null);
            er.rules             = parseRules(o.optJSONArray("rules"));

            out.add(er);
        }
        return out;
    }

    private static List<RuleSet.Rule> parseRules(JSONArray arr) {
        List<RuleSet.Rule> out = new ArrayList<>();
        if (arr == null) return out;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;

            RuleSet.Rule r = new RuleSet.Rule();
            r.type        = o.optString("type", null);
            r.ref         = o.optString("ref", null);
            r.description = o.optString("description", null);
            r.pattern     = o.optString("pattern", null);
            r.tags        = toStringList(o.optJSONArray("tags"));

            out.add(r);
        }
        return out;
    }

    private static List<String> toStringList(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            out.add(arr.optString(i));
        }
        return out;
    }
}
