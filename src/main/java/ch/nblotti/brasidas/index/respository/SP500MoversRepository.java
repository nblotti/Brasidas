package ch.nblotti.brasidas.index.respository;

import ch.nblotti.brasidas.common.ReadOnlyRepository;
import ch.nblotti.brasidas.index.to.SP500MoversTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "sp500topmovers")
public interface SP500MoversRepository extends ReadOnlyRepository<SP500MoversTO, Integer> {

  @Cacheable("findFSPirst10TopLoosers")
  @Query("select sp from SP500MoversTO sp where sp.viewType =1 and sp.myRank <= 10")
  Iterable<SP500MoversTO> findFSPirst10TopLoosers();


  @Cacheable("findFSPirst3TopLoosers")
  @Query("select sp from SP500MoversTO sp where sp.viewType =1 and sp.myRank <= 3")
  Iterable<SP500MoversTO> findFSPirst3TopLoosers();

  @Cacheable("findFSPirst10TopWiners")
  @Query("select sp from SP500MoversTO sp where sp.viewType =2 and sp.myRank <= 10")
  Iterable<SP500MoversTO> findFSPirst10TopWiners();


  @Cacheable("findFSPirst3TopWiners")
  @Query("select sp from SP500MoversTO sp where sp.viewType =2 and sp.myRank <= 3")
  Iterable<SP500MoversTO> findFSPirst3TopWiners();

  @Cacheable("findFSPirst10TopMovers")
  @Query("select sp from SP500MoversTO sp where sp.viewType =3 and sp.myRank <= 10")
  Iterable<SP500MoversTO> findFSPirst10TopMovers();

  @Cacheable("findFSPirst3TopMovers")
  @Query("select sp from SP500MoversTO sp where sp.viewType =3 and sp.myRank <= 3")
  Iterable<SP500MoversTO> findFSPirst3TopMovers();


}
