package Project3_6813256;

import java.time.LocalDateTime;

/** One completed play session. */
public record LeaderboardEntry(
        LocalDateTime playedAt,
        String playerName,
        Difficulty difficulty,
        int floorsClimbed,
        long durationSeconds) {

    public LeaderboardEntry withPlayerName(String name) {
        return new LeaderboardEntry(playedAt, name, difficulty, floorsClimbed, durationSeconds);
    }
}
