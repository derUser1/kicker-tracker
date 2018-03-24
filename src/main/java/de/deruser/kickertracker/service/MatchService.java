package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Team;

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
      this.matchRepository.save(match);
      return true;
    }
    return false;
  }

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
}
