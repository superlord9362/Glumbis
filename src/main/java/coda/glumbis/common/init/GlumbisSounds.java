package coda.glumbis.common.init;

import coda.glumbis.Glumbis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class GlumbisSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Glumbis.MOD_ID);

    public static final RegistryObject<SoundEvent> GLUMBOSS_AMBIENT = SOUNDS.register("glumboss.ambient", () -> new SoundEvent(new ResourceLocation(Glumbis.MOD_ID, "glumboss.ambient")));
    public static final RegistryObject<SoundEvent> GLUMBOSS_HURT = SOUNDS.register("glumboss.hurt", () -> new SoundEvent(new ResourceLocation(Glumbis.MOD_ID, "glumboss.hurt")));

    public static final RegistryObject<SoundEvent> GLUMP_FLY = SOUNDS.register("glump.fly", () -> new SoundEvent(new ResourceLocation(Glumbis.MOD_ID, "glump.fly")));
    public static final RegistryObject<SoundEvent> GLUMP_HURT = SOUNDS.register("glump.hurt", () -> new SoundEvent(new ResourceLocation(Glumbis.MOD_ID, "glump.hurt")));
}
