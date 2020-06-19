package ch.nblotti.asset.index.respository;

import ch.nblotti.asset.index.to.IndexQuoteTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collection;

@RepositoryRestResource(path = "indexquote")
public interface IndexQuoteRepository extends PagingAndSortingRepository<IndexQuoteTO, Integer> {


  @Cacheable("indicesQuotesByCode")
  public Collection<IndexQuoteTO> findByCode(String code);

  @Cacheable("indicesQuotesByCodeAfterDate")
  public Collection<IndexQuoteTO> findByCodeAndDateAfterOrderByDate(String code, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date);
}
