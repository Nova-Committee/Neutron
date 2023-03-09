package committee.nova.neutron.api.player.storage

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.Vec3d

trait IPosWithDim {
  def getDim: Int

  def setDim(dim: Int): Unit

  def getPos: Vec3d

  def setPos(pos: Vec3d): Unit

  def getX: Double = getPos.x

  def getY: Double = getPos.y

  def getZ: Double = getPos.z

  def serialize: NBTTagCompound

  def deserialize(tag: NBTTagCompound): IPosWithDim
}
