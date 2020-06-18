package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.FirmEODShareStatsTO;
import ch.nblotti.asset.index.to.FirmEODValuationTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmsharestats")
public interface FirmSharesStatsRepository extends PagingAndSortingRepository<FirmEODShareStatsTO, Integer> {

  public FirmEODShareStatsTO findTopByCodeOrderByDate(String code);

}
