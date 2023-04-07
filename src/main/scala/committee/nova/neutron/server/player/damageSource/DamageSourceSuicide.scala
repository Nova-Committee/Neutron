package committee.nova.neutron.server.player.damageSource

import committee.nova.neutron.server.l10n.ChatComponentServerTranslation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.DamageSource
import net.minecraft.util.text.ITextComponent

class DamageSourceSuicide extends DamageSource("suicide") {
  setDamageBypassesArmor()
  setDamageAllowedInCreativeMode()
  setDamageIsAbsolute()

  override def getDeathMessage(e: EntityLivingBase): ITextComponent = new ChatComponentServerTranslation("ann.neutron.suicide", e.getName)
}
