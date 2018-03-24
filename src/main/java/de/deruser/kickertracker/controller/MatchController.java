package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.Team;
import de.deruser.kickertracker.model.view.MatchViewModel;
import de.deruser.kickertracker.model.view.TeamViewModel;
import de.deruser.kickertracker.service.MatchService;
import de.deruser.kickertracker.service.PlayerService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static java.util.stream.Collectors.toList;

@Controller
public class MatchController {

  private final static String NOT_AVAILBLE = "n/a";

  private MatchService matchService;
  private PlayerService playerService;

  @Autowired
  public MatchController(final MatchService matchService, final PlayerService playerService){
    this.matchService = matchService;
    this.playerService = playerService;
  }

  @GetMapping("/matches")
  public String getMatches(Model model) {
    List<String> players = new ArrayList<>();
    players.add(NOT_AVAILBLE);
    players.addAll(playerService.getAllPlayerNames());
    model.addAttribute("playerNames", players);
    model.addAttribute("matchViewModel", new MatchViewModel());
    return "matchesOverview";
  }


  @PostMapping("/api/matches")
  public String addMatch(@ModelAttribute("matchViewModel") MatchViewModel matchViewModel){
    matchService.addMatch(convertMatchViewModel(matchViewModel));
    return "redirect:/matches";
  }


  private Match convertMatchViewModel(final MatchViewModel matchViewModel){
    List<Team> teams = Arrays.asList(convertTeamViewModel(matchViewModel.getTeamOne()),
        convertTeamViewModel(matchViewModel.getTeamTwo()));
    return Match.builder()
        .timestamp(matchViewModel.getTimestamp() == null ? Instant.now() : matchViewModel.getTimestamp())
        .teams(teams)
        .build();
  }

  private Team convertTeamViewModel(final TeamViewModel teamViewModel){
    List<Player> players = teamViewModel.getPlayers()
        .stream()
        .filter(name -> !name.equals(NOT_AVAILBLE))
        .map(p -> Player.builder().name(p).build())
        .collect(toList());
    return Team.builder()
        .score(teamViewModel.getScore())
        .players(players)
        .build();
  }
}
