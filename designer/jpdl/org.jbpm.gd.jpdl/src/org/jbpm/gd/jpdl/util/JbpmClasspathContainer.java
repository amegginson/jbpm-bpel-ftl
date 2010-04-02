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
package org.jbpm.gd.jpdl.util;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.jbpm.gd.jpdl.Constants;
import org.jbpm.gd.jpdl.Logger;
import org.jbpm.gd.jpdl.prefs.JbpmInstallation;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class JbpmClasspathContainer implements IClasspathContainer, Constants {

	private static XPathFactory xPathFactory = XPathFactory.newInstance();
	
	IClasspathEntry[] jbpmJniEntries;
	JbpmInstallation jbpmInstallation;

	IJavaProject javaProject = null;

	public JbpmClasspathContainer(IJavaProject javaProject, JbpmInstallation jbpmInstallation) {
		this.javaProject = javaProject;
		this.jbpmInstallation = jbpmInstallation;

	}

	public IClasspathEntry[] getClasspathEntries() {
		if (jbpmJniEntries == null) {
			jbpmJniEntries = createJbpmJniEntries(javaProject);
		}
		return jbpmJniEntries;
	}

	public String getDescription() {
		return "jBPM Jni [" + jbpmInstallation.name + "]";
	}
		
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return new Path("JBPM/" + jbpmInstallation.name);
	}

	private IClasspathEntry[] createJbpmJniEntries(IJavaProject project) {
		Map jarNames = getJarNames();
		ArrayList entries = new ArrayList();
		Iterator iterator = jarNames.keySet().iterator();
		while (iterator.hasNext()) {
			IPath jarPath = (IPath)iterator.next();
			IPath srcPath = (IPath)jarNames.get(jarPath);
			IPath srcRoot = null;
			entries.add(JavaCore.newJniEntry(
					jarPath, 
					srcPath, 
					srcRoot));
		}
		return (IClasspathEntry[]) entries.toArray(new IClasspathEntry[entries
				.size()]);
	}

	private Map getJarNames() {
		HashMap result = new HashMap();
		try {
			String location = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(jbpmInstallation.location);
			IPath locationPath = new Path(location);
			File locationFile = locationPath.append("src/resources/gpd/version.info.xml").toFile();
			XPath xPath = xPathFactory.newXPath();
			XPathExpression xPathExpression = xPath.compile("/jbpm-version-info/classpathentry");
			InputSource inputSource = new InputSource(new FileReader(locationFile));
			NodeList nodeList = (NodeList)xPathExpression.evaluate(inputSource, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element entry = (Element)nodeList.item(i);
				IPath sourcePath = null;
				if (entry.hasAttribute("src")) {
					sourcePath = locationPath.append((String)entry.getAttribute("src"));
				}
				result.put(
						locationPath.append((String)entry.getAttribute("path")),
						sourcePath);
			}
		} 
		catch (Exception e) {
			Logger.logError("Problem while getting jar names", e);
		}
		return result;
	}
	
}
