package jp.apple.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GuiAppleListSelector extends GuiScreen {

    private static final int BG_COLOR = 0xEE0A1530;
    private static final int BORDER_COLOR = 0xFF2A3F70;
    private static final int ROW_H = 16;
    private static final int SEL_COLOR = 0xFF1A3A6A;
    private static final int HOVER_COLOR = 0x33FFFFFF;
    private static final int TEXT_COLOR = 0xFFCCDDFF;
    private static final int TEXT_SEL_COLOR = 0xFFFFDD44;

    private final GuiScreen parentScreen;
    private final int xPosition;
    private final int yPosition;
    private final int screenWidth;
    private final int screenHeight;
    private final Supplier<Integer> indexGetter;
    private final List<String> displayStringList;
    private final Consumer<Integer> onSelect;

    private int scrollOffset = 0;

    public GuiAppleListSelector(GuiScreen parentScreen, int x, int y, int width, int height,
                                Supplier<Integer> index, List<String> list, Consumer<Integer> onSelect) {
        this.parentScreen = parentScreen;
        this.xPosition = x;
        this.yPosition = y;
        this.screenWidth = width;
        this.screenHeight = height;
        this.indexGetter = index;
        this.displayStringList = list;
        this.onSelect = onSelect;
    }

    @Override
    public void initGui() {
        Integer cur = indexGetter.get();
        if (cur != null && cur >= 0) {
            int visibleRows = getVisibleRows();
            scrollOffset = Math.max(0, cur - visibleRows / 2);
        }
        clampScroll();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        if (this.mc != null && (this.width != width || this.height != height)) {
            super.setWorldAndResolution(mc, width, height);
            this.parentScreen.setWorldAndResolution(mc, width, height);
            mc.displayGuiScreen(this.parentScreen);
        } else {
            super.setWorldAndResolution(mc, width, height);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.inRange(mouseX, mouseY)) {
            this.parentScreen.drawScreen(-1, -1, partialTicks);
        } else {
            this.parentScreen.drawScreen(mouseX, mouseY, partialTicks);
        }
        
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        drawRect(xPosition - 1, yPosition - 1,
                xPosition + screenWidth + 1, yPosition + screenHeight + 1, BORDER_COLOR);
        drawRect(xPosition, yPosition, xPosition + screenWidth, yPosition + screenHeight, BG_COLOR);

        int rows = getVisibleRows();
        Integer selectedIdx = indexGetter.get();
        for (int row = 0; row < rows; row++) {
            int idx = scrollOffset + row;
            if (idx >= displayStringList.size())
                break;
            int ry = yPosition + row * ROW_H;
            if (ry + ROW_H > yPosition + screenHeight)
                break;

            boolean sel = (selectedIdx != null && idx == selectedIdx);
            boolean hover = mouseX >= xPosition && mouseX < xPosition + screenWidth
                    && mouseY >= ry && mouseY < ry + ROW_H;

            if (sel) {
                drawRect(xPosition, ry, xPosition + screenWidth, ry + ROW_H, SEL_COLOR);
            } else if (hover) {
                drawRect(xPosition, ry, xPosition + screenWidth, ry + ROW_H, HOVER_COLOR);
            }

            String text = displayStringList.get(idx);
            drawCenteredString(fontRendererObj, text, xPosition + screenWidth / 2, ry + 4,
                    sel ? TEXT_SEL_COLOR : TEXT_COLOR);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_FOG);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll == 0)
            return;
        int mx = Mouse.getEventX() * width / mc.displayWidth;
        int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (!inRange(mx, my))
            return;
        int old = scrollOffset;
        scrollOffset += (scroll > 0) ? -1 : 1;
        clampScroll();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!inRange(mouseX, mouseY)) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        int rows = getVisibleRows();
        for (int row = 0; row < rows; row++) {
            int idx = scrollOffset + row;
            if (idx >= displayStringList.size())
                break;
            int ry = yPosition + row * ROW_H;
            if (mouseX >= xPosition && mouseX < xPosition + screenWidth
                    && mouseY >= ry && mouseY < ry + ROW_H) {
                Integer cur = indexGetter.get();
                if (cur == null || idx != cur) {
                    onSelect.accept(idx);
                }
                mc.displayGuiScreen(parentScreen);
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    private int getVisibleRows() {
        return Math.max(0, Math.min(displayStringList.size(), screenHeight / ROW_H));
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, displayStringList.size() - getVisibleRows());
        if (scrollOffset < 0)
            scrollOffset = 0;
        if (scrollOffset > maxScroll)
            scrollOffset = maxScroll;
    }

    private boolean inRange(int x, int y) {
        return x >= xPosition && y >= yPosition && x < xPosition + screenWidth && y < yPosition + screenHeight;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}