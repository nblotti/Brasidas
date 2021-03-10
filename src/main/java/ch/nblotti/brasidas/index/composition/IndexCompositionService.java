package ch.nblotti.brasidas.index.composition;


import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


@Service
public class IndexCompositionService {


  public static final String INDEXES = "indexes";
  public static final String INDEX_MAP = "indexesMap";


  @Autowired
  private EODIndexCompositionRepository eODIndexCompositionRepository;



  @Autowired
  protected ModelMapper modelMapper;


  @Autowired
  protected DateTimeFormatter format1;


  public Iterable<IndexCompositionDTO> loadAndSaveIndexCompositionAtDate(LocalDate runDate, String index) {
    Collection<IndexCompositionDTO> indexCompositionDTOS = getIndexDataByDate(runDate, index);
    return saveIndexComposition(indexCompositionDTOS);
  }

  public List<IndexCompositionDTO> getIndexDataByDate(LocalDate runDate, String index) {

    List<IndexCompositionDTO> indexCompositionDTOs = new ArrayList<>();

    Collection<EODIndexCompositionDTO> EODIndexCompositionDTOS = eODIndexCompositionRepository.getIndexCompositionAtDate(runDate, index);

    for (EODIndexCompositionDTO current : EODIndexCompositionDTOS) {

      IndexCompositionDTO fHpost = modelMapper.map(current, IndexCompositionDTO.class);
      fHpost.setDate(runDate);
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

        indexCompositionDTO.setExchange(eODIndexCompositionDTO.getExchange());

        indexCompositionDTO.setName(eODIndexCompositionDTO.getName());

        indexCompositionDTO.setSector(eODIndexCompositionDTO.getSector());

        indexCompositionDTO.setIndustry(eODIndexCompositionDTO.getIndustry());

        return indexCompositionDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }

  public Iterable<IndexCompositionDTO> saveIndexComposition(Collection<IndexCompositionDTO> indexCompositionDTOS) {


    return indexCompositionDTOS;
  }


}
