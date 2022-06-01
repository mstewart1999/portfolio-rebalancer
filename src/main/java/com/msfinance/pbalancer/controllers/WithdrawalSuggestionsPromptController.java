package com.msfinance.pbalancer.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class WithdrawalSuggestionsPromptController extends BaseController<Portfolio,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(WithdrawalSuggestionsPromptController.class);
    public static final String APP_BAR_TITLE = "Withdrawal Suggestions";

    @FXML
    private Label nameLabel;

    @FXML
    private GridPane whichAccountsPane;

    @FXML
    private Button nextBtn;

    @FXML
    private Button cancelBtn;


    public WithdrawalSuggestionsPromptController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(whichAccountsPane);
        Validation.assertNonNull(nextBtn);
        Validation.assertNonNull(cancelBtn);

        ButtonBar.setButtonData(cancelBtn, ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonData(nextBtn, ButtonData.NEXT_FORWARD);
        cancelBtn.setGraphic(MaterialDesignIcon.CANCEL.graphic());
        nextBtn.setGraphic(MaterialDesignIcon.FORWARD.graphic());

        cancelBtn.setOnAction(e -> onCancel());
        nextBtn.setOnAction(e -> onNext());
    }


    @Override
    protected void doSizing()
    {
        super.doSizing();
        // critical to get proper scrollbar behavior
        getRoot().setMinSize(100, 100);
        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        // TODO: sizing of scroll pane?
        //((Pane)whichAccountsPane.getParent()).setPrefSize(20, 20);
        //whichAccountsPane.setPrefSize(20, 20);
    }

    @Override
    protected void populateData(final Portfolio p)
    {
        nameLabel.setText(p.getName());
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

    private void onCancel()
    {
        returnFailure();
    }

    private void onNext()
    {
        if(checkData())
        {
            getApp().<Map<Account,Double>,Void>mySwitchView(App.INVEST_SUGGESTIONS_RESULTS_VIEW, getData(),
                    a -> returnSuccess(a),
                    () -> returnFailure());
        }
    }

    private boolean checkData()
    {
        // TODO Auto-generated method stub
        return false;
    }

    private Map<Account,Double> getData()
    {
        // TODO Auto-generated method stub
        return new HashMap<>();
    }
}
