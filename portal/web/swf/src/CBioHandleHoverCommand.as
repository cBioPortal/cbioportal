package org.cytoscapeweb.controller
{	
	import flare.vis.data.DataSprite;
	import flare.vis.data.EdgeSprite;
	import flare.vis.data.NodeSprite;
	
	import org.cytoscapeweb.ApplicationFacade;
	import org.cytoscapeweb.model.converters.ExternalObjectConverter;
	import org.cytoscapeweb.util.Edges;
	import org.cytoscapeweb.util.ExternalFunctions;
	import org.cytoscapeweb.util.Groups;
	import org.cytoscapeweb.view.render.CBioNodeRenderer;
	import org.puremvc.as3.interfaces.INotification;

	public class CBioHandleHoverCommand extends HandleHoverCommand
	{
		public function CBioHandleHoverCommand()
		{
			super();
		}
		
		override public function execute(notification:INotification):void {
			super.execute(notification);
			
			var ds:DataSprite = notification.getBody() as DataSprite;
			var action:String = notification.getName();
			var group:String = Groups.groupOf(ds);
			
			var type:String = action === ApplicationFacade.ROLLOVER_EVENT ? "mouseover" : "mouseout";
			var previousDs:DataSprite;
			
			switch (action) {
				case ApplicationFacade.ROLLOVER_EVENT:
					// For the hovered node added prop.detailFlag true to show the details.			
					if (ds is NodeSprite) {
						ds.props.detailFlag = true;
						NodeSprite(ds).visitEdges(function(e:EdgeSprite):Boolean {
							graphMediator.resetDataSprite(e);
							return false;
						});
					} else if (ds is EdgeSprite) {
						
					}
					break;
				case ApplicationFacade.ROLLOUT_EVENT:
					if (ds is NodeSprite) {
						// If the node is selected rollout do not cause details go off
						if(ds.props.$selected)
							ds.props.detailFlag = true;
						else
							ds.props.detailFlag = false;
						NodeSprite(ds).visitEdges(function(e:EdgeSprite):Boolean {
							graphMediator.resetDataSprite(e);
							return false;
						});
					} else if (ds is EdgeSprite) {
						
					}
			}
		}
	}
}