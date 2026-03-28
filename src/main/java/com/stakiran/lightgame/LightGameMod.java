package com.stakiran.lightgame;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightGameMod implements ModInitializer {
    public static final String MOD_ID = "lightgame100";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[LightGame100] Initializing...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LightGameCommand.register(dispatcher);
        });

        // Register the tick event for game phase management
        GameManager.registerEvents();

        LOGGER.info("[LightGame100] Ready!");
    }
}
