package committee.nova.neutron.server.storage;

import committee.nova.neutron.Neutron;
import committee.nova.neutron.api.storage.INamedPos;
import committee.nova.neutron.storage.NamedPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public class NeutronPersistentState extends SavedData {
    private final Set<INamedPos> warps = new CopyOnWriteArraySet<>();

    public static NeutronPersistentState createFromTag(CompoundTag tag) {
        final NeutronPersistentState state = new NeutronPersistentState();
        if (!tag.contains("neutron")) return state;
        final CompoundTag neutron = tag.getCompound("neutron");
        if (!neutron.contains("warps")) return state;
        tag.getList("warps", 10).forEach(t -> {
            if (t instanceof CompoundTag c) state.warps.add(NamedPos.createNamedPos(c));
        });
        return state;
    }

    public static NeutronPersistentState getNeutron(MinecraftServer server) {
        final ServerLevel l = server.getLevel(Level.OVERWORLD);
        Objects.requireNonNull(l);
        return l.getDataStorage().computeIfAbsent(
                NeutronPersistentState::createFromTag,
                NeutronPersistentState::new,
                Neutron.MODID
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        final ListTag list = new ListTag();
        warps.forEach(p -> list.add(p.write()));
        final CompoundTag neutron = new CompoundTag();
        neutron.put("warps", list);
        tag.put("neutron", neutron);
        return tag;
    }

    public List<INamedPos> getWarps() {
        return List.copyOf(warps);
    }

    public <T> T modifyWarps(Function<Set<INamedPos>, T> fun) {
        final T result = fun.apply(warps);
        setDirty();
        return result;
    }
}
