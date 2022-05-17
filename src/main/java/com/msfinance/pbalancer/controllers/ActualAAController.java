package com.msfinance.pbalancer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.controllers.cells.NumericTableCell;
import com.msfinance.pbalancer.controllers.cells.NumericTreeTableCell;
import com.msfinance.pbalancer.controllers.cells.PercentTableCell;
import com.msfinance.pbalancer.controllers.cells.PercentTreeTableCell;
import com.msfinance.pbalancer.model.Portfolio;
import com.msfinance.pbalancer.model.rebalance.ActualAANode;
import com.msfinance.pbalancer.model.rebalance.RebalanceManager;
import com.msfinance.pbalancer.util.FXUtil;
import com.msfinance.pbalancer.util.Validation;

import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public class ActualAAController extends BaseController<Portfolio,Portfolio>
{
    private static final Logger LOG = LoggerFactory.getLogger(ActualAAController.class);
    public static final String APP_BAR_TITLE = "Actual Asset Allocation";

    @FXML
    private TabPane tabs;

    @FXML
    private TreeTableView<ActualAANode> tt;

    @FXML
    private TableView<ActualAANode> t;



    public ActualAAController()
    {
        super(null);
    }

    @FXML
    void initialize()
    {
        Validation.assertNonNull(tabs);
        Validation.assertNonNull(t);
        Validation.assertNonNull(tt);

        tt.getColumns().get(0).setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        tt.getColumns().get(1).setCellValueFactory(new TreeItemPropertyValueFactory<>("totalValue"));
        tt.getColumns().get(2).setCellValueFactory(new TreeItemPropertyValueFactory<>("targetPercentOfPortfolio"));
        tt.getColumns().get(3).setCellValueFactory(new TreeItemPropertyValueFactory<>("actualPercentOfPortfolio"));
        tt.getColumns().get(4).setCellValueFactory(new TreeItemPropertyValueFactory<>("buyLow"));
        tt.getColumns().get(5).setCellValueFactory(new TreeItemPropertyValueFactory<>("buyHigh"));
        tt.getColumns().get(6).setCellValueFactory(new TreeItemPropertyValueFactory<>("sellLow"));
        tt.getColumns().get(7).setCellValueFactory(new TreeItemPropertyValueFactory<>("sellHigh"));

        TreeTableColumn<ActualAANode,Number> ttColN;
        ttColN = (TreeTableColumn<ActualAANode,Number>) tt.getColumns().get(1);
        ttColN.setCellFactory(new NumericTreeTableCell.CurrencyFactory());

        TreeTableColumn<ActualAANode,Double> ttColP;
        ttColP = (TreeTableColumn<ActualAANode,Double>) tt.getColumns().get(2);
        ttColP.setCellFactory(new PercentTreeTableCell.Factory());
        ttColP = (TreeTableColumn<ActualAANode,Double>) tt.getColumns().get(3);
        ttColP.setCellFactory(new PercentTreeTableCell.Factory());

        ttColN = (TreeTableColumn<ActualAANode,Number>) tt.getColumns().get(4);
        ttColN.setCellFactory(new NumericTreeTableCell.ColoredCurrencyFactory());
        ttColN = (TreeTableColumn<ActualAANode,Number>) tt.getColumns().get(5);
        ttColN.setCellFactory(new NumericTreeTableCell.ColoredCurrencyFactory());
        ttColN = (TreeTableColumn<ActualAANode,Number>) tt.getColumns().get(6);
        ttColN.setCellFactory(new NumericTreeTableCell.ColoredCurrencyFactory());
        ttColN = (TreeTableColumn<ActualAANode,Number>) tt.getColumns().get(7);
        ttColN.setCellFactory(new NumericTreeTableCell.ColoredCurrencyFactory());


        t.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("path"));
        t.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        t.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        t.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("targetPercentOfPortfolio"));
        t.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("actualPercentOfPortfolio"));
        t.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("buyLow"));
        t.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("buyHigh"));
        t.getColumns().get(7).setCellValueFactory(new PropertyValueFactory<>("sellLow"));
        t.getColumns().get(8).setCellValueFactory(new PropertyValueFactory<>("sellHigh"));

        TableColumn<ActualAANode,Number> tcolN;
        tcolN = (TableColumn<ActualAANode, Number>) t.getColumns().get(2);
        tcolN.setCellFactory(new NumericTableCell.CurrencyFactory<ActualAANode>());

        TableColumn<ActualAANode,Double> tcolP;
        tcolP = (TableColumn<ActualAANode, Double>) t.getColumns().get(3);
        tcolP.setCellFactory(new PercentTableCell.Factory<ActualAANode>());
        tcolP = (TableColumn<ActualAANode, Double>) t.getColumns().get(4);
        tcolP.setCellFactory(new PercentTableCell.Factory<ActualAANode>());

        tcolN = (TableColumn<ActualAANode, Number>) t.getColumns().get(5);
        tcolN.setCellFactory(new NumericTableCell.ColoredCurrencyFactory<ActualAANode>());
        tcolN = (TableColumn<ActualAANode, Number>) t.getColumns().get(6);
        tcolN.setCellFactory(new NumericTableCell.ColoredCurrencyFactory<ActualAANode>());
        tcolN = (TableColumn<ActualAANode, Number>) t.getColumns().get(7);
        tcolN.setCellFactory(new NumericTableCell.ColoredCurrencyFactory<ActualAANode>());
        tcolN = (TableColumn<ActualAANode, Number>) t.getColumns().get(8);
        tcolN.setCellFactory(new NumericTableCell.ColoredCurrencyFactory<ActualAANode>());

        t.setOnSort(e -> t.refresh()); // this gets styling to reflect properly after a sort

        FXUtil.autoFitTable(tt);
        FXUtil.autoFitTable(t);
    }


    @Override
    protected void populateData(final Portfolio p)
    {
        ActualAANode rootAaan = RebalanceManager.toActualAssetAllocation(p);
        System.out.printf("[[-%n");
        RebalanceManager.dumpToConsole(rootAaan);
        System.out.printf("-]]%n");

        LOG.info("here");

        populateNestedView(rootAaan);
        populateFlatView();
    }

    private void populateNestedView(final ActualAANode rootAaan)
    {
        tt.getSelectionModel().clearSelection();
        tt.setRoot(convertToUI(rootAaan));
        expandAll(tt.getRoot());
        tt.refresh();
    }
    private void populateFlatView()
    {
        ActualAANode rootNode = tt.getRoot().getValue(); // get this from tree table for convenience
        SortedList<ActualAANode> sortedList = new SortedList<>(FXCollections.observableArrayList(rootNode.allLeaves()));

        t.getSelectionModel().clearSelection();
        t.setItems( sortedList );
        sortedList.comparatorProperty().bind(t.comparatorProperty());

        t.refresh();
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
            returnSuccess(getIn());
        }
    }

    private boolean save()
    {
        // no editing allowed on this screen
        return true;
    }


    private TreeItem<ActualAANode> convertToUI(final ActualAANode data)
    {
        TreeItem<ActualAANode> ui = new TreeItem<>(data);
        for(ActualAANode childData : data.getChildren())
        {
            TreeItem<ActualAANode> childUi = convertToUI(childData);
            ui.getChildren().add(childUi);
        }
        return ui;
    }

    private void expandAll(final TreeItem<ActualAANode> n)
    {
        n.setExpanded(true);
        for(TreeItem<ActualAANode> c : n.getChildren())
        {
            expandAll(c);
        }
    }

}
