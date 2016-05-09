/**
 *	Design for SBGNViz Editor actions.
 *  Command Design Pattern is used.
 *  A simple undo-redo manager is implemented(EditorActionsManager)
 *	Author: Istemi Bahceci<istemi.bahceci@gmail.com>
 */

function addNode(param)
{
  var result;
  if (param.firstTime) {
    var newNode = param.newNode;
    result = addRemoveUtilities.addNode(newNode.x, newNode.y, newNode.sbgnclass);
  }
  else {
    result = addRemoveUtilities.restoreEles(param);
  }
  return result;
}

function removeNodes(nodesToBeDeleted)
{
  return addRemoveUtilities.removeNodes(nodesToBeDeleted);
}

function removeEles(elesToBeRemoved) {
  return addRemoveUtilities.removeEles(elesToBeRemoved);
}

function restoreEles(eles)
{
  return addRemoveUtilities.restoreEles(eles);
}

function addEdge(param)
{
  var result;
  if (param.firstTime) {
    var newEdge = param.newEdge;
    result = addRemoveUtilities.addEdge(newEdge.source, newEdge.target, newEdge.sbgnclass);
  }
  else {
    result = addRemoveUtilities.restoreEles(param);
  }
  return result;
}

function removeEdges(edgesToBeDeleted)
{
  return addRemoveUtilities.removeEdges(edgesToBeDeleted);
}

function expandNode(param) {
  var result = {
    firstTime: false
  };
  var node = param.node;
  result.node = node;
  result.nodesData = getNodePositionsAndSizes();
  if (param.firstTime) {
    expandCollapseUtilities.expandNode(node);
  }
  else {
    expandCollapseUtilities.simpleExpandNode(node);
    returnToPositionsAndSizes(param.nodesData);
  }
  return result;
}

function collapseNode(param) {
  var result = {
    firstTime: false
  };
  var node = param.node;
  result.node = node;
  result.nodesData = getNodePositionsAndSizes();
  if (param.firstTime) {
    expandCollapseUtilities.collapseNode(node);
  }
  else {
    expandCollapseUtilities.simpleCollapseNode(node);
    returnToPositionsAndSizes(param.nodesData);
  }
  return result;
}

function expandGivenNodes(param) {
  var nodes = param.nodes;
  var result = {
    firstTime: false
  };
  result.nodes = nodes;
  result.nodesData = getNodePositionsAndSizes();
  if (param.firstTime) {
    expandCollapseUtilities.expandGivenNodes(nodes);
  }
  else {
    expandCollapseUtilities.simpleExpandGivenNodes(nodes);
    returnToPositionsAndSizes(param.nodesData);
  }
  return result;
}

function collapseGivenNodes(param) {
  var nodes = param.nodes;
  var result = {};
  result.nodes = nodes;
  result.nodesData = getNodePositionsAndSizes();
  if (param.firstTime) {
    expandCollapseUtilities.collapseGivenNodes(nodes);
  }
  else {
    expandCollapseUtilities.simpleCollapseGivenNodes(nodes);
    returnToPositionsAndSizes(param.nodesData);
  }
  return result;
}

function expandAllNodes(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  if (param.firstTime) {
    result.expandStack = expandCollapseUtilities.expandAllNodes(param.nodes, param.selector);
  }
  else {
    result.expandStack = expandCollapseUtilities.simpleExpandAllNodes();
    returnToPositionsAndSizes(param.nodesData);
  }
  return result;
}

function simpleExpandAllNodes(param) {
  return expandCollapseUtilities.simpleExpandAllNodes(param.nodes, param.selector);
}

function collapseExpandedStack(expandedStack) {
  return expandCollapseUtilities.collapseExpandedStack(expandedStack);
}

function undoExpandAllNodes(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  expandCollapseUtilities.collapseExpandedStack(param.expandStack);
  returnToPositionsAndSizes(param.nodesData);
  return result;
}

