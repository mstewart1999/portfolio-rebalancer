package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.StateManager;
import com.msfinance.pbalancer.controllers.cells.AAAlertsTreeTableCell;
import com.msfinance.pbalancer.controllers.cells.PercentTableCell;
import com.msfinance.pbalancer.controllers.cells.PredefinedAAListCell;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.aa.AANode;
import com.msfinance.pbalancer.model.aa.AANodeType;
import com.msfinance.pbalancer.model.aa.AssetAllocation;
import com.msfinance.pbalancer.model.aa.AssetClass;
import com.msfinance.pbalancer.model.aa.DoubleExpression;
import com.msfinance.pbalancer.model.aa.PredefinedAA;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class TargetAAController
{
    private static final Logger LOG = LoggerFactory.getLogger(TargetAAController.class);
    public static final String APP_BAR_TITLE = "Target Asset Allocation";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private RadioButton customRB;

    @FXML
    private Icon customHelpIcon;

    @FXML
    private Label customLabel;

    @FXML
    private Button customResetButton;

    @FXML
    private RadioButton predefinedRB;

    @FXML
    private ComboBox<PredefinedAA> predefinedCombo;

    @FXML
    private Icon predefinedHelpIcon;

    @FXML
    private Hyperlink predefinedUrlHref;

    @FXML
    private TreeTableView<AANode> tt;

    @FXML
    private TableView<AANode> t;

    @FXML
    private View view;

    @FXML
    private VBox customizePane;
    @FXML
    private Button addGroupButton;
    @FXML
    private Button addAssetButton;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;
    @FXML
    private Button deleteButton;

    @FXML
    void initialize()
    {
        Validation.assertNonNull(view);
        Validation.assertNonNull(predefinedRB);
        Validation.assertNonNull(customRB);
        Validation.assertNonNull(customHelpIcon);
        Validation.assertNonNull(customLabel);
        Validation.assertNonNull(customResetButton);
        Validation.assertNonNull(predefinedCombo);
        Validation.assertNonNull(predefinedHelpIcon);
        Validation.assertNonNull(predefinedUrlHref);
        Validation.assertNonNull(t);
        Validation.assertNonNull(tt);
        Validation.assertNonNull(customizePane);
        Validation.assertNonNull(addGroupButton);
        Validation.assertNonNull(addAssetButton);
        Validation.assertNonNull(upButton);
        Validation.assertNonNull(downButton);
        Validation.assertNonNull(deleteButton);

        //view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

        ToggleGroup toggleGroup = new ToggleGroup();
        predefinedRB.setToggleGroup(toggleGroup);
        customRB.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener(e -> onAATypeChange());

        predefinedCombo.getItems().setAll(PredefinedAA.values());
        predefinedCombo.setButtonCell(new PredefinedAAListCell());
        predefinedCombo.setCellFactory(new PredefinedAAListCell.Factory());
        predefinedCombo.onActionProperty().set(e -> onPredefinedAASelection());

        predefinedHelpIcon.setOnMouseClicked(e -> visitPredefinedHelp());
        predefinedUrlHref.setOnAction(e -> visitPredefinedUrl());
        customHelpIcon.setOnMouseClicked(e -> visitCustomHelp());
        customResetButton.setOnAction(e -> resetAA());

        tt.getColumns().get(0).setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        TreeTableColumn<AANode,String> ttCol1 = (TreeTableColumn<AANode,String>) tt.getColumns().get(1);
    ttCol1.setCellValueFactory(new TreeItemPropertyValueFactory<>("percentOfParentIndentedAsString"));
    ttCol1.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        TreeTableColumn<AANode,List<PortfolioAlert>> ttCol2 = (TreeTableColumn<AANode,List<PortfolioAlert>>) tt.getColumns().get(2);
        ttCol2.setCellValueFactory(new TreeItemPropertyValueFactory<>("alerts"));
        ttCol2.setCellFactory(new AAAlertsTreeTableCell.Factory());
        tt.setEditable(true);
        ttCol1.setEditable(true);
        ttCol1.setOnEditCommit(e -> onCommitPercentOfParent(e));

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("path"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<AANode,Double> tcol2 = (TableColumn<AANode, Double>) t.getColumns().get(2);
        tcol2.setCellValueFactory(new PropertyValueFactory<>("percentOfRoot"));
        tcol2.setCellFactory(new PercentTableCell.Factory<AANode>());
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("percentOfRootExprAsString"));

        tt.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable)
            {
                onAASelectionChanged();
            }
        });
        tt.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<AANode>>() {
            @Override
            public void changed(final ObservableValue<? extends TreeItem<AANode>> observable, final TreeItem<AANode> oldValue, final TreeItem<AANode> newValue)
            {
                onAASelectionChanged();
            }
        });

        addGroupButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        addAssetButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        upButton.setGraphic(MaterialDesignIcon.ARROW_UPWARD.graphic());
        downButton.setGraphic(MaterialDesignIcon.ARROW_DOWNWARD.graphic());
        deleteButton.setGraphic(MaterialDesignIcon.DELETE_FOREVER.graphic());

        addGroupButton.setOnAction(e -> onAddGroup());
        addAssetButton.setOnAction(e -> onAddAsset());
        upButton.setOnAction(e -> onUp());
        downButton.setOnAction(e -> onDown());
        deleteButton.setOnAction(e -> onDelete());
    }

    private void onAATypeChange()
    {
        // show these only after selection
        predefinedCombo.setVisible(predefinedRB.isSelected());
        predefinedHelpIcon.setVisible(predefinedRB.isSelected());
        predefinedUrlHref.setVisible(predefinedRB.isSelected());

        customHelpIcon.setVisible(customRB.isSelected());
        customLabel.setVisible(customRB.isSelected());
        customResetButton.setVisible(customRB.isSelected());

        predefinedCombo.getSelectionModel().clearSelection();

        if(predefinedRB.isSelected())
        {
            customizePane.setVisible(false);
            tt.setEditable(false);
        }
        if(customRB.isSelected())
        {
            customizePane.setVisible(true);
            tt.setEditable(true);
        }

        onAASelectionChanged();
    }

    private void onPredefinedAASelection()
    {
        if(predefinedRB.isSelected())
        {
            if(predefinedCombo.getSelectionModel().getSelectedItem() != null)
            {
                try
                {
                    PredefinedAA paa = predefinedCombo.getSelectionModel().getSelectedItem();
                    // copy to current state
                    AssetAllocation aa = new AssetAllocation(paa, paa.getAA().getNodeCsvs());
                    StateManager.currentPortfolio.setTargetAA(aa);
                    // repopulate
                    populateData();
                }
                catch (InvalidDataException e)
                {
                    LOG.error("Unable to build PredefinedAA", e);
                    view.getAppManager().showMessage("Unable to build PredefinedAA");
                }
            }
        }
    }

    protected void populateData()
    {
        StateManager.currentAssetAllocation = StateManager.currentPortfolio.getTargetAA();

        PredefinedAA p = StateManager.currentAssetAllocation.getPredefined();
        if(p != null)
        {
            predefinedRB.setSelected(true);
            predefinedCombo.getSelectionModel().select(p);
        }
        else
        {
            customRB.setSelected(true);
        }

        populateNestedView();
        populateFlatView();
    }

    private void populateNestedView()
    {
        tt.setRoot(convertToUI(StateManager.currentAssetAllocation.getRoot()));
        expandAll(tt.getRoot());
        tt.refresh();

        onAASelectionChanged();
    }
    private void populateFlatView()
    {
        t.setItems( FXCollections.observableList( tt.getRoot().getValue().allLeaves() ) );
        t.refresh();
    }


    protected void updateAppBar()
    {
        final AppBar appBar = view.getAppManager().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> goBack()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void goBack()
    {
        if(save())
        {
            view.getAppManager().switchToPreviousView();
        }
    }

    private boolean save()
    {
        if(predefinedRB.isSelected())
        {
            if(predefinedCombo.getSelectionModel().getSelectedItem() != null)
            {
                PredefinedAA paa = predefinedCombo.getSelectionModel().getSelectedItem();
                // create a copy
                try
                {
                    AssetAllocation aa = new AssetAllocation(paa, paa.getAA().getNodeCsvs());
                    StateManager.currentPortfolio.setTargetAA(aa);
                }
                catch (InvalidDataException e)
                {
                    throw new RuntimeException("Error cloning PredefinedAA!", e);
                }
            }
        }
        if(customRB.isSelected())
        {
            try
            {
                AssetAllocation aa = new AssetAllocation(null, convertFromUIToNodeCsvs(tt.getRoot()));
                StateManager.currentPortfolio.setTargetAA(aa);
            }
            catch (InvalidDataException e)
            {
                // TODO: stop "back" operation, figure out how to depict errors on UI
                LOG.error("Invalid target asset allocation edits", e);
                view.getAppManager().showMessage("Invalid target asset allocation edits");
                return false;
            }
        }
        try
        {
            DataFactory.get().updatePortfolio(StateManager.currentPortfolio);
            return true;
        }
        catch (IOException e)
        {
            LOG.error("Error updating: " + StateManager.currentPortfolio.getId(), e);
            view.getAppManager().showMessage("Error updating: " + StateManager.currentPortfolio.getId());
            return false;
        }
    }

    private void visitPredefinedHelp()
    {
        if(predefinedRB.isSelected())
        {
            save();
            StateManager.currentUrl = HelpUrls.TARGET_AA_PREDEFINED_HELP_URL;
            view.getAppManager().switchView(App.WEB_VIEW);
        }
    }

    private void visitCustomHelp()
    {
        if(customRB.isSelected())
        {
            save();
            StateManager.currentUrl = HelpUrls.TARGET_AA_CUSTOM_HELP_URL;
            view.getAppManager().switchView(App.WEB_VIEW);
        }
    }

    private void visitPredefinedUrl()
    {
        if(predefinedRB.isSelected())
        {
            if(predefinedCombo.getSelectionModel().getSelectedItem() != null)
            {
                save();
                // TODO: try to open system browser for this one!  page is too complicated and makes it buggy
                PredefinedAA p = predefinedCombo.getSelectionModel().getSelectedItem();
                StateManager.currentUrl = p.getUrl();
                view.getAppManager().switchView(App.WEB_VIEW);
            }
        }
    }

    private void resetAA()
    {
        if(customRB.isSelected())
        {
            tt.getRoot().getChildren().clear();
            tt.getRoot().getValue().clearChildren();
        }
    }

    private TreeItem<AANode> convertToUI(final AANode data)
    {
        TreeItem<AANode> ui = new TreeItem<>(data);
        for(AANode childData : data.children())
        {
            TreeItem<AANode> childUi = convertToUI(childData);
            ui.getChildren().add(childUi);
        }
        return ui;
    }

    private List<String> convertFromUIToNodeCsvs(final TreeItem<AANode> ui)
    {
        List<String> csvs = new ArrayList<>();
        csvs.add(ui.getValue().toCsvLine());
        for(TreeItem<AANode> childUi : ui.getChildren())
        {
            csvs.addAll(convertFromUIToNodeCsvs(childUi));
        }
        return csvs;
    }

    private void expandAll(final TreeItem<AANode> n)
    {
        n.setExpanded(true);
        for(TreeItem<AANode> c : n.getChildren())
        {
            expandAll(c);
        }
    }


    private void onAASelectionChanged()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();

        boolean hasSelection = (item != null);
        addGroupButton.setDisable(!hasSelection);
        addAssetButton.setDisable(!hasSelection);
        upButton.setDisable(!hasSelection);
        downButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);

        if(hasSelection)
        {
            // NOTE: while editing the tree, some "leaf" nodes are actually AANodeType.G (group)
            // AANode.isLeaf() should know the difference
            if(item.getValue().isLeaf())
            {
                addGroupButton.setDisable(true);
                addAssetButton.setDisable(true);
            }
            if(item.getValue().isRoot())
            {
                upButton.setDisable(true);
                downButton.setDisable(true);
                deleteButton.setDisable(true);
            }
            else
            {
                int numSiblings = item.getParent().getChildren().size()-1;
                if(numSiblings == 0)
                {
                    upButton.setDisable(true);
                    downButton.setDisable(true);
                }
            }
        }
    }

    private void onCommitPercentOfParent(final CellEditEvent<AANode, String> e)
    {
        String expr = e.getNewValue();
        TreeItem<AANode> item = e.getRowValue();
        try
        {
            item.getValue().setPercentOfParentAsString(expr);

            // changing a group node will effect all children
            try
            {
                e.getTreeTableView().getRoot().getValue().validate();
                e.getTreeTableView().refresh();
            }
            catch (InvalidDataException ex)
            {
                // ignore;
            }

            populateFlatView();
        }
        catch (InvalidDataException ex)
        {
            // warn user and discard edit
            view.getAppManager().showMessage("Invalid data");
        }
    }

    private void onAddGroup()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        AANode aan = item.getValue();
        String id = UUID.randomUUID().toString();
        int listPos = 1;
        Optional<Integer> currMaxListPosition = aan
            .children()
            .stream()
            .map(c -> c.getListPosition())
            .max((i, j) -> i.compareTo(j));
        if(currMaxListPosition.isPresent())
        {
            listPos = currMaxListPosition.get() + 1;
        }

        TextInputDialog inputdialog = new TextInputDialog("");
        inputdialog.setContentText("Name: ");
        inputdialog.setHeaderText("Enter a name for the category");
        inputdialog.setTitle("Add Category");
        inputdialog.initOwner(view.getScene().getWindow());
        inputdialog.initModality(Modality.WINDOW_MODAL);
        inputdialog.showAndWait();
        String name = inputdialog.getResult();
        if(!Validation.isBlank(name))
        {
            AANode aanChild = new AANode(aan.getId(), id, name, listPos, DoubleExpression.createSafe0Percent(), AANodeType.G);
            TreeItem<AANode> child = new TreeItem<>(aanChild);
            aan.addChild(aanChild);
            item.getChildren().add(child);

            try
            {
                tt.getRoot().getValue().validate();
                tt.refresh();
            }
            catch (InvalidDataException e)
            {
                // ignore;
            }
        }
    }

    private void onAddAsset()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        AANode aan = item.getValue();
        String id = UUID.randomUUID().toString();
        int listPos = 1;
        Optional<Integer> currMaxListPosition = aan
            .children()
            .stream()
            .map(c -> c.getListPosition())
            .max((i, j) -> i.compareTo(j));
        if(currMaxListPosition.isPresent())
        {
            listPos = currMaxListPosition.get() + 1;
        }

        List<String> choices = AssetClass.list().stream()
                .map(ac -> ac.getCode())
                .collect(Collectors.toList());
        ChoiceDialog<String> inputdialog = new ChoiceDialog<>("", choices);
        inputdialog.setContentText("Name: ");
        inputdialog.setHeaderText("Choose the holding (asset class)");
        inputdialog.setTitle("Add Holding");
        inputdialog.initOwner(view.getScene().getWindow());
        inputdialog.initModality(Modality.WINDOW_MODAL);
        inputdialog.showAndWait();
        String name = inputdialog.getResult();
        if(!Validation.isBlank(name))
        {
            AANode aanChild = new AANode(aan.getId(), id, name, listPos, DoubleExpression.createSafe0Percent(), AANodeType.AC);
            TreeItem<AANode> child = new TreeItem<>(aanChild);
            aan.addChild(aanChild);
            item.getChildren().add(child);

            try
            {
                tt.getRoot().getValue().validate();
                tt.refresh();
            }
            catch (InvalidDataException e)
            {
                // ignore;
            }
        }
    }

    private void onUp()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        TreeItem<AANode> parent = item.getParent();
        Validation.assertNonNull(item);

        if(parent == null)
        {
            // not allowed for root
            return;
        }
        if(parent.getChildren().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<parent.getChildren().size(); i++)
            {
                TreeItem<AANode> curr = parent.getChildren().get(i);
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
            parent.getChildren().remove(item);
            parent.getChildren().add(newPos, item);
            tt.getSelectionModel().select(item); // move selection with the row
            // renumber children
            for(int i=0; i<parent.getChildren().size(); i++)
            {
                parent.getChildren().get(i).getValue().setListPosition(i);
            }
        }
    }

    private void onDown()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        TreeItem<AANode> parent = item.getParent();
        Validation.assertNonNull(item);

        if(parent == null)
        {
            // not allowed for root
            return;
        }
        if(parent.getChildren().size() > 1)
        {
            int pos = 0;
            for(int i=0; i<parent.getChildren().size(); i++)
            {
                TreeItem<AANode> curr = parent.getChildren().get(i);
                if(curr == item)
                {
                    pos = i;
                }
            }
            if(pos == parent.getChildren().size()-1)
            {
                // already at bottom of list
                return;
            }
            int newPos = pos+1;
            parent.getChildren().remove(item);
            parent.getChildren().add(newPos, item);
            tt.getSelectionModel().select(item); // move selection with the row
            // renumber children
            for(int i=0; i<parent.getChildren().size(); i++)
            {
                parent.getChildren().get(i).getValue().setListPosition(i);
            }
        }
    }

    private void onDelete()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        Validation.assertNonNull(item);

        AANode aan = item.getValue();
        if(aan.getParent() != null)
        {
            aan.getParent().removeChild(aan);
            item.getParent().getChildren().remove(item);

            try
            {
                tt.getRoot().getValue().validate();
                tt.refresh();
            }
            catch (InvalidDataException e)
            {
                // ignore;
            }
        }
    }
}
