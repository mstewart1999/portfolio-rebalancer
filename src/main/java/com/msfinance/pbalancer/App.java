package com.msfinance.pbalancer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.msfinance.pbalancer.controllers.BaseController;
import com.msfinance.pbalancer.controllers.BaseController.FailureCallback;
import com.msfinance.pbalancer.controllers.BaseController.SuccessCallback;
import com.msfinance.pbalancer.controllers.PortfolioListController;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.aa.AssetTickerCache;
import com.msfinance.pbalancer.service.ProfileDataCache;
import com.msfinance.pbalancer.views.PrimaryView;
import com.msfinance.pbalancer.views.SecondaryView;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application
{
    private static final Logger LOG;
    static
    {
        String path = App.class.getClassLoader()
                                    .getResource("logging.properties")
                                    .getFile();
        System.setProperty("java.util.logging.config.file", path);
        LOG = LoggerFactory.getLogger(App.class);
    }

    public static final String PORTFOLIO_LIST_VIEW = AppManager.HOME_VIEW;
    public static final String ABOUT_VIEW = "ABOUT_VIEW";
    public static final String SETTINGS_VIEW = "SETTINGS_VIEW";

    public static final String PORTFOLIO_VIEW = "PORTFOLIO_VIEW";
    public static final String ACCOUNT_LIST_VIEW = "ACCOUNT_LIST_VIEW";
    public static final String ACCOUNT_EDIT_VIEW = "ACCOUNT_EDIT_VIEW";
    public static final String ASSET_ADD_VIEW = "ASSET_ADD_VIEW";
    public static final String ASSET_EDIT_PROXY_VIEW = "ASSET_EDIT_PROXY_VIEW";
    public static final String ASSET_EDIT_KNOWN_VIEW = "ASSET_EDIT_KNOWN_VIEW";
    public static final String ASSET_EDIT_MANUAL_VIEW = "ASSET_EDIT_MANUAL_VIEW";
    public static final String TARGET_AA_VIEW = "TARGET_AA_VIEW";
    public static final String ACTUAL_AA_VIEW = "ACTUAL_AA_VIEW";
    public static final String REBALANCE_SUGGESTIONS = "REBALANCE_SUGGESTIONS";
    public static final String WEB_VIEW = "WEB_VIEW";


    private final AppManager appManager = AppManager.initialize(this::postInit);

    private final Map<String,View> viewByKey = new HashMap<>();
    private final Map<String,BaseController<?,?>> controllerByKey = new HashMap<>();

    @Override
    public void init()
    {
        LOG.info("App.init()");

        // eagerly create all view
        createView(PORTFOLIO_LIST_VIEW, "portfolioList.fxml");
        createView(PORTFOLIO_VIEW, "portfolio.fxml");
        createView(ACCOUNT_LIST_VIEW, "accountList.fxml");
        createView(ACCOUNT_EDIT_VIEW, "accountEdit.fxml");
        createView(ASSET_ADD_VIEW, "assetAdd.fxml");
        createView(ASSET_EDIT_PROXY_VIEW, "assetEditProxy.fxml");
        createView(ASSET_EDIT_KNOWN_VIEW, "assetEditKnown.fxml");
        createView(ASSET_EDIT_MANUAL_VIEW, "assetEditManual.fxml");
        createView(ASSET_EDIT_MANUAL_VIEW, "assetEditManual.fxml");
        createView(TARGET_AA_VIEW, "targetAA.fxml");
        createView(REBALANCE_SUGGESTIONS, "rebalanceSuggestions.fxml");
        createView(ACTUAL_AA_VIEW, "actualAA.fxml");
        //createView(WEB_VIEW, "webView.fxml");

        // TODO: if first time, show a welcome screen as "HOME"?
        // views in drawer
        appManager.addViewFactory(PORTFOLIO_LIST_VIEW, () -> viewByKey.get(PORTFOLIO_LIST_VIEW));
        appManager.addViewFactory(ABOUT_VIEW, PrimaryView::new);
        appManager.addViewFactory(SETTINGS_VIEW, SecondaryView::new);

        // other views
        appManager.addViewFactory(PORTFOLIO_VIEW, () -> viewByKey.get(PORTFOLIO_VIEW));
        appManager.addViewFactory(ACCOUNT_LIST_VIEW, () -> viewByKey.get(ACCOUNT_LIST_VIEW));
        appManager.addViewFactory(ACCOUNT_EDIT_VIEW, () -> viewByKey.get(ACCOUNT_EDIT_VIEW));
        appManager.addViewFactory(ASSET_ADD_VIEW, () -> viewByKey.get(ASSET_ADD_VIEW));
        appManager.addViewFactory(ASSET_EDIT_PROXY_VIEW, () -> viewByKey.get(ASSET_EDIT_PROXY_VIEW));
        appManager.addViewFactory(ASSET_EDIT_KNOWN_VIEW, () -> viewByKey.get(ASSET_EDIT_KNOWN_VIEW));
        appManager.addViewFactory(ASSET_EDIT_MANUAL_VIEW, () -> viewByKey.get(ASSET_EDIT_MANUAL_VIEW));
        appManager.addViewFactory(TARGET_AA_VIEW, () -> viewByKey.get(TARGET_AA_VIEW));
        appManager.addViewFactory(ACTUAL_AA_VIEW, () -> viewByKey.get(ACTUAL_AA_VIEW));
        appManager.addViewFactory(REBALANCE_SUGGESTIONS, () -> viewByKey.get(REBALANCE_SUGGESTIONS));
        appManager.addViewFactory(WEB_VIEW, () -> createView(WEB_VIEW, "webView.fxml"));

        buildDrawer();

        // TODO: create new UI for add/switch profile
        try
        {
            ProfileDataCache.switchProfile(Profile.DEFAULT);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO: progress bar
        AssetTickerCache.getInstance();
    }

    private View createView(final String key, final String fxml)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("views/" + fxml)
                    // TODO: i18n? https://github.com/gluonhq/gluon-samples-sandbox/blob/master/HelloFXML/src/main/resources/hellofx/hello.properties
                    /*,ResourceBundle.getBundle("hellofx.hello")*/
                    );
            View root = loader.load();
            Object controller = loader.getController();
            if(controller instanceof BaseController<?,?> bc)
            {
                bc.initializeApp(this, root);
                controllerByKey.put(key, bc);
            }
            viewByKey.put(key, root);
            return root;
        }
        catch (IOException e)
        {
            LOG.error("Failed to create " + fxml);
            throw new RuntimeException("Failed to create " + fxml, e);
        }
    }

    private void buildDrawer()
    {
        NavigationDrawer drawer = appManager.getDrawer();

        NavigationDrawer.Header header = new NavigationDrawer.Header(
                "pBalancer",
                "Portfolio Manager",
                new Avatar(21, new Image(getClass().getResourceAsStream("/icon.png"))));
        drawer.setHeader(header);

        final Item i3 = new ViewItem(
                PortfolioListController.APP_BAR_TITLE,
                MaterialDesignIcon.LIST.graphic(),
                App.PORTFOLIO_LIST_VIEW,
                ViewStackPolicy.USE);
        final Item i2 = new ViewItem(
                "Settings",
                MaterialDesignIcon.SETTINGS.graphic(),
                App.SETTINGS_VIEW);
        final Item i1 = new ViewItem(
                "About",
                MaterialDesignIcon.INFO.graphic(),
                App.ABOUT_VIEW,
                ViewStackPolicy.SKIP);
        drawer.getItems().addAll(i3, i2, i1);

        if (Platform.isDesktop())
        {
            final Item quitItem = new Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
            quitItem.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv)
                {
                    Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                }
            });
            drawer.getItems().add(quitItem);
        }
    }

    @Override
    public void start(final Stage stage)
    {
        LOG.info("App.start() - begin");
        appManager.start(stage);
        // postInit is called here by appManager

        // UGGG: major flaw, this never gets called from drawer, or on first init
        this.<Profile,Profile>mySwitchView(PORTFOLIO_LIST_VIEW, ProfileDataCache.get().getProfile(),
                p -> {
                    // TODO: recalculate profile value, save profile
                },
                () -> {
                    // no-op
                });

        LOG.info("App.start() - end");
    }

    private void postInit(final Scene scene)
    {
        LOG.info("App.postInit()");
        //Swatch.getDefault().assignTo(scene);
        Swatch.LIGHT_GREEN.assignTo(scene);
        // hex #85bb65 (also known as Dollar bill)
        // 52.2% red, 73.3% green and 39.6% blue
//        new Color(0.522, 0.733, 0.396, 1.0);

        //setUserAgentStylesheet(STYLESHEET_MODENA); - not functioning
        scene.getStylesheets().add(App.class.getResource("app.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icon.png")));
        scene.getWindow().setWidth(800);
        scene.getWindow().setHeight(900);
        scene.getWindow().centerOnScreen();
    }


    public void mySwitchToPreviousView()
    {
        appManager.switchToPreviousView();
    }

    public void mySwitchView(final String key)
    {
        mySwitchView(key, null, null, null);
    }

    public <IN,OUT> void mySwitchView(final String key, final IN in)
    {
        mySwitchView(key, in, null, null);
    }

    public <IN,OUT> void mySwitchView(final String key, final IN in, final SuccessCallback<OUT> successCallback, final FailureCallback failureCallback)
    {
        appManager.switchView(key);
        BaseController<IN,OUT> bc = (BaseController<IN, OUT>) controllerByKey.get(key);
        if(bc != null)
        {
            bc.call(in, successCallback, failureCallback);
        }
        else
        {
            throw new RuntimeException("No controller for " + key);
        }
    }


    public void showMessage(final String msg)
    {
        appManager.showMessage(msg);
    }

    public void showDrawer()
    {
        appManager.getDrawer().open();
    }

    public static void main(final String args[])
    {
        launch(args);
    }
}
