package jp.apple.arad.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.apple.arad.handler.AradPacketHandler;
import jp.apple.arad.network.PacketSignalSpeedMarkerConfig;
import jp.apple.arad.section.SectionSlot;
import jp.apple.arad.signalspeed.TileEntitySignalSpeedMarker;

import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class GuiSignalSpeedMarker extends GuiScreen {

    private static final int GUI_W = 280;
    private static final int GUI_H = 230;
    private static final int C_BG = 0xFF0D1B3E;
    private static final int C_BORDER = 0xFF2A3F70;
    private static final int C_ACCENT = 0xFF44DDFF;
    private static final int C_TEXT = 0xFFCCDDFF;
    private static final int C_DIM = 0xFF8899CC;
    private static final int C_FIELD = 0xFF060E22;
    private static final int C_WHITE = 0xFFFFFFFF;

    private static final int BTN_SAVE = 1;
    private static final int BTN_CANCEL = 2;

    private static final int FW = 46;
    private static final int FH = 14;

    private final TileEntitySignalSpeedMarker te;
    private final int x, y, z;

    private final GuiTextField[] speedFields = new GuiTextField[SectionSlot.MAP_SIZE];

    private int gx, gy;

    public GuiSignalSpeedMarker(TileEntitySignalSpeedMarker te, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.te = te;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        gx = (width - GUI_W) / 2;
        gy = (height - GUI_H) / 2;
        buttonList.clear();

        int[] map = te.getSpeedMap();
        int rowY = gy + 28;
        int col0 = gx + 90;

        for (int i = 0; i < SectionSlot.MAP_SIZE; i++) {
            String val = (map[i] == SectionSlot.SPEED_FREE) ? "-1" : String.valueOf(map[i]);
            GuiTextField f = new GuiTextField(fontRendererObj, col0, rowY + i * 22, FW, FH);
            f.setMaxStringLength(5);
            f.setText(val);
            f.setCanLoseFocus(true);
            speedFields[i] = f;
        }

        int fy = gy + GUI_H - 26;
        buttonList.add(new GuiButton(BTN_SAVE, gx + GUI_W - 160, fy, 72, 20, "保存"));
        buttonList.add(new GuiButton(BTN_CANCEL, gx + GUI_W - 82, fy, 72, 20, "閉じる"));
    }

    @Override
    public void drawScreen(int mx, int my, float partial) {
        drawBackground(0);

        drawRect(gx, gy, gx + GUI_W, gy + GUI_H, C_BG);
        drawRect(gx, gy, gx + GUI_W, gy + 1, C_ACCENT);
        drawRect(gx, gy + GUI_H - 1, gx + GUI_W, gy + GUI_H, C_BORDER);
        drawRect(gx, gy, gx + 1, gy + GUI_H, C_BORDER);
        drawRect(gx + GUI_W - 1, gy, gx + GUI_W, gy + GUI_H, C_BORDER);
        drawRect(gx + 1, gy + GUI_H - 30, gx + GUI_W - 1, gy + GUI_H - 29, C_BORDER);

        drawString(fontRendererObj, "§l現示ごとの制限を設定", gx + 8, gy + 6, C_WHITE);

        int rowY = gy + 28;
        for (int i = 0; i < SectionSlot.MAP_SIZE; i++) {
            int y = rowY + i * 22;
            drawString(fontRendererObj, "現示 " + i + ":", gx + 8, y + 1, C_TEXT);
            GuiTextField f = speedFields[i];
            if (f != null) {
                drawRect(f.xPosition - 1, f.yPosition - 1, f.xPosition + f.width + 1, f.yPosition + f.height + 1,
                        C_BORDER);
                drawRect(f.xPosition, f.yPosition, f.xPosition + f.width, f.yPosition + f.height, C_FIELD);
                drawString(fontRendererObj, "km/h", f.xPosition + FW + 4, y + 1, C_DIM);
            }
        }

        super.drawScreen(mx, my, partial);
        for (GuiTextField f : speedFields)
            if (f != null)
                f.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == BTN_SAVE) {
            int[] map = new int[SectionSlot.MAP_SIZE];
            int[] def = SectionSlot.defaultSpeedMap();
            for (int i = 0; i < SectionSlot.MAP_SIZE; i++) {
                map[i] = parseSpeed(speedFields[i], def[i]);
            }
            AradPacketHandler.CHANNEL.sendToServer(
                    new PacketSignalSpeedMarkerConfig(x, y, z, map));
            mc.displayGuiScreen(null);
        } else if (btn.id == BTN_CANCEL) {
            mc.displayGuiScreen(null);
        }
    }

    private int parseSpeed(GuiTextField f, int fallback) {
        if (f == null)
            return fallback;
        try {
            int v = Integer.parseInt(f.getText().trim());
            return (v < 0) ? SectionSlot.SPEED_FREE : Math.min(v, 9999);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) {
        for (GuiTextField f : speedFields)
            if (f != null)
                f.mouseClicked(mx, my, btn);
        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void keyTyped(char ch, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        for (GuiTextField f : speedFields) {
            if (f != null && f.isFocused())
                f.textboxKeyTyped(ch, key);
        }
    }

    @Override
    public void updateScreen() {
        for (GuiTextField f : speedFields)
            if (f != null)
                f.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}