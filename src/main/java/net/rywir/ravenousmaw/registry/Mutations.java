package net.rywir.ravenousmaw.registry;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.system.ability.*;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.dstruct.CircularList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum Mutations {
    TECTONIC_BITE       (Stages.LATENT,     Items.SLIME_BALL,               Set.of(),                       new TectonicBite(),         List.of(Parameters.TECTONIC_AREA)),
    ELECTRIC_MENDING    (Stages.LATENT,     Items.RAW_COPPER,               Set.of(AbilityTypes.ON_CRAFT),  new ElectricMending(),      List.of(Parameters.MAGNETIC_RANGE, Parameters.MAGNETIC_FIELD)),

    COMBUSTIVE_ENZYME   (Stages.ADVANCED,   Items.MAGMA_CREAM,              Set.of(),                       new CombustiveEnzyme(),     List.of()),
    MENTAL_SUPREMACY    (Stages.ADVANCED,   Items.HEART_OF_THE_SEA,         Set.of(),                       new MentalSupremacy(),      List.of(Parameters.DREADFUL_GLANCE)),
    IRIS_OUT            (Stages.ADVANCED,   Items.BREEZE_ROD,               Set.of(),                       new IrisOut(),              List.of(Parameters.LIVING_PROJECTILE, Parameters.GRIM_TRACER)),

    UNDYING_FLESH       (Stages.NOBLE,      Items.TOTEM_OF_UNDYING,         Set.of(AbilityTypes.ON_CRAFT),  new UndyingFlesh(),         List.of()),
    SYMBIOTIC_AID       (Stages.NOBLE,      Items.PUFFERFISH,               Set.of(),                       new SymbioticAid(),         List.of(Parameters.SYMBIOTIC_IMMUNITY)),
    TENDRIL_LASH        (Stages.NOBLE,      Items.ANGLER_POTTERY_SHERD,     Set.of(AbilityTypes.ON_CRAFT),  new TendrilLash(),          List.of()),
    INSATIABLE_VORACITY (Stages.NOBLE,      Items.ENCHANTED_GOLDEN_APPLE,   Set.of(),                       new InsatiableVoracity(),   List.of()),

    RESONANT_RENDING    (Stages.EXCELSIOR,  Items.GHAST_TEAR,               Set.of(),                       new ResonantRending(),      List.of()),
    ARCANE_HYPERTROPHY  (Stages.EXCELSIOR,  Items.PRISMARINE_SHARD,         Set.of(),                       new ArcaneHypertrophy(),    List.of()),
    INDOMITABLE_WILL    (Stages.EXCELSIOR,  Items.DRAGON_BREATH,            Set.of(),                       new IndomitableWill(),      List.of()),
    PERPETUAL_MOTION    (Stages.EXCELSIOR,  Items.RABBIT_FOOT,              Set.of(),                       new PerpetualMotion(),      List.of()),
    ADAPTIVE_SHIFT      (Stages.EXCELSIOR,  Items.NETHER_STAR,              Set.of(AbilityTypes.ON_CRAFT),  new AdaptiveShift(),        List.of(Parameters.SILKY_FANG, Parameters.RECKLESS_DEVOURER, Parameters.EXCAVATION_HASTE));

    private final Component                 title;
    private final String                    key;
    private final Stages                    stage;
    private final Item                      mutagen;
    private final Set<AbilityTypes>         types;
    private final IMutationAbility          ability;
    private final ImmutableList<Parameters> parameters;

    private static final Map<String, Mutations>     BY_KEY = new HashMap<>();
    private static final Map<Item, Mutations>       BY_MUTAGEN = new HashMap<>();
    private static final Set<Mutations>             ON_CRAFT_TYPE_MEMBERS = new HashSet<>();
    private static final Map<Parameters, Mutations> BY_PARAMETER = new EnumMap<>(Parameters.class);

    Mutations(Stages stage, Item mutagen, Set<AbilityTypes> types, IMutationAbility ability, List<Parameters> parameters) {
        this.key = this.name().toLowerCase(Locale.ROOT);
        this.title = Component.translatable("mutation.ravenousmaw." + this.key);
        this.stage = stage;
        this.mutagen = mutagen;
        this.types = types;
        this.ability = ability;
        this.parameters = ImmutableList.copyOf(parameters);
    }

    public @NotNull String title() {
        return title.getString();
    }

    public String key() {
        return key;
    }

    public Stages stage() {
        return stage;
    }

    public Item mutagen() {
        return mutagen;
    }

    public IMutationAbility ability() {
        return ability;
    }

    public ImmutableList<Parameters> parameters() {
        return parameters;
    }

    public static @NotNull Set<String> titles() {
        Set<String> pseudo = new HashSet<>();

        for (var val : values()) {
            pseudo.add(val.title());
        }

        return pseudo;
    }

    public static Set<Mutations> getOnCraftTypeMembers() {
        return ON_CRAFT_TYPE_MEMBERS;
    }

    public static Mutations byMutagen(Item mutagen) {
        return BY_MUTAGEN.get(mutagen);
    }

    public static Mutations byKey(String key) {
        return BY_KEY.get(key);
    }

    public static Mutations byParameter(Parameters parameter) {
        return BY_PARAMETER.get(parameter);
    }

    static {
        for (Mutations mutation : values()) {
            if (mutation.types.contains(AbilityTypes.ON_CRAFT)) {
                ON_CRAFT_TYPE_MEMBERS.add(mutation);
            }
        }
    }

    static {
        for (Mutations mutation : values()) {
            BY_KEY.put(mutation.key(), mutation);
        }
    }

    static {
        for (Mutations mutation : values()) {
            BY_MUTAGEN.put(mutation.mutagen, mutation);
        }
    }

    static {
        for (Mutations mutation : values()) {
            for (Parameters parameter : mutation.parameters) {
                if (BY_PARAMETER.put(parameter, mutation) != null) {
                    throw new IllegalStateException(
                        "Parameter " + parameter.name() + " is assigned to more than one Mutation!"
                    );
                }
            }
        }
    }

    public enum AbilityTypes {
        ON_CRAFT
    }

    public enum Parameters {
        TECTONIC_AREA       (Type.VALUE,    CircularList.of(1, 3, 5, 7, 9)),
        SYMBIOTIC_IMMUNITY  (Type.TOGGLE,   CircularList.of(1, 0)),
        SILKY_FANG          (Type.TOGGLE,   CircularList.of(0, 1)),
        EXCAVATION_HASTE    (Type.VALUE,    CircularList.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
        LIVING_PROJECTILE   (Type.TOGGLE,   CircularList.of(0, 1)),
        GRIM_TRACER         (Type.TOGGLE,   CircularList.of(0, 1)),
        RECKLESS_DEVOURER   (Type.TOGGLE,   CircularList.of(0, 1)),
        MAGNETIC_FIELD      (Type.TOGGLE,   CircularList.of(0, 1)),
        MAGNETIC_RANGE      (Type.VALUE,    CircularList.of(4, 5, 6, 7, 8)),
        DREADFUL_GLANCE     (Type.TOGGLE,   CircularList.of(0, 1)),
        // Owned by no mutation: the Eldritch Antenna switch lives on the player (see AssimilationHandler), not on the item
        ELDRITCH_ANTENNA    (Type.TOGGLE,   CircularList.of(1, 0));

        private final String            key;
        private final Component         title;
        private final Component         description;
        private final Identifier  icon;
        private final CircularList      values;
        private final Type              type;

        private static final Map<String, Parameters> BY_KEY = new HashMap<>();

        Parameters(Type type, CircularList values) {
            this.key = this.name().toLowerCase(Locale.ROOT);
            this.title = Component.translatable("parameter.ravenousmaw." + this.key);
            this.type = type;
            this.description = Component.translatable("description.ravenousmaw." + this.key);
            this.icon = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/icon/" + this.key + ".png");
            this.values = values;
        }

        public Component title() {
            return title;
        }

        public String key() {
            return key;
        }

        public Component description() {
            return description;
        }

        public CircularList revolver() {
            return values;
        }

        public Identifier icon() {
            return icon;
        }

        public CircularList list() {
            return this.values;
        }

        public Type type() {
            return type;
        }

        public int getDefaultVal() {
            return this.values.head().val();
        }

        public int next(int i) {
            return values.byValue(i).next().val();
        }

        public int prev(int i) {
            return values.byValue(i).prev().val();
        }

        public int head() {
            return list().head().val();
        }

        public static Parameters byKey(String key) {
            return BY_KEY.get(key);
        }

        static {
            for (Parameters parameter : values()) {
                BY_KEY.put(parameter.key(), parameter);
            }
        }

        public enum Type {
            TOGGLE,
            VALUE
        }
    }
}
