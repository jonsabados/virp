package com.jshnd.virp.sample.model;

import com.jshnd.virp.annotation.DynamicTimeToLive;
import com.jshnd.virp.annotation.HasDynamicTimeToLive;
import com.jshnd.virp.annotation.Key;
import com.jshnd.virp.annotation.NamedColumn;
import com.jshnd.virp.annotation.RowMapper;
import com.jshnd.virp.annotation.TimeToLive;

@RowMapper(columnFamily = "VirpRecord", defaultTimeToLive = @TimeToLive(seconds = 120))
public class VirpRecord {

	@Key
	// although keys get populated based on row keys they can be columns if you
	// so desire - currently the behavior is undefined if the values don't match though
	@NamedColumn(name = "uuid")
	private String uuid;
	
	// although most everything about a VirpRecord will go away in 120 seconds we will keep the
	// join back to the owner around for a day
	@TimeToLive(seconds = 8640)
	@NamedColumn(name = "owner")
	private String owner;

	// and we will let the user specify how long to hold onto the star rating
	@HasDynamicTimeToLive(identifier = "stars")
	@NamedColumn(name = "stars")
	private Short starRating;
	
	@DynamicTimeToLive(forIdentifier = "stars")
	private int starRatingRetention = 120;
	
	@NamedColumn(name = "notes")
	private String notes;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Short getStarRating() {
		return starRating;
	}

	public void setStarRating(Short starRating) {
		this.starRating = starRating;
	}

	public int getStarRatingRetention() {
		return starRatingRetention;
	}

	public void setStarRatingRetention(int starRatingRetention) {
		this.starRatingRetention = starRatingRetention;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
}
