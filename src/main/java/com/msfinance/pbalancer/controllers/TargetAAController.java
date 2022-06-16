package com.msfinance.pbalancer.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.controllers.cells.AlertsTreeTableCell;
import com.msfinance.pbalancer.controllers.cells.AssetClassListCell;
import com.msfinance.pbalancer.controllers.cells.AssetClassTableCell;
import com.msfinance.pbalancer.controllers.cells.AssetClassTreeTableCell;
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
import com.msfinance.pbalancer.util.FXUtil;
import com.msfinance.pbalancer.util.HelpUrls;
import com.msfinance.pbalancer.util.Validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
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

public class TargetAAController extends BaseController<AssetAllocation,AssetAllocation>
{
    private static final Logger LOG = LoggerFactory.getLogger(TargetAAController.class);
    public static final String APP_BAR_TITLE = "Target Asset Allocation";

    @FXML
    private RadioButton customRB;

    @FXML
    private Icon customHelpIcon;

    @FXML
    private Label customLabel;

    @FXML
    private RadioButton predefinedRB;

    @FXML
    private ComboBox<PredefinedAA> predefinedCombo;

    @FXML
    private Icon predefinedHelpIcon;

    @FXML
    private Hyperlink predefinedUrlHref;

    @FXML
    private TabPane tabs;

    @FXML
    private TreeTableView<AANode> tt;

    @FXML
    private TableView<AANode> t;

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
    private Button resetButton;


    private AssetAllocation working;


    public TargetAAController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(predefinedRB);
        Validation.assertNonNull(customRB);
        Validation.assertNonNull(customHelpIcon);
        Validation.assertNonNull(customLabel);
        Validation.assertNonNull(predefinedCombo);
        Validation.assertNonNull(predefinedHelpIcon);
        Validation.assertNonNull(predefinedUrlHref);
        Validation.assertNonNull(tabs);
        Validation.assertNonNull(t);
        Validation.assertNonNull(tt);
        Validation.assertNonNull(customizePane);
        Validation.assertNonNull(addGroupButton);
        Validation.assertNonNull(addAssetButton);
        Validation.assertNonNull(upButton);
        Validation.assertNonNull(downButton);
        Validation.assertNonNull(deleteButton);
        Validation.assertNonNull(resetButton);

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

        tt.getColumns().get(0).setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        tt.getColumns().get(1).setCellValueFactory(new TreeItemPropertyValueFactory<>("percentOfParentIndentedAsString"));
        tt.getColumns().get(2).setCellValueFactory(new TreeItemPropertyValueFactory<>("alerts"));
        TreeTableColumn<AANode,String> ttCol0 = (TreeTableColumn<AANode,String>) tt.getColumns().get(0);
        ttCol0.setCellFactory(new AssetClassTreeTableCell.Factory<AANode>());
        TreeTableColumn<AANode,String> ttCol1 = (TreeTableColumn<AANode,String>) tt.getColumns().get(1);
        ttCol1.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        TreeTableColumn<AANode,List<PortfolioAlert>> ttCol2 = (TreeTableColumn<AANode,List<PortfolioAlert>>) tt.getColumns().get(2);
        ttCol2.setCellFactory(new AlertsTreeTableCell.Factory<AANode>());
        tt.setEditable(true);
        ttCol1.setEditable(true);
        ttCol1.setOnEditCommit(e -> onCommitPercentOfParent(e));

        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("path"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("percentOfRoot"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("percentOfRootExprAsString"));
        TableColumn<AANode,String> tcol1 = (TableColumn<AANode, String>) t.getColumns().get(1);
        tcol1.setCellFactory(new AssetClassTableCell.Factory<AANode>());
        TableColumn<AANode,Double> tcol2 = (TableColumn<AANode, Double>) t.getColumns().get(2);
        tcol2.setCellFactory(new PercentTableCell.Factory<AANode>());

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

        t.setOnSort(e -> t.refresh()); // this gets styling to reflect properly after a sort

        FXUtil.autoFitTable(tt);
        FXUtil.autoFitTable(t);

        FXUtil.tableHeaderTooltip(tt, 0, "Name of the category or holding (asset class) - hover for description");
        FXUtil.tableHeaderTooltip(tt, 1, "Double click to edit");
        FXUtil.tableHeaderTooltip(tt, 2, "Any issues discovered");

        FXUtil.tableHeaderTooltip(t, 0, "Full category path for this holding");
        FXUtil.tableHeaderTooltip(t, 1, "Name of the holding (asset class) - hover for description");
        //FXUtil.tableHeaderTooltip(t, 2, "");
        FXUtil.tableHeaderTooltip(t, 3, "How the % of portfolio was calculated");


        addGroupButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        addAssetButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        upButton.setGraphic(MaterialDesignIcon.ARROW_UPWARD.graphic());
        downButton.setGraphic(MaterialDesignIcon.ARROW_DOWNWARD.graphic());
        deleteButton.setGraphic(MaterialDesignIcon.DELETE_FOREVER.graphic());
        resetButton.setGraphic(MaterialDesignIcon.DELETE_SWEEP.graphic());


        addGroupButton.setOnAction(e -> onAddGroup());
        addAssetButton.setOnAction(e -> onAddAsset());
        upButton.setOnAction(e -> onUp());
        downButton.setOnAction(e -> onDown());
        deleteButton.setOnAction(e -> onDelete());
        resetButton.setOnAction(e -> resetAA());
    }

