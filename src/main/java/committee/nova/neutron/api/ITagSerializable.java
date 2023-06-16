package committee.nova.neutron.api;

import net.minecraft.nbt.CompoundTag;

public interface ITagSerializable {
    CompoundTag write();

    void read(CompoundTag tag);
}
