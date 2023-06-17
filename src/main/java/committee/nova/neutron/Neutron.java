package committee.nova.neutron;

import com.google.common.collect.Lists;
import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.callback.TeleportToPlaceCallback;
import committee.nova.neutron.command.init.CommandInit;
import committee.nova.neutron.config.NeutronConfig;
import committee.nova.neutron.storage.Pos;
import committee.nova.tprequest.callback.TeleportationCallback;
import committee.nova.tprequest.request.TeleportRequest;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.YamlConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Neutron implements ModInitializer {
    public static final String MODID = "neutron";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static NeutronConfig cfg;

    @Override
    public void onInitialize() {
        AutoConfig.register(NeutronConfig.class, YamlConfigSerializer::new);
        cfg = AutoConfig.getConfigHolder(NeutronConfig.class).getConfig();
        getPixelArt().forEach(LOGGER::info);
        LOGGER.info("Registering commands...");
        CommandRegistrationCallback.EVENT.register(CommandInit::register);
        LOGGER.info("Handling callbacks...");
        TeleportationCallback.EVENT.register((s, r, t, w, p) -> ((INeutronPlayer) (t.equals(TeleportRequest.TeleportationType.TO) ? s : r))
                .addFootprint(Pos.createPos(w, p)));
        TeleportToPlaceCallback.EVENT.register((s, f, p, t) -> {
            getNotificationSound().ifPresent(n -> s.playNotifySound(n, SoundSource.PLAYERS, 1.0F, 1.0F));
            ((INeutronPlayer) s).addFootprint(f);
        });
        LOGGER.info("Neutron initialized!");
    }

    public static List<String> getPixelArt() {
        return Lists.newArrayList(
                " $$\\   $$\\                       $$\\                                    ",
                " $$$\\  $$ |                      $$ |                                   ",
                " $$$$\\ $$ | $$$$$$\\  $$\\   $$\\ $$$$$$\\    $$$$$$\\   $$$$$$\\  $$$$$$$\\   ",
                " $$ $$\\$$ |$$  __$$\\ $$ |  $$ |\\_$$  _|  $$  __$$\\ $$  __$$\\ $$  __$$\\  ",
                " $$ \\$$$$ |$$$$$$$$ |$$ |  $$ |  $$ |    $$ |  \\__|$$ /  $$ |$$ |  $$ | ",
                " $$ |\\$$$ |$$   ____|$$ |  $$ |  $$ |$$\\ $$ |      $$ |  $$ |$$ |  $$ | ",
                " $$ | \\$$ |\\$$$$$$$\\ \\$$$$$$  |  \\$$$$  |$$ |      \\$$$$$$  |$$ |  $$ | ",
                " \\__|  \\__| \\_______| \\______/    \\____/ \\__|       \\______/ \\__|  \\__| ",
                "Initializing Neutron..."
        );
    }

    public static NeutronConfig getCfg() {
        return cfg;
    }

    public static Optional<SoundEvent> getNotificationSound() {
        try {
            return Optional.ofNullable(Registry.SOUND_EVENT.get(new ResourceLocation(getCfg().notificationSound)));
        } catch (ResourceLocationException ignored) {
            return Optional.empty();
        }
    }

    public static List<String> getAlternativesFor(String cmd) {
        return switch (cmd) {
            case "ntnsethome" -> cfg.saSethome;
            case "ntnhome" -> cfg.saHome;
            case "ntndelhome" -> cfg.saDelhome;
            case "ntnwarp" -> cfg.saWarp;
            case "ntnaddwarp" -> cfg.saAddwarp;
            case "ntndelwarp" -> cfg.saDelwarp;
            case "ntnback" -> cfg.saBack;
            default -> Collections.emptyList();
        };
    }

    public static boolean reload() {
        final var reloaded = AutoConfig.getConfigHolder(NeutronConfig.class).load();
        cfg = AutoConfig.getConfigHolder(NeutronConfig.class).getConfig();
        return reloaded;
    }

    public static int getWarpCd() {
        return getActualTick(getCfg().warpCd);
    }

    public static int getHomeCd() {
        return getActualTick(getCfg().homeCd);
    }

    public static int getBackCd() {
        return getActualTick(getCfg().backCd);
    }

    private static int getActualTick(double t) {
        return (int) (t * 20.0);
    }
}
