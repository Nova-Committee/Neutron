package committee.nova.neutron.common.reference;

import committee.nova.neutron.api.INamed;

public enum TagReferences implements INamed {
    NEUTRON_ROOT("Neutron"),
    CD_TPA("cdTpa"),
    ACCUMULATION_RTP("accRtp");

    TagReferences(String name) {
        this.name = name;
    }

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}
