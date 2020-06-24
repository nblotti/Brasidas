package ch.nblotti.asset.firm.repository;

import ch.nblotti.asset.firm.to.FirmEODHighlightsTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmhighlights")
public interface FirmHighlightsRepository extends PagingAndSortingRepository<FirmEODHighlightsTO, Long> {

  public FirmEODHighlightsTO findTopByCodeOrderByDate(String code);
}
