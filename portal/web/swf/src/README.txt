--------------------------------------------------------------------------------
CytoscapeWeb revision 29261 (June 5, 2012 - 11:50 am) was used to base network
visualization component for cBio portal! To reproduce necessary executables
	portal/web/swf/CytoscapeWeb.swf and
	portal/web/js/cytoscape_web/cytoscapeweb.min.js
you need to check this revision and make the changes listed below.
--------------------------------------------------------------------------------

Modified classes
----------------

/html-template/js/cytoscapeweb.js
- method profileDataAlwaysShown added

/org/cytoscapeweb/view/ExternalMediator.as
- wiring necessary to toggle whether or not profile data should always be shown.

/src/org/cytoscapeweb/ApplicationFacade.as
Modification is done on
- use CBioHandleHoverCommand for the mouse rollover and rollout events.
- use ShowProfileDataCommand to toggle whether or not profile data should always be shown.

/src/org/cytoscapeweb/controller/SelectCommand.as
Modification is done between lines 55~60 & 76 gives support to show detail discs of the selected nodes.

/src/org/cytoscapeweb/controller/DeselectCommand.as
Modification is done between lines 55~60 & 76 gives support to hide detail discs of the deselected nodes.

/src/org/cytoscapeweb/util/Nodes.as
Line 39 - CBioNodeRenderer is imported. Line 90 - CBioNodeRenderer is set.

/src/org/cytoscapeweb/util/Edges.as
Line 41 - CBioEdgeRenderer is imported. Line 78 - CBioEdgeRenderer is set.

New classes
-----------

/src/org/cytoscapeweb/view/render/CBioNodeRenderer.as
Extends NodeRenderer, changes render method to be able to draw the detail discs.

/src/org/cytoscapeweb/view/render/CBioEdgeRenderer.as
Extends EdgeRenderer, changes render method to be able to color merged edges.

/src/org/cytoscapeweb/controller/CBioHandleHoverCommand.as
Extends HandleHoverCommand, gives support to show details when a node is highlighted.

/src/org/cytoscapeweb/controller/ShowProfileDataCommand.as
Provides support to enable/disable whether or not profile data should always be shown.
