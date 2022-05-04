package com.msfinance.pbalancer.controllers;

import java.io.IOException;
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
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.NumericTableCell;
import com.msfinance.pbalancer.controllers.cells.PortfolioGoalTableCell;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.PortfolioGoal;
import com.msfinance.pbalancer.model.Profile;
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

    public static final String APP_BAR_TITLE = "Portfolio List";

    @FXML
    private Label nameLabel;

    @FXML
    private Label totalValueLabel;

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


    private boolean positionsDirty;


    public PortfolioListController()
    {
        super(BounceInRightTransition::new);
    }

    @FXML
    void initialize() throws IOException
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
        TableColumn<Portfolio,PortfolioGoal> tCol1 = (TableColumn<Portfolio,PortfolioGoal>) t.getColumns().get(1);
        tCol1.setCellValueFactory(new PropertyValueFactory<>("goal"));
        tCol1.setCellFactory(new PortfolioGoalTableCell.Factory<Portfolio>());
        TableColumn<Portfolio,Number> tCol2 = (TableColumn<Portfolio,Number>) t.getColumns().get(2);
        tCol2.setCellValueFactory(new PropertyValueFactory<>("lastValue"));
        tCol2.setCellFactory(new NumericTableCell.CurrencyFactory<Portfolio>());

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

//        // UGGG: major flaw, this never gets called from drawer, or on first init
//        getRoot().setOnShowing(e -> {
//            call(StateManager.currentProfile, null, null);
//        });
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
        populateTotalValue();

        t.getSelectionModel().clearSelection();
        t.setItems(FXCollections.observableList( items ));
        t.refresh();
        positionsDirty = false;
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
            totalValueLabel.setText("0");
        }
    }

    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().showDrawer()));
        appBar.getActionItems().clear();
// TODO
//        appBar.getActionItems().add(MaterialDesignIcon.ADD_CIRCLE.button(e -> addPortfolio()));
//        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> populateData()));
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
        if(positionsDirty)
        {
            try
            {
                for(Portfolio p : getIn().getPortfolios())
                {
                    DataFactory.get().updatePortfolio(p);
                }
            }
            catch (IOException e)
            {
                LOG.error("Error updating portfolio positions for profile: " + getIn().getId(), e);
                getApp().showMessage("Error updating portfolio positions");
                return false;
            }
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
        // link into hierarchy
        item.setProfile(getIn());
        getIn().getPortfolios().add(item);
        try
        {
            DataFactory.get().createPortfolio(item);
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
                    getIn().getPortfolios().remove(item);
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
        Validation.assertNonNull(item);

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
        Validation.assertNonNull(item);

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
                t.getItems().get(i).setListPosition(i);
            }

            positionsDirty = true;
            save();
        }
    }

    private void onDown()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

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
                t.getItems().get(i).setListPosition(i);
            }

            positionsDirty = true;
            save();
        }
    }

    private void onDelete()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

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
                DataFactory.get().updateProfile(getIn());
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
        Alert alert = new Alert(AlertType.INFORMATION, "This feature may be available to subscribers only.");
        Optional<ButtonType> result = alert.showAndWait();
    }
}
