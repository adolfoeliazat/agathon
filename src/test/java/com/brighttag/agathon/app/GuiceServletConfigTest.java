package com.brighttag.agathon.app;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.brighttag.agathon.dao.CassandraInstanceDAO;
import com.brighttag.agathon.dao.DAOModule;
import com.brighttag.agathon.model.CassandraInstance;
import com.brighttag.agathon.resources.CassandraInstanceResource;
import com.brighttag.agathon.resources.SeedResource;
import com.brighttag.agathon.resources.ValidatingJacksonJsonProvider;
import com.brighttag.agathon.service.impl.ServiceModule;

import static org.junit.Assert.assertNotNull;

/**
 * @author codyaray
 * @since 5/12/12
 */
public class GuiceServletConfigTest {

  private static final String CASSANDRA_ID = "id";

  private Injector injector;

  @BeforeClass
  public static void setRequiredSystemProperties() {
    System.setProperty(ServiceModule.CASSANDRA_ID_PROPERTY, CASSANDRA_ID);
    System.setProperty(DAOModule.DATABASE_PROPERTY, "fake"); // Use in-memory DAO for unit tests
  }

  @AfterClass
  public static void clearRequiredSystemProperties() {
    System.clearProperty(ServiceModule.CASSANDRA_ID_PROPERTY);
    System.clearProperty(DAOModule.DATABASE_PROPERTY);
  }

  @Before
  public void setUp() {
    injector = new GuiceServletConfig().getInjector();
    saveCassandraCoprocess(injector.getInstance(CassandraInstanceDAO.class));
  }

  @After
  public void tearDown() {
    // Sadly, have to do this
    injector.getInstance(GuiceFilter.class).destroy();
  }

  @Test
  public void bindings() {
    assertNotNull(injector.getInstance(ValidatingJacksonJsonProvider.class));
    assertNotNull(injector.getInstance(CassandraInstanceResource.class));
    assertNotNull(injector.getInstance(SeedResource.class));
  }

  private static void saveCassandraCoprocess(CassandraInstanceDAO dao) {
    dao.save(new CassandraInstance.Builder().id(CASSANDRA_ID).build());
  }

}