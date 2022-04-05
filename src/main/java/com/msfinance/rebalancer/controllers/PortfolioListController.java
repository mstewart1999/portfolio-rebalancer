package com.msfinance.rebalancer.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.rebalancer.App;
import com.msfinance.rebalancer.StateManager;
import com.msfinance.rebalancer.model.Portfolio;
import com.msfinance.rebalancer.model.aa.AANode;
import com.msfinance.rebalancer.model.aa.AssetAllocation;
import com.msfinance.rebalancer.model.aa.PresetAA;
import com.msfinance.rebalancer.service.DataFactory;
import com.msfinance.rebalancer.util.Validation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    private Button newButton;

    @FXML
    private ListView<Portfolio> list;

    @FXML
    private VBox noPortfolios;


    @FXML
    void initialize() throws IOException
    {
        // indicates fxml naming mismatch
        Validation.assertNonNull(view);
        Validation.assertNonNull(list);
        Validation.assertNonNull(newButton);

        view.setShowTransitionFactory(BounceInRightTransition::new);
        view.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));

        view.setOnShowing(e -> {
            populateData();
            updateAppBar();
        });

        newButton.setOnAction(e -> {
            newPortfolio();
            newW();
        });
        newButton.setGraphic(MaterialDesignIcon.ADD_CIRCLE.graphic());

        list.getSelectionModel().selectedItemProperty().addListener(e -> {
            Portfolio p = list.getSelectionModel().getSelectedItem();
            if(p != null)
            {
                StateManager.currentPortfolioId = p.getId(); // TODO: global state is ugly
                view.getApplication().switchView(App.PORTFOLIO_VIEW, ViewStackPolicy.USE);
            }
        });

        list.setCellFactory(new Callback<ListView<Portfolio>, ListCell<Portfolio>>() {
            @Override
            public ListCell<Portfolio> call(final ListView<Portfolio> l) {
                return new PortfolioListCell();
            }
        });
    }

    //------------------------------------
    protected void populateData()
    {
        try
        {
            List<String> ids = DataFactory.get().getPortfolioIds();
            List<Portfolio> items = new ArrayList<>();
            for(String id : ids)
            {
                // TODO: optimize - inefficient for remote calls
                items.add(DataFactory.get().getPortfolio(id));
            }
            items.sort(Comparator.comparing(Portfolio::getName));

            list.getSelectionModel().clearSelection();
            list.setItems(FXCollections.observableList( items ));

            if(items.isEmpty())
            {
                list.setVisible(false);
                noPortfolios.setVisible(true);
            }
            else
            {
                list.setVisible(true);
                noPortfolios.setVisible(false);
            }
        }
        catch (IOException e)
        {
            LOG.error("Error loading portfolios", e);
            view.getApplication().showMessage("Error loading portfolios");
        }
    }

    protected void updateAppBar()
    {
        final AppBar appBar = view.getApplication().getAppBar();
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> view.getApplication().getDrawer().open()));
        appBar.getActionItems().clear();
        appBar.getActionItems().add(MaterialDesignIcon.ADD_CIRCLE.button(e -> newPortfolio()));
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> populateData()));
        appBar.setTitleText(APP_BAR_TITLE);
    }


    private void newPortfolio()
    {
        System.out.println("new");
        Portfolio p = new Portfolio();
        try
        {
            DataFactory.get().createPortfolio(p);
            StateManager.currentPortfolioId = p.getId();
            view.getApplication().switchView(App.PORTFOLIO_VIEW, ViewStackPolicy.USE);
        }
        catch (IOException e)
        {
            LOG.error("Error creating portfolio: " + p.getId(), e);
            view.getApplication().showMessage("Error creating portfolio: " + p.getId());
        }
    }

    private static class PortfolioListCell extends ListCell<Portfolio>
    {
        @Override
        protected void updateItem(final Portfolio p, final boolean empty)
        {
            super.updateItem(p, empty);
            if(p != null)
            {
                setText(p.getName() + "  [" + p.getGoal().name() + "]");
            }
            else
            {
                setText("");
            }
        }
    }

    // TODO: migrate all of this experimental junk to new view
    private void newW()
    {
        try
        {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("../views/main.fxml"));

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("../views/main.css").toExternalForm());

            stage.setTitle("FXML Welcome");
            stage.setScene(scene);
            stage.show();

            AssetAllocation aa = PresetAA.SEVEN_TWELVE.getAA();
            ComboBox<String> targetAANameCombo = (ComboBox<String>) scene.lookup("#targetAANameCombo");
            for(PresetAA paa : PresetAA.values())
            {
                targetAANameCombo.getItems().add(paa.getAA().getName());
            }
            targetAANameCombo.getItems().add("Custom");
            targetAANameCombo.getSelectionModel().select(aa.getName());
            Button targetAAURLButton = (Button) scene.lookup("#targetAAURLButton");
            targetAAURLButton.setOnAction(e -> System.out.println(aa.getUrl()));
            targetAAURLButton.getTooltip().setText(aa.getUrl());

            TreeTableView<AANode> targetAATreeTable = (TreeTableView<AANode>) scene.lookup("#targetAATreeTable");
            targetAATreeTable.setRoot(convertToUI(aa.getRoot()));

            targetAATreeTable.getColumns().get(0).setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            targetAATreeTable.getColumns().get(1).setCellValueFactory(new TreeItemPropertyValueFactory<>("percentOfParentAsString"));
            targetAATreeTable.getColumns().get(2).setCellValueFactory(new TreeItemPropertyValueFactory<>("percentOfRootAsString"));

