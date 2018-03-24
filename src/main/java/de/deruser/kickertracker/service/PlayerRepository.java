package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.PlayerInfo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlayerRepository {

  private MongoTemplate mongoTemplate;

  @Autowired
  public PlayerRepository(final MongoTemplate mongoTemplate){
    this.mongoTemplate = mongoTemplate;
  }

  public List<PlayerInfo> getAllPlayers(){
    return mongoTemplate.findAll(PlayerInfo.class);
  }

  public void save(final PlayerInfo playerInfo){
    mongoTemplate.save(playerInfo);
  }
}
