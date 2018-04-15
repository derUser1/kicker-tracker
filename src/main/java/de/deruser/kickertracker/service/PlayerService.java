package de.deruser.kickertracker.service;

import de.deruser.kickertracker.repository.PlayerRepository;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PlayerService {

  private PlayerRepository playerRepository;

  private final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  private static final String DEFAULT_PASSWORD = "password";

  @Autowired
  public PlayerService(final PlayerRepository playerRepository){
    this.playerRepository = playerRepository;
  }


  /**
   * Adds a new player
   * @param name of the new player
   * @return Just added {@link PlayerInfo}
   */
  public PlayerInfo addPlayer(final String name){
    final PlayerInfo.Stats stats = PlayerInfo.Stats.builder()
            .glicko(1500).deviation(350).volatility(0.06d).build();
    final PlayerInfo playerInfo = PlayerInfo.builder().name(name)
            .password(PASSWORD_ENCODER.encode(DEFAULT_PASSWORD))
            .gameStats(stats)
            .created(Instant.now()).build();
    playerRepository.save(playerInfo);
    return playerInfo;
  }

  /**
   * Get all players
   * @return List of {@link PlayerInfo}
   */
  public List<PlayerInfo> getAllPlayers(){
    return playerRepository.getAllPlayers();
  }

  /**
   * Get a player by name
   * @param name of the player
   * @return {@link PlayerInfo}
   */
  public PlayerInfo getPlayer(final String name){
    return playerRepository.getPlayer(name);
  }


  /**
   * Get all player names
   * @return List of player names
   */
  public Set<String> getAllPlayerNames(){
    return playerRepository.getAllPlayers().stream()
        .map(PlayerInfo::getName)
        .collect(Collectors.toSet());
  }

  /**
   * Update  statisic of a player
   */
  public void updatePlayerStats(final String name, final int glicko, final int deviation, final double volatility,
      final boolean winner, final Instant matchTimestamp){
    PlayerInfo oldPlayerInfo = playerRepository.getPlayer(name);
    PlayerInfo.Stats.StatsBuilder statsBuilder = oldPlayerInfo.getGameStats().toBuilder();
    statsBuilder.glicko(glicko)
            .deviation(deviation)
            .volatility(volatility)
            .matchCount(oldPlayerInfo.getGameStats().getMatchCount()+1);

    if(winner){
      statsBuilder.winCount(oldPlayerInfo.getGameStats().getWinCount() +1);
    }else{
      statsBuilder.lossCount(oldPlayerInfo.getGameStats().getLossCount() + 1);
    }

    PlayerInfo.PlayerInfoBuilder playerInfoBuilder = oldPlayerInfo.toBuilder()
            .gameStats(statsBuilder.build())
            .lastModified(Instant.now())
            .lastMatch(matchTimestamp);

    playerRepository.save(playerInfoBuilder.build());
  }

  public void resetAllStats(final int glicko, final int deviation, final double volatility){
    playerRepository.restStats(glicko, deviation, volatility, 0, 0, 0);
  }

  public void resetPassword(String name) {
    playerRepository.resetPassword(name, PASSWORD_ENCODER.encode(DEFAULT_PASSWORD));
  }

}
