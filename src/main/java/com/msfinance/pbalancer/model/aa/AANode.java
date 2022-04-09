package com.msfinance.pbalancer.model.aa;

import static com.msfinance.pbalancer.model.PortfolioAlert.Level.ERROR;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.INFO;
import static com.msfinance.pbalancer.model.PortfolioAlert.Level.WARN;
import static com.msfinance.pbalancer.model.PortfolioAlert.Type.AA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.util.Validation;

public class AANode
{
    private static final String ROOT_ID = "ROOT";

    private final String parentId;
    private final String id;
    private final String name;
    private final int childPosition;
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

    public AANode(final String parentId, final String id, final String name, final int childPosition, final DoubleExpression percentOfParent, final AANodeType type)
    {
        this.parentId = Validation.assertNonNull(parentId);
        this.id = Validation.assertNonNull(id);
        this.name = Validation.assertNonNull(name);
        this.childPosition = childPosition;
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

    public int getChildPosition()
    {
        return childPosition;
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
    public String getPercentOfParentAsString()
    {
        return percentOfParent.getExpr();
    }

    @JsonIgnore
    public String getPercentOfRootAsString()
    {
        return String.format("%6.2f", percentOfRoot*100) + "%";
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
        Collections.sort(children, Comparator.comparingInt(AANode::getChildPosition));
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
            if( !AssetClass.exists(name) )
            {
                alerts.add(new PortfolioAlert(AA, WARN, "leaf node's name is not a known AssetClass"));
            }
            if( AssetClass.UNALLOCATED.equals(name) )
            {
                alerts.add(new PortfolioAlert(AA, WARN, "AssetClass 'Unallocated' should be defined"));
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
            Map<Integer,List<AANode>> childPositions = new HashMap<>();
            Map<String,List<AANode>> childNames = new HashMap<>();
            double sumChildrenAllocation = 0.0;
            for(AANode child : children)
            {
                if(child.parent != this)
                {
                    child.alerts.add(new PortfolioAlert(AA, ERROR, "parent/child mismatch::" + toString()));
                }
                if(!childPositions.containsKey(child.getChildPosition()))
                {
                    childPositions.put(child.getChildPosition(), new ArrayList<>());
                }
                if(!childNames.containsKey(child.getName()))
                {
                    childNames.put(child.getName(), new ArrayList<>());
                }
                childPositions.get(child.getChildPosition()).add(child);
                childNames.get(child.name).add(child);
                sumChildrenAllocation += child.getPercentOfParent().getValue();
            }
            for(List<AANode> byChildPosition : childPositions.values())
            {
                if(byChildPosition.size() > 1)
                {
                    for(AANode c : byChildPosition)
                    {
                        c.alerts.add(new PortfolioAlert(AA, INFO, "duplicate child position: " + c.getChildPosition()));
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
                this.alerts.add(new PortfolioAlert(AA, WARN, "all children's sum(percent) not 100%"));
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
                    alerts.add(new PortfolioAlert(AA, WARN, "sum of leaves not 100%"));
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


    @Override
    public String toString()
    {
        return "AANode [parentId=" + parentId
                + ", id=" + id
                + ", name=" + name
                + ", childPosition=" + childPosition
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
        sb.append(getChildPosition());
        sb.append(",");
        sb.append(getPercentOfParent().getExpr());
        sb.append(",");
        sb.append(getType());
        sb.append(",");
        // do not persist alerts
        return sb.toString();
    }

}
