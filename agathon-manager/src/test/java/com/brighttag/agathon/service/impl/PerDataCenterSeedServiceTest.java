package com.brighttag.agathon.service.impl;

import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.brighttag.agathon.model.CassandraInstance;
import com.brighttag.agathon.model.CassandraRing;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @author codyaray
 * @since 5/25/12
 */
public class PerDataCenterSeedServiceTest extends EasyMockSupport {

  private static final String DATACENTER1 = "dc1";
  private static final String DATACENTER2 = "dc2";

  private static final String IP_ADDRESS_1 = "1.1.1.1";
  private static final String IP_ADDRESS_2 = "2.2.2.2";
  private static final String IP_ADDRESS_3 = "3.3.3.3";
  private static final String IP_ADDRESS_4 = "4.4.4.4";

  private CassandraRing ring;
  private PerDataCenterSeedService seedProvider;

  @Before
  public void setUp() {
    ring = createMock(CassandraRing.class);
    seedProvider = new PerDataCenterSeedService(2);
  }

  @After
  public void tearDown() {
    verifyAll();
  }

  @Test
  public void getSeeds() {
    CassandraInstance instance1 = buildInstance(DATACENTER1, IP_ADDRESS_1);
    CassandraInstance instance2 = buildInstance(DATACENTER1, IP_ADDRESS_2);
    CassandraInstance instance3 = buildInstance(DATACENTER2, IP_ADDRESS_3);
    CassandraInstance instance4 = buildInstance(DATACENTER2, IP_ADDRESS_4);
    ImmutableSet<CassandraInstance> instances = ImmutableSet.of(instance1, instance2, instance3, instance4);
    expect(ring.getInstances()).andReturn(instances);
    replayAll();

    assertEquals(ImmutableSet.of(IP_ADDRESS_1, IP_ADDRESS_2, IP_ADDRESS_3, IP_ADDRESS_4),
        seedProvider.getSeeds(ring));
  }

  @Test
  public void getSeeds_insufficientInstancesInDataCenter() {
    CassandraInstance instance1 = buildInstance(DATACENTER1, IP_ADDRESS_1);
    CassandraInstance instance2 = buildInstance(DATACENTER2, IP_ADDRESS_2);
    CassandraInstance instance3 = buildInstance(DATACENTER2, IP_ADDRESS_3);
    ImmutableSet<CassandraInstance> instances = ImmutableSet.of(instance1, instance2, instance3);
    expect(ring.getInstances()).andReturn(instances);
    replayAll();

    assertEquals(ImmutableSet.of(IP_ADDRESS_1, IP_ADDRESS_2, IP_ADDRESS_3), seedProvider.getSeeds(ring));
  }

  private CassandraInstance buildInstance(String dataCenter, String publicIpAddress) {
    return new CassandraInstance.Builder()
        .id(1)
        .dataCenter(dataCenter)
        .publicIpAddress(publicIpAddress)
        .build();
  }

}
