package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AssetEditManualController
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetEditManualController.class);
    public static final String APP_BAR_TITLE = "Edit Manual Asset";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;


    @FXML
    private View view;

    @FXML
    private ToggleSwitch allAssetClassesTS;

    @FXML
    private ComboBox<String> assetClassCombo;

    @FXML
    private Label manualNameLabel;

    @FXML
    private TextField unitsText;

    @FXML
    private Label valuePerUnitLabel;

    @FXML
    private TextField valuePerUnitText;

    @FXML
    private Label valuePerWholeLabelLabel;

    @FXML
    private Label valuePerWholeContentLabel;


    @FXML
    void initialize()
    {
        Validation.assertNonNull(view);
        Validation.assertNonNull(allAssetClassesTS);
        Validation.assertNonNull(assetClassCombo);
        Validation.assertNonNull(manualNameLabel);
        Validation.assertNonNull(unitsText);
        Validation.assertNonNull(valuePerUnitLabel);
        Validation.assertNonNull(valuePerUnitText);
        Validation.assertNonNull(valuePerWholeLabelLabel);
        Validation.assertNonNull(valuePerWholeContentLabel);

        //view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

        allAssetClassesTS.selectedProperty().addListener(e -> populateAssetClasses());
        assetClassCombo.setEditable(true); // allow entry of item not in list

        unitsText.textProperty().addListener((observable, oldValue, newValue) -> onValueChanged());
        valuePerUnitText.textProperty().addListener((observable, oldValue, newValue) -> onValueChanged());
    }


    protected void populateData()
    {
        AssetAllocation aa = StateManager.currentPortfolio.getTargetAA();
        Asset asset = StateManager.currentAsset;
        Validation.assertNull(asset.getTicker());
        Validation.assertNonNull(asset.getManualName());
        Validation.assertTrue((asset.getPricingType() == PricingType.MANUAL_PER_UNIT) || (asset.getPricingType() == PricingType.MANUAL_PER_WHOLE));

        manualNameLabel.setText(asset.getManualName());

        assetClassCombo.setValue(asset.getAssetClass());
        if(aa != null)
        {
            allAssetClassesTS.setSelected(false); // default to targetAA choices
            allAssetClassesTS.setDisable(false);
        }
        else
        {
            allAssetClassesTS.setSelected(true);
            allAssetClassesTS.setDisable(true);
        }
        populateAssetClasses();

        if(asset.getPricingType() == PricingType.MANUAL_PER_UNIT)
        {
            unitsText.setText(NumberFormatHelper.formatWith3Decimals(asset.getUnits()));
            valuePerUnitText.setText( NumberFormatHelper.formatWith4Decimals(asset.getManualValue()) );
            valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

            unitsText.setEditable(true);
            valuePerUnitText.setEditable(true);

            valuePerUnitLabel.setText("Value per Share ($)");
            valuePerWholeLabelLabel.setVisible(true);
            valuePerWholeContentLabel.setVisible(true);
        }
        else if(asset.getPricingType() == PricingType.MANUAL_PER_WHOLE)
        {
            unitsText.setText(NumberFormatHelper.formatWith3Decimals(BigDecimal.ONE)); // just override this
            valuePerUnitText.setText( NumberFormatHelper.formatWith2Decimals(asset.getManualValue()) );
            valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

            unitsText.setEditable(false);
            valuePerUnitText.setEditable(true);

            valuePerUnitLabel.setText("Value ($)");
            valuePerWholeLabelLabel.setVisible(false);
            valuePerWholeContentLabel.setVisible(false);
        }
        valuePerUnitText.requestFocus();
    }

    protected void populateAssetClasses()
    {
        String lastChoice = assetClassCombo.getValue();

        List<String> assetClasses = new ArrayList<>();
        if(allAssetClassesTS.isSelected())
        {
            assetClasses = AssetClass.list()
                    .stream()
                    .map(ac -> ac.getCode())
                    .collect(Collectors.toList());
        }
        else
        {
            AssetAllocation aa = StateManager.currentPortfolio.getTargetAA();
            if(aa != null)
            {
                assetClasses = aa.getRoot().allLeaves()
                    .stream()
                    .map(n -> n.getName())
                    .collect(Collectors.toList());
            }
            assetClasses.add(AssetClass.UNDEFINED);
        }

        assetClassCombo.getItems().setAll(assetClasses);
        assetClassCombo.setValue(lastChoice);
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
            asset.setAssetClass(assetClassCombo.getValue());
            asset.setUnits(NumberFormatHelper.parseNumber3(unitsText.getText()));

            if(asset.getPricingType() == PricingType.MANUAL_PER_UNIT)
            {
                asset.setManualValue(NumberFormatHelper.parseNumber4(valuePerUnitText.getText()));
                asset.setManualValueTmstp(new Date());
            }
            else if(asset.getPricingType() == PricingType.MANUAL_PER_WHOLE)
            {
                asset.setManualValue(NumberFormatHelper.parseNumber2(valuePerUnitText.getText()));
                asset.setManualValueTmstp(new Date());
            }
            // TODO: better validation and error msg UX
            StateManager.recalculateAccountValue();
            StateManager.recalculatePortfolioValue();
            StateManager.recalculateProfileValue();

            AssetClass.add(asset.getAssetClass());
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

    private void onValueChanged()
    {
        // do this regardless of PricingType... sometimes the result is just invisible
        BigDecimal units = NumberFormatHelper.parseNumber3(unitsText.getText());
        BigDecimal unitValue = NumberFormatHelper.parseNumber4(valuePerUnitText.getText());
        BigDecimal wholeValue = Asset.totalValue(unitValue, units);
        valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(wholeValue) );
    }

    private void visitHelp()
    {
        StateManager.currentUrl = HelpUrls.ASSET_EDIT_MANUAL_HELP_URL;
        view.getAppManager().switchView(App.WEB_VIEW);
    }
}
