package com.msfinance.rebalancer;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.msfinance.rebalancer.views.PrimaryView;
import com.msfinance.rebalancer.views.SecondaryView;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends MobileApplication
{
    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String SECONDARY_VIEW = "Secondary View";
    
    @Override
    public void init()
    {
        addViewFactory(PRIMARY_VIEW, PrimaryView::new);
        addViewFactory(SECONDARY_VIEW, SecondaryView::new);
        
        DrawerManager.buildDrawer(this);
    }

    @Override
    public void postInit(Scene scene)
    {
        Swatch.BLUE.assignTo(scene);

        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icon.png")));
    }

    public static void main(String args[])
    {
        launch(args);
    }
}
