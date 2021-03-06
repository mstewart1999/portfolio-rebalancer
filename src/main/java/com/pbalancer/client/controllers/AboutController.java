package com.pbalancer.client.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.App;
import com.pbalancer.client.Version;
import com.pbalancer.client.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class AboutController extends BaseController<Void,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

    public static final String APP_BAR_TITLE = "About";

    @FXML
    private Label nameLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private Label buildNbrLabel;

    @FXML
    private Label buildDateLabel;

    @FXML
    private TextArea detailsTextArea;




    public AboutController()
    {
        super(BounceInRightTransition::new);
    }

    @FXML
    void initialize() throws IOException
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(versionLabel);
        Validation.assertNonNull(buildNbrLabel);
        Validation.assertNonNull(buildDateLabel);
        Validation.assertNonNull(detailsTextArea);
    }

    @Override
    public void initializeApp(final App app, final View root)
    {
        super.initializeApp(app, root);

        // UGGG: major flaw, this never gets called from drawer, or on first init
        getRoot().setOnShowing(e -> {
            call(null, null, null);
        });
    }

    @Override
    protected void doSizing()
    {
        super.doSizing();
        // critical to get proper scrollbar behavior
        getRoot().setMinSize(100, 100);
        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        ((AnchorPane)detailsTextArea.getParent()).setPrefSize(20, 20);
        detailsTextArea.setPrefSize(20, 20);
    }

    @Override
    protected void populateData(final Void voidObj)
    {
        nameLabel.setText("pBalancer");

        Version v = new Version();
        versionLabel.setText(v.getVersion());
        buildNbrLabel.setText(v.getBuildNbr());
        buildDateLabel.setText(v.getBuildDate());

        //detailsTextArea.setText(""); // TODO
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().showDrawer()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

}
