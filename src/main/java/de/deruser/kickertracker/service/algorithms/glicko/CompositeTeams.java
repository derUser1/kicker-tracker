package de.deruser.kickertracker.service.algorithms.glicko;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.domain.Team;
import de.deruser.kickertracker.service.PlayerService;
import de.deruser.kickertracker.service.StatsAlgorithm;
import forwardloop.glicko2s.EloResult;
import forwardloop.glicko2s.Glicko2;
import forwardloop.glicko2s.Glicko2J;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.deruser.kickertracker.service.algorithms.glicko.GlickoUtils.getStats;

/**
 * The opponent is determined as mean of the players of the opposite team. No periods.
 */
@Component
public class CompositeTeams implements StatsAlgorithm {

  private final PlayerService playerService;

  @Autowired
  public CompositeTeams(final PlayerService playerService){
    this.playerService = playerService;
  }

  @Override
  public String getName() {
    return "glickoCompositeTeam";
  }

  @Override
  public Match compute(Match match) {
    Map<String, PlayerInfo> playerInfoMap = match.getTeams().stream().flatMap(t -> t.getPlayers().stream())
            .map(p -> playerService.getPlayer(p.getName()))
            .collect(Collectors.toMap(PlayerInfo::getName, Function.identity()));

    Team teamOne = match.getTeams().get(0);
    Team teamTwo = match.getTeams().get(1);

    Team.TeamBuilder teamOneBuilder = teamOne.toBuilder().players(computeGlicko(teamOne, teamTwo, match.getTimestamp(), playerInfoMap));
    Team.TeamBuilder teamTwoBuilder = teamTwo.toBuilder().players(computeGlicko(teamTwo, teamOne, match.getTimestamp(), playerInfoMap));

    return match.toBuilder()
        .teams(Arrays.asList(teamOneBuilder.build(), teamTwoBuilder.build()))
        .build();
  }

  /**
   * Computes new stats for each player of the first team (teamOne)
   * @param teamOne team, which player stats will be computed
   * @param teamTwo opponent team
   * @return List of updated player stats
   */
  private List<Player> computeGlicko(final Team teamOne, final Team teamTwo, final Instant matchTimestamp, final Map<String, PlayerInfo> playerInfoMap){
    List<Player> result = new ArrayList<>();
    PlayerInfo.Stats opponentTeam = getTeamStats(teamTwo, matchTimestamp, playerInfoMap);
    PlayerInfo.Stats playersTeam = getTeamStats(teamOne, matchTimestamp, playerInfoMap);

    EloResult eloResult = teamOne.getScore() > teamTwo.getScore() ? Glicko2J.Win : Glicko2J.Loss;
    Glicko2 newGlicko = computeGlicko(playersTeam, opponentTeam, eloResult);

    for(Player player : teamOne.getPlayers()){
      PlayerInfo currentPlayerInfo = playerInfoMap.get(player.getName());

      double factor = getPercentOf(currentPlayerInfo, teamOne, playerInfoMap);

      double g = (newGlicko.rating() - playersTeam.getGlicko()) * factor;
      double d = (newGlicko.ratingDeviation() - playersTeam.getDeviation()) * factor;
      double v = (newGlicko.ratingVolatility() - playersTeam.getVolatility()) * factor;

      Player.PlayerBuilder playerBuilder = player.toBuilder()
          .glicko(currentPlayerInfo.getGameStats().getGlicko() + (int) g)
          .deviation(currentPlayerInfo.getGameStats().getDeviation() + (int) d)
          .volatility(currentPlayerInfo.getGameStats().getVolatility() + v)
          .glickoChange((int) g);
      result.add(playerBuilder.build());
    }
    return result;
  }

  private double getPercentOf(PlayerInfo player, Team team, Map<String, PlayerInfo> playerInfoMap){
    double sum = team.getPlayers().stream()
            .map(p -> playerInfoMap.get(p.getName()))
            .mapToDouble(p -> p.getGameStats().getGlicko()).sum();
    return player.getGameStats().getGlicko() / sum;
  }

  /**
   * Computes glicko for given player
   * @param player which stats will be computed
   * @param opponent of the player
   * @param outcome game outcome
   * @return new glicko value
   */
  private Glicko2 computeGlicko(final PlayerInfo.Stats player, final PlayerInfo.Stats opponent, final EloResult outcome){
    Glicko2 currentPlayer = new Glicko2(player.getGlicko(), player.getDeviation(), player.getVolatility());
    Glicko2 opponentPlayer = new Glicko2(opponent.getGlicko(), opponent.getDeviation(), opponent.getVolatility());

    Tuple2<Glicko2, EloResult> tuple2 = new Tuple2<>(opponentPlayer, outcome);

    return Glicko2J.calculateNewRating(currentPlayer, Collections.singletonList(tuple2));
  }

  /**
   * Determines opponent of the given player. The new glicko of the given player is computed in respect to the opponent.
   * The opponent is the player with the closest glicko to the current player.
   * @param team opponent team
   * @return opponent player
   */
  private PlayerInfo.Stats getTeamStats(final Team team, final Instant matchTimestamp, final Map<String, PlayerInfo> playerInfoMap){
    List<PlayerInfo> players = team.getPlayers().stream().map(p -> playerInfoMap.get(p.getName())).collect(Collectors.toList());

    int glicko = 0;
    int deviation = 0;
    double volatility = 0d;
    int count = players.size();

    for(PlayerInfo p : players){
      PlayerInfo.Stats stats = getStats(p, matchTimestamp);
      glicko += stats.getGlicko();
      deviation += stats.getDeviation();
      volatility += stats.getVolatility();
    }

    return PlayerInfo.Stats.builder()
            .deviation(deviation / count)
            .glicko(glicko / count)
            .volatility( volatility / count)
            .build();
  }
}