function getNodePositionsAndSizes() {
  var positionsAndSizes = {};
  var nodes = cy.nodes();

  for (var i = 0; i < nodes.length; i++) {
    var ele = nodes[i];
    positionsAndSizes[ele.id()] = {
      width: ele.width(),
      height: ele.height(),
      x: ele.position("x"),
      y: ele.position("y")
    };
  }

  return positionsAndSizes;
}

function undoExpandNode(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  result.node = expandCollapseUtilities.simpleCollapseNode(param.node);
  returnToPositionsAndSizes(param.nodesData);
  return result;
}

function undoCollapseNode(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  result.node = expandCollapseUtilities.simpleExpandNode(param.node);
  returnToPositionsAndSizes(param.nodesData);
  return result;
}

function undoExpandGivenNodes(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  result.nodes = expandCollapseUtilities.simpleCollapseGivenNodes(param.nodes);
  returnToPositionsAndSizes(param.nodesData);
  return result;
}

function undoCollapseGivenNodes(param) {
  var result = {
    firstTime: false
  };
  result.nodesData = getNodePositionsAndSizes();
  result.nodes = expandCollapseUtilities.simpleExpandGivenNodes(param.nodes);
  returnToPositionsAndSizes(param.nodesData);
  return result;
}

function simpleExpandNode(node) {
  return expandCollapseUtilities.simpleExpandNode(node);
}

function simpleCollapseNode(node) {
  return expandCollapseUtilities.simpleCollapseNode(node);
}

function simpleExpandGivenNodes(nodes) {
  return expandCollapseUtilities.simpleExpandGivenNodes(nodes);
}

function simpleCollapseGivenNodes(nodes) {
  return expandCollapseUtilities.simpleCollapseGivenNodes(nodes);
}

function returnToPositionsAndSizesConditionally(nodesData) {
  if (nodesData.firstTime) {
    delete nodesData.firstTime;
    return nodesData;
  }
  return returnToPositionsAndSizes(nodesData);
}

function returnToPositionsAndSizes(nodesData) {
  var currentPositionsAndSizes = {};
  cy.nodes().positions(function (i, ele) {
    currentPositionsAndSizes[ele.id()] = {
      width: ele.width(),
      height: ele.height(),
      x: ele.position("x"),
      y: ele.position("y")
    };
    var data = nodesData[ele.id()];
    ele._private.data.width = data.width;
    ele._private.data.height = data.height;
    return {
      x: data.x,
      y: data.y
    };
  });

  return currentPositionsAndSizes;
}

function moveNodesConditionally(param) {
  if (param.move) {
    moveNodes(param.positionDiff, param.nodes);
  }
  return param;
}

function moveNodesReversely(param) {
  var diff = {
    x: -1 * param.positionDiff.x,
    y: -1 * param.positionDiff.y
  };
  var result = {
    positionDiff: param.positionDiff,
    nodes: param.nodes,
    move: true
  };
  moveNodes(diff, param.nodes);
  return result;
}

function moveNodes(positionDiff, nodes) {
  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];
    var oldX = node.position("x");
    var oldY = node.position("y");
    node.position({
      x: oldX + positionDiff.x,
      y: oldY + positionDiff.y
    });
    var children = node.children();
    moveNodes(positionDiff, children);
  }
}

function deleteSelected(param) {
  if (param.firstTime) {
    return sbgnFiltering.deleteSelected();
  }
  return addRemoveUtilities.removeElesSimply(param.eles);
}

function restoreSelected(eles) {
  var param = {};
  param.eles = restoreEles(eles);
  param.firstTime = false;
  return param;
}

function hideSelected(param) {
  var currentNodes = cy.nodes(":visible");

  if(currentNodes.length == 0){
    return;
  }

  if (param.firstTime) {
    sbgnFiltering.hideSelected();
  }
  else {
    sbgnFiltering.showJustGivenNodes(param.nodesToShow);
  }
  clearDrawsOfNodeResize();
  return currentNodes;
}

