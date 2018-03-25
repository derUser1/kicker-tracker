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


  public PlayerInfo addPlayer(final String name){
    final PlayerInfo playerInfo = PlayerInfo.builder().name(name)
            .glicko(1500).deviation(350).volatility(0.06d)
            .created(Instant.now()).build();
    playerRepository.save(playerInfo);
    return playerInfo;
  }

  public List<PlayerInfo> getAllPlayers(){
    return playerRepository.getAllPlayers();
  }

  public PlayerInfo getPlayer(final String name){
    return playerRepository.getPlayer(name);
  }

  public Set<String> getAllPlayerNames(){
    return playerRepository.getAllPlayers().stream()
        .map(PlayerInfo::getName)
        .collect(Collectors.toSet());
  }

  public void updatePlayer(final Player player){
    PlayerInfo.PlayerInfoBuilder playerInfoBuilder = playerRepository.getPlayer(player.getName()).toBuilder()
            .glicko(player.getGlicko())
            .deviation(player.getDeviation())
            .volatility(player.getVolatility())
            .lastModified(Instant.now());
    playerRepository.save(playerInfoBuilder.build());
  }
}
