package committee.nova.neutron.api.reference

trait INamed {
  def getName: String
}

class Named(val name: String) extends INamed {
  override def getName: String = name
}
