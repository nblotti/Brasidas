package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.FirmEODQuoteTO;
import ch.nblotti.asset.index.to.FirmEODValuationTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@RepositoryRestResource(path = "firmvaluation")
public interface FirmValuationRepository extends PagingAndSortingRepository<FirmEODValuationTO, Integer> {


  public FirmEODValuationTO findTopByCodeOrderByDate(String code);



}
