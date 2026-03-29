package com.stakiran.lightgame;

import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
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

        // Create new objective with blank number format to hide red numbers
        ScoreboardObjective objective = scoreboard.addObjective(
            OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§lLight Game 100"),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            BlankNumberFormat.INSTANCE
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
        setScore(scoreboard, objective, "§fTime : §e" + timeStr, 3);
        setScore(scoreboard, objective, "§fLight: §a" + score, 2);
        setScore(scoreboard, objective, "§fTotal: §b" + total, 1);
        setScore(scoreboard, objective, String.format("§fRate : §6%.1f%%", percent), 0);
    }

    public static void clearSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective existing = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }
    }

    private static void clearScores(Scoreboard scoreboard, ScoreboardObjective objective) {
        scoreboard.removeObjective(objective);
        ScoreboardObjective newObj = scoreboard.addObjective(
            OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§lLight Game 100"),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            BlankNumberFormat.INSTANCE
        );
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, newObj);
    }

    private static void setScore(Scoreboard scoreboard, ScoreboardObjective objective, String name, int value) {
        ScoreAccess access = scoreboard.getOrCreateScore(ScoreHolder.fromName(name), objective);
        access.setScore(value);
    }
}
