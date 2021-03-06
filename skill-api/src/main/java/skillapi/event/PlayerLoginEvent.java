package skillapi.event;

import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import skillapi.api.annotation.SkillEvent;
import skillapi.event.base.BaseSkillEvent;
import skillapi.newpacket.TestPacket;

/**
 * @author Jun
 * @date 2020/8/20.
 */
@SkillEvent(Side.SERVER)
public class PlayerLoginEvent extends BaseSkillEvent<PlayerLoggedInEvent> {
    @Override
    public void onServer(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            new TestPacket("你登录了！").sendToClient((EntityPlayerMP) event.player);
        }

    }

    @Override
    public void onClient(PlayerLoggedInEvent event) {
        // Only happens on the server
    }
}
