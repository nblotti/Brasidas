package ch.nblotti.asset.firm.repository;

import ch.nblotti.asset.common.ReadOnlyRepository;
import ch.nblotti.asset.firm.to.FirmTopMoversTO;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "firmtopmovers")
public interface FirmTopMoversRepository extends ReadOnlyRepository<FirmTopMoversTO, String> {

  Iterable<FirmTopMoversTO> findFirst10ByOrderByVolumeDesc();

  Iterable<FirmTopMoversTO> findFirst10ByOrderByPercentChangeDesc();

  Iterable<FirmTopMoversTO> findFirst10ByOrderByPercentChangeAsc();
}
