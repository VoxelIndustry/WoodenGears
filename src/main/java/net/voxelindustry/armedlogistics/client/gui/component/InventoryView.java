package net.voxelindustry.armedlogistics.client.gui.component;

import fr.ourten.teabeans.binding.BaseExpression;
import fr.ourten.teabeans.value.BaseProperty;
import lombok.Getter;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.voxelindustry.armedlogistics.ArmedLogistics;
import net.voxelindustry.armedlogistics.common.grid.logistic.node.IItemFilter;
import net.voxelindustry.armedlogistics.common.tile.TileLogicisticNode;
import net.voxelindustry.brokkgui.data.RectAlignment;
import net.voxelindustry.brokkgui.data.RectBox;
import net.voxelindustry.brokkgui.data.RelativeBindingHelper;
import net.voxelindustry.brokkgui.element.GuiLabel;
import net.voxelindustry.brokkgui.element.input.GuiButton;
import net.voxelindustry.brokkgui.element.input.GuiToggleButton;
import net.voxelindustry.brokkgui.panel.GuiAbsolutePane;
import net.voxelindustry.brokkgui.shape.Rectangle;
import net.voxelindustry.brokkgui.wrapper.container.BrokkGuiContainer;
import net.voxelindustry.brokkgui.wrapper.elements.ItemStackView;
import net.voxelindustry.brokkgui.wrapper.elements.MCTooltip;
import net.voxelindustry.steamlayer.container.BuiltContainer;

import java.util.ArrayList;
import java.util.List;

public class InventoryView extends GuiAbsolutePane
{
    private final List<ItemStack>     rawStacks;
    private final List<ItemStackView> stacks;
    private final GuiAbsolutePane     stacksPane;
    private final GuiButton           moreButton;
    private final GuiLabel            emptyLabel;
    private final GuiLabel            invLabel;

    private GuiToggleButton hideButton;

    private final FullInventoryView fullStackView;
    private final Rectangle         shadowBottom;
    private final Rectangle         shadowRight;

    @Getter
    private BaseProperty<Boolean> showFiltered;

    private IItemFilter tileFilter;

    public InventoryView(BrokkGuiContainer<BuiltContainer> parent, TileLogicisticNode tile)
    {
        this.setSize(164, 74);

        this.showFiltered = new BaseProperty<>(true, "showFilteredProperty");
        this.showFiltered.addListener((obs, oldValue, newValue) ->
        {
            hideButton.setSelected(!newValue);
            this.refreshStacks(tile.getCachedInventoryProperty().getValue());
        });

        this.rawStacks = new ArrayList<>();
        this.stacks = new ArrayList<>();
        this.stacksPane = new GuiAbsolutePane();
        this.fullStackView = new FullInventoryView();

        stacksPane.setID("stacks-panel");
        stacksPane.setSize(164, 56);
        this.addChild(stacksPane, 0, 9);

        this.moreButton = new GuiButton();
        moreButton.setSize(148, 10);
        moreButton.setVisible(false);
        moreButton.setID("more-button");
        this.addChild(moreButton);
        moreButton.getLabel().setTextPadding(RectBox.build().top(1).create());
        moreButton.setxTranslate(7);
        RelativeBindingHelper.bindToPos(moreButton, stacksPane, null,
                BaseExpression.transform(stacksPane.getHeightProperty(), height -> height - 1));

        moreButton.setOnActionEvent(e ->
        {
            parent.addSubGui(fullStackView);
            fullStackView.refreshStacks(this.rawStacks);
        });

        this.emptyLabel = new GuiLabel(I18n.format(ArmedLogistics.MODID + ".gui.inventory.empty"));
        emptyLabel.setSize(148, 13);
        emptyLabel.setVisible(false);
        emptyLabel.setID("empty-label");
        this.addChild(emptyLabel, 8, 37 - 6.5f);

        this.invLabel = new GuiLabel("");
        invLabel.setHeight(9);
        invLabel.setExpandToText(true);
        invLabel.setTextAlignment(RectAlignment.LEFT_CENTER);
        invLabel.setID("inv-label");
        invLabel.setTextPadding(RectBox.build().left(2).right(2).top(1).create());
        this.addChild(invLabel, 1, 1);

        if (tile instanceof IItemFilter)
        {
            this.tileFilter = (IItemFilter) tile;

            this.hideButton = new GuiToggleButton();
            hideButton.setID("hide-button");
            hideButton.setTooltip(MCTooltip.build().dynamicLines(lines ->
            {
                lines.add(I18n.format(ArmedLogistics.MODID + ".gui.inventory.hidefiltered." +
                        (hideButton.isSelected() ? "off" : "on")));
            }).create());
            hideButton.setSize(12, 9);
            hideButton.getSelectedProperty().addListener((obs, oldValue, newValue) -> this.showFiltered.setValue(!newValue));

            this.addChild(hideButton, 164 - 12 - 2, 0);
        }

        this.shadowBottom = new Rectangle();
        this.addChild(shadowBottom, 1, 10);
        shadowBottom.setWidth(162);
        shadowBottom.addStyleClass("shadow-rect");

        this.shadowRight = new Rectangle();
        this.addChild(shadowRight, 1, 10);
        shadowRight.setHeight(18);
        shadowRight.addStyleClass("shadow-rect");

        parent.getListeners().attach(tile.getCachedInventoryProperty(),
                obs -> refreshStacks(tile.getCachedInventoryProperty().getValue()));
        this.refreshStacks(tile.getCachedInventoryProperty().getValue());
    }

