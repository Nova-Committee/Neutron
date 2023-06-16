package committee.nova.neutron.api.storage;

import net.minecraft.nbt.CompoundTag;

public interface INamedPos extends IPos {
    String getName();

    @Override
    default CompoundTag write() {
        final CompoundTag tag = IPos.super.write();
        tag.putString("name", getName());
        return tag;
    }

    @Override
    default String getDesc() {
        return "[" + getName() + "]" + IPos.super.getDesc();
    }
}
