package org.jbpm.ejb;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface LocalTimerEntityHome extends EJBLocalHome {

	public LocalTimerEntity create() throws CreateException;

	public LocalTimerEntity findByPrimaryKey(Long key) throws FinderException;

	public Collection findByTokenId(Long key) throws FinderException;

	public Collection findByTokenIdAndName(Long key, String name)
			throws FinderException;

	public Collection findByProcessInstanceId(Long key) throws FinderException;
}