function showSelected(param) {
  var currentNodes = cy.nodes(":visible");
  if (param.firstTime) {
    sbgnFiltering.showSelected();
  }
  else {
    sbgnFiltering.showJustGivenNodes(param.nodesToShow);
  }
  return currentNodes;
}

function showAll() {
  var currentNodes = cy.nodes(":visible");
  sbgnFiltering.showAll();
  return currentNodes;
}

function showJustGivenNodes(nodesToShow) {
  var param = {};
  param.nodesToShow = cy.nodes(":visible");
  param.firstTime = false;
  sbgnFiltering.showJustGivenNodes(nodesToShow);
  return param;
}

function highlightSelected(param) {
  var elementsToHighlight;
  var result = {};
  //If this is the first call of the function then call the original method
  if (param.firstTime) {
    if (sbgnFiltering.isAllElementsAreNotHighlighted()) {
      //mark that there was no highlighted element
      result.allElementsWasNotHighlighted = true;
    }

    var alreadyHighlighted = cy.elements("[highlighted='true']").filter(":visible");

    if(param.elesToHighlight){
      elementsToHighlight = param.elesToHighlight;
    }

    //If elementsToHighlight is undefined it will be calculated in the function else it
    //will be directly used in the function
    if (param.highlightNeighboursofSelected) {
      elementsToHighlight = sbgnFiltering.highlightNeighborsofSelected(elementsToHighlight);
    }
    else if (param.highlightProcessesOfSelected) {
      elementsToHighlight = sbgnFiltering.highlightProcessesOfSelected(elementsToHighlight);
    }

    elementsToHighlight = elementsToHighlight.not(alreadyHighlighted);
  }
  else {
    elementsToHighlight = param.elesToHighlight.not(cy.elements("[highlighted='true']").filter(":visible"));
    elementsToHighlight.data("highlighted", 'true');
    sbgnFiltering.highlightNodes(elementsToHighlight.nodes());
    sbgnFiltering.highlightEdges(elementsToHighlight.edges());

    //If there are some elements to not highlight handle them
    if (param.elesToNotHighlight != null) {
      var elesToNotHighlight = param.elesToNotHighlight;
      elesToNotHighlight.removeData("highlighted");
      sbgnFiltering.notHighlightNodes(elesToNotHighlight.nodes());
      sbgnFiltering.notHighlightEdges(elesToNotHighlight.edges());

      //If there are some elements to not highlight then allElementsWasNotHighlighted should be true
      result.allElementsWasNotHighlighted = true;
    }
  }
  result.elesToNotHighlight = elementsToHighlight;
  return result;
}

function notHighlightEles(param) {
  var elesToNotHighlight = param.elesToNotHighlight;
  var allElementsWasNotHighlighted = param.allElementsWasNotHighlighted;

  var result = {};

  if (param.allElementsWasNotHighlighted) {
    sbgnFiltering.removeHighlights();
    result.elesToHighlight = elesToNotHighlight;
    result.elesToNotHighlight = cy.elements(":visible").not(elesToNotHighlight);
  }
  else {
    sbgnFiltering.notHighlightNodes(elesToNotHighlight.nodes());
    sbgnFiltering.notHighlightEdges(elesToNotHighlight.edges());
    elesToNotHighlight.removeData("highlighted");

    result.elesToHighlight = elesToNotHighlight;
  }

  result.firstTime = false;
  return result;
}

function removeHighlights() {
  var result = {};
  if (sbgnFiltering.isAllElementsAreNotHighlighted()) {
    result.elesToHighlight = cy.elements(":visible");
  }
  else {
    result.elesToHighlight = cy.elements("[highlighted='true']").filter(":visible");
  }

  sbgnFiltering.removeHighlights();

  result.elesToNotHighlight = cy.elements(":visible").not(result.elesToHighlight);
  result.firstTime = false;
  return result;
}

