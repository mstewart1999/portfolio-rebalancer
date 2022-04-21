package com.msfinance.pbalancer.controllers.cells;

import com.msfinance.pbalancer.model.AccountType;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class AccountTypeListCell extends ListCell<AccountType>
{
    @Override
    protected void updateItem(final AccountType p, final boolean empty)
    {
        super.updateItem(p, empty);
        if(p != null)
        {
            setText(p.getText());
        }
        else
        {
            setText("");
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