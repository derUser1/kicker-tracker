package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

  public PlayerInfo getPlayer(final String name){
    Query query = new Query();
    query.addCriteria(Criteria.where("name").is(name));
    return mongoTemplate.findOne(query, PlayerInfo.class);
  }

  public void save(final PlayerInfo playerInfo){
    mongoTemplate.save(playerInfo);
  }

}
