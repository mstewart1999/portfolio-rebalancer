package com.pbalancer.client.controllers.cells;

import static com.pbalancer.client.model.PortfolioAlert.Level.ERROR;
import static com.pbalancer.client.model.PortfolioAlert.Level.INFO;
import static com.pbalancer.client.model.PortfolioAlert.Level.WARN;

import java.util.Collections;
import java.util.List;

import com.pbalancer.client.model.PortfolioAlert;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class AlertsTreeTableCell<T> extends TreeTableCell<T,List<PortfolioAlert>>
{
    @Override
    protected void updateItem(final List<PortfolioAlert> alerts, final boolean empty)
    {
        super.updateItem(alerts, empty);
        if(empty || (alerts == null) || alerts.isEmpty())
        {
            setText("");
            setGraphic(null);
        }
        else
        {
            List<PortfolioAlert> errorAlerts = alerts.stream().filter(a -> a.level() == ERROR).toList();
            List<PortfolioAlert> warnAlerts = alerts.stream().filter(a -> a.level() == WARN).toList();
            List<PortfolioAlert> infoAlerts = alerts.stream().filter(a -> a.level() == INFO).toList();
            List<PortfolioAlert> activeAlerts = Collections.emptyList();

            if(errorAlerts.size() > 0)
            {
                activeAlerts = errorAlerts;
                //setGraphic(MaterialDesignIcon.ERROR.graphic());
                setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/error24.png"))));
            }
            else if(warnAlerts.size() > 0)
            {
                activeAlerts = warnAlerts;
                //setGraphic(MaterialDesignIcon.WARNING.graphic());
                setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/warn24.png"))));
            }
            else if(infoAlerts.size() > 0)
            {
                activeAlerts = infoAlerts;
                //setGraphic(MaterialDesignIcon.INFO.graphic());
                setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/info24.png"))));
            }

            StringBuilder sb = new StringBuilder();
            for(PortfolioAlert a : activeAlerts)
            {
                sb.append(a.text());
                sb.append("\n ");
            }
            if(sb.length() >= 2)
            {
                // delete final '\n '
                sb.deleteCharAt(sb.length()-1);
                sb.deleteCharAt(sb.length()-1);
            }
            setText(sb.toString());
            setWrapText(true);
        }
    }

    public static class Factory<T> implements Callback<TreeTableColumn<T,List<PortfolioAlert>>, TreeTableCell<T,List<PortfolioAlert>>>
    {
        @Override
        public TreeTableCell<T,List<PortfolioAlert>> call(final TreeTableColumn<T,List<PortfolioAlert>> col)
        {
            return new AlertsTreeTableCell<>();
        }
    }
}