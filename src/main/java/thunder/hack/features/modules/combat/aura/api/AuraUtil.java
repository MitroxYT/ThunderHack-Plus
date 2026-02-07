package thunder.hack.features.modules.combat.aura.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.manager.IManager;

public class AuraUtil implements IManager {
    public static Vec3d getVector(Entity target) {
        double wHalf = target.getWidth() / 2;
        double yExpand = clamp(target.getEyeY() - target.getPos().getY(), 0, target.getHeight());
        double xExpand = clamp(mc.player.getPos().getX() - target.getPos().getX(), -wHalf, wHalf);
        double zExpand = clamp(mc.player.getPos().getZ() - target.getPos().getZ(), -wHalf, wHalf);

        return new Vec3d(
                target.getPos().getX() - mc.player.getPos().getX() + xExpand / 1.1,
                target.getPos().getY() - mc.player.getEyeY() + yExpand,
                target.getPos().getZ() - mc.player.getPos().getZ() + zExpand / 1.1
        );
    }
    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
}
