package ch.nblotti.brasidas;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.format.Formatter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class BrasidasApplication {

  public static void main(String[] args) {
    SpringApplication.run(BrasidasApplication.class, args);
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


  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public Queue loaderEvent() {
    return new Queue("loader_event");
  }

  @Bean
  FanoutExchange loaderExchange() {
    return new FanoutExchange("loader_exchange");
  }

  @Bean
  public Binding bindingFanoutExchangeQueueEFanout(FanoutExchange loaderExchange, Queue loaderEvent) {
    return BindingBuilder.bind(loaderEvent).to(loaderExchange);
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

}
