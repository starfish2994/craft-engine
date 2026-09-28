package net.momirealms.craftengine.core.pack.model.bbmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TextureAnimationMetadata {
    private TextureAnimationMetadata() {
    }

    @Nullable
    static JsonObject create(JsonObject texture, byte[] png, float uvWidth, float uvHeight) throws IOException {
        int width;
        int height;

        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(png))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return null;
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                width = reader.getWidth(0);
                height = reader.getHeight(0);
            } finally {
                reader.dispose();
            }
        }

        double ratio = (double) width / height;
        double uvRatio = (double) uvWidth / uvHeight;
        if (ratio == uvRatio) return null;
        int frameCount = (int) Math.ceil(uvRatio / ratio - 0.05);
        if (frameCount <= 1) return null;

        JsonObject animation = new JsonObject();
        animation.addProperty("frametime", GsonHelper.getAsInt(texture.get("frame_time"), 1));
        if (uvWidth != uvHeight) {
            animation.addProperty("width", uvWidth);
            animation.addProperty("height", uvHeight);
        }
        if (GsonHelper.getAsBoolean(texture.get("frame_interpolate"), false)) {
            animation.addProperty("interpolate", true);
        }
        JsonArray frames = new JsonArray();
        switch (GsonHelper.getAsString(texture.get("frame_order_type"), "loop")) {
            case "backwards" -> {
                for (int i = frameCount - 1; i >= 0; i--) frames.add(i);
            }
            case "back_and_forth" -> {
                for (int i = 0; i < frameCount; i++) frames.add(i);
                for (int i = frameCount - 2; i > 0; i--) frames.add(i);
            }
            case "custom" -> {
                String order = GsonHelper.getAsString(texture.get("frame_order"), "").trim();
                if (!order.isEmpty()) {
                    for (String frame : order.split("\\s+")) {
                        String[] parts = frame.split(":", 2);
                        int index = Integer.parseInt(parts[0]);
                        if (parts.length == 1) {
                            frames.add(index);
                        } else {
                            JsonObject timedFrame = new JsonObject();
                            timedFrame.addProperty("index", index);
                            timedFrame.addProperty("time", Integer.parseInt(parts[1]));
                            frames.add(timedFrame);
                        }
                    }
                }
            }
        }
        if (!frames.isEmpty()) animation.add("frames", frames);
        JsonObject metadata = new JsonObject();
        metadata.add("animation", animation);
        return metadata;
    }
}