//            targetAATreeTable.getColumns().get(0).setCellFactory(new Callback<TreeTableColumn<AANode, Object>, TreeTableCell<AANode, Object>>() {
//
//                @Override
//                public TreeTableCell<AANode, Object> call(final TreeTableColumn<AANode, Object> param)
//                {
//                    TreeTableCell<Object, String> cell = new TreeTableCell<Object, String>() {
//                        private final ColorPicker colorPicker = new ColorPicker();
//                        @Override
//                        protected void updateItem(final String t, final boolean bln)
//                        {
//                            super.updateItem(t, bln);
//                            setGraphic(colorPicker);
//                        }
//                    };
//                    return cell;
//                }
//
//            });
//            // targetAATreeTable.getRoot() );
        }
        catch (IOException e)
        {
            LOG.error("Error loading main.fxml", e);
            view.getApplication().showMessage("Error loading main.fxml");
        }
    }


    public TreeItem<AANode> convertToUI(final AANode data)
    {
        TreeItem<AANode> ui = new TreeItem<>(data, getGraphic(data));
        for(AANode childData : data.children())
        {
            TreeItem<AANode> childUi = convertToUI(childData);
            ui.getChildren().add(childUi);
        }
        return ui;
    }


    private final Map<String,Image> NODE_IMAGES = new HashMap<>();
    private synchronized Image loadImage(final String name) throws InterruptedException
    {
        Image img = NODE_IMAGES.get(name);
        if(img == null)
        {
            img = new Image(getClass().getResource(name).toString());
            NODE_IMAGES.put(name, img);
            while(img.getProgress() < 1.0)
            {
                img.wait(1L); // TODO: this didn't help with missing images
            }
            if(img.errorProperty().getValue() || img.isBackgroundLoading())
            {
                System.out.println("error or loading");
            }
        }
        return img;
    }
    private Node getGraphic(final AANode data)
    {
        try
        {
            if(data.isRoot())
            {
                return new ImageView(loadImage("../views/root-image.png"));
            }
            if(data.isLeaf())
            {
                return new ImageView(loadImage("../views/leaf-image.png"));
            }

            return new ImageView(loadImage("../views/node-image.png"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load AATreeItem image", e);
        }
    }
}
