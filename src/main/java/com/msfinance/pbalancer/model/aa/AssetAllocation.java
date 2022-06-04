package com.msfinance.pbalancer.model.aa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.pbalancer.model.InvalidDataException;
import com.msfinance.pbalancer.model.PortfolioAlert;
import com.msfinance.pbalancer.util.CSVHelper;
import com.msfinance.pbalancer.util.JSONHelper;
import com.msfinance.pbalancer.util.Validation;

public class AssetAllocation
{
    private PredefinedAA predefined; // or null
    private AANode root;

    public AssetAllocation(final PredefinedAA predefined, final List<String> nodeCsvs) throws InvalidDataException
    {
        setPredefined(predefined);
        setNodeCsvs(nodeCsvs.toArray(new String[0]));
    }

    public AssetAllocation(final PredefinedAA predefined, final String[] nodeCsvs) throws InvalidDataException
    {
        setPredefined(predefined);
        setNodeCsvs(nodeCsvs);
    }

    public AssetAllocation()
    {
        setPredefined(null);
        // make a dummy AA
        this.root = AANode.createRoot();
        root.addChild(new AANode(root.getId(), UUID.randomUUID().toString(), AssetClass.UNDEFINED, 1, DoubleExpression.createSafe100Percent(), AANodeType.AC));
    }

    @JsonProperty
    public PredefinedAA getPredefined()
    {
        return predefined;
    }

    @JsonProperty
    public void setPredefined(final PredefinedAA predefined)
    {
        this.predefined = predefined;
    }

    @JsonIgnore
    public AANode getRoot()
    {
        return root;
    }

    @JsonProperty
    public String[] getNodeCsvs()
    {
        List<String> list = new ArrayList<>();
        for(AANode n : root.allChildren())
        {
            list.add(n.toCsvLine());
        }
        return list.toArray(new String[list.size()]);
    }

    @JsonProperty
    public void setNodeCsvs(final String[] nodeCsvs) throws InvalidDataException
    {
        // prep
        root = null;

        Map<String,AANode> nodes = new HashMap<>();

        int lines = 0;
        for(String nodeCsv : nodeCsvs)
        {
            List<String> nodeFields = CSVHelper.fromCsvLine(nodeCsv);
            if(nodeFields.size() > 0)
            {
                // only process lines with content
                // TODO: header fields?
                lines++;
                if(nodeFields.size() != 6)
                {
                    throw new InvalidDataException("Wrong number of csv fields: " + nodeFields.size());
                }
                nodes.put(
                        nodeFields.get(1),
                        new AANode(
                                nodeFields.get(0),
                                nodeFields.get(1),
                                nodeFields.get(2),
                                Validation.toInt(nodeFields.get(3)),
                                new DoubleExpression(nodeFields.get(4)),
                                AANodeType.valueOf(nodeFields.get(5))
                                )
                        );
            }
        }
        if(nodes.size() != lines)
        {
            throw new InvalidDataException("Duplicate id found");
        }

        for(AANode node : nodes.values())
        {
            AANode parent = nodes.get(node.getParentId());
            if(parent != null)
            {
                node.setParent(parent);
                parent.addChild(node);
            }
            else
            {
                if(root != null)
                {
                    throw new InvalidDataException("Multiple roots found");
                }
                root = node;
            }
        }
        if(root == null)
        {
            throw new InvalidDataException("No root found");
        }

        root.computePercentOfRoot();
        root.validate();
    }


    public String toCsv()
    {
        StringBuilder sb = new StringBuilder();
        for(String n : getNodeCsvs())
        {
            sb.append(n);
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toJson() throws IOException
    {
        return JSONHelper.toJson(this);
    }


    public long countErrors()
    {
        return root.allChildren()
          .stream()
          .flatMap(n -> n.getAlerts().stream())
          .filter(a -> a.level() == PortfolioAlert.Level.ERROR)
          .count()
          ;
    }

    public long countWarns()
    {
        return root.allChildren()
                .stream()
                .flatMap(n -> n.getAlerts().stream())
                .filter(a -> a.level() == PortfolioAlert.Level.WARN)
                .count()
                ;
    }

    public long countInfos()
    {
        return root.allChildren()
                .stream()
                .flatMap(n -> n.getAlerts().stream())
                .filter(a -> a.level() == PortfolioAlert.Level.INFO)
                .count()
                ;
    }
}
