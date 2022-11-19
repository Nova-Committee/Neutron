package committee.nova.neutron.server.config

import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.config.Configuration

object ServerConfig {
  private var config: Configuration = _
  private var language: String = _
  private var tpTimeout: Int = _
  private var tpCoolDown: Int = _
  private var maxRtpChances: Int = _
  private var rtpChancesRecoveryTime: Int = _
  private var rtpMaxVerticalAxisRange: Int = _
  private var rtpMaxTriesOnFindingPosition: Int = _

  def init(event: FMLPreInitializationEvent): Unit = {
    config = new Configuration(event.getSuggestedConfigurationFile)
    sync()
  }

  def getLanguage: String = language

  def getMaxTpExpirationTime: Int = tpTimeout

  def getTpCoolDown: Int = tpCoolDown

  def getMaxRtpChances: Int = maxRtpChances

  def getRtpChancesRecoveryTime: Int = rtpChancesRecoveryTime

  def getRtpMaxVerticalAxisRange: Int = rtpMaxVerticalAxisRange

  def getRtpMaxTriesOnFindingPosition: Int = rtpMaxTriesOnFindingPosition

  def sync(): Unit = {
    config.load()
    language = config.getString("language", Configuration.CATEGORY_GENERAL, "en_us", "Language ID of the server messages")
    tpTimeout = config.getInt("tpTimeout", Configuration.CATEGORY_GENERAL, 1200, 200, 1728000, "Max expiration time (ticks) for teleportation requests")
    tpCoolDown = config.getInt("tpCoolDown", Configuration.CATEGORY_GENERAL, 1200, 0, 1728000, "Cool-down time (ticks) for teleportation")
    maxRtpChances = config.getInt("maxRtpChances", Configuration.CATEGORY_GENERAL, 5, 0, 100, "Max tries of RTP in a short time")
    rtpChancesRecoveryTime = config.getInt("rtpChancesRecoveryTime", Configuration.CATEGORY_GENERAL, 6000, 1, 1728000, "Recovery time (ticks) for an RTP try")
    rtpMaxVerticalAxisRange = config.getInt("rtpMaxVerticalAxisRange", Configuration.CATEGORY_GENERAL, 10000, 1000, 200000, "Max distance on a vertical axis to the player's original position when random teleporting")
    rtpMaxTriesOnFindingPosition = config.getInt("rtpMaxTriesOnFindingPosition", Configuration.CATEGORY_GENERAL, 10, 1, 100, "Max tries on finding a rtp target position. If exceeded, player's rtp chances won't be consumed")
    config.save()
  }
}
