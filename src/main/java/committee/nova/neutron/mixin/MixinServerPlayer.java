package committee.nova.neutron.mixin;

import com.mojang.authlib.GameProfile;
import committee.nova.neutron.Neutron;
import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.api.storage.INamedPos;
import committee.nova.neutron.api.storage.IPos;
import committee.nova.neutron.storage.NamedPos;
import committee.nova.neutron.storage.Pos;
import committee.nova.neutron.util.collection.LtdLinkedList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements INeutronPlayer {
    @Shadow
    public abstract void restoreFrom(ServerPlayer serverPlayer, boolean bl);

    private final Set<INamedPos> homes = new LinkedHashSet<>();
    private final LtdLinkedList<IPos> footsteps = new LtdLinkedList<>();

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public List<INamedPos> getHomes() {
        return List.copyOf(homes);
    }

    @Override
    public List<IPos> getFootprints() {
        return List.copyOf(footsteps);
    }

    @Override
    public boolean addHome(INamedPos home) {
        if (homes.size() >= Neutron.getCfg().maxHomes) return false;
        if (homes.contains(home)) return false;
        return homes.add(home);
    }

    @Override
    public boolean removeHome(String homeName) {
        return homes.removeIf(p -> p.getName().equals(homeName));
    }

    @Override
    public void addFootprint(IPos footprint) {
        footsteps.addWithLimit(footprint, getFootprintsLimit());
    }

    @Override
    public int getFootprintsLimit() {
        return Neutron.getCfg().maxFootprints;
    }

    @Override
    public CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        final ListTag homesTag = new ListTag();
        homes.forEach(h -> homesTag.add(h.write()));
        final ListTag footstepsTag = new ListTag();
        footsteps.forEach(f -> footstepsTag.add(f.write()));
        tag.put("homes", homesTag);
        tag.put("footsteps", footstepsTag);
        return tag;
    }

    @Override
    public void read(CompoundTag tag) {
        homes.clear();
        footsteps.clear();
        if (tag.contains("homes")) tag.getList("homes", 10).forEach(t -> {
            if (t instanceof CompoundTag c) homes.add(NamedPos.createNamedPos(c));
        });
        if (tag.contains("footsteps")) tag.getList("footsteps", 10).forEach(t -> {
            if (t instanceof CompoundTag c) footsteps.add(Pos.createPos(c));
        });
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void inject$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("neutron")) read(tag.getCompound("neutron"));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void inject$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.put("neutron", write());
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void inject$restoreFrom(ServerPlayer oldPlayer, boolean bl, CallbackInfo ci) {
        read(((INeutronPlayer) oldPlayer).write());
        addFootprint(Pos.createPos(oldPlayer.level.dimension(), oldPlayer.position()));
    }
}
