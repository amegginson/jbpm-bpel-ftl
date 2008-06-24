package org.jboss.bam.action.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;

import org.jboss.bam.action.identity.cmd.GroupPersistCommand;
import org.jboss.bam.action.identity.cmd.LoadGroupByIdCommand;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.identity.Group;

/**
 * Persistence handler for the group object.
 * 
 * @author Fady Matar
 * 
 */
@Name("groupHome")
public class GroupHome {

	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	private Long groupId;

	private Group group;

	private String name;

	private String type;

	private List<Group> children = new ArrayList<Group>();

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getGroupId() {
		return this.groupId;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return this.group;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setChildren(List<Group> children) {
		this.children = children;
	}

	public List<Group> getChildren() {
		return this.children;
	}

	public void setInstance(Group instance) {
		this.setGroup(instance);
	}

	@SuppressWarnings("unchecked")
	public Group getInstance() {
		if (this.getGroup() != null) {
			return this.getGroup();
		} else {
			this.setChildren(new ArrayList<Group>());
			try {
				LoadGroupByIdCommand cmd = new LoadGroupByIdCommand();
				cmd.setGroupId(this.getGroupId());
				Group group = (Group) cmd.execute(getJbpmContext());
				if (group.getChildren() != null && !group.getChildren().isEmpty()) {
					log.info("Group contains sub-groups");
					Set<Group> subGroups = group.getChildren();
					for (Group childGroup : subGroups) {
						log.info("Group: [" + childGroup.getId() + ", "
								+ childGroup.getName() + "]");
						this.getChildren().add(childGroup);
					}
				}
				return group;
			} catch (Exception ex) {
				log.error("Error retrieving group with id [" + this.getGroupId() + "]");
				FacesMessages.instance().add(
						new FacesMessage("Unable to retrieve the selected group"));
				ex.printStackTrace();
				return null;
			}
		}
	}

	public boolean isManaged() {
		try {
			return getInstance() != null
					&& getJbpmContext().getSession().contains(this.getInstance());
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private static JbpmContext getJbpmContext() {
		if (ManagedJbpmContext.instance() != null)
			return ManagedJbpmContext.instance().getJbpmConfiguration()
					.getCurrentJbpmContext();
		return null;
	}

	public String persist() {
		log.info("****************************************");
		GroupPersistCommand cmd = new GroupPersistCommand();
		Group group = new Group(this.getName());
		group.setType(this.getType());
		cmd.setGroup(group);
		try {
			this.setInstance((Group) cmd.execute(getJbpmContext()));
			FacesMessages.instance().add(
					new FacesMessage("Your new user has been added to the system."));
			return "persisted";
		} catch (Exception ex) {
			ex.printStackTrace();
			FacesMessages.instance().add(
					new FacesMessage("Unable to add new user to the system."));
			return "failed";
		}
	}

	public String update() {
		return "";
	}

	public String remove() {
		return "";
	}
}