function changeParent(param) {
  //If there is an inner param firstly call the function with it
  //Inner param is created if the change parent operation requires
  //another change parent operation in it.
  if (param.innerParam) {
    changeParent(param.innerParam);
  }

  var node = param.node;
  var oldParentId = node._private.data.parent;
  var oldParent = node.parent()[0];
  var newParent = param.newParent;
  var nodesData = param.nodesData;
  var result = {
    node: node,
    newParent: oldParent
  };

  result.nodesData = getNodesData();

  //If new parent is not null some checks should be performed
  if (newParent) {
    //check if the node was the anchestor of it's new parent
    var wasAnchestorOfNewParent = false;
    var temp = newParent.parent()[0];
    while (temp != null) {
      if (temp == node) {
        wasAnchestorOfNewParent = true;
        break;
      }
      temp = temp.parent()[0];
    }
    //if so firstly remove the parent from inside of the node
    if (wasAnchestorOfNewParent) {
      var parentOfNewParent = newParent.parent()[0];
      addRemoveUtilities.changeParent(newParent, newParent._private.data.parent, node._private.data.parent);
      oldParentId = node._private.data.parent;
      //We have an internal change parent operation to redo this operation
      //we need an inner param to call the function with it at the beginning
      result.innerParam = {
        node: newParent,
        newParent: parentOfNewParent,
        nodesData: {
          firstTime: true
        }
      };
    }
  }

  //Change the parent of the node
  addRemoveUtilities.changeParent(node, oldParentId, newParent ? newParent._private.data.id : undefined);

  if (param.posX && param.posY) {
    node.position({
      x: param.posX,
      y: param.posY
    });
  }

  cy.nodes().updateCompoundBounds();

  returnToPositionsAndSizesConditionally(nodesData);

  return result;
}

/*
 * This method assumes that param.nodesToMakeCompound contains at least one node
 * and all of the nodes including in it have the same parent
 */
function createCompoundForSelectedNodes(param) {
  var nodesToMakeCompound = param.nodesToMakeCompound;
  var oldParentId = nodesToMakeCompound[0].data("parent");
  var newCompound;

  if (param.firstTime) {
    var eles = cy.add({
      group: "nodes",
      data: {
        sbgnclass: param.compundType,
        parent: oldParentId,
        sbgnbbox: {
        },
        sbgnstatesandinfos: [],
        ports: []
      }
    });

    newCompound = eles[eles.length - 1];
    newCompound._private.data.sbgnbbox.h = newCompound.height();
    newCompound._private.data.sbgnbbox.w = newCompound.width();
  }
  else {
    newCompound = param.removedCompund.restore();
  }

  var newCompoundId = newCompound.id();

  addRemoveUtilities.changeParent(nodesToMakeCompound, oldParentId, newCompoundId);
  refreshPaddings();
  return newCompound;
}

function removeCompound(compoundToRemove) {
  var compoundId = compoundToRemove.id();
  var newParentId = compoundToRemove.data("parent");
  var childrenOfCompound = compoundToRemove.children();

  addRemoveUtilities.changeParent(childrenOfCompound, compoundId, newParentId);
  var removedCompund = compoundToRemove.remove();

  refreshPaddings();

  var param = {
    nodesToMakeCompound: childrenOfCompound,
    removedCompund: removedCompund
  };

  return param;
}

function resizeNode(param) {
  var result = {
    firstTime: false
  };
  var node = param.node;
  result.width = node.width();
  result.height = node.height();
  result.node = node;
  if (!param.firstTime) {
    node.data("width", param.width);
    node.data("height", param.height);
  }

  node._private.data.sbgnbbox.w = node.width();
  node._private.data.sbgnbbox.h = node.height();
  return result;
}

