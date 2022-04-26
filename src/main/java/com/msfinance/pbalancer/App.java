package com.msfinance.pbalancer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.msfinance.pbalancer.controllers.PortfolioListController;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.aa.AssetTickerCache;
import com.msfinance.pbalancer.views.PrimaryView;
import com.msfinance.pbalancer.views.SecondaryView;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends MobileApplication
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
    public static final String ASSET_EDIT_KNOWN_VIEW = "ASSET_EDIT_KNOWN_VIEW";
    public static final String ASSET_EDIT_MANUAL_VIEW = "ASSET_EDIT_MANUAL_VIEW";
    public static final String TARGET_AA_VIEW = "TARGET_AA_VIEW";
    public static final String WEB_VIEW = "WEB_VIEW";

    @Override
    public void init()
    {
        // TODO: if first time, show a welcome screen as "HOME"?
        // views in drawer
        addViewFactory(PORTFOLIO_LIST_VIEW, () -> createView("portfolioList.fxml"));
        addViewFactory(ABOUT_VIEW, PrimaryView::new);
        addViewFactory(SETTINGS_VIEW, SecondaryView::new);

        // other views
        addViewFactory(PORTFOLIO_VIEW, () -> createView("portfolio.fxml"));
        addViewFactory(ACCOUNT_LIST_VIEW, () -> createView("accountList.fxml"));
        addViewFactory(ACCOUNT_EDIT_VIEW, () -> createView("accountEdit.fxml"));
        addViewFactory(ASSET_ADD_VIEW, () -> createView("assetAdd.fxml"));
        addViewFactory(ASSET_EDIT_KNOWN_VIEW, () -> createView("assetEditKnown.fxml"));
        addViewFactory(ASSET_EDIT_MANUAL_VIEW, () -> createView("assetEditManual.fxml"));
        addViewFactory(TARGET_AA_VIEW, () -> createView("targetAA.fxml"));
        addViewFactory(WEB_VIEW, () -> createView("webView.fxml"));

        buildDrawer();

        StateManager.reset();
        StateManager.currentProfile = new Profile("DEFAULT"); // TODO: create new UI for add/switch profile
        StateManager.currentProfile.setName("DEFAULT");

        // TODO: progress bar
        AssetTickerCache.getInstance();
    }

    private View createView(final String fxml)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("views/" + fxml)
                    // TODO: i18n? https://github.com/gluonhq/gluon-samples-sandbox/blob/master/HelloFXML/src/main/resources/hellofx/hello.properties
                    /*,ResourceBundle.getBundle("hellofx.hello")*/
                    );
            View root = loader.load();
            loader.getController(); // TODO: any value in knowing controller?  parameter passing...
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
        NavigationDrawer drawer = this.getDrawer();

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
    public void postInit(final Scene scene)
    {
        Swatch.getDefault().assignTo(scene);

        //setUserAgentStylesheet(STYLESHEET_MODENA); - not functioning
        scene.getStylesheets().add(App.class.getResource("app.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icon.png")));
        scene.getWindow().setWidth(800);
        scene.getWindow().setHeight(600);
        scene.getWindow().centerOnScreen();
    }

    public static void main(final String args[])
    {
        launch(args);
    }
}
