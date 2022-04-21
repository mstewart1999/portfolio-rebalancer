package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class AssetAddController
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetAddController.class);
    public static final String APP_BAR_TITLE = "Add Asset";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;


    @FXML
    private View view;

    @FXML
    private Label autoNameLabel;

    @FXML
    private Pane manualNamePanel;

    @FXML
    private RadioButton manualNameRB;

    @FXML
    private TextField manualNameText;

    @FXML
    private RadioButton priceManualPerUnitRB;

    @FXML
    private RadioButton priceManualPerWholeRB;

    @FXML
    private Pane tickerPanel;

    @FXML
    private RadioButton tickerRB;

    @FXML
    private TextField tickerText;


    @FXML
    void initialize()
    {
        Validation.assertNonNull(view);
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(manualNameRB);
        Validation.assertNonNull(manualNamePanel);
        Validation.assertNonNull(manualNameText);
        Validation.assertNonNull(priceManualPerUnitRB);
        Validation.assertNonNull(priceManualPerWholeRB);
        Validation.assertNonNull(tickerRB);
        Validation.assertNonNull(tickerPanel);
        Validation.assertNonNull(tickerText);

        //view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

        ToggleGroup nameToggleGroup = new ToggleGroup();
        tickerRB.setToggleGroup(nameToggleGroup);
        manualNameRB.setToggleGroup(nameToggleGroup);
        nameToggleGroup.selectedToggleProperty().addListener(e -> onNameTypeChange());

        ToggleGroup priceToggleGroup = new ToggleGroup();
        priceManualPerUnitRB.setToggleGroup(priceToggleGroup);
        priceManualPerWholeRB.setToggleGroup(priceToggleGroup);
        // no need for listener here

        // TODO: tickerText auto complete
    }


    protected void populateData()
    {
        Asset asset = StateManager.currentAsset;

        // NOTE: we expect all of these to be blank for an "add", but do this anyway
        tickerText.setText(asset.getTicker());
        autoNameLabel.setText(asset.getAutoName());
        manualNameText.setText(asset.getManualName());

        if(asset.getPricingType() == PricingType.MANUAL_PER_UNIT)
        {
            priceManualPerUnitRB.setSelected(true);
        }
        else if(asset.getPricingType() == PricingType.MANUAL_PER_WHOLE)
        {
            priceManualPerWholeRB.setSelected(true);
        }
        else
        {
            // leave unselected
        }

        if(asset.getTicker() != null)
        {
            tickerRB.setSelected(true);
        }
        else if(asset.getManualName() != null)
        {
            manualNameRB.setSelected(true);
        }
        else
        {
            // default to ticker entry, it should be the majority
            tickerRB.setSelected(true);
        }
    }


    protected void updateAppBar()
    {
        final AppBar appBar = view.getAppManager().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.getActionItems().add(MaterialDesignIcon.HELP.button(e -> visitHelp()));
        appBar.setTitleText(APP_BAR_TITLE);
    }


    private void goBack()
    {
        if(save())
        {
            view.getAppManager().switchToPreviousView();
        }
    }

    private boolean save()
    {
        try
        {
            Asset asset = StateManager.currentAsset;

            if(tickerRB.isSelected())
            {
                asset.setTicker(tickerText.getText());
                asset.setAutoName(autoNameLabel.getText());
                asset.setManualName(null);
                asset.setPricingType(PricingType.AUTO_PER_UNIT);
                asset.setUnits(null);
                asset.setManualValue(null);
                asset.setManualValueTmstp(null);
                asset.setLastAutoValue( new BigDecimal("1.00") ); // TODO
                asset.setLastAutoValueTmstp(new Date()); // TODO: somehow get this from the api, probably close of prior business day
            }
            if(manualNameRB.isSelected())
            {
                asset.setTicker(null);
                asset.setAutoName(null);
                asset.setManualName(manualNameText.getText());
                if(!priceManualPerUnitRB.isSelected() && !priceManualPerWholeRB.isSelected())
                {
                    // TODO: better validation and error msg UX
                    view.getAppManager().showMessage("Please select a pricing type for manual assets.");
                    return false;
                }
                else if(priceManualPerUnitRB.isSelected())
                {
                    asset.setPricingType(PricingType.MANUAL_PER_UNIT);
                }
                else if(priceManualPerWholeRB.isSelected())
                {
                    asset.setPricingType(PricingType.MANUAL_PER_WHOLE);
                }
                asset.setUnits(null);
                asset.setManualValue(null);
                asset.setManualValueTmstp(null);
                asset.setLastAutoValue(null);
                asset.setLastAutoValueTmstp(null);
            }

            DataFactory.get().updatePortfolio(StateManager.currentPortfolio);
            return true;
        }
        catch (IOException e)
        {
            LOG.error("Error updating: " + StateManager.currentPortfolio.getId(), e);
            view.getAppManager().showMessage("Error updating: " + StateManager.currentPortfolio.getId());
            return false;
        }
    }

    private void onNameTypeChange()
    {
        if(tickerRB.isSelected())
        {
            tickerPanel.setVisible(true);
            manualNamePanel.setVisible(false);

            manualNameText.setText("");

            tickerText.requestFocus();
        }
        if(manualNameRB.isSelected())
        {
            tickerPanel.setVisible(false);
            manualNamePanel.setVisible(true);

            tickerText.setText("");
            autoNameLabel.setText("");

            manualNameText.requestFocus();
        }
    }

    private void visitHelp()
    {
        StateManager.currentUrl = HelpUrls.ASSET_ADD_HELP_URL;
        view.getAppManager().switchView(App.WEB_VIEW);
    }

}
