package com.stakiran.lightgame;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
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
}
