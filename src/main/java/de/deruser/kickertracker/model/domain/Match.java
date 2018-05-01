package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@Document(collection = "matches")
public class Match {

    private ObjectId id;
    private Instant timestamp;
    private String userCreated;
    private List<Team> teams;

}
