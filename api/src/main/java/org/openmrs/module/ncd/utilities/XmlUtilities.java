/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Class providing useful utilities in the way of XML interaction.
 */
public class XmlUtilities
{
    private static TransformerFactory tf                            = TransformerFactory.newInstance();
    private static DocumentBuilderFactory documentBuilderFactory    = DocumentBuilderFactory.newInstance();
    private static SchemaFactory schemaFactory                      = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    private static XPathFactory xpathFactory                        = XPathFactory.newInstance();

    private XmlUtilities()
    {
    }

    public static Document createDocument(String rootName) throws ParserConfigurationException
    {        
        Document d = documentBuilderFactory.newDocumentBuilder().newDocument();
        d.appendChild(d.createElement(rootName));
        
        return d;
    }

    public static Document parseXmlContent(String content) throws IOException, ParserConfigurationException, SAXException
    {
        ByteArrayInputStream bas = new ByteArrayInputStream(content.getBytes());
        
        return documentBuilderFactory.newDocumentBuilder().parse(bas);
    }

    public static String translateNodeToString(Node node) throws TransformerException
    {
        Transformer t       = tf.newTransformer();
        DOMSource dSource   = new DOMSource(node);
        StringWriter sw     = new StringWriter();
        StreamResult result = new StreamResult(sw);

        t.transform(dSource, result);

        return sw.toString();
    }

    public static XPathExpression createXPathExpression(String expression) throws XPathExpressionException
    {
        return xpathFactory.newXPath().compile(expression);
    }

    /**
     * Find the value of a field value using an XPath statement.
     * 
     * This XPath is simpler than standard XPath and may not support all
     * constructs. In addition, when using "//" the first element
     * with that tag name will be used.
     * @param xpath
     *            The XPath statement that specifies the field value to find.
     * @param elem
     *            The element from which to do the search.
     * @return A String containing the field value
     */
    public static String findFieldValue(String xpath, Element elem)
    {
        if( !xpath.endsWith("text()") )
        {
            throw new IllegalArgumentException("The XPath statement must end with a 'text()' in order to get a string output.");
        }

        Element lastElem = findElement(xpath, elem);

        return lastElem != null ? lastElem.getTextContent() : "";
    }
    
    public static String findFieldValue(String xPath, Node node)
    {
    	return findFieldValue(xPath, (Element) node);
    }

    /**
     * Searches several xPaths for a field value, returning the first non-null one it finds.
     * 
     * @param xPaths
     * @param node
     * @return
     */
    public static String findFieldValue(List<String> xPaths, Element elem) {

    	for (String xPath : xPaths) 
    	{
    		String result = findFieldValue(xPath, elem);
    		
    		if (!StringUtils.isEmpty(result)) 
    		{
    			return result;
    		}
    	}
    	
    	return null;
    }

    /**
     * Searches several xPaths for a field value, returning the first non-null one it finds.
     * 
     * @param xPaths
     * @param node
     * @return
     */
    public static String findFieldValue(List<String> xPaths, Node node) 
    {
    	return findFieldValue(xPaths, (Element) node);
    }

    /**
     * Find an HL7 part (segment, field, etc.) using an XPath statement.
     * 
     * This XPath is simpler than standard XPath and may not support all
     * constructs. In addition, when using "//" the first element
     * with that tag name will be used.
     * 
     * @param xpath
     *            The XPath statement that specified the part to find.
     * @param elem
     *            The element from which to do the search.
     * @return An XML node that represents the specified HL7 part.
     */
    public static Node findHL7Part(String xpath, Element elem)
    {
        Element lastElem = findElement(xpath, elem);

        return lastElem;
    }
    
    public static Node findHL7Part(String xpath, Node node) 
    {
    	return findHL7Part(xpath, (Element)node);
    }

    private static Element findElement(String xpath, Element elem)
    {        
        if (elem == null)
        {
            return null;
        }
                
        xpath                       = xpath.replace("//", "**");
        String[] xpathElements      = xpath.split("/");
        int lastXPathElementIndex   = xpathElements.length - 1;
        
        if (xpathElements[lastXPathElementIndex].equals("text()"))
        {
            lastXPathElementIndex --;
        }

        Element curElem = elem;
        
        for( int curElementIndex = 0; curElementIndex <= lastXPathElementIndex; curElementIndex++ )
        {
            String curXPathElement = xpathElements[curElementIndex];
    
            if( curXPathElement.equals(".") )
            {
                // do nothing since this means the current node
                continue;
            }
            else if( curXPathElement.equals("..") )
            {
                // get the parent node
                curElem = (Element)curElem.getParentNode();
            }            
            else if ( curXPathElement.startsWith("**"))
            {
                curXPathElement = curXPathElement.replace("**", "");
                curElem = (Element)curElem.getOwnerDocument().getElementsByTagName(curXPathElement).item(0);
            }
            else if ( curXPathElement.startsWith("*"))
            {
            	curXPathElement = curXPathElement.replace("*", "");
                curElem = (Element)curElem.getElementsByTagName(curXPathElement).item(0);
            }
            else
            {
                curElem = (Element)curElem.getElementsByTagName(curXPathElement).item(0);
            }
            
            if (curElem == null)
            {
                break;
            }
        }
        
        return curElem;
    }

    public static Element appendTextNode(Document document, Node parentNode, String nodeName, String nodeContent)
    {
        Element e = document.createElement(nodeName);
        e.setTextContent(nodeContent);
        parentNode.appendChild(e);

        return e;
    }

    public static Document parseXmlContentFromFile(String fileLocation) throws IOException, ParserConfigurationException, SAXException
    {
        return XmlUtilities.parseXmlContent(FileUtilities.readFileFromClasspathToString( XmlUtilities.class, fileLocation).replaceAll("[\n\r]","") );
    }

    public static Schema createSchemaFromDocument(Document d) throws SAXException
    {
        return schemaFactory.newSchema(new DOMSource(d));
    }

    public static Schema createSchemaFromClasspathFile(String fileLocation) throws SAXException
    {
        return schemaFactory.newSchema(new StreamSource(XmlUtilities.class.getClassLoader().getResourceAsStream(fileLocation)));
    }

    public static void validateDocumentAgainstSchema(Document document, Schema schema) throws IOException, SAXException
    {
        schema.newValidator().validate(new DOMSource(document));
    }

    /** Converts an XML DOM Node tree into an S-expression-like list, for
     * debugging purposes.
     * 
     * @param node The root of the node tree to convert
     * @return The text representation of the node tree.
     */
    public static String toString(Node node) 
    {
    	if (node == null) 
    	{
    		return null;
    	}
    	else 
    	{
    		return nodeToString(node, "");
    	}
    }

    private static String nodeToString(Node node, String indent) 
    {
    	StringBuilder buf = new StringBuilder(); 	
    	buf.append("\n" + indent + "name=" + node.getNodeName() + ", value=" + node.getNodeValue());
    	
    	for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) 
    	{
    		buf.append(nodeToString(child, indent + "  "));
    	}
    	
    	return buf.toString();
    }
        
}
