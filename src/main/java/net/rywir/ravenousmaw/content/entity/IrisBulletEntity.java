package net.rywir.ravenousmaw.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousEntityTypes;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;
import org.jetbrains.annotations.NotNull;

import org.jspecify.annotations.Nullable;
import java.util.List;

public class IrisBulletEntity extends AbstractArrow {
    private static final float HOMING_STRENGTH = 0.12F;
    private static final float MAX_HOMING_DISTANCE = 64.0F;
    private static final float MIN_SPEED = 0.5F;
    private static final float MAX_SPEED = 4.5F;
    private static final double CONE_EXPANSION = 2.0;
    private static final double DEFAULT_GRAVITY = 0.01;
    private static final int DESTRUCTION_TICKS = 20 * 2;

    private MutationHandler handler;
    private AbilityDispatcher dispatcher;
    private Stages stage;
    private ItemStack stack;
    private int onGroundTicks;

    @Nullable
    private LivingEntity target;

    private static final EntityDataAccessor<Float> POWER =
        SynchedEntityData.defineId(IrisBulletEntity.class, EntityDataSerializers.FLOAT);

    // Synced so the client copy (built from the spawn packet) knows to draw the tracing particles.
    private static final EntityDataAccessor<Boolean> TRACING =
        SynchedEntityData.defineId(IrisBulletEntity.class, EntityDataSerializers.BOOLEAN);

    public IrisBulletEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public IrisBulletEntity(LivingEntity shooter, Level level, boolean tracing, MutationHandler handler, AbilityDispatcher dispatcher, Stages stage, ItemStack stack) {
        super(
            RavenousEntityTypes.IRIS_BULLET_ENTITY_TYPE.get(),
            shooter,
            level,
            new ItemStack(Items.ARROW),
            shooter.getWeaponItem()
        );
        this.setNoGravity(tracing);

        this.entityData.set(TRACING, tracing);
        this.handler = handler;
        this.stage = stage;
        this.dispatcher = dispatcher;
        this.stack = stack;
    }

    @Override
    protected double getDefaultGravity() {
        return DEFAULT_GRAVITY;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POWER, 1.0F);
        builder.define(TRACING, false);
    }

    public boolean isTracing() {
        return this.entityData.get(TRACING);
    }

    public void setPower(float power) {
        this.entityData.set(POWER, power);
    }

    public float getPower() {
        return this.entityData.get(POWER);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (isTracing()) {
                this.level().addParticle(
                    ParticleTypes.ENCHANT,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0
                );
            }
            return;
        }

        // A bullet loaded from an old save (before noSave) has no ability state; it cannot hit anything.
        if (dispatcher == null) {
            this.discard();
            return;
        }

        if (this.inGround || this.tickCount > 200) {
            onGroundTicks++;

            if (onGroundTicks >= DESTRUCTION_TICKS) {
                this.discard();
                return;
            }

            return;
        }

        if (!isTracing()) {
            return;
        }

        if (target == null || !target.isAlive() || target.isRemoved() || this.distanceTo(target) > MAX_HOMING_DISTANCE) {
            target = lock();
        }

        if (target != null) {
            homeTowards(target);
        }
    }

    private void homeTowards(LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 toTarget = targetPos.subtract(this.position());

        if (toTarget.lengthSqr() < 0.25) return;

        Vec3 desiredDir = toTarget.normalize();
        Vec3 velocity = this.getDeltaMovement();

        double speed = Math.max(velocity.length(), MIN_SPEED);

        Vec3 currentDir = velocity.lengthSqr() > 1e-4
            ? velocity.normalize()
            : desiredDir;

        Vec3 newDir = currentDir
            .lerp(desiredDir, HOMING_STRENGTH)
            .normalize();

        speed = Math.min(speed + 0.05, MAX_SPEED);

        this.setDeltaMovement(newDir.scale(speed));
        updateRotation(newDir);
    }

    private void updateRotation(Vec3 dir) {
        float yaw = (float)(Math.atan2(dir.x, dir.z) * (180 / Math.PI));
        float pitch = (float)(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180 / Math.PI));
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Nullable
    private LivingEntity lock() {
        LivingEntity owner = (LivingEntity)this.getOwner();
        if (owner == null) return findNearestEntity();

        Vec3 eye = owner.getEyePosition();
        Vec3 look = owner.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(MAX_HOMING_DISTANCE));

        AABB box = new AABB(eye, end).inflate(CONE_EXPANSION);

        List<LivingEntity> list = this.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e != owner && e.isAlive() && !e.isSpectator()
        );

        LivingEntity best = null;
        double bestScore = -Double.MAX_VALUE;

        for (LivingEntity e : list) {
            if (!hasLineOfSight(eye, e)) continue;

            Vec3 dir = e.position().subtract(owner.position()).normalize();
            double score = look.dot(dir);

            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }

        return best;
    }

    private boolean hasLineOfSight(Vec3 from, @NotNull LivingEntity target) {
        Vec3[] points = {
            target.getEyePosition(),
            target.position().add(0, target.getBbHeight() * 0.5, 0),
            target.position().add(0, 0.1, 0)
        };

        for (Vec3 p : points) {
            ClipContext ctx = new ClipContext(
                from, p,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                target
            );
            BlockHitResult hit = this.level().clip(ctx);
            if (hit.getType() == HitResult.Type.MISS ||
                hit.getLocation().distanceToSqr(p) < 0.5) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private LivingEntity findNearestEntity() {
        return this.level().getNearestEntity(
            LivingEntity.class,
            TargetingConditions.forCombat().range(MAX_HOMING_DISTANCE),
            null,
            this.getX(), this.getY(), this.getZ(),
            this.getBoundingBox().inflate(MAX_HOMING_DISTANCE)
        );
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (this.level().isClientSide) return;

        Entity hit = result.getEntity();
        if (hit instanceof EnderDragonPart part) {
            hit = part.parentMob;
        }

        if (hit instanceof LivingEntity entity) {
            float bonus = this.dispatcher.getAttackDamageBonus(handler, stack, entity, level(), stage);
            RavenousMaw.LOGGER.debug("Bonus damage: {}", bonus);

            entity.hurt(
                this.damageSources().thrown(this, this.getOwner()),
                (2.0F + bonus) * dispatcher.getDamageMultiplier(handler) * getPower()
            );

            if (handler.has(Mutations.COMBUSTIVE_ENZYME)) {
                Mutations.COMBUSTIVE_ENZYME.ability().onAttack(stack, entity, level());
            }

            this.discard();
        }
    }

    @Override
    protected boolean tryPickup(@NotNull net.minecraft.world.entity.player.Player player) {
        return false;
    }
}