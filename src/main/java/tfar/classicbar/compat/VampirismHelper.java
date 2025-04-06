package tfar.classicbar.compat;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import net.minecraft.world.entity.player.Player;

public class VampirismHelper {
    public static boolean isVampire(Player entity) {
        return VReference.VAMPIRE_FACTION.equals(getFactionPlayerHandler(entity).getCurrentFaction());
    }

    public static IFactionPlayerHandler getFactionPlayerHandler(Player player) {
        return VampirismAPI.factionPlayerHandler(player);
    }
}
