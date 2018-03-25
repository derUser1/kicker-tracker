package de.deruser.kickertracker.service;


import de.deruser.kickertracker.model.domain.Match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

  public List<Match> findMatchesOrdered(final int limit){
    Query query = new Query();
    query.with(new Sort(Sort.Direction.DESC, "timestamp"))
         .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }

  public void save(final Match match){
    this.mongoTemplate.save(match);
  }
}
