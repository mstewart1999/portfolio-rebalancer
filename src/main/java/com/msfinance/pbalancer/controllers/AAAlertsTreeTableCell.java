package com.msfinance.pbalancer.controllers;

import static com.msfinance.pbalancer.model.PortfolioAlert.Level.ERROR;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.INFO;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.WARN;

import java.util.List;

import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.model.aa.AANode;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

class AAAlertsTreeTableCell extends TreeTableCell<AANode,List<PortfolioAlert>>
{
    @Override
    protected void updateItem(final List<PortfolioAlert> alerts, final boolean empty)
    {
        if (alerts == getItem() && empty == isEmpty()) return;

        super.updateItem(alerts, empty);
        if((alerts == null) || alerts.isEmpty())
        {
            setText("");
            this.getStyleClass().clear();
        }
        else
        {
            boolean hasErrorAlert = alerts.stream().filter(a -> a.level() == ERROR).count() > 0;
            boolean hasWarnAlert = alerts.stream().filter(a -> a.level() == WARN).count() > 0;
            boolean hasInfoAlert = alerts.stream().filter(a -> a.level() == INFO).count() > 0;

            this.getStyleClass().clear();
            if(hasErrorAlert)
            {
                this.getStyleClass().add("has-error-alert");
            }
            else if(hasWarnAlert)
            {
                this.getStyleClass().add("has-warn-alert");
            }
            else if(hasInfoAlert)
            {
                this.getStyleClass().add("has-info-alert");
            }

            StringBuilder sb = new StringBuilder();
            for(PortfolioAlert a : alerts)
            {
                sb.append(a.text());
                sb.append("; ");
            }
            // delete final '; '
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
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