package com.stakiran.lightgame;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class LightGameCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("lightgame")
                .then(CommandManager.literal("setup")
                    .executes(LightGameCommand::executeSetup))
                .then(CommandManager.literal("start")
                    .executes(LightGameCommand::executeStart))
                .then(CommandManager.literal("stop")
                    .executes(LightGameCommand::executeStop))
                .then(CommandManager.literal("score")
                    .executes(LightGameCommand::executeScore))
                .then(CommandManager.literal("kit")
                    .executes(LightGameCommand::executeKit))
        );
    }

    private static int executeSetup(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (GameManager.isRunning()) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] ゲームが実行中です。先に /lightgame stop で停止してください。"), false);
            return 0;
        }

        GameManager.setup(source);
        return 1;
    }

    private static int executeStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!GameManager.isSetupDone()) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] 先に /lightgame setup を実行してください。"), false);
            return 0;
        }

        if (GameManager.isRunning()) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] ゲームは既に実行中です。"), false);
            return 0;
        }

        GameManager.start(source);
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!GameManager.isRunning()) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] ゲームは実行中ではありません。"), false);
            return 0;
        }

        GameManager.stop(source, false);
        return 1;
    }

    private static int executeScore(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        GameManager.showScore(source);
        return 1;
    }

    private static int executeKit(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] プレイヤーがコマンドを実行してください。"), false);
            return 0;
        }

        KitManager.giveKit(player);
        source.sendFeedback(() -> Text.literal("§a[LightGame] キットを付与しました！"), false);
        return 1;
    }
}
