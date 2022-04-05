package com.msfinance.rebalancer.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.rebalancer.StateManager;
import com.msfinance.rebalancer.model.PortfolioGoal;
import com.msfinance.rebalancer.service.DataFactory;
import com.msfinance.rebalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

public class PortfolioController
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioController.class);
    public static final String APP_BAR_TITLE = "Portfolio";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private View view;

    @FXML
    private Button accountsButton;

    @FXML
    private Button alertsButton;

    @FXML
    private Icon alertsErrorIcon;

    @FXML
    private Icon alertsInfoIcon;

    @FXML
    private Icon alertsWarnIcon;

    @FXML
    private ComboBox<PortfolioGoal> goalCombo;

    @FXML
    private TextField nameText;

    @FXML
    private Button rebalancingButton;

    @FXML
    private Button targetAAButton;

    @FXML
    private Label valueAsOfLabel;

    @FXML
    private Label valueLabel;

    @FXML
    void initialize()
    {
        Validation.assertNonNull(view);
        Validation.assertNonNull(accountsButton);
        Validation.assertNonNull(alertsErrorIcon);
        Validation.assertNonNull(alertsButton);
        Validation.assertNonNull(alertsInfoIcon);
        Validation.assertNonNull(alertsWarnIcon);
        Validation.assertNonNull(goalCombo);
        Validation.assertNonNull(nameText);
        Validation.assertNonNull(rebalancingButton);
        Validation.assertNonNull(targetAAButton);
        Validation.assertNonNull(valueAsOfLabel);
        Validation.assertNonNull(valueLabel);

        //view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });
// Is there another way to get "back" other than our app bar nav icon?
//        view.setOnHiding(e -> {
//            savePortfolio();
//            e.consume();
//        });

        for(PortfolioGoal g : PortfolioGoal.values())
        {
            goalCombo.getItems().add(g);
        }

        // TODO
        accountsButton.setOnAction(e -> {});
        targetAAButton.setOnAction(e -> {});
        rebalancingButton.setOnAction(e -> {});
        alertsButton.setOnAction(e -> {});

        // colors?
        alertsInfoIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), null, null)));
        alertsWarnIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("yellow"), null, null)));
        alertsErrorIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("red"), null, null)));
    }

    //------------------------------------
    protected void populateData()
    {
        try
        {
            String id = StateManager.currentPortfolioId;
            StateManager.currentPortfolio = DataFactory.get().getPortfolio(id);

            nameText.setText(StateManager.currentPortfolio.getName());
            goalCombo.getSelectionModel().select(StateManager.currentPortfolio.getGoal());

            valueLabel.setText("$ 75.22 K"); // TODO
            valueAsOfLabel.setText(String.format("(as of %s)", DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())));

            boolean infos = false;
            boolean warns = true;
            boolean errors = true;
            alertsInfoIcon.setVisible(infos);
            alertsWarnIcon.setVisible(warns);
            alertsErrorIcon.setVisible(errors);
        }
        catch (IOException e)
        {
            LOG.error("Error reading: " + StateManager.currentPortfolioId, e);
            view.getApplication().showMessage("Error reading: " + StateManager.currentPortfolioId);
        }
    }

    protected void updateAppBar()
    {
        final AppBar appBar = view.getApplication().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.getActionItems().add(MaterialDesignIcon.DELETE_FOREVER.button(e -> deletePortfolio()));
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        savePortfolio();
        view.getApplication().switchToPreviousView();
    }

    private void savePortfolio()
    {
        boolean hasChanges = false;

        String oldName = StateManager.currentPortfolio.getName();
        String newName = nameText.getText();
        if(!oldName.equals(newName))
        {
            StateManager.currentPortfolio.setName(newName);
            hasChanges = true;
        }

        PortfolioGoal oldGoal = StateManager.currentPortfolio.getGoal();
        PortfolioGoal newGoal = goalCombo.getSelectionModel().getSelectedItem();
        if(oldGoal != newGoal)
        {
            StateManager.currentPortfolio.setGoal(newGoal);
            hasChanges = true;
        }

        if(hasChanges)
        {
            try
            {
                DataFactory.get().updatePortfolio(StateManager.currentPortfolio);
            }
            catch (IOException e)
            {
                LOG.error("Error updating: " + StateManager.currentPortfolioId, e);
                view.getApplication().showMessage("Error updating: " + StateManager.currentPortfolioId);
            }
        }
    }

    private void deletePortfolio()
    {
        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this portfolio?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deletePortfolio(StateManager.currentPortfolioId);
                view.getApplication().showMessage("Deleted: " + StateManager.currentPortfolioId);
                view.getApplication().switchToPreviousView();
            }
            catch (IOException e)
            {
                LOG.error("Error deleting: " + StateManager.currentPortfolioId, e);
                view.getApplication().showMessage("Error deleting: " + StateManager.currentPortfolioId);
            }
        }
    }
}
