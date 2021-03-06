package net.voxelindustry.armedlogistics.common.grid.logistic;

import net.minecraft.item.ItemStack;
import net.voxelindustry.armedlogistics.common.grid.RailGrid;
import net.voxelindustry.armedlogistics.common.grid.logistic.node.BaseItemProvider;
import net.voxelindustry.armedlogistics.common.grid.logistic.node.BaseItemRequester;
import net.voxelindustry.armedlogistics.common.test.GridTestBuilder;
import net.voxelindustry.armedlogistics.common.test.TestItemProvider;
import net.voxelindustry.armedlogistics.common.test.TestItemRequester;
import net.voxelindustry.armedlogistics.common.test.WGTestExt;
import net.voxelindustry.steamlayer.grid.GridManager;
import net.voxelindustry.steamlayer.grid.ITileCable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.minecraft.item.ItemStack.EMPTY;
import static net.minecraft.item.Items.APPLE;
import static net.minecraft.util.math.BlockPos.ZERO;
import static net.voxelindustry.armedlogistics.common.grid.logistic.OrderState.SHIPPING;
import static net.voxelindustry.armedlogistics.common.grid.logistic.OrderState.SUBMITTED;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(WGTestExt.class)
class LogisticNetworkRailGridTest
{
    private GridManager instance;

    @BeforeAll
    void setup()
    {
        instance = GridManager.createGetInstance("armedlogistics:test");
    }

    @Test
    void closestProvider()
    {
        // Rails setup
        RailGrid railGrid = new RailGrid(instance.getNextID());

        GridTestBuilder railGridBuilder = GridTestBuilder.build(railGrid).origin(ZERO);
        ITileCable requesterRail = railGridBuilder.northGet();

        ITileCable farthestRail = railGridBuilder.east().east().east().east().eastGet();

        railGridBuilder.current(requesterRail).west();

        ITileCable closestRail = railGridBuilder.westGet();

        // Logistic setup
        LogisticNetwork<ItemStack> grid = new LogisticNetwork<>(railGrid, ItemStack.class,
                ItemStackMethods.getInstance());

        ItemStack apple2 = new ItemStack(APPLE, 2);

        BaseItemProvider farthestProvider =
                TestItemProvider.build().pos(farthestRail.getBlockPos()).stacks(apple2).create();
        BaseItemProvider closestProvider =
                TestItemProvider.build().pos(closestRail.getBlockPos()).stacks(apple2).create();

        BaseItemRequester requester = TestItemRequester.build()
                .stacks(EMPTY)
                .pos(requesterRail.getBlockPos())
                .create();

        grid.addProvider(farthestProvider);
        grid.addProvider(closestProvider);

        LogisticOrder<ItemStack> order = grid.makeOrder(requester, new ItemStack(APPLE, 2));

        assertThat(order.getState()).isEqualTo(SUBMITTED);

        grid.tick();

        assertThat(order.getState()).isEqualTo(SHIPPING);

        assertThat(order.getShippedParts().get(0).getFrom()).isEqualTo(closestRail.getBlockPos());
    }
}
