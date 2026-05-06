package net.momirealms.craftengine.core.pack;

import org.joml.Vector3f;
import java.util.EnumMap;
import java.util.Map;

public final class CubeRotationProcessor {
    private static final float MIN = -16.0f;
    private static final float MAX = 32.0f;

    public enum Face {
        down, up, north, south, west, east
    }

    public record RotationResult(
            Vector3f from,
            Vector3f to,
            Vector3f origin,
            float angle,
            Map<Face, FaceData> faceChanges
    ) {}

    public record FaceData(Face targetFace, int uvRotation) {}

    public static RotationResult process(Vector3f from, Vector3f to, Vector3f origin, String axis, float angle) {
        float normalized = normalizeAngle(angle);
        int steps = Math.round(normalized / 90.0f);
        float finalAngle = normalized - (steps * 90.0f);

        Vector3f fFrom = new Vector3f(from);
        Vector3f fTo = new Vector3f(to);
        Vector3f fOrigin = new Vector3f(origin);

        Map<Face, FaceData> faceChanges = new EnumMap<>(Face.class);
        for (Face f : Face.values()) {
            faceChanges.put(f, new FaceData(f, 0));
        }

        if (steps != 0) {
            int count = Math.abs(steps);
            boolean cw = steps > 0;

            for (int i = 0; i < count; i++) {
                rotatePhysical(fFrom, fTo, fOrigin, axis, cw);
                updateFaceMapping(faceChanges, axis, cw);
            }
        }

        clampVector(fFrom);
        clampVector(fTo);

        return new RotationResult(fFrom, fTo, fOrigin, finalAngle, faceChanges);
    }

    private static void rotatePhysical(Vector3f from, Vector3f to, Vector3f origin, String axis, boolean cw) {
        rotatePoint90(from, origin, axis, cw);
        rotatePoint90(to, origin, axis, cw);

        float x1 = Math.min(from.x, to.x), x2 = Math.max(from.x, to.x);
        float y1 = Math.min(from.y, to.y), y2 = Math.max(from.y, to.y);
        float z1 = Math.min(from.z, to.z), z2 = Math.max(from.z, to.z);
        from.set(x1, y1, z1);
        to.set(x2, y2, z2);

        Vector3f shift = new Vector3f(0);
        if (from.x < MIN) shift.x = MIN - from.x; else if (to.x > MAX) shift.x = MAX - to.x;
        if (from.y < MIN) shift.y = MIN - from.y; else if (to.y > MAX) shift.y = MAX - to.y;
        if (from.z < MIN) shift.z = MIN - from.z; else if (to.z > MAX) shift.z = MAX - to.z;

        from.add(shift);
        to.add(shift);
        origin.add(shift);
    }

    private static void updateFaceMapping(Map<Face, FaceData> currentMap, String axis, boolean cw) {
        Map<Face, Face> nextPosMap = new EnumMap<>(Face.class);
        Map<Face, Integer> rotPatch = new EnumMap<>(Face.class);

        switch (axis.toLowerCase()) {
            case "y" -> {
                link(nextPosMap, rotPatch, cw, Face.north, Face.east, Face.south, Face.west);
                nextPosMap.put(Face.up, Face.up);
                nextPosMap.put(Face.down, Face.down);
                rotPatch.put(Face.up, cw ? 90 : 270);
                rotPatch.put(Face.down, cw ? 270 : 90);
            }
            case "x" -> {
                link(nextPosMap, rotPatch, cw, Face.up, Face.north, Face.down, Face.south);
                nextPosMap.put(Face.east, Face.east);
                nextPosMap.put(Face.west, Face.west);
                rotPatch.put(Face.east, cw ? 90 : 270);
                rotPatch.put(Face.west, cw ? 270 : 90);
                rotPatch.put(Face.north, 0);
                rotPatch.put(Face.south, 0);
            }
            case "z" -> {
                link(nextPosMap, rotPatch, cw, Face.up, Face.east, Face.down, Face.west);
                nextPosMap.put(Face.north, Face.north);
                nextPosMap.put(Face.south, Face.south);
                rotPatch.put(Face.north, cw ? 90 : 270);
                rotPatch.put(Face.south, cw ? 270 : 90);
            }
        }

        Map<Face, FaceData> nextStep = new EnumMap<>(Face.class);
        for (var entry : currentMap.entrySet()) {
            Face originalFace = entry.getKey();
            FaceData currentData = entry.getValue();

            Face nextFace = nextPosMap.getOrDefault(currentData.targetFace(), currentData.targetFace());
            int additionalRot = rotPatch.getOrDefault(currentData.targetFace(), 0);

            nextStep.put(originalFace, new FaceData(nextFace, (currentData.uvRotation() + additionalRot) % 360));
        }
        currentMap.clear();
        currentMap.putAll(nextStep);
    }

    private static void link(Map<Face, Face> pos, Map<Face, Integer> rots, boolean cw, Face f1, Face f2, Face f3, Face f4) {
        Face[] c = {f1, f2, f3, f4};
        for (int i = 0; i < 4; i++) {
            int next = cw ? (i + 1) % 4 : (i + 3) % 4;
            pos.put(c[i], c[next]);
            rots.put(c[i], 0);
        }
    }

    private static void rotatePoint90(Vector3f p, Vector3f o, String axis, boolean cw) {
        float x = p.x - o.x, y = p.y - o.y, z = p.z - o.z;
        switch (axis.toLowerCase()) {
            case "x" -> { if (cw) p.set(p.x, o.y - z, o.z + y); else p.set(p.x, o.y + z, o.z - y); }
            case "y" -> { if (cw) p.set(o.x + z, p.y, o.z - x); else p.set(o.x - z, p.y, o.z + x); }
            case "z" -> { if (cw) p.set(o.x - y, o.y + x, p.z); else p.set(o.x + y, o.y - x, p.z); }
        }
    }

    private static void clampVector(Vector3f v) {
        v.x = Math.clamp(v.x, MIN, MAX);
        v.y = Math.clamp(v.y, MIN, MAX);
        v.z = Math.clamp(v.z, MIN, MAX);
    }

    private static float normalizeAngle(float a) {
        return ((a + 180) % 360 + 360) % 360 - 180;
    }
}