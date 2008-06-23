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
package org.jbpm.jpdl.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.jpdl.par.ProcessArchive;

public class ConversionTestCase extends TestCase {
	
	private static final String TESTPAR_1 = "payraiseprocess-2.0.par";
	private static final String TESTPAR_2 = "exampleprocess-2.0.par";

	public void testPDConversion(){
		
		Converter converter = new Converter();
		try
		{
		  SAXReader reader = new SAXReader();
		
		  Document doc = reader.read( 
				  this.getClass().getResourceAsStream("jpdl-sample-2.0.xml") );
		  Document doc30 = converter.convert( doc );		  
		  ByteArrayOutputStream bos = new ByteArrayOutputStream();
		  
		  converter.serializetoXML( bos, doc30) ;
		  
		  System.out.println(bos.toString());
		  
		  ProcessDefinition.parseXmlString( bos.toString() );
		}
		catch(Exception ex)
		{
			fail("2.0 PDL did not convert successfully to 3.0 PDL" +  ex.toString());
		}
	}
	
	public void testParConversion01(){
		
		Converter converter = new Converter();
		
		try
		{
          // Create memory version of the process archive
  		  ProcessArchive pa = new ProcessArchive(
		    new ZipInputStream(
		      this.getClass().getResourceAsStream( TESTPAR_1 ) ) );
		  String result = converter.convertPar( pa );
		
		  if (result == null)
		    fail("2.0 PAR did not convert successfully.");
		  else
		  {
		    System.out.println(result);
			try
			{
			  ProcessDefinition.parseXmlString( result );	
			}
			catch(Exception ex)
			{
				fail("Converted PDL is invalid " + ex.toString());
			}
		  }
	  	}
		catch(IOException ioe)
		{
			fail("Unexpected IO error " + ioe.toString());
		}
	}
		
	public void testParConversion02(){
		
		Converter converter = new Converter();
		
		try
		{
          // Create memory version of the process archive
  		  ProcessArchive pa = new ProcessArchive(
		    new ZipInputStream(
			  this.getClass().getResourceAsStream( TESTPAR_2 ) ) );
		  String result = converter.convertPar( pa );
		
		  if (result == null)
		    fail("2.0 PAR did not convert successfully.");
		  else
		  {
		    System.out.println(result);
			try
			{
			  ProcessDefinition.parseXmlString( result );	
			}
			catch(Exception ex)
			{
				fail("Converted PDL is invalid " + ex.toString());
			}
		  }
	  	}
		catch(IOException ioe)
		{
			fail("Unexpected IO error " + ioe.toString());
		}
			
	}
	
	public void testDirectoryConversion(){
		
		try
		{
			Converter.main( 
					new String[] {"src/java.jbpm.test/org/jbpm/jpdl/convert", 
							      "src/java.jbpm.test/org/jbpm/jpdl/convert/output"} );
		}
		catch(Exception ex)
		{
			fail("Directory conversion failed:  " + ex.toString());
		}
	}
}
