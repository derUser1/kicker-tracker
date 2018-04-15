package de.deruser.kickertracker.service.algorithms.glicko;

import de.deruser.kickertracker.model.domain.PlayerInfo;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;

final class GlickoUtils{

    static int getDeviation(PlayerInfo.Stats playerInfo, Instant lastMatch, Instant currentMatch){
        long daysBetween = DAYS.between(lastMatch, currentMatch);
        long deviation = playerInfo.getDeviation() + Math.abs(daysBetween);
        return (int) deviation;
    }

    static PlayerInfo.Stats getStats(PlayerInfo playerInfo, Instant currentMatch){
        final Instant lastMatch = playerInfo.getLastMatch() == null ? currentMatch : playerInfo.getLastMatch();
        return playerInfo.getGameStats().toBuilder()
                .deviation(getDeviation(playerInfo.getGameStats(), lastMatch, currentMatch))
                .build();
    }
}
