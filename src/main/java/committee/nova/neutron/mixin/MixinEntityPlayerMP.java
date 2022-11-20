package committee.nova.neutron.mixin;

import com.mojang.authlib.GameProfile;
import committee.nova.neutron.server.event.impl.TeleportFromEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {
    public MixinEntityPlayerMP(World w, GameProfile g) {
        super(w, g);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    public void inject$onDeath(DamageSource src, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TeleportFromEvent((EntityPlayerMP) (Object) this, dimension, posX, posY, posZ));
    }
}
