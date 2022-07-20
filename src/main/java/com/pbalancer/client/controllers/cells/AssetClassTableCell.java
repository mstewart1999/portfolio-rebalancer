package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.aa.AssetClass;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

public class AssetClassTableCell<T> extends TableCell<T,String>
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
                setTooltip(null);
            }
        }
    }

    public static class Factory<T> implements Callback<TableColumn<T,String>, TableCell<T,String>>
    {
        @Override
        public TableCell<T,String> call(final TableColumn<T,String> col)
        {
            return new AssetClassTableCell<T>();
        }
    }
}