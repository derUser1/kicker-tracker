package de.deruser.kickertracker.service;

import de.deruser.kickertracker.model.domain.Match;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StatsComputationService {

  private final String algorithmName;
  private final Map<String, StatsAlgorithm> statsAlgorithmMap;

  @Autowired
  public StatsComputationService(
      @Value("${stats.computation.algorithm:glicko}") final String algorithmName,
      final List<StatsAlgorithm> algorithms){
    this.algorithmName = algorithmName;
    this.statsAlgorithmMap = algorithms.stream()
        .collect(Collectors.toMap(StatsAlgorithm::getName, Function.identity()));

    if(statsAlgorithmMap.get(algorithmName) == null){
      throw new IllegalArgumentException("Unknown stats computation algorithms: " + algorithmName);
    }
  }

  public Match compute(final Match match){
    final StatsAlgorithm statsAlgorithm = statsAlgorithmMap.get(algorithmName);
    return statsAlgorithm.compute(match);
  }

}
