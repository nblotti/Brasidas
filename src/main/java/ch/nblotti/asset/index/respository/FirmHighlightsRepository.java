package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.FirmEODHighlightsTO;
import ch.nblotti.asset.index.to.FirmEODInfoTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmhighlights")
public interface FirmHighlightsRepository extends PagingAndSortingRepository<FirmEODHighlightsTO, Long> {

  public FirmEODHighlightsTO findTopByCodeOrderByDate(String code);
}
