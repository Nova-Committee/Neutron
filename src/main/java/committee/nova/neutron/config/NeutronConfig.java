package committee.nova.neutron.config;

import committee.nova.neutron.Neutron;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = Neutron.MODID)
public class NeutronConfig implements ConfigData {
    @Comment("The maximum number of homes a player can set")
    public int maxHomes = 5;
    @Comment("The maximum number of footprints a player can store")
    public int maxFootprints = 5;
    @Comment("The cool-down time(tick) applied after a successful home teleportation")
    public double homeCd = 60.0;
    @Comment("The cool-down time(tick) applied after a successful warp teleportation")
    public double warpCd = 60.0;
    @Comment("The cool-down time(tick) applied after a successful back teleportation")
    public double backCd = 60.0;
    @Comment("Alternatives of /ntnsethome")
    public List<String> saSethome = List.of("sethome");
    @Comment("Alternatives of /ntnhome")
    public List<String> saHome = List.of("home");
    @Comment("Alternatives of /ntndelhome")
    public List<String> saDelhome = List.of("delhome");
    @Comment("Alternatives of /ntnback")
    public List<String> saBack = List.of("back");
    @Comment("Alternatives of /ntnwarp")
    public List<String> saWarp = List.of("warp");
    @Comment("Alternatives of /ntnsetwarp")
    public List<String> saAddwarp = List.of("addwarp");
    @Comment("Alternatives of /ntnsetwarp")
    public List<String> saDelwarp = List.of("delwarp");
    @Comment("Alternatives of /ntnhat")
    public List<String> saHat = List.of("hat");
    @Comment("Notification sound to be played after a teleportation to a place. Leave a blank to disable.")
    public String notificationSound = "minecraft:entity.enderman.teleport";
}
