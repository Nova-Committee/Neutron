package committee.nova.neutron.compat

import committee.nova.neutron.util.reference.PermNodes
import committee.nova.proton.core.event.impl.{ProtonImmutableGroupInitializationEvent, ProtonPermNodeInitializationEvent, ProtonPlayerInitializationEvent}
import committee.nova.proton.core.perm.{Group, PermNode}
import committee.nova.proton.implicits.PlayerImplicit
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.common.MinecraftForge

object ProtonCompat {
  def init(): Unit = MinecraftForge.EVENT_BUS.register(new ProtonCompat)

  def hasPerm(player: EntityPlayerMP, perm: String): Boolean = player.hasPerm(perm)
}

class ProtonCompat {
  @SubscribeEvent
  def onPermInit(e: ProtonPermNodeInitializationEvent): Unit = PermNodes.ADMIN_PERMS.foreach(p => e.addNodeFromString(p.getName))

  @SubscribeEvent
  def onGroupInit(e: ProtonImmutableGroupInitializationEvent): Unit = {
    val neutronDefault = Group("NeutronDefault")
    PermNodes.COMMON_PERMS.map(p => PermNode(p.getName)).foreach(p => neutronDefault.addPerm(p))
    val neutronManager = Group("NeutronManager")
    PermNodes.MANAGER_PERMS.map(p => PermNode(p.getName)).foreach(p => neutronManager.addPerm(p))
    val neutronAdmin = Group("NeutronAdmin")
    PermNodes.ADMIN_PERMS.map(p => PermNode(p.getName)).foreach(p => neutronAdmin.addPerm(p))
    e.addGroup(neutronDefault, neutronManager, neutronAdmin)
  }

  @SubscribeEvent
  def onPlayerInit(e: ProtonPlayerInitializationEvent): Unit = {
    e.addFunc(p => p.addToGroup("NeutronDefault"))
  }
}
