package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.AlertsTableCell;
import com.msfinance.pbalancer.controllers.cells.NumericTableCell;
import com.msfinance.pbalancer.controllers.cells.PortfolioGoalTableCell;
import com.msfinance.pbalancer.model.Asset;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.PortfolioGoal;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.service.ProfileDataCache;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class PortfolioListController extends BaseController<Profile,Profile>
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioListController.class);

    public static final String APP_BAR_TITLE = "Profile Home (Portfolio List)";

    @FXML
    private Label nameLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label valueAsOfLabel;

    @FXML
    private TableView<Portfolio> t;

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


    public PortfolioListController()
    {
        super(BounceInRightTransition::new);
    }

    @FXML
    void initialize() throws IOException
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
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("goal"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("lastValue"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("accountAlerts"));
        t.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("lastValueTmstpRange"));
        TableColumn<Portfolio,PortfolioGoal> tCol1 = (TableColumn<Portfolio,PortfolioGoal>) t.getColumns().get(1);
        tCol1.setCellFactory(new PortfolioGoalTableCell.Factory<Portfolio>());
        TableColumn<Portfolio,Number> tCol2 = (TableColumn<Portfolio,Number>) t.getColumns().get(2);
        tCol2.setCellFactory(new NumericTableCell.CurrencyFactory<Portfolio>());
        TableColumn<Portfolio,List<PortfolioAlert>> tCol3 = (TableColumn<Portfolio,List<PortfolioAlert>>) t.getColumns().get(3);
        tCol3.setCellFactory(new AlertsTableCell.Factory<Portfolio>());

        t.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onSelectionChanged();
            }
        });
        t.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Portfolio>() {
            @Override
            public void changed(final ObservableValue<? extends Portfolio> observable, final Portfolio oldValue, final Portfolio newValue)
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
    public void initializeApp(final App app, final View root)
    {
        super.initializeApp(app, root);

        // UGGG: major flaw, this never gets called from drawer, or on first init
        getRoot().setOnShowing(e -> {
            call(ProfileDataCache.get().getProfile(), null, null);
        });
        getRoot().setOnHiding(e -> {
            save();
        });
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
    protected void populateData(final Profile profile)
    {
        List<Portfolio> items = profile.getPortfolios();

        nameLabel.setText(profile.getName());

        t.getSelectionModel().clearSelection();
        t.setItems(FXCollections.observableList( items ));
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
        Profile p = getIn();
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

        statusLabel.setText(String.format("Total Portfolios: %,d", t.getItems().size()));
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().showDrawer()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }


    private void onSelectionChanged()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();

        boolean hasSelection = (item != null);
        addButton.setDisable(false);
        editButton.setDisable(!hasSelection);
        upButton.setDisable(!hasSelection);
        downButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        refreshButton.setDisable(false);
    }

    private boolean save()
    {
        // NOTE: any changes to table data, other than "positions" has already been saved
        try
        {
            PersistManager.persistAll(getIn());
            getApp().showMessage("Saved profile");
        }
        catch (IOException e)
        {
            LOG.error("Error updating profile: " + getIn().getId(), e);
            getApp().showMessage("Error updating profile");
            return false;
        }
        return true;
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

        Portfolio item = new Portfolio(getIn().getId());
        item.setName("");
        item.setListPosition(listPosition);
        item.markDirty();
        // link into hierarchy
        item.setProfile(getIn());
        t.getItems().add(item);

        try
        {
            DataFactory.get().createPortfolio(item);
            item.markClean();
        }
        catch (IOException e)
        {
            LOG.error("Error creating portfolio: " + item.getId(), e);
            getApp().showMessage("Error creating portfolio");
        }

        getApp().<Portfolio,Portfolio>mySwitchView(App.PORTFOLIO_VIEW, item,
                p -> {
                    t.refresh();
                    populateTotalValue();
                },
                () -> {
                    // remove it upon cancel
                    t.getItems().remove(item);
                    t.refresh();

                    try
                    {
                        DataFactory.get().deletePortfolio(item);
                    }
                    catch (IOException e)
                    {
                        LOG.error("Error deleting portfolio: " + item.getId(), e);
                        getApp().showMessage("Error deleting portfolio");
                    }
                });
    }

    private void onEdit()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        getApp().<Portfolio,Portfolio>mySwitchView(App.PORTFOLIO_VIEW, item,
                p -> {
                    t.refresh();
                    populateTotalValue();
                },
                () -> {
                    // no-op
                });
    }

    private void onUp()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Portfolio curr = t.getItems().get(i);
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

            // unlike other screens, there is no "back" - so save immediately
            save();
        }
    }

    private void onDown()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        if(t.getItems().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<t.getItems().size(); i++)
            {
                Portfolio curr = t.getItems().get(i);
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

            // unlike other screens, there is no "back" - so save immediately
            save();
        }
    }

    private void onDelete()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        if(item == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this portfolio?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                DataFactory.get().deletePortfolio(item);
                getApp().showMessage("Deleted portfolio");
            }
            catch (IOException e)
            {
                LOG.error("Error deleting portfolio: " + item.getId(), e);
                getApp().showMessage("Error deleting portfolio: " + item.getId());
            }

            t.getItems().remove(item);
            t.refresh();

            StateManager.recalculateProfileValue(getIn());

            populateTotalValue();

            try
            {
                PersistManager.persistAll(getIn());
            }
            catch (IOException e)
            {
                LOG.error("Error updating totals for profile: " + getIn().getId(), e);
                getApp().showMessage("Error updating totals for profile: " + item.getId());
            }
        }
    }

    private void onRefresh()
    {
        Profile in = getIn();
        Collection<Asset> updated = StateManager.refreshPrices(in);

        t.refresh();
        populateTotalValue();
        getApp().showMessage(String.format("Updated prices for %s assets", updated.size()));

        try
        {
            PersistManager.persistAll(in);
        }
        catch (IOException e)
        {
            LOG.error("Error updating assets for profile: " + in.getId(), e);
            getApp().showMessage("Error updating assets");
        }
    }
}
