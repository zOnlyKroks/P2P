package de.zonlykroks.p2p4all.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.List;

public class P2PYACLConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("p2p.json");

    @SerialEntry
    public String golePath = "";

    @SerialEntry
    public List<String> savedIPs = List.of(
    );

    public static final ConfigClassHandler<P2PYACLConfig> HANDLER = ConfigClassHandler.createBuilder(P2PYACLConfig.class)
            .id(new Identifier("p2p4all", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    public static P2PYACLConfig get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }

    public static YetAnotherConfigLib getInstance() {
        return YetAnotherConfigLib.create(HANDLER, (P2PYACLConfig defaults, P2PYACLConfig config, YetAnotherConfigLib.Builder builder) -> {
            var supportedBiomesOption = ListOption.<String>createBuilder()
                    .name(Text.translatable("p2p4all.config.ips"))
                    .description(OptionDescription.of(Text.translatable("p2p4all.config.ips.description")))
                    .controller(StringControllerBuilder::create)
                    .initial("127.0.0.1")
                    .binding(defaults.savedIPs, () -> config.savedIPs, (v) -> config.savedIPs = v)
                    .build();

            var golePath = Option.<String>createBuilder()
                    .name(Text.translatable("p2p4all.config.golePath"))
                    .description(OptionDescription.of(Text.translatable("p2p4all.config.golePath.description")))
                    .controller(StringControllerBuilder::create)
                    .binding(defaults.golePath, () -> config.golePath, (v) -> config.golePath = v)
                    .build();

            return builder
                    .title(Text.translatable("p2p4all.config.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(Text.translatable("p2p4all.config.title"))
                            .options(List.of(golePath))
                            .group(supportedBiomesOption)
                            .build());
        });

    }

}
