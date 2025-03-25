package valorless.havenbags.utils;

import com.google.gson.*;

public class FoodComponentFixer {
    public static String fixFoodJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (root.has("meta")) {
            JsonObject meta = root.getAsJsonObject("meta");

            if (meta.has("food")) {
                JsonObject food = meta.getAsJsonObject("food");

                if (food.has("effects")) {
                    JsonArray oldEffects = food.getAsJsonArray("effects");

                    // Convert each FoodEffect into ConsumableEffect
                    for (JsonElement effectElement : oldEffects) {
                        JsonObject effectObj = effectElement.getAsJsonObject();

                        if (effectObj.has("==") && "FoodEffect".equals(effectObj.get("==").getAsString())) {
                            effectObj.remove("=="); // Remove old class identifier
                        }
                    }

                    // Remove effects from food
                    food.remove("effects");
                }
            }
        }

        return root.toString();
    }
}
