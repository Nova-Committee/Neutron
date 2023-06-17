package committee.nova.neutron.mixin;

import committee.nova.neutron.Neutron;
import committee.nova.neutron.api.player.INeutronPlayer;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void inject$placeNewPlayer(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        final INeutronPlayer n = (INeutronPlayer) serverPlayer;
        final int backCd = Neutron.getBackCd();
        final int homeCd = Neutron.getHomeCd();
        final int warpCd = Neutron.getWarpCd();
        if (n.getBackCd() > backCd) n.setBackCd(backCd);
        if (n.getHomeCd() > homeCd) n.setHomeCd(homeCd);
        if (n.getWarpCd() > warpCd) n.setWarpCd(warpCd);
    }
}
