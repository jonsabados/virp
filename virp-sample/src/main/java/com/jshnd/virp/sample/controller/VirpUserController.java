package com.jshnd.virp.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.sample.VirpSampleWebSession;
import com.jshnd.virp.sample.model.VirpUser;

@Controller
/**
 * Sample controller showing get/save functionality functionality
 */
public class VirpUserController {

	@Autowired
	private VirpSampleWebSession webSession;
	
	@Autowired
	private VirpConfig virpConfig;
	
	@RequestMapping(value = "edit_user.html", method = RequestMethod.GET)
	public String userForm(@ModelAttribute("user") VirpUser user) {
		return "editUser";
	}
	
	@RequestMapping(value = "edit_user.html", method = RequestMethod.POST)
	public String editUser(@ModelAttribute("user") VirpUser user) {
		VirpSession session = virpConfig.newSession();
		// If we wanted to over-ride the default flush mode we could do:
		//     virpConfig.newSession(SessionAttachmentMode.AUTO_FLUSH);
		// and the flush would happen on session close
		try {
			// it would be just as simple to overwrite the row, but for sample purposes
			// we will try to modify an existing record if possible
			VirpUser attachedRecord = session.get(VirpUser.class, user.getEmail());
			if(attachedRecord != null) {
				// this wont work if flush mode is set to NONE
				attachedRecord.setFirstName(user.getFirstName());
				attachedRecord.setLastName(user.getLastName());
				// note, if we were try to set the email address an VirpOperationException
				// would be thrown since @Key properties are immutable for attached objects
			} else {
				session.save(user);
			}
			// this bit is optional if the flush mode is set to AUTO_FLUSH
			session.flush();
			webSession.setCurrentUser(user);
		} finally {
			session.close();
		}
		return "redirect:my_virps.html";
	}
	
}
