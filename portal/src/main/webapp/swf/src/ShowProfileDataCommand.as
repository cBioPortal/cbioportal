package org.cytoscapeweb.controller {
	import org.puremvc.as3.interfaces.INotification;
	import org.cytoscapeweb.view.render.CBioNodeRenderer;	
	
	/**
	 * Handle enable and disable of whether profile data should always be shown.
	 */
	public class ShowProfileDataCommand extends BaseSimpleCommand {
		
		override public function execute(notification:INotification):void {
			var show:Boolean = notification.getBody() as Boolean;
			
			if (show != CBioNodeRenderer.instance.detailFlag) {
				CBioNodeRenderer.instance.detailFlag = show;
				graphMediator.updateView();
			}
		}
	}
}