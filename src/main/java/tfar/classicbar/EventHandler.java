package tfar.classicbar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import tfar.classicbar.api.BarOverlay;
import tfar.classicbar.compat.ModCompat;
import tfar.classicbar.config.ClassicBarsConfig;
import tfar.classicbar.config.ConfigCache;
import tfar.classicbar.impl.overlays.mod.Blood;
import tfar.classicbar.impl.overlays.mod.StaminaB;
import tfar.classicbar.impl.overlays.mod.Thirst;
import tfar.classicbar.impl.overlays.vanilla.*;
import tfar.classicbar.util.ModUtils;

import java.util.*;

public class EventHandler implements LayeredDraw.Layer {

  private static final List<BarOverlay> all = new ArrayList<>();
  public static final Map<String, BarOverlay> registry = new HashMap<>();

  private static final List<BarOverlay> errored = new ArrayList<>();

  public static void register(BarOverlay iBarOverlay) {
    registry.put(iBarOverlay.name(), iBarOverlay);
  }

  public static void registerAll(BarOverlay... iBarOverlay) {
    Arrays.stream(iBarOverlay).forEach(overlay -> {
      if (overlay != null) {
      registry.put(overlay.name(), overlay);
      }
    });
  }

  @Override
  public void render(@NotNull GuiGraphics gui, @NotNull DeltaTracker tracker) {
    Entity entity = ModUtils.mc.getCameraEntity();
    if (!(entity instanceof Player player)) return;
    if (player.getAbilities().instabuild || player.isSpectator()) return;
    ModUtils.mc.getProfiler().push("classicbars_hud");

    for (BarOverlay overlay : all) {
      boolean rightHand = overlay.rightHandSide();
      try {
        overlay.render(gui, player, gui.guiWidth(), gui.guiHeight(), getOffset(Minecraft.getInstance().gui, rightHand));
      } catch (Error e) {
        ClassicBar.logger.error("Removing broken overlay "+overlay.name());
        e.printStackTrace();
        errored.add(overlay);
      }
    }
    if (!errored.isEmpty()) all.removeAll(errored);

    // mc.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
    ModUtils.mc.getProfiler().pop();
  }

  public static void increment(Gui gui,boolean side ,int amount){
    if (side)gui.rightHeight+=amount;
    else gui.leftHeight+=amount;
  }

  public static int getOffset(Gui gui,boolean right) {
    return right ? gui.rightHeight : gui.leftHeight;
  }

  public static void cacheConfigs() {
    all.clear();
    ClassicBarsConfig.leftorder.get().stream().filter(s -> registry.get(s) != null).forEach(e -> all.add(registry.get(e).setSide(false)));
    ClassicBarsConfig.rightorder.get().stream().filter(s -> registry.get(s) != null).forEach(e -> all.add(registry.get(e).setSide(true)));
    all.removeAll(errored);
    ConfigCache.bake();
  }

  public static void sendModMessage(InterModEnqueueEvent e) {
    InterModComms.sendTo("vampirism", "disable-blood-bar", () -> true);
  }

  public static void setupOverlays(RegisterGuiLayersEvent e) {
    NeoForge.EVENT_BUS.addListener(EventHandler::disableOtherOverlays);
    e.registerBelow(VanillaGuiLayers.SELECTED_ITEM_NAME,ResourceLocation.fromNamespaceAndPath(ClassicBar.MODID, "layer"),new EventHandler());

    //Register renderers for events
    ClassicBar.logger.info("Registering Vanilla Overlays");

    EventHandler.registerAll(new Absorption(), new Air(), new Armor(), new ArmorToughness(),
            new Health(), new Hunger(), new MountHealth());

    //mod renderers
    ClassicBar.logger.info("Registering Mod Overlays");
    if (ModCompat.vampirism.loaded)EventHandler.register(new Blood());
  //  if (ModCompat.feathers.loaded)EventHandler.register(new Feathers());
    if (ModCompat.parcool.loaded)EventHandler.register(new StaminaB());
    if (ModCompat.toughasnails.loaded)EventHandler.register(new Thirst());
    // if (ModList.get().isLoaded("randomthings")) MinecraftForge.EVENT_BUS.register(new LavaCharmRenderer());
    // if (ModList.get().isLoaded("lavawaderbauble")) {
    //    MinecraftForge.EVENT_BUS.register(new LavaWaderBaubleRenderer());
    // }

    //if (ModList.get().isLoaded("superiorshields"))
    //  MinecraftForge.EVENT_BUS.register(new SuperiorShieldRenderer());

    //MinecraftForge.EVENT_BUS.register(new BetterDivingRenderer());
    //  if (ModList.get().isLoaded("botania")) MinecraftForge.EVENT_BUS.register(new TiaraBarRenderer());
    // Regenerate configs
    ClassicBarsConfig.onConfigChainged();
  }

  private static final List<ResourceLocation> vanilla_overlays = List.of(VanillaGuiLayers.AIR_LEVEL,VanillaGuiLayers.ARMOR_LEVEL,
          VanillaGuiLayers.PLAYER_HEALTH,VanillaGuiLayers.VEHICLE_HEALTH,VanillaGuiLayers.FOOD_LEVEL);
  public static void disableOtherOverlays(RenderGuiLayerEvent.Pre e) {
    ResourceLocation overlay = e.getName();
    if (vanilla_overlays.contains(overlay)) e.setCanceled(true);
    else if (overlay.getNamespace().equals("parcool") && StaminaB.checkConfigs()) e.setCanceled(true);
    else if (ModCompat.toughasnails.loaded && Thirst.isEnabled() && Thirst.OVERLAY_ID.equals(overlay)) e.setCanceled(true);
  }
}