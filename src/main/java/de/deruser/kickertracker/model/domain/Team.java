package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Team {
  private List<Player> players;
  private int score;
}
