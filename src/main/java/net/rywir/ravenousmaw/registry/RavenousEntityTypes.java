package net.rywir.ravenousmaw.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.entity.IrisBulletEntity;

import java.util.function.Supplier;

public class RavenousEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, RavenousMaw.MOD_ID);

    public static final Supplier<EntityType<IrisBulletEntity>> IRIS_BULLET_ENTITY_TYPE = ENTITY_TYPES.register("iris_bullet",
        () -> EntityType.Builder.<IrisBulletEntity>of(IrisBulletEntity::new, MobCategory.MISC)
            // The bullet holds live references (handler, dispatcher, stack) that cannot be
            // serialized, so it must never be written to disk. It simply vanishes on unload.
            .noSave()
            .sized(0.5f, 1.15f).build("iris_bullet")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
