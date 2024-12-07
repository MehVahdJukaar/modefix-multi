package net.mehvahdjukaar.modelfix;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static net.mehvahdjukaar.modelfix.ModelFix.*;

public class ModelFixGeom {

    private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.parse("textures/atlas/blocks.png");

    //who needs anti atlas bleeding when it doesn't occur even on mipmap 4 high render distance lol
    //apparently on mac os it does waaa
    public static float getShrinkRatio(ResourceLocation atlasLocation, float defaultValue, float returnValue) {
        if (atlasLocation.equals(BLOCK_ATLAS) && defaultValue == returnValue) {
            return (float) (defaultValue * shrinkMult.get());
        }
        return -1;
    }

    public static void createOrExpandSpan(List<ItemModelGenerator.Span> listSpans, ItemModelGenerator.SpanFacing spanFacing,
                                          int pixelX, int pixelY) {

        ItemModelGenerator.Span existingSpan = null;
        for (ItemModelGenerator.Span span : listSpans) {
            if (span.getFacing() == spanFacing) {
                int i = spanFacing.isHorizontal() ? pixelY : pixelX;
                if (span.getAnchor() == i) {
                    //skips faces with transparent pixels so we can enlarge safely
                    if (expansion.get() != 0 && span.getMax() != (!spanFacing.isHorizontal() ? pixelY : pixelX) - 1)
                        continue;
                    existingSpan = span;
                    break;
                }
            }
        }


        int length = spanFacing.isHorizontal() ? pixelX : pixelY;
        if (existingSpan == null) {
            int newStart = spanFacing.isHorizontal() ? pixelY : pixelX;
            listSpans.add(new ItemModelGenerator.Span(spanFacing, length, newStart));
        } else {
            existingSpan.expand(length);
        }
    }


    public static void enlargeFaces(List<BlockElement> original) {
        double inc = indent.get();
        double inc2 = expansion.get();
        for (var e : original) {
            Vector3f from = e.from;
            Vector3f to = e.to;

            var set = e.faces.keySet();
            if (set.size() == 1) {
                var dir = set.stream().findAny().get();
                switch (dir) {
                    case UP -> {
                        from.set(from.x() - inc2, from.y() - inc, from.z() - inc2);
                        to.set(to.x() + inc2, to.y() - inc, to.z() + inc2);
                    }
                    case DOWN -> {
                        from.set(from.x() - inc2, from.y() + inc, from.z() - inc2);
                        to.set(to.x() + inc2, to.y() + inc, to.z() + inc2);
                    }
                    case WEST -> {
                        from.set(from.x() - inc, from.y() + inc2, from.z() - inc2);
                        to.set(to.x() - inc, to.y() - inc2, to.z() + inc2);
                    }
                    case EAST -> {
                        from.set(from.x() + inc, from.y() + inc2, from.z() - inc2);
                        to.set(to.x() + inc, to.y() - inc2, to.z() + inc2);
                    }
                }
            }
        }
    }
}
