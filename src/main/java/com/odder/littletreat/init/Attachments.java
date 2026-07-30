package com.odder.littletreat.init;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.ActiveModificationDefinition;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Attachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LittleTreat.MODID);

    public static final Supplier<AttachmentType<List<ActiveModificationDefinition>>> ACTIVE_MODIFICATIONS =
            ATTACHMENTS.register("active_modifications", () ->
                AttachmentType.<List<ActiveModificationDefinition>>builder(() -> new ArrayList<>())
                        .serialize(ActiveModificationDefinition.CODEC.listOf())
                        .build());
}
