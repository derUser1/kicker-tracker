package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

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
  private List<String> roles;
  private String team;
  private Instant created;
  private Instant lastModified;
  private Instant lastMatch;
  private Stats gameStats;

  @Getter
  @Builder(toBuilder = true)
  @ToString
  public static class Stats{
    private int glicko;
    private int deviation;
    private double volatility;
    private int matchCount;
    private int winCount;
    private int lossCount;
  }
}


