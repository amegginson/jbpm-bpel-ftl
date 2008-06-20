package org.jbpm.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class InboxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  SimpleSmtpServer server = null;

  public void init() throws ServletException {
    server = SimpleSmtpServer.start();
  }

  public void destroy() {
    server.stop();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    out.println("<html>");

    String headers = request.getParameter("headers");
    if (headers!=null) {
      out.println("<h1>Headers for mail "+headers+"</h1>");
      Iterator iter = server.getReceivedEmail();
      int headersId = Integer.parseInt(headers);
      for (int i=1; i<headersId; i++) {
        iter.next();
      }
      SmtpMessage email = (SmtpMessage) iter.next();
      
      out.println("<table border=\"1\">");
      out.println("<tr>");
      out.println(" <th>Header</th>");
      out.println(" <th>Value</th>");
      out.println("</tr>");
      
      Iterator nameIter = email.getHeaderNames();
      while(nameIter.hasNext()) {
        String headerName = (String) nameIter.next();
        out.println("<tr>");
        out.println(" <td><b>"+headerName+"</b></td>");
        out.println(" <td>"+email.getHeaderValue(headerName)+"</td>");
        out.println("</tr>");
      }

      out.println("<p><a href=\"http://localhost:8080/jbpm-mail/inbox\">Back to inbox</a></p>");

    } else {
      out.println("<script language=\"javascript\"><!--");
      out.println("setTimeout('Refresh()',2000);");
      out.println("function Refresh() {");
      out.println("  window.location='http://localhost:8080/jbpm-mail/inbox';");
      out.println("}");
      out.println("--></script>");

      out.println("<h1>Inbox</h1>");
      out.println("<p><a href=\"http://localhost:8080/jbpm-mail/inbox\">Refresh</a></p>");
      String operation = request.getParameter("operation");
      if ("clean".equals(operation)) {
        server.stop();
        server = SimpleSmtpServer.start();
        out.println("<p><i>Inbox cleaned</i></p>");
      } else {
        out.println("<p><a href=\"http://localhost:8080/jbpm-mail/inbox?operation=clean\">Clean inbox</a></p>");
      }

      out.println("<table border=\"1\">");
      out.println("<tr>");
      out.println(" <th>#</th>");
      out.println(" <th>From</th>");
      out.println(" <th>To</th>");
      out.println(" <th>Subject</th>");
      out.println(" <th>Message</th>");
      out.println(" <th>Sent</th>");
      out.println(" <th>Headers</th>");
      out.println("</tr>");
      int counter = 1;
      Iterator iter = server.getReceivedEmail();
      while (iter.hasNext()) {
        SmtpMessage email = (SmtpMessage) iter.next();
        out.println("<tr>");
        out.println(" <td>"+counter+"</td>");
        out.println(" <td>"+email.getHeaderValue("From")+"</td>");
        out.println(" <td>"+email.getHeaderValue("To")+"</td>");
        out.println(" <td>"+email.getHeaderValue("Subject")+"</td>");
        out.println(" <td>"+email.getBody()+"</td>");
        out.println(" <td>"+email.getHeaderValue("Date")+"</td>");
        out.println(" <td><a href=\"http://localhost:8080/jbpm-mail/inbox?headers="+counter+"\">Show</a></td>");
        out.println("</tr>");
        counter++;
      }
      out.println("</table>");
      out.println("</html>");
    }
  }
}
