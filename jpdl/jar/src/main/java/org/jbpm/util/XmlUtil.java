/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class XmlUtil {
  
  public static Document parseXmlText(String xml) {
    ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
    return parseXmlInputSource(new InputSource(bais));
  }

  public static Document parseXmlResource(String resource) {
    InputStream inputStream = ClassLoaderUtil.getStream(resource);
    InputSource inputSource = new InputSource(inputStream);
    return parseXmlInputSource(inputSource);
  }

  public static Document parseXmlInputStream(InputStream inputStream) {
    Document document = null;
    try {
      document  = getDocumentBuilder().parse(inputStream);
    } catch (Exception e) {
      throw new XmlException("couldn't parse xml", e);
    }
    return document;
  }

  public static Document parseXmlInputSource(InputSource inputSource) {
    Document document = null;
    try {
      document  = getDocumentBuilder().parse(inputSource);
    } catch (Exception e) {
      throw new XmlException("couldn't parse xml", e);
    }
    return document;
  }

  public static DocumentBuilder getDocumentBuilder() throws FactoryConfigurationError, ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    return factory.newDocumentBuilder();
  }

  public static Iterator elementIterator(Element element, String tagName) {
    return elements(element, tagName).iterator();
  }

  public static List elements(Element element, String tagName) {
    NodeList nodeList = element.getElementsByTagName(tagName);
    List elements = new ArrayList(nodeList.getLength());
    for (int i=0; i<nodeList.getLength(); i++) {
      Node child = nodeList.item(i);
      if(child.getParentNode()==element) {
        elements.add(child);
      }
    }
    return elements;
  }

  public static Element element(Element element, String name) {
    Element childElement = null;
    NodeList nodeList = element.getElementsByTagName(name);
    if (nodeList.getLength()>0) {
      childElement = (Element) nodeList.item(0);
    }
    return childElement;
  }

  public static Iterator elementIterator(Element element) {
    return elements(element).iterator();
  }

  public static List elements(Element element) {
    List elements = new ArrayList();
    NodeList nodeList = element.getChildNodes();
    for (int i=0; i<nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if ( (node instanceof Element)
           && (element==node.getParentNode())
         ){
        elements.add(node);
      }
    }
    return elements;
  }


  public static Element element(Element element) {
    Element onlyChild = null;
    List elements = elements(element);
    if (! elements.isEmpty()) {
      onlyChild = (Element) elements.get(0);
    }
    return onlyChild;
  }

  public static String toString(Element element) {
    if (element==null) return "null";

    Source source = new DOMSource(element);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    Result result = new StreamResult(printWriter);
    
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(source, result);
    } catch (Exception e) {
      throw new XmlException("couldn't write element '"+element.getTagName()+"' to string", e);
    }
    
    printWriter.close();

    return stringWriter.toString();
  }

  public static String getContentText(Element element) {
    StringBuffer buffer = new StringBuffer();
    NodeList nodeList = element.getChildNodes();
    for (int i=0; i<nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof CharacterData) {
        CharacterData characterData = (CharacterData) node;
        buffer.append(characterData.getData());
      }
    }
    return buffer.toString();
  }
}