package coda.glumbis.common.entities;

import coda.glumbis.common.registry.GlumbisItems;
import coda.glumbis.common.registry.GlumbisSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class BigSockEntity extends Animal implements IAnimatable, IAnimationTickable {
	public static final EntityDataAccessor<Boolean> SLAMMING = SynchedEntityData.defineId(BigSockEntity.class, EntityDataSerializers.BOOLEAN);
	private final AnimationFactory factory = new AnimationFactory(this);
	public float distance = 4F;
	public int attackTick;

	public BigSockEntity(EntityType<? extends Animal> type, Level worldIn) {
		super(type, worldIn);
		this.noCulling = true;
	}

	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(SLAMMING, false);
	}

	public boolean isSlamming() {
		return this.entityData.get(SLAMMING);
	}

	public void setSlamming(boolean slamming) {
		this.entityData.set(SLAMMING, slamming);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!this.isVehicle()) {
			if (player.isShiftKeyDown()) {
				discard();
				spawnAtLocation(new ItemStack(GlumbisItems.BIG_SOCK.get()));
				player.playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.0F);
			}
			else {
				player.startRiding(this);
				player.setYRot(getYRot());
			}
			return super.mobInteract(player, hand);
		}

		return super.mobInteract(player, hand);
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		return PlayState.CONTINUE;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5F).add(Attributes.ATTACK_DAMAGE, 0.0F);
	}

	@Override
	public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
		return false;
	}

	@Override
	protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
		if (!this.level.isClientSide && this.fallDistance > 1.0F && pOnGround) {
			float f = (float)Mth.ceil(this.fallDistance - 1.0F);
			if (!pState.isAir()) {
				double d0 = Math.min(0.2F + f / 15.0F, 2.5D);
				int i = (int)(1000D * d0);
				if (!pState.addLandingEffects((ServerLevel)this.level, pPos, pState, this, i)) {
					((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, pState).setPos(pPos), this.getX(), this.getY(), this.getZ(), i, 0.0D, 0.0D, 0.0D, 0.15D);
				}
			}
		}

		super.checkFallDamage(pY, pOnGround, pState, pPos);
	}

	private void slam() {
		List<Entity> list = level.getEntities(this, getBoundingBox().inflate(6.0D));

		setSlamming(false);

		attackTick = 100;

		playSound(GlumbisSounds.BIG_SOCK_SLAM.get(), 2.0F, 1.0F);

		for (Entity entity : list) {
			if (getControllingPassenger() instanceof Player player && entity != player && entity != this && entity instanceof LivingEntity) {

				entity.hurt(DamageSource.mobAttack(this), Math.min(distance - (float) entity.getY() * 0.66F, 10.0F));
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (isVehicle()) {
			LivingEntity passenger = (LivingEntity) this.getControllingPassenger();

			if (this.attackTick > 0) {
				--this.attackTick;
			}

			this.setYRot(passenger.yBodyRot);
		}

		if (isSlamming() && isOnGround()) {
			slam();
		}
	}

	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		return pSource == DamageSource.OUT_OF_WORLD && super.hurt(pSource, pAmount);
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return new ItemStack(GlumbisItems.BIG_SOCK.get());
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	public void travel(Vec3 pos) {
		if (this.isAlive()) {
			if (this.isVehicle()) {
				this.setSpeed(0.3F);

				super.travel(jump(pos));
			}
			else {
				super.travel(Vec3.ZERO);
			}
		}
	}

	@Override
	public boolean canBeRiddenInWater(Entity rider) {
		return true;
	}

	private Vec3 jump(Vec3 pos) {
		if (!(getControllingPassenger() instanceof LivingEntity)) return Vec3.ZERO;

		LivingEntity passenger = (LivingEntity) this.getControllingPassenger();
		float f1 = passenger.zza;
		if (f1 <= 0.0F) {
			f1 *= 0.25F;
		}

		float distance = 2.0F;
		double x, z;

		if (f1 > 0 && isOnGround()) {
			float yRot = passenger.getViewYRot(1.0F);

			x = -Mth.sin((float) (yRot * Math.PI/180F)) * distance;
			z = Mth.cos((float) (yRot * Math.PI/180F)) * distance;

			setDeltaMovement(x, distance * 0.45, z);
			playSound(GlumbisSounds.BIG_SOCK_JUMP.get(), Math.min(random.nextFloat() + 0.4F, 1.0F), 1.0F);
		}
		else if (f1 < 0 && isOnGround()) {
			float yRot = passenger.getViewYRot(1.0F);

			x = Mth.sin((float) (yRot * Math.PI/180F)) * (distance / 2);
			z = -Mth.cos((float) (yRot * Math.PI/180F)) * (distance / 2);

			setDeltaMovement(x, distance * 0.45, z);
			playSound(GlumbisSounds.BIG_SOCK_JUMP.get(), Math.min(random.nextFloat() + 0.4F, 1.0F), 1.0F);
		}

		if (isInWater() && !isOnGround()) {
			return new Vec3(0, pos.y, 0);
		}

		return new Vec3(0, pos.y, f1);
	}

	@Override
	public double getPassengersRidingOffset() {
		return 1.0;
	}

	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
	}

	@Override
	public boolean canBeControlledByRider() {
		return true;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
	}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
		return null;
	}

	@Override
	public int tickTimer() {
		return tickCount;
	}
}
