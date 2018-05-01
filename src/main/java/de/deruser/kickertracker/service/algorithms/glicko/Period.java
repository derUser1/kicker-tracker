package de.deruser.kickertracker.service.algorithms.glicko;

import de.deruser.kickertracker.repository.MatchRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FIXME: Not yet implemented
 */
@Component
@Deprecated
public class Period implements StatsAlgorithm {

  private final PlayerService playerService;
  private final MatchRepository matchRepository;

  @Autowired
  public Period(final PlayerService playerService,
                final MatchRepository matchRepository){
    this.playerService = playerService;
    this.matchRepository = matchRepository;
  }

  @Override
  public String getName() {
    return "glickoPeriod";
  }

  @Override
  public Match compute(Match match) {
//    Team teamOne = match.getTeams().get(0);
//    Team teamTwo = match.getTeams().get(1);
//
//    Team.TeamBuilder teamOneBuilder = teamOne.toBuilder().players(computeGlicko(teamOne, teamTwo));
//    Team.TeamBuilder teamTwoBuilder = teamTwo.toBuilder().players(computeGlicko(teamTwo, teamOne));
//
//    return match.toBuilder()
//        .teams(Arrays.asList(teamOneBuilder.build(), teamTwoBuilder.build()))
//        .build();

    return null;
  }

  /**
   * Computes new stats for each player of the first team (teamOne)
   * @param teamOne team, which player stats will be computed
   * @param teamTwo opponent team
   * @return List of updated player stats
   */
  private List<Player> computeGlicko(final Team teamOne, final Team teamTwo){
    List<Player> result = new ArrayList<>();
    EloResult eloResult = teamOne.getScore() > teamTwo.getScore() ? Glicko2J.Win : Glicko2J.Loss;
    for(Player player : teamOne.getPlayers()){
      Player opponent = getOpponent(player, teamTwo);
      PlayerInfo currentPlayerInfo = playerService.getPlayer(player.getName());
      PlayerInfo opponentPlayerInfo = playerService.getPlayer(opponent.getName());
      Glicko2 newGlicko = computeGlicko(currentPlayerInfo.getGameStats(), opponentPlayerInfo.getGameStats(), eloResult);

      Player.PlayerBuilder playerBuilder = player.toBuilder()
          .glicko( newGlicko.rating())
          .deviation( newGlicko.ratingDeviation())
          .volatility(newGlicko.ratingVolatility())
          .glickoChange( newGlicko.rating() - currentPlayerInfo.getGameStats().getGlicko());
      result.add(playerBuilder.build());
    }
    return result;
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
   * @param player current player
   * @param team opponent team
   * @return opponent player
   */
  private Player getOpponent(final Player player, final Team team){
    Player opponent = null;
    for(Player currentOpponent : team.getPlayers()){
      if(opponent == null){
        opponent = currentOpponent;
        continue;
      }
      double currentOpponentGlicko = Math.abs(player.getGlicko() - currentOpponent.getGlicko());
      double oldOpponentGlicko = Math.abs(player.getGlicko() - currentOpponent.getGlicko());
      if(currentOpponentGlicko < oldOpponentGlicko){
        opponent = currentOpponent;
      }
    }
    return opponent;
  }
}
