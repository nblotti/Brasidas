package ch.nblotti.brasidas.index.respository;

import ch.nblotti.brasidas.common.ReadOnlyRepository;
import ch.nblotti.brasidas.index.to.IndexMoversTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "indices")
public interface IndexMoversRepository extends ReadOnlyRepository<IndexMoversTO, String> {

  @Cacheable("indicesMovers")
  Iterable<IndexMoversTO> findAllByNbrDays(int nbrDays);
}
