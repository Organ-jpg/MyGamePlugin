package mygameplugin.feature.specialItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpecialItemManager {
    private final Map<String, SpecialItem> items = new HashMap<>();

    public void register(SpecialItem item) {
        items.put(item.id(), item);
    }

    public Optional<SpecialItem> get(String id) {
        return Optional.ofNullable(items.get(id));
    }
}