function changeNodeLabel(param) {
  var result = {
  };
  var node = param.node;
  result.node = node;
  result.sbgnlabel = node._private.data.sbgnlabel;

  node._private.data.sbgnlabel = param.sbgnlabel;

  node.removeClass('changeContent');
  node.addClass('changeContent');

  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.node) {
    handleSBGNInspector();
  }

  return result;
}

function changeStateVariable(param) {
  var result = {
  };
  var state = param.state;
  var type = param.type;
  result.state = state;
  result.type = type;
  result.valueOrVariable = state.state[type];
  result.node = param.node;
  result.width = param.width;

  state.state[type] = param.valueOrVariable;
  cy.forceRender();

  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.node) {
    fillInspectorStateAndInfos(param.node, param.width);
  }

  return result;
}

function changeUnitOfInformation(param) {
  var result = {
  };
  var state = param.state;
  result.state = state;
  result.text = state.label.text;
  result.node = param.node;
  result.width = param.width;

  state.label.text = param.text;
  cy.forceRender();

  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.node) {
    fillInspectorStateAndInfos(param.node, param.width);
  }

  return result;
}

function addStateAndInfo(param) {
  var obj = param.obj;
  var node = param.node;
  var stateAndInfos = node._private.data.sbgnstatesandinfos;

  stateAndInfos.push(obj);
  relocateStateAndInfos(stateAndInfos);
  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == node) {
    fillInspectorStateAndInfos(node, param.width);
  }
  cy.forceRender();

  var result = {
    node: node,
    width: param.width,
    obj: obj
  };
  return result;
}

function removeStateAndInfo(param) {
  var obj = param.obj;
  var node = param.node;
  var stateAndInfos = node._private.data.sbgnstatesandinfos;

  var index = stateAndInfos.indexOf(obj);
  stateAndInfos.splice(index, 1);
  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == node) {
    fillInspectorStateAndInfos(node, param.width);
  }
  relocateStateAndInfos(stateAndInfos);
  cy.forceRender();

  var result = {
    node: node,
    width: param.width,
    obj: obj
  };
  return result;
}

function changeIsMultimerStatus(param) {
  var node = param.node;
  var makeMultimer = param.makeMultimer;
  var sbgnclass = node.data('sbgnclass');
  if (makeMultimer) {
    node.data('sbgnclass', sbgnclass + ' multimer');
  }
  else {
    node.data('sbgnclass', sbgnclass.replace(' multimer', ''));
  }
  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.node) {
    $('#inspector-is-multimer').attr('checked', makeMultimer);
  }
  var result = {
    makeMultimer: !makeMultimer,
    node: node
  };
  return result;
}

function changeIsCloneMarkerStatus(param) {
  var node = param.node;
  var makeCloneMarker = param.makeCloneMarker;
  node._private.data.sbgnclonemarker = makeCloneMarker ? true : undefined;
  cy.forceRender();
  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.node) {
    $('#inspector-is-clone-marker').attr('checked', makeCloneMarker);
  }
  var result = {
    makeCloneMarker: !makeCloneMarker,
    node: node
  };
  return result;
}

function changeStyleData(param) {
  var result = {
  };
  var ele = param.ele;
  result.dataType = param.dataType;
  result.data = ele.data(param.dataType);
  result.ele = ele;

  ele.data(param.dataType, param.data);
  cy.forceRender();

  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.ele) {
    handleSBGNInspector();
  }

  return result;
}

function changeStyleCss(param) {
  var result = {
  };
  var ele = param.ele;
  result.dataType = param.dataType;
  result.data = ele.css(param.dataType);
  result.ele = ele;

  ele.css(param.dataType, param.data);
  cy.forceRender();

  if (cy.elements(":selected").length == 1 && cy.elements(":selected")[0] == param.ele) {
    handleSBGNInspector();
  }

  return result;
}

