package com.msfinance.pbalancer.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.rebalance.ActualAANode;
import com.msfinance.pbalancer.model.rebalance.RebalanceManager;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class RebalanceSuggestionsController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(RebalanceSuggestionsController.class);
    public static final String APP_BAR_TITLE = "Rebalancing Suggestions";

    @FXML
    private Label nameLabel;

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
        Validation.assertNonNull(instructionsTextArea);
        Validation.assertNonNull(statusLabel);

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
        ActualAANode rootAaan = RebalanceManager.toActualAssetAllocation(p);
        List<String> suggestions = RebalanceManager.toRebalanceSuggestions(p, rootAaan);
        String suggestionText = String.join("\n", suggestions);

        nameLabel.setText(p.getName());
        instructionsTextArea.setText( suggestionText );

        statusLabel.setText(String.format("Total suggested transactions: %,d", suggestions.size()));
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
        returnSuccess(getIn());
    }

    private String toSuggestions(final ActualAANode rootAaan)
    {
        return "";
    }

}
