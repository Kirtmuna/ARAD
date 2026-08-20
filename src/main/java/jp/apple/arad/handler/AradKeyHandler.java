package jp.apple.arad.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.apple.arad.gui.GuiRailMap;

import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class AradKeyHandler {

    public static final KeyBinding KEY_OPEN_MAP = new KeyBinding(
            "key.arad.open_map",
            Keyboard.KEY_M,
            "key.categories.arad");

    public static void register() {
        ClientRegistry.registerKeyBinding(KEY_OPEN_MAP);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!KEY_OPEN_MAP.isPressed())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null && mc.theWorld != null) {
            mc.displayGuiScreen(new GuiRailMap());
        }
    }
}