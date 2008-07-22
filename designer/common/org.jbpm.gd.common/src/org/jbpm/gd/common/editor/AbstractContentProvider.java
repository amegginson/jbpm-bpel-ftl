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
package org.jbpm.gd.common.editor;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.jbpm.gd.common.Logger;
import org.jbpm.gd.common.model.NamedElement;
import org.jbpm.gd.common.model.SemanticElement;
import org.jbpm.gd.common.notation.AbstractNodeContainer;
import org.jbpm.gd.common.notation.BendPoint;
import org.jbpm.gd.common.notation.Edge;
import org.jbpm.gd.common.notation.Node;
import org.jbpm.gd.common.notation.NodeContainer;
import org.jbpm.gd.common.notation.NotationElement;
import org.jbpm.gd.common.notation.NotationMapping;
import org.jbpm.gd.common.notation.RootContainer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public abstract class AbstractContentProvider implements ContentProvider{
	
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	protected abstract SemanticElement getEdgeSemanticElement(Node node, Element notationInfo, int index);
	protected abstract SemanticElement getNodeSemanticElement(NodeContainer node, Element notationInfo, int index);
	protected abstract void addNodes(NodeContainer nodeContainer, Element notationInfo);
	protected abstract void addEdges(Node node, Element notationInfo);
	protected abstract SemanticElement findDestination(Edge edge, Node source);
	
	protected String getRootNotationInfoElement() {
		return "<root-container/>";
	}
	
	protected String createInitialNotationInfo() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("\n\n");
		buffer.append(getRootNotationInfoElement());
		return buffer.toString();
	}

	protected String getNotationInfoFileName(String semanticInfoFileName) {
		return ".gpd." + semanticInfoFileName;
	}

	protected String getSemanticInfoFileName(String notationInfoFileName) {
		return notationInfoFileName.substring(5);
	}

	protected void processRootContainer(RootContainer rootContainer, Element notationInfo) {
		addDimension(rootContainer, notationInfo);
		addNodes(rootContainer, notationInfo);
		postProcess(rootContainer);
	}
	
	private ArrayList getNodeElements(Element notationElement) {
		ArrayList result = new ArrayList();
		NodeList nodeList = notationElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);
			if (!(node instanceof Element)) continue;
			Element element = (Element)node;
			if ("node".equals(element.getNodeName()) || "node-container".equals(element.getNodeName())) {
				result.add(element);
			}
		}
		return result;
	}
	
	protected void addNodes(NodeContainer nodeContainer, SemanticElement[] semanticElements, Element notationInfo) {
		ArrayList children = getNodeElements(notationInfo);
		for (int i = 0; i < semanticElements.length; i++) {
			Element notationInfoElement = null;
			String nodeName = ((NamedElement)semanticElements[i]).getName();
			for (int j = 0; j < children.size(); j++) {
				Element element = (Element)children.get(j);
				String elementName = getAttribute(element, "name");
 				if ((elementName != null && elementName.equals(nodeName)) || (elementName == null && nodeName == null)) {
					notationInfoElement = element;
					break;
				}
			}
			if (notationInfoElement != null) {
				addNode(nodeContainer, semanticElements[i], notationInfoElement);
			}
		}
	}
	
