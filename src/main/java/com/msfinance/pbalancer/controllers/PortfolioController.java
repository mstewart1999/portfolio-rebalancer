package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.PortfolioGoalListCell;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioGoal;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.aa.PreferredAsset;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class PortfolioController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioController.class);
    public static final String APP_BAR_TITLE = "Portfolio";

    @FXML
    private Button accountsButton;

    @FXML
    private Button acmButton;

    @FXML
    private Button actualAAButton;

    @FXML
    private ImageView accountsErrorImg;

    @FXML
    private ImageView accountsInfoImg;

    @FXML
    private ImageView accountsWarnImg;

    @FXML
    private ImageView taaErrorImg;

    @FXML
    private ImageView taaInfoImg;

    @FXML
    private ImageView taaWarnImg;

    @FXML
    private ImageView acmErrorImg;

    @FXML
    private ImageView acmInfoImg;

    @FXML
    private ImageView acmWarnImg;

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
    private Label totalValueLabel;

    @FXML
    private Label valueAsOfLabel;

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
        Validation.assertNonNull(acmButton);
        Validation.assertNonNull(actualAAButton);
        Validation.assertNonNull(accountsErrorImg);
        Validation.assertNonNull(accountsInfoImg);
        Validation.assertNonNull(accountsWarnImg);
        Validation.assertNonNull(taaErrorImg);
        Validation.assertNonNull(taaInfoImg);
        Validation.assertNonNull(taaWarnImg);
        Validation.assertNonNull(acmErrorImg);
        Validation.assertNonNull(acmInfoImg);
        Validation.assertNonNull(acmWarnImg);
        Validation.assertNonNull(goalCombo);
        Validation.assertNonNull(goalLabel);
        Validation.assertNonNull(investButton);
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(nameText);
        Validation.assertNonNull(rebalanceButton);
        Validation.assertNonNull(targetAAButton);
        Validation.assertNonNull(totalValueLabel);
        Validation.assertNonNull(valueAsOfLabel);
        Validation.assertNonNull(withdrawalButton);

        nameLabel.setLabelFor(nameText);
        goalLabel.setLabelFor(goalCombo);

        goalCombo.getItems().setAll(PortfolioGoal.values());
        goalCombo.setButtonCell(new PortfolioGoalListCell());
        goalCombo.setCellFactory(new PortfolioGoalListCell.Factory());


        accountsButton.setOnAction(e -> visitAccountList());
        targetAAButton.setOnAction(e -> visitTargetAAList());
        acmButton.setOnAction(e -> visitAssetClassMapping());
        actualAAButton.setOnAction(e -> visitActualAAList());
        investButton.setOnAction(e -> visitInvest());
        withdrawalButton.setOnAction(e -> visitWithdrawal());
        rebalanceButton.setOnAction(e -> visitRebalance());
        accountsButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        targetAAButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        acmButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        actualAAButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        investButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        withdrawalButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
        rebalanceButton.setGraphic(MaterialDesignIcon.CHEVRON_RIGHT.graphic());
    }

    @Override
    protected void populateData(final Portfolio p)
    {
        nameText.setText(p.getName());
        goalCombo.getSelectionModel().select(p.getGoal());

        populateTotalValue();
        populateAlerts(p);
    }

    private void populateTotalValue()
    {
        Portfolio p = getIn();

        if(p.getLastValue() != null)
        {
            totalValueLabel.setText("$ " + NumberFormatHelper.prettyFormatCurrency(p.getLastValue()));
        }
        else
        {
            totalValueLabel.setText("$ 0");
        }
        if(p.getLastValueTmstp() != null)
        {
            valueAsOfLabel.setText(
                String.format("(as of %s)", p.getLastValueTmstpRange()));
        }
        else
        {
            valueAsOfLabel.setText("");
        }
    }

    private void populateAlerts(final Portfolio p)
    {
        accountsInfoImg.setVisible(p.countAccountInfos() > 0);
        accountsWarnImg.setVisible(p.countAccountWarns() > 0);
        accountsErrorImg.setVisible(p.countAccountErrors() > 0);
//        Tooltip.install(taaInfoImg, new Tooltip("Account Info Alerts: " + p.countAccountInfos()));
//        Tooltip.install(taaWarnImg, new Tooltip("Account Warn Alerts: " + p.countAccountWarns()));
//        Tooltip.install(taaErrorImg, new Tooltip("Account Error Alerts: " + p.countAccountErrors()));

        taaInfoImg.setVisible(p.countTAAInfos() > 0);
        taaWarnImg.setVisible(p.countTAAWarns() > 0);
        taaErrorImg.setVisible(p.countTAAErrors() > 0);
//        Tooltip.install(taaInfoImg, new Tooltip("AA Info Alerts: " + p.countTAAInfos()));
//        Tooltip.install(taaWarnImg, new Tooltip("AA Warn Alerts: " + p.countTAAWarns()));
//        Tooltip.install(taaErrorImg, new Tooltip("AA Error Alerts: " + p.countTAAErrors()));

        acmInfoImg.setVisible(p.countACMInfos() > 0);
        acmWarnImg.setVisible(p.countACMWarns() > 0);
        acmErrorImg.setVisible(p.countACMErrors() > 0);
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        if(nameText.getText().isEmpty())
        {
            nameText.requestFocus();
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
                    populateAlerts(getIn());
                },
                () -> {
                    // no-op
                });
    }

    private void visitTargetAAList()
    {
        AssetAllocation aaIn = getIn().getTargetAA();
        getApp().<AssetAllocation,AssetAllocation>mySwitchView(App.TARGET_AA_VIEW, aaIn,
                aaOut -> {
                    getIn().setTargetAA(aaOut);
                    getIn().markDirty();

                    List<PreferredAsset> created = getIn().validateAssetClassMappings();
                    for(PreferredAsset acm : created)
                    {
                        try
                        {
                            DataFactory.get().createAssetClassMapping(acm);
                            acm.markClean();
                        }
                        catch (IOException e)
                        {
                            LOG.error("Error creating preferred assets: " + acm.getId(), e);
                            getApp().showMessage("Error creating preferred assets");
                        }
                    }

                    populateAlerts(getIn());

                    try
                    {
                        PersistManager.persistAll(getIn().getProfile());
                        getApp().showMessage("Saved asset allocation");
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

    private void visitAssetClassMapping()
    {
        getApp().<Portfolio,Portfolio>mySwitchView(App.PREFERRED_ASSETS_VIEW, getIn(),
                p -> {
                    List<PreferredAsset> created = getIn().validateAssetClassMappings();
                    for(PreferredAsset acm : created)
                    {
                        try
                        {
                            DataFactory.get().createAssetClassMapping(acm);
                            acm.markClean();
                        }
                        catch (IOException e)
                        {
                            LOG.error("Error creating preferred assets: " + acm.getId(), e);
                            getApp().showMessage("Error creating preferred assets");
                        }
                    }

                    populateAlerts(getIn());
                },
                () -> {
                    // no-op
                });
    }

    private void visitActualAAList()
    {
        if(!confirmTargetAA(getIn()))
        {
            return;
        }
        getApp().<Portfolio,Portfolio>mySwitchView(App.ACTUAL_AA_VIEW, getIn(),
                p -> {
                    // no-op
                },
                () -> {
                    // no-op
                });
    }

    private void visitInvest()
    {
        if(!confirmTargetAA(getIn()))
        {
            return;
        }
        if(!confirmAccounts(getIn()))
        {
            return;
        }
        // not needed for invest
//        if(!confirmAssets(getIn()))
//        {
//            return;
//        }
        if(!confirmAssetClassMapping(getIn()))
        {
            return;
        }
        getApp().<Portfolio,Void>mySwitchView(App.INVEST_SUGGESTIONS_PROMPT_VIEW, getIn(),
                p -> {
                    // no-op
                },
                () -> {
                    // no-op
                });
    }

    private void visitWithdrawal()
    {
        if(!confirmTargetAA(getIn()))
        {
            return;
        }
        if(!confirmAccounts(getIn()))
        {
            return;
        }
        if(!confirmAssets(getIn()))
        {
            return;
        }
        // not needed for withdrawal
//        if(!confirmAssetClassMapping(getIn()))
//        {
//            return;
//        }
        getApp().<Portfolio,Void>mySwitchView(App.WITHDRAWAL_SUGGESTIONS_PROMPT_VIEW, getIn(),
                p -> {
                    // no-op
                },
                () -> {
                    // no-op
                });
    }

    private void visitRebalance()
    {
        if(!confirmTargetAA(getIn()))
        {
            return;
        }
        if(!confirmAccounts(getIn()))
        {
            return;
        }
        if(!confirmAssets(getIn()))
        {
            return;
        }
        if(!confirmAssetClassMapping(getIn()))
        {
            return;
        }
        getApp().<Portfolio,Void>mySwitchView(App.REBALANCE_SUGGESTIONS_VIEW, getIn(),
                p -> {
                    // no-op
                },
                () -> {
                    // no-op
                });
    }


    private boolean confirmTargetAA(final Portfolio p)
    {
        AssetAllocation aa = p.getTargetAA();
        if(aa == null)
        {
            getApp().showMessage("Define target asset allocation first");
            return false;
        }
        try
        {
            aa.getRoot().validate();
        }
        catch (InvalidDataException e)
        {
            getApp().showMessage("Fix target asset allocation first");
            return false;
        }
        if((aa.countErrors() > 0) || (aa.countWarns() > 0))
        {
            getApp().showMessage("Fix target asset allocation alerts first");
            return false;
        }
        return true;
    }

    private boolean confirmAccounts(final Portfolio p)
    {
        if(p.getAccounts().size() == 0)
        {
            getApp().showMessage("Define accounts first");
            return false;
        }
        return true;
    }

    private boolean confirmAssets(final Portfolio p)
    {
        if(StateManager.listAssets(p).size() == 0)
        {
            getApp().showMessage("Define assets first");
            return false;
        }
        return true;
    }

    private boolean confirmAssetClassMapping(final Portfolio p)
    {
        if((p.countACMErrors() > 0) || (p.countACMWarns() > 0))
        {
            getApp().showMessage("Fix preferred asset alerts first");
            return false;
        }
        return true;
    }
}
