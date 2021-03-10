package ch.nblotti.brasidas.index.quote;


import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
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
public class IndexQuoteService {


  public static final String INDEXES = "indexes";
  public static final String INDEX_MAP = "indexesMap";


  @Autowired
  private EODIndexQuoteRepository eodIndexQuoteRepository;


  @Autowired
  protected ModelMapper modelMapper;


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
