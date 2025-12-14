package com.taos.xmlstrchecker.core;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class RuleSetLoader {

    private static final ConcurrentHashMap<String, RuleSet> CACHE = new ConcurrentHashMap<>();

    public RuleSet loadByVersion(String versionName) {
        String file = mapVersionToFile(versionName);
        return loadFromBundle("rules/" + file);
    }

    public RuleSet loadFromBundle(String bundleRelativePath) {
        return CACHE.computeIfAbsent(bundleRelativePath, k -> {
            try {
                Bundle bundle = FrameworkUtil.getBundle(RuleSetLoader.class);
                if (bundle == null) {
                    throw new IllegalStateException("OSGi bundle not found");
                }

                try (InputStream is = FileLocator.openStream(bundle, new Path(k), false)) {
                    if (is == null) {
                        throw new IllegalArgumentException("Rules json not found: " + k);
                    }

                    String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject root = new JSONObject(json);

                    return RuleSetJsonMapper.fromJson(root);
                }

            } catch (Exception e) {
            	e.printStackTrace();
                throw new RuntimeException("Failed to load rules: " + k, e);
            }
        });
    }

    private String mapVersionToFile(String versionName) {
        if ("STR Dependency Rules".equalsIgnoreCase(versionName)
            || "STR Dependency Rules 5.6 (250416)".equalsIgnoreCase(versionName)) {
            return "dependency_rules.json";
        }
        return "dependency_rules.json";
    }
}
