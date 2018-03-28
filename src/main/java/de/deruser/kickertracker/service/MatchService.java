package de.deruser.kickertracker.service;

import de.deruser.kickertracker.Repository.MatchRepository;
import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MatchService {

  private MatchRepository matchRepository;
  private PlayerService playerService;
  private StatsComputationService statsComputationService;


  @Autowired
  public MatchService(final MatchRepository matchRepository,
      final PlayerService playerService,
      final StatsComputationService statsComputationService){
    this.matchRepository = matchRepository;
    this.playerService = playerService;
    this.statsComputationService = statsComputationService;
  }

  /**
   * Adds a match into data base.
   * @param match to add
   * @return true, if the match could be added to db; false - otherwise
   */
  public boolean addMatch(final Match match) {
    if(isValid(match)) {
      Match computed = statsComputationService.compute(match);
      matchRepository.save(computed);
      updatePlayersGlicko(computed);
      return true;
    }
    return false;
  }

  private void updatePlayersGlicko(final Match match){
//    match.getTeams().stream().flatMap(t -> t.getPlayers().stream()).forEach({playerService::updatePlayerStats);
    match.getTeams().forEach(t -> {
      for (Player player : t.getPlayers()) {
        playerService.updatePlayerStats(player.getName(), player.getGlicko(), player.getDeviation(), player.getVolatility(),
            t.getScore() == 10);
      }
    });
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
      throw new IllegalArgumentException("There is an empty team");
    }

    // check whether all players exists
    final Set<String> players = playerService.getAllPlayerNames();
    final Set<String> uniquePlayers = new HashSet<>();
    match.getTeams().forEach(team -> {
      team.getPlayers().forEach(player -> {
        if(!players.contains(player.getName())){
          throw new IllegalArgumentException("Unknown player " + player.getName());
        }
        if(!uniquePlayers.add(player.getName())){
          throw new IllegalArgumentException("Same player exists on both teams: " + player.getName());
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
      Match updatedMatch = statsComputationService.compute(match);
      matchRepository.save(updatedMatch);
      updatePlayersGlicko(updatedMatch);
    }
  }
}
