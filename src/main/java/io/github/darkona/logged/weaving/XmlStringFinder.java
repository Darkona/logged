package io.github.darkona.logged.weaving;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

class XmlStringFinder {

    private static final Logger log = LoggerFactory.getLogger(XmlStringFinder.class);

    public static boolean check(Resource res, String searchString) {
        // getInputStream instead of getFile: aop.xml is usually inside a jar, where getFile throws
        try (InputStream in = res.getInputStream()) {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            var doc = factory.newDocumentBuilder().parse(in);

            var allNodes = doc.getElementsByTagName("aspect");

            for (int i = 0; i < allNodes.getLength(); i++) {
                if (allNodes.item(i) instanceof Element e && e.getAttribute("name").equals(searchString)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.debug("Could not inspect {} for aspect '{}'", res, searchString, ex);
        }
        return false;
    }

}
