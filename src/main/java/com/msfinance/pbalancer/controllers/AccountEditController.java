package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.AccountTypeListCell;
import com.msfinance.pbalancer.controllers.cells.AlertsTableCell;
import com.msfinance.pbalancer.controllers.cells.AssetClassTableCell;
import com.msfinance.pbalancer.controllers.cells.NumericTableCell;
import com.msfinance.pbalancer.model.Account;
import com.msfinance.pbalancer.model.AccountType;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Asset.PricingType;
import com.msfinance.pbalancer.model.Institution;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.FXUtil;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class AccountEditController extends BaseController<Account,Account>
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountEditController.class);
    public static final String APP_BAR_TITLE = "Account Edit";

    @FXML
    private TextField nameText;

    @FXML
    private ComboBox<String> institutionCombo;

    @FXML
    private ComboBox<AccountType> typeCombo;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label valueAsOfLabel;

    @FXML
    private TableView<Asset> t;

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


    public AccountEditController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameText);
        Validation.assertNonNull(institutionCombo);
        Validation.assertNonNull(typeCombo);
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

        institutionCombo.getItems().addAll(Institution.ALL);
        institutionCombo.setEditable(false); // only allow selections from list
        typeCombo.getItems().setAll(AccountType.values());
        typeCombo.setButtonCell(new AccountTypeListCell());
        typeCombo.setCellFactory(new AccountTypeListCell.Factory());
        typeCombo.setEditable(false); // only allow selections from list

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("ticker"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("bestName"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("assetClass"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("units"));
        t.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("bestTotalValue"));
        t.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("alerts"));
        TableColumn<Asset,String> tCol2 = (TableColumn<Asset,String>) t.getColumns().get(2);
        tCol2.setCellFactory(new AssetClassTableCell.Factory<Asset>());
        TableColumn<Asset,Number> tCol3 = (TableColumn<Asset,Number>) t.getColumns().get(3);
        tCol3.setCellFactory(new NumericTableCell.UnitsFactory<Asset>());
        TableColumn<Asset,Number> tCol4 = (TableColumn<Asset,Number>) t.getColumns().get(4);
        tCol4.setCellFactory(new NumericTableCell.CurrencyFactory<Asset>());
        TableColumn<Asset,List<PortfolioAlert>> tCol5 = (TableColumn<Asset,List<PortfolioAlert>>) t.getColumns().get(5);
        tCol5.setCellFactory(new AlertsTableCell.Factory<Asset>());

        t.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onSelectionChanged();
            }
        });
        t.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Asset>() {
            @Override
            public void changed(final ObservableValue<? extends Asset> observable, final Asset oldValue, final Asset newValue)
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
        FXUtil.tableHeaderTooltip(t, 2, "Hover over each cell to see description");


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
    protected void populateData(final Account acct)
    {
        nameText.setText( acct.getName() );
        institutionCombo.getSelectionModel().select( acct.getInstitution() );
        typeCombo.getSelectionModel().select( acct.getType() );

        t.getSelectionModel().clearSelection();
        t.setItems( FXCollections.observableList( acct.getAssets() ) );
        t.refresh();

        populateTotalValue();
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

    private void populateTotalValue()
    {
        Account acct = getIn();
        if(acct.getLastValue() != null)
        {
            totalValueLabel.setText("$ " + NumberFormatHelper.prettyFormatCurrency(acct.getLastValue()));
        }
        else
        {
            totalValueLabel.setText("$ 0");
        }
        if(acct.getLastValueTmstp() != null)
        {
            valueAsOfLabel.setText(
                    String.format(
                        "(as of %s)",
                        DateTimeFormatter.ISO_LOCAL_DATE.format(
                                acct.getLastValueTmstp().toInstant().atZone(ZoneId.systemDefault()))));
        }
        else
        {
            valueAsOfLabel.setText("");
        }

        statusLabel.setText(String.format("Total Assets: %,d", t.getItems().size()));
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
        Account acct = getIn();
        acct.setName( nameText.getText() );
        acct.setInstitution( institutionCombo.getValue() );
        acct.setType( typeCombo.getValue() );
        acct.markDirty();

        try
        {
            // NOTE: any changes to table data, other than "positions" has already been saved
            PersistManager.persistAll(acct.getPortfolio().getProfile());
        }
        catch (IOException e)
        {
            LOG.error("Error updating account: " + getIn().getId(), e);
            getApp().showMessage("Error updating account");
            return false;
        }
        return true;
    }

    private void onSelectionChanged()
    {
        Asset item = t.getSelectionModel().getSelectedItem();

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

        Asset item = new Asset(getIn().getId());
        item.setListPosition(listPosition);
        item.markDirty();
        // link into hierarchy
        item.setAccount(getIn());
        t.getItems().add(item);
        // wait until success to persist - this is different from many screens

        getApp().<Asset,Asset>mySwitchView(App.ASSET_ADD_VIEW, item,
                a -> {
                    a.validate();
                    StateManager.recalculateAccountValue(a.getAccount());
                    StateManager.recalculatePortfolioValue(a.getAccount().getPortfolio());
                    StateManager.recalculateProfileValue(a.getAccount().getPortfolio().getProfile());

                    t.refresh();
                    populateTotalValue();
                    try
                    {
                        DataFactory.get().createAsset(a);
                        a.markClean();
                        PersistManager.persistAll(a.getAccount().getPortfolio().getProfile());
                        getApp().showMessage("Created Asset");
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error creating asset: " + a.getId(), e);
                        getApp().showMessage("Error creating asset");
                    }
                },
                () -> {
                    // remove it upon cancel, but it hasn't been persisted yet
                    t.getItems().remove(item);
                    t.refresh();
                });
    }

    private void onEdit()
    {
        Asset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        String editScreen;

        if(item.getProxy() != null)
        {
            editScreen = App.ASSET_EDIT_PROXY_VIEW;
        }
        else if(item.getPricingType() == PricingType.AUTO_PER_UNIT)
        {
            editScreen = App.ASSET_EDIT_KNOWN_VIEW;
        }
        else
        {
            editScreen = App.ASSET_EDIT_MANUAL_VIEW;
        }

        getApp().<Asset,Asset>mySwitchView(editScreen, item,
                a -> {
                    a.validate();
                    StateManager.recalculateAccountValue(a.getAccount());
                    StateManager.recalculatePortfolioValue(a.getAccount().getPortfolio());
                    StateManager.recalculateProfileValue(a.getAccount().getPortfolio().getProfile());

                    t.refresh();
                    populateTotalValue();
                    try
                    {
                        PersistManager.persistAll(a.getAccount().getPortfolio().getProfile());
                        getApp().showMessage("Updated Asset");
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error updating asset: " + a.getId(), e);
                        getApp().showMessage("Error updating asset");
                    }
                },
                () -> {
                    // no-op
                });
    }

    private void onUp()
    {
        Asset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Asset curr = t.getItems().get(i);
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
        Asset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Asset curr = t.getItems().get(i);
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
        Asset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        // TODO: android book suggests not to do confirmations, but allow "undo"
        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this asset?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deleteAsset(item);
                getApp().showMessage("Deleted asset");
            }
            catch (IOException e)
            {
                LOG.error("Error deleting asset: " + item.getId(), e);
                getApp().showMessage("Error deleting asset: " + item.getId());
            }

            t.getItems().remove(item);
            t.refresh();

            StateManager.recalculateAccountValue(getIn());
            StateManager.recalculatePortfolioValue(getIn().getPortfolio());
            StateManager.recalculateProfileValue(getIn().getPortfolio().getProfile());

            populateTotalValue();

            try
            {
                PersistManager.persistAll(getIn().getPortfolio().getProfile());
            }
            catch (IOException e)
            {
                LOG.error("Error updating totals for account: " + getIn().getId(), e);
                getApp().showMessage("Error updating totals for account: " + item.getId());
            }
        }
    }

    private void onRefresh()
    {
        Account in = getIn();
        Collection<Asset> updated = StateManager.refreshPrices(in);

        t.refresh();
        populateTotalValue();
        getApp().showMessage(String.format("Updated prices for %s assets", updated.size()));

        try
        {
            PersistManager.persistAll(in.getPortfolio().getProfile());
        }
        catch (IOException e)
        {
            LOG.error("Error updating assets for account: " + in.getId(), e);
            getApp().showMessage("Error updating assets");
        }
    }
}
