package xland.mcmod.enchlevellangpatch.ext.config2;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public final class LangPatchConfig {
    private ResourceLocation enchantmentCfg, potionCfg;
    private final Path configurationFile;
    private static final Path CFG
            = FabricLoader.getInstance().getConfigDir()
            .resolve("enchlevel-langpatch")
            .resolve("2conf.txt");
    private static final Logger LOGGER = LogManager.getLogger("LangPatchConfig");

    public void load() {
        try {
            if (checkExistence()) {
                Properties properties = new Properties();
                properties.load(Files.newBufferedReader(configurationFile));
                String ec = properties.getProperty("enchantment-cfg", "enchlevel-langpatch:roman");
                String pc = properties.getProperty("potion-cfg", "enchlevel-langpatch:roman");
                verify(ec, EnchantmentLevelLangPatchConfig::getEnchantmentHooksContext)
                        .ifPresent(a -> enchantmentCfg = a);
                verify(pc, EnchantmentLevelLangPatchConfig::getPotionHooksContext)
                        .ifPresent(a -> potionCfg = a);
            }
        } catch (IOException e) {
            LOGGER.error("Can't load config from " + configurationFile, e);
        } finally {
            apply();
        }
    }

    public void apply() {
        EnchantmentLevelLangPatchConfig.setCurrentEnchantmentHooks(
                EnchantmentLevelLangPatchConfig.getEnchantmentHooksContext().get(enchantmentCfg));
        EnchantmentLevelLangPatchConfig.setCurrentPotionHooks(
                EnchantmentLevelLangPatchConfig.getPotionHooksContext().get(potionCfg));
    }

    private static Optional<ResourceLocation> verify(String s,
                                                     Supplier<BiMap<ResourceLocation, EnchantmentLevelLangPatch>> ctxSup) {
        final BiMap<ResourceLocation, EnchantmentLevelLangPatch> enchantmentCtx
                = ctxSup.get();
        ResourceLocation id = new ResourceLocation(s);
        if (enchantmentCtx.containsKey(id))
            return Optional.of(id);
        return Optional.empty();
    }

    private boolean checkExistence() throws IOException {
        if (Files.notExists(configurationFile)) {
            Files.createDirectories(configurationFile.getParent());
            try (Writer w = Files.newBufferedWriter(configurationFile, Charsets.UTF_8)) {
                w.write("# Change to enchlevel-langpatch:default for numeric format\n" +
                        "enchantment-cfg=enchlevel-langpatch:roman\npotion-cfg=enchlevel-langpatch:roman\n");
            }
            return false;
        }
        return true;
    }

    public static void init() {
        getInstance().load();
    }

    private static final LangPatchConfig INSTANCE = new LangPatchConfig(CFG);
    public static LangPatchConfig getInstance() {
        return INSTANCE;
    }
    LangPatchConfig(Path configurationFile) {
        enchantmentCfg = potionCfg = new ResourceLocation("enchlevel-langpatch:roman");
        this.configurationFile = configurationFile;
    }
}
