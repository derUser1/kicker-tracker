package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.domain.Team;
import de.deruser.kickertracker.model.view.MatchViewModel;
import de.deruser.kickertracker.model.view.PlayerViewModel;
import de.deruser.kickertracker.model.view.TeamViewModel;
import de.deruser.kickertracker.service.MatchService;
import de.deruser.kickertracker.service.PlayerService;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

  @ModelAttribute("module")
  public String module() {
    return "matchOverview";
  }

  @GetMapping("/matches")
  public String getMatches(final Model model) {
    List<String> players = new ArrayList<>();
    players.add(NOT_AVAILBLE);
    players.addAll(playerService.getAllPlayerNames());

    List<MatchViewModel> matchList = matchService.getRecentMatches(0,10).stream()
            .map(this::convertMatch).collect(toList());

    List<PlayerViewModel> playerList = playerService.getActivePlayers(10).stream()
            .sorted((p1, p2) -> Double.compare(p2.getGameStats().getGlicko(), p1.getGameStats().getGlicko()))
            .map(this::convertToPlayerViewModel)
            .collect(Collectors.toList());
    model.addAttribute("playerList", playerList);

    model.addAttribute("playerNames", players);
    model.addAttribute("matchList", matchList);
    model.addAttribute("matchViewModel", new MatchViewModel());
    return "matchesOverview";
  }

  /*--------------- API part -----------------*/
  //change url
  @PostMapping("/api/matches")
  public String addMatch(@ModelAttribute("matchViewModel") MatchViewModel matchViewModel){
    matchService.addMatch(convertMatchViewModel(matchViewModel));
    return "redirect:/matches";
  }

  @PostMapping("/api/matches/reprocess")
  public ResponseEntity<Void> reprocessStats(@ModelAttribute("matchViewModel") MatchViewModel matchViewModel){
    matchService.reprocessStats();
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/api/matches", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<Match> getAllMatches(){
    return matchService.getAllMatches();
  }

  @PostMapping(value = "/api/matches", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void addMatches(@RequestBody final List<Match> matches){
    matches.forEach(matchService::addMatch);
  }

  /*--------------- Helper methods -----------------*/
  private PlayerViewModel convertToPlayerViewModel(final PlayerInfo playerInfo){
    PlayerViewModel playerViewModel = new PlayerViewModel();
    playerViewModel.setName(playerInfo.getName());
    playerViewModel.setGlicko(playerInfo.getGameStats().getGlicko());
    return playerViewModel;
  }

  private Match convertMatchViewModel(final MatchViewModel matchViewModel){
    Principal principal = SecurityContextHolder.getContext().getAuthentication();
    List<Team> teams = Arrays.asList(convertTeamViewModel(matchViewModel.getTeamOne()),
        convertTeamViewModel(matchViewModel.getTeamTwo()));
    return Match.builder()
        .timestamp(matchViewModel.getTimestamp() == null ? Instant.now() : matchViewModel.getTimestamp())
        .createdBy(principal.getName())
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

  private MatchViewModel convertMatch(final Match match){
      MatchViewModel matchViewModel = new MatchViewModel();
      matchViewModel.setTeamOne(convertTeam(match.getTeams().get(0)));
      matchViewModel.setTeamTwo(convertTeam(match.getTeams().get(1)));
      return matchViewModel;
  }

  private TeamViewModel convertTeam(final Team team){
      TeamViewModel teamViewModel = new TeamViewModel();
      teamViewModel.setPlayers(team.getPlayers().stream().map(Player::getName).collect(toList()));
      teamViewModel.setScore(team.getScore());
      return teamViewModel;
  }
}
