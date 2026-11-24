package Project3_6813256;

import java.time.LocalDateTime;

/** One completed play session. */
public record LeaderboardEntry(      // record --> adaptor for data storing class  have build for (constructor, getter, toString, equals, ecs.)
        LocalDateTime playedAt,
        String playerName,
        Difficulty difficulty,
        int floorsClimbed,
        long durationSeconds) {

    public LeaderboardEntry withPlayerName(String name) {    // method for return the entire object
        return new LeaderboardEntry(playedAt, name, difficulty, floorsClimbed, durationSeconds);
    }
}
