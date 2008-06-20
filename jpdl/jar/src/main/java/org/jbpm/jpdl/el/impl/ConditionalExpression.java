/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package org.jbpm.jpdl.el.impl;

import org.jbpm.jpdl.el.ELException;
import org.jbpm.jpdl.el.FunctionMapper;
import org.jbpm.jpdl.el.VariableResolver;

/**
 *
 * <p>Represents a conditional expression.  I've decided not to produce an
 * abstract base "TernaryOperatorExpression" class since (a) future ternary
 * operators are unlikely and (b) it's not clear that there would be a
 * meaningful way to abstract them.  (For instance, would they all be right-
 * associative?  Would they all have two fixed operator symbols?)
 * 
 * @author Shawn Bayern
 **/

public class ConditionalExpression
  extends Expression
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property condition

  Expression mCondition;
  public Expression getCondition ()
  { return mCondition; }
  public void setCondition (Expression pCondition)
  { mCondition = pCondition; }

  //-------------------------------------
  // property trueBranch

  Expression mTrueBranch;
  public Expression getTrueBranch ()
  { return mTrueBranch; }
  public void setTrueBranch (Expression pTrueBranch)
  { mTrueBranch = pTrueBranch; }

  //-------------------------------------
  // property falseBranch

  Expression mFalseBranch;
  public Expression getFalseBranch ()
  { return mFalseBranch; }
  public void setFalseBranch (Expression pFalseBranch)
  { mFalseBranch = pFalseBranch; }

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public ConditionalExpression (Expression pCondition,
				Expression pTrueBranch,
				Expression pFalseBranch)
  {
    mCondition = pCondition;
    mTrueBranch = pTrueBranch;
    mFalseBranch = pFalseBranch;
  }

  //-------------------------------------
  // Expression methods
  //-------------------------------------
  /**
   *
   * Returns the expression in the expression language syntax
   **/
  public String getExpressionString ()
  {
    return
      "( " + mCondition.getExpressionString() + " ? " + 
      mTrueBranch.getExpressionString() + " : " +
      mFalseBranch.getExpressionString() + " )";
  }

  //-------------------------------------
  /**
   *
   * Evaluates the conditional expression and returns the literal result
   **/
  public Object evaluate (VariableResolver vr,
			  FunctionMapper f,
			  Logger l)
    throws ELException
  {
    // first, evaluate the condition (and coerce the result to a boolean value)
    boolean condition =
      Coercions.coerceToBoolean(
        mCondition.evaluate(vr, f, l), l).booleanValue();

    // then, use this boolean to branch appropriately
    if (condition)
      return mTrueBranch.evaluate(vr, f, l);
    else
      return mFalseBranch.evaluate(vr, f, l);
  }

  //-------------------------------------
}
