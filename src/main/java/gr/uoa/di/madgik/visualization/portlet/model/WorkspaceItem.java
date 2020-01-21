package gr.uoa.di.madgik.visualization.portlet.model;

public class WorkspaceItem {
	
	private String name;
	
	private String id;
	
	private String path;

	public WorkspaceItem(String name, String id, String path) {
		super();
		this.name = name;
		this.id = id;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public WorkspaceItem() {
		
	}

}