function changeBendPoints(param){
  var edge = param.edge;
  var result = {
    edge: edge,
    weights: param.set?edge.data('weights'):param.weights,
    distances: param.set?edge.data('distances'):param.distances,
    set: true//As the result will not be used for the first function call params should be used to set the data
  };

  //Check if we need to set the weights and distances by the param values
  if(param.set) {
    param.weights?edge.data('weights', param.weights):edge.removeData('weights');
    param.distances?edge.data('distances', param.distances):edge.removeData('distances');

    //refresh the curve style as the number of bend point would be changed by the previous operation
    if(param.weights){
      edge.css('curve-style', 'segments');
    }
    else {
      edge.css('curve-style', 'bezier');
    }
  }

  return result;
}

/*
 *	Base command class
 * do: reference to the function that performs actual action for this command.
 * undo: reference to the action that is reverse of this action's command.
 * params: additional parameters for this command
 */
var Command = function (_do, undo, params) {
  this._do = _do;
  this.undo = undo;
  this.params = params;
};

var AddNodeCommand = function (newNode)
{
  return new Command(addNode, removeNodes, newNode);
};

//var RemoveNodesCommand = function (nodesTobeDeleted)
//{
//  return new Command(removeNodes, restoreEles, nodesTobeDeleted);
//};

var RemoveElesCommand = function (elesTobeDeleted)
{
  return new Command(removeEles, restoreEles, elesTobeDeleted);
};

var AddEdgeCommand = function (newEdge)
{
  return new Command(addEdge, removeEdges, newEdge);
};

//var RemoveEdgesCommand = function (edgesTobeDeleted)
//{
//  return new Command(removeEdges, restoreEles, edgesTobeDeleted);
//};

var ExpandNodeCommand = function (param) {
  return new Command(expandNode, undoExpandNode, param);
};

var CollapseNodeCommand = function (param) {
  return new Command(collapseNode, undoCollapseNode, param);
};

var SimpleExpandNodeCommand = function (node) {
  return new Command(simpleExpandNode, simpleCollapseNode, node);
};

var SimpleCollapseNodeCommand = function (node) {
  return new Command(simpleCollapseNode, simpleExpandNode, node);
};

var ExpandGivenNodesCommand = function (param) {
  return new Command(expandGivenNodes, undoExpandGivenNodes, param);
};

var CollapseGivenNodesCommand = function (param) {
  return new Command(collapseGivenNodes, undoCollapseGivenNodes, param);
};

var SimpleExpandGivenNodesCommand = function (nodes) {
  return new Command(simpleExpandGivenNodes, simpleCollapseGivenNodes, nodes);
};

var SimpleCollapseGivenNodesCommand = function (nodes) {
  return new Command(simpleCollapseGivenNodes, simpleExpandGivenNodes, nodes);
};

var SimpleExpandAllNodesCommand = function (param) {
  return new Command(simpleExpandAllNodes, collapseExpandedStack);
};

var ExpandAllNodesCommand = function (param) {
  return new Command(expandAllNodes, undoExpandAllNodes, param);
};

var ReturnToPositionsAndSizesCommand = function (nodesData) {
  return new Command(returnToPositionsAndSizesConditionally, returnToPositionsAndSizes, nodesData);
};

var MoveNodeCommand = function (param) {
  return new Command(moveNodesConditionally, moveNodesReversely, param);
};

var DeleteSelectedCommand = function (param) {
  return new Command(deleteSelected, restoreSelected, param);
};

var HideSelectedCommand = function (param) {
  return new Command(hideSelected, showJustGivenNodes, param);
};

var ShowSelectedCommand = function (param) {
  return new Command(showSelected, showJustGivenNodes, param);
};

var ShowAllCommand = function () {
  return new Command(showAll, showJustGivenNodes);
};

var HighlightNeighborsofSelectedCommand = function (param) {
  param.highlightNeighboursofSelected = true;
  return new Command(highlightSelected, notHighlightEles, param);
};

var HighlightProcessesOfSelectedCommand = function (param) {
  param.highlightProcessesOfSelected = true;
  return new Command(highlightSelected, notHighlightEles, param);
};

