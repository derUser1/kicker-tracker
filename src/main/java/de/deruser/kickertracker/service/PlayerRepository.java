package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.PlayerInfo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

  public PlayerInfo getPlayer(final String name){
    Query query = new Query();
    query.addCriteria(Criteria.where("name").is(name));
    return mongoTemplate.findOne(query, PlayerInfo.class);
  }

  public void save(final PlayerInfo playerInfo){
    mongoTemplate.save(playerInfo);
  }

  public void restStats(final int glicko, final int deviation, final double volatility) {
    Query query = new Query();
    Update update = new Update()
        .set("glicko", glicko)
        .set("deviation", deviation)
        .set("volatility", volatility);
    mongoTemplate.updateMulti(query, update, PlayerInfo.class);
  }
}
