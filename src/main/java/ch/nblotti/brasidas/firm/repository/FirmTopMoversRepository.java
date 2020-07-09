package ch.nblotti.brasidas.firm.repository;

import ch.nblotti.brasidas.common.ReadOnlyRepository;
import ch.nblotti.brasidas.firm.to.FirmTopMoversTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "firmtopmovers")
public interface FirmTopMoversRepository extends ReadOnlyRepository<FirmTopMoversTO, String> {

  @Cacheable("firmFirst10ByVolume")
  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByVolumeDesc(String exchange);

  @Cacheable("firmFirst10ByPercentDesc")
  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByPercentChangeDesc(String exchange);
  @Cacheable("firmFirst10ByPercentAsc")
  Iterable<FirmTopMoversTO> findFirst10ByCurrentExchangeOrderByPercentChangeAsc(String exchange);

  FirmTopMoversTO findByCode(String code);
}
