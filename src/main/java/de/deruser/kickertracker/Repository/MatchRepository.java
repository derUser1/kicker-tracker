package de.deruser.kickertracker.repository;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import scala.collection.parallel.ParIterableLike;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.UnwindOperation.newUnwind;

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

  public List<Match> findAllMatches(){
    return mongoTemplate.findAll(Match.class);
  }

  public List<Match> findMatchesOrdered(final int skip, final int limit){
    Query query = new Query();
    query.with(new Sort(Sort.Direction.DESC, "timestamp"))
        .skip(skip)
        .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }

  public List<Match> findRecentMatchesForPlayer(final String name, final int skip, final int limit){
    Query query = new Query()
        .addCriteria(Criteria.where("teams.players.name").is(name))
        .with(new Sort(Sort.Direction.DESC, "timestamp"))
        .skip(skip)
        .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }

  public List<Match> findRecentMatchesForPlayerAggregated(final String name, final int limit){
    Aggregation aggregation = Aggregation.newAggregation(
        match(Criteria.where("teams.players.name").is(name)),
        sort(by("timestamp").descending()),
        new  LimitOperation(limit),
        newUnwind().path("$teams").noArrayIndex().skipNullAndEmptyArrays(),
        newUnwind().path("$teams.players").noArrayIndex().skipNullAndEmptyArrays(),
        match(Criteria.where("teams.players.name").is(name)),
        new ProjectionOperation(Fields.fields("teams.players.glicko"))
    );

    AggregationResults<Match> results = mongoTemplate.aggregate(aggregation, "match", Match.class);
    return results.getMappedResults();
  }

  public List<Match> findMatchForPlayerUntil(final String name, final int limit, final Instant timestamp){
    Query query = new Query()
        .addCriteria(Criteria.where("teams.players.name").is(name))
        .addCriteria(Criteria.where("timestamp").lt(timestamp))
        .with(new Sort(Sort.Direction.DESC, "timestamp"))
        .limit(limit);
    return mongoTemplate.find(query, Match.class);
  }
  public void save(final Match match){
    this.mongoTemplate.save(match);
  }
}
