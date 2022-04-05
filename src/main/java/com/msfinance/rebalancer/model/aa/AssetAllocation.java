package com.msfinance.rebalancer.model.aa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msfinance.rebalancer.model.InvalidDataException;
import com.msfinance.rebalancer.util.CSVHelper;
import com.msfinance.rebalancer.util.JSONHelper;
import com.msfinance.rebalancer.util.Validation;

public class AssetAllocation
{
    private final String name;
    private final String url;
    private AANode root;

    @JsonCreator
    public AssetAllocation(
            @JsonProperty("name") final String name,
            @JsonProperty("url") final String url,
            @JsonProperty("nodeCsvs") final List<String> nodeCsvs) throws InvalidDataException
    {
        this.name = Objects.requireNonNullElse(name, "");
        this.url = Objects.requireNonNullElse(url, "");

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
                if(nodeFields.size() != 5)
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
                                new DoubleExpression(nodeFields.get(4))));
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

        root.validate();
        root.computePercentOfRoot();

        Double total = root.allLeaves()
            .stream()
            .map(n -> n.getPercentOfRoot())
            .reduce(0.0, Double::sum);
        if(!Validation.almostEqual(1.0, total, 0.0001))
        {
            throw new InvalidDataException("Sum of leaves not 100%");
        }
    }

    public AssetAllocation(final List<String> nodeCsvs) throws InvalidDataException
    {
        this("", "", nodeCsvs);
    }

    public AssetAllocation()
    {
        this.name = "";
        this.url = "";
        // make a dummy AA
        this.root = AANode.createRoot();
        root.addChild(new AANode(root.getId(), UUID.randomUUID().toString(), "Unallocated", 1, DoubleExpression.createSafe100Percent()));
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public String getUrl()
    {
        return url;
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

}
