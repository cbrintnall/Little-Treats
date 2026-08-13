package com.odder.littletreat;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class Utilities {
    public static <T> TagKey<T> parseTag(String tag, ResourceKey<? extends Registry<T>> registry) {
        if (tag.startsWith("#")) {
            ResourceLocation loc =  ResourceLocation.tryParse(tag.substring(1));
            if (loc == null) return null;
            return TagKey.create(registry, loc);
        }

        return null;
    }
}
