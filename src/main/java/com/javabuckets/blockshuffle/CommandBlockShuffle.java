package com.javabuckets.blockshuffle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBlockShuffle implements CommandExecutor {
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
            BlockShuffle.initialize(); // TODO: /blockshuffle <timer> ...<player>
            return true;
        }
        return false;
    }
}
