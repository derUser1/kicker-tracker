package de.deruser.kickertracker.model.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class MatchViewModel {

  private Instant timestamp;
  private TeamViewModel teamOne;
  private TeamViewModel teamTwo;
}
