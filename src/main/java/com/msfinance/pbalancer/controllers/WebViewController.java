package com.msfinance.pbalancer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInDownTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class WebViewController extends BaseController<String,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(WebViewController.class);
    public static final String APP_BAR_TITLE = "Help";

    @FXML
    private WebView web;



    public WebViewController()
    {
        super(BounceInDownTransition::new);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(web);

        String ua = web.getEngine().userAgentProperty().get();
        // ex: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/612.1 (KHTML, like Gecko) JavaFX/17 Safari/612.1"
        ua = ua.replaceFirst(" JavaFX/\\d* ", " ");
        //System.out.println(ua);
        web.getEngine().userAgentProperty().set(ua);
    }

    @Override
    protected void populateData(final String in)
    {
        web.getEngine().load(in);
        //web.getEngine().load("https://duckduckgo.com/?t=ffab&q=what+is+my+user+agent&ia=answer");
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        //xyzText.requestFocus();
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        returnSuccess(null);
    }

}
