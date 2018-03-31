package net.opmcorp.woodengears.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.opmcorp.woodengears.common.block.BlockCable;
import net.opmcorp.woodengears.common.init.WGItems;

public class EntityLogisticArm extends Entity implements ILockableContainer
{
    public static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityLogisticArm.class, DataSerializers.FLOAT);
    private NonNullList<ItemStack> logisticArmItems = NonNullList.withSize(1, ItemStack.EMPTY);

    public EntityLogisticArm(World worldIn)
    {
        super(worldIn);
        this.setSize(1.0F, 1.0F);
    }

    public EntityLogisticArm(World world, BlockPos pos)
    {
        this(world);
        this.setPositionAndRotation(pos.getX() + 0.5D, pos.getY() - 1.0D, pos.getZ() + 0.5D, 0.0F, 90.0F);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = pos.getX();
        this.prevPosY = pos.getY();
        this.prevPosZ = pos.getZ();
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(DAMAGE, 0.0F);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    @Override
    public void onUpdate()
    {
        if(!this.world.isRemote)
        {
            if(!(this.world.getBlockState(new BlockPos(posX, posY + 1.0D, posZ)).getBlock() instanceof BlockCable))
            {
                this.setDead();
                if(this.world.getGameRules().getBoolean("doEntityDrops"))
                {
                    ItemStack logistic_arm = new ItemStack(WGItems.logistic_arm);

                    InventoryHelper.dropInventoryItems(this.world, this, this);

                    this.entityDropItem(logistic_arm, 0.0F);
                }
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        ItemStackHelper.saveAllItems(compound, this.logisticArmItems);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        this.logisticArmItems = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.logisticArmItems);
    }

    @Override
    public boolean isLocked()
    {
        return false;
    }

    @Override
    public void setLockCode(LockCode code)
    {
    }

    @Override
    public LockCode getLockCode()
    {
        return LockCode.EMPTY_CODE;
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack itemStack : this.logisticArmItems)
        {
            if(!itemStack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.logisticArmItems.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return ItemStackHelper.getAndSplit(this.logisticArmItems, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack itemStack = this.logisticArmItems.get(index);

        if(itemStack.isEmpty())
            return ItemStack.EMPTY;
        else
        {
            this.logisticArmItems.set(index, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.logisticArmItems.set(index, stack);

        if(!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
            stack.setCount(this.getInventoryStackLimit());
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if(this.isDead)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq(this) <= 64.0D;
        }
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        this.logisticArmItems.clear();
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return null;
    }

    @Override
    public String getGuiID()
    {
        return null;
    }
}