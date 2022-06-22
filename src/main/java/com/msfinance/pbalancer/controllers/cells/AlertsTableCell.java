package com.msfinance.pbalancer.controllers.cells;

import static com.msfinance.pbalancer.model.PortfolioAlert.Level.ERROR;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.INFO;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.WARN;

import java.util.Collections;
import java.util.List;

import com.msfinance.pbalancer.model.PortfolioAlert;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class AlertsTableCell<T> extends TableCell<T,List<PortfolioAlert>>
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

    public static class Factory<T> implements Callback<TableColumn<T,List<PortfolioAlert>>, TableCell<T,List<PortfolioAlert>>>
    {
        @Override
        public TableCell<T,List<PortfolioAlert>> call(final TableColumn<T,List<PortfolioAlert>> col)
        {
            return new AlertsTableCell<T>();
        }
    }
}