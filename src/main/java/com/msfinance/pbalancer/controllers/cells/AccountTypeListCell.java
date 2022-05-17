package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.AccountType;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AccountTypeListCell extends ListCell<AccountType>
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

    public static class Factory implements Callback<ListView<AccountType>, ListCell<AccountType>>
    {
        @Override
        public ListCell<AccountType> call(final ListView<AccountType> l)
        {
            return new AccountTypeListCell();
        }
    }
}