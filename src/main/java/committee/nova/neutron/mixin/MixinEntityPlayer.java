package committee.nova.neutron.mixin;

import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.api.player.storage.IHome;
import committee.nova.neutron.common.reference.TagReferences;
import committee.nova.neutron.server.player.storage.Home;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements INeutronPlayer {
    private int cdTpa = 0;
    private int rtpAccumulation = 0;
    private final LinkedHashSet<IHome> homes = new LinkedHashSet<>();
    private double formerX = Double.MIN_VALUE;
    private double formerY = Double.MIN_VALUE;
    private double formerZ = Double.MIN_VALUE;
    private int formerDim = Integer.MIN_VALUE;

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

    public void write(NBTTagCompound tag) {
        final NBTTagCompound neutronTag = new NBTTagCompound();
        neutronTag.setInteger(TagReferences.CD_TPA.getName(), getTpaCoolDown());
        neutronTag.setInteger(TagReferences.ACCUMULATION_RTP.getName(), getRtpAccumulation());
        final NBTTagList homesTag = new NBTTagList();
        for (final IHome home : homes) homesTag.appendTag(home.serialize());
        neutronTag.setTag(TagReferences.HOMES.getName(), homesTag);
        final NBTTagCompound formerPos = new NBTTagCompound();
        formerPos.setDouble(TagReferences.X.getName(), formerX);
        formerPos.setDouble(TagReferences.Y.getName(), formerY);
        formerPos.setDouble(TagReferences.Z.getName(), formerZ);
        formerPos.setInteger(TagReferences.DIM.getName(), formerDim);
        neutronTag.setTag(TagReferences.FORMER_POS.getName(), formerPos);
        tag.setTag(TagReferences.NEUTRON_ROOT.getName(), neutronTag);
    }

    public void read(NBTTagCompound tag) {
        if (!tag.hasKey(TagReferences.NEUTRON_ROOT.getName())) return;
        final NBTTagCompound neutronTag = tag.getCompoundTag(TagReferences.NEUTRON_ROOT.getName());
        setTpaCoolDown(neutronTag.getInteger(TagReferences.CD_TPA.getName()));
        setRtpAccumulation(neutronTag.getInteger(TagReferences.ACCUMULATION_RTP.getName()));
        if (neutronTag.hasKey(TagReferences.HOMES.getName())) {
            homes.clear();
            final NBTTagList homesTag = neutronTag.getTagList(TagReferences.HOMES.getName(), 10);
            final int size = homesTag.tagCount();
            for (int i = 0; i < size; i++) homes.add(new Home().deserialize(homesTag.getCompoundTagAt(i)));
        }
        if (neutronTag.hasKey(TagReferences.FORMER_POS.getName())) {
            final NBTTagCompound former = neutronTag.getCompoundTag(TagReferences.FORMER_POS.getName());
            formerX = former.getDouble(TagReferences.X.getName());
            formerY = former.getDouble(TagReferences.Y.getName());
            formerZ = former.getDouble(TagReferences.Z.getName());
            formerDim = former.getInteger(TagReferences.DIM.getName());
        }
    }

    @Override
    public int getRtpAccumulation() {
        return rtpAccumulation;
    }

    @Override
    public void setRtpAccumulation(int rtpAccumulation) {
        this.rtpAccumulation = rtpAccumulation;
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

    @Override
    public HashSet<IHome> getHomes() {
        return homes;
    }

    @Override
    public void setHomes(HashSet<IHome> homes) {
        this.homes.clear();
        this.homes.addAll(homes);
    }

    @Override
    public double getFormerX() {
        return formerX;
    }

    @Override
    public double getFormerY() {
        return formerY;
    }

    @Override
    public double getFormerZ() {
        return formerZ;
    }

    @Override
    public int getFormerDim() {
        return formerDim;
    }

    @Override
    public void setFormerX(double x) {
        formerX = x;
    }

    @Override
    public void setFormerY(double y) {
        formerY = y;
    }

    @Override
    public void setFormerZ(double z) {
        formerZ = z;
    }

    @Override
    public void setFormerDim(int dim) {
        formerDim = dim;
    }

    @Override
    public boolean hasNoValidFormerPos() {
        return formerDim == Integer.MIN_VALUE;
    }
}
