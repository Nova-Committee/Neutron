package committee.nova.neutron.api.player.storage

import net.minecraft.nbt.NBTTagCompound

trait IHome {
  def getName: String

  def setName(name: String): Unit

  def getDim: Int

  def setDim(dim: Int): Unit

  def getPos: (Double, Double, Double)

  def setPos(pos: (Double, Double, Double)): Unit

  def getX: Double = getPos._1

  def getY: Double = getPos._2

  def getZ: Double = getPos._3

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
    setPos((tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")))
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
