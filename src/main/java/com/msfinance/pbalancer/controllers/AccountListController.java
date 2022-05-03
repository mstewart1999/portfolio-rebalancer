package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.AccountTypeTableCell;
import com.msfinance.pbalancer.controllers.cells.NumericTableCell;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.AccountType;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class AccountListController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountListController.class);
    public static final String APP_BAR_TITLE = "Account List";

    @FXML
    private Label nameLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private TableView<Account> t;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button upButton;

    @FXML
    private Button downButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;


    private boolean positionsDirty;


    public AccountListController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(totalValueLabel);
        Validation.assertNonNull(t);
        Validation.assertNonNull(addButton);
        Validation.assertNonNull(editButton);
        Validation.assertNonNull(upButton);
        Validation.assertNonNull(downButton);
        Validation.assertNonNull(deleteButton);
        Validation.assertNonNull(refreshButton);

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("institution"));
        TableColumn<Account,AccountType> tCol2 = (TableColumn<Account,AccountType>) t.getColumns().get(2);
        tCol2.setCellValueFactory(new PropertyValueFactory<>("type"));
        tCol2.setCellFactory(new AccountTypeTableCell.Factory<Account>());
        TableColumn<Account,Number> tCol3 = (TableColumn<Account,Number>) t.getColumns().get(3);
        tCol3.setCellValueFactory(new PropertyValueFactory<>("lastValue"));
        tCol3.setCellFactory(new NumericTableCell.CurrencyFactory<Account>());

        t.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onSelectionChanged();
            }
        });
        t.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Account>() {
            @Override
            public void changed(final ObservableValue<? extends Account> observable, final Account oldValue, final Account newValue)
            {
                onSelectionChanged();
            }
        });
        t.setOnMousePressed(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2){
                onEdit();
            }
        });


        addButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        editButton.setGraphic(MaterialDesignIcon.EDIT.graphic());
        upButton.setGraphic(MaterialDesignIcon.ARROW_UPWARD.graphic());
        downButton.setGraphic(MaterialDesignIcon.ARROW_DOWNWARD.graphic());
        deleteButton.setGraphic(MaterialDesignIcon.DELETE_FOREVER.graphic());
        refreshButton.setGraphic(MaterialDesignIcon.REFRESH.graphic());

        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        upButton.setOnAction(e -> onUp());
        downButton.setOnAction(e -> onDown());
        deleteButton.setOnAction(e -> onDelete());
        refreshButton.setOnAction(e -> onRefresh());
    }


    @Override
    protected void doSizing()
    {
        super.doSizing();
        // critical to get proper scrollbar behavior
        getRoot().setMinSize(100, 100);
        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        ((AnchorPane)t.getParent()).setPrefSize(20, 20);
        t.setPrefSize(20, 20);
    }

    @Override
    protected void populateData(final Portfolio p)
    {
        nameLabel.setText( p.getName() );
        populateTotalValue();

        t.getSelectionModel().clearSelection();
        t.setItems( FXCollections.observableList( p.getAccounts() ) );
        t.refresh();
        positionsDirty = false;
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
            totalValueLabel.setText("0");
        }
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
        if(save())
        {
            returnSuccess(getIn());
        }
    }

    private boolean save()
    {
        // NOTE: any changes to table data, other than "positions" has already been saved
        // anything else about the portfolio is modified on a different screen

        if(positionsDirty)
        {
            try
            {
                for(Account a : getIn().getAccounts())
                {
                    DataFactory.get().updateAccount(a);
                }
            }
            catch (IOException e)
            {
                LOG.error("Error updating account positions for portfolio: " + getIn().getId(), e);
                getApp().showMessage("Error updating account positions");
                return false;
            }
        }
        return true;
    }

    private void onSelectionChanged()
    {
        Account item = t.getSelectionModel().getSelectedItem();

        boolean hasSelection = (item != null);
        addButton.setDisable(false);
        editButton.setDisable(!hasSelection);
        upButton.setDisable(!hasSelection);
        downButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        refreshButton.setDisable(false);
    }

    private void onAdd()
    {
        int listPosition = 1;
        Optional<Integer> currMaxListPosition = t
            .getItems()
            .stream()
            .map(c -> c.getListPosition())
            .max((i, j) -> i.compareTo(j));
        if(currMaxListPosition.isPresent())
        {
            listPosition = currMaxListPosition.get() + 1;
        }

        Account item = new Account(getIn().getId());
        item.setName("");
        item.setInstitution("");
        item.setType(AccountType.UNDEFINED);
        item.setListPosition(listPosition);
        // link into hierarchy
        item.setPortfolio(getIn());
        getIn().getAccounts().add(item);
        try
        {
            DataFactory.get().createAccount(item);
        }
        catch (IOException e)
        {
            LOG.error("Error creating account: " + item.getId(), e);
            getApp().showMessage("Error creating account");
        }

        getApp().<Account,Account>mySwitchView(App.ACCOUNT_EDIT_VIEW, item,
                a -> {
                    t.refresh();
                    populateTotalValue();
                },
                () -> {
                    // remove it upon cancel
                    getIn().getAccounts().remove(item);
                    t.refresh();

                    try
                    {
                        DataFactory.get().deleteAccount(item);
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error deleting account: " + item.getId(), e);
                        getApp().showMessage("Error deleting account");
                    }
                });
    }

    private void onEdit()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        getApp().<Account,Account>mySwitchView(App.ACCOUNT_EDIT_VIEW, item,
                a -> {
                    t.refresh();
                    populateTotalValue();
                },
                () -> {
                    // no-op
                });
    }

    private void onUp()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Account curr = t.getItems().get(i);
                if(curr == item)
                {
                    pos = i;
                }
            }
            if(pos == 0)
            {
                // already at top of list
                return;
            }
            int newPos = pos-1;
            t.getItems().remove(item);
            t.getItems().add(newPos, item);
            t.getSelectionModel().select(item); // move selection with the row
            // renumber list
            for(int i=0; i<t.getItems().size(); i++)
            {
                t.getItems().get(i).setListPosition(i);
            }

            positionsDirty = true;
        }
    }

    private void onDown()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Account curr = t.getItems().get(i);
                if(curr == item)
                {
                    pos = i;
                }
            }
            if(pos == t.getItems().size()-1)
            {
                // already at bottom of list
                return;
            }
            int newPos = pos+1;
            t.getItems().remove(item);
            t.getItems().add(newPos, item);
            t.getSelectionModel().select(item); // move selection with the row
            // renumber list
            for(int i=0; i<t.getItems().size(); i++)
            {
                t.getItems().get(i).setListPosition(i);
            }

            positionsDirty = true;
        }
    }

    private void onDelete()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this account?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deleteAccount(item);
                getApp().showMessage("Deleted account");
            }
            catch (IOException e)
            {
                LOG.error("Error deleting account: " + item.getId(), e);
                getApp().showMessage("Error deleting account: " + item.getId());
            }

            t.getItems().remove(item);
            t.refresh();

            StateManager.recalculatePortfolioValue(getIn());
            StateManager.recalculateProfileValue(getIn().getProfile());

            populateTotalValue();

            try
            {
                DataFactory.get().updatePortfolio(getIn());
                DataFactory.get().updateProfile(getIn().getProfile());
            }
            catch (IOException e)
            {
                LOG.error("Error updating totals for portfolio: " + getIn().getId(), e);
                getApp().showMessage("Error updating totals for portfolio: " + item.getId());
            }
        }
    }

    private void onRefresh()
    {
        Map<String,BigDecimal> priceUpdates = new HashMap<>();
        for(Account acct : t.getItems())
        {
            for(Asset asset : acct.getAssets())
            {
                priceUpdates.put(asset.getTicker(), BigDecimal.ZERO);
            }
        }
        // TODO: implement pricing API, update data model
        for(Account acct : t.getItems())
        {
            for(Asset asset : acct.getAssets())
            {
                BigDecimal newPrice = priceUpdates.get(asset.getTicker());
                if(newPrice.doubleValue() > 0.0)
                {
                    //asset.setPrice(newPrice);
                }
            }
        }
        // TODO: update totalValueLabel, update portfolio object

        Alert alert = new Alert(AlertType.INFORMATION, "This feature may be available to subscribers only.");
        Optional<ButtonType> result = alert.showAndWait();
        /*
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deletePortfolio(StateManager.currentPortfolioId);
                view.getAppManager().showMessage("Deleted: " + StateManager.currentPortfolioId);
                view.getAppManager().switchToPreviousView();
            }
            catch (IOException e)
            {
                LOG.error("Error deleting: " + StateManager.currentPortfolioId, e);
                view.getAppManager().showMessage("Error deleting: " + StateManager.currentPortfolioId);
            }
        }
        */
    }
}
