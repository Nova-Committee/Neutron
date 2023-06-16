package committee.nova.neutron.api.player;

import committee.nova.neutron.api.ITagSerializable;
import committee.nova.neutron.api.storage.INamedPos;
import committee.nova.neutron.api.storage.IPos;

import java.util.List;

public interface INeutronPlayer extends ITagSerializable {
    List<INamedPos> getHomes();

    List<IPos> getFootprints();

    boolean addHome(INamedPos home);

    boolean removeHome(String homeName);

    void addFootprint(IPos footprint);

    int getFootprintsLimit();
}
