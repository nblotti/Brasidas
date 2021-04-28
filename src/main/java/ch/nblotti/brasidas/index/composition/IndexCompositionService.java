package ch.nblotti.brasidas.index.composition;


import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Service
public class IndexCompositionService {


  public static final String INDEXES = "indexes";
  public static final String INDEX_MAP = "indexesMap";


  @Autowired
  private EODIndexCompositionRepository eODIndexCompositionRepository;

  @Value("${referential.index.composition.baseurl}")
  public String indexComponentUrl;


  @Autowired
  protected RestTemplate internalRestTemplate;

  @Autowired
  protected ModelMapper modelMapper;


  @Autowired
  protected DateTimeFormatter format1;


  public List<IndexCompositionDTO> getIndexComposition(String index) {

    List<IndexCompositionDTO> indexCompositionDTOs = new ArrayList<>();

    Collection<EODIndexCompositionDTO> EODIndexCompositionDTOS = eODIndexCompositionRepository.getIndexComposition(index);

    for (EODIndexCompositionDTO current : EODIndexCompositionDTOS) {

      IndexCompositionDTO fHpost = modelMapper.map(current, IndexCompositionDTO.class);
      indexCompositionDTOs.add(fHpost);

    }
    return indexCompositionDTOs;
  }


  @PostConstruct
  void initEODIndexCompositionDTOMapper() {

    Converter<EODIndexCompositionDTO, IndexCompositionDTO> toUppercase = new AbstractConverter<EODIndexCompositionDTO, IndexCompositionDTO>() {

      @Override
      protected IndexCompositionDTO convert(EODIndexCompositionDTO eODIndexCompositionDTO) {

        IndexCompositionDTO indexCompositionDTO = new IndexCompositionDTO();

        indexCompositionDTO.setCode(eODIndexCompositionDTO.getCode());

        indexCompositionDTO.setActiveNow(Boolean.parseBoolean(eODIndexCompositionDTO.getIsActiveNow()));

        indexCompositionDTO.setDelisted(Boolean.parseBoolean(eODIndexCompositionDTO.getIsDelisted()));

        indexCompositionDTO.setStartDate(LocalDate.parse(eODIndexCompositionDTO.getStartDate(), format1));

        indexCompositionDTO.setEndDate(LocalDate.parse(eODIndexCompositionDTO.getEndDate(), format1));

        return indexCompositionDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public List<IndexCompositionDTO> saveAll(List<IndexCompositionDTO> indexCompositionDTOS) {

    HttpEntity<Collection<IndexCompositionDTO>> request = new HttpEntity<Collection<IndexCompositionDTO>>(indexCompositionDTOS);

    IndexCompositionDTO[] responseEntity = internalRestTemplate.postForObject(indexComponentUrl, request, IndexCompositionDTO[].class);

    return Arrays.asList(responseEntity);

  }

  public void deleteAll() {

    internalRestTemplate.delete(indexComponentUrl);


  }


}
