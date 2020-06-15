package ch.nblotti.asset.firm.repository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.firm.to.FirmTopMoversTO;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "firmtopmovers")
public interface FirmTopMoversRepository extends ReadOnlyRepository<FirmTopMoversTO, String> {

  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByVolumeDesc(String exchange);

  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByPercentChangeDesc(String exchange);

  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByPercentChangeAsc(String exchange);
}
