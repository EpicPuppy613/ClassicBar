package tfar.classicbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfar.classicbar.ClassicBar;

public class MessageSaturationSync implements CustomPacketPayload {
    public static final Type<MessageSaturationSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ClassicBar.MODID, "saturation"));

    private final float saturationLevel;

    public MessageSaturationSync(float saturationLevel)
    {
        this.saturationLevel = saturationLevel;
    }

    public MessageSaturationSync(FriendlyByteBuf buf)
    {
        this.saturationLevel = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeFloat(saturationLevel);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ctx.player().getFoodData().setSaturation(saturationLevel));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
