/*
 * Copyright (C) 2013 University of Edinburgh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.sdss.xalan.ukf;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * members.xml-related extensions for Xalan.
 * 
 * Author: Ian A. Young, ian@iay.org.uk
 */
class OrganizationName {

    private final Element e;
    
    OrganizationName(Node node) {
        if (!isOrganizationName(node)) {
            throw new IllegalArgumentException("expected a members:Name or md:OrganizationName element");
        }
        this.e = (Element)node;
    }

    static boolean isOrganizationName(Node node) {
        // Must be an Element
        if (!(node instanceof Element)) {
            return false;
        }

        /*
         * Representation of an owner name in metadata and in the old schema for
         * members.xml is as an md:OrganizationName.
         */
        if ("urn:oasis:names:tc:SAML:2.0:metadata".equals(node.getNamespaceURI()) &&
                "OrganizationName".equals(node.getLocalName())) {
            return true;
        }

        /*
         * Representation of an owner name in the new schema for
         * members.xml is as a members:Name.
         */
        if ("http://ukfederation.org.uk/2007/01/members".equals(node.getNamespaceURI()) &&
                "Name".equals(node.getLocalName())) {
            return true;
        }

        // Otherwise...
        return false;
    }
    
    String getName() {
        Node node = e.getFirstChild();
        StringBuilder b = new StringBuilder();
        while (node != null) {
            if (node instanceof Text) {
                b.append(node.getNodeValue());
            }
            node = node.getNextSibling();
        }
        return b.toString();
    }

}

public class Members {

    /**
     * Set of all the owner names within the members document.
     */
    private Set<String> ownerNames = new HashSet<String>();
    
    /**
     * Constructs a Members object representing the supplied members.xml document.
     * 
     * @param node    members.xml as a DOM instance.
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Members(Node node) throws ParserConfigurationException, SAXException, IOException {
        collectOwnerNames(node);
        // System.out.println("Owner names: " + ownerNames.size());
    }

    private void collectOwnerNames(Node node) {
        if (OrganizationName.isOrganizationName(node)) {
            OrganizationName org = new OrganizationName(node);
            ownerNames.add(org.getName());
            return;
        }
        NodeList inner = node.getChildNodes();
        for (int i = 0; i < inner.getLength(); i++) {
            collectOwnerNames(inner.item(i));
        }
    }
    
    /**
     * Checks whether the provided document node is an XML representation of an
     * owner name which is one of those declared in the members.xml document.
     * 
     * @param node    Metadata document element to check.
     * @return
     */
    public boolean isOwnerName(Node node) {
        if (OrganizationName.isOrganizationName(node)) {
            OrganizationName org = new OrganizationName(node);
            return ownerNames.contains(org.getName());
        }
        return false;
    }
    
}
