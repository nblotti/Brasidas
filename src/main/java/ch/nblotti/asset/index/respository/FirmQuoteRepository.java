package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.FirmEODQuoteTO;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collection;

@RepositoryRestResource(path = "firmquotes")
public interface FirmQuoteRepository extends PagingAndSortingRepository<FirmEODQuoteTO, Integer> {

  public Collection<FirmEODQuoteTO> findAllByDate(LocalDate date);

  public Collection<FirmEODQuoteTO> findByCodeOrderByDateAsc(String code);

  public Collection<FirmEODQuoteTO> findByCodeAndDateAfterOrderByDate(String code, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date);
}