//	private void write(org.w3c.dom.Node element) {
//		try {
//			DOMSource domSource = new DOMSource(element);
//			Transformer transformer = transformerFactory.newTransformer();
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			StreamResult streamResult = new StreamResult(System.out);
//			transformer.transform(domSource, streamResult);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private ArrayList getEdgeElements(Element notationElement) {
		ArrayList result = new ArrayList();
		NodeList nodeList = notationElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);
			if (!(node instanceof Element)) continue;
			Element element = (Element)node;
			if ("edge".equals(element.getNodeName())) {
				result.add(element);
			}
		}
		return result;
	}
	
	protected void addEdges(Node node, SemanticElement[] semanticElements, Element notationInfo) {
		ArrayList children = getEdgeElements(notationInfo);
		for (int i = 0; i < semanticElements.length; i++) {
			Element notationInfoElement = null;
			if (children.size() >= i + 1) {
				notationInfoElement = (Element)children.get(i);
			}
			addEdge(node, semanticElements[i], notationInfoElement);
		}
	}
	
	protected void addNode(NodeContainer nodeContainer, SemanticElement semanticElement, Element notationInfoElement) {
		String notationElementId = NotationMapping.getNotationElementId(semanticElement.getElementId());
		Node notationElement = (Node)nodeContainer.getFactory().create(notationElementId);
		notationElement.setSemanticElement(semanticElement);
		notationElement.register();
		nodeContainer.addNode(notationElement);
		semanticElement.addPropertyChangeListener(notationElement);
		processNode(notationElement, notationInfoElement);
		if (notationElement instanceof NodeContainer) {
			addNodes((NodeContainer)notationElement, notationInfoElement);
		}
	}
	
	protected void addEdge(Node node, SemanticElement semanticElement, Element notationInfoElement) {
		NotationElement notationElement = node.getRegisteredNotationElementFor(semanticElement);
		if (notationElement == null) {
			String notationElementId = NotationMapping.getNotationElementId(semanticElement.getElementId());
			notationElement = (NotationElement)node.getFactory().create(notationElementId);
			notationElement.setSemanticElement(semanticElement);
			notationElement.register();
			node.addLeavingEdge((Edge)notationElement);
			semanticElement.addPropertyChangeListener(notationElement);
		}
		processEdge((Edge)notationElement, notationInfoElement);
	}
	
	protected void addDimension(
			RootContainer processDefinitionNotationElement, 
			Element processDiagramInfo) {
		String width = getAttribute(processDiagramInfo, "width");
		String height = getAttribute(processDiagramInfo, "height");
		Dimension dimension = new Dimension(
			width == null ? 0 : Integer.valueOf(width).intValue(),
			height == null ? 0 : Integer.valueOf(height).intValue());
		processDefinitionNotationElement.setDimension(dimension);
	}

	protected void processNode(Node node, Element notationInfoElement) {
		addConstraint(node, notationInfoElement);		
		addEdges(node, notationInfoElement);
	}
	
	protected void processEdge(Edge edge, Element edgeInfo) {
		processLabel(edge, edgeInfo);
		addBendpoints(edge, edgeInfo);		
	}
	
	private ArrayList getBendpointElements(Element notationInfo) {
		ArrayList result = new ArrayList();
		NodeList nodeList = notationInfo.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);
			if (!(node instanceof Element)) continue;
			Element element = (Element)node;
			if ("bendpoint".equals(element.getNodeName())) {
				result.add(element);
			}
		}
		return result;
	}
	
	protected void addBendpoints(Edge edge, Element edgeInfo) {
		if (edgeInfo != null) {
			ArrayList children = getBendpointElements(edgeInfo);
			for (int i = 0; i < children.size(); i++) {
				addBendpoint(edge, (Element)children.get(i), i);
			}
		}
	}
	
	protected BendPoint addBendpoint(Edge edge, Element bendpointInfo, int index) {
		BendPoint result = new BendPoint();
		processBendpoint(result, bendpointInfo);
		edge.addBendPoint(result);
		return result;
	}
	
	protected void processBendpoint(BendPoint bendPoint, Element bendpointInfo) {
		int w1 = Integer.valueOf(getAttribute(bendpointInfo, "w1")).intValue();
		int h1 = Integer.valueOf(getAttribute(bendpointInfo, "h1")).intValue();
		int w2 = Integer.valueOf(getAttribute(bendpointInfo, "w2")).intValue();
		int h2 = Integer.valueOf(getAttribute(bendpointInfo, "h2")).intValue();
		Dimension d1 = new Dimension(w1, h1);
		Dimension d2 = new Dimension(w2, h2);
		bendPoint.setRelativeDimensions(d1, d2);		
	}
	
	
	
	private Element getLabelElement(Element notationInfo) {
		NodeList nodeList = notationInfo.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node node = nodeList.item(i);
			if (!(node instanceof Element)) continue;
			Element element = (Element)node;
			if ("label".equals(element.getNodeName())) {
				return element;
			}
		}
		return null;
	}
	
	private void processLabel(Edge edge, Element edgeInfo) {
		Element label = null;
		if (edgeInfo != null) {
			label = getLabelElement(edgeInfo);
		}
		if (label != null) {
			Point offset = new Point();
			offset.x = Integer.valueOf(getAttribute(label, "x")).intValue();
			offset.y = Integer.valueOf(getAttribute(label, "y")).intValue();
			edge.getLabel().setOffset(offset);
		}
	}
	
	private void addConstraint(Node node, Element nodeInfo) {
		Rectangle constraint = node.getConstraint().getCopy();
		Dimension initialDimension = NotationMapping.getInitialDimension(node.getSemanticElement().getElementId());
		if (initialDimension != null) {
			constraint.setSize(initialDimension);
		}
		if (nodeInfo != null) {
			constraint.x = Integer.valueOf(getAttribute(nodeInfo, "x")).intValue();
			constraint.y = Integer.valueOf(getAttribute(nodeInfo, "y")).intValue();
			constraint.width = Integer.valueOf(getAttribute(nodeInfo, "width")).intValue();
			constraint.height = Integer.valueOf(getAttribute(nodeInfo, "height")).intValue();
		}
		node.setConstraint(constraint);
	}
	
	protected void postProcess(NodeContainer nodeContainer) {
		List nodes = nodeContainer.getNodes();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = (Node)nodes.get(i);
			List edges = node.getLeavingEdges();
			for (int j = 0; j < edges.size(); j++) {
				Edge edge = (Edge)edges.get(j);
				SemanticElement destination = findDestination(edge, node);
				Node target = (Node)edge.getFactory().getRegisteredNotationElementFor(destination);
				if (target != null && edge.getTarget() == null) {
					target.addArrivingEdge(edge);
				}
			}
			if (node instanceof NodeContainer) {
				postProcess((NodeContainer)node);
			}
		}
	}
	
	public boolean saveToInput(
			IEditorInput input,
			RootContainer rootContainer) {
		boolean result = true;
		try {
			IFile file = getNotationInfoFile(((IFileEditorInput)input).getFile());
			Element notationInfo = documentBuilderFactory.newDocumentBuilder().parse(file.getContents()).getDocumentElement();
			if (upToDateCheck(notationInfo)) {
				getNotationInfoFile(((IFileEditorInput)input).getFile()).setContents(
					new ByteArrayInputStream(toNotationInfoXml(rootContainer).getBytes()), true, true, null);
			} else {
				result = false;
			}
		} catch (Exception e) {
			Logger.logError("Problem while saving the input.", e);
		}
		return result;
	 }	
	
	private String toNotationInfoXml(RootContainer rootContainer) {
		StringWriter writer = new StringWriter();
		write(rootContainer, writer);
		return writer.toString();
	}

	private void write(
			RootContainer rootContainer, Writer writer) {
		try {
			Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
			Element root = document.createElement("root-container");
			document.appendChild(root);
			write(rootContainer, root);
			DOMSource domSource = new DOMSource(root);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult streamResult = new StreamResult(writer);
			transformer.transform(domSource, streamResult);
//			transformer.transform(domSource, new StreamResult(System.out));
		} catch (Exception e) {
			Logger.logError("Problem while saving to disk", e);
		}
	}
	
	private void write(
			RootContainer rootContainer,
			Element element) {
		addAttribute(element, "name", ((NamedElement)rootContainer.getSemanticElement()).getName());
		addAttribute(element, "width", Integer.toString(rootContainer.getDimension().width));
		addAttribute(element, "height", Integer.toString(rootContainer.getDimension().height));
		Iterator iter = rootContainer.getNodes().iterator();
		while (iter.hasNext()) {
			write("\n  ", (Node) iter.next(), element);
		}
	}
	
	private void addText(String indent, org.w3c.dom.Node element) {
		element.appendChild(element.getOwnerDocument().createTextNode(indent));
	}
	
	private void write(String indent, Node node, Element element) {
		boolean childAdded = false;
		addText(indent, element);
		Element newElement = null;
		if (node instanceof AbstractNodeContainer) {
			newElement = addElement(element, "node-container");
		} else {
			newElement = addElement(element, "node");
		}
		addAttribute(newElement, "name", ((NamedElement)node.getSemanticElement()).getName());
		addAttribute(newElement, "x", String.valueOf(node.getConstraint().x));
		addAttribute(newElement, "y", String.valueOf(node.getConstraint().y));
		addAttribute(newElement, "width", String.valueOf(node.getConstraint().width));
		addAttribute(newElement, "height", String.valueOf(node.getConstraint().height));
		if (node instanceof AbstractNodeContainer) {
			Iterator nodes = ((AbstractNodeContainer)node).getNodes().iterator();
			while (nodes.hasNext()) {
				childAdded = true;
				write(indent + "  ", (Node)nodes.next(), newElement); 
			}
		}
		Iterator edges = node.getLeavingEdges().iterator();
		while (edges.hasNext()) {
			childAdded = true;
			Edge edge = (Edge) edges.next();
			addText(indent + "  ", newElement);
			Element newEdge = addElement(newElement, "edge");
			write(indent + "  ", edge, newEdge);
		}
		if (childAdded) {
			addText(indent, newElement);
		}
	}

	private void write(String indent, Edge edge, Element element) {
		boolean childAdded = false;
		Point offset = edge.getLabel().getOffset();
		if (offset != null) {
			childAdded = true;
			addText(indent + "  ", element);
			Element label = addElement(element, "label");
			addAttribute(label, "x", String.valueOf(offset.x));
			addAttribute(label, "y", String.valueOf(offset.y));
		}
		Iterator bendpoints = edge.getBendPoints().iterator();
		while (bendpoints.hasNext()) {
			childAdded = true;
			addText(indent + "  ", element);
			Element bendPoint = addElement(element, "bendpoint");
			write(indent + "  ", (BendPoint) bendpoints.next(), bendPoint);
		}
		if (childAdded) {
			addText(indent, element);
		}
	}

	private void write(String indent, BendPoint bendpoint, Element bendpointElement) {
		addAttribute(bendpointElement, "w1", String.valueOf(bendpoint
				.getFirstRelativeDimension().width));
		addAttribute(bendpointElement, "h1", String.valueOf(bendpoint
				.getFirstRelativeDimension().height));
		addAttribute(bendpointElement, "w2", String.valueOf(bendpoint
				.getSecondRelativeDimension().width));
		addAttribute(bendpointElement, "h2", String.valueOf(bendpoint
				.getSecondRelativeDimension().height));
	}
	
	private Element addElement(Element element, String elementName) {
		Element newElement = element.getOwnerDocument().createElement(elementName);
		return (Element)element.appendChild(newElement);
	}

	private void addAttribute(Element e, String attributeName, String value) {
		if (value != null) {
			Attr attr = e.getOwnerDocument().createAttribute(attributeName);
			attr.setNodeValue(value);
			e.setAttributeNode(attr);
		}
	}

	private void createNotationInfoFile(IFile notationInfoFile) {
		try {
			notationInfoFile.create(new ByteArrayInputStream(createInitialNotationInfo().toString().getBytes()), true, null);
		} catch (CoreException e) {
			Logger.logError(e);
		}
	}
	
	private IFile getNotationInfoFile(IFile semanticInfoFile) {
		IProject project = semanticInfoFile.getProject();
		IPath semanticInfoPath = semanticInfoFile.getProjectRelativePath();
		IPath notationInfoPath = semanticInfoPath.removeLastSegments(1).append(getNotationInfoFileName(semanticInfoFile.getName()));
		IFile notationInfoFile = project.getFile(notationInfoPath);
		if (!notationInfoFile.exists()) {
			createNotationInfoFile(notationInfoFile);
		}
		return notationInfoFile;
	}
	
	public void addNotationInfo(RootContainer rootContainer, IEditorInput input) { 
		try {
			IFile file = getNotationInfoFile(((FileEditorInput)input).getFile());
			if (file.exists()) {
				Element notationInfo = documentBuilderFactory.newDocumentBuilder().parse(file.getContents()).getDocumentElement();
				Element changedInfo = convertCheck(notationInfo);
				processRootContainer(rootContainer, changedInfo == null ? notationInfo : changedInfo);
				if (changedInfo != null) {
					file.setContents(new ByteArrayInputStream(toNotationInfoXml(rootContainer).getBytes()), true, true, null);
				}
			} else {
				file.create(new ByteArrayInputStream(createInitialNotationInfo().toString().getBytes()), true, null);
			}
		} catch (Exception e) {
			Logger.logError("Problem adding notation info", e);
			throw new RuntimeException(e);
		}
	}

	private Element convertCheck(Element notationInfo) {
		if ("process-diagram".equals(notationInfo.getNodeName()) || "pageflow-diagram".equals(notationInfo.getNodeName())) {
			MessageDialog dialog = new MessageDialog(
					null, 
					"Convert To 3.1.x Format", 
					null, 
					"A file created with an older GPD version was detected. " +
					"If you open this file it will be converted to the 3.1.x " +
					"format and overwritten.\n" +
					"Do you want to continue?",
					MessageDialog.QUESTION,
					new String[] {"Convert And Open", "Continue Without Converting"},
					0);
			if (dialog.open() == 0) {
				return convertToRootContainer(notationInfo);
			}
		}
		return null;
	}
	
	private boolean upToDateCheck(Element notationInfo) {
		if ("process-diagram".equals(notationInfo.getNodeName()) || "pageflow-diagram".equals(notationInfo.getNodeName())) {
			MessageDialog dialog = new MessageDialog(
					null, 
					"GPD 3.0.x Format Detected", 
					null, 
					"The file you are trying to save contains GPD 3.0.x information." +
					"Saving the file will result in an automatic conversion into the 3.1.x format." +
					"It will be impossible to open it with the old GPD.\n" +
					"Do you want to continue?",
					MessageDialog.QUESTION,
					new String[] {"Save And Convert", "Cancel"},
					0);
			return dialog.open() == 0;
		}
		return true;
	}
	
	private Element convertToRootContainer(Element notationInfo) {
		Element newNotationInfo = notationInfo.getOwnerDocument().createElement("root-container");
		notationInfo.getParentNode().replaceChild(newNotationInfo, notationInfo);
		NamedNodeMap attributes = notationInfo.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			newNotationInfo.setAttributeNode((Attr)attributes.item(i).cloneNode(true));
		}
		NodeList nodeList = notationInfo.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			newNotationInfo.appendChild(nodeList.item(i).cloneNode(true));
		}		
		convertChildrenToEdge(newNotationInfo);
		return newNotationInfo;
	}

	private void convertChildrenToEdge(Element element) {
		NodeList list = element.getElementsByTagName("transition");
		while (list.getLength() > 0) {
			convertToEdge((Element)list.item(0));
		}
	}
	
	private void convertToEdge(Element element) {
		Element parent = (Element)element.getParentNode();
		Element newElement = element.getOwnerDocument().createElement("edge");
		parent.replaceChild(newElement, element);
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			newElement.setAttributeNode((Attr)attributes.item(i).cloneNode(true));
		}
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			newElement.appendChild(nodeList.item(i).cloneNode(true));
		}
		convertChildrenToEdge(newElement);
	}
	
	private String getAttribute(Element element, String name) {
		return element.hasAttribute(name) ? element.getAttribute(name) : null;
	}

}
