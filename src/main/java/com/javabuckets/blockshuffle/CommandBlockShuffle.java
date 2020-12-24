package com.javabuckets.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandBlockShuffle implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            ArrayList<Player> contestants = new ArrayList<>();

            // Game already in progress
            if (BlockShuffle.isRunning) {
                if (args.length == 1 && args[0].equals("stop")) {
                    BlockShuffle.deinitialize();
                    Bukkit.broadcastMessage("BlockShuffle has stopped");
                    return true;
                } else {
                    playerSender.sendMessage("BlockShuffle game already in progress. To stop, run /blockshuffle stop");
                    return false;
                }
            }

            if (args.length >= 2) {
                if (Arrays.stream(ShuffleMode.values()).map(Enum::toString).collect(Collectors.toList()).contains(args[0])) {
                    if (isNumeric(args[1])) {
                        if (args.length == 2) {
                            // Start the game with all the players in the server
                            contestants.addAll(Bukkit.getOnlinePlayers());
                        } else {
                            // Start the game with the players added from the command arguments
                            contestants.add(playerSender);
                            for (String arg : Arrays.copyOfRange(args, 2, args.length)) {
                                Player p = Bukkit.getPlayer(arg);

                                if (p == null) {
                                    contestants.clear();
                                    return false;
                                } else {
                                    contestants.add(p);
                                }
                            }
                        }

                        BlockShuffle.initialize(contestants, ShuffleMode.valueOf(args[0]), Integer.parseInt(args[1]));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                return Arrays.stream(ShuffleMode.values()).map(Enum::toString).collect(Collectors.toList());
            }
            if (args.length == 2) {
                if (args[0].equals(ShuffleMode.DEFAULT.toString()) || args[0].equals(ShuffleMode.DEFAULT_SAME_BLOCKS.toString())) {
                    return Collections.singletonList("5");
                } else {
                    return Collections.singletonList("30");
                }
            }
            if (args.length > 2) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        }
        return null;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
