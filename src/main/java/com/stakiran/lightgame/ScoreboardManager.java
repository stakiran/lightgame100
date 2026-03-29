package com.stakiran.lightgame;

import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class ScoreboardManager {

    private static final String OBJECTIVE_NAME = "lightgame";

    public static void setupSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        // Remove existing objective if present
        ScoreboardObjective existing = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }

        // Create new objective
        ScoreboardObjective objective = scoreboard.addObjective(
            OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§l🔥 Light Game 100"),
            ScoreboardCriterion.RenderType.INTEGER,
            true,  // displayAutoUpdate
            null   // numberFormat
        );

        // Set as sidebar display
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);

        // Initial values
        updateSidebar(server, 0, 0, 10, 0);
    }

    public static void updateSidebar(MinecraftServer server, int score, int total, int minutes, int seconds) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (objective == null) return;

        // Clear old scores (recreates the objective)
        clearScores(scoreboard, objective);
        objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (objective == null) return;

        double percent = total > 0 ? (score * 100.0 / total) : 0;
        String timeStr = String.format("%d:%02d", minutes, seconds);

        // Scoreboard lines (higher number = higher on sidebar)
        setScore(scoreboard, objective, "§7─────────", 6);
        setScore(scoreboard, objective, "§f⏱ 残り時間: §e" + timeStr, 5);
        setScore(scoreboard, objective, "§7", 4);
        setScore(scoreboard, objective, "§f💡 照らした: §a" + score, 3);
        setScore(scoreboard, objective, "§f📊 対象: §b" + total, 2);
        setScore(scoreboard, objective, String.format("§f📈 達成率: §6%.1f%%", percent), 1);
        setScore(scoreboard, objective, "§7──────────", 0);
    }

    public static void clearSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective existing = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }
    }

    private static void clearScores(Scoreboard scoreboard, ScoreboardObjective objective) {
        // Remove all scores for this objective by removing and re-adding
        // This is the simplest approach for Fabric
        scoreboard.removeObjective(objective);
        ScoreboardObjective newObj = scoreboard.addObjective(
            OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§l🔥 Light Game 100"),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        );
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, newObj);
    }

    private static void setScore(Scoreboard scoreboard, ScoreboardObjective objective, String name, int value) {
        // In 1.21.4, we use getOrCreateScore with a string holder
        ScoreAccess access = scoreboard.getOrCreateScore(ScoreHolder.fromName(name), objective);
        access.setScore(value);
    }
}
