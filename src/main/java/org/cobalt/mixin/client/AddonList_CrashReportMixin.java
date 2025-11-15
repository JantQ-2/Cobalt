package org.cobalt.mixin.client;

import net.minecraft.util.crash.CrashReport;
import org.cobalt.internal.loader.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Collectors;

@Mixin(CrashReport.class)
public abstract class AddonList_CrashReportMixin {
    
    @Inject(
        method = "addDetails",
        at = @At("HEAD")
    )
    private void addAddonInfo(StringBuilder crashReportBuilder, CallbackInfo ci) {
        String addons = Loader.INSTANCE.getLoadedAddonsList().stream()
            .map(info -> info.getPath().getFileName().toString())
            .collect(Collectors.joining(", "));
        
        if (addons.isEmpty()) {
            addons = "None";
        }
        
        int count = Loader.INSTANCE.getActiveAddonCount();
        
        crashReportBuilder.append("\n========================================")
                         .append("\nCobalt Addons (").append(count).append("): ").append(addons)
                         .append("\n========================================\n");
    }
}