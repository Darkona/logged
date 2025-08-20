package io.github.darkona.logged.weaving;

import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

class XmlStringFinder {

    public static boolean check(Resource res, String searchString) {
        try {
            var doc = DocumentBuilderFactory.newInstance()
                                            .newDocumentBuilder()
                                            .parse(res.getFile());

            var allNodes = doc.getElementsByTagName("aspect");

            for (int i = 0; i < allNodes.getLength(); i++) {
                if (allNodes.item(i) instanceof Element e && e.getAttribute("name").equals(searchString)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return false;
    }

}
