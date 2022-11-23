package committee.nova.neutron.server.player.storage

import committee.nova.neutron.api.player.storage.IPosWithDim
import committee.nova.neutron.implicits.named2Str
import committee.nova.neutron.util.reference.Tags
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3

class FormerPos extends IPosWithDim {
  var dim: Int = _
  var pos: Vec3 = _

  def this(dim: Int, pos: Vec3) = {
    this
    this.dim = dim
    this.pos = pos
  }

  override def getDim: Int = dim

  override def setDim(dim: Int): Unit = this.dim = dim

  override def getPos: Vec3 = pos

  override def setPos(pos: Vec3): Unit = this.pos = pos

  def serialize: NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setInteger(Tags.DIM, getDim)
    tag.setDouble(Tags.X, getX)
    tag.setDouble(Tags.Y, getY)
    tag.setDouble(Tags.Z, getZ)
    tag
  }

  def deserialize(tag: NBTTagCompound): IPosWithDim = {
    setDim(tag.getInteger(Tags.DIM))
    setPos(Vec3.createVectorHelper(tag.getDouble(Tags.X), tag.getDouble(Tags.Y), tag.getDouble(Tags.Z)))
    this
  }
}
