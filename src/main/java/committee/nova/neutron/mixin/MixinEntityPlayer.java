package committee.nova.neutron.mixin;

import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.api.player.storage.IHome;
import committee.nova.neutron.api.player.storage.IPosWithDim;
import committee.nova.neutron.common.reference.TagReferences;
import committee.nova.neutron.server.player.storage.FormerPos;
import committee.nova.neutron.server.player.storage.Home;
import committee.nova.neutron.util.collection.LimitedLinkedList;
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
    private final LimitedLinkedList<IPosWithDim> formerPosQueue = new LimitedLinkedList<>();

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
        final NBTTagList formerPos = new NBTTagList();
        for (final IPosWithDim pos : formerPosQueue) formerPos.appendTag(pos.serialize());
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
            formerPosQueue.clear();
            final NBTTagList formerPosTag = neutronTag.getTagList(TagReferences.FORMER_POS.getName(), 10);
            final int size = formerPosTag.tagCount();
            for (int i = 0; i < size; i++)
                formerPosQueue.add(new FormerPos().deserialize(formerPosTag.getCompoundTagAt(i)));
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
    public LimitedLinkedList<IPosWithDim> getFormerPos() {
        return formerPosQueue;
    }

    @Override
    public void setFormerPos(LimitedLinkedList<IPosWithDim> former) {
        this.formerPosQueue.clear();
        this.formerPosQueue.addAll(former);
    }
}
