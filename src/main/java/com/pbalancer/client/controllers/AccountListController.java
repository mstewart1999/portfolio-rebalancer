package com.pbalancer.client.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.pbalancer.client.App;
import com.pbalancer.client.PersistManager;
import com.pbalancer.client.StateManager;
import com.pbalancer.client.controllers.cells.AccountTypeTableCell;
import com.pbalancer.client.controllers.cells.AlertsTableCell;
import com.pbalancer.client.controllers.cells.NumericTableCell;
import com.pbalancer.client.model.Account;
import com.pbalancer.client.model.AccountType;
import com.pbalancer.client.model.Asset;
import com.pbalancer.client.model.Portfolio;
import com.pbalancer.client.model.PortfolioAlert;
import com.pbalancer.client.service.DataFactory;
import com.pbalancer.client.service.ServiceException;
import com.pbalancer.client.util.FXUtil;
import com.pbalancer.client.util.NumberFormatHelper;
import com.pbalancer.client.util.Validation;

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
    private Label valueAsOfLabel;

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

    @FXML
    private Label statusLabel;


    public AccountListController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(totalValueLabel);
        Validation.assertNonNull(valueAsOfLabel);
        Validation.assertNonNull(t);
        Validation.assertNonNull(addButton);
        Validation.assertNonNull(editButton);
        Validation.assertNonNull(upButton);
        Validation.assertNonNull(downButton);
        Validation.assertNonNull(deleteButton);
        Validation.assertNonNull(refreshButton);
        Validation.assertNonNull(statusLabel);

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("institution"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("lastValue"));
        t.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("alerts"));
        t.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("lastValueTmstpRange"));
        TableColumn<Account,AccountType> tCol2 = (TableColumn<Account,AccountType>) t.getColumns().get(2);
        tCol2.setCellFactory(new AccountTypeTableCell.Factory<Account>());
        TableColumn<Account,Number> tCol3 = (TableColumn<Account,Number>) t.getColumns().get(3);
        tCol3.setCellFactory(new NumericTableCell.CurrencyFactory<Account>());
        TableColumn<Account,List<PortfolioAlert>> tCol4 = (TableColumn<Account,List<PortfolioAlert>>) t.getColumns().get(4);
        tCol4.setCellFactory(new AlertsTableCell.Factory<Account>());

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
        FXUtil.autoFitTable(t);


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

        t.getSelectionModel().clearSelection();
        t.setItems( FXCollections.observableList( p.getAccounts() ) );
        t.refresh();

        populateTotalValue();
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        //xyzText.requestFocus();
        FXUtil.autoFitTableNow(t);
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
        if(p.getLastValueTmstp() != null)
        {
            valueAsOfLabel.setText(
                String.format("(as of %s)", p.getLastValueTmstpRange()));
        }
        else
        {
            valueAsOfLabel.setText("");
        }

        statusLabel.setText(String.format("Total Accounts: %,d", t.getItems().size()));
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

        try
        {
            PersistManager.persistAll(getIn().getProfile());
        }
        catch (IOException e)
        {
            LOG.error("Error updating account positions for portfolio: " + getIn().getId(), e);
            getApp().showMessage("Error updating account positions");
            return false;
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
        int currMaxListPosition = t
                .getItems()
                .stream()
                .map(c -> c.getListPosition())
                .max((i, j) -> i.compareTo(j))
                .orElse(0);
        int listPosition = currMaxListPosition + 1;

        Account item = new Account(getIn().getId());
        item.setName("");
        item.setInstitution("");
        item.setType(AccountType.UNDEFINED);
        item.setListPosition(listPosition);
        item.markDirty();
        // link into hierarchy
        item.setPortfolio(getIn());
        t.getItems().add(item);

        try
        {
            DataFactory.get().createAccount(item);
            item.markClean();
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
                    t.getItems().remove(item);
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
        if(item == null) return;

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
        if(item == null) return;

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
                if(t.getItems().get(i).getListPosition() != i)
                {
                    t.getItems().get(i).setListPosition(i);
                    t.getItems().get(i).markDirty();
                }
            }
            // persist changes on "back"
        }
    }

    private void onDown()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

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
                if(t.getItems().get(i).getListPosition() != i)
                {
                    t.getItems().get(i).setListPosition(i);
                    t.getItems().get(i).markDirty();
                }
            }
            // persist changes on "back"
        }
    }

    private void onDelete()
    {
        Account item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

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
                PersistManager.persistAll(getIn().getProfile());
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
        Portfolio in = getIn();
        try
        {
            Collection<Asset> updated = StateManager.refreshPrices(in);

            t.refresh();
            populateTotalValue();
            getApp().showMessage(String.format("Updated prices for %s assets", updated.size()));

            PersistManager.persistAll(in.getProfile());
        }
        catch (ServiceException|IOException e)
        {
            LOG.error("Error updating assets for portfolio: " + in.getId(), e);
            getApp().showMessage("Error updating assets");
        }
    }
}
