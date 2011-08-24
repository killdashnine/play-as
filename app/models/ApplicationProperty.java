package models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.Play.Mode;
import play.db.jpa.Model;

@Entity
@Table(name="application_properties")
public class ApplicationProperty extends Model {

	@ManyToOne(fetch=FetchType.LAZY)
	public Application application;
	
	public String key;
	public String value;
	public Mode mode;
}
