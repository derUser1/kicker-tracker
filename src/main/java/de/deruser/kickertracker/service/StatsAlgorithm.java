package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.Match;

public interface StatsAlgorithm {

  /**
   * Name of the algorithm
   * @return name
   */
  String getName();


  /**
   * Computes new stats for each player of the given match
   * @param match Match information
   * @return Update player stats
   */
  Match compute(final Match match);
}
