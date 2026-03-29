package com.stakiran.lightgame;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.LightType;
import net.minecraft.world.border.WorldBorder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GameManager {

    // Game phases
    public enum Phase {
        IDLE,       // No game running
        PREVIEW,    // 30-second spectator preview
        GAME,       // 10-minute game phase
        ENDED       // Game ended, showing results
    }

    public enum StopReason {
        MANUAL, TIMEOUT, DEATH
    }

    private static Phase currentPhase = Phase.IDLE;
    private static boolean setupDone = false;

    // Setup data
    private static BlockPos centerPos;
    private static ServerWorld gameWorld;

    // Timer
    private static int ticksRemaining;
    private static final int PREVIEW_SECONDS = 30;
    private static final int GAME_SECONDS = 600; // 10 minutes
    private static final int TICKS_PER_SECOND = 20;

    // Score tracking
    private static final int SCORE_UPDATE_INTERVAL = 40; // Update every 2 seconds
    private static int ticksSinceLastScoreUpdate;

    // Snapshot of air blocks at game start (y < 64)
    private static final Set<Long> snapshotAirBlocks = new HashSet<>();

    // Player's last position (saved each tick during GAME phase)
    private static BlockPos lastPlayerPos;


    // World border size
    private static final int BORDER_SIZE = 100;

    // Y threshold
    private static final int Y_THRESHOLD = 64;

    public static boolean isRunning() {
        return currentPhase == Phase.PREVIEW || currentPhase == Phase.GAME;
    }

    public static boolean isSetupDone() {
        return setupDone;
    }

    // ========== SETUP ==========

    public static void setup(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] プレイヤーがコマンドを実行してください。"), false);
            return;
        }

        gameWorld = (ServerWorld) player.getWorld();
        centerPos = player.getBlockPos();

        // Set world border
        WorldBorder border = gameWorld.getWorldBorder();
        border.setCenter(centerPos.getX() + 0.5, centerPos.getZ() + 0.5);
        border.setSize(BORDER_SIZE);

        // Teleport player to center
        player.teleport(gameWorld, centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5, Collections.emptySet(), player.getYaw(), player.getPitch(), false);

        setupDone = true;
        currentPhase = Phase.IDLE;

        source.sendFeedback(() -> Text.literal("§a[LightGame] セットアップ完了！"), false);
        source.sendFeedback(() -> Text.literal("§a  中心座標: " + centerPos.getX() + ", " + centerPos.getY() + ", " + centerPos.getZ()), false);
        source.sendFeedback(() -> Text.literal("§a  ワールドボーダー: " + BORDER_SIZE + "ブロック"), false);
        source.sendFeedback(() -> Text.literal("§e  クリエイティブモードでアイテムを準備してから /lightgame start を実行してください。"), false);
    }

    // ========== START ==========

    public static void start(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return;

        // Enter preview phase (spectator mode)
        currentPhase = Phase.PREVIEW;
        ticksRemaining = PREVIEW_SECONDS * TICKS_PER_SECOND;

        player.changeGameMode(GameMode.SPECTATOR);

        source.sendFeedback(() -> Text.literal("§b[LightGame] プレビューフェーズ開始！ " + PREVIEW_SECONDS + "秒間スペクテイターモードで地下を確認できます。"), false);
    }

    // ========== SNAPSHOT ==========

    private static void takeSnapshot() {
        snapshotAirBlocks.clear();

        WorldBorder border = gameWorld.getWorldBorder();
        int minX = (int) Math.floor(border.getBoundWest());
        int maxX = (int) Math.floor(border.getBoundEast());
        int minZ = (int) Math.floor(border.getBoundNorth());
        int maxZ = (int) Math.floor(border.getBoundSouth());

        // y = -64 (world bottom in 1.21) to y = 63 (below Y_THRESHOLD)
        int minY = gameWorld.getBottomY();
        int maxY = Y_THRESHOLD - 1;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = gameWorld.getBlockState(pos);

                    // Record dark air blocks (air, cave_air, void_air) with no block light
                    if (state.isAir() && gameWorld.getLightLevel(LightType.BLOCK, pos) == 0) {
                        snapshotAirBlocks.add(pos.asLong());
                    }
                }
            }
        }
    }

    // ========== SCORE CALCULATION ==========

    public static int calculateScore() {
        if (gameWorld == null || snapshotAirBlocks.isEmpty()) return 0;

        int litCount = 0;
        for (long posLong : snapshotAirBlocks) {
            BlockPos pos = BlockPos.fromLong(posLong);
            int blockLight = gameWorld.getLightLevel(LightType.BLOCK, pos);
            if (blockLight >= 1) {
                litCount++;
            }
        }
        return litCount;
    }

    public static void showScore(ServerCommandSource source) {
        int total = snapshotAirBlocks.size();
        int lit = calculateScore();
        double percent = total > 0 ? (lit * 100.0 / total) : 0;

        source.sendFeedback(() -> Text.literal(String.format(
            "§6[LightGame] スコア: %d / %d (%.1f%%)", lit, total, percent
        )), false);
    }

    // ========== BEACON MARKER ==========

    private static void placeBeaconMarker(BlockPos playerPos) {
        // Place 3x3 iron block base one block below the player
        int baseY = playerPos.getY() - 1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                gameWorld.setBlockState(new BlockPos(playerPos.getX() + dx, baseY, playerPos.getZ() + dz),
                    Blocks.IRON_BLOCK.getDefaultState());
            }
        }

        // Place beacon on top of the base
        gameWorld.setBlockState(new BlockPos(playerPos.getX(), baseY + 1, playerPos.getZ()),
            Blocks.BEACON.getDefaultState());

        // Clear blocks above the beacon up to world height to let beam through
        int worldTop = gameWorld.getTopYInclusive();
        for (int y = baseY + 2; y <= worldTop; y++) {
            BlockPos pos = new BlockPos(playerPos.getX(), y, playerPos.getZ());
            if (!gameWorld.getBlockState(pos).isAir()) {
                gameWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    // ========== STOP ==========

    public static void stop(ServerCommandSource source, StopReason reason) {
        currentPhase = Phase.ENDED;

        // Show final score
        int total = snapshotAirBlocks.size();
        int lit = calculateScore();
        double percent = total > 0 ? (lit * 100.0 / total) : 0;

        MinecraftServer server = source.getServer();

        switch (reason) {
            case TIMEOUT -> server.getPlayerManager().broadcast(
                Text.literal("§c§l[LightGame] 時間切れ！ ゲーム終了！"), false);
            case DEATH -> server.getPlayerManager().broadcast(
                Text.literal("§c§l[LightGame] プレイヤーが死亡！ ゲーム終了！"), false);
            case MANUAL -> server.getPlayerManager().broadcast(
                Text.literal("§c§l[LightGame] ゲームを手動停止しました。"), false);
        }

        server.getPlayerManager().broadcast(
            Text.literal(String.format("§6§l[LightGame] 最終スコア: %d / %d (%.1f%%)", lit, total, percent)), false);

        // Place beacon marker at player's last position
        if (lastPlayerPos != null) {
            placeBeaconMarker(lastPlayerPos);
            server.getPlayerManager().broadcast(
                Text.literal(String.format("§e[LightGame] 最終位置: %d, %d, %d",
                    lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ())), false);
        }

        // Switch to spectator at last position for review
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(GameMode.SPECTATOR);
            BlockPos tp = lastPlayerPos != null ? lastPlayerPos : centerPos;
            player.teleport(gameWorld, tp.getX() + 0.5, tp.getY(), tp.getZ() + 0.5,
                Collections.emptySet(), player.getYaw(), player.getPitch(), false);
        }

        // Clear the sidebar scoreboard
        ScoreboardManager.clearSidebar(server);

        currentPhase = Phase.IDLE;
    }

    // ========== TICK EVENT ==========

    public static void resetState() {
        currentPhase = Phase.IDLE;
        setupDone = false;
        centerPos = null;
        gameWorld = null;
        ticksRemaining = 0;
        ticksSinceLastScoreUpdate = 0;
        snapshotAirBlocks.clear();
        lastPlayerPos = null;
    }

    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(GameManager::onTick);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            resetState();
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (currentPhase == Phase.GAME && entity instanceof ServerPlayerEntity) {
                MinecraftServer server = entity.getServer();
                if (server != null) {
                    stop(server.getCommandSource(), StopReason.DEATH);
                }
            }
        });
    }

    private static void onTick(MinecraftServer server) {
        if (currentPhase == Phase.IDLE || currentPhase == Phase.ENDED) return;

        ticksRemaining--;

        if (currentPhase == Phase.PREVIEW) {
            onPreviewTick(server);
        } else if (currentPhase == Phase.GAME) {
            onGameTick(server);
        }
    }

    private static void onPreviewTick(MinecraftServer server) {
        // Show countdown every 10 seconds, and at 5, 3, 2, 1
        int secondsLeft = ticksRemaining / TICKS_PER_SECOND;
        if (ticksRemaining % TICKS_PER_SECOND == 0) {
            if (secondsLeft % 10 == 0 || secondsLeft <= 5) {
                if (secondsLeft > 0) {
                    server.getPlayerManager().broadcast(
                        Text.literal("§b[プレビュー] 残り " + secondsLeft + " 秒"), false);
                }
            }
        }

        if (ticksRemaining <= 0) {
            // Transition to game phase
            transitionToGamePhase(server);
        }
    }

    private static void transitionToGamePhase(MinecraftServer server) {
        // Take snapshot after preview so loaded chunks include natural light sources
        server.getPlayerManager().broadcast(
            Text.literal("§e[LightGame] 地下の空気ブロックをスキャン中..."), false);
        takeSnapshot();
        server.getPlayerManager().broadcast(
            Text.literal("§a[LightGame] スキャン完了！ 対象ブロック数: " + snapshotAirBlocks.size()), false);

        currentPhase = Phase.GAME;
        ticksRemaining = GAME_SECONDS * TICKS_PER_SECOND;
        ticksSinceLastScoreUpdate = 0;

        // Teleport back to center and switch to survival
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.teleport(gameWorld, centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5,
                Collections.emptySet(), player.getYaw(), player.getPitch(), false);
            player.changeGameMode(GameMode.SURVIVAL);
        }

        // Setup scoreboard sidebar
        ScoreboardManager.setupSidebar(server);

        server.getPlayerManager().broadcast(
            Text.literal("§a§l[LightGame] ゲーム開始！ " + GAME_SECONDS / 60 + "分以内にできるだけ地下を照らしましょう！"), false);
    }

    private static void onGameTick(MinecraftServer server) {
        // Save player position each tick (so we have it even on death)
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            lastPlayerPos = player.getBlockPos();
        }

        // Show time countdown
        int secondsLeft = ticksRemaining / TICKS_PER_SECOND;
        if (ticksRemaining % TICKS_PER_SECOND == 0) {
            // Every minute
            if (secondsLeft > 0 && secondsLeft % 60 == 0) {
                int minutesLeft = secondsLeft / 60;
                server.getPlayerManager().broadcast(
                    Text.literal("§e[LightGame] 残り " + minutesLeft + " 分"), false);
            }
            // Last 10 seconds
            if (secondsLeft > 0 && secondsLeft <= 10) {
                server.getPlayerManager().broadcast(
                    Text.literal("§c[LightGame] 残り " + secondsLeft + " 秒！"), false);
            }
        }

        // Update scoreboard periodically
        ticksSinceLastScoreUpdate++;
        if (ticksSinceLastScoreUpdate >= SCORE_UPDATE_INTERVAL) {
            ticksSinceLastScoreUpdate = 0;
            int score = calculateScore();
            int total = snapshotAirBlocks.size();
            int minutes = secondsLeft / 60;
            int secs = secondsLeft % 60;
            ScoreboardManager.updateSidebar(server, score, total, minutes, secs);
        }

        if (ticksRemaining <= 0) {
            // Game over
            ServerCommandSource source = server.getCommandSource();
            stop(source, StopReason.TIMEOUT);
        }
    }
}
