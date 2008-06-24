package org.jboss.bam.test.model;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.SQLGrammarException;
import org.jboss.bam.AbstractDashboardQuery;
import org.jboss.bam.HqlDashboardQuery;
import org.jboss.bam.SqlDashboardQuery;
import org.jboss.bam.test.model.TestCase;

public class DashboardQueryTest extends TestCase {
	
	public DashboardQueryTest(String x) {
		super(x);
	}
	
	public void testQueryBuilder() throws Exception {
		Session s;
		Transaction tx;
		s = openSession();
		tx = s.beginTransaction();
		HqlDashboardQuery hqlQuery = new HqlDashboardQuery();
		hqlQuery.setName("test-query");
		hqlQuery.setDescription("Demo query");
		hqlQuery.setQuery("select dashboardQuery from AbstractDashboardQuery dashboardQuery");
		s.persist(hqlQuery);
		
		Query query = s.createQuery(hqlQuery.getQuery());
		assertEquals(1, query.list().size());
		AbstractDashboardQuery retrievedQuery = (AbstractDashboardQuery) query
				.list().get(0);
		assertTrue(retrievedQuery instanceof HqlDashboardQuery);
		assertEquals("test-query", retrievedQuery.getName());
		
		SqlDashboardQuery sqlQuery = new SqlDashboardQuery();
		sqlQuery.setName("tester");
		sqlQuery.setDescription("Hello");
		sqlQuery.setQuery("select * from JBPM_LOG");
		s.persist(sqlQuery);
		
		query = s.createQuery(hqlQuery.getQuery());
		
		System.out.println(query.list().size());
		try {
			tx.commit();
		} catch (SQLGrammarException e) {
			System.err.println(e.getSQLException().getNextException());
			throw e;
		}
		s.close();
	}
	
	public void testQlValidation() throws Exception {
		Session s;
		Transaction tx;
		s = openSession();
		tx = s.beginTransaction();
		
		Query hqlQuery = null;
		try {
			hqlQuery = s.createQuery("select the moon from the skies");
		} catch (Exception ex) {
			hqlQuery = null;
		}
		assertNull(hqlQuery);
		
		// Does not throw an exception
		Query sqlQuery = null;
		try {
			sqlQuery = s.createSQLQuery("select the moon from the skies");
		} catch (Exception ex) {
			sqlQuery = null;
		}
		assertNotNull(sqlQuery);
		
		try {
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		s.close();
	}
	
	
	/**
	 * Return the defined mappings of the objects.
	 * 
	 * @see org.jboss.jbam.test.model.TestCase#getMappings()
	 */
	protected Class<?>[] getMappings() {
		return new Class[] { AbstractDashboardQuery.class, HqlDashboardQuery.class,
				SqlDashboardQuery.class };
	}
	
}
