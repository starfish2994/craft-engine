package net.momirealms.craftengine.core.pack;

import org.joml.Vector3f;

public final class CubeRotationProcessor {
    private static final float MIN = -16.0f;
    private static final float MAX = 32.0f;

    private CubeRotationProcessor() {}

    public static RotationResult processWithOriginShift(Vector3f from, Vector3f to, Vector3f origin, String axis, float angle) {
        float normalized = normalizeAngle(angle);
        int steps = Math.round(normalized / 90.0f);
        float finalAngle = normalized - (steps * 90.0f);

        Vector3f finalFrom = new Vector3f(from);
        Vector3f finalTo = new Vector3f(to);
        Vector3f finalOrigin = new Vector3f(origin);

        if (steps != 0) {
            // 如果我们物理上旋转了坐标，origin 必须保持在相对于方块几何中心的相同相对位置。
            applyPivotRotation(finalFrom, finalTo, finalOrigin, axis, steps);
        }

        // 即使移动了 origin，如果方块本身跨度 > 48，依然无法合规
        // 此时只能进行最后的强制截断
        clampVector(finalFrom);
        clampVector(finalTo);

        return new RotationResult(finalFrom, finalTo, finalOrigin, finalAngle, steps != 0);
    }

    private static void applyPivotRotation(Vector3f from, Vector3f to, Vector3f origin, String axis, int steps) {
        boolean clockwise = steps > 0;
        int count = Math.abs(steps);

        for (int i = 0; i < count; i++) {
            // 旋转坐标点 (绕当前 origin)
            rotatePoint90(from, origin, axis, clockwise);
            rotatePoint90(to, origin, axis, clockwise);

            // 重新校准 min/max 以满足 from < to
            rectifyBounds(from, to);

            // 如果旋转后坐标溢出了（比如到了 33），我们可以整体平移坐标和 origin
            // 只要 (newTo - newFrom) <= 48，我们就一定能找到一个偏移量把它挪回 [-16, 32]
            shiftBackToValidRange(from, to, origin);
        }
    }

    private static void shiftBackToValidRange(Vector3f from, Vector3f to, Vector3f origin) {
        Vector3f shift = new Vector3f(0);

        if (from.x < MIN) shift.x = MIN - from.x;
        else if (to.x > MAX) shift.x = MAX - to.x;

        if (from.y < MIN) shift.y = MIN - from.y;
        else if (to.y > MAX) shift.y = MAX - to.y;

        if (from.z < MIN) shift.z = MIN - from.z;
        else if (to.z > MAX) shift.z = MAX - to.z;

        from.add(shift);
        to.add(shift);
        origin.add(shift);
    }

    private static void rotatePoint90(Vector3f p, Vector3f o, String axis, boolean cw) {
        float x = p.x - o.x, y = p.y - o.y, z = p.z - o.z;
        switch (axis.toLowerCase()) {
            case "x" -> { if (cw) p.set(p.x, o.y - z, o.z + y); else p.set(p.x, o.y + z, o.z - y); }
            case "y" -> { if (cw) p.set(o.x + z, p.y, o.z - x); else p.set(o.x - z, p.y, o.z + x); }
            case "z" -> { if (cw) p.set(o.x - y, o.y + x, p.z); else p.set(o.x + y, o.y - x, p.z); }
        }
    }

    private static void rectifyBounds(Vector3f from, Vector3f to) {
        float x1 = Math.min(from.x, to.x), x2 = Math.max(from.x, to.x);
        float y1 = Math.min(from.y, to.y), y2 = Math.max(from.y, to.y);
        float z1 = Math.min(from.z, to.z), z2 = Math.max(from.z, to.z);
        from.set(x1, y1, z1);
        to.set(x2, y2, z2);
    }

    private static void clampVector(Vector3f v) {
        v.x = Math.clamp(v.x, MIN, MAX);
        v.y = Math.clamp(v.y, MIN, MAX);
        v.z = Math.clamp(v.z, MIN, MAX);
    }

    private static float normalizeAngle(float a) {
        return ((a + 180) % 360 + 360) % 360 - 180;
    }

    public record RotationResult(Vector3f from, Vector3f to, Vector3f origin, float angle, boolean shifted) {}
}