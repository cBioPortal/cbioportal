Modify

ApplicationFacade.as

/src/org/cytoscapeweb/ApplicationFacade.as

Modification is done on lines 151 and 152 to be able to use CBioHandleHoverCommand for the mouse rollover and rollout events.



New Class

CBioNodeRenderer.as

/src/org/cytoscapeweb/view/render/CBioNodeRenderer.as

Extends NodeRenderer, changes render method to be able to draw the detail discs.



New Class

CBioHandleHoverCommand.as

/src/org/cytoscapeweb/controller/CBioHandleHoverCommand.as

Extends HandleHoverCommand, gives support to show details when a node is highlighted.



Modify

SelectCommand.as

/src/org/cytoscapeweb/controller/SelectCommand.as

Modification is done between lines 55~60, gives support to show detail discs of the selected nodes.



Modify

DeselectCommand.as

/src/org/cytoscapeweb/controller/DeselectCommand.a
s
Modification is done between lines 55~60, gives support to hide detail discs of the deselected nodes.



Modify

Nodes.as

/src/org/cytoscapeweb/util/Nodes.as

Line 39 - CBioNodeRenderer is imported. Line 90 - CBioNodeRenderer is set.
