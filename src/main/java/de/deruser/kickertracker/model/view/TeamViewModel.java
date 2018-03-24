package de.deruser.kickertracker.model.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TeamViewModel {

  private int score;
  private List<String> players;
}
