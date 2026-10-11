package com.stakiran.lightgame;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitManager {

    public static void giveKit(ServerPlayerEntity player) {
        Registry<Enchantment> enchantmentRegistry = player.getEntityWorld()
            .getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        List<ItemStack> contents = new ArrayList<>(Collections.nCopies(27, ItemStack.EMPTY));

        // 種類ごとに分けた並び（特定のホットバー配置を前提にしない）
        // 左4列: ツール / 防具 / 移動・消耗品、6列目: 丸石・盾、右3列: 松明
        // Row 1: ネザライトの剣, ネザライトのツルハシ, ネザライトのショベル, ネザライトの斧, (空き), 丸石, 松明 x3
        contents.set(0, new ItemStack(Items.NETHERITE_SWORD));
        contents.set(1, createPickaxe(enchantmentRegistry));
        contents.set(2, createShovel(enchantmentRegistry));
        contents.set(3, createAxe(enchantmentRegistry));
        contents.set(5, new ItemStack(Items.COBBLESTONE, 64));

        // Row 2: 鉄レギンス, 鉄ブーツ, 鉄ヘルメット, 鉄チェストプレート, (空き), (空き), 盾, 松明 x2
        contents.set(9, new ItemStack(Items.IRON_LEGGINGS));
        contents.set(10, new ItemStack(Items.IRON_BOOTS));
        contents.set(11, new ItemStack(Items.IRON_HELMET));
        contents.set(12, new ItemStack(Items.IRON_CHESTPLATE));
        contents.set(15, new ItemStack(Items.SHIELD));

        // Row 3: エリトラ, ロケット花火, 水バケツ, 牛肉, (空き), (空き), 盾, 松明 x2
        contents.set(18, new ItemStack(Items.ELYTRA));
        contents.set(19, createFireworkRocket(64));
        contents.set(20, new ItemStack(Items.WATER_BUCKET));
        contents.set(21, new ItemStack(Items.COOKED_BEEF, 64));
        contents.set(24, new ItemStack(Items.SHIELD));

        for (int slot : new int[] {6, 7, 8, 16, 17, 25, 26}) {
            contents.set(slot, new ItemStack(Items.TORCH, 64));
        }

        ItemStack shulkerBox = new ItemStack(Items.SHULKER_BOX);
        shulkerBox.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(contents));

        // Reset inventory to the starting state (makes retrying easy)
        player.getInventory().clear();
        player.giveItemStack(shulkerBox);
    }

    private static ItemStack createPickaxe(Registry<Enchantment> registry) {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        addEnchantment(builder, registry, Enchantments.EFFICIENCY, 5);
        addEnchantment(builder, registry, Enchantments.AQUA_AFFINITY, 1);
        addEnchantment(builder, registry, Enchantments.UNBREAKING, 3);
        addEnchantment(builder, registry, Enchantments.MENDING, 1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        return stack;
    }

    private static ItemStack createAxe(Registry<Enchantment> registry) {
        ItemStack stack = new ItemStack(Items.NETHERITE_AXE);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        addEnchantment(builder, registry, Enchantments.EFFICIENCY, 5);
        addEnchantment(builder, registry, Enchantments.UNBREAKING, 3);
        addEnchantment(builder, registry, Enchantments.MENDING, 1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        return stack;
    }

    private static ItemStack createShovel(Registry<Enchantment> registry) {
        ItemStack stack = new ItemStack(Items.NETHERITE_SHOVEL);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        addEnchantment(builder, registry, Enchantments.EFFICIENCY, 5);
        addEnchantment(builder, registry, Enchantments.UNBREAKING, 3);
        addEnchantment(builder, registry, Enchantments.MENDING, 1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        return stack;
    }

    private static ItemStack createFireworkRocket(int count) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, count);
        stack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of()));
        return stack;
    }

    private static void addEnchantment(ItemEnchantmentsComponent.Builder builder,
            Registry<Enchantment> registry, RegistryKey<Enchantment> key, int level) {
        registry.getEntry(key.getValue()).ifPresent(entry -> builder.add(entry, level));
    }
}
