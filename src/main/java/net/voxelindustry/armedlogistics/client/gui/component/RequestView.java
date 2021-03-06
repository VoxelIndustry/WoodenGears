package net.voxelindustry.armedlogistics.client.gui.component;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.voxelindustry.armedlogistics.ArmedLogistics;
import net.voxelindustry.brokkgui.BrokkGuiPlatform;
import net.voxelindustry.brokkgui.data.RectAlignment;
import net.voxelindustry.brokkgui.data.RectBox;
import net.voxelindustry.brokkgui.element.GuiLabel;
import net.voxelindustry.brokkgui.event.HoverEvent;
import net.voxelindustry.brokkgui.event.KeyEvent;
import net.voxelindustry.brokkgui.internal.PopupHandler;
import net.voxelindustry.brokkgui.panel.GuiAbsolutePane;
import net.voxelindustry.brokkgui.wrapper.elements.ItemStackView;

import java.util.List;
import java.util.function.BiConsumer;

public class RequestView extends GuiAbsolutePane implements ICopyPasteHandler<MutableItemStackView>
{
    private ItemStackView[]      requests;
    private MutableItemStackView selected;

    public RequestView(List<ItemStack> requestList, BiConsumer<Integer, ItemStack> onRequestChange)
    {
        setSize(164, 29);
        setID("requestview");

        GuiLabel requestLabel = new GuiLabel(I18n.format(ArmedLogistics.MODID + ".gui.request.title"));
        requestLabel.setExpandToText(true);
        requestLabel.setHeight(9);
        requestLabel.setTextAlignment(RectAlignment.LEFT_CENTER);
        requestLabel.setTextPadding(RectBox.build().left(2).right(2).top(1).create());
        requestLabel.setID("request-label");
        addChild(requestLabel, 1, 1);

        requests = new ItemStackView[9];
        for (int i = 0; i < 9; i++)
        {
            int finalIndex = i;

            MutableItemStackView view = new MutableItemStackView(requestList.size() > i ? requestList.get(i).copy() :
                    ItemStack.EMPTY, true, this);
            view.setSize(18, 18);
            view.getStackProperty().addListener(obs -> onRequestChange.accept(finalIndex, view.getItemStack()));

            requests[i] = view;
            addChild(view, i * 18 + 1, 10);
        }

        setFocusable(true);
        getEventDispatcher().addHandler(HoverEvent.TYPE, this::onHover);
        getEventDispatcher().addHandler(KeyEvent.PRESS, this::onKeyPressed);
        getEventDispatcher().addHandler(KeyEvent.RELEASE, this::onKeyReleased);
    }

    public void setRequestStack(int index, ItemStack stack)
    {
        requests[index].setItemStack(stack);
    }

    private void onKeyPressed(KeyEvent.Press event)
    {
        if (BrokkGuiPlatform.getInstance().getKeyboardUtil().isCtrlKeyDown() &&
                !getStyleClass().contains("copypasting"))
        {
            addStyleClass("copypasting");

            if (selected != null)
                selected.addStyleClass("selected");
        }
    }

    private void onKeyReleased(KeyEvent.Release event)
    {
        if (!BrokkGuiPlatform.getInstance().getKeyboardUtil().isCtrlKeyDown() &&
                getStyleClass().contains("copypasting"))
        {
            removeStyleClass("copypasting");

            if (selected != null)
                selected.removeStyleClass("selected");
        }
    }

    private void onHover(HoverEvent event)
    {
        if (!BrokkGuiPlatform.getInstance().getKeyboardUtil().isCtrlKeyDown())
            return;

        if (event.isEntering())
        {
            addStyleClass("copypasting");

            if (selected != null)
                selected.addStyleClass("selected");
        }
        else if (getStyleClass().contains("copypasting"))
        {
            removeStyleClass("copypasting");
            if (selected != null)
                selected.removeStyleClass("selected");
        }
    }

    @Override
    public void setClipboard(MutableItemStackView value)
    {
        if (value != null)
            addStyleClass("copypasting");

        if (value == selected)
            return;

        showCopiedPopup(value);

        if (selected != null)
            selected.removeStyleClass("selected");
        selected = value;

        if (selected != null)
            selected.addStyleClass("selected");
    }

    @Override
    public MutableItemStackView getClipboard()
    {
        return selected;
    }

    private void showCopiedPopup(MutableItemStackView node)
    {
        PopupHandler.getInstance(getWindow()).addPopup(new MiniStatePopup(node,
                I18n.format("armedlogistics.gui.copy")));
    }
}
