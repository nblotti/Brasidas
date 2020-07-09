package ch.nblotti.brasidas.firm.repository;

import ch.nblotti.brasidas.firm.to.FirmEODValuationTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmvaluation")
public interface FirmValuationRepository extends PagingAndSortingRepository<FirmEODValuationTO, Integer> {


  public FirmEODValuationTO findTopByCodeOrderByDate(String code);



}
