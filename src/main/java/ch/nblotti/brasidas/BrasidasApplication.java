package ch.nblotti.brasidas;

import ch.nblotti.brasidas.security.JwtLocalToken;
import ch.nblotti.brasidas.security.SecurityConstants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.format.Formatter;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@PropertySource(value = "classpath:override.properties", ignoreResourceNotFound = true)
public class BrasidasApplication {

  private static final Logger logger = Logger.getLogger("BrasidasApplication");

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

    restTemplate.setInterceptors(Arrays.asList(interceptor(jwtLocalToken)));
    return restTemplate;
  }

  @Bean
  ClientHttpRequestInterceptor interceptor(JwtLocalToken jwtLocalToken) {

    ClientHttpRequestInterceptor clientHttpRequestInterceptor = new ClientHttpRequestInterceptor() {

      @Override
      public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {


        String bearer = "";
        ClientHttpResponse response = null;

        if (!httpRequest.getURI().getPath().contains("sharedKey") && !httpRequest.getURI().getPath().contains("login")) {
          while (bearer.isEmpty()) {
            try {
              bearer = jwtLocalToken.getJWT();
            } catch (Exception exception) {
              logger.severe("Error creating jwt token, retrying");
              logger.severe(exception.getMessage());
            }
          }
          httpRequest.getHeaders().add(SecurityConstants.TOKEN_HEADER, bearer);
        }

        try {
          response = clientHttpRequestExecution.execute(httpRequest, bytes);
        } catch (IOException exception) {
          try{
          logger.severe("Error sending request, retrying in 30s");
            Thread.sleep(30000);
          } catch (InterruptedException e) {
            logger.severe("Sleep time interrupted have not waited 30s before retry");
          }
        }
        while (response == null || (HttpStatus.UNAUTHORIZED == response.getStatusCode() || HttpStatus.FORBIDDEN == response.getStatusCode())) {
          try {
            httpRequest.getHeaders().remove(SecurityConstants.TOKEN_HEADER);
            bearer = jwtLocalToken.getNewJWT();
            httpRequest.getHeaders().add(SecurityConstants.TOKEN_HEADER, bearer);

            response = clientHttpRequestExecution.execute(httpRequest, bytes);
          } catch (Exception exception) {
            logger.severe("Error sending request");

            logger.severe(exception.getMessage());
          }
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
