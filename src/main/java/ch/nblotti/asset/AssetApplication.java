package ch.nblotti.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class AssetApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssetApplication.class, args);
  }

  @Value("${global.date-format}")
  public String dateFormat;


  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder
      .setConnectTimeout(Duration.ofSeconds(30))
      .setReadTimeout(Duration.ofSeconds(30))
      .build();
  }

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate rt = new RestTemplate();
    rt.getMessageConverters().add(new StringHttpMessageConverter());
    return rt;

  }
  @Bean
  public DateTimeFormatter format1() {
    return DateTimeFormatter.ofPattern(dateFormat);
  }

}
