package ch.nblotti.brasidas.index.composition;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

@Component
public class EODIndexCompositionRepository {


  @Autowired
  private DateTimeFormatter format1;

  @Value("${index.component.api.url}")
  public String eodIndexComponentUrl;


  public String conponentStr = "$.HistoricalTickerComponents[*]";


  @Value("${spring.application.eod.api.key}")
  public String apiKey;


  @Autowired
  private RestTemplate externalShortRestTemplate;

  @Autowired
  protected ModelMapper modelMapper;


  public Collection<EODIndexCompositionDTO> getIndexComposition(String index) {


    String finalUrl = String.format(eodIndexComponentUrl, index, apiKey);

    final ResponseEntity<String> response = externalShortRestTemplate.getForEntity(finalUrl, String.class);

    DocumentContext jsonContext = JsonPath.parse(response.getBody());

    return Arrays.asList(jsonContext.read(conponentStr, EODIndexCompositionDTO[].class));

  }


  @PostConstruct
  void initFirmQuoteMapper() {

    Converter<JSONObject, EODIndexCompositionDTO> toUppercase = new AbstractConverter<JSONObject, EODIndexCompositionDTO>() {

      @Override
      protected EODIndexCompositionDTO convert(JSONObject firmDTO) {
        EODIndexCompositionDTO eodIndexCompositionDTO = new EODIndexCompositionDTO();

        eodIndexCompositionDTO.setCode(firmDTO.getAsString("Code"));
        eodIndexCompositionDTO.setName(firmDTO.getAsString("Name"));
        eodIndexCompositionDTO.setStartDate(firmDTO.getAsString("StartDate"));
        eodIndexCompositionDTO.setEndDate(firmDTO.getAsString("EndDate"));
        eodIndexCompositionDTO.setIsActiveNow(firmDTO.getAsString("IsActiveNow"));
        eodIndexCompositionDTO.setIsDelisted(firmDTO.getAsString("IsDelisted"));

        return eodIndexCompositionDTO;
      }
    };

    modelMapper.addConverter(toUppercase);

  }


}


