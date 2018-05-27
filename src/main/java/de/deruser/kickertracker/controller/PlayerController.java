package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.Player;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.domain.Team;
import de.deruser.kickertracker.model.view.PlayerViewModel;
import de.deruser.kickertracker.service.MatchService;
import de.deruser.kickertracker.service.PlayerService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.joining;

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

  @GetMapping(value = "/players/current", produces = MediaType.TEXT_HTML_VALUE)
  public String getPlayer(Principal principal){
    return "redirect:/players/" + principal.getName();
  }

  @GetMapping(value = "/players/{name}", produces = MediaType.TEXT_HTML_VALUE)
  public String getPlayer(@PathVariable("name") String name, Model model, HttpServletRequest request){
    if(!name.equals(request.getUserPrincipal().getName()) && !request.isUserInRole("ROLE_ADMIN")){
      throw new IllegalArgumentException("Logged in user differs from url one");
    }

    //TODO: add a new query for graph, change this back to only 10 results
    List<Match> recentMatches = matchService.getRecentMatches(name, 0, 100);

    List<Map<String, String>> recentGameList = new ArrayList<>();

    for(Match match : recentMatches) {
      Team oppositeTeam = null;
      Team playersTeam = null;
      Player player = null;
      for(Team team : match.getTeams()) {
        Optional<Player> playerOptional = team.getPlayers().stream()
                .filter(p -> p.getName().equals(name)).findAny();

        if (playerOptional.isPresent()) {
          playersTeam = team;
          player = playerOptional.get();
        }
        else {
          oppositeTeam = team;
        }
      }

      if(player != null && oppositeTeam != null) {
        Map<String, String> playerData = new HashMap<>();
        playerData.put("timestamp", match.getTimestamp().toString());
        playerData.put("glicko", String.valueOf(player.getGlicko()));
        playerData.put("result", formatResult(playersTeam.getScore(), oppositeTeam.getScore()));
        playerData.put("won", String.valueOf(playersTeam.getScore() > oppositeTeam.getScore()));
        playerData.put("deviation", String.valueOf(player.getDeviation()));
        playerData.put("glickoChange", String.valueOf(player.getGlickoChange()));
        recentGameList.add(playerData);
      }
    }
    model.addAttribute("recentGameList", recentGameList);

    PlayerInfo player = playerService.getPlayer(name);
    if(player != null) {
      model.addAttribute("name", player.getName());
      model.addAttribute("gameCount", player.getGameStats().getMatchCount());
      model.addAttribute("winCount", player.getGameStats().getWinCount());
      model.addAttribute("lossCount", player.getGameStats().getLossCount());

      model.addAttribute("dataLables", IntStream.rangeClosed(1, recentGameList.size())
          .mapToObj(String::valueOf)
          .collect(Collectors.toList()));

      model.addAttribute("data", recentGameList.stream().map(m -> m.get("glicko")).collect(Collectors.toList()));
    }
    return "playerOverview";
  }

  private String formatResult(int playersTeam, int oppositeTeam){
    return String.format("%s : %s", playersTeam, oppositeTeam);
  }


  /*-------------- API part ----------------*/
  @GetMapping(value = "/api/players", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<PlayerInfo> getAllPlayers(){
    return playerService.getAllPlayers();
  }

  //TODO change to RequestBody / JSON
  @Secured("ADMIN")
  @PostMapping(value = "/api/players", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<?> addPlayer(@RequestParam("name") String name){
    return ResponseEntity.ok(playerService.addPlayer(name));
  }

  private PlayerViewModel convertToPlayerViewModel(final PlayerInfo playerInfo){
    PlayerViewModel playerViewModel = new PlayerViewModel();
    playerViewModel.setName(playerInfo.getName());
    playerViewModel.setGlicko((int)playerInfo.getGameStats().getGlicko());
    return playerViewModel;
  }
}
