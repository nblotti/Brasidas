package ch.nblotti.asset.common;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RabbitMQReceiver {
  @Autowired
  private CacheManager cacheManager;

  @RabbitListener(queues = "#{loaderEvent.name}")
  public void loaderEvent(LoadingEvent loadingEvent) {
    Optional.ofNullable(cacheManager.getCache("indicesMovers")).ifPresent(c -> {
      c.clear();
    });

    Optional.ofNullable(cacheManager.getCache("firmFirst10ByVolume")).ifPresent(c -> {
      c.clear();
    });

    Optional.ofNullable(cacheManager.getCache("firmFirst10ByPercentDesc")).ifPresent(c -> {
      c.clear();
    });

    Optional.ofNullable(cacheManager.getCache("firmFirst10ByPercentAsc")).ifPresent(c -> {
      c.clear();
    });

    Optional.ofNullable(cacheManager.getCache("indicesQuotesByCode")).ifPresent(c -> {
      c.clear();
    });

    Optional.ofNullable(cacheManager.getCache("indicesQuotesByCodeAfterDate")).ifPresent(c -> {
      c.clear();
    });


  }
}
