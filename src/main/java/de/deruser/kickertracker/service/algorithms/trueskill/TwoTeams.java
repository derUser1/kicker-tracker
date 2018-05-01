package de.deruser.kickertracker.service.algorithms.trueskill;

import de.deruser.kickertracker.model.domain.Match;
import de.deruser.kickertracker.model.domain.PlayerInfo;
import de.deruser.kickertracker.model.domain.Team;
import de.deruser.kickertracker.service.PlayerService;
import de.deruser.kickertracker.service.StatsAlgorithm;
import de.gesundkrank.jskills.GameInfo;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.Player;
import de.gesundkrank.jskills.Rating;
import de.gesundkrank.jskills.trueskill.TwoTeamTrueSkillCalculator;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
public class TwoTeams implements StatsAlgorithm {

    private TwoTeamTrueSkillCalculator calculator;
    private PlayerService playerService;

    @Autowired
    public TwoTeams(final PlayerService playerService){
        this.calculator = new TwoTeamTrueSkillCalculator();
        this.playerService = playerService;
    }


    @Override
    public String getName() {
        return "trueSkillTwoTeams";
    }

    @Override
    public Match compute(Match match) {
        Map<String, PlayerInfo> playerInfoMap = match.getTeams().stream().flatMap(t -> t.getPlayers().stream())
            .map(p -> playerService.getPlayer(p.getName()))
            .collect(Collectors.toMap(PlayerInfo::getName, Function.identity()));

        Team teamOne = match.getTeams().get(0);
        Team teamTwo = match.getTeams().get(1);

        de.gesundkrank.jskills.Team team1 = mapToTeam(teamOne, playerInfoMap, match.getTimestamp());
        de.gesundkrank.jskills.Team team2 = mapToTeam(teamTwo, playerInfoMap, match.getTimestamp());

        GameInfo gameInfo = GameInfo.getDefaultGameInfo();

        Collection<ITeam> teams = de.gesundkrank.jskills.Team.concat(team1, team2);

        Map<IPlayer, Rating> iPlayerRatingMap = calculator.calculateNewRatings(gameInfo, teams, outcome(teamOne, teamTwo));
        Map<String, Rating> mappedPlayers = new HashMap<>();
        for(Map.Entry<IPlayer, Rating> playerRatingEntry : iPlayerRatingMap.entrySet()){
            Player<String> player = (Player<String>)playerRatingEntry.getKey();
            mappedPlayers.put(player.getId(), playerRatingEntry.getValue());
        }

        return match.toBuilder()
            .teams(asList(
                updateStats(teamOne, mappedPlayers, playerInfoMap),
                updateStats(teamTwo, mappedPlayers, playerInfoMap)))
            .build();
    }

    /**
     * Updates states of each player in a given team
     * @param team which player will be updated
     * @param ratingMap new player stats
     * @param playerInfoMap
     * @return team with updated player stats
     */
    private Team updateStats(Team team, Map<String, Rating> ratingMap, Map<String, PlayerInfo> playerInfoMap){
        return team.toBuilder()
            .players(team.getPlayers().stream()
                .map(p -> {
                    Rating rating = ratingMap.get(p.getName());
                    return p.toBuilder()
                        .glicko(rating.getMean())
                        .deviation(rating.getStandardDeviation())
                        .glickoChange(p.getGlicko()-playerInfoMap.get(p.getName()).getGameStats().getGlicko())
                        .volatility(0d)
                        .build();
                })
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Return an array of match outcome for the given teams. The position in the array is essential for the correct value. Lower value means
     * win.
     *
     */
    private int[] outcome(Team teamOne, Team teamTwo){
        if(teamOne.getScore() > teamTwo.getScore()){
            return new int[]{1, 2};
        }
        if(teamOne.getScore() < teamTwo.getScore()){
            return new int[]{2, 1};
        }
        return new int[]{1,1};
    }

    /**
     * Converts {@link Team} domain object to {@link de.gesundkrank.jskills.Team} domain object
     */
    private de.gesundkrank.jskills.Team mapToTeam(final Team team, final Map<String, PlayerInfo> playerInfoMap, Instant currentMatch){
        de.gesundkrank.jskills.Team result = new de.gesundkrank.jskills.Team();
        for(de.deruser.kickertracker.model.domain.Player player: team.getPlayers()){
            Player<String> tPlayer = new Player<>(player.getName());
            PlayerInfo playerInfo = playerInfoMap.get(player.getName());
            PlayerInfo.Stats stats = TrueSkillUtils.getStats(playerInfo, currentMatch);
            Rating rating = new Rating(stats.getGlicko(), stats.getDeviation());
            result.addPlayer(tPlayer, rating);
        }
        return result;
    }
}
