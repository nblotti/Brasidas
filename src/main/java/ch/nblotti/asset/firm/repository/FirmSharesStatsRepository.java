package ch.nblotti.asset.firm.repository;

import ch.nblotti.asset.firm.to.FirmEODShareStatsTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmsharestats")
public interface FirmSharesStatsRepository extends PagingAndSortingRepository<FirmEODShareStatsTO, Integer> {

  public FirmEODShareStatsTO findTopByCodeOrderByDate(String code);

}
