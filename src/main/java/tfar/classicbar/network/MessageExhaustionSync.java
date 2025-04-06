package tfar.classicbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfar.classicbar.ClassicBar;

public class MessageExhaustionSync implements CustomPacketPayload {
    public static final Type<MessageExhaustionSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ClassicBar.MODID, "exaustion"));

    private final float exhaustionLevel;

    public MessageExhaustionSync(float exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
    }

    public MessageExhaustionSync(FriendlyByteBuf buf) {
        this.exhaustionLevel = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(exhaustionLevel);
    }

    public void handle(IPayloadContext ctx) {
        // defer to the next game loop; we can't guarantee that Minecraft.thePlayer is initialized yet
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            player.getFoodData().setExhaustion(exhaustionLevel);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
