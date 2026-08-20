package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.concurrent.ThreadLocalRandom;

public final class ViewPointSelector {

    private ViewPointSelector() {
    }

    public record Point3D(double x, double y, double z) {
    }

    /**
     * 在碰撞箱圆柱（底面中心 target，半径 radius，高 height）上，
     * 取视线与圆柱第一个交点附近的随机点
     */
    public static Point3D findViewIntersection(Point3D camera, Point3D direction, Point3D target,
                                               double radius, double height,
                                               double angleSpreadDeg, double heightSpread) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double ox = camera.x() - target.x();
        double oz = camera.z() - target.z();
        double dx = direction.x();
        double dy = direction.y();
        double dz = direction.z();
        Double t = null;
        double a = dx * dx + dz * dz;
        if (a > 1e-12) {
            double b = 2 * (ox * dx + oz * dz);
            double c = ox * ox + oz * oz - radius * radius;
            double discriminant = b * b - 4 * a * c;
            if (discriminant >= 0) {
                double tSide = (-b - Math.sqrt(discriminant)) / (2 * a);
                if (tSide >= 0) {
                    double y = camera.y() + dy * tSide;
                    if (y >= target.y() && y <= target.y() + height) {
                        t = tSide;
                    }
                }
            }
        }
        if (Math.abs(dy) > 1e-12) {
            for (double capY : new double[]{target.y(), target.y() + height}) {
                double tCap = (capY - camera.y()) / dy;
                if (tCap >= 0 && (t == null || tCap < t)) {
                    double px = ox + dx * tCap;
                    double pz = oz + dz * tCap;
                    if (px * px + pz * pz <= radius * radius) {
                        t = tCap;
                    }
                }
            }
        }
        double angle;
        double hitHeight;
        if (t != null) {
            angle = Math.atan2(oz + dz * t, ox + dx * t);
            hitHeight = camera.y() + dy * t - target.y();
        } else {
            angle = Math.atan2(oz, ox);
            hitHeight = height / 2;
        }
        double finalAngle = angle + (random.nextDouble() - 0.5) * 2 * MiscUtils.toRadians((float) angleSpreadDeg);
        double finalHeight = Math.max(0, Math.min(height, hitHeight + (random.nextDouble() - 0.5) * 2 * heightSpread * height));
        return new Point3D(
                target.x() + radius * MiscUtils.cos((float) finalAngle),
                target.y() + finalHeight,
                target.z() + radius * MiscUtils.sin((float) finalAngle)
        );
    }
}
