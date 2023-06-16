package committee.nova.neutron.storage;

import committee.nova.neutron.api.storage.INamedPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class NamedPos extends Pos implements INamedPos {
    protected String name;

    protected NamedPos(String name, ResourceKey<Level> world, Vec3 position) {
        super(world, position);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        this.name = tag.getString("name");
    }

    public static NamedPos createNamedPos(CompoundTag tag) {
        final NamedPos base = new NamedPos(null, null, null);
        base.read(tag);
        return base;
    }

    public static NamedPos createNamedPos(String name, ResourceKey<Level> world, Vec3 position) {
        return new NamedPos(name, world, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(name, ((NamedPos) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
