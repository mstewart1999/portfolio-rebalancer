package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.controllers.cells.PortfolioGoalListCell;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioGoal;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

public class PortfolioController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioController.class);
    public static final String APP_BAR_TITLE = "Portfolio";

    @FXML
    private Button accountsButton;

    @FXML
    private Button actualAAButton;

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
    private Label goalLabel;

    @FXML
    private Button investButton;

    @FXML
    private Label nameLabel;

    @FXML
    private TextField nameText;

    @FXML
    private Button rebalanceButton;

    @FXML
    private Button targetAAButton;

    @FXML
    private Label valueAsOfLabel;

    @FXML
    private Label valueLabel;

    @FXML
    private Button withdrawalButton;


    public PortfolioController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(accountsButton);
        Validation.assertNonNull(actualAAButton);
        Validation.assertNonNull(alertsErrorIcon);
        Validation.assertNonNull(alertsButton);
        Validation.assertNonNull(alertsInfoIcon);
        Validation.assertNonNull(alertsWarnIcon);
        Validation.assertNonNull(goalCombo);
        Validation.assertNonNull(goalLabel);
        Validation.assertNonNull(investButton);
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(nameText);
        Validation.assertNonNull(rebalanceButton);
        Validation.assertNonNull(targetAAButton);
        Validation.assertNonNull(valueAsOfLabel);
        Validation.assertNonNull(valueLabel);
        Validation.assertNonNull(withdrawalButton);

        nameLabel.setLabelFor(nameText);
        goalLabel.setLabelFor(goalCombo);

        goalCombo.getItems().setAll(PortfolioGoal.values());
        goalCombo.setButtonCell(new PortfolioGoalListCell());
        goalCombo.setCellFactory(new PortfolioGoalListCell.Factory());


        // TODO
        //accountsButton.setDisable(true);
        //targetAAButton.setDisable(true);
        actualAAButton.setDisable(true);
        alertsButton.setDisable(true);
        investButton.setDisable(true);
        withdrawalButton.setDisable(true);
        rebalanceButton.setDisable(true);
        accountsButton.setOnAction(e -> visitAccountList());
        targetAAButton.setOnAction(e -> visitTargetAAList());
        actualAAButton.setOnAction(e -> {});
        alertsButton.setOnAction(e -> {});
        investButton.setOnAction(e -> {});
        withdrawalButton.setOnAction(e -> {});
        rebalanceButton.setOnAction(e -> {});
        accountsButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        targetAAButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        actualAAButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        alertsButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        investButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        withdrawalButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        rebalanceButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());

        // colors?
        alertsInfoIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), null, null)));
        alertsWarnIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("yellow"), null, null)));
        alertsErrorIcon.setBackground(new Background(new BackgroundFill(Paint.valueOf("red"), null, null)));
    }

    @Override
    protected void populateData(final Portfolio p)
    {
        nameText.setText(p.getName());
        goalCombo.getSelectionModel().select(p.getGoal());

        populateTotalValue();

        boolean infos = false;
        boolean warns = true;
        boolean errors = true;
        alertsInfoIcon.setVisible(infos);
        alertsWarnIcon.setVisible(warns);
        alertsErrorIcon.setVisible(errors);
    }

    private void populateTotalValue()
    {
        Portfolio p = getIn();

        if(p.getLastValue() != null)
        {
            valueLabel.setText("$ " + NumberFormatHelper.prettyFormatCurrency(p.getLastValue()));
        }
        else
        {
            valueLabel.setText("$ 0");
        }
        if(p.getLastValueTmstp() != null)
        {
            valueAsOfLabel.setText(
                    String.format(
                        "(as of %s)",
                        DateTimeFormatter.ISO_LOCAL_DATE.format(
                                p.getLastValueTmstp().toInstant().atZone(ZoneId.systemDefault()))));
        }
        else
        {
            valueAsOfLabel.setText("");
        }
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        //appBar.getActionItems().add(MaterialDesignIcon.DELETE_FOREVER.button(e -> deletePortfolio()));
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        if(save())
        {
            returnSuccess(getIn());
        }
    }

    private boolean save()
    {
        Portfolio p = getIn();
        String oldName = p.getName();
        String newName = nameText.getText();
        if(!oldName.equals(newName))
        {
            p.setName(newName);
            p.markDirty();
        }

        PortfolioGoal oldGoal = p.getGoal();
        PortfolioGoal newGoal = goalCombo.getSelectionModel().getSelectedItem();
        if(oldGoal != newGoal)
        {
            p.setGoal(newGoal);
            p.markDirty();
        }

        try
        {
            PersistManager.persistAll(p.getProfile());
        }
        catch (IOException e)
        {
            LOG.error("Error updating portfolio: " + p.getId(), e);
            getApp().showMessage("Error updating portfolio");
            return false;
        }

        return true;
    }

    private void visitAccountList()
    {
        getApp().<Portfolio,Portfolio>mySwitchView(App.ACCOUNT_LIST_VIEW, getIn(),
                p -> {
                    populateTotalValue();
                },
                () -> {
                    // no-op
                });
    }

    private void visitTargetAAList()
    {
        getApp().<AssetAllocation,AssetAllocation>mySwitchView(App.TARGET_AA_VIEW, getIn().getTargetAA(),
                aa -> {
                    getIn().setTargetAA(aa);
                    getIn().markDirty();

                    try
                    {
                        PersistManager.persistAll(getIn().getProfile());
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error updating portfolio: " + getIn().getId(), e);
                        getApp().showMessage("Error updating portfolio");
                    }
                },
                () -> {
                    // no-op
                });
    }
}
