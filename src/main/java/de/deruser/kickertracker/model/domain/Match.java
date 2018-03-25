package de.deruser.kickertracker.model.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@Document(collection = "matches")
public class Match {

    private Instant timestamp;
    private List<Team> teams;

}