var RemoveHighlightsCommand = function () {
  return new Command(removeHighlights, highlightSelected);
};

var CreateCompundForSelectedNodesCommand = function (param) {
  return new Command(createCompoundForSelectedNodes, removeCompound, param);
};

var ResizeNodeCommand = function (param) {
  return new Command(resizeNode, resizeNode, param);
};

var ChangeNodeLabelCommand = function (param) {
  return new Command(changeNodeLabel, changeNodeLabel, param);
};

var AddStateAndInfoCommand = function (param) {
  return new Command(addStateAndInfo, removeStateAndInfo, param);
};

var RemoveStateAndInfoCommand = function (param) {
  return new Command(removeStateAndInfo, addStateAndInfo, param);
};

var ChangeStateVariableCommand = function (param) {
  return new Command(changeStateVariable, changeStateVariable, param);
};

var ChangeUnitOfInformationCommand = function (param) {
  return new Command(changeUnitOfInformation, changeUnitOfInformation, param);
};

var ChangeStyleDataCommand = function (param) {
  return new Command(changeStyleData, changeStyleData, param);
};

var ChangeStyleCssCommand = function (param) {
  return new Command(changeStyleCss, changeStyleCss, param);
};

var changeIsMultimerStatusCommand = function (param) {
  return new Command(changeIsMultimerStatus, changeIsMultimerStatus, param);
};

var changeIsCloneMarkerStatusCommand = function (param) {
  return new Command(changeIsCloneMarkerStatus, changeIsCloneMarkerStatus, param);
};

var changeParentCommand = function (param) {
  return new Command(changeParent, changeParent, param);
};

var changeBendPointsCommand = function (param) {
  return new Command(changeBendPoints, changeBendPoints, param);
};

/**
 *  Description: A simple action manager that acts also as a undo-redo manager regarding Command Design Pattern
 *	Author: Istemi Bahceci<istemi.bahceci@gmail.com>
 */
function EditorActionsManager()
{
  this.undoStack = [];
  this.redoStack = [];

  /*
   *  Executes given command by calling do method of given command
   *  pushes the action to the undoStack after execution.
   */
  this._do = function (command)
  {
    //_do function returns the parameters for undo function
    command.undoparams = command._do(command.params);
    this.undoStack.push(command);
  };

  /*
   *  Undo last command.
   *  Pushes the reversed action to the redoStack after undo operation.
   */
  this.undo = function ()
  {
    if (this.undoStack.length == 0) {
      return;
    }
    var lastCommand = this.undoStack.pop();
    var result = lastCommand.undo(lastCommand.undoparams);
    //If undo function returns something then do function params should be refreshed
    if (result != null) {
      lastCommand.params = result;
    }
    this.redoStack.push(lastCommand);
  };

  /*
   *  Redo last command that is previously undid.
   *  This method basically calls do method for the last command that is popped of the redoStack.
   */
  this.redo = function ()
  {
    if (this.redoStack.length == 0) {
      return;
    }
    var lastCommand = this.redoStack.pop();
    this._do(lastCommand);
  };

  /*
   *
   * This method indicates whether the undo stack is empty
   */
  this.isUndoStackEmpty = function () {
    return this.undoStack.length == 0;
  }

  /*
   *
   * This method indicates whether the redo stack is empty
   */
  this.isRedoStackEmpty = function () {
    return this.redoStack.length == 0;
  }

  /*
   *  Empties undo and redo stacks !
   */
  this.reset = function ()
  {
    this.undoStack = [];
    this.redoStack = [];
  };
}
var editorActionsManager = new EditorActionsManager();

/*
 *  A sample run that gives insight about the usage of EditorActionsManager and commands
 */
function sampleRun()
{
  var editorActionsManager = new EditorActionsManager();

  // issue commands
  editorActionsManager._do(new AddNodeCommand(newNode));

  // undo redo mechanism
  editorActionsManager.undo();
  editorActionsManager.redo();

}
