package tfar.classicbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfar.classicbar.ClassicBar;
import tfar.classicbar.compat.ModCompat;
import toughasnails.api.thirst.ThirstHelper;

public class MessageHydrationSync implements CustomPacketPayload {
    public static final Type<MessageHydrationSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ClassicBar.MODID, "hydration"));

    private final float hydrationLevel;

    public MessageHydrationSync(float hydrationLevel) {
        this.hydrationLevel = hydrationLevel;
    }

    public MessageHydrationSync(FriendlyByteBuf buf) {
        this.hydrationLevel = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(hydrationLevel);
    }

    public void handle(IPayloadContext ctx) {
        if (ModCompat.toughasnails.loaded) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                ThirstHelper.getThirst(player).setHydration(hydrationLevel);
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
