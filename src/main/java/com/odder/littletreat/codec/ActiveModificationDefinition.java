package com.odder.littletreat.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.odder.littletreat.LittleTreat;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ActiveModificationDefinition{
    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveModificationDefinition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ActiveModificationDefinition::source,
            AttributeModificationDefinition.STREAM_CODEC, ActiveModificationDefinition::def,
            ByteBufCodecs.VAR_INT, ActiveModificationDefinition::remainingTicks,
            ActiveModificationDefinition::new
    );

    public static final Codec<ActiveModificationDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("source").forGetter(ActiveModificationDefinition::source),
        AttributeModificationDefinition.CODEC.fieldOf("def").forGetter(ActiveModificationDefinition::def),
        Codec.INT.fieldOf("remainingTicks").forGetter(ActiveModificationDefinition::remainingTicks)
    ).apply(inst, ActiveModificationDefinition::new));

    public static ResourceLocation createIdFor(Holder<Item> src, AttributeModificationDefinition def) {
        var path = String.format(
                "treat/%s/%s",
                def.attribute().getRegisteredName().replace(":", "_"),
                src.getRegisteredName().replace(":", "_")
        );

        return ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, path);
    }

    public Holder<Item> source;
    public AttributeModificationDefinition def;
    public int remainingTicks;

    private final ResourceLocation id;
    private final ItemStack item;

    public ActiveModificationDefinition(Holder<Item> source, AttributeModificationDefinition def, int remainingTicks) {
        this.source = source;
        this.def = def;
        this.remainingTicks = remainingTicks;
        this.id = createIdFor(source, def);
        this.item = new ItemStack(BuiltInRegistries.ITEM.get(source.getKey().location()));
    }

    public ActiveModificationDefinition(Holder<Item> source, AttributeModificationDefinition def) {
        this.source = source;
        this.def = def;
        this.remainingTicks = def.duration();

        this.id = createIdFor(source, def);
        this.item = new ItemStack(BuiltInRegistries.ITEM.get(source.getKey().location()));
    }

    public float getRemainingPercent() {
        return (float) this.remainingTicks / this.def.duration();
    }

    public void resetTicks() {
        this.remainingTicks = def.duration();
    }

    public ItemStack getItem() {
        return item;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Holder<Item> source() { return source; }

    public AttributeModificationDefinition def() {
        return def;
    }

    public int remainingTicks() {
        return remainingTicks;
    }


    public AttributeModifier toModifier() {
        return new AttributeModifier(getId(), def.amount(), def.op());
    }

    public void progressTicks(int ticks) {
        remainingTicks = Math.max(remainingTicks - ticks, 0);
    }

    public boolean isFinished() {
        return remainingTicks <= 0;
    }
}
