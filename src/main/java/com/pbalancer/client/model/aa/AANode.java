package com.pbalancer.client.model.aa;

import static com.pbalancer.client.model.PortfolioAlert.Level.ERROR;
import static com.pbalancer.client.model.PortfolioAlert.Level.INFO;
import static com.pbalancer.client.model.PortfolioAlert.Level.WARN;
import static com.pbalancer.client.model.PortfolioAlert.Type.AA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pbalancer.client.model.InvalidDataException;
import com.pbalancer.client.model.PortfolioAlert;
import com.pbalancer.client.util.Validation;

public class AANode
{
    private static final String ROOT_ID = "ROOT";

    private final String parentId;
    private final String id;
    private final String name;
    private int listPosition;
    private DoubleExpression percentOfParent;
    private final AANodeType type;
    private final List<PortfolioAlert> alerts;

    private double percentOfRoot;
    private final List<AANode> children = new ArrayList<>();
    private AANode parent;



    public static AANode createRoot()
    {
        return new AANode("", ROOT_ID, "All", 1, DoubleExpression.createSafe100Percent(), AANodeType.R);
    }

    public AANode(final String parentId, final String id, final String name, final int listPosition, final DoubleExpression percentOfParent, final AANodeType type)
    {
        this.parentId = Validation.assertNonNull(parentId);
        this.id = Validation.assertNonNull(id);
        this.name = Validation.assertNonNull(name);
        this.listPosition = listPosition;
        this.percentOfParent = Validation.assertNonNull(percentOfParent);
        this.type = type;
        this.alerts = new ArrayList<>();

        // see validate()
    }

