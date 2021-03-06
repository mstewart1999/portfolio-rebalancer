package com.pbalancer.client.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.App;
import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.rebalance.PortfolioCashSuggestionsRequest;
import com.pbalancer.client.model.rebalance.TempCash;
import com.pbalancer.client.util.NumberFormatHelper;
import com.pbalancer.client.util.Validation;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

public class InvestSuggestionsPromptController extends BaseController<Portfolio,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(InvestSuggestionsPromptController.class);
    public static final String APP_BAR_TITLE = "Invest Suggestions";

    @FXML
    private Label nameLabel;

    @FXML
    private GridPane whichAccountsPane;

    @FXML
    private Button nextBtn;

    @FXML
    private Button cancelBtn;


    private List<Account> accounts;
    private final List<TextField> cashTextFields;
    private final List<Label> errorLabels;


    public InvestSuggestionsPromptController()
    {
        super(null);
        accounts = null;
        cashTextFields = new ArrayList<>();
        errorLabels = new ArrayList<>();
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(whichAccountsPane);
        Validation.assertNonNull(nextBtn);
        Validation.assertNonNull(cancelBtn);

        whichAccountsPane.setVgap(4);

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

        whichAccountsPane.getChildren().clear();
        whichAccountsPane.getRowConstraints().clear();
        cashTextFields.clear();
        errorLabels.clear();

        int row = 0;
        accounts = p.getAccounts();
        for(Account a : accounts)
        {
            Label acctNameLabel = new Label(a.getName());
            TextField cashText = new TextField();
            Label errorLabel = new Label();
            acctNameLabel.setPadding(new Insets(8));
            cashText.setPadding(new Insets(8));
            errorLabel.setPadding(new Insets(8));
            cashTextFields.add(cashText);
            errorLabels.add(errorLabel);

            whichAccountsPane.add(acctNameLabel, 0, row);
            whichAccountsPane.add(cashText, 1, row);
            whichAccountsPane.add(errorLabel, 2, row);

            whichAccountsPane.getRowConstraints().add(new RowConstraints(0, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE));
            row++;
        }
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        //appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
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
            PortfolioCashSuggestionsRequest request = new PortfolioCashSuggestionsRequest(getIn(), getData());
            getApp().<PortfolioCashSuggestionsRequest,Void>mySwitchView(App.INVEST_SUGGESTIONS_RESULTS_VIEW, request,
                    a -> returnSuccess(a),
                    () -> returnFailure());
        }
    }

    private boolean checkData()
    {
        int valid = 0;
        int invalid = 0;
        int row = 0;
        for(TextField tf : cashTextFields)
        {
            errorLabels.get(row).setText("");
            String valStr = tf.getText().trim();
            if(!valStr.isBlank())
            {
                BigDecimal valNbr = NumberFormatHelper.parseNumber2(valStr);
                if((valNbr != null) && (valNbr.doubleValue() >= 0.0))
                {
                    if(valNbr.doubleValue() >= 0.01)
                    {
                        // zeros are not invalid, but only positive values really matter
                        valid++;
                    }
                }
                else
                {
                    // user wrote something in the field, but it either isn't
                    // a number, or is negative
                    errorLabels.get(row).setText("Invalid number");
                    invalid++;
                }
            }
            row++;
        }
        if(invalid > 0)
        {
            // error messages are inline
            return false;
        }
        if(valid == 0)
        {
            getApp().showMessage("Enter dollar values in the desired accounts");
            return false;
        }
        return true;
    }

    private TempCash getData()
    {
        List<Account> accounts = getIn().getAccounts();
        TempCash out = new TempCash();
        int row = 0;
        for(TextField tf : cashTextFields)
        {
            Account account = accounts.get(row);
            String valStr = tf.getText().trim();
            if(!valStr.isBlank())
            {
                BigDecimal valNbr = NumberFormatHelper.parseNumber2(valStr);
                if((valNbr != null) && (valNbr.doubleValue() >= 0.0))
                {
                    if(valNbr.doubleValue() >= 0.01)
                    {
                        // zeros are not invalid, but only positive values really matter
                        out.add(account, valNbr.doubleValue());
                    }
                }
                else
                {
                    // ignore, assume this was caught by checkData()
                }
            }
            row++;
        }
        return out;
    }
}
