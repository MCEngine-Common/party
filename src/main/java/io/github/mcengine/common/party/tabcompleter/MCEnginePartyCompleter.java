package io.github.mcengine.common.party.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tab completer for the /party command and its subcommands.
 * <p>
 * - First argument: suggests create, invite, kick, leave.
 * - Second argument (for invite and kick): suggests online player names.
 */
public class MCEnginePartyCompleter implements TabCompleter {

    /**
     * List of primary party subcommands for first-argument completion.
     */
    private static final List<String> MAIN_COMMANDS;

    static {
        List<String> cmds = new ArrayList<>();
        cmds.add("create");
        cmds.add("invite");
        cmds.add("kick");
        cmds.add("leave");
        MAIN_COMMANDS = Collections.unmodifiableList(cmds);
    }

    /**
     * Provides tab-completion options for the /party command.
     *
     * @param sender  the command sender
     * @param command the command object
     * @param alias   the alias used
     * @param args    the current arguments
     * @return a list of possible completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        // /party <subcommand>
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String cmd : MAIN_COMMANDS) {
                if (cmd.startsWith(current)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        // /party invite <player> or /party kick <player>
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick")) {
                String current = args[1].toLowerCase();
                List<String> suggestions = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    // For kick, don't suggest self
                    if (sub.equals("kick") && online.equals(sender)) continue;
                    if (online.getName().toLowerCase().startsWith(current)) {
                        suggestions.add(online.getName());
                    }
                }
                return suggestions;
            }
        }
        return Collections.emptyList();
    }
}
