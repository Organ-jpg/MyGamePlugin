package mygameplugin.gamemode.imposter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ImposterRoleService {
    public Map<UUID, ImposterRole> assignRoles(Set<UUID> players) {
        List<UUID> ordered = new ArrayList<>(players);
        Map<UUID, ImposterRole> roles = new LinkedHashMap<>();
        int imposterCount = ordered.size() <= 6 ? 1 : 2;
        for (int index = 0; index < ordered.size(); index++) {
            roles.put(ordered.get(index), index < imposterCount ? ImposterRole.IMPOSTER : ImposterRole.INNOCENT);
        }
        return roles;
    }
}
