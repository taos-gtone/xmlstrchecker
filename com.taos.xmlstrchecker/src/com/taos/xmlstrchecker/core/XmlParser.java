package com.taos.xmlstrchecker.core;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlParser {

    public Document parse(File file) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override public void error(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
                @Override public void fatalError(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
                @Override public void warning(org.xml.sax.SAXParseException e) throws SAXException { /* ignore */ }
            });

            return builder.parse(file);

        } catch (SAXException e) {
            throw new Exception("XML syntax error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("File read error: " + e.getMessage(), e);
        }
    }
}
