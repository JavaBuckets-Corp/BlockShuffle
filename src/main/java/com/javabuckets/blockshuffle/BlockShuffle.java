package com.javabuckets.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public final class BlockShuffle extends JavaPlugin {

    public static boolean isRunning = false;

    public static int defaultTimer = 5 * 60; // minutes * seconds_converter

    public static ArrayList<Player> contestants = new ArrayList<>();
    public static HashMap<Player, Material> targets = new HashMap<>();
    public static HashMap<Player, Integer> timers = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("blockshuffle").setExecutor(new CommandBlockShuffle());

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    for (Player contestant : contestants) {

                        // If the timer hits below 0 for any contestant we end the game
                        if (timers.get(contestant) < 0) {
                            isRunning = false;
                            deinitialize(contestant);
                        }

                        // Notify the contestants when any contestant's timer hits 10 seconds
                        if (timers.get(contestant) == 10) {
                            contestant.sendMessage("You have 10 seconds left!");
                        }

                        // This will still run if someone lose, so we have to wrap the rest of the checks in another if statement and check if isRunning is still true
                        if (isRunning) {
                            // If the contestant is standing on their target
                            if (contestant.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial() == targets.get(contestant)) {
                                foundBlockSuccess(contestant);
                            }

                            // Last thing to do is to decrease timer
                            timers.put(contestant, timers.get(contestant) - 1);
                        }
                    }
                }
            }
        }, 0, 20); // Should be every second
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void foundBlockSuccess(Player contestant) {
        timers.put(contestant, defaultTimer);
        targets.put(contestant, getRandomBlock());
        informContestantOfBlock(contestant);
    }

    public static void initialize() {
        for (Player contestant : contestants) {
            timers.put(contestant, defaultTimer);
            targets.put(contestant, getRandomBlock());
            informContestantOfBlock(contestant);
        }
    }

    public static void informContestantOfBlock(Player contestant) {
        contestant.sendMessage("You must find and stand on " + targets.get(contestant).name().toLowerCase().replace('_', ' '));
    }

    public static void deinitialize(Player loser) {
        Bukkit.broadcastMessage("Game over! " + loser.getDisplayName() + " lost as they couldn't stand on " + targets.get(loser).name().toLowerCase().replace('_', ' ') + " in time!");
        contestants.clear();
        targets.clear();
        timers.clear();
    }

    public static Material getRandomBlock() {
        Material material = null;
        Random random = new Random();
        while (material == null) {
            material = Material.values()[random.nextInt(Material.values().length)];

            switch (material) {
                case AIR:
                case BARRIER:
                case END_STONE:
                case END_PORTAL:
                case END_PORTAL_FRAME:
                case END_ROD:
                case END_CRYSTAL:
                case END_GATEWAY:
                case END_STONE_BRICK_SLAB:
                case END_STONE_BRICK_STAIRS:
                case END_STONE_BRICK_WALL:
                case END_STONE_BRICKS:
                case DRAGON_EGG:
                case DRAGON_HEAD:
                case DRAGON_WALL_HEAD:
                case BEACON:
                case INFESTED_CHISELED_STONE_BRICKS:
                case INFESTED_COBBLESTONE:
                case INFESTED_CRACKED_STONE_BRICKS:
                case INFESTED_MOSSY_STONE_BRICKS:
                case INFESTED_STONE:
                case INFESTED_STONE_BRICKS:
                case COMMAND_BLOCK:
                case CHORUS_FLOWER:
                case CHORUS_FRUIT:
                case CHORUS_PLANT:
                case CONDUIT:
                case CREEPER_HEAD:
                case ZOMBIE_HEAD:
                case SKELETON_SKULL:
                case PLAYER_HEAD:
                case WITHER_SKELETON_SKULL:
                case CREEPER_WALL_HEAD:
                case ZOMBIE_WALL_HEAD:
                case SKELETON_WALL_SKULL:
                case PLAYER_WALL_HEAD:
                case WITHER_SKELETON_WALL_SKULL:
                case WITHER_ROSE:
                case POTTED_WITHER_ROSE:
                    material = null;
                    break;
                default:
                    if (!(material.isBlock())) {
                        material = null;
                    }
                    break;
            }
        }
        return material;
    }
}
