package net.arsenalists.createenergycannons.client;

import net.arsenalists.createenergycannons.report.CrashReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Crash-share prompt: Send / Always / Not now, or auto-share if already enabled. Copies the link to the clipboard. */
public class CrashReportScreen extends Screen {

    private final Screen parent;
    private final boolean auto;
    private String status;

    public CrashReportScreen(Screen parent, boolean auto) {
        super(Component.literal("Create: Energy Cannons"));
        this.parent = parent;
        this.auto = auto;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2;

        if (status != null) {
            addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                    .bounds(cx - 100, y + 36, 200, 20).build());
            return;
        }

        if (auto) {
            status = "Sharing crash report…";
            CrashReporter.submitPending(this::onResult);
            return;
        }

        addRenderableWidget(Button.builder(Component.literal("Send crash report"), b -> share())
                .bounds(cx - 100, y, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Always send automatically"), b -> {
            CrashReporter.setAutoEnabled(true);
            share();
        }).bounds(cx - 100, y + 24, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Not this time"), b -> {
            CrashReporter.dismissPending();
            onClose();
        }).bounds(cx - 100, y + 48, 200, 20).build());
    }

    private void share() {
        status = "Sharing crash report…";
        rebuildWidgets();
        CrashReporter.submitPending(this::onResult);
    }

    private void onResult(String link) {
        Minecraft.getInstance().execute(() -> {
            if (link != null) {
                Minecraft.getInstance().keyboardHandler.setClipboard(link);
                status = "Shared! Link copied to your clipboard:\n" + link;
            } else {
                status = "Couldn't upload it. You can still share the file in crash-reports/ manually.";
            }
            if (Minecraft.getInstance().screen == this) rebuildWidgets();
        });
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        //? if <1.21 {
        this.renderBackground(g);
        //?}
        super.render(g, mouseX, mouseY, partial);
        int cx = this.width / 2;
        g.drawCenteredString(this.font, "A recent crash looks like it involved Create: Energy Cannons.",
                cx, this.height / 2 - 40, 0xFFFFFF);
        if (status != null) {
            int y = this.height / 2 - 14;
            for (String line : status.split("\n")) {
                g.drawCenteredString(this.font, line, cx, y, 0xA0A0A0);
                y += 12;
            }
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
