package com.jshnd.virp.sample.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.query.Query;
import com.jshnd.virp.sample.VirpSampleWebSession;
import com.jshnd.virp.sample.model.VirpRecord;

@Controller
public class VirpRecordController {

	@Autowired
	private VirpConfig virpConfig;
	
	@Autowired
	private VirpSampleWebSession webSession;
	
	@RequestMapping(value = "my_virps.html", method = RequestMethod.GET)
	public String listVirps(ModelMap model) {
		if(webSession.getCurrentUser() == null) {
			return "redirect:index.html";
		}
		VirpSession session = virpConfig.newSession();
		try {
			VirpRecord example = new VirpRecord();
			example.setOwner(webSession.getCurrentUser().getEmail());
			Query<VirpRecord> query = session.createByExampleQuery(example);
			List<VirpRecord> allVirps = session.find(query);
			List<VirpRecord> activeVirps = new ArrayList<VirpRecord>();
			int withStars = 0;
			int totalStars = 0;
			for(VirpRecord record : allVirps) {
				if(record.getUuid() != null) {
					activeVirps.add(record);
				}
				if(record.getStarRating() != null) {
					withStars++;
					totalStars += record.getStarRating().shortValue();
				}
			}
			model.put("virpCount", Integer.valueOf(allVirps.size()));
			model.put("activeVirps", activeVirps);
			if(withStars > 0) {
				model.put("averageRating", Double.valueOf((double)totalStars/ (double)totalStars));
			}
		} finally {
			session.close();
		}
		return "virps.jsp";
	}
	
}
