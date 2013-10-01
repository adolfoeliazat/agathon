package com.brighttag.agathon.dao.zerg;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import com.brighttag.agathon.dao.BackingStoreException;
import com.brighttag.agathon.dao.CassandraRingDao;
import com.brighttag.agathon.model.CassandraRing;

/**
 *
 * @author codyaray
 * @since 9/17/2013
 */
public class ZergCassandraRingDao implements CassandraRingDao {

  private final ZergConnector zergConnector;

  @Inject
  public ZergCassandraRingDao(ZergConnector zergConnector) {
    this.zergConnector = zergConnector;
  }

  @Override
  public ImmutableSet<CassandraRing> findAll() throws BackingStoreException {
    ImmutableSet.Builder<CassandraRing> ringBuilder = ImmutableSet.builder();
    ZergHosts hosts = ZergHosts.from(zergConnector.getHosts());
    for (String ring : hosts.rings()) {
      ringBuilder.add(getByName(ring, hosts));
    }
    return ringBuilder.build();
  }

  @Override
  public @Nullable CassandraRing findByName(String name) throws BackingStoreException {
    ZergHosts hosts = ZergHosts.from(zergConnector.getHosts());
    if (!hosts.rings().contains(name)) {
      return null;
    }
    return getByName(name, hosts);
  }

  @Override
  public void save(CassandraRing ring) {
    throw new UnsupportedOperationException("Save is not supported for " + getClass().getSimpleName());
  }

  @Override
  public void delete(CassandraRing ring) {
    throw new UnsupportedOperationException("Delete is not supported for " + getClass().getSimpleName());
  }

  private static CassandraRing getByName(String ring, ZergHosts hosts) {
    return new CassandraRing.Builder()
        .name(ring)
        .instances(hosts.filter(ring).toCassandraInstances())
        .build();
  }

}
