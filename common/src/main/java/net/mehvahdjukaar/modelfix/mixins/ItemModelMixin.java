package net.mehvahdjukaar.modelfix.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.modelfix.ModelFixGeom;
import net.mehvahdjukaar.modelfix.PlatStuff;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ItemModelGenerator.class)
public abstract class ItemModelMixin {

    @ModifyReturnValue(method = "createSideElements", at = @At("RETURN"))
    public List<BlockElement> increaseSide(List<BlockElement> original) {
        if (PlatStuff.isModStateValid()) ModelFixGeom.enlargeFaces(original);
        return original;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixing item models gaps
     */
    @Overwrite
    private void createOrExpandSpan(List<ItemModelGenerator.Span> listSpans, ItemModelGenerator.SpanFacing spanFacing, int pixelX, int pixelY) {
        if (PlatStuff.isModStateValid()){
            ModelFixGeom.createOrExpandSpan(listSpans, spanFacing, pixelX, pixelY);
        }
    }

}
