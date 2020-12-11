package com.javabuckets.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;

public final class BlockShuffle extends JavaPlugin {

    public static boolean isRunning = false;

    public static int defaultTimer = 5 * 60; // minutes * seconds_converter

    public static ArrayList<Player> contestants = new ArrayList<>();
    public static HashMap<Player, Material> targets = new HashMap<>();
    public static HashMap<Player, Integer> timers = new HashMap<>();
    public static HashMap<Player, Integer> scores = new HashMap<>();

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
                            Bukkit.broadcastMessage("Game over! " + contestant.getDisplayName() + " lost as they couldn't stand on " + targets.get(contestant).name().toLowerCase().replace('_', ' ') + " in time!");
                            deinitialize();
                        }

                        // Notify time left
                        if (timers.get(contestant) == 10) {
                            contestant.sendMessage("You have 10 seconds left!");
                        } else if (timers.get(contestant) == 60) {
                            contestant.sendMessage("You have 1 minute left!");
                        }


                        // This will still run if someone lose, so we have to wrap the rest of the checks in another if statement and check if isRunning is still true
                        if (isRunning) {
                            // If the contestant is standing on their target
                            if (contestant.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial() == targets.get(contestant)) {
                                foundBlockSuccess(contestant);

                                if (scores.get(contestant) == 3) {
                                    Bukkit.broadcastMessage(contestant.getDisplayName() + " has unlocked the medium tier!");
                                }
                                else if (scores.get(contestant) == 7) {
                                    Bukkit.broadcastMessage(contestant.getDisplayName() + " has unlocked the nether tier!");
                                }
                                else if (scores.get(contestant) == 12) {
                                    Bukkit.broadcastMessage(contestant.getDisplayName() + " has unlocked the hard tier! Good luck...");
                                }
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
        scores.put(contestant, scores.get(contestant) + 1);
        targets.put(contestant, RandomBlockSelector.getRandomBlock(scores.get(contestant)));
        informContestantOfBlock(contestant);
    }

    public static void initialize() {
        for (Player contestant : contestants) {
            timers.put(contestant, defaultTimer);
            targets.put(contestant, RandomBlockSelector.getRandomBlock(0));
            scores.put(contestant, 0);
            informContestantOfBlock(contestant);
        }
    }

    public static void informContestantOfBlock(Player contestant) {
        String prettyName = targets.get(contestant).name().toLowerCase().replace('_', ' ');
        Bukkit.broadcastMessage(contestant.getDisplayName() + " must find " + prettyName + "!");
        contestant.sendMessage("You must find and stand on " + prettyName);
    }

    public static void deinitialize() {
        isRunning = false;
        contestants.clear();
        targets.clear();
        scores.clear();
        timers.clear();
    }
}
