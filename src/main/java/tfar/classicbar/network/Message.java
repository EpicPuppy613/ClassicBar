package tfar.classicbar.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Message {

  public static boolean presentOnServer;

  @SubscribeEvent
  public static void registerMessages(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1");

    registerPayload(registrar, MessageExhaustionSync.TYPE, MessageExhaustionSync::encode, MessageExhaustionSync::new, MessageExhaustionSync::handle);
    registerPayload(registrar, MessageHydrationSync.TYPE, MessageHydrationSync::encode, MessageHydrationSync::new, MessageHydrationSync::handle);
    registerPayload(registrar, MessageSaturationSync.TYPE, MessageSaturationSync::encode, MessageSaturationSync::new, MessageSaturationSync::handle);
    registerPayload(registrar, MessageThirstExhaustionSync.TYPE, MessageThirstExhaustionSync::encode, MessageThirstExhaustionSync::new, MessageThirstExhaustionSync::handle);

    NeoForge.EVENT_BUS.register(SyncHandler.instance());
  }

  private static <T extends CustomPacketPayload> void registerPayload(PayloadRegistrar registrar, CustomPacketPayload.Type<T> data, BiConsumer<T, ? super RegistryFriendlyByteBuf> encode, Function<? super RegistryFriendlyByteBuf, T> decode, BiConsumer<T, IPayloadContext> handle) {
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec = new StreamCodec<>() {
      @Override
      public @NotNull T decode(@NotNull RegistryFriendlyByteBuf buffer) {
        return decode.apply(buffer);
      }

      @Override
      public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull T value) {
        encode.accept(value, buffer);
      }
    };
    registrar.playBidirectional(data, codec, handle::accept);
  }

  private Message() {}

}
