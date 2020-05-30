package ch.nblotti.asset.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

@NoRepositoryBean
public interface ReadOnlyRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

}
