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

import java.util.ArrayList;
import java.util.List;

// 画面右のサイドバーにスコア（照らした数 / 合計 / 割合）を表示する。残り時間は TimerBar が担当。
public class ScoreboardManager {

    private static final String OBJECTIVE_NAME = "lightgame";

    // Sidebar lines are keyed by their text, so remember what was shown to remove stale lines
    private static final List<String> shownLines = new ArrayList<>();

    public static void setupSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        // Remove existing objective if present
        clearSidebar(server);

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
        updateSidebar(server, 0, 0);
    }

    public static void updateSidebar(MinecraftServer server, int score, int total) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (objective == null) return;

        double percent = total > 0 ? (score * 100.0 / total) : 0;

        // Scoreboard lines, top to bottom
        List<String> lines = List.of(
            "§fLight: §a" + score,
            "§fTotal: §b" + total,
            String.format("§fRate : §6%.1f%%", percent)
        );

        // Remove lines that are no longer shown, then (re)set the current ones.
        // Unchanged lines are left alone so the sidebar does not flicker.
        for (String old : shownLines) {
            if (!lines.contains(old)) {
                scoreboard.removeScore(ScoreHolder.fromName(old), objective);
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            // Higher number = higher on the sidebar
            ScoreAccess access = scoreboard.getOrCreateScore(ScoreHolder.fromName(lines.get(i)), objective);
            access.setScore(lines.size() - 1 - i);
        }
        shownLines.clear();
        shownLines.addAll(lines);
    }

    public static void clearSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective existing = scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }
        shownLines.clear();
    }
}
