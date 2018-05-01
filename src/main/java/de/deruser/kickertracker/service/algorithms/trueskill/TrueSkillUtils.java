package de.deruser.kickertracker.service.algorithms.trueskill;

import de.deruser.kickertracker.model.domain.PlayerInfo;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;

final class TrueSkillUtils {

    static double getDeviation(PlayerInfo.Stats playerInfo, Instant lastMatch, Instant currentMatch){
        long daysBetween = DAYS.between(lastMatch, currentMatch);
        return Math.min(playerInfo.getDeviation() + 0.05d * daysBetween, 8.33333333d);
    }

    static PlayerInfo.Stats getStats(PlayerInfo playerInfo, Instant currentMatch){
        final Instant lastMatch = playerInfo.getLastMatch() == null ? currentMatch : playerInfo.getLastMatch();
        return playerInfo.getGameStats().toBuilder()
                .deviation(getDeviation(playerInfo.getGameStats(), lastMatch, currentMatch))
                .build();
    }
}
