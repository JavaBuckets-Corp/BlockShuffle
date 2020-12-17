package com.javabuckets.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandBlockShuffle implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            if (BlockShuffle.isRunning) {
                if (args[0].equals("stop")) {
                    BlockShuffle.deinitialize();
                    Bukkit.broadcastMessage("BlockShuffle has stopped");
                    return true;
                } else
                {
                    return false;
                }
            }

            BlockShuffle.contestants.add(playerSender);

            for (String arg : args) {
                Player p = Bukkit.getPlayer(arg);
                if (p == null) {
                    BlockShuffle.contestants.clear();
                    return false;
                } else {
                    BlockShuffle.contestants.add(p);
                }
            }

            BlockShuffle.isRunning = true;
            BlockShuffle.initialize();
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                return Collections.singletonList(Arrays.toString(ShuffleMode.values()));
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
}
