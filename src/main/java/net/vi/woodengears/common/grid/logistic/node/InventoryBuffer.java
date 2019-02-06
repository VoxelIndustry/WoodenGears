package net.vi.woodengears.common.grid.logistic.node;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.voxelindustry.steamlayer.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryBuffer
{
    private NonNullList<ItemStack> stacks;

    @Getter
    private int typeCapacity;
    @Getter
    private int countCapacity;

    @Getter
    private int currentCount;

    @Getter
    private boolean isFull;

    /**
     * @param typeCapacity  how many different stacks this buffer will store.
     * @param countCapacity maximum sum of all contained stacks.
     */
    public InventoryBuffer(int typeCapacity, int countCapacity)
    {
        this.typeCapacity = typeCapacity;
        this.countCapacity = countCapacity;

        this.stacks = NonNullList.withSize(typeCapacity, ItemStack.EMPTY);
    }

    public ItemStack add(ItemStack stack)
    {
        if (this.isFull())
            return ItemStack.EMPTY;

        int freeSlot = 0;

        for (ItemStack contained : stacks)
        {
            if (!contained.isEmpty() && !ItemUtils.deepEquals(stack, contained))
                freeSlot++;
            else
                break;
        }

        if (freeSlot == stacks.size())
            return ItemStack.EMPTY;

        ItemStack added = stack;
        int quantity = stack.getCount();

        if (this.getCountCapacity() - this.getCurrentCount() < quantity)
            quantity = this.getCountCapacity() - this.getCurrentCount();

        if (quantity != stack.getCount())
        {
            added = stack.copy();
            added.setCount(quantity);
        }

        if (stacks.get(freeSlot).isEmpty())
            stacks.set(freeSlot, added);
        else
            stacks.get(freeSlot).grow(quantity);

        this.currentCount += quantity;

        if (this.getCurrentCount() >= this.getCountCapacity())
            this.isFull = true;

        return added;
    }

    public List<ItemStack> addAll(List<ItemStack> stacks)
    {
        List<ItemStack> added = new ArrayList<>();

        for (ItemStack stack : stacks)
        {
            added.add(this.add(stack));

            if (this.isFull)
                break;
        }

        return added;
    }

    public void remove(ItemStack stack)
    {
        int matchedSlot = 0;

        for (ItemStack contained : stacks)
        {
            if (contained.isEmpty() || !ItemUtils.deepEquals(stack, contained))
                matchedSlot++;
            else
                break;
        }

        if (matchedSlot == stacks.size())
            throw new RuntimeException("Invalid stack retrieval from Buffer!");

        stacks.get(matchedSlot).shrink(stack.getCount());

        this.currentCount -= stack.getCount();
        this.isFull = false;
    }

    public void removeAll(List<ItemStack> stacks)
    {
        stacks.forEach(this::remove);
    }

    public NBTTagCompound writeNBT(NBTTagCompound tag)
    {
        for (int i = 0; i < stacks.size(); i++)
        {
            ItemStack stack = stacks.get(i);

            tag.setTag("stack" + i, stack.writeToNBT(new NBTTagCompound()));
        }
        tag.setInteger("count", this.currentCount);
        tag.setBoolean("isFull", this.isFull);

        return tag;
    }

    public void readNBT(NBTTagCompound tag)
    {
        for (int i = 0; i < this.typeCapacity; i++)
            stacks.set(i, new ItemStack(tag.getCompoundTag("stack" + i)));

        this.currentCount = tag.getInteger("count");
        this.isFull = tag.getBoolean("isFull");
    }
}