package net.rywir.ravenousmaw.system;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.content.component.MutationComponent;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;

import java.util.*;

public class MutationHandler {
    private final ItemStack stack;
    private ImmutableMap<String, MutationComponent.ConfigurationComponent> mutmap;

    public MutationHandler(ItemStack stack) {
        this.stack = stack;
        this.mutmap = stack.get(DataComponentTypes.MUTATION_COMPONENT_TYPE).mutmap();
    }

    public void syncMutations(ItemStack target) {
        MutationComponent component = stack.get(DataComponentTypes.MUTATION_COMPONENT_TYPE);
        target.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(component.mutmap()));
    }

    public boolean has(Mutations mutation) {
        return mutmap.containsKey(mutation.key());
    }

    public void add(Mutations mutation) {
        if (has(mutation)) {
            return;
        }

        Map<String, Integer> pseudoConfmap = new HashMap<>();
        mutation.parameters().forEach(parameter -> {
            pseudoConfmap.put(parameter.key(), parameter.getDefaultVal());
        });

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        updateMap();
    }

    public void addWithCraft(Mutations mutation, HolderLookup.Provider registries) {
        if (has(mutation)) {
            return;
        }

        Map<String, Integer> pseudoConfmap = new HashMap<>();
        mutation.parameters().forEach(parameter -> {
            pseudoConfmap.put(parameter.key(), parameter.getDefaultVal());
        });

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        mutation.ability().onCraft(stack, registries);

        updateMap();
    }

    public void remove(Mutations mutation, HolderLookup.Provider registries) {
        if (!has(mutation)) {
            return;
        }

        Map<String, MutationComponent.ConfigurationComponent> pseudo = new HashMap<>(mutmap);
        pseudo.remove(mutation.key());
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudo)));

        mutation.ability().decraft(stack, registries);

        updateMap();
    }

    public void configure(Mutations.Parameters parameter, int value) {
        Mutations mutation = Mutations.byParameter(parameter);

        if (!has(mutation)) {
            return;
        }

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        Map<String, Integer> pseudoConfmap = new HashMap<>(pseudoMutmap.get(mutation.key()).confmap());

        pseudoConfmap.put(parameter.key(), value);

        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        updateMap();
    }

    public void nextConfval(Mutations.Parameters parameter, Level level, Stages stage) {
        Mutations mutation = Mutations.byParameter(parameter);

        if (!has(mutation)) {
            return;
        }

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        Map<String, Integer> pseudoConfmap = new HashMap<>(pseudoMutmap.get(mutation.key()).confmap());

        int newval;

        if (parameter == Mutations.Parameters.TECTONIC_AREA) {
            int limit = 1;

            limit = switch (stage) {
                case LATENT -> 3;
                case ADVANCED -> 5;
                case NOBLE -> 7;
                case EXCELSIOR -> 9;
            };

            boolean isApplicable = parameter.next(storedValOrDefault(pseudoConfmap, parameter)) <= limit;
            newval = isApplicable ? parameter.next(storedValOrDefault(pseudoConfmap, parameter)) : parameter.head();
        } else {
            newval = parameter.next(storedValOrDefault(pseudoConfmap, parameter));
        }

        pseudoConfmap.put(
            parameter.key(),
            newval
        );

        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        mutation.ability().onUpdate(stack, level.registryAccess());

        updateMap();
    }

    public void nextConfVal(String key, Level level, Stages stage) {
        nextConfval(Mutations.Parameters.byKey(key), level, stage);
    }

    public void prevConfigVal(Mutations.Parameters parameter, Level level, Stages stage) {
        Mutations mutation = Mutations.byParameter(parameter);

        if (!has(mutation)) {
            return;
        }

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        Map<String, Integer> pseudoConfmap = new HashMap<>(pseudoMutmap.get(mutation.key()).confmap());

        int newval;

        if (parameter == Mutations.Parameters.TECTONIC_AREA) {
            int limit = 1;

            limit = switch (stage) {
                case LATENT -> 3;
                case ADVANCED -> 5;
                case NOBLE -> 7;
                case EXCELSIOR -> 9;
            };

            boolean isApplicable = parameter.prev(storedValOrDefault(pseudoConfmap, parameter)) <= limit;
            newval = isApplicable ? parameter.prev(storedValOrDefault(pseudoConfmap, parameter)) : limit;
        } else {
            newval = parameter.prev(storedValOrDefault(pseudoConfmap, parameter));
        }

        pseudoConfmap.put(
            parameter.key(),
            newval
        );

        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        mutation.ability().onUpdate(stack, level.registryAccess());

        updateMap();
    }

    public void prevConfigVal(String key, Level level, Stages stage) {
        prevConfigVal(Mutations.Parameters.byKey(key), level, stage);
    }

    public void resetConfigVal(Mutations.Parameters parameter, Level level) {
        Mutations mutation = Mutations.byParameter(parameter);

        if (!has(mutation)) {
            return;
        }

        Map<String, MutationComponent.ConfigurationComponent> pseudoMutmap = new HashMap<>(mutmap);
        Map<String, Integer> pseudoConfmap = new HashMap<>(pseudoMutmap.get(mutation.key()).confmap());

        pseudoConfmap.put(
            parameter.key(),
            parameter.head()
        );

        pseudoMutmap.put(mutation.key(), new MutationComponent.ConfigurationComponent(ImmutableMap.copyOf(pseudoConfmap)));
        stack.set(DataComponentTypes.MUTATION_COMPONENT_TYPE, new MutationComponent(ImmutableMap.copyOf(pseudoMutmap)));

        mutation.ability().onUpdate(stack, level.registryAccess());

        updateMap();
    }

    public void resetConfigVal(String key, Level level) {
        resetConfigVal(Mutations.Parameters.byKey(key), level);
    }

    public int getConfigVal(Mutations.Parameters parameter) {
        Mutations mutation = Mutations.byParameter(parameter);

        if (!has(mutation)) {
            return 0;
        }

        ImmutableMap<String, MutationComponent.ConfigurationComponent> currentMutmap = stack.get(DataComponentTypes.MUTATION_COMPONENT_TYPE).mutmap();

        return storedValOrDefault(currentMutmap.get(mutation.key()).confmap(), parameter);
    }

    /**
     * Reads a parameter from a stored config map. Items saved before a parameter was added or renamed
     * lack its key, and a changed value range can leave a value that is no longer an option.
     * Both cases fall back to the parameter's default instead of crashing on a null unbox.
     */
    private static int storedValOrDefault(Map<String, Integer> confmap, Mutations.Parameters parameter) {
        Integer stored = confmap.get(parameter.key());

        if (stored == null || parameter.list().byValue(stored) == null) {
            return parameter.getDefaultVal();
        }

        return stored;
    }

    public int getKills() {
        if (!has(Mutations.INSATIABLE_VORACITY)) {
            return 0;
        }

        return stack.getOrDefault(DataComponentTypes.VORACITY_COMPONENT_TYPE, 0);
    }

    public void increaseKill() {
        if (!has(Mutations.INSATIABLE_VORACITY)) {
            return;
        }

        int newKillsNumber = getKills() + 1;
        stack.set(DataComponentTypes.VORACITY_COMPONENT_TYPE, newKillsNumber);

        updateMap();
    }

    public ItemStack getStack() {
        return stack;
    }

    public List<String> getDisplayNames() {
        List<String> displayNames = new ArrayList<>();

        for (String key : mutmap.keySet()) {
            Mutations pseudo = Mutations.byKey(key);

            if (pseudo != null) {
                displayNames.add(pseudo.title());
            }
        }

        return displayNames;
    }

    public List<Mutations.Parameters> getAllParameters() {
        Set<Mutations> muts = matchMutations();
        List<Mutations.Parameters> parameters = new ArrayList<>();

        for (var mut : muts) {
            for (var param : mut.parameters()) {
                parameters.add(param);
            }
        }

        return parameters;
    }

    public Set<Mutations> matchMutations() {
        MutationComponent component = stack.get(DataComponentTypes.MUTATION_COMPONENT_TYPE);

        Set<String> mutationKeys = new HashSet<>(component.mutmap().keySet());

        Set<Mutations> muts = new HashSet<>();

        // Skip keys of mutations that no longer exist, byKey returns null for them.
        mutationKeys.forEach((key) -> {
            Mutations mutation = Mutations.byKey(key);

            if (mutation != null) {
                muts.add(mutation);
            }
        });

        return muts;
    }

    public void updateMap() {
        this.mutmap = stack.get(DataComponentTypes.MUTATION_COMPONENT_TYPE).mutmap();
    }
}
