package com.taos.xmlstrchecker.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.xpath.*;

import org.w3c.dom.*;

public class StrRuleValidator {

  private final RuleSet ruleSet;

  public StrRuleValidator(RuleSet ruleSet) {
    this.ruleSet = ruleSet;
  }

  public CheckResult validate(Document doc) {
    CheckResult result = new CheckResult();
    if (ruleSet == null || ruleSet.elements == null) {
      result.addError("RULES", "Ruleset is empty.", "");
      return result;
    }

    XPath xp = XPathFactory.newInstance().newXPath();

    // code set cache
    Map<String, Set<String>> codeSetIds = buildCodeSetIds(ruleSet);

    for (RuleSet.ElementRule er : ruleSet.elements) {
      if (er == null || er.path == null || er.path.isBlank()) continue;

      try {
        NodeList nodes = (NodeList) xp.evaluate(er.path, doc, XPathConstants.NODESET);
        int count = nodes.getLength();

        // cardinality
        Integer min = er.cardinality != null ? er.cardinality.min : null;
        Integer max = er.cardinality != null ? er.cardinality.max : null;

        if (min != null && count < min) {
          result.addError("RULES", "Missing required element (min=" + min + ", found=" + count + ")", er.path);
        }
        if (max != null && max > 0 && count > max) {
          result.addError("RULES", "Too many occurrences (max=" + max + ", found=" + count + ")", er.path);
        }

        for (int i = 0; i < count; i++) {
          Node n = nodes.item(i);
          if (n.getNodeType() != Node.ELEMENT_NODE) continue;

          Element el = (Element) n;
          String loc = er.path + "[" + (i + 1) + "]";

          // required attributes
          validateRequiredAttributes(er, el, loc, result);

          // length checks
          validateLengths(er, el, loc, result);

          // structured rules
          if (er.rules != null) {
            for (RuleSet.Rule r : er.rules) {
              if (r == null || r.type == null) continue;

              switch (r.type) {
                case "forbidden_chars":
                  checkForbiddenChars(textOf(el), loc, result);
                  break;

                case "date_format":
                  if ("YYYYMMDD".equalsIgnoreCase(r.pattern)) {
                    checkYYYYMMDD(textOf(el), loc, result);
                  }
                  break;

                case "code_set_check": {
                  String ref = r.ref;
                  String codeValue = pickCodeValue(el);
                  if (ref == null || ref.isBlank()) {
                    result.addError("RULES", "code_set_check: ref is missing", er.path);
                    break;
                  }
                  Set<String> allowed = codeSetIds.get(ref);
                  if (allowed == null || allowed.isEmpty()) {
                    result.addError("RULES", "code_set_check: code set not found/empty: " + ref, er.path);
                    break;
                  }
                  if (codeValue == null || codeValue.isBlank()) {
                    result.addError("RULES", "code_set_check: code value is empty (ref=" + ref + ")", er.path);
                    break;
                  }
                  if (!allowed.contains(codeValue.trim())) {
                    result.addError("RULES", "Invalid code '" + codeValue + "' (ref=" + ref + ")", er.path);
                  }
                  break;
                }

                case "references":
                  // 예: Method Code=31/32면 AccountRelation/Account 필요 같은 룰
                  if ("Method".equals(er.tag) && r.description != null
                      && (r.description.contains("31") || r.description.contains("32"))) {
                    applyVirtualAssetAccountRequirement(el, loc, result);
                  } else {
                    // Count = A+B 형태 (warn 정도로)
                    applyNumericSumReference(el, r.tags, loc, result);
                  }
                  break;

                default:
                  // 모르는 rule 타입은 경고/무시
                  // result.addError(new CheckError(loc, "Unsupported rule type: " + r.type));
                  break;
              }
            }
          }
        }
      } catch (Exception ex) {
        result.addError("RULES", "Validator exception: " + ex.getMessage(), er.path);
      }
    }

    // cross rules (Transaction 단위)
    applyTransactionCrossRules(doc, result);

    return result;
  }

