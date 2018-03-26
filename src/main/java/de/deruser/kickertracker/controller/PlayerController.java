package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.view.PlayerViewModel;
import de.deruser.kickertracker.service.MatchService;
import de.deruser.kickertracker.service.PlayerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlayerController {

  private PlayerService playerService;
  private MatchService matchService;

  @Autowired
  public PlayerController(final PlayerService playerService, final MatchService matchService){
    this.playerService = playerService;
    this.matchService = matchService;
  }

  @ModelAttribute("module")
  public String module() {
    return "playerOverview";
  }


  @GetMapping(value = "/players/{name}", produces = MediaType.TEXT_HTML_VALUE)
  public String getPlayer(@PathVariable("name") String name, Model model){
    List<Match> recentMatches = matchService.getRecentMatches(name, 0, 10);

    List<Map<String, String>> recentGameList = new ArrayList<>();
    for(Match match : recentMatches) {
      Optional<Player> player = match.getTeams().stream()
          .flatMap(team -> team.getPlayers().stream())
          .filter(p -> p.getName().equals(name))
          .findAny();
      if (player.isPresent()) {
        Map<String, String> playerData = new HashMap<>();
        playerData.put("timestamp", match.getTimestamp().toString());
        playerData.put("glicko", String.valueOf(player.get().getGlicko()));
        playerData.put("deviation", String.valueOf(player.get().getDeviation()));
        playerData.put("glickoChange", String.valueOf(player.get().getGlickoChange()));

        recentGameList.add(playerData);
      }
    }
    model.addAttribute("recentGameList", recentGameList);
    return "playerOverview";
  }



  /*-------------- API part ----------------*/
  @GetMapping(value = "/api/players", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<PlayerInfo> getAllPlayers(){
    return playerService.getAllPlayers();
  }

  //TODO change to RequestBody / JSON
  @PostMapping(value = "/api/players", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<?> addPlayer(@RequestParam("name") String name){
    return ResponseEntity.ok(playerService.addPlayer(name));
  }


  private PlayerViewModel convertToPlayerViewModel(final PlayerInfo playerInfo){
    PlayerViewModel playerViewModel = new PlayerViewModel();
    playerViewModel.setName(playerInfo.getName());
    playerViewModel.setGlicko(playerInfo.getGlicko());
    return playerViewModel;
  }
}