    public String getParentId()
    {
        return parentId;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getListPosition()
    {
        return listPosition;
    }

    public void setListPosition(final int listPosition)
    {
        this.listPosition = listPosition;
    }

    public DoubleExpression getPercentOfParent()
    {
        return percentOfParent;
    }

    public double getPercentOfRoot()
    {
        return percentOfRoot;
    }

    public AANodeType getType()
    {
        return type;
    }

    @JsonIgnore
    public AANode getParent()
    {
        return parent;
    }

    @JsonIgnore
    public String getPercentOfParentIndentedAsString()
    {
        int depth = getDepth();
        String lPad = "    ".repeat(depth);
        return lPad + percentOfParent.getExpr();
    }

    @JsonIgnore
    public String getPercentOfRootAsString()
    {
        // TODO: remove
        return String.format("TODO %6.2f", percentOfRoot*100) + "%";
    }

    @JsonIgnore
    public void setPercentOfParentAsString(final String str) throws InvalidDataException
    {
        if(isRoot())
        {
            // ignore any edits, keep 100%
            percentOfParent = DoubleExpression.createSafe100Percent();
        }
        else if(Validation.isBlank(str))
        {
            percentOfParent = DoubleExpression.createSafe0Percent();
        }
        else
        {
            try
            {
                percentOfParent = new DoubleExpression(str);
            }
            catch (InvalidDataException e)
            {
                throw e;
            }
        }

        computePercentOfRoot();
        if(isLeaf())
        {
            for(AANode n : children)
            {
                n.computePercentOfRoot();
            }
        }
    }

    public List<PortfolioAlert> getAlerts()
    {
        return Collections.unmodifiableList(alerts);
    }


    public boolean isRoot()
    {
        return type == AANodeType.R;
    }

    public boolean isLeaf()
    {
        return type == AANodeType.AC;
    }

    public void addChild(final AANode child)
    {
        Validation.assertNonNull(child);
        children.add(child);
        sortChildren();
    }

    public void removeChild(final AANode child)
    {
        Validation.assertNonNull(child);
        children.remove(child);
    }

    public void clearChildren()
    {
        children.clear();
    }

    private void sortChildren()
    {
        Collections.sort(children, Comparator.comparingInt(AANode::getListPosition));
    }

    public List<AANode> children()
    {
        return Collections.unmodifiableList(children);
    }

    public List<AANode> allChildren()
    {
        List<AANode> all = new ArrayList<>();
        all.add(this);
        for(AANode child : children)
        {
            all.addAll(child.allChildren());
        }
        return all;
    }

    public List<AANode> allLeaves()
    {
        List<AANode> all = new ArrayList<>();
        if(this.isLeaf())
        {
            all.add(this);
        }
        for(AANode child : children)
        {
            all.addAll(child.allLeaves());
        }
        return all;
    }

    public void setParent(final AANode parent)
    {
        Validation.assertNull(this.parent);
        Validation.assertNonNull(parent);
        this.parent = parent;
    }

    public void validate() throws InvalidDataException
    {
        alerts.clear();

        if( !Validation.isBlank(parentId) && parentId.contains(",") ) // TODO: also no '"'
        {
            alerts.add(new PortfolioAlert(AA, ERROR, "invalid parentId::" + toString()));
        }
        if( Validation.isBlank(id) || id.contains(",") ) // TODO: also no '"'
        {
            alerts.add(new PortfolioAlert(AA, ERROR, "invalid id::" + toString()));
        }
        if( Validation.isBlank(name) || name.contains(",") ) // TODO: also no '"'
        {
            alerts.add(new PortfolioAlert(AA, ERROR, "invalid name"));
        }
        if( percentOfParent.getValue() < 0.0 )
        {
            // allow for borrowing?
            alerts.add(new PortfolioAlert(AA, INFO, "percent too low (borrowing?)"));
        }
        if( percentOfParent.getValue() > 1.0 )
        {
            // allow for leverage?
            alerts.add(new PortfolioAlert(AA, INFO, "percent too high (leverage)"));
        }

        if(isRoot())
        {
            // root node
            if(!Validation.isBlank(parentId))
            {
                alerts.add(new PortfolioAlert(AA, ERROR, "Root of AA tree must have no parentId, but parentId=" + parentId));
            }
            if(!ROOT_ID.equals(id))
            {
                alerts.add(new PortfolioAlert(AA, ERROR, "Root of AA tree must have id=" + ROOT_ID + "::" + toString()));
            }
            if( !Validation.almostEqual(1.0, percentOfParent.getValue(), 0.0001) )
            {
                alerts.add(new PortfolioAlert(AA, ERROR, "Root of AA tree must have percent=100%"));
            }
        }

        if(isLeaf())
        {
            // leaf node
            if(children.size() > 0)
            {
                alerts.add(new PortfolioAlert(AA, ERROR, "leaf node must have no children"));
            }
            if(AssetClass.lookup(name) == null)
            {
                alerts.add(new PortfolioAlert(AA, INFO, "Custom AssetClass"));
            }
            if( AssetClass.UNDEFINED.equals(name) )
            {
                alerts.add(new PortfolioAlert(AA, WARN, "AssetClass 'Undefined' should be replaced"));
            }
        }
        else
        {
            // non leaf node
            if((type != AANodeType.R) && (type != AANodeType.G))
            {
                alerts.add(new PortfolioAlert(AA, ERROR, "non leaf node must be type R or G, but type=" + type));
            }

            // validate children - this clears alerts
            for(AANode child : children)
            {
                // recurse
                // TODO: loop detection??? other tree malformation?
                child.validate();
            }

            // then validate aggregate things which may add additional alerts
            Map<Integer,List<AANode>> listPositions = new HashMap<>();
            Map<String,List<AANode>> childNames = new HashMap<>();
            double sumChildrenAllocation = 0.0;
            for(AANode child : children)
            {
                if(child.parent != this)
                {
                    child.alerts.add(new PortfolioAlert(AA, ERROR, "parent/child mismatch::" + toString()));
                }
                if(!listPositions.containsKey(child.getListPosition()))
                {
                    listPositions.put(child.getListPosition(), new ArrayList<>());
                }
                if(!childNames.containsKey(child.getName()))
                {
                    childNames.put(child.getName(), new ArrayList<>());
                }
                listPositions.get(child.getListPosition()).add(child);
                childNames.get(child.name).add(child);
                sumChildrenAllocation += child.getPercentOfParent().getValue();
            }
            for(List<AANode> byListPosition : listPositions.values())
            {
                if(byListPosition.size() > 1)
                {
                    for(AANode c : byListPosition)
                    {
                        c.alerts.add(new PortfolioAlert(AA, INFO, "duplicate list position: " + c.getListPosition()));
                    }
                }
            }
            for(List<AANode> byChildName : childNames.values())
            {
                if(byChildName.size() > 1)
                {
                    for(AANode c : byChildName)
                    {
                        c.alerts.add(new PortfolioAlert(AA, INFO, "duplicate child name: " + c.getName()));
                    }
                }
            }
            if( !Validation.almostEqual(1.0, sumChildrenAllocation, 0.0001) )
            {
                this.alerts.add(new PortfolioAlert(AA, WARN, "all children must sum to 100% (currently " + Math.round(sumChildrenAllocation*100) + "%)"));
            }
        }

        // special final check for root
        if(isRoot())
        {
            long allAlerts = this.allChildren().stream().flatMap(n -> n.getAlerts().stream()).count();
            if(allAlerts == 0)
            {
                Double total = this.allLeaves()
                    .stream()
                    .map(n -> n.getPercentOfRoot())
                    .reduce(0.0, Double::sum);
                if(!Validation.almostEqual(1.0, total, 0.0001))
                {
                    alerts.add(new PortfolioAlert(AA, WARN, "sum of leaves must be 100% (currently " + Math.round(total*100) + "%)"));
                }
            }
        }
    }

    public void computePercentOfRoot()
    {
        if(parent == null)
        {
            percentOfRoot = percentOfParent.getValue();
        }
        else
        {
            percentOfRoot = percentOfParent.getValue() * parent.percentOfRoot;
        }
        for(AANode child : children)
        {
            child.computePercentOfRoot();
        }
    }

    @JsonIgnore
    public String getPath()
    {
        if(isRoot())
        {
            return "";
        }
        if(parent == null)
        {
            // this happens for "Unallocated" scenario a ActualAANode.getPath() caller
            return "";
        }
        return parent.getPath() + "/" + parent.getName();
    }

    @JsonIgnore
    public String getPercentOfRootExprAsString()
    {
        if(isRoot() || (parent == null))
        {
            return "";
        }
        String thisExpr = percentOfParent.getExpr();
        if(thisExpr.startsWith("="))
        {
            thisExpr = "(" + thisExpr.substring(1) + ")";
        }
        if(parent.isRoot())
        {
            return thisExpr;
        }
        return parent.getPercentOfRootExprAsString() + " * " + thisExpr;
    }

    @JsonIgnore
    public int getDepth()
    {
        if(isRoot() || (parent == null))
        {
            return 0;
        }
        return parent.getDepth() + 1;
    }


    @Override
    public String toString()
    {
        return "AANode [parentId=" + parentId
                + ", id=" + id
                + ", name=" + name
                + ", listPosition=" + listPosition
                + ", percentOfParent=" + percentOfParent
                + ", type=" + type
                //+ ", alerts=" + alerts
                + "]";
    }

    public String toCsvLine()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getParentId());
        sb.append(",");
        sb.append(getId());
        sb.append(",");
        sb.append(getName());
        sb.append(",");
        sb.append(getListPosition());
        sb.append(",");
        sb.append(getPercentOfParent().getExpr());
        sb.append(",");
        sb.append(getType());
        sb.append(",");
        // do not persist alerts
        return sb.toString();
    }

}