  // ---------------- helpers ----------------

  private Map<String, Set<String>> buildCodeSetIds(RuleSet rs) {
    Map<String, Set<String>> out = new HashMap<>();
    if (rs.code_sets == null) return out;

    for (Map.Entry<String, List<RuleSet.CodeItem>> e : rs.code_sets.entrySet()) {
      Set<String> ids = new HashSet<>();
      if (e.getValue() != null) {
        for (RuleSet.CodeItem ci : e.getValue()) {
          if (ci != null && ci.id != null) ids.add(ci.id.trim());
        }
      }
      out.put(e.getKey(), ids);
    }
    return out;
  }

  private void validateRequiredAttributes(RuleSet.ElementRule er, Element el, String loc, CheckResult result) {
    if (er.attributes == null || er.attributes.isEmpty()) return;
    List<String> attrs = er.attributes;
    List<String> cons = splitLines(er.attr_constraints);

    for (int i = 0; i < attrs.size(); i++) {
      String a = attrs.get(i);
      String c = (i < cons.size() ? cons.get(i) : "");
      boolean required = c.toLowerCase().contains("required");

      if (required) {
        String v = el.getAttribute(a);
        if (v == null || v.isBlank()) {
          result.addError("RULES", "Missing required attribute @", el.getNodeName());
        }
      }
    }
  }

  private void validateLengths(RuleSet.ElementRule er, Element el, String loc, CheckResult result) {
    if (er.data_length == null || er.data_length.isBlank()) return;

    List<String> lensRaw = splitLines(er.data_length);
    List<Integer> lens = new ArrayList<>();
    for (String s : lensRaw) {
      String num = s.replaceAll("[^0-9]", "");
      if (!num.isEmpty()) {
        try { lens.add(Integer.parseInt(num)); } catch (Exception ignore) {}
      }
    }
    if (lens.isEmpty()) return;

    List<String> attrs = er.attributes == null ? List.of() : er.attributes;

    int textMax = -1;
    int offset = 0;
    if (lens.size() == attrs.size() + 1) {
      textMax = lens.get(0);
      offset = 1;
    }

    if (textMax > 0) {
      String t = textOf(el);
      if (t != null && t.length() > textMax) {
        result.addError("RULES", "Text length exceeds " + textMax + " (len=" + t.length() + ")", loc);
      }
    }

    for (int i = 0; i < attrs.size(); i++) {
      int idx = i + offset;
      if (idx >= lens.size()) break;

      int maxLen = lens.get(idx);
      String v = el.getAttribute(attrs.get(i));
      if (v != null && !v.isBlank() && v.length() > maxLen) {
        result.addError("RULES",
            "Attribute @" + attrs.get(i) + " length exceeds " + maxLen + " (len=" + v.length() + ")", loc);
      }
    }
  }

  private String pickCodeValue(Element el) {
    String code = el.getAttribute("Code");
    if (code != null && !code.isBlank()) return code.trim();
    String t = el.getTextContent();
    return t == null ? null : t.trim();
  }

  private String textOf(Element el) {
    String t = el.getTextContent();
    return t == null ? null : t.trim();
  }

  private void checkForbiddenChars(String v, String loc, CheckResult result) {
    if (v == null) return;
    if (v.contains("<") || v.contains(">") || v.contains("\"") || v.contains(";")) {
      result.addError("RULES", "Forbidden characters found", loc);
    }
  }

  private void checkYYYYMMDD(String v, String loc, CheckResult result) {
    if (v == null || v.isBlank()) return;
    if (!Pattern.matches("\\d{8}", v)) {
      result.addError("RULES", "Invalid date format (YYYYMMDD): " + v, loc);
      return;
    }
    try {
      LocalDate.parse(v, DateTimeFormatter.BASIC_ISO_DATE);
    } catch (Exception e) {
      result.addError("RULES", "Invalid calendar date: " + v, loc);
    }
  }

