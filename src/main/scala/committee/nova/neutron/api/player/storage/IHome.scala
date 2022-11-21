package committee.nova.neutron.api.player.storage

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3

trait IHome extends IPosWithDim {
  def getName: String

  def setName(name: String): Unit

  def serialize: NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setString("name", getName)
    tag.setInteger("dim", getDim)
    tag.setDouble("x", getX)
    tag.setDouble("y", getY)
    tag.setDouble("z", getZ)
    tag
  }

  def deserialize(tag: NBTTagCompound): IHome = {
    setName(tag.getString("name"))
    setDim(tag.getInteger("dim"))
    setPos(Vec3.createVectorHelper(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")))
    this
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case h: IHome => h.getName == this.getName
      case _ => false
    }
  }

  override def hashCode(): Int = getName.hashCode
}
