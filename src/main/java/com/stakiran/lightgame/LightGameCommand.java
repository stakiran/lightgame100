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
                .executes(LightGameCommand::executeSetup)
        );
    }

    // ゲーム中・終了後でも呼べる（リセットを兼ねる）
    private static int executeSetup(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendFeedback(() -> Text.literal("§c[LightGame] プレイヤーがコマンドを実行してください。"), false);
            return 0;
        }

        GameManager.setup(source);
        return 1;
    }
}
