package com.taos.xmlstrchecker.core;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DependencyChecker {

    public void check(Document doc, CheckResult result) {
        NodeList items = doc.getElementsByTagName("item");

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            String id = el.getAttribute("id");
            if (id == null || id.isEmpty()) {
                result.addError("ERROR", "<item> element has no id attribute.", getElementLocation(el));
            } else {
                ids.add(id);
            }
        }

        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            String ref = el.getAttribute("ref");
            if (ref != null && !ref.isEmpty() && !ids.contains(ref)) {
                result.addError("ERROR",
                        "No <item> with id matching ref=\"" + ref + "\".",
                        getElementLocation(el));
            }
        }
    }

    private String getElementLocation(Element el) {
        String tag = el.getTagName();
        String id = el.getAttribute("id");
        return (id != null && !id.isEmpty())
                ? "<" + tag + " id=\"" + id + "\">"
                : "<" + tag + ">";
    }
}
