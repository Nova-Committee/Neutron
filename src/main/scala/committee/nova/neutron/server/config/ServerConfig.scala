package committee.nova.neutron.server.config

import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.config.Configuration

object ServerConfig {
  private var config: Configuration = _
  private var language: String = "en_us"
  private var tpTimeout: Int = 1200

  def init(event: FMLPreInitializationEvent): Unit = {
    config = new Configuration(event.getSuggestedConfigurationFile)
    sync()
  }

  def getLanguage: String = language

  def getMaxTPExpirationTime: Int = tpTimeout

  def sync(): Unit = {
    config.load()
    language = config.getString("language", Configuration.CATEGORY_GENERAL, "en_us", "Language ID of the server messages")
    tpTimeout = config.getInt("tpTimeout", Configuration.CATEGORY_GENERAL, 1200, 200, 6000, "Max expiration time for teleportation requests")
    config.save()
  }
}
