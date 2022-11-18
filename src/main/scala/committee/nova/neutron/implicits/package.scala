package committee.nova.neutron

import committee.nova.neutron.api.player.INeutronPlayer
import cpw.mods.fml.common.event.FMLServerStartingEvent
import net.minecraft.command.ICommand
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.NBTTagCompound

import scala.language.implicitConversions

package object implicits {
  implicit class PlayerImplicit(val player: EntityPlayer) extends INeutronPlayer {
    private def getNeutron: INeutronPlayer = player.asInstanceOf[INeutronPlayer]

    override def getTpaCoolDown: Int = getNeutron.getTpaCoolDown

    override def setTpaCoolDown(cd: Int): Unit = getNeutron.setTpaCoolDown(cd)

    override def teleportTo(that: EntityPlayerMP): Boolean = getNeutron.teleportTo(that)

    override def write(tag: NBTTagCompound): Unit = getNeutron.write(tag)

    override def read(tag: NBTTagCompound): Unit = getNeutron.read(tag)
  }

  implicit class FMLServerStartingEventImplicit(val event: FMLServerStartingEvent) {
    def registerServerCommands(cmds: ICommand*): Unit = cmds.foreach(c => event.registerServerCommand(c))
  }
}
