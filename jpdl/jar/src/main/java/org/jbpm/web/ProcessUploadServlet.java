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
package org.jbpm.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();

    public static final String UPLOAD_TYPE_DEFINITION = "definition";
    public static final String UPLOAD_TYPE_ARCHIVE = "archive";

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //upload from gpd has url mapping: /upload

        JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
        try {
            response.setContentType("text/html");
            response.getWriter().println(handleRequest(request));
        } finally {
            jbpmContext.close();
        }
    }

    private String handleRequest(HttpServletRequest request) {
        //check if request is multipart content
        log.debug("Handling upload request");
        if (!FileUpload.isMultipartContent(request)) {
            log.debug("Not a multipart request");
            return "Not a multipart request";
        }

        try {
            DiskFileUpload fileUpload = new DiskFileUpload();
            List list = fileUpload.parseRequest(request);
            log.debug("Upload from GPD");
            Iterator iterator = list.iterator();
            if (!iterator.hasNext()) {
                log.debug("No process file in the request");
                return "No process file in the request";
            }
            FileItem fileItem = (FileItem) iterator.next();
            if (fileItem.getContentType().indexOf("application/x-zip-compressed") == -1) {
                log.debug("Not a process archive");
                return "Not a process archive";
            }
            try {
                log.debug("Deploying process archive " + fileItem.getName());
                ZipInputStream zipInputStream = new ZipInputStream(fileItem.getInputStream());
                JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
                log.debug("Preparing to parse process archive");
                ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zipInputStream);
                log.debug("Created a processdefinition : " + processDefinition.getName());
                jbpmContext.deployProcessDefinition(processDefinition);
                zipInputStream.close();
                return "Deployed archive " + processDefinition.getName() + " successfully";
            } catch (IOException e) {
                log.debug("Failed to read process archive", e);
                return "IOException";
            }
        } catch (FileUploadException e) {
            log.debug("Failed to parse HTTP request", e);
            return "FileUploadException";
        }
    }

    private static final Log log = LogFactory.getLog(ProcessUploadServlet.class);
}