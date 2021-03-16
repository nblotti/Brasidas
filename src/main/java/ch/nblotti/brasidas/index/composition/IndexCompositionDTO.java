package ch.nblotti.brasidas.index.composition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class IndexCompositionDTO {

  private Integer id;

  private String code;

  private LocalDate startDate;

  private LocalDate endDate;

  private boolean isActiveNow;

  private boolean isDelisted;


}


