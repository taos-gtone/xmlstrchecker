package com.taos.xmlstrchecker.core;

import java.util.List;
import java.util.Map;

public class RuleSet {
  public Map<String, Object> meta;
  public Map<String, List<CodeItem>> code_sets;
  public List<ElementRule> elements;

  public static class CodeItem {
    public String id;
    public String general;
    public String casino;
    public String remark;
  }

  public static class ElementRule {
    public String path;              // XPath: /STR/Detail/Transaction/Method
    public String tag;               // tag name: Method
    public Cardinality cardinality;  // min/max
    public List<String> attributes;  // ["Code", ...]
    public String attr_constraints;  // line-based: required / fixed / ...
    public String data_length;       // line-based: text + attrs max length
    public String constraint1;
    public String constraint2;
    public String optional_required;
    public List<Rule> rules;
  }

  public static class Cardinality {
    public Integer min;
    public Integer max;
  }

  public static class Rule {
    public String type;        // code_set_check, forbidden_chars, date_format, references
    public String ref;         // code set name
    public String description; // human
    public String pattern;     // YYYYMMDD
    public List<String> tags;  // references 대상 tag들
  }
}