    private void onAATypeChange()
    {
        if(predefinedRB.isSelected() && (working.getPredefined() == null))
        {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to leave your customized asset allocation?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() != ButtonType.OK)
            {
                // bail - return to custom
                customRB.setSelected(true);
                return;
            }
        }
        if(customRB.isSelected())
        {
            working.setPredefined(null); // after this change, it is no longer a predefined AA
        }

        // show these only after selection
        predefinedCombo.setVisible(predefinedRB.isSelected());
        predefinedHelpIcon.setVisible(predefinedRB.isSelected());
        predefinedUrlHref.setVisible(predefinedRB.isSelected());

        customHelpIcon.setVisible(customRB.isSelected());
        customLabel.setVisible(customRB.isSelected());

        predefinedCombo.getSelectionModel().clearSelection();

        if(predefinedRB.isSelected())
        {
            customizePane.setDisable(true);
            tt.setEditable(false);
        }
        if(customRB.isSelected())
        {
            customizePane.setDisable(false);
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
                    // copy to current state, repopulate gui
                    populateData( new AssetAllocation(paa, paa.getAA().getNodeCsvs()) );
                }
                catch (InvalidDataException e)
                {
                    LOG.error("Unable to build PredefinedAA", e);
                    getApp().showMessage("Unable to build PredefinedAA");
                }
            }
        }
    }

    @Override
    protected void populateData(final AssetAllocation aa)
    {
        working = aa;
        PredefinedAA p = working.getPredefined();
        if(p != null)
        {
            predefinedRB.setSelected(true);
            predefinedCombo.getSelectionModel().select(p);
            customizePane.setDisable(true);
        }
        else
        {
            customRB.setSelected(true);
            customizePane.setDisable(false);
        }

        populateNestedView(working);
        populateFlatView();
    }

    private void populateNestedView(final AssetAllocation aa)
    {
        tt.getSelectionModel().clearSelection();
        tt.setRoot(convertToUI(aa.getRoot()));
        expandAll(tt.getRoot());
        tt.refresh();
        FXUtil.autoFitTableNow(tt);

        onAASelectionChanged();
    }
    private void populateFlatView()
    {
        AANode rootNode = tt.getRoot().getValue(); // get this from tree table for convenience
        SortedList<AANode> sortedList = new SortedList<>(FXCollections.observableArrayList(rootNode.allLeaves()));

        t.getSelectionModel().clearSelection();
        t.setItems( sortedList );
        sortedList.comparatorProperty().bind(t.comparatorProperty());

        t.refresh();
        FXUtil.autoFitTableNow(t);
    }

    @Override
    protected void setFocus()
    {
        super.setFocus();
        //xyzText.requestFocus();
        tabs.getSelectionModel().selectFirst();
        FXUtil.autoFitTableNow(tt);
        FXUtil.autoFitTableNow(t);
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
            returnSuccess(working);
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
                    working = new AssetAllocation(paa, paa.getAA().getNodeCsvs());
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
                working = new AssetAllocation(null, convertFromUIToNodeCsvs(tt.getRoot()));
            }
            catch (InvalidDataException e)
            {
                LOG.error("Invalid target asset allocation edits", e);
                getApp().showMessage("Invalid target asset allocation edits");
                return false;
            }
        }
        return true;
    }

    private void visitPredefinedHelp()
    {
        if(predefinedRB.isSelected())
        {
            getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.TARGET_AA_PREDEFINED_HELP_URL);
        }
    }

    private void visitCustomHelp()
    {
        if(customRB.isSelected())
        {
            getApp().<String,Void>mySwitchView(App.WEB_VIEW, HelpUrls.TARGET_AA_CUSTOM_HELP_URL);
        }
    }

    private void visitPredefinedUrl()
    {
        if(predefinedRB.isSelected())
        {
            if(predefinedCombo.getSelectionModel().getSelectedItem() != null)
            {
                // TODO: try to open system browser for this one!  page is too complicated and makes it buggy
                PredefinedAA p = predefinedCombo.getSelectionModel().getSelectedItem();
                getApp().<String,Void>mySwitchView(App.WEB_VIEW, p.getUrl());
            }
        }
    }

    private void resetAA()
    {
        if(customRB.isSelected())
        {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to reset entire asset allocation?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK)
            {
                tt.getRoot().getChildren().clear();
                tt.getRoot().getValue().clearChildren();
            }
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
            validateAndRefresh(e.getTreeTableView());
            populateFlatView();
        }
        catch (InvalidDataException ex)
        {
            // warn user and discard edit
            getApp().showMessage("Invalid data");
        }
    }

    private void onAddGroup()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        if(item == null) return;

        AANode aan = item.getValue();
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
        inputdialog.initOwner(getRoot().getScene().getWindow());
        inputdialog.initModality(Modality.WINDOW_MODAL);
        inputdialog.showAndWait();
        String name = inputdialog.getResult();
        if(!Validation.isBlank(name))
        {
            String id = UUID.randomUUID().toString();
            AANode aanChild = new AANode(aan.getId(), id, name, listPos, DoubleExpression.createSafe0Percent(), AANodeType.G);
            aanChild.setParent(aan);
            aan.addChild(aanChild);

            TreeItem<AANode> child = new TreeItem<>(aanChild);
            item.getChildren().add(child);

            validateAndRefresh(tt);
            populateFlatView();
        }
    }

    private void onAddAsset()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        if(item == null) return;

        AANode aan = item.getValue();
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
                .toList();

        ChoiceDialog<String> inputdialog = new ChoiceDialog<>(AssetClass.UNDEFINED, choices);
        inputdialog.setContentText("Name: ");
        inputdialog.setHeaderText("Choose the holding (asset class - equity, fixed income, other)");
        inputdialog.setTitle("Add Holding");
        {
            // ugly hack to customize the ComboBox
            Parent grid = (Parent) (inputdialog.getDialogPane().getContent());
            for(Node n : grid.getChildrenUnmodifiable())
            {
                if(n instanceof ComboBox cb)
                {
                    cb.setButtonCell(new AssetClassListCell(AssetClass.all()));
                    cb.setCellFactory(new AssetClassListCell.Factory(AssetClass.all()));
                }
            }
        }
        inputdialog.initOwner(getRoot().getScene().getWindow());
        inputdialog.initModality(Modality.WINDOW_MODAL);
        inputdialog.showAndWait();
        String name = inputdialog.getResult();
        if(!Validation.isBlank(name))
        {
            AssetClass.add(name); // in case it is a new custom one
            String id = UUID.randomUUID().toString();
            AANode aanChild = new AANode(aan.getId(), id, name, listPos, DoubleExpression.createSafe0Percent(), AANodeType.AC);
            aanChild.setParent(aan);
            aan.addChild(aanChild);

            TreeItem<AANode> child = new TreeItem<>(aanChild);
            item.getChildren().add(child);

            validateAndRefresh(tt);
            populateFlatView();
        }
    }

    private void onUp()
    {
        TreeItem<AANode> item = tt.getSelectionModel().getSelectedItem();
        if(item == null) return;
        TreeItem<AANode> parent = item.getParent();

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
        if(item == null) return;
        TreeItem<AANode> parent = item.getParent();

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
        if(item == null) return;

        AANode aan = item.getValue();
        if(aan.getParent() != null)
        {
            aan.getParent().removeChild(aan);
            item.getParent().getChildren().remove(item);

            validateAndRefresh(tt);
            populateFlatView();
        }
    }

    private static void validateAndRefresh(final TreeTableView<AANode> tt)
    {
        try
        {
            TreeItem<AANode> root = tt.getRoot();
            root.getValue().validate();
            // trigger column auto-resize
            tt.setRoot(null);
            tt.setRoot(root);

            tt.refresh();
        }
        catch (InvalidDataException ex)
        {
            // ignore;
        }
    }
}
