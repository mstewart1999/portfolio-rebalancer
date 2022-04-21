package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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

public class PortfolioListController
{
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioListController.class);

    public static final String APP_BAR_TITLE = "Portfolio List";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private View view;

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


    @FXML
    void initialize() throws IOException
    {
        // indicates fxml naming mismatch
        Validation.assertNonNull(view);
        Validation.assertNonNull(nameLabel);
        Validation.assertNonNull(totalValueLabel);
        Validation.assertNonNull(t);
        Validation.assertNonNull(addButton);
        Validation.assertNonNull(editButton);
        Validation.assertNonNull(upButton);
        Validation.assertNonNull(downButton);
        Validation.assertNonNull(deleteButton);
        Validation.assertNonNull(refreshButton);

        // critical to get proper scrollbar behavior
        view.setMinSize(100, 100);
        view.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        ((AnchorPane)t.getParent()).setPrefSize(20, 20);
        t.setPrefSize(20, 20);

        view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

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

    protected void populateData()
    {
        try
        {
            List<Portfolio> items = DataFactory.get().getPortfolios(StateManager.currentProfile.getId());
            items.sort(Comparator.comparing(Portfolio::getName));

            nameLabel.setText(StateManager.currentProfile.getName());
            populateTotalValue();

            t.getSelectionModel().clearSelection();
            t.setItems(FXCollections.observableList( items ));

        }
        catch (IOException e)
        {
            LOG.error("Error loading portfolios", e);
            view.getAppManager().showMessage("Error loading portfolios");
        }
    }

    private void populateTotalValue()
    {
        Profile p = StateManager.currentProfile;
        if(p.getLastValue() != null)
        {
            totalValueLabel.setText("$ " + NumberFormatHelper.prettyFormatCurrency(p.getLastValue()));
        }
        else
        {
            totalValueLabel.setText("0");
        }
    }

    protected void updateAppBar()
    {
        final AppBar appBar = view.getAppManager().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> view.getAppManager().getDrawer().open()));
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
        Portfolio p = StateManager.currentPortfolio;
        try
        {
            DataFactory.get().createPortfolio(p);
            StateManager.currentPortfolio = p;
            return true;
        }
        catch (IOException e)
        {
            LOG.error("Error creating portfolio: " + p.getId(), e);
            view.getAppManager().showMessage("Error creating portfolio: " + p.getId());
            return false;
        }
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

        Portfolio item = new Portfolio(StateManager.currentProfile.getId());
        item.setName("");
        item.setListPosition(listPosition);
        t.getItems().add(item);

        StateManager.currentPortfolio = item;
        if(save())
        {
            view.getAppManager().switchView(App.PORTFOLIO_VIEW); // TODO: ViewStackPolicy.USE
        }
    }

    private void onEdit()
    {
        Portfolio item = t.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        StateManager.currentPortfolio = item;
        //save();
        view.getAppManager().switchView(App.PORTFOLIO_VIEW);
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
                view.getAppManager().showMessage("Deleted: " + item.getId());
            }
            catch (IOException e)
            {
                LOG.error("Error deleting: " + StateManager.currentPortfolio.getId(), e);
                view.getAppManager().showMessage("Error deleting: " + item.getId());
            }

            t.getItems().remove(item);

            t.refresh();
            StateManager.recalculateProfileValue();
            populateTotalValue();
        }
    }

    private void onRefresh()
    {
        Alert alert = new Alert(AlertType.INFORMATION, "This feature may be available to subscribers only.");
        Optional<ButtonType> result = alert.showAndWait();
    }
}
