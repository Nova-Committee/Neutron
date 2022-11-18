package committee.nova.neutron.mixin;

import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.common.reference.TagReferences;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements INeutronPlayer {
    private int cdTpa = 0;

    public MixinEntityPlayer(World w) {
        super(w);
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    public void inject$writeEntityToNBT(NBTTagCompound tag, CallbackInfo ci) {
        write(tag);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    public void inject$readEntityFromNBT(NBTTagCompound tag, CallbackInfo ci) {
        read(tag);
    }

    @Override
    public void write(NBTTagCompound tag) {
        final NBTTagCompound neutronTag = new NBTTagCompound();
        neutronTag.setInteger(TagReferences.CD_TPA.getName(), getTpaCoolDown());
        tag.setTag(TagReferences.NEUTRON_ROOT.getName(), neutronTag);
    }

    @Override
    public void read(NBTTagCompound tag) {
        if (!tag.hasKey(TagReferences.NEUTRON_ROOT.getName())) return;
        final NBTTagCompound neutronTag = tag.getCompoundTag(TagReferences.NEUTRON_ROOT.getName());
        setTpaCoolDown(neutronTag.getInteger(TagReferences.CD_TPA.getName()));
    }

    @Override
    public int getTpaCoolDown() {
        return cdTpa;
    }

    @Override
    public void setTpaCoolDown(int cd) {
        cdTpa = cd;
    }

    @Override
    public boolean teleportTo(EntityPlayerMP that) {
        if (!(((EntityPlayer) (Object) this) instanceof EntityPlayerMP)) return false;
        final EntityPlayerMP mp = (EntityPlayerMP) (Object) this;
        try {
            if (this.dimension != that.dimension) this.travelToDimension(that.dimension);
            mp.playerNetServerHandler.setPlayerLocation(that.posX, that.posY, that.posZ, that.rotationYaw, that.rotationPitch);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
