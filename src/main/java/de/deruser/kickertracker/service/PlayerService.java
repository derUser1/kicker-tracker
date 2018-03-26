package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayerService {

  private PlayerRepository playerRepository;

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
    final PlayerInfo playerInfo = PlayerInfo.builder().name(name)
            .glicko(1500).deviation(350).volatility(0.06d)
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
   * @param player new player stats
   */
  public void updatePlayerStats(final Player player){
    PlayerInfo.PlayerInfoBuilder playerInfoBuilder = playerRepository.getPlayer(player.getName()).toBuilder()
            .glicko(player.getGlicko())
            .deviation(player.getDeviation())
            .volatility(player.getVolatility())
            .lastModified(Instant.now());
    playerRepository.save(playerInfoBuilder.build());
  }

  public void resetAllStats(final int glicko, final int deviation, final double volatility){
    playerRepository.restStats(glicko, deviation, volatility);
  }
}
