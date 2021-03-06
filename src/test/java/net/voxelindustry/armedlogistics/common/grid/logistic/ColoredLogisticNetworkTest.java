package net.voxelindustry.armedlogistics.common.grid.logistic;

import net.minecraft.item.ItemStack;
import net.voxelindustry.armedlogistics.common.grid.logistic.node.BaseItemRequester;
import net.voxelindustry.armedlogistics.common.grid.logistic.node.ColoredItemProvider;
import net.voxelindustry.armedlogistics.common.test.ItemStackMatcher;
import net.voxelindustry.armedlogistics.common.test.TestColoredItemProvider;
import net.voxelindustry.armedlogistics.common.test.TestItemRequester;
import net.voxelindustry.armedlogistics.common.test.WGTestExt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.minecraft.item.DyeColor.RED;
import static net.minecraft.item.ItemStack.EMPTY;
import static net.minecraft.item.Items.APPLE;
import static net.minecraft.item.Items.POTATO;
import static net.voxelindustry.armedlogistics.common.grid.logistic.OrderState.SHIPPING;
import static net.voxelindustry.armedlogistics.common.grid.logistic.OrderState.SUBMITTED;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(WGTestExt.class)
class ColoredLogisticNetworkTest
{
    @Test
    void simpleShipping()
    {
        LogisticNetwork<ItemStack> grid = new LogisticNetwork<>(null, ItemStack.class, ItemStackMethods.getInstance());

        ItemStack apple2 = new ItemStack(APPLE, 2);

        ColoredItemProvider provider1 = TestColoredItemProvider.build()
                .stacks(apple2)
                .color(RED, new ItemStack(APPLE))
                .create();
        BaseItemRequester requester = TestItemRequester.build()
                .stacks(EMPTY)
                .create();

        grid.addProvider(provider1);

        ColoredOrder<ItemStack> order = grid.makeOrder(requester, RED, 1);

        assertThat(order.getState()).isEqualTo(SUBMITTED);

        grid.tick();

        assertThat(order.getState()).isEqualTo(SHIPPING);
        assertThat(order.getShippedParts().get(0)).isInstanceOf(ColoredShipment.class);
        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(0).getRawContent(),
                new ItemStack(APPLE, 1));
    }

    @Test
    void mixedShipping()
    {
        LogisticNetwork<ItemStack> grid = new LogisticNetwork<>(null, ItemStack.class, ItemStackMethods.getInstance());

        ItemStack apple2 = new ItemStack(APPLE, 2);
        ItemStack potato3 = new ItemStack(POTATO, 3);

        ColoredItemProvider provider1 = TestColoredItemProvider.build()
                .stacks(apple2, potato3)
                .color(RED, new ItemStack(APPLE), new ItemStack(POTATO))
                .create();
        BaseItemRequester requester = TestItemRequester.build()
                .stacks(EMPTY)
                .create();

        grid.addProvider(provider1);

        ColoredOrder<ItemStack> order = grid.makeOrder(requester, RED, 4);

        assertThat(order.getState()).isEqualTo(SUBMITTED);

        grid.tick();

        assertThat(order.getState()).isEqualTo(SHIPPING);

        assertThat(order.getShippedParts()).hasSize(2);
        assertThat(order.getShippedParts().get(0)).isInstanceOf(ColoredShipment.class);
        assertThat(order.getShippedParts().get(1)).isInstanceOf(ColoredShipment.class);

        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(0).getRawContent(),
                new ItemStack(APPLE, 2));
        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(1).getRawContent(),
                new ItemStack(POTATO, 2));
    }

    @Test
    void mixedShippingMultiProvider()
    {
        LogisticNetwork<ItemStack> grid = new LogisticNetwork<>(null, ItemStack.class, ItemStackMethods.getInstance());

        ItemStack apple2 = new ItemStack(APPLE, 2);
        ItemStack potato3 = new ItemStack(POTATO, 3);

        ColoredItemProvider provider1 = TestColoredItemProvider.build()
                .stacks(apple2, potato3)
                .color(RED, new ItemStack(APPLE), new ItemStack(POTATO))
                .create();
        ColoredItemProvider provider2 = TestColoredItemProvider.build()
                .stacks(apple2.copy())
                .color(RED, new ItemStack(APPLE))
                .create();

        BaseItemRequester requester = TestItemRequester.build()
                .stacks(EMPTY)
                .create();

        grid.addProvider(provider1);
        grid.addProvider(provider2);

        ColoredOrder<ItemStack> order = grid.makeOrder(requester, RED, 6);

        assertThat(order.getState()).isEqualTo(SUBMITTED);

        grid.tick();

        assertThat(order.getState()).isEqualTo(SHIPPING);

        assertThat(order.getShippedParts()).hasSize(3);
        assertThat(order.getShippedParts().get(0)).isInstanceOf(ColoredShipment.class);
        assertThat(order.getShippedParts().get(1)).isInstanceOf(ColoredShipment.class);
        assertThat(order.getShippedParts().get(2)).isInstanceOf(ColoredShipment.class);

        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(0).getRawContent(),
                new ItemStack(APPLE, 2));
        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(1).getRawContent(),
                new ItemStack(POTATO, 3));
        ItemStackMatcher.assertEqualsStrict(order.getShippedParts().get(2).getRawContent(),
                new ItemStack(APPLE, 1));
    }
}
