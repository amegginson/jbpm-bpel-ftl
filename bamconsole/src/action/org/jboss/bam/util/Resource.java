package org.jboss.bam.util;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class that renders an array of bytes into a downloadable resource
 * 
 */
public abstract class Resource {
	public void render() throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext
			    .getExternalContext().getResponse();
			response.setContentType(getContentType().toString());
			response.setContentLength(getContentSize());
			if (Disposition.ATTACHMENT.equals(getDisposition())) {
				response.setHeader("Content-disposition", "attachment; filename="
				    + getFileName());
			}
			ServletOutputStream out = response.getOutputStream();
			out.write(getContent());
			out.flush();
			facesContext.responseComplete();
		}
	}

	public abstract Object getContentType();

	public abstract byte[] getContent() throws IOException;

	public int getContentSize() throws IOException {
		return getContent().length;
	}

	public String getFileName() {
		return "";
	}

	public Disposition getDisposition() {
		return Disposition.INLINE;
	}

	public enum Disposition {
		INLINE, ATTACHMENT;
	}
}
