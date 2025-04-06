package tfar.classicbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tfar.classicbar.ClassicBar;
import tfar.classicbar.compat.ModCompat;
import toughasnails.api.thirst.ThirstHelper;

public class MessageThirstExhaustionSync implements CustomPacketPayload {
    public static final Type<MessageThirstExhaustionSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ClassicBar.MODID, "thirst"));

    private final float exhaustionLevel;

    public MessageThirstExhaustionSync(float exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
    }

    public MessageThirstExhaustionSync(FriendlyByteBuf buf) {
        this.exhaustionLevel = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(exhaustionLevel);
    }

    public void handle(IPayloadContext ctx) {
        if (ModCompat.toughasnails.loaded) {
            ctx.enqueueWork(() -> {
                Player player = ctx.player();
                ThirstHelper.getThirst(player).setExhaustion(exhaustionLevel);
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
