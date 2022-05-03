package com.msfinance.pbalancer.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AssetEditKnownController extends BaseController<Asset,Asset>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetEditKnownController.class);
    public static final String APP_BAR_TITLE = "Edit Known Asset";

    @FXML
    private ToggleSwitch allAssetClassesTS;

    @FXML
    private ComboBox<String> assetClassCombo;

    @FXML
    private Label autoNameLabel;

    @FXML
    private Label autoValuePerUnitLabel;

    @FXML
    private Label autoValuePerWholeLabel;

    @FXML
    private Label tickerLabel;

    @FXML
    private TextField unitsText;

    @FXML
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;


    public AssetEditKnownController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(allAssetClassesTS);
        Validation.assertNonNull(assetClassCombo);
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(autoValuePerUnitLabel);
        Validation.assertNonNull(autoValuePerWholeLabel);
        Validation.assertNonNull(tickerLabel);
        Validation.assertNonNull(unitsText);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(saveBtn);

        allAssetClassesTS.selectedProperty().addListener(e -> populateAssetClasses());
        assetClassCombo.setEditable(true); // allow entry of item not in list

        unitsText.textProperty().addListener((observable, oldValue, newValue) -> onUnitsChanged());

        ButtonBar.setButtonData(cancelBtn, ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonData(saveBtn, ButtonData.FINISH);
        cancelBtn.setGraphic(MaterialDesignIcon.CANCEL.graphic());
        saveBtn.setGraphic(MaterialDesignIcon.SAVE.graphic());

        cancelBtn.setOnAction(e -> onCancel());
        saveBtn.setOnAction(e -> onSave());
    }


    @Override
    protected void populateData(final Asset asset)
    {
        AssetAllocation aa = asset.getAccount().getPortfolio().getTargetAA();
        Validation.assertNonNull(asset.getTicker());
        Validation.assertNull(asset.getManualName());
        Validation.assertTrue(asset.getPricingType() == PricingType.AUTO_PER_UNIT);

        tickerLabel.setText(asset.getTicker());
        autoNameLabel.setText(asset.getAutoName());

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

        unitsText.setText(NumberFormatHelper.formatWith3Decimals(asset.getUnits()));

        autoValuePerUnitLabel.setText( NumberFormatHelper.formatWith3Decimals(asset.getLastAutoValue()) );
        autoValuePerWholeLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

        unitsText.requestFocus();
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
            AssetAllocation aa = getIn().getAccount().getPortfolio().getTargetAA();
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

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        //appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.getActionItems().add(MaterialDesignIcon.HELP.button(e -> visitHelp()));
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void onCancel()
    {
        returnFailure();
    }

    private void onSave()
    {
        if(save())
        {
            returnSuccess(getIn());
        }
    }

    private boolean save()
    {
        Asset asset = getIn();
        asset.setAssetClass(assetClassCombo.getValue());
        asset.setUnits(NumberFormatHelper.parseNumber3(unitsText.getText()));
        // TODO: better validation and error msg UX

        AssetClass.add(asset.getAssetClass());
        return true;
    }

    private void onUnitsChanged()
    {
        BigDecimal unitValue = NumberFormatHelper.parseNumber4(autoValuePerUnitLabel.getText());
        BigDecimal units = NumberFormatHelper.parseNumber3(unitsText.getText());
        BigDecimal wholeValue = Asset.totalValue(unitValue, units);
        autoValuePerWholeLabel.setText( NumberFormatHelper.formatWith2Decimals(wholeValue) );
    }

    private void visitHelp()
    {
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.ASSET_EDIT_KNOWN_HELP_URL);
    }
}
