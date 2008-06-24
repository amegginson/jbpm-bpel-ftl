package org.jboss.bam.action.identity;

import javax.faces.application.FacesMessage;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotNull;
import org.jboss.bam.action.identity.cmd.LoadUserByIdCommand;
import org.jboss.bam.action.identity.cmd.UserDeleteCommand;
import org.jboss.bam.action.identity.cmd.UserPersistCommand;
import org.jboss.bam.action.identity.cmd.UserUpdateCommand;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.identity.User;

/**
 * Persistence handler for the user object.
 * 
 * @author Fady Matar
 * 
 */
@Name("userHome")
public class UserHome {

	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	private Long userId;

	private User user;

	@NotNull
	private String name;

	@NotNull
	private String password;

	@NotNull
	private String confirmedPassword;

	@Email
	@NotNull
	private String email;

	private String currentPassword;

	private String newPassword;

	private String confirmedNewPassword;

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setConfirmedPassword(String confirmedPassword) {
		this.confirmedPassword = confirmedPassword;
	}

	public String getConfirmedPassword() {
		return this.confirmedPassword;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getCurrentPassword() {
		return this.currentPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPassword() {
		return this.newPassword;
	}

	public String getConfirmedNewPassword() {
		return this.confirmedNewPassword;
	}

	public void setConfirmedNewPassword(String confirmedNewPassword) {
		this.confirmedNewPassword = confirmedNewPassword;
	}

	public User getInstance() {
		if (getUserId() == null) {
			return new User();
		}
		try {
			LoadUserByIdCommand cmd = new LoadUserByIdCommand();
			cmd.setUserId(this.getUserId());
			User user = (User) cmd.execute(getJbpmContext());
			this.setUser(user);
			this.setName(user.getName());
			this.setEmail(user.getEmail());
			this.setUserId(user.getId());
			return this.getUser();
		} catch (Exception ex) {
			log.debug("Error retrieving user");
			FacesMessages.instance().add(
			    new FacesMessage("Unable to retrieve the selected user"));
			ex.printStackTrace();
			return null;
		}
	}

	public String persist() {
		if (!this.getPassword().equals(this.getConfirmedPassword())) {
			FacesMessages.instance().add(
			    new FacesMessage("The provided passwords do not match."));
			return "failed";
		}
		UserPersistCommand cmd = new UserPersistCommand();
		cmd.setEmail(this.getEmail());
		cmd.setName(this.getName());
		cmd.setPassword(this.getPassword());
		try {
			this.setUser((User) cmd.execute(getJbpmContext()));
			this.setUserId(user.getId());
			this.setName(user.getName());
			this.setEmail(user.getEmail());
			FacesMessages.instance().add(
			    new FacesMessage("Your new user has been added to the system."));
			return "persisted";
		} catch (Exception ex) {
			FacesMessages.instance().add(
			    new FacesMessage("Unable to add new user to the system."));
			ex.printStackTrace();
			return "failed";
		}
	}

	public String update() {
		UserUpdateCommand cmd = new UserUpdateCommand();
		this.getUser().setEmail(this.getEmail());
		cmd.setUser(this.getUser());
		try {
			cmd.execute(getJbpmContext());
			FacesMessages.instance().add(
			    new FacesMessage("The user has been updated."));
			return "updated";
		} catch (Exception ex) {
			ex.printStackTrace();
			FacesMessages.instance().add(
			    new FacesMessage(
			        "An error occured while updating the user information."));
			return "failed";
		}
	}

	public String remove() {
		UserDeleteCommand cmd = new UserDeleteCommand();
		cmd.setUser(this.getUser());
		try {
			cmd.execute(getJbpmContext());
			this.setUser(null);
			this.setEmail(null);
			this.setName(null);
			FacesMessages.instance().add(
			    new FacesMessage("The user has been removed from the system."));
			return "deleted";
		} catch (Exception ex) {
			ex.printStackTrace();
			FacesMessages.instance().add(
			    new FacesMessage("Unable to remove the user from the system."));
			return "failed";
		}
	}

	public String changePassword() {
		// Compare if new passwords match
		if (!this.getNewPassword().equals(this.getConfirmedNewPassword())) {
			FacesMessages.instance().add(
			    new FacesMessage("Newly entered passwords do not match."));
			return "failed";
		}
		// Compare if old password matches the entered current Password
		else if (!this.getInstance().getPassword()
		    .equals(this.getCurrentPassword())) {
			FacesMessages.instance()
			    .add(
			        new FacesMessage(
			            "Your current password does not match your old one"));
			return "failed";
		}
		// Perform the password change command
		else {
			UserUpdateCommand cmd = new UserUpdateCommand();
			this.getUser().setPassword(this.getNewPassword());
			cmd.setUser(this.getUser());
			try {
				cmd.execute(getJbpmContext());
				FacesMessages.instance().add(
				    new FacesMessage(
				        "The user's password has been successfully updated."));
				return "updated";
			} catch (Exception ex) {
				ex.printStackTrace();
				FacesMessages.instance().add(
				    new FacesMessage(
				        "An error occured while updating the user information."));
				return "failed";
			}
		}
	}

	public boolean isManaged() {
		try {
			return getInstance() != null
			    && getJbpmContext().getSession().contains(getUser());
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

}