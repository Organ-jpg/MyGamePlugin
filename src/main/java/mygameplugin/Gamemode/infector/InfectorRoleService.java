package mygameplugin.gamemode.infector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InfectorRoleService {
    public Map<UUID, InfectorRole> assignRoles(Set<UUID> players) {
        List<UUID> ordered = new ArrayList<>(players);
        Map<UUID, InfectorRole> roles = new LinkedHashMap<>();
        for (int index = 0; index < ordered.size(); index++) {
            roles.put(ordered.get(index), index == 0 ? InfectorRole.QUEEN : InfectorRole.RUNNER);
        }
        return roles;
    }
}
