package com.msfinance.pbalancer.controllers;

import java.util.Map;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.AssetTickerListCell;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.aa.AssetTicker;
import com.msfinance.pbalancer.model.aa.AssetTickerCache;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class AssetAddController extends BaseController<Asset,Asset>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetAddController.class);
    public static final String APP_BAR_TITLE = "Add Asset";

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
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button nextBtn;


    public AssetAddController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(manualNameRB);
        Validation.assertNonNull(manualNamePanel);
        Validation.assertNonNull(manualNameText);
        Validation.assertNonNull(priceManualPerUnitRB);
        Validation.assertNonNull(priceManualPerWholeRB);
        Validation.assertNonNull(tickerRB);
        Validation.assertNonNull(tickerPanel);
        Validation.assertNonNull(tickerText);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(nextBtn);

        ToggleGroup nameToggleGroup = new ToggleGroup();
        tickerRB.setToggleGroup(nameToggleGroup);
        manualNameRB.setToggleGroup(nameToggleGroup);
        nameToggleGroup.selectedToggleProperty().addListener(e -> onNameTypeChange());

        ToggleGroup priceToggleGroup = new ToggleGroup();
        priceManualPerUnitRB.setToggleGroup(priceToggleGroup);
        priceManualPerWholeRB.setToggleGroup(priceToggleGroup);
        // no need for listener here

        // tickerText auto complete
        final Map<String,AssetTicker> tickerSuggestions = AssetTickerCache.getInstance().all();
        AutoCompletionBinding<String> bindAutoCompletion = TextFields.bindAutoCompletion(tickerText, new TickerSuggestionProvider(tickerSuggestions));
        AutoCompletePopup<String> tickerCompletionPopup = bindAutoCompletion.getAutoCompletionPopup();
        tickerCompletionPopup.setSkin(new AutoCompletePopupSkin<String>(tickerCompletionPopup, new AssetTickerListCell.Factory(tickerSuggestions)));
        tickerCompletionPopup.setPrefWidth(500);
        tickerCompletionPopup.setMaxWidth(500);
        tickerCompletionPopup.setWidth(500);

        //tickerCompletionPopup.setMaxWidth(tickerText.getMaxWidth());
        tickerText.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onTickerChanged();
            }
        });
        tickerText.textProperty().addListener(e -> onTickerChanged());

        ButtonBar.setButtonData(cancelBtn, ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonData(nextBtn, ButtonData.NEXT_FORWARD);
        cancelBtn.setGraphic(MaterialDesignIcon.CANCEL.graphic());
        nextBtn.setGraphic(MaterialDesignIcon.FORWARD.graphic());

        cancelBtn.setOnAction(e -> onCancel());
        nextBtn.setOnAction(e -> onNext());
    }


    @Override
    protected void populateData(final Asset asset)
    {
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

        // TODO: enable/disable based on qty of data entry
        //nextBtn.setDisable(true);
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

    private void onNext()
    {
        if(save())
        {
            if(tickerRB.isSelected())
            {
                getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_KNOWN_VIEW, getIn(),
                        a -> returnSuccess(a),
                        () -> returnFailure());
            }
            if(manualNameRB.isSelected())
            {
                getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_MANUAL_VIEW, getIn(),
                        a ->  returnSuccess(a),
                        () -> returnFailure());
            }
        }
    }

    private boolean save()
    {
        Asset asset = getIn();

        if(tickerRB.isSelected())
        {
            asset.setTicker(tickerText.getText().trim().toUpperCase());
            asset.setAutoName(autoNameLabel.getText().trim());
            asset.setManualName(null);
            asset.setPricingType(PricingType.AUTO_PER_UNIT);
            asset.setUnits(null);
            asset.setManualValue(null);
            asset.setManualValueTmstp(null);
            asset.markDirty();

            if(Validation.isBlank(asset.getTicker()))
            {
                getApp().showMessage("Ticker required");
                return false;
            }
            if(Validation.isBlank(asset.getAutoName()))
            {
                AssetTicker found = AssetTickerCache.getInstance().lookup(asset.getTicker());
                if(found == null)
                {
                    getApp().showMessage("Ticker selection required");
                    return false;
                }
                // quirk of autosuggest widget - you can enter exactly the right value but make no selection
                asset.setAutoName(found.getName());
                asset.markDirty();
            }

            if(!StateManager.refreshPrice(asset))
            {
                getApp().showMessage("Error getting asset price per unit");
                return false;
            }

            return true;
        }
        if(manualNameRB.isSelected())
        {
            asset.setTicker(null);
            asset.setAutoName(null);
            asset.setManualName(manualNameText.getText().trim());
            asset.markDirty();

            if(Validation.isBlank(asset.getManualName()))
            {
                getApp().showMessage("Name required");
                return false;
            }
            if(!priceManualPerUnitRB.isSelected() && !priceManualPerWholeRB.isSelected())
            {
                // TODO: better validation and error msg UX
                getApp().showMessage("Please select a pricing type for manual assets.");
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
            return true;
        }

        return false;
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

    private void onTickerChanged()
    {
        String ticker = tickerText.getText();
        AssetTicker t = AssetTickerCache.getInstance().lookup(ticker);
        if(t == null)
        {
            autoNameLabel.setText("");
        }
        else
        {
            autoNameLabel.setText(t.getName());
        }
    }

    private void visitHelp()
    {
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.ASSET_ADD_HELP_URL);
    }

}
