package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.Validation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class PortfolioListController
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioListController.class);

    public static final String APP_BAR_TITLE = "Portfolio List";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private View view;

    @FXML
    private Button newButton;

    @FXML
    private ListView<Portfolio> list;

    @FXML
    private VBox noPortfolios;


    @FXML
    void initialize() throws IOException
    {
        // indicates fxml naming mismatch
        Validation.assertNonNull(view);
        Validation.assertNonNull(list);
        Validation.assertNonNull(newButton);

        view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

        newButton.setOnAction(e -> newPortfolio());
        newButton.setGraphic(MaterialDesignIcon.ADD_CIRCLE.graphic());

        list.getSelectionModel().selectedItemProperty().addListener(e -> {
            Portfolio p = list.getSelectionModel().getSelectedItem();
            if(p != null)
            {
                StateManager.currentPortfolioId = p.getId(); // TODO: global state is ugly
                view.getAppManager().switchView(App.PORTFOLIO_VIEW, ViewStackPolicy.USE);
            }
        });

        list.setCellFactory(new PortfolioListCell.Factory());
    }

    protected void populateData()
    {
        try
        {
            List<String> ids = DataFactory.get().getPortfolioIds();
            List<Portfolio> items = new ArrayList<>();
            for(String id : ids)
            {
                // TODO: optimize - inefficient for remote calls
                items.add(DataFactory.get().getPortfolio(id));
            }
            items.sort(Comparator.comparing(Portfolio::getName));

            list.getSelectionModel().clearSelection();
            list.setItems(FXCollections.observableList( items ));

            if(items.isEmpty())
            {
                list.setVisible(false);
                noPortfolios.setVisible(true);
            }
            else
            {
                list.setVisible(true);
                noPortfolios.setVisible(false);
            }
        }
        catch (IOException e)
        {
            LOG.error("Error loading portfolios", e);
            view.getAppManager().showMessage("Error loading portfolios");
        }
    }

    protected void updateAppBar()
    {
        final AppBar appBar = view.getAppManager().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> view.getAppManager().getDrawer().open()));
        appBar.getActionItems().clear();
        appBar.getActionItems().add(MaterialDesignIcon.ADD_CIRCLE.button(e -> newPortfolio()));
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> populateData()));
        appBar.setTitleText(APP_BAR_TITLE);
    }


    private void newPortfolio()
    {
        Portfolio p = new Portfolio();
        try
        {
            DataFactory.get().createPortfolio(p);
            StateManager.currentPortfolioId = p.getId();
            view.getAppManager().switchView(App.PORTFOLIO_VIEW, ViewStackPolicy.USE);
        }
        catch (IOException e)
        {
            LOG.error("Error creating portfolio: " + p.getId(), e);
            view.getAppManager().showMessage("Error creating portfolio: " + p.getId());
        }
    }

}