  private void applyNumericSumReference(Element el, List<String> tags, String loc, CheckResult result) {
    if (tags == null || tags.isEmpty()) return;

    Integer self = tryInt(textOf(el));
    if (self == null) return;

    Node parent = el.getParentNode();
    if (parent == null || parent.getNodeType() != Node.ELEMENT_NODE) return;

    Element p = (Element) parent;
    int sum = 0;
    for (String tag : tags) {
      NodeList nl = p.getElementsByTagName(tag);
      if (nl.getLength() == 0) continue;
      Node n = nl.item(0);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Integer v = tryInt(((Element) n).getTextContent().trim());
        if (v != null) sum += v;
      }
    }

    if (self != sum) {
      // 여기서는 error 대신 warn성격인데, CheckResult에 warn이 없으면 message로 구분하거나 ErrorTablePresenter에서 레벨 지원해도 됨
      result.addError("RULES", "Reference sum mismatch: expected " + sum + ", actual " + self, loc);
    }
  }

  private void applyVirtualAssetAccountRequirement(Element methodEl, String loc, CheckResult result) {
    String code = methodEl.getAttribute("Code");
    if (code == null || code.isBlank()) return;
    code = code.trim();

    if (!"31".equals(code) && !"32".equals(code)) return;

    Element tx = findAncestor(methodEl, "Transaction");
    if (tx == null) {
      result.addError("RULES", "Cannot find ancestor Transaction", loc);
      return;
    }
    boolean hasAccountRelation = tx.getElementsByTagName("AccountRelation").getLength() > 0;
    boolean hasAccount = tx.getElementsByTagName("Account").getLength() > 0;

    if (!hasAccountRelation || !hasAccount) {
      result.addError("RULES",
          "Method Code=" + code + " requires <AccountRelation> and <Account> in same <Transaction>", loc);
    }
  }

  private void applyTransactionCrossRules(Document doc, CheckResult result) {
    NodeList txs = doc.getElementsByTagName("Transaction");
    for (int i = 0; i < txs.getLength(); i++) {
      Element tx = (Element) txs.item(i);
      String loc = "/STR/Detail/Transaction[" + (i + 1) + "]";

      NodeList userRelations = tx.getElementsByTagName("UserRelation");

      Set<String> roles = new HashSet<>();
      int role01Count = 0;

      for (int j = 0; j < userRelations.getLength(); j++) {
        Element ur = (Element) userRelations.item(j);
        NodeList rr = ur.getElementsByTagName("RelationRole");
        if (rr.getLength() == 0) continue;

        Element rrEl = (Element) rr.item(0);
        String role = rrEl.getAttribute("Code");
        if (role == null || role.isBlank()) role = rrEl.getTextContent().trim();
        if (role == null || role.isBlank()) continue;

        if (!roles.add(role)) {
          result.addError("RULES", "Duplicate RelationRole: " + role, loc);
        }
        if ("01".equals(role)) role01Count++;
      }

      if (role01Count == 0) {
        result.addError("RULES", "Missing required RelationRole Code='01' (의심거래자)",loc);
      }

      if (roles.contains("12") && !roles.contains("11")) {
        result.addError("RULES", "If RelationRole '12' exists, '11' must also exist", loc);
      }
    }
  }

  private Element findAncestor(Node n, String tag) {
    Node cur = n;
    while (cur != null) {
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) cur;
        if (tag.equals(e.getTagName())) return e;
      }
      cur = cur.getParentNode();
    }
    return null;
  }

  private Integer tryInt(String s) {
    try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
  }

  private List<String> splitLines(String s) {
    if (s == null) return List.of();
    String[] arr = s.split("\\r?\\n");
    List<String> out = new ArrayList<>();
    for (String a : arr) out.add(a.trim());
    return out;
  }
}
