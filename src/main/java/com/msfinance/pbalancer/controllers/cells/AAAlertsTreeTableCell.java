package com.msfinance.pbalancer.controllers.cells;

import static com.msfinance.pbalancer.model.PortfolioAlert.Level.ERROR;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.INFO;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.WARN;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.aa.AANode;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class AAAlertsTreeTableCell extends TreeTableCell<AANode,List<PortfolioAlert>>
{
    @Override
    protected void updateItem(final List<PortfolioAlert> alerts, final boolean empty)
    {
        if (alerts == getItem() && empty == isEmpty()) return;

        super.updateItem(alerts, empty);
        if((alerts == null) || alerts.isEmpty())
        {
            setText("");
            setGraphic(null);
            this.getStyleClass().clear();
        }
        else
        {
            List<PortfolioAlert> errorAlerts = alerts.stream().filter(a -> a.level() == ERROR).collect(Collectors.toList());
            List<PortfolioAlert> warnAlerts = alerts.stream().filter(a -> a.level() == WARN).collect(Collectors.toList());
            List<PortfolioAlert> infoAlerts = alerts.stream().filter(a -> a.level() == INFO).collect(Collectors.toList());
            List<PortfolioAlert> activeAlerts = Collections.emptyList();

            this.getStyleClass().clear();
            if(errorAlerts.size() > 0)
            {
                activeAlerts = errorAlerts;
                setGraphic(MaterialDesignIcon.ERROR.graphic());
                this.getStyleClass().add("has-error-alert");
            }
            else if(warnAlerts.size() > 0)
            {
                activeAlerts = warnAlerts;
                setGraphic(MaterialDesignIcon.WARNING.graphic());
                this.getStyleClass().add("has-warn-alert");
            }
            else if(infoAlerts.size() > 0)
            {
                activeAlerts = infoAlerts;
                setGraphic(MaterialDesignIcon.INFO.graphic());
                this.getStyleClass().add("has-info-alert");
            }

            StringBuilder sb = new StringBuilder();
            for(PortfolioAlert a : activeAlerts)
            {
                sb.append(a.text());
                sb.append("; ");
            }
            if(sb.length() >= 2)
            {
                // delete final '; '
                sb.deleteCharAt(sb.length()-1);
                sb.deleteCharAt(sb.length()-1);
            }
            setText(sb.toString());
        }
    }

    public static class Factory implements Callback<TreeTableColumn<AANode,List<PortfolioAlert>>, TreeTableCell<AANode,List<PortfolioAlert>>>
    {
        @Override
        public TreeTableCell<AANode,List<PortfolioAlert>> call(final TreeTableColumn<AANode,List<PortfolioAlert>> col)
        {
            return new AAAlertsTreeTableCell();
        }
    }
}