package committee.nova.neutron.storage;

import committee.nova.neutron.api.storage.IPos;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Pos implements IPos {
    protected ResourceKey<Level> world;
    protected Vec3 position;

    protected Pos(ResourceKey<Level> world, Vec3 position) {
        this.world = world;
        this.position = position;
    }

    @Override
    public ResourceKey<Level> getWorld() {
        return world;
    }

    @Override
    public Vec3 getPosition() {
        return position;
    }

    @Override
    public void read(CompoundTag tag) {
        this.position = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        try {
            final ResourceLocation key = new ResourceLocation(tag.getString("rl_key"));
            final ResourceLocation loc = new ResourceLocation(tag.getString("rl_loc"));
            this.world = ResourceKey.create(ResourceKey.createRegistryKey(key), loc);
        } catch (ResourceLocationException ignored) {

        }
    }

    public static Pos createPos(CompoundTag tag) {
        final Pos base = new Pos(null, null);
        base.read(tag);
        return base;
    }

    public static Pos createPos(ResourceKey<Level> world, Vec3 position) {
        return new Pos(world, position);
    }
}
