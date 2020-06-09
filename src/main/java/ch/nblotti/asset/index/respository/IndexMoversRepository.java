package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.index.to.IndexMoversTO;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "indexmovers")
public interface IndexMoversRepository extends ReadOnlyRepository<IndexMoversTO, String> {

  Iterable<IndexMoversTO> findFirst10ByOrderByVolumeDesc();

  Iterable<IndexMoversTO> findFirst10ByOrderByPercentChangeDesc();

  Iterable<IndexMoversTO> findFirst10ByOrderByPercentChangeAsc();
}
