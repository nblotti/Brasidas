package ch.nblotti.brasidas.firm.repository;

import ch.nblotti.brasidas.firm.to.FirmEODInfoTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "firminfo")
public interface FirmInfoRepository extends PagingAndSortingRepository<FirmEODInfoTO, Integer> {


  public FirmEODInfoTO findTopByCodeOrderByDate(String code);
}
