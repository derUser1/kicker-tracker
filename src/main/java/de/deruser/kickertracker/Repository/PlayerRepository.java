package de.deruser.kickertracker.repository;

import de.deruser.kickertracker.model.domain.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

  public void restStats(final double glicko, final double deviation, final double volatility, final int mathCount,
      final int winCount, final int lossCount) {
    Query query = new Query();
    Update update = new Update()
        .set("gameStats.glicko", glicko)
        .set("gameStats.deviation", deviation)
        .set("gameStats.volatility", volatility)
        .set("gameStats.matchCount", mathCount)
        .set("gameStats.winCount", winCount)
        .set("gameStats.lossCount", lossCount)
        .set("lastModified", Instant.now())
        .unset("lastMatch");
    mongoTemplate.updateMulti(query, update, PlayerInfo.class);
  }

  public void resetPassword(String name, String newPassword) {
    Query query = new Query()
        .addCriteria(Criteria.where("name").is(name));
    Update update = new Update()
        .set("password", newPassword);
    mongoTemplate.updateFirst(query, update, PlayerInfo.class);
  }

  public List<PlayerInfo> getActivePlayers(int gameCount) {
    Query query = new Query();
    query.addCriteria(Criteria.where("gameStats.matchCount").gte(gameCount));
    return mongoTemplate.find(query, PlayerInfo.class);
  }
}
