package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.FirmEODInfoTO;
import ch.nblotti.asset.index.to.FirmEODShareStatsTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "firminfo")
public interface FirmInfoRepository extends PagingAndSortingRepository<FirmEODInfoTO, Integer> {


  public FirmEODInfoTO findTopByCodeOrderByDate(String code);
}
