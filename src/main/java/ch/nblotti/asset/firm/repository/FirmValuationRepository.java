package ch.nblotti.asset.firm.repository;

import ch.nblotti.asset.firm.to.FirmEODValuationTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "firmvaluation")
public interface FirmValuationRepository extends PagingAndSortingRepository<FirmEODValuationTO, Integer> {


  public FirmEODValuationTO findTopByCodeOrderByDate(String code);



}
