package com.msfinance.pbalancer.util;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableColumnHeader;

public class FXUtil
{

    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableColumnHeader.class.getDeclaredMethod("resizeColumnToFitContent", int.class);
            columnToFitMethod.setAccessible(true);

            // WARN: in order to get this to run, add the following runtime param:
            // --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED
        } catch (NoSuchMethodException|InaccessibleObjectException e) {
            e.printStackTrace();
        }
    }

    public static <T> void autoFitTable(final TableView<T> t)
    {
        t.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        /*
        t.sceneProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                //System.out.println("sceneProperty invalidated");
                autoFitTableNow(t);
            }
        });
        t.sceneProperty().addListener((obs, o, n) -> {
                //System.out.println("sceneProperty changed");
                autoFitTableNow(t);
            });
        */
        t.itemsProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                //System.out.println("items invalidated");
                autoFitTableNow(t);
            }
        });
        t.itemsProperty().addListener((obs,o,n) -> {
                //System.out.println("items changed");
                autoFitTableNow(t);
            });
    }


    public static <T> void autoFitTable(final TreeTableView<T> t)
    {
        t.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);

        /*
        t.sceneProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                //System.out.println("sceneProperty invalidated");
                autoFitTableNow(t);
            }
        });
        t.sceneProperty().addListener((obs, o, n) -> {
                //System.out.println("sceneProperty changed");
                autoFitTableNow(t);
            });
        */
        t.rootProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                //System.out.println("root invalidated");
                autoFitTableNow(t);
            }
        });
        t.rootProperty().addListener((obs,o,n) -> {
                //System.out.println("root changed");
                autoFitTableNow(t);
            });
    }


    public static <T> void autoFitTableNow(final TableView<T> t)
    {
        //System.out.println("autoFitTableNow(t)");
        Node n = t;
        autoFitTableNow(n, t.getColumns().size());
    }

    public static <T> void autoFitTableNow(final TreeTableView<T> tt)
    {
        //System.out.println("autoFitTableNow(tt)");
        Node n = tt;
        autoFitTableNow(n, tt.getColumns().size());
    }

    private static void autoFitTableNow(final Node n, final int expectedCols)
    {
        Set<Node> columnHeaders = n.lookupAll(".column-header");
        if((columnHeaders.size() == 0) && (expectedCols > 0))
        {
            //System.out.println("...later");
            Platform.runLater(() -> autoFitTableNow(n, expectedCols));
            return;
        }
        //System.out.println("Found cols=" + columnHeaders.size() + "; expected=" + expectedCols); // typically 1 more than expected
        for (Node columnHeader : columnHeaders)
        {
            if(columnHeader instanceof TableColumnHeader tch)
            {
                if(tch.getTableColumn() != null)
                {
                    try {
                        // violate module integrity with reflection :(
                        columnToFitMethod.invoke(tch, -1);
                        //System.out.println("  resized");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //System.out.println("  No column");
                }
            }
        }
    }


    public static <T> void tableHeaderTooltip(final TableView<T> t, final int colNbr, final String text)
    {
        Node n = t;
        tableHeaderTooltip(n, t.getColumns().get(colNbr), t.getColumns().size(), colNbr, text);
    }

    public static <T> void tableHeaderTooltip(final TreeTableView<T> tt, final int colNbr, final String text)
    {
        Node n = tt;
        tableHeaderTooltip(n, tt.getColumns().get(colNbr), tt.getColumns().size(), colNbr, text);
    }

    public static void tableHeaderTooltip(final Node n, final TableColumnBase<?,?> desiredColumn, final int expectedCols, final int colNbr, final String text)
    {
        Set<Node> columnHeaders = n.lookupAll(".column-header");
        boolean found = false;
        for (Node columnHeader : columnHeaders)
        {
            if(columnHeader instanceof TableColumnHeader tch)
            {
                if(tch.getTableColumn() == desiredColumn)
                {
                    // Get column header's (untooltipped) label
                    Label label = (Label) tch.lookup(".label");
                    if(label != null)
                    {
                        // Give the label a tooltip
                        label.setTooltip(new Tooltip(text));

                        // Makes the tooltip display, no matter where the mouse is inside the column header.
                        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                        found = true;
                    }
                }
                else
                {
                    //System.out.println("  No column");
                }
            }
        }
        if(!found)
        {
            Platform.runLater(() -> tableHeaderTooltip(n, desiredColumn, expectedCols, colNbr, text));
            return;
        }
    }
}
