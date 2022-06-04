package com.msfinance.pbalancer.controllers;

import java.math.BigDecimal;
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
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.aa.AssetTicker;
import com.msfinance.pbalancer.model.aa.AssetTickerCache;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class AssetAddController extends BaseController<Asset,Asset>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetAddController.class);
    public static final String APP_BAR_TITLE = "Add Asset";

    @FXML
    private RadioButton publicRB;

    @FXML
    private RadioButton privateRB;

    @FXML
    private RadioButton propertyRB;

    @FXML
    private RadioButton cashRB;


    @FXML
    private Pane publicPanel2;

    @FXML
    private Pane privatePanel2;

    @FXML
    private Pane propertyPanel2;

    @FXML
    private Pane cashPanel2;



    @FXML
    private TextField tickerText;

    @FXML
    private Label autoNameLabel;


    @FXML
    private TextField privateNameText;

    @FXML
    private CheckBox proxyCB;


    @FXML
    private TextField propertyNameText;


    @FXML
    private TextField cashNameText;


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
        Validation.assertNonNull(publicRB);
        Validation.assertNonNull(privateRB);
        Validation.assertNonNull(propertyRB);
        Validation.assertNonNull(cashRB);
        Validation.assertNonNull(publicPanel2);
        Validation.assertNonNull(privatePanel2);
        Validation.assertNonNull(propertyPanel2);
        Validation.assertNonNull(cashPanel2);
        Validation.assertNonNull(tickerText);
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(privateNameText);
        Validation.assertNonNull(proxyCB);
        Validation.assertNonNull(propertyNameText);
        Validation.assertNonNull(cashNameText);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(nextBtn);

        ToggleGroup nameToggleGroup = new ToggleGroup();
        publicRB.setToggleGroup(nameToggleGroup);
        privateRB.setToggleGroup(nameToggleGroup);
        propertyRB.setToggleGroup(nameToggleGroup);
        cashRB.setToggleGroup(nameToggleGroup);
        nameToggleGroup.selectedToggleProperty().addListener(e -> onTypeChange());

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
    protected void populateData(final Asset asset)
    {
        // just assume all of these to are blank for an "add"
        tickerText.setText("");
        autoNameLabel.setText("");
        privateNameText.setText("");
        proxyCB.setSelected(false);
        propertyNameText.setText("");
        cashNameText.setText(Asset.CASH);

        // default to ticker entry, it should be the majority
        publicRB.setSelected(true);
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        tickerText.requestFocus();
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
            if(publicRB.isSelected())
            {
                getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_KNOWN_VIEW, getIn(),
                        a -> returnSuccess(a),
                        () -> returnFailure());
            }
            if(privateRB.isSelected())
            {
                if(proxyCB.isSelected())
                {
                    getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_PROXY_VIEW, getIn(),
                            a ->  returnSuccess(a),
                            () -> returnFailure());
                }
                else
                {
                    getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_MANUAL_VIEW, getIn(),
                            a ->  returnSuccess(a),
                            () -> returnFailure());
                }
            }
            if(propertyRB.isSelected())
            {
                getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_MANUAL_VIEW, getIn(),
                        a ->  returnSuccess(a),
                        () -> returnFailure());
            }
            if(cashRB.isSelected())
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

        if(publicRB.isSelected())
        {
            asset.setTicker(tickerText.getText().trim().toUpperCase());
            asset.setAutoName(autoNameLabel.getText().trim());
            asset.setManualName(null);
            asset.setPricingType(PricingType.AUTO_PER_UNIT);
            asset.setUnits(null);
            asset.setManualValue(null);
            asset.setManualValueTmstp(null);
            asset.setAssetClass(AssetClass.UNDEFINED);
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
        if(privateRB.isSelected())
        {
            asset.setTicker(null);
            asset.setAutoName(null);
            asset.setManualName(privateNameText.getText().trim());
            asset.setPricingType(PricingType.MANUAL_PER_UNIT);
            asset.setUnits(null);
            asset.setManualValue(null);
            asset.setManualValueTmstp(null);
            asset.setLastAutoValue(null);
            asset.setLastAutoValueTmstp(null);
            asset.setAssetClass(AssetClass.UNDEFINED);
            asset.markDirty();

            if(proxyCB.isSelected())
            {
                //TODO;
            }

            if(Validation.isBlank(asset.getManualName()))
            {
                getApp().showMessage("Misc asset name required");
                return false;
            }

            return true;
        }
        if(propertyRB.isSelected())
        {
            asset.setTicker(null);
            asset.setAutoName(null);
            asset.setManualName(propertyNameText.getText().trim());
            asset.setPricingType(PricingType.MANUAL_PER_WHOLE);
            asset.setUnits(BigDecimal.ONE);
            asset.setManualValue(null);
            asset.setManualValueTmstp(null);
            asset.setLastAutoValue(null);
            asset.setLastAutoValueTmstp(null);
            asset.setAssetClass(AssetClass.UNDEFINED);
            asset.markDirty();

            if(Validation.isBlank(asset.getManualName()))
            {
                getApp().showMessage("Property name required");
                return false;
            }

            return true;
        }
        if(cashRB.isSelected())
        {
            asset.setTicker(null);
            asset.setAutoName(null);
            asset.setManualName(cashNameText.getText().trim());
            asset.setPricingType(PricingType.FIXED_PER_UNIT);
            asset.setUnits(null);
            asset.setManualValue(BigDecimal.ONE);
            asset.setManualValueTmstp(null);
            asset.setLastAutoValue(null);
            asset.setLastAutoValueTmstp(null);
            asset.setAssetClass(AssetClass.CASH);
            asset.markDirty();

            if(Validation.isBlank(asset.getManualName()))
            {
                getApp().showMessage("Cash name required");
                return false;
            }

            return true;
        }

        return false;
    }

    private void onTypeChange()
    {
        publicPanel2.managedProperty().bind(publicPanel2.visibleProperty());
        privatePanel2.managedProperty().bind(privatePanel2.visibleProperty());
        propertyPanel2.managedProperty().bind(propertyPanel2.visibleProperty());
        cashPanel2.managedProperty().bind(cashPanel2.visibleProperty());

        if(publicRB.isSelected())
        {
            publicPanel2.setVisible(true);
            privatePanel2.setVisible(false);
            propertyPanel2.setVisible(false);
            cashPanel2.setVisible(false);

            Platform.runLater(() -> tickerText.requestFocus());
        }
        if(privateRB.isSelected())
        {
            publicPanel2.setVisible(false);
            privatePanel2.setVisible(true);
            propertyPanel2.setVisible(false);
            cashPanel2.setVisible(false);

            Platform.runLater(() -> privateNameText.requestFocus());
        }
        if(propertyRB.isSelected())
        {
            publicPanel2.setVisible(false);
            privatePanel2.setVisible(false);
            propertyPanel2.setVisible(true);
            cashPanel2.setVisible(false);

            Platform.runLater(() -> propertyNameText.requestFocus());
        }
        if(cashRB.isSelected())
        {
            publicPanel2.setVisible(false);
            privatePanel2.setVisible(false);
            propertyPanel2.setVisible(false);
            cashPanel2.setVisible(true);

            Platform.runLater(() -> cashNameText.requestFocus());
        }

        publicPanel2.getParent().requestLayout();
        privatePanel2.getParent().requestLayout();
        propertyPanel2.getParent().requestLayout();
        cashPanel2.getParent().requestLayout();
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
