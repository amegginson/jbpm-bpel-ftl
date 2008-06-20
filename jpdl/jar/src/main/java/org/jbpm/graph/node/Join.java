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
package org.jbpm.graph.node;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.graph.action.Script;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

public class Join extends Node implements Parsable {

  private static final long serialVersionUID = 1L;
  
  /** specifies wether what type of hibernate lock should be acquired.  null value defaults to LockMode.Force */ 
  String parentLockMode;

  /**
   * specifies if this joinhandler is a discriminator.
   * a descriminator reactivates the parent when the first 
   * concurrent token enters the join. 
   */
  boolean isDiscriminator = false;

  /**
   * a fixed set of concurrent tokens.
   */
  Collection tokenNames = null;

  /**
   * a script that calculates concurrent tokens at runtime.
   */
  Script script = null;
  
  /**
   * reactivate the parent if the n-th token arrives in the join.
   */
  int nOutOfM = -1;
  

  public Join() {
  }

  public Join(String name) {
    super(name);
  }

  public void read(Element element, JpdlXmlReader jpdlReader) {
    String lock = element.attributeValue("lock");
    if ( (lock!=null) 
         && (lock.equalsIgnoreCase("pessimistic"))
       ) {
      parentLockMode = LockMode.UPGRADE.toString();
    }
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    
    boolean isAbleToReactivateParent = token.isAbleToReactivateParent();
    
    if (!token.hasEnded()) {
      token.end(false);
    }
    
    // if this token is not able to reactivate the parent, 
    // we don't need to check anything
    if ( isAbleToReactivateParent ) {

      // the token arrived in the join and can only reactivate 
      // the parent once
      token.setAbleToReactivateParent(false);

      Token parentToken = token.getParent();
      
      if ( parentToken != null ) {
        
        JbpmContext jbpmContext = executionContext.getJbpmContext();
        Session session = (jbpmContext!=null ? jbpmContext.getSession() : null);
        if (session!=null) {
          LockMode lockMode = LockMode.FORCE;
          if (parentLockMode!=null) {
            lockMode = LockMode.parse(parentLockMode);
          }
          log.debug("forcing version increment on parent token "+parentToken);
          session.flush();
          session.lock(parentToken, lockMode);
        }

        boolean reactivateParent = true;

        // if this is a discriminator
        if ( isDiscriminator ) {
          // reactivate the parent when the first token arrives in the 
          // join.  this must be the first token arriving because otherwise
          // the isAbleToReactivateParent() of this token should have been false
          // above.
          reactivateParent = true;

        // if a fixed set of tokenNames is specified at design time...
        } else if ( tokenNames != null ) {
          // check reactivation on the basis of those tokenNames
          reactivateParent = mustParentBeReactivated(parentToken, tokenNames.iterator() );

        // if a script is specified
        } else if ( script != null ) {

          // check if the script returns a collection or a boolean
          Object result = null;
          try {
            result = script.eval( token );
          } catch (Exception e) {
            this.raiseException(e, executionContext);
          }
          // if the result is a collection 
          if ( result instanceof Collection ) {
            // it must be a collection of tokenNames 
            Collection runtimeTokenNames = (Collection) result;
            reactivateParent = mustParentBeReactivated(parentToken, runtimeTokenNames.iterator() );


          // if it's a boolean... 
          } else if ( result instanceof Boolean ) {
            // the boolean specifies if the parent needs to be reactivated
            reactivateParent = ((Boolean)result).booleanValue();
          }

        // if a nOutOfM is specified
        } else if ( nOutOfM != -1 ) {

          int n = 0;
          // wheck how many tokens already arrived in the join
          Iterator iter = parentToken.getChildren().values().iterator();
          while ( iter.hasNext() ) {
            Token concurrentToken = (Token)iter.next();
            if (this.equals(concurrentToken.getNode())) {
              n++;
            }
          }
          if ( n < nOutOfM ) {
            reactivateParent = false;
          }
          
        // if no configuration is specified..
        } else {
          // the default behaviour is to check all concurrent tokens and reactivate
          // the parent if the last token arrives in the join
          reactivateParent = mustParentBeReactivated(parentToken, parentToken.getChildren().keySet().iterator() );
        }

        // if the parent token needs to be reactivated from this join node
        if (reactivateParent) {

          // write to all child tokens that the parent is already reactivated
          Iterator iter = parentToken.getChildren().values().iterator();
          while ( iter.hasNext() ) {
            ((Token)iter.next()).setAbleToReactivateParent( false );
          }

          // write to all child tokens that the parent is already reactivated
          ExecutionContext parentContext = new ExecutionContext(parentToken);
          leave(parentContext);
        }
      }
    }
  }

  public boolean mustParentBeReactivated(Token parentToken, Iterator childTokenNameIterator) {
    boolean reactivateParent = true;
    while ( (childTokenNameIterator.hasNext())
            && (reactivateParent) ){
      String concurrentTokenName = (String) childTokenNameIterator.next();
      
      Token concurrentToken = parentToken.getChild( concurrentTokenName );
      
      if (concurrentToken.isAbleToReactivateParent()) {
        log.debug("join will not yet reactivate parent: found concurrent token '"+concurrentToken+"'");
        reactivateParent = false;
      }
    }
    return reactivateParent;
  }

  public Script getScript() {
    return script;
  }
  public void setScript(Script script) {
    this.script = script;
  }
  public Collection getTokenNames() {
    return tokenNames;
  }
  public void setTokenNames(Collection tokenNames) {
    this.tokenNames = tokenNames;
  }
  public boolean isDiscriminator() {
    return isDiscriminator;
  }
  public void setDiscriminator(boolean isDiscriminator) {
    this.isDiscriminator = isDiscriminator;
  }
  public int getNOutOfM() {
    return nOutOfM;
  }
  public void setNOutOfM(int nOutOfM) {
    this.nOutOfM = nOutOfM;
  }

  private static final Log log = LogFactory.getLog(Join.class);
}
