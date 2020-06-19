package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.index.to.IndexMoversTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "indices")
public interface IndexMoversRepository extends ReadOnlyRepository<IndexMoversTO, String> {

  @Cacheable("indicesMovers")
  Iterable<IndexMoversTO> findAllByNbrDays(int nbrDays);
}
