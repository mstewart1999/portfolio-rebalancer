package com.pbalancer.client.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.App;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.rebalance.ActualAANode;
import com.pbalancer.client.model.rebalance.PortfolioCashSuggestionsRequest;
import com.pbalancer.client.model.rebalance.RebalanceManager;
import com.pbalancer.client.model.rebalance.TempCash;
import com.pbalancer.client.model.rebalance.TransactionSpecific;
import com.pbalancer.client.util.HelpUrls;
import com.pbalancer.client.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class InvestSuggestionsResultsController extends BaseController<PortfolioCashSuggestionsRequest,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(InvestSuggestionsResultsController.class);
    public static final String APP_BAR_TITLE = "Invest Suggestions";

    @FXML
    private Label nameLabel;

    @FXML
    private Button disclaimerBtn;

    @FXML
    private Button tipsBtn;

    @FXML
    private TextArea instructionsTextArea;

    @FXML
    private Label statusLabel;


    public InvestSuggestionsResultsController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(disclaimerBtn);
        Validation.assertNonNull(tipsBtn);
        Validation.assertNonNull(instructionsTextArea);
        Validation.assertNonNull(statusLabel);

        disclaimerBtn.setGraphic(MaterialDesignIcon.INFO_OUTLINE.graphic());
        disclaimerBtn.setOnMouseClicked(e -> visitDisclaimer());

        tipsBtn.setGraphic(MaterialDesignIcon.INFO_OUTLINE.graphic());
        tipsBtn.setOnMouseClicked(e -> visitTips());

        instructionsTextArea.setEditable(false);
    }


    @Override
    protected void doSizing()
    {
        super.doSizing();
        // critical to get proper scrollbar behavior
        getRoot().setMinSize(100, 100);
        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        ((AnchorPane)instructionsTextArea.getParent()).setPrefSize(20, 20);
        instructionsTextArea.setPrefSize(20, 20);
    }

    @Override
    protected void populateData(final PortfolioCashSuggestionsRequest pc)
    {
        Portfolio p = pc.portfolio();
        TempCash tempCash = pc.cash();
        ActualAANode rootAaan = RebalanceManager.toActualAssetAllocation(p);
        List<TransactionSpecific> suggestions = RebalanceManager.toInvestSuggestions(p, rootAaan, tempCash);
        String suggestionText = toString(suggestions);

        nameLabel.setText(p.getName());
        instructionsTextArea.setText( suggestionText );

        long suggCount = suggestions.size();
        statusLabel.setText(String.format("Total suggested transactions: %,d", suggCount));
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        returnSuccess(null);
    }

    private String toString(final List<TransactionSpecific> suggestions)
    {
        // TODO: sort by (account, sell/buy)
        // TODO: add some plain text formatting, separators between accounts
        StringBuilder sb = new StringBuilder();
        for(TransactionSpecific t : suggestions)
        {
            sb.append(t.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


    private void visitDisclaimer()
    {
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.SUGGESTIONS_DISCLAIMER_URL);
    }

    private void visitTips()
    {
        getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.SUGGESTIONS_TIPS_URL);
    }
}
