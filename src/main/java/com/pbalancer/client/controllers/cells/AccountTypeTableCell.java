package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.AccountType;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class AccountTypeTableCell<T> extends TableCell<T,AccountType>
{

    @Override
    protected void updateItem(final AccountType e, final boolean empty)
    {
        super.updateItem(e, empty);
        if(empty || (e == null))
        {
            setText("");
        }
        else
        {
            setText(e.getText());
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