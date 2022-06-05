package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.aa.AssetClass;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class AssetClassTreeTableCell<T> extends TreeTableCell<T,String>
{

    @Override
    protected void updateItem(final String val, final boolean empty)
    {
        super.updateItem(val, empty);
        if(empty || (val == null))
        {
            setText("");
            setTooltip(null);
        }
        else
        {
            setText(val);
            AssetClass ac = AssetClass.lookup(val);
            if(ac != null)
            {
                setTooltip(new Tooltip(ac.getShortDescription()));
            }
            else
            {
                // category nodes should not have a tooltip
                setTooltip(null);
            }
        }
    }

    public static class Factory<T> implements Callback<TreeTableColumn<T,String>, TreeTableCell<T,String>>
    {
        @Override
        public TreeTableCell<T,String> call(final TreeTableColumn<T,String> col)
        {
            return new AssetClassTreeTableCell<T>();
        }
    }
}