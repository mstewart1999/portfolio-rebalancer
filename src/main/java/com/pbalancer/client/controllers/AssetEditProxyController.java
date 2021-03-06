package com.pbalancer.client.controllers;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.App;
import com.pbalancer.client.StateManager;
import com.pbalancer.client.controllers.cells.AssetTickerListCell;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.AssetProxy;
import com.pbalancer.client.model.aa.AssetTicker;
import com.pbalancer.client.model.aa.AssetTickerCache;
import com.pbalancer.client.util.DateHelper;
import com.pbalancer.client.util.HelpUrls;
import com.pbalancer.client.util.NumberFormatHelper;
import com.pbalancer.client.util.Validation;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class AssetEditProxyController extends BaseController<Asset,Asset>
{
    private static final Logger LOG = LoggerFactory.getLogger(AssetEditProxyController.class);
    public static final String APP_BAR_TITLE = "Define Proxy Asset";


    @FXML
    private TextField tickerText;

    @FXML
    private Label autoNameLabel;


    @FXML
    private TextField privatePriceText;

    @FXML
    private TextField proxyPriceText;

    @FXML
    private TextField pricingDateText;


    @FXML
    private ButtonBar buttonBar;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button nextBtn;


    public AssetEditProxyController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(tickerText);
        Validation.assertNonNull(autoNameLabel);
        Validation.assertNonNull(privatePriceText);
        Validation.assertNonNull(proxyPriceText);
        Validation.assertNonNull(pricingDateText);
        Validation.assertNonNull(buttonBar);
        Validation.assertNonNull(cancelBtn);
        Validation.assertNonNull(nextBtn);

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
        AssetProxy proxy = asset.getProxy();
        if(proxy == null)
        {
            tickerText.setText("");
            autoNameLabel.setText("");
            privatePriceText.setText("");
            proxyPriceText.setText("");
            pricingDateText.setText("");
        }
        else
        {
            tickerText.setText(proxy.getProxyTicker());
            autoNameLabel.setText(proxy.getProxyAutoName());
            privatePriceText.setText(NumberFormatHelper.formatWith2Decimals(proxy.getPrivateAssetPriceComp()));
            proxyPriceText.setText(NumberFormatHelper.formatWith2Decimals(proxy.getProxyAssetPriceComp()));
            pricingDateText.setText(DateHelper.formatUSLocalDate(proxy.getPricingCompDate()));
        }
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
            getApp().<Asset,Asset>mySwitchView(App.ASSET_EDIT_KNOWN_VIEW, getIn(),
                    a ->  returnSuccess(a),
                    () -> returnFailure());
        }
    }

    private boolean save()
    {
        Asset asset = getIn();
        AssetProxy proxy = Objects.requireNonNullElse(asset.getProxy(), new AssetProxy());

        proxy.setProxyTicker(tickerText.getText().trim().toUpperCase());
        proxy.setProxyAutoName(autoNameLabel.getText().trim());

        proxy.setPrivateAssetPriceComp(NumberFormatHelper.parseNumber2(privatePriceText.getText()));
        proxy.setProxyAssetPriceComp(NumberFormatHelper.parseNumber2(proxyPriceText.getText()));
        try
        {
            proxy.setPricingCompDate(DateHelper.parseUSLocalDate(pricingDateText.getText()));
        }
        catch (DateTimeParseException|IndexOutOfBoundsException e)
        {
            getApp().showMessage("Valid pricing date required (MM/DD/YYYY)");
            return false;
        }

        if(Validation.isBlank(proxy.getProxyTicker()))
        {
            getApp().showMessage("Ticker required");
            return false;
        }
        if(Validation.isBlank(proxy.getProxyAutoName()))
        {
            AssetTicker found = AssetTickerCache.getInstance().lookup(proxy.getProxyTicker());
            if(found == null)
            {
                getApp().showMessage("Ticker selection required");
                return false;
            }
            // quirk of autosuggest widget - you can enter exactly the right value but make no selection
            proxy.setProxyAutoName(found.getName());
        }

        if(proxy.getPrivateAssetPriceComp() == null)
        {
            getApp().showMessage("Private asset price required");
            return false;
        }
        if(proxy.getProxyAssetPriceComp() == null)
        {
            getApp().showMessage("Proxy asset price required");
            return false;
        }
        if(proxy.getPricingCompDate() == null)
        {
            getApp().showMessage("Pricing date required");
            return false;
        }

        asset.setProxy(proxy);
        asset.markDirty();

        if(proxy.getLastProxyValue() == null)
        {
            if(!StateManager.refreshPrice(asset))
            {
                getApp().showMessage("Error getting asset price per unit");
                return false;
            }
        }

        return true;
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
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.ASSET_EDIT_PROXY_HELP_URL);
    }

}
