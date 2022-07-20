package com.pbalancer.client.controllers.cells;

import com.pbalancer.client.model.Profile;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ProfileListCell extends ListCell<Profile>
{
    @Override
    protected void updateItem(final Profile p, final boolean empty)
    {
        super.updateItem(p, empty);
        if(empty || (p == null))
        {
            setText("");
        }
        else
        {
            setText(p.getName());
        }
    }

    public static class Factory implements Callback<ListView<Profile>, ListCell<Profile>>
    {
        @Override
        public ListCell<Profile> call(final ListView<Profile> l)
        {
            return new ProfileListCell();
        }
    }
}