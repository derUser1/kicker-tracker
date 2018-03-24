package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.PlayerInfo;

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
    final PlayerInfo playerInfo = PlayerInfo.builder().name(name).glicko(1500).deviation(350).volatility(0.5d).build();
    playerRepository.save(playerInfo);
    return playerInfo;
  }

  public List<PlayerInfo> getAllPlayers(){
    return playerRepository.getAllPlayers();
  }

  public Set<String> getAllPlayerNames(){
    return playerRepository.getAllPlayers().stream()
        .map(PlayerInfo::getName)
        .collect(Collectors.toSet());
  }
}
