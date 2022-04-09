package com.msfinance.pbalancer.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;

public class WebViewController
{
    private static final Logger LOG = LoggerFactory.getLogger(WebViewController.class);
    public static final String APP_BAR_TITLE = "Help";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private View view;

    @FXML
    private WebView web;



    @FXML
    void initialize()
    {
        Validation.assertNonNull(view);
        Validation.assertNonNull(web);

        // critical to get proper scrollbar behavior
        view.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        //view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });
    }

    protected void populateData()
    {
        web.getEngine().load(StateManager.currentUrl);
    }

    protected void updateAppBar()
    {
        final AppBar appBar = view.getAppManager().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        view.getAppManager().switchToPreviousView();
    }

}
