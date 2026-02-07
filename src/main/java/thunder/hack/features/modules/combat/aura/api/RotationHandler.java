package thunder.hack.features.modules.combat.aura.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface RotationHandler {
    Vec3d getRotationVector(Entity entity, Vec3d currentRotation);
    boolean isSnap();
}
