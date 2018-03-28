package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder(toBuilder = true)
@ToString
@Document(collection = "players")
public class PlayerInfo {

  @Id
  private String name;
  private String password;

  /* e.g. department */
  private String team;
  private int glicko;
  private int deviation;
  private double volatility;
  private Instant created;
  private Instant lastModified;
  private int matchCount;
  private int winCount;
  private int lossCount;
}


