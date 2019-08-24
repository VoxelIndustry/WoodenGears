package net.vi.woodengears.common.entity;

import lombok.Getter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public enum FacingLane
{
    NORTH(7 / 32D, 0, 0),
    SOUTH(-7 / 32D, 0, 0),
    EAST(0, 0, -7 / 32D),
    WEST(0, 0, 7 / 32D);

    @Getter
    private Vec3d vector;

    FacingLane(double x, double y, double z)
    {
        vector = new Vec3d(x, y, z);
    }

    public static FacingLane fromFacing(EnumFacing facing)
    {
        switch (facing)
        {
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
        }
        return null;
    }
}