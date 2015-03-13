package org.mskcc.cbio.importer.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 1/30/15.
 */
public class OncoTreeNode {
    /*
    basic pojo to support a direct graph of oncotree entries
     */
    private final String oncoTreeCode;
    private final String oncoTreeName;
    private String majorCancerType;
    private OncoTreeNode parentOncoTreeNode;
    private List<OncoTreeNode> oncoTreeChildrenList;

    public OncoTreeNode(String aCode, String aName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aCode),
                "An OncoTree code value is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aName),
                "An OncoTree Name is required");
        this.oncoTreeCode = aCode;
        this.oncoTreeName = aName;
        this.oncoTreeChildrenList = Lists.newArrayList();
    }

    public OncoTreeNode(String aCode, String aName, OncoTreeNode aParentNode){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aCode),
                "An OncoTree code value is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aName),
                "An OncoTree Name is required");
        Preconditions.checkArgument(null != aParentNode,
                "A parent oncotree node is required");
        this.oncoTreeCode = aCode;
        this.oncoTreeName = aName;
        this.parentOncoTreeNode = aParentNode;
        this.oncoTreeChildrenList = Lists.newArrayList();
    }


    public String getOncoTreeCode() {
        return oncoTreeCode;
    }

    public String getOncoTreeName() {
        return oncoTreeName;
    }

    public String getMajorCancerType() {
        return majorCancerType;
    }

    public void setMajorCancerType(String aType){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aType),
                "A major cancer type specification is required");
        this.majorCancerType = aType;
    }

    public OncoTreeNode getParentOncoTreeNode() {
        return parentOncoTreeNode;
    }

    public void setParentOncoTreeNode(OncoTreeNode aNode) {
        Preconditions.checkArgument(null != aNode,
                "A  parent oncotree node is required");
        this.parentOncoTreeNode = aNode;
    }

    public List<OncoTreeNode> getChildrenNodeList() {
        return this.oncoTreeChildrenList;
    }

}
