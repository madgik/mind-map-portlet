package gr.uoa.di.madgik.visualization.portlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gcube.common.portal.PortalContext;
import org.gcube.common.storagehub.client.dsl.FileContainer;
import org.gcube.common.storagehub.client.dsl.FolderContainer;
import org.gcube.common.storagehub.client.dsl.StorageHubClient;
import org.gcube.common.storagehub.model.exceptions.StorageHubException;
import org.gcube.common.storagehub.model.items.Item;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.uoa.di.madgik.visualization.portlet.model.WorkspaceItem;

public class MindMapPortlet extends GenericPortlet {

	protected String viewTemplate;

	private static final String MINDMAP_WORKSPACE_FOLDER = "MindMap-Portlet-Folder";
	private static final String MINDMAP_WORKSPACE_FOLDER_DESCRIPTION = "Used to store all the mind maps created through the Mind Map portlet";
	private static final String COMMAND = "cmd";
	private static final String SAVE_TO_WORKSPACE_COMMAND = "saveMindmapToWorkspace";
	private static final String LIST_WORKSPACE_ITEMS_COMMAND = "listItemsFromWorkspace";
	private static final String SELECT_WORKSPACE_ITEM_COMMAND = "selectItemFromWorkspace";

	private static final String NAME_PARAM = "name";
	private static final String DESC_PARAM = "description";
	private static final String PAYLOAD_PARAM = "payload";

	private static Logger logger = LoggerFactory.getLogger(MindMapPortlet.class);

	@Override
	public void init() {
		viewTemplate = getInitParameter("view-template");
	}

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
		logger.debug("Mind map portlet is rendering the main view");
		PortalContext.setUserInSession(renderRequest); //needed only if you have custom servlet that needs to know the current user in your war

		include(viewTemplate, renderRequest, renderResponse);
	}

	@Override
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
		super.processAction(actionRequest, actionResponse);
	}

	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
		PortalContext pContext = PortalContext.getConfiguration();
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(resourceRequest);
		String scope = pContext.getCurrentScope(httpServletRequest);
		logger.info("Serving resource for scope: " + scope);
		String response = null;

		logger.debug("Getting request parameters....");


		String cmd = ParamUtil.getString(resourceRequest,COMMAND);
		if (cmd.equalsIgnoreCase(SAVE_TO_WORKSPACE_COMMAND)) {

			logger.debug("In save mind map method");

			String mindmapPayload = ParamUtil.getString(resourceRequest, PAYLOAD_PARAM);
			String mindmapName = ParamUtil.getString(resourceRequest, NAME_PARAM);
			String mindmapDesc = ParamUtil.getString(resourceRequest, DESC_PARAM);


			logger.debug("Name " + mindmapName);
			logger.debug("Desc " + mindmapDesc);
			logger.debug("Payload " + mindmapPayload);
			JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();

			String mindmapFolderID = null;
			FolderContainer mindmapFolder = null;
			StorageHubClient shc = new StorageHubClient();
			FolderContainer rootContainer = shc.getWSRoot();
			FolderContainer rootFolderContainer;
			try {
				rootFolderContainer = shc.open(rootContainer.getId()).asFolder();
				List<? extends Item> items = rootFolderContainer.list().getItems();
				for (Item item : items) {
					if ( item.getName().equals(MINDMAP_WORKSPACE_FOLDER)) {
						mindmapFolderID = item.getId();
						mindmapFolder = shc.open(mindmapFolderID).asFolder();
						logger.debug("Found MindMap workspace folder with ID -> " + mindmapFolderID);
						break;
					}
				}
				// MindMap folder does not yet exist on workspace
				if (mindmapFolderID == null) {
					logger.debug("MindMap folder does not exist on workspace. Going to create it");
					mindmapFolder = rootContainer.newFolder(MINDMAP_WORKSPACE_FOLDER, MINDMAP_WORKSPACE_FOLDER_DESCRIPTION);
					mindmapFolderID = mindmapFolder.getId();
				}
				
				try(InputStream is = new ByteArrayInputStream(mindmapPayload.getBytes())){
					FileContainer fContainer = mindmapFolder.uploadFile(is, mindmapName, mindmapDesc);
					jsonResponse.put("id", fContainer.getId());
					response = jsonResponse.toString();
					
				} catch (Exception e) {
					resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, 
                            Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
				}
			} catch (StorageHubException e1) {
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, 
                        Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			} catch (Exception e) {
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, 
                        Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			}
		}
		else if (cmd.equalsIgnoreCase(LIST_WORKSPACE_ITEMS_COMMAND)) {
			Gson gson = new Gson();
			List<WorkspaceItem> wItems = new ArrayList<WorkspaceItem>();
			String mindmapFolderID = null;
			StorageHubClient shc = new StorageHubClient();

			FolderContainer rootContainer = shc.getWSRoot();
			FolderContainer rootFolderContainer;
			try {
				rootFolderContainer = shc.open(rootContainer.getId()).asFolder();
				List<? extends Item> items = rootFolderContainer.list().getItems();
				for (Item item : items) {
					if ( item.getName().equals(MINDMAP_WORKSPACE_FOLDER)) {
						mindmapFolderID = item.getId();
						break;
					}
				}
				// MindMap folder does not yet exist on workspace
				if (mindmapFolderID == null) {
					resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
							Integer.toString(HttpServletResponse.SC_NO_CONTENT));
				}

				FolderContainer folderContainer = shc.open(mindmapFolderID).asFolder();
				List<? extends Item> mindmapItems = folderContainer.list().getItems();
				for (Item mmItem : mindmapItems) {
					WorkspaceItem wItem = new WorkspaceItem(mmItem.getName(), mmItem.getId(), mmItem.getPath());
					wItems.add(wItem);
				}
				Type listOfWorkspaceItemObject = new TypeToken<List<WorkspaceItem>>(){}.getType();
				response = gson.toJson(wItems, listOfWorkspaceItemObject);
				
			} catch (StorageHubException e) {
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, 
                        Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			}

			super.serveResource(resourceRequest, resourceResponse);

		}
		else if (cmd.equalsIgnoreCase(SELECT_WORKSPACE_ITEM_COMMAND)) {
			String itemID = ParamUtil.getString(resourceRequest, "id");
			StorageHubClient shc = new StorageHubClient();
			try {
				FileContainer fContainer = shc.open(itemID).asFile();
				InputStream is = fContainer.getPublicLink().openConnection().getInputStream();

				BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
				StringBuffer jsonString = new StringBuffer();
				String line = null;
				while( ( line = reader.readLine() ) != null )  {
					 jsonString.append(line);
				}
				reader.close();
				response = jsonString.toString();
			} catch (StorageHubException e) {
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, 
                        Integer.toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			}
		}
		PrintWriter writer = resourceResponse.getWriter();
		if (response != null)
			writer.println(response);
	}

	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		super.doDispatch(request, response);
	}

	protected void include(String path, RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {

		String url = null;
		if (renderRequest.getParameter("jspPage") == null || renderRequest.getParameter("jspPage").equals("./")) {
			url = path;
		} else {
			url = path + renderRequest.getParameter("jspPage");
		}

		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(url);

		if (portletRequestDispatcher == null) {
			logger.error(url + " is not a valid include");
		} else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}
}
