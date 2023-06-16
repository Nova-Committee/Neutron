package committee.nova.neutron.util;

import committee.nova.neutron.command.perm.PermNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public class Utilities {
    public static boolean checkPerm(CommandSourceStack player, PermNode permNode, int defaultRequiredLevel) {
        return Permissions.check(player, permNode.getNode(), defaultRequiredLevel);
    }

    public static String getDefault(String prefix, List<String> existed) {
        int i = 1;
        while (existed.contains(prefix + i)) i++;
        return prefix + i;
    }

    public static double getActualSecond(int tick) {
        return tick / 20.0;
    }

    public static String getActualSecondStr(int tick) {
        return String.format("%.1f", getActualSecond(tick));
    }
}
