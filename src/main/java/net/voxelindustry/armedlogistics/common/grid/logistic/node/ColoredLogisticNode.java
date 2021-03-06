package net.voxelindustry.armedlogistics.common.grid.logistic.node;

import net.voxelindustry.armedlogistics.common.grid.logistic.ColoredShipment;

import java.util.Collection;

public interface ColoredLogisticNode<T> extends LogisticNode<T>
{
    void addColoredShipment(ColoredShipment<T> shipment);

    boolean removeColoredShipment(ColoredShipment<T> shipment);

    Collection<ColoredShipment<T>> getColoredShipments();

    void deliverColoredShipment(ColoredShipment<T> shipment);
}
