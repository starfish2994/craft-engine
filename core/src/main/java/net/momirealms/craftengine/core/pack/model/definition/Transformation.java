package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@SuppressWarnings("DataFlowIssue") // 通过判断 isMatrix 确保空安全
public final class Transformation {
    private static final String[] RIGHT_ROTATION = new String[]{"right_rotation", "right-rotation"};
    private static final String[] LEFT_ROTATION = new String[]{"left_rotation", "left-rotation"};
    private static final Either<Pair<Float, Vector3f>, Quaternionf> DEFAULT_ROTATION = Either.right(ConfigConstants.ZERO_QUATERNION);
    public final Either<Pair<Float, Vector3f>, Quaternionf> rightRotation;
    public final Either<Pair<Float, Vector3f>, Quaternionf> leftRotation;
    public final Vector3f scale;
    public final Vector3f translation;
    public final List<Float> transformation;
    public final boolean isMatrix;

    public Transformation(Either<Pair<Float, Vector3f>, Quaternionf> rightRotation, Either<Pair<Float, Vector3f>, Quaternionf> leftRotation, Vector3f scale, Vector3f translation) {
        this.rightRotation = rightRotation;
        this.leftRotation = leftRotation;
        this.scale = scale;
        this.translation = translation;
        this.transformation = null;
        this.isMatrix = false;
    }

    public Transformation(List<Float> transformation) {
        this.rightRotation = null;
        this.leftRotation = null;
        this.scale = null;
        this.translation = null;
        this.transformation = transformation;
        this.isMatrix = true;
    }

    public static Transformation fromValue(ConfigValue value) {
        if (value.is(List.class)) {
            return new Transformation(value.getAsFixedSizeList(16, ConfigValue::getAsFloat));
        }
        ConfigSection section = value.getAsSection();
        Either<Pair<Float, Vector3f>, Quaternionf> rightRotation = section.getValue(RIGHT_ROTATION, Transformation::parseRotation, DEFAULT_ROTATION);
        Either<Pair<Float, Vector3f>, Quaternionf> leftRotation = section.getValue(LEFT_ROTATION, Transformation::parseRotation, DEFAULT_ROTATION);
        Vector3f scale = section.getVector3f("scale", ConfigConstants.NORMAL_SCALE);
        Vector3f translation = section.getVector3f("translation", ConfigConstants.ZERO_VECTOR3);
        return new Transformation(rightRotation, leftRotation, scale, translation);
    }

    public static Transformation fromJson(JsonElement json) {
        if (json.isJsonArray()) {
            List<Float> list = json.getAsJsonArray().asList().stream().map(JsonElement::getAsFloat).toList();
            if (list.size() != 16) throw new IllegalArgumentException();
            return new Transformation(list);
        }
        JsonObject jsonObject = json.getAsJsonObject();
        Either<Pair<Float, Vector3f>, Quaternionf> rightRotation = parseRotation(jsonObject.get("right_rotation"));
        Either<Pair<Float, Vector3f>, Quaternionf> leftRotation = parseRotation(jsonObject.get("left_rotation"));
        JsonArray scaleArray = jsonObject.getAsJsonArray("scale");
        Vector3f scale = new Vector3f(scaleArray.get(0).getAsFloat(), scaleArray.get(1).getAsFloat(), scaleArray.get(2).getAsFloat());
        JsonArray translationArray = jsonObject.getAsJsonArray("translation");
        Vector3f translation = new Vector3f(translationArray.get(0).getAsFloat(), translationArray.get(1).getAsFloat(), translationArray.get(2).getAsFloat());
        return new Transformation(rightRotation, leftRotation, scale, translation);
    }

    @SuppressWarnings("DuplicatedCode")
    public JsonElement toJson() {
        if (this.isMatrix) {
            JsonArray jsonArray = new JsonArray(16);
            for (float f : this.transformation) {
                jsonArray.add(f);
            }
            return jsonArray;
        } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("right_rotation", this.rightRotation.map(l -> {
                JsonObject rotation = new JsonObject();
                rotation.addProperty("angle", l.left());
                JsonArray array = new JsonArray();
                array.add(l.right().x);
                array.add(l.right().y);
                array.add(l.right().z);
                rotation.add("axis", array);
                return rotation;
            }, r -> {
                JsonArray array = new JsonArray();
                array.add(r.x);
                array.add(r.y);
                array.add(r.z);
                array.add(r.w);
                return array;
            }));
            jsonObject.add("left_rotation", this.leftRotation.map(l -> {
                JsonObject rotation = new JsonObject();
                rotation.addProperty("angle", l.left());
                JsonArray array = new JsonArray();
                array.add(l.right().x);
                array.add(l.right().y);
                array.add(l.right().z);
                rotation.add("axis", array);
                return rotation;
            }, r -> {
                JsonArray array = new JsonArray();
                array.add(r.x);
                array.add(r.y);
                array.add(r.z);
                array.add(r.w);
                return array;
            }));
            JsonArray scaleArray = new JsonArray();
            scaleArray.add(this.scale.x);
            scaleArray.add(this.scale.y);
            scaleArray.add(this.scale.z);
            jsonObject.add("scale", scaleArray);
            JsonArray translationArray = new JsonArray();
            translationArray.add(this.translation.x);
            translationArray.add(this.translation.y);
            translationArray.add(this.translation.z);
            jsonObject.add("translation", translationArray);
            return jsonObject;
        }
    }

    private static Either<Pair<Float, Vector3f>, Quaternionf> parseRotation(ConfigValue value) {
        if (value.is(Map.class)) {
            ConfigSection section = value.getAsSection();
            float angle = section.getFloat("angle");
            Vector3f axis = section.getVector3f("axis", ConfigConstants.ZERO_VECTOR3);
            return Either.left(Pair.of(angle, axis));
        }
        return Either.right(value.getAsQuaternion());
    }

    private static Either<Pair<Float, Vector3f>, Quaternionf> parseRotation(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray rotationArray = json.getAsJsonArray();
            return Either.right(new Quaternionf(rotationArray.get(0).getAsFloat(), rotationArray.get(1).getAsFloat(), rotationArray.get(2).getAsFloat(), rotationArray.get(3).getAsFloat()));
        }
        JsonObject rotation = json.getAsJsonObject();
        float angle = rotation.get("angle").getAsFloat();
        JsonArray axisArray = rotation.getAsJsonArray("axis");
        Vector3f axis = new Vector3f(axisArray.get(0).getAsFloat(), axisArray.get(1).getAsFloat(), axisArray.get(2).getAsFloat());
        return Either.left(Pair.of(angle, axis));
    }
}