    public void setInvStatus(String status)
    {
        this.invLabel.setText(status);
    }

    public void setInvValid(boolean valid)
    {
        if (valid)
        {
            this.invLabel.addStyleClass("status-valid");
            this.invLabel.removeStyleClass("status-invalid");
        }
        else
        {
            this.invLabel.addStyleClass("status-invalid");
            this.invLabel.removeStyleClass("status-valid");
        }
    }

    public void refreshStacks(IItemHandler inventory)
    {
        if (inventory == null)
        {
            this.emptyLabel.setVisible(true);
            this.stacksPane.setDisabled(true);
            return;
        }

        List<ItemStack> rawStacks = new ArrayList<>(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            ItemStack inSlot = inventory.getStackInSlot(slot);

            if (!inSlot.isEmpty() && (showFiltered.getValue() || tileFilter.test(inSlot)))
                rawStacks.add(inSlot);
        }

        int diff = this.stacks.size() - rawStacks.size();
        if (diff > 0)
        {
            int removeStart = this.stacks.size() - diff;
            for (int i = 0; i < diff; i++)
                stacksPane.removeChild(this.stacks.remove(removeStart));

            for (int i = 0; i < stacks.size(); i++)
            {
                ItemStackView view = stacks.get(i);
                view.setTranslate(18 * (i % 9), 18 * (i / 9));
            }
        }
        else if (diff < 0 && stacks.size() < 27)
        {
            diff = -diff;
            for (int slot = 0; slot < diff; slot++)
            {
                if (stacks.size() == 27)
                    break;

                ItemStackView view = new ItemStackView();
                view.setSize(18, 18);
                view.setItemTooltip(true);

                stacksPane.addChild(view, 1, 1);
                view.setTranslate(18 * (stacks.size() % 9), 18 * (stacks.size() / 9));
                stacks.add(view);
            }
        }

        this.moreButton.setVisible(false);
        this.emptyLabel.setVisible(false);
        this.stacksPane.setDisabled(false);

        if (this.stacks.isEmpty())
        {
            this.emptyLabel.setVisible(true);
            this.stacksPane.setDisabled(true);
        }
        else if (rawStacks.size() > 27)
        {
            this.rawStacks.clear();
            this.rawStacks.addAll(rawStacks);
            this.moreButton.setVisible(true);
            this.moreButton.setText(I18n.format(ArmedLogistics.MODID + ".gui.inventory.more", rawStacks.size() - 27));
        }

        for (int slot = 0; slot < Math.min(stacks.size(), 27); slot++)
            stacks.get(slot).setItemStack(rawStacks.get(slot));

        this.updateInventoryShadows(stacks.size());
    }

    private void updateInventoryShadows(int stackCount)
    {
        if (stackCount == 0 || stackCount == 27)
        {
            this.shadowBottom.setVisible(false);
            this.shadowRight.setVisible(false);
            return;
        }

        this.shadowBottom.setVisible(true);
        this.shadowRight.setVisible(true);

        int shadowWidth = (9 - stackCount % 9) * 18;
        int shadowHeight = (3 - (int) Math.ceil(stackCount / 9f)) * 18;

        if (shadowWidth == 0 || shadowWidth == 162)
            this.shadowRight.setVisible(false);
        if (shadowHeight == 0)
            this.shadowBottom.setVisible(false);

        this.shadowBottom.setHeight(shadowHeight);
        this.shadowBottom.setyTranslate(18 * 3 - shadowHeight);

        this.shadowRight.setWidth(shadowWidth);
        this.shadowRight.setxTranslate(18 * 9 - shadowWidth);
        this.shadowRight.setyTranslate(18 * 2 - shadowHeight);
    }
}
