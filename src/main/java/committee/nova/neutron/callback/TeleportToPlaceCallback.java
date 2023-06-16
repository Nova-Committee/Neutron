package committee.nova.neutron.callback;

import committee.nova.neutron.api.storage.IPos;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface TeleportToPlaceCallback {
    Event<TeleportToPlaceCallback> EVENT = EventFactory.createArrayBacked(TeleportToPlaceCallback.class, listeners -> ((sender, formerPos, pos, tpType) -> {
        for (final var l : listeners) l.postTeleport(sender, formerPos, pos, tpType);
    }));

    void postTeleport(ServerPlayer sender, IPos formerPos, IPos pos, IPos.TeleportationType tpType);
}
