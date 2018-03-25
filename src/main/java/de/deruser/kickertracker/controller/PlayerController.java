package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.view.PlayerViewModel;
import de.deruser.kickertracker.service.PlayerService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PlayerController {

  private PlayerService playerService;

  @Autowired
  public PlayerController(final PlayerService playerService){
    this.playerService = playerService;
  }

  @ModelAttribute("module")
  public String module() {
    return "playerOverview";
  }

  @GetMapping(value = "/players", produces = MediaType.TEXT_HTML_VALUE)
  public String getPlayers(final Model model){
    List<PlayerViewModel> playerList = playerService.getAllPlayers().stream()
            .sorted(Comparator.comparingInt(PlayerInfo::getGlicko).reversed())
            .map(this::convertToPlayerViewModel)
            .collect(Collectors.toList());
    model.addAttribute("playerList", playerList);
    return "playerOverview";
  }

//  @GetMapping(value = "/players/{name}", produces = MediaType.TEXT_HTML_VALUE)
//  public String getPlayer(@PathVariable("name") String name){
//    return "playerOverview";
//  }



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
