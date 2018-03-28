package de.deruser.kickertracker.Repository;

import de.deruser.kickertracker.model.domain.Match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Access to mongodb is done through {@link MongoTemplate}.
 */
@Repository
public class MatchRepository {

  private MongoTemplate mongoTemplate;

  @Autowired
  public MatchRepository(final MongoTemplate mongoTemplate){
    this.mongoTemplate = mongoTemplate;
  }

  public List<Match> finaAllMatches(){
    return mongoTemplate.findAll(Match.class);
  }

  public List<Match> findMatchesOrdered(final int skip, final int limit){
    Query query = new Query();
    query.with(new Sort(Sort.Direction.DESC, "timestamp"))
         .skip(skip)
         .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }

  public List<Match> findRecentGamesForPlayer(final String name, final int skip, final int limit){
    Query query = new Query()
            .addCriteria(Criteria.where("teams.players.name").is(name))
            .with(new Sort(Sort.Direction.DESC, "timestamp"))
            .skip(skip)
            .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }

  public void save(final Match match){
    this.mongoTemplate.save(match);
  }
}
