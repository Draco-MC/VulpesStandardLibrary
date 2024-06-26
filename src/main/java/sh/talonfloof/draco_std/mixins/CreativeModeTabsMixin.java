package sh.talonfloof.draco_std.mixins;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.talonfloof.draco_std.creative_tab.DracoCreativeModeTab;
import sh.talonfloof.draco_std.creative_tab.DracoCreativeTabVars;
import sh.talonfloof.draco_std.debug.DracoEarlyLog;
import sh.talonfloof.draco_std.loading.DracoLoadingScreen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsMixin {
    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void validate(CallbackInfo ci) {
        DracoEarlyLog.addToLog("CreativeModeTabs -> FREEZE_DATA");
        DracoLoadingScreen.createCustomProgressBar("CreativeTabs","CreativeModeTabs -> FREEZE_DATA",0);
        int count = 0;
        final List<ResourceKey<CreativeModeTab>> vanillaGroups = List.of(
                ICreativeModeTabsAccessor.BUILDING_BLOCKS(),
                ICreativeModeTabsAccessor.COLORED_BLOCKS(),
                ICreativeModeTabsAccessor.NATURAL_BLOCKS(),
                ICreativeModeTabsAccessor.FUNCTIONAL_BLOCKS(),
                ICreativeModeTabsAccessor.REDSTONE_BLOCKS(),
                ICreativeModeTabsAccessor.HOTBAR(),
                ICreativeModeTabsAccessor.SEARCH(),
                ICreativeModeTabsAccessor.TOOLS_AND_UTILITIES(),
                ICreativeModeTabsAccessor.COMBAT(),
                ICreativeModeTabsAccessor.FOOD_AND_DRINKS(),
                ICreativeModeTabsAccessor.INGREDIENTS(),
                ICreativeModeTabsAccessor.SPAWN_EGGS(),
                ICreativeModeTabsAccessor.OP_BLOCKS(),
                ICreativeModeTabsAccessor.INVENTORY());
        final List<ResourceKey<CreativeModeTab>> sortedItemGroups = BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet().stream()
                .sorted(Comparator.comparing(ResourceKey::location))
                .toList();
        for (ResourceKey<CreativeModeTab> registryKey : sortedItemGroups) {
            final CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(registryKey);
            final DracoCreativeModeTab dracoTab = (DracoCreativeModeTab)tab;

            if (vanillaGroups.contains(registryKey)) {
                // Vanilla group goes on the first page.
                dracoTab.setPage(0);
                continue;
            }
            final ICreativeModeTabAccessor itemGroupAccessor = (ICreativeModeTabAccessor)tab;
            dracoTab.setPage((count / 10) + 1);
            int pageIndex = count % 10;
            CreativeModeTab.Row row = pageIndex < (10 / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            itemGroupAccessor.setRow(row);
            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % 10 : (pageIndex - 10 / 2) % (10));
            count++;
        }
        DracoCreativeTabVars.pageCount = Math.ceilDiv(count,10)+1;
        record TabPosition(CreativeModeTab.Row row, int column, int page) { }   
        var map = new HashMap<TabPosition, String>();
        for (ResourceKey<CreativeModeTab> registryKey : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
            final CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(registryKey);
            final DracoCreativeModeTab dracoTab = (DracoCreativeModeTab)tab;
            final String displayName = tab.getDisplayName().getString();
            final var position = new TabPosition(tab.row(), tab.column(), dracoTab.getPage());
            final String existingName = map.put(position, displayName);

            if (existingName != null) {
                throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
            }
        }
        DracoLoadingScreen.updateCustomBar("CreativeTabs",null,null,null);
        ci.cancel();
    }
}
