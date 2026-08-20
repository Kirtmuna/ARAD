package jp.apple.arad.gui;

// import jp.apple.gui.GuiAppleListSelector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.opengl.GL11;

import jp.apple.arad.data.StationSnapshot;
import jp.apple.arad.handler.AradPacketHandler;
import jp.apple.arad.network.PacketSubStationConfig;
import jp.apple.arad.substation.SubStationMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GuiSubStation extends GuiContainer {

    private static final int GUI_W = 196;
    private static final int GUI_H = 200;

    private static final int BTN_PARENT_DROPDOWN = 10;
    private static final int BTN_MODE_TOGGLE = 11;
    private static final int BTN_TURNBACK = 12;
    private static final int BTN_DOOR_LEFT = 20;
    private static final int BTN_DOOR_RIGHT = 21;

    private final ContainerSubStation container;
    private final int x, y, z;

    private String selectedParentId;
    private String selectedParentName = "(未選択)";
    private SubStationMode selectedMode;
    private boolean turnback;
    private boolean doorLeft;
    private boolean doorRight;

    private List<StationSnapshot> stationOptions = new ArrayList<>();

    private GuiButton btnParentDropdown;
    private GuiButton btnModeToggle;
    private GuiButton btnTurnback;
    private GuiButton btnDoorLeft;
    private GuiButton btnDoorRight;

    public GuiSubStation(ContainerSubStation container, int x, int y, int z) {
        super(container);
        this.x = x;
        this.y = y;
        this.z = z;
        this.container = container;

        this.xSize = GUI_W;
        this.ySize = GUI_H;

        this.selectedParentId = container.subStation.getParentStationId();
        this.selectedMode = container.subStation.getMode();
        this.turnback = container.subStation.isTurnback();
        this.doorLeft = container.subStation.isDoorLeft();
        this.doorRight = container.subStation.isDoorRight();
    }

    @Override
    public void initGui() {
        super.initGui();
        stationOptions = new ArrayList<>(jp.apple.arad.data.MapData.INSTANCE.getStations());
        resolveParentName();
        rebuildButtons();
    }

    private void resolveParentName() {
        if (selectedParentId == null || selectedParentId == null) {
            selectedParentName = "(未選択)";
            return;
        }
        for (StationSnapshot s : stationOptions) {
            if (s.id.equals(selectedParentId)) {
                selectedParentName = s.name;
                return;
            }
        }
        selectedParentName = "(不明な駅)";
    }

    @SuppressWarnings("unchecked")
    private void rebuildButtons() {
        buttonList.clear();

        btnParentDropdown = new GuiButton(BTN_PARENT_DROPDOWN, guiLeft + 8, guiTop + 24,
                GUI_W - 16, 18, "親駅: " + selectedParentName);
        buttonList.add(btnParentDropdown);

        btnModeToggle = new GuiButton(BTN_MODE_TOGGLE, guiLeft + 8, guiTop + 50,
                GUI_W - 16, 18, "モード: " + selectedMode.label);
        buttonList.add(btnModeToggle);

        btnTurnback = new GuiButton(BTN_TURNBACK, guiLeft + 8, guiTop + 74,
                GUI_W - 16, 18, turnback ? "§a折り返し" : "§7折り返し");
        buttonList.add(btnTurnback);

        btnDoorLeft = new GuiButton(BTN_DOOR_LEFT, guiLeft + 8, guiTop + 98, (GUI_W - 20) / 2, 18,
                doorLeft ? "§a◀ 左ドア" : "§7◀ 左ドア");
        buttonList.add(btnDoorLeft);

        btnDoorRight = new GuiButton(BTN_DOOR_RIGHT, guiLeft + 12 + (GUI_W - 20) / 2, guiTop + 98, (GUI_W - 20) / 2, 18,
                doorRight ? "§a右ドア ▶" : "§7右ドア ▶");
        buttonList.add(btnDoorRight);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        sendIfChanged();
    }

    private void sendIfChanged() {
        boolean changed = !java.util.Objects.equals(selectedParentId, container.subStation.getParentStationId())
                || selectedMode != container.subStation.getMode()
                || turnback != container.subStation.isTurnback()
                || doorLeft != container.subStation.isDoorLeft()
                || doorRight != container.subStation.isDoorRight();
        if (!changed)
            return;

        container.subStation.setParentStationId(selectedParentId);
        container.subStation.setMode(selectedMode);
        container.subStation.setTurnback(turnback);
        container.subStation.setDoorLeft(doorLeft);
        container.subStation.setDoorRight(doorRight);
        AradPacketHandler.CHANNEL.sendToServer(
                new PacketSubStationConfig(x, y, z, selectedParentId, selectedMode, turnback, doorLeft, doorRight));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == BTN_PARENT_DROPDOWN) {
            openParentSelector();
        } else if (id == BTN_MODE_TOGGLE) {
            selectedMode = selectedMode.next();
            rebuildButtons();
        } else if (id == BTN_TURNBACK) {
            turnback = !turnback;
            rebuildButtons();
        } else if (id == BTN_DOOR_LEFT) {
            doorLeft = !doorLeft;
            rebuildButtons();
        } else if (id == BTN_DOOR_RIGHT) {
            doorRight = !doorRight;
            rebuildButtons();
        }
    }

    private void openParentSelector() {
        List<String> names = stationOptions.stream().map(s -> s.name).collect(Collectors.toList());

        int currentIndex = -1;
        for (int i = 0; i < stationOptions.size(); i++) {
            if (stationOptions.get(i).id.equals(selectedParentId)) {
                currentIndex = i;
                break;
            }
        }
        final int initialIndex = currentIndex;

        // GuiAppleListSelector selector = new GuiAppleListSelector(
        // this,
        // // btnParentDropdown.xPosition, btnParentDropdown.yPosition +
        // btnParentDropdown.height,
        // btnParentDropdown.width, 80,
        // () -> initialIndex,
        // names,
        // (selectedIdx) -> {
        // selectedParentId = stationOptions.get(selectedIdx).id;
        // resolveParentName();
        // rebuildButtons();
        // });
        // this.mc.displayGuiScreen(selector);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF1A2A55);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + xSize - 1, guiTop + ySize - 1, 0xFF0D1B3E);

        fontRendererObj.drawString("§fサブ駅設定", guiLeft + 8, guiTop + 7, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    }
}