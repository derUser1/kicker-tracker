package de.deruser.kickertracker.controller;

import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.service.PlayerService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlayerController {

  private PlayerService playerService;

  @Autowired
  public PlayerController(final PlayerService playerService){
    this.playerService = playerService;
  }

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
}
