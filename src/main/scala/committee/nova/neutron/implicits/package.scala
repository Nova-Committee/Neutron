package committee.nova.neutron

import committee.nova.neutron.api.player.INeutronPlayer
import committee.nova.neutron.api.player.storage.IHome
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.command.ICommand
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}

import java.util.{HashSet => JSet}
import scala.language.implicitConversions

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) extends INeutronPlayer {
    private def getNeutron: INeutronPlayer = player.asInstanceOf[INeutronPlayer]

    override def getTpaCoolDown: Int = getNeutron.getTpaCoolDown

    override def setTpaCoolDown(cd: Int): Unit = getNeutron.setTpaCoolDown(cd)

    override def teleportTo(that: EntityPlayerMP): Boolean = getNeutron.teleportTo(that)

    override def getRtpAccumulation: Int = getNeutron.getRtpAccumulation

    override def setRtpAccumulation(acc: Int): Unit = getNeutron.setRtpAccumulation(acc)

    override def getHomes: JSet[IHome] = getNeutron.getHomes

    override def setHomes(homes: JSet[IHome]): Unit = getNeutron.setHomes(homes)

    override def getFormerX: Double = getNeutron.getFormerX

    override def getFormerY: Double = getNeutron.getFormerY

    override def getFormerZ: Double = getNeutron.getFormerZ

    override def getFormerDim: Int = getNeutron.getFormerDim

    override def setFormerX(x: Double): Unit = getNeutron.setFormerX(x)

    override def setFormerY(y: Double): Unit = getNeutron.setFormerY(y)

    override def setFormerZ(z: Double): Unit = getNeutron.setFormerZ(z)

    override def setFormerDim(dim: Int): Unit = getNeutron.setFormerDim(dim)

    override def hasNoValidFormerPos: Boolean = getNeutron.hasNoValidFormerPos
  }

  implicit class FMLServerStartingEventImplicit(val event: FMLServerStartingEvent) {
    def registerServerCommands(cmds: ICommand*): Unit = cmds.foreach(c => event.registerServerCommand(c))
  }
}
