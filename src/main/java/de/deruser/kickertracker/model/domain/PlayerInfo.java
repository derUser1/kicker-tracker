package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@ToString
@Document(collection = "players")
public class PlayerInfo {

  @Id
  private String name;
  private String password;
  private int glicko;
  private int deviation;
  private double volatility;
  private Instant created;
  private Instant lastModified;
}


