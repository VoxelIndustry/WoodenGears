package net.voxelindustry.armedlogistics.common.grid.logistic.node;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.voxelindustry.armedlogistics.common.grid.logistic.LogisticNetwork;
import net.voxelindustry.armedlogistics.common.grid.logistic.LogisticOrder;
import net.voxelindustry.armedlogistics.common.grid.logistic.LogisticShipment;
import net.voxelindustry.armedlogistics.common.grid.logistic.ProviderType;
import net.voxelindustry.armedlogistics.common.serializer.LogisticShipmentSerializer;
import net.voxelindustry.armedlogistics.common.tile.TileLogicisticNode;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BaseItemProvider extends BaseLogisticNode<ItemStack> implements Provider<ItemStack>
{
    @Getter(AccessLevel.PROTECTED)
    private IItemHandler           handler;
    @Getter(AccessLevel.PROTECTED)
    private InventoryBuffer        buffer;
    private NonNullList<ItemStack> handlerMirror;
    private NonNullList<ItemStack> compressedStacks;

    private List<LogisticShipment<ItemStack>> shipments;

    @Getter
    private ProviderType       providerType;
    @Getter(AccessLevel.PROTECTED)
    private TileLogicisticNode tile;
    @Getter
    @Setter
    private IItemFilter        filter = IItemFilter.ALWAYS_TRUE;

    private boolean isDirtyFromExternal;

    public BaseItemProvider(TileLogicisticNode tile,
                            ProviderType type,
                            IItemHandler handler,
                            Supplier<LogisticNetwork<ItemStack>> networkSupplier,
                            InventoryBuffer buffer)
    {
        super(networkSupplier);

        this.tile = tile;
        this.handler = handler;
        this.buffer = buffer;

        if (tile instanceof IItemFilter)
            filter = (IItemFilter) tile;

        providerType = type;

        handlerMirror = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        compressedStacks = NonNullList.create();

        shipments = new ArrayList<>();
    }

    @Override
    public BlockPos getRailPos()
    {
        return tile.getRailPos();
    }

    @Override
    public void wake()
    {
        if (isAwake())
            return;

        boolean isDirty = false;

        if (handlerMirror.size() != handler.getSlots())
        {
            isDirty = true;
            handlerMirror = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        }

        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack stack = handler.getStackInSlot(i);

            if (!ItemUtils.deepEqualsWithAmount(stack, handlerMirror.get(i)))
            {
                isDirty = true;
                if (stack.isEmpty())
                    handlerMirror.set(i, ItemStack.EMPTY);
                else
                    handlerMirror.set(i, stack.copy());
            }
        }

        if (isDirty || isDirtyFromExternal)
        {
            compressedStacks.clear();

            for (int i = 0; i < handler.getSlots(); i++)
            {
                ItemStack stack = handler.getStackInSlot(i);

                if (!filter.filter(stack))
                    continue;

                Optional<ItemStack> found =
                        compressedStacks.stream().filter(candidate -> ItemUtils.deepEquals(candidate, stack)).findFirst();
                if (found.isPresent())
                    found.get().grow(stack.getCount());
                else
                    compressedStacks.add(stack.copy());
            }

            isDirtyFromExternal = false;
        }

        super.wake();
    }

    @Override
    public int containedPart(ItemStack value)
    {
        if (value.isEmpty())
            return 0;

        wake();

        int quantity = 0;
        for (ItemStack stack : compressedStacks)
        {
            if (stack.isEmpty())
                continue;
            if (ItemUtils.deepEquals(stack, value))
            {
                quantity += stack.getCount();

                if (quantity >= value.getCount())
                    return value.getCount();
            }
        }

        return quantity;
    }

    @Override
    public boolean contains(ItemStack value)
    {
        if (value.isEmpty())
            return false;

        wake();

        for (ItemStack stack : compressedStacks)
        {
            if (stack.isEmpty())
                continue;
            if (ItemUtils.deepEquals(stack, value) && stack.getCount() >= value.getCount())
                return true;
        }
        return false;
    }

    @Override
    public boolean anyMatch(Predicate<ItemStack> matcher)
    {
        wake();

        for (ItemStack stack : compressedStacks)
        {
            if (matcher.test(stack))
                return true;
        }
        return false;
    }

    @Override
    public ItemStack firstMatching(Predicate<ItemStack> matcher)
    {
        wake();

        for (ItemStack stack : compressedStacks)
        {
            if (matcher.test(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> allMatching(Predicate<ItemStack> matcher)
    {
        wake();

        ArrayList<ItemStack> stacks = new ArrayList<>(handler.getSlots());

        for (ItemStack stack : compressedStacks)
        {
            if (matcher.test(stack))
                stacks.add(stack);
        }
        stacks.trimToSize();
        return stacks;
    }

    @Override
    public ItemStack extract(ItemStack value)
    {
        if (buffer.isFull())
            return ItemStack.EMPTY;

        if (containedPart(value) == 0)
            return ItemStack.EMPTY;

        sleep();

        int extracted = 0;

        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack stack = handler.getStackInSlot(i);

            if (ItemUtils.deepEquals(stack, value))
            {
                ItemStack extractedStack = stack.copy();
                extractedStack.setCount(value.getCount() - extracted);

                extractedStack = handler.extractItem(i, extractedStack.getCount(), true);

                extracted += extractedStack.getCount();

                handler.extractItem(i, extractedStack.getCount(), false);

                if (extracted == value.getCount())
                    break;
            }
        }

        ItemStack copy = value.copy();
        copy.setCount(extracted);
        return buffer.add(copy);
    }

    @Override
    public ItemStack fromBuffer(ItemStack value)
    {
        buffer.remove(value);
        return value;
    }

    @Override
    public boolean isBufferFull()
    {
        return buffer.isFull();
    }

    @Override
    public NonNullList<ItemStack> getCompressedContents()
    {
        return compressedStacks;
    }

    @Override
    public boolean isColored()
    {
        return false;
    }

    @Override
    public void markDirty()
    {
        isDirtyFromExternal = true;
    }

    @Override
    public void addShipment(LogisticShipment<ItemStack> shipment)
    {
        shipments.add(shipment);
    }

    @Override
    public boolean removeShipment(LogisticShipment<ItemStack> shipment)
    {
        return shipments.remove(shipment);
    }

    @Override
    public Collection<LogisticShipment<ItemStack>> getShipments()
    {
        return shipments;
    }

    @Override
    public void deliverShipment(LogisticShipment<ItemStack> shipment)
    {

    }

    @Override
    public void addOrder(LogisticOrder<ItemStack> order)
    {

    }

    @Override
    public void removeOrder(LogisticOrder<ItemStack> order)
    {

    }

    public CompoundNBT toNBT(CompoundNBT tag)
    {
        int i = 0;
        for (LogisticShipment<ItemStack> shipment : shipments)
        {
            tag.put("shipment" + i, LogisticShipmentSerializer.itemShipmentToNBT(shipment));
            i++;
        }
        tag.putInt("shipmentCount", i);

        return tag;
    }

    public void fromNBT(CompoundNBT tag)
    {
        int count = tag.getInt("shipmentCount");

        for (int i = 0; i < count; i++)
            shipments.add(LogisticShipmentSerializer.itemShipmentFromNBT(tag.getCompound("shipment" + i)));
    }
}
