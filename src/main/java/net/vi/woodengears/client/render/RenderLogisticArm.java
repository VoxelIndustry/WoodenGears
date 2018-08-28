package net.vi.woodengears.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.vi.woodengears.WoodenGears;
import net.vi.woodengears.common.entity.EntityLogisticArm;

import javax.annotation.Nullable;

public class RenderLogisticArm extends Render<EntityLogisticArm>
{
    private       ModelLogisticArm modelLogisticArm = new ModelLogisticArm();
    private final RenderItem       itemRenderer;

    public RenderLogisticArm(RenderManager renderManager)
    {
        super(renderManager);
        this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
    }

    @Override
    public void doRender(EntityLogisticArm logisticArm, double x, double y, double z, float entityYaw,
                         float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.25F, (float) y + 0.75F, (float) z);

        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        this.bindEntityTexture(logisticArm);

        this.modelLogisticArm.renderPedestal(0.0625F);
        this.modelLogisticArm.renderFirstPiston(0.0625F);

        if (logisticArm.isHoverBlockProvider())
        {
            if (logisticArm.getPickupCount() > 40)
            {
                GlStateManager.translate(0, 2.35 * (1 - (logisticArm.getPickupCount() / 40D) / 2D), 0);
                this.modelLogisticArm.renderSecondPiston(0.0625F);
                GlStateManager.translate(0, 2 * (1 - (logisticArm.getPickupCount() / 40D) / 2D), 0);
                this.modelLogisticArm.renderHead(0.0625F);
            }
            else
            {
                GlStateManager.translate(0, 1.175 * (logisticArm.getPickupCount() / 40D), 0);
                this.modelLogisticArm.renderSecondPiston(0.0625F);
                GlStateManager.translate(0, (logisticArm.getPickupCount() / 40D), 0);
                this.modelLogisticArm.renderHead(0.0625F);
            }
        }
        else
        {
            this.modelLogisticArm.renderSecondPiston(0.0625F);
            this.modelLogisticArm.renderHead(0.0625F);
        }

        GlStateManager.translate(0.5D, 1.375D, 0.0D);
        this.renderItem(logisticArm);

        GlStateManager.popMatrix();
    }

    private void renderItem(EntityLogisticArm logisticArm)
    {
        ItemStack itemStack = logisticArm.getStackInSlot(0);

        if (!itemStack.isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.pushAttrib();
            RenderHelper.disableStandardItemLighting();
            this.itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popAttrib();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLogisticArm entity)
    {
        return new ResourceLocation(WoodenGears.MODID, "textures/models/logistic_arm.png");
    }

    private double interp(double previous, double next, double partialTicks)
    {
        return previous + (next - previous) * partialTicks;
    }
}