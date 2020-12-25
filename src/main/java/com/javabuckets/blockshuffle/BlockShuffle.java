package com.javabuckets.blockshuffle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BlockShuffle extends JavaPlugin {

    public static boolean isRunning = false;
    public static ShuffleMode gamemode = ShuffleMode.DEFAULT;

    public static int defaultTimer = 5 * 60; // minutes * seconds_converter
    public static int globalTimer = 30 * 60; // minutes * seconds_converter

    public static ArrayList<Player> contestants = new ArrayList<>();
    public static HashMap<Player, Material> targets = new HashMap<>();
    public static HashMap<Player, Integer> timers = new HashMap<>();
    public static HashMap<Player, Integer> scores = new HashMap<>();
    public static ArrayList<Player> losers = new ArrayList<>();

    public static ArrayList<Material> sameTargets = new ArrayList<>();

    private static ScoreboardManager manager;
    private static Scoreboard board;
    private static Objective objective;
    private static Team team;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(this.getCommand("blockshuffle")).setExecutor(new CommandBlockShuffle());

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            if (isRunning) {
                if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.DEFAULT_SAME_BLOCKS) {
                    for (Player contestant : contestants) {
                        // Player lose
                        if (timers.get(contestant) <= 0) {
                            contestant.setGameMode(GameMode.SPECTATOR);
                            Bukkit.broadcastMessage(ChatColor.RED + contestant.getDisplayName() + " is eliminated, as they couldn't stand on " + targets.get(contestant).name().toLowerCase().replace('_', ' ') + " in time!");
                            losers.add(contestant);
                        }

                        if (isRunning) {
                            // Notifications
                            if (!losers.contains(contestant)) {
                                if (timers.get(contestant) == 10) {
                                    contestant.sendMessage("You have 10 seconds left!");
                                } else if (timers.get(contestant) == 60) {
                                    contestant.sendMessage("You have 1 minute left!");
                                }
                                contestant.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + timers.get(contestant).toString()));
                            }
                        }
                    }

                    // Win condition
                    if (losers.size() + 1 == contestants.size()) {
                        for (Player contestant : contestants) {
                            if (!losers.contains(contestant)) {
                                Bukkit.broadcastMessage(ChatColor.GREEN + "Game over! " + contestant.getDisplayName() + " won!");
                                deinitialize();
                            }
                        }
                    } else if (losers.size() == contestants.size()) {
                        Bukkit.broadcastMessage(ChatColor.GREEN + "Game over! It's a tie!");
                        deinitialize();
                    }
                } else {
                    // Win condition
                    if (globalTimer <= 0) {
                        ArrayList<Player> winners = new ArrayList<>();
                        Map.Entry<Player, Integer> maxEntry = null;

                        for (Map.Entry<Player, Integer> entry : scores.entrySet()) {
                            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                                maxEntry = entry;
                                winners.clear();
                                winners.add(entry.getKey());
                                continue;
                            }
                            if (entry.getValue().equals(maxEntry.getValue())) {
                                winners.add(entry.getKey());
                            }
                        }

                        if (winners.size() > 1) {
                            Bukkit.broadcastMessage("We have a tie!");
                        } else {
                            Bukkit.broadcastMessage("The winner is " + winners.get(0).getName() + "!");
                        }

                        deinitialize();
                    }

                    if (isRunning) {
                        // Notifications
                        if (globalTimer == 10) {
                            Bukkit.broadcastMessage("10 seconds left!");
                        } else if (globalTimer == 60) {
                            Bukkit.broadcastMessage("There is now only 1 minute left!");
                        } else if (globalTimer % (5 * 60) == 0) {
                            Bukkit.broadcastMessage("There is " + (globalTimer / 60) + " minutes left!");
                        }
                        for (Player contestant : contestants) {
                            contestant.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + String.valueOf(globalTimer)));
                        }
                    }
                }
                for (Player contestant : contestants) {
                    // This will still run if someone lose, so we have to wrap the rest of the checks in another if statement and check if isRunning is still true
                    if (isRunning) {
                        // If the contestant is standing on their target
                        if (contestant.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial() == targets.get(contestant)) {
                            foundBlockSuccess(contestant);

                            if (scores.get(contestant) == (gamemode == ShuffleMode.DEFAULT ? 3 : 6)) {
                                Bukkit.broadcastMessage(ChatColor.GREEN + contestant.getDisplayName() + " has unlocked the medium tier!");
                            }
                            else if (scores.get(contestant) == (gamemode == ShuffleMode.DEFAULT ? 7 : 12)) {
                                Bukkit.broadcastMessage(ChatColor.RED + contestant.getDisplayName() + " has unlocked the nether tier!");
                            }
                            else if (scores.get(contestant) == (gamemode == ShuffleMode.DEFAULT ? 12 : 20)) {
                                Bukkit.broadcastMessage(ChatColor.BLACK + contestant.getDisplayName() + " has unlocked the hard tier! Good luck...");
                            }
                        }

                        if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.DEFAULT_SAME_BLOCKS) {
                            // Last thing to do is to decrease timer
                            timers.put(contestant, timers.get(contestant) - 1);
                        }
                    }
                }
                if (isRunning) {
                    if (gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER || gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER_SAME_BLOCKS) {
                        // Last thing to do is to decrease timer
                        globalTimer--;
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
        scores.put(contestant, scores.get(contestant) + 1);
        objective.getScore(contestant.getName()).setScore(scores.get(contestant));

        if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.DEFAULT_SAME_BLOCKS) {
            timers.put(contestant, defaultTimer);
        }

        if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER) {
            targets.put(contestant, RandomBlockSelector.getRandomBlock(scores.get(contestant)));
        } else {
            targets.put(contestant, sameTargets.get(scores.get(contestant)));
        }

        informContestantOfBlock(contestant);
    }

    public static void initialize(ArrayList<Player> players, ShuffleMode mode, int timer) {
        isRunning = true;
        contestants.addAll(players);
        gamemode = mode;
        defaultTimer = timer * 60; // minutes * seconds_converter
        globalTimer = timer * 60; // minutes * seconds_converter

        // SAME_BLOCKS modes require a list of materials so all players will receive the same blocks at N score
        if (gamemode == ShuffleMode.DEFAULT_SAME_BLOCKS || gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER_SAME_BLOCKS) {
            for (int i = 0; i < 250; i++) {
                // 20+ hard     12+ nether     6+ medium     0+ easy
                sameTargets.add(RandomBlockSelector.getRandomBlock(i > 20 ? 15 : i > 12 ? 10 : i > 6 ? 5 : 0));
            }
        }

        // Scoreboard init
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("points", "points", "BlockShuffle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        team = board.registerNewTeam("BlockShuffle");

        team.setDisplayName("BlockShuffle");
        team.setAllowFriendlyFire(false);

        for (Player contestant : contestants) {
            timers.put(contestant, defaultTimer);
            scores.put(contestant, 0);

            team.addEntry(contestant.getName());
            objective.getScore(contestant.getName()).setScore(0);
            contestant.setScoreboard(board);

            if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER) {
                targets.put(contestant, RandomBlockSelector.getRandomBlock(0));
            } else {
                targets.put(contestant, sameTargets.get(0));
            }
            informContestantOfBlock(contestant);
        }
    }

    public static void informContestantOfBlock(Player contestant) {
        String prettyName = targets.get(contestant).name().toLowerCase().replace('_', ' ');

        if (gamemode == ShuffleMode.DEFAULT || gamemode == ShuffleMode.HIGHEST_BEFORE_TIMER) {
            Bukkit.broadcastMessage(ChatColor.GOLD + contestant.getDisplayName() + " must find " + prettyName + "!");
        }
        contestant.sendMessage(ChatColor.BLUE + "You must find and stand on " + prettyName);

    }

    public static void deinitialize() {
        isRunning = false;

        for (Player contestant : contestants) {
            team.removeEntry(contestant.getName());
            contestant.setScoreboard(manager.getNewScoreboard());
            contestant.setGameMode(GameMode.SURVIVAL);
        }

        contestants.clear();
        targets.clear();
        scores.clear();
        timers.clear();
        sameTargets.clear();
        losers.clear();
    }
}
