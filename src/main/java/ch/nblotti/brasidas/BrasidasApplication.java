package ch.nblotti.brasidas;

import ch.nblotti.brasidas.security.JwtLocalToken;
import ch.nblotti.brasidas.security.SecurityConstants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.format.Formatter;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@PropertySource(value = "classpath:override.properties", ignoreResourceNotFound = true)
public class BrasidasApplication {

  public static void main(String[] args) {
    SpringApplication.run(BrasidasApplication.class, args);
  }


  public static final String CACHE_NAME = "cache";

  @Value("${global.date-format}")
  public String dateFormat;


  @Value("${keystore.location}")
  private Resource keystoreLocation;

  @Value("${keystore.password}")
  private String keyStorePassword;


  @Value("${key.name}")
  private String key_name = "brasidas";


  @Value("zeus.validation.url")
  private String validationUrl;

  private KeyStore ks;


  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }


  @Bean
  public Key key(KeyStore keyStore) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    return keyStore.getKey(key_name, keyStorePassword.toCharArray());

  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, JwtLocalToken jwtLocalToken) {


    RestTemplate restTemplate = restTemplateBuilder
      .setConnectTimeout(Duration.ofSeconds(30))
      .setReadTimeout(Duration.ofMinutes(5))
      .build();

    restTemplate.setInterceptors(Arrays.asList(interceptor(restTemplate, jwtLocalToken)));
    return restTemplate;
  }

  @Bean
  ClientHttpRequestInterceptor interceptor(RestTemplate restTemplate, JwtLocalToken jwtLocalToken) {

    jwtLocalToken.setRestTemplate(restTemplate);
    ClientHttpRequestInterceptor clientHttpRequestInterceptor = new ClientHttpRequestInterceptor() {

      @Override
      public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {


        String bearer = "";
        ClientHttpResponse response;

        if (!httpRequest.getURI().getPath().contains("sharedKey") && !httpRequest.getURI().getPath().contains("login")) {
          bearer = jwtLocalToken.getJWT();
          httpRequest.getHeaders().add(SecurityConstants.TOKEN_HEADER, bearer);
        }

        response = clientHttpRequestExecution.execute(httpRequest, bytes);

        if (HttpStatus.UNAUTHORIZED == response.getStatusCode() || HttpStatus.FORBIDDEN == response.getStatusCode() ) {
          httpRequest.getHeaders().remove(SecurityConstants.TOKEN_HEADER);
          jwtLocalToken.getNewJWT();
          httpRequest.getHeaders().add(SecurityConstants.TOKEN_HEADER, bearer);
          return clientHttpRequestExecution.execute(httpRequest, bytes);

        }

        return response;

      }
    };
    return clientHttpRequestInterceptor;

  }

  @Bean
  public DateTimeFormatter format1() {
    return DateTimeFormatter.ofPattern(dateFormat);
  }


  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }


  @Bean
  public Cache cacheOne() {
    return new CaffeineCache(CACHE_NAME, Caffeine.newBuilder()
      .expireAfterWrite(30, TimeUnit.SECONDS)
      .build());
  }

  @Bean
  public Formatter<LocalDate> localDateFormatter() {

    return new Formatter<LocalDate>() {
      @Override
      public LocalDate parse(String text, Locale locale) throws ParseException {
        return LocalDate.parse(text, format1());
      }

      @Override
      public String print(LocalDate object, Locale locale) {
        return format1().format(object);
      }
    };
  }


  @Bean
  public KeyStore keyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

    if (ks == null) {
      ks = KeyStore.getInstance("JKS");
      ks.load(keystoreLocation.getInputStream(), keyStorePassword.toCharArray());
    }
    return ks;
  }


}
