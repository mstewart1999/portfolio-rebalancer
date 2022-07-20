package com.pbalancer.client.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.controllers.cells.AssetClassListCell;
import com.pbalancer.client.controllers.cells.AssetTickerListCell;
import com.pbalancer.client.model.aa.AssetAllocation;
import com.pbalancer.client.model.aa.AssetClass;
import com.pbalancer.client.model.aa.AssetTicker;
import com.pbalancer.client.model.aa.AssetTickerCache;
import com.pbalancer.client.model.aa.DefaultPreferredAsset;
import com.pbalancer.client.model.aa.DefaultPreferredAssetCache;
import com.pbalancer.client.model.aa.PreferredAsset;
import com.pbalancer.client.util.Validation;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

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
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private GridPane primaryAssetGrid;

    @FXML
    private Label tickerTitleLabel;

    @FXML
    private Label nameTitleLabel;

    @FXML
    private RadioButton otherRB;

    @FXML
    private TextField otherTickerText;

    @FXML
    private Label otherNameLabel;

    private List<RadioButton> assetRBs;
    private List<DefaultPreferredAsset> assetChoices;


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
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(saveBtn);
        Validation.assertNonNull(primaryAssetGrid);
        Validation.assertNonNull(tickerTitleLabel);
        Validation.assertNonNull(nameTitleLabel);
        Validation.assertNonNull(otherRB);
        Validation.assertNonNull(otherTickerText);
        Validation.assertNonNull(otherNameLabel);

        filterAssetClassesTS.selectedProperty().addListener(e -> populateAssetClasses());
        assetClassCombo.setEditable(true); // allow entry of item not in list
        assetClassCombo.setButtonCell(new AssetClassListCell(AssetClass.all()));
        assetClassCombo.setCellFactory(new AssetClassListCell.Factory(AssetClass.all()));
        assetClassCombo.setOnAction(e -> onAssetClassChanged());

        // otherTickerText auto complete
        final Map<String,AssetTicker> tickerSuggestions = AssetTickerCache.getInstance().all();
        AutoCompletionBinding<String> bindAutoCompletion = TextFields.bindAutoCompletion(otherTickerText, new TickerSuggestionProvider(tickerSuggestions));
        AutoCompletePopup<String> tickerCompletionPopup = bindAutoCompletion.getAutoCompletionPopup();
        tickerCompletionPopup.setSkin(new AutoCompletePopupSkin<String>(tickerCompletionPopup, new AssetTickerListCell.Factory(tickerSuggestions)));
        tickerCompletionPopup.setPrefWidth(500);
        tickerCompletionPopup.setMaxWidth(500);
        tickerCompletionPopup.setWidth(500);

        //tickerCompletionPopup.setMaxWidth(tickerText.getMaxWidth());
        otherTickerText.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onTickerChanged();
            }
        });
        otherTickerText.textProperty().addListener(e -> onTickerChanged());


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

        populateAssetClasses();
        onAssetClassChanged();

        // populate radio button selection and/or text box
        setSelectedDPA(mapping.getPrimaryAssetTicker(), mapping.getPrimaryAssetName());
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

        DefaultPreferredAsset choice = getSelectedDPA();
        if(choice != null)
        {
            mapping.setPrimaryAssetTicker(choice.getTicker());
            mapping.setPrimaryAssetName(choice.getName());
            mapping.markDirty();
        }
        else
        {
            if(otherRB.isSelected())
            {
                String ticker = otherTickerText.getText();
                AssetTicker at = AssetTickerCache.getInstance().lookup(ticker);
                if(at == null)
                {
                    getApp().showMessage("A valid ticker is required");
                    return false;
                }
                mapping.setPrimaryAssetTicker(at.getSymbol());
                mapping.setPrimaryAssetName(at.getName());
                mapping.markDirty();
            }
            else
            {
                getApp().showMessage("Selection for primary asset is required.");
                return false;
            }
        }

        AssetClass.add(mapping.getAssetClass());
        return true;
    }

    private void onAssetClassChanged()
    {
        List<Node> trash = new ArrayList<>();
        for(Node child : primaryAssetGrid.getChildren())
        {
            // leave header labels
            if((child != tickerTitleLabel) && (child != nameTitleLabel))
            {
                trash.add(child);
            }
        }
        primaryAssetGrid.getChildren().removeAll(trash);
        primaryAssetGrid.getRowConstraints().clear();
        primaryAssetGrid.getRowConstraints().add(new RowConstraints(0, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE));

        String ac = assetClassCombo.getValue();
        assetChoices = DefaultPreferredAssetCache.getInstance().lookupChoices(ac);
        assetRBs = new ArrayList<>();
        ToggleGroup primaryAssetToggleGroup = new ToggleGroup();

        int row = 1;
        for(DefaultPreferredAsset choice : assetChoices)
        {
            RadioButton rb = new RadioButton();
            Label tickerLabel = new Label(choice.getTicker());
            Label nameLabel = new Label(choice.getName());
            rb.setPadding(new Insets(8));
            tickerLabel.setPadding(new Insets(8));
            nameLabel.setPadding(new Insets(8));
            primaryAssetGrid.add(rb, 0, row);
            primaryAssetGrid.add(tickerLabel, 1, row);
            primaryAssetGrid.add(nameLabel, 2, row);
            primaryAssetGrid.getRowConstraints().add(new RowConstraints(0, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE));
            assetRBs.add(rb);
            rb.setToggleGroup(primaryAssetToggleGroup);

            row++;
        }
        otherRB.selectedProperty().set(false);
        otherTickerText.setText("");
        otherNameLabel.setText("");
        primaryAssetGrid.add(otherRB, 0, row);
        primaryAssetGrid.add(otherTickerText, 1, row);
        primaryAssetGrid.add(otherNameLabel, 2, row);
        otherRB.setPadding(new Insets(8));
        otherTickerText.setPadding(new Insets(8));
        // no assetRBs.add(otherRB);
        otherRB.setToggleGroup(primaryAssetToggleGroup);

        primaryAssetToggleGroup.selectedToggleProperty().addListener(e -> onPrimaryChange());
        primaryAssetToggleGroup.selectToggle(null);
    }

    private void onPrimaryChange()
    {
        if(otherRB.isSelected())
        {
            otherTickerText.setDisable(false);
        }
        else
        {
            otherTickerText.setDisable(true);
        }
    }


    private void onTickerChanged()
    {
        String ticker = otherTickerText.getText();
        AssetTicker t = AssetTickerCache.getInstance().lookup(ticker);
        if(t == null)
        {
            otherNameLabel.setText("");
        }
        else
        {
            otherNameLabel.setText(t.getName());
        }
    }

    private DefaultPreferredAsset getSelectedDPA()
    {
        for(int i=0; i<assetRBs.size(); i++)
        {
            if(assetRBs.get(i).isSelected())
            {
                return assetChoices.get(i);
            }
        }
        return null;
    }

    private void setSelectedDPA(final String ticker, final String name)
    {
        for(int i=0; i<assetRBs.size(); i++)
        {
            if(assetChoices.get(i).getTicker().equals(ticker))
            {
                assetRBs.get(i).selectedProperty().set(true);
                return;
            }
        }
        otherRB.selectedProperty().set(true);
        otherTickerText.setText(ticker);
        otherNameLabel.setText(name);
    }
}
