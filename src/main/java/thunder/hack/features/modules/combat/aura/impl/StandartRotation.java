package thunder.hack.features.modules.combat.aura.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thunder.hack.features.modules.combat.aura.api.AuraUtil;
import thunder.hack.features.modules.combat.aura.api.RotationHandler;

import static thunder.hack.core.manager.IManager.mc;

public class StandartRotation implements RotationHandler {
    @Override
    public Vec3d getRotationVector(Entity entity, Vec3d currentRotation) {
        Vec3d targetVector = AuraUtil.getVector(entity);
        if (currentRotation == null) currentRotation = targetVector;
        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(targetVector.z, targetVector.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(targetVector.y, Math.hypot(targetVector.x, targetVector.z))) + 12);
        float yawDelta = (float) MathHelper.wrapDegrees(yawToTarget - currentRotation.x);
        float pitchDelta = (float) MathHelper.wrapDegrees(pitchToTarget - currentRotation.y);
        float yaw = Math.min(Math.max(Math.abs(yawDelta), 3), 9999.0f);
        float pitch = Math.min(Math.max(Math.abs(pitchDelta), 4), 9999.0f);
        float yaw33 = (float) (currentRotation.x + (yawDelta > 1.6f ? yaw : -yaw));
        float gcd = (float) (currentRotation.x + (yawDelta > 1.0f ? yaw : -yaw));
        pitch = (float) MathHelper.clamp(currentRotation.y + (pitchDelta > 1.0f ? pitch : -pitch), -91, 89);
        double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
        gcd -= (yaw33 - currentRotation.x) % gcdFix;
        pitch -= (pitch - currentRotation.y) % gcdFix;
        return new Vec3d(gcd, pitch,0);
    }

    @Override
    public boolean isSnap() {
        return false;
    }
}
