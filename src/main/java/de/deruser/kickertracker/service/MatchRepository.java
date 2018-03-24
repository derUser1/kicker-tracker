package de.deruser.kickertracker.service;


import de.deruser.kickertracker.model.domain.Match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

  public void save(final Match match){
    this.mongoTemplate.save(match);
  }
}
