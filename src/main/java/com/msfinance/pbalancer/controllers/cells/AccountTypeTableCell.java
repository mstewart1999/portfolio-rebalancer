package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.AccountType;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class AccountTypeTableCell<T> extends TableCell<T,AccountType>
{

    @Override
    protected void updateItem(final AccountType value, final boolean empty)
    {
        if (value == getItem() && empty == isEmpty()) return;

        super.updateItem(value, empty);
        if(value == null)
        {
            setText("");
        }
        else
        {
            setText(value.getText());
        }
    }

    public static class Factory<T> implements Callback<TableColumn<T,AccountType>, TableCell<T,AccountType>>
    {
        @Override
        public TableCell<T,AccountType> call(final TableColumn<T,AccountType> col)
        {
            return new AccountTypeTableCell<T>();
        }
    }
}