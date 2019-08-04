package net.vi.woodengears.common.entity;

import net.minecraft.util.math.BlockPos;
import net.vi.woodengears.common.grid.Path;

public class PathToStepConverter
{
    public static double getOffsetFromCorner(BlockPos previous, BlockPos next)
    {
        if (previous.getX() == 1)
        {
            if (next.getZ() == -1)
                return 9 / 32D;
            else if (next.getZ() == 1)
                return 9 / 32D;

        }
        else if (previous.getX() == -1)
        {
            if (next.getZ() == -1)
                return 23 / 32D;
            else if (next.getZ() == 1)
                return 23 / 32D;
        }
        else if (previous.getZ() == -1)
        {
            if (next.getX() == -1)
                return 9 / 32D;
            else if (next.getX() == 1)
                return 23 / 32D;
        }
        else if (previous.getZ() == 1)
        {
            if (next.getX() == -1)
                return 9 / 32D;
            else if (next.getX() == 1)
                return 23 / 32D;
        }
        return 0;
    }

    public static double getSideOffsetFromCorner(BlockPos previous, BlockPos next)
    {
        if (previous.getX() == 1)
        {
            if (next.getZ() == -1)
                return 7 / 32D;
            else if (next.getZ() == 1)
                return -7 / 32D;

        }
        else if (previous.getX() == -1)
        {
            if (next.getZ() == -1)
                return 7 / 32D;
            else if (next.getZ() == 1)
                return -7 / 32D;
        }
        else if (previous.getZ() == -1)
        {
            if (next.getX() == -1)
                return -7 / 32D;
            else if (next.getX() == 1)
                return -7 / 32D;
        }
        else if (previous.getZ() == 1)
        {
            if (next.getX() == -1)
                return 7 / 32D;
            else if (next.getX() == 1)
                return 7 / 32D;
        }

        return 0;
    }

    public static boolean isNextRailCorner(int index, Path path)
    {
        BlockPos previous = path.getPoints().get(index - 1);
        BlockPos next = path.getPoints().get(index + 1);

        return previous.getX() != next.getX() && previous.getZ() != next.getZ();
    }
}
