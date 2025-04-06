package tfar.classicbar;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.classicbar.config.ClassicBarsConfig;
import tfar.classicbar.network.Message;

@Mod(value = ClassicBar.MODID)
public class ClassicBar {

  public static final String MODID = "classicbar";

  public static final Logger logger = LogManager.getLogger();

  public static final ClassicBarsConfig CLIENT;
  public static final ModConfigSpec CLIENT_SPEC;

  static {
    final Pair<ClassicBarsConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClassicBarsConfig::new);
    CLIENT_SPEC = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  public ClassicBar(IEventBus bus, ModContainer container) {
    container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    bus.addListener(Message::registerMessages);

    if (FMLEnvironment.dist.isClient()) {
      bus.addListener(this::postInit);
      bus.addListener(EventHandler::setupOverlays);
      bus.addListener(EventHandler::sendModMessage);
    }
  }

  public void postInit(FMLClientSetupEvent event) {
    EventHandler.cacheConfigs();
  }

}
