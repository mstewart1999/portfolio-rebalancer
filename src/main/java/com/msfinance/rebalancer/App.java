package com.msfinance.rebalancer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.msfinance.rebalancer.controllers.PortfolioListController;
import com.msfinance.rebalancer.views.PrimaryView;
import com.msfinance.rebalancer.views.SecondaryView;

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

    public static final String PORTFOLIO_LIST_VIEW = HOME_VIEW;
    public static final String ABOUT_VIEW = "ABOUT_VIEW";
    public static final String SETTINGS_VIEW = "SETTINGS_VIEW";

    public static final String PORTFOLIO_VIEW = "PORTFOLIO_VIEW";

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

        buildDrawer();

        StateManager.reset();
    }

    private View createView(final String fxml)
    {
        try
        {
            View root = FXMLLoader.load(
                    getClass().getResource("views/" + fxml)
                    // TODO: i18n? https://github.com/gluonhq/gluon-samples-sandbox/blob/master/HelloFXML/src/main/resources/hellofx/hello.properties
                    /*,ResourceBundle.getBundle("hellofx.hello")*/
                    );
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
                "MAS Finance Tools",
                "Portfolio Rebalancer",
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
