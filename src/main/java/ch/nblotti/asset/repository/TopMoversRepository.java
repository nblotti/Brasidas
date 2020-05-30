package ch.nblotti.asset.repository;

import ch.nblotti.asset.firm.to.TopMoversTO;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "topmovers")
public interface TopMoversRepository extends ReadOnlyRepository<TopMoversTO, String> {

  Iterable<TopMoversTO> findFirst10ByOrderByVolumeDesc();

  Iterable<TopMoversTO> findFirst10ByOrderByPercentChangeDesc();

  Iterable<TopMoversTO> findFirst10ByOrderByPercentChangeAsc();
}
