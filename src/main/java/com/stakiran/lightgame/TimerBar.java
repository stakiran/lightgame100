package com.stakiran.lightgame;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

// 画面上部のボスバーに残り時間を表示する。
// 残り時間は全員共通なので、ボスバーは1本を全プレイヤーで共有する。
public class TimerBar {

    private static final int TICKS_PER_SECOND = 20;
    // Turn yellow / red when this many seconds are left in the game phase
    private static final int WARN_SECONDS = 3 * 60;
    private static final int DANGER_SECONDS = 60;

    private static ServerBossBar bar;

    public static void hide() {
        if (bar != null) {
            bar.clearPlayers();
            bar = null;
        }
    }

    public static void updatePreview(MinecraftServer server, int ticksRemaining, int totalTicks) {
        int secondsLeft = ceilSeconds(ticksRemaining);
        update(server, "§bプレビュー 残り " + secondsLeft + " 秒",
            BossBar.Color.BLUE, (float) ticksRemaining / totalTicks);
    }

    public static void updateGame(MinecraftServer server, int ticksRemaining, int totalTicks) {
        int secondsLeft = ceilSeconds(ticksRemaining);
        BossBar.Color color;
        String prefix;
        if (secondsLeft <= DANGER_SECONDS) {
            color = BossBar.Color.RED;
            prefix = "§c";
        } else if (secondsLeft <= WARN_SECONDS) {
            color = BossBar.Color.YELLOW;
            prefix = "§e";
        } else {
            color = BossBar.Color.GREEN;
            prefix = "§a";
        }
        update(server, prefix + "残り時間 " + formatTime(secondsLeft),
            color, (float) ticksRemaining / totalTicks);
    }

    private static void update(MinecraftServer server, String text, BossBar.Color color, float percent) {
        if (bar == null) {
            bar = new ServerBossBar(Text.literal(""), color, BossBar.Style.PROGRESS);
        }
        // Covers players who joined after setup or reconnected (new player object)
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }
        // setName/setColor/setPercent only send packets when the value changes
        bar.setName(Text.literal(text));
        bar.setColor(color);
        bar.setPercent(Math.max(0.0f, Math.min(1.0f, percent)));
    }

    // Round up so the display reads "1" during the final second rather than "0"
    private static int ceilSeconds(int ticks) {
        return (ticks + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;
    }

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
