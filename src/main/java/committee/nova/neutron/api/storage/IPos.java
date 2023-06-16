package committee.nova.neutron.api.storage;

import committee.nova.neutron.Neutron;
import committee.nova.neutron.api.ITagSerializable;
import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.callback.TeleportToPlaceCallback;
import committee.nova.neutron.storage.Pos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public interface IPos extends ITagSerializable {
    ResourceKey<Level> getWorld();

    Vec3 getPosition();

    default String getDesc() {
        final String world = "<" + getWorld().location().toString() + ">";
        final String pos = "(" + String.format("%.2f", getPosition().x()) + ", "
                + String.format("%.2f", getPosition().y()) + ", "
                + String.format("%.2f", getPosition().z()) + ")";
        return world + ":" + pos;
    }

    @Override
    default CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("rl_key", getWorld().registry().toString());
        tag.putString("rl_loc", getWorld().location().toString());
        tag.putDouble("x", getPosition().x());
        tag.putDouble("y", getPosition().y());
        tag.putDouble("z", getPosition().z());
        return tag;
    }

    default boolean teleportPlayer(ServerPlayer player, TeleportationType tpType) {
        final MinecraftServer server = player.getServer();
        if (server == null) return false;
        final ServerLevel targetWorld = server.getLevel(getWorld());
        if (targetWorld == null) return false;
        final IPos formerPos = Pos.createPos(player.level.dimension(), player.position());
        player.teleportTo(targetWorld, getPosition().x(), getPosition().y(), getPosition().z(), player.getYRot(), player.getXRot());
        tpType.applyCd(player);
        TeleportToPlaceCallback.EVENT.invoker().postTeleport(player, formerPos, this, tpType);
        return true;
    }

    enum TeleportationType {
        HOME(p -> p.setHomeCd(Neutron.getHomeCd())),
        BACK(p -> p.setBackCd(Neutron.getBackCd())),
        WARP(p -> p.setWarpCd(Neutron.getWarpCd()));


        private final Consumer<INeutronPlayer> cdSetter;

        TeleportationType(Consumer<INeutronPlayer> cdSetter) {
            this.cdSetter = cdSetter;
        }

        public void applyCd(ServerPlayer player) {
            this.cdSetter.accept((INeutronPlayer) player);
        }
    }
}
