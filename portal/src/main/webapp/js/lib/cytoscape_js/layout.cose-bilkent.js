(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
var FDLayoutConstants = _dereq_('./FDLayoutConstants');

function CoSEConstants() {
}

//CoSEConstants inherits static props in FDLayoutConstants
for (var prop in FDLayoutConstants) {
  CoSEConstants[prop] = FDLayoutConstants[prop];
}

CoSEConstants.DEFAULT_USE_MULTI_LEVEL_SCALING = false;
CoSEConstants.DEFAULT_RADIAL_SEPARATION = FDLayoutConstants.DEFAULT_EDGE_LENGTH;
CoSEConstants.DEFAULT_COMPONENT_SEPERATION = 60;

module.exports = CoSEConstants;

},{"./FDLayoutConstants":9}],2:[function(_dereq_,module,exports){
var FDLayoutEdge = _dereq_('./FDLayoutEdge');

function CoSEEdge(source, target, vEdge) {
  FDLayoutEdge.call(this, source, target, vEdge);
}

CoSEEdge.prototype = Object.create(FDLayoutEdge.prototype);
for (var prop in FDLayoutEdge) {
  CoSEEdge[prop] = FDLayoutEdge[prop];
}

module.exports = CoSEEdge

},{"./FDLayoutEdge":10}],3:[function(_dereq_,module,exports){
var LGraph = _dereq_('./LGraph');

function CoSEGraph(parent, graphMgr, vGraph) {
  LGraph.call(this, parent, graphMgr, vGraph);
}

CoSEGraph.prototype = Object.create(LGraph.prototype);
for (var prop in LGraph) {
  CoSEGraph[prop] = LGraph[prop];
}

module.exports = CoSEGraph;

},{"./LGraph":18}],4:[function(_dereq_,module,exports){
var LGraphManager = _dereq_('./LGraphManager');

function CoSEGraphManager(layout) {
  LGraphManager.call(this, layout);
}

CoSEGraphManager.prototype = Object.create(LGraphManager.prototype);
for (var prop in LGraphManager) {
  CoSEGraphManager[prop] = LGraphManager[prop];
}

module.exports = CoSEGraphManager;

},{"./LGraphManager":19}],5:[function(_dereq_,module,exports){
var FDLayout = _dereq_('./FDLayout');
var CoSEGraphManager = _dereq_('./CoSEGraphManager');
var CoSEGraph = _dereq_('./CoSEGraph');
var CoSENode = _dereq_('./CoSENode');
var CoSEEdge = _dereq_('./CoSEEdge');

function CoSELayout() {
  FDLayout.call(this);
}

CoSELayout.prototype = Object.create(FDLayout.prototype);

for (var prop in FDLayout) {
  CoSELayout[prop] = FDLayout[prop];
}

CoSELayout.prototype.newGraphManager = function () {
  var gm = new CoSEGraphManager(this);
  this.graphManager = gm;
  return gm;
};

CoSELayout.prototype.newGraph = function (vGraph) {
  return new CoSEGraph(null, this.graphManager, vGraph);
};

CoSELayout.prototype.newNode = function (vNode) {
  return new CoSENode(this.graphManager, vNode);
};

CoSELayout.prototype.newEdge = function (vEdge) {
  return new CoSEEdge(null, null, vEdge);
};

CoSELayout.prototype.initParameters = function () {
  FDLayout.prototype.initParameters.call(this, arguments);
  if (!this.isSubLayout) {
    if (layoutOptionsPack.idealEdgeLength < 10)
    {
      this.idealEdgeLength = 10;
    }
    else
    {
      this.idealEdgeLength = layoutOptionsPack.idealEdgeLength;
    }

    this.useSmartIdealEdgeLengthCalculation =
            layoutOptionsPack.smartEdgeLengthCalc;
    this.springConstant =
            Layout.transform(layoutOptionsPack.springStrength,
                    FDLayoutConstants.DEFAULT_SPRING_STRENGTH, 5.0, 5.0);
    this.repulsionConstant =
            Layout.transform(layoutOptionsPack.repulsionStrength,
                    FDLayoutConstants.DEFAULT_REPULSION_STRENGTH, 5.0, 5.0);
    this.gravityConstant =
            Layout.transform(layoutOptionsPack.gravityStrength,
                    FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH);
    this.compoundGravityConstant =
            Layout.transform(layoutOptionsPack.compoundGravityStrength,
                    FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH);
    this.gravityRangeFactor =
            Layout.transform(layoutOptionsPack.gravityRange,
                    FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR);
    this.compoundGravityRangeFactor =
            Layout.transform(layoutOptionsPack.compoundGravityRange,
                    FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR);
  }
};

CoSELayout.prototype.layout = function () {
  var createBendsAsNeeded = layoutOptionsPack.createBendsAsNeeded;
  if (createBendsAsNeeded)
  {
    this.createBendpoints();
    this.graphManager.resetAllEdges();
  }

  this.level = 0;
  return this.classicLayout();
};

CoSELayout.prototype.classicLayout = function () {
  this.calculateNodesToApplyGravitationTo();
  this.graphManager.calcLowestCommonAncestors();
  this.graphManager.calcInclusionTreeDepths();
  this.graphManager.getRoot().calcEstimatedSize();
  this.calcIdealEdgeLengths();
  if (!this.incremental)
  {
    var forest = this.getFlatForest();

    // The graph associated with this layout is flat and a forest
    if (forest.length > 0)

    {
      this.positionNodesRadially(forest);
    }
    // The graph associated with this layout is not flat or a forest
    else
    {
      this.positionNodesRandomly();
    }
  }

  this.initSpringEmbedder();
  this.runSpringEmbedder();

  console.log("Classic CoSE layout finished after " +
          this.totalIterations + " iterations");

  return true;
};

CoSELayout.prototype.runSpringEmbedder = function () {
  var lastFrame = new Date().getTime();
  var initialAnimationPeriod = 25;
  var animationPeriod = initialAnimationPeriod;
  do
  {
    this.totalIterations++;

    if (this.totalIterations % FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
    {
      if (this.isConverged())
      {
        break;
      }

      this.coolingFactor = this.initialCoolingFactor *
              ((this.maxIterations - this.totalIterations) / this.maxIterations);
      animationPeriod = Math.ceil(initialAnimationPeriod * Math.sqrt(this.coolingFactor));

    }
    this.totalDisplacement = 0;
    this.graphManager.updateBounds();
    this.calcSpringForces();
    this.calcRepulsionForces();
    this.calcGravitationalForces();
    this.moveNodes();
    this.animate();
    if (layoutOptionsPack.animate && this.totalIterations % animationPeriod == 0) {
      for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - lastFrame) > 25) {
          break;
        }
      }
      lastFrame = new Date().getTime();
      var allNodes = this.graphManager.getAllNodes();
      var pData = {};
      for (var i = 0; i < allNodes.length; i++) {
        var rect = allNodes[i].rect;
        var id = allNodes[i].id;
        pData[id] = {
          id: id,
          x: rect.getCenterX(),
          y: rect.getCenterY(),
          w: rect.width,
          h: rect.height
        };
      }
      broadcast({pData: pData});
    }
  }
  while (this.totalIterations < this.maxIterations);

  this.graphManager.updateBounds();
};

CoSELayout.prototype.calculateNodesToApplyGravitationTo = function () {
  var nodeList = [];
  var graph;

  var graphs = this.graphManager.getGraphs();
  var size = graphs.length;
  var i;
  for (i = 0; i < size; i++)
  {
    graph = graphs[i];

    graph.updateConnected();

    if (!graph.isConnected)
    {
      nodeList = nodeList.concat(graph.getNodes());
    }
  }

  this.graphManager.setAllNodesToApplyGravitation(nodeList);
};

CoSELayout.prototype.createBendpoints = function () {
  var edges = [];
  edges = edges.concat(this.graphManager.getAllEdges());
  var visited = new HashSet();
  var i;
  for (i = 0; i < edges.length; i++)
  {
    var edge = edges[i];

    if (!visited.contains(edge))
    {
      var source = edge.getSource();
      var target = edge.getTarget();

      if (source == target)
      {
        edge.getBendpoints().push(new PointD());
        edge.getBendpoints().push(new PointD());
        this.createDummyNodesForBendpoints(edge);
        visited.add(edge);
      }
      else
      {
        var edgeList = [];

        edgeList = edgeList.concat(source.getEdgeListToNode(target));
        edgeList = edgeList.concat(target.getEdgeListToNode(source));

        if (!visited.contains(edgeList[0]))
        {
          if (edgeList.length > 1)
          {
            var k;
            for (k = 0; k < edgeList.length; k++)
            {
              var multiEdge = edgeList[k];
              multiEdge.getBendpoints().push(new PointD());
              this.createDummyNodesForBendpoints(multiEdge);
            }
          }
          visited.addAll(list);
        }
      }
    }

    if (visited.size() == edges.length)
    {
      break;
    }
  }
};

CoSELayout.prototype.positionNodesRadially = function (forest) {
  // We tile the trees to a grid row by row; first tree starts at (0,0)
  var currentStartingPoint = new Point(0, 0);
  var numberOfColumns = Math.ceil(Math.sqrt(forest.length));
  var height = 0;
  var currentY = 0;
  var currentX = 0;
  var point = new PointD(0, 0);

  for (var i = 0; i < forest.length; i++)
  {
    if (i % numberOfColumns == 0)
    {
      // Start of a new row, make the x coordinate 0, increment the
      // y coordinate with the max height of the previous row
      currentX = 0;
      currentY = height;

      if (i != 0)
      {
        currentY += CoSEConstants.DEFAULT_COMPONENT_SEPERATION;
      }

      height = 0;
    }

    var tree = forest[i];

    // Find the center of the tree
    var centerNode = Layout.findCenterOfTree(tree);

    // Set the staring point of the next tree
    currentStartingPoint.x = currentX;
    currentStartingPoint.y = currentY;

    // Do a radial layout starting with the center
    point =
            CoSELayout.radialLayout(tree, centerNode, currentStartingPoint);

    if (point.y > height)
    {
      height = Math.floor(point.y);
    }

    currentX = Math.floor(point.x + CoSEConstants.DEFAULT_COMPONENT_SEPERATION);
  }

  this.transform(
          new PointD(LayoutConstants.WORLD_CENTER_X - point.x / 2,
                  LayoutConstants.WORLD_CENTER_Y - point.y / 2));
};

CoSELayout.radialLayout = function (tree, centerNode, startingPoint) {
  var radialSep = Math.max(this.maxDiagonalInTree(tree),
          CoSEConstants.DEFAULT_RADIAL_SEPARATION);
  CoSELayout.branchRadialLayout(centerNode, null, 0, 359, 0, radialSep);
  var bounds = LGraph.calculateBounds(tree);

  var transform = new Transform();
  transform.setDeviceOrgX(bounds.getMinX());
  transform.setDeviceOrgY(bounds.getMinY());
  transform.setWorldOrgX(startingPoint.x);
  transform.setWorldOrgY(startingPoint.y);

  for (var i = 0; i < tree.length; i++)
  {
    var node = tree[i];
    node.transform(transform);
  }

  var bottomRight =
          new PointD(bounds.getMaxX(), bounds.getMaxY());

  return transform.inverseTransformPoint(bottomRight);
};

CoSELayout.branchRadialLayout = function (node, parentOfNode, startAngle, endAngle, distance, radialSeparation) {
  // First, position this node by finding its angle.
  var halfInterval = ((endAngle - startAngle) + 1) / 2;

  if (halfInterval < 0)
  {
    halfInterval += 180;
  }

  var nodeAngle = (halfInterval + startAngle) % 360;
  var teta = (nodeAngle * IGeometry.TWO_PI) / 360;

  // Make polar to java cordinate conversion.
  var cos_teta = Math.cos(teta);
  var x_ = distance * Math.cos(teta);
  var y_ = distance * Math.sin(teta);

  node.setCenter(x_, y_);

  // Traverse all neighbors of this node and recursively call this
  // function.
  var neighborEdges = [];
  neighborEdges = neighborEdges.concat(node.getEdges());
  var childCount = neighborEdges.length;

  if (parentOfNode != null)
  {
    childCount--;
  }

  var branchCount = 0;

  var incEdgesCount = neighborEdges.length;
  var startIndex;

  var edges = node.getEdgesBetween(parentOfNode);

  // If there are multiple edges, prune them until there remains only one
  // edge.
  while (edges.length > 1)
  {
    //neighborEdges.remove(edges.remove(0));
    var temp = edges[0];
    edges.splice(0, 1);
    var index = neighborEdges.indexOf(temp);
    if (index >= 0) {
      neighborEdges.splice(index, 1);
    }
    incEdgesCount--;
    childCount--;
  }

  if (parentOfNode != null)
  {
    //assert edges.length == 1;
    startIndex = (neighborEdges.indexOf(edges[0]) + 1) % incEdgesCount;
  }
  else
  {
    startIndex = 0;
  }

  var stepAngle = Math.abs(endAngle - startAngle) / childCount;

  for (var i = startIndex;
          branchCount != childCount;
          i = (++i) % incEdgesCount)
  {
    var currentNeighbor =
            neighborEdges[i].getOtherEnd(node);

    // Don't back traverse to root node in current tree.
    if (currentNeighbor == parentOfNode)
    {
      continue;
    }

    var childStartAngle =
            (startAngle + branchCount * stepAngle) % 360;
    var childEndAngle = (childStartAngle + stepAngle) % 360;

    CoSELayout.branchRadialLayout(currentNeighbor,
            node,
            childStartAngle, childEndAngle,
            distance + radialSeparation, radialSeparation);

    branchCount++;
  }
};

CoSELayout.maxDiagonalInTree = function (tree) {
  var maxDiagonal = Integer.MIN_VALUE;

  for (var i = 0; i < tree.length; i++)
  {
    var node = tree[i];
    var diagonal = node.getDiagonal();

    if (diagonal > maxDiagonal)
    {
      maxDiagonal = diagonal;
    }
  }

  return maxDiagonal;
};

CoSELayout.prototype.calcRepulsionRange = function () {
  // formula is 2 x (level + 1) x idealEdgeLength
  return (2 * (this.level + 1) * this.idealEdgeLength);
};

module.exports = CoSELayout;

},{"./CoSEEdge":2,"./CoSEGraph":3,"./CoSEGraphManager":4,"./CoSENode":6,"./FDLayout":8}],6:[function(_dereq_,module,exports){
var FDLayoutNode = _dereq_('./FDLayoutNode');

function CoSENode(gm, loc, size, vNode) {
  FDLayoutNode.call(this, gm, loc, size, vNode);
}


CoSENode.prototype = Object.create(FDLayoutNode.prototype);
for (var prop in FDLayoutNode) {
  CoSENode[prop] = FDLayoutNode[prop];
}

CoSENode.prototype.move = function ()
{
  var layout = this.graphManager.getLayout();
  this.displacementX = layout.coolingFactor *
          (this.springForceX + this.repulsionForceX + this.gravitationForceX);
  this.displacementY = layout.coolingFactor *
          (this.springForceY + this.repulsionForceY + this.gravitationForceY);


  if (Math.abs(this.displacementX) > layout.coolingFactor * layout.maxNodeDisplacement)
  {
    this.displacementX = layout.coolingFactor * layout.maxNodeDisplacement *
            IMath.sign(this.displacementX);
  }

  if (Math.abs(this.displacementY) > layout.coolingFactor * layout.maxNodeDisplacement)
  {
    this.displacementY = layout.coolingFactor * layout.maxNodeDisplacement *
            IMath.sign(this.displacementY);
  }

  // a simple node, just move it
  if (this.child == null)
  {
    this.moveBy(this.displacementX, this.displacementY);
  }
  // an empty compound node, again just move it
  else if (this.child.getNodes().length == 0)
  {
    this.moveBy(this.displacementX, this.displacementY);
  }
  // non-empty compound node, propogate movement to children as well
  else
  {
    this.propogateDisplacementToChildren(this.displacementX,
            this.displacementY);
  }

  layout.totalDisplacement +=
          Math.abs(this.displacementX) + Math.abs(this.displacementY);

  this.springForceX = 0;
  this.springForceY = 0;
  this.repulsionForceX = 0;
  this.repulsionForceY = 0;
  this.gravitationForceX = 0;
  this.gravitationForceY = 0;
  this.displacementX = 0;
  this.displacementY = 0;
};

CoSENode.prototype.propogateDisplacementToChildren = function (dX, dY)
{
  var nodes = this.getChild().getNodes();
  var node;
  for (var i = 0; i < nodes.length; i++)
  {
    node = nodes[i];
    if (node.getChild() == null)
    {
      node.moveBy(dX, dY);
      node.displacementX += dX;
      node.displacementY += dY;
    }
    else
    {
      node.propogateDisplacementToChildren(dX, dY);
    }
  }
};

CoSENode.prototype.setPred1 = function (pred1)
{
  this.pred1 = pred1;
};

CoSENode.prototype.getPred1 = function ()
{
  return pred1;
};

CoSENode.prototype.getPred2 = function ()
{
  return pred2;
};

CoSENode.prototype.setNext = function (next)
{
  this.next = next;
};

CoSENode.prototype.getNext = function ()
{
  return next;
};

CoSENode.prototype.setProcessed = function (processed)
{
  this.processed = processed;
};

CoSENode.prototype.isProcessed = function ()
{
  return processed;
};

module.exports = CoSENode;

},{"./FDLayoutNode":11}],7:[function(_dereq_,module,exports){
function DimensionD(width, height) {
  this.width = 0;
  this.height = 0;
  if (width !== null && height !== null) {
    this.height = height;
    this.width = width;
  }
}

DimensionD.prototype.getWidth = function ()
{
  return this.width;
};

DimensionD.prototype.setWidth = function (width)
{
  this.width = width;
};

DimensionD.prototype.getHeight = function ()
{
  return this.height;
};

DimensionD.prototype.setHeight = function (height)
{
  this.height = height;
};

module.exports = DimensionD;

},{}],8:[function(_dereq_,module,exports){
var Layout = _dereq_('./Layout');
var FDLayoutConstants = _dereq_('./FDLayoutConstants');

function FDLayout() {
  Layout.call(this);

  this.useSmartIdealEdgeLengthCalculation = FDLayoutConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;
  this.idealEdgeLength = FDLayoutConstants.DEFAULT_EDGE_LENGTH;
  this.springConstant = FDLayoutConstants.DEFAULT_SPRING_STRENGTH;
  this.repulsionConstant = FDLayoutConstants.DEFAULT_REPULSION_STRENGTH;
  this.gravityConstant = FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH;
  this.compoundGravityConstant = FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH;
  this.gravityRangeFactor = FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR;
  this.compoundGravityRangeFactor = FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR;
  this.displacementThresholdPerNode = (3.0 * FDLayoutConstants.DEFAULT_EDGE_LENGTH) / 100;
  this.coolingFactor = 1.0;
  this.initialCoolingFactor = 1.0;
  this.totalDisplacement = 0.0;
  this.oldTotalDisplacement = 0.0;
  this.maxIterations = FDLayoutConstants.MAX_ITERATIONS;
}

FDLayout.prototype = Object.create(Layout.prototype);

for (var prop in Layout) {
  FDLayout[prop] = Layout[prop];
}

FDLayout.prototype.initParameters = function () {
  Layout.prototype.initParameters.call(this, arguments);

  if (this.layoutQuality == LayoutConstants.DRAFT_QUALITY)
  {
    this.displacementThresholdPerNode += 0.30;
    this.maxIterations *= 0.8;
  }
  else if (this.layoutQuality == LayoutConstants.PROOF_QUALITY)
  {
    this.displacementThresholdPerNode -= 0.30;
    this.maxIterations *= 1.2;
  }

  this.totalIterations = 0;
  this.notAnimatedIterations = 0;

//    this.useFRGridVariant = layoutOptionsPack.smartRepulsionRangeCalc;
};

FDLayout.prototype.calcIdealEdgeLengths = function () {
  var edge;
  var lcaDepth;
  var source;
  var target;
  var sizeOfSourceInLca;
  var sizeOfTargetInLca;

  var allEdges = this.getGraphManager().getAllEdges();
  for (var i = 0; i < allEdges.length; i++)
  {
    edge = allEdges[i];

    edge.idealLength = this.idealEdgeLength;

    if (edge.isInterGraph)
    {
      source = edge.getSource();
      target = edge.getTarget();

      sizeOfSourceInLca = edge.getSourceInLca().getEstimatedSize();
      sizeOfTargetInLca = edge.getTargetInLca().getEstimatedSize();

      if (this.useSmartIdealEdgeLengthCalculation)
      {
        edge.idealLength += sizeOfSourceInLca + sizeOfTargetInLca -
                2 * LayoutConstants.SIMPLE_NODE_SIZE;
      }

      lcaDepth = edge.getLca().getInclusionTreeDepth();

      edge.idealLength += FDLayoutConstants.DEFAULT_EDGE_LENGTH *
              FDLayoutConstants.PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR *
              (source.getInclusionTreeDepth() +
                      target.getInclusionTreeDepth() - 2 * lcaDepth);
    }
  }
};

FDLayout.prototype.initSpringEmbedder = function () {

  if (this.incremental)
  {
    this.coolingFactor = 0.8;
    this.initialCoolingFactor = 0.8;
    this.maxNodeDisplacement =
            FDLayoutConstants.MAX_NODE_DISPLACEMENT_INCREMENTAL;
  }
  else
  {
    this.coolingFactor = 1.0;
    this.initialCoolingFactor = 1.0;
    this.maxNodeDisplacement =
            FDLayoutConstants.MAX_NODE_DISPLACEMENT;
  }

  this.maxIterations =
          Math.max(this.getAllNodes().length * 5, this.maxIterations);

  this.totalDisplacementThreshold =
          this.displacementThresholdPerNode * this.getAllNodes().length;

  this.repulsionRange = this.calcRepulsionRange();
};

FDLayout.prototype.calcSpringForces = function () {
  var lEdges = this.getAllEdges();
  var edge;

  for (var i = 0; i < lEdges.length; i++)
  {
    edge = lEdges[i];

    this.calcSpringForce(edge, edge.idealLength);
  }
};

FDLayout.prototype.calcRepulsionForces = function () {
  var i, j;
  var nodeA, nodeB;
  var lNodes = this.getAllNodes();

  for (i = 0; i < lNodes.length; i++)
  {
    nodeA = lNodes[i];

    for (j = i + 1; j < lNodes.length; j++)
    {
      nodeB = lNodes[j];

      // If both nodes are not members of the same graph, skip.
      if (nodeA.getOwner() != nodeB.getOwner())
      {
        continue;
      }

      this.calcRepulsionForce(nodeA, nodeB);
    }
  }
};

FDLayout.prototype.calcGravitationalForces = function () {
  var node;
  var lNodes = this.getAllNodesToApplyGravitation();

  for (var i = 0; i < lNodes.length; i++)
  {
    node = lNodes[i];
    this.calcGravitationalForce(node);
  }
};

FDLayout.prototype.moveNodes = function () {
  var lNodes = this.getAllNodes();
  var node;

  for (var i = 0; i < lNodes.length; i++)
  {
    node = lNodes[i];
    node.move();
  }
}

FDLayout.prototype.calcSpringForce = function (edge, idealLength) {
  var sourceNode = edge.getSource();
  var targetNode = edge.getTarget();

  var length;
  var springForce;
  var springForceX;
  var springForceY;

  // Update edge length
  if (this.uniformLeafNodeSizes &&
          sourceNode.getChild() == null && targetNode.getChild() == null)
  {
    edge.updateLengthSimple();
  }
  else
  {
    edge.updateLength();

    if (edge.isOverlapingSourceAndTarget)
    {
      return;
    }
  }

  length = edge.getLength();

  // Calculate spring forces
  springForce = this.springConstant * (length - idealLength);

  // Project force onto x and y axes
  springForceX = springForce * (edge.lengthX / length);
  springForceY = springForce * (edge.lengthY / length);

  // Apply forces on the end nodes
  sourceNode.springForceX += springForceX;
  sourceNode.springForceY += springForceY;
  targetNode.springForceX -= springForceX;
  targetNode.springForceY -= springForceY;
};

FDLayout.prototype.calcRepulsionForce = function (nodeA, nodeB) {
  var rectA = nodeA.getRect();
  var rectB = nodeB.getRect();
  var overlapAmount = new Array(2);
  var clipPoints = new Array(4);
  var distanceX;
  var distanceY;
  var distanceSquared;
  var distance;
  var repulsionForce;
  var repulsionForceX;
  var repulsionForceY;

  if (rectA.intersects(rectB))// two nodes overlap
  {
    // calculate separation amount in x and y directions
    IGeometry.calcSeparationAmount(rectA,
            rectB,
            overlapAmount,
            FDLayoutConstants.DEFAULT_EDGE_LENGTH / 2.0);

    repulsionForceX = overlapAmount[0];
    repulsionForceY = overlapAmount[1];
  }
  else// no overlap
  {
    // calculate distance

    if (this.uniformLeafNodeSizes &&
            nodeA.getChild() == null && nodeB.getChild() == null)// simply base repulsion on distance of node centers
    {
      distanceX = rectB.getCenterX() - rectA.getCenterX();
      distanceY = rectB.getCenterY() - rectA.getCenterY();
    }
    else// use clipping points
    {
      IGeometry.getIntersection(rectA, rectB, clipPoints);

      distanceX = clipPoints[2] - clipPoints[0];
      distanceY = clipPoints[3] - clipPoints[1];
    }

    // No repulsion range. FR grid variant should take care of this.
    if (Math.abs(distanceX) < FDLayoutConstants.MIN_REPULSION_DIST)
    {
      distanceX = IMath.sign(distanceX) *
              FDLayoutConstants.MIN_REPULSION_DIST;
    }

    if (Math.abs(distanceY) < FDLayoutConstants.MIN_REPULSION_DIST)
    {
      distanceY = IMath.sign(distanceY) *
              FDLayoutConstants.MIN_REPULSION_DIST;
    }

    distanceSquared = distanceX * distanceX + distanceY * distanceY;
    distance = Math.sqrt(distanceSquared);

    repulsionForce = this.repulsionConstant / distanceSquared;

    // Project force onto x and y axes
    repulsionForceX = repulsionForce * distanceX / distance;
    repulsionForceY = repulsionForce * distanceY / distance;
  }

  // Apply forces on the two nodes
  nodeA.repulsionForceX -= repulsionForceX;
  nodeA.repulsionForceY -= repulsionForceY;
  nodeB.repulsionForceX += repulsionForceX;
  nodeB.repulsionForceY += repulsionForceY;
};

FDLayout.prototype.calcGravitationalForce = function (node) {
  var ownerGraph;
  var ownerCenterX;
  var ownerCenterY;
  var distanceX;
  var distanceY;
  var absDistanceX;
  var absDistanceY;
  var estimatedSize;
  ownerGraph = node.getOwner();

  ownerCenterX = (ownerGraph.getRight() + ownerGraph.getLeft()) / 2;
  ownerCenterY = (ownerGraph.getTop() + ownerGraph.getBottom()) / 2;
  distanceX = node.getCenterX() - ownerCenterX;
  distanceY = node.getCenterY() - ownerCenterY;
  absDistanceX = Math.abs(distanceX);
  absDistanceY = Math.abs(distanceY);

  if (node.getOwner() == this.graphManager.getRoot())// in the root graph
  {
    Math.floor(80);
    estimatedSize = Math.floor(ownerGraph.getEstimatedSize() *
            this.gravityRangeFactor);

    if (absDistanceX > estimatedSize || absDistanceY > estimatedSize)
    {
      node.gravitationForceX = -this.gravityConstant * distanceX;
      node.gravitationForceY = -this.gravityConstant * distanceY;
    }
  }
  else// inside a compound
  {
    estimatedSize = Math.floor((ownerGraph.getEstimatedSize() *
            this.compoundGravityRangeFactor));

    if (absDistanceX > estimatedSize || absDistanceY > estimatedSize)
    {
      node.gravitationForceX = -this.gravityConstant * distanceX *
              this.compoundGravityConstant;
      node.gravitationForceY = -this.gravityConstant * distanceY *
              this.compoundGravityConstant;
    }
  }
};

FDLayout.prototype.isConverged = function () {
  var converged;
  var oscilating = false;

  if (this.totalIterations > this.maxIterations / 3)
  {
    oscilating =
            Math.abs(this.totalDisplacement - this.oldTotalDisplacement) < 2;
  }

  converged = this.totalDisplacement < this.totalDisplacementThreshold;

  this.oldTotalDisplacement = this.totalDisplacement;

  return converged || oscilating;
};

FDLayout.prototype.animate = function () {
  if (this.animationDuringLayout && !this.isSubLayout)
  {
    if (this.notAnimatedIterations == this.animationPeriod)
    {
      this.update();
      this.notAnimatedIterations = 0;
    }
    else
    {
      this.notAnimatedIterations++;
    }
  }
};

FDLayout.prototype.calcRepulsionRange = function () {
  return 0.0;
};

module.exports = FDLayout;

},{"./FDLayoutConstants":9,"./Layout":22}],9:[function(_dereq_,module,exports){
var layoutOptionsPack = _dereq_('./layoutOptionsPack');

function FDLayoutConstants() {
}

FDLayoutConstants.getUserOptions = function (options) {
  if (options.nodeRepulsion != null)
    FDLayoutConstants.DEFAULT_REPULSION_STRENGTH = options.nodeRepulsion;
  if (options.idealEdgeLength != null) {
    FDLayoutConstants.DEFAULT_EDGE_LENGTH = options.idealEdgeLength;
  }
  if (options.edgeElasticity != null)
    FDLayoutConstants.DEFAULT_SPRING_STRENGTH = options.edgeElasticity;
  if (options.nestingFactor != null)
    FDLayoutConstants.PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR = options.nestingFactor;
  if (options.gravity != null)
    FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH = options.gravity;
  if (options.numIter != null)
    FDLayoutConstants.MAX_ITERATIONS = options.numIter;
  
  layoutOptionsPack.incremental = !(options.randomize);
  layoutOptionsPack.animate = options.animate;
}

FDLayoutConstants.MAX_ITERATIONS = 2500;

FDLayoutConstants.DEFAULT_EDGE_LENGTH = 50;
FDLayoutConstants.DEFAULT_SPRING_STRENGTH = 0.45;
FDLayoutConstants.DEFAULT_REPULSION_STRENGTH = 4500.0;
FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH = 0.4;
FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH = 1.0;
FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR = 2.0;
FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR = 1.5;
FDLayoutConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION = true;
FDLayoutConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION = true;
FDLayoutConstants.MAX_NODE_DISPLACEMENT_INCREMENTAL = 100.0;
FDLayoutConstants.MAX_NODE_DISPLACEMENT = FDLayoutConstants.MAX_NODE_DISPLACEMENT_INCREMENTAL * 3;
FDLayoutConstants.MIN_REPULSION_DIST = FDLayoutConstants.DEFAULT_EDGE_LENGTH / 10.0;
FDLayoutConstants.CONVERGENCE_CHECK_PERIOD = 100;
FDLayoutConstants.PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR = 0.1;
FDLayoutConstants.MIN_EDGE_LENGTH = 1;
FDLayoutConstants.GRID_CALCULATION_CHECK_PERIOD = 10;

module.exports = FDLayoutConstants;

},{"./layoutOptionsPack":31}],10:[function(_dereq_,module,exports){
var LEdge = _dereq_('./LEdge');
var FDLayoutConstants = _dereq_('./FDLayoutConstants');

function FDLayoutEdge(source, target, vEdge) {
  LEdge.call(this, source, target, vEdge);
  this.idealLength = FDLayoutConstants.DEFAULT_EDGE_LENGTH;
}

FDLayoutEdge.prototype = Object.create(LEdge.prototype);

for (var prop in LEdge) {
  FDLayoutEdge[prop] = LEdge[prop];
}

module.exports = FDLayoutEdge;

},{"./FDLayoutConstants":9,"./LEdge":17}],11:[function(_dereq_,module,exports){
var LNode = _dereq_('./LNode');

function FDLayoutNode(gm, loc, size, vNode) {
  // alternative constructor is handled inside LNode
  LNode.call(this, gm, loc, size, vNode);
  //Spring, repulsion and gravitational forces acting on this node
  this.springForceX = 0;
  this.springForceY = 0;
  this.repulsionForceX = 0;
  this.repulsionForceY = 0;
  this.gravitationForceX = 0;
  this.gravitationForceY = 0;
  //Amount by which this node is to be moved in this iteration
  this.displacementX = 0;
  this.displacementY = 0;

  //Start and finish grid coordinates that this node is fallen into
  this.startX = 0;
  this.finishX = 0;
  this.startY = 0;
  this.finishY = 0;

  //Geometric neighbors of this node
  this.surrounding = [];
}

FDLayoutNode.prototype = Object.create(LNode.prototype);

for (var prop in LNode) {
  FDLayoutNode[prop] = LNode[prop];
}

FDLayoutNode.prototype.setGridCoordinates = function (_startX, _finishX, _startY, _finishY)
{
  this.startX = _startX;
  this.finishX = _finishX;
  this.startY = _startY;
  this.finishY = _finishY;

};

module.exports = FDLayoutNode;

},{"./LNode":21}],12:[function(_dereq_,module,exports){
var UniqueIDGeneretor = _dereq_('./UniqueIDGeneretor');

function HashMap() {
  this.map = {};
  this.keys = [];
}

HashMap.prototype.put = function (key, value) {
  var theId = UniqueIDGeneretor.createID(key);
  if (!this.contains(theId)) {
    this.map[theId] = value;
    this.keys.push(key);
  }
};

HashMap.prototype.contains = function (key) {
  var theId = UniqueIDGeneretor.createID(key);
  return this.map[key] != null;
};

HashMap.prototype.get = function (key) {
  var theId = UniqueIDGeneretor.createID(key);
  return this.map[theId];
};

HashMap.prototype.keySet = function () {
  return this.keys;
};

module.exports = HashMap;

},{"./UniqueIDGeneretor":29}],13:[function(_dereq_,module,exports){
var UniqueIDGeneretor = _dereq_('./UniqueIDGeneretor');

function HashSet() {
  this.set = {};
}
;

HashSet.prototype.add = function (obj) {
  var theId = UniqueIDGeneretor.createID(obj);
  if (!this.contains(theId))
    this.set[theId] = obj;
};

HashSet.prototype.remove = function (obj) {
  delete this.set[UniqueIDGeneretor.createID(obj)];
};

HashSet.prototype.clear = function () {
  this.set = {};
};

HashSet.prototype.contains = function (obj) {
  return this.set[UniqueIDGeneretor.createID(obj)] == obj;
};

HashSet.prototype.isEmpty = function () {
  return this.size() === 0;
};

HashSet.prototype.size = function () {
  return Object.keys(this.set).length;
};

//concats this.set to the given list
HashSet.prototype.addAllTo = function (list) {
  var keys = Object.keys(this.set);
  var length = keys.length;
  for (var i = 0; i < length; i++) {
    list.push(this.set[keys[i]]);
  }
};

HashSet.prototype.size = function () {
  return Object.keys(this.set).length;
};

HashSet.prototype.addAll = function (list) {
  var s = list.length;
  for (var i = 0; i < s; i++) {
    var v = list[i];
    this.add(v);
  }
};

module.exports = HashSet;

},{"./UniqueIDGeneretor":29}],14:[function(_dereq_,module,exports){
function IGeometry() {
}

IGeometry.calcSeparationAmount = function (rectA, rectB, overlapAmount, separationBuffer)
{
  if (!rectA.intersects(rectB)) {
    throw "assert failed";
  }
  var directions = new Array(2);
  IGeometry.decideDirectionsForOverlappingNodes(rectA, rectB, directions);
  overlapAmount[0] = Math.min(rectA.getRight(), rectB.getRight()) -
          Math.max(rectA.x, rectB.x);
  overlapAmount[1] = Math.min(rectA.getBottom(), rectB.getBottom()) -
          Math.max(rectA.y, rectB.y);
  // update the overlapping amounts for the following cases:
  if ((rectA.getX() <= rectB.getX()) && (rectA.getRight() >= rectB.getRight()))
  {
    overlapAmount[0] += Math.min((rectB.getX() - rectA.getX()),
            (rectA.getRight() - rectB.getRight()));
  }
  else if ((rectB.getX() <= rectA.getX()) && (rectB.getRight() >= rectA.getRight()))
  {
    overlapAmount[0] += Math.min((rectA.getX() - rectB.getX()),
            (rectB.getRight() - rectA.getRight()));
  }
  if ((rectA.getY() <= rectB.getY()) && (rectA.getBottom() >= rectB.getBottom()))
  {
    overlapAmount[1] += Math.min((rectB.getY() - rectA.getY()),
            (rectA.getBottom() - rectB.getBottom()));
  }
  else if ((rectB.getY() <= rectA.getY()) && (rectB.getBottom() >= rectA.getBottom()))
  {
    overlapAmount[1] += Math.min((rectA.getY() - rectB.getY()),
            (rectB.getBottom() - rectA.getBottom()));
  }

  // find slope of the line passes two centers
  var slope = Math.abs((rectB.getCenterY() - rectA.getCenterY()) /
          (rectB.getCenterX() - rectA.getCenterX()));
  // if centers are overlapped
  if ((rectB.getCenterY() == rectA.getCenterY()) &&
          (rectB.getCenterX() == rectA.getCenterX()))
  {
    // assume the slope is 1 (45 degree)
    slope = 1.0;
  }

  var moveByY = slope * overlapAmount[0];
  var moveByX = overlapAmount[1] / slope;
  if (overlapAmount[0] < moveByX)
  {
    moveByX = overlapAmount[0];
  }
  else
  {
    moveByY = overlapAmount[1];
  }
  // return half the amount so that if each rectangle is moved by these
  // amounts in opposite directions, overlap will be resolved
  overlapAmount[0] = -1 * directions[0] * ((moveByX / 2) + separationBuffer);
  overlapAmount[1] = -1 * directions[1] * ((moveByY / 2) + separationBuffer);
}

IGeometry.decideDirectionsForOverlappingNodes = function (rectA, rectB, directions)
{
  if (rectA.getCenterX() < rectB.getCenterX())
  {
    directions[0] = -1;
  }
  else
  {
    directions[0] = 1;
  }

  if (rectA.getCenterY() < rectB.getCenterY())
  {
    directions[1] = -1;
  }
  else
  {
    directions[1] = 1;
  }
}

IGeometry.getIntersection2 = function (rectA, rectB, result)
{
  //result[0-1] will contain clipPoint of rectA, result[2-3] will contain clipPoint of rectB
  var p1x = rectA.getCenterX();
  var p1y = rectA.getCenterY();
  var p2x = rectB.getCenterX();
  var p2y = rectB.getCenterY();

  //if two rectangles intersect, then clipping points are centers
  if (rectA.intersects(rectB))
  {
    result[0] = p1x;
    result[1] = p1y;
    result[2] = p2x;
    result[3] = p2y;
    return true;
  }
  //variables for rectA
  var topLeftAx = rectA.getX();
  var topLeftAy = rectA.getY();
  var topRightAx = rectA.getRight();
  var bottomLeftAx = rectA.getX();
  var bottomLeftAy = rectA.getBottom();
  var bottomRightAx = rectA.getRight();
  var halfWidthA = rectA.getWidthHalf();
  var halfHeightA = rectA.getHeightHalf();
  //variables for rectB
  var topLeftBx = rectB.getX();
  var topLeftBy = rectB.getY();
  var topRightBx = rectB.getRight();
  var bottomLeftBx = rectB.getX();
  var bottomLeftBy = rectB.getBottom();
  var bottomRightBx = rectB.getRight();
  var halfWidthB = rectB.getWidthHalf();
  var halfHeightB = rectB.getHeightHalf();
  //flag whether clipping points are found
  var clipPointAFound = false;
  var clipPointBFound = false;

  // line is vertical
  if (p1x == p2x)
  {
    if (p1y > p2y)
    {
      result[0] = p1x;
      result[1] = topLeftAy;
      result[2] = p2x;
      result[3] = bottomLeftBy;
      return false;
    }
    else if (p1y < p2y)
    {
      result[0] = p1x;
      result[1] = bottomLeftAy;
      result[2] = p2x;
      result[3] = topLeftBy;
      return false;
    }
    else
    {
      //not line, return null;
    }
  }
  // line is horizontal
  else if (p1y == p2y)
  {
    if (p1x > p2x)
    {
      result[0] = topLeftAx;
      result[1] = p1y;
      result[2] = topRightBx;
      result[3] = p2y;
      return false;
    }
    else if (p1x < p2x)
    {
      result[0] = topRightAx;
      result[1] = p1y;
      result[2] = topLeftBx;
      result[3] = p2y;
      return false;
    }
    else
    {
      //not valid line, return null;
    }
  }
  else
  {
    //slopes of rectA's and rectB's diagonals
    var slopeA = rectA.height / rectA.width;
    var slopeB = rectB.height / rectB.width;

    //slope of line between center of rectA and center of rectB
    var slopePrime = (p2y - p1y) / (p2x - p1x);
    var cardinalDirectionA;
    var cardinalDirectionB;
    var tempPointAx;
    var tempPointAy;
    var tempPointBx;
    var tempPointBy;

    //determine whether clipping point is the corner of nodeA
    if ((-slopeA) == slopePrime)
    {
      if (p1x > p2x)
      {
        result[0] = bottomLeftAx;
        result[1] = bottomLeftAy;
        clipPointAFound = true;
      }
      else
      {
        result[0] = topRightAx;
        result[1] = topLeftAy;
        clipPointAFound = true;
      }
    }
    else if (slopeA == slopePrime)
    {
      if (p1x > p2x)
      {
        result[0] = topLeftAx;
        result[1] = topLeftAy;
        clipPointAFound = true;
      }
      else
      {
        result[0] = bottomRightAx;
        result[1] = bottomLeftAy;
        clipPointAFound = true;
      }
    }

    //determine whether clipping point is the corner of nodeB
    if ((-slopeB) == slopePrime)
    {
      if (p2x > p1x)
      {
        result[2] = bottomLeftBx;
        result[3] = bottomLeftBy;
        clipPointBFound = true;
      }
      else
      {
        result[2] = topRightBx;
        result[3] = topLeftBy;
        clipPointBFound = true;
      }
    }
    else if (slopeB == slopePrime)
    {
      if (p2x > p1x)
      {
        result[2] = topLeftBx;
        result[3] = topLeftBy;
        clipPointBFound = true;
      }
      else
      {
        result[2] = bottomRightBx;
        result[3] = bottomLeftBy;
        clipPointBFound = true;
      }
    }

    //if both clipping points are corners
    if (clipPointAFound && clipPointBFound)
    {
      return false;
    }

    //determine Cardinal Direction of rectangles
    if (p1x > p2x)
    {
      if (p1y > p2y)
      {
        cardinalDirectionA = IGeometry.getCardinalDirection(slopeA, slopePrime, 4);
        cardinalDirectionB = IGeometry.getCardinalDirection(slopeB, slopePrime, 2);
      }
      else
      {
        cardinalDirectionA = IGeometry.getCardinalDirection(-slopeA, slopePrime, 3);
        cardinalDirectionB = IGeometry.getCardinalDirection(-slopeB, slopePrime, 1);
      }
    }
    else
    {
      if (p1y > p2y)
      {
        cardinalDirectionA = IGeometry.getCardinalDirection(-slopeA, slopePrime, 1);
        cardinalDirectionB = IGeometry.getCardinalDirection(-slopeB, slopePrime, 3);
      }
      else
      {
        cardinalDirectionA = IGeometry.getCardinalDirection(slopeA, slopePrime, 2);
        cardinalDirectionB = IGeometry.getCardinalDirection(slopeB, slopePrime, 4);
      }
    }
    //calculate clipping Point if it is not found before
    if (!clipPointAFound)
    {
      switch (cardinalDirectionA)
      {
        case 1:
          tempPointAy = topLeftAy;
          tempPointAx = p1x + (-halfHeightA) / slopePrime;
          result[0] = tempPointAx;
          result[1] = tempPointAy;
          break;
        case 2:
          tempPointAx = bottomRightAx;
          tempPointAy = p1y + halfWidthA * slopePrime;
          result[0] = tempPointAx;
          result[1] = tempPointAy;
          break;
        case 3:
          tempPointAy = bottomLeftAy;
          tempPointAx = p1x + halfHeightA / slopePrime;
          result[0] = tempPointAx;
          result[1] = tempPointAy;
          break;
        case 4:
          tempPointAx = bottomLeftAx;
          tempPointAy = p1y + (-halfWidthA) * slopePrime;
          result[0] = tempPointAx;
          result[1] = tempPointAy;
          break;
      }
    }
    if (!clipPointBFound)
    {
      switch (cardinalDirectionB)
      {
        case 1:
          tempPointBy = topLeftBy;
          tempPointBx = p2x + (-halfHeightB) / slopePrime;
          result[2] = tempPointBx;
          result[3] = tempPointBy;
          break;
        case 2:
          tempPointBx = bottomRightBx;
          tempPointBy = p2y + halfWidthB * slopePrime;
          result[2] = tempPointBx;
          result[3] = tempPointBy;
          break;
        case 3:
          tempPointBy = bottomLeftBy;
          tempPointBx = p2x + halfHeightB / slopePrime;
          result[2] = tempPointBx;
          result[3] = tempPointBy;
          break;
        case 4:
          tempPointBx = bottomLeftBx;
          tempPointBy = p2y + (-halfWidthB) * slopePrime;
          result[2] = tempPointBx;
          result[3] = tempPointBy;
          break;
      }
    }
  }
  return false;
}

IGeometry.getCardinalDirection = function (slope, slopePrime, line)
{
  if (slope > slopePrime)
  {
    return line;
  }
  else
  {
    return 1 + line % 4;
  }
}

IGeometry.getIntersection = function (s1, s2, f1, f2)
{
  if (f2 == null) {
    return IGeometry.getIntersection2(s1, s2, f1);
  }
  var x1 = s1.x;
  var y1 = s1.y;
  var x2 = s2.x;
  var y2 = s2.y;
  var x3 = f1.x;
  var y3 = f1.y;
  var x4 = f2.x;
  var y4 = f2.y;
  var x, y; // intersection point
  var a1, a2, b1, b2, c1, c2; // coefficients of line eqns.
  var denom;

  a1 = y2 - y1;
  b1 = x1 - x2;
  c1 = x2 * y1 - x1 * y2;  // { a1*x + b1*y + c1 = 0 is line 1 }

  a2 = y4 - y3;
  b2 = x3 - x4;
  c2 = x4 * y3 - x3 * y4;  // { a2*x + b2*y + c2 = 0 is line 2 }

  denom = a1 * b2 - a2 * b1;

  if (denom == 0)
  {
    return null;
  }

  x = (b1 * c2 - b2 * c1) / denom;
  y = (a2 * c1 - a1 * c2) / denom;

  return new Point(x, y);
}

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
/**
 * Some useful pre-calculated constants
 */
IGeometry.HALF_PI = 0.5 * Math.PI;
IGeometry.ONE_AND_HALF_PI = 1.5 * Math.PI;
IGeometry.TWO_PI = 2.0 * Math.PI;
IGeometry.THREE_PI = 3.0 * Math.PI;

module.exports = IGeometry;

},{}],15:[function(_dereq_,module,exports){
function IMath() {
}

/**
 * This method returns the sign of the input value.
 */
IMath.sign = function (value) {
  if (value > 0)
  {
    return 1;
  }
  else if (value < 0)
  {
    return -1;
  }
  else
  {
    return 0;
  }
}

IMath.floor = function (value) {
  return value < 0 ? Math.ceil(value) : Math.floor(value);
}

IMath.ceil = function (value) {
  return value < 0 ? Math.floor(value) : Math.ceil(value);
}

module.exports = IMath;

},{}],16:[function(_dereq_,module,exports){
function Integer() {
}

Integer.MAX_VALUE = 2147483647;
Integer.MIN_VALUE = -2147483648;

module.exports = Integer;

},{}],17:[function(_dereq_,module,exports){
var LGraphObject = _dereq_('./LGraphObject');

function LEdge(source, target, vEdge) {
  LGraphObject.call(this, vEdge);

  this.isOverlapingSourceAndTarget = false;
  this.vGraphObject = vEdge;
  this.bendpoints = [];
  this.source = source;
  this.target = target;
}

LEdge.prototype = Object.create(LGraphObject.prototype);

for (var prop in LGraphObject) {
  LEdge[prop] = LGraphObject[prop];
}

LEdge.prototype.getSource = function ()
{
  return this.source;
};

LEdge.prototype.getTarget = function ()
{
  return this.target;
};

LEdge.prototype.isInterGraph = function ()
{
  return this.isInterGraph;
};

LEdge.prototype.getLength = function ()
{
  return this.length;
};

LEdge.prototype.isOverlapingSourceAndTarget = function ()
{
  return this.isOverlapingSourceAndTarget;
};

LEdge.prototype.getBendpoints = function ()
{
  return this.bendpoints;
};

LEdge.prototype.getLca = function ()
{
  return this.lca;
};

LEdge.prototype.getSourceInLca = function ()
{
  return this.sourceInLca;
};

LEdge.prototype.getTargetInLca = function ()
{
  return this.targetInLca;
};

LEdge.prototype.getOtherEnd = function (node)
{
  if (this.source === node)
  {
    return this.target;
  }
  else if (this.target === node)
  {
    return this.source;
  }
  else
  {
    throw "Node is not incident with this edge";
  }
}

LEdge.prototype.getOtherEndInGraph = function (node, graph)
{
  var otherEnd = this.getOtherEnd(node);
  var root = graph.getGraphManager().getRoot();

  while (true)
  {
    if (otherEnd.getOwner() == graph)
    {
      return otherEnd;
    }

    if (otherEnd.getOwner() == root)
    {
      break;
    }

    otherEnd = otherEnd.getOwner().getParent();
  }

  return null;
};

LEdge.prototype.updateLength = function ()
{
  var clipPointCoordinates = new Array(4);

  this.isOverlapingSourceAndTarget =
          IGeometry.getIntersection(this.target.getRect(),
                  this.source.getRect(),
                  clipPointCoordinates);

  if (!this.isOverlapingSourceAndTarget)
  {
    this.lengthX = clipPointCoordinates[0] - clipPointCoordinates[2];
    this.lengthY = clipPointCoordinates[1] - clipPointCoordinates[3];

    if (Math.abs(this.lengthX) < 1.0)
    {
      this.lengthX = IMath.sign(this.lengthX);
    }

    if (Math.abs(this.lengthY) < 1.0)
    {
      this.lengthY = IMath.sign(this.lengthY);
    }

    this.length = Math.sqrt(
            this.lengthX * this.lengthX + this.lengthY * this.lengthY);
  }
};

LEdge.prototype.updateLengthSimple = function ()
{
  this.lengthX = this.target.getCenterX() - this.source.getCenterX();
  this.lengthY = this.target.getCenterY() - this.source.getCenterY();

  if (Math.abs(this.lengthX) < 1.0)
  {
    this.lengthX = IMath.sign(this.lengthX);
  }

  if (Math.abs(this.lengthY) < 1.0)
  {
    this.lengthY = IMath.sign(this.lengthY);
  }

  this.length = Math.sqrt(
          this.lengthX * this.lengthX + this.lengthY * this.lengthY);
}

module.exports = LEdge;

},{"./LGraphObject":20}],18:[function(_dereq_,module,exports){
var LGraphObject = _dereq_('./LGraphObject');
var Integer = _dereq_('./Integer');
var LayoutConstants = _dereq_('./LayoutConstants');
var LGraphManager = _dereq_('./LGraphManager');
var LNode = _dereq_('./LNode');

function LGraph(parent, obj2, vGraph) {
  LGraphObject.call(this, vGraph);
  this.estimatedSize = Integer.MIN_VALUE;
  this.margin = LayoutConstants.DEFAULT_GRAPH_MARGIN;
  this.edges = [];
  this.nodes = [];
  this.isConnected = false;
  this.parent = parent;

  if (obj2 != null && obj2 instanceof LGraphManager) {
    this.graphManager = obj2;
  }
  else if (obj2 != null && obj2 instanceof Layout) {
    this.graphManager = obj2.graphManager;
  }
}

LGraph.prototype = Object.create(LGraphObject.prototype);
for (var prop in LGraphObject) {
  LGraph[prop] = LGraphObject[prop];
}

LGraph.prototype.getNodes = function () {
  return this.nodes;
};

LGraph.prototype.getEdges = function () {
  return this.edges;
};

LGraph.prototype.getGraphManager = function ()
{
  return this.graphManager;
};

LGraph.prototype.getParent = function ()
{
  return this.parent;
};

LGraph.prototype.getLeft = function ()
{
  return this.left;
};

LGraph.prototype.getRight = function ()
{
  return this.right;
};

LGraph.prototype.getTop = function ()
{
  return this.top;
};

LGraph.prototype.getBottom = function ()
{
  return this.bottom;
};

LGraph.prototype.isConnected = function ()
{
  return this.isConnected;
};

LGraph.prototype.add = function (obj1, sourceNode, targetNode) {
  if (sourceNode == null && targetNode == null) {
    var newNode = obj1;
    if (this.graphManager == null) {
      throw "Graph has no graph mgr!";
    }
    if (this.getNodes().indexOf(newNode) > -1) {
      throw "Node already in graph!";
    }
    newNode.owner = this;
    this.getNodes().push(newNode);

    return newNode;
  }
  else {
    var newEdge = obj1;
    if (!(this.getNodes().indexOf(sourceNode) > -1 && (this.getNodes().indexOf(targetNode)) > -1)) {
      throw "Source or target not in graph!";
    }

    if (!(sourceNode.owner == targetNode.owner && sourceNode.owner == this)) {
      throw "Both owners must be this graph!";
    }

    if (sourceNode.owner != targetNode.owner)
    {
      return null;
    }

    // set source and target
    newEdge.source = sourceNode;
    newEdge.target = targetNode;

    // set as intra-graph edge
    newEdge.isInterGraph = false;

    // add to graph edge list
    this.getEdges().push(newEdge);

    // add to incidency lists
    sourceNode.edges.push(newEdge);

    if (targetNode != sourceNode)
    {
      targetNode.edges.push(newEdge);
    }

    return newEdge;
  }
};

LGraph.prototype.remove = function (obj) {
  var node = obj;
  if (obj instanceof LNode) {
    if (node == null) {
      throw "Node is null!";
    }
    if (!(node.owner != null && node.owner == this)) {
      throw "Owner graph is invalid!";
    }
    if (this.graphManager == null) {
      throw "Owner graph manager is invalid!";
    }
    // remove incident edges first (make a copy to do it safely)
    var edgesToBeRemoved = node.edges.slice();
    var edge;
    var s = edgesToBeRemoved.length;
    for (var i = 0; i < s; i++)
    {
      edge = edgesToBeRemoved[i];

      if (edge.isInterGraph)
      {
        this.graphManager.remove(edge);
      }
      else
      {
        edge.source.owner.remove(edge);
      }
    }

    // now the node itself
    var index = this.nodes.indexOf(node);
    if (index == -1) {
      throw "Node not in owner node list!";
    }

    this.nodes.splice(index, 1);
  }
  else if (obj instanceof LEdge) {
    var edge = obj;
    if (edge == null) {
      throw "Edge is null!";
    }
    if (!(edge.source != null && edge.target != null)) {
      throw "Source and/or target is null!";
    }
    if (!(edge.source.owner != null && edge.target.owner != null &&
            edge.source.owner == this && edge.target.owner == this)) {
      throw "Source and/or target owner is invalid!";
    }

    var sourceIndex = edge.source.edges.indexOf(edge);
    var targetIndex = edge.target.edges.indexOf(edge);
    if (!(sourceIndex > -1 && targetIndex > -1)) {
      throw "Source and/or target doesn't know this edge!";
    }

    edge.source.edges.splice(sourceIndex, 1);

    if (edge.target != edge.source)
    {
      edge.target.edges.splice(targetIndex, 1);
    }

    var index = edge.source.owner.getEdges().indexOf(edge);
    if (index == -1) {
      throw "Not in owner's edge list!";
    }

    edge.source.owner.getEdges().splice(index, 1);
  }
};

LGraph.prototype.updateLeftTop = function ()
{
  var top = Integer.MAX_VALUE;
  var left = Integer.MAX_VALUE;
  var nodeTop;
  var nodeLeft;

  var nodes = this.getNodes();
  var s = nodes.length;

  for (var i = 0; i < s; i++)
  {
    var lNode = nodes[i];
    nodeTop = Math.floor(lNode.getTop());
    nodeLeft = Math.floor(lNode.getLeft());

    if (top > nodeTop)
    {
      top = nodeTop;
    }

    if (left > nodeLeft)
    {
      left = nodeLeft;
    }
  }

  // Do we have any nodes in this graph?
  if (top == Integer.MAX_VALUE)
  {
    return null;
  }

  this.left = left - this.margin;
  this.top = top - this.margin;

  // Apply the margins and return the result
  return new Point(this.left, this.top);
};

LGraph.prototype.updateBounds = function (recursive)
{
  // calculate bounds
  var left = Integer.MAX_VALUE;
  var right = -Integer.MAX_VALUE;
  var top = Integer.MAX_VALUE;
  var bottom = -Integer.MAX_VALUE;
  var nodeLeft;
  var nodeRight;
  var nodeTop;
  var nodeBottom;

  var nodes = this.nodes;
  var s = nodes.length;
  for (var i = 0; i < s; i++)
  {
    var lNode = nodes[i];

    if (recursive && lNode.child != null)
    {
      lNode.updateBounds();
    }
    nodeLeft = Math.floor(lNode.getLeft());
    nodeRight = Math.floor(lNode.getRight());
    nodeTop = Math.floor(lNode.getTop());
    nodeBottom = Math.floor(lNode.getBottom());

    if (left > nodeLeft)
    {
      left = nodeLeft;
    }

    if (right < nodeRight)
    {
      right = nodeRight;
    }

    if (top > nodeTop)
    {
      top = nodeTop;
    }

    if (bottom < nodeBottom)
    {
      bottom = nodeBottom;
    }
  }

  var boundingRect = new RectangleD(left, top, right - left, bottom - top);
  if (left == Integer.MAX_VALUE)
  {
    this.left = Math.floor(this.parent.getLeft());
    this.right = Math.floor(this.parent.getRight());
    this.top = Math.floor(this.parent.getTop());
    this.bottom = Math.floor(this.parent.getBottom());
  }

  this.left = boundingRect.x - this.margin;
  this.right = boundingRect.x + boundingRect.width + this.margin;
  this.top = boundingRect.y - this.margin;
  this.bottom = boundingRect.y + boundingRect.height + this.margin;
};

LGraph.calculateBounds = function (nodes)
{
  var left = Integer.MAX_VALUE;
  var right = -Integer.MAX_VALUE;
  var top = Integer.MAX_VALUE;
  var bottom = -Integer.MAX_VALUE;
  var nodeLeft;
  var nodeRight;
  var nodeTop;
  var nodeBottom;

  var s = nodes.length;

  for (var i = 0; i < s; i++)
  {
    var lNode = nodes[i];
    nodeLeft = Math.floor(lNode.getLeft());
    nodeRight = Math.floor(lNode.getRight());
    nodeTop = Math.floor(lNode.getTop());
    nodeBottom = Math.floor(lNode.getBottom());

    if (left > nodeLeft)
    {
      left = nodeLeft;
    }

    if (right < nodeRight)
    {
      right = nodeRight;
    }

    if (top > nodeTop)
    {
      top = nodeTop;
    }

    if (bottom < nodeBottom)
    {
      bottom = nodeBottom;
    }
  }

  var boundingRect = new RectangleD(left, top, right - left, bottom - top);

  return boundingRect;
};

LGraph.prototype.getInclusionTreeDepth = function ()
{
  if (this == this.graphManager.getRoot())
  {
    return 1;
  }
  else
  {
    return this.parent.getInclusionTreeDepth();
  }
};

LGraph.prototype.getEstimatedSize = function ()
{
  if (this.estimatedSize == Integer.MIN_VALUE) {
    throw "assert failed";
  }
  return this.estimatedSize;
};

LGraph.prototype.calcEstimatedSize = function ()
{
  var size = 0;
  var nodes = this.nodes;
  var s = nodes.length;

  for (var i = 0; i < s; i++)
  {
    var lNode = nodes[i];
    size += lNode.calcEstimatedSize();
  }

  if (size == 0)
  {
    this.estimatedSize = LayoutConstants.EMPTY_COMPOUND_NODE_SIZE;
  }
  else
  {
    this.estimatedSize = Math.floor(size / Math.sqrt(this.nodes.length));
  }

  return Math.floor(this.estimatedSize);
};

LGraph.prototype.updateConnected = function ()
{
  if (this.nodes.length == 0)
  {
    this.isConnected = true;
    return;
  }

  var toBeVisited = [];
  var visited = new HashSet();
  var currentNode = this.nodes[0];
  var neighborEdges;
  var currentNeighbor;
  toBeVisited = toBeVisited.concat(currentNode.withChildren());

  while (toBeVisited.length > 0)
  {
    currentNode = toBeVisited.shift();
    visited.add(currentNode);

    // Traverse all neighbors of this node
    neighborEdges = currentNode.getEdges();
    var s = neighborEdges.length;
    for (var i = 0; i < s; i++)
    {
      var neighborEdge = neighborEdges[i];
      currentNeighbor =
              neighborEdge.getOtherEndInGraph(currentNode, this);

      // Add unvisited neighbors to the list to visit
      if (currentNeighbor != null &&
              !visited.contains(currentNeighbor))
      {
        toBeVisited = toBeVisited.concat(currentNeighbor.withChildren());
      }
    }
  }

  this.isConnected = false;

  if (visited.size() >= this.nodes.length)
  {
    var noOfVisitedInThisGraph = 0;

    var s = visited.size();
    for (var visitedId in visited.set)
    {
      var visitedNode = visited.set[visitedId];
      if (visitedNode.owner == this)
      {
        noOfVisitedInThisGraph++;
      }
    }

    if (noOfVisitedInThisGraph == this.nodes.length)
    {
      this.isConnected = true;
    }
  }
};

module.exports = LGraph;

},{"./Integer":16,"./LGraphManager":19,"./LGraphObject":20,"./LNode":21,"./LayoutConstants":23}],19:[function(_dereq_,module,exports){
function LGraphManager(layout) {
  this.layout = layout;

  this.graphs = [];
  this.edges = [];
}

LGraphManager.prototype.addRoot = function ()
{
  var ngraph = this.layout.newGraph();
  var nnode = this.layout.newNode(null);
  var root = this.add(ngraph, nnode);
  this.setRootGraph(root);
  return this.rootGraph;
};

LGraphManager.prototype.add = function (newGraph, parentNode, newEdge, sourceNode, targetNode)
{
  //there are just 2 parameters are passed then it adds an LGraph else it adds an LEdge
  if (newEdge == null && sourceNode == null && targetNode == null) {
    if (newGraph == null) {
      throw "Graph is null!";
    }
    if (parentNode == null) {
      throw "Parent node is null!";
    }
    if (this.graphs.indexOf(newGraph) > -1) {
      throw "Graph already in this graph mgr!";
    }

    this.graphs.push(newGraph);

    if (newGraph.parent != null) {
      throw "Already has a parent!";
    }
    if (parentNode.child != null) {
      throw  "Already has a child!";
    }

    newGraph.parent = parentNode;
    parentNode.child = newGraph;

    return newGraph;
  }
  else {
    //change the order of the parameters
    targetNode = newEdge;
    sourceNode = parentNode;
    newEdge = newGraph;
    var sourceGraph = sourceNode.getOwner();
    var targetGraph = targetNode.getOwner();

    if (!(sourceGraph != null && sourceGraph.getGraphManager() == this)) {
      throw "Source not in this graph mgr!";
    }
    if (!(targetGraph != null && targetGraph.getGraphManager() == this)) {
      throw "Target not in this graph mgr!";
    }

    if (sourceGraph == targetGraph)
    {
      newEdge.isInterGraph = false;
      return sourceGraph.add(newEdge, sourceNode, targetNode);
    }
    else
    {
      newEdge.isInterGraph = true;

      // set source and target
      newEdge.source = sourceNode;
      newEdge.target = targetNode;

      // add edge to inter-graph edge list
      if (this.edges.indexOf(newEdge) > -1) {
        throw "Edge already in inter-graph edge list!";
      }

      this.edges.push(newEdge);

      // add edge to source and target incidency lists
      if (!(newEdge.source != null && newEdge.target != null)) {
        throw "Edge source and/or target is null!";
      }

      if (!(newEdge.source.edges.indexOf(newEdge) == -1 && newEdge.target.edges.indexOf(newEdge) == -1)) {
        throw "Edge already in source and/or target incidency list!";
      }

      newEdge.source.edges.push(newEdge);
      newEdge.target.edges.push(newEdge);

      return newEdge;
    }
  }
};

LGraphManager.prototype.remove = function (lObj) {
  if (lObj instanceof LGraph) {
    var graph = lObj;
    if (graph.getGraphManager() != this) {
      throw "Graph not in this graph mgr";
    }
    if (!(graph == this.rootGraph || (graph.parent != null && graph.parent.graphManager == this))) {
      throw "Invalid parent node!";
    }

    // first the edges (make a copy to do it safely)
    var edgesToBeRemoved = [];

    edgesToBeRemoved = edgesToBeRemoved.concat(graph.getEdges());

    var edge;
    var s = edgesToBeRemoved.length;
    for (var i = 0; i < s; i++)
    {
      edge = edgesToBeRemoved[i];
      graph.remove(edge);
    }

    // then the nodes (make a copy to do it safely)
    var nodesToBeRemoved = [];

    nodesToBeRemoved = nodesToBeRemoved.concat(graph.getNodes());

    var node;
    s = nodesToBeRemoved.length;
    for (var i = 0; i < s; i++)
    {
      node = nodesToBeRemoved[i];
      graph.remove(node);
    }

    // check if graph is the root
    if (graph == this.rootGraph)
    {
      this.setRootGraph(null);
    }

    // now remove the graph itself
    var index = this.graphs.indexOf(graph);
    this.graphs.splice(index, 1);

    // also reset the parent of the graph
    graph.parent = null;
  }
  else if (lObj instanceof LEdge) {
    edge = lObj;
    if (edge == null) {
      throw "Edge is null!";
    }
    if (!edge.isInterGraph) {
      throw "Not an inter-graph edge!";
    }
    if (!(edge.source != null && edge.target != null)) {
      throw "Source and/or target is null!";
    }

    // remove edge from source and target nodes' incidency lists

    if (!(edge.source.edges.indexOf(edge) != -1 && edge.target.edges.indexOf(edge) != -1)) {
      throw "Source and/or target doesn't know this edge!";
    }

    var index = edge.source.edges.indexOf(edge);
    edge.source.edges.splice(index, 1);
    index = edge.target.edges.indexOf(edge);
    edge.target.edges.splice(index, 1);

    // remove edge from owner graph manager's inter-graph edge list

    if (!(edge.source.owner != null && edge.source.owner.getGraphManager() != null)) {
      throw "Edge owner graph or owner graph manager is null!";
    }
    if (edge.source.owner.getGraphManager().edges.indexOf(edge) == -1) {
      throw "Not in owner graph manager's edge list!";
    }

    var index = edge.source.owner.getGraphManager().edges.indexOf(edge);
    edge.source.owner.getGraphManager().edges.splice(index, 1);
  }
};

LGraphManager.prototype.updateBounds = function ()
{
  this.rootGraph.updateBounds(true);
};

LGraphManager.prototype.getGraphs = function ()
{
  return this.graphs;
};

LGraphManager.prototype.getAllNodes = function ()
{
  if (this.allNodes == null)
  {
    var nodeList = [];
    var graphs = this.getGraphs();
    var s = graphs.length;
    for (var i = 0; i < s; i++)
    {
      nodeList = nodeList.concat(graphs[i].getNodes());
    }
    this.allNodes = nodeList;
  }
  return this.allNodes;
};

LGraphManager.prototype.resetAllNodes = function ()
{
  this.allNodes = null;
};

LGraphManager.prototype.resetAllEdges = function ()
{
  this.allEdges = null;
};

LGraphManager.prototype.resetAllNodesToApplyGravitation = function ()
{
  this.allNodesToApplyGravitation = null;
};

LGraphManager.prototype.getAllEdges = function ()
{
  if (this.allEdges == null)
  {
    var edgeList = [];
    var graphs = this.getGraphs();
    var s = graphs.length;
    for (var i = 0; i < graphs.length; i++)
    {
      edgeList = edgeList.concat(graphs[i].getEdges());
    }

    edgeList = edgeList.concat(this.edges);

    this.allEdges = edgeList;
  }
  return this.allEdges;
};

LGraphManager.prototype.getAllNodesToApplyGravitation = function ()
{
  return this.allNodesToApplyGravitation;
};

LGraphManager.prototype.setAllNodesToApplyGravitation = function (nodeList)
{
  if (this.allNodesToApplyGravitation != null) {
    throw "assert failed";
  }

  this.allNodesToApplyGravitation = nodeList;
};

LGraphManager.prototype.getRoot = function ()
{
  return this.rootGraph;
};

LGraphManager.prototype.setRootGraph = function (graph)
{
  if (graph.getGraphManager() != this) {
    throw "Root not in this graph mgr!";
  }

  this.rootGraph = graph;
  // root graph must have a root node associated with it for convenience
  if (graph.parent == null)
  {
    graph.parent = this.layout.newNode("Root node");
  }
};

LGraphManager.prototype.getLayout = function ()
{
  return this.layout;
};

LGraphManager.prototype.isOneAncestorOfOther = function (firstNode, secondNode)
{
  if (!(firstNode != null && secondNode != null)) {
    throw "assert failed";
  }

  if (firstNode == secondNode)
  {
    return true;
  }
  // Is second node an ancestor of the first one?
  var ownerGraph = firstNode.getOwner();
  var parentNode;

  do
  {
    parentNode = ownerGraph.getParent();

    if (parentNode == null)
    {
      break;
    }

    if (parentNode == secondNode)
    {
      return true;
    }

    ownerGraph = parentNode.getOwner();
    if (ownerGraph == null)
    {
      break;
    }
  } while (true);
  // Is first node an ancestor of the second one?
  ownerGraph = secondNode.getOwner();

  do
  {
    parentNode = ownerGraph.getParent();

    if (parentNode == null)
    {
      break;
    }

    if (parentNode == firstNode)
    {
      return true;
    }

    ownerGraph = parentNode.getOwner();
    if (ownerGraph == null)
    {
      break;
    }
  } while (true);

  return false;
};

LGraphManager.prototype.calcLowestCommonAncestors = function ()
{
  var edge;
  var sourceNode;
  var targetNode;
  var sourceAncestorGraph;
  var targetAncestorGraph;

  var edges = this.getAllEdges();
  var s = edges.length;
  for (var i = 0; i < s; i++)
  {
    edge = edges[i];

    sourceNode = edge.source;
    targetNode = edge.target;
    edge.lca = null;
    edge.sourceInLca = sourceNode;
    edge.targetInLca = targetNode;

    if (sourceNode == targetNode)
    {
      edge.lca = sourceNode.getOwner();
      continue;
    }

    sourceAncestorGraph = sourceNode.getOwner();

    while (edge.lca == null)
    {
      targetAncestorGraph = targetNode.getOwner();

      while (edge.lca == null)
      {
        if (targetAncestorGraph == sourceAncestorGraph)
        {
          edge.lca = targetAncestorGraph;
          break;
        }

        if (targetAncestorGraph == this.rootGraph)
        {
          break;
        }

        if (edge.lca != null) {
          throw "assert failed";
        }
        edge.targetInLca = targetAncestorGraph.getParent();
        targetAncestorGraph = edge.targetInLca.getOwner();
      }

      if (sourceAncestorGraph == this.rootGraph)
      {
        break;
      }

      if (edge.lca == null)
      {
        edge.sourceInLca = sourceAncestorGraph.getParent();
        sourceAncestorGraph = edge.sourceInLca.getOwner();
      }
    }

    if (edge.lca == null) {
      throw "assert failed";
    }
  }
};

LGraphManager.prototype.calcLowestCommonAncestor = function (firstNode, secondNode)
{
  if (firstNode == secondNode)
  {
    return firstNode.getOwner();
  }
  var firstOwnerGraph = firstNode.getOwner();

  do
  {
    if (firstOwnerGraph == null)
    {
      break;
    }
    var secondOwnerGraph = secondNode.getOwner();

    do
    {
      if (secondOwnerGraph == null)
      {
        break;
      }

      if (secondOwnerGraph == firstOwnerGraph)
      {
        return secondOwnerGraph;
      }
      secondOwnerGraph = secondOwnerGraph.getParent().getOwner();
    } while (true);

    firstOwnerGraph = firstOwnerGraph.getParent().getOwner();
  } while (true);

  return firstOwnerGraph;
};

LGraphManager.prototype.calcInclusionTreeDepths = function (graph, depth) {
  if (graph == null && depth == null) {
    graph = this.rootGraph;
    depth = 1;
  }
  var node;

  var nodes = graph.getNodes();
  var s = nodes.length;
  for (var i = 0; i < s; i++)
  {
    node = nodes[i];
    node.inclusionTreeDepth = depth;

    if (node.child != null)
    {
      this.calcInclusionTreeDepths(node.child, depth + 1);
    }
  }
};

LGraphManager.prototype.includesInvalidEdge = function ()
{
  var edge;

  var s = this.edges.length;
  for (var i = 0; i < s; i++)
  {
    edge = this.edges[i];

    if (this.isOneAncestorOfOther(edge.source, edge.target))
    {
      return true;
    }
  }
  return false;
};

module.exports = LGraphManager;

},{}],20:[function(_dereq_,module,exports){
function LGraphObject(vGraphObject) {
  this.vGraphObject = vGraphObject;
}

module.exports = LGraphObject;

},{}],21:[function(_dereq_,module,exports){
var LGraphObject = _dereq_('./LGraphObject');
var Integer = _dereq_('./Integer');
var RectangleD = _dereq_('./RectangleD');

function LNode(gm, loc, size, vNode) {
  //Alternative constructor 1 : LNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
  if (size == null && vNode == null) {
    vNode = loc;
  }

  LGraphObject.call(this, vNode);

  //Alternative constructor 2 : LNode(Layout layout, Object vNode)
  if (gm.graphManager != null)
    gm = gm.graphManager;

  this.estimatedSize = Integer.MIN_VALUE;
  this.inclusionTreeDepth = Integer.MAX_VALUE;
  this.vGraphObject = vNode;
  this.edges = [];
  this.graphManager = gm;

  if (size != null && loc != null)
    this.rect = new RectangleD(loc.x, loc.y, size.width, size.height);
  else
    this.rect = new RectangleD();
}

LNode.prototype = Object.create(LGraphObject.prototype);
for (var prop in LGraphObject) {
  LNode[prop] = LGraphObject[prop];
}

LNode.prototype.getEdges = function ()
{
  return this.edges;
};

LNode.prototype.getChild = function ()
{
  return this.child;
};

LNode.prototype.getOwner = function ()
{
  if (this.owner != null) {
    if (!(this.owner == null || this.owner.getNodes().indexOf(this) > -1)) {
      throw "assert failed";
    }
  }

  return this.owner;
};

LNode.prototype.getWidth = function ()
{
  return this.rect.width;
};

LNode.prototype.setWidth = function (width)
{
  this.rect.width = width;
};

LNode.prototype.getHeight = function ()
{
  return this.rect.height;
};

LNode.prototype.setHeight = function (height)
{
  this.rect.height = height;
};

LNode.prototype.getCenterX = function ()
{
  return this.rect.x + this.rect.width / 2;
};

LNode.prototype.getCenterY = function ()
{
  return this.rect.y + this.rect.height / 2;
};

LNode.prototype.getCenter = function ()
{
  return new PointD(this.rect.x + this.rect.width / 2,
          this.rect.y + this.rect.height / 2);
};

LNode.prototype.getLocation = function ()
{
  return new PointD(this.rect.x, this.rect.y);
};

LNode.prototype.getRect = function ()
{
  return this.rect;
};

LNode.prototype.getDiagonal = function ()
{
  return Math.sqrt(this.rect.width * this.rect.width +
          this.rect.height * this.rect.height);
};

LNode.prototype.setRect = function (upperLeft, dimension)
{
  this.rect.x = upperLeft.x;
  this.rect.y = upperLeft.y;
  this.rect.width = dimension.width;
  this.rect.height = dimension.height;
};

LNode.prototype.setCenter = function (cx, cy)
{
  this.rect.x = cx - this.rect.width / 2;
  this.rect.y = cy - this.rect.height / 2;
};

LNode.prototype.setLocation = function (x, y)
{
  this.rect.x = x;
  this.rect.y = y;
};

LNode.prototype.moveBy = function (dx, dy)
{
  this.rect.x += dx;
  this.rect.y += dy;
};

LNode.prototype.getEdgeListToNode = function (to)
{
  var edgeList = [];
  var edge;

  for (var obj in this.edges)
  {
    edge = obj;

    if (edge.target == to)
    {
      if (edge.source != this)
        throw "Incorrect edge source!";

      edgeList.push(edge);
    }
  }

  return edgeList;
};

LNode.prototype.getEdgesBetween = function (other)
{
  var edgeList = [];
  var edge;

  for (var obj in this.edges)
  {
    edge = this.edges[obj];

    if (!(edge.source == this || edge.target == this))
      throw "Incorrect edge source and/or target";

    if ((edge.target == other) || (edge.source == other))
    {
      edgeList.push(edge);
    }
  }

  return edgeList;
};

LNode.prototype.getNeighborsList = function ()
{
  var neighbors = new HashSet();
  var edge;

  for (var obj in this.edges)
  {
    edge = this.edges[obj];

    if (edge.source == this)
    {
      neighbors.add(edge.target);
    }
    else
    {
      if (!edge.target == this)
        throw "Incorrect incidency!";
      neighbors.add(edge.source);
    }
  }

  return neighbors;
};

LNode.prototype.withChildren = function ()
{
  var withNeighborsList = [];
  var childNode;

  withNeighborsList.push(this);

  if (this.child != null)
  {
    var nodes = this.child.getNodes();
    for (var i = 0; i < nodes.length; i++)
    {
      childNode = nodes[i];

      withNeighborsList = withNeighborsList.concat(childNode.withChildren());
    }
  }

  return withNeighborsList;
};

LNode.prototype.getEstimatedSize = function () {
  if (this.estimatedSize == Integer.MIN_VALUE) {
    throw "assert failed";
  }
  return this.estimatedSize;
};

LNode.prototype.calcEstimatedSize = function () {
  if (this.child == null)
  {
    return this.estimatedSize = Math.floor((this.rect.width + this.rect.height) / 2);
  }
  else
  {
    this.estimatedSize = this.child.calcEstimatedSize();
    this.rect.width = this.estimatedSize;
    this.rect.height = this.estimatedSize;

    return this.estimatedSize;
  }
};

LNode.prototype.scatter = function () {
  var randomCenterX;
  var randomCenterY;

  var minX = -LayoutConstants.INITIAL_WORLD_BOUNDARY;
  var maxX = LayoutConstants.INITIAL_WORLD_BOUNDARY;
  randomCenterX = LayoutConstants.WORLD_CENTER_X +
          (RandomSeed.nextDouble() * (maxX - minX)) + minX;

  var minY = -LayoutConstants.INITIAL_WORLD_BOUNDARY;
  var maxY = LayoutConstants.INITIAL_WORLD_BOUNDARY;
  randomCenterY = LayoutConstants.WORLD_CENTER_Y +
          (RandomSeed.nextDouble() * (maxY - minY)) + minY;

  this.rect.x = randomCenterX;
  this.rect.y = randomCenterY
};

LNode.prototype.updateBounds = function () {
  if (this.getChild() == null) {
    throw "assert failed";
  }
  if (this.getChild().getNodes().length != 0)
  {
    // wrap the children nodes by re-arranging the boundaries
    var childGraph = this.getChild();
    childGraph.updateBounds(true);

    this.rect.x = childGraph.getLeft();
    this.rect.y = childGraph.getTop();

    this.setWidth(childGraph.getRight() - childGraph.getLeft() +
            2 * LayoutConstants.COMPOUND_NODE_MARGIN);
    this.setHeight(childGraph.getBottom() - childGraph.getTop() +
            2 * LayoutConstants.COMPOUND_NODE_MARGIN +
            LayoutConstants.LABEL_HEIGHT);
  }
};

LNode.prototype.getInclusionTreeDepth = function ()
{
  if (this.inclusionTreeDepth == Integer.MAX_VALUE) {
    throw "assert failed";
  }
  return this.inclusionTreeDepth;
};

LNode.prototype.transform = function (trans)
{
  var left = this.rect.x;

  if (left > LayoutConstants.WORLD_BOUNDARY)
  {
    left = LayoutConstants.WORLD_BOUNDARY;
  }
  else if (left < -LayoutConstants.WORLD_BOUNDARY)
  {
    left = -LayoutConstants.WORLD_BOUNDARY;
  }

  var top = this.rect.y;

  if (top > LayoutConstants.WORLD_BOUNDARY)
  {
    top = LayoutConstants.WORLD_BOUNDARY;
  }
  else if (top < -LayoutConstants.WORLD_BOUNDARY)
  {
    top = -LayoutConstants.WORLD_BOUNDARY;
  }

  var leftTop = new PointD(left, top);
  var vLeftTop = trans.inverseTransformPoint(leftTop);

  this.setLocation(vLeftTop.x, vLeftTop.y);
};

LNode.prototype.getLeft = function ()
{
  return this.rect.x;
};

LNode.prototype.getRight = function ()
{
  return this.rect.x + this.rect.width;
};

LNode.prototype.getTop = function ()
{
  return this.rect.y;
};

LNode.prototype.getBottom = function ()
{
  return this.rect.y + this.rect.height;
};

LNode.prototype.getParent = function ()
{
  if (this.owner == null)
  {
    return null;
  }

  return this.owner.getParent();
};

module.exports = LNode;

},{"./Integer":16,"./LGraphObject":20,"./RectangleD":27}],22:[function(_dereq_,module,exports){
var LayoutConstants = _dereq_('./LayoutConstants');
var HashMap = _dereq_('./HashMap');
var LGraphManager = _dereq_('./LGraphManager');

function Layout(isRemoteUse) {
  //Layout Quality: 0:proof, 1:default, 2:draft
  this.layoutQuality = LayoutConstants.DEFAULT_QUALITY;
  //Whether layout should create bendpoints as needed or not
  this.createBendsAsNeeded =
          LayoutConstants.DEFAULT_CREATE_BENDS_AS_NEEDED;
  //Whether layout should be incremental or not
  this.incremental = LayoutConstants.DEFAULT_INCREMENTAL;
  //Whether we animate from before to after layout node positions
  this.animationOnLayout =
          LayoutConstants.DEFAULT_ANIMATION_ON_LAYOUT;
  //Whether we animate the layout process or not
  this.animationDuringLayout = LayoutConstants.DEFAULT_ANIMATION_DURING_LAYOUT;
  //Number iterations that should be done between two successive animations
  this.animationPeriod = LayoutConstants.DEFAULT_ANIMATION_PERIOD;
  /**
   * Whether or not leaf nodes (non-compound nodes) are of uniform sizes. When
   * they are, both spring and repulsion forces between two leaf nodes can be
   * calculated without the expensive clipping point calculations, resulting
   * in major speed-up.
   */
  this.uniformLeafNodeSizes =
          LayoutConstants.DEFAULT_UNIFORM_LEAF_NODE_SIZES;
  /**
   * This is used for creation of bendpoints by using dummy nodes and edges.
   * Maps an LEdge to its dummy bendpoint path.
   */
  this.edgeToDummyNodes = new HashMap();
  this.graphManager = new LGraphManager(this);
  this.isLayoutFinished = false;
  this.isSubLayout = false;
  this.isRemoteUse = false;

  if (isRemoteUse != null) {
    this.isRemoteUse = isRemoteUse;
  }
}

Layout.RANDOM_SEED = 1;

Layout.prototype.getGraphManager = function () {
  return this.graphManager;
};

Layout.prototype.getAllNodes = function () {
  return this.graphManager.getAllNodes();
};

Layout.prototype.getAllEdges = function () {
  return this.graphManager.getAllEdges();
};

Layout.prototype.getAllNodesToApplyGravitation = function () {
  return this.graphManager.getAllNodesToApplyGravitation();
};

Layout.prototype.newGraphManager = function () {
  var gm = new LGraphManager(this);
  this.graphManager = gm;
  return gm;
};

Layout.prototype.newGraph = function (vGraph)
{
  return new LGraph(null, this.graphManager, vGraph);
};

Layout.prototype.newNode = function (vNode)
{
  return new LNode(this.graphManager, vNode);
};

Layout.prototype.newEdge = function (vEdge)
{
  return new LEdge(null, null, vEdge);
};

Layout.prototype.runLayout = function ()
{
  this.isLayoutFinished = false;

  this.initParameters();
  var isLayoutSuccessfull;

  if ((this.graphManager.getRoot() == null)
          || this.graphManager.getRoot().getNodes().length == 0
          || this.graphManager.includesInvalidEdge())
  {
    isLayoutSuccessfull = false;
  }
  else
  {
    // calculate execution time
    var startTime = 0;

    if (!this.isSubLayout)
    {
      startTime = new Date().getTime()
    }

    isLayoutSuccessfull = this.layout();

    if (!this.isSubLayout)
    {
      var endTime = new Date().getTime();
      var excTime = endTime - startTime;

      console.log("Total execution time: " + excTime + " miliseconds.");
    }
  }

  if (isLayoutSuccessfull)
  {
    if (!this.isSubLayout)
    {
      this.doPostLayout();
    }
  }

  this.isLayoutFinished = true;

  return isLayoutSuccessfull;
};

/**
 * This method performs the operations required after layout.
 */
Layout.prototype.doPostLayout = function ()
{
  //assert !isSubLayout : "Should not be called on sub-layout!";
  // Propagate geometric changes to v-level objects
  this.transform();
  this.update();
};

/**
 * This method updates the geometry of the target graph according to
 * calculated layout.
 */
Layout.prototype.update2 = function () {
  // update bend points
  if (this.createBendsAsNeeded)
  {
    this.createBendpointsFromDummyNodes();

    // reset all edges, since the topology has changed
    this.graphManager.resetAllEdges();
  }

  // perform edge, node and root updates if layout is not called
  // remotely
  if (!this.isRemoteUse)
  {
    // update all edges
    var edge;
    var allEdges = this.graphManager.getAllEdges();
    for (var i = 0; i < allEdges.length; i++)
    {
      edge = allEdges[i];
//      this.update(edge);
    }

    // recursively update nodes
    var node;
    var nodes = this.graphManager.getRoot().getNodes();
    for (var i = 0; i < nodes.length; i++)
    {
      node = nodes[i];
//      this.update(node);
    }

    // update root graph
    this.update(this.graphManager.getRoot());
  }
};

Layout.prototype.update = function (obj) {
  if (obj == null) {
    this.update2();
  }
  else if (obj instanceof LNode) {
    var node = obj;
    if (node.getChild() != null)
    {
      // since node is compound, recursively update child nodes
      var nodes = node.getChild().getNodes();
      for (var i = 0; i < nodes.length; i++)
      {
        update(nodes[i]);
      }
    }

    // if the l-level node is associated with a v-level graph object,
    // then it is assumed that the v-level node implements the
    // interface Updatable.
    if (node.vGraphObject != null)
    {
      // cast to Updatable without any type check
      var vNode = node.vGraphObject;

      // call the update method of the interface
      vNode.update(node);
    }
  }
  else if (obj instanceof LEdge) {
    var edge = obj;
    // if the l-level edge is associated with a v-level graph object,
    // then it is assumed that the v-level edge implements the
    // interface Updatable.

    if (edge.vGraphObject != null)
    {
      // cast to Updatable without any type check
      var vEdge = edge.vGraphObject;

      // call the update method of the interface
      vEdge.update(edge);
    }
  }
  else if (obj instanceof LGraph) {
    var graph = obj;
    // if the l-level graph is associated with a v-level graph object,
    // then it is assumed that the v-level object implements the
    // interface Updatable.

    if (graph.vGraphObject != null)
    {
      // cast to Updatable without any type check
      var vGraph = graph.vGraphObject;

      // call the update method of the interface
      vGraph.update(graph);
    }
  }
};

/**
 * This method is used to set all layout parameters to default values
 * determined at compile time.
 */
Layout.prototype.initParameters = function () {
  if (!this.isSubLayout)
  {
    this.layoutQuality = layoutOptionsPack.layoutQuality;
    this.animationDuringLayout = layoutOptionsPack.animationDuringLayout;
    this.animationPeriod = Math.floor(Layout.transform(layoutOptionsPack.animationPeriod,
            LayoutConstants.DEFAULT_ANIMATION_PERIOD));
    this.animationOnLayout = layoutOptionsPack.animationOnLayout;
    this.incremental = layoutOptionsPack.incremental;
    this.createBendsAsNeeded = layoutOptionsPack.createBendsAsNeeded;
    this.uniformLeafNodeSizes = layoutOptionsPack.uniformLeafNodeSizes;
  }

  if (this.animationDuringLayout)
  {
    animationOnLayout = false;
  }
};

Layout.prototype.transform = function (newLeftTop) {
  if (newLeftTop == undefined) {
    this.transform(new PointD(0, 0));
  }
  else {
    // create a transformation object (from Eclipse to layout). When an
    // inverse transform is applied, we get upper-left coordinate of the
    // drawing or the root graph at given input coordinate (some margins
    // already included in calculation of left-top).

    var trans = new Transform();
    var leftTop = this.graphManager.getRoot().updateLeftTop();

    if (leftTop != null)
    {
      trans.setWorldOrgX(newLeftTop.x);
      trans.setWorldOrgY(newLeftTop.y);

      trans.setDeviceOrgX(leftTop.x);
      trans.setDeviceOrgY(leftTop.y);

      var nodes = this.getAllNodes();
      var node;

      for (var i = 0; i < nodes.length; i++)
      {
        node = nodes[i];
        node.transform(trans);
      }
    }
  }
};

Layout.prototype.positionNodesRandomly = function (graph) {

  if (graph == undefined) {
    //assert !this.incremental;
    this.positionNodesRandomly(this.getGraphManager().getRoot());
    this.getGraphManager().getRoot().updateBounds(true);
  }
  else {
    var lNode;
    var childGraph;

    var nodes = graph.getNodes();
    for (var i = 0; i < nodes.length; i++)
    {
      lNode = nodes[i];
      childGraph = lNode.getChild();

      if (childGraph == null)
      {
        lNode.scatter();
      }
      else if (childGraph.getNodes().length == 0)
      {
        lNode.scatter();
      }
      else
      {
        this.positionNodesRandomly(childGraph);
        lNode.updateBounds();
      }
    }
  }
};

/**
 * This method returns a list of trees where each tree is represented as a
 * list of l-nodes. The method returns a list of size 0 when:
 * - The graph is not flat or
 * - One of the component(s) of the graph is not a tree.
 */
Layout.prototype.getFlatForest = function ()
{
  var flatForest = [];
  var isForest = true;

  // Quick reference for all nodes in the graph manager associated with
  // this layout. The list should not be changed.
  var allNodes = this.graphManager.getRoot().getNodes();

  // First be sure that the graph is flat
  var isFlat = true;

  for (var i = 0; i < allNodes.length; i++)
  {
    if (allNodes[i].getChild() != null)
    {
      isFlat = false;
    }
  }

  // Return empty forest if the graph is not flat.
  if (!isFlat)
  {
    return flatForest;
  }

  // Run BFS for each component of the graph.

  var visited = new HashSet();
  var toBeVisited = [];
  var parents = new HashMap();
  var unProcessedNodes = [];

  unProcessedNodes = unProcessedNodes.concat(allNodes);

  // Each iteration of this loop finds a component of the graph and
  // decides whether it is a tree or not. If it is a tree, adds it to the
  // forest and continued with the next component.

  while (unProcessedNodes.length > 0 && isForest)
  {
    toBeVisited.push(unProcessedNodes[0]);

    // Start the BFS. Each iteration of this loop visits a node in a
    // BFS manner.
    while (toBeVisited.length > 0 && isForest)
    {
      //pool operation
      var currentNode = toBeVisited[0];
      toBeVisited.splice(0, 1);
      visited.add(currentNode);

      // Traverse all neighbors of this node
      var neighborEdges = currentNode.getEdges();

      for (var i = 0; i < neighborEdges.length; i++)
      {
        var currentNeighbor =
                neighborEdges[i].getOtherEnd(currentNode);

        // If BFS is not growing from this neighbor.
        if (parents.get(currentNode) != currentNeighbor)
        {
          // We haven't previously visited this neighbor.
          if (!visited.contains(currentNeighbor))
          {
            toBeVisited.push(currentNeighbor);
            parents.put(currentNeighbor, currentNode);
          }
          // Since we have previously visited this neighbor and
          // this neighbor is not parent of currentNode, given
          // graph contains a component that is not tree, hence
          // it is not a forest.
          else
          {
            isForest = false;
            break;
          }
        }
      }
    }

    // The graph contains a component that is not a tree. Empty
    // previously found trees. The method will end.
    if (!isForest)
    {
      flatForest = [];
    }
    // Save currently visited nodes as a tree in our forest. Reset
    // visited and parents lists. Continue with the next component of
    // the graph, if any.
    else
    {
      var temp = [];
      visited.addAllTo(temp);
      flatForest.push(temp);
      //flatForest = flatForest.concat(temp);
      //unProcessedNodes.removeAll(visited);
      for (var i = 0; i < temp.length; i++) {
        var value = temp[i];
        var index = unProcessedNodes.indexOf(value);
        if (index > -1) {
          unProcessedNodes.splice(index, 1);
        }
      }
      visited = new HashSet();
      parents = new HashMap();
    }
  }

  return flatForest;
};

/**
 * This method creates dummy nodes (an l-level node with minimal dimensions)
 * for the given edge (one per bendpoint). The existing l-level structure
 * is updated accordingly.
 */
Layout.prototype.createDummyNodesForBendpoints = function (edge)
{
  var dummyNodes = [];
  var prev = edge.source;

  var graph = this.graphManager.calcLowestCommonAncestor(edge.source, edge.target);

  for (var i = 0; i < edge.bendpoints.length; i++)
  {
    // create new dummy node
    var dummyNode = this.newNode(null);
    dummyNode.setRect(new Point(0, 0), new Dimension(1, 1));

    graph.add(dummyNode);

    // create new dummy edge between prev and dummy node
    var dummyEdge = this.newEdge(null);
    this.graphManager.add(dummyEdge, prev, dummyNode);

    dummyNodes.add(dummyNode);
    prev = dummyNode;
  }

  var dummyEdge = this.newEdge(null);
  this.graphManager.add(dummyEdge, prev, edge.target);

  this.edgeToDummyNodes.put(edge, dummyNodes);

  // remove real edge from graph manager if it is inter-graph
  if (edge.isInterGraph())
  {
    this.graphManager.remove(edge);
  }
  // else, remove the edge from the current graph
  else
  {
    graph.remove(edge);
  }

  return dummyNodes;
};

/**
 * This method creates bendpoints for edges from the dummy nodes
 * at l-level.
 */
Layout.prototype.createBendpointsFromDummyNodes = function ()
{
  var edges = [];
  edges = edges.concat(this.graphManager.getAllEdges());
  edges = this.edgeToDummyNodes.keySet().concat(edges);

  for (var k = 0; k < edges.length; k++)
  {
    var lEdge = edges[k];

    if (lEdge.bendpoints.length > 0)
    {
      var path = this.edgeToDummyNodes.get(lEdge);

      for (var i = 0; i < path.length; i++)
      {
        var dummyNode = path[i];
        var p = new PointD(dummyNode.getCenterX(),
                dummyNode.getCenterY());

        // update bendpoint's location according to dummy node
        var ebp = lEdge.bendpoints.get(i);
        ebp.x = p.x;
        ebp.y = p.y;

        // remove the dummy node, dummy edges incident with this
        // dummy node is also removed (within the remove method)
        dummyNode.getOwner().remove(dummyNode);
      }

      // add the real edge to graph
      this.graphManager.add(lEdge, lEdge.source, lEdge.target);
    }
  }
};

Layout.transform = function (sliderValue, defaultValue, minDiv, maxMul) {
  if (minDiv != undefined && maxMul != undefined) {
    var value = defaultValue;

    if (sliderValue <= 50)
    {
      var minValue = defaultValue / minDiv;
      value -= ((defaultValue - minValue) / 50) * (50 - sliderValue);
    }
    else
    {
      var maxValue = defaultValue * maxMul;
      value += ((maxValue - defaultValue) / 50) * (sliderValue - 50);
    }

    return value;
  }
  else {
    var a, b;

    if (sliderValue <= 50)
    {
      a = 9.0 * defaultValue / 500.0;
      b = defaultValue / 10.0;
    }
    else
    {
      a = 9.0 * defaultValue / 50.0;
      b = -8 * defaultValue;
    }

    return (a * sliderValue + b);
  }
};

/**
 * This method finds and returns the center of the given nodes, assuming
 * that the given nodes form a tree in themselves.
 */
Layout.findCenterOfTree = function (nodes)
{
  var list = [];
  list = list.concat(nodes);

  var removedNodes = [];
  var remainingDegrees = new HashMap();
  var foundCenter = false;
  var centerNode = null;

  if (list.length == 1 || list.length == 2)
  {
    foundCenter = true;
    centerNode = list[0];
  }

  for (var i = 0; i < list.length; i++)
  {
    var node = list[i];
    var degree = node.getNeighborsList().size();
    remainingDegrees.put(node, node.getNeighborsList().size());

    if (degree == 1)
    {
      removedNodes.push(node);
    }
  }

  var tempList = [];
  tempList = tempList.concat(removedNodes);

  while (!foundCenter)
  {
    var tempList2 = [];
    tempList2 = tempList2.concat(tempList);
    tempList = [];

    for (var i = 0; i < list.length; i++)
    {
      var node = list[i];

      var index = list.indexOf(node);
      if (index >= 0) {
        list.splice(index, 1);
      }

      var neighbours = node.getNeighborsList();

      for (var j in neighbours.set)
      {
        var neighbour = neighbours.set[j];
        if (removedNodes.indexOf(neighbour) < 0)
        {
          var otherDegree = remainingDegrees.get(neighbour);
          var newDegree = otherDegree - 1;

          if (newDegree == 1)
          {
            tempList.push(neighbour);
          }

          remainingDegrees.put(neighbour, newDegree);
        }
      }
    }

    removedNodes = removedNodes.concat(tempList);

    if (list.length == 1 || list.length == 2)
    {
      foundCenter = true;
      centerNode = list[0];
    }
  }

  return centerNode;
};

/**
 * During the coarsening process, this layout may be referenced by two graph managers
 * this setter function grants access to change the currently being used graph manager
 */
Layout.prototype.setGraphManager = function (gm)
{
  this.graphManager = gm;
};

module.exports = Layout;

},{"./HashMap":12,"./LGraphManager":19,"./LayoutConstants":23}],23:[function(_dereq_,module,exports){
function LayoutConstants() {
}

/**
 * Layout Quality
 */
LayoutConstants.PROOF_QUALITY = 0;
LayoutConstants.DEFAULT_QUALITY = 1;
LayoutConstants.DRAFT_QUALITY = 2;

/**
 * Default parameters
 */
LayoutConstants.DEFAULT_CREATE_BENDS_AS_NEEDED = false;
//LayoutConstants.DEFAULT_INCREMENTAL = true;
LayoutConstants.DEFAULT_INCREMENTAL = false;
LayoutConstants.DEFAULT_ANIMATION_ON_LAYOUT = true;
LayoutConstants.DEFAULT_ANIMATION_DURING_LAYOUT = false;
LayoutConstants.DEFAULT_ANIMATION_PERIOD = 50;
LayoutConstants.DEFAULT_UNIFORM_LEAF_NODE_SIZES = false;

// -----------------------------------------------------------------------------
// Section: General other constants
// -----------------------------------------------------------------------------
/*
 * Margins of a graph to be applied on bouding rectangle of its contents. We
 * assume margins on all four sides to be uniform.
 */
LayoutConstants.DEFAULT_GRAPH_MARGIN = 10;

/*
 * The height of the label of a compound. We assume the label of a compound
 * node is placed at the bottom with a dynamic width same as the compound
 * itself.
 */
LayoutConstants.LABEL_HEIGHT = 20;

/*
 * Additional margins that we maintain as safety buffer for node-node
 * overlaps. Compound node labels as well as graph margins are handled
 * separately!
 */
LayoutConstants.COMPOUND_NODE_MARGIN = 5;

/*
 * Default dimension of a non-compound node.
 */
LayoutConstants.SIMPLE_NODE_SIZE = 40;

/*
 * Default dimension of a non-compound node.
 */
LayoutConstants.SIMPLE_NODE_HALF_SIZE = LayoutConstants.SIMPLE_NODE_SIZE / 2;

/*
 * Empty compound node size. When a compound node is empty, its both
 * dimensions should be of this value.
 */
LayoutConstants.EMPTY_COMPOUND_NODE_SIZE = 40;

/*
 * Minimum length that an edge should take during layout
 */
LayoutConstants.MIN_EDGE_LENGTH = 1;

/*
 * World boundaries that layout operates on
 */
LayoutConstants.WORLD_BOUNDARY = 1000000;

/*
 * World boundaries that random positioning can be performed with
 */
LayoutConstants.INITIAL_WORLD_BOUNDARY = LayoutConstants.WORLD_BOUNDARY / 1000;

/*
 * Coordinates of the world center
 */
LayoutConstants.WORLD_CENTER_X = 1200;
LayoutConstants.WORLD_CENTER_Y = 900;

module.exports = LayoutConstants;

},{}],24:[function(_dereq_,module,exports){
/*
 *This class is the javascript implementation of the Point.java class in jdk
 */
function Point(x, y, p) {
  this.x = null;
  this.y = null;
  if (x == null && y == null && p == null) {
    this.x = 0;
    this.y = 0;
  }
  else if (typeof x == 'number' && typeof y == 'number' && p == null) {
    this.x = x;
    this.y = y;
  }
  else if (x.constructor.name == 'Point' && y == null && p == null) {
    p = x;
    this.x = p.x;
    this.y = p.y;
  }
}

Point.prototype.getX = function () {
  return this.x;
}

Point.prototype.getY = function () {
  return this.y;
}

Point.prototype.getLocation = function () {
  return new Point(this.x, this.y);
}

Point.prototype.setLocation = function (x, y, p) {
  if (x.constructor.name == 'Point' && y == null && p == null) {
    p = x;
    this.setLocation(p.x, p.y);
  }
  else if (typeof x == 'number' && typeof y == 'number' && p == null) {
    //if both parameters are integer just move (x,y) location
    if (parseInt(x) == x && parseInt(y) == y) {
      this.move(x, y);
    }
    else {
      this.x = Math.floor(x + 0.5);
      this.y = Math.floor(y + 0.5);
    }
  }
}

Point.prototype.move = function (x, y) {
  this.x = x;
  this.y = y;
}

Point.prototype.translate = function (dx, dy) {
  this.x += dx;
  this.y += dy;
}

Point.prototype.equals = function (obj) {
  if (obj.constructor.name == "Point") {
    var pt = obj;
    return (this.x == pt.x) && (this.y == pt.y);
  }
  return this == obj;
}

Point.prototype.toString = function () {
  return new Point().constructor.name + "[x=" + this.x + ",y=" + this.y + "]";
}

module.exports = Point;

},{}],25:[function(_dereq_,module,exports){
function PointD(x, y) {
  if (x == null && y == null) {
    this.x = 0;
    this.y = 0;
  } else {
    this.x = x;
    this.y = y;
  }
}

PointD.prototype.getX = function ()
{
  return this.x;
};

PointD.prototype.getY = function ()
{
  return this.y;
};

PointD.prototype.setX = function (x)
{
  this.x = x;
};

PointD.prototype.setY = function (y)
{
  this.y = y;
};

PointD.prototype.getDifference = function (pt)
{
  return new DimensionD(this.x - pt.x, this.y - pt.y);
};

PointD.prototype.getCopy = function ()
{
  return new PointD(this.x, this.y);
};

PointD.prototype.translate = function (dim)
{
  this.x += dim.width;
  this.y += dim.height;
  return this;
};

module.exports = PointD;

},{}],26:[function(_dereq_,module,exports){
function RandomSeed() {
}
RandomSeed.seed = 1;
RandomSeed.x = 0;

RandomSeed.nextDouble = function () {
  RandomSeed.x = Math.sin(RandomSeed.seed++) * 10000;
  return RandomSeed.x - Math.floor(RandomSeed.x);
};

module.exports = RandomSeed;

},{}],27:[function(_dereq_,module,exports){
function RectangleD(x, y, width, height) {
  this.x = 0;
  this.y = 0;
  this.width = 0;
  this.height = 0;

  if (x != null && y != null && width != null && height != null) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
}

RectangleD.prototype.getX = function ()
{
  return this.x;
};

RectangleD.prototype.setX = function (x)
{
  this.x = x;
};

RectangleD.prototype.getY = function ()
{
  return this.y;
};

RectangleD.prototype.setY = function (y)
{
  this.y = y;
};

RectangleD.prototype.getWidth = function ()
{
  return this.width;
};

RectangleD.prototype.setWidth = function (width)
{
  this.width = width;
};

RectangleD.prototype.getHeight = function ()
{
  return this.height;
};

RectangleD.prototype.setHeight = function (height)
{
  this.height = height;
};

RectangleD.prototype.getRight = function ()
{
  return this.x + this.width;
};

RectangleD.prototype.getBottom = function ()
{
  return this.y + this.height;
};

RectangleD.prototype.intersects = function (a)
{
  if (this.getRight() < a.x)
  {
    return false;
  }

  if (this.getBottom() < a.y)
  {
    return false;
  }

  if (a.getRight() < this.x)
  {
    return false;
  }

  if (a.getBottom() < this.y)
  {
    return false;
  }

  return true;
};

RectangleD.prototype.getCenterX = function ()
{
  return this.x + this.width / 2;
};

RectangleD.prototype.getMinX = function ()
{
  return this.getX();
};

RectangleD.prototype.getMaxX = function ()
{
  return this.getX() + this.width;
};

RectangleD.prototype.getCenterY = function ()
{
  return this.y + this.height / 2;
};

RectangleD.prototype.getMinY = function ()
{
  return this.getY();
};

RectangleD.prototype.getMaxY = function ()
{
  return this.getY() + this.height;
};

RectangleD.prototype.getWidthHalf = function ()
{
  return this.width / 2;
};

RectangleD.prototype.getHeightHalf = function ()
{
  return this.height / 2;
};

module.exports = RectangleD;

},{}],28:[function(_dereq_,module,exports){
function Transform(x, y) {
  this.lworldOrgX = 0.0;
  this.lworldOrgY = 0.0;
  this.ldeviceOrgX = 0.0;
  this.ldeviceOrgY = 0.0;
  this.lworldExtX = 1.0;
  this.lworldExtY = 1.0;
  this.ldeviceExtX = 1.0;
  this.ldeviceExtY = 1.0;
}

Transform.prototype.getWorldOrgX = function ()
{
  return this.lworldOrgX;
}

Transform.prototype.setWorldOrgX = function (wox)
{
  this.lworldOrgX = wox;
}

Transform.prototype.getWorldOrgY = function ()
{
  return this.lworldOrgY;
}

Transform.prototype.setWorldOrgY = function (woy)
{
  this.lworldOrgY = woy;
}

Transform.prototype.getWorldExtX = function ()
{
  return this.lworldExtX;
}

Transform.prototype.setWorldExtX = function (wex)
{
  this.lworldExtX = wex;
}

Transform.prototype.getWorldExtY = function ()
{
  return this.lworldExtY;
}

Transform.prototype.setWorldExtY = function (wey)
{
  this.lworldExtY = wey;
}

/* Device related */

Transform.prototype.getDeviceOrgX = function ()
{
  return this.ldeviceOrgX;
}

Transform.prototype.setDeviceOrgX = function (dox)
{
  this.ldeviceOrgX = dox;
}

Transform.prototype.getDeviceOrgY = function ()
{
  return this.ldeviceOrgY;
}

Transform.prototype.setDeviceOrgY = function (doy)
{
  this.ldeviceOrgY = doy;
}

Transform.prototype.getDeviceExtX = function ()
{
  return this.ldeviceExtX;
}

Transform.prototype.setDeviceExtX = function (dex)
{
  this.ldeviceExtX = dex;
}

Transform.prototype.getDeviceExtY = function ()
{
  return this.ldeviceExtY;
}

Transform.prototype.setDeviceExtY = function (dey)
{
  this.ldeviceExtY = dey;
}

Transform.prototype.transformX = function (x)
{
  var xDevice = 0.0;
  var worldExtX = this.lworldExtX;
  if (worldExtX != 0.0)
  {
    xDevice = this.ldeviceOrgX +
            ((x - this.lworldOrgX) * this.ldeviceExtX / worldExtX);
  }

  return xDevice;
}

Transform.prototype.transformY = function (y)
{
  var yDevice = 0.0;
  var worldExtY = this.lworldExtY;
  if (worldExtY != 0.0)
  {
    yDevice = this.ldeviceOrgY +
            ((y - this.lworldOrgY) * this.ldeviceExtY / worldExtY);
  }


  return yDevice;
}

Transform.prototype.inverseTransformX = function (x)
{
  var xWorld = 0.0;
  var deviceExtX = this.ldeviceExtX;
  if (deviceExtX != 0.0)
  {
    xWorld = this.lworldOrgX +
            ((x - this.ldeviceOrgX) * this.lworldExtX / deviceExtX);
  }


  return xWorld;
}

Transform.prototype.inverseTransformY = function (y)
{
  var yWorld = 0.0;
  var deviceExtY = this.ldeviceExtY;
  if (deviceExtY != 0.0)
  {
    yWorld = this.lworldOrgY +
            ((y - this.ldeviceOrgY) * this.lworldExtY / deviceExtY);
  }
  return yWorld;
}

Transform.prototype.inverseTransformPoint = function (inPoint)
{
  var outPoint =
          new PointD(this.inverseTransformX(inPoint.x),
                  this.inverseTransformY(inPoint.y));
  return outPoint;
}

module.exports = Transform;

},{}],29:[function(_dereq_,module,exports){
function UniqueIDGeneretor() {
}

UniqueIDGeneretor.lastID = 0;

UniqueIDGeneretor.createID = function (obj) {
  if (UniqueIDGeneretor.isPrimitive(obj)) {
    return obj;
  }
  if (obj.uniqueID != null) {
    return obj.uniqueID;
  }
  obj.uniqueID = UniqueIDGeneretor.getString();
  UniqueIDGeneretor.lastID++;
  return obj.uniqueID;
}

UniqueIDGeneretor.getString = function (id) {
  if (id == null)
    id = UniqueIDGeneretor.lastID;
  return "Object#" + id + "";
}

UniqueIDGeneretor.isPrimitive = function (arg) {
  var type = typeof arg;
  return arg == null || (type != "object" && type != "function");
}

module.exports = UniqueIDGeneretor;

},{}],30:[function(_dereq_,module,exports){
'use strict';

var Thread;

var DimensionD = _dereq_('./DimensionD');
var HashMap = _dereq_('./HashMap');
var HashSet = _dereq_('./HashSet');
var IGeometry = _dereq_('./IGeometry');
var IMath = _dereq_('./IMath');
var Integer = _dereq_('./Integer');
var Point = _dereq_('./Point');
var PointD = _dereq_('./PointD');
var RandomSeed = _dereq_('./RandomSeed');
var RectangleD = _dereq_('./RectangleD');
var Transform = _dereq_('./Transform');
var UniqueIDGeneretor = _dereq_('./UniqueIDGeneretor');
var LGraphObject = _dereq_('./LGraphObject');
var LGraph = _dereq_('./LGraph');
var LEdge = _dereq_('./LEdge');
var LGraphManager = _dereq_('./LGraphManager');
var LNode = _dereq_('./LNode');
var Layout = _dereq_('./Layout');
var LayoutConstants = _dereq_('./LayoutConstants');
var FDLayout = _dereq_('./FDLayout');
var FDLayoutConstants = _dereq_('./FDLayoutConstants');
var FDLayoutEdge = _dereq_('./FDLayoutEdge');
var FDLayoutNode = _dereq_('./FDLayoutNode');
var CoSEConstants = _dereq_('./CoSEConstants');
var CoSEEdge = _dereq_('./CoSEEdge');
var CoSEGraph = _dereq_('./CoSEGraph');
var CoSEGraphManager = _dereq_('./CoSEGraphManager');
var CoSELayout = _dereq_('./CoSELayout');
var CoSENode = _dereq_('./CoSENode');
var layoutOptionsPack = _dereq_('./layoutOptionsPack');

layoutOptionsPack.layoutQuality; // proof, default, draft
layoutOptionsPack.animationDuringLayout; // T-F
layoutOptionsPack.animationOnLayout; // T-F
layoutOptionsPack.animationPeriod; // 0-100
layoutOptionsPack.incremental; // T-F
layoutOptionsPack.createBendsAsNeeded; // T-F
layoutOptionsPack.uniformLeafNodeSizes; // T-F

layoutOptionsPack.defaultLayoutQuality = LayoutConstants.DEFAULT_QUALITY;
layoutOptionsPack.defaultAnimationDuringLayout = LayoutConstants.DEFAULT_ANIMATION_DURING_LAYOUT;
layoutOptionsPack.defaultAnimationOnLayout = LayoutConstants.DEFAULT_ANIMATION_ON_LAYOUT;
layoutOptionsPack.defaultAnimationPeriod = 50;
layoutOptionsPack.defaultIncremental = LayoutConstants.DEFAULT_INCREMENTAL;
layoutOptionsPack.defaultCreateBendsAsNeeded = LayoutConstants.DEFAULT_CREATE_BENDS_AS_NEEDED;
layoutOptionsPack.defaultUniformLeafNodeSizes = LayoutConstants.DEFAULT_UNIFORM_LEAF_NODE_SIZES;

function setDefaultLayoutProperties() {
  layoutOptionsPack.layoutQuality = layoutOptionsPack.defaultLayoutQuality;
  layoutOptionsPack.animationDuringLayout = layoutOptionsPack.defaultAnimationDuringLayout;
  layoutOptionsPack.animationOnLayout = layoutOptionsPack.defaultAnimationOnLayout;
  layoutOptionsPack.animationPeriod = layoutOptionsPack.defaultAnimationPeriod;
  layoutOptionsPack.incremental = layoutOptionsPack.defaultIncremental;
  layoutOptionsPack.createBendsAsNeeded = layoutOptionsPack.defaultCreateBendsAsNeeded;
  layoutOptionsPack.uniformLeafNodeSizes = layoutOptionsPack.defaultUniformLeafNodeSizes;
}

setDefaultLayoutProperties();

function fillCoseLayoutOptionsPack() {
  layoutOptionsPack.defaultIdealEdgeLength = CoSEConstants.DEFAULT_EDGE_LENGTH;
  layoutOptionsPack.defaultSpringStrength = 50;
  layoutOptionsPack.defaultRepulsionStrength = 50;
  layoutOptionsPack.defaultSmartRepulsionRangeCalc = CoSEConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
  layoutOptionsPack.defaultGravityStrength = 50;
  layoutOptionsPack.defaultGravityRange = 50;
  layoutOptionsPack.defaultCompoundGravityStrength = 50;
  layoutOptionsPack.defaultCompoundGravityRange = 50;
  layoutOptionsPack.defaultSmartEdgeLengthCalc = CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;
  layoutOptionsPack.defaultMultiLevelScaling = CoSEConstants.DEFAULT_USE_MULTI_LEVEL_SCALING;

  layoutOptionsPack.idealEdgeLength = layoutOptionsPack.defaultIdealEdgeLength;
  layoutOptionsPack.springStrength = layoutOptionsPack.defaultSpringStrength;
  layoutOptionsPack.repulsionStrength = layoutOptionsPack.defaultRepulsionStrength;
  layoutOptionsPack.smartRepulsionRangeCalc = layoutOptionsPack.defaultSmartRepulsionRangeCalc;
  layoutOptionsPack.gravityStrength = layoutOptionsPack.defaultGravityStrength;
  layoutOptionsPack.gravityRange = layoutOptionsPack.defaultGravityRange;
  layoutOptionsPack.compoundGravityStrength = layoutOptionsPack.defaultCompoundGravityStrength;
  layoutOptionsPack.compoundGravityRange = layoutOptionsPack.defaultCompoundGravityRange;
  layoutOptionsPack.smartEdgeLengthCalc = layoutOptionsPack.defaultSmartEdgeLengthCalc;
  layoutOptionsPack.multiLevelScaling = layoutOptionsPack.defaultMultiLevelScaling;
}

_CoSELayout.idToLNode = {};
_CoSELayout.toBeTiled = {};

var defaults = {
  // Called on `layoutready`
  ready: function () {
  },
  // Called on `layoutstop`
  stop: function () {
  },
  // Whether to fit the network view after when done
  fit: true,
  // Padding on fit
  padding: 10,
  // Whether to enable incremental mode
  randomize: false,
  // Node repulsion (non overlapping) multiplier
  nodeRepulsion: 4500,
  // Ideal edge (non nested) length
  idealEdgeLength: 50,
  // Divisor to compute edge forces
  edgeElasticity: 0.45,
  // Nesting factor (multiplier) to compute ideal edge length for nested edges
  nestingFactor: 0.1,
  // Gravity force (constant)
  gravity: 0.4,
  // Maximum number of iterations to perform
  numIter: 2500,
  // For enabling tiling
  tile: true,
  //whether to make animation while performing the layout
  animate: true
};

function extend(defaults, options) {
  var obj = {};

  for (var i in defaults) {
    obj[i] = defaults[i];
  }

  for (var i in options) {
    obj[i] = options[i];
  }

  return obj;
}
;

_CoSELayout.layout = new CoSELayout();
function _CoSELayout(options) {

  this.options = extend(defaults, options);
  FDLayoutConstants.getUserOptions(this.options);
  fillCoseLayoutOptionsPack();
}

_CoSELayout.prototype.run = function () {
  var layout = this;

  _CoSELayout.idToLNode = {};
  _CoSELayout.toBeTiled = {};
  _CoSELayout.layout = new CoSELayout();
  this.cy = this.options.cy;
  var after = this;

  this.cy.trigger('layoutstart');

  var gm = _CoSELayout.layout.newGraphManager();
  this.gm = gm;

  var nodes = this.options.eles.nodes();
  var edges = this.options.eles.edges();

  this.root = gm.addRoot();

  if (!this.options.tile) {
    this.processChildrenList(this.root, nodes.orphans());
  }
  else {
    // Find zero degree nodes and create a compound for each level
    var memberGroups = this.groupZeroDegreeMembers();
    // Tile and clear children of each compound
    var tiledMemberPack = this.clearCompounds(this.options);
    // Separately tile and clear zero degree nodes for each level
    var tiledZeroDegreeNodes = this.clearZeroDegreeMembers(memberGroups);
  }


  for (var i = 0; i < edges.length; i++) {
    var edge = edges[i];
    var sourceNode = _CoSELayout.idToLNode[edge.data("source")];
    var targetNode = _CoSELayout.idToLNode[edge.data("target")];
    var e1 = gm.add(_CoSELayout.layout.newEdge(), sourceNode, targetNode);
    e1.id = edge.id();
  }


  var t1 = layout.thread;

  if (!t1 || t1.stopped()) { // try to reuse threads
    t1 = layout.thread = Thread();

    t1.require(DimensionD, 'DimensionD');
    t1.require(HashMap, 'HashMap');
    t1.require(HashSet, 'HashSet');
    t1.require(IGeometry, 'IGeometry');
    t1.require(IMath, 'IMath');
    t1.require(Integer, 'Integer');
    t1.require(Point, 'Point');
    t1.require(PointD, 'PointD');
    t1.require(RandomSeed, 'RandomSeed');
    t1.require(RectangleD, 'RectangleD');
    t1.require(Transform, 'Transform');
    t1.require(UniqueIDGeneretor, 'UniqueIDGeneretor');
    t1.require(LGraphObject, 'LGraphObject');
    t1.require(LGraph, 'LGraph');
    t1.require(LEdge, 'LEdge');
    t1.require(LGraphManager, 'LGraphManager');
    t1.require(LNode, 'LNode');
    t1.require(Layout, 'Layout');
    t1.require(LayoutConstants, 'LayoutConstants');
    t1.require(layoutOptionsPack, 'layoutOptionsPack');
    t1.require(FDLayout, 'FDLayout');
    t1.require(FDLayoutConstants, 'FDLayoutConstants');
    t1.require(FDLayoutEdge, 'FDLayoutEdge');
    t1.require(FDLayoutNode, 'FDLayoutNode');
    t1.require(CoSEConstants, 'CoSEConstants');
    t1.require(CoSEEdge, 'CoSEEdge');
    t1.require(CoSEGraph, 'CoSEGraph');
    t1.require(CoSEGraphManager, 'CoSEGraphManager');
    t1.require(CoSELayout, 'CoSELayout');
    t1.require(CoSENode, 'CoSENode');
  }

  var nodes = this.options.eles.nodes();
  var edges = this.options.eles.edges();

  // First I need to create the data structure to pass to the worker
  var pData = {
    'nodes': [],
    'edges': []
  };

  var lnodes = gm.getAllNodes();
  for (var i = 0; i < lnodes.length; i++) {
    var lnode = lnodes[i];
    var nodeId = lnode.id;
    var cyNode = this.options.cy.getElementById(nodeId);
    var parentId = cyNode.data('parent');
    var w = lnode.rect.width;
    var posX = lnode.rect.x;
    var posY = lnode.rect.y;
    var h = lnode.rect.height;
    var dummy_parent_id = cyNode.data('dummy_parent_id');

    pData[ 'nodes' ].push({
      id: nodeId,
      pid: parentId,
      x: posX,
      y: posY,
      width: w,
      height: h,
      dummy_parent_id: dummy_parent_id
    });

  }

  var ledges = gm.getAllEdges();
  for (var i = 0; i < ledges.length; i++) {
    var ledge = ledges[i];
    var edgeId = ledge.id;
    var cyEdge = this.options.cy.getElementById(edgeId);
    var srcNodeId = cyEdge.source().id();
    var tgtNodeId = cyEdge.target().id();
    pData[ 'edges' ].push({
      id: edgeId,
      source: srcNodeId,
      target: tgtNodeId
    });
  }

  var ready = false;

  t1.pass(pData).run(function (pData) {
    var log = function (msg) {
      broadcast({log: msg});
    };

    log("start thread");

    //the layout will be run in the thread and the results are to be passed
    //to the main thread with the result map
    var layout_t = new CoSELayout();
    var gm_t = layout_t.newGraphManager();
    var ngraph = gm_t.layout.newGraph();
    var nnode = gm_t.layout.newNode(null);
    var root = gm_t.add(ngraph, nnode);
    root.graphManager = gm_t;
    gm_t.setRootGraph(root);
    var root_t = gm_t.rootGraph;

    //maps for inner usage of the thread
    var orphans_t = [];
    var idToLNode_t = {};
    var childrenMap = {};

    //A map of node id to corresponding node position and sizes
    //it is to be returned at the end of the thread function
    var result = {};

    //this function is similar to processChildrenList function in the main thread
    //it is to process the nodes in correct order recursively
    var processNodes = function (parent, children) {
      var size = children.length;
      for (var i = 0; i < size; i++) {
        var theChild = children[i];
        var children_of_children = childrenMap[theChild.id];
        var theNode;

        if (theChild.width != null
                && theChild.height != null) {
          theNode = parent.add(new CoSENode(gm_t,
                  new PointD(theChild.x, theChild.y),
                  new DimensionD(parseFloat(theChild.width),
                          parseFloat(theChild.height))));
        }
        else {
          theNode = parent.add(new CoSENode(gm_t));
        }
        theNode.id = theChild.id;
        idToLNode_t[theChild.id] = theNode;

        if (isNaN(theNode.rect.x)) {
          theNode.rect.x = 0;
        }

        if (isNaN(theNode.rect.y)) {
          theNode.rect.y = 0;
        }

        if (children_of_children != null && children_of_children.length > 0) {
          var theNewGraph;
          theNewGraph = layout_t.getGraphManager().add(layout_t.newGraph(), theNode);
          theNewGraph.graphManager = gm_t;
          processNodes(theNewGraph, children_of_children);
        }
      }
    }

    //fill the chidrenMap and orphans_t maps to process the nodes in the correct order
    var nodes = pData.nodes;
    for (var i = 0; i < nodes.length; i++) {
      var theNode = nodes[i];
      var p_id = theNode.pid;
      if (p_id != null) {
        if (childrenMap[p_id] == null) {
          childrenMap[p_id] = [];
        }
        childrenMap[p_id].push(theNode);
      }
      else {
        orphans_t.push(theNode);
      }
    }

    processNodes(root_t, orphans_t);

    //handle the edges
    var edges = pData.edges;
    for (var i = 0; i < edges.length; i++) {
      var edge = edges[i];
      var sourceNode = idToLNode_t[edge.source];
      var targetNode = idToLNode_t[edge.target];
      var e1 = gm_t.add(layout_t.newEdge(), sourceNode, targetNode);
    }

    //run the layout crated in this thread
    layout_t.runLayout();

    //fill the result map
    for (var id in idToLNode_t) {
      var lNode = idToLNode_t[id];
      var rect = lNode.rect;
      result[id] = {
        id: id,
        x: rect.x,
        y: rect.y,
        w: rect.width,
        h: rect.height
      };
    }
    var seeds = {};
    seeds.rsSeed = RandomSeed.seed;
    seeds.rsX = RandomSeed.x;
    var pass = {
      result: result,
      seeds: seeds
    }
    //return the result map to pass it to the then function as parameter
    return pass;
  }).then(function (pass) {
    var result = pass.result;
    var seeds = pass.seeds;
    RandomSeed.seed = seeds.rsSeed;
    RandomSeed.x = seeds.rsX;
    //refresh the lnode positions and sizes by using result map
    for (var id in result) {
      var lNode = _CoSELayout.idToLNode[id];
      var node = result[id];
      lNode.rect.x = node.x;
      lNode.rect.y = node.y;
      lNode.rect.width = node.w;
      lNode.rect.height = node.h;
    }
    if (after.options.tile) {
      // Repopulate members
      after.repopulateZeroDegreeMembers(tiledZeroDegreeNodes);
      after.repopulateCompounds(tiledMemberPack);
      after.options.eles.nodes().updateCompoundBounds();
    }

    after.options.eles.nodes().positions(function (i, ele) {
      var theId = ele.data('id');
      var lNode = _CoSELayout.idToLNode[theId];

      return {
        x: lNode.getRect().getCenterX(),
        y: lNode.getRect().getCenterY()
      };
    });

    if (after.options.fit)
      after.options.cy.fit(after.options.eles.nodes(), after.options.padding);

    //trigger layoutready when each node has had its position set at least once
    if (!ready) {
      after.cy.one('layoutready', after.options.ready);
      after.cy.trigger('layoutready');
    }

    // trigger layoutstop when the layout stops (e.g. finishes)
    after.cy.one('layoutstop', after.options.stop);
    after.cy.trigger('layoutstop');
    t1.stop();

    after.options.eles.nodes().removeData('dummy_parent_id');
  });

  t1.on('message', function (e) {
    var logMsg = e.message.log;
    if (logMsg != null) {
      console.log('Thread log: ' + logMsg);
      return;
    }
    var pData = e.message.pData;
    if (pData != null) {
      after.options.eles.nodes().positions(function (i, ele) {
        if (ele.data('dummy_parent_id')) {
          return {
            x: pData[ele.data('dummy_parent_id')].x,
            y: pData[ele.data('dummy_parent_id')].y
          };
        }
        var theId = ele.data('id');
        var pNode = pData[theId];
        var temp = this;
        while (pNode == null) {
          temp = temp.parent()[0];
          pNode = pData[temp.id()];
          pData[theId] = pNode;
        }
        return {
          x: pNode.x,
          y: pNode.y
        };
      });

      if (after.options.fit)
        after.options.cy.fit(after.options.eles.nodes(), after.options.padding);

      if (!ready) {
        ready = true;
        after.one('layoutready', after.options.ready);
        after.trigger({type: 'layoutready', layout: after});
      }
      return;
    }
  });

  return this; // chaining
};

_CoSELayout.prototype.getToBeTiled = function (node) {
  var id = node.data("id");
  //firstly check the previous results
  if (_CoSELayout.toBeTiled[id] != null) {
    return _CoSELayout.toBeTiled[id];
  }

  //only compound nodes are to be tiled
  var children = node.children();
  if (children == null || children.length == 0) {
    _CoSELayout.toBeTiled[id] = false;
    return false;
  }

  //a compound node is not to be tiled if all of its compound children are not to be tiled
  for (var i = 0; i < children.length; i++) {
    var theChild = children[i];

    if (this.getNodeDegree(theChild) > 0) {
      _CoSELayout.toBeTiled[id] = false;
      return false;
    }

    //pass the children not having the compound structure
    if (theChild.children() == null || theChild.children().length == 0) {
      _CoSELayout.toBeTiled[theChild.data("id")] = false;
      continue;
    }

    if (!this.getToBeTiled(theChild)) {
      _CoSELayout.toBeTiled[id] = false;
      return false;
    }
  }
  _CoSELayout.toBeTiled[id] = true;
  return true;
};

_CoSELayout.prototype.getNodeDegree = function (node) {
  var id = node.id();
  var edges = this.options.eles.edges().filter(function (i, ele) {
    var source = ele.data('source');
    var target = ele.data('target');
    if (source != target && (source == id || target == id)) {
      return true;
    }
  });
  return edges.length;
};

_CoSELayout.prototype.getNodeDegreeWithChildren = function (node) {
  var degree = this.getNodeDegree(node);
  var children = node.children();
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    degree += this.getNodeDegreeWithChildren(child);
  }
  return degree;
};

_CoSELayout.prototype.groupZeroDegreeMembers = function () {
  // array of [parent_id x oneDegreeNode_id] 
  var tempMemberGroups = [];
  var memberGroups = [];
  var self = this;
  // Find all zero degree nodes which aren't covered by a compound
  var zeroDegree = this.options.eles.nodes().filter(function (i, ele) {
    if (self.getNodeDegreeWithChildren(ele) == 0 && (ele.parent().length == 0 || (ele.parent().length > 0 && !self.getToBeTiled(ele.parent()[0]))))
      return true;
    else
      return false;
  });

  // Create a map of parent node and its zero degree members
  for (var i = 0; i < zeroDegree.length; i++)
  {
    var node = zeroDegree[i];
    var p_id = node.parent().id();

    if (typeof tempMemberGroups[p_id] === "undefined")
      tempMemberGroups[p_id] = [];

    tempMemberGroups[p_id] = tempMemberGroups[p_id].concat(node);
  }

  // If there are at least two nodes at a level, create a dummy compound for them
  for (var p_id in tempMemberGroups) {
    if (tempMemberGroups[p_id].length > 1) {
      var dummyCompoundId = "DummyCompound_" + p_id;
      memberGroups[dummyCompoundId] = tempMemberGroups[p_id];

      // Create a dummy compound
      if (this.options.cy.getElementById(dummyCompoundId).empty()) {
        this.options.cy.add({
          group: "nodes",
          data: {id: dummyCompoundId, parent: p_id
          }
        });

        var dummy = this.options.cy.nodes()[this.options.cy.nodes().length - 1];
        this.options.eles = this.options.eles.union(dummy);
        dummy.hide();

        for (var i = 0; i < tempMemberGroups[p_id].length; i++) {
          if (i == 0) {
            dummy.data('tempchildren', []);
          }
          var node = tempMemberGroups[p_id][i];
          node.data('dummy_parent_id', dummyCompoundId);
          this.options.cy.add({
            group: "nodes",
            data: {parent: dummyCompoundId, width: node.width(), height: node.height()
            }
          });
          var tempchild = this.options.cy.nodes()[this.options.cy.nodes().length - 1];
          tempchild.hide();
          tempchild.css('width', tempchild.data('width'));
          tempchild.css('height', tempchild.data('height'));
          tempchild.width();
          dummy.data('tempchildren').push(tempchild);
        }
      }
    }
  }

  return memberGroups;
};

_CoSELayout.prototype.performDFSOnCompounds = function (options) {
  var compoundOrder = [];

  var roots = this.options.eles.nodes().orphans();
  this.fillCompexOrderByDFS(compoundOrder, roots);

  return compoundOrder;
};

_CoSELayout.prototype.fillCompexOrderByDFS = function (compoundOrder, children) {
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    this.fillCompexOrderByDFS(compoundOrder, child.children());
    if (this.getToBeTiled(child)) {
      compoundOrder.push(child);
    }
  }
};

_CoSELayout.prototype.clearCompounds = function (options) {
  var childGraphMap = [];

  // Get compound ordering by finding the inner one first
  var compoundOrder = this.performDFSOnCompounds(options);
  _CoSELayout.compoundOrder = compoundOrder;
  this.processChildrenList(this.root, this.options.eles.nodes().orphans());

  for (var i = 0; i < compoundOrder.length; i++) {
    // find the corresponding layout node
    var lCompoundNode = _CoSELayout.idToLNode[compoundOrder[i].id()];

    childGraphMap[compoundOrder[i].id()] = compoundOrder[i].children();

    // Remove children of compounds 
    lCompoundNode.child = null;
  }

  // Tile the removed children
  var tiledMemberPack = this.tileCompoundMembers(childGraphMap);

  return tiledMemberPack;
};

_CoSELayout.prototype.clearZeroDegreeMembers = function (memberGroups) {
  var tiledZeroDegreePack = [];

  for (var id in memberGroups) {
    var compoundNode = _CoSELayout.idToLNode[id];

    tiledZeroDegreePack[id] = this.tileNodes(memberGroups[id]);

    // Set the width and height of the dummy compound as calculated
    compoundNode.rect.width = tiledZeroDegreePack[id].width;
    compoundNode.rect.height = tiledZeroDegreePack[id].height;
  }
  return tiledZeroDegreePack;
};

_CoSELayout.prototype.repopulateCompounds = function (tiledMemberPack) {
  for (var i = _CoSELayout.compoundOrder.length - 1; i >= 0; i--) {
    var id = _CoSELayout.compoundOrder[i].id();
    var lCompoundNode = _CoSELayout.idToLNode[id];

    this.adjustLocations(tiledMemberPack[id], lCompoundNode.rect.x, lCompoundNode.rect.y);
  }
};

_CoSELayout.prototype.repopulateZeroDegreeMembers = function (tiledPack) {
  for (var i in tiledPack) {
    var compound = this.cy.getElementById(i);
    var compoundNode = _CoSELayout.idToLNode[i];

    // Adjust the positions of nodes wrt its compound
    this.adjustLocations(tiledPack[i], compoundNode.rect.x, compoundNode.rect.y);

    var tempchildren = compound.data('tempchildren');
    for (var i = 0; i < tempchildren.length; i++) {
      tempchildren[i].remove();
    }

    // Remove the dummy compound
    compound.remove();
  }
};

/**
 * This method places each zero degree member wrt given (x,y) coordinates (top left). 
 */
_CoSELayout.prototype.adjustLocations = function (organization, x, y) {
  x += organization.compoundMargin;
  y += organization.compoundMargin;

  var left = x;

  for (var i = 0; i < organization.rows.length; i++) {
    var row = organization.rows[i];
    x = left;
    var maxHeight = 0;

    for (var j = 0; j < row.length; j++) {
      var lnode = row[j];

      var node = this.cy.getElementById(lnode.id);
      node.position({
        x: x + lnode.rect.width / 2,
        y: y + lnode.rect.height / 2
      });

      lnode.rect.x = x;// + lnode.rect.width / 2;
      lnode.rect.y = y;// + lnode.rect.height / 2;

      x += lnode.rect.width + organization.horizontalPadding;

      if (lnode.rect.height > maxHeight)
        maxHeight = lnode.rect.height;
    }

    y += maxHeight + organization.verticalPadding;
  }
};

_CoSELayout.prototype.tileCompoundMembers = function (childGraphMap) {
  var tiledMemberPack = [];

  for (var id in childGraphMap) {
    // Access layoutInfo nodes to set the width and height of compounds
    var compoundNode = _CoSELayout.idToLNode[id];

    tiledMemberPack[id] = this.tileNodes(childGraphMap[id]);

    compoundNode.rect.width = tiledMemberPack[id].width + 20;
    compoundNode.rect.height = tiledMemberPack[id].height + 20;
  }

  return tiledMemberPack;
};

_CoSELayout.prototype.tileNodes = function (nodes) {
  var organization = {
    rows: [],
    rowWidth: [],
    rowHeight: [],
    compoundMargin: 10,
    width: 20,
    height: 20,
    verticalPadding: 10,
    horizontalPadding: 10
  };

  var layoutNodes = [];

  // Get layout nodes
  for (var i = 0; i < nodes.length; i++) {
    var node = nodes[i];
    var lNode = _CoSELayout.idToLNode[node.id()];

    if (!node.data('dummy_parent_id')) {
      var owner = lNode.owner;
      owner.remove(lNode);

      this.gm.resetAllNodes();
      this.gm.getAllNodes();
    }

    layoutNodes.push(lNode);
  }

  // Sort the nodes in ascending order of their areas
  layoutNodes.sort(function (n1, n2) {
    if (n1.rect.width * n1.rect.height > n2.rect.width * n2.rect.height)
      return -1;
    if (n1.rect.width * n1.rect.height < n2.rect.width * n2.rect.height)
      return 1;
    return 0;
  });

  // Create the organization -> tile members
  for (var i = 0; i < layoutNodes.length; i++) {
    var lNode = layoutNodes[i];

    if (organization.rows.length == 0) {
      this.insertNodeToRow(organization, lNode, 0);
    }
    else if (this.canAddHorizontal(organization, lNode.rect.width, lNode.rect.height)) {
      this.insertNodeToRow(organization, lNode, this.getShortestRowIndex(organization));
    }
    else {
      this.insertNodeToRow(organization, lNode, organization.rows.length);
    }

    this.shiftToLastRow(organization);
  }

  return organization;
};

_CoSELayout.prototype.insertNodeToRow = function (organization, node, rowIndex) {
  var minCompoundSize = organization.compoundMargin * 2;

  // Add new row if needed
  if (rowIndex == organization.rows.length) {
    var secondDimension = [];

    organization.rows.push(secondDimension);
    organization.rowWidth.push(minCompoundSize);
    organization.rowHeight.push(0);
  }

  // Update row width
  var w = organization.rowWidth[rowIndex] + node.rect.width;

  if (organization.rows[rowIndex].length > 0) {
    w += organization.horizontalPadding;
  }

  organization.rowWidth[rowIndex] = w;
  // Update compound width
  if (organization.width < w) {
    organization.width = w;
  }

  // Update height
  var h = node.rect.height;
  if (rowIndex > 0)
    h += organization.verticalPadding;

  var extraHeight = 0;
  if (h > organization.rowHeight[rowIndex]) {
    extraHeight = organization.rowHeight[rowIndex];
    organization.rowHeight[rowIndex] = h;
    extraHeight = organization.rowHeight[rowIndex] - extraHeight;
  }

  organization.height += extraHeight;

  // Insert node
  organization.rows[rowIndex].push(node);
};

//Scans the rows of an organization and returns the one with the min width
_CoSELayout.prototype.getShortestRowIndex = function (organization) {
  var r = -1;
  var min = Number.MAX_VALUE;

  for (var i = 0; i < organization.rows.length; i++) {
    if (organization.rowWidth[i] < min) {
      r = i;
      min = organization.rowWidth[i];
    }
  }
  return r;
};

//Scans the rows of an organization and returns the one with the max width
_CoSELayout.prototype.getLongestRowIndex = function (organization) {
  var r = -1;
  var max = Number.MIN_VALUE;

  for (var i = 0; i < organization.rows.length; i++) {

    if (organization.rowWidth[i] > max) {
      r = i;
      max = organization.rowWidth[i];
    }
  }

  return r;
};

/**
 * This method checks whether adding extra width to the organization violates
 * the aspect ratio(1) or not.
 */
_CoSELayout.prototype.canAddHorizontal = function (organization, extraWidth, extraHeight) {

  var sri = this.getShortestRowIndex(organization);

  if (sri < 0) {
    return true;
  }

  var min = organization.rowWidth[sri];

  if (min + organization.horizontalPadding + extraWidth <= organization.width)
    return true;

  var hDiff = 0;

  // Adding to an existing row
  if (organization.rowHeight[sri] < extraHeight) {
    if (sri > 0)
      hDiff = extraHeight + organization.verticalPadding - organization.rowHeight[sri];
  }

  var add_to_row_ratio;
  if (organization.width - min >= extraWidth + organization.horizontalPadding) {
    add_to_row_ratio = (organization.height + hDiff) / (min + extraWidth + organization.horizontalPadding);
  } else {
    add_to_row_ratio = (organization.height + hDiff) / organization.width;
  }

  // Adding a new row for this node
  hDiff = extraHeight + organization.verticalPadding;
  var add_new_row_ratio;
  if (organization.width < extraWidth) {
    add_new_row_ratio = (organization.height + hDiff) / extraWidth;
  } else {
    add_new_row_ratio = (organization.height + hDiff) / organization.width;
  }

  if (add_new_row_ratio < 1)
    add_new_row_ratio = 1 / add_new_row_ratio;

  if (add_to_row_ratio < 1)
    add_to_row_ratio = 1 / add_to_row_ratio;

  return add_to_row_ratio < add_new_row_ratio;
};


//If moving the last node from the longest row and adding it to the last
//row makes the bounding box smaller, do it.
_CoSELayout.prototype.shiftToLastRow = function (organization) {
  var longest = this.getLongestRowIndex(organization);
  var last = organization.rowWidth.length - 1;
  var row = organization.rows[longest];
  var node = row[row.length - 1];

  var diff = node.width + organization.horizontalPadding;

  // Check if there is enough space on the last row
  if (organization.width - organization.rowWidth[last] > diff && longest != last) {
    // Remove the last element of the longest row
    row.splice(-1, 1);

    // Push it to the last row
    organization.rows[last].push(node);

    organization.rowWidth[longest] = organization.rowWidth[longest] - diff;
    organization.rowWidth[last] = organization.rowWidth[last] + diff;
    organization.width = organization.rowWidth[this.getLongestRowIndex(organization)];

    // Update heights of the organization
    var maxHeight = Number.MIN_VALUE;
    for (var i = 0; i < row.length; i++) {
      if (row[i].height > maxHeight)
        maxHeight = row[i].height;
    }
    if (longest > 0)
      maxHeight += organization.verticalPadding;

    var prevTotal = organization.rowHeight[longest] + organization.rowHeight[last];

    organization.rowHeight[longest] = maxHeight;
    if (organization.rowHeight[last] < node.height + organization.verticalPadding)
      organization.rowHeight[last] = node.height + organization.verticalPadding;

    var finalTotal = organization.rowHeight[longest] + organization.rowHeight[last];
    organization.height += (finalTotal - prevTotal);

    this.shiftToLastRow(organization);
  }
};

/**
 * @brief : called on continuous layouts to stop them before they finish
 */
_CoSELayout.prototype.stop = function () {
  this.stopped = true;

  return this; // chaining
};

_CoSELayout.prototype.processChildrenList = function (parent, children) {
  var size = children.length;
  for (var i = 0; i < size; i++) {
    var theChild = children[i];
    this.options.eles.nodes().length;
    var children_of_children = theChild.children();
    var theNode;

    if (theChild.width() != null
            && theChild.height() != null) {
      theNode = parent.add(new CoSENode(_CoSELayout.layout.graphManager,
              new PointD(theChild.position('x'), theChild.position('y')),
              new DimensionD(parseFloat(theChild.width()),
                      parseFloat(theChild.height()))));
    }
    else {
      theNode = parent.add(new CoSENode(this.graphManager));
    }
    theNode.id = theChild.data("id");
    _CoSELayout.idToLNode[theChild.data("id")] = theNode;

    if (isNaN(theNode.rect.x)) {
      theNode.rect.x = 0;
    }

    if (isNaN(theNode.rect.y)) {
      theNode.rect.y = 0;
    }

    if (children_of_children != null && children_of_children.length > 0) {
      var theNewGraph;
      theNewGraph = _CoSELayout.layout.getGraphManager().add(_CoSELayout.layout.newGraph(), theNode);
      this.processChildrenList(theNewGraph, children_of_children);
    }
  }
};

module.exports = function get(cytoscape) {
  Thread = cytoscape.Thread;

  return _CoSELayout;
};
},{"./CoSEConstants":1,"./CoSEEdge":2,"./CoSEGraph":3,"./CoSEGraphManager":4,"./CoSELayout":5,"./CoSENode":6,"./DimensionD":7,"./FDLayout":8,"./FDLayoutConstants":9,"./FDLayoutEdge":10,"./FDLayoutNode":11,"./HashMap":12,"./HashSet":13,"./IGeometry":14,"./IMath":15,"./Integer":16,"./LEdge":17,"./LGraph":18,"./LGraphManager":19,"./LGraphObject":20,"./LNode":21,"./Layout":22,"./LayoutConstants":23,"./Point":24,"./PointD":25,"./RandomSeed":26,"./RectangleD":27,"./Transform":28,"./UniqueIDGeneretor":29,"./layoutOptionsPack":31}],31:[function(_dereq_,module,exports){
function layoutOptionsPack() {
}

module.exports = layoutOptionsPack;
},{}],32:[function(_dereq_,module,exports){
'use strict';

(function(){

  // registers the extension on a cytoscape lib ref
  var getLayout = _dereq_('./Layout');
  var register = function( cytoscape ){
    var Layout = getLayout( cytoscape );

    cytoscape('layout', 'cose-bilkent', Layout);
  };

  if( typeof module !== 'undefined' && module.exports ){ // expose as a commonjs module
    module.exports = register;
  }

  if( typeof define !== 'undefined' && define.amd ){ // expose as an amd/requirejs module
    define('cytoscape-cose-bilkent', function(){
      return register;
    });
  }

  if( typeof cytoscape !== 'undefined' ){ // expose to global cytoscape (i.e. window.cytoscape)
    register( cytoscape );
  }

})();

},{"./Layout":30}]},{},[32])
//# sourceMappingURL=data:application/json;charset:utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvTGF5b3V0L0NvU0VDb25zdGFudHMuanMiLCJzcmMvTGF5b3V0L0NvU0VFZGdlLmpzIiwic3JjL0xheW91dC9Db1NFR3JhcGguanMiLCJzcmMvTGF5b3V0L0NvU0VHcmFwaE1hbmFnZXIuanMiLCJzcmMvTGF5b3V0L0NvU0VMYXlvdXQuanMiLCJzcmMvTGF5b3V0L0NvU0VOb2RlLmpzIiwic3JjL0xheW91dC9EaW1lbnNpb25ELmpzIiwic3JjL0xheW91dC9GRExheW91dC5qcyIsInNyYy9MYXlvdXQvRkRMYXlvdXRDb25zdGFudHMuanMiLCJzcmMvTGF5b3V0L0ZETGF5b3V0RWRnZS5qcyIsInNyYy9MYXlvdXQvRkRMYXlvdXROb2RlLmpzIiwic3JjL0xheW91dC9IYXNoTWFwLmpzIiwic3JjL0xheW91dC9IYXNoU2V0LmpzIiwic3JjL0xheW91dC9JR2VvbWV0cnkuanMiLCJzcmMvTGF5b3V0L0lNYXRoLmpzIiwic3JjL0xheW91dC9JbnRlZ2VyLmpzIiwic3JjL0xheW91dC9MRWRnZS5qcyIsInNyYy9MYXlvdXQvTEdyYXBoLmpzIiwic3JjL0xheW91dC9MR3JhcGhNYW5hZ2VyLmpzIiwic3JjL0xheW91dC9MR3JhcGhPYmplY3QuanMiLCJzcmMvTGF5b3V0L0xOb2RlLmpzIiwic3JjL0xheW91dC9MYXlvdXQuanMiLCJzcmMvTGF5b3V0L0xheW91dENvbnN0YW50cy5qcyIsInNyYy9MYXlvdXQvUG9pbnQuanMiLCJzcmMvTGF5b3V0L1BvaW50RC5qcyIsInNyYy9MYXlvdXQvUmFuZG9tU2VlZC5qcyIsInNyYy9MYXlvdXQvUmVjdGFuZ2xlRC5qcyIsInNyYy9MYXlvdXQvVHJhbnNmb3JtLmpzIiwic3JjL0xheW91dC9VbmlxdWVJREdlbmVyZXRvci5qcyIsInNyYy9MYXlvdXQvaW5kZXguanMiLCJzcmMvTGF5b3V0L2xheW91dE9wdGlvbnNQYWNrLmpzIiwic3JjL2luZGV4LmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDZkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDWkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDWkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDWkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9hQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkhBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzlCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5V0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNmQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMxQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkRBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMxWkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDUEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2SkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNuY0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdGVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNMQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdWQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2cEJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDbEZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDekVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDWEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNsSUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzNKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDN0JBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM5L0JBO0FBQ0E7QUFDQTtBQUNBOztBQ0hBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsInZhciBGRExheW91dENvbnN0YW50cyA9IHJlcXVpcmUoJy4vRkRMYXlvdXRDb25zdGFudHMnKTtcclxuXHJcbmZ1bmN0aW9uIENvU0VDb25zdGFudHMoKSB7XHJcbn1cclxuXHJcbi8vQ29TRUNvbnN0YW50cyBpbmhlcml0cyBzdGF0aWMgcHJvcHMgaW4gRkRMYXlvdXRDb25zdGFudHNcclxuZm9yICh2YXIgcHJvcCBpbiBGRExheW91dENvbnN0YW50cykge1xyXG4gIENvU0VDb25zdGFudHNbcHJvcF0gPSBGRExheW91dENvbnN0YW50c1twcm9wXTtcclxufVxyXG5cclxuQ29TRUNvbnN0YW50cy5ERUZBVUxUX1VTRV9NVUxUSV9MRVZFTF9TQ0FMSU5HID0gZmFsc2U7XHJcbkNvU0VDb25zdGFudHMuREVGQVVMVF9SQURJQUxfU0VQQVJBVElPTiA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEg7XHJcbkNvU0VDb25zdGFudHMuREVGQVVMVF9DT01QT05FTlRfU0VQRVJBVElPTiA9IDYwO1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBDb1NFQ29uc3RhbnRzO1xyXG4iLCJ2YXIgRkRMYXlvdXRFZGdlID0gcmVxdWlyZSgnLi9GRExheW91dEVkZ2UnKTtcclxuXHJcbmZ1bmN0aW9uIENvU0VFZGdlKHNvdXJjZSwgdGFyZ2V0LCB2RWRnZSkge1xyXG4gIEZETGF5b3V0RWRnZS5jYWxsKHRoaXMsIHNvdXJjZSwgdGFyZ2V0LCB2RWRnZSk7XHJcbn1cclxuXHJcbkNvU0VFZGdlLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoRkRMYXlvdXRFZGdlLnByb3RvdHlwZSk7XHJcbmZvciAodmFyIHByb3AgaW4gRkRMYXlvdXRFZGdlKSB7XHJcbiAgQ29TRUVkZ2VbcHJvcF0gPSBGRExheW91dEVkZ2VbcHJvcF07XHJcbn1cclxuXHJcbm1vZHVsZS5leHBvcnRzID0gQ29TRUVkZ2VcclxuIiwidmFyIExHcmFwaCA9IHJlcXVpcmUoJy4vTEdyYXBoJyk7XHJcblxyXG5mdW5jdGlvbiBDb1NFR3JhcGgocGFyZW50LCBncmFwaE1nciwgdkdyYXBoKSB7XHJcbiAgTEdyYXBoLmNhbGwodGhpcywgcGFyZW50LCBncmFwaE1nciwgdkdyYXBoKTtcclxufVxyXG5cclxuQ29TRUdyYXBoLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoTEdyYXBoLnByb3RvdHlwZSk7XHJcbmZvciAodmFyIHByb3AgaW4gTEdyYXBoKSB7XHJcbiAgQ29TRUdyYXBoW3Byb3BdID0gTEdyYXBoW3Byb3BdO1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IENvU0VHcmFwaDtcclxuIiwidmFyIExHcmFwaE1hbmFnZXIgPSByZXF1aXJlKCcuL0xHcmFwaE1hbmFnZXInKTtcclxuXHJcbmZ1bmN0aW9uIENvU0VHcmFwaE1hbmFnZXIobGF5b3V0KSB7XHJcbiAgTEdyYXBoTWFuYWdlci5jYWxsKHRoaXMsIGxheW91dCk7XHJcbn1cclxuXHJcbkNvU0VHcmFwaE1hbmFnZXIucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShMR3JhcGhNYW5hZ2VyLnByb3RvdHlwZSk7XHJcbmZvciAodmFyIHByb3AgaW4gTEdyYXBoTWFuYWdlcikge1xyXG4gIENvU0VHcmFwaE1hbmFnZXJbcHJvcF0gPSBMR3JhcGhNYW5hZ2VyW3Byb3BdO1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IENvU0VHcmFwaE1hbmFnZXI7XHJcbiIsInZhciBGRExheW91dCA9IHJlcXVpcmUoJy4vRkRMYXlvdXQnKTtcclxudmFyIENvU0VHcmFwaE1hbmFnZXIgPSByZXF1aXJlKCcuL0NvU0VHcmFwaE1hbmFnZXInKTtcclxudmFyIENvU0VHcmFwaCA9IHJlcXVpcmUoJy4vQ29TRUdyYXBoJyk7XHJcbnZhciBDb1NFTm9kZSA9IHJlcXVpcmUoJy4vQ29TRU5vZGUnKTtcclxudmFyIENvU0VFZGdlID0gcmVxdWlyZSgnLi9Db1NFRWRnZScpO1xyXG5cclxuZnVuY3Rpb24gQ29TRUxheW91dCgpIHtcclxuICBGRExheW91dC5jYWxsKHRoaXMpO1xyXG59XHJcblxyXG5Db1NFTGF5b3V0LnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoRkRMYXlvdXQucHJvdG90eXBlKTtcclxuXHJcbmZvciAodmFyIHByb3AgaW4gRkRMYXlvdXQpIHtcclxuICBDb1NFTGF5b3V0W3Byb3BdID0gRkRMYXlvdXRbcHJvcF07XHJcbn1cclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLm5ld0dyYXBoTWFuYWdlciA9IGZ1bmN0aW9uICgpIHtcclxuICB2YXIgZ20gPSBuZXcgQ29TRUdyYXBoTWFuYWdlcih0aGlzKTtcclxuICB0aGlzLmdyYXBoTWFuYWdlciA9IGdtO1xyXG4gIHJldHVybiBnbTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLm5ld0dyYXBoID0gZnVuY3Rpb24gKHZHcmFwaCkge1xyXG4gIHJldHVybiBuZXcgQ29TRUdyYXBoKG51bGwsIHRoaXMuZ3JhcGhNYW5hZ2VyLCB2R3JhcGgpO1xyXG59O1xyXG5cclxuQ29TRUxheW91dC5wcm90b3R5cGUubmV3Tm9kZSA9IGZ1bmN0aW9uICh2Tm9kZSkge1xyXG4gIHJldHVybiBuZXcgQ29TRU5vZGUodGhpcy5ncmFwaE1hbmFnZXIsIHZOb2RlKTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLm5ld0VkZ2UgPSBmdW5jdGlvbiAodkVkZ2UpIHtcclxuICByZXR1cm4gbmV3IENvU0VFZGdlKG51bGwsIG51bGwsIHZFZGdlKTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLmluaXRQYXJhbWV0ZXJzID0gZnVuY3Rpb24gKCkge1xyXG4gIEZETGF5b3V0LnByb3RvdHlwZS5pbml0UGFyYW1ldGVycy5jYWxsKHRoaXMsIGFyZ3VtZW50cyk7XHJcbiAgaWYgKCF0aGlzLmlzU3ViTGF5b3V0KSB7XHJcbiAgICBpZiAobGF5b3V0T3B0aW9uc1BhY2suaWRlYWxFZGdlTGVuZ3RoIDwgMTApXHJcbiAgICB7XHJcbiAgICAgIHRoaXMuaWRlYWxFZGdlTGVuZ3RoID0gMTA7XHJcbiAgICB9XHJcbiAgICBlbHNlXHJcbiAgICB7XHJcbiAgICAgIHRoaXMuaWRlYWxFZGdlTGVuZ3RoID0gbGF5b3V0T3B0aW9uc1BhY2suaWRlYWxFZGdlTGVuZ3RoO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMudXNlU21hcnRJZGVhbEVkZ2VMZW5ndGhDYWxjdWxhdGlvbiA9XHJcbiAgICAgICAgICAgIGxheW91dE9wdGlvbnNQYWNrLnNtYXJ0RWRnZUxlbmd0aENhbGM7XHJcbiAgICB0aGlzLnNwcmluZ0NvbnN0YW50ID1cclxuICAgICAgICAgICAgTGF5b3V0LnRyYW5zZm9ybShsYXlvdXRPcHRpb25zUGFjay5zcHJpbmdTdHJlbmd0aCxcclxuICAgICAgICAgICAgICAgICAgICBGRExheW91dENvbnN0YW50cy5ERUZBVUxUX1NQUklOR19TVFJFTkdUSCwgNS4wLCA1LjApO1xyXG4gICAgdGhpcy5yZXB1bHNpb25Db25zdGFudCA9XHJcbiAgICAgICAgICAgIExheW91dC50cmFuc2Zvcm0obGF5b3V0T3B0aW9uc1BhY2sucmVwdWxzaW9uU3RyZW5ndGgsXHJcbiAgICAgICAgICAgICAgICAgICAgRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9SRVBVTFNJT05fU1RSRU5HVEgsIDUuMCwgNS4wKTtcclxuICAgIHRoaXMuZ3Jhdml0eUNvbnN0YW50ID1cclxuICAgICAgICAgICAgTGF5b3V0LnRyYW5zZm9ybShsYXlvdXRPcHRpb25zUGFjay5ncmF2aXR5U3RyZW5ndGgsXHJcbiAgICAgICAgICAgICAgICAgICAgRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9HUkFWSVRZX1NUUkVOR1RIKTtcclxuICAgIHRoaXMuY29tcG91bmRHcmF2aXR5Q29uc3RhbnQgPVxyXG4gICAgICAgICAgICBMYXlvdXQudHJhbnNmb3JtKGxheW91dE9wdGlvbnNQYWNrLmNvbXBvdW5kR3Jhdml0eVN0cmVuZ3RoLFxyXG4gICAgICAgICAgICAgICAgICAgIEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfQ09NUE9VTkRfR1JBVklUWV9TVFJFTkdUSCk7XHJcbiAgICB0aGlzLmdyYXZpdHlSYW5nZUZhY3RvciA9XHJcbiAgICAgICAgICAgIExheW91dC50cmFuc2Zvcm0obGF5b3V0T3B0aW9uc1BhY2suZ3Jhdml0eVJhbmdlLFxyXG4gICAgICAgICAgICAgICAgICAgIEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfR1JBVklUWV9SQU5HRV9GQUNUT1IpO1xyXG4gICAgdGhpcy5jb21wb3VuZEdyYXZpdHlSYW5nZUZhY3RvciA9XHJcbiAgICAgICAgICAgIExheW91dC50cmFuc2Zvcm0obGF5b3V0T3B0aW9uc1BhY2suY29tcG91bmRHcmF2aXR5UmFuZ2UsXHJcbiAgICAgICAgICAgICAgICAgICAgRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9DT01QT1VORF9HUkFWSVRZX1JBTkdFX0ZBQ1RPUik7XHJcbiAgfVxyXG59O1xyXG5cclxuQ29TRUxheW91dC5wcm90b3R5cGUubGF5b3V0ID0gZnVuY3Rpb24gKCkge1xyXG4gIHZhciBjcmVhdGVCZW5kc0FzTmVlZGVkID0gbGF5b3V0T3B0aW9uc1BhY2suY3JlYXRlQmVuZHNBc05lZWRlZDtcclxuICBpZiAoY3JlYXRlQmVuZHNBc05lZWRlZClcclxuICB7XHJcbiAgICB0aGlzLmNyZWF0ZUJlbmRwb2ludHMoKTtcclxuICAgIHRoaXMuZ3JhcGhNYW5hZ2VyLnJlc2V0QWxsRWRnZXMoKTtcclxuICB9XHJcblxyXG4gIHRoaXMubGV2ZWwgPSAwO1xyXG4gIHJldHVybiB0aGlzLmNsYXNzaWNMYXlvdXQoKTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLmNsYXNzaWNMYXlvdXQgPSBmdW5jdGlvbiAoKSB7XHJcbiAgdGhpcy5jYWxjdWxhdGVOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvblRvKCk7XHJcbiAgdGhpcy5ncmFwaE1hbmFnZXIuY2FsY0xvd2VzdENvbW1vbkFuY2VzdG9ycygpO1xyXG4gIHRoaXMuZ3JhcGhNYW5hZ2VyLmNhbGNJbmNsdXNpb25UcmVlRGVwdGhzKCk7XHJcbiAgdGhpcy5ncmFwaE1hbmFnZXIuZ2V0Um9vdCgpLmNhbGNFc3RpbWF0ZWRTaXplKCk7XHJcbiAgdGhpcy5jYWxjSWRlYWxFZGdlTGVuZ3RocygpO1xyXG4gIGlmICghdGhpcy5pbmNyZW1lbnRhbClcclxuICB7XHJcbiAgICB2YXIgZm9yZXN0ID0gdGhpcy5nZXRGbGF0Rm9yZXN0KCk7XHJcblxyXG4gICAgLy8gVGhlIGdyYXBoIGFzc29jaWF0ZWQgd2l0aCB0aGlzIGxheW91dCBpcyBmbGF0IGFuZCBhIGZvcmVzdFxyXG4gICAgaWYgKGZvcmVzdC5sZW5ndGggPiAwKVxyXG5cclxuICAgIHtcclxuICAgICAgdGhpcy5wb3NpdGlvbk5vZGVzUmFkaWFsbHkoZm9yZXN0KTtcclxuICAgIH1cclxuICAgIC8vIFRoZSBncmFwaCBhc3NvY2lhdGVkIHdpdGggdGhpcyBsYXlvdXQgaXMgbm90IGZsYXQgb3IgYSBmb3Jlc3RcclxuICAgIGVsc2VcclxuICAgIHtcclxuICAgICAgdGhpcy5wb3NpdGlvbk5vZGVzUmFuZG9tbHkoKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHRoaXMuaW5pdFNwcmluZ0VtYmVkZGVyKCk7XHJcbiAgdGhpcy5ydW5TcHJpbmdFbWJlZGRlcigpO1xyXG5cclxuICBjb25zb2xlLmxvZyhcIkNsYXNzaWMgQ29TRSBsYXlvdXQgZmluaXNoZWQgYWZ0ZXIgXCIgK1xyXG4gICAgICAgICAgdGhpcy50b3RhbEl0ZXJhdGlvbnMgKyBcIiBpdGVyYXRpb25zXCIpO1xyXG5cclxuICByZXR1cm4gdHJ1ZTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLnJ1blNwcmluZ0VtYmVkZGVyID0gZnVuY3Rpb24gKCkge1xyXG4gIHZhciBsYXN0RnJhbWUgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuICB2YXIgaW5pdGlhbEFuaW1hdGlvblBlcmlvZCA9IDI1O1xyXG4gIHZhciBhbmltYXRpb25QZXJpb2QgPSBpbml0aWFsQW5pbWF0aW9uUGVyaW9kO1xyXG4gIGRvXHJcbiAge1xyXG4gICAgdGhpcy50b3RhbEl0ZXJhdGlvbnMrKztcclxuXHJcbiAgICBpZiAodGhpcy50b3RhbEl0ZXJhdGlvbnMgJSBGRExheW91dENvbnN0YW50cy5DT05WRVJHRU5DRV9DSEVDS19QRVJJT0QgPT0gMClcclxuICAgIHtcclxuICAgICAgaWYgKHRoaXMuaXNDb252ZXJnZWQoKSlcclxuICAgICAge1xyXG4gICAgICAgIGJyZWFrO1xyXG4gICAgICB9XHJcblxyXG4gICAgICB0aGlzLmNvb2xpbmdGYWN0b3IgPSB0aGlzLmluaXRpYWxDb29saW5nRmFjdG9yICpcclxuICAgICAgICAgICAgICAoKHRoaXMubWF4SXRlcmF0aW9ucyAtIHRoaXMudG90YWxJdGVyYXRpb25zKSAvIHRoaXMubWF4SXRlcmF0aW9ucyk7XHJcbiAgICAgIGFuaW1hdGlvblBlcmlvZCA9IE1hdGguY2VpbChpbml0aWFsQW5pbWF0aW9uUGVyaW9kICogTWF0aC5zcXJ0KHRoaXMuY29vbGluZ0ZhY3RvcikpO1xyXG5cclxuICAgIH1cclxuICAgIHRoaXMudG90YWxEaXNwbGFjZW1lbnQgPSAwO1xyXG4gICAgdGhpcy5ncmFwaE1hbmFnZXIudXBkYXRlQm91bmRzKCk7XHJcbiAgICB0aGlzLmNhbGNTcHJpbmdGb3JjZXMoKTtcclxuICAgIHRoaXMuY2FsY1JlcHVsc2lvbkZvcmNlcygpO1xyXG4gICAgdGhpcy5jYWxjR3Jhdml0YXRpb25hbEZvcmNlcygpO1xyXG4gICAgdGhpcy5tb3ZlTm9kZXMoKTtcclxuICAgIHRoaXMuYW5pbWF0ZSgpO1xyXG4gICAgaWYgKGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGUgJiYgdGhpcy50b3RhbEl0ZXJhdGlvbnMgJSBhbmltYXRpb25QZXJpb2QgPT0gMCkge1xyXG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IDFlNzsgaSsrKSB7XHJcbiAgICAgICAgaWYgKChuZXcgRGF0ZSgpLmdldFRpbWUoKSAtIGxhc3RGcmFtZSkgPiAyNSkge1xyXG4gICAgICAgICAgYnJlYWs7XHJcbiAgICAgICAgfVxyXG4gICAgICB9XHJcbiAgICAgIGxhc3RGcmFtZSA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG4gICAgICB2YXIgYWxsTm9kZXMgPSB0aGlzLmdyYXBoTWFuYWdlci5nZXRBbGxOb2RlcygpO1xyXG4gICAgICB2YXIgcERhdGEgPSB7fTtcclxuICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBhbGxOb2Rlcy5sZW5ndGg7IGkrKykge1xyXG4gICAgICAgIHZhciByZWN0ID0gYWxsTm9kZXNbaV0ucmVjdDtcclxuICAgICAgICB2YXIgaWQgPSBhbGxOb2Rlc1tpXS5pZDtcclxuICAgICAgICBwRGF0YVtpZF0gPSB7XHJcbiAgICAgICAgICBpZDogaWQsXHJcbiAgICAgICAgICB4OiByZWN0LmdldENlbnRlclgoKSxcclxuICAgICAgICAgIHk6IHJlY3QuZ2V0Q2VudGVyWSgpLFxyXG4gICAgICAgICAgdzogcmVjdC53aWR0aCxcclxuICAgICAgICAgIGg6IHJlY3QuaGVpZ2h0XHJcbiAgICAgICAgfTtcclxuICAgICAgfVxyXG4gICAgICBicm9hZGNhc3Qoe3BEYXRhOiBwRGF0YX0pO1xyXG4gICAgfVxyXG4gIH1cclxuICB3aGlsZSAodGhpcy50b3RhbEl0ZXJhdGlvbnMgPCB0aGlzLm1heEl0ZXJhdGlvbnMpO1xyXG5cclxuICB0aGlzLmdyYXBoTWFuYWdlci51cGRhdGVCb3VuZHMoKTtcclxufTtcclxuXHJcbkNvU0VMYXlvdXQucHJvdG90eXBlLmNhbGN1bGF0ZU5vZGVzVG9BcHBseUdyYXZpdGF0aW9uVG8gPSBmdW5jdGlvbiAoKSB7XHJcbiAgdmFyIG5vZGVMaXN0ID0gW107XHJcbiAgdmFyIGdyYXBoO1xyXG5cclxuICB2YXIgZ3JhcGhzID0gdGhpcy5ncmFwaE1hbmFnZXIuZ2V0R3JhcGhzKCk7XHJcbiAgdmFyIHNpemUgPSBncmFwaHMubGVuZ3RoO1xyXG4gIHZhciBpO1xyXG4gIGZvciAoaSA9IDA7IGkgPCBzaXplOyBpKyspXHJcbiAge1xyXG4gICAgZ3JhcGggPSBncmFwaHNbaV07XHJcblxyXG4gICAgZ3JhcGgudXBkYXRlQ29ubmVjdGVkKCk7XHJcblxyXG4gICAgaWYgKCFncmFwaC5pc0Nvbm5lY3RlZClcclxuICAgIHtcclxuICAgICAgbm9kZUxpc3QgPSBub2RlTGlzdC5jb25jYXQoZ3JhcGguZ2V0Tm9kZXMoKSk7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICB0aGlzLmdyYXBoTWFuYWdlci5zZXRBbGxOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvbihub2RlTGlzdCk7XHJcbn07XHJcblxyXG5Db1NFTGF5b3V0LnByb3RvdHlwZS5jcmVhdGVCZW5kcG9pbnRzID0gZnVuY3Rpb24gKCkge1xyXG4gIHZhciBlZGdlcyA9IFtdO1xyXG4gIGVkZ2VzID0gZWRnZXMuY29uY2F0KHRoaXMuZ3JhcGhNYW5hZ2VyLmdldEFsbEVkZ2VzKCkpO1xyXG4gIHZhciB2aXNpdGVkID0gbmV3IEhhc2hTZXQoKTtcclxuICB2YXIgaTtcclxuICBmb3IgKGkgPSAwOyBpIDwgZWRnZXMubGVuZ3RoOyBpKyspXHJcbiAge1xyXG4gICAgdmFyIGVkZ2UgPSBlZGdlc1tpXTtcclxuXHJcbiAgICBpZiAoIXZpc2l0ZWQuY29udGFpbnMoZWRnZSkpXHJcbiAgICB7XHJcbiAgICAgIHZhciBzb3VyY2UgPSBlZGdlLmdldFNvdXJjZSgpO1xyXG4gICAgICB2YXIgdGFyZ2V0ID0gZWRnZS5nZXRUYXJnZXQoKTtcclxuXHJcbiAgICAgIGlmIChzb3VyY2UgPT0gdGFyZ2V0KVxyXG4gICAgICB7XHJcbiAgICAgICAgZWRnZS5nZXRCZW5kcG9pbnRzKCkucHVzaChuZXcgUG9pbnREKCkpO1xyXG4gICAgICAgIGVkZ2UuZ2V0QmVuZHBvaW50cygpLnB1c2gobmV3IFBvaW50RCgpKTtcclxuICAgICAgICB0aGlzLmNyZWF0ZUR1bW15Tm9kZXNGb3JCZW5kcG9pbnRzKGVkZ2UpO1xyXG4gICAgICAgIHZpc2l0ZWQuYWRkKGVkZ2UpO1xyXG4gICAgICB9XHJcbiAgICAgIGVsc2VcclxuICAgICAge1xyXG4gICAgICAgIHZhciBlZGdlTGlzdCA9IFtdO1xyXG5cclxuICAgICAgICBlZGdlTGlzdCA9IGVkZ2VMaXN0LmNvbmNhdChzb3VyY2UuZ2V0RWRnZUxpc3RUb05vZGUodGFyZ2V0KSk7XHJcbiAgICAgICAgZWRnZUxpc3QgPSBlZGdlTGlzdC5jb25jYXQodGFyZ2V0LmdldEVkZ2VMaXN0VG9Ob2RlKHNvdXJjZSkpO1xyXG5cclxuICAgICAgICBpZiAoIXZpc2l0ZWQuY29udGFpbnMoZWRnZUxpc3RbMF0pKVxyXG4gICAgICAgIHtcclxuICAgICAgICAgIGlmIChlZGdlTGlzdC5sZW5ndGggPiAxKVxyXG4gICAgICAgICAge1xyXG4gICAgICAgICAgICB2YXIgaztcclxuICAgICAgICAgICAgZm9yIChrID0gMDsgayA8IGVkZ2VMaXN0Lmxlbmd0aDsgaysrKVxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgdmFyIG11bHRpRWRnZSA9IGVkZ2VMaXN0W2tdO1xyXG4gICAgICAgICAgICAgIG11bHRpRWRnZS5nZXRCZW5kcG9pbnRzKCkucHVzaChuZXcgUG9pbnREKCkpO1xyXG4gICAgICAgICAgICAgIHRoaXMuY3JlYXRlRHVtbXlOb2Rlc0ZvckJlbmRwb2ludHMobXVsdGlFZGdlKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgfVxyXG4gICAgICAgICAgdmlzaXRlZC5hZGRBbGwobGlzdCk7XHJcbiAgICAgICAgfVxyXG4gICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKHZpc2l0ZWQuc2l6ZSgpID09IGVkZ2VzLmxlbmd0aClcclxuICAgIHtcclxuICAgICAgYnJlYWs7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuQ29TRUxheW91dC5wcm90b3R5cGUucG9zaXRpb25Ob2Rlc1JhZGlhbGx5ID0gZnVuY3Rpb24gKGZvcmVzdCkge1xyXG4gIC8vIFdlIHRpbGUgdGhlIHRyZWVzIHRvIGEgZ3JpZCByb3cgYnkgcm93OyBmaXJzdCB0cmVlIHN0YXJ0cyBhdCAoMCwwKVxyXG4gIHZhciBjdXJyZW50U3RhcnRpbmdQb2ludCA9IG5ldyBQb2ludCgwLCAwKTtcclxuICB2YXIgbnVtYmVyT2ZDb2x1bW5zID0gTWF0aC5jZWlsKE1hdGguc3FydChmb3Jlc3QubGVuZ3RoKSk7XHJcbiAgdmFyIGhlaWdodCA9IDA7XHJcbiAgdmFyIGN1cnJlbnRZID0gMDtcclxuICB2YXIgY3VycmVudFggPSAwO1xyXG4gIHZhciBwb2ludCA9IG5ldyBQb2ludEQoMCwgMCk7XHJcblxyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgZm9yZXN0Lmxlbmd0aDsgaSsrKVxyXG4gIHtcclxuICAgIGlmIChpICUgbnVtYmVyT2ZDb2x1bW5zID09IDApXHJcbiAgICB7XHJcbiAgICAgIC8vIFN0YXJ0IG9mIGEgbmV3IHJvdywgbWFrZSB0aGUgeCBjb29yZGluYXRlIDAsIGluY3JlbWVudCB0aGVcclxuICAgICAgLy8geSBjb29yZGluYXRlIHdpdGggdGhlIG1heCBoZWlnaHQgb2YgdGhlIHByZXZpb3VzIHJvd1xyXG4gICAgICBjdXJyZW50WCA9IDA7XHJcbiAgICAgIGN1cnJlbnRZID0gaGVpZ2h0O1xyXG5cclxuICAgICAgaWYgKGkgIT0gMClcclxuICAgICAge1xyXG4gICAgICAgIGN1cnJlbnRZICs9IENvU0VDb25zdGFudHMuREVGQVVMVF9DT01QT05FTlRfU0VQRVJBVElPTjtcclxuICAgICAgfVxyXG5cclxuICAgICAgaGVpZ2h0ID0gMDtcclxuICAgIH1cclxuXHJcbiAgICB2YXIgdHJlZSA9IGZvcmVzdFtpXTtcclxuXHJcbiAgICAvLyBGaW5kIHRoZSBjZW50ZXIgb2YgdGhlIHRyZWVcclxuICAgIHZhciBjZW50ZXJOb2RlID0gTGF5b3V0LmZpbmRDZW50ZXJPZlRyZWUodHJlZSk7XHJcblxyXG4gICAgLy8gU2V0IHRoZSBzdGFyaW5nIHBvaW50IG9mIHRoZSBuZXh0IHRyZWVcclxuICAgIGN1cnJlbnRTdGFydGluZ1BvaW50LnggPSBjdXJyZW50WDtcclxuICAgIGN1cnJlbnRTdGFydGluZ1BvaW50LnkgPSBjdXJyZW50WTtcclxuXHJcbiAgICAvLyBEbyBhIHJhZGlhbCBsYXlvdXQgc3RhcnRpbmcgd2l0aCB0aGUgY2VudGVyXHJcbiAgICBwb2ludCA9XHJcbiAgICAgICAgICAgIENvU0VMYXlvdXQucmFkaWFsTGF5b3V0KHRyZWUsIGNlbnRlck5vZGUsIGN1cnJlbnRTdGFydGluZ1BvaW50KTtcclxuXHJcbiAgICBpZiAocG9pbnQueSA+IGhlaWdodClcclxuICAgIHtcclxuICAgICAgaGVpZ2h0ID0gTWF0aC5mbG9vcihwb2ludC55KTtcclxuICAgIH1cclxuXHJcbiAgICBjdXJyZW50WCA9IE1hdGguZmxvb3IocG9pbnQueCArIENvU0VDb25zdGFudHMuREVGQVVMVF9DT01QT05FTlRfU0VQRVJBVElPTik7XHJcbiAgfVxyXG5cclxuICB0aGlzLnRyYW5zZm9ybShcclxuICAgICAgICAgIG5ldyBQb2ludEQoTGF5b3V0Q29uc3RhbnRzLldPUkxEX0NFTlRFUl9YIC0gcG9pbnQueCAvIDIsXHJcbiAgICAgICAgICAgICAgICAgIExheW91dENvbnN0YW50cy5XT1JMRF9DRU5URVJfWSAtIHBvaW50LnkgLyAyKSk7XHJcbn07XHJcblxyXG5Db1NFTGF5b3V0LnJhZGlhbExheW91dCA9IGZ1bmN0aW9uICh0cmVlLCBjZW50ZXJOb2RlLCBzdGFydGluZ1BvaW50KSB7XHJcbiAgdmFyIHJhZGlhbFNlcCA9IE1hdGgubWF4KHRoaXMubWF4RGlhZ29uYWxJblRyZWUodHJlZSksXHJcbiAgICAgICAgICBDb1NFQ29uc3RhbnRzLkRFRkFVTFRfUkFESUFMX1NFUEFSQVRJT04pO1xyXG4gIENvU0VMYXlvdXQuYnJhbmNoUmFkaWFsTGF5b3V0KGNlbnRlck5vZGUsIG51bGwsIDAsIDM1OSwgMCwgcmFkaWFsU2VwKTtcclxuICB2YXIgYm91bmRzID0gTEdyYXBoLmNhbGN1bGF0ZUJvdW5kcyh0cmVlKTtcclxuXHJcbiAgdmFyIHRyYW5zZm9ybSA9IG5ldyBUcmFuc2Zvcm0oKTtcclxuICB0cmFuc2Zvcm0uc2V0RGV2aWNlT3JnWChib3VuZHMuZ2V0TWluWCgpKTtcclxuICB0cmFuc2Zvcm0uc2V0RGV2aWNlT3JnWShib3VuZHMuZ2V0TWluWSgpKTtcclxuICB0cmFuc2Zvcm0uc2V0V29ybGRPcmdYKHN0YXJ0aW5nUG9pbnQueCk7XHJcbiAgdHJhbnNmb3JtLnNldFdvcmxkT3JnWShzdGFydGluZ1BvaW50LnkpO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHRyZWUubGVuZ3RoOyBpKyspXHJcbiAge1xyXG4gICAgdmFyIG5vZGUgPSB0cmVlW2ldO1xyXG4gICAgbm9kZS50cmFuc2Zvcm0odHJhbnNmb3JtKTtcclxuICB9XHJcblxyXG4gIHZhciBib3R0b21SaWdodCA9XHJcbiAgICAgICAgICBuZXcgUG9pbnREKGJvdW5kcy5nZXRNYXhYKCksIGJvdW5kcy5nZXRNYXhZKCkpO1xyXG5cclxuICByZXR1cm4gdHJhbnNmb3JtLmludmVyc2VUcmFuc2Zvcm1Qb2ludChib3R0b21SaWdodCk7XHJcbn07XHJcblxyXG5Db1NFTGF5b3V0LmJyYW5jaFJhZGlhbExheW91dCA9IGZ1bmN0aW9uIChub2RlLCBwYXJlbnRPZk5vZGUsIHN0YXJ0QW5nbGUsIGVuZEFuZ2xlLCBkaXN0YW5jZSwgcmFkaWFsU2VwYXJhdGlvbikge1xyXG4gIC8vIEZpcnN0LCBwb3NpdGlvbiB0aGlzIG5vZGUgYnkgZmluZGluZyBpdHMgYW5nbGUuXHJcbiAgdmFyIGhhbGZJbnRlcnZhbCA9ICgoZW5kQW5nbGUgLSBzdGFydEFuZ2xlKSArIDEpIC8gMjtcclxuXHJcbiAgaWYgKGhhbGZJbnRlcnZhbCA8IDApXHJcbiAge1xyXG4gICAgaGFsZkludGVydmFsICs9IDE4MDtcclxuICB9XHJcblxyXG4gIHZhciBub2RlQW5nbGUgPSAoaGFsZkludGVydmFsICsgc3RhcnRBbmdsZSkgJSAzNjA7XHJcbiAgdmFyIHRldGEgPSAobm9kZUFuZ2xlICogSUdlb21ldHJ5LlRXT19QSSkgLyAzNjA7XHJcblxyXG4gIC8vIE1ha2UgcG9sYXIgdG8gamF2YSBjb3JkaW5hdGUgY29udmVyc2lvbi5cclxuICB2YXIgY29zX3RldGEgPSBNYXRoLmNvcyh0ZXRhKTtcclxuICB2YXIgeF8gPSBkaXN0YW5jZSAqIE1hdGguY29zKHRldGEpO1xyXG4gIHZhciB5XyA9IGRpc3RhbmNlICogTWF0aC5zaW4odGV0YSk7XHJcblxyXG4gIG5vZGUuc2V0Q2VudGVyKHhfLCB5Xyk7XHJcblxyXG4gIC8vIFRyYXZlcnNlIGFsbCBuZWlnaGJvcnMgb2YgdGhpcyBub2RlIGFuZCByZWN1cnNpdmVseSBjYWxsIHRoaXNcclxuICAvLyBmdW5jdGlvbi5cclxuICB2YXIgbmVpZ2hib3JFZGdlcyA9IFtdO1xyXG4gIG5laWdoYm9yRWRnZXMgPSBuZWlnaGJvckVkZ2VzLmNvbmNhdChub2RlLmdldEVkZ2VzKCkpO1xyXG4gIHZhciBjaGlsZENvdW50ID0gbmVpZ2hib3JFZGdlcy5sZW5ndGg7XHJcblxyXG4gIGlmIChwYXJlbnRPZk5vZGUgIT0gbnVsbClcclxuICB7XHJcbiAgICBjaGlsZENvdW50LS07XHJcbiAgfVxyXG5cclxuICB2YXIgYnJhbmNoQ291bnQgPSAwO1xyXG5cclxuICB2YXIgaW5jRWRnZXNDb3VudCA9IG5laWdoYm9yRWRnZXMubGVuZ3RoO1xyXG4gIHZhciBzdGFydEluZGV4O1xyXG5cclxuICB2YXIgZWRnZXMgPSBub2RlLmdldEVkZ2VzQmV0d2VlbihwYXJlbnRPZk5vZGUpO1xyXG5cclxuICAvLyBJZiB0aGVyZSBhcmUgbXVsdGlwbGUgZWRnZXMsIHBydW5lIHRoZW0gdW50aWwgdGhlcmUgcmVtYWlucyBvbmx5IG9uZVxyXG4gIC8vIGVkZ2UuXHJcbiAgd2hpbGUgKGVkZ2VzLmxlbmd0aCA+IDEpXHJcbiAge1xyXG4gICAgLy9uZWlnaGJvckVkZ2VzLnJlbW92ZShlZGdlcy5yZW1vdmUoMCkpO1xyXG4gICAgdmFyIHRlbXAgPSBlZGdlc1swXTtcclxuICAgIGVkZ2VzLnNwbGljZSgwLCAxKTtcclxuICAgIHZhciBpbmRleCA9IG5laWdoYm9yRWRnZXMuaW5kZXhPZih0ZW1wKTtcclxuICAgIGlmIChpbmRleCA+PSAwKSB7XHJcbiAgICAgIG5laWdoYm9yRWRnZXMuc3BsaWNlKGluZGV4LCAxKTtcclxuICAgIH1cclxuICAgIGluY0VkZ2VzQ291bnQtLTtcclxuICAgIGNoaWxkQ291bnQtLTtcclxuICB9XHJcblxyXG4gIGlmIChwYXJlbnRPZk5vZGUgIT0gbnVsbClcclxuICB7XHJcbiAgICAvL2Fzc2VydCBlZGdlcy5sZW5ndGggPT0gMTtcclxuICAgIHN0YXJ0SW5kZXggPSAobmVpZ2hib3JFZGdlcy5pbmRleE9mKGVkZ2VzWzBdKSArIDEpICUgaW5jRWRnZXNDb3VudDtcclxuICB9XHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIHN0YXJ0SW5kZXggPSAwO1xyXG4gIH1cclxuXHJcbiAgdmFyIHN0ZXBBbmdsZSA9IE1hdGguYWJzKGVuZEFuZ2xlIC0gc3RhcnRBbmdsZSkgLyBjaGlsZENvdW50O1xyXG5cclxuICBmb3IgKHZhciBpID0gc3RhcnRJbmRleDtcclxuICAgICAgICAgIGJyYW5jaENvdW50ICE9IGNoaWxkQ291bnQ7XHJcbiAgICAgICAgICBpID0gKCsraSkgJSBpbmNFZGdlc0NvdW50KVxyXG4gIHtcclxuICAgIHZhciBjdXJyZW50TmVpZ2hib3IgPVxyXG4gICAgICAgICAgICBuZWlnaGJvckVkZ2VzW2ldLmdldE90aGVyRW5kKG5vZGUpO1xyXG5cclxuICAgIC8vIERvbid0IGJhY2sgdHJhdmVyc2UgdG8gcm9vdCBub2RlIGluIGN1cnJlbnQgdHJlZS5cclxuICAgIGlmIChjdXJyZW50TmVpZ2hib3IgPT0gcGFyZW50T2ZOb2RlKVxyXG4gICAge1xyXG4gICAgICBjb250aW51ZTtcclxuICAgIH1cclxuXHJcbiAgICB2YXIgY2hpbGRTdGFydEFuZ2xlID1cclxuICAgICAgICAgICAgKHN0YXJ0QW5nbGUgKyBicmFuY2hDb3VudCAqIHN0ZXBBbmdsZSkgJSAzNjA7XHJcbiAgICB2YXIgY2hpbGRFbmRBbmdsZSA9IChjaGlsZFN0YXJ0QW5nbGUgKyBzdGVwQW5nbGUpICUgMzYwO1xyXG5cclxuICAgIENvU0VMYXlvdXQuYnJhbmNoUmFkaWFsTGF5b3V0KGN1cnJlbnROZWlnaGJvcixcclxuICAgICAgICAgICAgbm9kZSxcclxuICAgICAgICAgICAgY2hpbGRTdGFydEFuZ2xlLCBjaGlsZEVuZEFuZ2xlLFxyXG4gICAgICAgICAgICBkaXN0YW5jZSArIHJhZGlhbFNlcGFyYXRpb24sIHJhZGlhbFNlcGFyYXRpb24pO1xyXG5cclxuICAgIGJyYW5jaENvdW50Kys7XHJcbiAgfVxyXG59O1xyXG5cclxuQ29TRUxheW91dC5tYXhEaWFnb25hbEluVHJlZSA9IGZ1bmN0aW9uICh0cmVlKSB7XHJcbiAgdmFyIG1heERpYWdvbmFsID0gSW50ZWdlci5NSU5fVkFMVUU7XHJcblxyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgdHJlZS5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbm9kZSA9IHRyZWVbaV07XHJcbiAgICB2YXIgZGlhZ29uYWwgPSBub2RlLmdldERpYWdvbmFsKCk7XHJcblxyXG4gICAgaWYgKGRpYWdvbmFsID4gbWF4RGlhZ29uYWwpXHJcbiAgICB7XHJcbiAgICAgIG1heERpYWdvbmFsID0gZGlhZ29uYWw7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICByZXR1cm4gbWF4RGlhZ29uYWw7XHJcbn07XHJcblxyXG5Db1NFTGF5b3V0LnByb3RvdHlwZS5jYWxjUmVwdWxzaW9uUmFuZ2UgPSBmdW5jdGlvbiAoKSB7XHJcbiAgLy8gZm9ybXVsYSBpcyAyIHggKGxldmVsICsgMSkgeCBpZGVhbEVkZ2VMZW5ndGhcclxuICByZXR1cm4gKDIgKiAodGhpcy5sZXZlbCArIDEpICogdGhpcy5pZGVhbEVkZ2VMZW5ndGgpO1xyXG59O1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBDb1NFTGF5b3V0O1xyXG4iLCJ2YXIgRkRMYXlvdXROb2RlID0gcmVxdWlyZSgnLi9GRExheW91dE5vZGUnKTtcclxuXHJcbmZ1bmN0aW9uIENvU0VOb2RlKGdtLCBsb2MsIHNpemUsIHZOb2RlKSB7XHJcbiAgRkRMYXlvdXROb2RlLmNhbGwodGhpcywgZ20sIGxvYywgc2l6ZSwgdk5vZGUpO1xyXG59XHJcblxyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShGRExheW91dE5vZGUucHJvdG90eXBlKTtcclxuZm9yICh2YXIgcHJvcCBpbiBGRExheW91dE5vZGUpIHtcclxuICBDb1NFTm9kZVtwcm9wXSA9IEZETGF5b3V0Tm9kZVtwcm9wXTtcclxufVxyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlLm1vdmUgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdmFyIGxheW91dCA9IHRoaXMuZ3JhcGhNYW5hZ2VyLmdldExheW91dCgpO1xyXG4gIHRoaXMuZGlzcGxhY2VtZW50WCA9IGxheW91dC5jb29saW5nRmFjdG9yICpcclxuICAgICAgICAgICh0aGlzLnNwcmluZ0ZvcmNlWCArIHRoaXMucmVwdWxzaW9uRm9yY2VYICsgdGhpcy5ncmF2aXRhdGlvbkZvcmNlWCk7XHJcbiAgdGhpcy5kaXNwbGFjZW1lbnRZID0gbGF5b3V0LmNvb2xpbmdGYWN0b3IgKlxyXG4gICAgICAgICAgKHRoaXMuc3ByaW5nRm9yY2VZICsgdGhpcy5yZXB1bHNpb25Gb3JjZVkgKyB0aGlzLmdyYXZpdGF0aW9uRm9yY2VZKTtcclxuXHJcblxyXG4gIGlmIChNYXRoLmFicyh0aGlzLmRpc3BsYWNlbWVudFgpID4gbGF5b3V0LmNvb2xpbmdGYWN0b3IgKiBsYXlvdXQubWF4Tm9kZURpc3BsYWNlbWVudClcclxuICB7XHJcbiAgICB0aGlzLmRpc3BsYWNlbWVudFggPSBsYXlvdXQuY29vbGluZ0ZhY3RvciAqIGxheW91dC5tYXhOb2RlRGlzcGxhY2VtZW50ICpcclxuICAgICAgICAgICAgSU1hdGguc2lnbih0aGlzLmRpc3BsYWNlbWVudFgpO1xyXG4gIH1cclxuXHJcbiAgaWYgKE1hdGguYWJzKHRoaXMuZGlzcGxhY2VtZW50WSkgPiBsYXlvdXQuY29vbGluZ0ZhY3RvciAqIGxheW91dC5tYXhOb2RlRGlzcGxhY2VtZW50KVxyXG4gIHtcclxuICAgIHRoaXMuZGlzcGxhY2VtZW50WSA9IGxheW91dC5jb29saW5nRmFjdG9yICogbGF5b3V0Lm1heE5vZGVEaXNwbGFjZW1lbnQgKlxyXG4gICAgICAgICAgICBJTWF0aC5zaWduKHRoaXMuZGlzcGxhY2VtZW50WSk7XHJcbiAgfVxyXG5cclxuICAvLyBhIHNpbXBsZSBub2RlLCBqdXN0IG1vdmUgaXRcclxuICBpZiAodGhpcy5jaGlsZCA9PSBudWxsKVxyXG4gIHtcclxuICAgIHRoaXMubW92ZUJ5KHRoaXMuZGlzcGxhY2VtZW50WCwgdGhpcy5kaXNwbGFjZW1lbnRZKTtcclxuICB9XHJcbiAgLy8gYW4gZW1wdHkgY29tcG91bmQgbm9kZSwgYWdhaW4ganVzdCBtb3ZlIGl0XHJcbiAgZWxzZSBpZiAodGhpcy5jaGlsZC5nZXROb2RlcygpLmxlbmd0aCA9PSAwKVxyXG4gIHtcclxuICAgIHRoaXMubW92ZUJ5KHRoaXMuZGlzcGxhY2VtZW50WCwgdGhpcy5kaXNwbGFjZW1lbnRZKTtcclxuICB9XHJcbiAgLy8gbm9uLWVtcHR5IGNvbXBvdW5kIG5vZGUsIHByb3BvZ2F0ZSBtb3ZlbWVudCB0byBjaGlsZHJlbiBhcyB3ZWxsXHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIHRoaXMucHJvcG9nYXRlRGlzcGxhY2VtZW50VG9DaGlsZHJlbih0aGlzLmRpc3BsYWNlbWVudFgsXHJcbiAgICAgICAgICAgIHRoaXMuZGlzcGxhY2VtZW50WSk7XHJcbiAgfVxyXG5cclxuICBsYXlvdXQudG90YWxEaXNwbGFjZW1lbnQgKz1cclxuICAgICAgICAgIE1hdGguYWJzKHRoaXMuZGlzcGxhY2VtZW50WCkgKyBNYXRoLmFicyh0aGlzLmRpc3BsYWNlbWVudFkpO1xyXG5cclxuICB0aGlzLnNwcmluZ0ZvcmNlWCA9IDA7XHJcbiAgdGhpcy5zcHJpbmdGb3JjZVkgPSAwO1xyXG4gIHRoaXMucmVwdWxzaW9uRm9yY2VYID0gMDtcclxuICB0aGlzLnJlcHVsc2lvbkZvcmNlWSA9IDA7XHJcbiAgdGhpcy5ncmF2aXRhdGlvbkZvcmNlWCA9IDA7XHJcbiAgdGhpcy5ncmF2aXRhdGlvbkZvcmNlWSA9IDA7XHJcbiAgdGhpcy5kaXNwbGFjZW1lbnRYID0gMDtcclxuICB0aGlzLmRpc3BsYWNlbWVudFkgPSAwO1xyXG59O1xyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlLnByb3BvZ2F0ZURpc3BsYWNlbWVudFRvQ2hpbGRyZW4gPSBmdW5jdGlvbiAoZFgsIGRZKVxyXG57XHJcbiAgdmFyIG5vZGVzID0gdGhpcy5nZXRDaGlsZCgpLmdldE5vZGVzKCk7XHJcbiAgdmFyIG5vZGU7XHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBub2Rlcy5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICBub2RlID0gbm9kZXNbaV07XHJcbiAgICBpZiAobm9kZS5nZXRDaGlsZCgpID09IG51bGwpXHJcbiAgICB7XHJcbiAgICAgIG5vZGUubW92ZUJ5KGRYLCBkWSk7XHJcbiAgICAgIG5vZGUuZGlzcGxhY2VtZW50WCArPSBkWDtcclxuICAgICAgbm9kZS5kaXNwbGFjZW1lbnRZICs9IGRZO1xyXG4gICAgfVxyXG4gICAgZWxzZVxyXG4gICAge1xyXG4gICAgICBub2RlLnByb3BvZ2F0ZURpc3BsYWNlbWVudFRvQ2hpbGRyZW4oZFgsIGRZKTtcclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG5Db1NFTm9kZS5wcm90b3R5cGUuc2V0UHJlZDEgPSBmdW5jdGlvbiAocHJlZDEpXHJcbntcclxuICB0aGlzLnByZWQxID0gcHJlZDE7XHJcbn07XHJcblxyXG5Db1NFTm9kZS5wcm90b3R5cGUuZ2V0UHJlZDEgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHByZWQxO1xyXG59O1xyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlLmdldFByZWQyID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiBwcmVkMjtcclxufTtcclxuXHJcbkNvU0VOb2RlLnByb3RvdHlwZS5zZXROZXh0ID0gZnVuY3Rpb24gKG5leHQpXHJcbntcclxuICB0aGlzLm5leHQgPSBuZXh0O1xyXG59O1xyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlLmdldE5leHQgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIG5leHQ7XHJcbn07XHJcblxyXG5Db1NFTm9kZS5wcm90b3R5cGUuc2V0UHJvY2Vzc2VkID0gZnVuY3Rpb24gKHByb2Nlc3NlZClcclxue1xyXG4gIHRoaXMucHJvY2Vzc2VkID0gcHJvY2Vzc2VkO1xyXG59O1xyXG5cclxuQ29TRU5vZGUucHJvdG90eXBlLmlzUHJvY2Vzc2VkID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiBwcm9jZXNzZWQ7XHJcbn07XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IENvU0VOb2RlO1xyXG4iLCJmdW5jdGlvbiBEaW1lbnNpb25EKHdpZHRoLCBoZWlnaHQpIHtcclxuICB0aGlzLndpZHRoID0gMDtcclxuICB0aGlzLmhlaWdodCA9IDA7XHJcbiAgaWYgKHdpZHRoICE9PSBudWxsICYmIGhlaWdodCAhPT0gbnVsbCkge1xyXG4gICAgdGhpcy5oZWlnaHQgPSBoZWlnaHQ7XHJcbiAgICB0aGlzLndpZHRoID0gd2lkdGg7XHJcbiAgfVxyXG59XHJcblxyXG5EaW1lbnNpb25ELnByb3RvdHlwZS5nZXRXaWR0aCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy53aWR0aDtcclxufTtcclxuXHJcbkRpbWVuc2lvbkQucHJvdG90eXBlLnNldFdpZHRoID0gZnVuY3Rpb24gKHdpZHRoKVxyXG57XHJcbiAgdGhpcy53aWR0aCA9IHdpZHRoO1xyXG59O1xyXG5cclxuRGltZW5zaW9uRC5wcm90b3R5cGUuZ2V0SGVpZ2h0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmhlaWdodDtcclxufTtcclxuXHJcbkRpbWVuc2lvbkQucHJvdG90eXBlLnNldEhlaWdodCA9IGZ1bmN0aW9uIChoZWlnaHQpXHJcbntcclxuICB0aGlzLmhlaWdodCA9IGhlaWdodDtcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gRGltZW5zaW9uRDtcclxuIiwidmFyIExheW91dCA9IHJlcXVpcmUoJy4vTGF5b3V0Jyk7XHJcbnZhciBGRExheW91dENvbnN0YW50cyA9IHJlcXVpcmUoJy4vRkRMYXlvdXRDb25zdGFudHMnKTtcclxuXHJcbmZ1bmN0aW9uIEZETGF5b3V0KCkge1xyXG4gIExheW91dC5jYWxsKHRoaXMpO1xyXG5cclxuICB0aGlzLnVzZVNtYXJ0SWRlYWxFZGdlTGVuZ3RoQ2FsY3VsYXRpb24gPSBGRExheW91dENvbnN0YW50cy5ERUZBVUxUX1VTRV9TTUFSVF9JREVBTF9FREdFX0xFTkdUSF9DQUxDVUxBVElPTjtcclxuICB0aGlzLmlkZWFsRWRnZUxlbmd0aCA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEg7XHJcbiAgdGhpcy5zcHJpbmdDb25zdGFudCA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfU1BSSU5HX1NUUkVOR1RIO1xyXG4gIHRoaXMucmVwdWxzaW9uQ29uc3RhbnQgPSBGRExheW91dENvbnN0YW50cy5ERUZBVUxUX1JFUFVMU0lPTl9TVFJFTkdUSDtcclxuICB0aGlzLmdyYXZpdHlDb25zdGFudCA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfR1JBVklUWV9TVFJFTkdUSDtcclxuICB0aGlzLmNvbXBvdW5kR3Jhdml0eUNvbnN0YW50ID0gRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9DT01QT1VORF9HUkFWSVRZX1NUUkVOR1RIO1xyXG4gIHRoaXMuZ3Jhdml0eVJhbmdlRmFjdG9yID0gRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9HUkFWSVRZX1JBTkdFX0ZBQ1RPUjtcclxuICB0aGlzLmNvbXBvdW5kR3Jhdml0eVJhbmdlRmFjdG9yID0gRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9DT01QT1VORF9HUkFWSVRZX1JBTkdFX0ZBQ1RPUjtcclxuICB0aGlzLmRpc3BsYWNlbWVudFRocmVzaG9sZFBlck5vZGUgPSAoMy4wICogRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9FREdFX0xFTkdUSCkgLyAxMDA7XHJcbiAgdGhpcy5jb29saW5nRmFjdG9yID0gMS4wO1xyXG4gIHRoaXMuaW5pdGlhbENvb2xpbmdGYWN0b3IgPSAxLjA7XHJcbiAgdGhpcy50b3RhbERpc3BsYWNlbWVudCA9IDAuMDtcclxuICB0aGlzLm9sZFRvdGFsRGlzcGxhY2VtZW50ID0gMC4wO1xyXG4gIHRoaXMubWF4SXRlcmF0aW9ucyA9IEZETGF5b3V0Q29uc3RhbnRzLk1BWF9JVEVSQVRJT05TO1xyXG59XHJcblxyXG5GRExheW91dC5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKExheW91dC5wcm90b3R5cGUpO1xyXG5cclxuZm9yICh2YXIgcHJvcCBpbiBMYXlvdXQpIHtcclxuICBGRExheW91dFtwcm9wXSA9IExheW91dFtwcm9wXTtcclxufVxyXG5cclxuRkRMYXlvdXQucHJvdG90eXBlLmluaXRQYXJhbWV0ZXJzID0gZnVuY3Rpb24gKCkge1xyXG4gIExheW91dC5wcm90b3R5cGUuaW5pdFBhcmFtZXRlcnMuY2FsbCh0aGlzLCBhcmd1bWVudHMpO1xyXG5cclxuICBpZiAodGhpcy5sYXlvdXRRdWFsaXR5ID09IExheW91dENvbnN0YW50cy5EUkFGVF9RVUFMSVRZKVxyXG4gIHtcclxuICAgIHRoaXMuZGlzcGxhY2VtZW50VGhyZXNob2xkUGVyTm9kZSArPSAwLjMwO1xyXG4gICAgdGhpcy5tYXhJdGVyYXRpb25zICo9IDAuODtcclxuICB9XHJcbiAgZWxzZSBpZiAodGhpcy5sYXlvdXRRdWFsaXR5ID09IExheW91dENvbnN0YW50cy5QUk9PRl9RVUFMSVRZKVxyXG4gIHtcclxuICAgIHRoaXMuZGlzcGxhY2VtZW50VGhyZXNob2xkUGVyTm9kZSAtPSAwLjMwO1xyXG4gICAgdGhpcy5tYXhJdGVyYXRpb25zICo9IDEuMjtcclxuICB9XHJcblxyXG4gIHRoaXMudG90YWxJdGVyYXRpb25zID0gMDtcclxuICB0aGlzLm5vdEFuaW1hdGVkSXRlcmF0aW9ucyA9IDA7XHJcblxyXG4vLyAgICB0aGlzLnVzZUZSR3JpZFZhcmlhbnQgPSBsYXlvdXRPcHRpb25zUGFjay5zbWFydFJlcHVsc2lvblJhbmdlQ2FsYztcclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5jYWxjSWRlYWxFZGdlTGVuZ3RocyA9IGZ1bmN0aW9uICgpIHtcclxuICB2YXIgZWRnZTtcclxuICB2YXIgbGNhRGVwdGg7XHJcbiAgdmFyIHNvdXJjZTtcclxuICB2YXIgdGFyZ2V0O1xyXG4gIHZhciBzaXplT2ZTb3VyY2VJbkxjYTtcclxuICB2YXIgc2l6ZU9mVGFyZ2V0SW5MY2E7XHJcblxyXG4gIHZhciBhbGxFZGdlcyA9IHRoaXMuZ2V0R3JhcGhNYW5hZ2VyKCkuZ2V0QWxsRWRnZXMoKTtcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGFsbEVkZ2VzLmxlbmd0aDsgaSsrKVxyXG4gIHtcclxuICAgIGVkZ2UgPSBhbGxFZGdlc1tpXTtcclxuXHJcbiAgICBlZGdlLmlkZWFsTGVuZ3RoID0gdGhpcy5pZGVhbEVkZ2VMZW5ndGg7XHJcblxyXG4gICAgaWYgKGVkZ2UuaXNJbnRlckdyYXBoKVxyXG4gICAge1xyXG4gICAgICBzb3VyY2UgPSBlZGdlLmdldFNvdXJjZSgpO1xyXG4gICAgICB0YXJnZXQgPSBlZGdlLmdldFRhcmdldCgpO1xyXG5cclxuICAgICAgc2l6ZU9mU291cmNlSW5MY2EgPSBlZGdlLmdldFNvdXJjZUluTGNhKCkuZ2V0RXN0aW1hdGVkU2l6ZSgpO1xyXG4gICAgICBzaXplT2ZUYXJnZXRJbkxjYSA9IGVkZ2UuZ2V0VGFyZ2V0SW5MY2EoKS5nZXRFc3RpbWF0ZWRTaXplKCk7XHJcblxyXG4gICAgICBpZiAodGhpcy51c2VTbWFydElkZWFsRWRnZUxlbmd0aENhbGN1bGF0aW9uKVxyXG4gICAgICB7XHJcbiAgICAgICAgZWRnZS5pZGVhbExlbmd0aCArPSBzaXplT2ZTb3VyY2VJbkxjYSArIHNpemVPZlRhcmdldEluTGNhIC1cclxuICAgICAgICAgICAgICAgIDIgKiBMYXlvdXRDb25zdGFudHMuU0lNUExFX05PREVfU0laRTtcclxuICAgICAgfVxyXG5cclxuICAgICAgbGNhRGVwdGggPSBlZGdlLmdldExjYSgpLmdldEluY2x1c2lvblRyZWVEZXB0aCgpO1xyXG5cclxuICAgICAgZWRnZS5pZGVhbExlbmd0aCArPSBGRExheW91dENvbnN0YW50cy5ERUZBVUxUX0VER0VfTEVOR1RIICpcclxuICAgICAgICAgICAgICBGRExheW91dENvbnN0YW50cy5QRVJfTEVWRUxfSURFQUxfRURHRV9MRU5HVEhfRkFDVE9SICpcclxuICAgICAgICAgICAgICAoc291cmNlLmdldEluY2x1c2lvblRyZWVEZXB0aCgpICtcclxuICAgICAgICAgICAgICAgICAgICAgIHRhcmdldC5nZXRJbmNsdXNpb25UcmVlRGVwdGgoKSAtIDIgKiBsY2FEZXB0aCk7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuRkRMYXlvdXQucHJvdG90eXBlLmluaXRTcHJpbmdFbWJlZGRlciA9IGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgaWYgKHRoaXMuaW5jcmVtZW50YWwpXHJcbiAge1xyXG4gICAgdGhpcy5jb29saW5nRmFjdG9yID0gMC44O1xyXG4gICAgdGhpcy5pbml0aWFsQ29vbGluZ0ZhY3RvciA9IDAuODtcclxuICAgIHRoaXMubWF4Tm9kZURpc3BsYWNlbWVudCA9XHJcbiAgICAgICAgICAgIEZETGF5b3V0Q29uc3RhbnRzLk1BWF9OT0RFX0RJU1BMQUNFTUVOVF9JTkNSRU1FTlRBTDtcclxuICB9XHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIHRoaXMuY29vbGluZ0ZhY3RvciA9IDEuMDtcclxuICAgIHRoaXMuaW5pdGlhbENvb2xpbmdGYWN0b3IgPSAxLjA7XHJcbiAgICB0aGlzLm1heE5vZGVEaXNwbGFjZW1lbnQgPVxyXG4gICAgICAgICAgICBGRExheW91dENvbnN0YW50cy5NQVhfTk9ERV9ESVNQTEFDRU1FTlQ7XHJcbiAgfVxyXG5cclxuICB0aGlzLm1heEl0ZXJhdGlvbnMgPVxyXG4gICAgICAgICAgTWF0aC5tYXgodGhpcy5nZXRBbGxOb2RlcygpLmxlbmd0aCAqIDUsIHRoaXMubWF4SXRlcmF0aW9ucyk7XHJcblxyXG4gIHRoaXMudG90YWxEaXNwbGFjZW1lbnRUaHJlc2hvbGQgPVxyXG4gICAgICAgICAgdGhpcy5kaXNwbGFjZW1lbnRUaHJlc2hvbGRQZXJOb2RlICogdGhpcy5nZXRBbGxOb2RlcygpLmxlbmd0aDtcclxuXHJcbiAgdGhpcy5yZXB1bHNpb25SYW5nZSA9IHRoaXMuY2FsY1JlcHVsc2lvblJhbmdlKCk7XHJcbn07XHJcblxyXG5GRExheW91dC5wcm90b3R5cGUuY2FsY1NwcmluZ0ZvcmNlcyA9IGZ1bmN0aW9uICgpIHtcclxuICB2YXIgbEVkZ2VzID0gdGhpcy5nZXRBbGxFZGdlcygpO1xyXG4gIHZhciBlZGdlO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGxFZGdlcy5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICBlZGdlID0gbEVkZ2VzW2ldO1xyXG5cclxuICAgIHRoaXMuY2FsY1NwcmluZ0ZvcmNlKGVkZ2UsIGVkZ2UuaWRlYWxMZW5ndGgpO1xyXG4gIH1cclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5jYWxjUmVwdWxzaW9uRm9yY2VzID0gZnVuY3Rpb24gKCkge1xyXG4gIHZhciBpLCBqO1xyXG4gIHZhciBub2RlQSwgbm9kZUI7XHJcbiAgdmFyIGxOb2RlcyA9IHRoaXMuZ2V0QWxsTm9kZXMoKTtcclxuXHJcbiAgZm9yIChpID0gMDsgaSA8IGxOb2Rlcy5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICBub2RlQSA9IGxOb2Rlc1tpXTtcclxuXHJcbiAgICBmb3IgKGogPSBpICsgMTsgaiA8IGxOb2Rlcy5sZW5ndGg7IGorKylcclxuICAgIHtcclxuICAgICAgbm9kZUIgPSBsTm9kZXNbal07XHJcblxyXG4gICAgICAvLyBJZiBib3RoIG5vZGVzIGFyZSBub3QgbWVtYmVycyBvZiB0aGUgc2FtZSBncmFwaCwgc2tpcC5cclxuICAgICAgaWYgKG5vZGVBLmdldE93bmVyKCkgIT0gbm9kZUIuZ2V0T3duZXIoKSlcclxuICAgICAge1xyXG4gICAgICAgIGNvbnRpbnVlO1xyXG4gICAgICB9XHJcblxyXG4gICAgICB0aGlzLmNhbGNSZXB1bHNpb25Gb3JjZShub2RlQSwgbm9kZUIpO1xyXG4gICAgfVxyXG4gIH1cclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5jYWxjR3Jhdml0YXRpb25hbEZvcmNlcyA9IGZ1bmN0aW9uICgpIHtcclxuICB2YXIgbm9kZTtcclxuICB2YXIgbE5vZGVzID0gdGhpcy5nZXRBbGxOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvbigpO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGxOb2Rlcy5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICBub2RlID0gbE5vZGVzW2ldO1xyXG4gICAgdGhpcy5jYWxjR3Jhdml0YXRpb25hbEZvcmNlKG5vZGUpO1xyXG4gIH1cclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5tb3ZlTm9kZXMgPSBmdW5jdGlvbiAoKSB7XHJcbiAgdmFyIGxOb2RlcyA9IHRoaXMuZ2V0QWxsTm9kZXMoKTtcclxuICB2YXIgbm9kZTtcclxuXHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBsTm9kZXMubGVuZ3RoOyBpKyspXHJcbiAge1xyXG4gICAgbm9kZSA9IGxOb2Rlc1tpXTtcclxuICAgIG5vZGUubW92ZSgpO1xyXG4gIH1cclxufVxyXG5cclxuRkRMYXlvdXQucHJvdG90eXBlLmNhbGNTcHJpbmdGb3JjZSA9IGZ1bmN0aW9uIChlZGdlLCBpZGVhbExlbmd0aCkge1xyXG4gIHZhciBzb3VyY2VOb2RlID0gZWRnZS5nZXRTb3VyY2UoKTtcclxuICB2YXIgdGFyZ2V0Tm9kZSA9IGVkZ2UuZ2V0VGFyZ2V0KCk7XHJcblxyXG4gIHZhciBsZW5ndGg7XHJcbiAgdmFyIHNwcmluZ0ZvcmNlO1xyXG4gIHZhciBzcHJpbmdGb3JjZVg7XHJcbiAgdmFyIHNwcmluZ0ZvcmNlWTtcclxuXHJcbiAgLy8gVXBkYXRlIGVkZ2UgbGVuZ3RoXHJcbiAgaWYgKHRoaXMudW5pZm9ybUxlYWZOb2RlU2l6ZXMgJiZcclxuICAgICAgICAgIHNvdXJjZU5vZGUuZ2V0Q2hpbGQoKSA9PSBudWxsICYmIHRhcmdldE5vZGUuZ2V0Q2hpbGQoKSA9PSBudWxsKVxyXG4gIHtcclxuICAgIGVkZ2UudXBkYXRlTGVuZ3RoU2ltcGxlKCk7XHJcbiAgfVxyXG4gIGVsc2VcclxuICB7XHJcbiAgICBlZGdlLnVwZGF0ZUxlbmd0aCgpO1xyXG5cclxuICAgIGlmIChlZGdlLmlzT3ZlcmxhcGluZ1NvdXJjZUFuZFRhcmdldClcclxuICAgIHtcclxuICAgICAgcmV0dXJuO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgbGVuZ3RoID0gZWRnZS5nZXRMZW5ndGgoKTtcclxuXHJcbiAgLy8gQ2FsY3VsYXRlIHNwcmluZyBmb3JjZXNcclxuICBzcHJpbmdGb3JjZSA9IHRoaXMuc3ByaW5nQ29uc3RhbnQgKiAobGVuZ3RoIC0gaWRlYWxMZW5ndGgpO1xyXG5cclxuICAvLyBQcm9qZWN0IGZvcmNlIG9udG8geCBhbmQgeSBheGVzXHJcbiAgc3ByaW5nRm9yY2VYID0gc3ByaW5nRm9yY2UgKiAoZWRnZS5sZW5ndGhYIC8gbGVuZ3RoKTtcclxuICBzcHJpbmdGb3JjZVkgPSBzcHJpbmdGb3JjZSAqIChlZGdlLmxlbmd0aFkgLyBsZW5ndGgpO1xyXG5cclxuICAvLyBBcHBseSBmb3JjZXMgb24gdGhlIGVuZCBub2Rlc1xyXG4gIHNvdXJjZU5vZGUuc3ByaW5nRm9yY2VYICs9IHNwcmluZ0ZvcmNlWDtcclxuICBzb3VyY2VOb2RlLnNwcmluZ0ZvcmNlWSArPSBzcHJpbmdGb3JjZVk7XHJcbiAgdGFyZ2V0Tm9kZS5zcHJpbmdGb3JjZVggLT0gc3ByaW5nRm9yY2VYO1xyXG4gIHRhcmdldE5vZGUuc3ByaW5nRm9yY2VZIC09IHNwcmluZ0ZvcmNlWTtcclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5jYWxjUmVwdWxzaW9uRm9yY2UgPSBmdW5jdGlvbiAobm9kZUEsIG5vZGVCKSB7XHJcbiAgdmFyIHJlY3RBID0gbm9kZUEuZ2V0UmVjdCgpO1xyXG4gIHZhciByZWN0QiA9IG5vZGVCLmdldFJlY3QoKTtcclxuICB2YXIgb3ZlcmxhcEFtb3VudCA9IG5ldyBBcnJheSgyKTtcclxuICB2YXIgY2xpcFBvaW50cyA9IG5ldyBBcnJheSg0KTtcclxuICB2YXIgZGlzdGFuY2VYO1xyXG4gIHZhciBkaXN0YW5jZVk7XHJcbiAgdmFyIGRpc3RhbmNlU3F1YXJlZDtcclxuICB2YXIgZGlzdGFuY2U7XHJcbiAgdmFyIHJlcHVsc2lvbkZvcmNlO1xyXG4gIHZhciByZXB1bHNpb25Gb3JjZVg7XHJcbiAgdmFyIHJlcHVsc2lvbkZvcmNlWTtcclxuXHJcbiAgaWYgKHJlY3RBLmludGVyc2VjdHMocmVjdEIpKS8vIHR3byBub2RlcyBvdmVybGFwXHJcbiAge1xyXG4gICAgLy8gY2FsY3VsYXRlIHNlcGFyYXRpb24gYW1vdW50IGluIHggYW5kIHkgZGlyZWN0aW9uc1xyXG4gICAgSUdlb21ldHJ5LmNhbGNTZXBhcmF0aW9uQW1vdW50KHJlY3RBLFxyXG4gICAgICAgICAgICByZWN0QixcclxuICAgICAgICAgICAgb3ZlcmxhcEFtb3VudCxcclxuICAgICAgICAgICAgRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9FREdFX0xFTkdUSCAvIDIuMCk7XHJcblxyXG4gICAgcmVwdWxzaW9uRm9yY2VYID0gb3ZlcmxhcEFtb3VudFswXTtcclxuICAgIHJlcHVsc2lvbkZvcmNlWSA9IG92ZXJsYXBBbW91bnRbMV07XHJcbiAgfVxyXG4gIGVsc2UvLyBubyBvdmVybGFwXHJcbiAge1xyXG4gICAgLy8gY2FsY3VsYXRlIGRpc3RhbmNlXHJcblxyXG4gICAgaWYgKHRoaXMudW5pZm9ybUxlYWZOb2RlU2l6ZXMgJiZcclxuICAgICAgICAgICAgbm9kZUEuZ2V0Q2hpbGQoKSA9PSBudWxsICYmIG5vZGVCLmdldENoaWxkKCkgPT0gbnVsbCkvLyBzaW1wbHkgYmFzZSByZXB1bHNpb24gb24gZGlzdGFuY2Ugb2Ygbm9kZSBjZW50ZXJzXHJcbiAgICB7XHJcbiAgICAgIGRpc3RhbmNlWCA9IHJlY3RCLmdldENlbnRlclgoKSAtIHJlY3RBLmdldENlbnRlclgoKTtcclxuICAgICAgZGlzdGFuY2VZID0gcmVjdEIuZ2V0Q2VudGVyWSgpIC0gcmVjdEEuZ2V0Q2VudGVyWSgpO1xyXG4gICAgfVxyXG4gICAgZWxzZS8vIHVzZSBjbGlwcGluZyBwb2ludHNcclxuICAgIHtcclxuICAgICAgSUdlb21ldHJ5LmdldEludGVyc2VjdGlvbihyZWN0QSwgcmVjdEIsIGNsaXBQb2ludHMpO1xyXG5cclxuICAgICAgZGlzdGFuY2VYID0gY2xpcFBvaW50c1syXSAtIGNsaXBQb2ludHNbMF07XHJcbiAgICAgIGRpc3RhbmNlWSA9IGNsaXBQb2ludHNbM10gLSBjbGlwUG9pbnRzWzFdO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIE5vIHJlcHVsc2lvbiByYW5nZS4gRlIgZ3JpZCB2YXJpYW50IHNob3VsZCB0YWtlIGNhcmUgb2YgdGhpcy5cclxuICAgIGlmIChNYXRoLmFicyhkaXN0YW5jZVgpIDwgRkRMYXlvdXRDb25zdGFudHMuTUlOX1JFUFVMU0lPTl9ESVNUKVxyXG4gICAge1xyXG4gICAgICBkaXN0YW5jZVggPSBJTWF0aC5zaWduKGRpc3RhbmNlWCkgKlxyXG4gICAgICAgICAgICAgIEZETGF5b3V0Q29uc3RhbnRzLk1JTl9SRVBVTFNJT05fRElTVDtcclxuICAgIH1cclxuXHJcbiAgICBpZiAoTWF0aC5hYnMoZGlzdGFuY2VZKSA8IEZETGF5b3V0Q29uc3RhbnRzLk1JTl9SRVBVTFNJT05fRElTVClcclxuICAgIHtcclxuICAgICAgZGlzdGFuY2VZID0gSU1hdGguc2lnbihkaXN0YW5jZVkpICpcclxuICAgICAgICAgICAgICBGRExheW91dENvbnN0YW50cy5NSU5fUkVQVUxTSU9OX0RJU1Q7XHJcbiAgICB9XHJcblxyXG4gICAgZGlzdGFuY2VTcXVhcmVkID0gZGlzdGFuY2VYICogZGlzdGFuY2VYICsgZGlzdGFuY2VZICogZGlzdGFuY2VZO1xyXG4gICAgZGlzdGFuY2UgPSBNYXRoLnNxcnQoZGlzdGFuY2VTcXVhcmVkKTtcclxuXHJcbiAgICByZXB1bHNpb25Gb3JjZSA9IHRoaXMucmVwdWxzaW9uQ29uc3RhbnQgLyBkaXN0YW5jZVNxdWFyZWQ7XHJcblxyXG4gICAgLy8gUHJvamVjdCBmb3JjZSBvbnRvIHggYW5kIHkgYXhlc1xyXG4gICAgcmVwdWxzaW9uRm9yY2VYID0gcmVwdWxzaW9uRm9yY2UgKiBkaXN0YW5jZVggLyBkaXN0YW5jZTtcclxuICAgIHJlcHVsc2lvbkZvcmNlWSA9IHJlcHVsc2lvbkZvcmNlICogZGlzdGFuY2VZIC8gZGlzdGFuY2U7XHJcbiAgfVxyXG5cclxuICAvLyBBcHBseSBmb3JjZXMgb24gdGhlIHR3byBub2Rlc1xyXG4gIG5vZGVBLnJlcHVsc2lvbkZvcmNlWCAtPSByZXB1bHNpb25Gb3JjZVg7XHJcbiAgbm9kZUEucmVwdWxzaW9uRm9yY2VZIC09IHJlcHVsc2lvbkZvcmNlWTtcclxuICBub2RlQi5yZXB1bHNpb25Gb3JjZVggKz0gcmVwdWxzaW9uRm9yY2VYO1xyXG4gIG5vZGVCLnJlcHVsc2lvbkZvcmNlWSArPSByZXB1bHNpb25Gb3JjZVk7XHJcbn07XHJcblxyXG5GRExheW91dC5wcm90b3R5cGUuY2FsY0dyYXZpdGF0aW9uYWxGb3JjZSA9IGZ1bmN0aW9uIChub2RlKSB7XHJcbiAgdmFyIG93bmVyR3JhcGg7XHJcbiAgdmFyIG93bmVyQ2VudGVyWDtcclxuICB2YXIgb3duZXJDZW50ZXJZO1xyXG4gIHZhciBkaXN0YW5jZVg7XHJcbiAgdmFyIGRpc3RhbmNlWTtcclxuICB2YXIgYWJzRGlzdGFuY2VYO1xyXG4gIHZhciBhYnNEaXN0YW5jZVk7XHJcbiAgdmFyIGVzdGltYXRlZFNpemU7XHJcbiAgb3duZXJHcmFwaCA9IG5vZGUuZ2V0T3duZXIoKTtcclxuXHJcbiAgb3duZXJDZW50ZXJYID0gKG93bmVyR3JhcGguZ2V0UmlnaHQoKSArIG93bmVyR3JhcGguZ2V0TGVmdCgpKSAvIDI7XHJcbiAgb3duZXJDZW50ZXJZID0gKG93bmVyR3JhcGguZ2V0VG9wKCkgKyBvd25lckdyYXBoLmdldEJvdHRvbSgpKSAvIDI7XHJcbiAgZGlzdGFuY2VYID0gbm9kZS5nZXRDZW50ZXJYKCkgLSBvd25lckNlbnRlclg7XHJcbiAgZGlzdGFuY2VZID0gbm9kZS5nZXRDZW50ZXJZKCkgLSBvd25lckNlbnRlclk7XHJcbiAgYWJzRGlzdGFuY2VYID0gTWF0aC5hYnMoZGlzdGFuY2VYKTtcclxuICBhYnNEaXN0YW5jZVkgPSBNYXRoLmFicyhkaXN0YW5jZVkpO1xyXG5cclxuICBpZiAobm9kZS5nZXRPd25lcigpID09IHRoaXMuZ3JhcGhNYW5hZ2VyLmdldFJvb3QoKSkvLyBpbiB0aGUgcm9vdCBncmFwaFxyXG4gIHtcclxuICAgIE1hdGguZmxvb3IoODApO1xyXG4gICAgZXN0aW1hdGVkU2l6ZSA9IE1hdGguZmxvb3Iob3duZXJHcmFwaC5nZXRFc3RpbWF0ZWRTaXplKCkgKlxyXG4gICAgICAgICAgICB0aGlzLmdyYXZpdHlSYW5nZUZhY3Rvcik7XHJcblxyXG4gICAgaWYgKGFic0Rpc3RhbmNlWCA+IGVzdGltYXRlZFNpemUgfHwgYWJzRGlzdGFuY2VZID4gZXN0aW1hdGVkU2l6ZSlcclxuICAgIHtcclxuICAgICAgbm9kZS5ncmF2aXRhdGlvbkZvcmNlWCA9IC10aGlzLmdyYXZpdHlDb25zdGFudCAqIGRpc3RhbmNlWDtcclxuICAgICAgbm9kZS5ncmF2aXRhdGlvbkZvcmNlWSA9IC10aGlzLmdyYXZpdHlDb25zdGFudCAqIGRpc3RhbmNlWTtcclxuICAgIH1cclxuICB9XHJcbiAgZWxzZS8vIGluc2lkZSBhIGNvbXBvdW5kXHJcbiAge1xyXG4gICAgZXN0aW1hdGVkU2l6ZSA9IE1hdGguZmxvb3IoKG93bmVyR3JhcGguZ2V0RXN0aW1hdGVkU2l6ZSgpICpcclxuICAgICAgICAgICAgdGhpcy5jb21wb3VuZEdyYXZpdHlSYW5nZUZhY3RvcikpO1xyXG5cclxuICAgIGlmIChhYnNEaXN0YW5jZVggPiBlc3RpbWF0ZWRTaXplIHx8IGFic0Rpc3RhbmNlWSA+IGVzdGltYXRlZFNpemUpXHJcbiAgICB7XHJcbiAgICAgIG5vZGUuZ3Jhdml0YXRpb25Gb3JjZVggPSAtdGhpcy5ncmF2aXR5Q29uc3RhbnQgKiBkaXN0YW5jZVggKlxyXG4gICAgICAgICAgICAgIHRoaXMuY29tcG91bmRHcmF2aXR5Q29uc3RhbnQ7XHJcbiAgICAgIG5vZGUuZ3Jhdml0YXRpb25Gb3JjZVkgPSAtdGhpcy5ncmF2aXR5Q29uc3RhbnQgKiBkaXN0YW5jZVkgKlxyXG4gICAgICAgICAgICAgIHRoaXMuY29tcG91bmRHcmF2aXR5Q29uc3RhbnQ7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuRkRMYXlvdXQucHJvdG90eXBlLmlzQ29udmVyZ2VkID0gZnVuY3Rpb24gKCkge1xyXG4gIHZhciBjb252ZXJnZWQ7XHJcbiAgdmFyIG9zY2lsYXRpbmcgPSBmYWxzZTtcclxuXHJcbiAgaWYgKHRoaXMudG90YWxJdGVyYXRpb25zID4gdGhpcy5tYXhJdGVyYXRpb25zIC8gMylcclxuICB7XHJcbiAgICBvc2NpbGF0aW5nID1cclxuICAgICAgICAgICAgTWF0aC5hYnModGhpcy50b3RhbERpc3BsYWNlbWVudCAtIHRoaXMub2xkVG90YWxEaXNwbGFjZW1lbnQpIDwgMjtcclxuICB9XHJcblxyXG4gIGNvbnZlcmdlZCA9IHRoaXMudG90YWxEaXNwbGFjZW1lbnQgPCB0aGlzLnRvdGFsRGlzcGxhY2VtZW50VGhyZXNob2xkO1xyXG5cclxuICB0aGlzLm9sZFRvdGFsRGlzcGxhY2VtZW50ID0gdGhpcy50b3RhbERpc3BsYWNlbWVudDtcclxuXHJcbiAgcmV0dXJuIGNvbnZlcmdlZCB8fCBvc2NpbGF0aW5nO1xyXG59O1xyXG5cclxuRkRMYXlvdXQucHJvdG90eXBlLmFuaW1hdGUgPSBmdW5jdGlvbiAoKSB7XHJcbiAgaWYgKHRoaXMuYW5pbWF0aW9uRHVyaW5nTGF5b3V0ICYmICF0aGlzLmlzU3ViTGF5b3V0KVxyXG4gIHtcclxuICAgIGlmICh0aGlzLm5vdEFuaW1hdGVkSXRlcmF0aW9ucyA9PSB0aGlzLmFuaW1hdGlvblBlcmlvZClcclxuICAgIHtcclxuICAgICAgdGhpcy51cGRhdGUoKTtcclxuICAgICAgdGhpcy5ub3RBbmltYXRlZEl0ZXJhdGlvbnMgPSAwO1xyXG4gICAgfVxyXG4gICAgZWxzZVxyXG4gICAge1xyXG4gICAgICB0aGlzLm5vdEFuaW1hdGVkSXRlcmF0aW9ucysrO1xyXG4gICAgfVxyXG4gIH1cclxufTtcclxuXHJcbkZETGF5b3V0LnByb3RvdHlwZS5jYWxjUmVwdWxzaW9uUmFuZ2UgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIDAuMDtcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gRkRMYXlvdXQ7XHJcbiIsInZhciBsYXlvdXRPcHRpb25zUGFjayA9IHJlcXVpcmUoJy4vbGF5b3V0T3B0aW9uc1BhY2snKTtcclxuXHJcbmZ1bmN0aW9uIEZETGF5b3V0Q29uc3RhbnRzKCkge1xyXG59XHJcblxyXG5GRExheW91dENvbnN0YW50cy5nZXRVc2VyT3B0aW9ucyA9IGZ1bmN0aW9uIChvcHRpb25zKSB7XHJcbiAgaWYgKG9wdGlvbnMubm9kZVJlcHVsc2lvbiAhPSBudWxsKVxyXG4gICAgRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9SRVBVTFNJT05fU1RSRU5HVEggPSBvcHRpb25zLm5vZGVSZXB1bHNpb247XHJcbiAgaWYgKG9wdGlvbnMuaWRlYWxFZGdlTGVuZ3RoICE9IG51bGwpIHtcclxuICAgIEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEggPSBvcHRpb25zLmlkZWFsRWRnZUxlbmd0aDtcclxuICB9XHJcbiAgaWYgKG9wdGlvbnMuZWRnZUVsYXN0aWNpdHkgIT0gbnVsbClcclxuICAgIEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfU1BSSU5HX1NUUkVOR1RIID0gb3B0aW9ucy5lZGdlRWxhc3RpY2l0eTtcclxuICBpZiAob3B0aW9ucy5uZXN0aW5nRmFjdG9yICE9IG51bGwpXHJcbiAgICBGRExheW91dENvbnN0YW50cy5QRVJfTEVWRUxfSURFQUxfRURHRV9MRU5HVEhfRkFDVE9SID0gb3B0aW9ucy5uZXN0aW5nRmFjdG9yO1xyXG4gIGlmIChvcHRpb25zLmdyYXZpdHkgIT0gbnVsbClcclxuICAgIEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfR1JBVklUWV9TVFJFTkdUSCA9IG9wdGlvbnMuZ3Jhdml0eTtcclxuICBpZiAob3B0aW9ucy5udW1JdGVyICE9IG51bGwpXHJcbiAgICBGRExheW91dENvbnN0YW50cy5NQVhfSVRFUkFUSU9OUyA9IG9wdGlvbnMubnVtSXRlcjtcclxuICBcclxuICBsYXlvdXRPcHRpb25zUGFjay5pbmNyZW1lbnRhbCA9ICEob3B0aW9ucy5yYW5kb21pemUpO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGUgPSBvcHRpb25zLmFuaW1hdGU7XHJcbn1cclxuXHJcbkZETGF5b3V0Q29uc3RhbnRzLk1BWF9JVEVSQVRJT05TID0gMjUwMDtcclxuXHJcbkZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEggPSA1MDtcclxuRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9TUFJJTkdfU1RSRU5HVEggPSAwLjQ1O1xyXG5GRExheW91dENvbnN0YW50cy5ERUZBVUxUX1JFUFVMU0lPTl9TVFJFTkdUSCA9IDQ1MDAuMDtcclxuRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9HUkFWSVRZX1NUUkVOR1RIID0gMC40O1xyXG5GRExheW91dENvbnN0YW50cy5ERUZBVUxUX0NPTVBPVU5EX0dSQVZJVFlfU1RSRU5HVEggPSAxLjA7XHJcbkZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfR1JBVklUWV9SQU5HRV9GQUNUT1IgPSAyLjA7XHJcbkZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfQ09NUE9VTkRfR1JBVklUWV9SQU5HRV9GQUNUT1IgPSAxLjU7XHJcbkZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfVVNFX1NNQVJUX0lERUFMX0VER0VfTEVOR1RIX0NBTENVTEFUSU9OID0gdHJ1ZTtcclxuRkRMYXlvdXRDb25zdGFudHMuREVGQVVMVF9VU0VfU01BUlRfUkVQVUxTSU9OX1JBTkdFX0NBTENVTEFUSU9OID0gdHJ1ZTtcclxuRkRMYXlvdXRDb25zdGFudHMuTUFYX05PREVfRElTUExBQ0VNRU5UX0lOQ1JFTUVOVEFMID0gMTAwLjA7XHJcbkZETGF5b3V0Q29uc3RhbnRzLk1BWF9OT0RFX0RJU1BMQUNFTUVOVCA9IEZETGF5b3V0Q29uc3RhbnRzLk1BWF9OT0RFX0RJU1BMQUNFTUVOVF9JTkNSRU1FTlRBTCAqIDM7XHJcbkZETGF5b3V0Q29uc3RhbnRzLk1JTl9SRVBVTFNJT05fRElTVCA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEggLyAxMC4wO1xyXG5GRExheW91dENvbnN0YW50cy5DT05WRVJHRU5DRV9DSEVDS19QRVJJT0QgPSAxMDA7XHJcbkZETGF5b3V0Q29uc3RhbnRzLlBFUl9MRVZFTF9JREVBTF9FREdFX0xFTkdUSF9GQUNUT1IgPSAwLjE7XHJcbkZETGF5b3V0Q29uc3RhbnRzLk1JTl9FREdFX0xFTkdUSCA9IDE7XHJcbkZETGF5b3V0Q29uc3RhbnRzLkdSSURfQ0FMQ1VMQVRJT05fQ0hFQ0tfUEVSSU9EID0gMTA7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IEZETGF5b3V0Q29uc3RhbnRzO1xyXG4iLCJ2YXIgTEVkZ2UgPSByZXF1aXJlKCcuL0xFZGdlJyk7XHJcbnZhciBGRExheW91dENvbnN0YW50cyA9IHJlcXVpcmUoJy4vRkRMYXlvdXRDb25zdGFudHMnKTtcclxuXHJcbmZ1bmN0aW9uIEZETGF5b3V0RWRnZShzb3VyY2UsIHRhcmdldCwgdkVkZ2UpIHtcclxuICBMRWRnZS5jYWxsKHRoaXMsIHNvdXJjZSwgdGFyZ2V0LCB2RWRnZSk7XHJcbiAgdGhpcy5pZGVhbExlbmd0aCA9IEZETGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEg7XHJcbn1cclxuXHJcbkZETGF5b3V0RWRnZS5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKExFZGdlLnByb3RvdHlwZSk7XHJcblxyXG5mb3IgKHZhciBwcm9wIGluIExFZGdlKSB7XHJcbiAgRkRMYXlvdXRFZGdlW3Byb3BdID0gTEVkZ2VbcHJvcF07XHJcbn1cclxuXHJcbm1vZHVsZS5leHBvcnRzID0gRkRMYXlvdXRFZGdlO1xyXG4iLCJ2YXIgTE5vZGUgPSByZXF1aXJlKCcuL0xOb2RlJyk7XHJcblxyXG5mdW5jdGlvbiBGRExheW91dE5vZGUoZ20sIGxvYywgc2l6ZSwgdk5vZGUpIHtcclxuICAvLyBhbHRlcm5hdGl2ZSBjb25zdHJ1Y3RvciBpcyBoYW5kbGVkIGluc2lkZSBMTm9kZVxyXG4gIExOb2RlLmNhbGwodGhpcywgZ20sIGxvYywgc2l6ZSwgdk5vZGUpO1xyXG4gIC8vU3ByaW5nLCByZXB1bHNpb24gYW5kIGdyYXZpdGF0aW9uYWwgZm9yY2VzIGFjdGluZyBvbiB0aGlzIG5vZGVcclxuICB0aGlzLnNwcmluZ0ZvcmNlWCA9IDA7XHJcbiAgdGhpcy5zcHJpbmdGb3JjZVkgPSAwO1xyXG4gIHRoaXMucmVwdWxzaW9uRm9yY2VYID0gMDtcclxuICB0aGlzLnJlcHVsc2lvbkZvcmNlWSA9IDA7XHJcbiAgdGhpcy5ncmF2aXRhdGlvbkZvcmNlWCA9IDA7XHJcbiAgdGhpcy5ncmF2aXRhdGlvbkZvcmNlWSA9IDA7XHJcbiAgLy9BbW91bnQgYnkgd2hpY2ggdGhpcyBub2RlIGlzIHRvIGJlIG1vdmVkIGluIHRoaXMgaXRlcmF0aW9uXHJcbiAgdGhpcy5kaXNwbGFjZW1lbnRYID0gMDtcclxuICB0aGlzLmRpc3BsYWNlbWVudFkgPSAwO1xyXG5cclxuICAvL1N0YXJ0IGFuZCBmaW5pc2ggZ3JpZCBjb29yZGluYXRlcyB0aGF0IHRoaXMgbm9kZSBpcyBmYWxsZW4gaW50b1xyXG4gIHRoaXMuc3RhcnRYID0gMDtcclxuICB0aGlzLmZpbmlzaFggPSAwO1xyXG4gIHRoaXMuc3RhcnRZID0gMDtcclxuICB0aGlzLmZpbmlzaFkgPSAwO1xyXG5cclxuICAvL0dlb21ldHJpYyBuZWlnaGJvcnMgb2YgdGhpcyBub2RlXHJcbiAgdGhpcy5zdXJyb3VuZGluZyA9IFtdO1xyXG59XHJcblxyXG5GRExheW91dE5vZGUucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShMTm9kZS5wcm90b3R5cGUpO1xyXG5cclxuZm9yICh2YXIgcHJvcCBpbiBMTm9kZSkge1xyXG4gIEZETGF5b3V0Tm9kZVtwcm9wXSA9IExOb2RlW3Byb3BdO1xyXG59XHJcblxyXG5GRExheW91dE5vZGUucHJvdG90eXBlLnNldEdyaWRDb29yZGluYXRlcyA9IGZ1bmN0aW9uIChfc3RhcnRYLCBfZmluaXNoWCwgX3N0YXJ0WSwgX2ZpbmlzaFkpXHJcbntcclxuICB0aGlzLnN0YXJ0WCA9IF9zdGFydFg7XHJcbiAgdGhpcy5maW5pc2hYID0gX2ZpbmlzaFg7XHJcbiAgdGhpcy5zdGFydFkgPSBfc3RhcnRZO1xyXG4gIHRoaXMuZmluaXNoWSA9IF9maW5pc2hZO1xyXG5cclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gRkRMYXlvdXROb2RlO1xyXG4iLCJ2YXIgVW5pcXVlSURHZW5lcmV0b3IgPSByZXF1aXJlKCcuL1VuaXF1ZUlER2VuZXJldG9yJyk7XHJcblxyXG5mdW5jdGlvbiBIYXNoTWFwKCkge1xyXG4gIHRoaXMubWFwID0ge307XHJcbiAgdGhpcy5rZXlzID0gW107XHJcbn1cclxuXHJcbkhhc2hNYXAucHJvdG90eXBlLnB1dCA9IGZ1bmN0aW9uIChrZXksIHZhbHVlKSB7XHJcbiAgdmFyIHRoZUlkID0gVW5pcXVlSURHZW5lcmV0b3IuY3JlYXRlSUQoa2V5KTtcclxuICBpZiAoIXRoaXMuY29udGFpbnModGhlSWQpKSB7XHJcbiAgICB0aGlzLm1hcFt0aGVJZF0gPSB2YWx1ZTtcclxuICAgIHRoaXMua2V5cy5wdXNoKGtleSk7XHJcbiAgfVxyXG59O1xyXG5cclxuSGFzaE1hcC5wcm90b3R5cGUuY29udGFpbnMgPSBmdW5jdGlvbiAoa2V5KSB7XHJcbiAgdmFyIHRoZUlkID0gVW5pcXVlSURHZW5lcmV0b3IuY3JlYXRlSUQoa2V5KTtcclxuICByZXR1cm4gdGhpcy5tYXBba2V5XSAhPSBudWxsO1xyXG59O1xyXG5cclxuSGFzaE1hcC5wcm90b3R5cGUuZ2V0ID0gZnVuY3Rpb24gKGtleSkge1xyXG4gIHZhciB0aGVJZCA9IFVuaXF1ZUlER2VuZXJldG9yLmNyZWF0ZUlEKGtleSk7XHJcbiAgcmV0dXJuIHRoaXMubWFwW3RoZUlkXTtcclxufTtcclxuXHJcbkhhc2hNYXAucHJvdG90eXBlLmtleVNldCA9IGZ1bmN0aW9uICgpIHtcclxuICByZXR1cm4gdGhpcy5rZXlzO1xyXG59O1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBIYXNoTWFwO1xyXG4iLCJ2YXIgVW5pcXVlSURHZW5lcmV0b3IgPSByZXF1aXJlKCcuL1VuaXF1ZUlER2VuZXJldG9yJyk7XHJcblxyXG5mdW5jdGlvbiBIYXNoU2V0KCkge1xyXG4gIHRoaXMuc2V0ID0ge307XHJcbn1cclxuO1xyXG5cclxuSGFzaFNldC5wcm90b3R5cGUuYWRkID0gZnVuY3Rpb24gKG9iaikge1xyXG4gIHZhciB0aGVJZCA9IFVuaXF1ZUlER2VuZXJldG9yLmNyZWF0ZUlEKG9iaik7XHJcbiAgaWYgKCF0aGlzLmNvbnRhaW5zKHRoZUlkKSlcclxuICAgIHRoaXMuc2V0W3RoZUlkXSA9IG9iajtcclxufTtcclxuXHJcbkhhc2hTZXQucHJvdG90eXBlLnJlbW92ZSA9IGZ1bmN0aW9uIChvYmopIHtcclxuICBkZWxldGUgdGhpcy5zZXRbVW5pcXVlSURHZW5lcmV0b3IuY3JlYXRlSUQob2JqKV07XHJcbn07XHJcblxyXG5IYXNoU2V0LnByb3RvdHlwZS5jbGVhciA9IGZ1bmN0aW9uICgpIHtcclxuICB0aGlzLnNldCA9IHt9O1xyXG59O1xyXG5cclxuSGFzaFNldC5wcm90b3R5cGUuY29udGFpbnMgPSBmdW5jdGlvbiAob2JqKSB7XHJcbiAgcmV0dXJuIHRoaXMuc2V0W1VuaXF1ZUlER2VuZXJldG9yLmNyZWF0ZUlEKG9iaildID09IG9iajtcclxufTtcclxuXHJcbkhhc2hTZXQucHJvdG90eXBlLmlzRW1wdHkgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMuc2l6ZSgpID09PSAwO1xyXG59O1xyXG5cclxuSGFzaFNldC5wcm90b3R5cGUuc2l6ZSA9IGZ1bmN0aW9uICgpIHtcclxuICByZXR1cm4gT2JqZWN0LmtleXModGhpcy5zZXQpLmxlbmd0aDtcclxufTtcclxuXHJcbi8vY29uY2F0cyB0aGlzLnNldCB0byB0aGUgZ2l2ZW4gbGlzdFxyXG5IYXNoU2V0LnByb3RvdHlwZS5hZGRBbGxUbyA9IGZ1bmN0aW9uIChsaXN0KSB7XHJcbiAgdmFyIGtleXMgPSBPYmplY3Qua2V5cyh0aGlzLnNldCk7XHJcbiAgdmFyIGxlbmd0aCA9IGtleXMubGVuZ3RoO1xyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgbGVuZ3RoOyBpKyspIHtcclxuICAgIGxpc3QucHVzaCh0aGlzLnNldFtrZXlzW2ldXSk7XHJcbiAgfVxyXG59O1xyXG5cclxuSGFzaFNldC5wcm90b3R5cGUuc2l6ZSA9IGZ1bmN0aW9uICgpIHtcclxuICByZXR1cm4gT2JqZWN0LmtleXModGhpcy5zZXQpLmxlbmd0aDtcclxufTtcclxuXHJcbkhhc2hTZXQucHJvdG90eXBlLmFkZEFsbCA9IGZ1bmN0aW9uIChsaXN0KSB7XHJcbiAgdmFyIHMgPSBsaXN0Lmxlbmd0aDtcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHM7IGkrKykge1xyXG4gICAgdmFyIHYgPSBsaXN0W2ldO1xyXG4gICAgdGhpcy5hZGQodik7XHJcbiAgfVxyXG59O1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBIYXNoU2V0O1xyXG4iLCJmdW5jdGlvbiBJR2VvbWV0cnkoKSB7XHJcbn1cclxuXHJcbklHZW9tZXRyeS5jYWxjU2VwYXJhdGlvbkFtb3VudCA9IGZ1bmN0aW9uIChyZWN0QSwgcmVjdEIsIG92ZXJsYXBBbW91bnQsIHNlcGFyYXRpb25CdWZmZXIpXHJcbntcclxuICBpZiAoIXJlY3RBLmludGVyc2VjdHMocmVjdEIpKSB7XHJcbiAgICB0aHJvdyBcImFzc2VydCBmYWlsZWRcIjtcclxuICB9XHJcbiAgdmFyIGRpcmVjdGlvbnMgPSBuZXcgQXJyYXkoMik7XHJcbiAgSUdlb21ldHJ5LmRlY2lkZURpcmVjdGlvbnNGb3JPdmVybGFwcGluZ05vZGVzKHJlY3RBLCByZWN0QiwgZGlyZWN0aW9ucyk7XHJcbiAgb3ZlcmxhcEFtb3VudFswXSA9IE1hdGgubWluKHJlY3RBLmdldFJpZ2h0KCksIHJlY3RCLmdldFJpZ2h0KCkpIC1cclxuICAgICAgICAgIE1hdGgubWF4KHJlY3RBLngsIHJlY3RCLngpO1xyXG4gIG92ZXJsYXBBbW91bnRbMV0gPSBNYXRoLm1pbihyZWN0QS5nZXRCb3R0b20oKSwgcmVjdEIuZ2V0Qm90dG9tKCkpIC1cclxuICAgICAgICAgIE1hdGgubWF4KHJlY3RBLnksIHJlY3RCLnkpO1xyXG4gIC8vIHVwZGF0ZSB0aGUgb3ZlcmxhcHBpbmcgYW1vdW50cyBmb3IgdGhlIGZvbGxvd2luZyBjYXNlczpcclxuICBpZiAoKHJlY3RBLmdldFgoKSA8PSByZWN0Qi5nZXRYKCkpICYmIChyZWN0QS5nZXRSaWdodCgpID49IHJlY3RCLmdldFJpZ2h0KCkpKVxyXG4gIHtcclxuICAgIG92ZXJsYXBBbW91bnRbMF0gKz0gTWF0aC5taW4oKHJlY3RCLmdldFgoKSAtIHJlY3RBLmdldFgoKSksXHJcbiAgICAgICAgICAgIChyZWN0QS5nZXRSaWdodCgpIC0gcmVjdEIuZ2V0UmlnaHQoKSkpO1xyXG4gIH1cclxuICBlbHNlIGlmICgocmVjdEIuZ2V0WCgpIDw9IHJlY3RBLmdldFgoKSkgJiYgKHJlY3RCLmdldFJpZ2h0KCkgPj0gcmVjdEEuZ2V0UmlnaHQoKSkpXHJcbiAge1xyXG4gICAgb3ZlcmxhcEFtb3VudFswXSArPSBNYXRoLm1pbigocmVjdEEuZ2V0WCgpIC0gcmVjdEIuZ2V0WCgpKSxcclxuICAgICAgICAgICAgKHJlY3RCLmdldFJpZ2h0KCkgLSByZWN0QS5nZXRSaWdodCgpKSk7XHJcbiAgfVxyXG4gIGlmICgocmVjdEEuZ2V0WSgpIDw9IHJlY3RCLmdldFkoKSkgJiYgKHJlY3RBLmdldEJvdHRvbSgpID49IHJlY3RCLmdldEJvdHRvbSgpKSlcclxuICB7XHJcbiAgICBvdmVybGFwQW1vdW50WzFdICs9IE1hdGgubWluKChyZWN0Qi5nZXRZKCkgLSByZWN0QS5nZXRZKCkpLFxyXG4gICAgICAgICAgICAocmVjdEEuZ2V0Qm90dG9tKCkgLSByZWN0Qi5nZXRCb3R0b20oKSkpO1xyXG4gIH1cclxuICBlbHNlIGlmICgocmVjdEIuZ2V0WSgpIDw9IHJlY3RBLmdldFkoKSkgJiYgKHJlY3RCLmdldEJvdHRvbSgpID49IHJlY3RBLmdldEJvdHRvbSgpKSlcclxuICB7XHJcbiAgICBvdmVybGFwQW1vdW50WzFdICs9IE1hdGgubWluKChyZWN0QS5nZXRZKCkgLSByZWN0Qi5nZXRZKCkpLFxyXG4gICAgICAgICAgICAocmVjdEIuZ2V0Qm90dG9tKCkgLSByZWN0QS5nZXRCb3R0b20oKSkpO1xyXG4gIH1cclxuXHJcbiAgLy8gZmluZCBzbG9wZSBvZiB0aGUgbGluZSBwYXNzZXMgdHdvIGNlbnRlcnNcclxuICB2YXIgc2xvcGUgPSBNYXRoLmFicygocmVjdEIuZ2V0Q2VudGVyWSgpIC0gcmVjdEEuZ2V0Q2VudGVyWSgpKSAvXHJcbiAgICAgICAgICAocmVjdEIuZ2V0Q2VudGVyWCgpIC0gcmVjdEEuZ2V0Q2VudGVyWCgpKSk7XHJcbiAgLy8gaWYgY2VudGVycyBhcmUgb3ZlcmxhcHBlZFxyXG4gIGlmICgocmVjdEIuZ2V0Q2VudGVyWSgpID09IHJlY3RBLmdldENlbnRlclkoKSkgJiZcclxuICAgICAgICAgIChyZWN0Qi5nZXRDZW50ZXJYKCkgPT0gcmVjdEEuZ2V0Q2VudGVyWCgpKSlcclxuICB7XHJcbiAgICAvLyBhc3N1bWUgdGhlIHNsb3BlIGlzIDEgKDQ1IGRlZ3JlZSlcclxuICAgIHNsb3BlID0gMS4wO1xyXG4gIH1cclxuXHJcbiAgdmFyIG1vdmVCeVkgPSBzbG9wZSAqIG92ZXJsYXBBbW91bnRbMF07XHJcbiAgdmFyIG1vdmVCeVggPSBvdmVybGFwQW1vdW50WzFdIC8gc2xvcGU7XHJcbiAgaWYgKG92ZXJsYXBBbW91bnRbMF0gPCBtb3ZlQnlYKVxyXG4gIHtcclxuICAgIG1vdmVCeVggPSBvdmVybGFwQW1vdW50WzBdO1xyXG4gIH1cclxuICBlbHNlXHJcbiAge1xyXG4gICAgbW92ZUJ5WSA9IG92ZXJsYXBBbW91bnRbMV07XHJcbiAgfVxyXG4gIC8vIHJldHVybiBoYWxmIHRoZSBhbW91bnQgc28gdGhhdCBpZiBlYWNoIHJlY3RhbmdsZSBpcyBtb3ZlZCBieSB0aGVzZVxyXG4gIC8vIGFtb3VudHMgaW4gb3Bwb3NpdGUgZGlyZWN0aW9ucywgb3ZlcmxhcCB3aWxsIGJlIHJlc29sdmVkXHJcbiAgb3ZlcmxhcEFtb3VudFswXSA9IC0xICogZGlyZWN0aW9uc1swXSAqICgobW92ZUJ5WCAvIDIpICsgc2VwYXJhdGlvbkJ1ZmZlcik7XHJcbiAgb3ZlcmxhcEFtb3VudFsxXSA9IC0xICogZGlyZWN0aW9uc1sxXSAqICgobW92ZUJ5WSAvIDIpICsgc2VwYXJhdGlvbkJ1ZmZlcik7XHJcbn1cclxuXHJcbklHZW9tZXRyeS5kZWNpZGVEaXJlY3Rpb25zRm9yT3ZlcmxhcHBpbmdOb2RlcyA9IGZ1bmN0aW9uIChyZWN0QSwgcmVjdEIsIGRpcmVjdGlvbnMpXHJcbntcclxuICBpZiAocmVjdEEuZ2V0Q2VudGVyWCgpIDwgcmVjdEIuZ2V0Q2VudGVyWCgpKVxyXG4gIHtcclxuICAgIGRpcmVjdGlvbnNbMF0gPSAtMTtcclxuICB9XHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIGRpcmVjdGlvbnNbMF0gPSAxO1xyXG4gIH1cclxuXHJcbiAgaWYgKHJlY3RBLmdldENlbnRlclkoKSA8IHJlY3RCLmdldENlbnRlclkoKSlcclxuICB7XHJcbiAgICBkaXJlY3Rpb25zWzFdID0gLTE7XHJcbiAgfVxyXG4gIGVsc2VcclxuICB7XHJcbiAgICBkaXJlY3Rpb25zWzFdID0gMTtcclxuICB9XHJcbn1cclxuXHJcbklHZW9tZXRyeS5nZXRJbnRlcnNlY3Rpb24yID0gZnVuY3Rpb24gKHJlY3RBLCByZWN0QiwgcmVzdWx0KVxyXG57XHJcbiAgLy9yZXN1bHRbMC0xXSB3aWxsIGNvbnRhaW4gY2xpcFBvaW50IG9mIHJlY3RBLCByZXN1bHRbMi0zXSB3aWxsIGNvbnRhaW4gY2xpcFBvaW50IG9mIHJlY3RCXHJcbiAgdmFyIHAxeCA9IHJlY3RBLmdldENlbnRlclgoKTtcclxuICB2YXIgcDF5ID0gcmVjdEEuZ2V0Q2VudGVyWSgpO1xyXG4gIHZhciBwMnggPSByZWN0Qi5nZXRDZW50ZXJYKCk7XHJcbiAgdmFyIHAyeSA9IHJlY3RCLmdldENlbnRlclkoKTtcclxuXHJcbiAgLy9pZiB0d28gcmVjdGFuZ2xlcyBpbnRlcnNlY3QsIHRoZW4gY2xpcHBpbmcgcG9pbnRzIGFyZSBjZW50ZXJzXHJcbiAgaWYgKHJlY3RBLmludGVyc2VjdHMocmVjdEIpKVxyXG4gIHtcclxuICAgIHJlc3VsdFswXSA9IHAxeDtcclxuICAgIHJlc3VsdFsxXSA9IHAxeTtcclxuICAgIHJlc3VsdFsyXSA9IHAyeDtcclxuICAgIHJlc3VsdFszXSA9IHAyeTtcclxuICAgIHJldHVybiB0cnVlO1xyXG4gIH1cclxuICAvL3ZhcmlhYmxlcyBmb3IgcmVjdEFcclxuICB2YXIgdG9wTGVmdEF4ID0gcmVjdEEuZ2V0WCgpO1xyXG4gIHZhciB0b3BMZWZ0QXkgPSByZWN0QS5nZXRZKCk7XHJcbiAgdmFyIHRvcFJpZ2h0QXggPSByZWN0QS5nZXRSaWdodCgpO1xyXG4gIHZhciBib3R0b21MZWZ0QXggPSByZWN0QS5nZXRYKCk7XHJcbiAgdmFyIGJvdHRvbUxlZnRBeSA9IHJlY3RBLmdldEJvdHRvbSgpO1xyXG4gIHZhciBib3R0b21SaWdodEF4ID0gcmVjdEEuZ2V0UmlnaHQoKTtcclxuICB2YXIgaGFsZldpZHRoQSA9IHJlY3RBLmdldFdpZHRoSGFsZigpO1xyXG4gIHZhciBoYWxmSGVpZ2h0QSA9IHJlY3RBLmdldEhlaWdodEhhbGYoKTtcclxuICAvL3ZhcmlhYmxlcyBmb3IgcmVjdEJcclxuICB2YXIgdG9wTGVmdEJ4ID0gcmVjdEIuZ2V0WCgpO1xyXG4gIHZhciB0b3BMZWZ0QnkgPSByZWN0Qi5nZXRZKCk7XHJcbiAgdmFyIHRvcFJpZ2h0QnggPSByZWN0Qi5nZXRSaWdodCgpO1xyXG4gIHZhciBib3R0b21MZWZ0QnggPSByZWN0Qi5nZXRYKCk7XHJcbiAgdmFyIGJvdHRvbUxlZnRCeSA9IHJlY3RCLmdldEJvdHRvbSgpO1xyXG4gIHZhciBib3R0b21SaWdodEJ4ID0gcmVjdEIuZ2V0UmlnaHQoKTtcclxuICB2YXIgaGFsZldpZHRoQiA9IHJlY3RCLmdldFdpZHRoSGFsZigpO1xyXG4gIHZhciBoYWxmSGVpZ2h0QiA9IHJlY3RCLmdldEhlaWdodEhhbGYoKTtcclxuICAvL2ZsYWcgd2hldGhlciBjbGlwcGluZyBwb2ludHMgYXJlIGZvdW5kXHJcbiAgdmFyIGNsaXBQb2ludEFGb3VuZCA9IGZhbHNlO1xyXG4gIHZhciBjbGlwUG9pbnRCRm91bmQgPSBmYWxzZTtcclxuXHJcbiAgLy8gbGluZSBpcyB2ZXJ0aWNhbFxyXG4gIGlmIChwMXggPT0gcDJ4KVxyXG4gIHtcclxuICAgIGlmIChwMXkgPiBwMnkpXHJcbiAgICB7XHJcbiAgICAgIHJlc3VsdFswXSA9IHAxeDtcclxuICAgICAgcmVzdWx0WzFdID0gdG9wTGVmdEF5O1xyXG4gICAgICByZXN1bHRbMl0gPSBwMng7XHJcbiAgICAgIHJlc3VsdFszXSA9IGJvdHRvbUxlZnRCeTtcclxuICAgICAgcmV0dXJuIGZhbHNlO1xyXG4gICAgfVxyXG4gICAgZWxzZSBpZiAocDF5IDwgcDJ5KVxyXG4gICAge1xyXG4gICAgICByZXN1bHRbMF0gPSBwMXg7XHJcbiAgICAgIHJlc3VsdFsxXSA9IGJvdHRvbUxlZnRBeTtcclxuICAgICAgcmVzdWx0WzJdID0gcDJ4O1xyXG4gICAgICByZXN1bHRbM10gPSB0b3BMZWZ0Qnk7XHJcbiAgICAgIHJldHVybiBmYWxzZTtcclxuICAgIH1cclxuICAgIGVsc2VcclxuICAgIHtcclxuICAgICAgLy9ub3QgbGluZSwgcmV0dXJuIG51bGw7XHJcbiAgICB9XHJcbiAgfVxyXG4gIC8vIGxpbmUgaXMgaG9yaXpvbnRhbFxyXG4gIGVsc2UgaWYgKHAxeSA9PSBwMnkpXHJcbiAge1xyXG4gICAgaWYgKHAxeCA+IHAyeClcclxuICAgIHtcclxuICAgICAgcmVzdWx0WzBdID0gdG9wTGVmdEF4O1xyXG4gICAgICByZXN1bHRbMV0gPSBwMXk7XHJcbiAgICAgIHJlc3VsdFsyXSA9IHRvcFJpZ2h0Qng7XHJcbiAgICAgIHJlc3VsdFszXSA9IHAyeTtcclxuICAgICAgcmV0dXJuIGZhbHNlO1xyXG4gICAgfVxyXG4gICAgZWxzZSBpZiAocDF4IDwgcDJ4KVxyXG4gICAge1xyXG4gICAgICByZXN1bHRbMF0gPSB0b3BSaWdodEF4O1xyXG4gICAgICByZXN1bHRbMV0gPSBwMXk7XHJcbiAgICAgIHJlc3VsdFsyXSA9IHRvcExlZnRCeDtcclxuICAgICAgcmVzdWx0WzNdID0gcDJ5O1xyXG4gICAgICByZXR1cm4gZmFsc2U7XHJcbiAgICB9XHJcbiAgICBlbHNlXHJcbiAgICB7XHJcbiAgICAgIC8vbm90IHZhbGlkIGxpbmUsIHJldHVybiBudWxsO1xyXG4gICAgfVxyXG4gIH1cclxuICBlbHNlXHJcbiAge1xyXG4gICAgLy9zbG9wZXMgb2YgcmVjdEEncyBhbmQgcmVjdEIncyBkaWFnb25hbHNcclxuICAgIHZhciBzbG9wZUEgPSByZWN0QS5oZWlnaHQgLyByZWN0QS53aWR0aDtcclxuICAgIHZhciBzbG9wZUIgPSByZWN0Qi5oZWlnaHQgLyByZWN0Qi53aWR0aDtcclxuXHJcbiAgICAvL3Nsb3BlIG9mIGxpbmUgYmV0d2VlbiBjZW50ZXIgb2YgcmVjdEEgYW5kIGNlbnRlciBvZiByZWN0QlxyXG4gICAgdmFyIHNsb3BlUHJpbWUgPSAocDJ5IC0gcDF5KSAvIChwMnggLSBwMXgpO1xyXG4gICAgdmFyIGNhcmRpbmFsRGlyZWN0aW9uQTtcclxuICAgIHZhciBjYXJkaW5hbERpcmVjdGlvbkI7XHJcbiAgICB2YXIgdGVtcFBvaW50QXg7XHJcbiAgICB2YXIgdGVtcFBvaW50QXk7XHJcbiAgICB2YXIgdGVtcFBvaW50Qng7XHJcbiAgICB2YXIgdGVtcFBvaW50Qnk7XHJcblxyXG4gICAgLy9kZXRlcm1pbmUgd2hldGhlciBjbGlwcGluZyBwb2ludCBpcyB0aGUgY29ybmVyIG9mIG5vZGVBXHJcbiAgICBpZiAoKC1zbG9wZUEpID09IHNsb3BlUHJpbWUpXHJcbiAgICB7XHJcbiAgICAgIGlmIChwMXggPiBwMngpXHJcbiAgICAgIHtcclxuICAgICAgICByZXN1bHRbMF0gPSBib3R0b21MZWZ0QXg7XHJcbiAgICAgICAgcmVzdWx0WzFdID0gYm90dG9tTGVmdEF5O1xyXG4gICAgICAgIGNsaXBQb2ludEFGb3VuZCA9IHRydWU7XHJcbiAgICAgIH1cclxuICAgICAgZWxzZVxyXG4gICAgICB7XHJcbiAgICAgICAgcmVzdWx0WzBdID0gdG9wUmlnaHRBeDtcclxuICAgICAgICByZXN1bHRbMV0gPSB0b3BMZWZ0QXk7XHJcbiAgICAgICAgY2xpcFBvaW50QUZvdW5kID0gdHJ1ZTtcclxuICAgICAgfVxyXG4gICAgfVxyXG4gICAgZWxzZSBpZiAoc2xvcGVBID09IHNsb3BlUHJpbWUpXHJcbiAgICB7XHJcbiAgICAgIGlmIChwMXggPiBwMngpXHJcbiAgICAgIHtcclxuICAgICAgICByZXN1bHRbMF0gPSB0b3BMZWZ0QXg7XHJcbiAgICAgICAgcmVzdWx0WzFdID0gdG9wTGVmdEF5O1xyXG4gICAgICAgIGNsaXBQb2ludEFGb3VuZCA9IHRydWU7XHJcbiAgICAgIH1cclxuICAgICAgZWxzZVxyXG4gICAgICB7XHJcbiAgICAgICAgcmVzdWx0WzBdID0gYm90dG9tUmlnaHRBeDtcclxuICAgICAgICByZXN1bHRbMV0gPSBib3R0b21MZWZ0QXk7XHJcbiAgICAgICAgY2xpcFBvaW50QUZvdW5kID0gdHJ1ZTtcclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8vZGV0ZXJtaW5lIHdoZXRoZXIgY2xpcHBpbmcgcG9pbnQgaXMgdGhlIGNvcm5lciBvZiBub2RlQlxyXG4gICAgaWYgKCgtc2xvcGVCKSA9PSBzbG9wZVByaW1lKVxyXG4gICAge1xyXG4gICAgICBpZiAocDJ4ID4gcDF4KVxyXG4gICAgICB7XHJcbiAgICAgICAgcmVzdWx0WzJdID0gYm90dG9tTGVmdEJ4O1xyXG4gICAgICAgIHJlc3VsdFszXSA9IGJvdHRvbUxlZnRCeTtcclxuICAgICAgICBjbGlwUG9pbnRCRm91bmQgPSB0cnVlO1xyXG4gICAgICB9XHJcbiAgICAgIGVsc2VcclxuICAgICAge1xyXG4gICAgICAgIHJlc3VsdFsyXSA9IHRvcFJpZ2h0Qng7XHJcbiAgICAgICAgcmVzdWx0WzNdID0gdG9wTGVmdEJ5O1xyXG4gICAgICAgIGNsaXBQb2ludEJGb3VuZCA9IHRydWU7XHJcbiAgICAgIH1cclxuICAgIH1cclxuICAgIGVsc2UgaWYgKHNsb3BlQiA9PSBzbG9wZVByaW1lKVxyXG4gICAge1xyXG4gICAgICBpZiAocDJ4ID4gcDF4KVxyXG4gICAgICB7XHJcbiAgICAgICAgcmVzdWx0WzJdID0gdG9wTGVmdEJ4O1xyXG4gICAgICAgIHJlc3VsdFszXSA9IHRvcExlZnRCeTtcclxuICAgICAgICBjbGlwUG9pbnRCRm91bmQgPSB0cnVlO1xyXG4gICAgICB9XHJcbiAgICAgIGVsc2VcclxuICAgICAge1xyXG4gICAgICAgIHJlc3VsdFsyXSA9IGJvdHRvbVJpZ2h0Qng7XHJcbiAgICAgICAgcmVzdWx0WzNdID0gYm90dG9tTGVmdEJ5O1xyXG4gICAgICAgIGNsaXBQb2ludEJGb3VuZCA9IHRydWU7XHJcbiAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvL2lmIGJvdGggY2xpcHBpbmcgcG9pbnRzIGFyZSBjb3JuZXJzXHJcbiAgICBpZiAoY2xpcFBvaW50QUZvdW5kICYmIGNsaXBQb2ludEJGb3VuZClcclxuICAgIHtcclxuICAgICAgcmV0dXJuIGZhbHNlO1xyXG4gICAgfVxyXG5cclxuICAgIC8vZGV0ZXJtaW5lIENhcmRpbmFsIERpcmVjdGlvbiBvZiByZWN0YW5nbGVzXHJcbiAgICBpZiAocDF4ID4gcDJ4KVxyXG4gICAge1xyXG4gICAgICBpZiAocDF5ID4gcDJ5KVxyXG4gICAgICB7XHJcbiAgICAgICAgY2FyZGluYWxEaXJlY3Rpb25BID0gSUdlb21ldHJ5LmdldENhcmRpbmFsRGlyZWN0aW9uKHNsb3BlQSwgc2xvcGVQcmltZSwgNCk7XHJcbiAgICAgICAgY2FyZGluYWxEaXJlY3Rpb25CID0gSUdlb21ldHJ5LmdldENhcmRpbmFsRGlyZWN0aW9uKHNsb3BlQiwgc2xvcGVQcmltZSwgMik7XHJcbiAgICAgIH1cclxuICAgICAgZWxzZVxyXG4gICAgICB7XHJcbiAgICAgICAgY2FyZGluYWxEaXJlY3Rpb25BID0gSUdlb21ldHJ5LmdldENhcmRpbmFsRGlyZWN0aW9uKC1zbG9wZUEsIHNsb3BlUHJpbWUsIDMpO1xyXG4gICAgICAgIGNhcmRpbmFsRGlyZWN0aW9uQiA9IElHZW9tZXRyeS5nZXRDYXJkaW5hbERpcmVjdGlvbigtc2xvcGVCLCBzbG9wZVByaW1lLCAxKTtcclxuICAgICAgfVxyXG4gICAgfVxyXG4gICAgZWxzZVxyXG4gICAge1xyXG4gICAgICBpZiAocDF5ID4gcDJ5KVxyXG4gICAgICB7XHJcbiAgICAgICAgY2FyZGluYWxEaXJlY3Rpb25BID0gSUdlb21ldHJ5LmdldENhcmRpbmFsRGlyZWN0aW9uKC1zbG9wZUEsIHNsb3BlUHJpbWUsIDEpO1xyXG4gICAgICAgIGNhcmRpbmFsRGlyZWN0aW9uQiA9IElHZW9tZXRyeS5nZXRDYXJkaW5hbERpcmVjdGlvbigtc2xvcGVCLCBzbG9wZVByaW1lLCAzKTtcclxuICAgICAgfVxyXG4gICAgICBlbHNlXHJcbiAgICAgIHtcclxuICAgICAgICBjYXJkaW5hbERpcmVjdGlvbkEgPSBJR2VvbWV0cnkuZ2V0Q2FyZGluYWxEaXJlY3Rpb24oc2xvcGVBLCBzbG9wZVByaW1lLCAyKTtcclxuICAgICAgICBjYXJkaW5hbERpcmVjdGlvbkIgPSBJR2VvbWV0cnkuZ2V0Q2FyZGluYWxEaXJlY3Rpb24oc2xvcGVCLCBzbG9wZVByaW1lLCA0KTtcclxuICAgICAgfVxyXG4gICAgfVxyXG4gICAgLy9jYWxjdWxhdGUgY2xpcHBpbmcgUG9pbnQgaWYgaXQgaXMgbm90IGZvdW5kIGJlZm9yZVxyXG4gICAgaWYgKCFjbGlwUG9pbnRBRm91bmQpXHJcbiAgICB7XHJcbiAgICAgIHN3aXRjaCAoY2FyZGluYWxEaXJlY3Rpb25BKVxyXG4gICAgICB7XHJcbiAgICAgICAgY2FzZSAxOlxyXG4gICAgICAgICAgdGVtcFBvaW50QXkgPSB0b3BMZWZ0QXk7XHJcbiAgICAgICAgICB0ZW1wUG9pbnRBeCA9IHAxeCArICgtaGFsZkhlaWdodEEpIC8gc2xvcGVQcmltZTtcclxuICAgICAgICAgIHJlc3VsdFswXSA9IHRlbXBQb2ludEF4O1xyXG4gICAgICAgICAgcmVzdWx0WzFdID0gdGVtcFBvaW50QXk7XHJcbiAgICAgICAgICBicmVhaztcclxuICAgICAgICBjYXNlIDI6XHJcbiAgICAgICAgICB0ZW1wUG9pbnRBeCA9IGJvdHRvbVJpZ2h0QXg7XHJcbiAgICAgICAgICB0ZW1wUG9pbnRBeSA9IHAxeSArIGhhbGZXaWR0aEEgKiBzbG9wZVByaW1lO1xyXG4gICAgICAgICAgcmVzdWx0WzBdID0gdGVtcFBvaW50QXg7XHJcbiAgICAgICAgICByZXN1bHRbMV0gPSB0ZW1wUG9pbnRBeTtcclxuICAgICAgICAgIGJyZWFrO1xyXG4gICAgICAgIGNhc2UgMzpcclxuICAgICAgICAgIHRlbXBQb2ludEF5ID0gYm90dG9tTGVmdEF5O1xyXG4gICAgICAgICAgdGVtcFBvaW50QXggPSBwMXggKyBoYWxmSGVpZ2h0QSAvIHNsb3BlUHJpbWU7XHJcbiAgICAgICAgICByZXN1bHRbMF0gPSB0ZW1wUG9pbnRBeDtcclxuICAgICAgICAgIHJlc3VsdFsxXSA9IHRlbXBQb2ludEF5O1xyXG4gICAgICAgICAgYnJlYWs7XHJcbiAgICAgICAgY2FzZSA0OlxyXG4gICAgICAgICAgdGVtcFBvaW50QXggPSBib3R0b21MZWZ0QXg7XHJcbiAgICAgICAgICB0ZW1wUG9pbnRBeSA9IHAxeSArICgtaGFsZldpZHRoQSkgKiBzbG9wZVByaW1lO1xyXG4gICAgICAgICAgcmVzdWx0WzBdID0gdGVtcFBvaW50QXg7XHJcbiAgICAgICAgICByZXN1bHRbMV0gPSB0ZW1wUG9pbnRBeTtcclxuICAgICAgICAgIGJyZWFrO1xyXG4gICAgICB9XHJcbiAgICB9XHJcbiAgICBpZiAoIWNsaXBQb2ludEJGb3VuZClcclxuICAgIHtcclxuICAgICAgc3dpdGNoIChjYXJkaW5hbERpcmVjdGlvbkIpXHJcbiAgICAgIHtcclxuICAgICAgICBjYXNlIDE6XHJcbiAgICAgICAgICB0ZW1wUG9pbnRCeSA9IHRvcExlZnRCeTtcclxuICAgICAgICAgIHRlbXBQb2ludEJ4ID0gcDJ4ICsgKC1oYWxmSGVpZ2h0QikgLyBzbG9wZVByaW1lO1xyXG4gICAgICAgICAgcmVzdWx0WzJdID0gdGVtcFBvaW50Qng7XHJcbiAgICAgICAgICByZXN1bHRbM10gPSB0ZW1wUG9pbnRCeTtcclxuICAgICAgICAgIGJyZWFrO1xyXG4gICAgICAgIGNhc2UgMjpcclxuICAgICAgICAgIHRlbXBQb2ludEJ4ID0gYm90dG9tUmlnaHRCeDtcclxuICAgICAgICAgIHRlbXBQb2ludEJ5ID0gcDJ5ICsgaGFsZldpZHRoQiAqIHNsb3BlUHJpbWU7XHJcbiAgICAgICAgICByZXN1bHRbMl0gPSB0ZW1wUG9pbnRCeDtcclxuICAgICAgICAgIHJlc3VsdFszXSA9IHRlbXBQb2ludEJ5O1xyXG4gICAgICAgICAgYnJlYWs7XHJcbiAgICAgICAgY2FzZSAzOlxyXG4gICAgICAgICAgdGVtcFBvaW50QnkgPSBib3R0b21MZWZ0Qnk7XHJcbiAgICAgICAgICB0ZW1wUG9pbnRCeCA9IHAyeCArIGhhbGZIZWlnaHRCIC8gc2xvcGVQcmltZTtcclxuICAgICAgICAgIHJlc3VsdFsyXSA9IHRlbXBQb2ludEJ4O1xyXG4gICAgICAgICAgcmVzdWx0WzNdID0gdGVtcFBvaW50Qnk7XHJcbiAgICAgICAgICBicmVhaztcclxuICAgICAgICBjYXNlIDQ6XHJcbiAgICAgICAgICB0ZW1wUG9pbnRCeCA9IGJvdHRvbUxlZnRCeDtcclxuICAgICAgICAgIHRlbXBQb2ludEJ5ID0gcDJ5ICsgKC1oYWxmV2lkdGhCKSAqIHNsb3BlUHJpbWU7XHJcbiAgICAgICAgICByZXN1bHRbMl0gPSB0ZW1wUG9pbnRCeDtcclxuICAgICAgICAgIHJlc3VsdFszXSA9IHRlbXBQb2ludEJ5O1xyXG4gICAgICAgICAgYnJlYWs7XHJcbiAgICAgIH1cclxuICAgIH1cclxuICB9XHJcbiAgcmV0dXJuIGZhbHNlO1xyXG59XHJcblxyXG5JR2VvbWV0cnkuZ2V0Q2FyZGluYWxEaXJlY3Rpb24gPSBmdW5jdGlvbiAoc2xvcGUsIHNsb3BlUHJpbWUsIGxpbmUpXHJcbntcclxuICBpZiAoc2xvcGUgPiBzbG9wZVByaW1lKVxyXG4gIHtcclxuICAgIHJldHVybiBsaW5lO1xyXG4gIH1cclxuICBlbHNlXHJcbiAge1xyXG4gICAgcmV0dXJuIDEgKyBsaW5lICUgNDtcclxuICB9XHJcbn1cclxuXHJcbklHZW9tZXRyeS5nZXRJbnRlcnNlY3Rpb24gPSBmdW5jdGlvbiAoczEsIHMyLCBmMSwgZjIpXHJcbntcclxuICBpZiAoZjIgPT0gbnVsbCkge1xyXG4gICAgcmV0dXJuIElHZW9tZXRyeS5nZXRJbnRlcnNlY3Rpb24yKHMxLCBzMiwgZjEpO1xyXG4gIH1cclxuICB2YXIgeDEgPSBzMS54O1xyXG4gIHZhciB5MSA9IHMxLnk7XHJcbiAgdmFyIHgyID0gczIueDtcclxuICB2YXIgeTIgPSBzMi55O1xyXG4gIHZhciB4MyA9IGYxLng7XHJcbiAgdmFyIHkzID0gZjEueTtcclxuICB2YXIgeDQgPSBmMi54O1xyXG4gIHZhciB5NCA9IGYyLnk7XHJcbiAgdmFyIHgsIHk7IC8vIGludGVyc2VjdGlvbiBwb2ludFxyXG4gIHZhciBhMSwgYTIsIGIxLCBiMiwgYzEsIGMyOyAvLyBjb2VmZmljaWVudHMgb2YgbGluZSBlcW5zLlxyXG4gIHZhciBkZW5vbTtcclxuXHJcbiAgYTEgPSB5MiAtIHkxO1xyXG4gIGIxID0geDEgLSB4MjtcclxuICBjMSA9IHgyICogeTEgLSB4MSAqIHkyOyAgLy8geyBhMSp4ICsgYjEqeSArIGMxID0gMCBpcyBsaW5lIDEgfVxyXG5cclxuICBhMiA9IHk0IC0geTM7XHJcbiAgYjIgPSB4MyAtIHg0O1xyXG4gIGMyID0geDQgKiB5MyAtIHgzICogeTQ7ICAvLyB7IGEyKnggKyBiMip5ICsgYzIgPSAwIGlzIGxpbmUgMiB9XHJcblxyXG4gIGRlbm9tID0gYTEgKiBiMiAtIGEyICogYjE7XHJcblxyXG4gIGlmIChkZW5vbSA9PSAwKVxyXG4gIHtcclxuICAgIHJldHVybiBudWxsO1xyXG4gIH1cclxuXHJcbiAgeCA9IChiMSAqIGMyIC0gYjIgKiBjMSkgLyBkZW5vbTtcclxuICB5ID0gKGEyICogYzEgLSBhMSAqIGMyKSAvIGRlbm9tO1xyXG5cclxuICByZXR1cm4gbmV3IFBvaW50KHgsIHkpO1xyXG59XHJcblxyXG4vLyAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4vLyBTZWN0aW9uOiBDbGFzcyBDb25zdGFudHNcclxuLy8gLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuLyoqXHJcbiAqIFNvbWUgdXNlZnVsIHByZS1jYWxjdWxhdGVkIGNvbnN0YW50c1xyXG4gKi9cclxuSUdlb21ldHJ5LkhBTEZfUEkgPSAwLjUgKiBNYXRoLlBJO1xyXG5JR2VvbWV0cnkuT05FX0FORF9IQUxGX1BJID0gMS41ICogTWF0aC5QSTtcclxuSUdlb21ldHJ5LlRXT19QSSA9IDIuMCAqIE1hdGguUEk7XHJcbklHZW9tZXRyeS5USFJFRV9QSSA9IDMuMCAqIE1hdGguUEk7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IElHZW9tZXRyeTtcclxuIiwiZnVuY3Rpb24gSU1hdGgoKSB7XHJcbn1cclxuXHJcbi8qKlxyXG4gKiBUaGlzIG1ldGhvZCByZXR1cm5zIHRoZSBzaWduIG9mIHRoZSBpbnB1dCB2YWx1ZS5cclxuICovXHJcbklNYXRoLnNpZ24gPSBmdW5jdGlvbiAodmFsdWUpIHtcclxuICBpZiAodmFsdWUgPiAwKVxyXG4gIHtcclxuICAgIHJldHVybiAxO1xyXG4gIH1cclxuICBlbHNlIGlmICh2YWx1ZSA8IDApXHJcbiAge1xyXG4gICAgcmV0dXJuIC0xO1xyXG4gIH1cclxuICBlbHNlXHJcbiAge1xyXG4gICAgcmV0dXJuIDA7XHJcbiAgfVxyXG59XHJcblxyXG5JTWF0aC5mbG9vciA9IGZ1bmN0aW9uICh2YWx1ZSkge1xyXG4gIHJldHVybiB2YWx1ZSA8IDAgPyBNYXRoLmNlaWwodmFsdWUpIDogTWF0aC5mbG9vcih2YWx1ZSk7XHJcbn1cclxuXHJcbklNYXRoLmNlaWwgPSBmdW5jdGlvbiAodmFsdWUpIHtcclxuICByZXR1cm4gdmFsdWUgPCAwID8gTWF0aC5mbG9vcih2YWx1ZSkgOiBNYXRoLmNlaWwodmFsdWUpO1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IElNYXRoO1xyXG4iLCJmdW5jdGlvbiBJbnRlZ2VyKCkge1xyXG59XHJcblxyXG5JbnRlZ2VyLk1BWF9WQUxVRSA9IDIxNDc0ODM2NDc7XHJcbkludGVnZXIuTUlOX1ZBTFVFID0gLTIxNDc0ODM2NDg7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IEludGVnZXI7XHJcbiIsInZhciBMR3JhcGhPYmplY3QgPSByZXF1aXJlKCcuL0xHcmFwaE9iamVjdCcpO1xyXG5cclxuZnVuY3Rpb24gTEVkZ2Uoc291cmNlLCB0YXJnZXQsIHZFZGdlKSB7XHJcbiAgTEdyYXBoT2JqZWN0LmNhbGwodGhpcywgdkVkZ2UpO1xyXG5cclxuICB0aGlzLmlzT3ZlcmxhcGluZ1NvdXJjZUFuZFRhcmdldCA9IGZhbHNlO1xyXG4gIHRoaXMudkdyYXBoT2JqZWN0ID0gdkVkZ2U7XHJcbiAgdGhpcy5iZW5kcG9pbnRzID0gW107XHJcbiAgdGhpcy5zb3VyY2UgPSBzb3VyY2U7XHJcbiAgdGhpcy50YXJnZXQgPSB0YXJnZXQ7XHJcbn1cclxuXHJcbkxFZGdlLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoTEdyYXBoT2JqZWN0LnByb3RvdHlwZSk7XHJcblxyXG5mb3IgKHZhciBwcm9wIGluIExHcmFwaE9iamVjdCkge1xyXG4gIExFZGdlW3Byb3BdID0gTEdyYXBoT2JqZWN0W3Byb3BdO1xyXG59XHJcblxyXG5MRWRnZS5wcm90b3R5cGUuZ2V0U291cmNlID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnNvdXJjZTtcclxufTtcclxuXHJcbkxFZGdlLnByb3RvdHlwZS5nZXRUYXJnZXQgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMudGFyZ2V0O1xyXG59O1xyXG5cclxuTEVkZ2UucHJvdG90eXBlLmlzSW50ZXJHcmFwaCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5pc0ludGVyR3JhcGg7XHJcbn07XHJcblxyXG5MRWRnZS5wcm90b3R5cGUuZ2V0TGVuZ3RoID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxlbmd0aDtcclxufTtcclxuXHJcbkxFZGdlLnByb3RvdHlwZS5pc092ZXJsYXBpbmdTb3VyY2VBbmRUYXJnZXQgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuaXNPdmVybGFwaW5nU291cmNlQW5kVGFyZ2V0O1xyXG59O1xyXG5cclxuTEVkZ2UucHJvdG90eXBlLmdldEJlbmRwb2ludHMgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuYmVuZHBvaW50cztcclxufTtcclxuXHJcbkxFZGdlLnByb3RvdHlwZS5nZXRMY2EgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMubGNhO1xyXG59O1xyXG5cclxuTEVkZ2UucHJvdG90eXBlLmdldFNvdXJjZUluTGNhID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnNvdXJjZUluTGNhO1xyXG59O1xyXG5cclxuTEVkZ2UucHJvdG90eXBlLmdldFRhcmdldEluTGNhID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnRhcmdldEluTGNhO1xyXG59O1xyXG5cclxuTEVkZ2UucHJvdG90eXBlLmdldE90aGVyRW5kID0gZnVuY3Rpb24gKG5vZGUpXHJcbntcclxuICBpZiAodGhpcy5zb3VyY2UgPT09IG5vZGUpXHJcbiAge1xyXG4gICAgcmV0dXJuIHRoaXMudGFyZ2V0O1xyXG4gIH1cclxuICBlbHNlIGlmICh0aGlzLnRhcmdldCA9PT0gbm9kZSlcclxuICB7XHJcbiAgICByZXR1cm4gdGhpcy5zb3VyY2U7XHJcbiAgfVxyXG4gIGVsc2VcclxuICB7XHJcbiAgICB0aHJvdyBcIk5vZGUgaXMgbm90IGluY2lkZW50IHdpdGggdGhpcyBlZGdlXCI7XHJcbiAgfVxyXG59XHJcblxyXG5MRWRnZS5wcm90b3R5cGUuZ2V0T3RoZXJFbmRJbkdyYXBoID0gZnVuY3Rpb24gKG5vZGUsIGdyYXBoKVxyXG57XHJcbiAgdmFyIG90aGVyRW5kID0gdGhpcy5nZXRPdGhlckVuZChub2RlKTtcclxuICB2YXIgcm9vdCA9IGdyYXBoLmdldEdyYXBoTWFuYWdlcigpLmdldFJvb3QoKTtcclxuXHJcbiAgd2hpbGUgKHRydWUpXHJcbiAge1xyXG4gICAgaWYgKG90aGVyRW5kLmdldE93bmVyKCkgPT0gZ3JhcGgpXHJcbiAgICB7XHJcbiAgICAgIHJldHVybiBvdGhlckVuZDtcclxuICAgIH1cclxuXHJcbiAgICBpZiAob3RoZXJFbmQuZ2V0T3duZXIoKSA9PSByb290KVxyXG4gICAge1xyXG4gICAgICBicmVhaztcclxuICAgIH1cclxuXHJcbiAgICBvdGhlckVuZCA9IG90aGVyRW5kLmdldE93bmVyKCkuZ2V0UGFyZW50KCk7XHJcbiAgfVxyXG5cclxuICByZXR1cm4gbnVsbDtcclxufTtcclxuXHJcbkxFZGdlLnByb3RvdHlwZS51cGRhdGVMZW5ndGggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdmFyIGNsaXBQb2ludENvb3JkaW5hdGVzID0gbmV3IEFycmF5KDQpO1xyXG5cclxuICB0aGlzLmlzT3ZlcmxhcGluZ1NvdXJjZUFuZFRhcmdldCA9XHJcbiAgICAgICAgICBJR2VvbWV0cnkuZ2V0SW50ZXJzZWN0aW9uKHRoaXMudGFyZ2V0LmdldFJlY3QoKSxcclxuICAgICAgICAgICAgICAgICAgdGhpcy5zb3VyY2UuZ2V0UmVjdCgpLFxyXG4gICAgICAgICAgICAgICAgICBjbGlwUG9pbnRDb29yZGluYXRlcyk7XHJcblxyXG4gIGlmICghdGhpcy5pc092ZXJsYXBpbmdTb3VyY2VBbmRUYXJnZXQpXHJcbiAge1xyXG4gICAgdGhpcy5sZW5ndGhYID0gY2xpcFBvaW50Q29vcmRpbmF0ZXNbMF0gLSBjbGlwUG9pbnRDb29yZGluYXRlc1syXTtcclxuICAgIHRoaXMubGVuZ3RoWSA9IGNsaXBQb2ludENvb3JkaW5hdGVzWzFdIC0gY2xpcFBvaW50Q29vcmRpbmF0ZXNbM107XHJcblxyXG4gICAgaWYgKE1hdGguYWJzKHRoaXMubGVuZ3RoWCkgPCAxLjApXHJcbiAgICB7XHJcbiAgICAgIHRoaXMubGVuZ3RoWCA9IElNYXRoLnNpZ24odGhpcy5sZW5ndGhYKTtcclxuICAgIH1cclxuXHJcbiAgICBpZiAoTWF0aC5hYnModGhpcy5sZW5ndGhZKSA8IDEuMClcclxuICAgIHtcclxuICAgICAgdGhpcy5sZW5ndGhZID0gSU1hdGguc2lnbih0aGlzLmxlbmd0aFkpO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMubGVuZ3RoID0gTWF0aC5zcXJ0KFxyXG4gICAgICAgICAgICB0aGlzLmxlbmd0aFggKiB0aGlzLmxlbmd0aFggKyB0aGlzLmxlbmd0aFkgKiB0aGlzLmxlbmd0aFkpO1xyXG4gIH1cclxufTtcclxuXHJcbkxFZGdlLnByb3RvdHlwZS51cGRhdGVMZW5ndGhTaW1wbGUgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdGhpcy5sZW5ndGhYID0gdGhpcy50YXJnZXQuZ2V0Q2VudGVyWCgpIC0gdGhpcy5zb3VyY2UuZ2V0Q2VudGVyWCgpO1xyXG4gIHRoaXMubGVuZ3RoWSA9IHRoaXMudGFyZ2V0LmdldENlbnRlclkoKSAtIHRoaXMuc291cmNlLmdldENlbnRlclkoKTtcclxuXHJcbiAgaWYgKE1hdGguYWJzKHRoaXMubGVuZ3RoWCkgPCAxLjApXHJcbiAge1xyXG4gICAgdGhpcy5sZW5ndGhYID0gSU1hdGguc2lnbih0aGlzLmxlbmd0aFgpO1xyXG4gIH1cclxuXHJcbiAgaWYgKE1hdGguYWJzKHRoaXMubGVuZ3RoWSkgPCAxLjApXHJcbiAge1xyXG4gICAgdGhpcy5sZW5ndGhZID0gSU1hdGguc2lnbih0aGlzLmxlbmd0aFkpO1xyXG4gIH1cclxuXHJcbiAgdGhpcy5sZW5ndGggPSBNYXRoLnNxcnQoXHJcbiAgICAgICAgICB0aGlzLmxlbmd0aFggKiB0aGlzLmxlbmd0aFggKyB0aGlzLmxlbmd0aFkgKiB0aGlzLmxlbmd0aFkpO1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IExFZGdlO1xyXG4iLCJ2YXIgTEdyYXBoT2JqZWN0ID0gcmVxdWlyZSgnLi9MR3JhcGhPYmplY3QnKTtcclxudmFyIEludGVnZXIgPSByZXF1aXJlKCcuL0ludGVnZXInKTtcclxudmFyIExheW91dENvbnN0YW50cyA9IHJlcXVpcmUoJy4vTGF5b3V0Q29uc3RhbnRzJyk7XHJcbnZhciBMR3JhcGhNYW5hZ2VyID0gcmVxdWlyZSgnLi9MR3JhcGhNYW5hZ2VyJyk7XHJcbnZhciBMTm9kZSA9IHJlcXVpcmUoJy4vTE5vZGUnKTtcclxuXHJcbmZ1bmN0aW9uIExHcmFwaChwYXJlbnQsIG9iajIsIHZHcmFwaCkge1xyXG4gIExHcmFwaE9iamVjdC5jYWxsKHRoaXMsIHZHcmFwaCk7XHJcbiAgdGhpcy5lc3RpbWF0ZWRTaXplID0gSW50ZWdlci5NSU5fVkFMVUU7XHJcbiAgdGhpcy5tYXJnaW4gPSBMYXlvdXRDb25zdGFudHMuREVGQVVMVF9HUkFQSF9NQVJHSU47XHJcbiAgdGhpcy5lZGdlcyA9IFtdO1xyXG4gIHRoaXMubm9kZXMgPSBbXTtcclxuICB0aGlzLmlzQ29ubmVjdGVkID0gZmFsc2U7XHJcbiAgdGhpcy5wYXJlbnQgPSBwYXJlbnQ7XHJcblxyXG4gIGlmIChvYmoyICE9IG51bGwgJiYgb2JqMiBpbnN0YW5jZW9mIExHcmFwaE1hbmFnZXIpIHtcclxuICAgIHRoaXMuZ3JhcGhNYW5hZ2VyID0gb2JqMjtcclxuICB9XHJcbiAgZWxzZSBpZiAob2JqMiAhPSBudWxsICYmIG9iajIgaW5zdGFuY2VvZiBMYXlvdXQpIHtcclxuICAgIHRoaXMuZ3JhcGhNYW5hZ2VyID0gb2JqMi5ncmFwaE1hbmFnZXI7XHJcbiAgfVxyXG59XHJcblxyXG5MR3JhcGgucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShMR3JhcGhPYmplY3QucHJvdG90eXBlKTtcclxuZm9yICh2YXIgcHJvcCBpbiBMR3JhcGhPYmplY3QpIHtcclxuICBMR3JhcGhbcHJvcF0gPSBMR3JhcGhPYmplY3RbcHJvcF07XHJcbn1cclxuXHJcbkxHcmFwaC5wcm90b3R5cGUuZ2V0Tm9kZXMgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMubm9kZXM7XHJcbn07XHJcblxyXG5MR3JhcGgucHJvdG90eXBlLmdldEVkZ2VzID0gZnVuY3Rpb24gKCkge1xyXG4gIHJldHVybiB0aGlzLmVkZ2VzO1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5nZXRHcmFwaE1hbmFnZXIgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuZ3JhcGhNYW5hZ2VyO1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5nZXRQYXJlbnQgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMucGFyZW50O1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5nZXRMZWZ0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxlZnQ7XHJcbn07XHJcblxyXG5MR3JhcGgucHJvdG90eXBlLmdldFJpZ2h0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnJpZ2h0O1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5nZXRUb3AgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMudG9wO1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5nZXRCb3R0b20gPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuYm90dG9tO1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5pc0Nvbm5lY3RlZCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5pc0Nvbm5lY3RlZDtcclxufTtcclxuXHJcbkxHcmFwaC5wcm90b3R5cGUuYWRkID0gZnVuY3Rpb24gKG9iajEsIHNvdXJjZU5vZGUsIHRhcmdldE5vZGUpIHtcclxuICBpZiAoc291cmNlTm9kZSA9PSBudWxsICYmIHRhcmdldE5vZGUgPT0gbnVsbCkge1xyXG4gICAgdmFyIG5ld05vZGUgPSBvYmoxO1xyXG4gICAgaWYgKHRoaXMuZ3JhcGhNYW5hZ2VyID09IG51bGwpIHtcclxuICAgICAgdGhyb3cgXCJHcmFwaCBoYXMgbm8gZ3JhcGggbWdyIVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKHRoaXMuZ2V0Tm9kZXMoKS5pbmRleE9mKG5ld05vZGUpID4gLTEpIHtcclxuICAgICAgdGhyb3cgXCJOb2RlIGFscmVhZHkgaW4gZ3JhcGghXCI7XHJcbiAgICB9XHJcbiAgICBuZXdOb2RlLm93bmVyID0gdGhpcztcclxuICAgIHRoaXMuZ2V0Tm9kZXMoKS5wdXNoKG5ld05vZGUpO1xyXG5cclxuICAgIHJldHVybiBuZXdOb2RlO1xyXG4gIH1cclxuICBlbHNlIHtcclxuICAgIHZhciBuZXdFZGdlID0gb2JqMTtcclxuICAgIGlmICghKHRoaXMuZ2V0Tm9kZXMoKS5pbmRleE9mKHNvdXJjZU5vZGUpID4gLTEgJiYgKHRoaXMuZ2V0Tm9kZXMoKS5pbmRleE9mKHRhcmdldE5vZGUpKSA+IC0xKSkge1xyXG4gICAgICB0aHJvdyBcIlNvdXJjZSBvciB0YXJnZXQgbm90IGluIGdyYXBoIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIGlmICghKHNvdXJjZU5vZGUub3duZXIgPT0gdGFyZ2V0Tm9kZS5vd25lciAmJiBzb3VyY2VOb2RlLm93bmVyID09IHRoaXMpKSB7XHJcbiAgICAgIHRocm93IFwiQm90aCBvd25lcnMgbXVzdCBiZSB0aGlzIGdyYXBoIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChzb3VyY2VOb2RlLm93bmVyICE9IHRhcmdldE5vZGUub3duZXIpXHJcbiAgICB7XHJcbiAgICAgIHJldHVybiBudWxsO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIHNldCBzb3VyY2UgYW5kIHRhcmdldFxyXG4gICAgbmV3RWRnZS5zb3VyY2UgPSBzb3VyY2VOb2RlO1xyXG4gICAgbmV3RWRnZS50YXJnZXQgPSB0YXJnZXROb2RlO1xyXG5cclxuICAgIC8vIHNldCBhcyBpbnRyYS1ncmFwaCBlZGdlXHJcbiAgICBuZXdFZGdlLmlzSW50ZXJHcmFwaCA9IGZhbHNlO1xyXG5cclxuICAgIC8vIGFkZCB0byBncmFwaCBlZGdlIGxpc3RcclxuICAgIHRoaXMuZ2V0RWRnZXMoKS5wdXNoKG5ld0VkZ2UpO1xyXG5cclxuICAgIC8vIGFkZCB0byBpbmNpZGVuY3kgbGlzdHNcclxuICAgIHNvdXJjZU5vZGUuZWRnZXMucHVzaChuZXdFZGdlKTtcclxuXHJcbiAgICBpZiAodGFyZ2V0Tm9kZSAhPSBzb3VyY2VOb2RlKVxyXG4gICAge1xyXG4gICAgICB0YXJnZXROb2RlLmVkZ2VzLnB1c2gobmV3RWRnZSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV0dXJuIG5ld0VkZ2U7XHJcbiAgfVxyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS5yZW1vdmUgPSBmdW5jdGlvbiAob2JqKSB7XHJcbiAgdmFyIG5vZGUgPSBvYmo7XHJcbiAgaWYgKG9iaiBpbnN0YW5jZW9mIExOb2RlKSB7XHJcbiAgICBpZiAobm9kZSA9PSBudWxsKSB7XHJcbiAgICAgIHRocm93IFwiTm9kZSBpcyBudWxsIVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKCEobm9kZS5vd25lciAhPSBudWxsICYmIG5vZGUub3duZXIgPT0gdGhpcykpIHtcclxuICAgICAgdGhyb3cgXCJPd25lciBncmFwaCBpcyBpbnZhbGlkIVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKHRoaXMuZ3JhcGhNYW5hZ2VyID09IG51bGwpIHtcclxuICAgICAgdGhyb3cgXCJPd25lciBncmFwaCBtYW5hZ2VyIGlzIGludmFsaWQhXCI7XHJcbiAgICB9XHJcbiAgICAvLyByZW1vdmUgaW5jaWRlbnQgZWRnZXMgZmlyc3QgKG1ha2UgYSBjb3B5IHRvIGRvIGl0IHNhZmVseSlcclxuICAgIHZhciBlZGdlc1RvQmVSZW1vdmVkID0gbm9kZS5lZGdlcy5zbGljZSgpO1xyXG4gICAgdmFyIGVkZ2U7XHJcbiAgICB2YXIgcyA9IGVkZ2VzVG9CZVJlbW92ZWQubGVuZ3RoO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBzOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIGVkZ2UgPSBlZGdlc1RvQmVSZW1vdmVkW2ldO1xyXG5cclxuICAgICAgaWYgKGVkZ2UuaXNJbnRlckdyYXBoKVxyXG4gICAgICB7XHJcbiAgICAgICAgdGhpcy5ncmFwaE1hbmFnZXIucmVtb3ZlKGVkZ2UpO1xyXG4gICAgICB9XHJcbiAgICAgIGVsc2VcclxuICAgICAge1xyXG4gICAgICAgIGVkZ2Uuc291cmNlLm93bmVyLnJlbW92ZShlZGdlKTtcclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8vIG5vdyB0aGUgbm9kZSBpdHNlbGZcclxuICAgIHZhciBpbmRleCA9IHRoaXMubm9kZXMuaW5kZXhPZihub2RlKTtcclxuICAgIGlmIChpbmRleCA9PSAtMSkge1xyXG4gICAgICB0aHJvdyBcIk5vZGUgbm90IGluIG93bmVyIG5vZGUgbGlzdCFcIjtcclxuICAgIH1cclxuXHJcbiAgICB0aGlzLm5vZGVzLnNwbGljZShpbmRleCwgMSk7XHJcbiAgfVxyXG4gIGVsc2UgaWYgKG9iaiBpbnN0YW5jZW9mIExFZGdlKSB7XHJcbiAgICB2YXIgZWRnZSA9IG9iajtcclxuICAgIGlmIChlZGdlID09IG51bGwpIHtcclxuICAgICAgdGhyb3cgXCJFZGdlIGlzIG51bGwhXCI7XHJcbiAgICB9XHJcbiAgICBpZiAoIShlZGdlLnNvdXJjZSAhPSBudWxsICYmIGVkZ2UudGFyZ2V0ICE9IG51bGwpKSB7XHJcbiAgICAgIHRocm93IFwiU291cmNlIGFuZC9vciB0YXJnZXQgaXMgbnVsbCFcIjtcclxuICAgIH1cclxuICAgIGlmICghKGVkZ2Uuc291cmNlLm93bmVyICE9IG51bGwgJiYgZWRnZS50YXJnZXQub3duZXIgIT0gbnVsbCAmJlxyXG4gICAgICAgICAgICBlZGdlLnNvdXJjZS5vd25lciA9PSB0aGlzICYmIGVkZ2UudGFyZ2V0Lm93bmVyID09IHRoaXMpKSB7XHJcbiAgICAgIHRocm93IFwiU291cmNlIGFuZC9vciB0YXJnZXQgb3duZXIgaXMgaW52YWxpZCFcIjtcclxuICAgIH1cclxuXHJcbiAgICB2YXIgc291cmNlSW5kZXggPSBlZGdlLnNvdXJjZS5lZGdlcy5pbmRleE9mKGVkZ2UpO1xyXG4gICAgdmFyIHRhcmdldEluZGV4ID0gZWRnZS50YXJnZXQuZWRnZXMuaW5kZXhPZihlZGdlKTtcclxuICAgIGlmICghKHNvdXJjZUluZGV4ID4gLTEgJiYgdGFyZ2V0SW5kZXggPiAtMSkpIHtcclxuICAgICAgdGhyb3cgXCJTb3VyY2UgYW5kL29yIHRhcmdldCBkb2Vzbid0IGtub3cgdGhpcyBlZGdlIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIGVkZ2Uuc291cmNlLmVkZ2VzLnNwbGljZShzb3VyY2VJbmRleCwgMSk7XHJcblxyXG4gICAgaWYgKGVkZ2UudGFyZ2V0ICE9IGVkZ2Uuc291cmNlKVxyXG4gICAge1xyXG4gICAgICBlZGdlLnRhcmdldC5lZGdlcy5zcGxpY2UodGFyZ2V0SW5kZXgsIDEpO1xyXG4gICAgfVxyXG5cclxuICAgIHZhciBpbmRleCA9IGVkZ2Uuc291cmNlLm93bmVyLmdldEVkZ2VzKCkuaW5kZXhPZihlZGdlKTtcclxuICAgIGlmIChpbmRleCA9PSAtMSkge1xyXG4gICAgICB0aHJvdyBcIk5vdCBpbiBvd25lcidzIGVkZ2UgbGlzdCFcIjtcclxuICAgIH1cclxuXHJcbiAgICBlZGdlLnNvdXJjZS5vd25lci5nZXRFZGdlcygpLnNwbGljZShpbmRleCwgMSk7XHJcbiAgfVxyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS51cGRhdGVMZWZ0VG9wID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHZhciB0b3AgPSBJbnRlZ2VyLk1BWF9WQUxVRTtcclxuICB2YXIgbGVmdCA9IEludGVnZXIuTUFYX1ZBTFVFO1xyXG4gIHZhciBub2RlVG9wO1xyXG4gIHZhciBub2RlTGVmdDtcclxuXHJcbiAgdmFyIG5vZGVzID0gdGhpcy5nZXROb2RlcygpO1xyXG4gIHZhciBzID0gbm9kZXMubGVuZ3RoO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHM7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbE5vZGUgPSBub2Rlc1tpXTtcclxuICAgIG5vZGVUb3AgPSBNYXRoLmZsb29yKGxOb2RlLmdldFRvcCgpKTtcclxuICAgIG5vZGVMZWZ0ID0gTWF0aC5mbG9vcihsTm9kZS5nZXRMZWZ0KCkpO1xyXG5cclxuICAgIGlmICh0b3AgPiBub2RlVG9wKVxyXG4gICAge1xyXG4gICAgICB0b3AgPSBub2RlVG9wO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChsZWZ0ID4gbm9kZUxlZnQpXHJcbiAgICB7XHJcbiAgICAgIGxlZnQgPSBub2RlTGVmdDtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIC8vIERvIHdlIGhhdmUgYW55IG5vZGVzIGluIHRoaXMgZ3JhcGg/XHJcbiAgaWYgKHRvcCA9PSBJbnRlZ2VyLk1BWF9WQUxVRSlcclxuICB7XHJcbiAgICByZXR1cm4gbnVsbDtcclxuICB9XHJcblxyXG4gIHRoaXMubGVmdCA9IGxlZnQgLSB0aGlzLm1hcmdpbjtcclxuICB0aGlzLnRvcCA9IHRvcCAtIHRoaXMubWFyZ2luO1xyXG5cclxuICAvLyBBcHBseSB0aGUgbWFyZ2lucyBhbmQgcmV0dXJuIHRoZSByZXN1bHRcclxuICByZXR1cm4gbmV3IFBvaW50KHRoaXMubGVmdCwgdGhpcy50b3ApO1xyXG59O1xyXG5cclxuTEdyYXBoLnByb3RvdHlwZS51cGRhdGVCb3VuZHMgPSBmdW5jdGlvbiAocmVjdXJzaXZlKVxyXG57XHJcbiAgLy8gY2FsY3VsYXRlIGJvdW5kc1xyXG4gIHZhciBsZWZ0ID0gSW50ZWdlci5NQVhfVkFMVUU7XHJcbiAgdmFyIHJpZ2h0ID0gLUludGVnZXIuTUFYX1ZBTFVFO1xyXG4gIHZhciB0b3AgPSBJbnRlZ2VyLk1BWF9WQUxVRTtcclxuICB2YXIgYm90dG9tID0gLUludGVnZXIuTUFYX1ZBTFVFO1xyXG4gIHZhciBub2RlTGVmdDtcclxuICB2YXIgbm9kZVJpZ2h0O1xyXG4gIHZhciBub2RlVG9wO1xyXG4gIHZhciBub2RlQm90dG9tO1xyXG5cclxuICB2YXIgbm9kZXMgPSB0aGlzLm5vZGVzO1xyXG4gIHZhciBzID0gbm9kZXMubGVuZ3RoO1xyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgczsgaSsrKVxyXG4gIHtcclxuICAgIHZhciBsTm9kZSA9IG5vZGVzW2ldO1xyXG5cclxuICAgIGlmIChyZWN1cnNpdmUgJiYgbE5vZGUuY2hpbGQgIT0gbnVsbClcclxuICAgIHtcclxuICAgICAgbE5vZGUudXBkYXRlQm91bmRzKCk7XHJcbiAgICB9XHJcbiAgICBub2RlTGVmdCA9IE1hdGguZmxvb3IobE5vZGUuZ2V0TGVmdCgpKTtcclxuICAgIG5vZGVSaWdodCA9IE1hdGguZmxvb3IobE5vZGUuZ2V0UmlnaHQoKSk7XHJcbiAgICBub2RlVG9wID0gTWF0aC5mbG9vcihsTm9kZS5nZXRUb3AoKSk7XHJcbiAgICBub2RlQm90dG9tID0gTWF0aC5mbG9vcihsTm9kZS5nZXRCb3R0b20oKSk7XHJcblxyXG4gICAgaWYgKGxlZnQgPiBub2RlTGVmdClcclxuICAgIHtcclxuICAgICAgbGVmdCA9IG5vZGVMZWZ0O1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChyaWdodCA8IG5vZGVSaWdodClcclxuICAgIHtcclxuICAgICAgcmlnaHQgPSBub2RlUmlnaHQ7XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKHRvcCA+IG5vZGVUb3ApXHJcbiAgICB7XHJcbiAgICAgIHRvcCA9IG5vZGVUb3A7XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKGJvdHRvbSA8IG5vZGVCb3R0b20pXHJcbiAgICB7XHJcbiAgICAgIGJvdHRvbSA9IG5vZGVCb3R0b207XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICB2YXIgYm91bmRpbmdSZWN0ID0gbmV3IFJlY3RhbmdsZUQobGVmdCwgdG9wLCByaWdodCAtIGxlZnQsIGJvdHRvbSAtIHRvcCk7XHJcbiAgaWYgKGxlZnQgPT0gSW50ZWdlci5NQVhfVkFMVUUpXHJcbiAge1xyXG4gICAgdGhpcy5sZWZ0ID0gTWF0aC5mbG9vcih0aGlzLnBhcmVudC5nZXRMZWZ0KCkpO1xyXG4gICAgdGhpcy5yaWdodCA9IE1hdGguZmxvb3IodGhpcy5wYXJlbnQuZ2V0UmlnaHQoKSk7XHJcbiAgICB0aGlzLnRvcCA9IE1hdGguZmxvb3IodGhpcy5wYXJlbnQuZ2V0VG9wKCkpO1xyXG4gICAgdGhpcy5ib3R0b20gPSBNYXRoLmZsb29yKHRoaXMucGFyZW50LmdldEJvdHRvbSgpKTtcclxuICB9XHJcblxyXG4gIHRoaXMubGVmdCA9IGJvdW5kaW5nUmVjdC54IC0gdGhpcy5tYXJnaW47XHJcbiAgdGhpcy5yaWdodCA9IGJvdW5kaW5nUmVjdC54ICsgYm91bmRpbmdSZWN0LndpZHRoICsgdGhpcy5tYXJnaW47XHJcbiAgdGhpcy50b3AgPSBib3VuZGluZ1JlY3QueSAtIHRoaXMubWFyZ2luO1xyXG4gIHRoaXMuYm90dG9tID0gYm91bmRpbmdSZWN0LnkgKyBib3VuZGluZ1JlY3QuaGVpZ2h0ICsgdGhpcy5tYXJnaW47XHJcbn07XHJcblxyXG5MR3JhcGguY2FsY3VsYXRlQm91bmRzID0gZnVuY3Rpb24gKG5vZGVzKVxyXG57XHJcbiAgdmFyIGxlZnQgPSBJbnRlZ2VyLk1BWF9WQUxVRTtcclxuICB2YXIgcmlnaHQgPSAtSW50ZWdlci5NQVhfVkFMVUU7XHJcbiAgdmFyIHRvcCA9IEludGVnZXIuTUFYX1ZBTFVFO1xyXG4gIHZhciBib3R0b20gPSAtSW50ZWdlci5NQVhfVkFMVUU7XHJcbiAgdmFyIG5vZGVMZWZ0O1xyXG4gIHZhciBub2RlUmlnaHQ7XHJcbiAgdmFyIG5vZGVUb3A7XHJcbiAgdmFyIG5vZGVCb3R0b207XHJcblxyXG4gIHZhciBzID0gbm9kZXMubGVuZ3RoO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHM7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbE5vZGUgPSBub2Rlc1tpXTtcclxuICAgIG5vZGVMZWZ0ID0gTWF0aC5mbG9vcihsTm9kZS5nZXRMZWZ0KCkpO1xyXG4gICAgbm9kZVJpZ2h0ID0gTWF0aC5mbG9vcihsTm9kZS5nZXRSaWdodCgpKTtcclxuICAgIG5vZGVUb3AgPSBNYXRoLmZsb29yKGxOb2RlLmdldFRvcCgpKTtcclxuICAgIG5vZGVCb3R0b20gPSBNYXRoLmZsb29yKGxOb2RlLmdldEJvdHRvbSgpKTtcclxuXHJcbiAgICBpZiAobGVmdCA+IG5vZGVMZWZ0KVxyXG4gICAge1xyXG4gICAgICBsZWZ0ID0gbm9kZUxlZnQ7XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKHJpZ2h0IDwgbm9kZVJpZ2h0KVxyXG4gICAge1xyXG4gICAgICByaWdodCA9IG5vZGVSaWdodDtcclxuICAgIH1cclxuXHJcbiAgICBpZiAodG9wID4gbm9kZVRvcClcclxuICAgIHtcclxuICAgICAgdG9wID0gbm9kZVRvcDtcclxuICAgIH1cclxuXHJcbiAgICBpZiAoYm90dG9tIDwgbm9kZUJvdHRvbSlcclxuICAgIHtcclxuICAgICAgYm90dG9tID0gbm9kZUJvdHRvbTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHZhciBib3VuZGluZ1JlY3QgPSBuZXcgUmVjdGFuZ2xlRChsZWZ0LCB0b3AsIHJpZ2h0IC0gbGVmdCwgYm90dG9tIC0gdG9wKTtcclxuXHJcbiAgcmV0dXJuIGJvdW5kaW5nUmVjdDtcclxufTtcclxuXHJcbkxHcmFwaC5wcm90b3R5cGUuZ2V0SW5jbHVzaW9uVHJlZURlcHRoID0gZnVuY3Rpb24gKClcclxue1xyXG4gIGlmICh0aGlzID09IHRoaXMuZ3JhcGhNYW5hZ2VyLmdldFJvb3QoKSlcclxuICB7XHJcbiAgICByZXR1cm4gMTtcclxuICB9XHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIHJldHVybiB0aGlzLnBhcmVudC5nZXRJbmNsdXNpb25UcmVlRGVwdGgoKTtcclxuICB9XHJcbn07XHJcblxyXG5MR3JhcGgucHJvdG90eXBlLmdldEVzdGltYXRlZFNpemUgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgaWYgKHRoaXMuZXN0aW1hdGVkU2l6ZSA9PSBJbnRlZ2VyLk1JTl9WQUxVRSkge1xyXG4gICAgdGhyb3cgXCJhc3NlcnQgZmFpbGVkXCI7XHJcbiAgfVxyXG4gIHJldHVybiB0aGlzLmVzdGltYXRlZFNpemU7XHJcbn07XHJcblxyXG5MR3JhcGgucHJvdG90eXBlLmNhbGNFc3RpbWF0ZWRTaXplID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHZhciBzaXplID0gMDtcclxuICB2YXIgbm9kZXMgPSB0aGlzLm5vZGVzO1xyXG4gIHZhciBzID0gbm9kZXMubGVuZ3RoO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHM7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbE5vZGUgPSBub2Rlc1tpXTtcclxuICAgIHNpemUgKz0gbE5vZGUuY2FsY0VzdGltYXRlZFNpemUoKTtcclxuICB9XHJcblxyXG4gIGlmIChzaXplID09IDApXHJcbiAge1xyXG4gICAgdGhpcy5lc3RpbWF0ZWRTaXplID0gTGF5b3V0Q29uc3RhbnRzLkVNUFRZX0NPTVBPVU5EX05PREVfU0laRTtcclxuICB9XHJcbiAgZWxzZVxyXG4gIHtcclxuICAgIHRoaXMuZXN0aW1hdGVkU2l6ZSA9IE1hdGguZmxvb3Ioc2l6ZSAvIE1hdGguc3FydCh0aGlzLm5vZGVzLmxlbmd0aCkpO1xyXG4gIH1cclxuXHJcbiAgcmV0dXJuIE1hdGguZmxvb3IodGhpcy5lc3RpbWF0ZWRTaXplKTtcclxufTtcclxuXHJcbkxHcmFwaC5wcm90b3R5cGUudXBkYXRlQ29ubmVjdGVkID0gZnVuY3Rpb24gKClcclxue1xyXG4gIGlmICh0aGlzLm5vZGVzLmxlbmd0aCA9PSAwKVxyXG4gIHtcclxuICAgIHRoaXMuaXNDb25uZWN0ZWQgPSB0cnVlO1xyXG4gICAgcmV0dXJuO1xyXG4gIH1cclxuXHJcbiAgdmFyIHRvQmVWaXNpdGVkID0gW107XHJcbiAgdmFyIHZpc2l0ZWQgPSBuZXcgSGFzaFNldCgpO1xyXG4gIHZhciBjdXJyZW50Tm9kZSA9IHRoaXMubm9kZXNbMF07XHJcbiAgdmFyIG5laWdoYm9yRWRnZXM7XHJcbiAgdmFyIGN1cnJlbnROZWlnaGJvcjtcclxuICB0b0JlVmlzaXRlZCA9IHRvQmVWaXNpdGVkLmNvbmNhdChjdXJyZW50Tm9kZS53aXRoQ2hpbGRyZW4oKSk7XHJcblxyXG4gIHdoaWxlICh0b0JlVmlzaXRlZC5sZW5ndGggPiAwKVxyXG4gIHtcclxuICAgIGN1cnJlbnROb2RlID0gdG9CZVZpc2l0ZWQuc2hpZnQoKTtcclxuICAgIHZpc2l0ZWQuYWRkKGN1cnJlbnROb2RlKTtcclxuXHJcbiAgICAvLyBUcmF2ZXJzZSBhbGwgbmVpZ2hib3JzIG9mIHRoaXMgbm9kZVxyXG4gICAgbmVpZ2hib3JFZGdlcyA9IGN1cnJlbnROb2RlLmdldEVkZ2VzKCk7XHJcbiAgICB2YXIgcyA9IG5laWdoYm9yRWRnZXMubGVuZ3RoO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBzOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIHZhciBuZWlnaGJvckVkZ2UgPSBuZWlnaGJvckVkZ2VzW2ldO1xyXG4gICAgICBjdXJyZW50TmVpZ2hib3IgPVxyXG4gICAgICAgICAgICAgIG5laWdoYm9yRWRnZS5nZXRPdGhlckVuZEluR3JhcGgoY3VycmVudE5vZGUsIHRoaXMpO1xyXG5cclxuICAgICAgLy8gQWRkIHVudmlzaXRlZCBuZWlnaGJvcnMgdG8gdGhlIGxpc3QgdG8gdmlzaXRcclxuICAgICAgaWYgKGN1cnJlbnROZWlnaGJvciAhPSBudWxsICYmXHJcbiAgICAgICAgICAgICAgIXZpc2l0ZWQuY29udGFpbnMoY3VycmVudE5laWdoYm9yKSlcclxuICAgICAge1xyXG4gICAgICAgIHRvQmVWaXNpdGVkID0gdG9CZVZpc2l0ZWQuY29uY2F0KGN1cnJlbnROZWlnaGJvci53aXRoQ2hpbGRyZW4oKSk7XHJcbiAgICAgIH1cclxuICAgIH1cclxuICB9XHJcblxyXG4gIHRoaXMuaXNDb25uZWN0ZWQgPSBmYWxzZTtcclxuXHJcbiAgaWYgKHZpc2l0ZWQuc2l6ZSgpID49IHRoaXMubm9kZXMubGVuZ3RoKVxyXG4gIHtcclxuICAgIHZhciBub09mVmlzaXRlZEluVGhpc0dyYXBoID0gMDtcclxuXHJcbiAgICB2YXIgcyA9IHZpc2l0ZWQuc2l6ZSgpO1xyXG4gICAgZm9yICh2YXIgdmlzaXRlZElkIGluIHZpc2l0ZWQuc2V0KVxyXG4gICAge1xyXG4gICAgICB2YXIgdmlzaXRlZE5vZGUgPSB2aXNpdGVkLnNldFt2aXNpdGVkSWRdO1xyXG4gICAgICBpZiAodmlzaXRlZE5vZGUub3duZXIgPT0gdGhpcylcclxuICAgICAge1xyXG4gICAgICAgIG5vT2ZWaXNpdGVkSW5UaGlzR3JhcGgrKztcclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGlmIChub09mVmlzaXRlZEluVGhpc0dyYXBoID09IHRoaXMubm9kZXMubGVuZ3RoKVxyXG4gICAge1xyXG4gICAgICB0aGlzLmlzQ29ubmVjdGVkID0gdHJ1ZTtcclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IExHcmFwaDtcclxuIiwiZnVuY3Rpb24gTEdyYXBoTWFuYWdlcihsYXlvdXQpIHtcclxuICB0aGlzLmxheW91dCA9IGxheW91dDtcclxuXHJcbiAgdGhpcy5ncmFwaHMgPSBbXTtcclxuICB0aGlzLmVkZ2VzID0gW107XHJcbn1cclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmFkZFJvb3QgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdmFyIG5ncmFwaCA9IHRoaXMubGF5b3V0Lm5ld0dyYXBoKCk7XHJcbiAgdmFyIG5ub2RlID0gdGhpcy5sYXlvdXQubmV3Tm9kZShudWxsKTtcclxuICB2YXIgcm9vdCA9IHRoaXMuYWRkKG5ncmFwaCwgbm5vZGUpO1xyXG4gIHRoaXMuc2V0Um9vdEdyYXBoKHJvb3QpO1xyXG4gIHJldHVybiB0aGlzLnJvb3RHcmFwaDtcclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmFkZCA9IGZ1bmN0aW9uIChuZXdHcmFwaCwgcGFyZW50Tm9kZSwgbmV3RWRnZSwgc291cmNlTm9kZSwgdGFyZ2V0Tm9kZSlcclxue1xyXG4gIC8vdGhlcmUgYXJlIGp1c3QgMiBwYXJhbWV0ZXJzIGFyZSBwYXNzZWQgdGhlbiBpdCBhZGRzIGFuIExHcmFwaCBlbHNlIGl0IGFkZHMgYW4gTEVkZ2VcclxuICBpZiAobmV3RWRnZSA9PSBudWxsICYmIHNvdXJjZU5vZGUgPT0gbnVsbCAmJiB0YXJnZXROb2RlID09IG51bGwpIHtcclxuICAgIGlmIChuZXdHcmFwaCA9PSBudWxsKSB7XHJcbiAgICAgIHRocm93IFwiR3JhcGggaXMgbnVsbCFcIjtcclxuICAgIH1cclxuICAgIGlmIChwYXJlbnROb2RlID09IG51bGwpIHtcclxuICAgICAgdGhyb3cgXCJQYXJlbnQgbm9kZSBpcyBudWxsIVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKHRoaXMuZ3JhcGhzLmluZGV4T2YobmV3R3JhcGgpID4gLTEpIHtcclxuICAgICAgdGhyb3cgXCJHcmFwaCBhbHJlYWR5IGluIHRoaXMgZ3JhcGggbWdyIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMuZ3JhcGhzLnB1c2gobmV3R3JhcGgpO1xyXG5cclxuICAgIGlmIChuZXdHcmFwaC5wYXJlbnQgIT0gbnVsbCkge1xyXG4gICAgICB0aHJvdyBcIkFscmVhZHkgaGFzIGEgcGFyZW50IVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKHBhcmVudE5vZGUuY2hpbGQgIT0gbnVsbCkge1xyXG4gICAgICB0aHJvdyAgXCJBbHJlYWR5IGhhcyBhIGNoaWxkIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIG5ld0dyYXBoLnBhcmVudCA9IHBhcmVudE5vZGU7XHJcbiAgICBwYXJlbnROb2RlLmNoaWxkID0gbmV3R3JhcGg7XHJcblxyXG4gICAgcmV0dXJuIG5ld0dyYXBoO1xyXG4gIH1cclxuICBlbHNlIHtcclxuICAgIC8vY2hhbmdlIHRoZSBvcmRlciBvZiB0aGUgcGFyYW1ldGVyc1xyXG4gICAgdGFyZ2V0Tm9kZSA9IG5ld0VkZ2U7XHJcbiAgICBzb3VyY2VOb2RlID0gcGFyZW50Tm9kZTtcclxuICAgIG5ld0VkZ2UgPSBuZXdHcmFwaDtcclxuICAgIHZhciBzb3VyY2VHcmFwaCA9IHNvdXJjZU5vZGUuZ2V0T3duZXIoKTtcclxuICAgIHZhciB0YXJnZXRHcmFwaCA9IHRhcmdldE5vZGUuZ2V0T3duZXIoKTtcclxuXHJcbiAgICBpZiAoIShzb3VyY2VHcmFwaCAhPSBudWxsICYmIHNvdXJjZUdyYXBoLmdldEdyYXBoTWFuYWdlcigpID09IHRoaXMpKSB7XHJcbiAgICAgIHRocm93IFwiU291cmNlIG5vdCBpbiB0aGlzIGdyYXBoIG1nciFcIjtcclxuICAgIH1cclxuICAgIGlmICghKHRhcmdldEdyYXBoICE9IG51bGwgJiYgdGFyZ2V0R3JhcGguZ2V0R3JhcGhNYW5hZ2VyKCkgPT0gdGhpcykpIHtcclxuICAgICAgdGhyb3cgXCJUYXJnZXQgbm90IGluIHRoaXMgZ3JhcGggbWdyIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChzb3VyY2VHcmFwaCA9PSB0YXJnZXRHcmFwaClcclxuICAgIHtcclxuICAgICAgbmV3RWRnZS5pc0ludGVyR3JhcGggPSBmYWxzZTtcclxuICAgICAgcmV0dXJuIHNvdXJjZUdyYXBoLmFkZChuZXdFZGdlLCBzb3VyY2VOb2RlLCB0YXJnZXROb2RlKTtcclxuICAgIH1cclxuICAgIGVsc2VcclxuICAgIHtcclxuICAgICAgbmV3RWRnZS5pc0ludGVyR3JhcGggPSB0cnVlO1xyXG5cclxuICAgICAgLy8gc2V0IHNvdXJjZSBhbmQgdGFyZ2V0XHJcbiAgICAgIG5ld0VkZ2Uuc291cmNlID0gc291cmNlTm9kZTtcclxuICAgICAgbmV3RWRnZS50YXJnZXQgPSB0YXJnZXROb2RlO1xyXG5cclxuICAgICAgLy8gYWRkIGVkZ2UgdG8gaW50ZXItZ3JhcGggZWRnZSBsaXN0XHJcbiAgICAgIGlmICh0aGlzLmVkZ2VzLmluZGV4T2YobmV3RWRnZSkgPiAtMSkge1xyXG4gICAgICAgIHRocm93IFwiRWRnZSBhbHJlYWR5IGluIGludGVyLWdyYXBoIGVkZ2UgbGlzdCFcIjtcclxuICAgICAgfVxyXG5cclxuICAgICAgdGhpcy5lZGdlcy5wdXNoKG5ld0VkZ2UpO1xyXG5cclxuICAgICAgLy8gYWRkIGVkZ2UgdG8gc291cmNlIGFuZCB0YXJnZXQgaW5jaWRlbmN5IGxpc3RzXHJcbiAgICAgIGlmICghKG5ld0VkZ2Uuc291cmNlICE9IG51bGwgJiYgbmV3RWRnZS50YXJnZXQgIT0gbnVsbCkpIHtcclxuICAgICAgICB0aHJvdyBcIkVkZ2Ugc291cmNlIGFuZC9vciB0YXJnZXQgaXMgbnVsbCFcIjtcclxuICAgICAgfVxyXG5cclxuICAgICAgaWYgKCEobmV3RWRnZS5zb3VyY2UuZWRnZXMuaW5kZXhPZihuZXdFZGdlKSA9PSAtMSAmJiBuZXdFZGdlLnRhcmdldC5lZGdlcy5pbmRleE9mKG5ld0VkZ2UpID09IC0xKSkge1xyXG4gICAgICAgIHRocm93IFwiRWRnZSBhbHJlYWR5IGluIHNvdXJjZSBhbmQvb3IgdGFyZ2V0IGluY2lkZW5jeSBsaXN0IVwiO1xyXG4gICAgICB9XHJcblxyXG4gICAgICBuZXdFZGdlLnNvdXJjZS5lZGdlcy5wdXNoKG5ld0VkZ2UpO1xyXG4gICAgICBuZXdFZGdlLnRhcmdldC5lZGdlcy5wdXNoKG5ld0VkZ2UpO1xyXG5cclxuICAgICAgcmV0dXJuIG5ld0VkZ2U7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUucmVtb3ZlID0gZnVuY3Rpb24gKGxPYmopIHtcclxuICBpZiAobE9iaiBpbnN0YW5jZW9mIExHcmFwaCkge1xyXG4gICAgdmFyIGdyYXBoID0gbE9iajtcclxuICAgIGlmIChncmFwaC5nZXRHcmFwaE1hbmFnZXIoKSAhPSB0aGlzKSB7XHJcbiAgICAgIHRocm93IFwiR3JhcGggbm90IGluIHRoaXMgZ3JhcGggbWdyXCI7XHJcbiAgICB9XHJcbiAgICBpZiAoIShncmFwaCA9PSB0aGlzLnJvb3RHcmFwaCB8fCAoZ3JhcGgucGFyZW50ICE9IG51bGwgJiYgZ3JhcGgucGFyZW50LmdyYXBoTWFuYWdlciA9PSB0aGlzKSkpIHtcclxuICAgICAgdGhyb3cgXCJJbnZhbGlkIHBhcmVudCBub2RlIVwiO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIGZpcnN0IHRoZSBlZGdlcyAobWFrZSBhIGNvcHkgdG8gZG8gaXQgc2FmZWx5KVxyXG4gICAgdmFyIGVkZ2VzVG9CZVJlbW92ZWQgPSBbXTtcclxuXHJcbiAgICBlZGdlc1RvQmVSZW1vdmVkID0gZWRnZXNUb0JlUmVtb3ZlZC5jb25jYXQoZ3JhcGguZ2V0RWRnZXMoKSk7XHJcblxyXG4gICAgdmFyIGVkZ2U7XHJcbiAgICB2YXIgcyA9IGVkZ2VzVG9CZVJlbW92ZWQubGVuZ3RoO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBzOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIGVkZ2UgPSBlZGdlc1RvQmVSZW1vdmVkW2ldO1xyXG4gICAgICBncmFwaC5yZW1vdmUoZWRnZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLy8gdGhlbiB0aGUgbm9kZXMgKG1ha2UgYSBjb3B5IHRvIGRvIGl0IHNhZmVseSlcclxuICAgIHZhciBub2Rlc1RvQmVSZW1vdmVkID0gW107XHJcblxyXG4gICAgbm9kZXNUb0JlUmVtb3ZlZCA9IG5vZGVzVG9CZVJlbW92ZWQuY29uY2F0KGdyYXBoLmdldE5vZGVzKCkpO1xyXG5cclxuICAgIHZhciBub2RlO1xyXG4gICAgcyA9IG5vZGVzVG9CZVJlbW92ZWQubGVuZ3RoO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBzOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIG5vZGUgPSBub2Rlc1RvQmVSZW1vdmVkW2ldO1xyXG4gICAgICBncmFwaC5yZW1vdmUobm9kZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLy8gY2hlY2sgaWYgZ3JhcGggaXMgdGhlIHJvb3RcclxuICAgIGlmIChncmFwaCA9PSB0aGlzLnJvb3RHcmFwaClcclxuICAgIHtcclxuICAgICAgdGhpcy5zZXRSb290R3JhcGgobnVsbCk7XHJcbiAgICB9XHJcblxyXG4gICAgLy8gbm93IHJlbW92ZSB0aGUgZ3JhcGggaXRzZWxmXHJcbiAgICB2YXIgaW5kZXggPSB0aGlzLmdyYXBocy5pbmRleE9mKGdyYXBoKTtcclxuICAgIHRoaXMuZ3JhcGhzLnNwbGljZShpbmRleCwgMSk7XHJcblxyXG4gICAgLy8gYWxzbyByZXNldCB0aGUgcGFyZW50IG9mIHRoZSBncmFwaFxyXG4gICAgZ3JhcGgucGFyZW50ID0gbnVsbDtcclxuICB9XHJcbiAgZWxzZSBpZiAobE9iaiBpbnN0YW5jZW9mIExFZGdlKSB7XHJcbiAgICBlZGdlID0gbE9iajtcclxuICAgIGlmIChlZGdlID09IG51bGwpIHtcclxuICAgICAgdGhyb3cgXCJFZGdlIGlzIG51bGwhXCI7XHJcbiAgICB9XHJcbiAgICBpZiAoIWVkZ2UuaXNJbnRlckdyYXBoKSB7XHJcbiAgICAgIHRocm93IFwiTm90IGFuIGludGVyLWdyYXBoIGVkZ2UhXCI7XHJcbiAgICB9XHJcbiAgICBpZiAoIShlZGdlLnNvdXJjZSAhPSBudWxsICYmIGVkZ2UudGFyZ2V0ICE9IG51bGwpKSB7XHJcbiAgICAgIHRocm93IFwiU291cmNlIGFuZC9vciB0YXJnZXQgaXMgbnVsbCFcIjtcclxuICAgIH1cclxuXHJcbiAgICAvLyByZW1vdmUgZWRnZSBmcm9tIHNvdXJjZSBhbmQgdGFyZ2V0IG5vZGVzJyBpbmNpZGVuY3kgbGlzdHNcclxuXHJcbiAgICBpZiAoIShlZGdlLnNvdXJjZS5lZGdlcy5pbmRleE9mKGVkZ2UpICE9IC0xICYmIGVkZ2UudGFyZ2V0LmVkZ2VzLmluZGV4T2YoZWRnZSkgIT0gLTEpKSB7XHJcbiAgICAgIHRocm93IFwiU291cmNlIGFuZC9vciB0YXJnZXQgZG9lc24ndCBrbm93IHRoaXMgZWRnZSFcIjtcclxuICAgIH1cclxuXHJcbiAgICB2YXIgaW5kZXggPSBlZGdlLnNvdXJjZS5lZGdlcy5pbmRleE9mKGVkZ2UpO1xyXG4gICAgZWRnZS5zb3VyY2UuZWRnZXMuc3BsaWNlKGluZGV4LCAxKTtcclxuICAgIGluZGV4ID0gZWRnZS50YXJnZXQuZWRnZXMuaW5kZXhPZihlZGdlKTtcclxuICAgIGVkZ2UudGFyZ2V0LmVkZ2VzLnNwbGljZShpbmRleCwgMSk7XHJcblxyXG4gICAgLy8gcmVtb3ZlIGVkZ2UgZnJvbSBvd25lciBncmFwaCBtYW5hZ2VyJ3MgaW50ZXItZ3JhcGggZWRnZSBsaXN0XHJcblxyXG4gICAgaWYgKCEoZWRnZS5zb3VyY2Uub3duZXIgIT0gbnVsbCAmJiBlZGdlLnNvdXJjZS5vd25lci5nZXRHcmFwaE1hbmFnZXIoKSAhPSBudWxsKSkge1xyXG4gICAgICB0aHJvdyBcIkVkZ2Ugb3duZXIgZ3JhcGggb3Igb3duZXIgZ3JhcGggbWFuYWdlciBpcyBudWxsIVwiO1xyXG4gICAgfVxyXG4gICAgaWYgKGVkZ2Uuc291cmNlLm93bmVyLmdldEdyYXBoTWFuYWdlcigpLmVkZ2VzLmluZGV4T2YoZWRnZSkgPT0gLTEpIHtcclxuICAgICAgdGhyb3cgXCJOb3QgaW4gb3duZXIgZ3JhcGggbWFuYWdlcidzIGVkZ2UgbGlzdCFcIjtcclxuICAgIH1cclxuXHJcbiAgICB2YXIgaW5kZXggPSBlZGdlLnNvdXJjZS5vd25lci5nZXRHcmFwaE1hbmFnZXIoKS5lZGdlcy5pbmRleE9mKGVkZ2UpO1xyXG4gICAgZWRnZS5zb3VyY2Uub3duZXIuZ2V0R3JhcGhNYW5hZ2VyKCkuZWRnZXMuc3BsaWNlKGluZGV4LCAxKTtcclxuICB9XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS51cGRhdGVCb3VuZHMgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdGhpcy5yb290R3JhcGgudXBkYXRlQm91bmRzKHRydWUpO1xyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUuZ2V0R3JhcGhzID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmdyYXBocztcclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmdldEFsbE5vZGVzID0gZnVuY3Rpb24gKClcclxue1xyXG4gIGlmICh0aGlzLmFsbE5vZGVzID09IG51bGwpXHJcbiAge1xyXG4gICAgdmFyIG5vZGVMaXN0ID0gW107XHJcbiAgICB2YXIgZ3JhcGhzID0gdGhpcy5nZXRHcmFwaHMoKTtcclxuICAgIHZhciBzID0gZ3JhcGhzLmxlbmd0aDtcclxuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgczsgaSsrKVxyXG4gICAge1xyXG4gICAgICBub2RlTGlzdCA9IG5vZGVMaXN0LmNvbmNhdChncmFwaHNbaV0uZ2V0Tm9kZXMoKSk7XHJcbiAgICB9XHJcbiAgICB0aGlzLmFsbE5vZGVzID0gbm9kZUxpc3Q7XHJcbiAgfVxyXG4gIHJldHVybiB0aGlzLmFsbE5vZGVzO1xyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUucmVzZXRBbGxOb2RlcyA9IGZ1bmN0aW9uICgpXHJcbntcclxuICB0aGlzLmFsbE5vZGVzID0gbnVsbDtcclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLnJlc2V0QWxsRWRnZXMgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdGhpcy5hbGxFZGdlcyA9IG51bGw7XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS5yZXNldEFsbE5vZGVzVG9BcHBseUdyYXZpdGF0aW9uID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHRoaXMuYWxsTm9kZXNUb0FwcGx5R3Jhdml0YXRpb24gPSBudWxsO1xyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUuZ2V0QWxsRWRnZXMgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgaWYgKHRoaXMuYWxsRWRnZXMgPT0gbnVsbClcclxuICB7XHJcbiAgICB2YXIgZWRnZUxpc3QgPSBbXTtcclxuICAgIHZhciBncmFwaHMgPSB0aGlzLmdldEdyYXBocygpO1xyXG4gICAgdmFyIHMgPSBncmFwaHMubGVuZ3RoO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBncmFwaHMubGVuZ3RoOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIGVkZ2VMaXN0ID0gZWRnZUxpc3QuY29uY2F0KGdyYXBoc1tpXS5nZXRFZGdlcygpKTtcclxuICAgIH1cclxuXHJcbiAgICBlZGdlTGlzdCA9IGVkZ2VMaXN0LmNvbmNhdCh0aGlzLmVkZ2VzKTtcclxuXHJcbiAgICB0aGlzLmFsbEVkZ2VzID0gZWRnZUxpc3Q7XHJcbiAgfVxyXG4gIHJldHVybiB0aGlzLmFsbEVkZ2VzO1xyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUuZ2V0QWxsTm9kZXNUb0FwcGx5R3Jhdml0YXRpb24gPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuYWxsTm9kZXNUb0FwcGx5R3Jhdml0YXRpb247XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS5zZXRBbGxOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvbiA9IGZ1bmN0aW9uIChub2RlTGlzdClcclxue1xyXG4gIGlmICh0aGlzLmFsbE5vZGVzVG9BcHBseUdyYXZpdGF0aW9uICE9IG51bGwpIHtcclxuICAgIHRocm93IFwiYXNzZXJ0IGZhaWxlZFwiO1xyXG4gIH1cclxuXHJcbiAgdGhpcy5hbGxOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvbiA9IG5vZGVMaXN0O1xyXG59O1xyXG5cclxuTEdyYXBoTWFuYWdlci5wcm90b3R5cGUuZ2V0Um9vdCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5yb290R3JhcGg7XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS5zZXRSb290R3JhcGggPSBmdW5jdGlvbiAoZ3JhcGgpXHJcbntcclxuICBpZiAoZ3JhcGguZ2V0R3JhcGhNYW5hZ2VyKCkgIT0gdGhpcykge1xyXG4gICAgdGhyb3cgXCJSb290IG5vdCBpbiB0aGlzIGdyYXBoIG1nciFcIjtcclxuICB9XHJcblxyXG4gIHRoaXMucm9vdEdyYXBoID0gZ3JhcGg7XHJcbiAgLy8gcm9vdCBncmFwaCBtdXN0IGhhdmUgYSByb290IG5vZGUgYXNzb2NpYXRlZCB3aXRoIGl0IGZvciBjb252ZW5pZW5jZVxyXG4gIGlmIChncmFwaC5wYXJlbnQgPT0gbnVsbClcclxuICB7XHJcbiAgICBncmFwaC5wYXJlbnQgPSB0aGlzLmxheW91dC5uZXdOb2RlKFwiUm9vdCBub2RlXCIpO1xyXG4gIH1cclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmdldExheW91dCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5sYXlvdXQ7XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS5pc09uZUFuY2VzdG9yT2ZPdGhlciA9IGZ1bmN0aW9uIChmaXJzdE5vZGUsIHNlY29uZE5vZGUpXHJcbntcclxuICBpZiAoIShmaXJzdE5vZGUgIT0gbnVsbCAmJiBzZWNvbmROb2RlICE9IG51bGwpKSB7XHJcbiAgICB0aHJvdyBcImFzc2VydCBmYWlsZWRcIjtcclxuICB9XHJcblxyXG4gIGlmIChmaXJzdE5vZGUgPT0gc2Vjb25kTm9kZSlcclxuICB7XHJcbiAgICByZXR1cm4gdHJ1ZTtcclxuICB9XHJcbiAgLy8gSXMgc2Vjb25kIG5vZGUgYW4gYW5jZXN0b3Igb2YgdGhlIGZpcnN0IG9uZT9cclxuICB2YXIgb3duZXJHcmFwaCA9IGZpcnN0Tm9kZS5nZXRPd25lcigpO1xyXG4gIHZhciBwYXJlbnROb2RlO1xyXG5cclxuICBkb1xyXG4gIHtcclxuICAgIHBhcmVudE5vZGUgPSBvd25lckdyYXBoLmdldFBhcmVudCgpO1xyXG5cclxuICAgIGlmIChwYXJlbnROb2RlID09IG51bGwpXHJcbiAgICB7XHJcbiAgICAgIGJyZWFrO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChwYXJlbnROb2RlID09IHNlY29uZE5vZGUpXHJcbiAgICB7XHJcbiAgICAgIHJldHVybiB0cnVlO1xyXG4gICAgfVxyXG5cclxuICAgIG93bmVyR3JhcGggPSBwYXJlbnROb2RlLmdldE93bmVyKCk7XHJcbiAgICBpZiAob3duZXJHcmFwaCA9PSBudWxsKVxyXG4gICAge1xyXG4gICAgICBicmVhaztcclxuICAgIH1cclxuICB9IHdoaWxlICh0cnVlKTtcclxuICAvLyBJcyBmaXJzdCBub2RlIGFuIGFuY2VzdG9yIG9mIHRoZSBzZWNvbmQgb25lP1xyXG4gIG93bmVyR3JhcGggPSBzZWNvbmROb2RlLmdldE93bmVyKCk7XHJcblxyXG4gIGRvXHJcbiAge1xyXG4gICAgcGFyZW50Tm9kZSA9IG93bmVyR3JhcGguZ2V0UGFyZW50KCk7XHJcblxyXG4gICAgaWYgKHBhcmVudE5vZGUgPT0gbnVsbClcclxuICAgIHtcclxuICAgICAgYnJlYWs7XHJcbiAgICB9XHJcblxyXG4gICAgaWYgKHBhcmVudE5vZGUgPT0gZmlyc3ROb2RlKVxyXG4gICAge1xyXG4gICAgICByZXR1cm4gdHJ1ZTtcclxuICAgIH1cclxuXHJcbiAgICBvd25lckdyYXBoID0gcGFyZW50Tm9kZS5nZXRPd25lcigpO1xyXG4gICAgaWYgKG93bmVyR3JhcGggPT0gbnVsbClcclxuICAgIHtcclxuICAgICAgYnJlYWs7XHJcbiAgICB9XHJcbiAgfSB3aGlsZSAodHJ1ZSk7XHJcblxyXG4gIHJldHVybiBmYWxzZTtcclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmNhbGNMb3dlc3RDb21tb25BbmNlc3RvcnMgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdmFyIGVkZ2U7XHJcbiAgdmFyIHNvdXJjZU5vZGU7XHJcbiAgdmFyIHRhcmdldE5vZGU7XHJcbiAgdmFyIHNvdXJjZUFuY2VzdG9yR3JhcGg7XHJcbiAgdmFyIHRhcmdldEFuY2VzdG9yR3JhcGg7XHJcblxyXG4gIHZhciBlZGdlcyA9IHRoaXMuZ2V0QWxsRWRnZXMoKTtcclxuICB2YXIgcyA9IGVkZ2VzLmxlbmd0aDtcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHM7IGkrKylcclxuICB7XHJcbiAgICBlZGdlID0gZWRnZXNbaV07XHJcblxyXG4gICAgc291cmNlTm9kZSA9IGVkZ2Uuc291cmNlO1xyXG4gICAgdGFyZ2V0Tm9kZSA9IGVkZ2UudGFyZ2V0O1xyXG4gICAgZWRnZS5sY2EgPSBudWxsO1xyXG4gICAgZWRnZS5zb3VyY2VJbkxjYSA9IHNvdXJjZU5vZGU7XHJcbiAgICBlZGdlLnRhcmdldEluTGNhID0gdGFyZ2V0Tm9kZTtcclxuXHJcbiAgICBpZiAoc291cmNlTm9kZSA9PSB0YXJnZXROb2RlKVxyXG4gICAge1xyXG4gICAgICBlZGdlLmxjYSA9IHNvdXJjZU5vZGUuZ2V0T3duZXIoKTtcclxuICAgICAgY29udGludWU7XHJcbiAgICB9XHJcblxyXG4gICAgc291cmNlQW5jZXN0b3JHcmFwaCA9IHNvdXJjZU5vZGUuZ2V0T3duZXIoKTtcclxuXHJcbiAgICB3aGlsZSAoZWRnZS5sY2EgPT0gbnVsbClcclxuICAgIHtcclxuICAgICAgdGFyZ2V0QW5jZXN0b3JHcmFwaCA9IHRhcmdldE5vZGUuZ2V0T3duZXIoKTtcclxuXHJcbiAgICAgIHdoaWxlIChlZGdlLmxjYSA9PSBudWxsKVxyXG4gICAgICB7XHJcbiAgICAgICAgaWYgKHRhcmdldEFuY2VzdG9yR3JhcGggPT0gc291cmNlQW5jZXN0b3JHcmFwaClcclxuICAgICAgICB7XHJcbiAgICAgICAgICBlZGdlLmxjYSA9IHRhcmdldEFuY2VzdG9yR3JhcGg7XHJcbiAgICAgICAgICBicmVhaztcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIGlmICh0YXJnZXRBbmNlc3RvckdyYXBoID09IHRoaXMucm9vdEdyYXBoKVxyXG4gICAgICAgIHtcclxuICAgICAgICAgIGJyZWFrO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgaWYgKGVkZ2UubGNhICE9IG51bGwpIHtcclxuICAgICAgICAgIHRocm93IFwiYXNzZXJ0IGZhaWxlZFwiO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlZGdlLnRhcmdldEluTGNhID0gdGFyZ2V0QW5jZXN0b3JHcmFwaC5nZXRQYXJlbnQoKTtcclxuICAgICAgICB0YXJnZXRBbmNlc3RvckdyYXBoID0gZWRnZS50YXJnZXRJbkxjYS5nZXRPd25lcigpO1xyXG4gICAgICB9XHJcblxyXG4gICAgICBpZiAoc291cmNlQW5jZXN0b3JHcmFwaCA9PSB0aGlzLnJvb3RHcmFwaClcclxuICAgICAge1xyXG4gICAgICAgIGJyZWFrO1xyXG4gICAgICB9XHJcblxyXG4gICAgICBpZiAoZWRnZS5sY2EgPT0gbnVsbClcclxuICAgICAge1xyXG4gICAgICAgIGVkZ2Uuc291cmNlSW5MY2EgPSBzb3VyY2VBbmNlc3RvckdyYXBoLmdldFBhcmVudCgpO1xyXG4gICAgICAgIHNvdXJjZUFuY2VzdG9yR3JhcGggPSBlZGdlLnNvdXJjZUluTGNhLmdldE93bmVyKCk7XHJcbiAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBpZiAoZWRnZS5sY2EgPT0gbnVsbCkge1xyXG4gICAgICB0aHJvdyBcImFzc2VydCBmYWlsZWRcIjtcclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG5MR3JhcGhNYW5hZ2VyLnByb3RvdHlwZS5jYWxjTG93ZXN0Q29tbW9uQW5jZXN0b3IgPSBmdW5jdGlvbiAoZmlyc3ROb2RlLCBzZWNvbmROb2RlKVxyXG57XHJcbiAgaWYgKGZpcnN0Tm9kZSA9PSBzZWNvbmROb2RlKVxyXG4gIHtcclxuICAgIHJldHVybiBmaXJzdE5vZGUuZ2V0T3duZXIoKTtcclxuICB9XHJcbiAgdmFyIGZpcnN0T3duZXJHcmFwaCA9IGZpcnN0Tm9kZS5nZXRPd25lcigpO1xyXG5cclxuICBkb1xyXG4gIHtcclxuICAgIGlmIChmaXJzdE93bmVyR3JhcGggPT0gbnVsbClcclxuICAgIHtcclxuICAgICAgYnJlYWs7XHJcbiAgICB9XHJcbiAgICB2YXIgc2Vjb25kT3duZXJHcmFwaCA9IHNlY29uZE5vZGUuZ2V0T3duZXIoKTtcclxuXHJcbiAgICBkb1xyXG4gICAge1xyXG4gICAgICBpZiAoc2Vjb25kT3duZXJHcmFwaCA9PSBudWxsKVxyXG4gICAgICB7XHJcbiAgICAgICAgYnJlYWs7XHJcbiAgICAgIH1cclxuXHJcbiAgICAgIGlmIChzZWNvbmRPd25lckdyYXBoID09IGZpcnN0T3duZXJHcmFwaClcclxuICAgICAge1xyXG4gICAgICAgIHJldHVybiBzZWNvbmRPd25lckdyYXBoO1xyXG4gICAgICB9XHJcbiAgICAgIHNlY29uZE93bmVyR3JhcGggPSBzZWNvbmRPd25lckdyYXBoLmdldFBhcmVudCgpLmdldE93bmVyKCk7XHJcbiAgICB9IHdoaWxlICh0cnVlKTtcclxuXHJcbiAgICBmaXJzdE93bmVyR3JhcGggPSBmaXJzdE93bmVyR3JhcGguZ2V0UGFyZW50KCkuZ2V0T3duZXIoKTtcclxuICB9IHdoaWxlICh0cnVlKTtcclxuXHJcbiAgcmV0dXJuIGZpcnN0T3duZXJHcmFwaDtcclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmNhbGNJbmNsdXNpb25UcmVlRGVwdGhzID0gZnVuY3Rpb24gKGdyYXBoLCBkZXB0aCkge1xyXG4gIGlmIChncmFwaCA9PSBudWxsICYmIGRlcHRoID09IG51bGwpIHtcclxuICAgIGdyYXBoID0gdGhpcy5yb290R3JhcGg7XHJcbiAgICBkZXB0aCA9IDE7XHJcbiAgfVxyXG4gIHZhciBub2RlO1xyXG5cclxuICB2YXIgbm9kZXMgPSBncmFwaC5nZXROb2RlcygpO1xyXG4gIHZhciBzID0gbm9kZXMubGVuZ3RoO1xyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgczsgaSsrKVxyXG4gIHtcclxuICAgIG5vZGUgPSBub2Rlc1tpXTtcclxuICAgIG5vZGUuaW5jbHVzaW9uVHJlZURlcHRoID0gZGVwdGg7XHJcblxyXG4gICAgaWYgKG5vZGUuY2hpbGQgIT0gbnVsbClcclxuICAgIHtcclxuICAgICAgdGhpcy5jYWxjSW5jbHVzaW9uVHJlZURlcHRocyhub2RlLmNoaWxkLCBkZXB0aCArIDEpO1xyXG4gICAgfVxyXG4gIH1cclxufTtcclxuXHJcbkxHcmFwaE1hbmFnZXIucHJvdG90eXBlLmluY2x1ZGVzSW52YWxpZEVkZ2UgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgdmFyIGVkZ2U7XHJcblxyXG4gIHZhciBzID0gdGhpcy5lZGdlcy5sZW5ndGg7XHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBzOyBpKyspXHJcbiAge1xyXG4gICAgZWRnZSA9IHRoaXMuZWRnZXNbaV07XHJcblxyXG4gICAgaWYgKHRoaXMuaXNPbmVBbmNlc3Rvck9mT3RoZXIoZWRnZS5zb3VyY2UsIGVkZ2UudGFyZ2V0KSlcclxuICAgIHtcclxuICAgICAgcmV0dXJuIHRydWU7XHJcbiAgICB9XHJcbiAgfVxyXG4gIHJldHVybiBmYWxzZTtcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gTEdyYXBoTWFuYWdlcjtcclxuIiwiZnVuY3Rpb24gTEdyYXBoT2JqZWN0KHZHcmFwaE9iamVjdCkge1xyXG4gIHRoaXMudkdyYXBoT2JqZWN0ID0gdkdyYXBoT2JqZWN0O1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IExHcmFwaE9iamVjdDtcclxuIiwidmFyIExHcmFwaE9iamVjdCA9IHJlcXVpcmUoJy4vTEdyYXBoT2JqZWN0Jyk7XHJcbnZhciBJbnRlZ2VyID0gcmVxdWlyZSgnLi9JbnRlZ2VyJyk7XHJcbnZhciBSZWN0YW5nbGVEID0gcmVxdWlyZSgnLi9SZWN0YW5nbGVEJyk7XHJcblxyXG5mdW5jdGlvbiBMTm9kZShnbSwgbG9jLCBzaXplLCB2Tm9kZSkge1xyXG4gIC8vQWx0ZXJuYXRpdmUgY29uc3RydWN0b3IgMSA6IExOb2RlKExHcmFwaE1hbmFnZXIgZ20sIFBvaW50IGxvYywgRGltZW5zaW9uIHNpemUsIE9iamVjdCB2Tm9kZSlcclxuICBpZiAoc2l6ZSA9PSBudWxsICYmIHZOb2RlID09IG51bGwpIHtcclxuICAgIHZOb2RlID0gbG9jO1xyXG4gIH1cclxuXHJcbiAgTEdyYXBoT2JqZWN0LmNhbGwodGhpcywgdk5vZGUpO1xyXG5cclxuICAvL0FsdGVybmF0aXZlIGNvbnN0cnVjdG9yIDIgOiBMTm9kZShMYXlvdXQgbGF5b3V0LCBPYmplY3Qgdk5vZGUpXHJcbiAgaWYgKGdtLmdyYXBoTWFuYWdlciAhPSBudWxsKVxyXG4gICAgZ20gPSBnbS5ncmFwaE1hbmFnZXI7XHJcblxyXG4gIHRoaXMuZXN0aW1hdGVkU2l6ZSA9IEludGVnZXIuTUlOX1ZBTFVFO1xyXG4gIHRoaXMuaW5jbHVzaW9uVHJlZURlcHRoID0gSW50ZWdlci5NQVhfVkFMVUU7XHJcbiAgdGhpcy52R3JhcGhPYmplY3QgPSB2Tm9kZTtcclxuICB0aGlzLmVkZ2VzID0gW107XHJcbiAgdGhpcy5ncmFwaE1hbmFnZXIgPSBnbTtcclxuXHJcbiAgaWYgKHNpemUgIT0gbnVsbCAmJiBsb2MgIT0gbnVsbClcclxuICAgIHRoaXMucmVjdCA9IG5ldyBSZWN0YW5nbGVEKGxvYy54LCBsb2MueSwgc2l6ZS53aWR0aCwgc2l6ZS5oZWlnaHQpO1xyXG4gIGVsc2VcclxuICAgIHRoaXMucmVjdCA9IG5ldyBSZWN0YW5nbGVEKCk7XHJcbn1cclxuXHJcbkxOb2RlLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoTEdyYXBoT2JqZWN0LnByb3RvdHlwZSk7XHJcbmZvciAodmFyIHByb3AgaW4gTEdyYXBoT2JqZWN0KSB7XHJcbiAgTE5vZGVbcHJvcF0gPSBMR3JhcGhPYmplY3RbcHJvcF07XHJcbn1cclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRFZGdlcyA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5lZGdlcztcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRDaGlsZCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5jaGlsZDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRPd25lciA9IGZ1bmN0aW9uICgpXHJcbntcclxuICBpZiAodGhpcy5vd25lciAhPSBudWxsKSB7XHJcbiAgICBpZiAoISh0aGlzLm93bmVyID09IG51bGwgfHwgdGhpcy5vd25lci5nZXROb2RlcygpLmluZGV4T2YodGhpcykgPiAtMSkpIHtcclxuICAgICAgdGhyb3cgXCJhc3NlcnQgZmFpbGVkXCI7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICByZXR1cm4gdGhpcy5vd25lcjtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRXaWR0aCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5yZWN0LndpZHRoO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLnNldFdpZHRoID0gZnVuY3Rpb24gKHdpZHRoKVxyXG57XHJcbiAgdGhpcy5yZWN0LndpZHRoID0gd2lkdGg7XHJcbn07XHJcblxyXG5MTm9kZS5wcm90b3R5cGUuZ2V0SGVpZ2h0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnJlY3QuaGVpZ2h0O1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLnNldEhlaWdodCA9IGZ1bmN0aW9uIChoZWlnaHQpXHJcbntcclxuICB0aGlzLnJlY3QuaGVpZ2h0ID0gaGVpZ2h0O1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldENlbnRlclggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMucmVjdC54ICsgdGhpcy5yZWN0LndpZHRoIC8gMjtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRDZW50ZXJZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnJlY3QueSArIHRoaXMucmVjdC5oZWlnaHQgLyAyO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldENlbnRlciA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gbmV3IFBvaW50RCh0aGlzLnJlY3QueCArIHRoaXMucmVjdC53aWR0aCAvIDIsXHJcbiAgICAgICAgICB0aGlzLnJlY3QueSArIHRoaXMucmVjdC5oZWlnaHQgLyAyKTtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRMb2NhdGlvbiA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gbmV3IFBvaW50RCh0aGlzLnJlY3QueCwgdGhpcy5yZWN0LnkpO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldFJlY3QgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMucmVjdDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXREaWFnb25hbCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gTWF0aC5zcXJ0KHRoaXMucmVjdC53aWR0aCAqIHRoaXMucmVjdC53aWR0aCArXHJcbiAgICAgICAgICB0aGlzLnJlY3QuaGVpZ2h0ICogdGhpcy5yZWN0LmhlaWdodCk7XHJcbn07XHJcblxyXG5MTm9kZS5wcm90b3R5cGUuc2V0UmVjdCA9IGZ1bmN0aW9uICh1cHBlckxlZnQsIGRpbWVuc2lvbilcclxue1xyXG4gIHRoaXMucmVjdC54ID0gdXBwZXJMZWZ0Lng7XHJcbiAgdGhpcy5yZWN0LnkgPSB1cHBlckxlZnQueTtcclxuICB0aGlzLnJlY3Qud2lkdGggPSBkaW1lbnNpb24ud2lkdGg7XHJcbiAgdGhpcy5yZWN0LmhlaWdodCA9IGRpbWVuc2lvbi5oZWlnaHQ7XHJcbn07XHJcblxyXG5MTm9kZS5wcm90b3R5cGUuc2V0Q2VudGVyID0gZnVuY3Rpb24gKGN4LCBjeSlcclxue1xyXG4gIHRoaXMucmVjdC54ID0gY3ggLSB0aGlzLnJlY3Qud2lkdGggLyAyO1xyXG4gIHRoaXMucmVjdC55ID0gY3kgLSB0aGlzLnJlY3QuaGVpZ2h0IC8gMjtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5zZXRMb2NhdGlvbiA9IGZ1bmN0aW9uICh4LCB5KVxyXG57XHJcbiAgdGhpcy5yZWN0LnggPSB4O1xyXG4gIHRoaXMucmVjdC55ID0geTtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5tb3ZlQnkgPSBmdW5jdGlvbiAoZHgsIGR5KVxyXG57XHJcbiAgdGhpcy5yZWN0LnggKz0gZHg7XHJcbiAgdGhpcy5yZWN0LnkgKz0gZHk7XHJcbn07XHJcblxyXG5MTm9kZS5wcm90b3R5cGUuZ2V0RWRnZUxpc3RUb05vZGUgPSBmdW5jdGlvbiAodG8pXHJcbntcclxuICB2YXIgZWRnZUxpc3QgPSBbXTtcclxuICB2YXIgZWRnZTtcclxuXHJcbiAgZm9yICh2YXIgb2JqIGluIHRoaXMuZWRnZXMpXHJcbiAge1xyXG4gICAgZWRnZSA9IG9iajtcclxuXHJcbiAgICBpZiAoZWRnZS50YXJnZXQgPT0gdG8pXHJcbiAgICB7XHJcbiAgICAgIGlmIChlZGdlLnNvdXJjZSAhPSB0aGlzKVxyXG4gICAgICAgIHRocm93IFwiSW5jb3JyZWN0IGVkZ2Ugc291cmNlIVwiO1xyXG5cclxuICAgICAgZWRnZUxpc3QucHVzaChlZGdlKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHJldHVybiBlZGdlTGlzdDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRFZGdlc0JldHdlZW4gPSBmdW5jdGlvbiAob3RoZXIpXHJcbntcclxuICB2YXIgZWRnZUxpc3QgPSBbXTtcclxuICB2YXIgZWRnZTtcclxuXHJcbiAgZm9yICh2YXIgb2JqIGluIHRoaXMuZWRnZXMpXHJcbiAge1xyXG4gICAgZWRnZSA9IHRoaXMuZWRnZXNbb2JqXTtcclxuXHJcbiAgICBpZiAoIShlZGdlLnNvdXJjZSA9PSB0aGlzIHx8IGVkZ2UudGFyZ2V0ID09IHRoaXMpKVxyXG4gICAgICB0aHJvdyBcIkluY29ycmVjdCBlZGdlIHNvdXJjZSBhbmQvb3IgdGFyZ2V0XCI7XHJcblxyXG4gICAgaWYgKChlZGdlLnRhcmdldCA9PSBvdGhlcikgfHwgKGVkZ2Uuc291cmNlID09IG90aGVyKSlcclxuICAgIHtcclxuICAgICAgZWRnZUxpc3QucHVzaChlZGdlKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHJldHVybiBlZGdlTGlzdDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXROZWlnaGJvcnNMaXN0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHZhciBuZWlnaGJvcnMgPSBuZXcgSGFzaFNldCgpO1xyXG4gIHZhciBlZGdlO1xyXG5cclxuICBmb3IgKHZhciBvYmogaW4gdGhpcy5lZGdlcylcclxuICB7XHJcbiAgICBlZGdlID0gdGhpcy5lZGdlc1tvYmpdO1xyXG5cclxuICAgIGlmIChlZGdlLnNvdXJjZSA9PSB0aGlzKVxyXG4gICAge1xyXG4gICAgICBuZWlnaGJvcnMuYWRkKGVkZ2UudGFyZ2V0KTtcclxuICAgIH1cclxuICAgIGVsc2VcclxuICAgIHtcclxuICAgICAgaWYgKCFlZGdlLnRhcmdldCA9PSB0aGlzKVxyXG4gICAgICAgIHRocm93IFwiSW5jb3JyZWN0IGluY2lkZW5jeSFcIjtcclxuICAgICAgbmVpZ2hib3JzLmFkZChlZGdlLnNvdXJjZSk7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICByZXR1cm4gbmVpZ2hib3JzO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLndpdGhDaGlsZHJlbiA9IGZ1bmN0aW9uICgpXHJcbntcclxuICB2YXIgd2l0aE5laWdoYm9yc0xpc3QgPSBbXTtcclxuICB2YXIgY2hpbGROb2RlO1xyXG5cclxuICB3aXRoTmVpZ2hib3JzTGlzdC5wdXNoKHRoaXMpO1xyXG5cclxuICBpZiAodGhpcy5jaGlsZCAhPSBudWxsKVxyXG4gIHtcclxuICAgIHZhciBub2RlcyA9IHRoaXMuY2hpbGQuZ2V0Tm9kZXMoKTtcclxuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbm9kZXMubGVuZ3RoOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIGNoaWxkTm9kZSA9IG5vZGVzW2ldO1xyXG5cclxuICAgICAgd2l0aE5laWdoYm9yc0xpc3QgPSB3aXRoTmVpZ2hib3JzTGlzdC5jb25jYXQoY2hpbGROb2RlLndpdGhDaGlsZHJlbigpKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHJldHVybiB3aXRoTmVpZ2hib3JzTGlzdDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRFc3RpbWF0ZWRTaXplID0gZnVuY3Rpb24gKCkge1xyXG4gIGlmICh0aGlzLmVzdGltYXRlZFNpemUgPT0gSW50ZWdlci5NSU5fVkFMVUUpIHtcclxuICAgIHRocm93IFwiYXNzZXJ0IGZhaWxlZFwiO1xyXG4gIH1cclxuICByZXR1cm4gdGhpcy5lc3RpbWF0ZWRTaXplO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmNhbGNFc3RpbWF0ZWRTaXplID0gZnVuY3Rpb24gKCkge1xyXG4gIGlmICh0aGlzLmNoaWxkID09IG51bGwpXHJcbiAge1xyXG4gICAgcmV0dXJuIHRoaXMuZXN0aW1hdGVkU2l6ZSA9IE1hdGguZmxvb3IoKHRoaXMucmVjdC53aWR0aCArIHRoaXMucmVjdC5oZWlnaHQpIC8gMik7XHJcbiAgfVxyXG4gIGVsc2VcclxuICB7XHJcbiAgICB0aGlzLmVzdGltYXRlZFNpemUgPSB0aGlzLmNoaWxkLmNhbGNFc3RpbWF0ZWRTaXplKCk7XHJcbiAgICB0aGlzLnJlY3Qud2lkdGggPSB0aGlzLmVzdGltYXRlZFNpemU7XHJcbiAgICB0aGlzLnJlY3QuaGVpZ2h0ID0gdGhpcy5lc3RpbWF0ZWRTaXplO1xyXG5cclxuICAgIHJldHVybiB0aGlzLmVzdGltYXRlZFNpemU7XHJcbiAgfVxyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLnNjYXR0ZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgdmFyIHJhbmRvbUNlbnRlclg7XHJcbiAgdmFyIHJhbmRvbUNlbnRlclk7XHJcblxyXG4gIHZhciBtaW5YID0gLUxheW91dENvbnN0YW50cy5JTklUSUFMX1dPUkxEX0JPVU5EQVJZO1xyXG4gIHZhciBtYXhYID0gTGF5b3V0Q29uc3RhbnRzLklOSVRJQUxfV09STERfQk9VTkRBUlk7XHJcbiAgcmFuZG9tQ2VudGVyWCA9IExheW91dENvbnN0YW50cy5XT1JMRF9DRU5URVJfWCArXHJcbiAgICAgICAgICAoUmFuZG9tU2VlZC5uZXh0RG91YmxlKCkgKiAobWF4WCAtIG1pblgpKSArIG1pblg7XHJcblxyXG4gIHZhciBtaW5ZID0gLUxheW91dENvbnN0YW50cy5JTklUSUFMX1dPUkxEX0JPVU5EQVJZO1xyXG4gIHZhciBtYXhZID0gTGF5b3V0Q29uc3RhbnRzLklOSVRJQUxfV09STERfQk9VTkRBUlk7XHJcbiAgcmFuZG9tQ2VudGVyWSA9IExheW91dENvbnN0YW50cy5XT1JMRF9DRU5URVJfWSArXHJcbiAgICAgICAgICAoUmFuZG9tU2VlZC5uZXh0RG91YmxlKCkgKiAobWF4WSAtIG1pblkpKSArIG1pblk7XHJcblxyXG4gIHRoaXMucmVjdC54ID0gcmFuZG9tQ2VudGVyWDtcclxuICB0aGlzLnJlY3QueSA9IHJhbmRvbUNlbnRlcllcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS51cGRhdGVCb3VuZHMgPSBmdW5jdGlvbiAoKSB7XHJcbiAgaWYgKHRoaXMuZ2V0Q2hpbGQoKSA9PSBudWxsKSB7XHJcbiAgICB0aHJvdyBcImFzc2VydCBmYWlsZWRcIjtcclxuICB9XHJcbiAgaWYgKHRoaXMuZ2V0Q2hpbGQoKS5nZXROb2RlcygpLmxlbmd0aCAhPSAwKVxyXG4gIHtcclxuICAgIC8vIHdyYXAgdGhlIGNoaWxkcmVuIG5vZGVzIGJ5IHJlLWFycmFuZ2luZyB0aGUgYm91bmRhcmllc1xyXG4gICAgdmFyIGNoaWxkR3JhcGggPSB0aGlzLmdldENoaWxkKCk7XHJcbiAgICBjaGlsZEdyYXBoLnVwZGF0ZUJvdW5kcyh0cnVlKTtcclxuXHJcbiAgICB0aGlzLnJlY3QueCA9IGNoaWxkR3JhcGguZ2V0TGVmdCgpO1xyXG4gICAgdGhpcy5yZWN0LnkgPSBjaGlsZEdyYXBoLmdldFRvcCgpO1xyXG5cclxuICAgIHRoaXMuc2V0V2lkdGgoY2hpbGRHcmFwaC5nZXRSaWdodCgpIC0gY2hpbGRHcmFwaC5nZXRMZWZ0KCkgK1xyXG4gICAgICAgICAgICAyICogTGF5b3V0Q29uc3RhbnRzLkNPTVBPVU5EX05PREVfTUFSR0lOKTtcclxuICAgIHRoaXMuc2V0SGVpZ2h0KGNoaWxkR3JhcGguZ2V0Qm90dG9tKCkgLSBjaGlsZEdyYXBoLmdldFRvcCgpICtcclxuICAgICAgICAgICAgMiAqIExheW91dENvbnN0YW50cy5DT01QT1VORF9OT0RFX01BUkdJTiArXHJcbiAgICAgICAgICAgIExheW91dENvbnN0YW50cy5MQUJFTF9IRUlHSFQpO1xyXG4gIH1cclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRJbmNsdXNpb25UcmVlRGVwdGggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgaWYgKHRoaXMuaW5jbHVzaW9uVHJlZURlcHRoID09IEludGVnZXIuTUFYX1ZBTFVFKSB7XHJcbiAgICB0aHJvdyBcImFzc2VydCBmYWlsZWRcIjtcclxuICB9XHJcbiAgcmV0dXJuIHRoaXMuaW5jbHVzaW9uVHJlZURlcHRoO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLnRyYW5zZm9ybSA9IGZ1bmN0aW9uICh0cmFucylcclxue1xyXG4gIHZhciBsZWZ0ID0gdGhpcy5yZWN0Lng7XHJcblxyXG4gIGlmIChsZWZ0ID4gTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZKVxyXG4gIHtcclxuICAgIGxlZnQgPSBMYXlvdXRDb25zdGFudHMuV09STERfQk9VTkRBUlk7XHJcbiAgfVxyXG4gIGVsc2UgaWYgKGxlZnQgPCAtTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZKVxyXG4gIHtcclxuICAgIGxlZnQgPSAtTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZO1xyXG4gIH1cclxuXHJcbiAgdmFyIHRvcCA9IHRoaXMucmVjdC55O1xyXG5cclxuICBpZiAodG9wID4gTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZKVxyXG4gIHtcclxuICAgIHRvcCA9IExheW91dENvbnN0YW50cy5XT1JMRF9CT1VOREFSWTtcclxuICB9XHJcbiAgZWxzZSBpZiAodG9wIDwgLUxheW91dENvbnN0YW50cy5XT1JMRF9CT1VOREFSWSlcclxuICB7XHJcbiAgICB0b3AgPSAtTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZO1xyXG4gIH1cclxuXHJcbiAgdmFyIGxlZnRUb3AgPSBuZXcgUG9pbnREKGxlZnQsIHRvcCk7XHJcbiAgdmFyIHZMZWZ0VG9wID0gdHJhbnMuaW52ZXJzZVRyYW5zZm9ybVBvaW50KGxlZnRUb3ApO1xyXG5cclxuICB0aGlzLnNldExvY2F0aW9uKHZMZWZ0VG9wLngsIHZMZWZ0VG9wLnkpO1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldExlZnQgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMucmVjdC54O1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldFJpZ2h0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnJlY3QueCArIHRoaXMucmVjdC53aWR0aDtcclxufTtcclxuXHJcbkxOb2RlLnByb3RvdHlwZS5nZXRUb3AgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMucmVjdC55O1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldEJvdHRvbSA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5yZWN0LnkgKyB0aGlzLnJlY3QuaGVpZ2h0O1xyXG59O1xyXG5cclxuTE5vZGUucHJvdG90eXBlLmdldFBhcmVudCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICBpZiAodGhpcy5vd25lciA9PSBudWxsKVxyXG4gIHtcclxuICAgIHJldHVybiBudWxsO1xyXG4gIH1cclxuXHJcbiAgcmV0dXJuIHRoaXMub3duZXIuZ2V0UGFyZW50KCk7XHJcbn07XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IExOb2RlO1xyXG4iLCJ2YXIgTGF5b3V0Q29uc3RhbnRzID0gcmVxdWlyZSgnLi9MYXlvdXRDb25zdGFudHMnKTtcclxudmFyIEhhc2hNYXAgPSByZXF1aXJlKCcuL0hhc2hNYXAnKTtcclxudmFyIExHcmFwaE1hbmFnZXIgPSByZXF1aXJlKCcuL0xHcmFwaE1hbmFnZXInKTtcclxuXHJcbmZ1bmN0aW9uIExheW91dChpc1JlbW90ZVVzZSkge1xyXG4gIC8vTGF5b3V0IFF1YWxpdHk6IDA6cHJvb2YsIDE6ZGVmYXVsdCwgMjpkcmFmdFxyXG4gIHRoaXMubGF5b3V0UXVhbGl0eSA9IExheW91dENvbnN0YW50cy5ERUZBVUxUX1FVQUxJVFk7XHJcbiAgLy9XaGV0aGVyIGxheW91dCBzaG91bGQgY3JlYXRlIGJlbmRwb2ludHMgYXMgbmVlZGVkIG9yIG5vdFxyXG4gIHRoaXMuY3JlYXRlQmVuZHNBc05lZWRlZCA9XHJcbiAgICAgICAgICBMYXlvdXRDb25zdGFudHMuREVGQVVMVF9DUkVBVEVfQkVORFNfQVNfTkVFREVEO1xyXG4gIC8vV2hldGhlciBsYXlvdXQgc2hvdWxkIGJlIGluY3JlbWVudGFsIG9yIG5vdFxyXG4gIHRoaXMuaW5jcmVtZW50YWwgPSBMYXlvdXRDb25zdGFudHMuREVGQVVMVF9JTkNSRU1FTlRBTDtcclxuICAvL1doZXRoZXIgd2UgYW5pbWF0ZSBmcm9tIGJlZm9yZSB0byBhZnRlciBsYXlvdXQgbm9kZSBwb3NpdGlvbnNcclxuICB0aGlzLmFuaW1hdGlvbk9uTGF5b3V0ID1cclxuICAgICAgICAgIExheW91dENvbnN0YW50cy5ERUZBVUxUX0FOSU1BVElPTl9PTl9MQVlPVVQ7XHJcbiAgLy9XaGV0aGVyIHdlIGFuaW1hdGUgdGhlIGxheW91dCBwcm9jZXNzIG9yIG5vdFxyXG4gIHRoaXMuYW5pbWF0aW9uRHVyaW5nTGF5b3V0ID0gTGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfQU5JTUFUSU9OX0RVUklOR19MQVlPVVQ7XHJcbiAgLy9OdW1iZXIgaXRlcmF0aW9ucyB0aGF0IHNob3VsZCBiZSBkb25lIGJldHdlZW4gdHdvIHN1Y2Nlc3NpdmUgYW5pbWF0aW9uc1xyXG4gIHRoaXMuYW5pbWF0aW9uUGVyaW9kID0gTGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfQU5JTUFUSU9OX1BFUklPRDtcclxuICAvKipcclxuICAgKiBXaGV0aGVyIG9yIG5vdCBsZWFmIG5vZGVzIChub24tY29tcG91bmQgbm9kZXMpIGFyZSBvZiB1bmlmb3JtIHNpemVzLiBXaGVuXHJcbiAgICogdGhleSBhcmUsIGJvdGggc3ByaW5nIGFuZCByZXB1bHNpb24gZm9yY2VzIGJldHdlZW4gdHdvIGxlYWYgbm9kZXMgY2FuIGJlXHJcbiAgICogY2FsY3VsYXRlZCB3aXRob3V0IHRoZSBleHBlbnNpdmUgY2xpcHBpbmcgcG9pbnQgY2FsY3VsYXRpb25zLCByZXN1bHRpbmdcclxuICAgKiBpbiBtYWpvciBzcGVlZC11cC5cclxuICAgKi9cclxuICB0aGlzLnVuaWZvcm1MZWFmTm9kZVNpemVzID1cclxuICAgICAgICAgIExheW91dENvbnN0YW50cy5ERUZBVUxUX1VOSUZPUk1fTEVBRl9OT0RFX1NJWkVTO1xyXG4gIC8qKlxyXG4gICAqIFRoaXMgaXMgdXNlZCBmb3IgY3JlYXRpb24gb2YgYmVuZHBvaW50cyBieSB1c2luZyBkdW1teSBub2RlcyBhbmQgZWRnZXMuXHJcbiAgICogTWFwcyBhbiBMRWRnZSB0byBpdHMgZHVtbXkgYmVuZHBvaW50IHBhdGguXHJcbiAgICovXHJcbiAgdGhpcy5lZGdlVG9EdW1teU5vZGVzID0gbmV3IEhhc2hNYXAoKTtcclxuICB0aGlzLmdyYXBoTWFuYWdlciA9IG5ldyBMR3JhcGhNYW5hZ2VyKHRoaXMpO1xyXG4gIHRoaXMuaXNMYXlvdXRGaW5pc2hlZCA9IGZhbHNlO1xyXG4gIHRoaXMuaXNTdWJMYXlvdXQgPSBmYWxzZTtcclxuICB0aGlzLmlzUmVtb3RlVXNlID0gZmFsc2U7XHJcblxyXG4gIGlmIChpc1JlbW90ZVVzZSAhPSBudWxsKSB7XHJcbiAgICB0aGlzLmlzUmVtb3RlVXNlID0gaXNSZW1vdGVVc2U7XHJcbiAgfVxyXG59XHJcblxyXG5MYXlvdXQuUkFORE9NX1NFRUQgPSAxO1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5nZXRHcmFwaE1hbmFnZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMuZ3JhcGhNYW5hZ2VyO1xyXG59O1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5nZXRBbGxOb2RlcyA9IGZ1bmN0aW9uICgpIHtcclxuICByZXR1cm4gdGhpcy5ncmFwaE1hbmFnZXIuZ2V0QWxsTm9kZXMoKTtcclxufTtcclxuXHJcbkxheW91dC5wcm90b3R5cGUuZ2V0QWxsRWRnZXMgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMuZ3JhcGhNYW5hZ2VyLmdldEFsbEVkZ2VzKCk7XHJcbn07XHJcblxyXG5MYXlvdXQucHJvdG90eXBlLmdldEFsbE5vZGVzVG9BcHBseUdyYXZpdGF0aW9uID0gZnVuY3Rpb24gKCkge1xyXG4gIHJldHVybiB0aGlzLmdyYXBoTWFuYWdlci5nZXRBbGxOb2Rlc1RvQXBwbHlHcmF2aXRhdGlvbigpO1xyXG59O1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5uZXdHcmFwaE1hbmFnZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgdmFyIGdtID0gbmV3IExHcmFwaE1hbmFnZXIodGhpcyk7XHJcbiAgdGhpcy5ncmFwaE1hbmFnZXIgPSBnbTtcclxuICByZXR1cm4gZ207XHJcbn07XHJcblxyXG5MYXlvdXQucHJvdG90eXBlLm5ld0dyYXBoID0gZnVuY3Rpb24gKHZHcmFwaClcclxue1xyXG4gIHJldHVybiBuZXcgTEdyYXBoKG51bGwsIHRoaXMuZ3JhcGhNYW5hZ2VyLCB2R3JhcGgpO1xyXG59O1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5uZXdOb2RlID0gZnVuY3Rpb24gKHZOb2RlKVxyXG57XHJcbiAgcmV0dXJuIG5ldyBMTm9kZSh0aGlzLmdyYXBoTWFuYWdlciwgdk5vZGUpO1xyXG59O1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5uZXdFZGdlID0gZnVuY3Rpb24gKHZFZGdlKVxyXG57XHJcbiAgcmV0dXJuIG5ldyBMRWRnZShudWxsLCBudWxsLCB2RWRnZSk7XHJcbn07XHJcblxyXG5MYXlvdXQucHJvdG90eXBlLnJ1bkxheW91dCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICB0aGlzLmlzTGF5b3V0RmluaXNoZWQgPSBmYWxzZTtcclxuXHJcbiAgdGhpcy5pbml0UGFyYW1ldGVycygpO1xyXG4gIHZhciBpc0xheW91dFN1Y2Nlc3NmdWxsO1xyXG5cclxuICBpZiAoKHRoaXMuZ3JhcGhNYW5hZ2VyLmdldFJvb3QoKSA9PSBudWxsKVxyXG4gICAgICAgICAgfHwgdGhpcy5ncmFwaE1hbmFnZXIuZ2V0Um9vdCgpLmdldE5vZGVzKCkubGVuZ3RoID09IDBcclxuICAgICAgICAgIHx8IHRoaXMuZ3JhcGhNYW5hZ2VyLmluY2x1ZGVzSW52YWxpZEVkZ2UoKSlcclxuICB7XHJcbiAgICBpc0xheW91dFN1Y2Nlc3NmdWxsID0gZmFsc2U7XHJcbiAgfVxyXG4gIGVsc2VcclxuICB7XHJcbiAgICAvLyBjYWxjdWxhdGUgZXhlY3V0aW9uIHRpbWVcclxuICAgIHZhciBzdGFydFRpbWUgPSAwO1xyXG5cclxuICAgIGlmICghdGhpcy5pc1N1YkxheW91dClcclxuICAgIHtcclxuICAgICAgc3RhcnRUaW1lID0gbmV3IERhdGUoKS5nZXRUaW1lKClcclxuICAgIH1cclxuXHJcbiAgICBpc0xheW91dFN1Y2Nlc3NmdWxsID0gdGhpcy5sYXlvdXQoKTtcclxuXHJcbiAgICBpZiAoIXRoaXMuaXNTdWJMYXlvdXQpXHJcbiAgICB7XHJcbiAgICAgIHZhciBlbmRUaW1lID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcbiAgICAgIHZhciBleGNUaW1lID0gZW5kVGltZSAtIHN0YXJ0VGltZTtcclxuXHJcbiAgICAgIGNvbnNvbGUubG9nKFwiVG90YWwgZXhlY3V0aW9uIHRpbWU6IFwiICsgZXhjVGltZSArIFwiIG1pbGlzZWNvbmRzLlwiKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIGlmIChpc0xheW91dFN1Y2Nlc3NmdWxsKVxyXG4gIHtcclxuICAgIGlmICghdGhpcy5pc1N1YkxheW91dClcclxuICAgIHtcclxuICAgICAgdGhpcy5kb1Bvc3RMYXlvdXQoKTtcclxuICAgIH1cclxuICB9XHJcblxyXG4gIHRoaXMuaXNMYXlvdXRGaW5pc2hlZCA9IHRydWU7XHJcblxyXG4gIHJldHVybiBpc0xheW91dFN1Y2Nlc3NmdWxsO1xyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgbWV0aG9kIHBlcmZvcm1zIHRoZSBvcGVyYXRpb25zIHJlcXVpcmVkIGFmdGVyIGxheW91dC5cclxuICovXHJcbkxheW91dC5wcm90b3R5cGUuZG9Qb3N0TGF5b3V0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIC8vYXNzZXJ0ICFpc1N1YkxheW91dCA6IFwiU2hvdWxkIG5vdCBiZSBjYWxsZWQgb24gc3ViLWxheW91dCFcIjtcclxuICAvLyBQcm9wYWdhdGUgZ2VvbWV0cmljIGNoYW5nZXMgdG8gdi1sZXZlbCBvYmplY3RzXHJcbiAgdGhpcy50cmFuc2Zvcm0oKTtcclxuICB0aGlzLnVwZGF0ZSgpO1xyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgbWV0aG9kIHVwZGF0ZXMgdGhlIGdlb21ldHJ5IG9mIHRoZSB0YXJnZXQgZ3JhcGggYWNjb3JkaW5nIHRvXHJcbiAqIGNhbGN1bGF0ZWQgbGF5b3V0LlxyXG4gKi9cclxuTGF5b3V0LnByb3RvdHlwZS51cGRhdGUyID0gZnVuY3Rpb24gKCkge1xyXG4gIC8vIHVwZGF0ZSBiZW5kIHBvaW50c1xyXG4gIGlmICh0aGlzLmNyZWF0ZUJlbmRzQXNOZWVkZWQpXHJcbiAge1xyXG4gICAgdGhpcy5jcmVhdGVCZW5kcG9pbnRzRnJvbUR1bW15Tm9kZXMoKTtcclxuXHJcbiAgICAvLyByZXNldCBhbGwgZWRnZXMsIHNpbmNlIHRoZSB0b3BvbG9neSBoYXMgY2hhbmdlZFxyXG4gICAgdGhpcy5ncmFwaE1hbmFnZXIucmVzZXRBbGxFZGdlcygpO1xyXG4gIH1cclxuXHJcbiAgLy8gcGVyZm9ybSBlZGdlLCBub2RlIGFuZCByb290IHVwZGF0ZXMgaWYgbGF5b3V0IGlzIG5vdCBjYWxsZWRcclxuICAvLyByZW1vdGVseVxyXG4gIGlmICghdGhpcy5pc1JlbW90ZVVzZSlcclxuICB7XHJcbiAgICAvLyB1cGRhdGUgYWxsIGVkZ2VzXHJcbiAgICB2YXIgZWRnZTtcclxuICAgIHZhciBhbGxFZGdlcyA9IHRoaXMuZ3JhcGhNYW5hZ2VyLmdldEFsbEVkZ2VzKCk7XHJcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGFsbEVkZ2VzLmxlbmd0aDsgaSsrKVxyXG4gICAge1xyXG4gICAgICBlZGdlID0gYWxsRWRnZXNbaV07XHJcbi8vICAgICAgdGhpcy51cGRhdGUoZWRnZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLy8gcmVjdXJzaXZlbHkgdXBkYXRlIG5vZGVzXHJcbiAgICB2YXIgbm9kZTtcclxuICAgIHZhciBub2RlcyA9IHRoaXMuZ3JhcGhNYW5hZ2VyLmdldFJvb3QoKS5nZXROb2RlcygpO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub2Rlcy5sZW5ndGg7IGkrKylcclxuICAgIHtcclxuICAgICAgbm9kZSA9IG5vZGVzW2ldO1xyXG4vLyAgICAgIHRoaXMudXBkYXRlKG5vZGUpO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIHVwZGF0ZSByb290IGdyYXBoXHJcbiAgICB0aGlzLnVwZGF0ZSh0aGlzLmdyYXBoTWFuYWdlci5nZXRSb290KCkpO1xyXG4gIH1cclxufTtcclxuXHJcbkxheW91dC5wcm90b3R5cGUudXBkYXRlID0gZnVuY3Rpb24gKG9iaikge1xyXG4gIGlmIChvYmogPT0gbnVsbCkge1xyXG4gICAgdGhpcy51cGRhdGUyKCk7XHJcbiAgfVxyXG4gIGVsc2UgaWYgKG9iaiBpbnN0YW5jZW9mIExOb2RlKSB7XHJcbiAgICB2YXIgbm9kZSA9IG9iajtcclxuICAgIGlmIChub2RlLmdldENoaWxkKCkgIT0gbnVsbClcclxuICAgIHtcclxuICAgICAgLy8gc2luY2Ugbm9kZSBpcyBjb21wb3VuZCwgcmVjdXJzaXZlbHkgdXBkYXRlIGNoaWxkIG5vZGVzXHJcbiAgICAgIHZhciBub2RlcyA9IG5vZGUuZ2V0Q2hpbGQoKS5nZXROb2RlcygpO1xyXG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IG5vZGVzLmxlbmd0aDsgaSsrKVxyXG4gICAgICB7XHJcbiAgICAgICAgdXBkYXRlKG5vZGVzW2ldKTtcclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8vIGlmIHRoZSBsLWxldmVsIG5vZGUgaXMgYXNzb2NpYXRlZCB3aXRoIGEgdi1sZXZlbCBncmFwaCBvYmplY3QsXHJcbiAgICAvLyB0aGVuIGl0IGlzIGFzc3VtZWQgdGhhdCB0aGUgdi1sZXZlbCBub2RlIGltcGxlbWVudHMgdGhlXHJcbiAgICAvLyBpbnRlcmZhY2UgVXBkYXRhYmxlLlxyXG4gICAgaWYgKG5vZGUudkdyYXBoT2JqZWN0ICE9IG51bGwpXHJcbiAgICB7XHJcbiAgICAgIC8vIGNhc3QgdG8gVXBkYXRhYmxlIHdpdGhvdXQgYW55IHR5cGUgY2hlY2tcclxuICAgICAgdmFyIHZOb2RlID0gbm9kZS52R3JhcGhPYmplY3Q7XHJcblxyXG4gICAgICAvLyBjYWxsIHRoZSB1cGRhdGUgbWV0aG9kIG9mIHRoZSBpbnRlcmZhY2VcclxuICAgICAgdk5vZGUudXBkYXRlKG5vZGUpO1xyXG4gICAgfVxyXG4gIH1cclxuICBlbHNlIGlmIChvYmogaW5zdGFuY2VvZiBMRWRnZSkge1xyXG4gICAgdmFyIGVkZ2UgPSBvYmo7XHJcbiAgICAvLyBpZiB0aGUgbC1sZXZlbCBlZGdlIGlzIGFzc29jaWF0ZWQgd2l0aCBhIHYtbGV2ZWwgZ3JhcGggb2JqZWN0LFxyXG4gICAgLy8gdGhlbiBpdCBpcyBhc3N1bWVkIHRoYXQgdGhlIHYtbGV2ZWwgZWRnZSBpbXBsZW1lbnRzIHRoZVxyXG4gICAgLy8gaW50ZXJmYWNlIFVwZGF0YWJsZS5cclxuXHJcbiAgICBpZiAoZWRnZS52R3JhcGhPYmplY3QgIT0gbnVsbClcclxuICAgIHtcclxuICAgICAgLy8gY2FzdCB0byBVcGRhdGFibGUgd2l0aG91dCBhbnkgdHlwZSBjaGVja1xyXG4gICAgICB2YXIgdkVkZ2UgPSBlZGdlLnZHcmFwaE9iamVjdDtcclxuXHJcbiAgICAgIC8vIGNhbGwgdGhlIHVwZGF0ZSBtZXRob2Qgb2YgdGhlIGludGVyZmFjZVxyXG4gICAgICB2RWRnZS51cGRhdGUoZWRnZSk7XHJcbiAgICB9XHJcbiAgfVxyXG4gIGVsc2UgaWYgKG9iaiBpbnN0YW5jZW9mIExHcmFwaCkge1xyXG4gICAgdmFyIGdyYXBoID0gb2JqO1xyXG4gICAgLy8gaWYgdGhlIGwtbGV2ZWwgZ3JhcGggaXMgYXNzb2NpYXRlZCB3aXRoIGEgdi1sZXZlbCBncmFwaCBvYmplY3QsXHJcbiAgICAvLyB0aGVuIGl0IGlzIGFzc3VtZWQgdGhhdCB0aGUgdi1sZXZlbCBvYmplY3QgaW1wbGVtZW50cyB0aGVcclxuICAgIC8vIGludGVyZmFjZSBVcGRhdGFibGUuXHJcblxyXG4gICAgaWYgKGdyYXBoLnZHcmFwaE9iamVjdCAhPSBudWxsKVxyXG4gICAge1xyXG4gICAgICAvLyBjYXN0IHRvIFVwZGF0YWJsZSB3aXRob3V0IGFueSB0eXBlIGNoZWNrXHJcbiAgICAgIHZhciB2R3JhcGggPSBncmFwaC52R3JhcGhPYmplY3Q7XHJcblxyXG4gICAgICAvLyBjYWxsIHRoZSB1cGRhdGUgbWV0aG9kIG9mIHRoZSBpbnRlcmZhY2VcclxuICAgICAgdkdyYXBoLnVwZGF0ZShncmFwaCk7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgbWV0aG9kIGlzIHVzZWQgdG8gc2V0IGFsbCBsYXlvdXQgcGFyYW1ldGVycyB0byBkZWZhdWx0IHZhbHVlc1xyXG4gKiBkZXRlcm1pbmVkIGF0IGNvbXBpbGUgdGltZS5cclxuICovXHJcbkxheW91dC5wcm90b3R5cGUuaW5pdFBhcmFtZXRlcnMgPSBmdW5jdGlvbiAoKSB7XHJcbiAgaWYgKCF0aGlzLmlzU3ViTGF5b3V0KVxyXG4gIHtcclxuICAgIHRoaXMubGF5b3V0UXVhbGl0eSA9IGxheW91dE9wdGlvbnNQYWNrLmxheW91dFF1YWxpdHk7XHJcbiAgICB0aGlzLmFuaW1hdGlvbkR1cmluZ0xheW91dCA9IGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGlvbkR1cmluZ0xheW91dDtcclxuICAgIHRoaXMuYW5pbWF0aW9uUGVyaW9kID0gTWF0aC5mbG9vcihMYXlvdXQudHJhbnNmb3JtKGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGlvblBlcmlvZCxcclxuICAgICAgICAgICAgTGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfQU5JTUFUSU9OX1BFUklPRCkpO1xyXG4gICAgdGhpcy5hbmltYXRpb25PbkxheW91dCA9IGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGlvbk9uTGF5b3V0O1xyXG4gICAgdGhpcy5pbmNyZW1lbnRhbCA9IGxheW91dE9wdGlvbnNQYWNrLmluY3JlbWVudGFsO1xyXG4gICAgdGhpcy5jcmVhdGVCZW5kc0FzTmVlZGVkID0gbGF5b3V0T3B0aW9uc1BhY2suY3JlYXRlQmVuZHNBc05lZWRlZDtcclxuICAgIHRoaXMudW5pZm9ybUxlYWZOb2RlU2l6ZXMgPSBsYXlvdXRPcHRpb25zUGFjay51bmlmb3JtTGVhZk5vZGVTaXplcztcclxuICB9XHJcblxyXG4gIGlmICh0aGlzLmFuaW1hdGlvbkR1cmluZ0xheW91dClcclxuICB7XHJcbiAgICBhbmltYXRpb25PbkxheW91dCA9IGZhbHNlO1xyXG4gIH1cclxufTtcclxuXHJcbkxheW91dC5wcm90b3R5cGUudHJhbnNmb3JtID0gZnVuY3Rpb24gKG5ld0xlZnRUb3ApIHtcclxuICBpZiAobmV3TGVmdFRvcCA9PSB1bmRlZmluZWQpIHtcclxuICAgIHRoaXMudHJhbnNmb3JtKG5ldyBQb2ludEQoMCwgMCkpO1xyXG4gIH1cclxuICBlbHNlIHtcclxuICAgIC8vIGNyZWF0ZSBhIHRyYW5zZm9ybWF0aW9uIG9iamVjdCAoZnJvbSBFY2xpcHNlIHRvIGxheW91dCkuIFdoZW4gYW5cclxuICAgIC8vIGludmVyc2UgdHJhbnNmb3JtIGlzIGFwcGxpZWQsIHdlIGdldCB1cHBlci1sZWZ0IGNvb3JkaW5hdGUgb2YgdGhlXHJcbiAgICAvLyBkcmF3aW5nIG9yIHRoZSByb290IGdyYXBoIGF0IGdpdmVuIGlucHV0IGNvb3JkaW5hdGUgKHNvbWUgbWFyZ2luc1xyXG4gICAgLy8gYWxyZWFkeSBpbmNsdWRlZCBpbiBjYWxjdWxhdGlvbiBvZiBsZWZ0LXRvcCkuXHJcblxyXG4gICAgdmFyIHRyYW5zID0gbmV3IFRyYW5zZm9ybSgpO1xyXG4gICAgdmFyIGxlZnRUb3AgPSB0aGlzLmdyYXBoTWFuYWdlci5nZXRSb290KCkudXBkYXRlTGVmdFRvcCgpO1xyXG5cclxuICAgIGlmIChsZWZ0VG9wICE9IG51bGwpXHJcbiAgICB7XHJcbiAgICAgIHRyYW5zLnNldFdvcmxkT3JnWChuZXdMZWZ0VG9wLngpO1xyXG4gICAgICB0cmFucy5zZXRXb3JsZE9yZ1kobmV3TGVmdFRvcC55KTtcclxuXHJcbiAgICAgIHRyYW5zLnNldERldmljZU9yZ1gobGVmdFRvcC54KTtcclxuICAgICAgdHJhbnMuc2V0RGV2aWNlT3JnWShsZWZ0VG9wLnkpO1xyXG5cclxuICAgICAgdmFyIG5vZGVzID0gdGhpcy5nZXRBbGxOb2RlcygpO1xyXG4gICAgICB2YXIgbm9kZTtcclxuXHJcbiAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbm9kZXMubGVuZ3RoOyBpKyspXHJcbiAgICAgIHtcclxuICAgICAgICBub2RlID0gbm9kZXNbaV07XHJcbiAgICAgICAgbm9kZS50cmFuc2Zvcm0odHJhbnMpO1xyXG4gICAgICB9XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuTGF5b3V0LnByb3RvdHlwZS5wb3NpdGlvbk5vZGVzUmFuZG9tbHkgPSBmdW5jdGlvbiAoZ3JhcGgpIHtcclxuXHJcbiAgaWYgKGdyYXBoID09IHVuZGVmaW5lZCkge1xyXG4gICAgLy9hc3NlcnQgIXRoaXMuaW5jcmVtZW50YWw7XHJcbiAgICB0aGlzLnBvc2l0aW9uTm9kZXNSYW5kb21seSh0aGlzLmdldEdyYXBoTWFuYWdlcigpLmdldFJvb3QoKSk7XHJcbiAgICB0aGlzLmdldEdyYXBoTWFuYWdlcigpLmdldFJvb3QoKS51cGRhdGVCb3VuZHModHJ1ZSk7XHJcbiAgfVxyXG4gIGVsc2Uge1xyXG4gICAgdmFyIGxOb2RlO1xyXG4gICAgdmFyIGNoaWxkR3JhcGg7XHJcblxyXG4gICAgdmFyIG5vZGVzID0gZ3JhcGguZ2V0Tm9kZXMoKTtcclxuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbm9kZXMubGVuZ3RoOyBpKyspXHJcbiAgICB7XHJcbiAgICAgIGxOb2RlID0gbm9kZXNbaV07XHJcbiAgICAgIGNoaWxkR3JhcGggPSBsTm9kZS5nZXRDaGlsZCgpO1xyXG5cclxuICAgICAgaWYgKGNoaWxkR3JhcGggPT0gbnVsbClcclxuICAgICAge1xyXG4gICAgICAgIGxOb2RlLnNjYXR0ZXIoKTtcclxuICAgICAgfVxyXG4gICAgICBlbHNlIGlmIChjaGlsZEdyYXBoLmdldE5vZGVzKCkubGVuZ3RoID09IDApXHJcbiAgICAgIHtcclxuICAgICAgICBsTm9kZS5zY2F0dGVyKCk7XHJcbiAgICAgIH1cclxuICAgICAgZWxzZVxyXG4gICAgICB7XHJcbiAgICAgICAgdGhpcy5wb3NpdGlvbk5vZGVzUmFuZG9tbHkoY2hpbGRHcmFwaCk7XHJcbiAgICAgICAgbE5vZGUudXBkYXRlQm91bmRzKCk7XHJcbiAgICAgIH1cclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBtZXRob2QgcmV0dXJucyBhIGxpc3Qgb2YgdHJlZXMgd2hlcmUgZWFjaCB0cmVlIGlzIHJlcHJlc2VudGVkIGFzIGFcclxuICogbGlzdCBvZiBsLW5vZGVzLiBUaGUgbWV0aG9kIHJldHVybnMgYSBsaXN0IG9mIHNpemUgMCB3aGVuOlxyXG4gKiAtIFRoZSBncmFwaCBpcyBub3QgZmxhdCBvclxyXG4gKiAtIE9uZSBvZiB0aGUgY29tcG9uZW50KHMpIG9mIHRoZSBncmFwaCBpcyBub3QgYSB0cmVlLlxyXG4gKi9cclxuTGF5b3V0LnByb3RvdHlwZS5nZXRGbGF0Rm9yZXN0ID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHZhciBmbGF0Rm9yZXN0ID0gW107XHJcbiAgdmFyIGlzRm9yZXN0ID0gdHJ1ZTtcclxuXHJcbiAgLy8gUXVpY2sgcmVmZXJlbmNlIGZvciBhbGwgbm9kZXMgaW4gdGhlIGdyYXBoIG1hbmFnZXIgYXNzb2NpYXRlZCB3aXRoXHJcbiAgLy8gdGhpcyBsYXlvdXQuIFRoZSBsaXN0IHNob3VsZCBub3QgYmUgY2hhbmdlZC5cclxuICB2YXIgYWxsTm9kZXMgPSB0aGlzLmdyYXBoTWFuYWdlci5nZXRSb290KCkuZ2V0Tm9kZXMoKTtcclxuXHJcbiAgLy8gRmlyc3QgYmUgc3VyZSB0aGF0IHRoZSBncmFwaCBpcyBmbGF0XHJcbiAgdmFyIGlzRmxhdCA9IHRydWU7XHJcblxyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgYWxsTm9kZXMubGVuZ3RoOyBpKyspXHJcbiAge1xyXG4gICAgaWYgKGFsbE5vZGVzW2ldLmdldENoaWxkKCkgIT0gbnVsbClcclxuICAgIHtcclxuICAgICAgaXNGbGF0ID0gZmFsc2U7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICAvLyBSZXR1cm4gZW1wdHkgZm9yZXN0IGlmIHRoZSBncmFwaCBpcyBub3QgZmxhdC5cclxuICBpZiAoIWlzRmxhdClcclxuICB7XHJcbiAgICByZXR1cm4gZmxhdEZvcmVzdDtcclxuICB9XHJcblxyXG4gIC8vIFJ1biBCRlMgZm9yIGVhY2ggY29tcG9uZW50IG9mIHRoZSBncmFwaC5cclxuXHJcbiAgdmFyIHZpc2l0ZWQgPSBuZXcgSGFzaFNldCgpO1xyXG4gIHZhciB0b0JlVmlzaXRlZCA9IFtdO1xyXG4gIHZhciBwYXJlbnRzID0gbmV3IEhhc2hNYXAoKTtcclxuICB2YXIgdW5Qcm9jZXNzZWROb2RlcyA9IFtdO1xyXG5cclxuICB1blByb2Nlc3NlZE5vZGVzID0gdW5Qcm9jZXNzZWROb2Rlcy5jb25jYXQoYWxsTm9kZXMpO1xyXG5cclxuICAvLyBFYWNoIGl0ZXJhdGlvbiBvZiB0aGlzIGxvb3AgZmluZHMgYSBjb21wb25lbnQgb2YgdGhlIGdyYXBoIGFuZFxyXG4gIC8vIGRlY2lkZXMgd2hldGhlciBpdCBpcyBhIHRyZWUgb3Igbm90LiBJZiBpdCBpcyBhIHRyZWUsIGFkZHMgaXQgdG8gdGhlXHJcbiAgLy8gZm9yZXN0IGFuZCBjb250aW51ZWQgd2l0aCB0aGUgbmV4dCBjb21wb25lbnQuXHJcblxyXG4gIHdoaWxlICh1blByb2Nlc3NlZE5vZGVzLmxlbmd0aCA+IDAgJiYgaXNGb3Jlc3QpXHJcbiAge1xyXG4gICAgdG9CZVZpc2l0ZWQucHVzaCh1blByb2Nlc3NlZE5vZGVzWzBdKTtcclxuXHJcbiAgICAvLyBTdGFydCB0aGUgQkZTLiBFYWNoIGl0ZXJhdGlvbiBvZiB0aGlzIGxvb3AgdmlzaXRzIGEgbm9kZSBpbiBhXHJcbiAgICAvLyBCRlMgbWFubmVyLlxyXG4gICAgd2hpbGUgKHRvQmVWaXNpdGVkLmxlbmd0aCA+IDAgJiYgaXNGb3Jlc3QpXHJcbiAgICB7XHJcbiAgICAgIC8vcG9vbCBvcGVyYXRpb25cclxuICAgICAgdmFyIGN1cnJlbnROb2RlID0gdG9CZVZpc2l0ZWRbMF07XHJcbiAgICAgIHRvQmVWaXNpdGVkLnNwbGljZSgwLCAxKTtcclxuICAgICAgdmlzaXRlZC5hZGQoY3VycmVudE5vZGUpO1xyXG5cclxuICAgICAgLy8gVHJhdmVyc2UgYWxsIG5laWdoYm9ycyBvZiB0aGlzIG5vZGVcclxuICAgICAgdmFyIG5laWdoYm9yRWRnZXMgPSBjdXJyZW50Tm9kZS5nZXRFZGdlcygpO1xyXG5cclxuICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBuZWlnaGJvckVkZ2VzLmxlbmd0aDsgaSsrKVxyXG4gICAgICB7XHJcbiAgICAgICAgdmFyIGN1cnJlbnROZWlnaGJvciA9XHJcbiAgICAgICAgICAgICAgICBuZWlnaGJvckVkZ2VzW2ldLmdldE90aGVyRW5kKGN1cnJlbnROb2RlKTtcclxuXHJcbiAgICAgICAgLy8gSWYgQkZTIGlzIG5vdCBncm93aW5nIGZyb20gdGhpcyBuZWlnaGJvci5cclxuICAgICAgICBpZiAocGFyZW50cy5nZXQoY3VycmVudE5vZGUpICE9IGN1cnJlbnROZWlnaGJvcilcclxuICAgICAgICB7XHJcbiAgICAgICAgICAvLyBXZSBoYXZlbid0IHByZXZpb3VzbHkgdmlzaXRlZCB0aGlzIG5laWdoYm9yLlxyXG4gICAgICAgICAgaWYgKCF2aXNpdGVkLmNvbnRhaW5zKGN1cnJlbnROZWlnaGJvcikpXHJcbiAgICAgICAgICB7XHJcbiAgICAgICAgICAgIHRvQmVWaXNpdGVkLnB1c2goY3VycmVudE5laWdoYm9yKTtcclxuICAgICAgICAgICAgcGFyZW50cy5wdXQoY3VycmVudE5laWdoYm9yLCBjdXJyZW50Tm9kZSk7XHJcbiAgICAgICAgICB9XHJcbiAgICAgICAgICAvLyBTaW5jZSB3ZSBoYXZlIHByZXZpb3VzbHkgdmlzaXRlZCB0aGlzIG5laWdoYm9yIGFuZFxyXG4gICAgICAgICAgLy8gdGhpcyBuZWlnaGJvciBpcyBub3QgcGFyZW50IG9mIGN1cnJlbnROb2RlLCBnaXZlblxyXG4gICAgICAgICAgLy8gZ3JhcGggY29udGFpbnMgYSBjb21wb25lbnQgdGhhdCBpcyBub3QgdHJlZSwgaGVuY2VcclxuICAgICAgICAgIC8vIGl0IGlzIG5vdCBhIGZvcmVzdC5cclxuICAgICAgICAgIGVsc2VcclxuICAgICAgICAgIHtcclxuICAgICAgICAgICAgaXNGb3Jlc3QgPSBmYWxzZTtcclxuICAgICAgICAgICAgYnJlYWs7XHJcbiAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLy8gVGhlIGdyYXBoIGNvbnRhaW5zIGEgY29tcG9uZW50IHRoYXQgaXMgbm90IGEgdHJlZS4gRW1wdHlcclxuICAgIC8vIHByZXZpb3VzbHkgZm91bmQgdHJlZXMuIFRoZSBtZXRob2Qgd2lsbCBlbmQuXHJcbiAgICBpZiAoIWlzRm9yZXN0KVxyXG4gICAge1xyXG4gICAgICBmbGF0Rm9yZXN0ID0gW107XHJcbiAgICB9XHJcbiAgICAvLyBTYXZlIGN1cnJlbnRseSB2aXNpdGVkIG5vZGVzIGFzIGEgdHJlZSBpbiBvdXIgZm9yZXN0LiBSZXNldFxyXG4gICAgLy8gdmlzaXRlZCBhbmQgcGFyZW50cyBsaXN0cy4gQ29udGludWUgd2l0aCB0aGUgbmV4dCBjb21wb25lbnQgb2ZcclxuICAgIC8vIHRoZSBncmFwaCwgaWYgYW55LlxyXG4gICAgZWxzZVxyXG4gICAge1xyXG4gICAgICB2YXIgdGVtcCA9IFtdO1xyXG4gICAgICB2aXNpdGVkLmFkZEFsbFRvKHRlbXApO1xyXG4gICAgICBmbGF0Rm9yZXN0LnB1c2godGVtcCk7XHJcbiAgICAgIC8vZmxhdEZvcmVzdCA9IGZsYXRGb3Jlc3QuY29uY2F0KHRlbXApO1xyXG4gICAgICAvL3VuUHJvY2Vzc2VkTm9kZXMucmVtb3ZlQWxsKHZpc2l0ZWQpO1xyXG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHRlbXAubGVuZ3RoOyBpKyspIHtcclxuICAgICAgICB2YXIgdmFsdWUgPSB0ZW1wW2ldO1xyXG4gICAgICAgIHZhciBpbmRleCA9IHVuUHJvY2Vzc2VkTm9kZXMuaW5kZXhPZih2YWx1ZSk7XHJcbiAgICAgICAgaWYgKGluZGV4ID4gLTEpIHtcclxuICAgICAgICAgIHVuUHJvY2Vzc2VkTm9kZXMuc3BsaWNlKGluZGV4LCAxKTtcclxuICAgICAgICB9XHJcbiAgICAgIH1cclxuICAgICAgdmlzaXRlZCA9IG5ldyBIYXNoU2V0KCk7XHJcbiAgICAgIHBhcmVudHMgPSBuZXcgSGFzaE1hcCgpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcmV0dXJuIGZsYXRGb3Jlc3Q7XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBtZXRob2QgY3JlYXRlcyBkdW1teSBub2RlcyAoYW4gbC1sZXZlbCBub2RlIHdpdGggbWluaW1hbCBkaW1lbnNpb25zKVxyXG4gKiBmb3IgdGhlIGdpdmVuIGVkZ2UgKG9uZSBwZXIgYmVuZHBvaW50KS4gVGhlIGV4aXN0aW5nIGwtbGV2ZWwgc3RydWN0dXJlXHJcbiAqIGlzIHVwZGF0ZWQgYWNjb3JkaW5nbHkuXHJcbiAqL1xyXG5MYXlvdXQucHJvdG90eXBlLmNyZWF0ZUR1bW15Tm9kZXNGb3JCZW5kcG9pbnRzID0gZnVuY3Rpb24gKGVkZ2UpXHJcbntcclxuICB2YXIgZHVtbXlOb2RlcyA9IFtdO1xyXG4gIHZhciBwcmV2ID0gZWRnZS5zb3VyY2U7XHJcblxyXG4gIHZhciBncmFwaCA9IHRoaXMuZ3JhcGhNYW5hZ2VyLmNhbGNMb3dlc3RDb21tb25BbmNlc3RvcihlZGdlLnNvdXJjZSwgZWRnZS50YXJnZXQpO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGVkZ2UuYmVuZHBvaW50cy5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICAvLyBjcmVhdGUgbmV3IGR1bW15IG5vZGVcclxuICAgIHZhciBkdW1teU5vZGUgPSB0aGlzLm5ld05vZGUobnVsbCk7XHJcbiAgICBkdW1teU5vZGUuc2V0UmVjdChuZXcgUG9pbnQoMCwgMCksIG5ldyBEaW1lbnNpb24oMSwgMSkpO1xyXG5cclxuICAgIGdyYXBoLmFkZChkdW1teU5vZGUpO1xyXG5cclxuICAgIC8vIGNyZWF0ZSBuZXcgZHVtbXkgZWRnZSBiZXR3ZWVuIHByZXYgYW5kIGR1bW15IG5vZGVcclxuICAgIHZhciBkdW1teUVkZ2UgPSB0aGlzLm5ld0VkZ2UobnVsbCk7XHJcbiAgICB0aGlzLmdyYXBoTWFuYWdlci5hZGQoZHVtbXlFZGdlLCBwcmV2LCBkdW1teU5vZGUpO1xyXG5cclxuICAgIGR1bW15Tm9kZXMuYWRkKGR1bW15Tm9kZSk7XHJcbiAgICBwcmV2ID0gZHVtbXlOb2RlO1xyXG4gIH1cclxuXHJcbiAgdmFyIGR1bW15RWRnZSA9IHRoaXMubmV3RWRnZShudWxsKTtcclxuICB0aGlzLmdyYXBoTWFuYWdlci5hZGQoZHVtbXlFZGdlLCBwcmV2LCBlZGdlLnRhcmdldCk7XHJcblxyXG4gIHRoaXMuZWRnZVRvRHVtbXlOb2Rlcy5wdXQoZWRnZSwgZHVtbXlOb2Rlcyk7XHJcblxyXG4gIC8vIHJlbW92ZSByZWFsIGVkZ2UgZnJvbSBncmFwaCBtYW5hZ2VyIGlmIGl0IGlzIGludGVyLWdyYXBoXHJcbiAgaWYgKGVkZ2UuaXNJbnRlckdyYXBoKCkpXHJcbiAge1xyXG4gICAgdGhpcy5ncmFwaE1hbmFnZXIucmVtb3ZlKGVkZ2UpO1xyXG4gIH1cclxuICAvLyBlbHNlLCByZW1vdmUgdGhlIGVkZ2UgZnJvbSB0aGUgY3VycmVudCBncmFwaFxyXG4gIGVsc2VcclxuICB7XHJcbiAgICBncmFwaC5yZW1vdmUoZWRnZSk7XHJcbiAgfVxyXG5cclxuICByZXR1cm4gZHVtbXlOb2RlcztcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIG1ldGhvZCBjcmVhdGVzIGJlbmRwb2ludHMgZm9yIGVkZ2VzIGZyb20gdGhlIGR1bW15IG5vZGVzXHJcbiAqIGF0IGwtbGV2ZWwuXHJcbiAqL1xyXG5MYXlvdXQucHJvdG90eXBlLmNyZWF0ZUJlbmRwb2ludHNGcm9tRHVtbXlOb2RlcyA9IGZ1bmN0aW9uICgpXHJcbntcclxuICB2YXIgZWRnZXMgPSBbXTtcclxuICBlZGdlcyA9IGVkZ2VzLmNvbmNhdCh0aGlzLmdyYXBoTWFuYWdlci5nZXRBbGxFZGdlcygpKTtcclxuICBlZGdlcyA9IHRoaXMuZWRnZVRvRHVtbXlOb2Rlcy5rZXlTZXQoKS5jb25jYXQoZWRnZXMpO1xyXG5cclxuICBmb3IgKHZhciBrID0gMDsgayA8IGVkZ2VzLmxlbmd0aDsgaysrKVxyXG4gIHtcclxuICAgIHZhciBsRWRnZSA9IGVkZ2VzW2tdO1xyXG5cclxuICAgIGlmIChsRWRnZS5iZW5kcG9pbnRzLmxlbmd0aCA+IDApXHJcbiAgICB7XHJcbiAgICAgIHZhciBwYXRoID0gdGhpcy5lZGdlVG9EdW1teU5vZGVzLmdldChsRWRnZSk7XHJcblxyXG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHBhdGgubGVuZ3RoOyBpKyspXHJcbiAgICAgIHtcclxuICAgICAgICB2YXIgZHVtbXlOb2RlID0gcGF0aFtpXTtcclxuICAgICAgICB2YXIgcCA9IG5ldyBQb2ludEQoZHVtbXlOb2RlLmdldENlbnRlclgoKSxcclxuICAgICAgICAgICAgICAgIGR1bW15Tm9kZS5nZXRDZW50ZXJZKCkpO1xyXG5cclxuICAgICAgICAvLyB1cGRhdGUgYmVuZHBvaW50J3MgbG9jYXRpb24gYWNjb3JkaW5nIHRvIGR1bW15IG5vZGVcclxuICAgICAgICB2YXIgZWJwID0gbEVkZ2UuYmVuZHBvaW50cy5nZXQoaSk7XHJcbiAgICAgICAgZWJwLnggPSBwLng7XHJcbiAgICAgICAgZWJwLnkgPSBwLnk7XHJcblxyXG4gICAgICAgIC8vIHJlbW92ZSB0aGUgZHVtbXkgbm9kZSwgZHVtbXkgZWRnZXMgaW5jaWRlbnQgd2l0aCB0aGlzXHJcbiAgICAgICAgLy8gZHVtbXkgbm9kZSBpcyBhbHNvIHJlbW92ZWQgKHdpdGhpbiB0aGUgcmVtb3ZlIG1ldGhvZClcclxuICAgICAgICBkdW1teU5vZGUuZ2V0T3duZXIoKS5yZW1vdmUoZHVtbXlOb2RlKTtcclxuICAgICAgfVxyXG5cclxuICAgICAgLy8gYWRkIHRoZSByZWFsIGVkZ2UgdG8gZ3JhcGhcclxuICAgICAgdGhpcy5ncmFwaE1hbmFnZXIuYWRkKGxFZGdlLCBsRWRnZS5zb3VyY2UsIGxFZGdlLnRhcmdldCk7XHJcbiAgICB9XHJcbiAgfVxyXG59O1xyXG5cclxuTGF5b3V0LnRyYW5zZm9ybSA9IGZ1bmN0aW9uIChzbGlkZXJWYWx1ZSwgZGVmYXVsdFZhbHVlLCBtaW5EaXYsIG1heE11bCkge1xyXG4gIGlmIChtaW5EaXYgIT0gdW5kZWZpbmVkICYmIG1heE11bCAhPSB1bmRlZmluZWQpIHtcclxuICAgIHZhciB2YWx1ZSA9IGRlZmF1bHRWYWx1ZTtcclxuXHJcbiAgICBpZiAoc2xpZGVyVmFsdWUgPD0gNTApXHJcbiAgICB7XHJcbiAgICAgIHZhciBtaW5WYWx1ZSA9IGRlZmF1bHRWYWx1ZSAvIG1pbkRpdjtcclxuICAgICAgdmFsdWUgLT0gKChkZWZhdWx0VmFsdWUgLSBtaW5WYWx1ZSkgLyA1MCkgKiAoNTAgLSBzbGlkZXJWYWx1ZSk7XHJcbiAgICB9XHJcbiAgICBlbHNlXHJcbiAgICB7XHJcbiAgICAgIHZhciBtYXhWYWx1ZSA9IGRlZmF1bHRWYWx1ZSAqIG1heE11bDtcclxuICAgICAgdmFsdWUgKz0gKChtYXhWYWx1ZSAtIGRlZmF1bHRWYWx1ZSkgLyA1MCkgKiAoc2xpZGVyVmFsdWUgLSA1MCk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV0dXJuIHZhbHVlO1xyXG4gIH1cclxuICBlbHNlIHtcclxuICAgIHZhciBhLCBiO1xyXG5cclxuICAgIGlmIChzbGlkZXJWYWx1ZSA8PSA1MClcclxuICAgIHtcclxuICAgICAgYSA9IDkuMCAqIGRlZmF1bHRWYWx1ZSAvIDUwMC4wO1xyXG4gICAgICBiID0gZGVmYXVsdFZhbHVlIC8gMTAuMDtcclxuICAgIH1cclxuICAgIGVsc2VcclxuICAgIHtcclxuICAgICAgYSA9IDkuMCAqIGRlZmF1bHRWYWx1ZSAvIDUwLjA7XHJcbiAgICAgIGIgPSAtOCAqIGRlZmF1bHRWYWx1ZTtcclxuICAgIH1cclxuXHJcbiAgICByZXR1cm4gKGEgKiBzbGlkZXJWYWx1ZSArIGIpO1xyXG4gIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIG1ldGhvZCBmaW5kcyBhbmQgcmV0dXJucyB0aGUgY2VudGVyIG9mIHRoZSBnaXZlbiBub2RlcywgYXNzdW1pbmdcclxuICogdGhhdCB0aGUgZ2l2ZW4gbm9kZXMgZm9ybSBhIHRyZWUgaW4gdGhlbXNlbHZlcy5cclxuICovXHJcbkxheW91dC5maW5kQ2VudGVyT2ZUcmVlID0gZnVuY3Rpb24gKG5vZGVzKVxyXG57XHJcbiAgdmFyIGxpc3QgPSBbXTtcclxuICBsaXN0ID0gbGlzdC5jb25jYXQobm9kZXMpO1xyXG5cclxuICB2YXIgcmVtb3ZlZE5vZGVzID0gW107XHJcbiAgdmFyIHJlbWFpbmluZ0RlZ3JlZXMgPSBuZXcgSGFzaE1hcCgpO1xyXG4gIHZhciBmb3VuZENlbnRlciA9IGZhbHNlO1xyXG4gIHZhciBjZW50ZXJOb2RlID0gbnVsbDtcclxuXHJcbiAgaWYgKGxpc3QubGVuZ3RoID09IDEgfHwgbGlzdC5sZW5ndGggPT0gMilcclxuICB7XHJcbiAgICBmb3VuZENlbnRlciA9IHRydWU7XHJcbiAgICBjZW50ZXJOb2RlID0gbGlzdFswXTtcclxuICB9XHJcblxyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgbGlzdC5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbm9kZSA9IGxpc3RbaV07XHJcbiAgICB2YXIgZGVncmVlID0gbm9kZS5nZXROZWlnaGJvcnNMaXN0KCkuc2l6ZSgpO1xyXG4gICAgcmVtYWluaW5nRGVncmVlcy5wdXQobm9kZSwgbm9kZS5nZXROZWlnaGJvcnNMaXN0KCkuc2l6ZSgpKTtcclxuXHJcbiAgICBpZiAoZGVncmVlID09IDEpXHJcbiAgICB7XHJcbiAgICAgIHJlbW92ZWROb2Rlcy5wdXNoKG5vZGUpO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgdmFyIHRlbXBMaXN0ID0gW107XHJcbiAgdGVtcExpc3QgPSB0ZW1wTGlzdC5jb25jYXQocmVtb3ZlZE5vZGVzKTtcclxuXHJcbiAgd2hpbGUgKCFmb3VuZENlbnRlcilcclxuICB7XHJcbiAgICB2YXIgdGVtcExpc3QyID0gW107XHJcbiAgICB0ZW1wTGlzdDIgPSB0ZW1wTGlzdDIuY29uY2F0KHRlbXBMaXN0KTtcclxuICAgIHRlbXBMaXN0ID0gW107XHJcblxyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBsaXN0Lmxlbmd0aDsgaSsrKVxyXG4gICAge1xyXG4gICAgICB2YXIgbm9kZSA9IGxpc3RbaV07XHJcblxyXG4gICAgICB2YXIgaW5kZXggPSBsaXN0LmluZGV4T2Yobm9kZSk7XHJcbiAgICAgIGlmIChpbmRleCA+PSAwKSB7XHJcbiAgICAgICAgbGlzdC5zcGxpY2UoaW5kZXgsIDEpO1xyXG4gICAgICB9XHJcblxyXG4gICAgICB2YXIgbmVpZ2hib3VycyA9IG5vZGUuZ2V0TmVpZ2hib3JzTGlzdCgpO1xyXG5cclxuICAgICAgZm9yICh2YXIgaiBpbiBuZWlnaGJvdXJzLnNldClcclxuICAgICAge1xyXG4gICAgICAgIHZhciBuZWlnaGJvdXIgPSBuZWlnaGJvdXJzLnNldFtqXTtcclxuICAgICAgICBpZiAocmVtb3ZlZE5vZGVzLmluZGV4T2YobmVpZ2hib3VyKSA8IDApXHJcbiAgICAgICAge1xyXG4gICAgICAgICAgdmFyIG90aGVyRGVncmVlID0gcmVtYWluaW5nRGVncmVlcy5nZXQobmVpZ2hib3VyKTtcclxuICAgICAgICAgIHZhciBuZXdEZWdyZWUgPSBvdGhlckRlZ3JlZSAtIDE7XHJcblxyXG4gICAgICAgICAgaWYgKG5ld0RlZ3JlZSA9PSAxKVxyXG4gICAgICAgICAge1xyXG4gICAgICAgICAgICB0ZW1wTGlzdC5wdXNoKG5laWdoYm91cik7XHJcbiAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgcmVtYWluaW5nRGVncmVlcy5wdXQobmVpZ2hib3VyLCBuZXdEZWdyZWUpO1xyXG4gICAgICAgIH1cclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHJlbW92ZWROb2RlcyA9IHJlbW92ZWROb2Rlcy5jb25jYXQodGVtcExpc3QpO1xyXG5cclxuICAgIGlmIChsaXN0Lmxlbmd0aCA9PSAxIHx8IGxpc3QubGVuZ3RoID09IDIpXHJcbiAgICB7XHJcbiAgICAgIGZvdW5kQ2VudGVyID0gdHJ1ZTtcclxuICAgICAgY2VudGVyTm9kZSA9IGxpc3RbMF07XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICByZXR1cm4gY2VudGVyTm9kZTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBEdXJpbmcgdGhlIGNvYXJzZW5pbmcgcHJvY2VzcywgdGhpcyBsYXlvdXQgbWF5IGJlIHJlZmVyZW5jZWQgYnkgdHdvIGdyYXBoIG1hbmFnZXJzXHJcbiAqIHRoaXMgc2V0dGVyIGZ1bmN0aW9uIGdyYW50cyBhY2Nlc3MgdG8gY2hhbmdlIHRoZSBjdXJyZW50bHkgYmVpbmcgdXNlZCBncmFwaCBtYW5hZ2VyXHJcbiAqL1xyXG5MYXlvdXQucHJvdG90eXBlLnNldEdyYXBoTWFuYWdlciA9IGZ1bmN0aW9uIChnbSlcclxue1xyXG4gIHRoaXMuZ3JhcGhNYW5hZ2VyID0gZ207XHJcbn07XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IExheW91dDtcclxuIiwiZnVuY3Rpb24gTGF5b3V0Q29uc3RhbnRzKCkge1xyXG59XHJcblxyXG4vKipcclxuICogTGF5b3V0IFF1YWxpdHlcclxuICovXHJcbkxheW91dENvbnN0YW50cy5QUk9PRl9RVUFMSVRZID0gMDtcclxuTGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfUVVBTElUWSA9IDE7XHJcbkxheW91dENvbnN0YW50cy5EUkFGVF9RVUFMSVRZID0gMjtcclxuXHJcbi8qKlxyXG4gKiBEZWZhdWx0IHBhcmFtZXRlcnNcclxuICovXHJcbkxheW91dENvbnN0YW50cy5ERUZBVUxUX0NSRUFURV9CRU5EU19BU19ORUVERUQgPSBmYWxzZTtcclxuLy9MYXlvdXRDb25zdGFudHMuREVGQVVMVF9JTkNSRU1FTlRBTCA9IHRydWU7XHJcbkxheW91dENvbnN0YW50cy5ERUZBVUxUX0lOQ1JFTUVOVEFMID0gZmFsc2U7XHJcbkxheW91dENvbnN0YW50cy5ERUZBVUxUX0FOSU1BVElPTl9PTl9MQVlPVVQgPSB0cnVlO1xyXG5MYXlvdXRDb25zdGFudHMuREVGQVVMVF9BTklNQVRJT05fRFVSSU5HX0xBWU9VVCA9IGZhbHNlO1xyXG5MYXlvdXRDb25zdGFudHMuREVGQVVMVF9BTklNQVRJT05fUEVSSU9EID0gNTA7XHJcbkxheW91dENvbnN0YW50cy5ERUZBVUxUX1VOSUZPUk1fTEVBRl9OT0RFX1NJWkVTID0gZmFsc2U7XHJcblxyXG4vLyAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4vLyBTZWN0aW9uOiBHZW5lcmFsIG90aGVyIGNvbnN0YW50c1xyXG4vLyAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4vKlxyXG4gKiBNYXJnaW5zIG9mIGEgZ3JhcGggdG8gYmUgYXBwbGllZCBvbiBib3VkaW5nIHJlY3RhbmdsZSBvZiBpdHMgY29udGVudHMuIFdlXHJcbiAqIGFzc3VtZSBtYXJnaW5zIG9uIGFsbCBmb3VyIHNpZGVzIHRvIGJlIHVuaWZvcm0uXHJcbiAqL1xyXG5MYXlvdXRDb25zdGFudHMuREVGQVVMVF9HUkFQSF9NQVJHSU4gPSAxMDtcclxuXHJcbi8qXHJcbiAqIFRoZSBoZWlnaHQgb2YgdGhlIGxhYmVsIG9mIGEgY29tcG91bmQuIFdlIGFzc3VtZSB0aGUgbGFiZWwgb2YgYSBjb21wb3VuZFxyXG4gKiBub2RlIGlzIHBsYWNlZCBhdCB0aGUgYm90dG9tIHdpdGggYSBkeW5hbWljIHdpZHRoIHNhbWUgYXMgdGhlIGNvbXBvdW5kXHJcbiAqIGl0c2VsZi5cclxuICovXHJcbkxheW91dENvbnN0YW50cy5MQUJFTF9IRUlHSFQgPSAyMDtcclxuXHJcbi8qXHJcbiAqIEFkZGl0aW9uYWwgbWFyZ2lucyB0aGF0IHdlIG1haW50YWluIGFzIHNhZmV0eSBidWZmZXIgZm9yIG5vZGUtbm9kZVxyXG4gKiBvdmVybGFwcy4gQ29tcG91bmQgbm9kZSBsYWJlbHMgYXMgd2VsbCBhcyBncmFwaCBtYXJnaW5zIGFyZSBoYW5kbGVkXHJcbiAqIHNlcGFyYXRlbHkhXHJcbiAqL1xyXG5MYXlvdXRDb25zdGFudHMuQ09NUE9VTkRfTk9ERV9NQVJHSU4gPSA1O1xyXG5cclxuLypcclxuICogRGVmYXVsdCBkaW1lbnNpb24gb2YgYSBub24tY29tcG91bmQgbm9kZS5cclxuICovXHJcbkxheW91dENvbnN0YW50cy5TSU1QTEVfTk9ERV9TSVpFID0gNDA7XHJcblxyXG4vKlxyXG4gKiBEZWZhdWx0IGRpbWVuc2lvbiBvZiBhIG5vbi1jb21wb3VuZCBub2RlLlxyXG4gKi9cclxuTGF5b3V0Q29uc3RhbnRzLlNJTVBMRV9OT0RFX0hBTEZfU0laRSA9IExheW91dENvbnN0YW50cy5TSU1QTEVfTk9ERV9TSVpFIC8gMjtcclxuXHJcbi8qXHJcbiAqIEVtcHR5IGNvbXBvdW5kIG5vZGUgc2l6ZS4gV2hlbiBhIGNvbXBvdW5kIG5vZGUgaXMgZW1wdHksIGl0cyBib3RoXHJcbiAqIGRpbWVuc2lvbnMgc2hvdWxkIGJlIG9mIHRoaXMgdmFsdWUuXHJcbiAqL1xyXG5MYXlvdXRDb25zdGFudHMuRU1QVFlfQ09NUE9VTkRfTk9ERV9TSVpFID0gNDA7XHJcblxyXG4vKlxyXG4gKiBNaW5pbXVtIGxlbmd0aCB0aGF0IGFuIGVkZ2Ugc2hvdWxkIHRha2UgZHVyaW5nIGxheW91dFxyXG4gKi9cclxuTGF5b3V0Q29uc3RhbnRzLk1JTl9FREdFX0xFTkdUSCA9IDE7XHJcblxyXG4vKlxyXG4gKiBXb3JsZCBib3VuZGFyaWVzIHRoYXQgbGF5b3V0IG9wZXJhdGVzIG9uXHJcbiAqL1xyXG5MYXlvdXRDb25zdGFudHMuV09STERfQk9VTkRBUlkgPSAxMDAwMDAwO1xyXG5cclxuLypcclxuICogV29ybGQgYm91bmRhcmllcyB0aGF0IHJhbmRvbSBwb3NpdGlvbmluZyBjYW4gYmUgcGVyZm9ybWVkIHdpdGhcclxuICovXHJcbkxheW91dENvbnN0YW50cy5JTklUSUFMX1dPUkxEX0JPVU5EQVJZID0gTGF5b3V0Q29uc3RhbnRzLldPUkxEX0JPVU5EQVJZIC8gMTAwMDtcclxuXHJcbi8qXHJcbiAqIENvb3JkaW5hdGVzIG9mIHRoZSB3b3JsZCBjZW50ZXJcclxuICovXHJcbkxheW91dENvbnN0YW50cy5XT1JMRF9DRU5URVJfWCA9IDEyMDA7XHJcbkxheW91dENvbnN0YW50cy5XT1JMRF9DRU5URVJfWSA9IDkwMDtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gTGF5b3V0Q29uc3RhbnRzO1xyXG4iLCIvKlxyXG4gKlRoaXMgY2xhc3MgaXMgdGhlIGphdmFzY3JpcHQgaW1wbGVtZW50YXRpb24gb2YgdGhlIFBvaW50LmphdmEgY2xhc3MgaW4gamRrXHJcbiAqL1xyXG5mdW5jdGlvbiBQb2ludCh4LCB5LCBwKSB7XHJcbiAgdGhpcy54ID0gbnVsbDtcclxuICB0aGlzLnkgPSBudWxsO1xyXG4gIGlmICh4ID09IG51bGwgJiYgeSA9PSBudWxsICYmIHAgPT0gbnVsbCkge1xyXG4gICAgdGhpcy54ID0gMDtcclxuICAgIHRoaXMueSA9IDA7XHJcbiAgfVxyXG4gIGVsc2UgaWYgKHR5cGVvZiB4ID09ICdudW1iZXInICYmIHR5cGVvZiB5ID09ICdudW1iZXInICYmIHAgPT0gbnVsbCkge1xyXG4gICAgdGhpcy54ID0geDtcclxuICAgIHRoaXMueSA9IHk7XHJcbiAgfVxyXG4gIGVsc2UgaWYgKHguY29uc3RydWN0b3IubmFtZSA9PSAnUG9pbnQnICYmIHkgPT0gbnVsbCAmJiBwID09IG51bGwpIHtcclxuICAgIHAgPSB4O1xyXG4gICAgdGhpcy54ID0gcC54O1xyXG4gICAgdGhpcy55ID0gcC55O1xyXG4gIH1cclxufVxyXG5cclxuUG9pbnQucHJvdG90eXBlLmdldFggPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMueDtcclxufVxyXG5cclxuUG9pbnQucHJvdG90eXBlLmdldFkgPSBmdW5jdGlvbiAoKSB7XHJcbiAgcmV0dXJuIHRoaXMueTtcclxufVxyXG5cclxuUG9pbnQucHJvdG90eXBlLmdldExvY2F0aW9uID0gZnVuY3Rpb24gKCkge1xyXG4gIHJldHVybiBuZXcgUG9pbnQodGhpcy54LCB0aGlzLnkpO1xyXG59XHJcblxyXG5Qb2ludC5wcm90b3R5cGUuc2V0TG9jYXRpb24gPSBmdW5jdGlvbiAoeCwgeSwgcCkge1xyXG4gIGlmICh4LmNvbnN0cnVjdG9yLm5hbWUgPT0gJ1BvaW50JyAmJiB5ID09IG51bGwgJiYgcCA9PSBudWxsKSB7XHJcbiAgICBwID0geDtcclxuICAgIHRoaXMuc2V0TG9jYXRpb24ocC54LCBwLnkpO1xyXG4gIH1cclxuICBlbHNlIGlmICh0eXBlb2YgeCA9PSAnbnVtYmVyJyAmJiB0eXBlb2YgeSA9PSAnbnVtYmVyJyAmJiBwID09IG51bGwpIHtcclxuICAgIC8vaWYgYm90aCBwYXJhbWV0ZXJzIGFyZSBpbnRlZ2VyIGp1c3QgbW92ZSAoeCx5KSBsb2NhdGlvblxyXG4gICAgaWYgKHBhcnNlSW50KHgpID09IHggJiYgcGFyc2VJbnQoeSkgPT0geSkge1xyXG4gICAgICB0aGlzLm1vdmUoeCwgeSk7XHJcbiAgICB9XHJcbiAgICBlbHNlIHtcclxuICAgICAgdGhpcy54ID0gTWF0aC5mbG9vcih4ICsgMC41KTtcclxuICAgICAgdGhpcy55ID0gTWF0aC5mbG9vcih5ICsgMC41KTtcclxuICAgIH1cclxuICB9XHJcbn1cclxuXHJcblBvaW50LnByb3RvdHlwZS5tb3ZlID0gZnVuY3Rpb24gKHgsIHkpIHtcclxuICB0aGlzLnggPSB4O1xyXG4gIHRoaXMueSA9IHk7XHJcbn1cclxuXHJcblBvaW50LnByb3RvdHlwZS50cmFuc2xhdGUgPSBmdW5jdGlvbiAoZHgsIGR5KSB7XHJcbiAgdGhpcy54ICs9IGR4O1xyXG4gIHRoaXMueSArPSBkeTtcclxufVxyXG5cclxuUG9pbnQucHJvdG90eXBlLmVxdWFscyA9IGZ1bmN0aW9uIChvYmopIHtcclxuICBpZiAob2JqLmNvbnN0cnVjdG9yLm5hbWUgPT0gXCJQb2ludFwiKSB7XHJcbiAgICB2YXIgcHQgPSBvYmo7XHJcbiAgICByZXR1cm4gKHRoaXMueCA9PSBwdC54KSAmJiAodGhpcy55ID09IHB0LnkpO1xyXG4gIH1cclxuICByZXR1cm4gdGhpcyA9PSBvYmo7XHJcbn1cclxuXHJcblBvaW50LnByb3RvdHlwZS50b1N0cmluZyA9IGZ1bmN0aW9uICgpIHtcclxuICByZXR1cm4gbmV3IFBvaW50KCkuY29uc3RydWN0b3IubmFtZSArIFwiW3g9XCIgKyB0aGlzLnggKyBcIix5PVwiICsgdGhpcy55ICsgXCJdXCI7XHJcbn1cclxuXHJcbm1vZHVsZS5leHBvcnRzID0gUG9pbnQ7XHJcbiIsImZ1bmN0aW9uIFBvaW50RCh4LCB5KSB7XHJcbiAgaWYgKHggPT0gbnVsbCAmJiB5ID09IG51bGwpIHtcclxuICAgIHRoaXMueCA9IDA7XHJcbiAgICB0aGlzLnkgPSAwO1xyXG4gIH0gZWxzZSB7XHJcbiAgICB0aGlzLnggPSB4O1xyXG4gICAgdGhpcy55ID0geTtcclxuICB9XHJcbn1cclxuXHJcblBvaW50RC5wcm90b3R5cGUuZ2V0WCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy54O1xyXG59O1xyXG5cclxuUG9pbnRELnByb3RvdHlwZS5nZXRZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnk7XHJcbn07XHJcblxyXG5Qb2ludEQucHJvdG90eXBlLnNldFggPSBmdW5jdGlvbiAoeClcclxue1xyXG4gIHRoaXMueCA9IHg7XHJcbn07XHJcblxyXG5Qb2ludEQucHJvdG90eXBlLnNldFkgPSBmdW5jdGlvbiAoeSlcclxue1xyXG4gIHRoaXMueSA9IHk7XHJcbn07XHJcblxyXG5Qb2ludEQucHJvdG90eXBlLmdldERpZmZlcmVuY2UgPSBmdW5jdGlvbiAocHQpXHJcbntcclxuICByZXR1cm4gbmV3IERpbWVuc2lvbkQodGhpcy54IC0gcHQueCwgdGhpcy55IC0gcHQueSk7XHJcbn07XHJcblxyXG5Qb2ludEQucHJvdG90eXBlLmdldENvcHkgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIG5ldyBQb2ludEQodGhpcy54LCB0aGlzLnkpO1xyXG59O1xyXG5cclxuUG9pbnRELnByb3RvdHlwZS50cmFuc2xhdGUgPSBmdW5jdGlvbiAoZGltKVxyXG57XHJcbiAgdGhpcy54ICs9IGRpbS53aWR0aDtcclxuICB0aGlzLnkgKz0gZGltLmhlaWdodDtcclxuICByZXR1cm4gdGhpcztcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gUG9pbnREO1xyXG4iLCJmdW5jdGlvbiBSYW5kb21TZWVkKCkge1xyXG59XHJcblJhbmRvbVNlZWQuc2VlZCA9IDE7XHJcblJhbmRvbVNlZWQueCA9IDA7XHJcblxyXG5SYW5kb21TZWVkLm5leHREb3VibGUgPSBmdW5jdGlvbiAoKSB7XHJcbiAgUmFuZG9tU2VlZC54ID0gTWF0aC5zaW4oUmFuZG9tU2VlZC5zZWVkKyspICogMTAwMDA7XHJcbiAgcmV0dXJuIFJhbmRvbVNlZWQueCAtIE1hdGguZmxvb3IoUmFuZG9tU2VlZC54KTtcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gUmFuZG9tU2VlZDtcclxuIiwiZnVuY3Rpb24gUmVjdGFuZ2xlRCh4LCB5LCB3aWR0aCwgaGVpZ2h0KSB7XHJcbiAgdGhpcy54ID0gMDtcclxuICB0aGlzLnkgPSAwO1xyXG4gIHRoaXMud2lkdGggPSAwO1xyXG4gIHRoaXMuaGVpZ2h0ID0gMDtcclxuXHJcbiAgaWYgKHggIT0gbnVsbCAmJiB5ICE9IG51bGwgJiYgd2lkdGggIT0gbnVsbCAmJiBoZWlnaHQgIT0gbnVsbCkge1xyXG4gICAgdGhpcy54ID0geDtcclxuICAgIHRoaXMueSA9IHk7XHJcbiAgICB0aGlzLndpZHRoID0gd2lkdGg7XHJcbiAgICB0aGlzLmhlaWdodCA9IGhlaWdodDtcclxuICB9XHJcbn1cclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLmdldFggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMueDtcclxufTtcclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLnNldFggPSBmdW5jdGlvbiAoeClcclxue1xyXG4gIHRoaXMueCA9IHg7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5nZXRZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnk7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5zZXRZID0gZnVuY3Rpb24gKHkpXHJcbntcclxuICB0aGlzLnkgPSB5O1xyXG59O1xyXG5cclxuUmVjdGFuZ2xlRC5wcm90b3R5cGUuZ2V0V2lkdGggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMud2lkdGg7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5zZXRXaWR0aCA9IGZ1bmN0aW9uICh3aWR0aClcclxue1xyXG4gIHRoaXMud2lkdGggPSB3aWR0aDtcclxufTtcclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLmdldEhlaWdodCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5oZWlnaHQ7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5zZXRIZWlnaHQgPSBmdW5jdGlvbiAoaGVpZ2h0KVxyXG57XHJcbiAgdGhpcy5oZWlnaHQgPSBoZWlnaHQ7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5nZXRSaWdodCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy54ICsgdGhpcy53aWR0aDtcclxufTtcclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLmdldEJvdHRvbSA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy55ICsgdGhpcy5oZWlnaHQ7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5pbnRlcnNlY3RzID0gZnVuY3Rpb24gKGEpXHJcbntcclxuICBpZiAodGhpcy5nZXRSaWdodCgpIDwgYS54KVxyXG4gIHtcclxuICAgIHJldHVybiBmYWxzZTtcclxuICB9XHJcblxyXG4gIGlmICh0aGlzLmdldEJvdHRvbSgpIDwgYS55KVxyXG4gIHtcclxuICAgIHJldHVybiBmYWxzZTtcclxuICB9XHJcblxyXG4gIGlmIChhLmdldFJpZ2h0KCkgPCB0aGlzLngpXHJcbiAge1xyXG4gICAgcmV0dXJuIGZhbHNlO1xyXG4gIH1cclxuXHJcbiAgaWYgKGEuZ2V0Qm90dG9tKCkgPCB0aGlzLnkpXHJcbiAge1xyXG4gICAgcmV0dXJuIGZhbHNlO1xyXG4gIH1cclxuXHJcbiAgcmV0dXJuIHRydWU7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5nZXRDZW50ZXJYID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLnggKyB0aGlzLndpZHRoIC8gMjtcclxufTtcclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLmdldE1pblggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuZ2V0WCgpO1xyXG59O1xyXG5cclxuUmVjdGFuZ2xlRC5wcm90b3R5cGUuZ2V0TWF4WCA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5nZXRYKCkgKyB0aGlzLndpZHRoO1xyXG59O1xyXG5cclxuUmVjdGFuZ2xlRC5wcm90b3R5cGUuZ2V0Q2VudGVyWSA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy55ICsgdGhpcy5oZWlnaHQgLyAyO1xyXG59O1xyXG5cclxuUmVjdGFuZ2xlRC5wcm90b3R5cGUuZ2V0TWluWSA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5nZXRZKCk7XHJcbn07XHJcblxyXG5SZWN0YW5nbGVELnByb3RvdHlwZS5nZXRNYXhZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmdldFkoKSArIHRoaXMuaGVpZ2h0O1xyXG59O1xyXG5cclxuUmVjdGFuZ2xlRC5wcm90b3R5cGUuZ2V0V2lkdGhIYWxmID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLndpZHRoIC8gMjtcclxufTtcclxuXHJcblJlY3RhbmdsZUQucHJvdG90eXBlLmdldEhlaWdodEhhbGYgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMuaGVpZ2h0IC8gMjtcclxufTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gUmVjdGFuZ2xlRDtcclxuIiwiZnVuY3Rpb24gVHJhbnNmb3JtKHgsIHkpIHtcclxuICB0aGlzLmx3b3JsZE9yZ1ggPSAwLjA7XHJcbiAgdGhpcy5sd29ybGRPcmdZID0gMC4wO1xyXG4gIHRoaXMubGRldmljZU9yZ1ggPSAwLjA7XHJcbiAgdGhpcy5sZGV2aWNlT3JnWSA9IDAuMDtcclxuICB0aGlzLmx3b3JsZEV4dFggPSAxLjA7XHJcbiAgdGhpcy5sd29ybGRFeHRZID0gMS4wO1xyXG4gIHRoaXMubGRldmljZUV4dFggPSAxLjA7XHJcbiAgdGhpcy5sZGV2aWNlRXh0WSA9IDEuMDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXRXb3JsZE9yZ1ggPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMubHdvcmxkT3JnWDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5zZXRXb3JsZE9yZ1ggPSBmdW5jdGlvbiAod294KVxyXG57XHJcbiAgdGhpcy5sd29ybGRPcmdYID0gd294O1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLmdldFdvcmxkT3JnWSA9IGZ1bmN0aW9uICgpXHJcbntcclxuICByZXR1cm4gdGhpcy5sd29ybGRPcmdZO1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLnNldFdvcmxkT3JnWSA9IGZ1bmN0aW9uICh3b3kpXHJcbntcclxuICB0aGlzLmx3b3JsZE9yZ1kgPSB3b3k7XHJcbn1cclxuXHJcblRyYW5zZm9ybS5wcm90b3R5cGUuZ2V0V29ybGRFeHRYID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmx3b3JsZEV4dFg7XHJcbn1cclxuXHJcblRyYW5zZm9ybS5wcm90b3R5cGUuc2V0V29ybGRFeHRYID0gZnVuY3Rpb24gKHdleClcclxue1xyXG4gIHRoaXMubHdvcmxkRXh0WCA9IHdleDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXRXb3JsZEV4dFkgPSBmdW5jdGlvbiAoKVxyXG57XHJcbiAgcmV0dXJuIHRoaXMubHdvcmxkRXh0WTtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5zZXRXb3JsZEV4dFkgPSBmdW5jdGlvbiAod2V5KVxyXG57XHJcbiAgdGhpcy5sd29ybGRFeHRZID0gd2V5O1xyXG59XHJcblxyXG4vKiBEZXZpY2UgcmVsYXRlZCAqL1xyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXREZXZpY2VPcmdYID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxkZXZpY2VPcmdYO1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLnNldERldmljZU9yZ1ggPSBmdW5jdGlvbiAoZG94KVxyXG57XHJcbiAgdGhpcy5sZGV2aWNlT3JnWCA9IGRveDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXREZXZpY2VPcmdZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxkZXZpY2VPcmdZO1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLnNldERldmljZU9yZ1kgPSBmdW5jdGlvbiAoZG95KVxyXG57XHJcbiAgdGhpcy5sZGV2aWNlT3JnWSA9IGRveTtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXREZXZpY2VFeHRYID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxkZXZpY2VFeHRYO1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLnNldERldmljZUV4dFggPSBmdW5jdGlvbiAoZGV4KVxyXG57XHJcbiAgdGhpcy5sZGV2aWNlRXh0WCA9IGRleDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5nZXREZXZpY2VFeHRZID0gZnVuY3Rpb24gKClcclxue1xyXG4gIHJldHVybiB0aGlzLmxkZXZpY2VFeHRZO1xyXG59XHJcblxyXG5UcmFuc2Zvcm0ucHJvdG90eXBlLnNldERldmljZUV4dFkgPSBmdW5jdGlvbiAoZGV5KVxyXG57XHJcbiAgdGhpcy5sZGV2aWNlRXh0WSA9IGRleTtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS50cmFuc2Zvcm1YID0gZnVuY3Rpb24gKHgpXHJcbntcclxuICB2YXIgeERldmljZSA9IDAuMDtcclxuICB2YXIgd29ybGRFeHRYID0gdGhpcy5sd29ybGRFeHRYO1xyXG4gIGlmICh3b3JsZEV4dFggIT0gMC4wKVxyXG4gIHtcclxuICAgIHhEZXZpY2UgPSB0aGlzLmxkZXZpY2VPcmdYICtcclxuICAgICAgICAgICAgKCh4IC0gdGhpcy5sd29ybGRPcmdYKSAqIHRoaXMubGRldmljZUV4dFggLyB3b3JsZEV4dFgpO1xyXG4gIH1cclxuXHJcbiAgcmV0dXJuIHhEZXZpY2U7XHJcbn1cclxuXHJcblRyYW5zZm9ybS5wcm90b3R5cGUudHJhbnNmb3JtWSA9IGZ1bmN0aW9uICh5KVxyXG57XHJcbiAgdmFyIHlEZXZpY2UgPSAwLjA7XHJcbiAgdmFyIHdvcmxkRXh0WSA9IHRoaXMubHdvcmxkRXh0WTtcclxuICBpZiAod29ybGRFeHRZICE9IDAuMClcclxuICB7XHJcbiAgICB5RGV2aWNlID0gdGhpcy5sZGV2aWNlT3JnWSArXHJcbiAgICAgICAgICAgICgoeSAtIHRoaXMubHdvcmxkT3JnWSkgKiB0aGlzLmxkZXZpY2VFeHRZIC8gd29ybGRFeHRZKTtcclxuICB9XHJcblxyXG5cclxuICByZXR1cm4geURldmljZTtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5pbnZlcnNlVHJhbnNmb3JtWCA9IGZ1bmN0aW9uICh4KVxyXG57XHJcbiAgdmFyIHhXb3JsZCA9IDAuMDtcclxuICB2YXIgZGV2aWNlRXh0WCA9IHRoaXMubGRldmljZUV4dFg7XHJcbiAgaWYgKGRldmljZUV4dFggIT0gMC4wKVxyXG4gIHtcclxuICAgIHhXb3JsZCA9IHRoaXMubHdvcmxkT3JnWCArXHJcbiAgICAgICAgICAgICgoeCAtIHRoaXMubGRldmljZU9yZ1gpICogdGhpcy5sd29ybGRFeHRYIC8gZGV2aWNlRXh0WCk7XHJcbiAgfVxyXG5cclxuXHJcbiAgcmV0dXJuIHhXb3JsZDtcclxufVxyXG5cclxuVHJhbnNmb3JtLnByb3RvdHlwZS5pbnZlcnNlVHJhbnNmb3JtWSA9IGZ1bmN0aW9uICh5KVxyXG57XHJcbiAgdmFyIHlXb3JsZCA9IDAuMDtcclxuICB2YXIgZGV2aWNlRXh0WSA9IHRoaXMubGRldmljZUV4dFk7XHJcbiAgaWYgKGRldmljZUV4dFkgIT0gMC4wKVxyXG4gIHtcclxuICAgIHlXb3JsZCA9IHRoaXMubHdvcmxkT3JnWSArXHJcbiAgICAgICAgICAgICgoeSAtIHRoaXMubGRldmljZU9yZ1kpICogdGhpcy5sd29ybGRFeHRZIC8gZGV2aWNlRXh0WSk7XHJcbiAgfVxyXG4gIHJldHVybiB5V29ybGQ7XHJcbn1cclxuXHJcblRyYW5zZm9ybS5wcm90b3R5cGUuaW52ZXJzZVRyYW5zZm9ybVBvaW50ID0gZnVuY3Rpb24gKGluUG9pbnQpXHJcbntcclxuICB2YXIgb3V0UG9pbnQgPVxyXG4gICAgICAgICAgbmV3IFBvaW50RCh0aGlzLmludmVyc2VUcmFuc2Zvcm1YKGluUG9pbnQueCksXHJcbiAgICAgICAgICAgICAgICAgIHRoaXMuaW52ZXJzZVRyYW5zZm9ybVkoaW5Qb2ludC55KSk7XHJcbiAgcmV0dXJuIG91dFBvaW50O1xyXG59XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IFRyYW5zZm9ybTtcclxuIiwiZnVuY3Rpb24gVW5pcXVlSURHZW5lcmV0b3IoKSB7XHJcbn1cclxuXHJcblVuaXF1ZUlER2VuZXJldG9yLmxhc3RJRCA9IDA7XHJcblxyXG5VbmlxdWVJREdlbmVyZXRvci5jcmVhdGVJRCA9IGZ1bmN0aW9uIChvYmopIHtcclxuICBpZiAoVW5pcXVlSURHZW5lcmV0b3IuaXNQcmltaXRpdmUob2JqKSkge1xyXG4gICAgcmV0dXJuIG9iajtcclxuICB9XHJcbiAgaWYgKG9iai51bmlxdWVJRCAhPSBudWxsKSB7XHJcbiAgICByZXR1cm4gb2JqLnVuaXF1ZUlEO1xyXG4gIH1cclxuICBvYmoudW5pcXVlSUQgPSBVbmlxdWVJREdlbmVyZXRvci5nZXRTdHJpbmcoKTtcclxuICBVbmlxdWVJREdlbmVyZXRvci5sYXN0SUQrKztcclxuICByZXR1cm4gb2JqLnVuaXF1ZUlEO1xyXG59XHJcblxyXG5VbmlxdWVJREdlbmVyZXRvci5nZXRTdHJpbmcgPSBmdW5jdGlvbiAoaWQpIHtcclxuICBpZiAoaWQgPT0gbnVsbClcclxuICAgIGlkID0gVW5pcXVlSURHZW5lcmV0b3IubGFzdElEO1xyXG4gIHJldHVybiBcIk9iamVjdCNcIiArIGlkICsgXCJcIjtcclxufVxyXG5cclxuVW5pcXVlSURHZW5lcmV0b3IuaXNQcmltaXRpdmUgPSBmdW5jdGlvbiAoYXJnKSB7XHJcbiAgdmFyIHR5cGUgPSB0eXBlb2YgYXJnO1xyXG4gIHJldHVybiBhcmcgPT0gbnVsbCB8fCAodHlwZSAhPSBcIm9iamVjdFwiICYmIHR5cGUgIT0gXCJmdW5jdGlvblwiKTtcclxufVxyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBVbmlxdWVJREdlbmVyZXRvcjtcclxuIiwiJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFRocmVhZDtcclxuXHJcbnZhciBEaW1lbnNpb25EID0gcmVxdWlyZSgnLi9EaW1lbnNpb25EJyk7XHJcbnZhciBIYXNoTWFwID0gcmVxdWlyZSgnLi9IYXNoTWFwJyk7XHJcbnZhciBIYXNoU2V0ID0gcmVxdWlyZSgnLi9IYXNoU2V0Jyk7XHJcbnZhciBJR2VvbWV0cnkgPSByZXF1aXJlKCcuL0lHZW9tZXRyeScpO1xyXG52YXIgSU1hdGggPSByZXF1aXJlKCcuL0lNYXRoJyk7XHJcbnZhciBJbnRlZ2VyID0gcmVxdWlyZSgnLi9JbnRlZ2VyJyk7XHJcbnZhciBQb2ludCA9IHJlcXVpcmUoJy4vUG9pbnQnKTtcclxudmFyIFBvaW50RCA9IHJlcXVpcmUoJy4vUG9pbnREJyk7XHJcbnZhciBSYW5kb21TZWVkID0gcmVxdWlyZSgnLi9SYW5kb21TZWVkJyk7XHJcbnZhciBSZWN0YW5nbGVEID0gcmVxdWlyZSgnLi9SZWN0YW5nbGVEJyk7XHJcbnZhciBUcmFuc2Zvcm0gPSByZXF1aXJlKCcuL1RyYW5zZm9ybScpO1xyXG52YXIgVW5pcXVlSURHZW5lcmV0b3IgPSByZXF1aXJlKCcuL1VuaXF1ZUlER2VuZXJldG9yJyk7XHJcbnZhciBMR3JhcGhPYmplY3QgPSByZXF1aXJlKCcuL0xHcmFwaE9iamVjdCcpO1xyXG52YXIgTEdyYXBoID0gcmVxdWlyZSgnLi9MR3JhcGgnKTtcclxudmFyIExFZGdlID0gcmVxdWlyZSgnLi9MRWRnZScpO1xyXG52YXIgTEdyYXBoTWFuYWdlciA9IHJlcXVpcmUoJy4vTEdyYXBoTWFuYWdlcicpO1xyXG52YXIgTE5vZGUgPSByZXF1aXJlKCcuL0xOb2RlJyk7XHJcbnZhciBMYXlvdXQgPSByZXF1aXJlKCcuL0xheW91dCcpO1xyXG52YXIgTGF5b3V0Q29uc3RhbnRzID0gcmVxdWlyZSgnLi9MYXlvdXRDb25zdGFudHMnKTtcclxudmFyIEZETGF5b3V0ID0gcmVxdWlyZSgnLi9GRExheW91dCcpO1xyXG52YXIgRkRMYXlvdXRDb25zdGFudHMgPSByZXF1aXJlKCcuL0ZETGF5b3V0Q29uc3RhbnRzJyk7XHJcbnZhciBGRExheW91dEVkZ2UgPSByZXF1aXJlKCcuL0ZETGF5b3V0RWRnZScpO1xyXG52YXIgRkRMYXlvdXROb2RlID0gcmVxdWlyZSgnLi9GRExheW91dE5vZGUnKTtcclxudmFyIENvU0VDb25zdGFudHMgPSByZXF1aXJlKCcuL0NvU0VDb25zdGFudHMnKTtcclxudmFyIENvU0VFZGdlID0gcmVxdWlyZSgnLi9Db1NFRWRnZScpO1xyXG52YXIgQ29TRUdyYXBoID0gcmVxdWlyZSgnLi9Db1NFR3JhcGgnKTtcclxudmFyIENvU0VHcmFwaE1hbmFnZXIgPSByZXF1aXJlKCcuL0NvU0VHcmFwaE1hbmFnZXInKTtcclxudmFyIENvU0VMYXlvdXQgPSByZXF1aXJlKCcuL0NvU0VMYXlvdXQnKTtcclxudmFyIENvU0VOb2RlID0gcmVxdWlyZSgnLi9Db1NFTm9kZScpO1xyXG52YXIgbGF5b3V0T3B0aW9uc1BhY2sgPSByZXF1aXJlKCcuL2xheW91dE9wdGlvbnNQYWNrJyk7XHJcblxyXG5sYXlvdXRPcHRpb25zUGFjay5sYXlvdXRRdWFsaXR5OyAvLyBwcm9vZiwgZGVmYXVsdCwgZHJhZnRcclxubGF5b3V0T3B0aW9uc1BhY2suYW5pbWF0aW9uRHVyaW5nTGF5b3V0OyAvLyBULUZcclxubGF5b3V0T3B0aW9uc1BhY2suYW5pbWF0aW9uT25MYXlvdXQ7IC8vIFQtRlxyXG5sYXlvdXRPcHRpb25zUGFjay5hbmltYXRpb25QZXJpb2Q7IC8vIDAtMTAwXHJcbmxheW91dE9wdGlvbnNQYWNrLmluY3JlbWVudGFsOyAvLyBULUZcclxubGF5b3V0T3B0aW9uc1BhY2suY3JlYXRlQmVuZHNBc05lZWRlZDsgLy8gVC1GXHJcbmxheW91dE9wdGlvbnNQYWNrLnVuaWZvcm1MZWFmTm9kZVNpemVzOyAvLyBULUZcclxuXHJcbmxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRMYXlvdXRRdWFsaXR5ID0gTGF5b3V0Q29uc3RhbnRzLkRFRkFVTFRfUVVBTElUWTtcclxubGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdEFuaW1hdGlvbkR1cmluZ0xheW91dCA9IExheW91dENvbnN0YW50cy5ERUZBVUxUX0FOSU1BVElPTl9EVVJJTkdfTEFZT1VUO1xyXG5sYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0QW5pbWF0aW9uT25MYXlvdXQgPSBMYXlvdXRDb25zdGFudHMuREVGQVVMVF9BTklNQVRJT05fT05fTEFZT1VUO1xyXG5sYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0QW5pbWF0aW9uUGVyaW9kID0gNTA7XHJcbmxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRJbmNyZW1lbnRhbCA9IExheW91dENvbnN0YW50cy5ERUZBVUxUX0lOQ1JFTUVOVEFMO1xyXG5sYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0Q3JlYXRlQmVuZHNBc05lZWRlZCA9IExheW91dENvbnN0YW50cy5ERUZBVUxUX0NSRUFURV9CRU5EU19BU19ORUVERUQ7XHJcbmxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRVbmlmb3JtTGVhZk5vZGVTaXplcyA9IExheW91dENvbnN0YW50cy5ERUZBVUxUX1VOSUZPUk1fTEVBRl9OT0RFX1NJWkVTO1xyXG5cclxuZnVuY3Rpb24gc2V0RGVmYXVsdExheW91dFByb3BlcnRpZXMoKSB7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2subGF5b3V0UXVhbGl0eSA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRMYXlvdXRRdWFsaXR5O1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLmFuaW1hdGlvbkR1cmluZ0xheW91dCA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRBbmltYXRpb25EdXJpbmdMYXlvdXQ7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suYW5pbWF0aW9uT25MYXlvdXQgPSBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0QW5pbWF0aW9uT25MYXlvdXQ7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suYW5pbWF0aW9uUGVyaW9kID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdEFuaW1hdGlvblBlcmlvZDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5pbmNyZW1lbnRhbCA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRJbmNyZW1lbnRhbDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5jcmVhdGVCZW5kc0FzTmVlZGVkID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdENyZWF0ZUJlbmRzQXNOZWVkZWQ7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2sudW5pZm9ybUxlYWZOb2RlU2l6ZXMgPSBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0VW5pZm9ybUxlYWZOb2RlU2l6ZXM7XHJcbn1cclxuXHJcbnNldERlZmF1bHRMYXlvdXRQcm9wZXJ0aWVzKCk7XHJcblxyXG5mdW5jdGlvbiBmaWxsQ29zZUxheW91dE9wdGlvbnNQYWNrKCkge1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRJZGVhbEVkZ2VMZW5ndGggPSBDb1NFQ29uc3RhbnRzLkRFRkFVTFRfRURHRV9MRU5HVEg7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFNwcmluZ1N0cmVuZ3RoID0gNTA7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFJlcHVsc2lvblN0cmVuZ3RoID0gNTA7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFNtYXJ0UmVwdWxzaW9uUmFuZ2VDYWxjID0gQ29TRUNvbnN0YW50cy5ERUZBVUxUX1VTRV9TTUFSVF9SRVBVTFNJT05fUkFOR0VfQ0FMQ1VMQVRJT047XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdEdyYXZpdHlTdHJlbmd0aCA9IDUwO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRHcmF2aXR5UmFuZ2UgPSA1MDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0Q29tcG91bmRHcmF2aXR5U3RyZW5ndGggPSA1MDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0Q29tcG91bmRHcmF2aXR5UmFuZ2UgPSA1MDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0U21hcnRFZGdlTGVuZ3RoQ2FsYyA9IENvU0VDb25zdGFudHMuREVGQVVMVF9VU0VfU01BUlRfSURFQUxfRURHRV9MRU5HVEhfQ0FMQ1VMQVRJT047XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdE11bHRpTGV2ZWxTY2FsaW5nID0gQ29TRUNvbnN0YW50cy5ERUZBVUxUX1VTRV9NVUxUSV9MRVZFTF9TQ0FMSU5HO1xyXG5cclxuICBsYXlvdXRPcHRpb25zUGFjay5pZGVhbEVkZ2VMZW5ndGggPSBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0SWRlYWxFZGdlTGVuZ3RoO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLnNwcmluZ1N0cmVuZ3RoID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFNwcmluZ1N0cmVuZ3RoO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLnJlcHVsc2lvblN0cmVuZ3RoID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFJlcHVsc2lvblN0cmVuZ3RoO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLnNtYXJ0UmVwdWxzaW9uUmFuZ2VDYWxjID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFNtYXJ0UmVwdWxzaW9uUmFuZ2VDYWxjO1xyXG4gIGxheW91dE9wdGlvbnNQYWNrLmdyYXZpdHlTdHJlbmd0aCA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRHcmF2aXR5U3RyZW5ndGg7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2suZ3Jhdml0eVJhbmdlID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdEdyYXZpdHlSYW5nZTtcclxuICBsYXlvdXRPcHRpb25zUGFjay5jb21wb3VuZEdyYXZpdHlTdHJlbmd0aCA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRDb21wb3VuZEdyYXZpdHlTdHJlbmd0aDtcclxuICBsYXlvdXRPcHRpb25zUGFjay5jb21wb3VuZEdyYXZpdHlSYW5nZSA9IGxheW91dE9wdGlvbnNQYWNrLmRlZmF1bHRDb21wb3VuZEdyYXZpdHlSYW5nZTtcclxuICBsYXlvdXRPcHRpb25zUGFjay5zbWFydEVkZ2VMZW5ndGhDYWxjID0gbGF5b3V0T3B0aW9uc1BhY2suZGVmYXVsdFNtYXJ0RWRnZUxlbmd0aENhbGM7XHJcbiAgbGF5b3V0T3B0aW9uc1BhY2subXVsdGlMZXZlbFNjYWxpbmcgPSBsYXlvdXRPcHRpb25zUGFjay5kZWZhdWx0TXVsdGlMZXZlbFNjYWxpbmc7XHJcbn1cclxuXHJcbl9Db1NFTGF5b3V0LmlkVG9MTm9kZSA9IHt9O1xyXG5fQ29TRUxheW91dC50b0JlVGlsZWQgPSB7fTtcclxuXHJcbnZhciBkZWZhdWx0cyA9IHtcclxuICAvLyBDYWxsZWQgb24gYGxheW91dHJlYWR5YFxyXG4gIHJlYWR5OiBmdW5jdGlvbiAoKSB7XHJcbiAgfSxcclxuICAvLyBDYWxsZWQgb24gYGxheW91dHN0b3BgXHJcbiAgc3RvcDogZnVuY3Rpb24gKCkge1xyXG4gIH0sXHJcbiAgLy8gV2hldGhlciB0byBmaXQgdGhlIG5ldHdvcmsgdmlldyBhZnRlciB3aGVuIGRvbmVcclxuICBmaXQ6IHRydWUsXHJcbiAgLy8gUGFkZGluZyBvbiBmaXRcclxuICBwYWRkaW5nOiAxMCxcclxuICAvLyBXaGV0aGVyIHRvIGVuYWJsZSBpbmNyZW1lbnRhbCBtb2RlXHJcbiAgcmFuZG9taXplOiBmYWxzZSxcclxuICAvLyBOb2RlIHJlcHVsc2lvbiAobm9uIG92ZXJsYXBwaW5nKSBtdWx0aXBsaWVyXHJcbiAgbm9kZVJlcHVsc2lvbjogNDUwMCxcclxuICAvLyBJZGVhbCBlZGdlIChub24gbmVzdGVkKSBsZW5ndGhcclxuICBpZGVhbEVkZ2VMZW5ndGg6IDUwLFxyXG4gIC8vIERpdmlzb3IgdG8gY29tcHV0ZSBlZGdlIGZvcmNlc1xyXG4gIGVkZ2VFbGFzdGljaXR5OiAwLjQ1LFxyXG4gIC8vIE5lc3RpbmcgZmFjdG9yIChtdWx0aXBsaWVyKSB0byBjb21wdXRlIGlkZWFsIGVkZ2UgbGVuZ3RoIGZvciBuZXN0ZWQgZWRnZXNcclxuICBuZXN0aW5nRmFjdG9yOiAwLjEsXHJcbiAgLy8gR3Jhdml0eSBmb3JjZSAoY29uc3RhbnQpXHJcbiAgZ3Jhdml0eTogMC40LFxyXG4gIC8vIE1heGltdW0gbnVtYmVyIG9mIGl0ZXJhdGlvbnMgdG8gcGVyZm9ybVxyXG4gIG51bUl0ZXI6IDI1MDAsXHJcbiAgLy8gRm9yIGVuYWJsaW5nIHRpbGluZ1xyXG4gIHRpbGU6IHRydWUsXHJcbiAgLy93aGV0aGVyIHRvIG1ha2UgYW5pbWF0aW9uIHdoaWxlIHBlcmZvcm1pbmcgdGhlIGxheW91dFxyXG4gIGFuaW1hdGU6IHRydWVcclxufTtcclxuXHJcbmZ1bmN0aW9uIGV4dGVuZChkZWZhdWx0cywgb3B0aW9ucykge1xyXG4gIHZhciBvYmogPSB7fTtcclxuXHJcbiAgZm9yICh2YXIgaSBpbiBkZWZhdWx0cykge1xyXG4gICAgb2JqW2ldID0gZGVmYXVsdHNbaV07XHJcbiAgfVxyXG5cclxuICBmb3IgKHZhciBpIGluIG9wdGlvbnMpIHtcclxuICAgIG9ialtpXSA9IG9wdGlvbnNbaV07XHJcbiAgfVxyXG5cclxuICByZXR1cm4gb2JqO1xyXG59XHJcbjtcclxuXHJcbl9Db1NFTGF5b3V0LmxheW91dCA9IG5ldyBDb1NFTGF5b3V0KCk7XHJcbmZ1bmN0aW9uIF9Db1NFTGF5b3V0KG9wdGlvbnMpIHtcclxuXHJcbiAgdGhpcy5vcHRpb25zID0gZXh0ZW5kKGRlZmF1bHRzLCBvcHRpb25zKTtcclxuICBGRExheW91dENvbnN0YW50cy5nZXRVc2VyT3B0aW9ucyh0aGlzLm9wdGlvbnMpO1xyXG4gIGZpbGxDb3NlTGF5b3V0T3B0aW9uc1BhY2soKTtcclxufVxyXG5cclxuX0NvU0VMYXlvdXQucHJvdG90eXBlLnJ1biA9IGZ1bmN0aW9uICgpIHtcclxuICB2YXIgbGF5b3V0ID0gdGhpcztcclxuXHJcbiAgX0NvU0VMYXlvdXQuaWRUb0xOb2RlID0ge307XHJcbiAgX0NvU0VMYXlvdXQudG9CZVRpbGVkID0ge307XHJcbiAgX0NvU0VMYXlvdXQubGF5b3V0ID0gbmV3IENvU0VMYXlvdXQoKTtcclxuICB0aGlzLmN5ID0gdGhpcy5vcHRpb25zLmN5O1xyXG4gIHZhciBhZnRlciA9IHRoaXM7XHJcblxyXG4gIHRoaXMuY3kudHJpZ2dlcignbGF5b3V0c3RhcnQnKTtcclxuXHJcbiAgdmFyIGdtID0gX0NvU0VMYXlvdXQubGF5b3V0Lm5ld0dyYXBoTWFuYWdlcigpO1xyXG4gIHRoaXMuZ20gPSBnbTtcclxuXHJcbiAgdmFyIG5vZGVzID0gdGhpcy5vcHRpb25zLmVsZXMubm9kZXMoKTtcclxuICB2YXIgZWRnZXMgPSB0aGlzLm9wdGlvbnMuZWxlcy5lZGdlcygpO1xyXG5cclxuICB0aGlzLnJvb3QgPSBnbS5hZGRSb290KCk7XHJcblxyXG4gIGlmICghdGhpcy5vcHRpb25zLnRpbGUpIHtcclxuICAgIHRoaXMucHJvY2Vzc0NoaWxkcmVuTGlzdCh0aGlzLnJvb3QsIG5vZGVzLm9ycGhhbnMoKSk7XHJcbiAgfVxyXG4gIGVsc2Uge1xyXG4gICAgLy8gRmluZCB6ZXJvIGRlZ3JlZSBub2RlcyBhbmQgY3JlYXRlIGEgY29tcG91bmQgZm9yIGVhY2ggbGV2ZWxcclxuICAgIHZhciBtZW1iZXJHcm91cHMgPSB0aGlzLmdyb3VwWmVyb0RlZ3JlZU1lbWJlcnMoKTtcclxuICAgIC8vIFRpbGUgYW5kIGNsZWFyIGNoaWxkcmVuIG9mIGVhY2ggY29tcG91bmRcclxuICAgIHZhciB0aWxlZE1lbWJlclBhY2sgPSB0aGlzLmNsZWFyQ29tcG91bmRzKHRoaXMub3B0aW9ucyk7XHJcbiAgICAvLyBTZXBhcmF0ZWx5IHRpbGUgYW5kIGNsZWFyIHplcm8gZGVncmVlIG5vZGVzIGZvciBlYWNoIGxldmVsXHJcbiAgICB2YXIgdGlsZWRaZXJvRGVncmVlTm9kZXMgPSB0aGlzLmNsZWFyWmVyb0RlZ3JlZU1lbWJlcnMobWVtYmVyR3JvdXBzKTtcclxuICB9XHJcblxyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGVkZ2VzLmxlbmd0aDsgaSsrKSB7XHJcbiAgICB2YXIgZWRnZSA9IGVkZ2VzW2ldO1xyXG4gICAgdmFyIHNvdXJjZU5vZGUgPSBfQ29TRUxheW91dC5pZFRvTE5vZGVbZWRnZS5kYXRhKFwic291cmNlXCIpXTtcclxuICAgIHZhciB0YXJnZXROb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW2VkZ2UuZGF0YShcInRhcmdldFwiKV07XHJcbiAgICB2YXIgZTEgPSBnbS5hZGQoX0NvU0VMYXlvdXQubGF5b3V0Lm5ld0VkZ2UoKSwgc291cmNlTm9kZSwgdGFyZ2V0Tm9kZSk7XHJcbiAgICBlMS5pZCA9IGVkZ2UuaWQoKTtcclxuICB9XHJcblxyXG5cclxuICB2YXIgdDEgPSBsYXlvdXQudGhyZWFkO1xyXG5cclxuICBpZiAoIXQxIHx8IHQxLnN0b3BwZWQoKSkgeyAvLyB0cnkgdG8gcmV1c2UgdGhyZWFkc1xyXG4gICAgdDEgPSBsYXlvdXQudGhyZWFkID0gVGhyZWFkKCk7XHJcblxyXG4gICAgdDEucmVxdWlyZShEaW1lbnNpb25ELCAnRGltZW5zaW9uRCcpO1xyXG4gICAgdDEucmVxdWlyZShIYXNoTWFwLCAnSGFzaE1hcCcpO1xyXG4gICAgdDEucmVxdWlyZShIYXNoU2V0LCAnSGFzaFNldCcpO1xyXG4gICAgdDEucmVxdWlyZShJR2VvbWV0cnksICdJR2VvbWV0cnknKTtcclxuICAgIHQxLnJlcXVpcmUoSU1hdGgsICdJTWF0aCcpO1xyXG4gICAgdDEucmVxdWlyZShJbnRlZ2VyLCAnSW50ZWdlcicpO1xyXG4gICAgdDEucmVxdWlyZShQb2ludCwgJ1BvaW50Jyk7XHJcbiAgICB0MS5yZXF1aXJlKFBvaW50RCwgJ1BvaW50RCcpO1xyXG4gICAgdDEucmVxdWlyZShSYW5kb21TZWVkLCAnUmFuZG9tU2VlZCcpO1xyXG4gICAgdDEucmVxdWlyZShSZWN0YW5nbGVELCAnUmVjdGFuZ2xlRCcpO1xyXG4gICAgdDEucmVxdWlyZShUcmFuc2Zvcm0sICdUcmFuc2Zvcm0nKTtcclxuICAgIHQxLnJlcXVpcmUoVW5pcXVlSURHZW5lcmV0b3IsICdVbmlxdWVJREdlbmVyZXRvcicpO1xyXG4gICAgdDEucmVxdWlyZShMR3JhcGhPYmplY3QsICdMR3JhcGhPYmplY3QnKTtcclxuICAgIHQxLnJlcXVpcmUoTEdyYXBoLCAnTEdyYXBoJyk7XHJcbiAgICB0MS5yZXF1aXJlKExFZGdlLCAnTEVkZ2UnKTtcclxuICAgIHQxLnJlcXVpcmUoTEdyYXBoTWFuYWdlciwgJ0xHcmFwaE1hbmFnZXInKTtcclxuICAgIHQxLnJlcXVpcmUoTE5vZGUsICdMTm9kZScpO1xyXG4gICAgdDEucmVxdWlyZShMYXlvdXQsICdMYXlvdXQnKTtcclxuICAgIHQxLnJlcXVpcmUoTGF5b3V0Q29uc3RhbnRzLCAnTGF5b3V0Q29uc3RhbnRzJyk7XHJcbiAgICB0MS5yZXF1aXJlKGxheW91dE9wdGlvbnNQYWNrLCAnbGF5b3V0T3B0aW9uc1BhY2snKTtcclxuICAgIHQxLnJlcXVpcmUoRkRMYXlvdXQsICdGRExheW91dCcpO1xyXG4gICAgdDEucmVxdWlyZShGRExheW91dENvbnN0YW50cywgJ0ZETGF5b3V0Q29uc3RhbnRzJyk7XHJcbiAgICB0MS5yZXF1aXJlKEZETGF5b3V0RWRnZSwgJ0ZETGF5b3V0RWRnZScpO1xyXG4gICAgdDEucmVxdWlyZShGRExheW91dE5vZGUsICdGRExheW91dE5vZGUnKTtcclxuICAgIHQxLnJlcXVpcmUoQ29TRUNvbnN0YW50cywgJ0NvU0VDb25zdGFudHMnKTtcclxuICAgIHQxLnJlcXVpcmUoQ29TRUVkZ2UsICdDb1NFRWRnZScpO1xyXG4gICAgdDEucmVxdWlyZShDb1NFR3JhcGgsICdDb1NFR3JhcGgnKTtcclxuICAgIHQxLnJlcXVpcmUoQ29TRUdyYXBoTWFuYWdlciwgJ0NvU0VHcmFwaE1hbmFnZXInKTtcclxuICAgIHQxLnJlcXVpcmUoQ29TRUxheW91dCwgJ0NvU0VMYXlvdXQnKTtcclxuICAgIHQxLnJlcXVpcmUoQ29TRU5vZGUsICdDb1NFTm9kZScpO1xyXG4gIH1cclxuXHJcbiAgdmFyIG5vZGVzID0gdGhpcy5vcHRpb25zLmVsZXMubm9kZXMoKTtcclxuICB2YXIgZWRnZXMgPSB0aGlzLm9wdGlvbnMuZWxlcy5lZGdlcygpO1xyXG5cclxuICAvLyBGaXJzdCBJIG5lZWQgdG8gY3JlYXRlIHRoZSBkYXRhIHN0cnVjdHVyZSB0byBwYXNzIHRvIHRoZSB3b3JrZXJcclxuICB2YXIgcERhdGEgPSB7XHJcbiAgICAnbm9kZXMnOiBbXSxcclxuICAgICdlZGdlcyc6IFtdXHJcbiAgfTtcclxuXHJcbiAgdmFyIGxub2RlcyA9IGdtLmdldEFsbE5vZGVzKCk7XHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBsbm9kZXMubGVuZ3RoOyBpKyspIHtcclxuICAgIHZhciBsbm9kZSA9IGxub2Rlc1tpXTtcclxuICAgIHZhciBub2RlSWQgPSBsbm9kZS5pZDtcclxuICAgIHZhciBjeU5vZGUgPSB0aGlzLm9wdGlvbnMuY3kuZ2V0RWxlbWVudEJ5SWQobm9kZUlkKTtcclxuICAgIHZhciBwYXJlbnRJZCA9IGN5Tm9kZS5kYXRhKCdwYXJlbnQnKTtcclxuICAgIHZhciB3ID0gbG5vZGUucmVjdC53aWR0aDtcclxuICAgIHZhciBwb3NYID0gbG5vZGUucmVjdC54O1xyXG4gICAgdmFyIHBvc1kgPSBsbm9kZS5yZWN0Lnk7XHJcbiAgICB2YXIgaCA9IGxub2RlLnJlY3QuaGVpZ2h0O1xyXG4gICAgdmFyIGR1bW15X3BhcmVudF9pZCA9IGN5Tm9kZS5kYXRhKCdkdW1teV9wYXJlbnRfaWQnKTtcclxuXHJcbiAgICBwRGF0YVsgJ25vZGVzJyBdLnB1c2goe1xyXG4gICAgICBpZDogbm9kZUlkLFxyXG4gICAgICBwaWQ6IHBhcmVudElkLFxyXG4gICAgICB4OiBwb3NYLFxyXG4gICAgICB5OiBwb3NZLFxyXG4gICAgICB3aWR0aDogdyxcclxuICAgICAgaGVpZ2h0OiBoLFxyXG4gICAgICBkdW1teV9wYXJlbnRfaWQ6IGR1bW15X3BhcmVudF9pZFxyXG4gICAgfSk7XHJcblxyXG4gIH1cclxuXHJcbiAgdmFyIGxlZGdlcyA9IGdtLmdldEFsbEVkZ2VzKCk7XHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBsZWRnZXMubGVuZ3RoOyBpKyspIHtcclxuICAgIHZhciBsZWRnZSA9IGxlZGdlc1tpXTtcclxuICAgIHZhciBlZGdlSWQgPSBsZWRnZS5pZDtcclxuICAgIHZhciBjeUVkZ2UgPSB0aGlzLm9wdGlvbnMuY3kuZ2V0RWxlbWVudEJ5SWQoZWRnZUlkKTtcclxuICAgIHZhciBzcmNOb2RlSWQgPSBjeUVkZ2Uuc291cmNlKCkuaWQoKTtcclxuICAgIHZhciB0Z3ROb2RlSWQgPSBjeUVkZ2UudGFyZ2V0KCkuaWQoKTtcclxuICAgIHBEYXRhWyAnZWRnZXMnIF0ucHVzaCh7XHJcbiAgICAgIGlkOiBlZGdlSWQsXHJcbiAgICAgIHNvdXJjZTogc3JjTm9kZUlkLFxyXG4gICAgICB0YXJnZXQ6IHRndE5vZGVJZFxyXG4gICAgfSk7XHJcbiAgfVxyXG5cclxuICB2YXIgcmVhZHkgPSBmYWxzZTtcclxuXHJcbiAgdDEucGFzcyhwRGF0YSkucnVuKGZ1bmN0aW9uIChwRGF0YSkge1xyXG4gICAgdmFyIGxvZyA9IGZ1bmN0aW9uIChtc2cpIHtcclxuICAgICAgYnJvYWRjYXN0KHtsb2c6IG1zZ30pO1xyXG4gICAgfTtcclxuXHJcbiAgICBsb2coXCJzdGFydCB0aHJlYWRcIik7XHJcblxyXG4gICAgLy90aGUgbGF5b3V0IHdpbGwgYmUgcnVuIGluIHRoZSB0aHJlYWQgYW5kIHRoZSByZXN1bHRzIGFyZSB0byBiZSBwYXNzZWRcclxuICAgIC8vdG8gdGhlIG1haW4gdGhyZWFkIHdpdGggdGhlIHJlc3VsdCBtYXBcclxuICAgIHZhciBsYXlvdXRfdCA9IG5ldyBDb1NFTGF5b3V0KCk7XHJcbiAgICB2YXIgZ21fdCA9IGxheW91dF90Lm5ld0dyYXBoTWFuYWdlcigpO1xyXG4gICAgdmFyIG5ncmFwaCA9IGdtX3QubGF5b3V0Lm5ld0dyYXBoKCk7XHJcbiAgICB2YXIgbm5vZGUgPSBnbV90LmxheW91dC5uZXdOb2RlKG51bGwpO1xyXG4gICAgdmFyIHJvb3QgPSBnbV90LmFkZChuZ3JhcGgsIG5ub2RlKTtcclxuICAgIHJvb3QuZ3JhcGhNYW5hZ2VyID0gZ21fdDtcclxuICAgIGdtX3Quc2V0Um9vdEdyYXBoKHJvb3QpO1xyXG4gICAgdmFyIHJvb3RfdCA9IGdtX3Qucm9vdEdyYXBoO1xyXG5cclxuICAgIC8vbWFwcyBmb3IgaW5uZXIgdXNhZ2Ugb2YgdGhlIHRocmVhZFxyXG4gICAgdmFyIG9ycGhhbnNfdCA9IFtdO1xyXG4gICAgdmFyIGlkVG9MTm9kZV90ID0ge307XHJcbiAgICB2YXIgY2hpbGRyZW5NYXAgPSB7fTtcclxuXHJcbiAgICAvL0EgbWFwIG9mIG5vZGUgaWQgdG8gY29ycmVzcG9uZGluZyBub2RlIHBvc2l0aW9uIGFuZCBzaXplc1xyXG4gICAgLy9pdCBpcyB0byBiZSByZXR1cm5lZCBhdCB0aGUgZW5kIG9mIHRoZSB0aHJlYWQgZnVuY3Rpb25cclxuICAgIHZhciByZXN1bHQgPSB7fTtcclxuXHJcbiAgICAvL3RoaXMgZnVuY3Rpb24gaXMgc2ltaWxhciB0byBwcm9jZXNzQ2hpbGRyZW5MaXN0IGZ1bmN0aW9uIGluIHRoZSBtYWluIHRocmVhZFxyXG4gICAgLy9pdCBpcyB0byBwcm9jZXNzIHRoZSBub2RlcyBpbiBjb3JyZWN0IG9yZGVyIHJlY3Vyc2l2ZWx5XHJcbiAgICB2YXIgcHJvY2Vzc05vZGVzID0gZnVuY3Rpb24gKHBhcmVudCwgY2hpbGRyZW4pIHtcclxuICAgICAgdmFyIHNpemUgPSBjaGlsZHJlbi5sZW5ndGg7XHJcbiAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgc2l6ZTsgaSsrKSB7XHJcbiAgICAgICAgdmFyIHRoZUNoaWxkID0gY2hpbGRyZW5baV07XHJcbiAgICAgICAgdmFyIGNoaWxkcmVuX29mX2NoaWxkcmVuID0gY2hpbGRyZW5NYXBbdGhlQ2hpbGQuaWRdO1xyXG4gICAgICAgIHZhciB0aGVOb2RlO1xyXG5cclxuICAgICAgICBpZiAodGhlQ2hpbGQud2lkdGggIT0gbnVsbFxyXG4gICAgICAgICAgICAgICAgJiYgdGhlQ2hpbGQuaGVpZ2h0ICE9IG51bGwpIHtcclxuICAgICAgICAgIHRoZU5vZGUgPSBwYXJlbnQuYWRkKG5ldyBDb1NFTm9kZShnbV90LFxyXG4gICAgICAgICAgICAgICAgICBuZXcgUG9pbnREKHRoZUNoaWxkLngsIHRoZUNoaWxkLnkpLFxyXG4gICAgICAgICAgICAgICAgICBuZXcgRGltZW5zaW9uRChwYXJzZUZsb2F0KHRoZUNoaWxkLndpZHRoKSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICBwYXJzZUZsb2F0KHRoZUNoaWxkLmhlaWdodCkpKSk7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGVsc2Uge1xyXG4gICAgICAgICAgdGhlTm9kZSA9IHBhcmVudC5hZGQobmV3IENvU0VOb2RlKGdtX3QpKTtcclxuICAgICAgICB9XHJcbiAgICAgICAgdGhlTm9kZS5pZCA9IHRoZUNoaWxkLmlkO1xyXG4gICAgICAgIGlkVG9MTm9kZV90W3RoZUNoaWxkLmlkXSA9IHRoZU5vZGU7XHJcblxyXG4gICAgICAgIGlmIChpc05hTih0aGVOb2RlLnJlY3QueCkpIHtcclxuICAgICAgICAgIHRoZU5vZGUucmVjdC54ID0gMDtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIGlmIChpc05hTih0aGVOb2RlLnJlY3QueSkpIHtcclxuICAgICAgICAgIHRoZU5vZGUucmVjdC55ID0gMDtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIGlmIChjaGlsZHJlbl9vZl9jaGlsZHJlbiAhPSBudWxsICYmIGNoaWxkcmVuX29mX2NoaWxkcmVuLmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgIHZhciB0aGVOZXdHcmFwaDtcclxuICAgICAgICAgIHRoZU5ld0dyYXBoID0gbGF5b3V0X3QuZ2V0R3JhcGhNYW5hZ2VyKCkuYWRkKGxheW91dF90Lm5ld0dyYXBoKCksIHRoZU5vZGUpO1xyXG4gICAgICAgICAgdGhlTmV3R3JhcGguZ3JhcGhNYW5hZ2VyID0gZ21fdDtcclxuICAgICAgICAgIHByb2Nlc3NOb2Rlcyh0aGVOZXdHcmFwaCwgY2hpbGRyZW5fb2ZfY2hpbGRyZW4pO1xyXG4gICAgICAgIH1cclxuICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8vZmlsbCB0aGUgY2hpZHJlbk1hcCBhbmQgb3JwaGFuc190IG1hcHMgdG8gcHJvY2VzcyB0aGUgbm9kZXMgaW4gdGhlIGNvcnJlY3Qgb3JkZXJcclxuICAgIHZhciBub2RlcyA9IHBEYXRhLm5vZGVzO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub2Rlcy5sZW5ndGg7IGkrKykge1xyXG4gICAgICB2YXIgdGhlTm9kZSA9IG5vZGVzW2ldO1xyXG4gICAgICB2YXIgcF9pZCA9IHRoZU5vZGUucGlkO1xyXG4gICAgICBpZiAocF9pZCAhPSBudWxsKSB7XHJcbiAgICAgICAgaWYgKGNoaWxkcmVuTWFwW3BfaWRdID09IG51bGwpIHtcclxuICAgICAgICAgIGNoaWxkcmVuTWFwW3BfaWRdID0gW107XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGNoaWxkcmVuTWFwW3BfaWRdLnB1c2godGhlTm9kZSk7XHJcbiAgICAgIH1cclxuICAgICAgZWxzZSB7XHJcbiAgICAgICAgb3JwaGFuc190LnB1c2godGhlTm9kZSk7XHJcbiAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBwcm9jZXNzTm9kZXMocm9vdF90LCBvcnBoYW5zX3QpO1xyXG5cclxuICAgIC8vaGFuZGxlIHRoZSBlZGdlc1xyXG4gICAgdmFyIGVkZ2VzID0gcERhdGEuZWRnZXM7XHJcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGVkZ2VzLmxlbmd0aDsgaSsrKSB7XHJcbiAgICAgIHZhciBlZGdlID0gZWRnZXNbaV07XHJcbiAgICAgIHZhciBzb3VyY2VOb2RlID0gaWRUb0xOb2RlX3RbZWRnZS5zb3VyY2VdO1xyXG4gICAgICB2YXIgdGFyZ2V0Tm9kZSA9IGlkVG9MTm9kZV90W2VkZ2UudGFyZ2V0XTtcclxuICAgICAgdmFyIGUxID0gZ21fdC5hZGQobGF5b3V0X3QubmV3RWRnZSgpLCBzb3VyY2VOb2RlLCB0YXJnZXROb2RlKTtcclxuICAgIH1cclxuXHJcbiAgICAvL3J1biB0aGUgbGF5b3V0IGNyYXRlZCBpbiB0aGlzIHRocmVhZFxyXG4gICAgbGF5b3V0X3QucnVuTGF5b3V0KCk7XHJcblxyXG4gICAgLy9maWxsIHRoZSByZXN1bHQgbWFwXHJcbiAgICBmb3IgKHZhciBpZCBpbiBpZFRvTE5vZGVfdCkge1xyXG4gICAgICB2YXIgbE5vZGUgPSBpZFRvTE5vZGVfdFtpZF07XHJcbiAgICAgIHZhciByZWN0ID0gbE5vZGUucmVjdDtcclxuICAgICAgcmVzdWx0W2lkXSA9IHtcclxuICAgICAgICBpZDogaWQsXHJcbiAgICAgICAgeDogcmVjdC54LFxyXG4gICAgICAgIHk6IHJlY3QueSxcclxuICAgICAgICB3OiByZWN0LndpZHRoLFxyXG4gICAgICAgIGg6IHJlY3QuaGVpZ2h0XHJcbiAgICAgIH07XHJcbiAgICB9XHJcbiAgICB2YXIgc2VlZHMgPSB7fTtcclxuICAgIHNlZWRzLnJzU2VlZCA9IFJhbmRvbVNlZWQuc2VlZDtcclxuICAgIHNlZWRzLnJzWCA9IFJhbmRvbVNlZWQueDtcclxuICAgIHZhciBwYXNzID0ge1xyXG4gICAgICByZXN1bHQ6IHJlc3VsdCxcclxuICAgICAgc2VlZHM6IHNlZWRzXHJcbiAgICB9XHJcbiAgICAvL3JldHVybiB0aGUgcmVzdWx0IG1hcCB0byBwYXNzIGl0IHRvIHRoZSB0aGVuIGZ1bmN0aW9uIGFzIHBhcmFtZXRlclxyXG4gICAgcmV0dXJuIHBhc3M7XHJcbiAgfSkudGhlbihmdW5jdGlvbiAocGFzcykge1xyXG4gICAgdmFyIHJlc3VsdCA9IHBhc3MucmVzdWx0O1xyXG4gICAgdmFyIHNlZWRzID0gcGFzcy5zZWVkcztcclxuICAgIFJhbmRvbVNlZWQuc2VlZCA9IHNlZWRzLnJzU2VlZDtcclxuICAgIFJhbmRvbVNlZWQueCA9IHNlZWRzLnJzWDtcclxuICAgIC8vcmVmcmVzaCB0aGUgbG5vZGUgcG9zaXRpb25zIGFuZCBzaXplcyBieSB1c2luZyByZXN1bHQgbWFwXHJcbiAgICBmb3IgKHZhciBpZCBpbiByZXN1bHQpIHtcclxuICAgICAgdmFyIGxOb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW2lkXTtcclxuICAgICAgdmFyIG5vZGUgPSByZXN1bHRbaWRdO1xyXG4gICAgICBsTm9kZS5yZWN0LnggPSBub2RlLng7XHJcbiAgICAgIGxOb2RlLnJlY3QueSA9IG5vZGUueTtcclxuICAgICAgbE5vZGUucmVjdC53aWR0aCA9IG5vZGUudztcclxuICAgICAgbE5vZGUucmVjdC5oZWlnaHQgPSBub2RlLmg7XHJcbiAgICB9XHJcbiAgICBpZiAoYWZ0ZXIub3B0aW9ucy50aWxlKSB7XHJcbiAgICAgIC8vIFJlcG9wdWxhdGUgbWVtYmVyc1xyXG4gICAgICBhZnRlci5yZXBvcHVsYXRlWmVyb0RlZ3JlZU1lbWJlcnModGlsZWRaZXJvRGVncmVlTm9kZXMpO1xyXG4gICAgICBhZnRlci5yZXBvcHVsYXRlQ29tcG91bmRzKHRpbGVkTWVtYmVyUGFjayk7XHJcbiAgICAgIGFmdGVyLm9wdGlvbnMuZWxlcy5ub2RlcygpLnVwZGF0ZUNvbXBvdW5kQm91bmRzKCk7XHJcbiAgICB9XHJcblxyXG4gICAgYWZ0ZXIub3B0aW9ucy5lbGVzLm5vZGVzKCkucG9zaXRpb25zKGZ1bmN0aW9uIChpLCBlbGUpIHtcclxuICAgICAgdmFyIHRoZUlkID0gZWxlLmRhdGEoJ2lkJyk7XHJcbiAgICAgIHZhciBsTm9kZSA9IF9Db1NFTGF5b3V0LmlkVG9MTm9kZVt0aGVJZF07XHJcblxyXG4gICAgICByZXR1cm4ge1xyXG4gICAgICAgIHg6IGxOb2RlLmdldFJlY3QoKS5nZXRDZW50ZXJYKCksXHJcbiAgICAgICAgeTogbE5vZGUuZ2V0UmVjdCgpLmdldENlbnRlclkoKVxyXG4gICAgICB9O1xyXG4gICAgfSk7XHJcblxyXG4gICAgaWYgKGFmdGVyLm9wdGlvbnMuZml0KVxyXG4gICAgICBhZnRlci5vcHRpb25zLmN5LmZpdChhZnRlci5vcHRpb25zLmVsZXMubm9kZXMoKSwgYWZ0ZXIub3B0aW9ucy5wYWRkaW5nKTtcclxuXHJcbiAgICAvL3RyaWdnZXIgbGF5b3V0cmVhZHkgd2hlbiBlYWNoIG5vZGUgaGFzIGhhZCBpdHMgcG9zaXRpb24gc2V0IGF0IGxlYXN0IG9uY2VcclxuICAgIGlmICghcmVhZHkpIHtcclxuICAgICAgYWZ0ZXIuY3kub25lKCdsYXlvdXRyZWFkeScsIGFmdGVyLm9wdGlvbnMucmVhZHkpO1xyXG4gICAgICBhZnRlci5jeS50cmlnZ2VyKCdsYXlvdXRyZWFkeScpO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIHRyaWdnZXIgbGF5b3V0c3RvcCB3aGVuIHRoZSBsYXlvdXQgc3RvcHMgKGUuZy4gZmluaXNoZXMpXHJcbiAgICBhZnRlci5jeS5vbmUoJ2xheW91dHN0b3AnLCBhZnRlci5vcHRpb25zLnN0b3ApO1xyXG4gICAgYWZ0ZXIuY3kudHJpZ2dlcignbGF5b3V0c3RvcCcpO1xyXG4gICAgdDEuc3RvcCgpO1xyXG5cclxuICAgIGFmdGVyLm9wdGlvbnMuZWxlcy5ub2RlcygpLnJlbW92ZURhdGEoJ2R1bW15X3BhcmVudF9pZCcpO1xyXG4gIH0pO1xyXG5cclxuICB0MS5vbignbWVzc2FnZScsIGZ1bmN0aW9uIChlKSB7XHJcbiAgICB2YXIgbG9nTXNnID0gZS5tZXNzYWdlLmxvZztcclxuICAgIGlmIChsb2dNc2cgIT0gbnVsbCkge1xyXG4gICAgICBjb25zb2xlLmxvZygnVGhyZWFkIGxvZzogJyArIGxvZ01zZyk7XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICAgIHZhciBwRGF0YSA9IGUubWVzc2FnZS5wRGF0YTtcclxuICAgIGlmIChwRGF0YSAhPSBudWxsKSB7XHJcbiAgICAgIGFmdGVyLm9wdGlvbnMuZWxlcy5ub2RlcygpLnBvc2l0aW9ucyhmdW5jdGlvbiAoaSwgZWxlKSB7XHJcbiAgICAgICAgaWYgKGVsZS5kYXRhKCdkdW1teV9wYXJlbnRfaWQnKSkge1xyXG4gICAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgeDogcERhdGFbZWxlLmRhdGEoJ2R1bW15X3BhcmVudF9pZCcpXS54LFxyXG4gICAgICAgICAgICB5OiBwRGF0YVtlbGUuZGF0YSgnZHVtbXlfcGFyZW50X2lkJyldLnlcclxuICAgICAgICAgIH07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIHZhciB0aGVJZCA9IGVsZS5kYXRhKCdpZCcpO1xyXG4gICAgICAgIHZhciBwTm9kZSA9IHBEYXRhW3RoZUlkXTtcclxuICAgICAgICB2YXIgdGVtcCA9IHRoaXM7XHJcbiAgICAgICAgd2hpbGUgKHBOb2RlID09IG51bGwpIHtcclxuICAgICAgICAgIHRlbXAgPSB0ZW1wLnBhcmVudCgpWzBdO1xyXG4gICAgICAgICAgcE5vZGUgPSBwRGF0YVt0ZW1wLmlkKCldO1xyXG4gICAgICAgICAgcERhdGFbdGhlSWRdID0gcE5vZGU7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICB4OiBwTm9kZS54LFxyXG4gICAgICAgICAgeTogcE5vZGUueVxyXG4gICAgICAgIH07XHJcbiAgICAgIH0pO1xyXG5cclxuICAgICAgaWYgKGFmdGVyLm9wdGlvbnMuZml0KVxyXG4gICAgICAgIGFmdGVyLm9wdGlvbnMuY3kuZml0KGFmdGVyLm9wdGlvbnMuZWxlcy5ub2RlcygpLCBhZnRlci5vcHRpb25zLnBhZGRpbmcpO1xyXG5cclxuICAgICAgaWYgKCFyZWFkeSkge1xyXG4gICAgICAgIHJlYWR5ID0gdHJ1ZTtcclxuICAgICAgICBhZnRlci5vbmUoJ2xheW91dHJlYWR5JywgYWZ0ZXIub3B0aW9ucy5yZWFkeSk7XHJcbiAgICAgICAgYWZ0ZXIudHJpZ2dlcih7dHlwZTogJ2xheW91dHJlYWR5JywgbGF5b3V0OiBhZnRlcn0pO1xyXG4gICAgICB9XHJcbiAgICAgIHJldHVybjtcclxuICAgIH1cclxuICB9KTtcclxuXHJcbiAgcmV0dXJuIHRoaXM7IC8vIGNoYWluaW5nXHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuZ2V0VG9CZVRpbGVkID0gZnVuY3Rpb24gKG5vZGUpIHtcclxuICB2YXIgaWQgPSBub2RlLmRhdGEoXCJpZFwiKTtcclxuICAvL2ZpcnN0bHkgY2hlY2sgdGhlIHByZXZpb3VzIHJlc3VsdHNcclxuICBpZiAoX0NvU0VMYXlvdXQudG9CZVRpbGVkW2lkXSAhPSBudWxsKSB7XHJcbiAgICByZXR1cm4gX0NvU0VMYXlvdXQudG9CZVRpbGVkW2lkXTtcclxuICB9XHJcblxyXG4gIC8vb25seSBjb21wb3VuZCBub2RlcyBhcmUgdG8gYmUgdGlsZWRcclxuICB2YXIgY2hpbGRyZW4gPSBub2RlLmNoaWxkcmVuKCk7XHJcbiAgaWYgKGNoaWxkcmVuID09IG51bGwgfHwgY2hpbGRyZW4ubGVuZ3RoID09IDApIHtcclxuICAgIF9Db1NFTGF5b3V0LnRvQmVUaWxlZFtpZF0gPSBmYWxzZTtcclxuICAgIHJldHVybiBmYWxzZTtcclxuICB9XHJcblxyXG4gIC8vYSBjb21wb3VuZCBub2RlIGlzIG5vdCB0byBiZSB0aWxlZCBpZiBhbGwgb2YgaXRzIGNvbXBvdW5kIGNoaWxkcmVuIGFyZSBub3QgdG8gYmUgdGlsZWRcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IGNoaWxkcmVuLmxlbmd0aDsgaSsrKSB7XHJcbiAgICB2YXIgdGhlQ2hpbGQgPSBjaGlsZHJlbltpXTtcclxuXHJcbiAgICBpZiAodGhpcy5nZXROb2RlRGVncmVlKHRoZUNoaWxkKSA+IDApIHtcclxuICAgICAgX0NvU0VMYXlvdXQudG9CZVRpbGVkW2lkXSA9IGZhbHNlO1xyXG4gICAgICByZXR1cm4gZmFsc2U7XHJcbiAgICB9XHJcblxyXG4gICAgLy9wYXNzIHRoZSBjaGlsZHJlbiBub3QgaGF2aW5nIHRoZSBjb21wb3VuZCBzdHJ1Y3R1cmVcclxuICAgIGlmICh0aGVDaGlsZC5jaGlsZHJlbigpID09IG51bGwgfHwgdGhlQ2hpbGQuY2hpbGRyZW4oKS5sZW5ndGggPT0gMCkge1xyXG4gICAgICBfQ29TRUxheW91dC50b0JlVGlsZWRbdGhlQ2hpbGQuZGF0YShcImlkXCIpXSA9IGZhbHNlO1xyXG4gICAgICBjb250aW51ZTtcclxuICAgIH1cclxuXHJcbiAgICBpZiAoIXRoaXMuZ2V0VG9CZVRpbGVkKHRoZUNoaWxkKSkge1xyXG4gICAgICBfQ29TRUxheW91dC50b0JlVGlsZWRbaWRdID0gZmFsc2U7XHJcbiAgICAgIHJldHVybiBmYWxzZTtcclxuICAgIH1cclxuICB9XHJcbiAgX0NvU0VMYXlvdXQudG9CZVRpbGVkW2lkXSA9IHRydWU7XHJcbiAgcmV0dXJuIHRydWU7XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuZ2V0Tm9kZURlZ3JlZSA9IGZ1bmN0aW9uIChub2RlKSB7XHJcbiAgdmFyIGlkID0gbm9kZS5pZCgpO1xyXG4gIHZhciBlZGdlcyA9IHRoaXMub3B0aW9ucy5lbGVzLmVkZ2VzKCkuZmlsdGVyKGZ1bmN0aW9uIChpLCBlbGUpIHtcclxuICAgIHZhciBzb3VyY2UgPSBlbGUuZGF0YSgnc291cmNlJyk7XHJcbiAgICB2YXIgdGFyZ2V0ID0gZWxlLmRhdGEoJ3RhcmdldCcpO1xyXG4gICAgaWYgKHNvdXJjZSAhPSB0YXJnZXQgJiYgKHNvdXJjZSA9PSBpZCB8fCB0YXJnZXQgPT0gaWQpKSB7XHJcbiAgICAgIHJldHVybiB0cnVlO1xyXG4gICAgfVxyXG4gIH0pO1xyXG4gIHJldHVybiBlZGdlcy5sZW5ndGg7XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuZ2V0Tm9kZURlZ3JlZVdpdGhDaGlsZHJlbiA9IGZ1bmN0aW9uIChub2RlKSB7XHJcbiAgdmFyIGRlZ3JlZSA9IHRoaXMuZ2V0Tm9kZURlZ3JlZShub2RlKTtcclxuICB2YXIgY2hpbGRyZW4gPSBub2RlLmNoaWxkcmVuKCk7XHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBjaGlsZHJlbi5sZW5ndGg7IGkrKykge1xyXG4gICAgdmFyIGNoaWxkID0gY2hpbGRyZW5baV07XHJcbiAgICBkZWdyZWUgKz0gdGhpcy5nZXROb2RlRGVncmVlV2l0aENoaWxkcmVuKGNoaWxkKTtcclxuICB9XHJcbiAgcmV0dXJuIGRlZ3JlZTtcclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5ncm91cFplcm9EZWdyZWVNZW1iZXJzID0gZnVuY3Rpb24gKCkge1xyXG4gIC8vIGFycmF5IG9mIFtwYXJlbnRfaWQgeCBvbmVEZWdyZWVOb2RlX2lkXSBcclxuICB2YXIgdGVtcE1lbWJlckdyb3VwcyA9IFtdO1xyXG4gIHZhciBtZW1iZXJHcm91cHMgPSBbXTtcclxuICB2YXIgc2VsZiA9IHRoaXM7XHJcbiAgLy8gRmluZCBhbGwgemVybyBkZWdyZWUgbm9kZXMgd2hpY2ggYXJlbid0IGNvdmVyZWQgYnkgYSBjb21wb3VuZFxyXG4gIHZhciB6ZXJvRGVncmVlID0gdGhpcy5vcHRpb25zLmVsZXMubm9kZXMoKS5maWx0ZXIoZnVuY3Rpb24gKGksIGVsZSkge1xyXG4gICAgaWYgKHNlbGYuZ2V0Tm9kZURlZ3JlZVdpdGhDaGlsZHJlbihlbGUpID09IDAgJiYgKGVsZS5wYXJlbnQoKS5sZW5ndGggPT0gMCB8fCAoZWxlLnBhcmVudCgpLmxlbmd0aCA+IDAgJiYgIXNlbGYuZ2V0VG9CZVRpbGVkKGVsZS5wYXJlbnQoKVswXSkpKSlcclxuICAgICAgcmV0dXJuIHRydWU7XHJcbiAgICBlbHNlXHJcbiAgICAgIHJldHVybiBmYWxzZTtcclxuICB9KTtcclxuXHJcbiAgLy8gQ3JlYXRlIGEgbWFwIG9mIHBhcmVudCBub2RlIGFuZCBpdHMgemVybyBkZWdyZWUgbWVtYmVyc1xyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgemVyb0RlZ3JlZS5sZW5ndGg7IGkrKylcclxuICB7XHJcbiAgICB2YXIgbm9kZSA9IHplcm9EZWdyZWVbaV07XHJcbiAgICB2YXIgcF9pZCA9IG5vZGUucGFyZW50KCkuaWQoKTtcclxuXHJcbiAgICBpZiAodHlwZW9mIHRlbXBNZW1iZXJHcm91cHNbcF9pZF0gPT09IFwidW5kZWZpbmVkXCIpXHJcbiAgICAgIHRlbXBNZW1iZXJHcm91cHNbcF9pZF0gPSBbXTtcclxuXHJcbiAgICB0ZW1wTWVtYmVyR3JvdXBzW3BfaWRdID0gdGVtcE1lbWJlckdyb3Vwc1twX2lkXS5jb25jYXQobm9kZSk7XHJcbiAgfVxyXG5cclxuICAvLyBJZiB0aGVyZSBhcmUgYXQgbGVhc3QgdHdvIG5vZGVzIGF0IGEgbGV2ZWwsIGNyZWF0ZSBhIGR1bW15IGNvbXBvdW5kIGZvciB0aGVtXHJcbiAgZm9yICh2YXIgcF9pZCBpbiB0ZW1wTWVtYmVyR3JvdXBzKSB7XHJcbiAgICBpZiAodGVtcE1lbWJlckdyb3Vwc1twX2lkXS5sZW5ndGggPiAxKSB7XHJcbiAgICAgIHZhciBkdW1teUNvbXBvdW5kSWQgPSBcIkR1bW15Q29tcG91bmRfXCIgKyBwX2lkO1xyXG4gICAgICBtZW1iZXJHcm91cHNbZHVtbXlDb21wb3VuZElkXSA9IHRlbXBNZW1iZXJHcm91cHNbcF9pZF07XHJcblxyXG4gICAgICAvLyBDcmVhdGUgYSBkdW1teSBjb21wb3VuZFxyXG4gICAgICBpZiAodGhpcy5vcHRpb25zLmN5LmdldEVsZW1lbnRCeUlkKGR1bW15Q29tcG91bmRJZCkuZW1wdHkoKSkge1xyXG4gICAgICAgIHRoaXMub3B0aW9ucy5jeS5hZGQoe1xyXG4gICAgICAgICAgZ3JvdXA6IFwibm9kZXNcIixcclxuICAgICAgICAgIGRhdGE6IHtpZDogZHVtbXlDb21wb3VuZElkLCBwYXJlbnQ6IHBfaWRcclxuICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgdmFyIGR1bW15ID0gdGhpcy5vcHRpb25zLmN5Lm5vZGVzKClbdGhpcy5vcHRpb25zLmN5Lm5vZGVzKCkubGVuZ3RoIC0gMV07XHJcbiAgICAgICAgdGhpcy5vcHRpb25zLmVsZXMgPSB0aGlzLm9wdGlvbnMuZWxlcy51bmlvbihkdW1teSk7XHJcbiAgICAgICAgZHVtbXkuaGlkZSgpO1xyXG5cclxuICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHRlbXBNZW1iZXJHcm91cHNbcF9pZF0ubGVuZ3RoOyBpKyspIHtcclxuICAgICAgICAgIGlmIChpID09IDApIHtcclxuICAgICAgICAgICAgZHVtbXkuZGF0YSgndGVtcGNoaWxkcmVuJywgW10pO1xyXG4gICAgICAgICAgfVxyXG4gICAgICAgICAgdmFyIG5vZGUgPSB0ZW1wTWVtYmVyR3JvdXBzW3BfaWRdW2ldO1xyXG4gICAgICAgICAgbm9kZS5kYXRhKCdkdW1teV9wYXJlbnRfaWQnLCBkdW1teUNvbXBvdW5kSWQpO1xyXG4gICAgICAgICAgdGhpcy5vcHRpb25zLmN5LmFkZCh7XHJcbiAgICAgICAgICAgIGdyb3VwOiBcIm5vZGVzXCIsXHJcbiAgICAgICAgICAgIGRhdGE6IHtwYXJlbnQ6IGR1bW15Q29tcG91bmRJZCwgd2lkdGg6IG5vZGUud2lkdGgoKSwgaGVpZ2h0OiBub2RlLmhlaWdodCgpXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgdmFyIHRlbXBjaGlsZCA9IHRoaXMub3B0aW9ucy5jeS5ub2RlcygpW3RoaXMub3B0aW9ucy5jeS5ub2RlcygpLmxlbmd0aCAtIDFdO1xyXG4gICAgICAgICAgdGVtcGNoaWxkLmhpZGUoKTtcclxuICAgICAgICAgIHRlbXBjaGlsZC5jc3MoJ3dpZHRoJywgdGVtcGNoaWxkLmRhdGEoJ3dpZHRoJykpO1xyXG4gICAgICAgICAgdGVtcGNoaWxkLmNzcygnaGVpZ2h0JywgdGVtcGNoaWxkLmRhdGEoJ2hlaWdodCcpKTtcclxuICAgICAgICAgIHRlbXBjaGlsZC53aWR0aCgpO1xyXG4gICAgICAgICAgZHVtbXkuZGF0YSgndGVtcGNoaWxkcmVuJykucHVzaCh0ZW1wY2hpbGQpO1xyXG4gICAgICAgIH1cclxuICAgICAgfVxyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcmV0dXJuIG1lbWJlckdyb3VwcztcclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5wZXJmb3JtREZTT25Db21wb3VuZHMgPSBmdW5jdGlvbiAob3B0aW9ucykge1xyXG4gIHZhciBjb21wb3VuZE9yZGVyID0gW107XHJcblxyXG4gIHZhciByb290cyA9IHRoaXMub3B0aW9ucy5lbGVzLm5vZGVzKCkub3JwaGFucygpO1xyXG4gIHRoaXMuZmlsbENvbXBleE9yZGVyQnlERlMoY29tcG91bmRPcmRlciwgcm9vdHMpO1xyXG5cclxuICByZXR1cm4gY29tcG91bmRPcmRlcjtcclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5maWxsQ29tcGV4T3JkZXJCeURGUyA9IGZ1bmN0aW9uIChjb21wb3VuZE9yZGVyLCBjaGlsZHJlbikge1xyXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgY2hpbGRyZW4ubGVuZ3RoOyBpKyspIHtcclxuICAgIHZhciBjaGlsZCA9IGNoaWxkcmVuW2ldO1xyXG4gICAgdGhpcy5maWxsQ29tcGV4T3JkZXJCeURGUyhjb21wb3VuZE9yZGVyLCBjaGlsZC5jaGlsZHJlbigpKTtcclxuICAgIGlmICh0aGlzLmdldFRvQmVUaWxlZChjaGlsZCkpIHtcclxuICAgICAgY29tcG91bmRPcmRlci5wdXNoKGNoaWxkKTtcclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuY2xlYXJDb21wb3VuZHMgPSBmdW5jdGlvbiAob3B0aW9ucykge1xyXG4gIHZhciBjaGlsZEdyYXBoTWFwID0gW107XHJcblxyXG4gIC8vIEdldCBjb21wb3VuZCBvcmRlcmluZyBieSBmaW5kaW5nIHRoZSBpbm5lciBvbmUgZmlyc3RcclxuICB2YXIgY29tcG91bmRPcmRlciA9IHRoaXMucGVyZm9ybURGU09uQ29tcG91bmRzKG9wdGlvbnMpO1xyXG4gIF9Db1NFTGF5b3V0LmNvbXBvdW5kT3JkZXIgPSBjb21wb3VuZE9yZGVyO1xyXG4gIHRoaXMucHJvY2Vzc0NoaWxkcmVuTGlzdCh0aGlzLnJvb3QsIHRoaXMub3B0aW9ucy5lbGVzLm5vZGVzKCkub3JwaGFucygpKTtcclxuXHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBjb21wb3VuZE9yZGVyLmxlbmd0aDsgaSsrKSB7XHJcbiAgICAvLyBmaW5kIHRoZSBjb3JyZXNwb25kaW5nIGxheW91dCBub2RlXHJcbiAgICB2YXIgbENvbXBvdW5kTm9kZSA9IF9Db1NFTGF5b3V0LmlkVG9MTm9kZVtjb21wb3VuZE9yZGVyW2ldLmlkKCldO1xyXG5cclxuICAgIGNoaWxkR3JhcGhNYXBbY29tcG91bmRPcmRlcltpXS5pZCgpXSA9IGNvbXBvdW5kT3JkZXJbaV0uY2hpbGRyZW4oKTtcclxuXHJcbiAgICAvLyBSZW1vdmUgY2hpbGRyZW4gb2YgY29tcG91bmRzIFxyXG4gICAgbENvbXBvdW5kTm9kZS5jaGlsZCA9IG51bGw7XHJcbiAgfVxyXG5cclxuICAvLyBUaWxlIHRoZSByZW1vdmVkIGNoaWxkcmVuXHJcbiAgdmFyIHRpbGVkTWVtYmVyUGFjayA9IHRoaXMudGlsZUNvbXBvdW5kTWVtYmVycyhjaGlsZEdyYXBoTWFwKTtcclxuXHJcbiAgcmV0dXJuIHRpbGVkTWVtYmVyUGFjaztcclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5jbGVhclplcm9EZWdyZWVNZW1iZXJzID0gZnVuY3Rpb24gKG1lbWJlckdyb3Vwcykge1xyXG4gIHZhciB0aWxlZFplcm9EZWdyZWVQYWNrID0gW107XHJcblxyXG4gIGZvciAodmFyIGlkIGluIG1lbWJlckdyb3Vwcykge1xyXG4gICAgdmFyIGNvbXBvdW5kTm9kZSA9IF9Db1NFTGF5b3V0LmlkVG9MTm9kZVtpZF07XHJcblxyXG4gICAgdGlsZWRaZXJvRGVncmVlUGFja1tpZF0gPSB0aGlzLnRpbGVOb2RlcyhtZW1iZXJHcm91cHNbaWRdKTtcclxuXHJcbiAgICAvLyBTZXQgdGhlIHdpZHRoIGFuZCBoZWlnaHQgb2YgdGhlIGR1bW15IGNvbXBvdW5kIGFzIGNhbGN1bGF0ZWRcclxuICAgIGNvbXBvdW5kTm9kZS5yZWN0LndpZHRoID0gdGlsZWRaZXJvRGVncmVlUGFja1tpZF0ud2lkdGg7XHJcbiAgICBjb21wb3VuZE5vZGUucmVjdC5oZWlnaHQgPSB0aWxlZFplcm9EZWdyZWVQYWNrW2lkXS5oZWlnaHQ7XHJcbiAgfVxyXG4gIHJldHVybiB0aWxlZFplcm9EZWdyZWVQYWNrO1xyXG59O1xyXG5cclxuX0NvU0VMYXlvdXQucHJvdG90eXBlLnJlcG9wdWxhdGVDb21wb3VuZHMgPSBmdW5jdGlvbiAodGlsZWRNZW1iZXJQYWNrKSB7XHJcbiAgZm9yICh2YXIgaSA9IF9Db1NFTGF5b3V0LmNvbXBvdW5kT3JkZXIubGVuZ3RoIC0gMTsgaSA+PSAwOyBpLS0pIHtcclxuICAgIHZhciBpZCA9IF9Db1NFTGF5b3V0LmNvbXBvdW5kT3JkZXJbaV0uaWQoKTtcclxuICAgIHZhciBsQ29tcG91bmROb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW2lkXTtcclxuXHJcbiAgICB0aGlzLmFkanVzdExvY2F0aW9ucyh0aWxlZE1lbWJlclBhY2tbaWRdLCBsQ29tcG91bmROb2RlLnJlY3QueCwgbENvbXBvdW5kTm9kZS5yZWN0LnkpO1xyXG4gIH1cclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5yZXBvcHVsYXRlWmVyb0RlZ3JlZU1lbWJlcnMgPSBmdW5jdGlvbiAodGlsZWRQYWNrKSB7XHJcbiAgZm9yICh2YXIgaSBpbiB0aWxlZFBhY2spIHtcclxuICAgIHZhciBjb21wb3VuZCA9IHRoaXMuY3kuZ2V0RWxlbWVudEJ5SWQoaSk7XHJcbiAgICB2YXIgY29tcG91bmROb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW2ldO1xyXG5cclxuICAgIC8vIEFkanVzdCB0aGUgcG9zaXRpb25zIG9mIG5vZGVzIHdydCBpdHMgY29tcG91bmRcclxuICAgIHRoaXMuYWRqdXN0TG9jYXRpb25zKHRpbGVkUGFja1tpXSwgY29tcG91bmROb2RlLnJlY3QueCwgY29tcG91bmROb2RlLnJlY3QueSk7XHJcblxyXG4gICAgdmFyIHRlbXBjaGlsZHJlbiA9IGNvbXBvdW5kLmRhdGEoJ3RlbXBjaGlsZHJlbicpO1xyXG4gICAgZm9yICh2YXIgaSA9IDA7IGkgPCB0ZW1wY2hpbGRyZW4ubGVuZ3RoOyBpKyspIHtcclxuICAgICAgdGVtcGNoaWxkcmVuW2ldLnJlbW92ZSgpO1xyXG4gICAgfVxyXG5cclxuICAgIC8vIFJlbW92ZSB0aGUgZHVtbXkgY29tcG91bmRcclxuICAgIGNvbXBvdW5kLnJlbW92ZSgpO1xyXG4gIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIG1ldGhvZCBwbGFjZXMgZWFjaCB6ZXJvIGRlZ3JlZSBtZW1iZXIgd3J0IGdpdmVuICh4LHkpIGNvb3JkaW5hdGVzICh0b3AgbGVmdCkuIFxyXG4gKi9cclxuX0NvU0VMYXlvdXQucHJvdG90eXBlLmFkanVzdExvY2F0aW9ucyA9IGZ1bmN0aW9uIChvcmdhbml6YXRpb24sIHgsIHkpIHtcclxuICB4ICs9IG9yZ2FuaXphdGlvbi5jb21wb3VuZE1hcmdpbjtcclxuICB5ICs9IG9yZ2FuaXphdGlvbi5jb21wb3VuZE1hcmdpbjtcclxuXHJcbiAgdmFyIGxlZnQgPSB4O1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IG9yZ2FuaXphdGlvbi5yb3dzLmxlbmd0aDsgaSsrKSB7XHJcbiAgICB2YXIgcm93ID0gb3JnYW5pemF0aW9uLnJvd3NbaV07XHJcbiAgICB4ID0gbGVmdDtcclxuICAgIHZhciBtYXhIZWlnaHQgPSAwO1xyXG5cclxuICAgIGZvciAodmFyIGogPSAwOyBqIDwgcm93Lmxlbmd0aDsgaisrKSB7XHJcbiAgICAgIHZhciBsbm9kZSA9IHJvd1tqXTtcclxuXHJcbiAgICAgIHZhciBub2RlID0gdGhpcy5jeS5nZXRFbGVtZW50QnlJZChsbm9kZS5pZCk7XHJcbiAgICAgIG5vZGUucG9zaXRpb24oe1xyXG4gICAgICAgIHg6IHggKyBsbm9kZS5yZWN0LndpZHRoIC8gMixcclxuICAgICAgICB5OiB5ICsgbG5vZGUucmVjdC5oZWlnaHQgLyAyXHJcbiAgICAgIH0pO1xyXG5cclxuICAgICAgbG5vZGUucmVjdC54ID0geDsvLyArIGxub2RlLnJlY3Qud2lkdGggLyAyO1xyXG4gICAgICBsbm9kZS5yZWN0LnkgPSB5Oy8vICsgbG5vZGUucmVjdC5oZWlnaHQgLyAyO1xyXG5cclxuICAgICAgeCArPSBsbm9kZS5yZWN0LndpZHRoICsgb3JnYW5pemF0aW9uLmhvcml6b250YWxQYWRkaW5nO1xyXG5cclxuICAgICAgaWYgKGxub2RlLnJlY3QuaGVpZ2h0ID4gbWF4SGVpZ2h0KVxyXG4gICAgICAgIG1heEhlaWdodCA9IGxub2RlLnJlY3QuaGVpZ2h0O1xyXG4gICAgfVxyXG5cclxuICAgIHkgKz0gbWF4SGVpZ2h0ICsgb3JnYW5pemF0aW9uLnZlcnRpY2FsUGFkZGluZztcclxuICB9XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUudGlsZUNvbXBvdW5kTWVtYmVycyA9IGZ1bmN0aW9uIChjaGlsZEdyYXBoTWFwKSB7XHJcbiAgdmFyIHRpbGVkTWVtYmVyUGFjayA9IFtdO1xyXG5cclxuICBmb3IgKHZhciBpZCBpbiBjaGlsZEdyYXBoTWFwKSB7XHJcbiAgICAvLyBBY2Nlc3MgbGF5b3V0SW5mbyBub2RlcyB0byBzZXQgdGhlIHdpZHRoIGFuZCBoZWlnaHQgb2YgY29tcG91bmRzXHJcbiAgICB2YXIgY29tcG91bmROb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW2lkXTtcclxuXHJcbiAgICB0aWxlZE1lbWJlclBhY2tbaWRdID0gdGhpcy50aWxlTm9kZXMoY2hpbGRHcmFwaE1hcFtpZF0pO1xyXG5cclxuICAgIGNvbXBvdW5kTm9kZS5yZWN0LndpZHRoID0gdGlsZWRNZW1iZXJQYWNrW2lkXS53aWR0aCArIDIwO1xyXG4gICAgY29tcG91bmROb2RlLnJlY3QuaGVpZ2h0ID0gdGlsZWRNZW1iZXJQYWNrW2lkXS5oZWlnaHQgKyAyMDtcclxuICB9XHJcblxyXG4gIHJldHVybiB0aWxlZE1lbWJlclBhY2s7XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUudGlsZU5vZGVzID0gZnVuY3Rpb24gKG5vZGVzKSB7XHJcbiAgdmFyIG9yZ2FuaXphdGlvbiA9IHtcclxuICAgIHJvd3M6IFtdLFxyXG4gICAgcm93V2lkdGg6IFtdLFxyXG4gICAgcm93SGVpZ2h0OiBbXSxcclxuICAgIGNvbXBvdW5kTWFyZ2luOiAxMCxcclxuICAgIHdpZHRoOiAyMCxcclxuICAgIGhlaWdodDogMjAsXHJcbiAgICB2ZXJ0aWNhbFBhZGRpbmc6IDEwLFxyXG4gICAgaG9yaXpvbnRhbFBhZGRpbmc6IDEwXHJcbiAgfTtcclxuXHJcbiAgdmFyIGxheW91dE5vZGVzID0gW107XHJcblxyXG4gIC8vIEdldCBsYXlvdXQgbm9kZXNcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IG5vZGVzLmxlbmd0aDsgaSsrKSB7XHJcbiAgICB2YXIgbm9kZSA9IG5vZGVzW2ldO1xyXG4gICAgdmFyIGxOb2RlID0gX0NvU0VMYXlvdXQuaWRUb0xOb2RlW25vZGUuaWQoKV07XHJcblxyXG4gICAgaWYgKCFub2RlLmRhdGEoJ2R1bW15X3BhcmVudF9pZCcpKSB7XHJcbiAgICAgIHZhciBvd25lciA9IGxOb2RlLm93bmVyO1xyXG4gICAgICBvd25lci5yZW1vdmUobE5vZGUpO1xyXG5cclxuICAgICAgdGhpcy5nbS5yZXNldEFsbE5vZGVzKCk7XHJcbiAgICAgIHRoaXMuZ20uZ2V0QWxsTm9kZXMoKTtcclxuICAgIH1cclxuXHJcbiAgICBsYXlvdXROb2Rlcy5wdXNoKGxOb2RlKTtcclxuICB9XHJcblxyXG4gIC8vIFNvcnQgdGhlIG5vZGVzIGluIGFzY2VuZGluZyBvcmRlciBvZiB0aGVpciBhcmVhc1xyXG4gIGxheW91dE5vZGVzLnNvcnQoZnVuY3Rpb24gKG4xLCBuMikge1xyXG4gICAgaWYgKG4xLnJlY3Qud2lkdGggKiBuMS5yZWN0LmhlaWdodCA+IG4yLnJlY3Qud2lkdGggKiBuMi5yZWN0LmhlaWdodClcclxuICAgICAgcmV0dXJuIC0xO1xyXG4gICAgaWYgKG4xLnJlY3Qud2lkdGggKiBuMS5yZWN0LmhlaWdodCA8IG4yLnJlY3Qud2lkdGggKiBuMi5yZWN0LmhlaWdodClcclxuICAgICAgcmV0dXJuIDE7XHJcbiAgICByZXR1cm4gMDtcclxuICB9KTtcclxuXHJcbiAgLy8gQ3JlYXRlIHRoZSBvcmdhbml6YXRpb24gLT4gdGlsZSBtZW1iZXJzXHJcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBsYXlvdXROb2Rlcy5sZW5ndGg7IGkrKykge1xyXG4gICAgdmFyIGxOb2RlID0gbGF5b3V0Tm9kZXNbaV07XHJcblxyXG4gICAgaWYgKG9yZ2FuaXphdGlvbi5yb3dzLmxlbmd0aCA9PSAwKSB7XHJcbiAgICAgIHRoaXMuaW5zZXJ0Tm9kZVRvUm93KG9yZ2FuaXphdGlvbiwgbE5vZGUsIDApO1xyXG4gICAgfVxyXG4gICAgZWxzZSBpZiAodGhpcy5jYW5BZGRIb3Jpem9udGFsKG9yZ2FuaXphdGlvbiwgbE5vZGUucmVjdC53aWR0aCwgbE5vZGUucmVjdC5oZWlnaHQpKSB7XHJcbiAgICAgIHRoaXMuaW5zZXJ0Tm9kZVRvUm93KG9yZ2FuaXphdGlvbiwgbE5vZGUsIHRoaXMuZ2V0U2hvcnRlc3RSb3dJbmRleChvcmdhbml6YXRpb24pKTtcclxuICAgIH1cclxuICAgIGVsc2Uge1xyXG4gICAgICB0aGlzLmluc2VydE5vZGVUb1Jvdyhvcmdhbml6YXRpb24sIGxOb2RlLCBvcmdhbml6YXRpb24ucm93cy5sZW5ndGgpO1xyXG4gICAgfVxyXG5cclxuICAgIHRoaXMuc2hpZnRUb0xhc3RSb3cob3JnYW5pemF0aW9uKTtcclxuICB9XHJcblxyXG4gIHJldHVybiBvcmdhbml6YXRpb247XHJcbn07XHJcblxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuaW5zZXJ0Tm9kZVRvUm93ID0gZnVuY3Rpb24gKG9yZ2FuaXphdGlvbiwgbm9kZSwgcm93SW5kZXgpIHtcclxuICB2YXIgbWluQ29tcG91bmRTaXplID0gb3JnYW5pemF0aW9uLmNvbXBvdW5kTWFyZ2luICogMjtcclxuXHJcbiAgLy8gQWRkIG5ldyByb3cgaWYgbmVlZGVkXHJcbiAgaWYgKHJvd0luZGV4ID09IG9yZ2FuaXphdGlvbi5yb3dzLmxlbmd0aCkge1xyXG4gICAgdmFyIHNlY29uZERpbWVuc2lvbiA9IFtdO1xyXG5cclxuICAgIG9yZ2FuaXphdGlvbi5yb3dzLnB1c2goc2Vjb25kRGltZW5zaW9uKTtcclxuICAgIG9yZ2FuaXphdGlvbi5yb3dXaWR0aC5wdXNoKG1pbkNvbXBvdW5kU2l6ZSk7XHJcbiAgICBvcmdhbml6YXRpb24ucm93SGVpZ2h0LnB1c2goMCk7XHJcbiAgfVxyXG5cclxuICAvLyBVcGRhdGUgcm93IHdpZHRoXHJcbiAgdmFyIHcgPSBvcmdhbml6YXRpb24ucm93V2lkdGhbcm93SW5kZXhdICsgbm9kZS5yZWN0LndpZHRoO1xyXG5cclxuICBpZiAob3JnYW5pemF0aW9uLnJvd3Nbcm93SW5kZXhdLmxlbmd0aCA+IDApIHtcclxuICAgIHcgKz0gb3JnYW5pemF0aW9uLmhvcml6b250YWxQYWRkaW5nO1xyXG4gIH1cclxuXHJcbiAgb3JnYW5pemF0aW9uLnJvd1dpZHRoW3Jvd0luZGV4XSA9IHc7XHJcbiAgLy8gVXBkYXRlIGNvbXBvdW5kIHdpZHRoXHJcbiAgaWYgKG9yZ2FuaXphdGlvbi53aWR0aCA8IHcpIHtcclxuICAgIG9yZ2FuaXphdGlvbi53aWR0aCA9IHc7XHJcbiAgfVxyXG5cclxuICAvLyBVcGRhdGUgaGVpZ2h0XHJcbiAgdmFyIGggPSBub2RlLnJlY3QuaGVpZ2h0O1xyXG4gIGlmIChyb3dJbmRleCA+IDApXHJcbiAgICBoICs9IG9yZ2FuaXphdGlvbi52ZXJ0aWNhbFBhZGRpbmc7XHJcblxyXG4gIHZhciBleHRyYUhlaWdodCA9IDA7XHJcbiAgaWYgKGggPiBvcmdhbml6YXRpb24ucm93SGVpZ2h0W3Jvd0luZGV4XSkge1xyXG4gICAgZXh0cmFIZWlnaHQgPSBvcmdhbml6YXRpb24ucm93SGVpZ2h0W3Jvd0luZGV4XTtcclxuICAgIG9yZ2FuaXphdGlvbi5yb3dIZWlnaHRbcm93SW5kZXhdID0gaDtcclxuICAgIGV4dHJhSGVpZ2h0ID0gb3JnYW5pemF0aW9uLnJvd0hlaWdodFtyb3dJbmRleF0gLSBleHRyYUhlaWdodDtcclxuICB9XHJcblxyXG4gIG9yZ2FuaXphdGlvbi5oZWlnaHQgKz0gZXh0cmFIZWlnaHQ7XHJcblxyXG4gIC8vIEluc2VydCBub2RlXHJcbiAgb3JnYW5pemF0aW9uLnJvd3Nbcm93SW5kZXhdLnB1c2gobm9kZSk7XHJcbn07XHJcblxyXG4vL1NjYW5zIHRoZSByb3dzIG9mIGFuIG9yZ2FuaXphdGlvbiBhbmQgcmV0dXJucyB0aGUgb25lIHdpdGggdGhlIG1pbiB3aWR0aFxyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuZ2V0U2hvcnRlc3RSb3dJbmRleCA9IGZ1bmN0aW9uIChvcmdhbml6YXRpb24pIHtcclxuICB2YXIgciA9IC0xO1xyXG4gIHZhciBtaW4gPSBOdW1iZXIuTUFYX1ZBTFVFO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IG9yZ2FuaXphdGlvbi5yb3dzLmxlbmd0aDsgaSsrKSB7XHJcbiAgICBpZiAob3JnYW5pemF0aW9uLnJvd1dpZHRoW2ldIDwgbWluKSB7XHJcbiAgICAgIHIgPSBpO1xyXG4gICAgICBtaW4gPSBvcmdhbml6YXRpb24ucm93V2lkdGhbaV07XHJcbiAgICB9XHJcbiAgfVxyXG4gIHJldHVybiByO1xyXG59O1xyXG5cclxuLy9TY2FucyB0aGUgcm93cyBvZiBhbiBvcmdhbml6YXRpb24gYW5kIHJldHVybnMgdGhlIG9uZSB3aXRoIHRoZSBtYXggd2lkdGhcclxuX0NvU0VMYXlvdXQucHJvdG90eXBlLmdldExvbmdlc3RSb3dJbmRleCA9IGZ1bmN0aW9uIChvcmdhbml6YXRpb24pIHtcclxuICB2YXIgciA9IC0xO1xyXG4gIHZhciBtYXggPSBOdW1iZXIuTUlOX1ZBTFVFO1xyXG5cclxuICBmb3IgKHZhciBpID0gMDsgaSA8IG9yZ2FuaXphdGlvbi5yb3dzLmxlbmd0aDsgaSsrKSB7XHJcblxyXG4gICAgaWYgKG9yZ2FuaXphdGlvbi5yb3dXaWR0aFtpXSA+IG1heCkge1xyXG4gICAgICByID0gaTtcclxuICAgICAgbWF4ID0gb3JnYW5pemF0aW9uLnJvd1dpZHRoW2ldO1xyXG4gICAgfVxyXG4gIH1cclxuXHJcbiAgcmV0dXJuIHI7XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBtZXRob2QgY2hlY2tzIHdoZXRoZXIgYWRkaW5nIGV4dHJhIHdpZHRoIHRvIHRoZSBvcmdhbml6YXRpb24gdmlvbGF0ZXNcclxuICogdGhlIGFzcGVjdCByYXRpbygxKSBvciBub3QuXHJcbiAqL1xyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuY2FuQWRkSG9yaXpvbnRhbCA9IGZ1bmN0aW9uIChvcmdhbml6YXRpb24sIGV4dHJhV2lkdGgsIGV4dHJhSGVpZ2h0KSB7XHJcblxyXG4gIHZhciBzcmkgPSB0aGlzLmdldFNob3J0ZXN0Um93SW5kZXgob3JnYW5pemF0aW9uKTtcclxuXHJcbiAgaWYgKHNyaSA8IDApIHtcclxuICAgIHJldHVybiB0cnVlO1xyXG4gIH1cclxuXHJcbiAgdmFyIG1pbiA9IG9yZ2FuaXphdGlvbi5yb3dXaWR0aFtzcmldO1xyXG5cclxuICBpZiAobWluICsgb3JnYW5pemF0aW9uLmhvcml6b250YWxQYWRkaW5nICsgZXh0cmFXaWR0aCA8PSBvcmdhbml6YXRpb24ud2lkdGgpXHJcbiAgICByZXR1cm4gdHJ1ZTtcclxuXHJcbiAgdmFyIGhEaWZmID0gMDtcclxuXHJcbiAgLy8gQWRkaW5nIHRvIGFuIGV4aXN0aW5nIHJvd1xyXG4gIGlmIChvcmdhbml6YXRpb24ucm93SGVpZ2h0W3NyaV0gPCBleHRyYUhlaWdodCkge1xyXG4gICAgaWYgKHNyaSA+IDApXHJcbiAgICAgIGhEaWZmID0gZXh0cmFIZWlnaHQgKyBvcmdhbml6YXRpb24udmVydGljYWxQYWRkaW5nIC0gb3JnYW5pemF0aW9uLnJvd0hlaWdodFtzcmldO1xyXG4gIH1cclxuXHJcbiAgdmFyIGFkZF90b19yb3dfcmF0aW87XHJcbiAgaWYgKG9yZ2FuaXphdGlvbi53aWR0aCAtIG1pbiA+PSBleHRyYVdpZHRoICsgb3JnYW5pemF0aW9uLmhvcml6b250YWxQYWRkaW5nKSB7XHJcbiAgICBhZGRfdG9fcm93X3JhdGlvID0gKG9yZ2FuaXphdGlvbi5oZWlnaHQgKyBoRGlmZikgLyAobWluICsgZXh0cmFXaWR0aCArIG9yZ2FuaXphdGlvbi5ob3Jpem9udGFsUGFkZGluZyk7XHJcbiAgfSBlbHNlIHtcclxuICAgIGFkZF90b19yb3dfcmF0aW8gPSAob3JnYW5pemF0aW9uLmhlaWdodCArIGhEaWZmKSAvIG9yZ2FuaXphdGlvbi53aWR0aDtcclxuICB9XHJcblxyXG4gIC8vIEFkZGluZyBhIG5ldyByb3cgZm9yIHRoaXMgbm9kZVxyXG4gIGhEaWZmID0gZXh0cmFIZWlnaHQgKyBvcmdhbml6YXRpb24udmVydGljYWxQYWRkaW5nO1xyXG4gIHZhciBhZGRfbmV3X3Jvd19yYXRpbztcclxuICBpZiAob3JnYW5pemF0aW9uLndpZHRoIDwgZXh0cmFXaWR0aCkge1xyXG4gICAgYWRkX25ld19yb3dfcmF0aW8gPSAob3JnYW5pemF0aW9uLmhlaWdodCArIGhEaWZmKSAvIGV4dHJhV2lkdGg7XHJcbiAgfSBlbHNlIHtcclxuICAgIGFkZF9uZXdfcm93X3JhdGlvID0gKG9yZ2FuaXphdGlvbi5oZWlnaHQgKyBoRGlmZikgLyBvcmdhbml6YXRpb24ud2lkdGg7XHJcbiAgfVxyXG5cclxuICBpZiAoYWRkX25ld19yb3dfcmF0aW8gPCAxKVxyXG4gICAgYWRkX25ld19yb3dfcmF0aW8gPSAxIC8gYWRkX25ld19yb3dfcmF0aW87XHJcblxyXG4gIGlmIChhZGRfdG9fcm93X3JhdGlvIDwgMSlcclxuICAgIGFkZF90b19yb3dfcmF0aW8gPSAxIC8gYWRkX3RvX3Jvd19yYXRpbztcclxuXHJcbiAgcmV0dXJuIGFkZF90b19yb3dfcmF0aW8gPCBhZGRfbmV3X3Jvd19yYXRpbztcclxufTtcclxuXHJcblxyXG4vL0lmIG1vdmluZyB0aGUgbGFzdCBub2RlIGZyb20gdGhlIGxvbmdlc3Qgcm93IGFuZCBhZGRpbmcgaXQgdG8gdGhlIGxhc3RcclxuLy9yb3cgbWFrZXMgdGhlIGJvdW5kaW5nIGJveCBzbWFsbGVyLCBkbyBpdC5cclxuX0NvU0VMYXlvdXQucHJvdG90eXBlLnNoaWZ0VG9MYXN0Um93ID0gZnVuY3Rpb24gKG9yZ2FuaXphdGlvbikge1xyXG4gIHZhciBsb25nZXN0ID0gdGhpcy5nZXRMb25nZXN0Um93SW5kZXgob3JnYW5pemF0aW9uKTtcclxuICB2YXIgbGFzdCA9IG9yZ2FuaXphdGlvbi5yb3dXaWR0aC5sZW5ndGggLSAxO1xyXG4gIHZhciByb3cgPSBvcmdhbml6YXRpb24ucm93c1tsb25nZXN0XTtcclxuICB2YXIgbm9kZSA9IHJvd1tyb3cubGVuZ3RoIC0gMV07XHJcblxyXG4gIHZhciBkaWZmID0gbm9kZS53aWR0aCArIG9yZ2FuaXphdGlvbi5ob3Jpem9udGFsUGFkZGluZztcclxuXHJcbiAgLy8gQ2hlY2sgaWYgdGhlcmUgaXMgZW5vdWdoIHNwYWNlIG9uIHRoZSBsYXN0IHJvd1xyXG4gIGlmIChvcmdhbml6YXRpb24ud2lkdGggLSBvcmdhbml6YXRpb24ucm93V2lkdGhbbGFzdF0gPiBkaWZmICYmIGxvbmdlc3QgIT0gbGFzdCkge1xyXG4gICAgLy8gUmVtb3ZlIHRoZSBsYXN0IGVsZW1lbnQgb2YgdGhlIGxvbmdlc3Qgcm93XHJcbiAgICByb3cuc3BsaWNlKC0xLCAxKTtcclxuXHJcbiAgICAvLyBQdXNoIGl0IHRvIHRoZSBsYXN0IHJvd1xyXG4gICAgb3JnYW5pemF0aW9uLnJvd3NbbGFzdF0ucHVzaChub2RlKTtcclxuXHJcbiAgICBvcmdhbml6YXRpb24ucm93V2lkdGhbbG9uZ2VzdF0gPSBvcmdhbml6YXRpb24ucm93V2lkdGhbbG9uZ2VzdF0gLSBkaWZmO1xyXG4gICAgb3JnYW5pemF0aW9uLnJvd1dpZHRoW2xhc3RdID0gb3JnYW5pemF0aW9uLnJvd1dpZHRoW2xhc3RdICsgZGlmZjtcclxuICAgIG9yZ2FuaXphdGlvbi53aWR0aCA9IG9yZ2FuaXphdGlvbi5yb3dXaWR0aFt0aGlzLmdldExvbmdlc3RSb3dJbmRleChvcmdhbml6YXRpb24pXTtcclxuXHJcbiAgICAvLyBVcGRhdGUgaGVpZ2h0cyBvZiB0aGUgb3JnYW5pemF0aW9uXHJcbiAgICB2YXIgbWF4SGVpZ2h0ID0gTnVtYmVyLk1JTl9WQUxVRTtcclxuICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcm93Lmxlbmd0aDsgaSsrKSB7XHJcbiAgICAgIGlmIChyb3dbaV0uaGVpZ2h0ID4gbWF4SGVpZ2h0KVxyXG4gICAgICAgIG1heEhlaWdodCA9IHJvd1tpXS5oZWlnaHQ7XHJcbiAgICB9XHJcbiAgICBpZiAobG9uZ2VzdCA+IDApXHJcbiAgICAgIG1heEhlaWdodCArPSBvcmdhbml6YXRpb24udmVydGljYWxQYWRkaW5nO1xyXG5cclxuICAgIHZhciBwcmV2VG90YWwgPSBvcmdhbml6YXRpb24ucm93SGVpZ2h0W2xvbmdlc3RdICsgb3JnYW5pemF0aW9uLnJvd0hlaWdodFtsYXN0XTtcclxuXHJcbiAgICBvcmdhbml6YXRpb24ucm93SGVpZ2h0W2xvbmdlc3RdID0gbWF4SGVpZ2h0O1xyXG4gICAgaWYgKG9yZ2FuaXphdGlvbi5yb3dIZWlnaHRbbGFzdF0gPCBub2RlLmhlaWdodCArIG9yZ2FuaXphdGlvbi52ZXJ0aWNhbFBhZGRpbmcpXHJcbiAgICAgIG9yZ2FuaXphdGlvbi5yb3dIZWlnaHRbbGFzdF0gPSBub2RlLmhlaWdodCArIG9yZ2FuaXphdGlvbi52ZXJ0aWNhbFBhZGRpbmc7XHJcblxyXG4gICAgdmFyIGZpbmFsVG90YWwgPSBvcmdhbml6YXRpb24ucm93SGVpZ2h0W2xvbmdlc3RdICsgb3JnYW5pemF0aW9uLnJvd0hlaWdodFtsYXN0XTtcclxuICAgIG9yZ2FuaXphdGlvbi5oZWlnaHQgKz0gKGZpbmFsVG90YWwgLSBwcmV2VG90YWwpO1xyXG5cclxuICAgIHRoaXMuc2hpZnRUb0xhc3RSb3cob3JnYW5pemF0aW9uKTtcclxuICB9XHJcbn07XHJcblxyXG4vKipcclxuICogQGJyaWVmIDogY2FsbGVkIG9uIGNvbnRpbnVvdXMgbGF5b3V0cyB0byBzdG9wIHRoZW0gYmVmb3JlIHRoZXkgZmluaXNoXHJcbiAqL1xyXG5fQ29TRUxheW91dC5wcm90b3R5cGUuc3RvcCA9IGZ1bmN0aW9uICgpIHtcclxuICB0aGlzLnN0b3BwZWQgPSB0cnVlO1xyXG5cclxuICByZXR1cm4gdGhpczsgLy8gY2hhaW5pbmdcclxufTtcclxuXHJcbl9Db1NFTGF5b3V0LnByb3RvdHlwZS5wcm9jZXNzQ2hpbGRyZW5MaXN0ID0gZnVuY3Rpb24gKHBhcmVudCwgY2hpbGRyZW4pIHtcclxuICB2YXIgc2l6ZSA9IGNoaWxkcmVuLmxlbmd0aDtcclxuICBmb3IgKHZhciBpID0gMDsgaSA8IHNpemU7IGkrKykge1xyXG4gICAgdmFyIHRoZUNoaWxkID0gY2hpbGRyZW5baV07XHJcbiAgICB0aGlzLm9wdGlvbnMuZWxlcy5ub2RlcygpLmxlbmd0aDtcclxuICAgIHZhciBjaGlsZHJlbl9vZl9jaGlsZHJlbiA9IHRoZUNoaWxkLmNoaWxkcmVuKCk7XHJcbiAgICB2YXIgdGhlTm9kZTtcclxuXHJcbiAgICBpZiAodGhlQ2hpbGQud2lkdGgoKSAhPSBudWxsXHJcbiAgICAgICAgICAgICYmIHRoZUNoaWxkLmhlaWdodCgpICE9IG51bGwpIHtcclxuICAgICAgdGhlTm9kZSA9IHBhcmVudC5hZGQobmV3IENvU0VOb2RlKF9Db1NFTGF5b3V0LmxheW91dC5ncmFwaE1hbmFnZXIsXHJcbiAgICAgICAgICAgICAgbmV3IFBvaW50RCh0aGVDaGlsZC5wb3NpdGlvbigneCcpLCB0aGVDaGlsZC5wb3NpdGlvbigneScpKSxcclxuICAgICAgICAgICAgICBuZXcgRGltZW5zaW9uRChwYXJzZUZsb2F0KHRoZUNoaWxkLndpZHRoKCkpLFxyXG4gICAgICAgICAgICAgICAgICAgICAgcGFyc2VGbG9hdCh0aGVDaGlsZC5oZWlnaHQoKSkpKSk7XHJcbiAgICB9XHJcbiAgICBlbHNlIHtcclxuICAgICAgdGhlTm9kZSA9IHBhcmVudC5hZGQobmV3IENvU0VOb2RlKHRoaXMuZ3JhcGhNYW5hZ2VyKSk7XHJcbiAgICB9XHJcbiAgICB0aGVOb2RlLmlkID0gdGhlQ2hpbGQuZGF0YShcImlkXCIpO1xyXG4gICAgX0NvU0VMYXlvdXQuaWRUb0xOb2RlW3RoZUNoaWxkLmRhdGEoXCJpZFwiKV0gPSB0aGVOb2RlO1xyXG5cclxuICAgIGlmIChpc05hTih0aGVOb2RlLnJlY3QueCkpIHtcclxuICAgICAgdGhlTm9kZS5yZWN0LnggPSAwO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChpc05hTih0aGVOb2RlLnJlY3QueSkpIHtcclxuICAgICAgdGhlTm9kZS5yZWN0LnkgPSAwO1xyXG4gICAgfVxyXG5cclxuICAgIGlmIChjaGlsZHJlbl9vZl9jaGlsZHJlbiAhPSBudWxsICYmIGNoaWxkcmVuX29mX2NoaWxkcmVuLmxlbmd0aCA+IDApIHtcclxuICAgICAgdmFyIHRoZU5ld0dyYXBoO1xyXG4gICAgICB0aGVOZXdHcmFwaCA9IF9Db1NFTGF5b3V0LmxheW91dC5nZXRHcmFwaE1hbmFnZXIoKS5hZGQoX0NvU0VMYXlvdXQubGF5b3V0Lm5ld0dyYXBoKCksIHRoZU5vZGUpO1xyXG4gICAgICB0aGlzLnByb2Nlc3NDaGlsZHJlbkxpc3QodGhlTmV3R3JhcGgsIGNoaWxkcmVuX29mX2NoaWxkcmVuKTtcclxuICAgIH1cclxuICB9XHJcbn07XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uIGdldChjeXRvc2NhcGUpIHtcclxuICBUaHJlYWQgPSBjeXRvc2NhcGUuVGhyZWFkO1xyXG5cclxuICByZXR1cm4gX0NvU0VMYXlvdXQ7XHJcbn07IiwiZnVuY3Rpb24gbGF5b3V0T3B0aW9uc1BhY2soKSB7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gbGF5b3V0T3B0aW9uc1BhY2s7IiwiJ3VzZSBzdHJpY3QnO1xyXG5cclxuKGZ1bmN0aW9uKCl7XHJcblxyXG4gIC8vIHJlZ2lzdGVycyB0aGUgZXh0ZW5zaW9uIG9uIGEgY3l0b3NjYXBlIGxpYiByZWZcclxuICB2YXIgZ2V0TGF5b3V0ID0gcmVxdWlyZSgnLi9MYXlvdXQnKTtcclxuICB2YXIgcmVnaXN0ZXIgPSBmdW5jdGlvbiggY3l0b3NjYXBlICl7XHJcbiAgICB2YXIgTGF5b3V0ID0gZ2V0TGF5b3V0KCBjeXRvc2NhcGUgKTtcclxuXHJcbiAgICBjeXRvc2NhcGUoJ2xheW91dCcsICdjb3NlLWJpbGtlbnQnLCBMYXlvdXQpO1xyXG4gIH07XHJcblxyXG4gIGlmKCB0eXBlb2YgbW9kdWxlICE9PSAndW5kZWZpbmVkJyAmJiBtb2R1bGUuZXhwb3J0cyApeyAvLyBleHBvc2UgYXMgYSBjb21tb25qcyBtb2R1bGVcclxuICAgIG1vZHVsZS5leHBvcnRzID0gcmVnaXN0ZXI7XHJcbiAgfVxyXG5cclxuICBpZiggdHlwZW9mIGRlZmluZSAhPT0gJ3VuZGVmaW5lZCcgJiYgZGVmaW5lLmFtZCApeyAvLyBleHBvc2UgYXMgYW4gYW1kL3JlcXVpcmVqcyBtb2R1bGVcclxuICAgIGRlZmluZSgnY3l0b3NjYXBlLWNvc2UtYmlsa2VudCcsIGZ1bmN0aW9uKCl7XHJcbiAgICAgIHJldHVybiByZWdpc3RlcjtcclxuICAgIH0pO1xyXG4gIH1cclxuXHJcbiAgaWYoIHR5cGVvZiBjeXRvc2NhcGUgIT09ICd1bmRlZmluZWQnICl7IC8vIGV4cG9zZSB0byBnbG9iYWwgY3l0b3NjYXBlIChpLmUuIHdpbmRvdy5jeXRvc2NhcGUpXHJcbiAgICByZWdpc3RlciggY3l0b3NjYXBlICk7XHJcbiAgfVxyXG5cclxufSkoKTtcclxuIl19
