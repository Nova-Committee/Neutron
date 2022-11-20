package committee.nova.neutron.server.player.storage

import committee.nova.neutron.api.player.storage.IHome

import scala.annotation.tailrec

object Home {
  def apply(name: String, dim: Int, pos: (Double, Double, Double)): Home = new Home(name, dim, pos)

  @tailrec
  def getNextUnOccupiedHomeName(start: Int, homes: Array[String]): String = {
    val name = s"Home$start"
    if (homes.contains(name)) getNextUnOccupiedHomeName(start + 1, homes) else name
  }
}

class Home extends IHome {
  private var name: String = _
  private var dim: Int = _
  private var pos: (Double, Double, Double) = _

  def this(name: String, dim: Int, pos: (Double, Double, Double)) = {
    this
    this.name = name
    this.dim = dim
    this.pos = pos
  }

  override def getName: String = name

  override def getDim: Int = dim

  override def getPos: (Double, Double, Double) = pos

  override def setName(name: String): Unit = this.name = name

  override def setDim(dim: Int): Unit = this.dim = dim

  override def setPos(pos: (Double, Double, Double)): Unit = this.pos = pos
}
