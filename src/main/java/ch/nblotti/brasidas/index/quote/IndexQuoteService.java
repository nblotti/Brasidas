package ch.nblotti.brasidas.index.quote;


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
import java.util.Collection;
import java.util.List;


@Service
public class IndexQuoteService {


  public static final String INDEXES = "indexes";
  public static final String INDEX_MAP = "indexesMap";


  @Autowired
  private EODIndexQuoteRepository eodIndexQuoteRepository;


  @Autowired
  protected ModelMapper modelMapper;

  @Autowired
  private RestTemplate internalRestTemplate;

  @Value("${referential.index.quote.baseurl}")
  private String indexQuoteStr;


  @Autowired
  protected DateTimeFormatter format1;


  public List<IndexQuoteDTO> getIndexDataByDate(LocalDate fromDate, LocalDate toDate, String index) {

    List<IndexQuoteDTO> indexQuoteDTOs = new ArrayList<>();

    Collection<EODIndexQuoteDTO> eODIndexQuoteDTOs = eodIndexQuoteRepository.getIndexDataByDate(fromDate, toDate, index);

    for (EODIndexQuoteDTO current : eODIndexQuoteDTOs) {

      IndexQuoteDTO fHpost = modelMapper.map(current, IndexQuoteDTO.class);
      indexQuoteDTOs.add(fHpost);

    }
    return indexQuoteDTOs;
  }


  public IndexQuoteDTO getIndexDataByDate(LocalDate fromDate, String index) {

    List<IndexQuoteDTO> indexQuoteDTOs = new ArrayList<>();

    Collection<EODIndexQuoteDTO> eODIndexQuoteDTOs = eodIndexQuoteRepository.getIndexDataByDate(fromDate, fromDate, index);

    for (EODIndexQuoteDTO current : eODIndexQuoteDTOs) {

      IndexQuoteDTO fHpost = modelMapper.map(current, IndexQuoteDTO.class);
      indexQuoteDTOs.add(fHpost);

    }
    if (indexQuoteDTOs.isEmpty())
      return null;
    return indexQuoteDTOs.get(0);
  }


  public IndexQuoteDTO saveEODIndexQuotes(IndexQuoteDTO indexQuoteDTO) {

    HttpEntity<IndexQuoteDTO> request = new HttpEntity<IndexQuoteDTO>(indexQuoteDTO);

    return internalRestTemplate.postForObject(indexQuoteStr, request, IndexQuoteDTO.class);

  }


  @PostConstruct
  void initIndexQuoteDTOMapper() {
    Converter<EODIndexQuoteDTO, IndexQuoteDTO> toUppercase = new AbstractConverter<EODIndexQuoteDTO, IndexQuoteDTO>() {

      @Override
      protected IndexQuoteDTO convert(EODIndexQuoteDTO eODIndexQuoteDTO) {
        IndexQuoteDTO indexQuoteDTO = new IndexQuoteDTO();

        indexQuoteDTO.setCode(eODIndexQuoteDTO.getCode());
        indexQuoteDTO.setDate(LocalDate.parse(eODIndexQuoteDTO.getDate(), format1));
        indexQuoteDTO.setOpen(eODIndexQuoteDTO.getOpen());
        indexQuoteDTO.setHigh(eODIndexQuoteDTO.getHigh());
        indexQuoteDTO.setLow(eODIndexQuoteDTO.getLow());
        indexQuoteDTO.setClose(eODIndexQuoteDTO.getClose());
        indexQuoteDTO.setAdjustedClose(eODIndexQuoteDTO.getAdjusted_close());
        indexQuoteDTO.setVolume(eODIndexQuoteDTO.getVolume());
        return indexQuoteDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }
}
