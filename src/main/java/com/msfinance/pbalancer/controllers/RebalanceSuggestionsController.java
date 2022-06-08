package com.msfinance.pbalancer.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.rebalance.RebalanceManager;
import com.msfinance.pbalancer.model.rebalance.TransactionSpecific;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class RebalanceSuggestionsController extends BaseController<Portfolio,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(RebalanceSuggestionsController.class);
    public static final String APP_BAR_TITLE = "Rebalancing Suggestions";

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


    public RebalanceSuggestionsController()
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
    protected void populateData(final Portfolio p)
    {
        List<TransactionSpecific> suggestions = RebalanceManager.toRebalanceSuggestions(p);
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
        if(suggestions.size() == 0)
        {
            return "No rebalancing suggested at this time";
        }

        Collections.sort(suggestions, new TransactionComparatorByAccount());

        // TODO: add some plain text formatting, separators between accounts
        Account prevAccount = null;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<suggestions.size(); i++)
        {
            TransactionSpecific curr = suggestions.get(i);
            TransactionSpecific next = ((i+1) == suggestions.size()) ? null : suggestions.get(i+1);
            sb.append(curr.toString());
            sb.append("\n");

            if((next != null) && !curr.where().getId().equals(next.where().getId()))
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    private static class TransactionComparatorByAccount implements Comparator<TransactionSpecific>
    {
        @Override
        public int compare(final TransactionSpecific o1, final TransactionSpecific o2)
        {
            if(!o1.where().getId().equals(o2.where().getId()))
            {
                // primary sort by account - using user's preferred order, not alphabetic
                return Integer.compare(o1.where().getListPosition(), o2.where().getListPosition());
            }
            // secondary sort by dollar amount (sells first, then buys)
            return Double.compare(o1.howMuchDollars(), o2.howMuchDollars());
        }
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
