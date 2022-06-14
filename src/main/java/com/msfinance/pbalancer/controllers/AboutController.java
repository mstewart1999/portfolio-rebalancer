package com.msfinance.pbalancer.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutController extends BaseController<Void,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

    public static final String APP_BAR_TITLE = "About";

    @FXML
    private Label versionLabel;



    public AboutController()
    {
        super(BounceInRightTransition::new);
    }

    @FXML
    void initialize() throws IOException
    {
        Validation.assertNonNull(versionLabel);
    }

//    @Override
//    public void initializeApp(final App app, final View root)
//    {
//        super.initializeApp(app, root);
//
////        // UGGG: major flaw, this never gets called from drawer, or on first init
////        getRoot().setOnShowing(e -> {
////            call(StateManager.currentProfile, null, null);
////        });
//    }
//
//    @Override
//    protected void doSizing()
//    {
//        super.doSizing();
//        // critical to get proper scrollbar behavior
//        getRoot().setMinSize(100, 100);
//        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
//        ((AnchorPane)t.getParent()).setPrefSize(20, 20);
//        t.setPrefSize(20, 20);
//    }

    @Override
    protected void populateData(final Void voidObj)
    {
        versionLabel.setText("TBD");
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().showDrawer()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

}
