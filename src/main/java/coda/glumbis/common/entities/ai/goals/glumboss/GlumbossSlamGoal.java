package coda.glumbis.common.entities.ai.goals.glumboss;

import coda.glumbis.common.entities.GlumbossEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class GlumbossSlamGoal extends Goal {
    protected GlumbossEntity entity;
    private int timer;
    private final int COOLDOWN = 200;
    private int cooldownTimer;
    public GlumbossSlamGoal(GlumbossEntity entity) {
        this.entity = entity;
    }


    @Override
    public boolean canUse() {

        if(this.entity.getTarget() != null){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(this.entity.getState() == 0 || this.entity.getState() == 2) {
            if (this.cooldownTimer < COOLDOWN) {
                this.cooldownTimer++;
            } else {
                if (this.timer <= 60) {
                    this.timer++;
                    this.entity.setState(2);
                    if (this.timer == 35) {
                        this.tryHurtTarget(this.entity, this.entity.distanceTo(this.entity.getTarget()));
                    }
                } else {
                    this.timer = 0;
                    this.cooldownTimer = 0;
                    this.entity.setState(0);
                }
            }
        }
        else{
            this.stop();
        }
    }

    protected void tryHurtTarget(GlumbossEntity entity, double distanceTo){
        if(distanceTo < this.getAttackReachSqr(entity)){
            LivingEntity target = this.entity.getTarget();
            double distanceToGlumboss = target.distanceToSqr(entity);
            float damage = 1 - Mth.sqrt((float) distanceToGlumboss) / 10;
            target.hurt(DamageSource.mobAttack(entity), (0.5F * damage + 0.5F) * 10);
            target.setDeltaMovement(target.getDeltaMovement().add(target.position().normalize().multiply(1.04, 1.4, 1.04)));
        }
    }

    protected double getAttackReachSqr(LivingEntity entity) {
        return (double)(entity.getBbWidth() * 2.0F * entity.getBbWidth() * 2.0F + entity.getBbWidth());
    }
}