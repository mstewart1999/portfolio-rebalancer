package com.msfinance.pbalancer.views;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PrimaryView extends View
{

    public PrimaryView()
    {

        getStylesheets().add(PrimaryView.class.getResource("primary.css").toExternalForm());

        Label label = new Label("Hello JavaFX World!");

        Button button = new Button("Change the World!");
        button.setGraphic(new Icon(MaterialDesignIcon.LANGUAGE));
        button.setOnAction(helloAction(label));

        VBox controls = new VBox(15.0, label, button);
        controls.setAlignment(Pos.CENTER);

        setCenter(controls);
    }

    private EventHandler<ActionEvent> helloAction(final Label label)
    {
        return e -> label.setText("Hello JavaFX Universe!");
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        super.updateAppBar(appBar);
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getAppManager().getDrawer().open()));
        appBar.setTitleText("About");
        appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> System.out.println("Search")));
    }

}
