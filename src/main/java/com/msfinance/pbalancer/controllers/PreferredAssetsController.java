package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.controllers.cells.AlertsTableCell;
import com.msfinance.pbalancer.controllers.cells.AssetClassTableCell;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.aa.PreferredAsset;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.FXUtil;
import com.msfinance.pbalancer.util.Validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
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

public class PreferredAssetsController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(PreferredAssetsController.class);
    public static final String APP_BAR_TITLE = "Preferred Assets (Asset Class Mapping)";

    @FXML
    private Label nameLabel;

    @FXML
    private TableView<PreferredAsset> t;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label statusLabel;


    public PreferredAssetsController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(t);
        Validation.assertNonNull(addButton);
        Validation.assertNonNull(editButton);
        Validation.assertNonNull(deleteButton);
        Validation.assertNonNull(statusLabel);

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("assetClass"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("primaryAssetTicker"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("primaryAssetName"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("alerts"));
        TableColumn<PreferredAsset,String> tCol0 = (TableColumn<PreferredAsset,String>) t.getColumns().get(0);
        tCol0.setCellFactory(new AssetClassTableCell.Factory<PreferredAsset>());
        TableColumn<PreferredAsset,List<PortfolioAlert>> tCol5 = (TableColumn<PreferredAsset,List<PortfolioAlert>>) t.getColumns().get(3);
        tCol5.setCellFactory(new AlertsTableCell.Factory<PreferredAsset>());

        t.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onSelectionChanged();
            }
        });
        t.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PreferredAsset>() {
            @Override
            public void changed(final ObservableValue<? extends PreferredAsset> observable, final PreferredAsset oldValue, final PreferredAsset newValue)
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
        FXUtil.tableHeaderTooltip(t, 0, "Hover over each cell to see description");


        addButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        editButton.setGraphic(MaterialDesignIcon.EDIT.graphic());
        deleteButton.setGraphic(MaterialDesignIcon.DELETE_FOREVER.graphic());

        addButton.setOnAction(e -> onAdd());
        editButton.setOnAction(e -> onEdit());
        deleteButton.setOnAction(e -> onDelete());
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

        tableRefresh(p.getAssetClassMappings());

        populateStatus();
    }

    private void populateStatus()
    {
        statusLabel.setText(String.format("Total Mappings: %,d", t.getItems().size()));
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
        Portfolio p = getIn();

        try
        {
            boolean saved = PersistManager.persistAll(p.getProfile());
            if(saved)
            {
                getApp().showMessage("Saved asset class mappings");
            }
        }
        catch (IOException e)
        {
            LOG.error("Error updating asset class mappings: " + getIn().getId(), e);
            getApp().showMessage("Error updating asset class mappings");
            return false;
        }
        return true;
    }

    private void onSelectionChanged()
    {
        PreferredAsset item = t.getSelectionModel().getSelectedItem();

        boolean hasSelection = (item != null);
        addButton.setDisable(false);
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
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

        PreferredAsset item = new PreferredAsset(getIn().getId());
        item.setListPosition(listPosition);
        item.markDirty();
        // link into hierarchy
        item.setPortfolio(getIn());
        getIn().getAssetClassMappings().add(item);
        // wait until success to persist - this is different from many screens

        getApp().<PreferredAsset,PreferredAsset>mySwitchView(App.PREFERRED_ASSET_EDIT_VIEW, item,
                acm -> {
                    List<PreferredAsset> created = acm.getPortfolio().validateAssetClassMappings();
                    created.add(acm);

                    tableRefresh(getIn().getAssetClassMappings());
                    populateStatus();
                    try
                    {
                        for(PreferredAsset acm2 : created)
                        {
                            DataFactory.get().createAssetClassMapping(acm2);
                            acm2.markClean();
                        }
                        PersistManager.persistAll(acm.getPortfolio().getProfile());
                        getApp().showMessage("Created Asset class mapping");
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error creating asset class mapping: " + acm.getId(), e);
                        getApp().showMessage("Error creating asset class mapping");
                    }
                },
                () -> {
                    // remove it upon cancel, but it hasn't been persisted yet
                    getIn().getAssetClassMappings().remove(item);
                    tableRefresh(getIn().getAssetClassMappings());
                });
    }

    private void onEdit()
    {
        PreferredAsset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        getApp().<PreferredAsset,PreferredAsset>mySwitchView(App.PREFERRED_ASSET_EDIT_VIEW, item,
                acm -> {
                    List<PreferredAsset> created = acm.getPortfolio().validateAssetClassMappings();

                    t.refresh(); // no add/remove, so regular refresh works
                    populateStatus();
                    try
                    {
                        for(PreferredAsset acm2 : created)
                        {
                            DataFactory.get().createAssetClassMapping(acm2);
                            acm2.markClean();
                        }
                        PersistManager.persistAll(acm.getPortfolio().getProfile());
                        getApp().showMessage("Updated Asset class mapping");
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error updating asset class mapping: " + acm.getId(), e);
                        getApp().showMessage("Error updating asset class mapping");
                    }
                },
                () -> {
                    // no-op
                });
    }


    private void onDelete()
    {
        PreferredAsset item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        // TODO: android book suggests not to do confirmations, but allow "undo"
        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this asset class mapping?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deleteAssetClassMapping(item);
                getApp().showMessage("Deleted asset class mapping");
            }
            catch (IOException e)
            {
                LOG.error("Error deleting asset class mapping: " + item.getId(), e);
                getApp().showMessage("Error deleting asset class mapping: " + item.getId());
            }

            getIn().getAssetClassMappings().remove(item);
            tableRefresh(getIn().getAssetClassMappings());

            populateStatus();
        }
    }

    private void tableRefresh(final List<PreferredAsset> raw)
    {
        // need to totally reset data model because of sorting wrapper
        SortedList<PreferredAsset> sortedList = new SortedList<>(FXCollections.observableArrayList(raw));
        t.getSelectionModel().clearSelection();
        t.setItems( sortedList );
        sortedList.comparatorProperty().bind(t.comparatorProperty());
        t.getSortOrder().clear();
        t.getSelectionModel().clearSelection();
        t.refresh();
    }
}