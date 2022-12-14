package committee.nova.neutron.server.l10n

import committee.nova.neutron.util.Utilities
import net.minecraft.util.ChatComponentText

import java.text.MessageFormat

class ChatComponentServerTranslation(key: String, args: Any*)
  extends ChatComponentText(MessageFormat.format(Utilities.L10n.getFromCurrentLang(key), args.toArray.asInstanceOf[Array[AnyRef]].toSeq: _*))
