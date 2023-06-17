package committee.nova.neutron.command.perm;

import java.util.Locale;

public enum PermNode {
    COMMON_HOME_SET,
    COMMON_HOME_DEL,
    COMMON_HOME_TP,
    COMMON_BACK,
    COMMON_WARP_TP,
    COMMON_HAT,
    COMMON_HELP,
    ADMIN_WARP_ADD,
    ADMIN_WARP_DEL,
    ADMIN_RELOAD;

    public String getNode() {
        return "neutron." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
    }
}
