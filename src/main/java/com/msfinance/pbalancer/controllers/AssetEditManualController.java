package com.msfinance.pbalancer.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AssetEditManualController extends BaseController<Asset,Asset>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetEditManualController.class);
    public static final String APP_BAR_TITLE = "Edit Manual Asset";

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
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;


    public AssetEditManualController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(allAssetClassesTS);
        Validation.assertNonNull(assetClassCombo);
        Validation.assertNonNull(manualNameLabel);
        Validation.assertNonNull(unitsText);
        Validation.assertNonNull(valuePerUnitLabel);
        Validation.assertNonNull(valuePerUnitText);
        Validation.assertNonNull(valuePerWholeLabelLabel);
        Validation.assertNonNull(valuePerWholeContentLabel);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(saveBtn);

        allAssetClassesTS.selectedProperty().addListener(e -> populateAssetClasses());
        assetClassCombo.setEditable(true); // allow entry of item not in list

        unitsText.textProperty().addListener((observable, oldValue, newValue) -> onValueChanged());
        valuePerUnitText.textProperty().addListener((observable, oldValue, newValue) -> onValueChanged());

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
        Validation.assertNull(asset.getTicker());
        Validation.assertNonNull(asset.getManualName());
        Validation.assertTrue(
                (asset.getPricingType() == PricingType.MANUAL_PER_UNIT)
                || (asset.getPricingType() == PricingType.MANUAL_PER_WHOLE)
                || (asset.getPricingType() == PricingType.FIXED_PER_UNIT));

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
            allAssetClassesTS.setDisable(false);
            assetClassCombo.setDisable(false);

            unitsText.setText(NumberFormatHelper.formatWith2Decimals(asset.getUnits()));
            valuePerUnitText.setText( NumberFormatHelper.formatWith4Decimals(asset.getManualValue()) );
            valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

            unitsText.setEditable(true);
            valuePerUnitText.setEditable(true);

            valuePerUnitLabel.setText("Value per Share ($)");
            valuePerWholeLabelLabel.setVisible(true);
            valuePerWholeContentLabel.setVisible(true);

            Platform.runLater(() -> unitsText.requestFocus());
        }
        else if(asset.getPricingType() == PricingType.MANUAL_PER_WHOLE)
        {
            allAssetClassesTS.setDisable(false);
            assetClassCombo.setDisable(false);

            unitsText.setText(NumberFormatHelper.formatWith3Decimals(asset.getUnits())); // should be "1"
            valuePerUnitText.setText( NumberFormatHelper.formatWith2Decimals(asset.getManualValue()) );
            valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

            unitsText.setEditable(false);
            valuePerUnitText.setEditable(true);

            valuePerUnitLabel.setText("Value ($)");
            valuePerWholeLabelLabel.setVisible(false);
            valuePerWholeContentLabel.setVisible(false);

            Platform.runLater(() -> valuePerUnitText.requestFocus());
        }
        else if(asset.getPricingType() == PricingType.FIXED_PER_UNIT)
        {
            allAssetClassesTS.setDisable(true);
            assetClassCombo.setDisable(true);

            unitsText.setText(NumberFormatHelper.formatWith3Decimals(asset.getUnits()));
            valuePerUnitText.setText( NumberFormatHelper.formatWith2Decimals(asset.getManualValue()) );
            valuePerWholeContentLabel.setText( NumberFormatHelper.formatWith2Decimals(asset.getBestTotalValue()) );

            unitsText.setEditable(true);
            valuePerUnitText.setEditable(false);

            valuePerUnitLabel.setText("Value per Share ($)");
            valuePerWholeLabelLabel.setVisible(true);
            valuePerWholeContentLabel.setVisible(true);

            Platform.runLater(() -> unitsText.requestFocus());
        }
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
        else if(asset.getPricingType() == PricingType.FIXED_PER_UNIT)
        {
            // was not editable
            asset.setManualValue(NumberFormatHelper.parseNumber2(valuePerUnitText.getText()));
            asset.setManualValueTmstp(new Date());
        }
        asset.markDirty();

        // TODO: better validation and error msg UX

        AssetClass.add(asset.getAssetClass());
        return true;
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
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.ASSET_EDIT_MANUAL_HELP_URL);
    }
}
