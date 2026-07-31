package jp.apple.arad.gui;

import jp.apple.arad.data.StationSnapshot;
import jp.apple.arad.handler.AradPacketHandler;
import jp.apple.arad.network.PacketSubStationConfig;
import jp.apple.arad.substation.SubStationMode;
import jp.apple.gui.GuiAppleListSelector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSubStation extends GuiContainer {

    private static final int GUI_W = 196;
    private static final int GUI_H = 86;

    private static final int BTN_PARENT_DROPDOWN = 10;
    private static final int BTN_MODE_TOGGLE = 11;

    private final ContainerSubStation container;
    private final BlockPos pos;

    private String selectedParentId;
    private String selectedParentName = "(未選択)";
    private SubStationMode selectedMode;

    private List<StationSnapshot> stationOptions = new ArrayList<>();

    private GuiButton btnParentDropdown;
    private GuiButton btnModeToggle;

    public GuiSubStation(ContainerSubStation container, BlockPos pos) {
        super(container);
        this.container = container;
        this.pos = pos;
        this.xSize = GUI_W;
        this.ySize = GUI_H;

        this.selectedParentId = container.subStation.getParentStationId();
        this.selectedMode = container.subStation.getMode();
    }

    @Override
    public void initGui() {
        super.initGui();
        stationOptions = new ArrayList<>(jp.apple.arad.data.MapData.INSTANCE.getStations());
        resolveParentName();
        rebuildButtons();
    }

    private void resolveParentName() {
        if (selectedParentId == null || selectedParentId.isEmpty()) {
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

    private void rebuildButtons() {
        buttonList.clear();

        btnParentDropdown = new GuiButton(BTN_PARENT_DROPDOWN, guiLeft + 8, guiTop + 24,
                GUI_W - 16, 18, "親駅: " + selectedParentName);
        buttonList.add(btnParentDropdown);

        btnModeToggle = new GuiButton(BTN_MODE_TOGGLE, guiLeft + 8, guiTop + 50,
                GUI_W - 16, 18, "モード: " + selectedMode.label);
        buttonList.add(btnModeToggle);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        sendIfChanged();
    }

    private void sendIfChanged() {
        boolean changed = !java.util.Objects.equals(selectedParentId, container.subStation.getParentStationId())
                || selectedMode != container.subStation.getMode();
        if (!changed)
            return;

        container.subStation.setParentStationId(selectedParentId);
        container.subStation.setMode(selectedMode);
        AradPacketHandler.CHANNEL.sendToServer(
                new PacketSubStationConfig(pos, selectedParentId, selectedMode));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        int id = button.id;
        if (id == BTN_PARENT_DROPDOWN) {
            openParentSelector();
        } else if (id == BTN_MODE_TOGGLE) {
            selectedMode = selectedMode.next();
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

        GuiAppleListSelector selector = new GuiAppleListSelector(
                this,
                btnParentDropdown.x, btnParentDropdown.y + btnParentDropdown.height,
                btnParentDropdown.width, 80,
                () -> initialIndex,
                names,
                (selectedIdx) -> {
                    selectedParentId = stationOptions.get(selectedIdx).id;
                    resolveParentName();
                    rebuildButtons();
                });
        this.mc.displayGuiScreen(selector);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF1A2A55);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + xSize - 1, guiTop + ySize - 1, 0xFF0D1B3E);

        fontRenderer.drawString("§fサブ駅設定", guiLeft + 8, guiTop + 7, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    }
}