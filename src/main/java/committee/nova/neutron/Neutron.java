package committee.nova.neutron;

import com.google.common.collect.Lists;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Neutron implements ModInitializer {
    public static final String MODID = "neutron";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        getPixelArt().forEach(LOGGER::info);
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
                "Activating Neutron..."
        );
    }
}
