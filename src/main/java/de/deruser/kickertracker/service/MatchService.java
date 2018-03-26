package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.domain.Team;
import forwardloop.glicko2s.EloResult;
import forwardloop.glicko2s.Glicko2;
import forwardloop.glicko2s.Glicko2J;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchService {

  private MatchRepository matchRepository;
  private PlayerService playerService;

  @Autowired
  public MatchService(final MatchRepository matchRepository,
      final PlayerService playerService){
    this.matchRepository = matchRepository;
    this.playerService = playerService;
  }

  /**
   * Adds a match into data base.
   * @param match to add
   * @return true, if the match could be added to db; false - otherwise
   */
  public boolean addMatch(final Match match) {
    if(isValid(match)) {
      Match computed = computeGlicko(match);
      matchRepository.save(computed);
      updatePlayersGlicko(computed);
      return true;
    }
    return false;
  }

  /**
   * Computes new stats for each player of the given match
   * @param match Match information
   * @return Update player stats
   */
  private Match computeGlicko(final Match match){
    Team teamOne = match.getTeams().get(0);
    Team teamTwo = match.getTeams().get(1);

    Team.TeamBuilder teamOneBuilder = teamOne.toBuilder().players(computeGlicko(teamOne, teamTwo));
    Team.TeamBuilder teamTwoBuilder = teamTwo.toBuilder().players(computeGlicko(teamTwo, teamOne));

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
  private List<Player> computeGlicko(final Team teamOne, final Team teamTwo){
    List<Player> result = new ArrayList<>();
    EloResult eloResult = teamOne.getScore() > teamTwo.getScore() ? Glicko2J.Win : Glicko2J.Loss;
    for(Player player : teamOne.getPlayers()){
        Player opponent = getOpponent(player, teamTwo);
        PlayerInfo currentPlayerInfo = playerService.getPlayer(player.getName());
        PlayerInfo opponentPlayerInfo = playerService.getPlayer(opponent.getName());
        Glicko2 newGlicko = computeGlicko(currentPlayerInfo, opponentPlayerInfo, eloResult);

        Player.PlayerBuilder playerBuilder = player.toBuilder()
                .glicko((int) newGlicko.rating())
                .deviation((int) newGlicko.ratingDeviation())
                .volatility(newGlicko.ratingVolatility())
                .glickoChange(currentPlayerInfo.getGlicko() - (int) newGlicko.rating());
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
  private Glicko2 computeGlicko(final PlayerInfo player, final PlayerInfo opponent, final EloResult outcome){
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
      int currentOpponentGlicko = Math.abs(player.getGlicko() - currentOpponent.getGlicko());
      int oldOpponentGlicko = Math.abs(player.getGlicko() - currentOpponent.getGlicko());
      if(currentOpponentGlicko < oldOpponentGlicko){
        opponent = currentOpponent;
      }
    }
    return opponent;
  }

  private void updatePlayersGlicko(final Match match){
    match.getTeams().stream().flatMap(t -> t.getPlayers().stream()).forEach(playerService::updatePlayerStats);
  }

  /**
   * Return a list of recent matches for a given player
   * @param name player
   * @param skip amount of entries to skip
   * @param limit amount of entries to receive
   * @return list of {@link Match}
   */
  public List<Match> getRecentMatches(final String name, final int skip, final int limit) {
    return this.matchRepository.findRecentGamesForPlayer(name, skip, limit);
  }

  /**
   * Return a list of recent matches
   * @param skip amount of entries to skip
   * @param limit amount of entries to receive
   * @return list of {@link Match}
   */
  public List<Match> getRecentMatches(final int skip, final int limit) {
    return this.matchRepository.findMatchesOrdered(skip, limit);
  }

  /**
   * Check whether mach is valid
   * @param match to check
   * @return true if valid; otherwise - false
   */
  private boolean isValid(final Match match){
    return isValidScore(match) && isValidPlayers(match);
  }

  /**
   * Checks whether there are valid players.
   * @param match to chekc
   * @return ture, if valid.
   */
  private boolean isValidPlayers(Match match) {
    if(match.getTeams().stream().anyMatch(team -> team.getPlayers().isEmpty())){
      throw new IllegalArgumentException("There is a empty team");
    }

    // check whether all players exists
    final Set<String> players = playerService.getAllPlayerNames();

    match.getTeams().forEach(team -> {
      team.getPlayers().forEach(player -> {
        if(!players.contains(player.getName())){
          throw new IllegalArgumentException("Unknown player " + player.getName());
        }
      });
    });

    return true;
  }

  /**
   * Checks whether score is valid, e.g. within the range [0,10].
   * There are several {@link IllegalArgumentException} that are thrown if a check fail.
   * @param match to check
   * @return true, if valid.
   */
  private boolean isValidScore(final Match match){
    if(match.getTeams().size() == 1){
      throw new IllegalArgumentException("Team size should be 2");
    }
    Team team1 = match.getTeams().get(0);
    Team team2 = match.getTeams().get(1);

    if(team1.getScore() == team2.getScore()){
      throw new IllegalArgumentException("Score of both teams can not be the same");
    }

    if(team1.getScore() > 10 && team1.getScore() < -1 && team2.getScore() > 10 && team2.getScore() < -1){
      throw new IllegalArgumentException("Score must be within the range 0 and 10");
    }
    return true;
  }

  public void reprocessStats() {
    playerService.resetAllStats(1500, 350, 0.06d);
    List<Match> matches = matchRepository.finaAllMatches();
    for(Match match : matches){
      Match updatedMatch = computeGlicko(match);
      matchRepository.save(updatedMatch);
      updatePlayersGlicko(updatedMatch);
    }
  }
}
