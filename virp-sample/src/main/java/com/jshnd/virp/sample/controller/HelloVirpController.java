package com.jshnd.virp.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.sample.model.VirpUser;

@Controller
public class HelloVirpController {

	@Autowired
	private VirpConfig virpConfig;
	
	@RequestMapping(value = "index.html", method = RequestMethod.GET)
	public String sayHello(ModelMap model) {
		model.put("form", new VirpUser());
		return "helloVirpUser";
	}

	@RequestMapping(value = "index.html", method = RequestMethod.POST)
	public String lookupUser(@ModelAttribute("form") VirpUser input, ModelMap model) {
		VirpSession session = virpConfig.newSession();
		try {
			VirpUser attached = session.get(VirpUser.class, input.getEmail());
			if(attached != null) {
				model.put("user", attached);
				return "redirect:my_virps.html";
			} else {
				model.put("user", input);
				return "redirect:new_user.html";
			}
		} finally {
			session.close();
		}
	}
	
}
