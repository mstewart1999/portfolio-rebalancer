package com.msfinance.pbalancer.model.aa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.util.Validation;

public class AANode
{
    private static final String ROOT_ID = "ROOT";

    private final String parentId;
    private final String id;
    private final String name;
    private final int childPosition;
    private final DoubleExpression percentOfParent;

    private double percentOfRoot;
    private final List<AANode> children = new ArrayList<>();
    private AANode parent;

    public static AANode createRoot()
    {
        return new AANode("", ROOT_ID, "All", 1, DoubleExpression.createSafe100Percent());
    }

    public AANode(final String parentId, final String id, final String name, final int childPosition, final DoubleExpression percentOfParent)
    {
        this.parentId = Validation.assertNonNull(parentId);
        this.id = Validation.assertNonNull(id);
        this.name = Validation.assertNonNull(name);
        this.childPosition = childPosition;
        this.percentOfParent = Validation.assertNonNull(percentOfParent);

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

    protected int getChildPosition()
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



    public boolean isRoot()
    {
        return (parent == null);
    }

    public boolean isLeaf()
    {
        return (children.size() == 0);
    }

    public void addChild(final AANode child)
    {
        Validation.assertNonNull(child);
        children.add(child);
        sortChildren();
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
        if( !Validation.isBlank(parentId) && parentId.contains(",") ) // TODO: also no '"'
        {
            throw new InvalidDataException("invalid parentId::" + toString());
        }
        if( Validation.isBlank(id) || id.contains(",") ) // TODO: also no '"'
        {
            throw new InvalidDataException("invalid id::" + toString());
        }
        if( Validation.isBlank(name) || name.contains(",") ) // TODO: also no '"'
        {
            throw new InvalidDataException("invalid name::" + toString());
        }
        if( percentOfParent.getValue() < 0.0 )
        {
            throw new InvalidDataException("percentOfParent too low::" + toString());
        }
        if( percentOfParent.getValue() > 1.0 )
        {
            throw new InvalidDataException("percentOfParent too high::" + toString());
        }

        if(isRoot())
        {
            // root node
            if(!ROOT_ID.equals(id))
            {
                throw new InvalidDataException("Root of AA tree must have id=" + ROOT_ID + "::" + toString());
            }
            if( !Validation.almostEqual(1.0, percentOfParent.getValue(), 0.0001) )
            {
                throw new InvalidDataException("Root of AA tree must have percentOfParent=100%" + "::" + toString());
            }
        }

        if(isLeaf())
        {
            // leaf node
            if( !AssetClass.exists(name) )
            {
                throw new InvalidDataException("leaf node, name is not in AssetClass::" + toString());
            }
        }
        else
        {
            // non leaf node
            Set<Integer> childPositions = new HashSet<>();
            Set<String> names = new HashSet<>();
            double sumChildrenAllocation = 0.0;
            for(AANode child : children)
            {
                if(child.parent != this)
                {
                    throw new InvalidDataException("parent/child mismatch::" + toString());
                }
                childPositions.add(child.getChildPosition());
                names.add(child.name);
                sumChildrenAllocation += child.getPercentOfParent().getValue();
            }
            if(childPositions.size() != children.size())
            {
                throw new InvalidDataException("duplicate child.childPosition::" + toString());
            }
            if(names.size() != children.size())
            {
                throw new InvalidDataException("duplicate child.name::" + toString());
            }
            if( !Validation.almostEqual(1.0, sumChildrenAllocation, 0.0001) )
            {
                throw new InvalidDataException("all children's sum(percentOfParent) != 100%::" + toString());
            }
        }

        for(AANode child : children)
        {
            // recurse
            // TODO: loop detection??? other tree malformation?
            child.validate();
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
        return sb.toString();
    }

}
