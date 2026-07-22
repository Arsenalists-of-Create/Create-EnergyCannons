package net.arsenalists.createenergycannons.client;

import net.arsenalists.createenergycannons.report.CrashReporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

/** Shows the crash-share prompt once, from the title screen, when {@link CrashReporter} has one pending. */
public final class CrashReportClient {

    private static boolean handled = false;

    private CrashReportClient() {}

    public static void clientTick() {
        if (handled || !CrashReporter.hasPending()) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof TitleScreen)) return; // wait for the menu, don't cut into loading
        handled = true;
        net.arsenalists.createenergycannons.CECMod.getLogger()
                .info("Offering to share a recent crash report on the title screen");
        mc.setScreen(new CrashReportScreen(mc.screen, CrashReporter.isAutoEnabled()));
    }
}
