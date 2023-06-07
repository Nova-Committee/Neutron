package committee.nova.neutron.server.world.teleporter

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.ITeleporter

object NeutronTeleporter {
  def apply(pos: BlockPos): NeutronTeleporter = new NeutronTeleporter(pos)
}

class NeutronTeleporter(target: BlockPos) extends ITeleporter {
  override def placeEntity(world: World, entity: Entity, yaw: Float): Unit = entity.moveToBlockPosAndAngles(target, yaw, entity.rotationPitch)

  override def isVanilla: Boolean = false
}
