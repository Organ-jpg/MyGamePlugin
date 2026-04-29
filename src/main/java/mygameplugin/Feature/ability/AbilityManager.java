package mygameplugin.feature.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AbilityManager {
    private final Map<String, Ability> abilities = new HashMap<>();

    public void register(Ability ability) {
        abilities.put(ability.id(), ability);
    }

    public Optional<Ability> get(String id) {
        return Optional.ofNullable(abilities.get(id));
    }
}
