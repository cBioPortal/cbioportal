package org.mskcc.cbio.importer.persistence.staging.util;

import java.util.Iterator;
/*
Google code implementation of a Trr data structure for Java
Re: https://code.google.com/p/yet-another-tree-structure/source/browse/trunk/java/src/com/tree/TreeNodeIter.java
 */

public class TreeNodeIter<T> implements Iterator<TreeNode<T>> {

    enum ProcessStages {
        ProcessParent, ProcessChildCurNode, ProcessChildSubNode
    }

    private TreeNode<T> treeNode;

    public TreeNodeIter(TreeNode<T> treeNode) {
        this.treeNode = treeNode;
        this.doNext = ProcessStages.ProcessParent;
        this.childrenCurNodeIter = treeNode.children.iterator();
    }

    private ProcessStages doNext;
    private TreeNode<T> next;
    private Iterator<TreeNode<T>> childrenCurNodeIter;
    private Iterator<TreeNode<T>> childrenSubNodeIter;

    @Override
    public boolean hasNext() {

        if (this.doNext == ProcessStages.ProcessParent) {
            this.next = this.treeNode;
            this.doNext = ProcessStages.ProcessChildCurNode;
            return true;
        }

        if (this.doNext == ProcessStages.ProcessChildCurNode) {
            if (childrenCurNodeIter.hasNext()) {
                TreeNode<T> childDirect = childrenCurNodeIter.next();
                childrenSubNodeIter = childDirect.iterator();
                this.doNext = ProcessStages.ProcessChildSubNode;
                return hasNext();
            }

            else {
                this.doNext = null;
                return false;
            }
        }

        if (this.doNext == ProcessStages.ProcessChildSubNode) {
            if (childrenSubNodeIter.hasNext()) {
                this.next = childrenSubNodeIter.next();
                return true;
            }
            else {
                this.next = null;
                this.doNext = ProcessStages.ProcessChildCurNode;
                return hasNext();
            }
        }

        return false;
    }

    @Override
    public TreeNode<T> next() {
        return this.next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
