package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.index.to.SPSectorResultTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collection;

@RepositoryRestResource(path = "spsector")
public interface SpSectorResultRepository extends ReadOnlyRepository<SPSectorResultTO, Integer> {

  @Cacheable("findByDateAfter")
  public Collection<SPSectorResultTO> findByDateAfter(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date);


}
