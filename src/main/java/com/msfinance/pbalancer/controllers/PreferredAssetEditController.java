package com.msfinance.pbalancer.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.controllers.cells.AssetClassListCell;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.aa.PreferredAsset;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class PreferredAssetEditController extends BaseController<PreferredAsset,PreferredAsset>
{
    private static final Logger LOG = LoggerFactory.getLogger(PreferredAssetEditController.class);
    public static final String APP_BAR_TITLE_ADD = "Add Preferred Asset (Asset class mapping)";
    public static final String APP_BAR_TITLE_EDIT = "Edit Preferred Asset (Asset class mapping)";

    @FXML
    private HBox aaFilterBox;

    @FXML
    private ToggleSwitch filterAssetClassesTS;

    @FXML
    private ComboBox<String> assetClassCombo;

    @FXML
    private Label autoNameLabel;

    @FXML
    private Label tickerLabel;

    @FXML
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;


    public PreferredAssetEditController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(aaFilterBox);
        Validation.assertNonNull(filterAssetClassesTS);
        Validation.assertNonNull(assetClassCombo);
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(tickerLabel);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(saveBtn);

        filterAssetClassesTS.selectedProperty().addListener(e -> populateAssetClasses());
        assetClassCombo.setEditable(true); // allow entry of item not in list
        assetClassCombo.setButtonCell(new AssetClassListCell(AssetClass.all()));
        assetClassCombo.setCellFactory(new AssetClassListCell.Factory(AssetClass.all()));

        ButtonBar.setButtonData(cancelBtn, ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonData(saveBtn, ButtonData.FINISH);
        cancelBtn.setGraphic(MaterialDesignIcon.CANCEL.graphic());
        saveBtn.setGraphic(MaterialDesignIcon.SAVE.graphic());

        cancelBtn.setOnAction(e -> onCancel());
        saveBtn.setOnAction(e -> onSave());
    }

    @Override
    protected void doSizing()
    {
        super.doSizing();
        // critical to get proper scrollbar behavior
        getRoot().setMinSize(100, 100);
        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        ((ScrollPane)getRoot().getCenter()).setPrefSize(20, 20);
        ((Region)((ScrollPane)getRoot().getCenter()).getContent()).setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }


    @Override
    protected void populateData(final PreferredAsset mapping)
    {
        if(Validation.isBlank(mapping.getAssetClass()))
        {
            // allow changing asset class from blank to a value
            aaFilterBox.setVisible(true);
            filterAssetClassesTS.setDisable(false);
            assetClassCombo.setEditable(true);
            assetClassCombo.setDisable(false);
            aaFilterBox.getParent().getParent().requestLayout();

            AssetAllocation aa = mapping.getPortfolio().getTargetAA();
            if(aa != null)
            {
                filterAssetClassesTS.setSelected(true); // default to targetAA choices
                filterAssetClassesTS.setDisable(false);
            }
            else
            {
                filterAssetClassesTS.setSelected(false);
                filterAssetClassesTS.setDisable(true);
            }
        }
        else
        {
            // do not let user change asset class once defined (add vs edit)
            aaFilterBox.setVisible(false);
            filterAssetClassesTS.setDisable(true);
            assetClassCombo.setEditable(false);
            assetClassCombo.setDisable(true);
            aaFilterBox.getParent().getParent().requestLayout();
        }

        assetClassCombo.setValue(mapping.getAssetClass());

        tickerLabel.setText(mapping.getPrimaryAssetTicker());
        autoNameLabel.setText(mapping.getPrimaryAssetName());

        populateAssetClasses();
    }

    protected void populateAssetClasses()
    {
        String lastChoice = assetClassCombo.getValue();

        List<String> assetClasses = new ArrayList<>();
        if(!filterAssetClassesTS.isSelected())
        {
            assetClasses = AssetClass.list()
                    .stream()
                    .map(ac -> ac.getCode())
                    .collect(Collectors.toList());
        }
        else
        {
            AssetAllocation aa = getIn().getPortfolio().getTargetAA();
            if(aa != null)
            {
                assetClasses = aa.getRoot().allLeaves()
                    .stream()
                    .map(n -> n.getName())
                    .collect(Collectors.toList());
            }
            if(!assetClasses.contains(AssetClass.UNDEFINED))
            {
                assetClasses.add(AssetClass.UNDEFINED);
            }
            if(!assetClasses.contains(lastChoice))
            {
                assetClasses.add(0, lastChoice);
            }
        }

        assetClassCombo.getItems().setAll(assetClasses);
        assetClassCombo.setValue(lastChoice);
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        assetClassCombo.requestFocus();
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        //appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();

        if(getIn() == null)
        {
            // ugly case during screen show, eventually it should be called properly
            appBar.setTitleText("");
        }
        else
        {
            if(Validation.isBlank(getIn().getAssetClass()))
            {
                appBar.setTitleText(APP_BAR_TITLE_ADD);
            }
            else
            {
                appBar.setTitleText(APP_BAR_TITLE_EDIT);
            }
        }
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
        if(Validation.isBlank(assetClassCombo.getValue()))
        {
            getApp().showMessage("Asset class is required");
            return false;
        }

        PreferredAsset mapping = getIn();
        mapping.setAssetClass(assetClassCombo.getValue());
        mapping.setPrimaryAssetTicker(tickerLabel.getText());
        mapping.setPrimaryAssetName(autoNameLabel.getText());
        mapping.markDirty();

        // TODO: better validation and error msg UX

        AssetClass.add(mapping.getAssetClass());
        return true;
    }

}
