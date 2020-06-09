package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.firm.to.FirmTopMoversTO;
import ch.nblotti.asset.index.to.IndexTopMoversTO;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "indextopmovers")
public interface IndexTopMoversRepository extends ReadOnlyRepository<IndexTopMoversTO, String> {

  Iterable<IndexTopMoversTO> findFirst10ByOrderByVolumeDesc();

  Iterable<IndexTopMoversTO> findFirst10ByOrderByPercentChangeDesc();

  Iterable<IndexTopMoversTO> findFirst10ByOrderByPercentChangeAsc();
}
