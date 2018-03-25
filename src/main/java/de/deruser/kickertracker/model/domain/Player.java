package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Builder(toBuilder = true)
@ToString
public class Player {

  @Indexed
  private String name;
  private int glicko;
  private int deviation;
  private double volatility;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Player player = (Player) o;
    return Objects.equals(name, player.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
