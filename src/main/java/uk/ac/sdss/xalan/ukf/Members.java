/**
 * members.xml-related extensions for Xalan.
 * 
 * Author: Ian A. Young, ian@iay.org.uk
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

class OrganizationName {

	private final Element e;
	
	static boolean isOrganizationName(Node node)
	{
		// Must be an Element
		if (!(node instanceof Element)) return false;

		/*
		 * Representation of an owner name in metadata and in the old schema for
		 * members.xml is as an md:OrganizationName.
		 */
		if ("urn:oasis:names:tc:SAML:2.0:metadata".equals(node.getNamespaceURI()) &&
				"OrganizationName".equals(node.getLocalName())) return true;

		/*
		 * Representation of an owner name in the new schema for
		 * members.xml is as a members:Name.
		 */
		if ("http://ukfederation.org.uk/2007/01/members".equals(node.getNamespaceURI()) &&
				"Name".equals(node.getLocalName())) return true;

		// Otherwise...
		return false;
	}
	
	String getName()
	{
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

	OrganizationName(Node node) {
		if (!isOrganizationName(node))
			throw new IllegalArgumentException("expected a members:Name or md:OrganizationName element");
		this.e = (Element)node;
	}
}

public class Members {

	/**
	 * Set of all the owner names within the members document.
	 */
	private Set<String> ownerNames = new HashSet<String>();
	
	private void collectOwnerNames(Node node)
	{
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
	 * @param node	Metadata document element to check.
	 * @return
	 */
	public boolean isOwnerName(Node node)
	{
		if (OrganizationName.isOrganizationName(node)) {
			OrganizationName org = new OrganizationName(node);
			return ownerNames.contains(org.getName());
		}
		return false;
	}
	
	/**
	 * Constructs a Members object representing the supplied members.xml document.
	 * 
	 * @param node	members.xml as a DOM instance.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Members(Node node) throws ParserConfigurationException, SAXException, IOException
	{
		collectOwnerNames(node);
		// System.out.println("Owner names: " + ownerNames.size());
	}
}
